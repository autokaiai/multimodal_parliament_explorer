package backend.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Philipp Hein
 * @date 10.03.2025
 * Modified with connection pooling by Kai
 */
public class MongoDBHandler {
    // Singleton instance for connection pooling
    private static volatile MongoDBHandler instance;

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final GridFSBucket gridFSBucket;

    /**
     * Private constructor for singleton pattern
     *
     * @param shouldCreateConnectionPool Whether to create a connection pool
     * @throws IOException falls die connection URL nicht gesetzt ist.
     */
    private MongoDBHandler(boolean shouldCreateConnectionPool) throws IOException {
        if (System.getenv("MONGODB_URI") == null) {
            throw new IOException("MONGODB_URI environment variable is not set.");
        }

        ConnectionString connString = new ConnectionString(System.getenv("MONGODB_URI"));

        if (shouldCreateConnectionPool) {
            // Create connection pool with optimized settings

            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connString)
                    .applyToConnectionPoolSettings(builder ->
                            builder.maxSize(Math.max(100, Runtime.getRuntime().availableProcessors() * 5))
                                    .minSize(Runtime.getRuntime().availableProcessors())
                                    .maxWaitTime(10, TimeUnit.MINUTES)
                                    .maxConnectionIdleTime(30, TimeUnit.MINUTES))
                    .applyToSocketSettings(builder ->
                            builder.connectTimeout(10, TimeUnit.MINUTES))
                    .build();

            this.mongoClient = MongoClients.create(settings);
            System.out.println("MongoDB connection pool initialized.");
        } else {
            // Standard client for backward compatibility
            this.mongoClient = MongoClients.create(connString.getConnectionString());
        }

