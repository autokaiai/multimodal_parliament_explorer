package backend.linguisticFeatures.Interface;

/**
 * @author Kai
 * @date 05/03/2025
 */
public interface IPosFeature {

    int getBegin();

    void setBegin(int begin);

    int getEnd();

    void setEnd(int end);

    String getCoveredText();

    void setCoveredText(String coveredText);

    String getPos();

    void setPos(String pos);

    String getCoarsePos();

    void setCoarsePos(String coarsePos);
}
