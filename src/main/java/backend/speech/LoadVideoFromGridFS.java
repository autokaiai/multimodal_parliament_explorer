package backend.speech;

import backend.database.MongoDBHandler;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import org.bson.types.ObjectId;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.HashMap;

/**
 * Class for loading video files from MongoDB GridFS.
 * Modified to use the connection pool.
 *
 * @author Kai
 * @date 16/03/2025
 */
public class LoadVideoFromGridFS {
    // Instance fields
    private final MongoDBHandler mongoDbHandler;
    private final GridFSBucket gridFSBucket;
    private final SpeechDAO speechDAO;

    /**
     * Constructor that takes a specific MongoDB handler
     *
     * @param mongoDbHandler The MongoDB handler to use
     */
    public LoadVideoFromGridFS(MongoDBHandler mongoDbHandler) {
        this.mongoDbHandler = mongoDbHandler;
        this.gridFSBucket = mongoDbHandler.getGridFSBucket();
        this.speechDAO = new SpeechDAO(mongoDbHandler.getSpeechCollection());
    }

    /**
     * Default constructor that uses the pooled connection
     */
    public LoadVideoFromGridFS() {
        try {
            this.mongoDbHandler = MongoDBHandler.getInstance();
            this.gridFSBucket = mongoDbHandler.getGridFSBucket();
            this.speechDAO = new SpeechDAO(mongoDbHandler.getSpeechCollection());
        } catch (IOException e) {
            System.out.println("Failed to initialize LoadVideoFromGridFS");
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize LoadVideoFromGridFS", e);
        }
    }

    /**
     * Example usage of this class.
     */
    public static void main(String[] args) {
        try {
            // Create an instance of the class using the pooled connection
            LoadVideoFromGridFS loader = new LoadVideoFromGridFS();

            // Get a speech implementation using the instance's DAO
            Speech_impl speechImpl = loader.getSpeechDAO().findById("ID2021201900");

            // Use the instance methods
            HashMap<String, String> videoData = loader.getVideoBase64(speechImpl);

            System.out.println("Video loaded, MIME type: " + videoData.get("pMimeType"));
            System.out.println("Video base64 length: " + videoData.get("videoBase64").length());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a video file by its GridFS ID and returns it as a byte array.
     *
     * @param videoId The ObjectId of the video file in GridFS
     * @return The byte array of the file
     * @throws IOException If an I/O error occurs
     */
    public byte[] loadVideo(ObjectId videoId) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try (GridFSDownloadStream downloadStream = gridFSBucket.openDownloadStream(videoId)) {
            transferToOutputStream(downloadStream, outputStream);
        }

        return outputStream.toByteArray();
    }

    /**
     * Retrieves a video from GridFS and returns it as a Base64-encoded string with its MIME type.
     *
     * @param speechImpl The speech implementation containing video info
     * @return HashMap containing the Base64-encoded video and its MIME type
     * @throws IOException If an I/O error occurs
     */
    public HashMap<String, String> getVideoBase64(Speech_impl speechImpl) throws IOException {
        ObjectId videoFileId = speechImpl.getVideoFileId();
        if (videoFileId == null) {
            return null;
        }

        String pMimeType = "video/mp4"; // Default MIME type

        // Try to get the file to extract MIME type from metadata
        try (MongoCursor<GridFSFile> cursor = gridFSBucket.find(Filters.eq("_id", videoFileId)).iterator()) {
            if (cursor.hasNext()) {
                GridFSFile gridFSFile = cursor.next();
                if (gridFSFile.getMetadata() != null && gridFSFile.getMetadata().containsKey("contentType")) {
                    pMimeType = gridFSFile.getMetadata().getString("contentType");
                }
            }
        }

        // Get the video bytes and convert to Base64
        System.out.println("Loading video from GridFS...");
        byte[] videoBytes = loadVideo(videoFileId);
        // Print the video bytes length
        System.out.println("Video bytes length: " + videoBytes.length);
        String videoBase64 = Base64.getEncoder().encodeToString(videoBytes);

        HashMap<String, String> videoMap = new HashMap<>();
        videoMap.put("videoBase64", videoBase64);
        videoMap.put("pMimeType", pMimeType);

        return videoMap;
    }

    /**
     * Efficiently transfers data from an input stream to an output stream.
     */
    private void transferToOutputStream(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
    }

    /**
     * Get the speech DAO instance.
     *
     * @return The SpeechDAO instance
     */
    public SpeechDAO getSpeechDAO() {
        return speechDAO;
    }
}