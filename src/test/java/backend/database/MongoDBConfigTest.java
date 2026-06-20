package backend.database; // Muss mit dem Package des Codes übereinstimmen

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Philipp Noah Hein #6356965
 * <p>
 * Testklasse für die MongoDBConfig-Klasse.
 */
class MongoDBConfigTest {

    private MongoDBConfig config;

    /**
     * Wird vor jedem Test ausgeführt.
     * Initialisiert eine neue Instanz von MongoDBConfig.
     */
    @BeforeEach
    void setUp() throws IOException {
        config = new MongoDBConfig();
    }

    /**
     * Testet, ob die Konfiguration erfolgreich geladen wird.
     */
    @Test
    void testConfigNotNull() {
        assertNotNull(config, "MongoDBConfig darf nicht null sein!");
    }

    /**
     * Testet, ob der Host-Wert nicht null oder leer ist.
     */
    @Test
    void testHostNotNull() {
        assertNotNull(config.getHost(), "Host darf nicht null sein!");
        assertFalse(config.getHost().isEmpty(), "Host darf nicht leer sein!");
    }

    /**
     * Testet, ob der Datenbankname nicht null oder leer ist.
     */
    @Test
    void testDatabaseNotNull() {
        assertNotNull(config.getDatabase(), "Datenbankname darf nicht null sein!");
        assertFalse(config.getDatabase().isEmpty(), "Datenbankname darf nicht leer sein!");
    }

    /**
     * Testet, ob der Benutzername nicht null oder leer ist.
     */
    @Test
    void testUserNotNull() {
        assertNotNull(config.getUser(), "Benutzername darf nicht null sein!");
        assertFalse(config.getUser().isEmpty(), "Benutzername darf nicht leer sein!");
    }

    /**
     * Testet, ob das Passwort nicht null oder leer ist.
     */
    @Test
    void testPasswordNotNull() {
        assertNotNull(config.getPassword(), "Passwort darf nicht null sein!");
        assertFalse(config.getPassword().isEmpty(), "Passwort darf nicht leer sein!");
    }

    /**
     * Testet, ob der Port-Wert nicht null oder leer ist.
     */
    @Test
    void testPortNotNull() {
        assertNotNull(config.getPort(), "Port darf nicht null sein!");
        assertFalse(config.getPort().isEmpty(), "Port darf nicht leer sein!");
    }

    /**
     * Testet, ob die URI korrekt generiert wird.
     */
    @Test
    void testURIGeneration() {
        String uri = config.getURI();
        assertNotNull(uri, "URI darf nicht null sein!");
        assertTrue(uri.startsWith("mongodb://"), "URI-Format ist ungültig!");
    }
}
