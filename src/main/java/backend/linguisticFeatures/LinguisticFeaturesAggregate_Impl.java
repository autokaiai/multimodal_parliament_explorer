package backend.linguisticFeatures;

import backend.linguisticFeatures.Interface.*;
import backend.linguisticFeatures.SubClasses.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Philipp Noah Hein #6356965
 */
public class LinguisticFeaturesAggregate_Impl implements ILinguisticFeaturesAggregate {

    private String redeId;
    private MetadataFeature_Impl metadata;
    private List<SentenceFeature_Impl> sentences = new ArrayList<>();
    private List<TokenFeature_Impl> tokens = new ArrayList<>();
    private List<SentimentFeature_Impl> sentiments = new ArrayList<>();
    private List<NamedEntityFeature_Impl> namedEntities = new ArrayList<>();
    private List<DependencyFeature_Impl> dependencies = new ArrayList<>();
    private List<TopicFeature_Impl> topics = new ArrayList<>();
    private List<AudioTokenFeature_Impl> audioTokens = new ArrayList<>();
    private List<PosFeature_Impl> posFeatures = new ArrayList<>();

    private Map<String, Integer> posCounts = new HashMap<>();
    private Map<String, Integer> namedEntityCounts = new HashMap<>();
    private Map<String, Integer> dependencyCounts = new HashMap<>();
    private Map<String, Integer> lemmaFrequency = new HashMap<>();
    private double overallSentiment;
    private Map<String, Double> sentimentDistribution = new HashMap<>();
    private Map<String, Integer> topicCounts = new HashMap<>();
    private Map<String, Integer> posCountsCoarse = new HashMap<>();

    private String topicsSearchField = "";

    // redeId getter und setter
    @Override
    public String getRedeId() {
        return redeId;
    }

    @Override
    public void setRedeId(String redeId) {
        this.redeId = redeId;
    }

    @Override
    public MetadataFeature_Impl getMetadata() {
        return metadata;
    }

    @Override
    public void setMetadata(MetadataFeature_Impl metadata) {
        this.metadata = metadata;
    }

    @Override
    public void setMetadata(IMetadataFeature metadata) {
        this.metadata = (MetadataFeature_Impl) metadata;
    }

    @Override
    public List<SentenceFeature_Impl> getSentences() {
        return sentences;
    }

    @Override
    public void setSentences(List<ISentenceFeature> sentences) {
        this.sentences = sentences.stream()
                .map(sentence -> (SentenceFeature_Impl) sentence) // Casting in die Implementierungsklasse
                .collect(Collectors.toList());
    }

    @Override
    public List<TokenFeature_Impl> getTokens() {
        return tokens;
    }

    @Override
    public void setTokens(List<ITokenFeature> tokens) {
        this.tokens = tokens.stream()
                .map(token -> (TokenFeature_Impl) token) // Explizites Casting
                .collect(Collectors.toList());
    }


    @Override
    public List<SentimentFeature_Impl> getSentiments() {
        return sentiments;
    }

    @Override
    public void setSentiments(List<ISentimentFeature> sentiments) {
        this.sentiments = sentiments.stream()
                .map(sentiment -> (SentimentFeature_Impl) sentiment) // Casting von Interface zu Implementierung
                .collect(Collectors.toList());
    }


    @Override
    public List<NamedEntityFeature_Impl> getNamedEntities() {
        return namedEntities;
    }

    @Override
    public void setNamedEntities(List<INamedEntityFeature> namedEntities) {
        this.namedEntities = namedEntities.stream()
                .map(entity -> (NamedEntityFeature_Impl) entity) // Casting von Interface zu Implementierung
                .collect(Collectors.toList());
    }


    @Override
    public List<DependencyFeature_Impl> getDependencies() {
        return dependencies;
    }

    @Override
    public void setDependencies(List<IDependencyFeature> dependencies) {
        this.dependencies = dependencies.stream()
                .map(dependency -> (DependencyFeature_Impl) dependency) // Casting von Interface zu Implementierung
                .collect(Collectors.toList());
    }


    @Override
    public List<TopicFeature_Impl> getTopics() {
        return topics;
    }

