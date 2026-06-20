package backend.plenarprotocol;

import backend.speech.interfaces.IProtocol;
import backend.speech.interfaces.ISpeech;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.List;

/**
 * Interface Plenarprotokoll.
 *
 * @author Philipp Hein
 * @date 12.03.2025
 */
public interface IPlenarprotocol {

    /**
     * @return die Protokoll ID als String
     * @author Philipp Hein
     * @date 12.03.2025
     */
    String getId();

    /**
     * @param id Protokoll ID als String
     * @author Philipp Hein
     * @date 12.03.2025
     */
    void setId(String id);

    /**
     * @return ein PlenarprotocolData Objekt
     * @author Philipp Hein
     * @date 12.03.2025
     */
    IProtocol getProtocol();

    /**
     * @param protocol ein PlenarprotocolData Objekt
     * @author Philipp Hein
     * @date 12.03.2025
     * Setzt die Protokolldaten.
     */
    void setProtocol(IProtocol protocol);

    /**
     * @return eine Liste von Objekten, die die Reden repräsentieren
     * @author Philipp Hein
     * @date 12.03.2025
     * Gibt die Liste der zugehörigen Reden zurück.
     */
    List<ISpeech> getSpeeches();

    /**
     * @param speeches eine Liste von Objekten, die die Reden repräsentieren
     * @author Philipp Hein
     * @date 12.03.2025
     * Setzt die Liste der zugehörigen Reden
     */
    void setSpeeches(List<ISpeech> speeches);

    /**
     * @param disableTikz boolean, ob Tikz deaktiviert werden soll
     * @return das Protokoll als Tex-Code (mit allen Reden)
     * @author Philipp Landmann
     */
    String toTex(boolean disableTikz) throws TemplateException, IOException;

}

