package backend.linguisticFeatures.Interface;

/**
 * @author Philipp Noah Hein #6356965
 */
public interface ITokenFeature {

    int getBegin();

    void setBegin(int begin);

    int getEnd();

    void setEnd(int end);

    String getCoveredText();

    void setCoveredText(String coveredText);

    String getLemma();

    void setLemma(String lemma);

    String getPos();

    void setPos(String pos);
}
