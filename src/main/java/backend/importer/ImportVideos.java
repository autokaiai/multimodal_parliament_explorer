package backend.importer;

import backend.database.MongoDBHandler;
import backend.plenarprotocol.PlenarprotocolDAO;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Import-Tool zum Extrahieren von Video-Links aus den Bundestag-Protokollen
 * und Verlinken mit den entsprechenden Reden in der Speech-Collection.
 * Das Video wird in GridFS gespeichert und nur die File-ID wird im Dokument abgelegt.
 * Neu: Verwendet den maximalen Protokoll-Index, ähnlich wie ImportHandler, um nur
 * neue Videos zu importieren.
 *  @author Philipp Schneider
 */
public class ImportVideos {

    private static final String BASE_URL = "https://www.bundestag.de/ajax/filterlist/de/dokumente/protokolle/442112-442112";
    private static final Pattern VIDEO_ID_PATTERN = Pattern.compile("videoid=([0-9]+)");
    private static final String DOWNLOAD_DIRECTORY = "bundestag_videos";

    private static final Set<String> SKIP_TITLES = new HashSet<>(Arrays.asList(
            "bundestagspräsident", "bundestagspräsidentin",
            "bundestagsvizepräsident", "bundestagsvizepräsidentin",
            "präsident", "präsidentin", "vizepräsident", "vizepräsidentin"
    ));

