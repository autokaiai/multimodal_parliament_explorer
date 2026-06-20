package backend.rest;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author: Philipp Noah Hein #6356965
 * <p>
 * Konfig für Javalin-Server.
 * Lädt Einstellungen aus Datei und stellt Methoden bereit um auf diese zuzugreifen.
 */
public class JavalinConfig extends Properties {

    /**
     * @param filePath Pfad zur Konfigdatei.
     * @author: Philipp Hein #6356965
     * <p>
     * Erstellt eine neue Instanz von JavalinConfig und lädt die Konfig aus Datei.
     */
    public JavalinConfig(String filePath) {
        try (FileInputStream input = new FileInputStream(filePath)) {
            this.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return Port des Javalin Servers - Standardwert: 3000.
     * @author: Philipp Hein #6356965
     */
    public int getJavalinPort() {
        return Integer.parseInt(getProperty("JavalinServer.port", "3000"));
    }

    /**
     * @return {@code true}, wenn das Banner angezeigt werden soll, sonst {@code false}.
     * Standardwert: {@code true}.
     * <p>
     * -> nicht wirklich relevant, nur ein gimik
     * @author: Philipp Hein #6356965
     */
    public boolean showBanner() {
        return Boolean.parseBoolean(getProperty("JavalinServer.showBanner", "true"));
    }
}
