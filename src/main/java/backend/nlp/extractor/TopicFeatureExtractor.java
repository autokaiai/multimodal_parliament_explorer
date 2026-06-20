package backend.nlp.extractor;

import backend.linguisticFeatures.Interface.ITopicFeature;
import backend.linguisticFeatures.SubClasses.TopicFeature_Impl;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.hucompute.textimager.uima.type.category.CategoryCoveredTagged;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kai
 * @date 05/03/2025
 */
public class TopicFeatureExtractor implements LinguisticFeatureExtractor<ITopicFeature> {
    @Override
    public List<ITopicFeature> extract(JCas jcas) {
        List<ITopicFeature> topics = new ArrayList<>();
        JCasUtil.select(jcas, CategoryCoveredTagged.class).forEach(topic ->
                topics.add(new TopicFeature_Impl(
                        topic.getBegin(),
                        topic.getEnd(),
                        topic.getCoveredText(),
                        topic.getValue(),
                        topic.getScore()
                ))
        );
        return topics;
    }
}
