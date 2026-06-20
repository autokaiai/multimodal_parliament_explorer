/**
 * @author Philipp Schneider
 * @date 19.03.2025
 *
 * Filter-Handler für die Redenliste im Multimodalen Parlament-Explorer
 * Verwaltet die Filterlogik für die SpeechList-Ansicht
 */

// Globales Objekt zum Speichern des aktuellen Filter-Status
const speechListFilterState = {
    search: '',
    fraction: '',
    topic: '',
    dateFrom: '',
    dateTo: '',
    sortField: '',
    sortDirection: 'asc',
    page: 1,
    pageSize: 20
};

// Initialisiert alle Filter-Handler für die SpeechList
document.addEventListener('DOMContentLoaded', function() {
    console.log('Initialisiere SpeechList-Filter-Handler...');
    setupSpeechListSearch();
    setupFractionFilter();
    setupDateFilter();
    setupTopicFilter();
    setupSortHandlers();
    setupPaginationHandlers();

    // Lade initiale Parameter aus der URL, falls vorhanden
    loadFiltersFromUrl();
});

// Lädt Filter-Parameter aus der URL
function loadFiltersFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);

    // Setze Filterparameter aus URL
    speechListFilterState.search = urlParams.get('search') || '';
    speechListFilterState.fraction = urlParams.get('fraction') || '';
    speechListFilterState.topic = urlParams.get('topic') || '';
    speechListFilterState.dateFrom = urlParams.get('dateFrom') || '';
    speechListFilterState.dateTo = urlParams.get('dateTo') || '';
    speechListFilterState.sortField = urlParams.get('sort') || '';
    speechListFilterState.sortDirection = urlParams.get('dir') || 'asc';
    speechListFilterState.page = parseInt(urlParams.get('page') || '1');
    speechListFilterState.pageSize = parseInt(urlParams.get('size') || '20');

    // Aktualisiere UI mit den geladenen Werten
    updateFilterUI();
}

// Aktualisiert die UI-Elemente basierend auf dem aktuellen Filter-Status
function updateFilterUI() {
    // Suchfeld
    const searchInput = document.getElementById('search-input');
    if (searchInput) {
        searchInput.value = speechListFilterState.search;
    }

    // Fraktionsfilter
    const fractionSelect = document.querySelector('select[name="fraction"]');
    if (fractionSelect && speechListFilterState.fraction) {
        fractionSelect.value = speechListFilterState.fraction;
    }

    // Topicfilter
    const topicSelect = document.querySelector('select[name="topic"]');
    if (topicSelect && speechListFilterState.topic) {
        topicSelect.value = speechListFilterState.topic;
    }

    // Datumsfilter
    const dateFromInput = document.querySelector('input[name="dateFrom"]');
    if (dateFromInput) {
        dateFromInput.value = speechListFilterState.dateFrom;
    }

    const dateToInput = document.querySelector('input[name="dateTo"]');
    if (dateToInput) {
        dateToInput.value = speechListFilterState.dateTo;
    }

    // Sortierung
    if (speechListFilterState.sortField) {
        // Entferne vorherige Sortier-Indikatoren
        document.querySelectorAll('th i.fa-sort, th i.fa-sort-up, th i.fa-sort-down').forEach(icon => {
            icon.classList.remove('fa-sort-up', 'fa-sort-down');
            icon.classList.add('fa-sort');
        });

        // Setze aktuellen Sortier-Indikator
        const sortHeader = document.querySelector(`th[data-sort="${speechListFilterState.sortField}"] i`);
        if (sortHeader) {
            sortHeader.classList.remove('fa-sort');
            sortHeader.classList.add(speechListFilterState.sortDirection === 'asc' ? 'fa-sort-up' : 'fa-sort-down');
        }
    }

    // Seitengröße
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    if (pageSizeSelect) {
        pageSizeSelect.value = speechListFilterState.pageSize.toString();
    }

    // Aktuelle Filtertags anzeigen
    updateFilterTags();
}

