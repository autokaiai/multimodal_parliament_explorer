<#macro speakerAnnotation >
<div class="content-section">
    <div class="section-toggle" data-target="annotations-legend">
        <div class="d-flex justify-content-between align-items-center p-3 bg-light rounded">
            <h5 class="mb-0">Legende der Annotationen</h5>
            <i class="fas fa-chevron-down"></i>
        </div>
    </div>

    <div id="annotations-legend" class="toggle-section card mb-3">
        <div class="card-body">
            <div class="d-flex flex-wrap gap-2 mb-3">
                                    <span class="badge bg-warning annotation-toggle active" data-entity="PER">
                                        <i class="fas fa-user me-1"></i>Person
                                    </span>
                <span class="badge bg-info annotation-toggle active" data-entity="LOC">
                                        <i class="fas fa-map-marker-alt me-1"></i>Ort
                                    </span>
                <span class="badge bg-light text-dark annotation-toggle active" data-entity="ORG">
                                        <i class="fas fa-building me-1"></i>Organisation
                                    </span>
                <span class="badge bg-secondary annotation-toggle active" data-entity="MISC">
                                        <i class="fas fa-tag me-1"></i>Sonstiges
                                    </span>
            </div>
            <p class="text-muted small">
                <i class="fas fa-info-circle me-1"></i>
                Klicke auf eine Kategorie, um die farbige Hervorhebung ein- oder auszuschalten.
            </p>
        </div>
    </div>
</div>
</#macro>