    @Override
    public void setTopics(List<ITopicFeature> topics) {
        this.topics = topics.stream()
                .map(topic -> (TopicFeature_Impl) topic) // Typumwandlung
                .collect(Collectors.toList());
    }


    @Override
    public Map<String, Integer> getPosCounts() {
        return posCounts;
    }

    @Override
    public void setPosCounts(Map<String, Integer> posCounts) {
        this.posCounts = posCounts;
    }

    @Override
    public Map<String, Integer> getNamedEntityCounts() {
        return namedEntityCounts;
    }

    @Override
    public void setNamedEntityCounts(Map<String, Integer> namedEntityCounts) {
        this.namedEntityCounts = namedEntityCounts;
    }

    @Override
    public Map<String, Integer> getDependencyCounts() {
        return dependencyCounts;
    }

    @Override
    public void setDependencyCounts(Map<String, Integer> dependencyCounts) {
        this.dependencyCounts = dependencyCounts;
    }

    @Override
    public Map<String, Integer> getLemmaFrequency() {
        return lemmaFrequency;
    }

    @Override
    public void setLemmaFrequency(Map<String, Integer> lemmaFrequency) {
        this.lemmaFrequency = lemmaFrequency;
    }

    @Override
    public double getOverallSentiment() {
        return overallSentiment;
    }

    @Override
    public void setOverallSentiment(double overallSentiment) {
        this.overallSentiment = overallSentiment;
    }

    @Override
    public Map<String, Double> getSentimentDistribution() {
        return sentimentDistribution;
    }

    @Override
    public void setSentimentDistribution(Map<String, Double> sentimentDistribution) {
        this.sentimentDistribution = sentimentDistribution;
    }

    @Override
    public Map<String, Integer> getTopicCounts() {
        return topicCounts;
    }

    @Override
    public void setTopicCounts(Map<String, Integer> topicCounts) {
        this.topicCounts = topicCounts;
    }

    @Override
    public List<AudioTokenFeature_Impl> getAudioTokens() {
        return audioTokens;
    }

    @Override
    public void setAudioTokens(List<IAudioTokenFeature> audioTokens) {
        this.audioTokens = audioTokens.stream()
                .map(audioToken -> (AudioTokenFeature_Impl) audioToken) // Casting von Interface zu Implementierung
                .collect(Collectors.toList());
    }

    @Override
    public List<PosFeature_Impl> getPosFeatures() {
        return posFeatures;
    }

    @Override
    public void setPosFeatures(List<IPosFeature> posFeatures) {
        this.posFeatures = posFeatures.stream()
                .map(posFeature -> (PosFeature_Impl) posFeature) // Casting von Interface zu Implementierung
                .collect(Collectors.toList());
    }

    /**
     * @author Kai
     */
    public String getTopicsSearchField() {
        return topicsSearchField;
    }

    /**
     * @author Kai
     */
    public void setTopicsSearchField(String topicsSearchField) {
        this.topicsSearchField = topicsSearchField;
    }

    /**
     * @author Kai
     * As we do not get the overall text topics when using the DUUI component ourselves, we have to remove them.
     * They are only present in the XMI file.
     */
    public void removeOverallTextTopics() {
        // Find the maximum 'end' value in the topics
        int maxEnd = topics.stream()
                .mapToInt(TopicFeature_Impl::getEnd)
                .max()
                .orElse(0);

        // Create a filtered list of topics that would remain after removal
        List<TopicFeature_Impl> remainingTopics = topics.stream()
                .filter(topic -> !(topic.getBegin() == 0 && topic.getEnd() == maxEnd))
                .collect(Collectors.toList());

        // Only update the topics list if it would not become empty
        if (!remainingTopics.isEmpty()) {
            topics.clear();
            topics.addAll(remainingTopics);
        }
    }

    /**
     * @author Kai
     */
    public void calculateTopicsSearchField() {
        // Find the topic with the maximum count
        Optional<Map.Entry<String, Integer>> maxEntry = topicCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue());


