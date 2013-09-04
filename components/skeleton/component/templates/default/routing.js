
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

	'/example1/': '@example', // see /libraries/manual-resources/example.js
	'/example2/': '/example/'  // see /libraries/scriptlet-resources/example.html
}

app.dispatchers = {
	javascript: '/manual-resources/'
}
