package backend.linguisticFeatures.SubClasses;

import backend.linguisticFeatures.Interface.ITopicFeature;

/**
 * @author Philipp Noah Hein #6356965
 */
public class TopicFeature_Impl implements ITopicFeature {

    private int begin;
    private int end;
    private String coveredText;
    private String value;
    private double score;

    // Konstruktor
    public TopicFeature_Impl() {
    }

    public TopicFeature_Impl(int begin, int end, String coveredText, String value, double score) {
        this.begin = begin;
        this.end = end;
        this.coveredText = coveredText;
        this.value = value;
        this.score = score;
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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
