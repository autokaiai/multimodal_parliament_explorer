package backend.utility;

import backend.database.MongoDBHandler;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Philipp Hein
 * @date 10.03.2025
 * Vergleich von Reden aus den Collections Speeches und Linguistic features, wenn in Speeches
 * welche enthalten sind, die noch nicht in Linguistic Features sind, werden diese zurückgegeben.
 */
public class ComparisonService {

    private final MongoDBHandler dbHandler;
    private final MongoCollection<Document> speechCollection;
    private final MongoCollection<Document> linguisticCollection;
    private final MongoCollection<Document> linguisticTranskriptCollection;

    /**
     * @throws IOException falls die Verbindung zur Datenbank nicht hergestellt werden kann.
     * @author Philipp Hein
     * @date 10.03.2025
     * Konstruktor: Baut eine Verbindung zur Datenbank auf und holt die beiden Collections.
     */
    public ComparisonService() throws IOException {
        this.dbHandler = new MongoDBHandler();
        MongoDatabase database = dbHandler.connect();
        this.speechCollection = dbHandler.getSpeechCollection();
        this.linguisticCollection = dbHandler.getLinguisticFeaturesCollection();
        this.linguisticTranskriptCollection = dbHandler.getLinguisticFeaturesTranskriptCollection();
    }

    /**
     * @return Eine Liste von IDs, die in der Speech-Collection existieren, aber in der Linguistic-Features-Transkript-Collection fehlen.
     * @author Kai
     * @date 16.03.2025
     * <p>
     * Vergleicht die IDs aus der Speech-Collection mit den redeIds aus der Linguistic-Features-Transkript-Collection.
     */
    public List<String> findMissingLinguisticTranskriptFeatureIds() {
        Set<String> speechIds = new HashSet<>();
        Set<String> linguisticIds = new HashSet<>();

        // Query to exclude documents where videoId is null or missing
        try (MongoCursor<Document> cursor = speechCollection.find(
                        new Document("videoId", new Document("$exists", true).append("$ne", null))) // Ensure videoId exists and is not null
                .projection(Projections.include("_id"))
                .iterator()) {

            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Object idObj = doc.get("_id");
                if (idObj != null) {
                    speechIds.add(idObj.toString());
                }
            }
        }


