package backend.linguisticFeatures;

import backend.database.MongoDBHandler;
import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LinguisticDAO {
    private final MongoCollection<Document> collection;
    private final MongoDBHandler dbHandler;
    private final Gson gson = new Gson();

    /**
     * Default constructor that uses the pooled connection.
     */
    public LinguisticDAO() {
        try {
            dbHandler = MongoDBHandler.getInstance();
            this.collection = dbHandler.getLinguisticFeaturesCollection();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize LinguisticDAO", e);
        }
    }

    /**
     * Converts a Map<String, Integer> to a List of { "key": ..., "value": ... }
     */
    private List<Document> mapToList(Map<String, ? extends Number> map) {
        return map.entrySet().stream()
                .map(entry -> new Document("key", entry.getKey()).append("value", entry.getValue()))
                .collect(Collectors.toList());
    }

    /**
     * Converts a List of { "key": ..., "value": ... } back to a Map<String, Integer>
     */
    private Map<String, Integer> listToMap(List<Document> list) {
        return list.stream()
                .collect(Collectors.toMap(
                        doc -> doc.getString("key"),
                        doc -> doc.getInteger("value", 0)
                ));
    }

    /**
     * Converts a List of { "key": ..., "value": ... } back to a Map<String, Integer>
     */
    private Map<String, Double> listToMapDouble(List<Document> list) {
        return list.stream()
                .collect(Collectors.toMap(
                        doc -> doc.getString("key"),
                        doc -> doc.getDouble("value")
                ));
    }

    /**
     * Saves a LinguisticFeaturesAggregate_Impl object to MongoDB,
     * ensuring problematic maps are converted to list structures.
     */
    public void save(LinguisticFeaturesAggregate_Impl linguisticFeaturesImpl) {
        Document doc = getDocumentFromCLass(linguisticFeaturesImpl);

        collection.insertOne(doc);
    }

    /**
     * Finds a LinguisticFeaturesAggregate_Impl object by documentId and converts lists back to maps.
     */
    public Optional<LinguisticFeaturesAggregate_Impl> findByDocumentId(String documentId) {
        Bson filter = Filters.eq("redeId", documentId);
        Document doc = collection.find(filter).first();

        if (doc != null) {
            LinguisticFeaturesAggregate_Impl result = parseDocument(doc);

            return Optional.of(result);
        }
        return Optional.empty();
    }

    /**
     * @author Kai
     */
    public LinguisticFeaturesAggregate_Impl parseDocument(Document doc) {
        // 1) Kopie anlegen, damit wir das Original-Dokument nicht verändern
        Document clone = Document.parse(doc.toJson());

        // 2) Felder entfernen, die in der DB als Array gespeichert sind,
        //    aber in der Klasse als Map erwartet werden
        clone.remove("posCounts");
        clone.remove("namedEntityCounts");
        clone.remove("dependencyCounts");
        clone.remove("lemmaFrequency");
        clone.remove("sentimentDistribution");
        clone.remove("topicCounts");

        // 3) Gson parst jetzt nur die restlichen Felder
        LinguisticFeaturesAggregate_Impl result = gson.fromJson(clone.toJson(), LinguisticFeaturesAggregate_Impl.class);

        // 4) Nun liest du posCounts & Co. manuell per listToMap(...) ein
        result.setPosCounts(listToMap(doc.getList("posCounts", Document.class)));
        result.setNamedEntityCounts(listToMap(doc.getList("namedEntityCounts", Document.class)));
        result.setDependencyCounts(listToMap(doc.getList("dependencyCounts", Document.class)));
        result.setLemmaFrequency(listToMap(doc.getList("lemmaFrequency", Document.class)));
        result.setSentimentDistribution(listToMapDouble(doc.getList("sentimentDistribution", Document.class)));
        result.setTopicCounts(listToMap(doc.getList("topicCounts", Document.class)));

        return result;
    }

    /**
     * Updates a LinguisticFeaturesAggregate_Impl object, converting maps to lists before saving.
     */
    public void update(LinguisticFeaturesAggregate_Impl updatedData) {
        Bson filter = Filters.eq("redeId", updatedData.getRedeId());
        ReplaceOptions options = new ReplaceOptions().upsert(true);

        Document doc = getDocumentFromCLass(updatedData);

        collection.replaceOne(filter, doc, options);
    }

    /**
     * @author Kai
     */
    public Document getDocumentFromCLass(LinguisticFeaturesAggregate_Impl updatedData) {
        Document doc = Document.parse(gson.toJson(updatedData));

        // Convert maps to lists
        doc.put("posCounts", mapToList(updatedData.getPosCounts()));
        doc.put("namedEntityCounts", mapToList(updatedData.getNamedEntityCounts()));
        doc.put("dependencyCounts", mapToList(updatedData.getDependencyCounts()));
        doc.put("lemmaFrequency", mapToList(updatedData.getLemmaFrequency()));
        doc.put("sentimentDistribution", mapToList(updatedData.getSentimentDistribution()));
        doc.put("topicCounts", mapToList(updatedData.getTopicCounts()));

        return doc;
    }

    public void delete(String documentId) {
        Bson filter = Filters.eq("redeId", documentId);
        collection.deleteOne(filter);
    }

    //public void close() {
      //  dbHandler.close();
   //}
}