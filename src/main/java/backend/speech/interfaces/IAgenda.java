package backend.speech.interfaces;

/**
 * Interface für Agenda.
 * Stellt Getter- und Setter für Attribute bereit.
 */
public interface IAgenda {

    /**
     * @return Index
     */
    String getIndex();

    /**
     * Setzt Index
     *
     * @param index neu.
     */
    void setIndex(String index);

    /**
     * @return ID Agenda.
     */
    String getId();

    /**
     * Setzt ID Agenda.
     *
     * @param id neu.
     */
    void setId(String id);

    /**
     * @return Titel Agenda.
     */
    String getTitle();

    /**
     * Setzt Titel Agenda.
     *
     * @param title neu.
     */
    void setTitle(String title);
}
