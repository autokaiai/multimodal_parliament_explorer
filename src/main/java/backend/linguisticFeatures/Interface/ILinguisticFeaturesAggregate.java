package backend.linguisticFeatures.Interface;

import backend.linguisticFeatures.SubClasses.*;

import java.util.List;
import java.util.Map;

/**
 * @author Philipp Noah Hein #6356965
 */
public interface ILinguisticFeaturesAggregate {

    MetadataFeature_Impl getMetadata();

    void setMetadata(MetadataFeature_Impl metadata);

    void setMetadata(IMetadataFeature metadata);

    List<SentenceFeature_Impl> getSentences();

    void setSentences(List<ISentenceFeature> sentences);

    // List<PosFeature_Impl> getPosFeatures();
    // void setPosFeatures(List<PosFeature_Impl> posFeatures);


    List<TokenFeature_Impl> getTokens();

    void setTokens(List<ITokenFeature> tokens);

    List<SentimentFeature_Impl> getSentiments();

    void setSentiments(List<ISentimentFeature> sentiments);

    List<NamedEntityFeature_Impl> getNamedEntities();

    void setNamedEntities(List<INamedEntityFeature> namedEntities);

    List<DependencyFeature_Impl> getDependencies();

    void setDependencies(List<IDependencyFeature> dependencies);

    List<TopicFeature_Impl> getTopics();

    void setTopics(List<ITopicFeature> topics);

    // Aggregierte Statistiken
    Map<String, Integer> getPosCounts();

    void setPosCounts(Map<String, Integer> posCounts);

    Map<String, Integer> getNamedEntityCounts();

    void setNamedEntityCounts(Map<String, Integer> namedEntityCounts);

    Map<String, Integer> getDependencyCounts();

    void setDependencyCounts(Map<String, Integer> dependencyCounts);

    Map<String, Integer> getLemmaFrequency();

    void setLemmaFrequency(Map<String, Integer> lemmaFrequency);

    double getOverallSentiment();

    void setOverallSentiment(double overallSentiment);

    Map<String, Double> getSentimentDistribution();

    void setSentimentDistribution(Map<String, Double> sentimentDistribution);

    Map<String, Integer> getTopicCounts();

    void setTopicCounts(Map<String, Integer> topicCounts);

    List<AudioTokenFeature_Impl> getAudioTokens();

    void setAudioTokens(List<IAudioTokenFeature> audioTokens);

    String getRedeId();

    void setRedeId(String redeId);

    List<PosFeature_Impl> getPosFeatures();

    void setPosFeatures(List<IPosFeature> posFeatures);
}
