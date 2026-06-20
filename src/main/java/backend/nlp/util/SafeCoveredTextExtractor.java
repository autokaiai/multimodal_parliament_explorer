package backend.nlp.util;

import org.apache.uima.jcas.tcas.Annotation;

/**
 * @author Kai
 * @date 05/03/2025
 */
public class SafeCoveredTextExtractor {

    public static String getSafeCoveredText(Annotation annotation, String documentText) {
        try {
            return annotation.getCoveredText();
        } catch (StringIndexOutOfBoundsException e) {
            // Manually extract text using valid substring bounds
            int begin = Math.max(0, annotation.getBegin());
            int end = Math.min(annotation.getEnd(), documentText.length());
            return documentText.substring(begin, end);
        }
    }

    // Extract the true end position of the annotation
    public static int getSafeEnd(Annotation annotation, String documentText) {
        // Manually extract text using valid substring bounds
        return Math.min(annotation.getEnd(), documentText.length());
    }
}
