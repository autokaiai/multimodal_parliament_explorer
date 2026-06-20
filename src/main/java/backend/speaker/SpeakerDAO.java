package backend.speaker;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static backend.utility.Factory_impl.createSpeaker;

/**
 * Data Access Object (DAO) für Speaker
 * Bietet Methoden für CRUD Operationen in MongoDB
 */
public class SpeakerDAO {

    // Collection für Speaker-Dokumente
    private final MongoCollection<Document> speakerCollection;

    /**
     * Konstruktor für SpeakerDAO Klasse.
     * @author: Philipp Hein #6356965
     * @param speakerCollection MongoDB Collection für Speaker Dokumente.
     */
    public SpeakerDAO(MongoCollection<Document> speakerCollection) {
        this.speakerCollection = speakerCollection;
    }

    /**
     * @author: Philipp Hein #6356965
     * Findet Speaker anhand ID.
     *
     * @param id ID Speaker
     * @return Speaker als {@link Speaker_impl} oder {@code null}, wenn kein Speaker gefunden
     */
    public Speaker_impl findById(String id) {
        Document doc = speakerCollection.find(new Document("_id", id)).first(); // Suche nach ID
        if (doc == null) {
            return null;
        }
        return createSpeaker(doc); // Speaker-Objekt erstellen
    }

    /**
     * @author: Philipp Hein #6356965
     * Lädt alle Speaker aus MongoDB
     *
     * @return Liste aller Speaker als {@link Speaker_impl}.
     */
    public List<Speaker_impl> loadAll() {
        List<Speaker_impl> speakers = new ArrayList<>();
        try (MongoCursor<Document> cursor = speakerCollection.find().iterator()) { // Iteriere über alle Dokumente
            while (cursor.hasNext()) {
                speakers.add(createSpeaker(cursor.next()));
            }
        }
        return speakers; // Rückgabe der Liste der Speaker
    }
}
