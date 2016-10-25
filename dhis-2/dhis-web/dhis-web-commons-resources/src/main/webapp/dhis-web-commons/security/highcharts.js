/**
 * Created by HISP-WS11 on 7/19/2016.
 */


//apiChart1="../../api/analytics.json?dimension=dx:VG82v0dFGeP;sgA1v7o6JL3&dimension=ou:GY9dLYENyOU&filter=pe:2012;2015;2013&displayProperty=NAME&outputIdScheme=CODE"

function filterParams(){

    var query = decodeURIComponent(window.location.search);
    //var query = decodeURIComponent("http://180.151.233.59:20092/test/newtab.html?arr=[{%22id%22:%22;OFhn35sUfSB;A049qT5clp5;qT2W96IdBC7;AWDDPPMOl2U%22,%22name%22:%22;Health%20worker%20density%20and%20distribution;Output%20training%20institutions;Headcount%20ratio%20of%20catastrophic%20health%20expenditure;Adolescents%2010-19%20yrs%20(%)%22}]");
    //var query = decodeURIComponent("http://180.151.233.59:20092/test/newtab.html?arr=[{%22id%22:%22;OFhn35sUfSB;A049qT5clp5;qT2W96IdBC7;AWDDPPMOl2U%22,%22name%22:%22;Health%20worker%20density%20and%20distribution;Output%20training%20institutions;Headcount%20ratio%20of%20catastrophic%20health%20expenditure;Adolescents%2010-19%20yrs%20(%)%22}]&peid=;2015;2013;2012;2011&orgid=;sCU6B2ZOTYr;GY9dLYENyOU;AFkkfLBwuHc;sQeCnfgIko4&orgname=BangladeshBhutanDPR%20KoreaIndia");
    console.log("query"+ query);
    query = query.substring(1);
    var params = query.split("&");
    for (var i=0;i<params.length;i++) {
        var param = params[i].split("=");
        switch(param[0]) {
            case "arr":
                arr = JSON.parse(param[1]);

                break;
            case "peid":
                peid = param[1];
                break;
            case "orgid":
                orgid = param[1];
                break;
            case "orgname":
                orgname = param[1];
                break;
        }
    }
}

function validateDashboard() {

    myFunction(orgname, orgid, peid);

}


function myFunction(orgname, orgid, peid) {

    var tempOrgUnitText = getNamebyId(tempOrgUnitUid, organisationUnits);

    calcLastSixMonths(tempYearUid,tempMonthUid);

    generateApiChart1(tempOrgUnitUid, tempYearUid);




    //var titleDe = getNamebyId(tempDataElementUid, chartDataGroup);

    createChart1({
        chartId: "chart1",
        //  title: titleDe,
        subtitle: tempOrgUnitText
    }, tempOrgUnitUid, tempMonthUid, tempYearUid);

}

function createChart1(chartInfo, tempOrgUnitUid, tempMonthUid, tempYearUid) {
    // var count = tempMonthUid;
    var count = 6;
    $.getJSON(apiChart1, function (jsonRes) {
        var json = standardlizeData_1(jsonRes, "dx");
        var chartData = preparingDataforChart(json, count, "dx");

        createBarCharts1(chartInfo.chartId, chartInfo.subtitle, chartData.xaxis, chartData.series);
    });
}

