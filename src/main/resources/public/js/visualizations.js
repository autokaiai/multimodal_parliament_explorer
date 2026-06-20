/**
 * @author Philipp Schneider
 * @date 22.03.2025
 * Visualisierungskomponente für den Multimodalen Parlament-Explorer
 */

// Hauptfunktion, die beim Laden der Seite ausgeführt wird
document.addEventListener('DOMContentLoaded', () => {
    console.log('Initialisiere Visualisierungen...');
    console.log('window.visualizationData vorhanden:', !!window.visualizationData);

    if (window.visualizationData) {
        console.log('Struktur von visualizationData:',
            Object.keys(window.visualizationData));

        if (window.visualizationData.aggregated_data) {
            console.log('Struktur von aggregated_data:',
                Object.keys(window.visualizationData.aggregated_data));
        }
    }

    // Prüfe, ob die Visualisierungscontainer vorhanden sind
    if (document.querySelector('.visualization-container')) {
        // Prüfe, ob visualizationData vom Server bereitgestellt wurde
        if (window.visualizationData && window.visualizationData.aggregated_data) {
            console.log('Visualisierungsdaten vom Server gefunden');
            renderVisualizationsFromData(window.visualizationData);
        } else {
            console.log('Keine Daten vom Server gefunden, lade Daten über API...');
            loadVisualizationData();
        }
    }
});

/**
 * Rendert alle Visualisierungen basierend auf den Serverdaten
 * @param {Object} data Die vom Server bereitgestellten Visualisierungsdaten
 */
function renderVisualizationsFromData(data) {
    try {
        // Überprüfe, ob die Daten das erwartete Format haben
        if (!data || !data.aggregated_data) {
            console.warn('Unerwartetes Datenformat:', data);
            showNoDataMessage();
            return;
        }

        const aggregatedData = data.aggregated_data;

        // Extrahiere Daten für die verschiedenen Visualisierungen
        const topicsData = extractTopicsData(aggregatedData);
        const posData = extractPosData(aggregatedData);
        const sentimentData = extractSentimentData(aggregatedData);
        const namedEntitiesData = extractNamedEntitiesData(aggregatedData);

        // Erstelle die Visualisierungen
        createBubbleChart(topicsData, '#topics-bubble-chart');
        createBarChart(posData, '#pos-barchart');
        createRadarChart(sentimentData, '#sentiment-radar-chart');
        createSunburstChart(namedEntitiesData, '#named-entities-sunburst');

        // Zeige die Filterinformationen an
        updateFilterInfo(data.filter_type, data.filter_value);

    } catch (error) {
        console.error('Fehler beim Rendern der Visualisierungen:', error);
        showErrorMessage(error.message);
    }
}

/**
 * Extrahiert die Topic-Daten aus dem aggregated_data Objekt
 * @param {Object} aggregatedData Die aggregierten Daten
 * @returns {Array} Formatierte Daten für das Bubble-Chart
 */
function extractTopicsData(aggregatedData) {
    console.log('Extrahiere Topics-Daten...');

    if (!aggregatedData.topicCounts) {
        console.warn('Keine topicCounts gefunden');
        return [];
    }

    // topicCounts ist ein Objekt mit Topic als Schlüssel und Anzahl als Wert
    const topicCountsObj = aggregatedData.topicCounts;

    // Konvertiere das Objekt in ein Array von Objekten mit name und value
    const result = Object.entries(topicCountsObj)
        .map(([name, value]) => ({ name, value }))
        .filter(item => item.value > 0)
        .sort((a, b) => b.value - a.value)
        .slice(0, 15); // Beschränke auf die 15 häufigsten Topics

    console.log('Extrahierte Topics:', result.length);
    return result;
}

/**
 * Extrahiert die POS-Daten aus dem aggregated_data Objekt
 * @param {Object} aggregatedData Die aggregierten Daten
 * @returns {Array} Formatierte Daten für das Bar-Chart
 */
