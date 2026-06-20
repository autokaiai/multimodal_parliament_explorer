package backend.importer;

import backend.database.MongoDBHandler;
import backend.plenarprotocol.PlenarprotocolDAO;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Philipp Schneider #7995354
 * @editor Kai (for scheduling)
 * ImportHandler für Plenarprotokolle des Deutschen Bundestags.
 * Lädt XML-Protokolle von der Bundestags-Website herunter,
 * extrahiert Reden und speichert sie in einer MongoDB-Datenbank.
 * Der Handler kann sowohl für den initialen Import aller verfügbaren
 * Protokolle als auch für tägliche Überprüfungen auf neue Protokolle
 * verwendet werden.
 */

public class ImportHandler {

    // Base URL for the opendata list
    private static final String BASE_URL = "https://www.bundestag.de/ajax/filterlist/de/services/opendata/866354-866354";
    private static final String LIMIT_PARAM = "limit=100&noFilterSet=true";
    private final MongoDBHandler mongoDBHandler; // Your MongoDB handler
    private final int minIndex; // Minimum index for import
    private final int offset; // Starting offset for the first page

    public ImportHandler(int minIndex) throws IOException {
        this.mongoDBHandler = MongoDBHandler.getInstance();
        // Connect to the database
        this.mongoDBHandler.connect();
        // Set the minimum index for the import
        this.minIndex = minIndex;

        // Calculate starting offset to avoid loading unnecessary pages
        // For minIndex 213, we only need the first page (offset 0)
        // For minIndex below 204 (last item on first page), we need to calculate the offset
        // Each page has 10 items
        this.offset = 0; // Always start with the first page as protocols are in descending order
    }

