package backend.linguisticFeatures.Interface;

/**
 * @author Kai
 * @date 05/03/2025
 */
public interface IAudioTokenFeature {

    int getBegin();

    void setBegin(int begin);

    int getEnd();

    void setEnd(int end);

    String getCoveredText();

    void setCoveredText(String coveredText);

    double getTimeStart();

    void setLemma(double start);

    double getTimeEnd();

    void setPos(double end);
}
