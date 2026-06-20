package backend.speech;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testklasse für SpeechDAO.
 */
@ExtendWith(MockitoExtension.class)
public class SpeechDAOTest {

    @Mock
    private MongoCollection<Document> mockCollection;

    @InjectMocks
    private SpeechDAO speechDAO;

    @Test
    void testFindByIdWithScreenshotData() {
        // ----------------------
        // Testdaten vorbereiten
        // ----------------------

        String speechId = "ID2017110560";

        Document dummyDoc = new Document("_id", speechId)
                .append("speaker", "99999912")
                .append("text", "Ich glaube, das ist tatsächlich ein sehr gutes Beispiel für die gelung...");

        // textContent
        List<Document> textContentList = new ArrayList<>();

        Document textContentDoc1 = new Document()
                .append("id", "ID2017110560--1905788692")
                .append("speaker", "99999912")
                .append("text", "Ich glaube, das ist tatsächlich ein sehr gutes Beispiel für die gelung...")
                .append("type", "text");
        textContentList.add(textContentDoc1);

        Document textContentDoc2 = new Document()
                .append("id", "ID2017110560--1905788693")
                .append("speaker", "99999912")
                .append("text", "Zweiter Eintrag im TextContent")
                .append("type", "text");
        textContentList.add(textContentDoc2);

        dummyDoc.append("textContent", textContentList);

        // protocol
        Document protocolDoc = new Document()
                .append("date", 1717538400000L)
                .append("starttime", 1717558200000L)
                .append("endtime", 1717609200000L)
                .append("index", 171)
                .append("title", "Plenarprotokoll 20/171")
                .append("place", "Berlin")
                .append("wp", 20);
        dummyDoc.append("protocol", protocolDoc);

        // "agenda": Objekt mit index, id, title
        Document agendaDoc = new Document()
                .append("index", "Tagesordnungspunkt 1")
                .append("id", "Tagesordnungspunkt_1Ich_rufe_nun_auf_den_Tagesordnungspunkt_1")
                .append("title", "Ich rufe nun auf den Tagesordnungspunkt 1");
        dummyDoc.append("agenda", agendaDoc);

        // ----------------------
        // 2) Mockito simulieren
        // ----------------------
        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(mockCollection.find(any(Document.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(dummyDoc);

        // ----------------------
        // 3) Methode aufrufen
        // ----------------------
        Speech_impl speech = speechDAO.findById(speechId);

        // ----------------------
        // 4) Ergebnisse prüfen
        // ----------------------
        assertNotNull(speech, "Es sollte ein gültiges Speech_impl-Objekt zurückgegeben werden.");
        assertEquals(speechId, speech.get_id(), "Die Speech-ID sollte übereinstimmen.");
        assertEquals("99999912", speech.getSpeaker(), "Speaker-ID sollte übereinstimmen.");
        assertEquals("Ich glaube, das ist tatsächlich ein sehr gutes Beispiel für die gelung...",
                speech.getText(),
                "Der Rede-Text sollte übereinstimmen.");

        // Prüfe textContent
        assertNotNull(speech.getTextContent(), "TextContent sollte nicht null sein.");
        assertEquals(2, speech.getTextContent().size(),
                "TextContent sollte 2 Einträge haben (gemäß unserem Dummy).");

        // Protokoll prüfen
        assertNotNull(speech.getProtocol(), "Protocol sollte nicht null sein.");
        assertEquals("Plenarprotokoll 20/171", speech.getProtocol().getTitle(),
                "Der Protocol-Titel sollte übereinstimmen.");
        assertEquals("Berlin", speech.getProtocol().getPlace(), "Der Ort sollte übereinstimmen.");
        assertEquals(20, speech.getProtocol().getWp(), "Die Wahlperiode (wp) sollte übereinstimmen.");

        // Agenda prüfen
        assertNotNull(speech.getAgenda(), "Agenda sollte nicht null sein.");
        assertEquals("Tagesordnungspunkt 1", speech.getAgenda().getIndex(),
                "Agenda-Index sollte übereinstimmen.");
        assertEquals("Tagesordnungspunkt_1Ich_rufe_nun_auf_den_Tagesordnungspunkt_1",
                speech.getAgenda().getId(),
                "Agenda-ID sollte übereinstimmen.");
        assertEquals("Ich rufe nun auf den Tagesordnungspunkt 1",
                speech.getAgenda().getTitle(),
                "Agenda-Titel sollte übereinstimmen.");

        // Wenn kein speakerInfo-Feld vorhanden ist, dürfte speech.getSpeakerObject() == null sein.
        assertNull(speech.getSpeakerObject(), "Ohne speakerInfo bleibt das Speaker-Objekt null.");
    }

    @Test
    void testFindByIdReturnsNullWhenNotFound() {
        // Arrange
        String speechId = "nonexistent";
        FindIterable<Document> findIterable = mock(FindIterable.class);
        when(mockCollection.find(any(Document.class))).thenReturn(findIterable);
        when(findIterable.first()).thenReturn(null);

        // Act
        Speech_impl result = speechDAO.findById(speechId);

        // Assert
        assertNull(result, "Erwartet null, wenn kein Dokument gefunden wird.");
    }
}
