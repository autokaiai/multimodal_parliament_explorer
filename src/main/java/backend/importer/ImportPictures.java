package backend.importer;

import backend.database.MongoDBHandler;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Philipp Schneider #7995354
 * Import-Tool zum Hinzufügen von Bildern aus der Bundestag-Bilddatenbank zu SpeakerInfo in der Speech Collection
 */
public class ImportPictures {

    private static final String BASE_URL = "https://bilddatenbank.bundestag.de";

    public static void main(String[] args) {
        try {
            // Verbindung zur MongoDB herstellen mit vorhandenem Handler
            MongoDBHandler dbHandler = new MongoDBHandler();
            dbHandler.connect();
            MongoCollection<Document> speakerCollection = dbHandler.getSpeakerCollection();
            MongoCollection<Document> speechCollection = dbHandler.getSpeechCollection();

            System.out.println("Verbindung zur MongoDB hergestellt.");
            System.out.println("Speaker Collection: " + speakerCollection.getNamespace().getCollectionName());
            System.out.println("Speech Collection: " + speechCollection.getNamespace().getCollectionName());

            // Erstelle eine Map für schnellen Zugriff auf Speaker anhand von Namen
            Map<String, Document> speakerMap = createSpeakerMap(speakerCollection);
            System.out.println("Speaker Map erstellt. Anzahl der Einträge: " + speakerMap.size());

            // Setze CookieManager
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
            CookieHandler.setDefault(cookieManager);

            // Hole CSRF-Token von der Hauptseite
            String mainPageUrl = BASE_URL + "/search/picture-result?query=&filterQuery%5Bereignis%5D%5B0%5D=Portr%C3%A4t%2FPortrait&sortVal=3";
            String mainPageResponse = sendGetRequest(mainPageUrl);

            // Extrahiere CSRF-Token
            String csrfToken = extractCsrfToken(mainPageResponse);
            System.out.println("CSRF-Token: " + csrfToken);

            // Sende Ajax-Anfragen und verarbeite die Ergebnisse
            int totalMatches = 0;
            int totalPages = 10000; // Maximal 10000 Seiten durchsuchen

            for (int page = 0; page <= totalPages; page++) {
                System.out.println("\n===== Verarbeite Seite " + page + " =====");
                String ajaxUrl = BASE_URL + "/ajax/picture-result?&cp=" + page;
                String jsonResponse = sendPostRequest(ajaxUrl, csrfToken);

                // Prüfe, ob die Antwort leer ist oder keine Fotos enthält
                if (jsonResponse.isEmpty() || !jsonResponse.contains("\"fotos\"")) {
                    System.out.println("Keine weiteren Bilder gefunden. Beende Suche.");
                    break;
                }

                // Finde Übereinstimmungen und aktualisiere die Collection
                int pageMatches = findMatchesAndUpdateCollection(jsonResponse, speakerMap, speechCollection);
                totalMatches += pageMatches;
                System.out.println("Auf Seite " + page + " wurden " + pageMatches + " Übereinstimmungen gefunden.");

                // Pause zwischen Anfragen
                if (page < totalPages) {
                    Thread.sleep(1000);
                }
            }

            System.out.println("\n=== Zusammenfassung ===");
            System.out.println("Insgesamt wurden " + totalMatches + " Übereinstimmungen gefunden und aktualisiert.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Erstellt eine Map für schnellen Zugriff auf Speaker-Dokumente anhand von Namen
     */
    private static Map<String, Document> createSpeakerMap(MongoCollection<Document> speakerCollection) {
        Map<String, Document> speakerMap = new HashMap<>();

        // Durchlaufe alle Speaker in der Collection
        for (Document speaker : speakerCollection.find()) {
            String name = speaker.getString("name");
            String firstName = speaker.getString("firstName");

            if (name != null && firstName != null && !name.isEmpty() && !firstName.isEmpty()) {
                // Erstelle verschiedene Schlüsselformate für die Suche
                String key1 = (name + ", " + firstName).toLowerCase(); // "nachname, vorname"
                String key2 = (firstName + " " + name).toLowerCase();  // "vorname nachname"

                speakerMap.put(key1, speaker);
                speakerMap.put(key2, speaker);

                // Debug: Gib einige Einträge aus
                if (speakerMap.size() <= 5) {
                    System.out.println("Speaker-Eintrag: " + key1);
                }
            }
        }

        return speakerMap;
    }

    /**
     * Sendet eine GET-Anfrage an die angegebene URL
     */
    private static String sendGetRequest(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    /**
     * Sendet eine POST-Anfrage an die angegebene URL
     */
    private static String sendPostRequest(String urlStr, String csrfToken) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
        connection.setRequestProperty("X-CSRF-Token", csrfToken);

        connection.setDoOutput(true);
        String postData = "filterQuery%5Bereignis%5D%5B%5D=Portr%C3%A4t%2FPortrait&sortVal=3";

        try (OutputStream os = connection.getOutputStream()) {
            os.write(postData.getBytes(StandardCharsets.UTF_8));
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }

    /**
     * Lädt ein Bild von der angegebenen URL herunter
     *
     * @param imageUrl URL des Bildes
     * @return Byte-Array mit den Bilddaten oder null bei Fehler
     */
    private static byte[] downloadImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                System.out.println("Fehler beim Herunterladen des Bildes. Response Code: " + responseCode);
                return null;
            }

            // Lese die Bildgröße aus dem Header
            int contentLength = connection.getContentLength();
            byte[] imageData;

            try (InputStream in = connection.getInputStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;

                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                imageData = out.toByteArray();
            }

            System.out.println("Bild heruntergeladen: " + imageUrl + " (" + imageData.length + " Bytes)");
            return imageData;

        } catch (Exception e) {
            System.out.println("Fehler beim Herunterladen des Bildes " + imageUrl + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Extrahiert das CSRF-Token aus der HTML-Antwort
     */
    private static String extractCsrfToken(String html) {
        int start = html.indexOf("csrf-token") + 21; // Länge von 'csrf-token" content="' ist 21
        int end = html.indexOf("\"", start);
        if (start > 0 && end > start) {
            return html.substring(start, end);
        }
        return "";
    }

    /**
     * Findet Übereinstimmungen zwischen Bildern und Sprechern und aktualisiert die Collection
     */
    private static int findMatchesAndUpdateCollection(String jsonResponse, Map<String, Document> speakerMap,
                                                      MongoCollection<Document> speechCollection) {
        int matches = 0;
        int totalUpdates = 0;

        // Durchsuche alle Vorkommen von "hqBild"
        int currentIndex = 0;

        while (true) {
            int hqBildIndex = jsonResponse.indexOf("\"hqBild\"", currentIndex);
            if (hqBildIndex == -1) break;

            // Finde den Bildnamen
            int startQuote = jsonResponse.indexOf("\"", hqBildIndex + 8) + 1;
            int endQuote = jsonResponse.indexOf("\"", startQuote);
            String hqBild = jsonResponse.substring(startQuote, endQuote);

            // Extrahiere den umgebenden Kontext für die Namensprüfung
            int contextStart = Math.max(0, hqBildIndex - 500);
            int contextEnd = Math.min(jsonResponse.length(), hqBildIndex + 1000);
            String context = jsonResponse.substring(contextStart, contextEnd);

            // Prüfe, ob "MdB" im Kontext vorkommt (nur MdB-Bilder berücksichtigen)
            if (context.contains("MdB")) {
                // Extrahiere den Namen aus dem Namen-Array
                String nameString = extractNameFromContext(context);

                if (!nameString.isEmpty()) {
                    // Erstelle die vollständige URL
                    String imageUrl = BASE_URL + "/fotos/" + hqBild;

                    // Suche nach passenden Sprechern
                    Document matchedSpeaker = findMatchingSpeaker(nameString, speakerMap);

                    if (matchedSpeaker != null) {
                        // Lade das Bild herunter
                        byte[] imageData = downloadImage(imageUrl);

                        // Hole die Speaker-ID
                        String speakerId = matchedSpeaker.getString("_id");

                        if (imageData != null && imageData.length > 0) {
                            // Aktualisiere alle Reden mit diesem Speaker im speakerInfo
                            int updated = updateSpeechesWithSpeakerImage(speakerId, imageUrl, imageData, speechCollection);
                            totalUpdates += updated;

                            matches++;
                            System.out.println("Match gefunden und " + updated + " Reden aktualisiert: " + nameString + " -> " + imageUrl);
                        } else {
                            // Aktualisiere nur mit der URL
                            int updated = updateSpeechesWithSpeakerImageUrl(speakerId, imageUrl, speechCollection);
                            totalUpdates += updated;

                            matches++;
                            System.out.println("Match gefunden, nur URLs in " + updated + " Reden aktualisiert: " + nameString + " -> " + imageUrl);
                        }
                    } else {
                        System.out.println("Kein passender Speaker für: " + nameString);
                    }
                }
            }

            currentIndex = endQuote;
        }

        System.out.println("Insgesamt wurden " + totalUpdates + " Reden aktualisiert.");
        return matches;
    }

    /**
     * Extrahiert den Namen aus dem Kontext
     */
    private static String extractNameFromContext(String context) {
        // Extrahiere Namen, wenn vorhanden
        int nameIndex = context.indexOf("\"name\"");
        if (nameIndex > 0) {
            int nameStart = context.indexOf("[", nameIndex);
            int nameEnd = context.indexOf("]", nameStart);
            if (nameStart > 0 && nameEnd > nameStart) {
                String nameArray = context.substring(nameStart + 1, nameEnd);

                // Extrahiere den ersten Namen aus dem Array
                Pattern pattern = Pattern.compile("\"([^\"]+)\"");
                Matcher matcher = pattern.matcher(nameArray);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        }

        return "";
    }

    /**
     * Findet einen passenden Speaker anhand des Namens
     */
    private static Document findMatchingSpeaker(String nameString, Map<String, Document> speakerMap) {
        // Versuche verschiedene Formate
        String normalizedName = nameString.toLowerCase();

        // Direkter Lookup
        if (speakerMap.containsKey(normalizedName)) {
            return speakerMap.get(normalizedName);
        }

        // Wenn Name im Format "Nachname, Vorname" ist
        if (normalizedName.contains(",")) {
            String[] parts = normalizedName.split(",", 2);
            String lastName = parts[0].trim();
            String firstName = parts.length > 1 ? parts[1].trim() : "";

            // Versuche verschiedene Kombinationen
            String key2 = (firstName + " " + lastName).toLowerCase();
            if (speakerMap.containsKey(key2)) {
                return speakerMap.get(key2);
            }
        }
        // Wenn Name im Format "Vorname Nachname" ist
        else if (normalizedName.contains(" ")) {
            String[] parts = normalizedName.split(" ", 2);
            String firstName = parts[0].trim();
            String lastName = parts.length > 1 ? parts[1].trim() : "";

            // Versuche verschiedene Kombinationen
            String key2 = (lastName + ", " + firstName).toLowerCase();
            if (speakerMap.containsKey(key2)) {
                return speakerMap.get(key2);
            }
        }

        // Keine Übereinstimmung gefunden
        return null;
    }

    /**
     * Aktualisiert alle Reden mit der SpeakerId und fügt die Bild-URL zum speakerInfo-Objekt hinzu
     */
    private static int updateSpeechesWithSpeakerImageUrl(String speakerId, String imageUrl,
                                                         MongoCollection<Document> speechCollection) {
        // Suche nach allen Reden mit diesem Speaker
        Bson filter = Filters.eq("speakerInfo._id", speakerId);

        // Aktualisiere das speakerInfo-Objekt mit der Bild-URL
        Bson update = Updates.set("speakerInfo.imageUrl", imageUrl);

        // Führe das Update auf der Collection aus
        try {
            return (int) speechCollection.updateMany(filter, update).getModifiedCount();
        } catch (Exception e) {
            System.out.println("Fehler beim Aktualisieren der Reden: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Aktualisiert alle Reden mit der SpeakerId und fügt sowohl die Bild-URL als auch die Bilddaten
     * zum speakerInfo-Objekt hinzu
     */
    private static int updateSpeechesWithSpeakerImage(String speakerId, String imageUrl, byte[] imageData,
                                                      MongoCollection<Document> speechCollection) {
        // Suche nach allen Reden mit diesem Speaker
        Bson filter = Filters.eq("speakerInfo._id", speakerId);

        // Konvertiere die Bilddaten in einen Base64-String
        String base64Image = Base64.getEncoder().encodeToString(imageData);

        // Erstelle die Updates
        List<Bson> updates = new ArrayList<>();
        updates.add(Updates.set("speakerInfo.imageUrl", imageUrl));
        updates.add(Updates.set("speakerInfo.imageData", "Binary.createFromBase64('" + base64Image + "', 0)"));

        Bson combinedUpdate = Updates.combine(updates);

        // Führe das Update auf der Collection aus
        try {
            return (int) speechCollection.updateMany(filter, combinedUpdate).getModifiedCount();
        } catch (Exception e) {
            System.out.println("Fehler beim Aktualisieren der Reden: " + e.getMessage());
            return 0;
        }
    }
}