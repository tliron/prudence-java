
/*
 * Joins this node to the "application" cluster. 
 */

document.require('/sincerity/container/')

var config = new Config()

config.instanceName = 'com.threecrickets.prudence.default'
config.groupConfig.name = 'com.threecrickets.prudence.default'
config.groupConfig.password = 'prudence'

// A comma-separated list of tags for this member
config.memberAttributeConfig.setStringAttribute('com.threecrickets.prudence.tags', 'default')

if (null == Hazelcast.getHazelcastInstanceByName(config.instanceName)) { // avoid creating it more than once

try {
	importClass(org.slf4j.Logger)
	config.setProperty('hazelcast.logging.type', 'slf4j')
}
catch(x) {}

Sincerity.Container.executeAll('.', ['default.js'])

Hazelcast.newHazelcastInstance(config)

}