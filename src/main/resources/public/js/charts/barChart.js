/**
 * @author Philipp Schneider
 * @date 15.03.2025
 */
function createBarChart(data, selector) {
    // Wenn keine Daten vorhanden sind
    if (!data || data.length === 0) {
        d3.select(selector).html("<div class='alert alert-info'>Keine POS-Daten verfügbar.</div>");
        return;
    }

    // Vorhandene SVG entfernen
    d3.select(selector).select("svg").remove();

    // Konfiguration
    const margin = {top: 20, right: 30, bottom: 50, left: 60};
    const width = 450 - margin.left - margin.right;
    const height = 400 - margin.top - margin.bottom;

    // SVG erstellen
    const svg = d3.select(selector)
        .append("svg")
        .attr("viewBox", [0, 0, width + margin.left + margin.right, height + margin.top + margin.bottom])
        .attr("width", "100%")
        .attr("height", "100%")
        .append("g")
        .attr("transform", `translate(${margin.left},${margin.top})`);

    // X-Achse
    const x = d3.scaleBand()
        .domain(data.map(d => d.category))
        .range([0, width])
        .padding(0.2);

    svg.append("g")
        .attr("transform", `translate(0,${height})`)
        .call(d3.axisBottom(x))
        .selectAll("text")
        .attr("transform", "rotate(-45)")
        .style("text-anchor", "end");

    // Y-Achse
    const y = d3.scaleLinear()
        .domain([0, d3.max(data, d => d.value) * 1.1])
        .nice()
        .range([height, 0]);

    svg.append("g")
        .call(d3.axisLeft(y));

    // Balken zeichnen
    svg.selectAll(".bar")
        .data(data)
        .join("rect")
        .attr("class", "bar")
        .attr("x", d => x(d.category))
        .attr("y", d => y(d.value))
        .attr("width", x.bandwidth())
        .attr("height", d => height - y(d.value))
        .attr("fill", "#4682B4");

    // Werte über den Balken anzeigen
    svg.selectAll(".bar-label")
        .data(data)
        .join("text")
        .attr("class", "bar-label")
        .attr("x", d => x(d.category) + x.bandwidth() / 2)
        .attr("y", d => y(d.value) - 5)
        .attr("text-anchor", "middle")
        .text(d => d.value)
        .attr("font-size", "12px");
}