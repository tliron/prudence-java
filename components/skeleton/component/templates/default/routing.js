
app.hosts = {
	'default': '/${APPLICATION}/'
}

app.routes = {
	'/*': [
		'manual',
		'scriptlet',
		{
			type: 'cacheControl',
			mediaTypes: {
				'image/png': '5m',
				'image/gif': '5m',
				'image/jpeg': '5m',
				'text/css': '5m',
				'application/x-javascript': '5m'
			},
			next: {
				type: 'zuss',
				next: 'static'
			}
		}
	],

	'/sample1/': '@sample', // see /libraries/manual-resources/sample.js
	'/sample2/': '/sample/'  // see /libraries/scriptlet-resources/sample.html
}

app.dispatchers = {
	javascript: '/manual-resources/'
}
