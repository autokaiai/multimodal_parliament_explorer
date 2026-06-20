package backend.nlp.extractor;

import backend.linguisticFeatures.Interface.INamedEntityFeature;
import backend.linguisticFeatures.SubClasses.NamedEntityFeature_Impl;
import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kai
 * @date 05/03/2025
 */
public class NamedEntityFeatureExtractor implements LinguisticFeatureExtractor<INamedEntityFeature> {
    @Override
    public List<INamedEntityFeature> extract(JCas jcas) {
        List<INamedEntityFeature> entities = new ArrayList<>();
        JCasUtil.select(jcas, NamedEntity.class).forEach(entity ->
                entities.add(new NamedEntityFeature_Impl(
                        entity.getBegin(),
                        entity.getEnd(),
                        entity.getCoveredText(),
                        entity.getValue()
                ))
        );
        return entities;
    }
}
