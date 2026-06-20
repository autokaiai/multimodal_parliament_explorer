package backend.speech.interfaces;

import backend.speaker.interfaces.Speaker;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Interface für Speech
 */
public interface ISpeech {

    /**
     * @return ID Speech
     */
    String get_id();

    /**
     * Setzt ID Speech
     *
     * @param _id neu
     */
    void set_id(String _id);

    /**
     * @return Text Speech
     */
    String getText();

    /**
     * Setzt Text Speech
     *
     * @param text neu
     */
    void setText(String text);

    /**
     * @return Speaker ID
     */
    String getSpeaker();

    /**
     * Setzt Speaker ID
     *
     * @param speaker neu
     */
    void setSpeaker(String speaker);

    /**
     * @return Protocol Speech
     */
    IProtocol getProtocol();

    /**
     * Setzt Protocol Speech
     *
     * @param protocol neu
     */
    void setProtocol(IProtocol protocol);

    /**
     * @return Textinhalt Speech
     */
    ArrayList<Object> getTextContent();

    /**
     * Setzt Textinhalt Speech
     *
     * @param textContent neu
     */
    void setTextContent(ArrayList<Object> textContent);

    /**
     * @return Agenda Speech
     */
    IAgenda getAgenda();

    /**
     * Setzt Agenda Speech
     *
     * @param agenda neu
     */
    void setAgenda(IAgenda agenda);

    /**
     * @return Speaker Objekt
     */
    Speaker getSpeakerObject();

    /**
     * Setzt Speaker Objekt
     *
     * @param speaker neu
     */
    void setSpeakerObject(Speaker speaker);

    String toTex(boolean disableTikz) throws IOException, TemplateException;
}
