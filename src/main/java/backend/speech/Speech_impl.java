package backend.speech;

import backend.export.LatexCompiler;
import backend.linguisticFeatures.LinguisticService;
import backend.speaker.interfaces.Speaker;
import backend.speech.interfaces.IAgenda;
import backend.speech.interfaces.IProtocol;
import backend.speech.interfaces.ISpeech;
import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.apache.uima.UIMAException;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Implementierung des {@link ISpeech}-Interfaces.
 *
 * @author Philipp Hein
 * @date 14.03.2025
 */
public class Speech_impl implements ISpeech {

    private String _id;
    private String text;
    private String speaker;
    private IProtocol protocol;
    private ArrayList<Object> textContent;
    private IAgenda agenda;
    private Speaker speakerObject;
    private String videoUrl;
    private String videoId;
    private ObjectId videoFileId;

    /**
     * @author: Philipp Hein #6356965
     * Standard-Konstruktor.
     *
     * @param _id           ID Speech
     * @param text          Text Speech
     * @param speaker       Speaker ID
     * @param protocol      Protocol Speech
     * @param textContent   Textinhalt Speech
     * @param agenda        Agenda Speech
     * @param speakerObject Speaker Objekt
     */
    public Speech_impl(String _id, String text, String speaker, IProtocol protocol,
                       ArrayList<Object> textContent, IAgenda agenda, Speaker speakerObject,
                       String videoUrl) {
        this._id = _id;
        this.text = text;
        this.speaker = speaker;
        this.protocol = protocol;
        this.textContent = textContent;
        this.agenda = agenda;
        this.speakerObject = speakerObject;
        this.videoUrl = videoUrl;
    }

    @Override
    public Speaker getSpeakerObject() {
        return this.speakerObject;
    }

