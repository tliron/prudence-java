//
// Prudence Component
//

importClass(
	java.lang.System,
	java.io.FileNotFoundException,
	java.util.logging.LogManager);

function executeOrDefault(name, def) {
	try {
		executable.container.execute(name);
	} catch(e if e.javaException instanceof FileNotFoundException) {
		if(!def) {
			def = 'defaults/' + name;
		}
		executable.container.execute(def);
	}
}

var tasks = [];

//
// Version
//

var prudenceVersion = '1.0';
var prudenceRevision = '-%REVISION%';
if(prudenceRevision.length == 1) {
	prudenceRevision = '';
}
var prudenceFlavor = 'JavaScript';

//
// Welcome
//

print('Prudence ' + prudenceVersion + prudenceRevision + ' for ' + prudenceFlavor + '.\n');

//
// Logging
//

// log4j: This is our actual logging engine
try {
	importClass(org.apache.log4j.PropertyConfigurator);
	PropertyConfigurator.configure('configuration/logging.conf');
} catch(x) {}

// JULI: Remove any pre-existing configuration
LogManager.logManager.reset();

// JULI: Bridge to SLF4J, which will use log4j as its engine 
try {
	importClass(org.slf4j.bridge.SLF4JBridgeHandler);
	SLF4JBridgeHandler.install();
} catch(x) {}

// Set Restlet to use SLF4J, which will use log4j as its engine
System.setProperty('org.restlet.engine.loggerFacadeClass', 'org.restlet.ext.slf4j.Slf4jLoggerFacade');

// Velocity logging
System.setProperty('com.sun.script.velocity.properties', 'configuration/velocity.conf');

//
// Component
//

executeOrDefault('instance/component/');

//
// Clients
//

executeOrDefault('instance/clients/');

//
// Routing
//

executeOrDefault('instance/routing/');

//
// Servers
//

executeOrDefault('instance/servers/');

//
// Start
//

component.start();

//
// Tasks
//

if(tasks.length > 0) {
	var futures = [];
	var startTime = System.currentTimeMillis();
	print('Executing ' + tasks.length + ' tasks...\n');
	for(var i in tasks) {
		var task = tasks[i];
		futures.push(executor.submit(task));
	}
	for(var i in futures) {
		var future = futures[i];
		future.get();
	}
	print('Finished tasks in ' + ((System.currentTimeMillis() - startTime) / 1000) + ' seconds.\n');
}
