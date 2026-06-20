package backend.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Testklasse für MongoDBHandler.
 */
class MongoDBHandlerTest {

    private MongoDBHandler mongoDBHandler;

    /**
     * Initialisiert den MongoDBHandler vor jedem Test.
     *
     * @throws IOException falls die Konfigurationsdatei nicht geladen werden kann.
     */
    @BeforeEach
    void setUp() throws IOException {
        mongoDBHandler = new MongoDBHandler();
    }

    /**
     * Testet, ob die Verbindung zur MongoDB hergestellt wird.
     */
    @Test
    void testMongoDBConnection() {
        MongoDatabase database = mongoDBHandler.connect();
        assertNotNull(database, "Die Verbindung zur MongoDB ist fehlgeschlagen!");
    }

    /**
     * Testet, ob die "speaker"-Collection korrekt abgerufen wird.
     */
    @Test
    void testSpeakerCollectionNotNull() {
        mongoDBHandler.connect();
        MongoCollection<Document> speakerCollection = mongoDBHandler.getSpeakerCollection();
        assertNotNull(speakerCollection, "Die Speaker-Collection darf nicht null sein!");
    }

    /**
     * Testet, ob die "speech"-Collection korrekt abgerufen wird.
     */
    @Test
    void testSpeechCollectionNotNull() {
        mongoDBHandler.connect();
        MongoCollection<Document> speechCollection = mongoDBHandler.getSpeechCollection();
        assertNotNull(speechCollection, "Die Speech-Collection darf nicht null sein!");
    }
}
