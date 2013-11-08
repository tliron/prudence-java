
//
// Allows access to the Prudence version as a component attribute.
//

document.require(
	'/sincerity/jvm/',
	'/sincerity/templates/')

var version = Sincerity.JVM.fromProperties(Sincerity.JVM.getResourceAsProperties('com/threecrickets/prudence/version.conf'))

component.context.attributes.put('com.threecrickets.prudence.version', version.version)

if (sincerity.verbosity >= 1) {
	var adapter = executable.context.adapter.attributes
	println('Prudence {prudence.version} (Restlet {restlet.edition} {restlet.version}, Jetty {jetty.version}, {language.name} {language.version})'.cast({
		'prudence.version': version.version,
		'restlet.edition': org.restlet.engine.Edition.CURRENT.shortName,
		'restlet.version': org.restlet.engine.Engine.VERSION,
		'jetty.version': Sincerity.JVM.getClass('org.eclipse.jetty.server.Server').package.implementationVersion,
		'language.name': adapter.get('name'),
		'language.version': adapter.get('version')}))
}
