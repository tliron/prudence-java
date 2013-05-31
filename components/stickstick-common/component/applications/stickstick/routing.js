
app.hosts = {
	'default': '/stickstick/'
}

app.routes = {
	'/*': [
		'manual',
		'textual',
		{type: 'zuss', next: [
			'static',
			{type: 'static', root: sincerity.container.getLibrariesFile('web')}]}
	],
	'/data/note/{id}/': '/data/note/',
	'/data/note/': 'hidden'
}
