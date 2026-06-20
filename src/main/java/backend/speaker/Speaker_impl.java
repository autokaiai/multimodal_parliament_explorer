package backend.speaker;

import backend.database.MongoDBHandler;
import backend.export.LatexCompiler;
import backend.linguisticFeatures.LinguisticService;
import backend.speaker.interfaces.Speaker;
import backend.speech.SpeechDAO;
import backend.speech.Speech_impl;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Philipp Noah Hein #6356965
 * Implementierung des {@link Speaker} Interfaces.
 */
public class Speaker_impl implements Speaker {

    private String _id;
    private String name;
    private String firstName;
    private String title;
    private LocalDate geburtsdatum;
    private String geburtsort;
    private LocalDate sterbedatum;
    private String geschlecht;
    private String beruf;
    private String akademischertitel;
    private String familienstand;
    private String religion;
    private String vita;
    private String party;
    private Set<Object> memberships;
    private String imageUrl;
    private String imageData;

    /**
     * @param _id       ID.
     * @param name      Name.
     * @param firstName Vorname.
     * @author Philipp Noah Hein #6356965
     * Konstruktor mit den nötigsten Elementen.
     */
    public void speaker(String _id, String name, String firstName) {
        this._id = _id;
        this.name = name;
        this.firstName = firstName;
    }

    /**
     * @param _id               ID.
     * @param name              Name.
     * @param firstName         Vorname.
     * @param title             Titel.
     * @param geburtsdatum      Geburtsdatum als String.
     * @param geburtsort        Geburtsort.
     * @param sterbedatum       Sterbedatum als String.
     * @param geschlecht        Geschlecht.
     * @param beruf             Beruf.
     * @param akademischertitel Akademischer Titel.
     * @param familienstand     Familienstand.
     * @param religion          Religion.
     * @param vita              Vita.
     * @param party             Partei.
     * @param memberships       Mitgliedschaften.
     * @param imageUrl          Bild-URL von Portrait-Foto.
     * @param imageData         Portrait-Bild als Base64-String.
     * @author Philipp Noah Hein #6356965
     * Konstruktor mit allen Attributen, Geburts- und Sterbedatum als String.
     */
    public void speaker(String _id, String name, String firstName, String title,
                        String geburtsdatum, String geburtsort, String sterbedatum, String geschlecht,
                        String beruf, String akademischertitel, String familienstand, String religion, String vita,
                        String party, Set<Object> memberships, String imageUrl, String imageData) {
        this._id = _id;
        this.name = name;
        this.firstName = firstName;
        this.title = title;
        this.geburtsdatum = convertStringToDate(geburtsdatum);
        this.geburtsort = geburtsort;
        this.sterbedatum = convertStringToDate(sterbedatum);
        this.geschlecht = geschlecht;
        this.beruf = beruf;
        this.akademischertitel = akademischertitel;
        this.familienstand = familienstand;
        this.religion = religion;
        this.vita = vita;
        this.party = party;
        this.memberships = memberships;
        this.imageUrl = imageUrl;
        this.imageData = imageData;

    }

    /**
     * @param _id               ID.
     * @param name              Name.
     * @param firstName         Vorname.
     * @param title             Titel.
     * @param geburtsdatum      Geburtsdatum.
     * @param geburtsort        Geburtsort.
     * @param sterbedatum       Sterbedatum.
     * @param geschlecht        Geschlecht.
     * @param beruf             Beruf.
     * @param akademischertitel Akademischer Titel.
     * @param familienstand     Familienstand.
     * @param religion          Religion.
     * @param vita              Vita.
     * @param party             Partei.
     * @param memberships       Mitgliedschaften.
     * @param imageUrl          Bild-URL von Portrait-Foto.
     * @param imageData         Portrait-Bild als Base64-String.
     * @author Philipp Noah Hein #6356965
     * Konstruktor mit allen Attributen, Geburts- und Sterbedatum als {@link LocalDate}.
     */
    public void speakers(String _id, String name, String firstName, String title,
                         LocalDate geburtsdatum, String geburtsort, LocalDate sterbedatum, String geschlecht,
                         String beruf, String akademischertitel, String familienstand, String religion, String vita,
                         String party, Set<Object> memberships, String imageUrl, String imageData) {
        this._id = _id;
        this.name = name;
        this.firstName = firstName;
        this.title = title;
        this.geburtsdatum = geburtsdatum;
        this.geburtsort = geburtsort;
        this.sterbedatum = sterbedatum;
        this.geschlecht = geschlecht;
        this.beruf = beruf;
        this.akademischertitel = akademischertitel;
        this.familienstand = familienstand;
        this.religion = religion;
        this.vita = vita;
        this.party = party;
        this.memberships = memberships;
        this.imageUrl = imageUrl;
        this.imageData = imageData;
    }


    @Override
    public String get_id() {
        return _id;
    }

