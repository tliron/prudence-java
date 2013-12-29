
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
	'/views/*': '@st:{rr}'	
}

app.hosts = {
	'default': '/st/'
}

app.dispatchers = {
	st: {
		dispatcher: '/dispatchers/st/'
	}
}
