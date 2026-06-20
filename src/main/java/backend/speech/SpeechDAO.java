package backend.speech;

import backend.database.MongoDBHandler;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static backend.utility.Factory.createSpeech;

/**
 * Data Access Object (DAO) für Speech Dokumente
 */
public class SpeechDAO {

    // Speech Speicher
    private final MongoCollection<Document> speechCollection;

    /**
     * Konstruktor
     * @author: Philipp Hein #6356965
     * @param speechCollection MongoDB Collection für Speech Dokumente
     */
    public SpeechDAO(MongoCollection<Document> speechCollection) {
        this.speechCollection = speechCollection;
    }

    /**
     * Default constructor that uses the pooled connection.
     *
     * @author Kai
     */
    public SpeechDAO() {
        try {
            MongoDBHandler mongoDbHandler = MongoDBHandler.getInstance();
            this.speechCollection = mongoDbHandler.getSpeechCollection();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize SpeechDAO", e);
        }
    }

    /**
     *  @author Philipp Schneider
     * Findet alle Reden eines Speakers anhand Speaker ID
     *
     * @param speakerId ID des Speakers
     * @return Liste Reden
     */
    public List<Speech_impl> findBySpeakerId(String speakerId) {
        List<Speech_impl> speeches = new ArrayList<>();
        try (MongoCursor<Document> cursor = speechCollection.find(new Document("speaker", speakerId)).iterator()) {
            while (cursor.hasNext()) {
                speeches.add(createSpeech(cursor.next()));
            }
        }
        return speeches;
    }

    /**
     *  @author Philipp Schneider
     * Zählt alle Reden eines Speakers anhand Speaker ID
     *
     * @param speakerId ID des Speakers
     * @return Anzahl Reden
     */
    public long countBySpeakerId(String speakerId) {
        return speechCollection.countDocuments(new Document("speaker", speakerId));
    }


    /**
     * Findet eine Rede anhand der ID - ohne Video-Daten
     * @author: Philipp Hein #6356965
     * @param speechId ID der Rede
     * @return Speech_impl oder null, wenn nichts gefunden wurde
     */
    public Speech_impl findById(String speechId) {
        Document doc = speechCollection.find(new Document("_id", speechId))
                .first();
        if (doc != null) {
            return createSpeech(doc);
        }
        return null;
    }


    /**
     *  @author Philipp Schneider
     * Liefert alle Reden aus der Collection speeches_1.
     *
     * @return Liste der Reden
     */
    public List<Speech_impl> findAll() {
        List<Speech_impl> speeches = new ArrayList<>();
        try (MongoCursor<Document> cursor = speechCollection.find().iterator()) {
            while (cursor.hasNext()) {
                speeches.add(createSpeech(cursor.next()));
            }
        }
        return speeches;
    }


