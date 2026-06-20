package backend.speech;

import backend.speech.interfaces.IAgenda;

/**
 * @author: Philipp Hein #6356965
 * Implementierung {@link IAgenda} Interfaces.
 */
public class Agenda_impl implements IAgenda {

    private String index;
    private String id;
    private String title;

    /**
     * @author: Philipp Hein #6356965
     * Konstruktor.
     *
     * @param index Index Agenda.
     * @param id    ID Agenda.
     * @param title Titel Agenda.
     */
    public Agenda_impl(String index, String id, String title) {
        this.index = index;
        this.id = id;
        this.title = title;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return Index Agenda.
     */
    @Override
    public String getIndex() {
        return index;
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt Index Agenda.
     *
     * @param index neu.
     */
    @Override
    public void setIndex(String index) {
        this.index = index;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return ID Agenda.
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt ID Agenda.
     *
     * @param id neu.
     */
    @Override
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return Titel Agenda.
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt Titel Agenda.
     *
     * @param title neu.
     */
    @Override
    public void setTitle(String title) {
        this.title = title;
    }
}