    @Override
    public void set_id(String _id) {
        this._id = _id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getGeburtsdatum() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return geburtsdatum != null ? geburtsdatum.format(formatter) : null;
    }

    @Override
    public void setGeburtsdatum(LocalDate geburtsdatum) {
        this.geburtsdatum = geburtsdatum;
    }

    @Override
    public String getGeburtsort() {
        return geburtsort;
    }

    @Override
    public void setGeburtsort(String geburtsort) {
        this.geburtsort = geburtsort;
    }

    @Override
    public String getSterbedatum() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return sterbedatum != null ? sterbedatum.format(formatter) : null;
    }

    @Override
    public void setSterbedatum(LocalDate sterbedatum) {
        this.sterbedatum = sterbedatum;
    }

    @Override
    public String getGeschlecht() {
        return geschlecht;
    }

    @Override
    public void setGeschlecht(String geschlecht) {
        this.geschlecht = geschlecht;
    }

    @Override
    public String getBeruf() {
        return beruf;
    }

    @Override
    public void setBeruf(String beruf) {
        this.beruf = beruf;
    }

    @Override
    public String getAkademischertitel() {
        return akademischertitel;
    }

    @Override
    public void setAkademischertitel(String akademischertitel) {
        this.akademischertitel = akademischertitel;
    }

    @Override
    public String getFamilienstand() {
        return familienstand;
    }

    @Override
    public void setFamilienstand(String familienstand) {
        this.familienstand = familienstand;
    }

    @Override
    public String getReligion() {
        return religion;
    }

    @Override
    public void setReligion(String religion) {
        this.religion = religion;
    }

    @Override
    public String getVita() {
        return vita;
    }

    @Override
    public void setVita(String vita) {
        this.vita = vita;
    }

    @Override
    public String getParty() {
        return party;
    }

    @Override
    public void setParty(String party) {
        this.party = party;
    }

    @Override
    public Set<Object> getMemberships() {
        return memberships;
    }

    @Override
    public void setMemberships(Object membership) {
        this.memberships.add(membership);
    }

    @Override
    public void setMemberships(Set<Object> memberships) {
        this.memberships = memberships;
    }


    /**
     * @param dateString Datum als String.
     * @return Datum als {@link LocalDate}.
     * @author Philipp Noah Hein #6356965
     * <p>
     * Konvertiert Datum String in {@link LocalDate}.
     */
    private LocalDate convertStringToDate(String dateString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");
        return LocalDate.parse(dateString, formatter);
    }

    @Override
    public String toTex(boolean disableTikz) throws IOException, TemplateException {
        // get all speeches
        MongoDBHandler mongoDBHandler = new MongoDBHandler();
        SpeakerDAO speakerDAO = new SpeakerDAO(
                mongoDBHandler.getSpeakerCollection()
        );
        SpeechDAO speechDAO = new SpeechDAO(mongoDBHandler.getSpeechCollection());
        SpeakerService speakerService = new SpeakerService(speakerDAO, speechDAO);
        List<Speech_impl> speeches = speakerService.getSpeechesBySpeaker(this._id);

        // config
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "templates/latex/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.GERMANY);


        // compile data for template
        Map<String, Object> data = new HashMap<>();
        data.put("speaker", this);
        data.put("speeches", speeches);
        data.put("disableTikz", disableTikz);
        data.put("linguisticService", new LinguisticService());

        // apply
        Template template = cfg.getTemplate("speaker/speaker.ftl");
        StringWriter stringWriter = new StringWriter();
        template.process(data, stringWriter);

        return stringWriter.toString();
    }

    public String toXML() throws IOException, TemplateException {
        // get all speeches
        MongoDBHandler mongoDBHandler = new MongoDBHandler();
        SpeakerDAO speakerDAO = new SpeakerDAO(
                mongoDBHandler.getSpeakerCollection()
        );
        SpeechDAO speechDAO = new SpeechDAO(mongoDBHandler.getSpeechCollection());
        SpeakerService speakerService = new SpeakerService(speakerDAO, speechDAO);
        List<Speech_impl> speeches = speakerService.getSpeechesBySpeaker(this._id);

        // config
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_34);
        cfg.setClassLoaderForTemplateLoading(Thread.currentThread().getContextClassLoader(), "templates/xml/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setLocale(Locale.GERMANY);


        // compile data for template
        Map<String, Object> data = new HashMap<>();
        data.put("speaker", this);
        data.put("speeches", speeches);
        data.put("linguisticService", new LinguisticService());

        // apply
        Template template = cfg.getTemplate("speaker/speaker.ftl");
        StringWriter stringWriter = new StringWriter();
        template.process(data, stringWriter);

        return stringWriter.toString();
    }

    public byte[] toPDF(boolean disableTikz) throws IOException, TemplateException {
        String texCode = this.toTex(disableTikz);
        return LatexCompiler.compileToByteArray(texCode);
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String getImageData() {
        return imageData;
    }

    @Override
    public void setImageData(String imageData) {
        this.imageData = imageData;
    }
}
