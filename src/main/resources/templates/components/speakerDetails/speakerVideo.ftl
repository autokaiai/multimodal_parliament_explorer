<#macro speakerVideo speech>
    <#if speech.videoUrl?? && speech.videoUrl?has_content>
        <div class="content-section">
            <h3 class="section-header">Video zur Rede mit Transkript</h3>

            <div class="row">
                <div class="col-md-6">
                    <!-- Video Container -->
                    <div class="video-container mb-3">
                        <iframe id="speech-video" class="embed-responsive-item"
                                src="${speech.videoUrl?string}" allowfullscreen>
                        </iframe>
                    </div>
                </div>

                <div class="col-md-6">
                    <!-- Transcript Container -->
                    <div class="card">
                        <div class="card-header">
                            <h5 class="mb-0">Video-Transkript mit Sentiment-Analyse</h5>
                        </div>
                        <div class="card-body">
                            <div id="transcript-container" class="transcript-container" style="max-height: 400px; overflow-y: auto;">
                                <#if transcriptSegments?? && transcriptSegments?size gt 0>
                                    <#list transcriptSegments as segment>
                                        <#assign sentimentClass = "neutral">
                                        <#assign sentimentValue = segment.sentiment!'0'>
                                        <#assign posScore = segment.posScore!'0'>
                                        <#assign neuScore = segment.neuScore!'0'>
                                        <#assign negScore = segment.negScore!'0'>

                                    <#-- Konvertiere String in Zahl -->
                                        <#assign sentimentValueStr = sentimentValue?string>
                                        <#assign sentimentValueNum = 0>
                                        <#if sentimentValueStr?contains(",")>
                                            <#assign sentimentValueStr = sentimentValueStr?replace(",", ".")>
                                        </#if>
                                        <#attempt>
                                            <#assign sentimentValueNum = sentimentValueStr?number>
                                            <#recover>
                                                <#assign sentimentValueNum = 0>
                                        </#attempt>

                                    <#-- Berechne den Sentiment-Typ basierend auf dem konvertierten Wert -->
                                        <#if sentimentValueNum gt 0>
                                            <#assign sentimentClass = "positive">
                                        <#elseif sentimentValueNum lt 0>
                                            <#assign sentimentClass = "negative">
                                        </#if>

                                        <div class="transcript-segment"
                                             data-start="${segment.start?string('0.####')}"
                                             data-end="${segment.end?string('0.####')}"
                                             data-sentiment="${sentimentClass}"
                                             data-sentiment-value="${sentimentValueStr}">
                                            ${segment.text}
                                            <span class="sentiment-badge ${sentimentClass}">
                                            <#if sentimentClass == "positive">
                                                <i class="fas fa-smile"></i> ${sentimentValueStr}
                                            <#elseif sentimentClass == "negative">
                                                <i class="fas fa-frown"></i> ${sentimentValueStr}
                                            <#else>
                                                <i class="fas fa-meh"></i> ${sentimentValueStr}
                                            </#if>
                                        </span>
                                            <div class="sentiment-details">
                                                <span class="score"><span class="score-label">Positiv:</span> ${posScore?string}</span>
                                                <span class="score"><span class="score-label">Neutral:</span> ${neuScore?string}</span>
                                                <span class="score"><span class="score-label">Negativ:</span> ${negScore?string}</span>
                                            </div>
                                        </div>
                                    </#list>
                                <#else>
                                    <div class="alert alert-info">
                                        Kein Transkript für dieses Video verfügbar.
                                    </div>
                                </#if>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <style>
            /* Sentiment Badges */
            .sentiment-badge {
                display: inline-block;
                font-size: 0.8rem;
                padding: 2px 6px;
                margin-left: 8px;
                border-radius: 10px;
                color: white;
            }

            .sentiment-badge.positive {
                background-color: rgba(40, 167, 69, 0.8);
            }

            .sentiment-badge.neutral {
                background-color: rgba(108, 117, 125, 0.8);
            }

            .sentiment-badge.negative {
                background-color: rgba(220, 53, 69, 0.8);
            }

            /* Hover-Effekt für Transkript-Segmente */
            .transcript-segment {
                position: relative;
                padding: 8px 12px;
                margin-bottom: 12px;
                border-radius: 6px;
                border-left: 4px solid transparent;
                transition: all 0.3s ease;
                background-color: #fff;
                box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
                cursor: pointer;
            }

            .transcript-segment .sentiment-details {
                position: absolute;
                right: 10px;
                top: -40px;
                background-color: rgba(0, 0, 0, 0.85);
                color: white;
                padding: 5px 10px;
                border-radius: 4px;
                font-size: 0.8rem;
                opacity: 0;
                transition: opacity 0.3s;
                pointer-events: none;
                z-index: 10;
                white-space: nowrap;
            }

            .transcript-segment:hover .sentiment-details {
                opacity: 1;
            }

            .transcript-segment:hover {
                background-color: #f1f3f5;
            }

            .transcript-segment.active {
                transform: translateX(3px);
            }

            /* Farbige Markierung nach Sentiment-Typ */
            .transcript-segment[data-sentiment="positive"] {
                border-left-color: rgba(40, 167, 69, 0.5);
                background-color: rgba(40, 167, 69, 0.05);
            }

            .transcript-segment[data-sentiment="neutral"] {
                border-left-color: rgba(108, 117, 125, 0.5);
                background-color: rgba(108, 117, 125, 0.05);
            }

            .transcript-segment[data-sentiment="negative"] {
                border-left-color: rgba(220, 53, 69, 0.5);
                background-color: rgba(220, 53, 69, 0.05);
            }

            /* Intensivere Farbigkeit für aktuelle Textstelle */
            .transcript-segment[data-sentiment="positive"].active {
                background-color: rgba(40, 167, 69, 0.2);
                box-shadow: -3px 0 0 rgba(40, 167, 69, 1);
            }

            .transcript-segment[data-sentiment="neutral"].active {
                background-color: rgba(108, 117, 125, 0.2);
                box-shadow: -3px 0 0 rgba(108, 117, 125, 1);
            }

            .transcript-segment[data-sentiment="negative"].active {
                background-color: rgba(220, 53, 69, 0.2);
                box-shadow: -3px 0 0 rgba(220, 53, 69, 1);
            }

            /* Container-Styling */
            .transcript-container {
                max-height: 400px;
                overflow-y: auto;
                padding: 15px;
                border-radius: 8px;
                background-color: #f8f9fa;
                border: 1px solid #e9ecef;
                box-shadow: inset 0 1px 3px rgba(0, 0, 0, 0.05);
                scrollbar-width: thin;
            }
        </style>

    </#if>
</#macro>