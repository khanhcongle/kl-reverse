var template = `
<script src="https://code.highcharts.com/gantt/highcharts-gantt.js"></script>
<script src="https://code.highcharts.com/gantt/modules/accessibility.js"></script>

<figure class="highcharts-figure">
    <div id="container"></div>
    <p class="highcharts-description">
        Gantt chart demonstrating custom symbols in the data labels.
    </p>
</figure>

<script>
pm.getData((error, dataHolder) => {
    let data = dataHolder.response;
    let minDate = Math.min(...data.map(each => each.start));
    let maxDate = Math.max(...data.map(each => each.end));

    let categories = [...new Set(data.map(each => each.name))];
    data.forEach(each => {each.y = categories.indexOf(each.name)});

    let series = [{
            name: 'Project 1',
            data
        }];

    // THE CHART
    Highcharts.ganttChart('container', {
        title: {
            text: undefined,
            align: 'left'
        },

    subtitle: {
        text: undefined
    },
        xAxis: {
            min: minDate,
            max: maxDate,
            minPadding: 0.05,
            maxPadding: 0.05
        },

        yAxis: {
            categories
        },
    tooltip: {
        outside: true
    },
        accessibility: {
            point: {
                descriptionFormatter: function (point) {
                    var completedValue = point.completed ?
                            point.completed.amount || point.completed : null,
                        completed = completedValue ?
                            ' Task completed ' + Math.round(completedValue * 1000) / 10 + '%.' :
                            '';
                    return Highcharts.format(
                        '{point.yCategory}.{completed} Start {point.x:%Y-%m-%d}, end {point.x2:%Y-%m-%d}.',
                        { point, completed }
                    );
                }
            }
        },

        lang: {
            accessibility: {
                axis: {
                    xAxisDescriptionPlural: 'The chart has a two-part X axis showing time in both week numbers and days.'
                }
            }
        },

        series
    });
});

</script>

`;

// Set visualizer
pm.visualizer.set(template, {
    // Pass the response body parsed as JSON as `data`
    response: pm.response.json().map(each => {
        return {
            name: each.path,
            start: new Date(each.start).getTime(),
            end: new Date(each.start + each.time).getTime(),
            completed: 1
        }
    })
});