        // Referenz auf die Datenbank holen
        this.database = mongoClient.getDatabase(connString.getDatabase() != null ? connString.getDatabase() : "data");
        this.gridFSBucket = GridFSBuckets.create(database, "fs");
    }

    /**
     * Standardkonstruktor
     *
     * @throws IOException falls die config.properties nicht gelesen werden kann.
     * @author Philipp Hein
     * @date 10.03.2025
     */
    public MongoDBHandler() throws IOException {
        this(false); // Default to non-pooled for backward compatibility
    }

    /**
     * Get the singleton instance with connection pooling
     *
     * @return The shared MongoDBHandler instance
     * @throws IOException If there's an error initializing
     */
    public static MongoDBHandler getInstance() throws IOException {
        if (instance == null) {
            synchronized (MongoDBHandler.class) {
                if (instance == null) {
                    instance = new MongoDBHandler(true);
                }
            }
        }
        return instance;
    }

    /**
     * Shutdown the connection pool when application exits
     */
    public static void shutdown() {
        if (instance != null) {
            synchronized (MongoDBHandler.class) {
                if (instance != null) {
                    instance.close();
                    instance = null;
                }
            }
        }
    }

    /**
     * @author Philipp Hein
     * @date 10.03.2025
     * Stellt die Verbindung zur Datenbank her.
     */
    public MongoDatabase connect() {
        return this.database;
    }

    /**
     * @author Philipp Hein
     * @date 10.03.2025
     * Schließt die Verbindung zur Datenbank.
     */
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("MongoDB connection closed");
        }
    }

    /**
     * @return MongoCollection für plenarprotokolle
     * @author Philipp Landmann
     * @date 13.03.2025
     */
    public MongoCollection<Document> getPlenarprotocolsCollection() {
        return database.getCollection("plenarprotocol");
    }

    /**
     * @return MongoCollection für speaker
     * @author Philipp Hein
     * @date 10.03.2025
     */
    public MongoCollection<Document> getSpeakerCollection() {
        return database.getCollection("speaker");
    }

    /**
     * @return MongoCollection für speech.
     * @author Philipp Hein
     * @date 10.03.2025
     */
    public MongoCollection<Document> getSpeechCollection() {
        return database.getCollection("speeches_1");
    }

    /**
     * @return MongoCollection für "linguistic_features".
     * @author Kai
     * @date 16.03.2025
     */
    public MongoCollection<Document> getLinguisticFeaturesTranskriptCollection() {
        return database.getCollection("linguistic_features_transcript");
    }

    /**
     * @return MongoCollection für "linguistic_features".
     * @author Philipp Hein
     * @date 16.03.2025
     */
    public MongoCollection<Document> getLinguisticFeaturesCollection() {
        return database.getCollection("linguistic_features");
    }

    /**
     * @return MongoCollection für "topics".
     * @date 13.03.2025
     */
    public MongoCollection<Document> getTopicsCollection() {
        return database.getCollection("topics");
    }

    /**
     * Get a collection by name
     *
     * @param collectionName The name of the collection
     * @return The MongoDB collection
     */
    public MongoCollection<Document> getCollection(String collectionName) {
        return database.getCollection(collectionName);
    }

    // Allgemeine Datenbankzugriffe

    /**
     * @param collectionName Name der Collection.
     * @param document       Das zu speichernde Dokument.
     * @author Philipp Hein
     * @date 10.03.2025
     * <p>
     * insert: Einfügen eines einzelnen Dokuments in der angegebenen Collection.
     */
    public void insertDocument(String collectionName, Document document) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.insertOne(document);
    }

    /**
     * @param collectionName Name der Collection.
     * @param filter         bsp: -> Filters.eq("key", "value")
     * @return Liste der gefundenen Dokumente.
     * @author Philipp Hein
     * @date 10.03.2025
     * <p>
     * find: Finde Dokumente aus der angegebenen Collection basierend auf Filter.
     * Gibt alle Dokumente zurück, falls der Filter null oder leer ist.
     */
    public List<Document> findDocuments(String collectionName, Bson filter) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        List<Document> results = new ArrayList<>();

        FindIterable<Document> iterable;
        if (filter != null) {
            iterable = collection.find(filter);
        } else {
            iterable = collection.find();
        }

        for (Document doc : iterable) {
            results.add(doc);
        }
        return results;
    }

    /**
     * Gibt das erste Dokument zurück, das dem angegebenen Filter in der Collection entspricht.
     *
     * @param collectionName Name der Collection.
     * @param filter         Filter -> Bsp: Filters.eq("key", "value")
     * @return Das erste gefundene Dokument oder null, falls keines gefunden wurde.
     * @author Philipp Hein
     * @date 12.03.2025
     */
    public Document findOne(String collectionName, Bson filter) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        return collection.find(filter).first();
    }

    /**
     * Gibt alle Dokumente der angegebenen Collection zurück.
     *
     * @param collectionName Name der Collection.
     * @return Liste aller Dokumente in der Collection.
     * @author Philipp Hein
     * @date 12.03.2025
     */
    public List<Document> findAll(String collectionName) {
        return findDocuments(collectionName, null);
    }

    /**
     * @param collectionName Name der Collection.
     * @param filter         um die zu löschenden Dokumente auszuwählen
     * @param update         Update -> Updates.set(feld, wert).
     * @return UpdateResult mit Informationen über die durchgeführte Operation.
     * @author Philipp Hein
     * @date 10.03.2025
     * <p>
     * update: updated Dokumente in der angegebenen Collection basierend auf Filter.
     */
    public UpdateResult updateDocuments(String collectionName, Bson filter, Bson update) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        return collection.updateMany(filter, update);
    }

    /**
     * @param collectionName Name der Collection.
     * @param filter         um die zu löschenden Dokumente auszuwählen
     * @return DeleteResult
     * @author Philipp Hein
     * @date 10.03.2025
     * <p>
     * Delete: Löscht Dokumente in der angegebenen Collection basierend auf Filter.
     */
    public DeleteResult deleteDocuments(String collectionName, Bson filter) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        return collection.deleteMany(filter);
    }

    /**
     * @param collectionName Name der Collection.
     * @param pipeline       Liste von Aggregations Schritten.
     * @return Die Ergebnisse der Aggregation.
     * @author Philipp Hein
     * @date 10.03.2025
     * <p>
     * Führt eine Aggregation auf der Collection aus.
     */
    public AggregateIterable<Document> aggregateDocuments(String collectionName, List<Bson> pipeline) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        return collection.aggregate(pipeline);
    }

    /**
     * @param collectionName Name der Collection.
     * @param filter         -> Filters.eq("key", "value").
     * @return Anzahl der Dokumente, die dem Filter entsprechen.
     * @author Philipp Hein
     * @date 10.03.2025
     * <p>
     * Zählt die Dokumente in einer Collection, die auf einen bestimmten Filter passen.
     */
    public long countDocuments(String collectionName, Bson filter) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        if (filter == null) {
            return collection.countDocuments();
        } else {
            return collection.countDocuments(filter);
        }
    }

    public GridFSBucket getGridFSBucket() {
        return this.gridFSBucket;
    }
}