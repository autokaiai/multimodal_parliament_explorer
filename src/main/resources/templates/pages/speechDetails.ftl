<#import "../components/speakerDetails/speakerCard.ftl" as spkr>
<#import "../components/speakerDetails/speakerAgenda.ftl" as spkrAgenda>
<#import "../components/speakerDetails/speakerVideo.ftl" as spkrVideo>
<#import "../components/speakerDetails/speakerAnnotation.ftl" as spkrAnnotation>
<#import "../components/speakerDetails/speakerVisualizations.ftl" as spkrVisualizations>
<#import "../components/speakerDetails/speakerSpeechContent.ftl" as spkrSpeechContent>
<#import "../components/speakerDetails/speakerCardHeader.ftl" as spkrCardHeader>
<#import "../components/speakerDetails/speakerCommentSection.ftl" as spkrCommentSection>

<!DOCTYPE html>
<html lang="de">
<head>
    <meta charset="UTF-8">
    <title>Rede Details</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="/css/styles.css">

</head>
<body>
<header class="bg-primary text-white p-3">
    <div class="container">
        <h1>Multimodaler Parlament-Explorer</h1>
    </div>
</header>

<main class="container my-4">
    <div class="card">
        <@spkrCardHeader.speakerCardHeader speech=speech />
        <div class="card-body">
            <#if speech??>
                <#if speech.speakerObject??>

                    <@spkr.speakerCard speaker=speech.speakerObject />

                    <@spkrAgenda.speakerAgenda speech=speech />

                    <@spkrVideo.speakerVideo speech=speech />

                    <@spkrVisualizations.speakerVisualization speech=speech />

                    <@spkrAnnotation.speakerAnnotation />

                    <@spkrSpeechContent.speakerSpeechContent speech=speech />

                    <@spkrCommentSection.speakerCommentSection speech=speech />

                <#else>
                    <div class="alert alert-danger">Keine Speaker Informationen vorhanden.</div>
                </#if>
            <#else>
                <div class="alert alert-danger">Keine Redeinformationen gefunden.</div>
            </#if>
        </div>
    </div>
</main>


<footer class="bg-dark text-white p-3">
    <div class="container">
        <p class="mb-0">© 2025 Multimodaler Parlament-Explorer</p>
    </div>
</footer>

<script src="https://code.jquery.com/jquery-3.6.4.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"></script>



<!-- D3.js für Visualisierungen -->
<script src="https://d3js.org/d3.v7.min.js"></script>


<script>
    console.log('Inline script is running');
    console.log('Hidden input element:', document.getElementById('speech-id'));
</script>

<!-- Chart-Skripte -->
<script src="/js/charts/bubbleChart.js"></script>
<script src="/js/charts/barChart.js"></script>
<script src="/js/charts/radarChart.js"></script>
<script src="/js/charts/sunburstChart.js"></script>
<script src="/js/speech-visualizations.js"></script>
<script src="/js/video-transcript-sync.js"></script>
<!-- Script for entity highlighting and toggle functionality -->
<script>


    $(document).ready(function() {
        // Benannte Entitätsdaten aus der Datenbank
        const namedEntities = [
            <#if namedEntities??>
            <#list namedEntities as entity>
            {
                begin: ${entity.begin!"0"},
                end: ${entity.end!"0"},
                coveredText: "${entity.coveredText?js_string}",
                type: "${entity.type?js_string}"
            }<#if entity?has_next>, </#if>
            </#list>
            </#if>
        ];

        // Funktion zur Hervorhebung von Entitäten im Text
        function highlightEntities() {
            // Zuerst wird der ursprüngliche Textinhalt wiederhergestellt
            $('.text-content').each(function () {
                $(this).html($(this).text());
            });

            // Aktive Entitätstypen ermitteln
            const activeTypes = [];
            $('.annotation-toggle.active').each(function () {
                activeTypes.push($(this).data('entity'));
            });

            // Überspringe die Verarbeitung, falls keine aktiven Typen vorhanden sind
            if (activeTypes.length === 0) return;

            // Jedes Textelement verarbeiten
            $('.text-content').each(function () {
                const $textElement = $(this);
                const textId = $textElement.attr('id');
                const paragraphIndex = parseInt(textId.replace('text-', ''));

                // Den ursprünglichen Text abrufen
                let text = $textElement.text();

                // Ein Array mit allen Zeichenpositionen im Text erstellen
                // und deren Entitätsinformationen zuordnen
                const textPositions = Array(text.length).fill(null);

                // Positionen markieren, die zu aktiven Entitätstypen gehören
                namedEntities.forEach(entity => {
                    // Überspringe, wenn der Entitätstyp nicht aktiv ist
                    if (!activeTypes.includes(entity.type)) return;

                    // Überprüfen, ob der Entitätstext im Absatz vorhanden ist
                    if (text.includes(entity.coveredText)) {
                        // Alle Vorkommen des Entitätstextes finden
                        let position = 0;
                        while ((position = text.indexOf(entity.coveredText, position)) !== -1) {
                            // Jede Position im Bereich mit dem Entitätstyp markieren
                            for (let i = 0; i < entity.coveredText.length; i++) {
                                textPositions[position + i] = entity.type;
                            }
                            position += 1;
                        }
                    }
                });

                // Ersetze text mit dem span tag
                let newHtml = '';
                let currentType = null;
                let currentSpan = '';

                for (let i = 0; i < text.length; i++) {
                    const entityType = textPositions[i];

                    if (entityType !== currentType) {
                        // Schließe aktuellen span tag
                        if (currentSpan.length > 0) {
                            if (currentType) {
                                newHtml += '<span class="highlighted-text entity-' + currentType + '">' + currentSpan + '</span>';
                            } else {
                                newHtml += currentSpan;
                            }
                            currentSpan = '';
                        }

                        // Starte neuen span
                        currentType = entityType;
                    }

                    // Füge den aktuellen charakter zum span hinzu
                    currentSpan += text[i];
                }

                // Füge den restlichen text hinzu
                if (currentSpan.length > 0) {
                    if (currentType) {
                        newHtml += '<span class="highlighted-text entity-' + currentType + '">' + currentSpan + '</span>';
                    } else {
                        newHtml += currentSpan;
                    }
                }

                // Update HTML element
                $textElement.html(newHtml);
            });
        }

        // Toggle entity highlighted wenn die items in der legende angeklickt werden
        $('.annotation-toggle').on('click', function () {
            $(this).toggleClass('active inactive');
            highlightEntities();
        });

        // Toggle sections wenn der header angeklickt wird
        $('.section-toggle').on('click', function () {
            const targetId = $(this).data('target');
            const $target = $('#' + targetId);
            const $icon = $(this).find('i');

            if ($target.hasClass('collapsed')) {
                $target.removeClass('collapsed');
                $(this).removeClass('collapsed');
                $icon.removeClass('fa-chevron-right').addClass('fa-chevron-down');
            } else {
                $target.addClass('collapsed');
                $(this).addClass('collapsed');
                $icon.removeClass('fa-chevron-down').addClass('fa-chevron-right');
            }
        });


        highlightEntities();
    });
</script>

</body>
</html>