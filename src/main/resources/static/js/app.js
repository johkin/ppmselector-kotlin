var margin = {top: 20, right: 50, bottom: 90, left: 50};
var width = 960 - margin.left - margin.right;
var height = 590 - margin.top - margin.bottom;

var svg = d3.select("#graph")

var initGraph = function () {

    graphContainer = svg
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
        .attr("id", "g-container")

    graphContainer.append("g")
        .attr("class", "x axis")
        .attr("id", "x-axis")
        .attr('transform', 'translate(0, ' + height + ')')

    var legendTop = height + margin.top + 30

    legend = svg
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + legendTop + ")")
        .attr("id", "legend")
}

var createHtmlTableCell = function (cellData, index) {
    if (cellData) {
        if (index == 1) {
            var text = cellData.fundName
            d3.select(this)
                .append("a")
                .attr("href", cellData.uri)
                .attr("target", "morningstar")
                .text(text)
        } else {
            d3.select(this)
                .text(cellData)
        }
    }
}

$(document).ready(function () {

    initGraph()

    d3.json("./api/selectedFunds", function (error, selectedFunds) {
        if (!error) {
            console.log("debug", selectedFunds);

            var tbody = d3.select("table#selectedFundsTable tbody")

            var rows = tbody.selectAll("tr")
                .data(selectedFunds)
                .enter()
                .append("tr")

            // create a cell in each row for each column
            var cells = rows.selectAll('td')
                .data(function (rowData, i) {
                    return [rowData.strategyDesc,
                        {"fundName": rowData.fundName, "uri": rowData.uri},
                        rowData.ppmNumber, rowData.selectedDate]
                })
                .enter()
                .append('td')
                .each(createHtmlTableCell);

        } else {
            console.log("error", error);
            // showError("Fel vid hämtning", "Kunde inte hämta kategorier och parametrar.", error)
        }
    });


})