package backend.importer;

import backend.database.MongoDBHandler;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ImportSpeaker – Importiert Abgeordneten-Stammdaten aus der MDB_STAMMDATEN.XML in eine MongoDB-Datenbank.
 * Das erzeugte Dokument hat exakt folgende Struktur:
 * {
 * "_id": "11004097",
 * "name": "Lindner",
 * "firstName": "Christian",
 * "title": "",
 * "geburtsdatum": { "$date": "1979-01-06T23:00:00.000Z" },
 * "geburtsort": "Wuppertal",
 * "sterbedatum": null,
 * "geschlecht": "männlich",
 * "beruf": "Politikwissenschaftler",
 * "akademischertitel": "",
 * "familienstand": "geschieden",
 * "religion": "konfessionslos",
 * "vita": "...",
 * "party": "FDP",
 * "memberships": [
 * {
 * "role": "Ordentliches Mitglied",
 * "member": "11004097",
 * "begin": { "$date": "2021-12-05T23:00:00.000Z" },
 * "end": { "$date": "2025-02-16T14:11:23.636Z" },
 * "label": "Hauptausschuss"
 * },
 * {
 * "member": "11004097",
 * "begin": { "$date": "2021-10-25T22:00:00.000Z" },
 * "end": { "$date": "2025-02-16T14:11:23.636Z" },
 * "label": "Fraktion der Freien Demokratischen Partei"
 * },
 * ... weitere memberships ...
 * ]
 * }
 * <p>
 * Die Logik:
 * - Aus dem XML wird zunächst die ID, die Namensbestandteile (NACHNAME → "name", VORNAME → "firstName")
 * sowie ANREDE_TITEL ("title") und AKAD_TITEL ("akademischertitel") ausgelesen.
 * - Aus den BIOGRAFISCHE_ANGABEN werden geburtsdatum, geburtsort, sterbedatum, geschlecht, beruf,
 * familienstand, religion, vita und party extrahiert.
 * - Für jedes <WAHLPERIODE>-Element wird:
 * a) eine "Party-Membership" (ohne "role") erstellt, deren "begin" und "end" aus MDBWP_VON/BIS geparst werden.
 * b) alle enthaltenen <INSTITUTION>-Elemente werden ausgelesen und jeweils als Membership
 * mit "role" (aus FKT_LANG), "label" (aus INS_LANG) und den entsprechenden Begin-/End-Daten (aus FKTINS_… oder MDBINS_…) eingefügt.
 *
 * @author Philipp Schneider
 */
public class ImportSpeaker {

