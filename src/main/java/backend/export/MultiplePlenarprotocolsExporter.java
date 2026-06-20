package backend.export;

import backend.database.MongoDBHandler;
import backend.linguisticFeatures.LinguisticService;
import backend.plenarprotocol.PlenarprotocolDAO;
import backend.plenarprotocol.Plenarprotocol_impl;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

public class MultiplePlenarprotocolsExporter {

    /**
     * @param plenarprotokollIds Liste der Plenarprotokoll IDs
     * @param disableTikz        boolean, ob Tikz deaktiviert werden soll
     * @return die Protokolle als Tex-Code
     * @author Philipp Landmann
     */
    public static String toTex(List<String> plenarprotokollIds, boolean disableTikz) throws IOException, TemplateException {
        PlenarprotocolDAO plenarprotocolDAO = new PlenarprotocolDAO(new MongoDBHandler());

        // schauen ob wir alle exportieren
        boolean isAll = false;
        if (plenarprotokollIds.size() >= plenarprotocolDAO.getProtocolsAmount()) {
            isAll = true;
        }

        // config
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "templates/latex/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.GERMANY);

        // get protocols
        List<Plenarprotocol_impl> protocols = new ArrayList<>();
        for (String id : plenarprotokollIds) {
            Plenarprotocol_impl plenarprotocolImpl = plenarprotocolDAO.getProtocolById(id);
            if (plenarprotocolImpl != null) {
                protocols.add(plenarprotocolImpl);
            }
        }

        // sort by index
        protocols.sort(Comparator.comparingInt(p -> {
            if (p == null || p.getProtocol() == null || p.getProtocol().getIndex() == null) {
                return 0;
            }
            return p.getProtocol().getIndex();
        }));

        // map
        Map<String, Object> data = new HashMap<>();
        data.put("protocols", protocols);
        data.put("isAll", isAll);
        data.put("disableTikz", disableTikz);
        data.put("linguisticService", new LinguisticService());


        // apply
        Template template = cfg.getTemplate("multipleProtocols/multipleProtocols.ftl");
        StringWriter stringWriter = new StringWriter();
        template.process(data, stringWriter);


        return stringWriter.toString();
    }

    public static String toXML(List<String> plenarprotokollIds) throws IOException, TemplateException, IllegalArgumentException {
        PlenarprotocolDAO plenarprotocolDAO = new PlenarprotocolDAO(new MongoDBHandler());

        // config
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "templates/xml/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.GERMANY);

        // get protocols
        List<Plenarprotocol_impl> protocols = new ArrayList<>();
        for (String id : plenarprotokollIds) {
            Plenarprotocol_impl plenarprotocolImpl = plenarprotocolDAO.getProtocolById(id);
            if (plenarprotocolImpl == null) {
                throw new IllegalArgumentException("Plenarprotokoll mit ID " + id + " nicht gefunden.");
            }
            protocols.add(plenarprotocolDAO.getProtocolById(id));
        }

        // map
        Map<String, Object> data = new HashMap<>();
        data.put("protocols", protocols);
        data.put("linguisticService", new LinguisticService());

        // apply
        Template template = cfg.getTemplate("multipleProtocols/multipleProtocols.ftl");
        StringWriter stringWriter = new StringWriter();
        template.process(data, stringWriter);

        return stringWriter.toString();
    }

    public static byte[] toPDF(List<String> plenarprotokollIds, boolean disableTikz) throws IOException, TemplateException {
        return LatexCompiler.compileToByteArray(toTex(plenarprotokollIds, disableTikz));
    }

}
