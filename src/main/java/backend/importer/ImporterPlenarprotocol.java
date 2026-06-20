package backend.importer;

import backend.database.MongoDBHandler;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PlenarprotocolImporter – Dieses Skript erstellt eine neue Collection "plenarprotocol".
 * Für jedes Protokoll (basierend auf dem Feld "protocol.title" in den Reden)
 * werden alle zugehörigen Reden in einem Array zusammengefasst.
 * @author Philipp Schneider
 */
public class ImporterPlenarprotocol {

    public static void main(String[] args) {
        try {
            // Verbindung zur MongoDB herstellen
            MongoDBHandler dbHandler = new MongoDBHandler();
            MongoDatabase database = dbHandler.connect();

            // Hole die bestehende speeches-Collection
            MongoCollection<Document> speechCollection = dbHandler.getSpeechCollection();

            // Erstelle (oder hole) die neue Collection "plenarprotocol"
            MongoCollection<Document> protocolCollection = database.getCollection("plenarprotocol");

            // Optional: leere die Collection, falls bereits alte Daten vorhanden sind
            protocolCollection.drop();

            // Map zum Gruppieren der Reden anhand des Protokoll-Schlüssels (hier: protocol.title)
            Map<String, List<Document>> groupedSpeeches = new HashMap<>();
            // Hier speichern wir die Protokoll-Information (aus dem ersten Dokument der Gruppe)
            Map<String, Document> protocolInfoMap = new HashMap<>();

            // Alle speeches-Dokumente iterieren
            FindIterable<Document> speeches = speechCollection.find();
            for (Document speech : speeches) {
                Document protocol = (Document) speech.get("protocol");
                if (protocol == null) {
                    continue;
                }

                // Gruppierungsschlüssel: verwende das Protokoll-Titel-Feld ("Plenarprotokoll 20/212")
                String protocolKey = protocol.getString("title");
                if (protocolKey == null || protocolKey.isEmpty()) {
                    // Falls title nicht vorhanden ist, kann ein Fallback-Schlüssel (z. B. index_wp) genutzt werden
                    protocolKey = protocol.getInteger("index", 0) + "_" + protocol.getInteger("wp", 0);
                }

                // Falls diese Gruppe noch nicht existiert, initialisiere sie
                if (!groupedSpeeches.containsKey(protocolKey)) {
                    groupedSpeeches.put(protocolKey, new ArrayList<>());
                    protocolInfoMap.put(protocolKey, protocol);
                }

                // Optional: Entferne das redundante "protocol"-Feld aus der Rede, um Duplikate zu vermeiden
                Document speechCopy = new Document(speech);
                speechCopy.remove("protocol");

                // Füge die Rede der entsprechenden Gruppe hinzu
                groupedSpeeches.get(protocolKey).add(speechCopy);
            }

            // Erstelle für jede Gruppe ein neues Protokoll-Dokument in der "plenarprotocol"-Collection
            for (String key : groupedSpeeches.keySet()) {
                Document protocolDoc = new Document();
                // Setze _id auf den Gruppenschlüssel (Protokoll-Titel)
                protocolDoc.append("_id", key);
                protocolDoc.append("protocol", protocolInfoMap.get(key));
                protocolDoc.append("speeches", groupedSpeeches.get(key));

                protocolCollection.insertOne(protocolDoc);
                System.out.println("Inserted protocol group: " + key + " with "
                        + groupedSpeeches.get(key).size() + " speeches.");
            }

            System.out.println("Import into 'plenarprotocol'-Collection completed.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
