
/*
 * Enable this if you want this node to be able to spawn tasks in the "task"
 * cluster via the application.distributedExecuteTask and
 * application.distributedCodeTask APIs.
 * 
 * Without this, the APIs would still work, but would be spawned within the
 * "application" cluster.
 * 
 * For the nodes that are to be members of the cluster, which execute the
 * tasks, enable the "1-server.js" file.
 * 
 * It doesn't make sense to have both "1-server.js" and "2-client.js" enabled
 * on the same node: a server is already a full member, and doesn't have to
 * also be a client. 
 */

/*
var config = new ClientConfig()

config.groupConfig.name = 'com.threecrickets.prudence.task'
config.groupConfig.password = 'prudence'
//config.addAddress('localhost')

try {
	importClass(org.slf4j.Logger)
	config.setProperty('hazelcast.logging.type', 'slf4j')
}
catch(x) {}

sharedGlobals['com.threecrickets.prudence.hazelcast.taskInstance'] = HazelcastClient.newHazelcastClient(config)
*/
