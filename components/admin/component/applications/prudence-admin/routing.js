
app.hosts = {
	'default': '/',
	internal: '/prudence-admin/' // If not provided will default to the application subdirectory name
}

app.routes = {
	'/*': [
		'manual',
		'textual',
		{type: 'javaScriptUnifyMinify', next:
			{type: 'zuss', next: [
				'static',
				{type: 'static', root: sincerity.container.getLibrariesFile('web')}]}}
	]
}