        // Hole alle redeId Felder aus der Linguistic_Features Collection.
        try (MongoCursor<Document> cursor = linguisticTranskriptCollection.find()
                .projection(Projections.include("redeId"))
                .iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Object redeIdObj = doc.get("redeId");
                if (redeIdObj != null) {
                    linguisticIds.add(redeIdObj.toString());
                }
            }
        }

        // Vergleich: IDs, die in speechIds, aber nicht in linguistic Ids enthalten sind.
        List<String> missingIds = new ArrayList<>();
        for (String id : speechIds) {
            if (!linguisticIds.contains(id)) {
                missingIds.add(id);
            }
        }
        return missingIds;
    }

    /**
     * @return Eine Liste von IDs, die in der Speech-Collection existieren, aber in der Linguistic-Features-Transkript-Collection fehlen.
     * @author Kai
     * @date 16.03.2025
     * <p>
     * Vergleicht die IDs aus der Speech-Collection mit den redeIds aus der Linguistic-Features-Transkript-Collection.
     */
    public List<String> findMissingLinguisticTranskriptFeatureIds(int protocolIndex, boolean force) {
        Set<String> speechIds = new HashSet<>();
        Set<String> linguisticIds = new HashSet<>();

        // Query to exclude documents where videoId is null or missing
        // Added filter for protocol.index equal to the specified protocolIndex
        try (MongoCursor<Document> cursor = speechCollection.find(
                        new Document("videoId", new Document("$exists", true).append("$ne", null))
                                .append("protocol.index", protocolIndex)) // Filter for specific protocol index
                .projection(Projections.include("_id"))
                .iterator()) {

            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Object idObj = doc.get("_id");
                if (idObj != null) {
                    speechIds.add(idObj.toString());
                }
            }
        }


        // Hole alle redeId Felder aus der Linguistic_Features Collection.
        try (MongoCursor<Document> cursor = linguisticTranskriptCollection.find()
                .projection(Projections.include("redeId"))
                .iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Object redeIdObj = doc.get("redeId");
                if (redeIdObj != null) {
                    linguisticIds.add(redeIdObj.toString());
                }
            }
        }

        // Vergleich: IDs, die in speechIds, aber nicht in linguistic Ids enthalten sind.
        List<String> missingIds = new ArrayList<>();
        for (String id : speechIds) {
            if (!linguisticIds.contains(id) || force) {
                missingIds.add(id);
            }
        }
        return missingIds;
    }

    /**
     * @return Eine Liste von IDs, die in der Speech-Collection existieren, aber in der Linguistic-Features-Collection fehlen.
     * @author Kai
     * @date 16.03.2025
     * <p>
     * Vergleicht die IDs aus der Speech-Collection mit den redeIds aus der Linguistic-Features-Collection.
     */
    public List<String> findMissingLinguisticFeatureIds(int protocolIndex, boolean force) {
        Set<String> speechIds = new HashSet<>();
        Set<String> linguisticIds = new HashSet<>();

        // Hole alle _id Felder aus der Speech Collection.
        try (MongoCursor<Document> cursor = speechCollection.find(
                        new Document("protocol.index", protocolIndex)) // Filter for specific protocol index
                .projection(Projections.include("_id"))
                .iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Object idObj = doc.get("_id");
                if (idObj != null) {
                    speechIds.add(idObj.toString());
                }
            }
        }

        // Hole alle redeId Felder aus der Linguistic_Features Collection.
        try (MongoCursor<Document> cursor = linguisticCollection.find()
                .projection(Projections.include("redeId"))
                .iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Object redeIdObj = doc.get("redeId");
                if (redeIdObj != null) {
                    linguisticIds.add(redeIdObj.toString());
                }
            }
        }

        // Vergleich: IDs, die in speechIds, aber nicht in linguistic Ids enthalten sind.
        List<String> missingIds = new ArrayList<>();
        for (String id : speechIds) {
            if (!linguisticIds.contains(id) || force) {
                missingIds.add(id);
            }
        }
        return missingIds;
    }

    /**
     * @return Eine Liste von IDs, die in der Speech-Collection existieren, aber in der Linguistic-Features-Collection fehlen.
     * @author Philipp Hein
     * @date 10.03.2025
     * <p>
     * Vergleicht die IDs aus der Speech-Collection mit den redeIds aus der Linguistic-Features-Collection.
     */
    public List<String> findMissingLinguisticFeatureIds() {
        Set<String> speechIds = new HashSet<>();
        Set<String> linguisticIds = new HashSet<>();

        // Hole alle _id Felder aus der Speech Collection.
        try (MongoCursor<Document> cursor = speechCollection.find()
                .projection(Projections.include("_id"))
                .iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Object idObj = doc.get("_id");
                if (idObj != null) {
                    speechIds.add(idObj.toString());
                }
            }
        }

        // Hole alle redeId Felder aus der Linguistic_Features Collection.
        try (MongoCursor<Document> cursor = linguisticCollection.find()
                .projection(Projections.include("redeId"))
                .iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Object redeIdObj = doc.get("redeId");
                if (redeIdObj != null) {
                    linguisticIds.add(redeIdObj.toString());
                }
            }
        }

        // Vergleich: IDs, die in speechIds, aber nicht in linguistic Ids enthalten sind.
        List<String> missingIds = new ArrayList<>();
        for (String id : speechIds) {
            if (!linguisticIds.contains(id)) {
                missingIds.add(id);
            }
        }
        return missingIds;
    }

    public List<String> getAllMissingLinguisticFeatureIds() {
        Set<String> missingIds = new HashSet<>();
        missingIds.addAll(findMissingLinguisticFeatureIds());
        missingIds.addAll(findMissingLinguisticTranskriptFeatureIds());
        return new ArrayList<>(missingIds);
    }

    public List<String> getAllMissingLinguisticFeatureIds(int protocolIndex, boolean force) {
        Set<String> missingIds = new HashSet<>();
        missingIds.addAll(findMissingLinguisticFeatureIds(protocolIndex, force));
        missingIds.addAll(findMissingLinguisticTranskriptFeatureIds(protocolIndex, force));
        return new ArrayList<>(missingIds);
    }

    /**
     * @author Philipp Hein
     * @date 10.03.2025
     * Schließt die Verbindung zur Datenbank.
     */
    public void close() {
        dbHandler.close();
    }
}
