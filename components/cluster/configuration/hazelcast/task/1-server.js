
/*
 * Enable this if you want this node to join as a full member of the "task"
 * cluster, which means it will be able to accept and run distributed tasks in
 * Prudence via the application.distributedExecuteTask and
 * application.distributedCodeTask APIs.
 * 
 * Without this, the APIs would still work, but would be spawned within the
 * "default" cluster.
 * 
 * For the other nodes that are to spawn tasks in this cluster, enable the 
 * "2-client.js" file.
 * 
 * It doesn't make sense to have both "1-server.js" and "2-client.js" enabled
 * on the same node: a server is already a full member, and doesn't have to
 * also be a client. 
 */

/*
var config = new Config()

config.instanceName = 'com.threecrickets.prudence.task'
config.groupConfig.name = 'com.threecrickets.prudence.task'
config.groupConfig.password = 'prudence'

// A comma-separated list of tags for this member
config.memberAttributeConfig.setStringAttribute('com.threecrickets.prudence.tags', 'default')

try {
	importClass(org.slf4j.Logger)
	config.setProperty('hazelcast.logging.type', 'slf4j')
}
catch(x) {}

// Executor

var executor = new ExecutorConfig()
executor.name = 'default'
//executor.poolSize = 10  	
config.addExecutorConfig(executor)

Hazelcast.newHazelcastInstance(config)
*/
