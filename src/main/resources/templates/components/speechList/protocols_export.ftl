<#-- protocols_export.ftl -->
<div class="card border-0 shadow-sm mb-4">
    <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3">
        <h5 class="mb-0"><i class="fas fa-file-export me-2"></i>Plenarprotokolle Export</h5>
        <span class="badge bg-light text-primary rounded-pill" id="selected-count">
            0 ausgewählt
        </span>
    </div>
    <div class="card-body">
        <div class="row g-3">
            <!-- Protokoll-Auswahl -->
            <div class="col-md-8">
                <label for="protocol-selector" class="form-label">Plenarprotokolle auswählen:</label>
                <select id="protocol-selector" class="form-select" multiple size="6">
                    <#-- Generiere eine Liste von Protokollen 20/1 bis 20/212 -->
                    <#list 1..212 as num>
                        <option value="Plenarprotokoll 20/${num}">Plenarprotokoll 20/${num}</option>
                    </#list>
                </select>
                <div class="form-text">Halten Sie STRG / CMD gedrückt, um mehrere Protokolle auszuwählen.</div>
            </div>

            <!-- Such- und Auswahloptionen -->
            <div class="col-md-4">
                <label class="form-label">Optionen:</label>
                <div class="input-group mb-3">
                    <input type="text" class="form-control" id="protocol-search" placeholder="Nummer suchen...">
                    <button class="btn btn-outline-secondary" type="button" id="protocol-search-btn">
                        <i class="fas fa-search"></i>
                    </button>
                </div>

                <div class="d-grid gap-2">
                    <button type="button" id="select-all-btn" class="btn btn-outline-secondary btn-sm">
                        <i class="fas fa-check-double me-1"></i> Alle auswählen
                    </button>
                    <button type="button" id="deselect-all-btn" class="btn btn-outline-secondary btn-sm">
                        <i class="fas fa-times me-1"></i> Auswahl zurücksetzen
                    </button>
                </div>
            </div>

            <!-- Export-Optionen -->
            <div class="col-12">
                <div class="form-check mb-2">
                    <input class="form-check-input" type="checkbox" id="disable-tikz" checked>
                    <label class="form-check-label" for="disable-tikz">
                        TikZ-Diagramme deaktivieren (schnellerer Export)
                    </label>
                </div>

                <div class="d-flex gap-2 mt-3">
                    <button type="button" id="export-pdf-btn" class="btn btn-primary" disabled>
                        <i class="fas fa-file-pdf me-1"></i> Als PDF exportieren
                    </button>
                    <button type="button" id="export-xml-btn" class="btn btn-secondary" disabled>
                        <i class="fas fa-file-code me-1"></i> Als XML exportieren
                    </button>
                </div>
            </div>

            <!-- Status-Anzeige -->
            <div class="col-12">
                <div id="export-status" class="alert alert-info d-none">
                    <div class="d-flex align-items-center">
                        <div class="spinner-border spinner-border-sm me-2" role="status">
                            <span class="visually-hidden">Wird generiert...</span>
                        </div>
                        <div>Export wird generiert, bitte warten...</div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {
        const protocolSelector = document.getElementById('protocol-selector');
        const selectAllBtn = document.getElementById('select-all-btn');
        const deselectAllBtn = document.getElementById('deselect-all-btn');
        const exportPdfBtn = document.getElementById('export-pdf-btn');
        const exportXmlBtn = document.getElementById('export-xml-btn');
        const disableTikzCheckbox = document.getElementById('disable-tikz');
        const protocolSearch = document.getElementById('protocol-search');
        const protocolSearchBtn = document.getElementById('protocol-search-btn');
        const selectedCountBadge = document.getElementById('selected-count');
        const exportStatus = document.getElementById('export-status');

        // Protokoll-Suche
        function filterProtocols() {
            const searchTerm = protocolSearch.value.toLowerCase().trim();
            Array.from(protocolSelector.options).forEach(option => {
                const text = option.text.toLowerCase();
                option.style.display = text.includes(searchTerm) ? '' : 'none';
            });
        }

        protocolSearchBtn.addEventListener('click', filterProtocols);
        protocolSearch.addEventListener('keyup', function(e) {
            if (e.key === 'Enter') filterProtocols();
        });

        // Alle auswählen
        selectAllBtn.addEventListener('click', function() {
            Array.from(protocolSelector.options).forEach(option => {
                if (option.style.display !== 'none' && !option.disabled) {
                    option.selected = true;
                }
            });
            updateSelectedCount();
            updateExportButtons();
        });

        // Auswahl zurücksetzen
        deselectAllBtn.addEventListener('click', function() {
            Array.from(protocolSelector.options).forEach(option => {
                option.selected = false;
            });
            updateSelectedCount();
            updateExportButtons();
        });

        // Zähler für Ausgewählte aktualisieren
        function updateSelectedCount() {
            const count = protocolSelector.selectedOptions.length;
            selectedCountBadge.textContent = count + ' ausgewählt';
        }

        // Export-Buttons aktivieren/deaktivieren
        function updateExportButtons() {
            const isEnabled = protocolSelector.selectedOptions.length > 0;
            exportPdfBtn.disabled = !isEnabled;
            exportXmlBtn.disabled = !isEnabled;
        }

        // Listen für Änderungen an der Auswahl
        protocolSelector.addEventListener('change', function() {
            updateSelectedCount();
            updateExportButtons();
        });

        // Export starten
        exportPdfBtn.addEventListener('click', function() {
            if (protocolSelector.selectedOptions.length === 0) return;

            const selectedIds = Array.from(protocolSelector.selectedOptions)
                    .map(option => option.value);

            // Bestimme, ob es ein einzelnes oder mehrere Protokolle sind
            if (selectedIds.length === 1) {
                // Einzelnes Protokoll
                const url = '/export/pdf/protocol/?id=' + selectedIds[0] + '&disableTikz=' + disableTikzCheckbox.checked;
                startExport(url);
            } else {
                // Mehrere Protokolle
                const url = '/export/pdf/protocols/?ids=' + selectedIds.join(',') + '&disableTikz=' + disableTikzCheckbox.checked;
                startExport(url);
            }
        });

        exportXmlBtn.addEventListener('click', function() {
            if (protocolSelector.selectedOptions.length === 0) return;

            const selectedIds = Array.from(protocolSelector.selectedOptions)
                    .map(option => option.value);

            if (selectedIds.length === 1) {
                const url = '/export/xml/protocol/?id=' + selectedIds[0];
                startExport(url);
            } else {
                const url = '/export/xml/protocols/?ids=' + selectedIds.join(',');
                startExport(url);
            }
        });

        function startExport(url) {
            // Status anzeigen
            exportStatus.classList.remove('d-none');

            // Iframe erstellen für Download ohne Seiten-Neuladen
            const downloadFrame = document.createElement('iframe');
            downloadFrame.style.display = 'none';
            document.body.appendChild(downloadFrame);

            // Warten, bis der Download beginnt
            downloadFrame.onload = function() {
                setTimeout(function() {
                    // Status ausblenden und iframe entfernen
                    exportStatus.classList.add('d-none');
                    document.body.removeChild(downloadFrame);
                }, 2000);
            };

            // Export starten
            downloadFrame.src = url;
        }

        // Initialer Zustand
        updateSelectedCount();
        updateExportButtons();
    });
</script>