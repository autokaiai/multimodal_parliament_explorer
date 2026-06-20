package backend.linguisticFeatures.SubClasses;

import backend.linguisticFeatures.Interface.IMetadataFeature;

/**
 * @author Philipp Noah Hein #6356965
 */
public class MetadataFeature_Impl implements IMetadataFeature {

    private String documentTitle;
    private String language;
    private String documentId;

    // Konstruktor
    public MetadataFeature_Impl() {
    }

    public MetadataFeature_Impl(String documentTitle, String language, String documentId) {
        this.documentTitle = documentTitle;
        this.language = language;
        this.documentId = documentId;
    }

    // Getter und Setter
    @Override
    public String getDocumentTitle() {
        return documentTitle;
    }

    @Override
    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    @Override
    public String getLanguage() {
        return language;
    }

    @Override
    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String getDocumentId() {
        return documentId;
    }

    @Override
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
