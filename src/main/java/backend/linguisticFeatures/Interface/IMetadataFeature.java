package backend.linguisticFeatures.Interface;

/**
 * @author Philipp Noah Hein #6356965
 */
public interface IMetadataFeature {

    String getDocumentTitle();

    void setDocumentTitle(String documentTitle);

    String getLanguage();

    void setLanguage(String language);

    String getDocumentId();

    void setDocumentId(String rednerId);
}
