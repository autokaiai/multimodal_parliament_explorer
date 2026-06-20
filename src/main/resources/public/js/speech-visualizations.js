/**
 * @author Philipp Schneider
 * @date 18.03.2025
 */

// Lade Visualisierungen für eine einzelne Rede
// In speech-visualizations.js
document.addEventListener('DOMContentLoaded', function() {
    console.log("DOMContentLoaded event in speech-visualizations.js");

    // Prüfe, ob wir auf der Speech-Detail-Seite sind
    const speechIdElement = document.getElementById('speech-id');
    console.log("Speech ID element:", speechIdElement);

    if (speechIdElement) {
        const speechId = speechIdElement.value;
        console.log("Speech ID value:", speechId);

        if (speechId) {
            console.log("Starting loadSpeechVisualizations with ID:", speechId);
            loadSpeechVisualizations(speechId);
        }
    }
});

// Zeigt Ladeanzeigen für alle Visualisierungen
function showLoadingIndicators() {
    const containers = [
        '#speech-topics-chart',
        '#speech-pos-chart',
        '#speech-sentiment-chart',
        '#speech-entities-chart'
    ];

    containers.forEach(selector => {
        const container = document.querySelector(selector);
        if (container) {
            container.innerHTML = '<div class="d-flex justify-content-center align-items-center" style="height: 300px;"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Lade...</span></div></div>';
        }
    });
}

// Zeigt Fehlermeldung für Visualisierungen an
function showErrorForVisualizations() {
    const containers = [
        '#speech-topics-chart',
        '#speech-pos-chart',
        '#speech-sentiment-chart',
        '#speech-entities-chart'
    ];

    containers.forEach(selector => {
        const container = document.querySelector(selector);
        if (container) {
            container.innerHTML = '<div class="alert alert-danger">Fehler beim Laden der Daten. Bitte versuchen Sie es später erneut.</div>';
        }
    });
}

// Lade Visualisierungsdaten für eine bestimmte Rede
async function loadSpeechVisualizations(speechId) {
    try {
        console.log("loadSpeechVisualizations called with ID:", speechId);
        showLoadingIndicators();

        const url = `/api/linguistic-features/${speechId}`;
        console.log("Fetching from URL:", url);
        const response = await fetch(url);
        console.log("Response status:", response.status);

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const data = await response.json();
        console.log("Data from API:", data);
        console.log("Data structure:", Object.keys(data));
        console.log("topicCounts structure:", data.topicCounts);
        console.log("posCountsCoarse structure:", data.posCountsCoarse);

        // Verpacke die Daten in ein Array
        const dataArray = [data];

        // Debug-Ausgaben für die aufbereiteten Daten
        const topicsData = prepareTopicsData(dataArray);
        console.log("Prepared topics data:", topicsData);

        const posData = preparePosData(dataArray);
        console.log("Prepared POS data:", posData);

        const sentimentData = prepareSentimentData(dataArray);
        console.log("Prepared sentiment data:", sentimentData);

        const namedEntitiesData = prepareNamedEntitiesData(dataArray);
        console.log("Prepared entities data:", namedEntitiesData);

        // Entferne Ladebalken vor dem Erstellen der Visualisierungen
        removeLoadingIndicators();

        // Visualisierungen erstellen
        console.log("Creating visualizations...");
        createBubbleChart(topicsData, '#speech-topics-chart');
        createBarChart(posData, '#speech-pos-chart');
        createRadarChart(sentimentData, '#speech-sentiment-chart');
        createSunburstChart(namedEntitiesData, '#speech-entities-chart');
        console.log("Visualizations created successfully.");


    } catch (error) {
        console.error('Fehler beim Laden der Visualisierungsdaten:', error);
        showErrorForVisualizations();
    }
}
// Datenaufbereitung für Topics-Bubble-Chart
function prepareTopicsData(data) {
    const topicCounts = {};

    data.forEach(item => {
        // Wenn topicCounts ein direktes Objekt ist
        if (item.topicCounts && typeof item.topicCounts === 'object' && !Array.isArray(item.topicCounts)) {
            Object.entries(item.topicCounts).forEach(([topic, count]) => {
                topicCounts[topic] = (topicCounts[topic] || 0) + count;
            });
        }
        // Wenn topicCounts ein Array von Objekten ist
        else if (item.topicCounts && Array.isArray(item.topicCounts)) {
            item.topicCounts.forEach(topicObj => {
                if (topicObj.key && topicObj.value) {
                    topicCounts[topicObj.key] = (topicCounts[topicObj.key] || 0) + topicObj.value;
                }
            });
        }
    });

    return Object.entries(topicCounts)
        .map(([name, value]) => ({ name, value }))
        .filter(item => item.value > 0)
        .sort((a, b) => b.value - a.value)
        .slice(0, 20); // Top 20 Topics
}

