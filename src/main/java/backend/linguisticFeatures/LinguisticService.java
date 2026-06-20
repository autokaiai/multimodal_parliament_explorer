package backend.linguisticFeatures;

import java.io.IOException;
import java.util.Optional;

/**
 * @author Philipp Noah Hein #6356965
 */
public class LinguisticService {

    private final LinguisticDAO linguisticDAO;

    /**
     * Initialisiert den Service und setzt die DAO.
     *
     * @throws IOException Falls die DAO nicht geladen werden kann.
     */
    public LinguisticService() throws IOException {
        this.linguisticDAO = new LinguisticDAO();
    }

    /**
     * Erstellt ein neues LinguisticFeature und speichert es in der Datenbank.
     *
     * @param linguisticFeaturesImpl Das zu speichernde Objekt.
     */
    public void createLinguisticFeatures(LinguisticFeaturesAggregate_Impl linguisticFeaturesImpl) {
        linguisticDAO.save(linguisticFeaturesImpl);
    }

    /**
     * Ruft ein LinguisticFeature anhand der documentId ab.
     *
     * @param documentId Die ID des Dokuments.
     * @return Das gefundene LinguisticFeaturesAggregate oder null.
     */
    public LinguisticFeaturesAggregate_Impl getLinguisticFeatures(String documentId) {
        Optional<LinguisticFeaturesAggregate_Impl> result = linguisticDAO.findByDocumentId(documentId);
        return result.orElse(null);
    }

    /**
     * Aktualisiert vorhandenes LinguisticFeature.
     *
     * @param documentId  Die ID des Dokuments.
     * @param updatedData Die aktualisierten Daten.
     */
    public void updateLinguisticFeatures(String documentId, LinguisticFeaturesAggregate_Impl updatedData) {
        linguisticDAO.update(updatedData);
    }

    /**
     * Löscht LinguisticFeatures anhand der documentId.
     *
     * @param documentId Die ID des Dokuments.
     */
    public void deleteLinguisticFeatures(String documentId) {
        linguisticDAO.delete(documentId);
    }

    /**
     * Schließt die Datenbankverbindung.
     */
   // public void close() {
    //    linguisticDAO.close();
    //}
}
