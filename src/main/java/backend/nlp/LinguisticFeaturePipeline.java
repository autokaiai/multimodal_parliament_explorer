package backend.nlp;


import backend.linguisticFeatures.Interface.*;
import backend.linguisticFeatures.LinguisticDAO;
import backend.linguisticFeatures.LinguisticFeaturesAggregate_Impl;
import backend.linguisticFeatures.LinguisticTranskriptDAO;
import backend.nlp.extractor.*;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.util.List;

/**
 * Pipeline zur Verarbeitung und Speicherung linguistischer Features.
 *
 * @author Kai
 */
public class LinguisticFeaturePipeline {

    private final LinguisticTranskriptDAO transkriptDAO;
    private final LinguisticDAO textDao;
    private LinguisticFeaturesAggregate_Impl linguisticFeaturesAggregate;

    public LinguisticFeaturePipeline(LinguisticDAO linguisticDAO, LinguisticTranskriptDAO linguisticTranskriptDAO) throws IOException {
        this.textDao = linguisticDAO;
        this.transkriptDAO = linguisticTranskriptDAO;
    }

    void processJCAS(JCas jcas, TypeOfImport typeOfImport) throws CASException, IOException {

        // Create LinguisiticFeaturesAggregate
        linguisticFeaturesAggregate = new LinguisticFeaturesAggregate_Impl();

        // Metadaten extrahieren und speichern
        MetadataFeatureExtractor metadataExtractor = new MetadataFeatureExtractor();
        IMetadataFeature metadata = metadataExtractor.extract(jcas);
        String documentId = metadata.getDocumentId();
        linguisticFeaturesAggregate.setMetadata(metadata);
        linguisticFeaturesAggregate.setRedeId(documentId);

        // Satz-Features extrahieren und speichern
        SentenceFeatureExtractor sentenceExtractor = new SentenceFeatureExtractor();
        List<ISentenceFeature> sentences = sentenceExtractor.extract(jcas);
        linguisticFeaturesAggregate.setSentences(sentences);

        // POS-Features extrahieren und speichern
        PosFeatureExtractor posExtractor = new PosFeatureExtractor();
        List<IPosFeature> posFeatures = posExtractor.extract(jcas);
        linguisticFeaturesAggregate.setPosFeatures(posFeatures);

        // Token-Features extrahieren und speichern
        TokenFeatureExtractor tokenExtractor = new TokenFeatureExtractor();
        List<ITokenFeature> tokens = tokenExtractor.extract(jcas);
        linguisticFeaturesAggregate.setTokens(tokens);

        // Sentiment-Features extrahieren und speichern
        SentimentFeatureExtractor sentimentExtractor = new SentimentFeatureExtractor();
        List<ISentimentFeature> sentiments = sentimentExtractor.extract(jcas);
        linguisticFeaturesAggregate.setSentiments(sentiments);

        // Named Entity-Features extrahieren und speichern
        NamedEntityFeatureExtractor neExtractor = new NamedEntityFeatureExtractor();
        List<INamedEntityFeature> namedEntities = neExtractor.extract(jcas);
        linguisticFeaturesAggregate.setNamedEntities(namedEntities);

        // Dependency-Features extrahieren und speichern
        DependencyFeatureExtractor depExtractor = new DependencyFeatureExtractor();
        List<IDependencyFeature> dependencies = depExtractor.extract(jcas);
        linguisticFeaturesAggregate.setDependencies(dependencies);

        // Audio Token-Features extrahieren und speichern
        JCas tCas = JCasUtil.getView(jcas, "transcript", true);
        AudioTokenFeatureExtractor audioExtractor = new AudioTokenFeatureExtractor();
        List<IAudioTokenFeature> audioTokens = audioExtractor.extract(tCas);
        linguisticFeaturesAggregate.setAudioTokens(audioTokens);

        // Topic-Features extrahieren und speichern
        TopicFeatureExtractor topicExtractor = new TopicFeatureExtractor();
        List<ITopicFeature> topics = topicExtractor.extract(jcas);
        linguisticFeaturesAggregate.setTopics(topics);

        // Feature stats berechnen
        linguisticFeaturesAggregate.calculateStatistics();

        // Save to DB
        if (typeOfImport == TypeOfImport.CALCULATED_TRANSCRIPT) {
            transkriptDAO.update(linguisticFeaturesAggregate);
        } else {
            textDao.update(linguisticFeaturesAggregate);
        }
    }

    // Type of import enum
    public enum TypeOfImport {
        PROVIDED,
        CALCULATED_TEXT,
        CALCULATED_TRANSCRIPT
    }
}
