
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
				'image/*': '1m',
				'text/css': '1m',
				'application/x-javascript': '1m'
			},
			next: {
				type: 'zuss',
				next: 'static'
			}
		}
	],

	'/example1/': '@example', // see /libraries/dispatched/example.js
	'/example2/': '/example/'  // see /libraries/includes/example.html
}
