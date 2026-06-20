package backend.speech.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

/**
 * VideoEncoder utility for encoding a video file into a Base64 string.
 *
 * @author Kai
 * @date 06/03/2025
 */
public class VideoEncoder {

    /**
     * Encodes a video file into a Base64-encoded string.
     *
     * @param videoFile The video file to encode.
     * @return A Base64-encoded string representation of the video.
     * @throws IOException              If the file cannot be read.
     * @throws IllegalArgumentException If the file is invalid.
     */
    public static String encodeVideoToBase64(File videoFile) throws IOException {
        // Validate the file object
        if (videoFile == null) {
            throw new IllegalArgumentException("The provided file is null.");
        }

        if (!videoFile.exists() || !videoFile.isFile()) {
            throw new IllegalArgumentException("The provided file is not valid: " + videoFile.getAbsolutePath());
        }

        // Read file into a byte array
        byte[] videoBytes = Files.readAllBytes(videoFile.toPath());

        // Encode the byte array into a Base64 string
        return Base64.getEncoder().encodeToString(videoBytes);
    }

    /**
     * Main method for testing the functionality.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Please provide the video file path as an argument.");
            return;
        }

        File videoFile = new File(args[0]);

        try {
            // Encode the video to a Base64 string
            String encodedString = encodeVideoToBase64(videoFile);

            // Print the result
            System.out.println("Base64 Encoded Video String:");
            System.out.println(encodedString);
        } catch (IOException e) {
            System.err.println("Error encoding video file: " + e.getMessage());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}