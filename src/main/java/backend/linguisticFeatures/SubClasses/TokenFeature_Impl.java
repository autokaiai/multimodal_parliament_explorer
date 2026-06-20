package backend.linguisticFeatures.SubClasses;

import backend.linguisticFeatures.Interface.ITokenFeature;

/**
 * @author Philipp Noah Hein #6356965
 */
public class TokenFeature_Impl implements ITokenFeature {

    private int begin;
    private int end;
    private String coveredText;
    private String lemma;
    private String pos;

    // Konstruktor
    public TokenFeature_Impl() {
    }

    public TokenFeature_Impl(int begin, int end, String coveredText, String lemma, String pos) {
        this.begin = begin;
        this.end = end;
        this.coveredText = coveredText;
        this.lemma = lemma;
        this.pos = pos;
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

    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }
}
