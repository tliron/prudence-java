
app.hosts = {
	'default': '/${APPLICATION}/',
	internal: '/${APPLICATION}/' // If not provided will default to the application subdirectory name
}

app.routes = {
	'/*': [
		'manual',
		'textual',
		// For our static files we'll cache all images on the client for the far future, and enable on-the-fly ZUSS support and JavaScript compression:
		{type: 'cacheControl', 'default': -1, mediaTypes: {'image/png': 'farFuture', 'image/jpeg': 'farFuture', 'image/gif': 'farFuture'}, next:
			{type: 'javaScriptUnifyMinify', next:
				{type: 'zuss', next: [
					'static',
					{type: 'static', root: sincerity.container.getLibrariesFile('web')}]}}}
	],
	// A sample dispatch resource, see /libraries/resources/sample.js:
	'/sample/': '#sample'
}

// See /libraries/resources/default.js:
app.dispatchers = {
	javascript: {library: '/resources/'}
}
