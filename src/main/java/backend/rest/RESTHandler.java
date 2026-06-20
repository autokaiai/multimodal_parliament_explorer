package backend.rest;

import backend.database.MongoDBHandler;
import backend.export.MultiplePlenarprotocolsExporter;
import backend.export.TopicExporter;
import backend.linguisticFeatures.LinguisticDAO;
import backend.linguisticFeatures.LinguisticFeaturesAggregate_Impl;
import backend.linguisticFeatures.TopicDAO;
import backend.plenarprotocol.PlenarprotocolDAO;
import backend.plenarprotocol.Plenarprotocol_impl;
import backend.speaker.SpeakerDAO;
import backend.speaker.Speaker_impl;
import backend.speech.SpeechDAO;
import backend.speech.SpeechService;
import backend.speech.Speech_impl;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import io.javalin.Javalin;
import io.javalin.http.Context;
import org.bson.Document;

import javax.sound.midi.SysexMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.eq;

/**
 * Definition der REST-Endpunkte
 *
 * @author Philipp Noah Hein
 * @date 10.03.2025
 */

public class RESTHandler {

    private static final MongoDBHandler dbHandler;

    // um IO Exception abzufangen
    static {
        try {
            dbHandler = new MongoDBHandler();
            dbHandler.connect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Registriert die REST-Routen
     *
     * @author Philipp Noah Hein
     * @date 10.03.2025
     */
    public static void registerRoutes(Javalin app) {
        // Latex Routen
        app.get("/export/pdf/speaker/", RESTHandler::exportBySpeaker);
        app.get("/export/pdf/topic/", RESTHandler::exportByTopic);
        app.get("/export/pdf/protocol/", RESTHandler::exportByProtocol);
        app.get("/export/pdf/protocols/", RESTHandler::exportMultipleProtocols);
        app.get("/export/pdf/speech/", RESTHandler::exportSpeech);

        // XML Routen
        app.get("/export/xml/speaker/", RESTHandler::exportBySpeaker);
        app.get("/export/xml/topic/", RESTHandler::exportByTopic);
        app.get("/export/xml/protocol/", RESTHandler::exportByProtocol);
        app.get("/export/xml/protocols/", RESTHandler::exportMultipleProtocols);
        app.get("/export/xml/speech/", RESTHandler::exportSpeech);

        // Visualisierungs-Routen
        app.get("/api/speeches", RESTHandler::getAllSpeeches);
        app.get("/api/speech/{speechId}", RESTHandler::getSpeechById);
        app.get("/api/linguistic-features", RESTHandler::getLinguisticFeatures);
        app.get("/api/linguistic-features/{speechId}", RESTHandler::getLinguisticFeatureById);


        // Neue Route für alle Reden über den Service
        app.get("/speeches/paginated", RESTHandler::getAllSpeechesPaginated);

        app.get("/speech/{speechId}", RESTHandler::showSpeechDetails);


        app.get("/api/topics", RESTHandler::getAllTopics);
        app.get("/api/linguistic-features/by-topic/{topic}", RESTHandler::getLinguisticFeaturesByTopic);

        // Root-Route für die Startseite
        app.get("/", RESTHandler::getAllSpeechesPaginated);

        // Restlichen Routen
        app.delete("/api/speech/{speechId}", RESTHandler::deleteSpeech);
        app.post("/api/speech/{speechId}/comment", RESTHandler::addComment);
        app.put("/api/speech/{speechId}", RESTHandler::updateSpeech);


    }

    /**
     * Exportiert eine Rede anhand der RedeID
     *
     * @author Philipp Landmann
     * @date 18.03.2025
     */
    private static void exportSpeech(Context ctx) throws IOException {
        if (!ctx.queryParamMap().containsKey("id")) {
            ctx.status(400);
            ctx.json(Map.of("error", "Keine Rede-ID angegeben."));
            return;
        }
        String speechId = ctx.queryParam("id");

        SpeechDAO speechDAO = new SpeechDAO(dbHandler.getSpeechCollection());
        Speech_impl speech = speechDAO.findById(speechId);
        if (speech == null) {
            ctx.status(404);
            ctx.json(Map.of("error", "Rede mit ID \"" + speechId + "\" nicht gefunden."));
            return;
        }

        boolean disableTikz = Boolean.parseBoolean(ctx.queryParam("disableTikz"));

        if (ctx.path().contains("pdf")) {
            export(ctx, disableTikz, "speech", speechId, speech, true);
        } else if (ctx.path().contains("xml")) {
            export(ctx, disableTikz, "speech", speechId, speech, false);
        }
    }

    /**
     * Exportiert alle Reden eines Redners anhand der SpeakerID
     *
     * @author Philipp Landmann
     * @date 16.03.2025
     */
    private static void exportBySpeaker(Context ctx) throws IOException {
        if (!ctx.queryParamMap().containsKey("id")) {
            ctx.status(400);
            ctx.json(Map.of("error", "Keine Speaker-ID angegeben."));
            return;
        }
        String speakerId = ctx.queryParam("id");

        SpeakerDAO speakerDAO = new SpeakerDAO(dbHandler.getSpeakerCollection());
        Speaker_impl speaker = speakerDAO.findById(speakerId);
        if (speaker == null) {
            ctx.status(404);
            ctx.json(Map.of("error", "Redner mit ID \"" + speakerId + "\" nicht gefunden."));
            return;
        }

        boolean disableTikz = Boolean.parseBoolean(ctx.queryParam("disableTikz"));

        if (ctx.path().contains("pdf")) {
            export(ctx, disableTikz, "speaker", speakerId, speaker, true);
        } else if (ctx.path().contains("xml")) {
            export(ctx, disableTikz, "speaker", speakerId, speaker, false);
        }
    }

    /**
     * Exportiert ein Objekt anhand des Typs und der ID (Hilfsmethode für die Export Routen)
     *
     * @param ctx            Javalin Context
     * @param disableTikz    Ob Tikz-Abbildungen deaktiviert werden sollen
     * @param exportType     Der Typ des Exports (speaker, topic, protocol, protocols)
     * @param identifier     Die ID des Objekts
     * @param objectToExport Das Objekt, das exportiert werden soll
     * @param isPDF          Ob das Exportformat PDF ist (sonst XML)
     * @author Philipp Landmann
     */
    private static void export(Context ctx, boolean disableTikz, String exportType, String identifier, Object objectToExport, boolean isPDF) {
        // Generiere einen Dateinamen basierend auf Datum und Typ
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + " - ";
        switch (exportType) {
            case "speaker" -> fileName += "Redner " + identifier;
            case "speech" -> fileName += "Rede " + identifier;
            case "topic" -> fileName += "Topic " + identifier;
            case "protocol" -> fileName += "Plenarprotokoll ";
            case "protocols" -> fileName += "Plenarprotokolle " + identifier;
        }
        if (isPDF) {
            fileName += ".pdf";
        } else {
            fileName += ".xml";
        }
        final String fileNameFinal = fileName.replace("/", "-").replace("\\", "-").replace(":", "-");

        // Export-Daten synchron erzeugen
        byte[] data;
        try {
            if (exportType.equals("speaker")) {
                Speaker_impl speaker = (Speaker_impl) objectToExport;
                data = isPDF ? speaker.toPDF(disableTikz) : speaker.toXML().getBytes();
            } else if (exportType.equals("topic")) {
                data = isPDF ? TopicExporter.toPDF(identifier, disableTikz) : TopicExporter.toXML(identifier).getBytes();
            } else if (exportType.equals("protocol")) {
                Plenarprotocol_impl plenarprotocol = (Plenarprotocol_impl) objectToExport;
                data = isPDF ? plenarprotocol.toPDF(disableTikz) : plenarprotocol.toXML().getBytes();
            } else if (exportType.equals("protocols")) {
                List<String> ids = Arrays.stream(identifier.split(", ")).toList();
                data = isPDF ? MultiplePlenarprotocolsExporter.toPDF(ids, disableTikz) : MultiplePlenarprotocolsExporter.toXML(ids).getBytes();
            } else if (exportType.equals("speech")) {
                Speech_impl speech = (Speech_impl) objectToExport;
                data = isPDF ? speech.toPDF(disableTikz) : speech.toXML().getBytes();
            } else {
                ctx.status(400);
                ctx.json(Map.of("error", "Ungültiger Export-Typ"));
                return;
            }
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(Map.of("error", "Fehler beim Erzeugen des Exports: " + e.getMessage()));
            return;
        }

        // Setze Content-Type basierend auf dem Dateityp
        if (fileNameFinal.toLowerCase().endsWith(".pdf")) {
            ctx.contentType("application/pdf");
        } else {
            ctx.contentType("application/xml");
        }
        // Setze den Download-Header, sodass der Browser den Download startet
        ctx.header("Content-Disposition", "attachment; filename=\"" + fileNameFinal + "\"");

        // Sende die exportierten Daten an den Client
        ctx.result(data);
    }


    /**
     * Exportiert alle Reden zu einem bestimmten Thema anhand des Topics
     *
     * @author Philipp Landmann
     * @date 16.03.2025
     */
    private static void exportByTopic(Context ctx) {
        if (!ctx.queryParamMap().containsKey("id")) {
            ctx.status(400);
            ctx.json(Map.of("error", "Keine Topic-ID angegeben."));
            return;
        }
        String topicId = ctx.queryParam("id");

        boolean disableTikz = Boolean.parseBoolean(ctx.queryParam("disableTikz"));

        if (ctx.path().contains("pdf")) {
            export(ctx, disableTikz, "topic", topicId, null, true);
        } else if (ctx.path().contains("xml")) {
            export(ctx, disableTikz, "topic", topicId, null, false);
        }
    }

    /**
     * Exportiert ein Plenarprotokoll anhand der ID
     *
     * @author Philipp Landmann
     * @date 16.03.2025
     */
    private static void exportByProtocol(Context ctx) {
        if (!ctx.queryParamMap().containsKey("id")) {
            ctx.status(400);
            ctx.json(Map.of("error", "Keine Plenarprotokoll-ID angegeben."));
            return;
        }

        String plenarprotocolId = ctx.queryParam("id");

        PlenarprotocolDAO plenarprotocolDAO = new PlenarprotocolDAO(dbHandler);
        Plenarprotocol_impl plenarprotocol = plenarprotocolDAO.getProtocolById(plenarprotocolId);

        if (plenarprotocol == null) {
            ctx.status(404);
            ctx.json(Map.of("error", "Plenarprotokoll mit ID \"" + plenarprotocolId + "\" nicht gefunden."));
            return;
        }

        boolean disableTikz = Boolean.parseBoolean(ctx.queryParam("disableTikz"));

        if (ctx.path().contains("pdf")) {
            export(ctx, disableTikz, "protocol", plenarprotocolId, plenarprotocol, true);

        } else if (ctx.path().contains("xml")) {
            export(ctx, disableTikz, "protocol", plenarprotocolId, plenarprotocol, false);
        }
    }

    /**
     * Exportiert mehrere Protokolle anhand der IDs
     *
     * @author Philipp Landmann
     * @date 16.03.2025
     */
    private static void exportMultipleProtocols(Context ctx) {
        if (!ctx.queryParamMap().containsKey("ids")) {
            ctx.status(400);
            ctx.json(Map.of("error", "Keine Plenarprotokoll-IDs angegeben."));
            return;
        }
        List<String> plenarprotocolIds = Arrays.stream(ctx.queryParam("ids").split(",")).map(String::trim).collect(Collectors.toList());

        boolean disableTikz = Boolean.parseBoolean(ctx.queryParam("disableTikz"));

        if (ctx.path().contains("pdf")) {
            export(ctx, disableTikz, "protocols", String.join(", ", plenarprotocolIds), null, true);
        } else if (ctx.path().contains("xml")) {
            export(ctx, disableTikz, "protocols", String.join(", ", plenarprotocolIds), null, false);
        }
    }


    /**
     * Gibt alle verfügbaren Reden zurück
     *
     * @author Philipp Schneider
     * @date 15.03.2025
     */
    private static void getAllSpeeches(Context ctx) {
        MongoCollection<Document> collection = dbHandler.getSpeechCollection();
        List<Document> speeches = collection.find().into(new ArrayList<>());
        ctx.json(speeches);
    }

    /**
     * Gibt eine Rede anhand der ID zurück
     *
     * @author Philipp Schneider
     * @date 15.03.2025
     */
    private static void getSpeechById(Context ctx) {
        String speechId = ctx.pathParam("speechId");
        MongoCollection<Document> collection = dbHandler.getSpeechCollection();
        Document speech = collection.find(eq("_id", speechId)).first();
        ctx.json(speech != null ? speech : Map.of("error", "Speech not found"));
    }

    /**
     * Gibt linguistische Features für mehrere Reden zurück
     *
     * @author Philipp Schneider
     * @date 15.03.2025
     */
    private static void getLinguisticFeatures(Context ctx) {
        try {
            String redeIdsParam = ctx.queryParam("redeIds");
            if (redeIdsParam == null) {
                redeIdsParam = "";
            }
            List<String> redeIds = new ArrayList<>();

            if (!redeIdsParam.isEmpty()) {
                redeIds = Arrays.asList(redeIdsParam.split(","));
            }

            List<LinguisticFeaturesAggregate_Impl> results = new ArrayList<>();
            LinguisticDAO linguisticDAO = new LinguisticDAO();

            if (redeIds.isEmpty()) {
                // Hole alle Daten (oder begrenzt)
                MongoCollection<Document> collection = dbHandler.getLinguisticFeaturesCollection();
                List<Document> docs = collection.find().limit(10).into(new ArrayList<>());

                for (Document doc : docs) {
                    results.add(linguisticDAO.parseDocument(doc));
                }
            } else {
                // Hole spezifische Reden
                for (String redeId : redeIds) {
                    Optional<LinguisticFeaturesAggregate_Impl> result = linguisticDAO.findByDocumentId(redeId);
                    result.ifPresent(results::add);
                }
            }

            ctx.json(results);

        } catch (Exception e) {
            ctx.status(500).json(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Gibt linguistische Features für eine einzelne Rede zurück
     *
     * @author Philipp Schneider
     * @date 15.03.2025
     */
    private static void getLinguisticFeatureById(Context ctx) {
        try {
            String speechId = ctx.pathParam("speechId");
            System.out.println("getLinguisticFeatureById aufgerufen mit speechId: " + speechId);

            if (speechId == null || speechId.trim().isEmpty()) {
                System.out.println("Fehler: Speech ID ist leer oder null");
                ctx.status(400).json(Map.of("error", "Speech ID is required"));
                return;
            }

            System.out.println("Erstelle LinguisticDAO...");
            LinguisticDAO linguisticDAO = new LinguisticDAO();
            try {
                System.out.println("Rufe findByDocumentId auf mit speechId: " + speechId);
                Optional<LinguisticFeaturesAggregate_Impl> result = linguisticDAO.findByDocumentId(speechId);

                System.out.println("Ergebnis erhalten: " + (result.isPresent() ? "gefunden" : "nicht gefunden"));

                if (result.isPresent()) {
                    LinguisticFeaturesAggregate_Impl features = result.get();
                    System.out.println("Gefundene Features: " + features);
                    System.out.println("topicCounts: " + features.getTopicCounts());
                    System.out.println("posCounts: " + features.getPosCounts());
                    System.out.println("sentimentDistribution: " + features.getSentimentDistribution());
                    System.out.println("namedEntityCounts: " + features.getNamedEntityCounts());

                    ctx.json(features);
                } else {
                    System.out.println("Keine Daten gefunden, erstelle leere Antwort-Struktur");
                    // Wenn keine Daten gefunden werden, geben wir eine leere Struktur zurück
                    Map<String, Object> emptyResponse = new HashMap<>();
                    emptyResponse.put("redeId", speechId);
                    emptyResponse.put("topicCounts", new HashMap<>());
                    emptyResponse.put("posCounts", new HashMap<>());
                    emptyResponse.put("sentimentDistribution", new HashMap<String, Double>() {{
                        put("positive", 0.0);
                        put("neutral", 1.0);
                        put("negative", 0.0);
                    }});
                    emptyResponse.put("namedEntityCounts", new ArrayList<>());

                    System.out.println("Sende leere Antwort-Struktur: " + emptyResponse);
                    ctx.json(emptyResponse);
                }
            } catch (Exception e) {
                System.out.println("Fehler im findByDocumentId-Block: " + e.getMessage());
                e.printStackTrace();
                throw e; // Werfe die Exception weiter, damit sie vom äußeren try-catch-Block gefangen wird
            } finally {
                System.out.println("Schließe LinguisticDAO...");
            }
        } catch (Exception e) {
            System.out.println("Fehler in getLinguisticFeatureById: " + e.getMessage());
            e.printStackTrace();
            ctx.status(500).json(Map.of(
                    "error", "Internal server error",
                    "message", e.getMessage(),
                    "stackTrace", Arrays.toString(e.getStackTrace())
            ));
        }
    }



    /**
     * Gibt alle Topics zurück
     *
     * @author Philipp Schneider
     * @date 15.03.2025
     */
    private static void getAllTopics(Context ctx) {
        try {
            TopicDAO topicDAO = new TopicDAO();
            List<Document> topics = topicDAO.getAllTopics();
            ctx.json(topics);
            topicDAO.close();
        } catch (Exception e) {
            ctx.status(500).result("Fehler beim Abrufen der Topics: " + e.getMessage());
        }
    }


    /**
     * Gibt linguistische Features für Reden eines bestimmten Topics zurück
     *
     * @author Philipp Schneider
     * @date 15.03.2025
     */
    private static void getLinguisticFeaturesByTopic(Context ctx) {
        try {
            String topic = ctx.pathParam("topic");
            TopicDAO topicDAO = new TopicDAO();
            List<Speech_impl> speeches = topicDAO.getSpeechesByTopic(topic);

            List<LinguisticFeaturesAggregate_Impl> results = new ArrayList<>();
            LinguisticDAO linguisticDAO = new LinguisticDAO();

            for (Speech_impl speech : speeches) {
                Optional<LinguisticFeaturesAggregate_Impl> result = linguisticDAO.findByDocumentId(speech.get_id());
                result.ifPresent(results::add);
            }

            ctx.json(results);

        } catch (Exception e) {
            ctx.status(500).result("Fehler beim Abrufen der linguistischen Daten nach Topic: " + e.getMessage());
        }
    }




    private static void showSpeechDetails(Context ctx) {
        try {
            String speechId = ctx.pathParam("speechId");

            // SpeechDAO und Service initialisieren
            SpeechDAO speechDAO = new SpeechDAO(dbHandler.getSpeechCollection());
            SpeechService speechService = new SpeechService(speechDAO);

            // Rede abrufen
            Speech_impl speech = speechService.getSpeechById(speechId);
            if (speech == null) {
                ctx.status(404).result("Rede nicht gefunden.");
                return;
            }

            // Verbindung zur `linguistic_features` Collection
            MongoCollection<Document> linguisticFeaturesCollection = dbHandler.getLinguisticFeaturesCollection();
            Document linguisticFeature = linguisticFeaturesCollection.find(eq("redeId", speechId)).first();




            List<Document> sentiments = new ArrayList<>();
            List<Document> namedEntities = new ArrayList<>();
            List<Document> posAnnotations = new ArrayList<>();
            if (linguisticFeature != null) {
                if (linguisticFeature.containsKey("sentiments")) {
                    sentiments = (List<Document>) linguisticFeature.get("sentiments");
                }
                if (linguisticFeature.containsKey("namedEntities")) {
                    namedEntities = (List<Document>) linguisticFeature.get("namedEntities");
                }
                if (linguisticFeature != null && linguisticFeature.containsKey("posAnnotations")) {
                    posAnnotations = (List<Document>) linguisticFeature.get("posFeatures");
                }
            }

            List<Document> transcriptSegments = new ArrayList<>();
            if (speech.getVideoUrl() != null && !speech.getVideoUrl().isEmpty()) {
                MongoCollection<Document> transcriptCollection = dbHandler.getLinguisticFeaturesTranskriptCollection();
                Document transcriptFeature = transcriptCollection.find(eq("redeId", speechId)).first();

                if (transcriptFeature != null) {
                    System.out.println("Transkript-Dokument gefunden für Rede-ID: " + speechId);
                    // Extrahiere die Segmente aus dem Transkript-Dokument
                    extractTranscriptSegmentsFromDocument(transcriptFeature, transcriptSegments);
                } else {
                    System.out.println("Kein Transkript-Dokument gefunden für Rede-ID: " + speechId);
                }
            }
            System.out.println("Anzahl extrahierter Transkript-Segmente: " + transcriptSegments.size());
            // Model-Daten für das Template
            Map<String, Object> model = new HashMap<>();
            model.put("speech", speech);
            model.put("sentiments", sentiments);
            model.put("namedEntities", namedEntities);
            model.put("posAnnotations", posAnnotations);
            model.put("transcriptSegments", transcriptSegments);

            // FreeMarker-Template rendern
            ctx.render("pages/speechDetails.ftl", model);

        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error: " + e.getMessage());
        }
    }


    /**
     * Gibt alle Reden paginiert zurück
     *
     * @author Philipp Schneider
     * @date 17.03.2025
     */
    private static void getAllSpeechesPaginated(Context ctx) {
        try {
            // Paginierungsparameter
            int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(1);
            int pageSize = ctx.queryParamAsClass("size", Integer.class).getOrDefault(20);

            // Fraktionsfilter
            String faction = ctx.queryParam("faction");
            System.out.println("Selected faction: " + faction);

            // Topic-Filter - könnte mehrere Werte haben
            List<String> selectedTopics = ctx.queryParams("topic");
            System.out.println("Selected topics: " + selectedTopics);

            // Namensfilter
            String speakerSearch = ctx.queryParam("search");

            if (page < 1) page = 1;
            if (pageSize < 1 || pageSize > 100) pageSize = 20;

            // Speech DAO und Service initialisieren
            SpeechDAO speechDAO = new SpeechDAO(dbHandler.getSpeechCollection());
            SpeechService speechService = new SpeechService(speechDAO);

            // Liste der Reden abrufen mit optionalem Fraktionsfilter
            List<Speech_impl> speeches = speechService.getAllSpeechesPaginated(page, pageSize, faction, selectedTopics, speakerSearch);


            // Verbindung zur `linguistic_features` Collection herstellen
            MongoCollection<Document> linguisticFeaturesCollection = dbHandler.getLinguisticFeaturesCollection();

            // Map zur Zuordnung von speechId zu topicCounts Größe
            Map<String, Integer> speechTopicCounts = new HashMap<>();

            for (Speech_impl speech : speeches) {
                String speechId = speech.get_id(); // ID der Rede

                // `linguistic_features`-Eintrag für diese Rede finden
                Document linguisticFeature = linguisticFeaturesCollection.find(eq("redeId", speechId)).first();

                if (linguisticFeature != null && linguisticFeature.containsKey("topicCounts")) {
                    List<Document> topicCounts = (List<Document>) linguisticFeature.get("topicCounts");
                    speechTopicCounts.put(speechId, topicCounts.size()); // Anzahl der Themen speichern
                } else {
                    speechTopicCounts.put(speechId, 0); // Falls keine Themen gefunden wurden
                }
            }

            // Gesamtzahl der Reden für Pagination bestimmen (mit Berücksichtigung des Filters)
            long totalSpeeches = speechService.countSpeeches(faction, selectedTopics);
            int totalPages = (int) Math.ceil((double) totalSpeeches / pageSize);

            // Liste aller verfügbaren Fraktionen abrufen (für Filter-Dropdown)
            List<String> availableFactions = getAllAvailableFactions(dbHandler.getSpeechCollection());

            // Visualisierungsdaten abrufen basierend auf den Filtern
            Document visualizationDoc = getVisualizationData(faction, selectedTopics);

            // Für das Modell aufbereiten (ähnlich wie in showSpeechDetails)
            Map<String, Object> visualizationMap = new HashMap<>();

            // Daten in die Map übertragen, wenn vorhanden
            if (visualizationDoc != null && visualizationDoc.containsKey("aggregated_data")) {
                Document aggregatedData = visualizationDoc.get("aggregated_data", Document.class);

                // Filter-Typ und -Wert extrahieren
                visualizationMap.put("filterType", visualizationDoc.getString("filter_type"));

                if (visualizationDoc.containsKey("filter_value")) {
                    visualizationMap.put("filterValue", visualizationDoc.get("filter_value"));
                }

                // Sentiment-Daten extrahieren
                if (aggregatedData.containsKey("sentimentDistribution")) {
                    Document sentimentDistribution = aggregatedData.get("sentimentDistribution", Document.class);
                    visualizationMap.put("sentimentDistribution", sentimentDistribution);
                    System.out.println("Sentiments gefunden: " + sentimentDistribution.keySet().size());
                }

                // Topic-Daten extrahieren
                if (aggregatedData.containsKey("topicCounts")) {
                    Document topicCounts = aggregatedData.get("topicCounts", Document.class);
                    visualizationMap.put("topicCounts", topicCounts);
                    System.out.println("Topics gefunden: " + topicCounts.keySet().size());
                }

                // POS-Daten extrahieren
                if (aggregatedData.containsKey("posCounts")) {
                    Document posCounts = aggregatedData.get("posCounts", Document.class);
                    visualizationMap.put("posCounts", posCounts);
                    System.out.println("POS-Tags gefunden: " + posCounts.keySet().size());
                }

                // Named Entity-Daten extrahieren
                if (aggregatedData.containsKey("namedEntityCounts")) {
                    Document namedEntityCounts = aggregatedData.get("namedEntityCounts", Document.class);
                    visualizationMap.put("namedEntityCounts", namedEntityCounts);
                    System.out.println("Named Entities gefunden: " + namedEntityCounts.keySet().size());
                }
            }


            // Datenmodell für FreeMarker
            Map<String, Object> model = new HashMap<>();
            model.put("speeches", speeches);
            model.put("speechTopicCounts", speechTopicCounts); // Map mit Themen-Anzahlen hinzufügen
            model.put("currentPage", page);
            model.put("pageSize", pageSize);
            model.put("totalPages", totalPages);
            model.put("totalSpeeches", totalSpeeches);
            model.put("availableFactions", availableFactions); // Für Dropdown
            model.put("selectedFaction", faction); // Aktuell ausgewählte Fraktion
            model.put("selectedTopics", selectedTopics); // Mehrere Topics
            if (visualizationDoc != null) {
                String visualizationJson = visualizationDoc.toJson();
                model.put("visualizationJson", visualizationJson);
            }
            System.out.println("Faction: " + faction);
            System.out.println("totalSpeeches: " + totalSpeeches);
            System.out.println("pageSize: " + pageSize);
            System.out.println("totalPages: " + totalPages);

            if (ctx.header("Accept") != null && ctx.header("Accept").contains("application/json")) {
                ctx.json(model);
            } else {
                ctx.render("pages/speechList.ftl", model);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Fehler beim Laden der Reden: " + e.getMessage());
        }
    }

    /**
     * Hilfsmethode zum Abrufen aller verfügbaren Fraktionen
     *
     * @param speechCollection Die Speech Collection
     * @return Liste der verfügbaren Fraktionen
     */
    private static List<String> getAllAvailableFactions(MongoCollection<Document> speechCollection) {
        // Distinct-Abfrage für alle Fraktionen
        DistinctIterable<String> factions = speechCollection.distinct("speakerObject.party", String.class);
        List<String> factionList = new ArrayList<>();

        // In eine Liste konvertieren
        for (String faction : factions) {
            if (faction != null && !faction.isEmpty()) {
                factionList.add(faction);
            }
        }

        // Alphabetisch sortieren
        Collections.sort(factionList);

        return factionList;
    }


    private static void deleteSpeech(Context ctx) {
        try {
            String speechId = ctx.pathParam("speechId");
            SpeechDAO speechDAO = new SpeechDAO(dbHandler.getSpeechCollection());

            // Führe das Löschen in der Datenbank durch
            boolean success = speechDAO.delete(speechId);

            if (success) {
                ctx.json(Map.of("message", "Rede gelöscht"));
            } else {
                ctx.status(404);
                ctx.json(Map.of("error", "Rede nicht gefunden"));
            }
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(Map.of("error", "Fehler beim Löschen der Rede: " + e.getMessage()));
        }
    }


    private static void addComment(Context ctx) {
        try {
            String speechId = ctx.pathParam("speechId");
            // Lese den Kommentar aus dem Request-Body (als JSON)
            Map<String, Object> commentData = ctx.bodyAsClass(Map.class);

            // Optional: Validierung des Kommentarinhalts
            if (!commentData.containsKey("text") || commentData.get("text") == null) {
                ctx.status(400);
                ctx.json(Map.of("error", "Kommentartext fehlt"));
                return;
            }

            // Füge ein Zeitstempel-Feld hinzu
            commentData.put("timestamp", LocalDateTime.now().toString());

            // Aktualisiere das Speech-Dokument in der Datenbank
            MongoCollection<Document> collection = dbHandler.getSpeechCollection();
            Document updateResult = collection.findOneAndUpdate(
                    eq("_id", speechId),
                    new Document("$push", new Document("comments", new Document(commentData))),
                    new com.mongodb.client.model.FindOneAndUpdateOptions().returnDocument(com.mongodb.client.model.ReturnDocument.AFTER)
            );

            if (updateResult == null) {
                ctx.status(404);
                ctx.json(Map.of("error", "Rede nicht gefunden"));
            } else {
                ctx.json(Map.of("message", "Kommentar hinzugefügt", "updatedSpeech", updateResult));
            }
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(Map.of("error", "Fehler beim Hinzufügen des Kommentars: " + e.getMessage()));
        }
    }

    private static void updateSpeech(Context ctx) {
        try {
            // Lese die Speech-ID aus dem Pfadparameter
            String speechId = ctx.pathParam("speechId");

            // Lese die aktualisierten Daten aus dem Request-Body als Speech_impl
            Speech_impl updatedSpeech = ctx.bodyAsClass(Speech_impl.class);

            // Initialisiere den SpeechDAO
            SpeechDAO speechDAO = new SpeechDAO(dbHandler.getSpeechCollection());

            // Führe das Update in der Datenbank durch
            boolean success = speechDAO.update(speechId, updatedSpeech);

            if (success) {
                ctx.json(Map.of("message", "Rede erfolgreich aktualisiert"));
            } else {
                ctx.status(404);
                ctx.json(Map.of("error", "Rede mit der angegebenen ID nicht gefunden"));
            }
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(Map.of("error", "Fehler beim Aktualisieren der Rede: " + e.getMessage()));
        }
    }


    /**
     * Hilfsmethode zum Abrufen der aggregierten Visualisierungsdaten basierend auf den Filtern
     *
     * @param faction Fraktionsfilter
     * @param selectedTopics Liste ausgewählter Topics
     * @return Document mit aggregierten Visualisierungsdaten oder Default-Daten wenn nichts gefunden wurde
     */
    private static Document getVisualizationData(String faction, List<String> selectedTopics) {
        try {
            System.out.println("\n----- DEBUG: getVisualizationData -----");
            System.out.println("Parameter faction: " + faction);
            System.out.println("Parameter selectedTopics: " + selectedTopics);

            MongoCollection<Document> collection = dbHandler.getCollection("visualization_aggregates");
            System.out.println("Collection: " + collection.getNamespace());

            Document filter = new Document();
            Document result = null;

            // Versuch 1: Wenn Fraktion gegeben ist, suche nach Fraktion
            if (faction != null && !faction.isEmpty()) {
                System.out.println("Suche nach Fraktion: " + faction);

                Document factionFilter = new Document("filter_type", "faction")
                        .append("filter_value.faction", faction);

                System.out.println("Fraktion-Filter: " + factionFilter.toJson());

                result = collection.find(factionFilter).first();
                if (result != null) {
                    System.out.println("Fraktionsdaten gefunden!");
                    System.out.println("Filter-Typ: " + result.getString("filter_type"));
                    System.out.println("Filter-Wert: " + result.get("filter_value"));
                    System.out.println("Hat aggregated_data: " + result.containsKey("aggregated_data"));
                    return result;
                } else {
                    System.out.println("Keine Daten für diese Fraktion gefunden.");
                }

                // Als Fallback nach generischen Fraktionsdaten suchen
                System.out.println("Suche nach  Fraktionsdaten");
                Document genericFactionFilter = new Document("filter_type", "faction_generic");

                Document genericResult = collection.find(genericFactionFilter).first();
                if (genericResult != null) {
                    System.out.println(" Fraktionsdaten gefunden!");
                    return genericResult;
                } else {
                    System.out.println("Keine  Fraktionsdaten gefunden.");
                }
            }

            // Versuch 2: Wenn Topics gegeben sind, suche basierend auf Topics
            if (selectedTopics != null && !selectedTopics.isEmpty()) {
                System.out.println("Suche nach Topics: " + selectedTopics);

                // Bei einem einzelnen Topic ist die Suche einfach
                if (selectedTopics.size() == 1) {
                    String topic = selectedTopics.get(0);
                    System.out.println("Suche nach einzelnem Topic: " + topic);

                    Document topicFilter = new Document("filter_type", "topic")
                            .append("filter_value.topic", topic);

                    System.out.println("Topic-Filter: " + topicFilter.toJson());

                    result = collection.find(topicFilter).first();
                    if (result != null) {
                        System.out.println("Topic-Daten gefunden!");
                        return result;
                    } else {
                        System.out.println("Keine Daten für dieses Topic gefunden.");
                    }
                } else {
                    System.out.println("Suche nach Topic-Kombination: " + selectedTopics);

                    // Bei mehreren Topics versuchen wir eine Kombination zu finden
                    List<Document> topicCombinations = collection
                            .find(new Document("filter_type", "topic_combination"))
                            .into(new ArrayList<>());

                    System.out.println("Gefundene Topic-Kombinationen: " + topicCombinations.size());

                    for (Document combo : topicCombinations) {
                        System.out.println("Prüfe Kombination: " + combo.toJson());

                        if (combo.containsKey("filter_value") && combo.get("filter_value") instanceof List) {
                            List<Document> filterTopics = (List<Document>) combo.get("filter_value");

                            // Extrahiere Topic-Namen aus dem filterTopics
                            Set<String> comboTopics = new HashSet<>();
                            for (Document topicDoc : filterTopics) {
                                for (String key : topicDoc.keySet()) {
                                    if (topicDoc.get(key) instanceof String) {
                                        comboTopics.add(topicDoc.getString(key));
                                    }
                                }
                            }

                            System.out.println("Extrahierte Topics aus Kombination: " + comboTopics);
                            System.out.println("Vergleiche mit ausgewählten Topics: " + selectedTopics);

                            // Vergleiche die Sets
                            if (comboTopics.size() == selectedTopics.size() &&
                                    comboTopics.containsAll(selectedTopics)) {
                                System.out.println("Übereinstimmende Kombination gefunden!");
                                return combo;
                            }
                        }
                    }

                    System.out.println("Keine passende Topic-Kombination gefunden, versuche mit dem ersten Topic");

                    // Wenn keine exakte Kombination gefunden wurde, suche nach dem ersten Topic
                    Document firstTopicFilter = new Document("filter_type", "topic")
                            .append("filter_value.topic", selectedTopics.get(0));

                    result = collection.find(firstTopicFilter).first();
                    if (result != null) {
                        System.out.println("Daten für das erste Topic gefunden!");
                        return result;
                    } else {
                        System.out.println("Keine Daten für das erste Topic gefunden.");
                    }
                }
            }

            // Versuch 3: Fallback auf "all" Daten
            System.out.println("Suche nach 'all' Daten als Fallback");

            result = collection.find(new Document("filter_type", "all")).first();
            if (result != null) {
                System.out.println("'all' Daten gefunden!");

                // Ausgabe der aggregated_data Struktur
                Document aggregatedData = (Document) result.get("aggregated_data");

                if (aggregatedData != null) {
                    System.out.println("\nStruktur von aggregated_data:");
                    for (String key : aggregatedData.keySet()) {
                        Object fieldValue = aggregatedData.get(key);
                        System.out.println("  - " + key + " (Typ: " + fieldValue.getClass().getSimpleName() + ")");

                        // Für jedes Feld detaillierte Informationen ausgeben
                        if (fieldValue instanceof Document) {
                            Document fieldDoc = (Document) fieldValue;
                            System.out.println("    " + key + " Details:");

                            // Maximal 10 Einträge anzeigen, um die Ausgabe übersichtlich zu halten
                            int count = 0;
                            for (String fieldKey : fieldDoc.keySet()) {
                                Object value = fieldDoc.get(fieldKey);
                                System.out.println("      " + fieldKey + " = " + value + " (Typ: " + value.getClass().getSimpleName() + ")");
                                count++;

                                if (count >= 10) {
                                    System.out.println("      ... und " + (fieldDoc.keySet().size() - 10) + " weitere Einträge");
                                    break;
                                }
                            }
                        } else if (fieldValue instanceof List) {
                            List<?> fieldList = (List<?>) fieldValue;
                            System.out.println("    " + key + " Details (Liste mit " + fieldList.size() + " Einträgen):");

                            // Maximal 10 Einträge anzeigen
                            for (int i = 0; i < Math.min(10, fieldList.size()); i++) {
                                Object item = fieldList.get(i);
                                System.out.println("      [" + i + "] = " + item + " (Typ: " + item.getClass().getSimpleName() + ")");
                            }

                            if (fieldList.size() > 10) {
                                System.out.println("      ... und " + (fieldList.size() - 10) + " weitere Einträge");
                            }
                        }
                    }
                    System.out.println();
                }

                return result;
            } else {
                System.out.println("Keine 'all' Daten gefunden!");
            }

            // Wenn keine Daten gefunden wurden, erstelle ein leeres Dokument mit minimaler Struktur
            System.out.println("Erstelle leere Visualisierungsdaten als letzten Ausweg");
            return createEmptyVisualizationData();

        } catch (Exception e) {
            System.err.println("Fehler beim Abrufen der Visualisierungsdaten: " + e.getMessage());
            e.printStackTrace();
            return createEmptyVisualizationData();
        } finally {
            System.out.println("----- DEBUG ENDE -----\n");
        }
    }

    /**
     * Erstellt ein leeres Visualisierungsdaten-Dokument mit minimaler Struktur
     *
     * @return Leeres Document für Visualisierungsdaten
     */
    private static Document createEmptyVisualizationData() {
        Document emptyData = new Document("filter_type", "empty")
                .append("aggregated_data", new Document()
                        .append("sentimentDistribution", new Document()
                                .append("positive", 0.0)
                                .append("neutral", 1.0)
                                .append("negative", 0.0))
                        .append("topicCounts", new Document())
                        .append("posCounts", new Document())
                        .append("namedEntityCounts", new ArrayList<>()));

        return emptyData;
    }



    private static void extractTranscriptSegmentsFromDocument(Document transcriptFeature, List<Document> transcriptSegments) {
        System.out.println("Extrahiere Transkript-Segmente aus dem sentiments-Feld...");

        // Verwende direkt die sentiments-Liste für das Transkript
        if (transcriptFeature.containsKey("sentiments")) {
            System.out.println("Verwende sentiments für das Transkript");
            List<Document> sentiments = (List<Document>) transcriptFeature.get("sentiments");

            // Sammle alle audioTokens zum Extrahieren der Zeitinformationen
            Map<String, List<Document>> wordToTokens = new HashMap<>();

            // Wenn audioTokens vorhanden sind, versuche, Zeitinformationen zu extrahieren
            if (transcriptFeature.containsKey("audioTokens")) {
                List<Document> audioTokens = (List<Document>) transcriptFeature.get("audioTokens");

                for (Document token : audioTokens) {
                    if (token.containsKey("coveredText") && token.containsKey("timeStart") && token.containsKey("timeEnd")) {
                        String text = token.getString("coveredText").toLowerCase();

                        if (!wordToTokens.containsKey(text)) {
                            wordToTokens.put(text, new ArrayList<>());
                        }
                        wordToTokens.get(text).add(token);
                    }
                }

                System.out.println("Anzahl der gefundenen Wörter mit Zeitinformationen: " + wordToTokens.size());
            }

            // Verarbeite jeden Satz (Sentiment)
            double defaultStartTime = 0.0;
            for (Document sentiment : sentiments) {
                if (sentiment.containsKey("coveredText") &&
                        sentiment.getString("coveredText") != null &&
                        !sentiment.getString("coveredText").isEmpty()) {

                    String sentenceText = sentiment.getString("coveredText");

                    // Erstelle ein neues Segment
                    Document segment = new Document();
                    segment.append("text", sentenceText);

                    // Füge die Sentiment-Werte direkt zum Segment hinzu
                    if (sentiment.containsKey("sentiment")) {
                        segment.append("sentiment", sentiment.getDouble("sentiment"));
                    }
                    if (sentiment.containsKey("posScore")) {
                        segment.append("posScore", sentiment.getDouble("posScore"));
                    }
                    if (sentiment.containsKey("neuScore")) {
                        segment.append("neuScore", sentiment.getDouble("neuScore"));
                    }
                    if (sentiment.containsKey("negScore")) {
                        segment.append("negScore", sentiment.getDouble("negScore"));
                    }

                    // Ermittle Start- und Endzeit für diesen Satz
                    double startTime = -1;
                    double endTime = -1;

                    // 1. Wenn begin/end Felder im Sentiment vorhanden sind, versuche diese mit audioTokens zu matchen
                    if (sentiment.containsKey("begin") && sentiment.containsKey("end")) {
                        int begin = sentiment.getInteger("begin");
                        int end = sentiment.getInteger("end");

                        // Finde alle Tokens, die innerhalb dieses Bereichs liegen
                        List<Document> tokensInRange = new ArrayList<>();
                        if (transcriptFeature.containsKey("tokens")) {
                            List<Document> allTokens = (List<Document>) transcriptFeature.get("tokens");

                            for (Document token : allTokens) {
                                if (token.containsKey("begin") && token.containsKey("end")) {
                                    int tokenBegin = token.getInteger("begin");
                                    int tokenEnd = token.getInteger("end");

                                    // Prüfe Überlappung
                                    if (tokenBegin >= begin && tokenEnd <= end) {
                                        String tokenText = token.getString("coveredText").toLowerCase();

                                        // Suche nach Zeitinformationen für dieses Token
                                        if (wordToTokens.containsKey(tokenText)) {
                                            tokensInRange.addAll(wordToTokens.get(tokenText));
                                        }
                                    }
                                }
                            }
                        }

                        // Ermittle Start- und Endzeit aus den gefundenen Tokens
                        if (!tokensInRange.isEmpty()) {
                            double minStart = Double.MAX_VALUE;
                            double maxEnd = 0;

                            for (Document token : tokensInRange) {
                                double tokenStart = token.getDouble("timeStart");
                                double tokenEnd = token.getDouble("timeEnd");

                                if (tokenStart < minStart) minStart = tokenStart;
                                if (tokenEnd > maxEnd) maxEnd = tokenEnd;
                            }

                            if (minStart != Double.MAX_VALUE && maxEnd > 0) {
                                startTime = minStart;
                                endTime = maxEnd;
                            }
                        }
                    }

                    // 2. Wenn keine Zeit gefunden wurde, versuche, einzelne Wörter zu matchen
                    if (startTime < 0 || endTime < 0) {
                        // Teile den Satz in Wörter und suche nach Zeitinformationen
                        String[] words = sentenceText.toLowerCase().split("\\s+");
                        List<Double> foundStarts = new ArrayList<>();
                        List<Double> foundEnds = new ArrayList<>();

                        for (String word : words) {
                            // Entferne Satzzeichen
                            String cleanWord = word.replaceAll("[.,;!?]", "");

                            if (wordToTokens.containsKey(cleanWord)) {
                                for (Document token : wordToTokens.get(cleanWord)) {
                                    foundStarts.add(token.getDouble("timeStart"));
                                    foundEnds.add(token.getDouble("timeEnd"));
                                }
                            }
                        }

                        // Wenn genügend Zeiten gefunden wurden, verwende die früheste Start- und späteste Endzeit
                        if (!foundStarts.isEmpty() && !foundEnds.isEmpty()) {
                            startTime = Collections.min(foundStarts);
                            endTime = Collections.max(foundEnds);
                        }
                    }

                    // 3. Fallback: Wenn immer noch keine Zeit gefunden wurde, erstelle künstliche Zeiten
                    if (startTime < 0 || endTime < 0) {
                        // Berechne Zeit basierend auf der Satzlänge (ungefähr 0.1 Sekunden pro Zeichen)
                        double duration = Math.max(5.0, sentenceText.length() * 0.1);
                        startTime = defaultStartTime;
                        endTime = startTime + duration;

                        // Aktualisiere die Standardstartzeit für den nächsten Satz
                        defaultStartTime = endTime + 0.5; // Kleine Pause zwischen den Sätzen
                    }

                    // Füge die ermittelten Zeiten zum Segment hinzu
                    segment.append("start", startTime);
                    segment.append("end", endTime);

                    // Debug-Ausgabe
                    System.out.printf("Segment: \"%s...\" - Zeit: %.2f bis %.2f%n",
                            sentenceText.substring(0, Math.min(20, sentenceText.length())),
                            startTime, endTime);

                    // Füge das Segment zur Liste hinzu
                    transcriptSegments.add(segment);
                }
            }

            System.out.println("Insgesamt " + transcriptSegments.size() + " Transkript-Segmente erstellt");
        } else {
            System.out.println("Keine sentiments-Daten gefunden");
        }
    }
}
