
document.require(
	'/sincerity/jvm/',
	'/sincerity/files/',
	'/sincerity/cryptography/')

importClass(
	com.threecrickets.sincerity.exception.CommandException,
	com.threecrickets.sincerity.exception.BadArgumentsCommandException,
	java.io.File,
	java.io.FileReader,
	java.io.FileWriter,
	java.io.StringWriter,
	java.util.Properties)

function getInterfaceVersion() {
	return 1
}

function getCommands() {
	return ['prudence']
}

function run(command) {
	switch (String(command.name)) {
		case 'prudence':
			prudence(command)
			break
	}
}

function prudence(command) {
	command.parse = true

	var prudenceCommand
	if (command.arguments.length > 0) {
		prudenceCommand = String(command.arguments[0])
	}
	else {
		prudenceCommand = 'help'
	}
	
	switch (prudenceCommand) {
		case 'help':
			help(command)
			break
		case 'version':
			version(command)
			break
		case 'create':
			create(command)
			break
		case 'digests':
			digests(command)
			break
	}
}

function help(command) {
	println('prudence help                         Show this help')
	println('prudence version                      Show the installed Prudence version')
	println('prudence create [name] [[template]]   Create a skeleton for a new Prudence application in the [name] application directory')
	println('prudence digests [name] [[algorithm]] Create digests for all files in the [name]/resources/ application directory')
}

function version(command) {
	var version = Sincerity.JVM.fromProperties(Sincerity.JVM.getReinputAsProperties('com/threecrickets/prudence/version.conf'))
	println('Version: ' + version.version)
	println('Built: ' + version.built)
}

function create(command) {
	if (command.arguments.length < 2) {
		throw new BadArgumentsCommandException(command, 'name', '[template=default]')
	}
	var name = command.arguments[1]
	var templateName = 'default'
	if (command.arguments.length > 2) {
		templateName = command.arguments[2]
	}
	var force = command.switches.contains('force')
	
	var applicationDir = new File(sincerity.container.getFile('component', 'applications', name))
	if (!force && applicationDir.exists()) {
		throw new CommandException(command, 'The application directory already exists: ' + applicationDir)		
	}
	
	var templateDir = new File(sincerity.container.getFile('component', 'templates', templateName))
	if (!templateDir.exists()) {
		throw new CommandException(command, 'The template does not exist: ' + templateDir)		
	}
	
	copy(templateDir, applicationDir, /\$\{APPLICATION\}/g, name)
}

function digests(command) {
	if (command.arguments.length < 2) {
		throw new BadArgumentsCommandException(command, 'name', '[algorithm=SHA-1]')
	}
	var name = command.arguments[1]
	var algorithm = 'SHA-1'
	if (command.arguments.length > 2) {
		algorithm = command.arguments[2]
	}

	var resourcesDir = new File(sincerity.container.getFile('component', 'applications', name, 'resources'))
	println('Calculating ' + algorithm + ' digests for all files under: ' + resourcesDir)

	var properties = new Properties()
	addDigests(resourcesDir, properties, algorithm, String(resourcesDir))

	var digestsFile = new File(sincerity.container.getFile('component', 'applications', name, 'digests.properties'))
	var output = new FileWriter(digestsFile)
	try {
		properties.store(output, 'Created by Prudence')
	}
	finally {
		output.close()
	}

	println('Saved digests to: ' + digestsFile)
}

function copy(source, destination, token, value) {
	if (source.directory) {
		var sourceFiles = source.listFiles()
		for (var f in sourceFiles) {
			sourceFile = sourceFiles[f]
			copy(sourceFile, new File(destination, sourceFile.name), token, value)
		}
	}
	else {
		destination.parentFile.mkdirs()
		var content = Sincerity.Files.loadText(source, 'UTF-8')
		content = String(content).replace(token, value)
		var output = new FileWriter(destination)
		try {
			output.write(content)
		}
		finally {
			output.close()
		}
	}
}

function addDigests(file, properties, algorithm, prefix) {
	if (file.directory) {
		var files = file.listFiles()
		for (var f in files) {
			file = files[f]
			addDigests(file, properties, algorithm, prefix)
		}
	}
	else {
		var digest = Sincerity.Cryptography.fileDigest(file, algorithm)
		var path = String(file).substring(prefix.length + 1)
		properties.put(path, digest)
	}
}