// Zeigt aktive Filter als Tags an
function updateFilterTags() {
    const filterTagsContainer = document.getElementById('active-filters');
    if (!filterTagsContainer) return;

    filterTagsContainer.innerHTML = '';

    const activeFilters = [];

    if (speechListFilterState.search) {
        activeFilters.push({
            type: 'search',
            label: `Suchbegriff: ${speechListFilterState.search}`
        });
    }

    if (speechListFilterState.fraction) {
        activeFilters.push({
            type: 'fraction',
            label: `Fraktion: ${speechListFilterState.fraction}`
        });
    }

    if (speechListFilterState.topic) {
        activeFilters.push({
            type: 'topic',
            label: `Thema: ${speechListFilterState.topic}`
        });
    }

    if (speechListFilterState.dateFrom) {
        activeFilters.push({
            type: 'dateFrom',
            label: `Von: ${speechListFilterState.dateFrom}`
        });
    }

    if (speechListFilterState.dateTo) {
        activeFilters.push({
            type: 'dateTo',
            label: `Bis: ${speechListFilterState.dateTo}`
        });
    }

    if (activeFilters.length === 0) {
        filterTagsContainer.innerHTML = '<span class="text-muted">Keine aktiven Filter</span>';
        return;
    }

    activeFilters.forEach(filter => {
        const tag = document.createElement('span');
        tag.className = 'badge bg-primary filter-tag me-2';
        tag.innerHTML = `${filter.label} <i class="fas fa-times ms-1" data-filter-type="${filter.type}"></i>`;

        // Event-Listener zum Entfernen des Filters
        tag.querySelector('i').addEventListener('click', (e) => {
            const filterType = e.target.getAttribute('data-filter-type');
            resetFilter(filterType);
            applyFilters();
        });

        filterTagsContainer.appendChild(tag);
    });

    // "Alle zurücksetzen" Button hinzufügen, wenn Filter aktiv sind
    if (activeFilters.length > 0) {
        const resetAllBtn = document.createElement('button');
        resetAllBtn.className = 'btn btn-sm btn-outline-secondary ms-2';
        resetAllBtn.innerHTML = '<i class="fas fa-times me-1"></i>Alle Filter zurücksetzen';
        resetAllBtn.addEventListener('click', () => {
            resetAllFilters();
            applyFilters();
        });
        filterTagsContainer.appendChild(resetAllBtn);
    }
}

// Setzt einen bestimmten Filter zurück
function resetFilter(filterType) {
    switch (filterType) {
        case 'search':
            speechListFilterState.search = '';
            const searchInput = document.getElementById('search-input');
            if (searchInput) searchInput.value = '';
            break;
        case 'fraction':
            speechListFilterState.fraction = '';
            const fractionSelect = document.querySelector('select[name="fraction"]');
            if (fractionSelect) fractionSelect.value = '';
            break;
        case 'topic':
            speechListFilterState.topic = '';
            const topicSelect = document.querySelector('select[name="topic"]');
            if (topicSelect) topicSelect.value = '';
            break;
        case 'dateFrom':
            speechListFilterState.dateFrom = '';
            const dateFromInput = document.querySelector('input[name="dateFrom"]');
            if (dateFromInput) dateFromInput.value = '';
            break;
        case 'dateTo':
            speechListFilterState.dateTo = '';
            const dateToInput = document.querySelector('input[name="dateTo"]');
            if (dateToInput) dateToInput.value = '';
            break;
    }

    // Bei Filteränderung zurück zur ersten Seite
    speechListFilterState.page = 1;
}

// Setzt alle Filter zurück
function resetAllFilters() {
    speechListFilterState.search = '';
    speechListFilterState.fraction = '';
    speechListFilterState.topic = '';
    speechListFilterState.dateFrom = '';
    speechListFilterState.dateTo = '';
    speechListFilterState.page = 1;

    // UI zurücksetzen
    const searchInput = document.getElementById('search-input');
    if (searchInput) searchInput.value = '';

    const fractionSelect = document.querySelector('select[name="fraction"]');
    if (fractionSelect) fractionSelect.value = '';

    const topicSelect = document.querySelector('select[name="topic"]');
    if (topicSelect) topicSelect.value = '';

    const dateFromInput = document.querySelector('input[name="dateFrom"]');
    if (dateFromInput) dateFromInput.value = '';

    const dateToInput = document.querySelector('input[name="dateTo"]');
    if (dateToInput) dateToInput.value = '';
}

