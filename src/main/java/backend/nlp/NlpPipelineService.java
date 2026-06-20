package backend.nlp;

import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Sentence;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.InvalidXMLException;
import org.hucompute.textimager.uima.type.Sentiment;
import org.texttechnologylab.DockerUnifiedUIMAInterface.DUUIComposer;
import org.texttechnologylab.DockerUnifiedUIMAInterface.driver.DUUIRemoteDriver;
import org.texttechnologylab.DockerUnifiedUIMAInterface.lua.DUUILuaContext;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Kai
 * @date 06/03/2025
 */
public class NlpPipelineService {

    private DUUIComposer composer; // Composer, der alle Komponententreiber verwaltet

    public NlpPipelineService(DocumentType documentType) throws IOException, URISyntaxException, CompressorException, InvalidXMLException, SAXException {
        init(documentType);
    }

    private void init(DocumentType documentType) throws IOException, URISyntaxException, CompressorException, InvalidXMLException, SAXException {
        // Allgemeiner Lua-Kontext
        DUUILuaContext ctx = new DUUILuaContext().withJsonLibrary();

        // Composer anlegen
        composer = new DUUIComposer()
                .withSkipVerification(true)
                .withLuaContext(ctx)
                .withWorkers(1);

        // Treiber instanziieren
        DUUIRemoteDriver remoteDriver = new DUUIRemoteDriver();

        // Treiber zum Composer hinzufügen
        composer.addDriver(remoteDriver);

        composer.resetPipeline();

        if (documentType == DocumentType.TEXT) {
            composer.add(new DUUIRemoteDriver.Component("http://spacy.lehre.texttechnologylab.org")
                    .withScale(1)
                    .build());

            composer.add(new DUUIRemoteDriver.Component("http://gervader.lehre.texttechnologylab.org")
                    .withScale(1)
                    .withParameter("selection", "text")
                    .build());

            composer.add(new DUUIRemoteDriver.Component("http://parlbert.lehre.texttechnologylab.org")
                    .withScale(1)
                    .build());

        } else if (documentType == DocumentType.VIDEO) {
            composer.add(new DUUIRemoteDriver.Component("http://whisperx.lehre.texttechnologylab.org")
                    .withScale(1)
                    .withSourceView("video")            // where is the video
                    .withTargetView("transcript")       // where the transcript must be annotated
                    .build());
        } else if (documentType == DocumentType.TRANSCRIPT) {
            composer.add(new DUUIRemoteDriver.Component("http://spacy.lehre.texttechnologylab.org")
                    .withScale(1)
                    .withSourceView("transcript")
                    .withTargetView("transcript")
                    .build());

            composer.add(new DUUIRemoteDriver.Component("http://gervader.lehre.texttechnologylab.org")
                    .withScale(1)
                    .withSourceView("transcript")
                    .withTargetView("transcript")
                    .build());

            composer.add(new DUUIRemoteDriver.Component("http://parlbert.lehre.texttechnologylab.org")
                    .withScale(1)
                    .withSourceView("transcript")
                    .withTargetView("transcript")
                    .build());
        }
    }

    public void processCas(JCas jcas) throws Exception {
        composer.run(jcas);

        for (Sentence sentence : JCasUtil.select(jcas, Sentence.class)) {
            System.out.println("Satz: " + sentence.getCoveredText());
            for (Sentiment s : JCasUtil.selectCovered(Sentiment.class, sentence)) {
                System.out.println("  -> Sentiment: " + s.getSentiment());
            }
        }
    }

    /**
     * Expose the internal DUUIComposer for multi-document processing.
     */
    public DUUIComposer getComposer() {
        return this.composer;
    }

    // ENUM für die Dokumentenart (z.B. Text, Video)
    public enum DocumentType {
        TEXT,
        VIDEO,
        TRANSCRIPT
    }
}
