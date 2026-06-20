package backend.plenarprotocol;

import backend.export.LatexCompiler;
import backend.linguisticFeatures.LinguisticService;
import backend.speech.interfaces.IAgenda;
import backend.speech.interfaces.IProtocol;
import backend.speech.interfaces.ISpeech;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

/**
 * @author Philipp Hein
 * @date 12.03.2025
 */
public class Plenarprotocol_impl implements IPlenarprotocol {
    private String _id;
    private IProtocol protocol;    // date, starttime, endtime, index, title, place, wp
    private List<ISpeech> speeches;
    private int speechCount;// Liste aller Reden

    public Plenarprotocol_impl(String _id, IProtocol protocol, List<ISpeech> speeches) {
        this._id = _id;
        this.protocol = protocol;
        this.speeches = speeches;
    }


    /**
     * Gibt die Protokoll ID zurück.
     *
     * @return Protokoll ID
     * @author Philipp Hein
     * @date 12.03.2025
     */
    public String getId() {
        return _id;
    }

    /**
     * Setzt die Protokoll ID.
     *
     * @param _id Protokoll ID
     * @author Philipp Hein
     * @date 12.03.2025
     */
    public void setId(String _id) {
        this._id = _id;
    }

    /**
     * Gibt die Protokolldaten zurück.
     *
     * @return Protokolldaten
     * @author Philipp Hein
     * @date 12.03.2025
     */
    public IProtocol getProtocol() {
        return protocol;
    }

    /**
     * Setzt die Protokolldaten.
     *
     * @param protocol Protokolldaten
     * @author Philipp Hein
     * @date 12.03.2025
     */
    public void setProtocol(IProtocol protocol) {
        this.protocol = protocol;
    }

    /**
     * Gibt die Liste der Reden zurück
     *
     * @return Liste der Reden
     * @author Philipp Hein
     * @date 12.03.2025
     */
    public List<ISpeech> getSpeeches() {
        return speeches;
    }

    /**
     * Setzt die Liste der Reden
     *
     * @param speeches Liste der Reden
     * @author Philipp Hein
     * @date 12.03.2025
     */
    public void setSpeeches(List<ISpeech> speeches) {
        this.speeches = speeches;
    }

    /**
     * @param disableTikz boolean, ob Tikz deaktiviert werden soll
     * @return das Protokoll als Tex-Code (mit allen Reden), aber mit wrapping
     * @author Philipp Landmann
     */
    @Override
    public String toTex(boolean disableTikz) throws TemplateException, IOException {
        // config
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "templates/latex/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.GERMANY);

        // map
        Map<String, Object> data = new HashMap<>();
        data.put("protocol", this);
        data.put("disableTikz", disableTikz);
        data.put("linguisticService", new LinguisticService());

        // apply
        Template template = cfg.getTemplate("singleProtocol/singleProtocol.ftl");
        StringWriter stringWriter = new StringWriter();
        template.process(data, stringWriter);

        return stringWriter.toString();
    }

    public byte[] toPDF(boolean disableTikz) throws IOException, TemplateException {
        return LatexCompiler.compileToByteArray(this.toTex(disableTikz));
    }

    public String toXML() throws IOException, TemplateException {
        // config
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "templates/xml/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.GERMANY);

        // map
        Map<String, Object> data = new HashMap<>();
        data.put("protocol", this);
        data.put("linguisticService", new LinguisticService());

        // apply
        Template template = cfg.getTemplate("singleProtocol/singleProtocol.ftl");
        StringWriter stringWriter = new StringWriter();
        template.process(data, stringWriter);

        return stringWriter.toString();
    }

    public Map<IAgenda, List<ISpeech>> getAgendaSpeechesMap() {
        List<ISpeech> speeches = this.getSpeeches();
        Map<IAgenda, List<ISpeech>> result = new LinkedHashMap<>();

        // wir gruppieren nach Agenda.id, denn die Objekte sind nicht die gleichen.
        Map<String, IAgenda> agendaIdMap = new HashMap<>();

        for (ISpeech speech : speeches) {
            String agendaId = speech.getAgenda().getId();
            if (!agendaIdMap.containsKey(agendaId)) {
                agendaIdMap.put(speech.getAgenda().getId(), speech.getAgenda());
            }

            if (!result.containsKey(agendaIdMap.get(agendaId))) {
                result.put(speech.getAgenda(), new ArrayList<>());
            }
            result.get(agendaIdMap.get(agendaId)).add(speech);
        }
        return result;
    }
}