function extractPosData(aggregatedData) {
    console.log('Extrahiere POS-Daten...');

    if (!aggregatedData.posCounts) {
        console.warn('Keine posCounts gefunden');
        return [];
    }

    // posCounts ist ein Objekt mit POS-Tag als Schlüssel und Anzahl als Wert
    const posCountsObj = aggregatedData.posCounts;

    // Konvertiere das Objekt in ein Array von Objekten mit category, originalPos und value
    // Verwende den original POS-Tag als category ohne Umbenennung
    const result = Object.entries(posCountsObj)
        .map(([pos, value]) => ({
            category: pos, // Verwende direkt den POS-Tag ohne vordefinierte Labels
            originalPos: pos,
            value
        }))
        .filter(item => item.value > 0)
        .sort((a, b) => b.value - a.value)
        .slice(0, 8); // Beschränke auf die 8 häufigsten POS-Tags

    console.log('Extrahierte POS-Tags:', result.length);
    return result;
}

/**
 * Extrahiert die Sentiment-Daten aus dem aggregated_data Objekt
 * @param {Object} aggregatedData Die aggregierten Daten
 * @returns {Array} Formatierte Daten für das Radar-Chart
 */
function extractSentimentData(aggregatedData) {
    console.log('Extrahiere Sentiment-Daten...');

    if (!aggregatedData.sentimentDistribution) {
        console.warn('Keine sentimentDistribution gefunden');
        return [
            { axis: "positive", value: 0 },
            { axis: "neutral", value: 1 },
            { axis: "negative", value: 0 }
        ];
    }

    // sentimentDistribution ist ein Objekt mit Sentiment-Typ als Schlüssel und Wert als Wert
    const sentimentDistObj = aggregatedData.sentimentDistribution;

    // Konvertiere das Objekt in ein Array von Objekten mit axis und value
    const result = Object.entries(sentimentDistObj)
        .map(([sentiment, value]) => {
            // Stelle sicher, dass value eine Zahl ist
            let numValue = 0;
            if (typeof value === 'number') {
                numValue = value;
            } else if (typeof value === 'string') {
                numValue = parseFloat(value);
                if (isNaN(numValue)) numValue = 0;
            }

            return {
                axis: sentiment, // Verwende direkt den Sentiment-Typ ohne vordefinierte Labels
                value: numValue
            };
        });

    console.log('Extrahierte Sentiments:', result);
    return result;
}

/**
 * Extrahiert die Named-Entity-Daten aus dem aggregated_data Objekt
 * @param {Object} aggregatedData Die aggregierten Daten
 * @returns {Object} Formatierte Daten für das Sunburst-Chart
 */
function extractNamedEntitiesData(aggregatedData) {
    console.log('Extrahiere Named-Entity-Daten...');

    if (!aggregatedData.namedEntityCounts) {
        console.warn('Keine namedEntityCounts gefunden');
        return { name: "Entities", children: [] };
    }

    // namedEntityCounts ist ein Objekt mit Entity als Schlüssel und Anzahl als Wert
    const entityCountsObj = aggregatedData.namedEntityCounts;

    // Konvertiere in Array und sortiere nach Häufigkeit
    const sortedEntities = Object.entries(entityCountsObj)
        .filter(([_, count]) => count > 0)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 40); // Top 40 Entitäten

    // Gruppiere Entitäten nach den ersten 10 Zeichen (oder weniger) für Übersichtlichkeit
    const groupedEntities = {};

    sortedEntities.forEach(([entity, count]) => {
        // Gruppiere nach den ersten 10 Zeichen oder dem ersten Wort
        const groupKey = entity.split(' ')[0].substring(0, 10);

        if (!groupedEntities[groupKey]) {
            groupedEntities[groupKey] = [];
        }

        groupedEntities[groupKey].push({
            name: entity,
            value: count
        });
    });

    // Erstelle die hierarchische Struktur für das Sunburst-Chart
    const result = {
        name: "Entities",
        children: Object.entries(groupedEntities)
            .map(([group, entities]) => ({
                name: group, // Verwende den Gruppennamen als Kategorie
                children: entities.slice(0, 10) // Maximal 10 Entitäten pro Gruppe
            }))
            .slice(0, 5) // Maximal 5 Gruppen für Übersichtlichkeit
    };

    console.log('Extrahierte Entitäten-Gruppen:', result.children.length);
    return result;
}

