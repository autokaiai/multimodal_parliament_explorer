/**
 * @author Philipp Schneider
 * @date 15.03.2025
 */

function createBubbleChart(data, selector) {
    // Wenn keine Daten vorhanden sind
    if (!data || data.length === 0) {
        d3.select(selector).html("<div class='alert alert-info'>Keine Themen-Daten verfügbar.</div>");
        return;
    }

    // Vorhandene SVG entfernen
    d3.select(selector).select("svg").remove();

    // Konfiguration
    const width = 450;
    const height = 450;

    // SVG erstellen
    const svg = d3.select(selector)
        .append("svg")
        .attr("viewBox", [0, 0, width, height])
        .attr("width", "100%")
        .attr("height", "100%");

    // Pack-Layout erstellen
    const pack = data => d3.pack()
        .size([width - 2, height - 2])
        .padding(3)
        (d3.hierarchy({children: data})
            .sum(d => d.value));

    const root = pack(data);

    // Farbskala
    const color = d3.scaleOrdinal()
        .domain(data.map(d => d.name))
        .range(d3.schemeSet3);

    // Kreise zeichnen
    const node = svg.append("g")
        .selectAll("g")
        .data(root.leaves())
        .join("g")
        .attr("transform", d => `translate(${d.x + 1},${d.y + 1})`);

    node.append("circle")
        .attr("r", d => d.r)
        .attr("fill", d => color(d.data.name))
        .attr("opacity", 0.7)
        .attr("stroke", "#fff")
        .attr("stroke-width", 1.5);

    // Beschriftungen hinzufügen (für größere Bubbles)
    node.append("text")
        .attr("text-anchor", "middle")
        .attr("dominant-baseline", "middle")
        .text(d => d.r > 20 ? d.data.name : '')
        .attr("font-size", d => Math.min(d.r / 3, 12))
        .attr("fill", "#000");

    // Tooltip-Daten hinzufügen
    node.append("title")
        .text(d => `${d.data.name}: ${d.data.value}`);
}