
app.routes = {
	'/*': [
		'manual',
		'templates',
		{
			type: 'cacheControl',
			mediaTypes: {
				'image/*': '1m',
				'text/css': '1m',
				'application/x-javascript': '1m'
			},
			next: {
				type: 'less',
				next: 'static'
			}
		}
	],

	// Presenters
	'/user/{name}/comments/': '/user/comments/!',
	
	// Views
	'/views/*': '@jinja:{rr}',
	'/jinja-template/{id}/': '/jinja-template/!'
}

app.hosts = {
	'default': '/jinja/'
}

app.dispatchers = {
	jinja: {
		dispatcher: '/dispatchers/jinja/'
	}
}