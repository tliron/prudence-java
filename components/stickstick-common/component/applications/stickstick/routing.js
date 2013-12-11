
app.routes = {
	'/*': [
		'manual',
		'templates',
		{
			type: 'cacheControl',
			mediaTypes: {
				'image/*': 'farFuture',
				'text/css': 'farFuture',
				'application/x-javascript': 'farFuture'
			},
			next: {
				type: 'less',
				next: 'static'
			}
		}
	],

	'/data/note/{id}/': '/data/note/!'
}

app.hosts = {
	'default': '/stickstick/'
}
