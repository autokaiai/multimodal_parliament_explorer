package backend.export;

import backend.linguisticFeatures.LinguisticService;
import backend.linguisticFeatures.TopicDAO;
import backend.speech.interfaces.ISpeech;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TopicExporter {

    /**
     * @param id          Topic ID
     * @param disableTikz boolean, ob Tikz deaktiviert werden soll
     * @return die Reden mit dem Topic als Tex-Code
     * @author Philipp Landmann
     */
    public static String toTex(String id, boolean disableTikz) throws IOException, TemplateException {
        TopicDAO TopicDAO = new TopicDAO();

        // config
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "templates/latex/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.GERMANY);

        // map
        Map<String, Object> data = new HashMap<>();
        data.put("speeches", TopicDAO.getSpeechesByTopic(id));
        data.put("topicId", id);
        data.put("disableTikz", disableTikz);
        data.put("linguisticService", new LinguisticService());

        for (ISpeech speech : (List<ISpeech>) data.get("speeches")) {
            if (speech.getSpeakerObject() == null) {
                System.out.println("Speaker is null in speech with id: " + speech.get_id());
            }
        }

        // apply
        Template template = cfg.getTemplate("topic/topic.ftl");
        StringWriter stringWriter = new StringWriter();
        template.process(data, stringWriter);

        return stringWriter.toString();
    }

    public static byte[] toPDF(String id, boolean disableTikz) throws IOException, TemplateException {
        return LatexCompiler.compileToByteArray(toTex(id, disableTikz));
    }

    public static String toXML(String id) throws IOException, TemplateException {
        TopicDAO TopicDAO = new TopicDAO();

        // config
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "templates/xml/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.GERMANY);

        // map
        Map<String, Object> data = new HashMap<>();
        data.put("speeches", TopicDAO.getSpeechesByTopic(id));
        data.put("topicId", id);
        data.put("linguisticService", new LinguisticService());

        // apply
        Template template = cfg.getTemplate("topic/topic.ftl");
        StringWriter stringWriter = new StringWriter();
        template.process(data, stringWriter);

        return stringWriter.toString();
    }

}
