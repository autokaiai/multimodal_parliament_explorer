<#macro speakerVisualization speech>
<#if speech??>
    <input type="hidden" id="speech-id" value="${speech._id}">

    <div class="content-section">
        <h3 class="section-header">Visualisierungen</h3>

        <ul class="nav nav-tabs mb-3" id="visualizationTabs" role="tablist">
            <li class="nav-item" role="presentation">
                <button class="nav-link active" id="topics-tab" data-bs-toggle="tab" data-bs-target="#topics-tab-content" type="button" role="tab" aria-controls="topics-tab-content" aria-selected="true">
                    <i class="fas fa-tags me-2"></i>Topics
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="pos-tab" data-bs-toggle="tab" data-bs-target="#pos-tab-content" type="button" role="tab" aria-controls="pos-tab-content" aria-selected="false">
                    <i class="fas fa-font me-2"></i>Wortarten
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="sentiment-tab" data-bs-toggle="tab" data-bs-target="#sentiment-tab-content" type="button" role="tab" aria-controls="sentiment-tab-content" aria-selected="false">
                    <i class="fas fa-smile me-2"></i>Sentiments
                </button>
            </li>
            <li class="nav-item" role="presentation">
                <button class="nav-link" id="entities-tab" data-bs-toggle="tab" data-bs-target="#entities-tab-content" type="button" role="tab" aria-controls="entities-tab-content" aria-selected="false">
                    <i class="fas fa-user-tag me-2"></i>Named Entities
                </button>
            </li>
        </ul>

        <div class="tab-content" id="visualizationTabsContent">
            <div class="tab-pane fade show active" id="topics-tab-content" role="tabpanel" aria-labelledby="topics-tab" tabindex="0">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">Themenverteilung</h5>
                        <p class="card-text text-muted">Visualisierung der Themen in dieser Rede als Bubble-Chart.</p>
                        <div id="speech-topics-chart" class="chart-container"></div>
                    </div>
                </div>
            </div>

            <div class="tab-pane fade" id="pos-tab-content" role="tabpanel" aria-labelledby="pos-tab" tabindex="0">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">Wortartenverteilung</h5>
                        <p class="card-text text-muted">Visualisierung der Wortarten (Parts of Speech) in dieser Rede als Bar-Chart.</p>
                        <div id="speech-pos-chart" class="chart-container"></div>
                    </div>
                </div>
            </div>

            <div class="tab-pane fade" id="sentiment-tab-content" role="tabpanel" aria-labelledby="sentiment-tab" tabindex="0">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">Sentimentanalyse</h5>
                        <p class="card-text text-muted">Visualisierung der Sentiments (positiv, neutral, negativ) in dieser Rede als Radar-Chart.</p>
                        <div id="speech-sentiment-chart" class="chart-container"></div>
                    </div>
                </div>
            </div>

            <div class="tab-pane fade" id="entities-tab-content" role="tabpanel" aria-labelledby="entities-tab" tabindex="0">
                <div class="card">
                    <div class="card-body">
                        <h5 class="card-title">Named Entities</h5>
                        <p class="card-text text-muted">Visualisierung der benannten Entitäten (Personen, Orte, Organisationen) in dieser Rede als Sunburst-Chart.</p>
                        <div id="speech-entities-chart" class="chart-container"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</#if>
</#macro>
