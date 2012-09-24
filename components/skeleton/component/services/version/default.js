
document.executeOnce('/sincerity/jvm/')
document.executeOnce('/sincerity/templates/')

var version = Sincerity.JVM.fromProperties(Sincerity.JVM.getResourceAsProperties('com/threecrickets/prudence/version.conf'))

component.context.attributes.put('com.threecrickets.prudence.version', version.version)

println('Prudence {0} (Restlet {1} {2})'.cast(version.version, org.restlet.engine.Engine.VERSION, org.restlet.engine.Edition.CURRENT.shortName))