/**
 * Aktualisiert die Filter-Info basierend auf filter_type und filter_value
 * @param {string} filterType Der Filtertyp
 * @param {Object} filterValue Der Filterwert
 */
function updateFilterInfo(filterType, filterValue) {
    const filterInfoElement = document.getElementById('filter-info');
    if (!filterInfoElement) return;

    let infoText = 'Visualisierung für alle Reden';

    if (filterType === 'faction' && filterValue && filterValue.faction) {
        infoText = `Visualisierung für Fraktion: ${filterValue.faction}`;
    }
    else if (filterType === 'topic' && filterValue && filterValue.topic) {
        infoText = `Visualisierung für Topic: ${filterValue.topic}`;
    }
    else if (filterType === 'topic_combination' && filterValue && Array.isArray(filterValue)) {
        const topics = [];
        filterValue.forEach(topicObj => {
            Object.values(topicObj).forEach(topic => {
                if (typeof topic === 'string') {
                    topics.push(topic);
                }
            });
        });

        if (topics.length > 0) {
            infoText = `Visualisierung für Topics: ${topics.join(', ')}`;
        }
    }

    filterInfoElement.textContent = infoText;
}

/**
 * Fallback: Lädt Visualisierungsdaten über die API
 * @param {Object} options Filteroptionen
 */
async function loadVisualizationData(options = {}) {
    // Zeige Ladeanimation
    showLoadingIndicators();

    // Bestimme API-URL basierend auf den Filter-Optionen
    let url;
    if (options.topic) {
        url = `/api/linguistic-features/by-topic/${options.topic}`;
    } else if (options.redeIds && options.redeIds.length > 0) {
        url = `/api/linguistic-features?redeIds=${options.redeIds.join(',')}`;
    } else {
        url = '/api/linguistic-features';
    }

    try {
        console.log('Lade Visualisierungsdaten von API:', url);
        const response = await fetch(url);
        if (!response.ok) throw new Error(`HTTP-Fehler! Status: ${response.status}`);

        const data = await response.json();
        console.log('API-Daten geladen:', data);

        // Daten als Array verarbeiten
        const dataArray = Array.isArray(data) ? data : [data];
        if (dataArray.length === 0) {
            showNoDataMessage();
            return;
        }

        // Bereite Daten für Charts vor
        // Für API-Daten verwenden wir andere Verarbeitungsfunktionen
        const topicsData = prepareApiTopicsData(dataArray);
        const posData = prepareApiPosData(dataArray);
        const sentimentData = prepareApiSentimentData(dataArray);
        const namedEntitiesData = prepareApiNamedEntitiesData(dataArray);

        // Erzeuge Charts
        createBubbleChart(topicsData, '#topics-bubble-chart');
        createBarChart(posData, '#pos-barchart');
        createRadarChart(sentimentData, '#sentiment-radar-chart');
        createSunburstChart(namedEntitiesData, '#named-entities-sunburst');
    } catch (error) {
        console.error('Fehler beim Laden der API-Daten:', error);
        showErrorMessage(error.message);
    }
}

// ---- API-Datenverarbeitungsfunktionen ----

