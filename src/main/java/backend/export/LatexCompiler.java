package backend.export;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Kompiliert .tex-Code zu .pdf Dateien
 *
 * @author Philipp Landmann
 */
public class LatexCompiler {
    /*
      Überprüft, ob compiler installiert ist
     */
    static {
        try {
            Process p = Runtime.getRuntime().exec("latexmk -v");
            p.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("latexmk ist nicht installiert.");
        }
        try {
            Process p = Runtime.getRuntime().exec("lualatex -v");
            p.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("lualatex ist nicht installiert.");
        }
    }

    /**
     * Kompiliert .tex-Code zu einer .pdf Datei
     *
     * @param texCode .tex-Code
     * @return Pfad zur kompilierten .pdf Datei (in tmp-Ordner)
     * @author Philipp Landmann
     */
    public static Path compile(String texCode) throws IOException {
        // create new temp dir for processing
        Path tempDir = Files.createTempDirectory("parliamentProgram");
        Path texPath = Paths.get(tempDir.toString(), "toCompile.tex");
        Files.writeString(texPath, texCode);
        System.out.println(tempDir);

        try {
            Process p = Runtime.getRuntime().exec(
                    "latexmk -f -lualatex -interaction=nonstopmode -output-directory=" + tempDir + " " + texPath
            );

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                 BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("stdout: " + line);
                }
                while ((line = errorReader.readLine()) != null) {
                    System.err.println("stderr: " + line);
                }
            }
            p.waitFor();
        } catch (Exception e) {
            throw new RuntimeException("Fehler beim Kompilieren der .tex-Datei: " + e.getMessage());
        }
        return Paths.get(tempDir.toString(), "toCompile.pdf");
    }

    /**
     * @param texCode .tex-Code
     * @return Kompilierte .pdf-Datei als byte-Array
     * @throws IOException wenn die PDF-Datei nicht eingelesen werden kann.
     * @author Philipp Landmann
     */
    public static byte[] compileToByteArray(String texCode) throws IOException {
        return Files.readAllBytes(compile(texCode));
    }
}
