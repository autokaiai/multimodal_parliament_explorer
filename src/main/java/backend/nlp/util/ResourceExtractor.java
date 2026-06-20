package backend.nlp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

/**
 * @author Kai
 * Date: 05/03/2025
 */
public class ResourceExtractor {

    public static void main(String[] args) {
        // Use the absolute paths you provided
        String resourceFolder = "src/main/resources/import/xmiAnnotations";
        String outputFolder = "src/main/resources/import/xmiAnnotationsUnpacked";

        // Debug: print absolute paths
        System.out.println("Input folder: " + resourceFolder);
        System.out.println("Output folder: " + outputFolder);

        extractResources(resourceFolder, outputFolder);
    }

    private static void extractResources(String resourceFolderPath, String outputFolderPath) {
        File resourceDir = new File(resourceFolderPath);
        if (!resourceDir.exists() || !resourceDir.isDirectory()) {
            System.err.println("Resource folder does not exist or is not a directory: " + resourceFolderPath);
            return;
        }

        File outputDir = new File(outputFolderPath);
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            System.err.println("Failed to create output folder: " + outputFolderPath);
            return;
        }

        // List all .xmi.gz files in the input directory
        File[] files = resourceDir.listFiles((dir, name) -> name.endsWith(".xmi.gz"));
        if (files == null || files.length == 0) {
            System.err.println("No .xmi.gz files found in " + resourceFolderPath);
            return;
        }

        // Process each file in parallel
        Arrays.stream(files).parallel().forEach(file -> {
            // Remove only the ".gz" extension so "ID203603700.xmi.gz" becomes "ID203603700.xmi"
            File outputFile = new File(outputDir, removeGzExtension(file.getName()));
            try {
                extractFile(file, outputFile);
                System.out.println("Extracted: " + file.getName() + " -> " + outputFile.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("Error extracting " + file.getName() + ": " + e.getMessage());
            }
        });
    }

    private static void extractFile(File inputFile, File outputFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFile);
             GZIPInputStream gzipIn = new GZIPInputStream(fis)) {
            Files.copy(gzipIn, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    // Remove only the ".gz" extension from the file name
    private static String removeGzExtension(String fileName) {
        if (fileName.endsWith(".gz")) {
            return fileName.substring(0, fileName.length() - 3);
        }
        return fileName;
    }
}
