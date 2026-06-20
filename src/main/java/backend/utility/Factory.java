package backend.utility;

import backend.plenarprotocol.Plenarprotocol_impl;
import backend.speaker.Speaker_impl;
import backend.speech.Agenda_impl;
import backend.speech.Speech_impl;
import backend.speech.interfaces.IProtocol;
import org.bson.Document;

/**
 * @author: Philipp Hein #6356965
 * Factory Interface für Erstellung von Objekten aus MongoDB Dokumenten
 */
public interface Factory {

    /**
     * @author: Philipp Hein #6356965
     * @param document BSON Dokument mit Speaker Daten
     * @return Speaker Objekt
     */
    static Speaker_impl createSpeaker(Document document) {
        return Factory_impl.createSpeaker(document);
    }

    /**
     * @author: Philipp Hein #6356965
     * @param document BSON Dokument mit Speech Daten
     * @return Speech Objekt
     */
    static Speech_impl createSpeech(Document document) {
        return Factory_impl.createSpeech(document);
    }

    /**
     * @author: Philipp Hein #6356965
     * @param document BSON Dokument mit Protocol Daten
     * @return Protocol Objekt
     */
    static IProtocol createProtocol(Document document) {
        return Factory_impl.createProtocol(document);
    }

    /**
     * @author: Philipp Hein #6356965
     * @param document BSON Dokument mit Agenda Daten
     * @return Agenda Objekt
     */
    static Agenda_impl createAgenda(Document document) {
        return Factory_impl.createAgenda(document);
    }

    static Plenarprotocol_impl createPlenarprotocol(Document document) {
        return Factory_impl.createPlenarprotocol(document);
    }
}
