package backend.plenarprotocol;

import backend.database.MongoDBHandler;
import backend.utility.Factory;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO für Plenarprotokolle
 *
 * @author Philipp Hein
 * @date 12.03.2025
 */
public class PlenarprotocolDAO {
    private MongoDBHandler dbHandler;

    /**
     * Standardkonstruktor.
     *
     * @param dbHandler MongoDBHandler
     * @author Philipp Hein
     * @date 12.03.2025
     */
    public PlenarprotocolDAO(MongoDBHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    /**
     * Alternative Konstruktor.
     * Der MongoDBHandler wird hier nicht benötigt.
     */
    public PlenarprotocolDAO() throws IOException {
        this.dbHandler = MongoDBHandler.getInstance();
    }

    /**
     * Gibt das Protokoll anhand der ID zurück
     *
     * @param id Protokoll-ID
     * @return Plenarprotocol_impl Objekt
     * @author Philipp Hein
     * @date 12.03.2025
     */
    public Plenarprotocol_impl getProtocolById(String id) {
        Document doc = dbHandler.findOne("plenarprotocol", Filters.eq("_id", id));
        return Factory.createPlenarprotocol(doc);
    }

    /**
     * Gibt alle Protokolle zurück
     *
     * @return Liste aller Plenarprotocol_impl Objekte
     * @author Philipp Hein
     * @date 12.03.2025
     */
    public List<Plenarprotocol_impl> getAllProtocols() {
        List<Document> docs = dbHandler.findAll("plenarprotocol");
        List<Plenarprotocol_impl> plenarprotokolle = new ArrayList<>();
        for (Document doc : docs) {
            plenarprotokolle.add(Factory.createPlenarprotocol(doc));
        }
        return plenarprotokolle;
    }

    /**
     * Gibt Anzahl der Protokolle zurück
     *
     * @return Anzahl an Plenarprotokollen in der DB
     * @author Philipp Landmann
     * @date 13.03.2025
     */
    public Long getProtocolsAmount() {
        return dbHandler.getPlenarprotocolsCollection().countDocuments();
    }

    /**
     * Simple implementation using find().sort().limit(1) with projection
     * This is more efficient for large collections as it only retrieves the needed field
     *
     * @return The maximum value of protocol.index or 1 if no protocols exist
     * @author Kai
     */
    public Integer getMaxProtocolIndex() {

        // Create a sort document to sort by protocol.index in descending order
        Document sortCriteria = new Document("protocol.index", -1);

        // Create a projection to only return the protocol.index field
        Document projection = new Document("protocol.index", 1);

        // Find the document with the highest protocol.index, only retrieving the necessary field
        Document highestDocument = dbHandler.getPlenarprotocolsCollection().find()
                .projection(projection)
                .sort(sortCriteria)
                .limit(1)
                .first();

        if (highestDocument == null) {
            // No documents found
            return 1;
        }

        // Access the nested field protocol.index
        Document protocol = (Document) highestDocument.get("protocol");
        if (protocol == null || !protocol.containsKey("index")) {
            // protocol field or index field doesn't exist
            return 1;
        }

        return protocol.getInteger("index");
    }
}
