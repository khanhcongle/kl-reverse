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
        time: {
            useUTC: false
        },
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
			pointFormat: \`<span>{point.label}</span>
                            <br><span>path: {point.name}</span>
                            <br><span>query: {point.query}</span>
                            <br><span>start: {point.start:%Y-%m-%dT%H:%M:%S.%L}</span>
                            <br/><p>time: {point.time} (ms)</p>
                            <br/><p>payload: {point.payload}</p>
                        \`,
			outside: true
		},
		series
	});
});
</script>

`;

function formatPayload (contentType, payload) {
    if(!contentType) {
        return payload;
    }
    if(contentType.includes('application/x-www-form-urlencoded')) {
        return decodeURIComponent(payload).split("&").join('<br/>');
    }
    return payload;
}

// Set visualizer
pm.visualizer.set(template, {
    // Pass the response body parsed as JSON as `data`
    response: pm.response.json().map(each => {
        return {
            name: each.path,
            label: `${each.time} ms`,
            start: each.start,
            end: each.start + each.time,
            time: each.time,
            query: each.query,
            payload: formatPayload(each.contentType, each.payload)
        }
    })
});