/**
 * @author Philipp Schneider
 * @date 18.03.2025
 *
 * Filter-Handler für den Multimodalen Parlament-Explorer
 * Verwaltet die Filter-Logik für Reden und Topics
 */

// Globales Objekt zum Speichern des aktuellen Filter-Status
const filterState = {
    mode: 'all', // 'all', 'speeches', 'topic'
    selectedSpeeches: [], // Array von Rede-IDs
    selectedTopic: '', // Ausgewähltes Topic
};

// Initialisiert alle Filter-Handler
function initializeFilterHandlers() {
    console.log('Initialisiere Filter-Handler...');
    setupSpeechSelectionHandler();
    setupTopicSelectionHandler();
    setupResetButtonHandlers();
    updateFilterInfoDisplay();
}

// Handler für die Reden-Auswahl
function setupSpeechSelectionHandler() {
    const speechForm = document.getElementById('speech-selection-form');
    const speechSelector = document.getElementById('speech-selector');
    const selectAllBtn = document.getElementById('select-all-btn');

    if (!speechForm || !speechSelector || !selectAllBtn) {
        console.error('Rede-Auswahl-Elemente nicht gefunden');
        return;
    }

    // "Alle auswählen" Button
    selectAllBtn.addEventListener('click', () => {
        Array.from(speechSelector.options).forEach(option => {
            option.selected = true;
        });
    });

    // Formular-Submit
    speechForm.addEventListener('submit', (event) => {
        event.preventDefault();

        const selectedOptions = Array.from(speechSelector.selectedOptions);
        const selectedIds = selectedOptions.map(option => option.value);

        if (selectedIds.length === 0) {
            alert('Bitte wählen Sie mindestens eine Rede aus.');
            return;
        }

        // Filter-Status aktualisieren
        filterState.mode = 'speeches';
        filterState.selectedSpeeches = selectedIds;
        filterState.selectedTopic = '';

        // Visualisierungen aktualisieren
        updateVisualizationsBasedOnFilters();

        // Filter-Info aktualisieren
        updateFilterInfoDisplay();
    });
}

// Handler für die Topic-Auswahl
function setupTopicSelectionHandler() {
    const topicForm = document.getElementById('topic-selection-form');
    const topicSelector = document.getElementById('topic-selector');

    if (!topicForm || !topicSelector) {
        console.error('Topic-Auswahl-Elemente nicht gefunden');
        return;
    }

    topicForm.addEventListener('submit', (event) => {
        event.preventDefault();

        const selectedTopic = topicSelector.value;

        if (selectedTopic) {
            // Filter-Status aktualisieren
            filterState.mode = 'topic';
            filterState.selectedTopic = selectedTopic;
            filterState.selectedSpeeches = [];

            // Visualisierungen aktualisieren
            updateVisualizationsBasedOnFilters();
        } else {
            // Wenn "Alle Topics" ausgewählt, zeige alle Reden
            filterState.mode = 'all';
            filterState.selectedTopic = '';
            filterState.selectedSpeeches = [];

            // Visualisierungen aktualisieren
            updateVisualizationsBasedOnFilters();
        }

        // Filter-Info aktualisieren
        updateFilterInfoDisplay();
    });
}

// Handler für Reset-Buttons
function setupResetButtonHandlers() {
    const resetButtons = document.querySelectorAll('.filter-reset-btn');

    resetButtons.forEach(button => {
        button.addEventListener('click', () => {
            // Filter-Status zurücksetzen
            filterState.mode = 'all';
            filterState.selectedSpeeches = [];
            filterState.selectedTopic = '';

            // Formular-Auswahlen zurücksetzen
            const speechSelector = document.getElementById('speech-selector');
            const topicSelector = document.getElementById('topic-selector');

            if (speechSelector) {
                Array.from(speechSelector.options).forEach(option => {
                    option.selected = false;
                });
            }

            if (topicSelector) {
                topicSelector.value = '';
            }

            // Visualisierungen aktualisieren
            updateVisualizationsBasedOnFilters();

            // Filter-Info aktualisieren
            updateFilterInfoDisplay();
        });
    });
}

// Aktualisiert die Filter-Info-Anzeige basierend auf den aktuellen Filtereinstellungen
function updateFilterInfoDisplay() {
    const filterInfoElement = document.getElementById('filter-info');

    if (!filterInfoElement) {
        console.error('Filter-Info-Element nicht gefunden');
        return;
    }

    let infoText = '';

    switch (filterState.mode) {
        case 'all':
            infoText = 'Visualisierung für alle Reden';
            break;
        case 'speeches':
            const count = filterState.selectedSpeeches.length;
            infoText = `Visualisierung für ${count} ausgewählte Rede${count !== 1 ? 'n' : ''}`;
            break;
        case 'topic':
            infoText = `Visualisierung für Reden zum Thema "${filterState.selectedTopic}"`;
            break;
    }

    filterInfoElement.textContent = infoText;
}

// Aktualisiert die Visualisierungen basierend auf den aktuellen Filtereinstellungen
function updateVisualizationsBasedOnFilters() {
    let options = {};

    switch (filterState.mode) {
        case 'all':
            // Keine speziellen Optionen für 'alle'
            break;
        case 'speeches':
            options.redeIds = filterState.selectedSpeeches;
            break;
        case 'topic':
            options.topic = filterState.selectedTopic;
            break;
    }

    // Ruft die vorhandene Funktion zum Laden der Visualisierungsdaten auf
    loadVisualizationData(options);
}

// Generiert einen URL-Parameter-String basierend auf den aktuellen Filtereinstellungen
function generateApiUrlParams() {
    switch (filterState.mode) {
        case 'all':
            return '';
        case 'speeches':
            return `?redeIds=${filterState.selectedSpeeches.join(',')}`;
        case 'topic':
            return `?topic=${encodeURIComponent(filterState.selectedTopic)}`;
    }
}

// Event-Listener für das Laden des DOM
document.addEventListener('DOMContentLoaded', initializeFilterHandlers);

// Exportiert die Funktionen für die Nutzung in anderen Skripten
window.FilterHandler = {
    updateVisualizationsBasedOnFilters,
    generateApiUrlParams,
    resetFilters: () => {
        filterState.mode = 'all';
        filterState.selectedSpeeches = [];
        filterState.selectedTopic = '';
        updateFilterInfoDisplay();
    }
};