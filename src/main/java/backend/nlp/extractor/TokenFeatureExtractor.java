package backend.nlp.extractor;

import backend.linguisticFeatures.Interface.ITokenFeature;
import backend.linguisticFeatures.SubClasses.TokenFeature_Impl;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Kai
 * @date 05/03/2025
 */
public class TokenFeatureExtractor implements LinguisticFeatureExtractor<ITokenFeature> {
    @Override
    public List<ITokenFeature> extract(JCas jcas) {
        List<ITokenFeature> tokens = new ArrayList<>();
        JCasUtil.select(jcas, Token.class).forEach(token ->
                tokens.add(new TokenFeature_Impl(
                        token.getBegin(),
                        token.getEnd(),
                        token.getCoveredText(),
                        token.getLemma().getValue(),
                        token.getPos().getPosValue()
                ))
        );
        return tokens;
    }
}
