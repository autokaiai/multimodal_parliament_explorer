/**
 *  * @author Philipp Schneider
 *  * @date 15.03.2025
 * Erstellt ein Sunburst-Diagramm für Named Entities
 * @param {Object} data Die hierarchischen Daten für das Sunburst-Diagramm
 * @param {string} selector Der CSS-Selektor für das Container-Element
 */
function createSunburstChart(data, selector) {
    // Wenn keine Daten vorhanden sind
    if (!data || !data.children || data.children.length === 0) {
        d3.select(selector).html("<div class='alert alert-info'>Keine Named-Entity-Daten verfügbar.</div>");
        return;
    }

    // Vorhandene SVG entfernen
    d3.select(selector).select("svg").remove();

    // Konfiguration
    const width = 450;
    const height = 450;
    const radius = Math.min(width, height) / 2;

    // SVG erstellen
    const svg = d3.select(selector)
        .append("svg")
        .attr("viewBox", [0, 0, width, height])
        .attr("width", "100%")
        .attr("height", "100%")
        .append("g")
        .attr("transform", `translate(${width / 2},${height / 2})`);

    // Hierarchie und Partition
    const root = d3.hierarchy(data)
        .sum(d => d.value || 0);

    // Partitionierung des Kreises
    const partition = d3.partition()
        .size([2 * Math.PI, radius]);

    partition(root);

    // Farbskala für die verschiedenen Ebenen und Typen
    const color = d3.scaleOrdinal()
        .domain(["Person", "Ort", "Organisation", "Sonstiges"])
        .range(["#66c2a5", "#fc8d62", "#8da0cb", "#e78ac3"]);

    // Bogenerzeuger für das Sunburst-Diagramm
    const arc = d3.arc()
        .startAngle(d => d.x0)
        .endAngle(d => d.x1)
        .innerRadius(d => d.y0)
        .outerRadius(d => d.y1)
        .padAngle(0.02)
        .padRadius(radius / 3);

    // Zeichne Segmente
    const path = svg.selectAll("path")
        .data(root.descendants().filter(d => d.depth)) // Root-Element ausschließen
        .join("path")
        .attr("fill", d => {
            while (d.depth > 1) d = d.parent;
            return color(d.data.name);
        })
        .attr("fill-opacity", d => 1.0 - d.depth * 0.15) // Innere Segmente dunkler
        .attr("d", arc);

    // Tooltips für Segmente
    path.append("title")
        .text(d => `${d.ancestors().map(d => d.data.name).reverse().join("/")}\nAnzahl: ${d.value}`);

    // Text für größere Segmente
    const textThreshold = 0.05; // Mindestgröße für Text

    const text = svg.selectAll("text")
        .data(root.descendants().filter(d => d.depth && (d.y1 - d.y0) * (d.x1 - d.x0) > textThreshold))
        .join("text")
        .attr("transform", function (d) {
            const x = (d.x0 + d.x1) / 2 * 180 / Math.PI;
            const y = (d.y0 + d.y1) / 2;
            return `rotate(${x - 90}) translate(${y},0) rotate(${x < 180 ? 0 : 180})`;
        })
        .attr("dy", "0.35em")
        .attr("text-anchor", d => (d.x0 + d.x1) / 2 < Math.PI ? "start" : "end")
        .text(d => d.data.name)
        .style("font-size", d => Math.min(10, Math.max(8, (d.y1 - d.y0) * 8)) + "px")
        .style("fill", "#fff")
        .style("pointer-events", "none");

    // Legende hinzufügen
    const legend = svg.append("g")
        .attr("transform", `translate(${-radius + 10}, ${-radius + 20})`);

    const legendItems = ["Person", "Ort", "Organisation", "Sonstiges"];

    legendItems.forEach((item, i) => {
        const legendItem = legend.append("g")
            .attr("transform", `translate(0, ${i * 20})`);

        legendItem.append("rect")
            .attr("width", 15)
            .attr("height", 15)
            .attr("fill", color(item));

        legendItem.append("text")
            .attr("x", 20)
            .attr("y", 12)
            .text(item)
            .style("font-size", "12px");
    });
}