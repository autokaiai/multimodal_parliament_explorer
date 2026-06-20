package backend.nlp;

import backend.linguisticFeatures.LinguisticDAO;
import backend.linguisticFeatures.LinguisticFeaturesAggregate_Impl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

public class linguisticDAOTest {
    /**
     * @throws IOException
     * @author Philipp Landmann
     * Testet die Methode {@link LinguisticDAO#findByDocumentId(String)}.
     */
    @Test
    public void testSerialize() throws IOException {
        Optional<LinguisticFeaturesAggregate_Impl> opt = new LinguisticDAO().findByDocumentId("ID2021200100");
        opt.ifPresent(linguisticFeaturesAggregate -> System.out.println(linguisticFeaturesAggregate.getRedeId() + " wurde exportiert."));
    }
}
