package backend.utility;

import backend.plenarprotocol.Plenarprotocol_impl;
import backend.speaker.Speaker_impl;
import backend.speaker.interfaces.Speaker;
import backend.speech.Agenda_impl;
import backend.speech.Protocol_impl;
import backend.speech.Speech_impl;
import org.bson.Document;
import org.bson.types.Binary;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementierung des Factory Interfaces
 */
public class Factory_impl implements Factory {

    /**
     * @return Speaker Objekt
     * @author Philipp Hein
     */
    public static Speaker_impl createSpeaker(Document document) {
        if (document == null) {
            return null;
        }

        Speaker_impl speaker = new Speaker_impl();

        speaker.set_id(document.getString("_id"));
        speaker.setName(document.getString("name"));
        speaker.setFirstName(document.getString("firstName"));
        speaker.setTitle(document.getString("title"));

        // Geburtsdatum
        if (document.containsKey("geburtsdatum")) {
            Object geburtsdatum = document.get("geburtsdatum");
            if (geburtsdatum instanceof String) {
                speaker.setGeburtsdatum(LocalDate.parse((String) geburtsdatum));
            } else if (geburtsdatum instanceof java.util.Date) {
                speaker.setGeburtsdatum(((java.util.Date) geburtsdatum).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate());
            }
        }
        speaker.setGeburtsort(document.getString("geburtsort"));

        // Sterbedatum
        if (document.containsKey("sterbedatum")) {
            Object sterbedatum = document.get("sterbedatum");
            if (sterbedatum instanceof String) {
                speaker.setSterbedatum(LocalDate.parse((String) sterbedatum));
            } else if (sterbedatum instanceof java.util.Date) {
                speaker.setSterbedatum(((java.util.Date) sterbedatum).toInstant()
                        .atZone(ZoneId.systemDefault()).toLocalDate());
            }
        }

        speaker.setGeschlecht(document.getString("geschlecht"));
        speaker.setBeruf(document.getString("beruf"));
        speaker.setAkademischertitel(document.getString("akademischertitel"));
        speaker.setFamilienstand(document.getString("familienstand"));
        speaker.setReligion(document.getString("religion"));
        speaker.setVita(document.getString("vita"));
        speaker.setParty(document.getString("party"));
        speaker.setImageUrl(document.getString("imageUrl"));

        // Manchmal ist imageData ein bson-Binary, manchmal ein String
        if (document.get("imageData") instanceof Binary) {
            speaker.setImageData(Base64.getEncoder().encodeToString(document.get("imageData", Binary.class).getData()));
        } else if (document.get("imageData") instanceof String) {
            speaker.setImageData(document.getString("imageData").split("'")[1]);
        }

        // Mitgliedschaften
        if (document.containsKey("memberships")) {
            List<Object> membershipsList = document.getList("memberships", Object.class);
            speaker.setMemberships(new HashSet<>(membershipsList));
        } else {
            speaker.setMemberships(new HashSet<>());
        }

        return speaker;
    }

    /**
     * @return Agenda Objekt
     * @author Philipp Hein
     */
    public static Agenda_impl createAgenda(Document document) {
        if (document == null) return null;

        return new Agenda_impl(
                document.getString("index"),
                document.getString("id"),
                document.getString("title")
        );
    }

    /**
     * @return Speech Objekt
     * @author Philipp Hein
     */
    public static Speech_impl createSpeech(Document document) {
        if (document == null) return null;

        String _id = document.getString("_id");
        String text = document.getString("text");
        String speaker = document.getString("speaker");

        Protocol_impl protocol = null;
        if (document.containsKey("protocol")) {
            Document protocolDoc = document.get("protocol", Document.class);
            if (protocolDoc != null) {
                protocol = createProtocol(protocolDoc);
            }
        }

        Agenda_impl agenda = null;
        if (document.containsKey("agenda")) {
            Document agendaDoc = document.get("agenda", Document.class);
            if (agendaDoc != null) {
                agenda = createAgenda(agendaDoc);
            }
        }

        // Video URL aus dem MongoDB-Dokument holen (falls vorhanden)
        String videoUrl = document.containsKey("videoUrl") ? document.getString("videoUrl") : null;


        List<Document> textContentDocs = (List<Document>) document.get("textContent");
        ArrayList<Object> textContent = new ArrayList<>();

        if (textContentDocs != null) {
            for (Document contentDoc : textContentDocs) {
                Map<String, Object> contentMap = new HashMap<>();
                contentMap.put("id", contentDoc.getString("id"));
                contentMap.put("text", contentDoc.getString("text"));
                contentMap.put("type", contentDoc.getString("type"));
                contentMap.put("history", contentDoc.get("history"));
                textContent.add(contentMap);
            }
        }

        Speaker speakerObject = createSpeaker(document.get("speakerInfo", Document.class));

        return new Speech_impl(_id, text, speaker, protocol, textContent, agenda, speakerObject, videoUrl);
    }

    public static Protocol_impl createProtocol(Document document) {
        if (document == null) return null;
        return new Protocol_impl(
                document.getLong("date"),
                document.getLong("starttime"),
                document.getLong("endtime"),
                document.getInteger("index"),
                document.getString("title"),
                document.getString("place"),
                document.getInteger("wp")
        );
    }

    public static Plenarprotocol_impl createPlenarprotocol(Document doc) {
        if (doc == null) {
            return null;
        }
        return new Plenarprotocol_impl(
                doc.getString("_id"),
                createProtocol(doc.get("protocol", Document.class)),
                doc.getList("speeches", Document.class).stream().map(Factory_impl::createSpeech).collect(Collectors.toList())
        );
    }
}
