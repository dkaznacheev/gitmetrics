var names = {
    %NAMETABLE%
}

function mouseOut(id) {
    if (document.getElementById(id).value == "Off") {
        document.getElementById(id).style.backgroundColor='inherit';
    }
}

function mouseOver(id) {
    if (document.getElementById(id).value == "Off") {
        document.getElementById(id).style.backgroundColor=getColor(names[id]);
        console.log(id);
        console.log(getColor(names[id]));
        console.log(names[id]);
    }
}

function stringToColor(name) {
    var res = 0;
    for (var i = 0; i < name.length; i++) {
        res += 239 * name.charCodeAt(i);
    }
    return res * 31 * 257;
}

function getColor(name) {
    var color =  '#'+(stringToColor(name)&0xFFFFFF<<0).toString(16);
    return color;
}

function getHalfColor(name) {
    var color = getColor(name);
    var result = "#";
    for (var i = 1; i < 7; i++) {
        result = result + (Math.floor(parseInt(color.charAt(i), 16) / 2)).toString();
    }
    return result;
}

var dataset = {
    %DATASET%
}
var currentMetric = "LOC";
var chart;
var set;

window.onload = function () {
    set = new Set();
    chart = new CanvasJS.Chart("chartContainer",
    {
    zoomEnabled: true, 
    data: [
        %CHARTS%
    ],
    title: {
        text: %METRICNAME%
    }
    });

    chart.render();
}


function selectChart (evt, name, label) {
    currentMetric = name;
    for (var i = 0; i < chart.options.data.length; i++) {
        chart.options.data[i].dataPoints = dataset[name][chart.options.data[i].name];
    }
    chart.options.title.text = label;
    chart.render();
}

function refreshColors(name, toAdd) {
    var totalSet = dataset[currentMetric]["total"];
    var committerSet = dataset[currentMetric][name];
    var ind = [];
    for (var i = 0; i < committerSet.length; i++) {
        ind.push(committerSet[i].x);
    }
    var j = 0;
    for (var i = 0; i < totalSet.length; i++) {
        if (totalSet[i].x == ind[j]) {
            j++;
            if (toAdd)
                totalSet[i].color = getColor(name);
            else 
                delete totalSet[i].color;
        }
        if (j >= ind.length) {
            break;
        }
    }
    dataset[currentMetric]["total"] = totalSet;
}

function onCommitterClick(id){
    var name = names[id];
    if(document.getElementById(id).value == "Off") {
        document.getElementById(id).style.backgroundColor=getHalfColor(name);        
        document.getElementById(id).value="On";
        refreshColors(name, true);
        for (var i = 0; i < chart.options.data.length; i++) {
            if (chart.options.data[i].name == name) {
                chart.options.data[i].visible = true;
                chart.options.data[i].color = getColor(name);
            }
        }
        chart.render();
    } else {
        document.getElementById(id).value="Off";
        document.getElementById(id).style.backgroundColor=getColor(name);
        refreshColors(name, false);
        for (var i = 0; i < chart.options.data.length; i++) {
            if (chart.options.data[i].name == name) {
                chart.options.data[i].visible = false;
                chart.options.data[i].color = getColor(name);
            }
        }
        chart.render();
    }
}