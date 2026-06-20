package backend.nlp;

import backend.linguisticFeatures.LinguisticDAO;
import backend.linguisticFeatures.LinguisticTranskriptDAO;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.impl.XmiCasDeserializer;
import org.apache.uima.fit.factory.JCasFactory;
import org.apache.uima.jcas.JCas;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * This class imports XMI annotations from the folder
 * "src/main/resources/import/xmiAnnotations".
 * All XMI files found in the folder are deserialized into a JCas object.
 *
 * @author Kai
 */
public class XmiAnnotationImporter {

    // Main method for running the XMI import
    public static void main(String[] args) {

        String externalFolderPath = "src/main/resources/import/xmiAnnotationsUnpacked";

        XmiAnnotationImporter importer = new XmiAnnotationImporter();
        try {
            importer.importAnnotations(externalFolderPath);
            System.out.println("All XMI files have been processed in parallel.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads all XMI files from the external folder and loads these annotations into a JCas object.
     *
     * @return A JCas object with the imported annotations.
     * @throws IOException if the folder or the files are not found.
     * @throws Exception   if an error occurs during the deserialization process.
     */
    public void importAnnotations(String externalFolderPath) throws Exception {
        Path annotationsPath = Paths.get(externalFolderPath);
        if (!Files.exists(annotationsPath) || !Files.isDirectory(annotationsPath)) {
            throw new IOException("The path " + annotationsPath.toAbsolutePath() + " is not a valid directory.");
        }

        // Find XMI files using NIO which is faster than File.listFiles()
        List<Path> xmiFiles = Files.find(annotationsPath, 1,
                        (path, attr) -> path.toString().toLowerCase().endsWith(".xmi") && attr.isRegularFile())
                .collect(Collectors.toList());

        if (xmiFiles.isEmpty()) {
            throw new IOException("No XMI files found in folder " + annotationsPath.toAbsolutePath());
        }

        // Pre-create thread-local JCas to avoid repeated creation cost
        ThreadLocal<JCas> threadLocalJCas = ThreadLocal.withInitial(() -> {
            try {
                return JCasFactory.createJCas();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create JCas", e);
            }
        });

        // Create thread-safe DAOs or use connection pooling if these are database related
        LinguisticDAO linguisticDAO = new LinguisticDAO();
        LinguisticTranskriptDAO linguisticTranskriptDAO = new LinguisticTranskriptDAO();

        // Configure thread pool with optimal number of threads
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(processors);

        // Use CompletableFuture for better exception handling and performance
        List<CompletableFuture<Void>> futures = xmiFiles.stream()
                .map(path -> CompletableFuture.runAsync(() -> {
                    JCas jcas = threadLocalJCas.get();
                    try (InputStream is = Files.newInputStream(path, StandardOpenOption.READ)) {
                        // Clear previous content
                        jcas.reset();

                        // Use buffered stream for better I/O performance
                        BufferedInputStream bis = new BufferedInputStream(is, 8192);
                        XmiCasDeserializer.deserialize(bis, jcas.getCas());

                        // Process the file
                        pushJCas(jcas, path.getFileName().toString(), linguisticDAO, linguisticTranskriptDAO);
                    } catch (Exception e) {
                        System.err.println("Error processing file " + path.getFileName() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }, executor))
                .collect(Collectors.toList());

        // Wait for all tasks to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Shutdown executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Pushes the given JCas for further processing.
     * In this example, it processes the JCas using a linguistic pipeline.
     *
     * @param jcas     the JCas created from an XMI file.
     * @param fileName the name of the file that was processed.
     */
    private void pushJCas(JCas jcas, String fileName, LinguisticDAO linguisticDAO, LinguisticTranskriptDAO linguisticTranskriptDAO) throws IOException, CASException {
        // Create an instance of your pipeline (if not thread-safe, consider instantiating per thread)
        LinguisticFeaturePipeline pipeline = new LinguisticFeaturePipeline(linguisticDAO, linguisticTranskriptDAO);
        pipeline.processJCAS(jcas, LinguisticFeaturePipeline.TypeOfImport.PROVIDED);
    }
}