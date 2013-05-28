
document.executeOnce('/sincerity/jvm/')
document.executeOnce('/sincerity/templates/')

var version = Sincerity.JVM.fromProperties(Sincerity.JVM.getResourceAsProperties('com/threecrickets/prudence/version.conf'))

component.context.attributes.put('com.threecrickets.prudence.version', version.version)
component.context.attributes.put('com.threecrickets.sincerity.version', sincerity.version.get('version'))

if (sincerity.verbosity >= 1) {
	println('Prudence {0} (Restlet {1} {2})'.cast(
		org.restlet.engine.Engine.VERSION,
		org.restlet.engine.Edition.CURRENT.shortName))
}
