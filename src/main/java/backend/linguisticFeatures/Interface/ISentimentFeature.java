package backend.linguisticFeatures.Interface;

/**
 * @author Philipp Noah Hein #6356965
 */
public interface ISentimentFeature {

    int getBegin();

    void setBegin(int begin);

    int getEnd();

    void setEnd(int end);

    String getCoveredText();

    void setCoveredText(String coveredText);

    double getSentiment();

    void setSentiment(double sentiment);

    double getPosScore();

    void setPosScore(double posScore);

    double getNeuScore();

    void setNeuScore(double neuScore);

    double getNegScore();

    void setNegScore(double negScore);

    double getSubjectivity();

    void setSubjectivity(double subjectivity);
}
