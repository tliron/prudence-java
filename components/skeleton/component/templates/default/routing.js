
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
				'image/png': '1m',
				'image/gif': '1m',
				'image/jpeg': '1m',
				'text/css': '1m',
				'application/x-javascript': '1m'
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
