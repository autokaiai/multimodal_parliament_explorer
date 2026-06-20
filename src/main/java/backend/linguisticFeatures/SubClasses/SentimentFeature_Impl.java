package backend.linguisticFeatures.SubClasses;

import backend.linguisticFeatures.Interface.ISentimentFeature;

/**
 * @author Philipp Noah Hein #6356965
 */
public class SentimentFeature_Impl implements ISentimentFeature {

    private int begin;
    private int end;
    private String coveredText;
    private double sentiment;
    private double posScore;
    private double neuScore;
    private double negScore;
    private double subjectivity;

    // Konstruktor
    public SentimentFeature_Impl() {
    }

    public SentimentFeature_Impl(int begin, int end, String coveredText, double sentiment, double posScore, double neuScore, double negScore, double subjectivity) {
        this.begin = begin;
        this.end = end;
        this.coveredText = coveredText;
        this.sentiment = sentiment;
        this.posScore = posScore;
        this.neuScore = neuScore;
        this.negScore = negScore;
        this.subjectivity = subjectivity;
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

    public double getSentiment() {
        return sentiment;
    }

    public void setSentiment(double sentiment) {
        this.sentiment = sentiment;
    }

    public double getPosScore() {
        return posScore;
    }

    public void setPosScore(double posScore) {
        this.posScore = posScore;
    }

    public double getNeuScore() {
        return neuScore;
    }

    public void setNeuScore(double neuScore) {
        this.neuScore = neuScore;
    }

    public double getNegScore() {
        return negScore;
    }

    public void setNegScore(double negScore) {
        this.negScore = negScore;
    }

    public double getSubjectivity() {
        return subjectivity;
    }

    public void setSubjectivity(double subjectivity) {
        this.subjectivity = subjectivity;
    }
}
