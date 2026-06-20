package backend.nlp.extractor;

import backend.linguisticFeatures.Interface.ISentenceFeature;
import backend.linguisticFeatures.SubClasses.SentenceFeature_Impl;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kai
 * @date 05/03/2025
 */
public class SentenceFeatureExtractor implements LinguisticFeatureExtractor<ISentenceFeature> {
    @Override
    public List<ISentenceFeature> extract(JCas jcas) {
        List<ISentenceFeature> sentences = new ArrayList<>();
        JCasUtil.select(jcas, Sentence.class).forEach(sentence ->
                sentences.add(new SentenceFeature_Impl(
                        sentence.getBegin(),
                        sentence.getEnd(),
                        sentence.getCoveredText()
                ))
        );
        return sentences;
    }
}

