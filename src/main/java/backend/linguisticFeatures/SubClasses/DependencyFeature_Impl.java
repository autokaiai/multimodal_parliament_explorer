package backend.linguisticFeatures.SubClasses;

import backend.linguisticFeatures.Interface.IDependencyFeature;

/**
 * @author Philipp Noah Hein #6356965
 */
public class DependencyFeature_Impl implements IDependencyFeature {

    private int begin;
    private int end;
    private String coveredText;
    private String dependencyType;
    private String governor;
    private String dependent;

    // Konstruktor
    public DependencyFeature_Impl() {
    }

    public DependencyFeature_Impl(int begin, int end, String coveredText, String dependencyType, String governor, String dependent) {
        this.begin = begin;
        this.end = end;
        this.coveredText = coveredText;
        this.dependencyType = dependencyType;
        this.governor = governor;
        this.dependent = dependent;
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

    public String getDependencyType() {
        return dependencyType;
    }

    public void setDependencyType(String dependencyType) {
        this.dependencyType = dependencyType;
    }

    public String getGovernor() {
        return governor;
    }

    public void setGovernor(String governor) {
        this.governor = governor;
    }

    public String getDependent() {
        return dependent;
    }

    public void setDependent(String dependent) {
        this.dependent = dependent;
    }
}