    public static void main(String[] args) {
        try {
            // Verbindung zur MongoDB herstellen
            MongoDBHandler dbHandler = new MongoDBHandler();
            MongoDatabase database = dbHandler.connect();

            // Hole die video und speech Collections
            MongoCollection<Document> videoCollection = database.getCollection("videos");
            MongoCollection<Document> speechCollection = dbHandler.getSpeechCollection();

            System.out.println("Verbindung zur MongoDB hergestellt.");
            System.out.println("Speech Collection: " + speechCollection.getNamespace().getCollectionName());
            System.out.println("Video Collection: " + videoCollection.getNamespace().getCollectionName());

            // Ermitteln des höchsten Protokoll-Index, für den bereits Videos importiert wurden
            // Analog zu ImportHandler, der PlenarprotocolDAO verwendet
            PlenarprotocolDAO plenarprotocolDAO = new PlenarprotocolDAO();
            int maxProcessedIndex = plenarprotocolDAO.getMaxProtocolIndex();

            // Wenn keine Protokolle vorhanden sind, einen Standardwert verwenden
            int startSession = maxProcessedIndex > 0 ? maxProcessedIndex + 1 : 212;
            System.out.println("Beginne Import ab Sitzung " + startSession + " (maximaler vorhandener Index: " + maxProcessedIndex + ") und verarbeite aufsteigend neuere Sitzungen");

            // Kein festes Ende mehr - wir importieren so lange, bis wir keine Ergebnisse mehr bekommen

            // Erstelle Download-Verzeichnis, falls nicht vorhanden
            Path downloadDir = Paths.get(DOWNLOAD_DIRECTORY);
            if (!Files.exists(downloadDir)) {
                Files.createDirectory(downloadDir);
            }

            // Erstelle einen GridFSBucket, um große Dateien zu speichern
            GridFSBucket gridFSBucket = GridFSBuckets.create(database);

            int totalUpdatedSpeeches = 0;
            boolean noMoreResults = false;

            // Für jede Sitzung, beginnend mit der ältesten, die noch nicht importiert wurde
            // und dann zu neueren fortschreitend
            for (int sessionNumber = startSession; !noMoreResults; sessionNumber++) {
                System.out.println("\n===== Verarbeite Sitzung " + sessionNumber + " =====");

                // URL für diese Sitzung erstellen
                String url = BASE_URL + "?limit=10&noFilterSet=false&sitzung=442110%23" + sessionNumber + "&wahlperiode=442108%2320";

                // Lade alle relevanten Reden aus der Datenbank
                String protocolTitle = "Plenarprotokoll 20/" + sessionNumber;
                FindIterable<Document> speeches = speechCollection.find(Filters.eq("protocol.title", protocolTitle))
                        .sort(new Document("_id", 1)); // Aufsteigend nach _id sortieren

                // Gruppiere die Reden nach speakerKey (Nachname_Vorname in Kleinbuchstaben)
                Map<String, List<Document>> speakerSpeechMap = new HashMap<>();
                for (Document speech : speeches) {
                    Document speakerInfo = (Document) speech.get("speakerInfo");
                    if (speakerInfo != null) {
                        String lastName = speakerInfo.getString("name");
                        String firstName = speakerInfo.getString("firstName");
                        if (lastName != null && firstName != null) {
                            String key = (lastName + "_" + firstName).toLowerCase();
                            speakerSpeechMap.computeIfAbsent(key, k -> new ArrayList<>()).add(speech);
                            System.out.println("Rede gefunden: " + lastName + ", " + firstName
                                    + " (Anzahl: " + speakerSpeechMap.get(key).size() + ")");
                        }
                    }
                }
                int totalSpeeches = speakerSpeechMap.values().stream().mapToInt(List::size).sum();
                System.out.println("Insgesamt " + totalSpeeches + " Reden für Protokoll " + protocolTitle + " gefunden.");

                // Überprüfen, ob für dieses Protokoll bereits Videos vorhanden sind
                long existingVideos = speechCollection.countDocuments(
                        Filters.and(
                                Filters.eq("protocol.title", protocolTitle),
                                Filters.exists("videoFileId", true)
                        )
                );

                long existingSpeeches = speechCollection.countDocuments(
                        Filters.and(
                                Filters.eq("protocol.title", protocolTitle)
                        )
                );

                if (existingSpeeches - existingVideos == 0) {
                    System.out.println("Protokoll " + protocolTitle + " hat bereits " + existingVideos +
                            " Videos. Überspringe dieses Protokoll.");
                    continue;
                }

                // Map zur Nachverfolgung der aktuellen Video-Zuordnung pro Redner
                Map<String, Integer> speakerVideoIndex = new HashMap<>();

                try {
                    // Lade die Seite mit JSOUP
                    org.jsoup.nodes.Document doc = Jsoup.connect(url).get();

                    // Prüfen, ob "keine Ergebnisse gefunden" angezeigt wird
                    Elements noResultsElements = doc.select("div.bt-slide-error");
                    if (!noResultsElements.isEmpty()) {
                        // Verifikation: Prüfe auf den spezifischen Text
                        Elements headingElements = noResultsElements.select("h3");
                        if (!headingElements.isEmpty() && headingElements.text().contains("Leider keine Ergebnisse gefunden")) {
                            System.out.println("Keine weiteren Sitzungen verfügbar. Import abgeschlossen.");
                            noMoreResults = true;
                            break;
                        }
                    }

                    // Finde den div mit der TOP-Collapser-Klasse
                    Element topContainer = doc.selectFirst("div.bt-top-collapser-wrap");
                    if (topContainer == null) {
                        System.out.println("Keine TOP-Container gefunden für Sitzung " + sessionNumber);
                        continue;
                    }

                    // Finde alle strong-Tags innerhalb des Containers
                    Elements topicElements = topContainer.select("strong");

                    for (Element topicElement : topicElements) {
                        String topic = topicElement.text().trim();

                        // Ignoriere die Sitzungseröffnung
                        if (topic.contains("Sitzungseröffnung")) {
                            System.out.println("Überspringe Sitzungseröffnung");
                            continue;
                        }

                        System.out.println("\n-- Topic: " + topic);

                        // Suche nach dem Redner-Container
                        Element rednerContainer = null;
                        Element parent = topicElement.parent();
                        if (parent != null) {
                            rednerContainer = parent.selectFirst("div.bt-redner-collapse");
                            if (rednerContainer == null) {
                                rednerContainer = parent.nextElementSibling();
                                if (rednerContainer != null && !rednerContainer.hasClass("bt-redner-collapse")) {
                                    rednerContainer = rednerContainer.selectFirst("div.bt-redner-collapse");
                                }
                            }
                        }
                        if (rednerContainer == null) {
                            System.out.println("Kein Redner-Container gefunden für Topic: " + topic);
                            continue;
                        }

                        // Finde alle Redner-Einträge
                        Elements rednerElements = rednerContainer.select("ul.bt-redner-liste > li");
                        System.out.println("Gefundene Redner: " + rednerElements.size());

                        for (Element rednerElement : rednerElements) {
                            try {
                                // Extrahiere Redner-Informationen
                                Element rednerLink = rednerElement.selectFirst("a[title]");
                                if (rednerLink == null) continue;

                                String rednerFullTitle = rednerLink.attr("title").trim();
                                Element nameElement = rednerLink.selectFirst("strong");
                                if (nameElement == null) continue;

                                String rednerName = nameElement.text().trim();
                                String rednerTitle = rednerFullTitle.replace(rednerName, "").trim();
                                if (rednerTitle.startsWith(",")) {
                                    rednerTitle = rednerTitle.substring(1).trim();
                                }

                                // Überspringe Präsidenten/Vizepräsidenten
                                if (shouldSkipSpeaker(rednerName, rednerTitle)) {
                                    System.out.println("Überspringe Präsident/Vizepräsident: " + rednerName + " (" + rednerTitle + ")");
                                    continue;
                                }

                                // Extrahiere Video-Link
                                Element videoLinkElement = rednerElement.selectFirst("a.bt-link-video");
                                if (videoLinkElement == null) {
                                    System.out.println("Kein Video-Link für: " + rednerName);
                                    continue;
                                }
                                String videoUrl = videoLinkElement.attr("href");
                                String videoId = extractVideoId(videoUrl);
                                if (videoId.isEmpty()) {
                                    System.out.println("Keine gültige Video-ID für: " + rednerName);
                                    continue;
                                }

                                // Konstruiere den Embed-Link für das Video
                                String directVideoUrl = "https://webtv.bundestag.de/pservices/player/embed/nokey?e=bt-od&ep=69&a=144277506&c="
                                        + videoId + "&t=https%3A%2F%2Fdbtg.tv%2Fcvid%2F7628688";

                                // --- Neuer Code: Video herunterladen und in GridFS speichern ---
                                // Erstelle den Download-Link mit dem doppelten Video-ID-Teil
                                String downloadVideoUrl = "https://cldf-od.r53.cdn.tv1.eu/1000153copo/ondemand/app144277506/145293313/"
                                        + videoId + "/" + videoId + "_h264_512_288_514kb_baseline_de_514.mp4?fdl=1";

                                // Lade das Video herunter
                                byte[] videoBytes = downloadFile(downloadVideoUrl);
                                if (videoBytes == null) {
                                    System.out.println("Fehler beim Herunterladen des Videos für: " + rednerName);
                                    continue;
                                }

                                // Lade das Video in GridFS hoch
                                ObjectId fileId;
                                try (InputStream streamToUploadFrom = new ByteArrayInputStream(videoBytes)) {
                                    GridFSUploadOptions options = new GridFSUploadOptions()
                                            .chunkSizeBytes(1024 * 1024) // z. B. 1 MB pro Chunk
                                            .metadata(new Document("videoId", videoId));
                                    fileId = gridFSBucket.uploadFromStream("video_" + videoId, streamToUploadFrom, options);
                                } catch (Exception e) {
                                    System.out.println("Fehler beim Speichern in GridFS für " + rednerName + ": " + e.getMessage());
                                    e.printStackTrace();
                                    continue;
                                }
                                // --- Ende neuer Code ---

                                // Extrahiere Vorname und Nachname
                                String[] nameParts = splitSpeakerName(rednerName);
                                String lastName = nameParts[0];
                                String firstName = nameParts[1];

                                // Normalisiere den Namen für die Suche
                                String speakerKey = (lastName + "_" + firstName).toLowerCase();

                                System.out.println("Video gefunden für: " + lastName + ", " + firstName);

                                // Ermittle den aktuellen Index für diesen Sprecher (Standard: 0)
                                int index = speakerVideoIndex.getOrDefault(speakerKey, 0);
                                List<Document> speechesOfSpeaker = speakerSpeechMap.get(speakerKey);
                                if (speechesOfSpeaker == null || speechesOfSpeaker.isEmpty()) {
                                    System.out.println("Keine Rede gefunden in der DB für: " + lastName + ", " + firstName);
                                    continue;
                                }
                                if (index >= speechesOfSpeaker.size()) {
                                    System.out.println("Mehr Videos als Reden für: " + lastName + ", " + firstName);
                                    continue;
                                }
                                // Hole die entsprechende Rede
                                Document speechToUpdate = speechesOfSpeaker.get(index);

                                // Aktualisiere die Rede mit Video-URL, Video-ID und der GridFS-File-ID
                                Bson filter = Filters.eq("_id", speechToUpdate.get("_id"));
                                Bson update = Updates.combine(
                                        Updates.set("videoUrl", directVideoUrl),
                                        Updates.set("videoId", videoId),
                                        Updates.set("videoFileId", fileId)
                                );
                                speechCollection.updateOne(filter, update);
                                totalUpdatedSpeeches++;
                                System.out.println("✓ Rede aktualisiert für: " + lastName + ", " + firstName + " (Rede " + (index + 1) + ")");

                                // Erhöhe den Index für diesen Sprecher
                                speakerVideoIndex.put(speakerKey, index + 1);

                                // Kurze Pause zwischen den Verarbeitungen
                                Thread.sleep(500);

                            } catch (Exception e) {
                                System.out.println("Fehler bei der Verarbeitung eines Redners: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Fehler beim Abrufen der Sitzung " + sessionNumber + ": " + e.getMessage());
                }
                // Kurze Pause zwischen den Sitzungen
                Thread.sleep(2000);
            }

            System.out.println("\n===== Import abgeschlossen =====");
            System.out.println("Insgesamt " + totalUpdatedSpeeches + " Reden mit Video-URLs aktualisiert.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Teilt einen Rednernamen in Nachname und Vorname.
     */
    private static String[] splitSpeakerName(String fullName) {
        String[] result = new String[2];
        fullName = fullName.replace("Dr.", "").trim();
        if (fullName.contains(",")) {
            String[] parts = fullName.split(",", 2);
            result[0] = parts[0].trim();
            result[1] = parts.length > 1 ? parts[1].trim() : "";
        } else {
            int lastSpace = fullName.lastIndexOf(" ");
            if (lastSpace > 0) {
                result[0] = fullName.substring(lastSpace + 1).trim();
                result[1] = fullName.substring(0, lastSpace).trim();
            } else {
                result[0] = fullName;
                result[1] = "";
            }
        }
        return result;
    }

    /**
     * Prüft, ob ein Redner basierend auf dem Titel übersprungen werden soll.
     */
    private static boolean shouldSkipSpeaker(String name, String title) {
        String lowerTitle = title.toLowerCase();
        for (String skipTitle : SKIP_TITLES) {
            if (lowerTitle.contains(skipTitle)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extrahiert die Video-ID aus der URL.
     */
    private static String extractVideoId(String url) {
        Matcher matcher = VIDEO_ID_PATTERN.matcher(url);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    /**
     * Lädt eine Datei von der angegebenen URL herunter und gibt den Inhalt als Byte-Array zurück.
     */
    private static byte[] downloadFile(String fileUrl) {
        try {
            URL url = new URL(fileUrl);
            URLConnection connection = url.openConnection();
            try (InputStream in = connection.getInputStream();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    baos.write(buffer, 0, bytesRead);
                }
                return baos.toByteArray();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}