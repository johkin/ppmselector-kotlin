var margin = {top: 20, right: 50, bottom: 90, left: 50};
var width = 960 - margin.left - margin.right;
var height = 590 - margin.top - margin.bottom;

var dateParser = d3.timeParse("%Y-%m-%d");

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

    graphContainer.append("g")
        .attr("class", "y axis")
        .attr("id", "y-axis")

    var legendTop = height + margin.top + 30

    legend = svg
        .append("g")
        .attr("transform", "translate(" + margin.left + "," + legendTop + ")")
        .attr("id", "legend")
}

var x = d3.scaleTime()
    .range([0, width]);

var y = d3.scaleLinear()
    .range([height, 0]);

var colorScale = d3.scaleOrdinal()
    .range(d3.schemeCategory20)

var xAxis = d3.axisBottom()
    .scale(x)
    .ticks(5)
    .tickFormat(d3.timeFormat('%Y-%m-%d'))

var drawline = function (yRef) {
    return d3.line()
        .x(function (d) {
            return x(d.date)
        })
        .y(function (d) {
            return yRef(d.value)
        })
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

function createTable() {
    d3.json("./api/selectedFunds", function (error, selectedFunds) {
        if (!error) {
            console.log("debug", selectedFunds)

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
                .each(createHtmlTableCell)

        } else {
            console.log("error", error)
            // showError("Fel vid hämtning", "Kunde inte hämta kategorier och parametrar.", error)
        }
    })
}

function createXAxis(transactions) {
    var dates = []

    d3.map(transactions).each(function (arr, key) {
        console.log("debug", "key: " + key)
        var buyDates = d3.extent(arr, function (t) {
            return dateParser(t.buyDate)
        })
        var sellDates = d3.extent(arr, function (t) {
            return dateParser(t.sellDate)
        })
        dates.push(buyDates[0])
        dates.push(sellDates[1])
    })
    dates = d3.extent(dates)

    x.domain(dates)

    d3.select("#x-axis")
        .call(d3.axisBottom(x));
}

var line = d3.line()
    .x(function(d) { return x(d.date); })
    .y(function(d) { return y(d.value); });

function createGraph() {
    d3.json("./api/transactions", function (error, transactions) {
        if (!error) {
            console.log("debug", transactions)

            createXAxis(transactions);

            var values = []

            d3.map(transactions).each(function (arr, key) {
                var previous = 100
                arr.forEach(function(t) {
                    t.value = previous
                    previous+= t.returnPercent
                    values.push(t.value)
                })
            })
            y.domain(d3.extent(values))

            d3.select("#y-axis")
                .call(d3.axisLeft(y))

            var data = []

            d3.map(transactions).each(function (arr, key) {
                values = []
                arr.forEach(function (v) {
                    values.push({date: dateParser(v.buyDate), value: v.value})
                })
                data.push({strategy: key,  values: values})
            })

            var strategy = graphContainer.selectAll(".strategy")
                .data(data)
                .enter().append("g")
                .attr("class", "strategy");

            strategy.append("path")
                .attr("class", "line")
                .attr("d", function(d) {
                    return line(d.values);
                })
                .style("stroke", function(d) {
                    return colorScale(d.strategy);
                });


        } else {
            console.log("error", error)
            // showError("Fel vid hämtning", "Kunde inte hämta kategorier och parametrar.", error)
        }
    })
}

$(document).ready(function () {

    initGraph()

    createTable()

    createGraph()

})