
app.hosts = {
	'default': '/prudence-example/',
	internal: '/prudence-example/' // If not provided will default to the application subdirectory name
}

app.routes = {
	'/*': [
		'manual',
		{type: 'filter', library: '/filters/statistics/', next: 'scriptlet'},
		{type: 'cacheControl', 'default': 10, mediaTypes: {'text/html': 15}, next:
			{type: 'javaScriptUnifyMinify', next:
				{type: 'zuss', next: [
					'static',
					{type: 'static', root: sincerity.container.getLibrariesFile('web')}]}}}
	],
	'/person/{id}/':        '@person',
	'/pythonperson/{id}/':  '@python:person',
	'/groovyperson/{id}/':  '@groovy:person',
	'/phpperson/{id}/':     '@php:person',
	'/rubyperson/{id}/':    '@ruby:person',
	'/clojureperson/{id}/': '@clojure:person'
}

app.dispatchers = {
	javascript: '/manual-resources/javascript/',
	python:     '/manual-resources/python/',
	ruby:       '/manual-resources/ruby/',
	groovy:     '/manual-resources/groovy/',
	clojure:    '/manual-resources/clojure/',
	php:        '/manual-resources/php/'
}

//
// Preheat
//

if (null !== executable.manager.getAdapterByTag('javscript')) {
	app.preheat.push('/scriptlet/javascript/')
	app.preheat.push('/manual/javascript/')
	app.preheat.push('/person/1/')
}
if (null !== executable.manager.getAdapterByTag('jython')) {
	app.preheat.push('/scriptlet/python/')
	app.preheat.push('/manual/python/')
	app.preheat.push('/pythonperson/1/')
}
if (null !== executable.manager.getAdapterByTag('groovy')) {
	app.preheat.push('/scriptlet/groovy/')
	app.preheat.push('/manual/groovy/')
	app.preheat.push('/groovyperson/1/')
}
if (null !== executable.manager.getAdapterByTag('php')) {
	app.preheat.push('/scriptlet/php/')
	app.preheat.push('/manual/php/')
	app.preheat.push('/phpperson/1/')
}
if (null !== executable.manager.getAdapterByTag('ruby')) {
	app.preheat.push('/scriptlet/ruby/')
	app.preheat.push('/manual/ruby/')
	app.preheat.push('/rubyperson/1/')
}
if (null !== executable.manager.getAdapterByTag('clojure')) {
	app.preheat.push('/scriptlet/clojure/')
	app.preheat.push('/manual/clojure/')
	app.preheat.push('/clojureperson/1/')
}
if (null !== executable.manager.getAdapterByTag('velocity')) {
	app.preheat.push('/scriptlet/velocity/')
}
