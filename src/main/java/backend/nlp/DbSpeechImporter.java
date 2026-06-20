package backend.nlp;

import backend.database.CreateSearchCollections;
import backend.database.MongoDBHandler;
import backend.linguisticFeatures.LinguisticDAO;
import backend.linguisticFeatures.LinguisticTranskriptDAO;
import backend.speech.LoadVideoFromGridFS;
import backend.speech.SpeechDAO;
import backend.speech.Speech_impl;
import backend.utility.ComparisonService;
import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Fetches speeches from the database and processes them with NLP pipelines.
 * Optimized for reliability with fixed thread count and improved error handling.
 *
 * @author Kai
 * @date 06/03/2025
 */
public class DbSpeechImporter {
    private static final Logger LOGGER = Logger.getLogger(DbSpeechImporter.class.getName());
    private static final int THREAD_COUNT = 4;
    private static final int BATCH_SIZE = 10;
    private static final int TIMEOUT_HOURS = 24;

    private final Map<String, Speech_impl> speechMap = new ConcurrentHashMap<>();
    private final MongoDBHandler mongoDbHandler;
    private final SpeechDAO speechDAO;
    private final LinguisticDAO linguisticDAO;
    private final LinguisticTranskriptDAO linguisticTranskriptDAO;
    private final LoadVideoFromGridFS loadVideoFromGridFS;

    /**
     * Constructs a DbSpeechImporter with efficient connection pooling.
     */
    public DbSpeechImporter() throws IOException {
        // Use the pooled connection - this is a singleton with connection pooling
        this.mongoDbHandler = MongoDBHandler.getInstance();

        // Create single instances of each DAO - they're thread-safe when using connection pooling
        this.speechDAO = new SpeechDAO(mongoDbHandler.getSpeechCollection());
        this.linguisticDAO = new LinguisticDAO();
        this.linguisticTranskriptDAO = new LinguisticTranskriptDAO();
        this.loadVideoFromGridFS = new LoadVideoFromGridFS();

        LOGGER.info("DbSpeechImporter initialized with connection pooling");
    }

    /**
     * Main method for testing the importer
     */
    public static void main(String[] args) {
        try {
            // Get speech IDs to import
            ComparisonService comparisonService = new ComparisonService();
            List<String> speechIds = comparisonService.findMissingLinguisticFeatureIds();
//            List<String> speechIds = comparisonService.getAllMissingLinguisticFeatureIds();
            LOGGER.info("Importing " + speechIds.size() + " speeches");

            // Create importer and process speeches
            DbSpeechImporter importer = new DbSpeechImporter();
            importer.importSpeeches(speechIds, false);

            // Create search collections
            CreateSearchCollections.main(args);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Fatal error during import", e);
            System.exit(1);
        } finally {
            // Shutdown connection pool when application exits
            MongoDBHandler.shutdown();
        }
    }

    /**
     * Fetches speeches concurrently by provided speech IDs and processes them.
     * Optimized with fixed thread count and improved error handling.
     *
     * @param speechIds a list of speech IDs to be fetched and processed
     */
    public void importSpeeches(List<String> speechIds, boolean video) throws UIMAException, IOException {
        if (speechIds == null || speechIds.isEmpty()) {
            LOGGER.severe("Error: The speech ID list is null or empty.");
            return;
        }

        long startTime = System.currentTimeMillis();
        LOGGER.info("Using " + THREAD_COUNT + " threads for all operations");

        // Create a single thread pool for all operations with proper naming
        ExecutorService executor = createThreadPool();

        try {
            // STEP 1: FETCH SPEECHES
            fetchSpeeches(speechIds, executor);

            // STEP 2: PROCESS SPEECHES
            processSpeeches(executor, video);

            // Print summary
            printSummary(startTime, speechMap.size());
        } finally {
            // Ensure executor is always shut down properly
            shutdownExecutor(executor);
        }
    }