    public static void main(String[] args) {
        try {
            // Pfad zur XML-Datei
            File xmlFile = new File("src/main/resources/import/MDB_STAMMDATEN.XML");
            FileInputStream fis = new FileInputStream(xmlFile);

            // XML-Parser konfigurieren (DTD-Validierung deaktivieren)
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            dbFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document xmlDoc = dBuilder.parse(fis);
            xmlDoc.getDocumentElement().normalize();

            // Verbindung zu MongoDB herstellen
            MongoDBHandler dbHandler = new MongoDBHandler();
            MongoDatabase database = dbHandler.connect();
            MongoCollection<Document> speakerCollection = dbHandler.getSpeakerCollection();

            // Alle <MDB>-Elemente ermitteln
            NodeList mdbNodes = xmlDoc.getElementsByTagName("MDB");
            System.out.println("Found " + mdbNodes.getLength() + " speaker entries.");

            // Datumsparser (Format: dd.MM.yyyy)
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

            // Iteriere über jeden Abgeordneten
            for (int i = 0; i < mdbNodes.getLength(); i++) {
                Element mdbElement = (Element) mdbNodes.item(i);

                // ID auslesen
                String id = getTagValue("ID", mdbElement);

                // <NAMEN> und das erste <NAME> verarbeiten
                String name = "";
                String firstName = "";
                String title = "";
                String akademischertitel = "";
                NodeList namenList = mdbElement.getElementsByTagName("NAMEN");
                if (namenList.getLength() > 0) {
                    Element namenElement = (Element) namenList.item(0);
                    NodeList nameList = namenElement.getElementsByTagName("NAME");
                    if (nameList.getLength() > 0) {
                        Element nameElement = (Element) nameList.item(0);
                        name = getTagValue("NACHNAME", nameElement);
                        firstName = getTagValue("VORNAME", nameElement);
                        title = getTagValue("ANREDE_TITEL", nameElement);
                        akademischertitel = getTagValue("AKAD_TITEL", nameElement);
                    }
                }

                // BIOGRAFISCHE_ANGABEN auswerten
                Element bioElement = (Element) mdbElement.getElementsByTagName("BIOGRAFISCHE_ANGABEN").item(0);
                String geburtsdatumStr = getTagValue("GEBURTSDATUM", bioElement);
                String geburtsort = getTagValue("GEBURTSORT", bioElement);
                String sterbedatumStr = getTagValue("STERBEDATUM", bioElement);
                String geschlecht = getTagValue("GESCHLECHT", bioElement);
                String familienstand = getTagValue("FAMILIENSTAND", bioElement);
                String religion = getTagValue("RELIGION", bioElement);
                String beruf = getTagValue("BERUF", bioElement);
                String party = getTagValue("PARTEI_KURZ", bioElement);
                String vita = getTagValue("VITA_KURZ", bioElement);

                Date geburtsdatum = null;
                Date sterbedatum = null;
                try {
                    if (!geburtsdatumStr.isEmpty()) {
                        geburtsdatum = sdf.parse(geburtsdatumStr);
                    }
                    if (!sterbedatumStr.isEmpty()) {
                        sterbedatum = sdf.parse(sterbedatumStr);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // memberships-Array aufbauen
                List<Document> memberships = new ArrayList<>();
                // Bestimme den Label für die Parteizugehörigkeit
                String partyLabel;
                if ("FDP".equalsIgnoreCase(party)) {
                    partyLabel = "Fraktion der Freien Demokratischen Partei";
                } else {
                    partyLabel = "Fraktion der " + party;
                }

                // Für jedes <WAHLPERIODE>-Element
                NodeList wpNodes = mdbElement.getElementsByTagName("WAHLPERIODE");
                for (int j = 0; j < wpNodes.getLength(); j++) {
                    Element wpElement = (Element) wpNodes.item(j);

                    // Erzeuge eine Membership für die Parteizugehörigkeit
                    String mdbwpVonStr = getTagValue("MDBWP_VON", wpElement);
                    String mdbwpBisStr = getTagValue("MDBWP_BIS", wpElement);
                    Date mdbwpVon = null;
                    Date mdbwpBis = null;
                    try {
                        if (!mdbwpVonStr.isEmpty()) {
                            mdbwpVon = sdf.parse(mdbwpVonStr);
                        }
                        if (!mdbwpBisStr.isEmpty()) {
                            mdbwpBis = sdf.parse(mdbwpBisStr);
                        } else {
                            mdbwpBis = mdbwpVon;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Document partyMembership = new Document();
                    partyMembership.append("member", id);
                    partyMembership.append("begin", mdbwpVon);
                    partyMembership.append("end", mdbwpBis);
                    partyMembership.append("label", partyLabel);
                    memberships.add(partyMembership);

                    // Verarbeite alle <INSTITUTION>-Elemente innerhalb der WAHLPERIODE
                    NodeList instNodes = wpElement.getElementsByTagName("INSTITUTION");
                    for (int k = 0; k < instNodes.getLength(); k++) {
                        Element instElement = (Element) instNodes.item(k);
                        String role = getTagValue("FKT_LANG", instElement);
                        String insLabel = getTagValue("INS_LANG", instElement);
                        String mdbinsVonStr = getTagValue("MDBINS_VON", instElement);
                        String mdbinsBisStr = getTagValue("MDBINS_BIS", instElement);
                        String fktinsVonStr = getTagValue("FKTINS_VON", instElement);
                        String fktinsBisStr = getTagValue("FKTINS_BIS", instElement);
                        Date beginDate = null;
                        Date endDate = null;
                        try {
                            if (!fktinsVonStr.isEmpty()) {
                                beginDate = sdf.parse(fktinsVonStr);
                            } else if (!mdbinsVonStr.isEmpty()) {
                                beginDate = sdf.parse(mdbinsVonStr);
                            }
                            if (!fktinsBisStr.isEmpty()) {
                                endDate = sdf.parse(fktinsBisStr);
                            } else if (!mdbinsBisStr.isEmpty()) {
                                endDate = sdf.parse(mdbinsBisStr);
                            } else {
                                endDate = beginDate;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Document instMembership = new Document();
                        if (!role.isEmpty()) {
                            instMembership.append("role", role);
                        }
                        instMembership.append("member", id);
                        instMembership.append("begin", beginDate);
                        instMembership.append("end", endDate);
                        instMembership.append("label", insLabel);
                        memberships.add(instMembership);
                    }
                }

                // Erstelle das finale Dokument mit exakt den gewünschten Feldbezeichnungen
                Document speakerDoc = new Document();
                speakerDoc.append("_id", id);
                speakerDoc.append("name", name);
                speakerDoc.append("firstName", firstName);
                speakerDoc.append("title", title);
                speakerDoc.append("geburtsdatum", geburtsdatum);
                speakerDoc.append("geburtsort", geburtsort);
                speakerDoc.append("sterbedatum", sterbedatum);
                speakerDoc.append("geschlecht", geschlecht);
                speakerDoc.append("beruf", beruf);
                speakerDoc.append("akademischertitel", akademischertitel);
                speakerDoc.append("familienstand", familienstand);
                speakerDoc.append("religion", religion);
                speakerDoc.append("vita", vita);
                speakerDoc.append("party", party);
                speakerDoc.append("memberships", memberships);

                // Dokument in die MongoDB einfügen
                speakerCollection.insertOne(speakerDoc);
                System.out.println("Inserted speaker: " + firstName + " " + name);
            }

            System.out.println("Import complete.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Hilfsmethode zum Extrahieren des Textinhalts eines bestimmten Tags aus einem Element.
     *
     * @param tag     Name des gesuchten Tags
     * @param element Übergeordnetes Element
     * @return Textinhalt oder leerer String, falls nicht vorhanden
     */
    private static String getTagValue(String tag, Element element) {
        if (element == null) {
            return "";
        }
        NodeList nodeList = element.getElementsByTagName(tag);
        if (nodeList != null && nodeList.getLength() > 0 && nodeList.item(0) != null) {
            return nodeList.item(0).getTextContent().trim();
        }
        return "";
    }
}
