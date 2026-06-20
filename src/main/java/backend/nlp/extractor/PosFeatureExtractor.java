package backend.nlp.extractor;

import backend.linguisticFeatures.Interface.IPosFeature;
import backend.linguisticFeatures.SubClasses.PosFeature_Impl;
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kai
 * @date 05/03/2025
 */
public class PosFeatureExtractor implements LinguisticFeatureExtractor<IPosFeature> {
    @Override
    public List<IPosFeature> extract(JCas jcas) {
        List<IPosFeature> posFeatures = new ArrayList<>();
        JCasUtil.select(jcas, POS.class).forEach(pos ->
                posFeatures.add(new PosFeature_Impl(
                        pos.getBegin(),         // Start position of the POS annotation
                        pos.getEnd(),           // End position of the POS annotation
                        pos.getCoveredText(),   // The text covered by this annotation
                        pos.getPosValue(),     // The POS tag value (e.g., "NN", "VB")
                        pos.getCoarseValue()  // The coarse POS tag value (e.g., "N", "V")
                ))
        );
        return posFeatures;
    }
}
