package backend.speech;

import backend.linguisticFeatures.TopicDAO;
import backend.speech.interfaces.ISpeech;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SpeechService
{

    private final SpeechDAO speechDAO;

    public SpeechService(SpeechDAO speechDAO)
    {
        this.speechDAO = speechDAO;
    }

    /**
     * @author: Philipp Hein #6356965
     * Liefert alle Reden aus der Collection speeches_1.
     *
     * @return Liste der Reden
     */
    public List<Speech_impl> getAllSpeeches()
    {
        return speechDAO.findAll();
    }


    /**
     * @author: Philipp Hein #6356965
     * Liefert eine Rede anhand der ID.
     *
     * @param speechId ID der Rede
     * @return Speech_impl oder null, wenn nicht gefunden.
     */
    public Speech_impl getSpeechById(String speechId)
    {
        return speechDAO.findById(speechId);
    }


     /**
     * Liefert alle Reden paginiert zurück, mit Filter nach einer Fraktion und/oder mehreren Topics
     *
     * @param page     Seitennummer (beginnend bei 1)
     * @param pageSize Anzahl der Einträge pro Seite
     * @param faction Optional: Filter nach einer Fraktion
     * @param topics Optional: Filter nach mehreren Topics
     * @return Liste von Speech_impl Objekten für die aktuelle Seite
     */
     public List<Speech_impl> getAllSpeechesPaginated(int page, int pageSize, String faction, List<String> topics, String speakerSearch) {
         Set<String> speechIds = null;

         try {
             // Wenn Topics angegeben wurden, hole die Reden-IDs für diese Topics
             if (topics != null && !topics.isEmpty()) {
                 TopicDAO topicDAO = new TopicDAO();
                 speechIds = topicDAO.getSpeechIdsByTopics(topics);

                 // Wenn keine passenden Reden gefunden wurden, geben wir eine leere Liste zurück
                 if (speechIds.isEmpty()) {
                     return new ArrayList<>();
                 }
             }
         } catch (IOException e) {
             System.err.println("Fehler beim Abrufen der Topics: " + e.getMessage());
             e.printStackTrace();
             // Falls ein Fehler auftritt, ignorieren wir den Topic-Filter
         }

         // Übergibt den zusätzlichen speakerSearch-Parameter an die DAO-Methode
         return speechDAO.findAllPaginated(page, pageSize, speechIds, faction, speakerSearch);
     }

    /**
     * Zählt die Gesamtanzahl aller Reden, gefiltert nach einer Fraktion und/oder mehreren Topics
     *
     * @param faction Optional: Filter nach einer Fraktion
     * @param topics Optional: Filter nach mehreren Topics
     * @return Gesamtanzahl der Reden
     */
    public long countSpeeches(String faction, List<String> topics) {
        Set<String> speechIds = null;

        try {
            // Wenn Topics angegeben wurden, hole die Reden-IDs für diese Topics
            if (topics != null && !topics.isEmpty()) {
                TopicDAO topicDAO = new TopicDAO();
                speechIds = topicDAO.getSpeechIdsByTopics(topics);

                // Wenn keine passenden Reden gefunden wurden, geben wir 0 zurück
                if (speechIds.isEmpty()) {
                    return 0;
                }
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Abrufen der Topics: " + e.getMessage());
            e.printStackTrace();
            // Falls ein Fehler auftritt, ignorieren wir den Topic-Filter
        }

        // Rufe die DAO-Methode mit Speeches-IDs und einer Fraktion auf
        return speechDAO.count(speechIds, faction);
    }
}