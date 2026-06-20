package backend.speaker.interfaces;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

/**
 * @author: Philipp Hein #6356965
 * Schnittstelle für Speaker.
 * Stellt Getter und Setter für Speaker Attribute bereit.
 * Memberships können einzeln, als Liste oder durch Zurücksetzen des gesamten Sets hinzugefügt werden.
 */
public interface Speaker {

    /**
     * @return ID
     * @author: Philipp Hein #6356965
     */
    String get_id();

    /**
     * @author: Philipp Hein #6356965
     * Setzt ID des Speakers.
     *
     * @param _id neu
     */
    void set_id(String _id);

    /**
     * @author: Philipp Hein #6356965
     * @return Name
     */
    String getName();

    /**
     * @author: Philipp Hein #6356965
     * Setzt Namen des Speakers.
     *
     * @param name neu
     */
    void setName(String name);

    /**
     * @author: Philipp Hein #6356965
     * @return Vorname
     */
    String getFirstName();

    /**
     * @author: Philipp Hein #6356965
     * Setzt Vornamen des Speakers.
     *
     * @param firstName neu
     */
    void setFirstName(String firstName);

    /**
     * @author: Philipp Hein #6356965
     * @return Titel des Speakers.
     */
    String getTitle();

    /**
     * @author: Philipp Hein #6356965
     * Setzt Titel des Speakers.
     *
     * @param title neu
     */
    void setTitle(String title);

    /**
     * @author: Philipp Hein #6356965
     * @return Geburtsdatum des Speakers.
     */
    String getGeburtsdatum();

    /**
     * @author: Philipp Hein #6356965
     * Setzt Geburtsdatum des Speakers.
     *
     * @param geburtsdatum neu
     */
    void setGeburtsdatum(LocalDate geburtsdatum);

    /**
     * @author: Philipp Hein #6356965
     * @return Geburtsort des Speakers.
     */
    String getGeburtsort();

    /**
     * @author: Philipp Hein #6356965
     * Setzt Geburtsort des Speakers.
     *
     * @param geburtsort neu
     */
    void setGeburtsort(String geburtsort);

    /**
     * @author: Philipp Hein #6356965
     * @return Sterbedatum
     */
    String getSterbedatum();

    /**
     * @author: Philipp Hein #6356965
     * Setzt Sterbedatum des Speakers.
     *
     * @param sterbedatum neu
     */
    void setSterbedatum(LocalDate sterbedatum);

    /**
     * @author: Philipp Hein #6356965
     * @return Geschlecht
     */
    String getGeschlecht();

    /**
     * @author: Philipp Hein #6356965
     * Setzt Geschlecht des Speakers.
     *
     * @param geschlecht neu
     */
    void setGeschlecht(String geschlecht);

    /**
     * @author: Philipp Hein #6356965
     * @return Beruf
     */
    String getBeruf();

    /**
     * @author: Philipp Hein #6356965
     * Setzt Beruf des Speakers.
     *
     * @param beruf neu
     */
    void setBeruf(String beruf);

    /**
     * @author: Philipp Hein #6356965
     * @return akademischer Titel
     */
    String getAkademischertitel();

    /**
     * @author: Philipp Hein #6356965
     * Setzt den akademischen Titel des Speakers.
     *
     * @param akademischertitel neu
     */
    void setAkademischertitel(String akademischertitel);

    /**
     * @author: Philipp Hein #6356965
     * @return Familienstand des Speakers.
     */
    String getFamilienstand();

    /**
     * @author: Philipp Hein #6356965
     * Setzt Familienstand des Speakers.
     *
     * @param familienstand neu
     */
    void setFamilienstand(String familienstand);

    /**
     * @author: Philipp Hein #6356965
     * @return Religion des Speakers.
     */
    String getReligion();

    /**
     * @author: Philipp Hein #6356965
     * Setzt Religion des Speakers.
     *
     * @param religion neu
     */
    void setReligion(String religion);

    /**
     * @author: Philipp Hein #6356965
     * @return Vita des Speakers.
     */
    String getVita();

    /**
     * @author: Philipp Hein #6356965
     * Setzt Vita des Speakers.
     *
     * @param vita neu
     */
    void setVita(String vita);

    /**
     * @author: Philipp Hein #6356965
     * @return Partei des Speakers.
     */
    String getParty();

    /**
     * @author: Philipp Hein #6356965
     * Setzt Partei des Speakers.
     *
     * @param party neu
     */
    void setParty(String party);

    /**
     * @author: Philipp Hein #6356965
     * @return Mitgliedschaften des Speakers.
     */
    Set<Object> getMemberships();

    /**
     * @author: Philipp Hein #6356965
     * Fügt Mitgliedschaft hinzu.
     *
     * @param membership neu
     */
    void setMemberships(Object membership);

    /**
     * @author: Philipp Hein #6356965
     * Setzt Mitgliedschaften neu.
     *
     * @param memberships neu
     */
    void setMemberships(Set<Object> memberships);

    /**
     * @author: Philipp Hein #6356965
     * @param disableTikz true, wenn Tikz deaktiviert werden soll.
     * @return LaTeX-String aller Reden des Speakers.
     */
    String toTex(boolean disableTikz) throws IOException, TemplateException;

    /**
     * @author: Philipp Hein #6356965
     * @return URL des Portrait-Bildes
     */
    String getImageUrl();

    /**
     * @author: Philipp Hein #6356965
     * @param imageUrl URL des Portrait-Bildes
     */
    void setImageUrl(String imageUrl);

    /**
     * @author: Philipp Hein #6356965
     * Gibt Base64 kodiertes Portrait zurück
     *
     * @return Base64 kodiertes Bild
     */
    String getImageData();

    /**
     * @author: Philipp Hein #6356965
     * Setzt Base64 kodiertes Bild
     *
     * @param imageData Base64 kodiertes Bild
     */
    void setImageData(String imageData);

}
