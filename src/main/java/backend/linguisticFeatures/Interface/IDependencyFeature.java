package backend.linguisticFeatures.Interface;

/**
 * @author Philipp Noah Hein #6356965
 */
public interface IDependencyFeature {

    int getBegin();

    void setBegin(int begin);

    int getEnd();

    void setEnd(int end);

    String getCoveredText();

    void setCoveredText(String coveredText);

    String getDependencyType();

    void setDependencyType(String dependencyType);

    String getGovernor();

    void setGovernor(String governor);

    String getDependent();

    void setDependent(String dependent);
}
