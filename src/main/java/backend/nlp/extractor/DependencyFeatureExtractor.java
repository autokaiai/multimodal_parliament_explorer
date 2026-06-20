package backend.nlp.extractor;

import backend.linguisticFeatures.Interface.IDependencyFeature;
import backend.linguisticFeatures.SubClasses.DependencyFeature_Impl;
import de.tudarmstadt.ukp.dkpro.core.api.syntax.type.dependency.Dependency;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kai
 * @date 05/03/2025
 */
public class DependencyFeatureExtractor implements LinguisticFeatureExtractor<IDependencyFeature> {
    @Override
    public List<IDependencyFeature> extract(JCas jcas) {
        List<IDependencyFeature> dependencies = new ArrayList<>();
        JCasUtil.select(jcas, Dependency.class).forEach(dep ->
                dependencies.add(new DependencyFeature_Impl(
                        dep.getBegin(),
                        dep.getEnd(),
                        dep.getCoveredText(),
                        dep.getDependencyType(),
                        dep.getGovernor() != null ? dep.getGovernor().getCoveredText() : "null",
                        dep.getDependent() != null ? dep.getDependent().getCoveredText() : "null"
                ))
        );
        return dependencies;
    }
}