// Suchfeld-Handler
function setupSpeechListSearch() {
    const searchInput = document.getElementById('search-input');
    const searchButton = document.getElementById('search-button');

    if (searchInput && searchButton) {
        // Suche bei Klick auf den Suchbutton
        searchButton.addEventListener('click', () => {
            speechListFilterState.search = searchInput.value.trim();
            speechListFilterState.page = 1;  // Zurück zur ersten Seite
            applyFilters();
        });

        // Suche bei Enter-Taste
        searchInput.addEventListener('keyup', (event) => {
            if (event.key === 'Enter') {
                speechListFilterState.search = searchInput.value.trim();
                speechListFilterState.page = 1;  // Zurück zur ersten Seite
                applyFilters();
            }
        });
    }
}

// Fraktionsfilter-Handler
function setupFractionFilter() {
    const fractionSelect = document.querySelector('select[name="fraction"]');
    const fractionFilterButtons = document.querySelectorAll('.dropdown-item[data-fraction]');

    if (fractionSelect) {
        fractionSelect.addEventListener('change', () => {
            speechListFilterState.fraction = fractionSelect.value;
            speechListFilterState.page = 1;  // Zurück zur ersten Seite
            applyFilters();
        });
    }

    // Alternative: Dropdown-Items mit data-fraction Attribut
    if (fractionFilterButtons.length > 0) {
        fractionFilterButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                e.preventDefault();
                speechListFilterState.fraction = button.getAttribute('data-fraction') || '';
                speechListFilterState.page = 1;  // Zurück zur ersten Seite
                applyFilters();
            });
        });
    }
}

// Datumsfilter-Handler
function setupDateFilter() {
    const dateFromInput = document.querySelector('input[name="dateFrom"]');
    const dateToInput = document.querySelector('input[name="dateTo"]');
    const dateFilterButtons = document.querySelectorAll('.dropdown-item[data-date-filter]');

    if (dateFromInput) {
        dateFromInput.addEventListener('change', () => {
            speechListFilterState.dateFrom = dateFromInput.value;
            speechListFilterState.page = 1;
            applyFilters();
        });
    }

    if (dateToInput) {
        dateToInput.addEventListener('change', () => {
            speechListFilterState.dateTo = dateToInput.value;
            speechListFilterState.page = 1;
            applyFilters();
        });
    }

    // Vordefinierte Zeiträume als Dropdown-Items
    if (dateFilterButtons.length > 0) {
        dateFilterButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                e.preventDefault();
                const filterType = button.getAttribute('data-date-filter');

                // Aktuelle Datum
                const today = new Date();
                let fromDate = new Date();

                switch (filterType) {
                    case 'all':
                        speechListFilterState.dateFrom = '';
                        speechListFilterState.dateTo = '';
                        break;
                    case 'lastWeek':
                        fromDate.setDate(today.getDate() - 7);
                        speechListFilterState.dateFrom = fromDate.toISOString().split('T')[0];
                        speechListFilterState.dateTo = today.toISOString().split('T')[0];
                        break;
                    case 'lastMonth':
                        fromDate.setMonth(today.getMonth() - 1);
                        speechListFilterState.dateFrom = fromDate.toISOString().split('T')[0];
                        speechListFilterState.dateTo = today.toISOString().split('T')[0];
                        break;
                    case 'lastYear':
                        fromDate.setFullYear(today.getFullYear() - 1);
                        speechListFilterState.dateFrom = fromDate.toISOString().split('T')[0];
                        speechListFilterState.dateTo = today.toISOString().split('T')[0];
                        break;
                    case 'custom':
                        // Wird über die InputFelder gehandhabt
                        return;
                }

                speechListFilterState.page = 1;

                // Update UI
                if (dateFromInput) {
                    dateFromInput.value = speechListFilterState.dateFrom;
                }
                if (dateToInput) {
                    dateToInput.value = speechListFilterState.dateTo;
                }

                applyFilters();
            });
        });
    }
}

// Topicfilter-Handler
function setupTopicFilter() {
    const topicSelect = document.querySelector('select[name="topic"]');
    const topicFilterButtons = document.querySelectorAll('.dropdown-item[data-topic]');

    if (topicSelect) {
        topicSelect.addEventListener('change', () => {
            speechListFilterState.topic = topicSelect.value;
            speechListFilterState.page = 1;
            applyFilters();
        });
    }

    // Alternative: Dropdown-Items mit data-topic Attribut
    if (topicFilterButtons.length > 0) {
        topicFilterButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                e.preventDefault();
                speechListFilterState.topic = button.getAttribute('data-topic') || '';
                speechListFilterState.page = 1;
                applyFilters();
            });
        });
    }
}

