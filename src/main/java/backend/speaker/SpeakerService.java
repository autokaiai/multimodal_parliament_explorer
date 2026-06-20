package backend.speaker;

import backend.speech.SpeechDAO;
import backend.speech.Speech_impl;

import java.util.List;

/**
 * Service Klasse für Verarbeitung vom Speaker
 */
public class SpeakerService {

    // DAO-Klassen für den Zugriff auf Daten
    private final SpeakerDAO speakerDAO; // DAO für Speaker-Daten
    private final SpeechDAO speechDAO;   // DAO für Speech-Daten

    /**
     * @author: Philipp Hein #6356965
     * Konstruktor - Initialisierung der DAO-Klassen.
     *
     * @param speakerDAO DAO für Speaker-Daten.
     * @param speechDAO  DAO für Speech-Daten.
     */
    public SpeakerService(SpeakerDAO speakerDAO, SpeechDAO speechDAO) {
        this.speakerDAO = speakerDAO;
        this.speechDAO = speechDAO;
    }

    /**
     * @author: Philipp Hein #6356965
     * Ruft alle Speaker aus MongoDB ab
     *
     * @return Liste aller Speaker als {@link Speaker_impl}.
     */
    public List<Speaker_impl> getAllSpeakers() {
        return speakerDAO.loadAll();
    }

    /**
     * @author: Philipp Hein #6356965
     * Ruft Speaker anhand ID ab.
     *
     * @param speakerId ID des Speakers.
     * @return Speaker als {@link Speaker_impl}, oder {@code null}, wenn kein Speaker gefunden wurde.
     */
    public Speaker_impl getSpeakerByID(String speakerId) {
        return speakerDAO.findById(speakerId);
    }

    /**
     * @author: Philipp Hein #6356965
     * Ruft alle Reden eines Speakers ab.
     *
     * @param speakerId ID des Speakers.
     * @return Liste aller Reden des Speakers als {@link Speech_impl}.
     */
    public List<Speech_impl> getSpeechesBySpeaker(String speakerId) {
        return speechDAO.findBySpeakerId(speakerId);
    }

    /**
     * @author: Philipp Hein #6356965
     * Zählt Reden eines Speakers.
     *
     * @param speakerId ID des Speakers.
     * @return Anzahl der Reden.
     */
    public long getSpeechCountBySpeakerId(String speakerId) {
        return speechDAO.countBySpeakerId(speakerId);
    }
}
