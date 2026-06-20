<#macro speakerSpeechContent speech>
<#if speech.textContent?? && (speech.textContent?size gt 0)>
    <div class="content-section">
        <h3 class="section-header">Textinhalt</h3>
        <div id="speechText" class="list-group">
            <#list speech.textContent as content>
                <#assign sentenceSentiment = "neutral">
                <#assign sentimentValue = 0>

                <!-- Prüfe, ob es einen passenden Sentiment-Wert gibt -->
                <#list sentiments as sentiment>
                    <#if sentiment.coveredText == content.text>
                        <#assign sentimentValue = sentiment.sentiment!"0">
                        <#if sentimentValue gt 0>
                            <#assign sentenceSentiment = "positiv">
                        <#elseif sentimentValue lt 0>
                            <#assign sentenceSentiment = "negativ">
                        </#if>
                    </#if>
                </#list>

                <#if content.type == "comment">
                    <!-- Darstellung für Kommentare -->
                    <div class="list-group-item list-group-item-warning">
                        <strong>Kommentar:</strong> <span
                                id="text-${content?index}">${content.text!"-"}</span>
                    </div>
                <#else>
                    <!-- Darstellung für normalen Text mit Sentiment-Wert -->
                    <div class="list-group-item d-flex justify-content-between align-items-center">
                        <div class="text-content"
                             id="text-${content?index}">${content.text!"-"}</div>
                        <span class="badge" style="
                        <#if sentimentValue gt 0>
                                background-color: green; color: white;
                        <#elseif sentimentValue lt 0>
                                background-color: red; color: white;
                        <#else>
                                background-color: gray; color: white;
                        </#if>
                                padding: 5px; border-radius: 5px;">
                                            <#if sentimentValue gt 0>
                                                😊 (+${sentimentValue?string("0.00")})
                                            <#elseif sentimentValue lt 0>
                                                😠 (${sentimentValue?string("0.00")})
                                            <#else>
                                                😐 (neutral)
                                            </#if>
                                            </span>
                    </div>
                </#if>
            </#list>
        </div>
    </div>
<#else>
    <p>Kein zusätzlicher Textinhalt vorhanden.</p>
</#if>

</#macro>