    /**
     * Creates a properly configured thread pool.
     */
    private ExecutorService createThreadPool() {
        return new ThreadPoolExecutor(
                THREAD_COUNT,                     // Core pool size
                THREAD_COUNT,                     // Max pool size
                60L, TimeUnit.SECONDS,           // Keep alive time
                new LinkedBlockingQueue<>(),      // Work queue
                new ThreadFactory() {
                    private final AtomicInteger counter = new AtomicInteger(1);

                    @Override
                    public Thread newThread(Runnable r) {
                        Thread thread = new Thread(r, "SpeechProcessor-" + counter.getAndIncrement());
                        thread.setDaemon(false);
                        return thread;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy() // Rejection policy
        );
    }

    /**
     * Fetches speeches in batches using the provided executor.
     */
    private void fetchSpeeches(List<String> speechIds, ExecutorService executor) {
        AtomicInteger fetchedCount = new AtomicInteger(0);
        final int totalToFetch = speechIds.size();
        LOGGER.info("Starting to fetch " + totalToFetch + " speeches...");

        List<CompletableFuture<Void>> fetchFutures = new ArrayList<>();

        // Split work into batches
        for (int i = 0; i < speechIds.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, speechIds.size());
            List<String> batch = speechIds.subList(i, endIndex);

            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    () -> fetchSpeechBatch(batch, fetchedCount, totalToFetch),
                    executor
            );

            fetchFutures.add(future);
        }

        // Wait for all fetches to complete with timeout
        try {
            CompletableFuture<Void> allFetches = CompletableFuture.allOf(
                    fetchFutures.toArray(new CompletableFuture[0]));
            allFetches.get(TIMEOUT_HOURS, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Fetch operation was interrupted", e);
        } catch (ExecutionException e) {
            LOGGER.log(Level.SEVERE, "Error during fetch operation", e.getCause());
        } catch (TimeoutException e) {
            LOGGER.log(Level.SEVERE, "Fetch operation timed out after " + TIMEOUT_HOURS + " hours", e);
        }

        LOGGER.info("Fetch completed. Retrieved " + speechMap.size() + " speeches.");
    }

    /**
     * Fetches a batch of speeches with retry mechanism.
     */
    private void fetchSpeechBatch(List<String> batch, AtomicInteger fetchedCount, int totalToFetch) {
        final int MAX_RETRIES = 3;

        for (String speechId : batch) {
            boolean success = false;
            int retries = 0;

            while (!success && retries < MAX_RETRIES) {
                try {
                    Speech_impl speech = speechDAO.findById(speechId);
                    if (speech != null) {
                        speechMap.put(speechId, speech);
                        int current = fetchedCount.incrementAndGet();
                        if (current % 10 == 0) {
                            LOGGER.info(String.format("Fetched %d/%d speeches (%.1f%%)",
                                    current, totalToFetch, (100.0 * current / totalToFetch)));
                        }
                        success = true;
                    } else {
                        LOGGER.warning("Speech not found: " + speechId);
                        success = true; // Not found, but not an error - don't retry
                    }
                } catch (Exception e) {
                    retries++;
                    if (retries >= MAX_RETRIES) {
                        LOGGER.log(Level.SEVERE, "Failed to fetch speech " + speechId + " after " + MAX_RETRIES + " attempts", e);
                    } else {
                        LOGGER.warning("Error fetching speech " + speechId + ", retrying (" + retries + "/" + MAX_RETRIES + "): " + e.getMessage());
                        try {
                            Thread.sleep(1000 * retries); // Exponential backoff
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            LOGGER.warning("Sleep interrupted during retry backoff");
                        }
                    }
                }
            }
        }
    }

    /**
     * Processes fetched speeches using the provided executor.
     */
    private void processSpeeches(ExecutorService executor, boolean video) {
        if (speechMap.isEmpty()) {
            LOGGER.info("No speeches to process. Exiting.");
            return;
        }

        AtomicInteger processedCount = new AtomicInteger(0);
        final int totalToProcess = speechMap.size();
        List<Speech_impl> speechesToProcess = new ArrayList<>(speechMap.values());
        List<CompletableFuture<Void>> processFutures = new ArrayList<>();

        // Process speeches in batches appropriate for thread count
        int processBatchSize = Math.max(1, speechesToProcess.size() / THREAD_COUNT);
        if (processBatchSize > 5) processBatchSize = 5; // Cap batch size for better load balancing

        for (int i = 0; i < speechesToProcess.size(); i += processBatchSize) {
            int endIndex = Math.min(i + processBatchSize, speechesToProcess.size());
            List<Speech_impl> batch = speechesToProcess.subList(i, endIndex);

            CompletableFuture<Void> future = CompletableFuture.runAsync(
                    () -> processSpeechBatch(batch, processedCount, totalToProcess, video),
                    executor
            );

            processFutures.add(future);
        }

        // Wait for all processing to complete with timeout
        try {
            CompletableFuture<Void> allProcessing = CompletableFuture.allOf(
                    processFutures.toArray(new CompletableFuture[0]));
            allProcessing.get(TIMEOUT_HOURS, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.SEVERE, "Processing operation was interrupted", e);
        } catch (ExecutionException e) {
            LOGGER.log(Level.SEVERE, "Error during processing operation", e.getCause());
        } catch (TimeoutException e) {
            LOGGER.log(Level.SEVERE, "Processing operation timed out after " + (TIMEOUT_HOURS) + " hours", e);
        }
    }

    /**
     * Processes a batch of speeches with thread-local NLP services.
     */
    private void processSpeechBatch(List<Speech_impl> batch, AtomicInteger processedCount, int totalToProcess, boolean video) {
        try {
            // Create thread-local NLP services
            NlpPipelineService nlpServiceText = new NlpPipelineService(NlpPipelineService.DocumentType.TEXT);
            NlpPipelineService nlpServiceVideo = new NlpPipelineService(NlpPipelineService.DocumentType.VIDEO);
            NlpPipelineService nlpServiceTranscript = new NlpPipelineService(NlpPipelineService.DocumentType.TRANSCRIPT);

            // Create linguistic feature pipeline
            LinguisticFeaturePipeline linguisticFeaturePipeline =
                    new LinguisticFeaturePipeline(linguisticDAO, linguisticTranskriptDAO);

            // Process each speech in the batch (sequentially within the thread)
            for (Speech_impl speech : batch) {
                processSingleSpeech(speech, nlpServiceText, nlpServiceVideo, nlpServiceTranscript,
                        linguisticFeaturePipeline, processedCount, totalToProcess, video);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in processing batch", e);
        }
    }

    /**
     * Processes a single speech with all its components.
     */
    private void processSingleSpeech(Speech_impl speech,
                                     NlpPipelineService nlpServiceText,
                                     NlpPipelineService nlpServiceVideo,
                                     NlpPipelineService nlpServiceTranscript,
                                     LinguisticFeaturePipeline linguisticFeaturePipeline,
                                     AtomicInteger processedCount,
                                     int totalToProcess,
                                     boolean video) {
        String speechId = speech.get_id();
        try {
            LOGGER.info("Processing speech ID: " + speechId);

            // Step 1: Create CAS objects
            JCas textCas = speech.toCAS(loadVideoFromGridFS, video);
            JCas videoCas = textCas.getView("video");
            JCas transcriptCas = textCas.getView("transcript");

            // Step 2: Process text if available
            processTextComponent(textCas, nlpServiceText, linguisticFeaturePipeline, speechId);

            if (video) {
                // Step 3: Process video if available
                processVideoComponent(videoCas, nlpServiceVideo, speechId);

                // Step 4: Process transcript if available
                processTranscriptComponent(transcriptCas, nlpServiceTranscript, linguisticFeaturePipeline, speechId);
            }

            int current = processedCount.incrementAndGet();
            LOGGER.info(String.format("Processed speech %d/%d (%.1f%%): %s",
                    current, totalToProcess, (100.0 * current / totalToProcess), speechId));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing speech " + speechId, e);
            // Continue with next speech rather than stopping the entire batch
        }
    }

    /**
     * Processes the text component of a speech.
     */
    private void processTextComponent(JCas textCas,
                                      NlpPipelineService nlpServiceText,
                                      LinguisticFeaturePipeline linguisticFeaturePipeline,
                                      String speechId) {
        try {
            if (!isEmpty(textCas.getDocumentText())) {
                LOGGER.info("Processing text for speech: " + speechId);
                nlpServiceText.getComposer().run(textCas);

                // Wait for text processing to complete
                linguisticFeaturePipeline.processJCAS(textCas,
                        LinguisticFeaturePipeline.TypeOfImport.CALCULATED_TEXT);

                LOGGER.info("Text processing completed for: " + speechId);
            } else {
                LOGGER.info("No text available for speech: " + speechId);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error processing text for speech " + speechId, e);
        }
    }

    /**
     * Processes the video component of a speech.
     */
    private void processVideoComponent(JCas videoCas,
                                       NlpPipelineService nlpServiceVideo,
                                       String speechId) {
        try {
            if (!isEmpty(videoCas.getSofaDataString())) {
                LOGGER.info("Processing video for speech: " + speechId);

                // Process video - this must complete before transcript processing
                nlpServiceVideo.getComposer().run(videoCas);
                LOGGER.info("Video processing completed for: " + speechId);
            } else {
                LOGGER.info("No video data available for speech: " + speechId);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error processing video for speech " + speechId, e);
        }
    }

    /**
     * Processes the transcript component of a speech.
     */
    private void processTranscriptComponent(JCas transcriptCas,
                                            NlpPipelineService nlpServiceTranscript,
                                            LinguisticFeaturePipeline linguisticFeaturePipeline,
                                            String speechId) {
        try {
            String transcriptText = transcriptCas.getDocumentText();

            if (!isEmpty(transcriptText)) {
                LOGGER.info("Found transcript text for: " + speechId +
                        " (length: " + transcriptText.length() + " characters)");

                // Process transcript
                LOGGER.info("Starting transcript processing for: " + speechId);
                nlpServiceTranscript.getComposer().run(transcriptCas);
                LOGGER.info("NLP pipeline completed for transcript: " + speechId);

                // Now store the processed transcript features
                linguisticFeaturePipeline.processJCAS(transcriptCas,
                        LinguisticFeaturePipeline.TypeOfImport.CALCULATED_TRANSCRIPT);

                LOGGER.info("Transcript feature processing completed for: " + speechId);
            } else {
                LOGGER.info("No transcript text available for speech: " + speechId);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error processing transcript for speech " + speechId, e);
        }
    }

    /**
     * Check if a string is null or empty
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Properly shuts down executor service with graceful timeout
     */
    private void shutdownExecutor(ExecutorService executor) {
        executor.shutdown();
        try {
            // Wait a while for existing tasks to terminate
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                // Cancel currently executing tasks
                executor.shutdownNow();
                // Wait a while for tasks to respond to being cancelled
                if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                    LOGGER.severe("Executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            // (Re-)Cancel if current thread also interrupted
            executor.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Shutdown interrupted", e);
        }
    }

    /**
     * Prints a summary of the processing operation
     */
    private void printSummary(long startTime, int processedCount) {
        long endTime = System.currentTimeMillis();
        double totalTimeSeconds = (endTime - startTime) / 1000.0;

        LOGGER.info("==================== Import Summary ====================");
        LOGGER.info("Total processing time: " + totalTimeSeconds + " seconds");
        LOGGER.info("Successfully processed " + processedCount + " speeches");
        if (processedCount > 0) {
            LOGGER.info("Average time per speech: " + (totalTimeSeconds / processedCount) + " seconds");
            LOGGER.info("Processing rate: " + (processedCount / totalTimeSeconds) + " speeches/second");
        }
        LOGGER.info("=======================================================");
    }
}