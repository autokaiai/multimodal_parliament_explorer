package backend.linguisticFeatures.SubClasses;

import backend.linguisticFeatures.Interface.IPosFeature;

/**
 * @author Kai
 * @date 05/03/2025
 */
public class PosFeature_Impl implements IPosFeature {
    private int begin;
    private int end;
    private String coveredText;
    private String pos;
    private String coarsePos;

    // Konstruktor
    public PosFeature_Impl() {
    }

    public PosFeature_Impl(int begin, int end, String coveredText, String pos, String coarsePos) {
        this.begin = begin;
        this.end = end;
        this.coveredText = coveredText;
        this.pos = pos;
        this.coarsePos = coarsePos;
    }

    // Getter und Setter
    public int getBegin() {
        return begin;
    }

    public void setBegin(int begin) {
        this.begin = begin;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getCoveredText() {
        return coveredText;
    }

    public void setCoveredText(String coveredText) {
        this.coveredText = coveredText;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getCoarsePos() {
        return coarsePos;
    }

    public void setCoarsePos(String coarsePos) {
        this.coarsePos = coarsePos;
    }
}
