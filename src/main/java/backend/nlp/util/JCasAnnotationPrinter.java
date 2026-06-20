package backend.nlp.util;

import org.apache.uima.cas.CASException;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;

import java.util.Iterator;

public class JCasAnnotationPrinter {

    /**
     * Durchläuft alle Views im übergebenen JCas, gibt den Namen (Sofa-Name) des Views
     * und alle darin enthaltenen Annotationen (Typ und abgedeckter Text) aus.
     *
     * @param jCas Das JCas-Objekt, das mehrere Views enthalten kann.
     * @throws CASException Falls ein Fehler beim Zugriff auf einen View auftritt.
     */
    public static void printAnnotationsFromAllViews(JCas jCas, boolean viewsOnly) throws CASException {
        // Hole den View-Iterator
        Iterator<JCas> viewIterator = jCas.getViewIterator();
        while (viewIterator.hasNext()) {
            JCas view = viewIterator.next();

            // Ermitteln des View-Namens: Hier nehmen wir das erste Sofa aus der Liste der Sofa-Namen.
            String viewName = view.getCas().getViewName();
            System.out.println("##### View: " + viewName);

            if (viewsOnly) {
                continue;
            }

            // Iteriere über alle Annotationen in diesem View
            for (Annotation annotation : JCasUtil.select(view, Annotation.class)) {
                // Drucke maximale Information: Typname und den abgedeckten Text der Annotation.
                System.out.println("Annotation: " + annotation.getType().getName()
                        + " | AnnotationFull: " + annotation);
            }
            System.out.println("--------------------------------------------------");
        }
    }
}