// Sortierungshandler
function setupSortHandlers() {
    const sortableHeaders = document.querySelectorAll('th[data-sort]');

    sortableHeaders.forEach(header => {
        header.addEventListener('click', () => {
            const sortField = header.getAttribute('data-sort');

            // Bei Klick auf aktuelle Sortierspalte: Richtung umkehren
            if (sortField === speechListFilterState.sortField) {
                speechListFilterState.sortDirection = speechListFilterState.sortDirection === 'asc' ? 'desc' : 'asc';
            } else {
                // Neue Spalte: Aufsteigend sortieren
                speechListFilterState.sortField = sortField;
                speechListFilterState.sortDirection = 'asc';
            }

            applyFilters();
        });

        // Cursor-Style für sortierbare Header
        header.style.cursor = 'pointer';
    });
}

// Paginierungshandler
function setupPaginationHandlers() {
    // Seitengröße-Änderung
    const pageSizeSelect = document.getElementById('pageSizeSelect');
    if (pageSizeSelect) {
        pageSizeSelect.addEventListener('change', () => {
            const newSize = parseInt(pageSizeSelect.value);
            speechListFilterState.pageSize = newSize;
            speechListFilterState.page = 1; // Zurück zur ersten Seite
            applyFilters();
        });
    }

    // Seitennavigation über die Paginierung
    document.addEventListener('click', (e) => {
        if (e.target.matches('.page-link') || e.target.closest('.page-link')) {
            e.preventDefault();

            const pageLink = e.target.matches('.page-link') ? e.target : e.target.closest('.page-link');
            const pageUrl = pageLink.getAttribute('href');

            if (!pageUrl) return;

            const pageUrlParams = new URLSearchParams(new URL(pageUrl, window.location.href).search);
            const newPage = parseInt(pageUrlParams.get('page') || '1');

            if (newPage !== speechListFilterState.page) {
                speechListFilterState.page = newPage;
                applyFilters();
            }
        }
    });
}

// Wendet alle Filter an und navigiert zur gefilterten URL
function applyFilters() {
    const params = new URLSearchParams();

    // Füge alle aktiven Filter zur URL hinzu
    if (speechListFilterState.search) {
        params.set('search', speechListFilterState.search);
    }

    if (speechListFilterState.fraction) {
        params.set('fraction', speechListFilterState.fraction);
    }

    if (speechListFilterState.topic) {
        params.set('topic', speechListFilterState.topic);
    }

    if (speechListFilterState.dateFrom) {
        params.set('dateFrom', speechListFilterState.dateFrom);
    }

    if (speechListFilterState.dateTo) {
        params.set('dateTo', speechListFilterState.dateTo);
    }

    // Sortierung
    if (speechListFilterState.sortField) {
        params.set('sort', speechListFilterState.sortField);
        params.set('dir', speechListFilterState.sortDirection);
    }

    // Paginierung
    params.set('page', speechListFilterState.page.toString());
    params.set('size', speechListFilterState.pageSize.toString());

    // Navigiere zur neuen URL
    const newUrl = `${window.location.pathname}?${params.toString()}`;
    window.location.href = newUrl;
}

// Event-Handler zum Anwenden der erweiterten Suche
function setupAdvancedSearchForm() {
    const advancedSearchForm = document.getElementById('advancedSearchForm');

    if (advancedSearchForm) {
        advancedSearchForm.addEventListener('submit', (e) => {
            e.preventDefault();

            // Sammle Formularwerte
            const formData = new FormData(advancedSearchForm);

            // Aktualisiere Filter-Status
            speechListFilterState.dateFrom = formData.get('dateFrom') || '';
            speechListFilterState.dateTo = formData.get('dateTo') || '';
            speechListFilterState.fraction = formData.get('fraction') || '';
            speechListFilterState.topic = formData.get('topic') || '';
            speechListFilterState.page = 1;  // Zurück zur ersten Seite

            // Wende Filter an
            applyFilters();
        });
    }
}