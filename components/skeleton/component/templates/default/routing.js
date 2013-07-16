
app.hosts = {
	'default': '/${APPLICATION}/',
	internal: '/${APPLICATION}/' // If not provided will default to the application subdirectory name
}

app.routes = {
	'/*': [
		'manual',
		'scriptlet',
		// For our static files we'll cache all images on the client for the far future, and enable on-the-fly ZUSS support and JavaScript compression:
		{type: 'cacheControl', mediaTypes: {'image/png': 'farFuture', 'image/jpeg': 'farFuture', 'image/gif': 'farFuture'}, next:
			{type: 'javaScriptUnifyMinify', next:
				{type: 'zuss', next: 'static'}}}
	],
	// Dispatched resource
	'/sample1/': '@sample', // see /libraries/manual-resources/sample.js
	// Captured resource
	'/sample2/': '/sample/'  // see /libraries/scriptlet-resources/sample.html
}

app.dispatchers = {
	javascript: '/manual-resources/'
}
