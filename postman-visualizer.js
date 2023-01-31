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
	data.forEach(each => {
		each.y = categories.indexOf(each.name)
	});

	let series = [{
		name: 'Project 1',
		data,
		dataLabels: {
			allowOverlap: false,
			format: '<span style="font-weight: bold;">{point.label}</span><br/>'
		},
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
			max: maxDate
		},

		yAxis: {
			categories,
			labels: {
				align: 'left'
			}
		},
		tooltip: {
			pointFormat: '<span>{point.label}</span><br><span>S: {point.start:%H:%M:%S.%L}</span><br/><span>E: {point.end:%H:%M:%S.%L}</span>',
			outside: true
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
            label: `${each.time} ms`,
            start: new Date(each.start).getTime(),
            end: new Date(each.start + each.time).getTime()
        }
    })
});