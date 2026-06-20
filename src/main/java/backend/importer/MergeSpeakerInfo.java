package backend.importer;

import backend.database.MongoDBHandler;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MergeSpeakerInfo {

    public static void main(String[] args) {
        try {
            // Verbindung zur MongoDB herstellen
            MongoDBHandler dbHandler = new MongoDBHandler();
            MongoDatabase database = dbHandler.connect();

            // Hole die Speaker-Collection (bereits importierte Speaker-Daten)
            MongoCollection<Document> speakerCollection = dbHandler.getSpeakerCollection();

            // Hole die Speech-Collection, in der die Reden liegen
            MongoCollection<Document> speechCollection = database.getCollection("speeches_1");

            // Iteriere über alle Dokumente in der Speech-Collection
            FindIterable<Document> speeches = speechCollection.find();
            for (Document speech : speeches) {
                // Hole den Speaker-ID-Wert (als String) aus dem Speech-Dokument
                String speakerId = speech.getString("speaker");
                if (speakerId == null || speakerId.isEmpty()) {
                    System.out.println("Kein speaker-Feld in Speech-Dokument: " + speech.get("_id"));
                    continue;
                }

                // Suche in der Speaker-Collection nach dem Dokument mit _id == speakerId
                Document speakerDoc = speakerCollection.find(new Document("_id", speakerId)).first();
                if (speakerDoc != null) {
                    // Aktualisiere das Speech-Dokument und füge das Feld "speakerInfo" ein
                    Document update = new Document("$set", new Document("speakerInfo", speakerDoc));
                    speechCollection.updateOne(new Document("_id", speech.get("_id")), update);
                    System.out.println("Merge erfolgreich für Speech-Dokument: " + speech.get("_id"));
                } else {
                    System.out.println("Kein Speaker-Dokument gefunden für speakerId: " + speakerId);
                }
            }

            System.out.println("Merge-Vorgang abgeschlossen.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
