
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
				'image/png': 'farFuture',
				'image/gif': 'farFuture',
				'image/jpeg': 'farFuture',
				'text/css': 'farFuture',
				'application/x-javascript': 'farFuture'
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