function prepareApiTopicsData(data) {
    const topicCounts = {};

    data.forEach(item => {
        if (item.topicCounts) {
            // Fall 1: topicCounts ist ein Objekt
            if (typeof item.topicCounts === 'object' && !Array.isArray(item.topicCounts)) {
                Object.entries(item.topicCounts).forEach(([topic, count]) => {
                    topicCounts[topic] = (topicCounts[topic] || 0) + count;
                });
            }
            // Fall 2: topicCounts ist ein Array
            else if (Array.isArray(item.topicCounts)) {
                item.topicCounts.forEach(topicObj => {
                    if (topicObj.key && topicObj.value !== undefined) {
                        topicCounts[topicObj.key] = (topicCounts[topicObj.key] || 0) + topicObj.value;
                    }
                });
            }
        }
    });

    return Object.entries(topicCounts)
        .map(([name, value]) => ({ name, value }))
        .filter(item => item.value > 0)
        .sort((a, b) => b.value - a.value)
        .slice(0, 15);
}

function prepareApiPosData(data) {
    const posCounts = {};

    data.forEach(item => {
        // Fall 1: posCounts ist ein Objekt
        if (item.posCounts && typeof item.posCounts === 'object' && !Array.isArray(item.posCounts)) {
            Object.entries(item.posCounts).forEach(([pos, count]) => {
                if (pos !== 'topicsSearchField') {
                    posCounts[pos] = (posCounts[pos] || 0) + count;
                }
            });
        }
        // Fall 2: posCounts ist ein Array
        else if (item.posCounts && Array.isArray(item.posCounts)) {
            item.posCounts.forEach(posObj => {
                if (posObj.key && posObj.value !== undefined && posObj.key !== 'topicsSearchField') {
                    posCounts[posObj.key] = (posCounts[posObj.key] || 0) + posObj.value;
                }
            });
        }
        // Fall 3: Alternative posCountsCoarse
        else if (item.posCountsCoarse && typeof item.posCountsCoarse === 'object') {
            Object.entries(item.posCountsCoarse).forEach(([pos, count]) => {
                if (pos !== 'topicsSearchField') {
                    posCounts[pos] = (posCounts[pos] || 0) + count;
                }
            });
        }
    });

    return Object.entries(posCounts)
        .map(([pos, value]) => ({
            category: pos, // Verwende den originalen POS-Tag ohne Labels
            originalPos: pos,
            value
        }))
        .sort((a, b) => b.value - a.value)
        .slice(0, 8);
}

function prepareApiSentimentData(data) {
    const sentimentSums = {
        "positive": 0,
        "neutral": 0,
        "negative": 0
    };

    let count = 0;

    data.forEach(item => {
        // Fall 1: sentimentDistribution ist ein Objekt
        if (item.sentimentDistribution && typeof item.sentimentDistribution === 'object' && !Array.isArray(item.sentimentDistribution)) {
            Object.entries(item.sentimentDistribution).forEach(([sentiment, value]) => {
                let numValue = 0;
                if (typeof value === 'number') {
                    numValue = value;
                } else if (typeof value === 'string') {
                    numValue = parseFloat(value);
                    if (isNaN(numValue)) numValue = 0;
                }
                sentimentSums[sentiment] = (sentimentSums[sentiment] || 0) + numValue;
            });
            count++;
        }
        // Fall 2: sentimentDistribution ist ein Array
        else if (item.sentimentDistribution && Array.isArray(item.sentimentDistribution)) {
            item.sentimentDistribution.forEach(sentObj => {
                if (sentObj.key && sentObj.value !== undefined) {
                    let numValue = 0;
                    if (typeof sentObj.value === 'number') {
                        numValue = sentObj.value;
                    } else if (typeof sentObj.value === 'string') {
                        numValue = parseFloat(sentObj.value);
                        if (isNaN(numValue)) numValue = 0;
                    }
                    sentimentSums[sentObj.key] = (sentimentSums[sentObj.key] || 0) + numValue;
                }
            });
            count++;
        }
    });

    // Berechne Durchschnitte
    const result = [];
    if (count > 0) {
        Object.entries(sentimentSums).forEach(([sentiment, sum]) => {
            result.push({
                axis: sentiment, // Verwende den originalen Sentiment-Typ ohne Labels
                value: sum / count
            });
        });
    }

    return result;
}