    @Override
    public void setSpeakerObject(Speaker speakerObject) {
        this.speakerObject = speakerObject;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return ID Speech
     */
    @Override
    public String get_id() {
        return this._id;
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt ID Speech
     *
     * @param _id neu
     */
    @Override
    public void set_id(String _id) {
        this._id = _id;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return Text Speech
     */
    @Override
    public String getText() {
        return this.text;
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt Text Speech
     *
     * @param text neu
     */
    @Override
    public void setText(String text) {
        this.text = text;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return Speaker ID
     */
    @Override
    public String getSpeaker() {
        return this.speaker;
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt Speaker ID
     *
     * @param speaker neu
     */
    @Override
    public void setSpeaker(String speaker) {
        this.speaker = speaker;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return Protocol Speech
     */
    @Override
    public IProtocol getProtocol() {
        return this.protocol;
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt Protocol Speech
     *
     * @param protocol neu
     */
    @Override
    public void setProtocol(IProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return Textinhalt Speech
     */
    @Override
    public ArrayList<Object> getTextContent() {
        return this.textContent;
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt Textinhalt Speech
     *
     * @param textContent neu
     */
    @Override
    public void setTextContent(ArrayList<Object> textContent) {
        this.textContent = textContent;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return Agenda Speech
     */
    @Override
    public IAgenda getAgenda() {
        return this.agenda;
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt Agenda Speech
     *
     * @param agenda neu
     */
    @Override
    public void setAgenda(IAgenda agenda) {
        this.agenda = agenda;
    }

    /**
     * Gibt die Video-ID zurück.
     *
     * @return Video-ID
     * @author Philipp Hein
     * @date 14.03.2025
     */
    public String getVideoId() {
        return videoId;
    }

    /**
     * Setzt die Video-ID.
     *
     * @param videoId neue Video-ID
     * @author Philipp Hein
     * @date 14.03.2025
     */
    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    /**
     * Gibt die Video-URL zurück.
     *
     * @return Video URL als String
     * @author Philipp Hein
     * @date 14.03.2025
     */
    public String getVideoUrl() {
        return videoUrl;
    }

    /**
     * Setzt die Video URL.
     *
     * @param videoUrl neue Video URL
     * @author Philipp Hein
     * @date 14.03.2025
     */
    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    /**
     * Gibt die Video ID zurück.
     *
     * @return Video ID als String
     * @author Philipp Hein
     * @date 14.03.2025
     */
    public ObjectId getVideoFileId() {
        return videoFileId;
    }


    public void setVideoFileId(ObjectId videoFileId) {
        this.videoFileId = videoFileId;
    }

    /**
     * @return Ein fertig erstelltes JCas-Dokument, die in DUUI oder andere Pipelines verwendet werden können.
     * @autor Kai
     */
    public JCas toCAS(LoadVideoFromGridFS loadVideoFromGridFS, boolean video) throws UIMAException, IOException {
        // Create a fresh JCas object to avoid Sofa issues
        JCas jcas = JCasFactory.createJCas();

        // Set the text and language for the main/default view
        jcas.setDocumentText(this.getText());
        jcas.setDocumentLanguage("de");

        // Define metadata for the default view
        DocumentMetaData metaDefault = DocumentMetaData.create(jcas);
        String redeId = this.get_id();
        metaDefault.setDocumentId(redeId);
        metaDefault.setDocumentTitle("Analyse für Rede: " + redeId);
        metaDefault.setLanguage("de");
        metaDefault.addToIndexes();

        // Create and configure the "video" view
        JCas vcas = jcas.createView("video");
        vcas.setDocumentLanguage("de");
        DocumentMetaData metaVideo = DocumentMetaData.create(vcas);
        metaVideo.setDocumentId(redeId);
        metaVideo.setDocumentTitle("Video für Rede: " + redeId);
        metaVideo.setLanguage("de");
        metaVideo.addToIndexes();
        HashMap<String, String> videoMap = loadVideoFromGridFS.getVideoBase64(this);
        if (videoMap != null && video) {
            vcas.setSofaDataString(videoMap.get("videoBase64"), videoMap.get("pMimeType"));
        }

        // Create and configure the "transcript" view
        JCas tcas = jcas.createView("transcript");
        tcas.setDocumentLanguage("de");
        DocumentMetaData metaTranscript = DocumentMetaData.create(tcas);
        metaTranscript.setDocumentId(redeId);
        metaTranscript.setDocumentTitle("Transkript (Analyse) für Rede: " + redeId);
        metaTranscript.setLanguage("de");
        metaTranscript.addToIndexes();

        return jcas;
    }

    @Override
    public String toTex(boolean disableTikz) throws IOException, TemplateException {
        // config
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "templates/latex/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.GERMANY);


        // compile data for template
        Map<String, Object> data = new HashMap<>();
        data.put("speech", this);
        data.put("disableTikz", disableTikz);
        data.put("linguisticService", new LinguisticService());

        // apply
        Template template = cfg.getTemplate("speech/speech.ftl");
        StringWriter stringWriter = new StringWriter();
        template.process(data, stringWriter);

        return stringWriter.toString();
    }

    public String toXML() throws IOException, TemplateException {
        // config
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "templates/xml/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.GERMANY);


        // compile data for template
        Map<String, Object> data = new HashMap<>();
        data.put("speech", this);
        data.put("linguisticService", new LinguisticService());

        // apply
        Template template = cfg.getTemplate("speech/speech.ftl");
        StringWriter stringWriter = new StringWriter();
        template.process(data, stringWriter);

        return stringWriter.toString();
    }

    public byte[] toPDF(boolean disableTikz) throws IOException, TemplateException {
        String texCode = this.toTex(disableTikz);
        return LatexCompiler.compileToByteArray(texCode);
    }

    public Document toDocument() {
        Document doc = new Document();
        // Füge einfache Felder hinzu
        doc.append("text", this.text);
        doc.append("speaker", this.speaker);
        doc.append("videoUrl", this.videoUrl);


        if (this.speakerObject != null) {

            Document speakerDoc = new Document();
            speakerDoc.append("name", this.speakerObject.getName());
            speakerDoc.append("firstName", this.speakerObject.getFirstName());
            speakerDoc.append("party", this.speakerObject.getParty());
            doc.append("speakerObject", speakerDoc);
        }
        if (this.textContent != null) {
            doc.append("textContent", this.textContent);
        }

        return doc;
    }


}
