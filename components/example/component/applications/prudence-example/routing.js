
app.hosts = {
	'default': '/prudence-example/'
}

app.routes = {
	'/*': [
		'manual',
		{
			type: 'filter',
			library: '/filters/statistics/',
			next: 'scriptlet'
		},
		{
			type: 'cacheControl',
			mediaTypes: {
				'image/*': 'farFuture',
				'text/css': 'farFuture',
				'application/x-javascript': 'farFuture'
			},
			next: {
				type: 'zuss',
				next: 'static'
			}
		}
	],

	'/person/{id}/':        'person',
	'/pythonperson/{id}/':  'python:person',
	'/rubyperson/{id}/':    'ruby:person',
	'/phpperson/{id}/':     'php:person',
	'/luaperson/{id}/':     'lua:person',
	'/groovyperson/{id}/':  'groovy:person',
	'/clojureperson/{id}/': 'clojure:person'
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
if (null !== executable.manager.getAdapterByTag('ruby')) {
	app.preheat.push('/scriptlet/ruby/')
	app.preheat.push('/manual/ruby/')
	app.preheat.push('/rubyperson/1/')
}
if (null !== executable.manager.getAdapterByTag('php')) {
	app.preheat.push('/scriptlet/php/')
	app.preheat.push('/manual/php/')
	app.preheat.push('/phpperson/1/')
}
if (null !== executable.manager.getAdapterByTag('lua')) {
	app.preheat.push('/scriptlet/lua/')
	app.preheat.push('/manual/lua/')
	app.preheat.push('/luaperson/1/')
}
if (null !== executable.manager.getAdapterByTag('groovy')) {
	app.preheat.push('/scriptlet/groovy/')
	app.preheat.push('/manual/groovy/')
	app.preheat.push('/groovyperson/1/')
}
if (null !== executable.manager.getAdapterByTag('clojure')) {
	app.preheat.push('/scriptlet/clojure/')
	app.preheat.push('/manual/clojure/')
	app.preheat.push('/clojureperson/1/')
}
if (null !== executable.manager.getAdapterByTag('velocity')) {
	app.preheat.push('/scriptlet/velocity/')
}