function prepareApiNamedEntitiesData(data) {
    // Sammle alle Entitäten mit ihren Häufigkeiten
    const entityCounts = {};

    data.forEach(item => {
        if (item.namedEntityCounts) {
            // Fall 1: namedEntityCounts ist ein Objekt
            if (typeof item.namedEntityCounts === 'object' && !Array.isArray(item.namedEntityCounts)) {
                Object.entries(item.namedEntityCounts).forEach(([entity, count]) => {
                    entityCounts[entity] = (entityCounts[entity] || 0) + count;
                });
            }
            // Fall 2: namedEntityCounts ist ein Array
            else if (Array.isArray(item.namedEntityCounts)) {
                item.namedEntityCounts.forEach(entityObj => {
                    if (entityObj.key && entityObj.value !== undefined) {
                        entityCounts[entityObj.key] = (entityCounts[entityObj.key] || 0) + entityObj.value;
                    }
                });
            }
        }
    });

    // Sortiere nach Häufigkeit und nimm die Top 40
    const sortedEntities = Object.entries(entityCounts)
        .filter(([_, count]) => count > 0)
        .sort((a, b) => b[1] - a[1])
        .slice(0, 40);

    // Gruppiere Entitäten für bessere Übersichtlichkeit
    const groupedEntities = {};

    sortedEntities.forEach(([entity, count]) => {
        // Einfache Gruppierung nach dem ersten Wort oder den ersten Zeichen
        const groupKey = entity.split(' ')[0].substring(0, 10);

        if (!groupedEntities[groupKey]) {
            groupedEntities[groupKey] = [];
        }

        groupedEntities[groupKey].push({
            name: entity,
            value: count
        });
    });

    // Format für Sunburst
    const result = {
        name: "Entities",
        children: Object.entries(groupedEntities)
            .map(([group, entities]) => ({
                name: group,
                children: entities.slice(0, 10) // Max. 10 pro Gruppe
            }))
            .slice(0, 5) // Max. 5 Gruppen
    };

    return result;
}

// ---- Hilfsfunktionen für UI-Feedback ----

function showLoadingIndicators() {
    const containers = [
        '#topics-bubble-chart',
        '#pos-barchart',
        '#sentiment-radar-chart',
        '#named-entities-sunburst'
    ];

    containers.forEach(selector => {
        const container = document.querySelector(selector);
        if (container) {
            container.innerHTML = `
                <div class="d-flex justify-content-center align-items-center h-100">
                    <div class="text-center">
                        <div class="spinner-border text-primary mb-3" role="status">
                            <span class="visually-hidden">Wird geladen...</span>
                        </div>
                        <p class="text-muted">Daten werden geladen...</p>
                    </div>
                </div>
            `;
        }
    });
}

function showNoDataMessage() {
    const containers = [
        '#topics-bubble-chart',
        '#pos-barchart',
        '#sentiment-radar-chart',
        '#named-entities-sunburst'
    ];

    containers.forEach(selector => {
        const container = document.querySelector(selector);
        if (container) {
            container.innerHTML = `
                <div class="d-flex justify-content-center align-items-center h-100">
                    <div class="text-center">
                        <i class="fas fa-info-circle fa-3x text-muted mb-3"></i>
                        <p class="text-muted">Keine Daten verfügbar für die aktuelle Auswahl.</p>
                    </div>
                </div>
            `;
        }
    });
}

function showErrorMessage(message) {
    const containers = [
        '#topics-bubble-chart',
        '#pos-barchart',
        '#sentiment-radar-chart',
        '#named-entities-sunburst'
    ];

    containers.forEach(selector => {
        const container = document.querySelector(selector);
        if (container) {
            container.innerHTML = `
                <div class="d-flex justify-content-center align-items-center h-100">
                    <div class="alert alert-danger">
                        <i class="fas fa-exclamation-triangle me-2"></i>
                        Fehler beim Laden der Daten: ${message || 'Unbekannter Fehler'}
                    </div>
                </div>
            `;
        }
    });
}