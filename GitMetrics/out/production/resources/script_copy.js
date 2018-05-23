
var dataset = {
    %DATASET%
}
var chart;

window.onload = function () {
    chart = new CanvasJS.Chart("chartContainer",
    {
    zoomEnabled: true, 
    data: [
        {
        type: "line",
        toolTipContent: "value:{y}<br/>{message}",
        dataPoints: dataset["LOC"]["total"]
        },
        {
        visible: false,
        toolTipContent: "value:{y}<br/>{message}",
        dataPoints: dataset["LOC"]["dkaznacheev"]
        }
    ],
    title: {
        text: "Lines of code"
    }
    });

    chart.render();
}

function selectChart (evt, name, label) {
    chart.options.data[0].dataPoints = dataset[name]["total"];
    chart.options.title.text = label;
    chart.render();
}
