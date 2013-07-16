
app.hosts = {
	'default': '/stickstick/'
}

app.routes = {
	'/*': [
		'manual',
		'scriptlet',
		{type: 'zuss', next: 'static'}],
	'/data/note/{id}/': '/data/note/!'
}
