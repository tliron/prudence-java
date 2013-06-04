
app.hosts = {
	'default': '/prudence-example/',
	internal: '/prudence-example/' // If not provided will default to the application subdirectory name
}

app.routes = {
	'/*': [
		'manual',
		{type: 'filter', library: '/filters/statistics/', next: 'textual'},
		{type: 'cacheControl', 'default': 10, mediaTypes: {'text/html': 15}, next:
			{type: 'javaScriptUnifyMinify', next:
				{type: 'zuss', next: [
					'static',
					{type: 'static', root: sincerity.container.getLibrariesFile('web')}]}}}
	],
	'/person/{id}/': {type: 'dispatch', id: 'person', dispatcher: 'javascript'},
	'/pythonperson/{id}/': {type: 'dispatch', id: 'person', dispatcher: 'python'},
	'/groovyperson/{id}/': {type: 'dispatch', id: 'person', dispatcher: 'groovy'},
	'/phpperson/{id}/': {type: 'dispatch', id: 'person', dispatcher: 'php'},
	'/rubyperson/{id}/': {type: 'dispatch', id: 'person', dispatcher: 'ruby'},
	'/clojureperson/{id}/': {type: 'dispatch', id: 'person', dispatcher: 'clojure'}
}

app.dispatchers = {
	javascript: {manual: '/prudence/dispatch/javascript/', library: '/resources/javascript/'},
	python: {manual: '/prudence/dispatch/python/', library: '/resources/python/'},
	ruby: {manual: '/prudence/dispatch/ruby/', library: '/resources/ruby/'},
	groovy: {manual: '/prudence/dispatch/groovy/', library: '/resources/groovy/'},
	clojure: {manual: '/prudence/dispatch/clojure/', library: '/resources/clojure/'},
	php: {manual: '/prudence/dispatch/php/', library: '/resources/php/'}
}

//
// Preheat
//

if (null !== executable.manager.getAdapterByTag('javscript')) {
	app.preheat.push('/textual/javascript/')
	app.preheat.push('/manual/javascript/')
	app.preheat.push('/person/1/')
}
if (null !== executable.manager.getAdapterByTag('jython')) {
	app.preheat.push('/textual/python/')
	app.preheat.push('/manual/python/')
	app.preheat.push('/pythonperson/1/')
}
if (null !== executable.manager.getAdapterByTag('groovy')) {
	app.preheat.push('/textual/groovy/')
	app.preheat.push('/manual/groovy/')
	app.preheat.push('/groovyperson/1/')
}
if (null !== executable.manager.getAdapterByTag('php')) {
	app.preheat.push('/textual/php/')
	app.preheat.push('/manual/php/')
	app.preheat.push('/phpperson/1/')
}
if (null !== executable.manager.getAdapterByTag('ruby')) {
	app.preheat.push('/textual/ruby/')
	app.preheat.push('/manual/ruby/')
	app.preheat.push('/rubyperson/1/')
}
if (null !== executable.manager.getAdapterByTag('clojure')) {
	app.preheat.push('/textual/clojure/')
	app.preheat.push('/manual/clojure/')
	app.preheat.push('/clojureperson/1/')
}
if (null !== executable.manager.getAdapterByTag('velocity')) {
	app.preheat.push('/textual/velocity/')
}