Highcharts.theme = {
    colors: ['#058DC7', '#50B432', '#ED561B', '#DDDF00', '#24CBE5', '#64E572', '#FF9655', '#FFF263', '#6AF9C4'],
    chart: {
        backgroundColor: {
            linearGradient: { x1: 0, y1: 0, x2: 1, y2: 1 },
            stops: [
                [0, 'rgb(255, 255, 255)'],
                [1, 'rgb(240, 240, 255)']
            ]
        },
        //borderWidth: 2,
        plotBackgroundColor: 'rgba(255, 255, 255, .9)'
        //plotShadow: true,
        //plotBorderWidth: 1
    },
    title: {
        style: {
            color: '#000',
            font: 'bold 16px "Trebuchet MS", Verdana, sans-serif'
        }
    },
    subtitle: {
        style: {
            color: '#666666',
            font: 'bold 12px "Trebuchet MS", Verdana, sans-serif'
        }
    },
    tooltip: {
        borderWidth: 0,
        backgroundColor: 'rgba(219,219,216,0.8)',
        shadow: false
    },
    legend: {
        itemStyle: {
            fontWeight: 'bold',
            fontSize: '13px'
        }
    },
    xAxis: {
        gridLineWidth: 0,

        lineColor: '#000',
        // tickColor: '#000',
        labels: {
            style: {
                color: '#000',
                font: '11px Trebuchet MS, Verdana, sans-serif'
            }
        },
        title: {
            style: {
                color: '#333',
                fontWeight: 'bold',
                fontSize: '12px',
                fontFamily: 'Trebuchet MS, Verdana, sans-serif'

            }
        }
    },
    yAxis: {
        minorTickInterval: 'auto',
        lineColor: '#000',
        gridLineWidth: 0,
        minorGridLineWidth:0,
        lineWidth: 1,
        labels: {
            style: {
                color: '#000',
                font: '11px Trebuchet MS, Verdana, sans-serif'
            }
        },
        title: {
            style: {
                color: '#333',
                fontWeight: 'bold',
                fontSize: '12px',
                fontFamily: 'Trebuchet MS, Verdana, sans-serif'
            }
        }
    },
    plotOptions: {
        candlestick: {
            lineColor: '#404048'
        }
    },
    legend: {
        itemStyle: {
            font: '9pt Trebuchet MS, Verdana, sans-serif',
            color: 'black'

        },
        itemHoverStyle: {
            color: '#039'
        },
        itemHiddenStyle: {
            color: 'gray'
        }
    },
    labels: {
        style: {
            color: '#99b'
        }
    },


    // General
    background2: '#F0F0EA'

};

// Apply the theme
Highcharts.setOptions(Highcharts.theme);







function createBarCharts1(view, subtitleStr, xAxisCategoriesArr, dataSeriesArrO) {
    $("#" + view).highcharts({
        chart: {
            type: 'column'
        },
        title: {
            text: "ANC Registration and Early ANC Registration"
        },
        subtitle: {
            text: subtitleStr
        },
        xAxis: {
            categories: xAxisCategoriesArr,
            crosshair: true
        },
        yAxis: {
            min: 0,
            title: {
                text: ''
            }
        },

        tooltip: {
            headerFormat: '<span style="font-size:10px">{point.key}</span><table>',
            pointFormat: '<tr><td style="color:{series.color};padding:0">{series.name}: </td>' +
            '<td style="padding:0"><b>{point.y:.1f}</b></td></tr>',
            footerFormat: '</table>',
            shared: true,
            useHTML: true
        },
        plotOptions: {


            column: {
                pointPadding: 0.2,
                borderWidth: 0,
                dataLabels: {
                    enabled: true,
                    style: {
                        fontWeight: 'bold',
                        fontSize:"14px"
                    }
                }
            }
        },

        series: dataSeriesArrO
    });
}
function preparingDataforChart(json, count, secondKey) {

    var peArr = json.metaData.pe;
    var facilityArr = json.metaData[secondKey];
    var dataResult = sortDataRowsbyPeArr_1(json, secondKey);


    var xAxisCategoriesArr = [];
    for (var i = 0; i < peArr.length; i++) {
        xAxisCategoriesArr.push(json.metaData.names[peArr[i]])
        if (xAxisCategoriesArr.length >= count) break;
    }
    var dataSeriesArrO = [];

    for (var i = 0; i < facilityArr.length; i++) {
        var name = json.metaData.names[facilityArr[i]];
        var data = [];
        for (var j = 0; j < dataResult.length; j++) {
            if (dataResult[j][0] == facilityArr[i]) {
                data.push(Number(dataResult[j][2]));
                if (data.length >= count) break;
            }
        }
        var seriO = {
            name: name,
            data: data
        }
        dataSeriesArrO.push(seriO);
    }

    return {xaxis: xAxisCategoriesArr, series: dataSeriesArrO};
}


