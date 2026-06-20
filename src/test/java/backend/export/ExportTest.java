package backend.export;

import backend.database.MongoDBHandler;
import backend.plenarprotocol.PlenarprotocolDAO;
import backend.plenarprotocol.Plenarprotocol_impl;
import backend.speaker.SpeakerDAO;
import backend.speaker.Speaker_impl;
import freemarker.template.TemplateException;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class ExportTest {
    /**
     * Testet die Methode {@link Speaker_impl#toTex(boolean)}.
     *
     * @throws IOException
     * @throws TemplateException
     * @author Philipp Landmann
     */
    @Test
    void testSpeakerToPDF() throws IOException, TemplateException {
        // get a speaker object
        MongoDBHandler mongoHandler = new MongoDBHandler();
        mongoHandler.connect();

        SpeakerDAO speakerDAO = new SpeakerDAO(mongoHandler.getSpeakerCollection());
        byte[] pdfBytes = speakerDAO.findById("11004830").toPDF(false);

        if (Files.notExists(Paths.get("src/test/out"))) {
            Files.createDirectories(Paths.get("src/test/out"));
        }
        Files.write(Paths.get("src/test/out", "exportTestSpeaker.pdf"), pdfBytes);

        mongoHandler.close();
    }

    /**
     * Testet die Methode {@link backend.plenarprotocol.Plenarprotocol_impl#toTex(boolean)}.
     *
     * @throws IOException
     * @throws TemplateException
     * @author Philipp Landmann
     */
    @Test
    void testSinglePlenarprotocolToTex() throws IOException, TemplateException {
        // get a speaker object
        MongoDBHandler mongoHandler = new MongoDBHandler();
        mongoHandler.connect();

        PlenarprotocolDAO plenarprotocolDAO = new PlenarprotocolDAO(mongoHandler);
        Plenarprotocol_impl plenarprotocol = plenarprotocolDAO.getProtocolById("Plenarprotokoll 20/42");
        String texCode = plenarprotocol.toTex(false);
        System.out.println(texCode);

        mongoHandler.close();
    }

    /**
     * Testet die Methode {@link backend.plenarprotocol.Plenarprotocol_impl#toTex(boolean)}.
     *
     * @throws IOException
     * @throws TemplateException
     * @author Philipp Landmann
     */
    @Test
    void testSinglePlenarprotocolToPDF() throws IOException, TemplateException {
        // get a speaker object
        MongoDBHandler mongoHandler = new MongoDBHandler();
        mongoHandler.connect();

        PlenarprotocolDAO plenarprotocolDAO = new PlenarprotocolDAO(mongoHandler);
        Plenarprotocol_impl plenarprotocol = plenarprotocolDAO.getProtocolById("Plenarprotokoll 20/42");

        byte[] pdfBytes = plenarprotocol.toPDF(false);
        if (Files.notExists(Paths.get("src/test/out"))) {
            Files.createDirectories(Paths.get("src/test/out"));
        }
        Files.write(Paths.get("src/test/out", "exportTestSinglePlenarprotocol.pdf"), pdfBytes);

        mongoHandler.close();
    }

    /**
     * Testet die Methode {@link backend.export.MultiplePlenarprotocolsExporter#toPDF(List, boolean)}.
     *
     * @throws IOException
     * @throws TemplateException
     */
    @Test
    void testMultiplePlenarprotocolsToPDF() throws IOException, TemplateException {
        // get a speaker object
        MongoDBHandler mongoHandler = new MongoDBHandler();
        mongoHandler.connect();

        byte[] pdfBytes = MultiplePlenarprotocolsExporter.toPDF(List.of("0_0"
//                , "Plenarprotokoll 20/42", "Plenarprotokoll 20/43"
        ), false);
        if (Files.notExists(Paths.get("src/test/out"))) {
            Files.createDirectories(Paths.get("src/test/out"));
        }
        Files.write(Paths.get("src/test/out", "exportTestMultiplePlenarprotocols.pdf"), pdfBytes);

        mongoHandler.close();
    }

    /**
     * Testet die Methode {@link backend.export.MultiplePlenarprotocolsExporter#toPDF(List, boolean)}.
     *
     * @throws IOException
     * @throws TemplateException
     */
    @Test
    void testAllPlenarprotocolsToPDF() throws IOException, TemplateException {
        // get a speaker object
        MongoDBHandler mongoHandler = new MongoDBHandler();
        mongoHandler.connect();

        PlenarprotocolDAO plenarprotocolDAO = new PlenarprotocolDAO(mongoHandler);

        byte[] pdfBytes = MultiplePlenarprotocolsExporter.toPDF(plenarprotocolDAO.getAllProtocols().stream().map(Plenarprotocol_impl::getId).collect(Collectors.toList()), true);
        if (Files.notExists(Paths.get("src/test/out"))) {
            Files.createDirectories(Paths.get("src/test/out"));
        }
        Files.write(Paths.get("src/test/out", "exportTestAllPlenarprotocols.pdf"), pdfBytes);

        mongoHandler.close();
    }

    /**
     * Testet die Methode {@link TopicExporter#toPDF(String, boolean)}.
     *
     * @throws IOException
     * @throws TemplateException
     */
    @Test
    void testTopicToPDF() throws IOException, TemplateException {
        byte[] pdfBytes = TopicExporter.toPDF("Culture", false);
        if (Files.notExists(Paths.get("src/test/out"))) {
            Files.createDirectories(Paths.get("src/test/out"));
        }
        Files.write(Paths.get("src/test/out", "exportTestTopic.pdf"), pdfBytes);
    }

    @Test
    void testSpeakerToXML() throws IOException, TemplateException {
        // get a speaker object
        MongoDBHandler mongoHandler = new MongoDBHandler();
        mongoHandler.connect();

        SpeakerDAO speakerDAO = new SpeakerDAO(mongoHandler.getSpeakerCollection());
        String xmlString = speakerDAO.findById("11004830").toXML();

        if (Files.notExists(Paths.get("src/test/out"))) {
            Files.createDirectories(Paths.get("src/test/out"));
        }
        Files.writeString(Paths.get("src/test/out", "exportTestSpeaker.xml"), xmlString);

        mongoHandler.close();
    }

    /**
     * Testet die Methode {@link backend.plenarprotocol.Plenarprotocol_impl#toTex(boolean)}.
     *
     * @throws IOException
     * @throws TemplateException
     * @author Philipp Landmann
     */
    @Test
    void testSinglePlenarprotocolToXML() throws IOException, TemplateException {
        // get a speaker object
        MongoDBHandler mongoHandler = new MongoDBHandler();
        mongoHandler.connect();

        PlenarprotocolDAO plenarprotocolDAO = new PlenarprotocolDAO(mongoHandler);
        Plenarprotocol_impl plenarprotocol = plenarprotocolDAO.getProtocolById("Plenarprotokoll 20/42");

        String xmlString = plenarprotocol.toXML();

        if (Files.notExists(Paths.get("src/test/out"))) {
            Files.createDirectories(Paths.get("src/test/out"));
        }
        Files.writeString(Paths.get("src/test/out", "exportTestSinglePlenarprotocol.xml"), xmlString);

        mongoHandler.close();
    }

    /**
     * Testet die Methode {@link backend.export.MultiplePlenarprotocolsExporter#toXML(List)}.
     *
     * @throws IOException
     * @throws TemplateException
     */
    @Test
    void testMultiplePlenarprotocolsToXML() throws IOException, TemplateException {
        // get a speaker object
        MongoDBHandler mongoHandler = new MongoDBHandler();
        mongoHandler.connect();

        String xmlString = MultiplePlenarprotocolsExporter.toXML(List.of("0_0", "Plenarprotokoll 20/42", "Plenarprotokoll 20/43"));
        if (Files.notExists(Paths.get("src/test/out"))) {
            Files.createDirectories(Paths.get("src/test/out"));
        }
        Files.writeString(Paths.get("src/test/out", "exportTestMultiplePlenarprotocols.xml"), xmlString);

        mongoHandler.close();
    }

    /**
     * Testet die Methode {@link backend.export.MultiplePlenarprotocolsExporter#toXML(List)}.
     *
     * @throws IOException
     * @throws TemplateException
     */
    @Test
    void testAllPlenarprotocolsToXML() throws IOException, TemplateException {
        // get a speaker object
        MongoDBHandler mongoHandler = new MongoDBHandler();
        mongoHandler.connect();

        PlenarprotocolDAO plenarprotocolDAO = new PlenarprotocolDAO(mongoHandler);
        String xmlString = MultiplePlenarprotocolsExporter.toXML(plenarprotocolDAO.getAllProtocols().stream().map(Plenarprotocol_impl::getId).collect(Collectors.toList()));
        if (Files.notExists(Paths.get("src/test/out"))) {
            Files.createDirectories(Paths.get("src/test/out"));
        }
        Files.writeString(Paths.get("src/test/out", "exportAllPlenarprotocols.xml"), xmlString);

        mongoHandler.close();
    }


}