    /**
     *  @author Philipp Schneider
     * Liefert alle Reden mit Paginierung und Filter nach einer Fraktion und/oder mehreren Topics
     * @param page     Seitennummer (beginnend bei 1)
     * @param pageSize Anzahl der Einträge pro Seite
     * @param ids Optional: Filter nach Reden-IDs (für Topic-Filter)
     * @param faction Optional: Filter nach einer Fraktion
     * @return Liste aller Reden für die angegebene Seite
     */
    public List<Speech_impl> findAllPaginated(int page, int pageSize, Set<String> ids, String faction, String speakerSearch) {
        int skip = (page - 1) * pageSize;
        List<Speech_impl> speeches = new ArrayList<>();
        MongoCursor<Document> cursor = null;

        try {
            // Sortierung: Erst nach Datum (falls vorhanden) und dann nach _id
            Bson sort = Sorts.orderBy(Sorts.descending("protocol.date"), Sorts.ascending("_id"));

            // Basisfilter erstellen
            List<Bson> allFilters = new ArrayList<>();

            // Filter: Reden-IDs (für Topics), falls vorhanden
            if (ids != null && !ids.isEmpty()) {
                List<Bson> idFilters = new ArrayList<>();
                for (String id : ids) {
                    idFilters.add(Filters.eq("_id", id));
                }
                allFilters.add(Filters.or(idFilters.toArray(new Bson[0])));
            }

            // Filter: Fraktion, falls vorhanden
            if (faction != null && !faction.isEmpty()) {
                allFilters.add(Filters.eq("speakerInfo.party", faction));
            }

            // Filter: Suche nach Redner (sucht in "name" und "fristName")
            if (speakerSearch != null && !speakerSearch.isEmpty()) {
                Pattern regex = Pattern.compile(speakerSearch, Pattern.CASE_INSENSITIVE);
                allFilters.add(Filters.or(
                        Filters.regex("speakerInfo.name", regex),
                        Filters.regex("speakerInfo.fristName", regex)
                ));
            }

            // Finalen Filter zusammenbauen
            Bson finalFilter = null;
            if (allFilters.isEmpty()) {
                // Keine Filter – alle Dokumente zurückgeben
                cursor = speechCollection.find()
                        .sort(sort)
                        .skip(skip)
                        .limit(pageSize)
                        .iterator();
            } else if (allFilters.size() == 1) {
                // Nur ein Filter (z. B. nur Fraktion oder nur Reden-IDs)
                finalFilter = allFilters.get(0);
                cursor = speechCollection.find(finalFilter)
                        .sort(sort)
                        .skip(skip)
                        .limit(pageSize)
                        .iterator();
            } else {
                // Mehrere Filter kombinieren (IDs UND/oder Fraktion UND/oder Redner)
                finalFilter = Filters.and(allFilters.toArray(new Bson[0]));
                cursor = speechCollection.find(finalFilter)
                        .sort(sort)
                        .skip(skip)
                        .limit(pageSize)
                        .iterator();
            }

            // Ergebnisse verarbeiten
            while (cursor.hasNext()) {
                speeches.add(createSpeech(cursor.next()));
            }
            cursor.close();

            System.out.println("Query with filter " + finalFilter + " returned " + speeches.size() + " speeches");
            return speeches;
        } catch (Exception e) {
            System.err.println("Fehler beim paginierten Abrufen der Reden: " + e.getMessage());
            e.printStackTrace();
            return speeches;
        }
    }


    /**
     * @author Philipp Schneider
     * Zählt die Gesamtanzahl der Reden mit Filter nach einer Fraktion und/oder mehreren Topics
     *
     * @param ids Optional: Filter nach Reden-IDs (für Topic-Filter)
     * @param faction Optional: Filter nach einer Fraktion
     * @return Anzahl der Reden
     */
    public long count(Set<String> ids, String faction) {
        try {
            List<Bson> allFilters = new ArrayList<>();

            // Filter nach Reden-IDs (für Topics), falls vorhanden
            if (ids != null && !ids.isEmpty()) {
                List<Bson> idFilters = new ArrayList<>();
                for (String id : ids) {
                    idFilters.add(Filters.eq("_id", id));
                }
                allFilters.add(Filters.or(idFilters.toArray(new Bson[0])));
            }

            // Filter nach einer Fraktion, falls vorhanden
            if (faction != null && !faction.isEmpty()) {
                allFilters.add(Filters.eq("speakerInfo.party", faction));
            }

            // finalen Filter
            Bson finalFilter = null;

            if (allFilters.isEmpty()) {
                // Keine Filter - alle Dokumente zählen
                return speechCollection.countDocuments();
            } else if (allFilters.size() == 1) {
                // Nur ein Filter (entweder IDs oder Fraktion)
                finalFilter = allFilters.get(0);
            } else {
                // Beide Filter (IDs UND Fraktion)
                finalFilter = Filters.and(allFilters.toArray(new Bson[0]));
            }

            long count = speechCollection.countDocuments(finalFilter);
            System.out.println("Count with filter " + finalFilter + " returned " + count + " speeches");

            return count;
        } catch (Exception e) {
            System.err.println("Fehler beim Zählen der Reden: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public boolean delete(String speechId) {
        // Löscht ein Speech-Dokument anhand der _id
        return speechCollection.deleteOne(Filters.eq("_id", speechId)).getDeletedCount() > 0;
    }

    public boolean update(String speechId, Speech_impl updatedSpeech) {

        Document updateDoc = updatedSpeech.toDocument();

        // Entferne das _id-Feld, da es nicht aktualisiert werden soll
        updateDoc.remove("_id");

        // Erstelle den Update-Operator ($set) mit den neuen Feldern
        Bson updateOperation = new Document("$set", updateDoc);

        // Führe das Update durch und gib true zurück, wenn mindestens ein Dokument modifiziert wurde
        return speechCollection.updateOne(Filters.eq("_id", speechId), updateOperation).getModifiedCount() > 0;
    }


}