/**
 *  * @author Philipp Schneider
 *  * @date 15.03.2025
 * Erstellt ein Radar-Chart für Sentiments
 * @param {Array} data Die Daten für das Radar-Chart
 * @param {string} selector Der CSS-Selektor für das Container-Element
 */
function createRadarChart(data, selector) {
    // Wenn keine Daten vorhanden sind
    if (!data || data.length === 0) {
        d3.select(selector).html("<div class='alert alert-info'>Keine Sentiment-Daten verfügbar.</div>");
        return;
    }

    // Vorhandene SVG entfernen
    d3.select(selector).select("svg").remove();

    // Konfiguration
    const width = 450;
    const height = 450;
    const radius = Math.min(width, height) / 2 * 0.8;

    // SVG erstellen
    const svg = d3.select(selector)
        .append("svg")
        .attr("viewBox", [0, 0, width, height])
        .attr("width", "100%")
        .attr("height", "100%")
        .append("g")
        .attr("transform", `translate(${width / 2},${height / 2})`);

    // Variablen
    const axisLabels = data.map(d => d.axis);
    const total = axisLabels.length;
    const angleSlice = Math.PI * 2 / total;

    // Skala für den Radius
    const maxValue = d3.max(data, d => d.value);
    const rScale = d3.scaleLinear()
        .domain([0, maxValue])
        .range([0, radius]);

    // Hintergrundkreise zeichnen
    const levels = 5;
    svg.selectAll(".grid-circle")
        .data(d3.range(1, levels + 1).reverse())
        .join("circle")
        .attr("class", "grid-circle")
        .attr("r", d => radius * d / levels)
        .style("fill", "none")
        .style("stroke", "#CDCDCD")
        .style("stroke-dasharray", "4 4");

    // Achsenlinien zeichnen
    const axes = svg.selectAll(".axis")
        .data(axisLabels)
        .join("g")
        .attr("class", "axis");

    axes.append("line")
        .attr("x1", 0)
        .attr("y1", 0)
        .attr("x2", (d, i) => rScale(maxValue * 1.1) * Math.cos(angleSlice * i - Math.PI / 2))
        .attr("y2", (d, i) => rScale(maxValue * 1.1) * Math.sin(angleSlice * i - Math.PI / 2))
        .style("stroke", "#CDCDCD")
        .style("stroke-width", "1px");

    // Achsenbeschriftungen
    axes.append("text")
        .attr("class", "legend")
        .attr("text-anchor", "middle")
        .attr("dy", "0.35em")
        .attr("x", (d, i) => rScale(maxValue * 1.2) * Math.cos(angleSlice * i - Math.PI / 2))
        .attr("y", (d, i) => rScale(maxValue * 1.2) * Math.sin(angleSlice * i - Math.PI / 2))
        .text(d => d)
        .style("font-size", "12px");

    // Radarlinie erstellen
    const radarLine = d3.lineRadial()
        .radius(d => rScale(d.value))
        .angle((d, i) => i * angleSlice);

    // Daten für die Radarlinie aufbereiten
    const dataForRadar = [];
    axisLabels.forEach((axis, i) => {
        const found = data.find(d => d.axis === axis);
        dataForRadar[i] = found || {axis, value: 0};
    });

    // Radarfeld einzeichnen
    svg.append("path")
        .datum(dataForRadar)
        .attr("class", "radar-area")
        .attr("d", radarLine)
        .style("fill", "#F08080")
        .style("fill-opacity", 0.5)
        .style("stroke", "#DD3333")
        .style("stroke-width", "2px");

    // Datenpunkte einzeichnen
    svg.selectAll(".radar-circle")
        .data(dataForRadar)
        .join("circle")
        .attr("class", "radar-circle")
        .attr("r", 4)
        .attr("cx", (d, i) => rScale(d.value) * Math.cos(angleSlice * i - Math.PI / 2))
        .attr("cy", (d, i) => rScale(d.value) * Math.sin(angleSlice * i - Math.PI / 2))
        .style("fill", "#DD3333")
        .style("stroke", "#fff")
        .style("stroke-width", "2px");

    // Werte anzeigen
    svg.selectAll(".value-text")
        .data(dataForRadar)
        .join("text")
        .attr("class", "value-text")
        .attr("x", (d, i) => rScale(d.value * 1.1) * Math.cos(angleSlice * i - Math.PI / 2))
        .attr("y", (d, i) => rScale(d.value * 1.1) * Math.sin(angleSlice * i - Math.PI / 2))
        .attr("text-anchor", "middle")
        .text(d => d.value.toFixed(1))
        .style("font-size", "10px");
}