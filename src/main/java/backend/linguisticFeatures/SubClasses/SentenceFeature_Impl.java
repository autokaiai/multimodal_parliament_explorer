package backend.linguisticFeatures.SubClasses;

import backend.linguisticFeatures.Interface.ISentenceFeature;

/**
 * @author Philipp Noah Hein #6356965
 */
public class SentenceFeature_Impl implements ISentenceFeature {

    private int begin;
    private int end;
    private String coveredText;

    // Konstruktor
    public SentenceFeature_Impl() {
    }

    public SentenceFeature_Impl(int begin, int end, String coveredText) {
        this.begin = begin;
        this.end = end;
        this.coveredText = coveredText;
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
}
