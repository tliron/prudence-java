
/*
 * Joins this node to the "application" cluster. 
 */

document.require('/sincerity/container/')

var config = new Config()

config.instanceName = 'com.threecrickets.prudence.application'
config.groupConfig.name = 'com.threecrickets.prudence.application'
config.groupConfig.password = 'prudence'

try {
	importClass(org.slf4j.Logger)
	config.setProperty('hazelcast.logging.type', 'slf4j')
}
catch(x) {}
	
Sincerity.Container.executeAll('.', ['default.js'])

Hazelcast.newHazelcastInstance(config)
