
/*
 * Joins this node to the "application" cluster. 
 */

document.require('/sincerity/container/')

var config = new Config()

config.instanceName = 'com.threecrickets.prudence.application'
config.groupConfig.name = 'com.threecrickets.prudence.application'
config.groupConfig.password = 'prudence'

try {
	importClass(org.apache.log4j.Logger)
	config.setProperty('hazelcast.logging.type', 'log4j')
}
catch(x) {}
	
Sincerity.Container.executeAll('.', ['default.js'])

Hazelcast.newHazelcastInstance(config)