// Datenaufbereitung für POS-Bar-Chart
function preparePosData(data) {
    const posCounts = {};

    data.forEach(item => {
        if (item.posCounts && Array.isArray(item.posCounts)) {
            item.posCounts.forEach(posObj => {
                if (posObj.key && posObj.value) {
                    posCounts[posObj.key] = (posCounts[posObj.key] || 0) + posObj.value;
                }
            });
        }
        else if (item.posCounts && typeof item.posCounts === 'object') {
            Object.entries(item.posCounts).forEach(([pos, count]) => {
                posCounts[pos] = (posCounts[pos] || 0) + count;
            });
        }

        else if (item.posCountsCoarse && typeof item.posCountsCoarse === 'object') {
            Object.entries(item.posCountsCoarse).forEach(([pos, count]) => {
                // Ignoriere topicsSearchField, falls vorhanden
                if (pos !== 'topicsSearchField') {
                    posCounts[pos] = (posCounts[pos] || 0) + count;
                }
            });
        }
    });

    return Object.entries(posCounts)
        .map(([pos, value]) => ({
            category: pos,
            originalPos: pos,
            value
        }))
        .sort((a, b) => b.value - a.value)
        .slice(0, 8); // Top 8 POS-Tags
}

// Datenaufbereitung für Sentiment-Radar-Chart
function prepareSentimentData(data) {
    const sentimentSums = {
        "positive": 0,
        "neutral": 0,
        "negative": 0
    };

    let count = 0;

    data.forEach(item => {
        // Option 1: sentimentDistribution als Array von Objekten
        if (item.sentimentDistribution && Array.isArray(item.sentimentDistribution)) {
            item.sentimentDistribution.forEach(sentObj => {
                if (sentObj.key && sentObj.value) {
                    sentimentSums[sentObj.key] = (sentimentSums[sentObj.key] || 0) + sentObj.value;
                }
            });
            count++;
        }
        // Option 2: sentimentDistribution als direktes Objekt
        else if (item.sentimentDistribution && typeof item.sentimentDistribution === 'object') {
            Object.entries(item.sentimentDistribution).forEach(([sentiment, value]) => {
                sentimentSums[sentiment] = (sentimentSums[sentiment] || 0) + value;
            });
            count++;
        }
    });

    // Berechne Durchschnitte
    const result = [];
    if (count > 0) {
        Object.entries(sentimentSums).forEach(([sentiment, sum]) => {
            result.push({ axis: sentiment, value: sum / count });
        });
    }

    return result;
}

// Datenaufbereitung für Named-Entities-Sunburst
function prepareNamedEntitiesData(data) {
    const entityTypes = {};

    data.forEach(item => {
        // Option 1: namedEntityCounts als Array von Objekten
        if (item.namedEntityCounts && Array.isArray(item.namedEntityCounts)) {
            item.namedEntityCounts.forEach(entityObj => {
                if (entityObj.key && entityObj.value) {
                    const entity = entityObj.key;
                    const count = entityObj.value;

                    // Da die Namen nicht in PER_Name-Format vorliegen,
                    // versuchen wir, den Typ aus dem Kontext zu bestimmen
                    let type = "MISC"; // Standardtyp

                    // Basierend auf deinen Beispielen, könnten wir eine einfache Heuristik verwenden
                    if (entity.includes("tag") || entity.includes("Tag")) {
                        type = "ORG";
                    } else if (entity.includes("Mai") || entity.endsWith(".")) {
                        type = "DATE";
                    } else if (entity.includes("branche") || entity.includes("Lohn") ||
                        entity.includes("Arbeit") || entity.includes("vertrag")) {
                        type = "TOPIC";
                    }

                    if (!entityTypes[type]) {
                        entityTypes[type] = {};
                    }

                    entityTypes[type][entity] = (entityTypes[type][entity] || 0) + count;
                }
            });
        }
        // Option 2: namedEntityCounts als direktes Objekt
        else if (item.namedEntityCounts && typeof item.namedEntityCounts === 'object' && !Array.isArray(item.namedEntityCounts)) {
            Object.entries(item.namedEntityCounts).forEach(([entity, count]) => {
                // Implementiere eine ähnliche Logik wie oben...
                let type = "MISC";

                if (entity.includes("tag") || entity.includes("Tag")) {
                    type = "ORG";
                } else if (entity.includes("Mai") || entity.endsWith(".")) {
                    type = "DATE";
                } else if (entity.includes("branche") || entity.includes("Lohn") ||
                    entity.includes("Arbeit") || entity.includes("vertrag")) {
                    type = "TOPIC";
                }

                if (!entityTypes[type]) {
                    entityTypes[type] = {};
                }

                entityTypes[type][entity] = (entityTypes[type][entity] || 0) + count;
            });
        }
    });

    // Format für Sunburst
    const result = {
        name: "Entities",
        children: []
    };

    Object.entries(entityTypes).forEach(([type, entities]) => {
        const children = Object.entries(entities)
            .map(([name, value]) => ({ name, value }))
            .filter(item => item.value > 0)
            .sort((a, b) => b.value - a.value)
            .slice(0, 10);  // Top 10 pro Typ

        if (children.length > 0) {
            result.children.push({
                name: getEntityTypeName(type),
                children
            });
        }
    });

    return result;
}

// Hilfsfunktion für Entity-Typen
function getEntityTypeName(type) {
    return type;
}

// Funktion zum Entfernen der Ladebalken
function removeLoadingIndicators() {
    const containers = [
        '#speech-topics-chart',
        '#speech-pos-chart',
        '#speech-sentiment-chart',
        '#speech-entities-chart'
    ];

    containers.forEach(selector => {
        const container = document.querySelector(selector);
        if (container) {
            // Leere den Container vollständig
            container.innerHTML = '';
        }
    });
}