        // Set topicsSearchField to the topic with the highest count, or empty string if no topics
        topicsSearchField = maxEntry.map(Map.Entry::getKey).orElse("");
    }

    /**
     * @author Kai
     */
    public void calculateStatistics() {
        removeOverallTextTopics();
        calculatePOSCounts();
        calculateNamedEntityCounts();
        calculateOverallSentiment();
        calculateDependencyCounts();
        calculateLemmaFrequency();
        calculateSentimentDistribution();
        calculateTopicCounts();
        calculateTopicsSearchField();
    }

    /**
     * @author Kai
     */
    private void calculatePOSCounts() {
        posCounts = posFeatures.stream()
                .collect(Collectors.groupingBy(
                        PosFeature_Impl::getPos,
                        Collectors.summingInt(f -> 1)
                ));
        posCountsCoarse = posFeatures.stream()
                .collect(Collectors.groupingBy(
                        PosFeature_Impl::getCoarsePos,
                        Collectors.summingInt(f -> 1)
                ));
    }

    /**
     * @author Kai
     */
    private void calculateNamedEntityCounts() {
        namedEntityCounts = namedEntities.stream()
                .collect(Collectors.groupingBy(NamedEntityFeature_Impl::getCoveredText, Collectors.summingInt(f -> 1)));
    }

    /**
     * @author Kai
     */
    private void calculateOverallSentiment() {
        if (sentiments == null || sentiments.isEmpty()) {
            return;
        }
        // Gesamt-Sentiment ist das erste in der Liste
        overallSentiment = sentiments.get(0).getSentiment();
    }

    /**
     * @author Kai
     */
    private void calculateLemmaFrequency() {
        lemmaFrequency = tokens.stream()
                .collect(Collectors.groupingBy(TokenFeature_Impl::getLemma, Collectors.summingInt(t -> 1)));
    }

    /**
     * @author Kai
     */
    private void calculateDependencyCounts() {
        dependencyCounts = dependencies.stream()
                .collect(Collectors.groupingBy(DependencyFeature_Impl::getDependencyType, Collectors.summingInt(f -> 1)));
    }

    /**
     * @author Kai
     */
    public void calculateSentimentDistribution() {
        double pos = 0.0;
        double neg = 0.0;
        double neutral = 0.0;

        // Find highest end value among all sentiments
        int maxEnd = 0;
        for (ISentimentFeature sentiment : sentiments) {
            if (sentiment.getEnd() > maxEnd) {
                maxEnd = sentiment.getEnd();
            }
        }

        // Process all sentiments except those with highest end AND begin of zero
        for (ISentimentFeature sentiment : sentiments) {
            // Skip sentiments with both the highest end value AND begin of zero
            if (sentiment.getEnd() == maxEnd && sentiment.getBegin() == 0) {
                continue;
            }

            double sentimentScore = sentiment.getSentiment();
            int textLength = sentiment.getEnd() - sentiment.getBegin();

            // Categorize based on which third of [-1,1] the sentiment score falls into
            if (sentimentScore < -0.3333) {
                neg += textLength;
            } else if (sentimentScore > 0.3333) {
                pos += textLength;
            } else {
                neutral += textLength;
            }
        }

        // Save the distribution in the HashMap
        sentimentDistribution.put("positive", pos);
        sentimentDistribution.put("negative", neg);
        sentimentDistribution.put("neutral", neutral);
    }

    /**
     * @author Kai
     */
    public void calculateTopicCounts() {
        // group topics by their begin/end positions
        Map<String, TopicFeature_Impl> topicsByPosition = topics.stream()
                .collect(Collectors.toMap(
                        topic -> topic.getBegin() + ":" + topic.getEnd(),  // Create a key using begin:end format
                        topic -> topic,
                        (existing, replacement) -> existing.getScore() > replacement.getScore() ? existing : replacement
                ));

        // Count occurrences of each topic value from the filtered set
        topicCounts = topicsByPosition.values().stream()
                .collect(Collectors.groupingBy(TopicFeature_Impl::getValue, Collectors.summingInt(f -> 1)));
    }
    /**
     * @author Philipp Schneider
     */
    public Map<String, Integer> getPosCountsCoarse() {
        return posCountsCoarse;
    }
    /**
     * @author Philipp Schneider
     */
    public void setPosCountsCoarse(Map<String, Integer> posCountsCoarse) {
        this.posCountsCoarse = posCountsCoarse;
    }
}
