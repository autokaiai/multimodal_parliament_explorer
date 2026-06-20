package backend.nlp;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

public class XmiAnnotationImporterTest {

    /**
     * Testet, ob die Methode importAnnotations() erfolgreich ein JCas-Objekt erstellt,
     * wenn der Ressourcenordner "reden_annotationen" existiert und mindestens eine XMI-Datei enthält.
     */
    @Test
    public void testImportAnnotations_success() throws Exception {
        // Überprüfen, ob der Test-Ressourcenordner vorhanden ist.
        ClassLoader classLoader = getClass().getClassLoader();
        File resourceFolder = new File(classLoader.getResource("reden_annotationen").toURI());
        Assertions.assertTrue(resourceFolder.exists(), "Ressourcenordner 'reden_annotationen' existiert nicht.");
        Assertions.assertTrue(resourceFolder.isDirectory(), "Der Pfad 'reden_annotationen' ist kein Ordner.");

        // Überprüfen, ob im Ordner mindestens eine XMI-Datei vorhanden ist.
        File[] xmiFiles = resourceFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".xmi"));
        Assertions.assertNotNull(xmiFiles, "Es konnte keine Liste von XMI-Dateien ermittelt werden.");
        Assertions.assertTrue(xmiFiles.length > 0, "Es wurden keine XMI-Dateien im Ordner gefunden.");

        // Instanziiere den Importer und führe den Import durch.
        XmiAnnotationImporter importer = new XmiAnnotationImporter();
        importer.importAnnotations(resourceFolder.getAbsolutePath());
    }
}

