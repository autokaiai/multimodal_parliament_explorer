package backend.nlp.extractor;

import backend.linguisticFeatures.SubClasses.MetadataFeature_Impl;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

/**
 * @author Kai
 * @date 05/03/2025
 */
public class MetadataFeatureExtractor {

    public MetadataFeature_Impl extract(JCas jcas) throws CASException {
        DocumentMetaData metaData = JCasUtil.selectSingle(jcas, DocumentMetaData.class);
        return new MetadataFeature_Impl(
                metaData.getDocumentTitle(),
                metaData.getLanguage(),
                metaData.getDocumentId()
        );
    }
}