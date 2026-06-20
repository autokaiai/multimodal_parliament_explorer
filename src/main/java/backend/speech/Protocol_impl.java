package backend.speech;

import backend.speech.interfaces.IProtocol;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * @author: Philipp Hein #6356965
 * Implementierung des {@link IProtocol} Interfaces
 */
public class Protocol_impl implements IProtocol {

    private Long date;
    private Long starttime;
    private Long endtime;
    private Integer index;
    private String title;
    private String place;
    private Integer wp;

    /**
     * @author: Philipp Hein #6356965
     * Konstruktor
     *
     * @param date      Datum als Long
     * @param starttime Startzeit als Long
     * @param endtime   Endzeit als Long
     * @param index     Index Protocol
     * @param title     Titel Protocol
     * @param place     Ort Protocol
     * @param wp        WP Protocol
     */
    public Protocol_impl(Long date, Long starttime, Long endtime, Integer index, String title, String place, Integer wp) {
        this.date = date;
        this.starttime = starttime;
        this.endtime = endtime;
        this.index = index;
        this.title = title;
        this.place = place;
        this.wp = wp;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return Datum Protocol als String
     */
    @Override
    public String getDate() {
        return dateFormat(date);
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt Datum Protocol
     *
     * @param date neu
     */
    @Override
    public void setDate(Long date) {
        this.date = date;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return Startzeit Protocol als String
     */
    @Override
    public String getStarttime() {
        return dateFormat(starttime);
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt Startzeit Protocol
     *
     * @param starttime neu
     */
    @Override
    public void setStarttime(Long starttime) {
        this.starttime = starttime;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return Endzeit Protocol als String
     */
    @Override
    public String getEndtime() {
        return dateFormat(endtime);
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt Endzeit Protocol
     *
     * @param endtime neu
     */
    @Override
    public void setEndtime(Long endtime) {
        this.endtime = endtime;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return Index Protocol
     */
    @Override
    public Integer getIndex() {
        return index;
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt Index Protocol
     *
     * @param index neu
     */
    @Override
    public void setIndex(Integer index) {
        this.index = index;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return Titel Protocol
     */
    @Override
    public String getTitle() {
        return title;
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt Titel Protocol
     *
     * @param title neu
     */
    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return Ort Protocol
     */
    @Override
    public String getPlace() {
        return place;
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt Ort Protocol
     *
     * @param place neu
     */
    @Override
    public void setPlace(String place) {
        this.place = place;
    }

    /**
     * @author: Philipp Hein #6356965
     * @return WP Protocol
     */
    @Override
    public Integer getWp() {
        return wp;
    }

    /**
     * @author: Philipp Hein #6356965
     * Setzt WP Protocol
     *
     * @param wp neu
     */
    @Override
    public void setWp(Integer wp) {
        this.wp = wp;
    }

    /**
     * @author: Philipp Hein #6356965
     * Formatiert long Wert in ein Datum als String
     *
     * @param number long Wert, der formatiert werden soll
     * @return formatiertes Datum als String
     */
    public String dateFormat(Long number) {
        Instant instant = Instant.ofEpochMilli(number);
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");
        return localDateTime.format(formatter);
    }
}
