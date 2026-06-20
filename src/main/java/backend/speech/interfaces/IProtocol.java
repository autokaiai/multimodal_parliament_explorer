package backend.speech.interfaces;

/**
 * Interface für Protocol.
 * Stellt Getter und Setter für die Attribute bereit.
 */
public interface IProtocol
{

    /**
     * @return Datum Protocol.
     */
    String getDate();

    /**
     * Setzt Datum Protocol.
     *
     * @param date neu.
     */
    void setDate(Long date);

    /**
     * @return Startzeit Protocol.
     */
    String getStarttime();

    /**
     * Setzt Startzeit Protocol.
     *
     * @param starttime neu.
     */
    void setStarttime(Long starttime);

    /**
     * @return Endzeit Protocol.
     */
    String getEndtime();

    /**
     * Setzt Endzeit Protocol.
     *
     * @param endtime neu.
     */
    void setEndtime(Long endtime);

    /**
     * @return Index Protocol.
     */
    Integer getIndex();

    /**
     * Setzt Index Protocol.
     *
     * @param index neu.
     */
    void setIndex(Integer index);

    /**
     * @return Titel Protocol.
     */
    String getTitle();

    /**
     * Setzt Titel Protocol.
     *
     * @param title neu.
     */
    void setTitle(String title);

    /**
     * @return Ort Protocol.
     */
    String getPlace();

    /**
     * Setzt Ort Protocol.
     *
     * @param place neu.
     */
    void setPlace(String place);

    /**
     * @return WP Protocol.
     */
    Integer getWp();

    /**
     * Setzt WP Protocol.
     *
     * @param wp neu.
     */
    void setWp(Integer wp);
}
