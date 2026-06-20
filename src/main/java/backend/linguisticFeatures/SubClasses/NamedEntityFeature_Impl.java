package backend.linguisticFeatures.SubClasses;

import backend.linguisticFeatures.Interface.INamedEntityFeature;

/**
 * @author Philipp Noah Hein #6356965
 */
public class NamedEntityFeature_Impl implements INamedEntityFeature {

    private int begin;
    private int end;
    private String coveredText;
    private String type;

    // Konstruktor
    public NamedEntityFeature_Impl() {
    }

    public NamedEntityFeature_Impl(int begin, int end, String coveredText, String type) {
        this.begin = begin;
        this.end = end;
        this.coveredText = coveredText;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
