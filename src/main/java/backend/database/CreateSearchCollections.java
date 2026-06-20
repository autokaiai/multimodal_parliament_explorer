package backend.database;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Projections;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;

/**
 * @author Kai
 * @date 13/03/2025
 */
public class CreateSearchCollections {

    public static void main(String[] args) throws IOException {
        // Create MongoDB connection with classes
        MongoDBHandler dbHandler = MongoDBHandler.getInstance();

        // Get the linguistic_features collection
        MongoCollection<Document> linguisticFeaturesCollection =
                dbHandler.getLinguisticFeaturesCollection();

        // Get topics collection
        MongoCollection<Document> topicsCollection = dbHandler.getTopicsCollection();

        // Empty existing topics collection if it exists
        try {
            topicsCollection.deleteMany(new Document());  // Removes all documents but keeps indexes
            System.out.println("Emptied existing 'topics' collection");
        } catch (Exception e) {
            // Collection might not exist yet
            System.out.println("Could not empty topics collection: " + e.getMessage());
        }

        // Map to store topics and their associated redeIds (using ConcurrentHashMap for thread safety)
        // And using Set instead of List to ensure uniqueness of redeIds
        Map<String, Set<String>> topicToRedeIds = new ConcurrentHashMap<>();

        // Find all documents in the linguistic_features collection
        FindIterable<Document> documents = linguisticFeaturesCollection.find()
                .projection(Projections.include("redeId", "topicsSearchField"));

        // Process documents in parallel using streams
        StreamSupport.stream(documents.spliterator(), true)
                .forEach(doc -> {
                    String redeId = doc.getString("redeId");
                    String topicsSearchField = doc.getString("topicsSearchField");

                    if (redeId != null && topicsSearchField != null && !topicsSearchField.isEmpty()) {
                        // Add redeId to the set for this topic (Set ensures uniqueness)
                        topicToRedeIds.computeIfAbsent(topicsSearchField, k -> ConcurrentHashMap.newKeySet())
                                .add(redeId);
                    }
                });

        // Prepare bulk insertion
        List<Document> topicDocuments = new ArrayList<>();

        // Create a document for each topic
        topicToRedeIds.forEach((topic, redeIds) -> {
            Document topicDoc = new Document()
                    .append("topic", topic)
                    .append("redeIds", new ArrayList<>(redeIds)); // Convert Set to List for MongoDB

            topicDocuments.add(topicDoc);
        });

        // Bulk insert all documents at once (much faster than individual inserts)
        if (!topicDocuments.isEmpty()) {
            topicsCollection.insertMany(topicDocuments);
        }

        System.out.println("Successfully created 'topics' collection with documents for each topic.");
        System.out.println("Processed " + topicToRedeIds.size() + " unique topics.");
        System.out.println("Total document count: " + topicsCollection.countDocuments());
    }
}