package backend.nlp.extractor;

import backend.linguisticFeatures.Interface.IAudioTokenFeature;
import backend.linguisticFeatures.SubClasses.AudioTokenFeature_Impl;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.texttechnologylab.annotation.type.AudioToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts AudioToken features from the JCas.
 *
 * @author Kai
 */
public class AudioTokenFeatureExtractor implements LinguisticFeatureExtractor<IAudioTokenFeature> {

    @Override
    public List<IAudioTokenFeature> extract(JCas jcas) throws CASException {
        List<IAudioTokenFeature> audioTokens = new ArrayList<>();
        JCasUtil.select(jcas, AudioToken.class).forEach(audioToken ->
                audioTokens.add(new AudioTokenFeature_Impl(
                        audioToken.getBegin(),
                        audioToken.getEnd(),
                        audioToken.getValue(),
                        audioToken.getTimeStart(),
                        audioToken.getTimeEnd()
                ))
        );
        return audioTokens;
    }
}