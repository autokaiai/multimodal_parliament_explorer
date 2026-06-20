package backend.nlp.extractor;

import backend.linguisticFeatures.Interface.ISentimentFeature;
import backend.linguisticFeatures.SubClasses.SentimentFeature_Impl;
import backend.nlp.util.SafeCoveredTextExtractor;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.hucompute.textimager.uima.type.GerVaderSentiment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kai
 * @date 05/03/2025
 */
public class SentimentFeatureExtractor implements LinguisticFeatureExtractor<ISentimentFeature> {
    @Override
    public List<ISentimentFeature> extract(JCas jcas) {
        List<ISentimentFeature> sentiments = new ArrayList<>();
        JCasUtil.select(jcas, GerVaderSentiment.class).forEach(sentiment ->
                sentiments.add(new SentimentFeature_Impl(
                        sentiment.getBegin(),
                        SafeCoveredTextExtractor.getSafeEnd(sentiment, jcas.getDocumentText()),
                        SafeCoveredTextExtractor.getSafeCoveredText(sentiment, jcas.getDocumentText()),
                        sentiment.getSentiment(),
                        sentiment.getPos(),
                        sentiment.getNeu(),
                        sentiment.getNeg(),
                        sentiment.getSubjectivity()
                ))
        );
        return sentiments;
    }
}
