package backend.linguisticFeatures.Interface;

/**
 * @author Philipp Noah Hein #6356965
 */
public interface ISentenceFeature {

    int getBegin();

    void setBegin(int begin);

    int getEnd();

    void setEnd(int end);

    String getCoveredText();

    void setCoveredText(String coveredText);
}
