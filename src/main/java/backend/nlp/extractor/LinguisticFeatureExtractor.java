package backend.nlp.extractor;

import org.apache.uima.cas.CASException;
import org.apache.uima.jcas.JCas;

import java.util.List;

/**
 * Interface for extracting linguistic features from a JCas instance.
 *
 * @param <T> The type of linguistic features to be extracted.
 * @author Kai
 */
public interface LinguisticFeatureExtractor<T> {
    /**
     * Extract linguistic features from a JCas instance.
     *
     * @param jcas The JCas instance containing the linguistic data.
     * @return A list of extracted linguistic features of type T.
     */
    List<T> extract(JCas jcas) throws CASException;
}