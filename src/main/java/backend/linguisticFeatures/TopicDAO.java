package backend.linguisticFeatures;

import backend.database.MongoDBHandler;
import backend.speech.SpeechDAO;
import backend.speech.Speech_impl;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Kai
 */
public class TopicDAO {
    private final MongoCollection<Document> collection;
    private final MongoDBHandler dbHandler;

    public TopicDAO() throws IOException {
        this.dbHandler = new MongoDBHandler();
        MongoDatabase database = dbHandler.connect();
        this.collection = dbHandler.getTopicsCollection();
    }

    /**
     * Returns all topics from the collection.
     *
     * @return List<Document> mit allen Topics
     * @author Philipp Schneider
     */
    public List<Document> getAllTopics() {
        List<Document> topics = new ArrayList<>();
        collection.find().into(topics);
        return topics;
    }

    /**
     * Returns a list of speeches by topic.
     *
     * @param id id des topics.
     * @author Philipp Landmann
     */
    public List<Speech_impl> getSpeechesByTopic(String id) throws IOException {
        Document topicDoc = this.collection.find(Filters.eq("topic", id)).first();
        if (topicDoc == null) {
            return new ArrayList<>();
        }
        List<String> speechIds = topicDoc.getList("redeIds", String.class);
        MongoDBHandler dbHandler = new MongoDBHandler();
        SpeechDAO speechDAO = new SpeechDAO(dbHandler.getSpeechCollection());

        List<Speech_impl> speeches = new ArrayList<>();
        for (String speechId : speechIds) {
            Speech_impl speech = speechDAO.findById(speechId);
            speeches.add(speech);
        }
        return speeches;
    }

    /**
     * Findet alle Reden-IDs für mehrere Topics.
     *
     * @param topics Liste der Topic-IDs
     * @return Set von Speech-IDs, die zu mindestens einem der Topics gehören
     * @author Philipp Schneider
     */
    public Set<String> getSpeechIdsByTopics(List<String> topics) {
        Set<String> speechIds = new HashSet<>();

        if (topics == null || topics.isEmpty()) {
            return speechIds;
        }

        System.out.println("Searching for topics: " + topics);
        for (String topic : topics) {
            Document topicDoc = this.collection.find(Filters.eq("topic", topic)).first();
            if (topicDoc != null) {
                List<String> redeIds = topicDoc.getList("redeIds", String.class);
                System.out.println("Topic '" + topic + "' has " + (redeIds != null ? redeIds.size() : 0) + " speeches");
            } else {
                System.out.println("Topic '" + topic + "' not found in database");
            }
        }

        // Erstelle Filter für die angegebenen Topics
        List<Bson> topicFilters = new ArrayList<>();
        for (String topic : topics) {
            topicFilters.add(Filters.eq("topic", topic));
        }

        Bson filter = Filters.or(topicFilters.toArray(new Bson[0]));
        System.out.println("Filtering topics with: " + filter);

        try (MongoCursor<Document> cursor = collection.find(filter).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                System.out.println("Found topic: " + doc.getString("topic"));

                List<String> redeIds = doc.getList("redeIds", String.class);
                if (redeIds != null) {
                    System.out.println("   with " + redeIds.size() + " speeches");
                    speechIds.addAll(redeIds);
                }
            }
        }

        System.out.println("Total unique speeches found: " + speechIds.size());
        return speechIds;
    }

    /**
     * @param topics Liste der Topic-IDs
     * @return Liste von Speech_impl-Objekten
     * @throws IOException wenn ein Datenbankfehler auftritt
     * @author Philipp
     */
    public List<Speech_impl> getSpeechesByTopics(List<String> topics) throws IOException {
        Set<String> speechIds = getSpeechIdsByTopics(topics);

        if (speechIds.isEmpty()) {
            return new ArrayList<>();
        }

        SpeechDAO speechDAO = new SpeechDAO(dbHandler.getSpeechCollection());
        List<Speech_impl> speeches = new ArrayList<>();

        for (String speechId : speechIds) {
            Speech_impl speech = speechDAO.findById(speechId);
            if (speech != null) {
                speeches.add(speech);
            }
        }

        return speeches;
    }

    /**
     * Saves a JSON string to MongoDB.
     */
    public void save(String jsonData) {
        Document doc = Document.parse(jsonData);
        collection.insertOne(doc);
    }

    /**
     * Finds a document by documentId and returns it as a JSON string.
     */
    public String findByTopic(String topic) {
        Bson filter = Filters.eq("topic", topic);
        Document doc = collection.find(filter).first();

        if (doc != null) {
            return doc.toJson();
        }
        return null;
    }

    /**
     * Updates a document using a JSON string.
     */
    public void update(String jsonData) {
        Document doc = Document.parse(jsonData);
        String topic = doc.getString("topic");

        Bson filter = Filters.eq("topic", topic);
        ReplaceOptions options = new ReplaceOptions().upsert(true);

        collection.replaceOne(filter, doc, options);
    }

    public void delete(String topic) {
        Bson filter = Filters.eq("topic", topic);
        collection.deleteOne(filter);
    }

    public void close() {
        dbHandler.close();
    }
}