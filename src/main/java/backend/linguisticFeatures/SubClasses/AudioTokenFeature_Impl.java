package backend.linguisticFeatures.SubClasses;

import backend.linguisticFeatures.Interface.IAudioTokenFeature;

/**
 * @author Kai
 * @date 05/03/2025
 */
public class AudioTokenFeature_Impl implements IAudioTokenFeature {

    private int begin;
    private int end;
    private String coveredText;
    private double timeStart;
    private double timeEnd;

    // Konstruktor
    public AudioTokenFeature_Impl() {
    }

    public AudioTokenFeature_Impl(int begin, int end, String coveredText, double timeStart, double timeEnd) {
        this.begin = begin;
        this.end = end;
        this.coveredText = coveredText;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
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

    public double getTimeStart() {
        return timeStart;
    }

    public void setLemma(double start) {
        this.timeStart = timeStart;
    }

    public double getTimeEnd() {
        return timeEnd;
    }

    public void setPos(double end) {
        this.timeEnd = timeEnd;
    }
}
