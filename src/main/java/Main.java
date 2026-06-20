import backend.rest.RESTHandler;
import freemarker.template.Configuration;
import io.javalin.Javalin;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinFreemarker;

public class Main {

    public static void main(String[] args) {

        // PeriodicTask starten, aus performance Gründen auskommentiert
//        PeriodicTask.startScheduledTask();

        // FreeMarker konfigurieren
        Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);
        freemarkerConfig.setClassLoaderForTemplateLoading(
                Main.class.getClassLoader(),
                "/templates"
        );
        freemarkerConfig.setDefaultEncoding("UTF-8");

        // Javalin-Server erstellen
        Javalin app = Javalin.create(config -> {
            // Static Files (CSS, JS) aus dem resources/public-Ordner bereitstellen
            config.staticFiles.add("/public", Location.CLASSPATH);

            // FreeMarker als Template-Engine einrichten (innerhalb der Javalin-Konfiguration)
            config.fileRenderer(new JavalinFreemarker(freemarkerConfig));

            // Debug-Modus (optional)
            config.showJavalinBanner = false;
        });

        // REST-Routen registrieren
        RESTHandler.registerRoutes(app);

        // Server auf Port 7001 starten
        app.start(8080);

        System.out.println("Multimodaler Parlament-Explorer läuft auf http://localhost:8080");
    }
}
