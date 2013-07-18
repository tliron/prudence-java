
app.hosts = {
	'default': '/stickstick/'
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

	'/data/note/{id}/': '/data/note/!'
}
