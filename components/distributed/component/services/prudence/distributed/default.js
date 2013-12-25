
//
// Configures Hazelcast using either the "/configuration/hazelcast/" script
// or the "/configuration/hazelcast.conf" file.
//

document.require('/sincerity/container/')

// Try "/configuration/hazelcast.conf" if it exists
var configFile = sincerity.container.getConfigurationFile('hazelcast.conf')
if (configFile.exists()) {
	com.hazelcast.core.Hazelcast.newHazelcastInstance(new com.hazelcast.config.FileSystemXmlConfig(configFile))
}
else {
	// Configuration by script
	importClass(
		com.hazelcast.core.Hazelcast,
		com.hazelcast.client.HazelcastClient)

	importPackage(com.hazelcast.config)
	importPackage(com.hazelcast.client.config)

	Sincerity.Container.executeAll(sincerity.container.getConfigurationFile('hazelcast'))
}