    public static void main(String[] args) {
        try {

            // Hole min index via plenary protocol collection DAO
            PlenarprotocolDAO plenarprotocolDAO = new PlenarprotocolDAO();
            int minIndex = plenarprotocolDAO.getMaxProtocolIndex() + 1;
            // Print min index
            System.out.println("Min index: " + minIndex);

            // Erstelle importer
            ImportHandler importer = new ImportHandler(minIndex);

            // Importiere protokolle hier 22 seiten da 212 protokolle jeweils 10 pro seite, automatisch errechnet
            importer.importAllProtocols();

        } catch (IOException e) {
            System.out.println("Error initializing MongoDB handler: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void importAllProtocols() {
        try {
            int offset = this.offset;
            int totalProcessed = 0;
            boolean hasMorePages = true;
            int pageNum = 0;

            while (hasMorePages) {
                String pageUrl = BASE_URL + "?" + LIMIT_PARAM + "&offset=" + offset;
                System.out.println("Fetching page " + (pageNum + 1) + ": " + pageUrl);

                // Use fully qualified class for JSoup documents
                org.jsoup.nodes.Document htmlDoc = Jsoup.connect(pageUrl)
                        .userAgent("Mozilla/5.0")
                        .timeout(30000) // Increased timeout for larger documents
                        .get();

                Elements xmlLinks = htmlDoc.select("a.bt-link-dokument");
                if (xmlLinks.isEmpty()) {
                    System.out.println("No more XML links found. Finished.");
                    hasMorePages = false;
                    break;
                }

                System.out.println("Found " + xmlLinks.size() + " protocol links on page " + (pageNum + 1));
                Elements titleElements = htmlDoc.select("div.bt-documents-description > p > strong");

                // Process each XML link on the page if the protocol index meets the minimum requirement
                for (int i = 0; i < xmlLinks.size(); i++) {
                    // Extract protocol index from title
                    String title = titleElements.get(i).text();
                    int protocolIndex = extractProtocolIndex(title);

                    // Only process protocols with index >= minIndex
                    if (protocolIndex >= this.minIndex) {
                        String xmlUrl = xmlLinks.get(i).absUrl("href");
                        System.out.println("Processing protocol " + (i + 1) + "/" + xmlLinks.size() +
                                " on page " + (pageNum + 1) + ": " + xmlUrl);

                        int processed = processProtocol(xmlUrl);
                        totalProcessed += processed;
                        System.out.println("Processed " + processed + " speeches from this protocol. " +
                                "Total speeches processed so far: " + totalProcessed);

                        // Small pause to avoid overloading the server
                        Thread.sleep(1000);
                    } else {
                        System.out.println("Skipping protocol with index " + protocolIndex +
                                " (below minimum index " + this.minIndex + ")");
                    }
                }

                offset += 10; // Move to next page
                pageNum++;

                // Short pause between pages
                Thread.sleep(3000);
            }

            System.out.println("Import completed. Total speeches processed: " + totalProcessed);

        } catch (Exception e) {
            System.out.println("Error during import: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Helper method to extract protocol index from title
    private int extractProtocolIndex(String title) {
        // Extract index from titles like "Plenarprotokoll der 213. Sitzung von Donnerstag, dem 13. März 2025"
        try {
            if (title.contains("Plenarprotokoll der ") && title.contains(". Sitzung")) {
                int startIndex = title.indexOf("Plenarprotokoll der ") + "Plenarprotokoll der ".length();
                int endIndex = title.indexOf(". Sitzung");
                String indexStr = title.substring(startIndex, endIndex);
                return Integer.parseInt(indexStr);
            }
        } catch (Exception e) {
            System.out.println("Error extracting protocol index from title: " + title);
        }
        return 0;
    }

    public int processProtocol(String xmlUrl) {
        int processedSpeeches = 0;
        try {
            System.out.println("Downloading protocol from: " + xmlUrl);
            String xmlContent = downloadContent(xmlUrl);
            System.out.println("Downloaded " + xmlContent.length() + " bytes. Now parsing...");

            // Konfiguriere den DocumentBuilderFactory, um DTD-Validierung zu deaktivieren
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // Diese Features deaktivieren die DTD-Validierung und externe Entity-Auflösung
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setValidating(false);
            factory.setFeature("http://xml.org/sax/features/namespaces", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);

            DocumentBuilder builder = factory.newDocumentBuilder();

            // Setze einen EntityResolver, der leere InputSources für externe Entities zurückgibt
            builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));

            InputSource is = new InputSource(new StringReader(xmlContent));
            // Verwende die vollständig qualifizierte Klasse für XML-Dokumente
            org.w3c.dom.Document xmlDoc = builder.parse(is);

            // Extrahiere Protokoll-Metadaten
            Document protocolInfo = extractProtocolInfo(xmlDoc);
            System.out.println("Protocol info extracted: " + protocolInfo.getString("title"));

            // Extrahiere alle Reden
            List<Document> speeches = new ArrayList<>();

            // Verbesserte Suche nach Reden - erst innerhalb von Tagesordnungspunkten
            org.w3c.dom.NodeList topList = xmlDoc.getElementsByTagName("tagesordnungspunkt");
            System.out.println("Found " + topList.getLength() + " agenda items");

            for (int i = 0; i < topList.getLength(); i++) {
                org.w3c.dom.Element topElement = (org.w3c.dom.Element) topList.item(i);
                Document agendaInfo = extractAgendaInfo(topElement);

                // Extrahiere Reden innerhalb dieses Tagesordnungspunkts
                org.w3c.dom.NodeList redeList = topElement.getElementsByTagName("rede");
                System.out.println("Found " + redeList.getLength() + " speeches in agenda item " + (i + 1));

                for (int j = 0; j < redeList.getLength(); j++) {
                    org.w3c.dom.Element redeElement = (org.w3c.dom.Element) redeList.item(j);
                    Document speechDoc = createSpeechDocument(redeElement, protocolInfo, agendaInfo);
                    speeches.add(speechDoc);
                }
            }

            // Suche auch nach Reden außerhalb von Tagesordnungspunkten (im Sitzungsverlauf)
            org.w3c.dom.NodeList sitzungsverlaufList = xmlDoc.getElementsByTagName("sitzungsverlauf");
            if (sitzungsverlaufList.getLength() > 0) {
                org.w3c.dom.Element sitzungsverlaufElement = (org.w3c.dom.Element) sitzungsverlaufList.item(0);
                org.w3c.dom.NodeList directRedeList = sitzungsverlaufElement.getElementsByTagName("rede");

                System.out.println("Found " + directRedeList.getLength() + " speeches directly in sitzungsverlauf");

                // Erstelle eine Standard-Agenda-Info für Reden ohne Tagesordnungspunkt
                Document defaultAgendaInfo = new Document()
                        .append("index", "")
                        .append("id", "allgemeine_aussprache")
                        .append("title", "Allgemeine Aussprache");

                for (int j = 0; j < directRedeList.getLength(); j++) {
                    org.w3c.dom.Element redeElement = (org.w3c.dom.Element) directRedeList.item(j);
                    // Prüfe, ob diese Rede bereits über einen Tagesordnungspunkt erfasst wurde
                    String redeId = redeElement.getAttribute("id");
                    boolean alreadyProcessed = false;

                    for (Document existing : speeches) {
                        if (redeId.equals(existing.getString("_id"))) {
                            alreadyProcessed = true;
                            break;
                        }
                    }

                    if (!alreadyProcessed) {
                        Document speechDoc = createSpeechDocument(redeElement, protocolInfo, defaultAgendaInfo);
                        speeches.add(speechDoc);
                    }
                }
            }

            System.out.println("Total speeches extracted: " + speeches.size());
            processedSpeeches = speeches.size();

            // Speichere in MongoDB, wenn Reden gefunden wurden
            if (!speeches.isEmpty()) {
                MongoCollection<Document> collection = mongoDBHandler.getSpeechCollection();

                // Füge die Dokumente eines nach dem anderen ein, um Duplikate zu vermeiden
                for (Document speech : speeches) {
                    try {
                        // Prüfe, ob bereits ein Dokument mit dieser ID existiert
                        String id = speech.getString("_id");
                        if (id != null && !id.isEmpty()) {
                            Document existingDoc = collection.find(new Document("_id", id)).first();

                            if (existingDoc == null) {
                                // Dokument existiert noch nicht, also einfügen
                                collection.insertOne(speech);
                            } else {
                                System.out.println("Speech with ID " + id + " already exists. Skipping.");
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Error inserting speech: " + e.getMessage());
                    }
                }

                System.out.println("Inserted speeches into MongoDB");
            }

        } catch (Exception e) {
            System.out.println("Error processing protocol " + xmlUrl + ": " + e.getMessage());
            e.printStackTrace();
        }

        return processedSpeeches;
    }

    private Document extractProtocolInfo(org.w3c.dom.Document xmlDoc) {
        Document protocol = new Document();

        try {
            // Extract kopfdaten information
            org.w3c.dom.Element kopfdatenElement = (org.w3c.dom.Element) xmlDoc.getElementsByTagName("kopfdaten").item(0);

            // Extract plenarprotokoll-nummer
            org.w3c.dom.Element plenarprotokollNummerElement = (org.w3c.dom.Element) kopfdatenElement.getElementsByTagName("plenarprotokoll-nummer").item(0);
            org.w3c.dom.Element wahlperiodeElement = null;
            org.w3c.dom.Element sitzungsnrElement = null;

            // Prüfen, ob die neuen Unterelemente existieren
            org.w3c.dom.NodeList wahlperiodeList = plenarprotokollNummerElement.getElementsByTagName("wahlperiode");
            org.w3c.dom.NodeList sitzungsnrList = plenarprotokollNummerElement.getElementsByTagName("sitzungsnr");

            if (wahlperiodeList.getLength() > 0 && sitzungsnrList.getLength() > 0) {
                wahlperiodeElement = (org.w3c.dom.Element) wahlperiodeList.item(0);
                sitzungsnrElement = (org.w3c.dom.Element) sitzungsnrList.item(0);
            }

            // Extract location und date information
            org.w3c.dom.Element veranstaltungsdatenElement = (org.w3c.dom.Element) kopfdatenElement.getElementsByTagName("veranstaltungsdaten").item(0);
            org.w3c.dom.Element ortElement = (org.w3c.dom.Element) veranstaltungsdatenElement.getElementsByTagName("ort").item(0);
            org.w3c.dom.Element datumElement = (org.w3c.dom.Element) veranstaltungsdatenElement.getElementsByTagName("datum").item(0);

            // Verwende das date-Attribut aus dem datum-Element
            String dateAttributeValue = datumElement.getAttribute("date");

            // Extract session times
            org.w3c.dom.NodeList sitzungsbeginnList = xmlDoc.getElementsByTagName("sitzungsbeginn");
            org.w3c.dom.NodeList sitzungsendeList = xmlDoc.getElementsByTagName("sitzungsende");

            // Parsen der Werte
            int wahlperiode = 0;
            int sitzungsnr = 0;

            if (wahlperiodeElement != null && sitzungsnrElement != null) {
                wahlperiode = Integer.parseInt(wahlperiodeElement.getTextContent().trim());
                sitzungsnr = Integer.parseInt(sitzungsnrElement.getTextContent().trim());
            } else {
                // Fallback: Versuche die Werte aus dem Text zu extrahieren
                String protokollNummerText = plenarprotokollNummerElement.getTextContent().trim();
                Pattern pattern = Pattern.compile("(\\d+)\\s*/\\s*(\\d+)");
                Matcher matcher = pattern.matcher(protokollNummerText);
                if (matcher.find()) {
                    wahlperiode = Integer.parseInt(matcher.group(1));
                    sitzungsnr = Integer.parseInt(matcher.group(2));
                }
            }

            String ort = ortElement.getTextContent().trim();

            // Parse Datum aus dem date-Attribut im Format "DD.MM.YYYY"
            long dateMillis = 0;
            if (dateAttributeValue != null && !dateAttributeValue.isEmpty()) {
                try {
                    String[] dateParts = dateAttributeValue.split("\\.");
                    if (dateParts.length == 3) {
                        int day = Integer.parseInt(dateParts[0]);
                        int month = Integer.parseInt(dateParts[1]) - 1; // Monate sind 0-basiert
                        int year = Integer.parseInt(dateParts[2]);

                        Calendar calendar = Calendar.getInstance();
                        calendar.set(year, month, day, 0, 0, 0);
                        calendar.set(Calendar.MILLISECOND, 0);

                        dateMillis = calendar.getTimeInMillis();
                        System.out.println("Parsed date attribute: " + dateAttributeValue + " to " + dateMillis);
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing date attribute: " + e.getMessage());
                    // Fallback: Weiterhin versuchen, aus dem Textinhalt zu parsen
                    dateMillis = parseGermanDate(datumElement.getTextContent().trim());
                }
            } else {
                // Fallback zur alten Methode
                dateMillis = parseGermanDate(datumElement.getTextContent().trim());
            }

            // Parse Sitzungszeiten
            long startTimeMillis = dateMillis + 9 * 3600 * 1000; // Default: 9:00 Uhr
            long endTimeMillis = dateMillis + 18 * 3600 * 1000;  // Default: 18:00 Uhr

            if (sitzungsbeginnList.getLength() > 0) {
                org.w3c.dom.Element sitzungsbeginnElement = (org.w3c.dom.Element) sitzungsbeginnList.item(0);
                String startTimeStr = sitzungsbeginnElement.getAttribute("sitzung-start-uhrzeit");
                if (startTimeStr != null && !startTimeStr.isEmpty()) {
                    startTimeMillis = parseTimeToMillis(startTimeStr, dateMillis);
                }
            }

            if (sitzungsendeList.getLength() > 0) {
                org.w3c.dom.Element sitzungsendeElement = (org.w3c.dom.Element) sitzungsendeList.item(0);
                String endTimeStr = sitzungsendeElement.getAttribute("sitzung-ende-uhrzeit");
                if (endTimeStr != null && !endTimeStr.isEmpty()) {
                    endTimeMillis = parseTimeToMillis(endTimeStr, dateMillis);
                }
            }

            // Setzen der Werte im Protokoll-Dokument
            protocol.append("date", dateMillis);
            protocol.append("starttime", startTimeMillis);
            protocol.append("endtime", endTimeMillis);
            protocol.append("index", sitzungsnr);
            protocol.append("title", "Plenarprotokoll " + wahlperiode + "/" + sitzungsnr);
            protocol.append("place", ort);
            protocol.append("wp", wahlperiode);

        } catch (Exception e) {
            System.out.println("Error extracting protocol info: " + e.getMessage());
            e.printStackTrace();
        }

        return protocol;
    }

    private Document extractAgendaInfo(org.w3c.dom.Element topElement) {
        Document agenda = new Document();

        try {
            String topId = topElement.getAttribute("top-id");
            org.w3c.dom.NodeList pElements = topElement.getElementsByTagName("p");
            String title = "Befragung der Bundesregierung"; // Default title

            // Extrahiere den Titel
            if (pElements.getLength() > 0) {
                org.w3c.dom.Element firstP = (org.w3c.dom.Element) pElements.item(0);
                if (firstP.getTextContent() != null && !firstP.getTextContent().trim().isEmpty()) {
                    title = firstP.getTextContent().trim();
                }
            }

            String id = topId.replaceAll("\\s+", "_") + title.replaceAll("\\s+", "_");

            agenda.append("index", topId);
            agenda.append("id", id);
            agenda.append("title", title);

        } catch (Exception e) {
            System.out.println("Error extracting agenda info: " + e.getMessage());
            e.printStackTrace();
        }

        return agenda;
    }

    private Document createSpeechDocument(org.w3c.dom.Element redeElement, Document protocolInfo, Document agendaInfo) {
        Document speechDoc = new Document();

        try {
            String redeId = redeElement.getAttribute("id");
            speechDoc.append("_id", redeId);

            // Extrahiere Redner infos
            String speakerId = extractSpeakerId(redeElement);
            speechDoc.append("speaker", speakerId);

            // Extrahiere volltext
            String fullText = extractSpeechText(redeElement);
            speechDoc.append("text", fullText);

            // Extrahiere textContent array
            List<Document> textContentList = extractTextContent(redeElement, speakerId);
            speechDoc.append("textContent", textContentList);

            // Füge protocol und agenda info hinzu
            speechDoc.append("protocol", protocolInfo);
            speechDoc.append("agenda", agendaInfo);

        } catch (Exception e) {
            System.out.println("Error creating speech document: " + e.getMessage());
            e.printStackTrace();
        }

        return speechDoc;
    }

    private String extractSpeakerId(org.w3c.dom.Element redeElement) {
        String speakerId = "";

        try {
            org.w3c.dom.NodeList pElements = redeElement.getElementsByTagName("p");

            // Finde den paragraphen "redner"
            for (int i = 0; i < pElements.getLength(); i++) {
                org.w3c.dom.Element pElement = (org.w3c.dom.Element) pElements.item(i);
                if ("redner".equals(pElement.getAttribute("klasse"))) {
                    org.w3c.dom.NodeList rednerElements = pElement.getElementsByTagName("redner");
                    if (rednerElements.getLength() > 0) {
                        org.w3c.dom.Element rednerElement = (org.w3c.dom.Element) rednerElements.item(0);
                        speakerId = rednerElement.getAttribute("id");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Error extracting speaker ID: " + e.getMessage());
        }

        return speakerId;
    }

    private List<Document> extractTextContent(org.w3c.dom.Element redeElement, String speakerId) {
        List<Document> textContentList = new ArrayList<>();

        try {
            // Extrahiere alle Kindelemente in der Rede
            org.w3c.dom.NodeList childNodes = redeElement.getChildNodes();
            String currentSpeakerId = speakerId; // Track the current active speaker
            boolean skipNextParagraph = false; // Flag zum Überspringen des nächsten Paragraphen (nach Präsidenten-Name)

            // Verarbeite die Elemente in der Reihenfolge ihres Auftretens
            for (int i = 0; i < childNodes.getLength(); i++) {
                org.w3c.dom.Node node = childNodes.item(i);

                // Überspringe Text-Nodes und nicht-Element-Nodes
                if (node.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                    continue;
                }

                org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                String tagName = element.getTagName();

                if ("name".equals(tagName)) {
                    // Prüfe, ob es sich um die Präsidentin oder den Vizepräsidenten handelt
                    String nameText = element.getTextContent();
                    if (nameText != null && (nameText.contains("Präsident") || nameText.contains("präsident"))) {
                        skipNextParagraph = true; // Überspringe den nächsten Paragraphen
                        continue; // Überspringe dieses Name-Element
                    }

                    // Normales Name-Element (nicht Präsident/in)
                    if (element.getTextContent() != null && !element.getTextContent().trim().isEmpty()) {
                        Document nameContent = new Document()
                                .append("id", redeElement.getAttribute("id") + "--" + generateRandomId())
                                .append("speaker", currentSpeakerId)
                                .append("text", element.getTextContent().trim() + ":")
                                .append("type", "text");

                        textContentList.add(nameContent);
                    }
                } else if ("p".equals(tagName)) {
                    // Wenn der vorherige Flag gesetzt ist, überspringe diesen Paragraphen
                    if (skipNextParagraph) {
                        skipNextParagraph = false; // Zurücksetzen des Flags
                        continue;
                    }

                    // Check if this is a new speaker paragraph
                    if ("redner".equals(element.getAttribute("klasse"))) {
                        // Extract the speaker ID from this paragraph
                        org.w3c.dom.NodeList rednerElements = element.getElementsByTagName("redner");
                        if (rednerElements.getLength() > 0) {
                            org.w3c.dom.Element rednerElement = (org.w3c.dom.Element) rednerElements.item(0);
                            String newSpeakerId = rednerElement.getAttribute("id");
                            if (newSpeakerId != null && !newSpeakerId.isEmpty()) {
                                currentSpeakerId = newSpeakerId;
                            }
                        }
                        continue;
                    }

                    // Füge den Paragraph als Text-Inhalt hinzu
                    if (element.getTextContent() != null && !element.getTextContent().trim().isEmpty()) {
                        Document textContent = new Document()
                                .append("id", redeElement.getAttribute("id") + "--" + generateRandomId())
                                .append("speaker", currentSpeakerId)
                                .append("text", element.getTextContent().trim())
                                .append("type", "text");

                        textContentList.add(textContent);
                    }
                } else if ("kommentar".equals(tagName)) {
                    // Füge den Kommentar als Comment-Inhalt hinzu
                    if (element.getTextContent() != null && !element.getTextContent().trim().isEmpty()) {
                        Document commentContent = new Document()
                                .append("id", redeElement.getAttribute("id") + "--" + generateRandomId())
                                .append("speaker", currentSpeakerId) // Use current speaker for comment
                                .append("text", element.getTextContent().trim()) // Umschließe Kommentare mit Klammern
                                .append("type", "comment");

                        textContentList.add(commentContent);
                    }
                }

                // Rekursiv in verschachtelte Elemente eintauchen
                if (element.hasChildNodes()) {
                    extractNestedTextContent(element, currentSpeakerId, redeElement.getAttribute("id"), textContentList);
                }
            }
        } catch (Exception e) {
            System.out.println("Error extracting text content: " + e.getMessage());
            e.printStackTrace();
        }

        return textContentList;
    }

    private void extractNestedTextContent(org.w3c.dom.Element parentElement, String speakerId,
                                          String redeId, List<Document> textContentList) {
    }

    private String extractSpeechText(org.w3c.dom.Element redeElement) {
        StringBuilder text = new StringBuilder();
        String mainSpeakerId = extractSpeakerId(redeElement); // ID des Hauptredners
        boolean skipNextParagraph = false; // Flag zum Überspringen des nächsten Paragraphen

        try {
            // Extrahiere alle direkten Kindelemente
            org.w3c.dom.NodeList childNodes = redeElement.getChildNodes();

            for (int i = 0; i < childNodes.getLength(); i++) {
                org.w3c.dom.Node node = childNodes.item(i);

                // Überspringe Text-Nodes und nicht-Element-Nodes
                if (node.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE) {
                    continue;
                }

                org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                String tagName = element.getTagName();

                if ("name".equals(tagName)) {
                    // Prüfe, ob es sich um die Präsidentin oder den Vizepräsidenten handelt
                    String nameText = element.getTextContent();
                    if (nameText != null && (nameText.contains("Präsident") || nameText.contains("präsident"))) {
                        skipNextParagraph = true; // Überspringe den nächsten Paragraphen
                        continue; // Überspringe dieses Name-Element
                    }

                    // Überprüfe, ob dieses Name-Element zum Hauptredner gehört
                    org.w3c.dom.Node parentNode = element.getParentNode();
                    boolean isParentRedner = false;
                    String parentSpeakerId = "";

                    if (parentNode != null && parentNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                        org.w3c.dom.Element parentElement = (org.w3c.dom.Element) parentNode;
                        if ("redner".equals(parentElement.getTagName())) {
                            isParentRedner = true;
                            parentSpeakerId = parentElement.getAttribute("id");
                        }
                    }

                    // Nur hinzufügen, wenn es der Name des Hauptredners ist
                    if (isParentRedner && mainSpeakerId.equals(parentSpeakerId)) {
                        if (element.getTextContent() != null && !element.getTextContent().trim().isEmpty()) {
                            if (text.length() > 0) {
                                text.append("\n");
                            }
                            text.append(element.getTextContent().trim()).append(":");
                        }
                    }
                } else if ("p".equals(tagName)) {
                    // Wenn der vorherige Flag gesetzt ist, überspringe diesen Paragraphen
                    if (skipNextParagraph) {
                        skipNextParagraph = false; // Zurücksetzen des Flags
                        continue;
                    }

                    // Überspringe Paragraphen mit Klasse "redner"
                    if ("redner".equals(element.getAttribute("klasse"))) {
                        continue;
                    }

                    // Ermittle den aktuellen Sprecher des Paragraphen
                    String currentSpeakerId = getCurrentSpeakerForElement(element, mainSpeakerId);

                    // Nur Text des Hauptredners in den Volltext aufnehmen
                    if (mainSpeakerId.equals(currentSpeakerId)) {
                        // Füge den Text des Paragraphen hinzu, wenn er vom Hauptredner stammt
                        if (element.getTextContent() != null && !element.getTextContent().trim().isEmpty()) {
                            if (text.length() > 0) {
                                text.append("\n");
                            }
                            text.append(element.getTextContent().trim());
                        }
                    }
                }
                // Kommentare werden nicht hinzugefügt
            }
        } catch (Exception e) {
            System.out.println("Error extracting speech text: " + e.getMessage());
            e.printStackTrace();
        }

        return text.toString();
    }

    // Hilfsmethode, um den aktuellen Sprecher eines Elements zu ermitteln
    private String getCurrentSpeakerForElement(org.w3c.dom.Element element, String defaultSpeakerId) {
        // Suche nach dem nächsten "redner" Paragraph vor diesem Element
        org.w3c.dom.Node prevSibling = element.getPreviousSibling();

        while (prevSibling != null) {
            if (prevSibling.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                org.w3c.dom.Element prevElement = (org.w3c.dom.Element) prevSibling;
                if ("p".equals(prevElement.getTagName()) && "redner".equals(prevElement.getAttribute("klasse"))) {
                    org.w3c.dom.NodeList rednerElements = prevElement.getElementsByTagName("redner");
                    if (rednerElements.getLength() > 0) {
                        org.w3c.dom.Element rednerElement = (org.w3c.dom.Element) rednerElements.item(0);
                        String speakerId = rednerElement.getAttribute("id");
                        if (speakerId != null && !speakerId.isEmpty()) {
                            return speakerId;
                        }
                    }
                }
            }
            prevSibling = prevSibling.getPreviousSibling();
        }

        // Wenn kein Redner-Paragraph gefunden wurde, verwende den Default (Hauptredner)
        return defaultSpeakerId;
    }

    private String generateRandomId() {
        return String.valueOf(Math.abs(new Random().nextInt(2000000000)));
    }

    private long parseGermanDate(String germanDateStr) {
        try {
            // Extrahiere tag, monat und jahr aus dem format "Dienstag, den 11. Februar 2025"
            Pattern datePattern = Pattern.compile("\\w+,\\s+den\\s+(\\d+)\\.\\s+(\\w+)\\s+(\\d{4})");
            Matcher matcher = datePattern.matcher(germanDateStr);

            if (matcher.find()) {
                int day = Integer.parseInt(matcher.group(1));
                String monthName = matcher.group(2);
                int year = Integer.parseInt(matcher.group(3));

                // Map German month names to month numbers
                Map<String, Integer> monthMap = new HashMap<>();
                monthMap.put("Januar", 0);
                monthMap.put("Februar", 1);
                monthMap.put("März", 2);
                monthMap.put("April", 3);
                monthMap.put("Mai", 4);
                monthMap.put("Juni", 5);
                monthMap.put("Juli", 6);
                monthMap.put("August", 7);
                monthMap.put("September", 8);
                monthMap.put("Oktober", 9);
                monthMap.put("November", 10);
                monthMap.put("Dezember", 11);

                int month = monthMap.getOrDefault(monthName, 0);

                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, day, 0, 0, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                return calendar.getTimeInMillis();
            }
        } catch (Exception e) {
            System.out.println("Error parsing German date: " + e.getMessage());
        }

        return 0;
    }

    private long parseTimeToMillis(String timeStr, long baseDate) {
        try {
            // Füge zeit in format  "9:00" hinzu
            String[] parts = timeStr.split(":");
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(baseDate);
            calendar.set(Calendar.HOUR_OF_DAY, hours);
            calendar.set(Calendar.MINUTE, minutes);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            return calendar.getTimeInMillis();
        } catch (Exception e) {
            System.out.println("Error parsing time: " + e.getMessage());
            return baseDate;
        }
    }

    private String downloadContent(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        return content.toString();
    }
}
