package backend.utility;

import backend.database.CreateSearchCollections;
import backend.importer.ImportHandler;
import backend.importer.ImportVideos;
import backend.importer.MergeSpeakerInfo;
import backend.nlp.DbSpeechImporter;
import backend.plenarprotocol.PlenarprotocolDAO;
import org.apache.uima.UIMAException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Kai
 * @date 13/03/2025
 */
public class PeriodicTask {
    private static ScheduledExecutorService scheduler;

    public static void startScheduledTask() {
        scheduler = Executors.newScheduledThreadPool(1);

        // Schedule to run every 2 hours (as an example)
        long timePeriodInHours = 24;

        Runnable task = () -> {
            System.out.println("Import schedule executed at: " + new java.util.Date());

            PlenarprotocolDAO plenarprotocolDAO = null;
            try {
                plenarprotocolDAO = new PlenarprotocolDAO();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            int minIndex = plenarprotocolDAO.getMaxProtocolIndex() + 1;

            try {
                ImportHandler importHandler = new ImportHandler(minIndex);
                importHandler.importAllProtocols();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            MergeSpeakerInfo.main(new String[0]);

            ImportVideos.main(new String[0]);

            // Hier ist die NLP automatisierung
            try {
                ComparisonService comparisonService = new ComparisonService();
                List<String> missingIds = comparisonService.getAllMissingLinguisticFeatureIds();

                DbSpeechImporter importer = new DbSpeechImporter();
                importer.importSpeeches(missingIds, true);

                CreateSearchCollections.main(new String[0]);
            } catch (IOException | UIMAException e) {
                System.err.println("An exception occurred: " + e.getMessage());
                e.printStackTrace();
            }

        };

        scheduler.scheduleAtFixedRate(task, 0, timePeriodInHours, TimeUnit.HOURS);

        // scheduler.shutdown();
    }
}
