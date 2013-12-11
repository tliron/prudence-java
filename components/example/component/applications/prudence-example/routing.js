
app.routes = {
	'/*': [
		'manual',
		{
			type: 'filter',
			library: '/filters/statistics/',
			next: 'templates'
		},
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

	'/person/{id}/':        '@person',
	'/pythonperson/{id}/':  '@python:person',
	'/rubyperson/{id}/':    '@ruby:person',
	'/phpperson/{id}/':     '@php:person',
	'/luaperson/{id}/':     '@lua:person',
	'/groovyperson/{id}/':  '@groovy:person',
	'/clojureperson/{id}/': '@clojure:person'
}

app.hosts = {
	'default': '/prudence-example/'
}

app.dispatchers = {
	javascript: '/dispatched/javascript/',
	python:     '/dispatched/python/',
	ruby:       '/dispatched/ruby/',
	php:        '/dispatched/php/',
	lua:        '/dispatched/lua/',
	groovy:     '/dispatched/groovy/',
	clojure:    '/dispatched/clojure/'
}

//
// Preheat
//

if (null !== executable.languageManager.getAdapterByTag('javscript')) {
	app.preheat.push('/templates/javascript/')
	app.preheat.push('/manual/javascript/')
	app.preheat.push('/person/1/')
}
if (null !== executable.languageManager.getAdapterByTag('jython')) {
	app.preheat.push('/templates/python/')
	app.preheat.push('/manual/python/')
	app.preheat.push('/pythonperson/1/')
}
if (null !== executable.languageManager.getAdapterByTag('ruby')) {
	app.preheat.push('/templates/ruby/')
	app.preheat.push('/manual/ruby/')
	app.preheat.push('/rubyperson/1/')
}
if (null !== executable.languageManager.getAdapterByTag('php')) {
	app.preheat.push('/templates/php/')
	app.preheat.push('/manual/php/')
	app.preheat.push('/phpperson/1/')
}
if (null !== executable.languageManager.getAdapterByTag('lua')) {
	app.preheat.push('/templates/lua/')
	app.preheat.push('/manual/lua/')
	app.preheat.push('/luaperson/1/')
}
if (null !== executable.languageManager.getAdapterByTag('groovy')) {
	app.preheat.push('/templates/groovy/')
	app.preheat.push('/manual/groovy/')
	app.preheat.push('/groovyperson/1/')
}
if (null !== executable.languageManager.getAdapterByTag('clojure')) {
	app.preheat.push('/templates/clojure/')
	app.preheat.push('/manual/clojure/')
	app.preheat.push('/clojureperson/1/')
}
if (null !== executable.languageManager.getAdapterByTag('velocity')) {
	app.preheat.push('/templates/velocity/')
}
