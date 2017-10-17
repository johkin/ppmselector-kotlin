var margin = {top: 20, right: 50, bottom: 90, left: 50};
var width = 960 - margin.left - margin.right;
var height = 590 - margin.top - margin.bottom;

var dateParser = d3.timeParse("%Y-%m-%d");
var formatDate = d3.timeFormat("%Y-%m-%d");

var legend = null

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
    .range(d3.schemeCategory20);

var xAxis = d3.axisBottom()
    .scale(x)
    .ticks(5)
    .tickFormat(d3.timeFormat('%Y-%m-%d'))

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
    .x(function (d) {
        return x(d.date);
    })
    .y(function (d) {
        return y(d.value);
    });

var createToolTipContents = function (value) {

    var contents = "Datum: " + formatDate(new Date(value.buyDate)) + "<br/>"
    contents += "<b>Fond: " + value.fundName + "</b><br>"
    contents += "<b>Utveckling: " + value.returnPercent + "%</b>"


    return contents
}

var div = d3.select("#tooltip");


function createGraph() {
    d3.json("./api/transactions", function (error, transactions) {
        if (!error) {
            console.log("debug", "transactions: ")
            console.log("debug", transactions)

            createXAxis(transactions);

            var values = []

            d3.map(transactions).each(function (arr, key) {
                var previous = 100
                arr.forEach(function (t) {
                    t.date = dateParser(t.buyDate)
                    t.value = previous
                    previous += t.returnPercent
                    values.push(t.value)
                })
            })
            y.domain(d3.extent(values))

            d3.select("#y-axis")
                .call(d3.axisLeft(y))

            var data = []

            d3.map(transactions).each(function (arr, key) {
                data.push({strategy: key, values: arr})
            })

            console.log("debug", "data: ")
            console.log("debug", data)

            var strategy = graphContainer.selectAll(".strategy")
                .data(data)
                .enter().append("g")
                .attr("class", "strategy");

            strategy.append("path")
                .attr("class", "line")
                .attr("d", function (d) {
                    return line(d.values);
                })
                .style("stroke", function (d) {
                    return colorScale(d.strategy);
                });

            data.forEach(function (o) {
                o.values.forEach(function (v) {

                    graphContainer.append("circle")
                        .attr('cx', function () {
                            return x(v.date);
                        })
                        .attr('cy', function () {
                            return y(v.value);
                        })
                        .attr('fill', function () {
                            return colorScale(o.strategy)
                        })
                        .attr('r', 4)
                        .on("click", function () {
                            d3.event.stopPropagation();
                            graphContainer.selectAll("circle")
                                .transition()
                                .attr("r", 4)

                            div.transition()
                                .duration(200)
                                .style("opacity", .8)
                                .style("left", (d3.event.pageX + 10) + "px")
                                .style("top", (d3.event.pageY + 10) + "px")


                            div.html(createToolTipContents(v))
                            d3.select(this)
                                .transition()
                                .attr("r", 6);
                        })
                })
            })


        } else {
            console.log("error", error)
            // showError("Fel vid hämtning", "Kunde inte hämta kategorier och parametrar.", error)
        }
    })
}

var createLegend = function () {
    d3.json("./api/strategies", function (error, strategies) {

        var columns = 5
        var legendWidth = width / columns
        var legendHeight = 20

        var index = 0

        d3.map(strategies).each(function (description, key) {

            var columnIndex = index - (Math.floor(index / columns) * columns)
            var xTranslation = columnIndex * legendWidth
            var yTranslation = Math.floor(index / columns) * legendHeight


            var legendGroup = legend.append("g")
                .classed("legendGroup", true)
                .attr("id", "legend-" + key)
                .attr("transform", "translate(" + xTranslation + ", " + yTranslation + ")")

            legendGroup.append("circle")
                .attr("cx", 4)
                .attr("cy", 4)
                .attr("r", 6)
                .attr("fill", colorScale(key))

            legendGroup.append("text")
                .attr("x", 15)
                .attr("y", 8)
                .attr("color", "black")
                .text(description)

            index++

        })
    })
}

$(document).ready(function () {

    initGraph()

    createTable()

    createGraph()

    createLegend()

})