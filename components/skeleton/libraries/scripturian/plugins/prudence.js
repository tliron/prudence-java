
document.require(
	'/sincerity/jvm/',
	'/sincerity/files/',
	'/sincerity/json/')

importClass(
	com.threecrickets.sincerity.exception.CommandException,
	com.threecrickets.sincerity.exception.BadArgumentsCommandException,
	java.io.File,
	java.io.FileReader,
	java.io.FileWriter,
	java.io.StringWriter)

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
		case 'describe':
			describe(command)
			break
	}
}

function help(command) {
	command.sincerity.out.println('prudence help                         Show this help')
	command.sincerity.out.println('prudence version                      Show the installed Prudence version')
	command.sincerity.out.println('prudence create [name] [[template]]   Create a skeleton for a new Prudence application using [name] as the directory name')
	command.sincerity.out.println('prudence describe [name] [[dir]]      Generate REST API description for Prudence application using [name] as the directory name')
}

function version(command) {
	var version = Sincerity.JVM.fromProperties(Sincerity.JVM.getResourceAsProperties('com/threecrickets/prudence/version.conf'))
	command.sincerity.out.println('Version: ' + version.version)
	command.sincerity.out.println('Built: ' + version.built)
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
		throw new CommandException(command, 'The application already exists: ' + applicationDir)
	}
	
	var templateDir = new File(sincerity.container.getFile('component', 'templates', templateName))
	if (!templateDir.exists()) {
		throw new CommandException(command, 'The template does not exist: ' + templateDir)
	}
	
	copy(templateDir, applicationDir, /\$\{APPLICATION\}/g, name)
}

function describe(command) {
	if (command.arguments.length < 2) {
		throw new BadArgumentsCommandException(command, 'name', '[dir=description]')
	}
	var name = command.arguments[1]
	var dir = 'description'
		if (command.arguments.length > 2) {
			dir = command.arguments[2]
		}

	var applicationDir = new File(sincerity.container.getFile('component', 'applications', name))
	if (!applicationDir.exists() || !applicationDir.directory) {
		throw new CommandException(command, 'The application does not exist: ' + applicationDir)
	}

	descriptions = []
	gatherDescriptions(command, applicationDir, descriptions)

	var swaggerInfo, swaggerApis = [], swaggerResourcePath

	function getResourcePath(source) {
		source = String(source).substring(String(applicationDir).length + 1)
		if (source.endsWith('.js')) {
			source = source.substring(0, source.length - 3)
			if (source.endsWith('.m') || source.endsWith('.t')) {
				source = source.substring(0, source.length - 2)
			}
		}
		return source
	}

	for (var i in descriptions) {
		var description = descriptions[i]
		var source = description._source
		delete description._source

		// Parse Swagger
		if (Sincerity.Objects.exists(description.swagger)) {
			if (description.swagger == 'info') {
				if (!Sincerity.Objects.exists(swaggerInfo)) {
					swaggerInfo = {}
				}
				Sincerity.Objects.merge(swaggerInfo, description)
				delete swaggerInfo.swagger
			}
			else if (description.swagger == 'api') {
				if (!Sincerity.Objects.exists(description.resourcePath)) {
					description.resourcePath = getResourcePath(source)
				}

				// Will be the default for following operations and models
				swaggerResourcePath = description.resourcePath

				var api = Sincerity.Objects.clone(description)
				delete api.swagger

				if (Sincerity.Objects.exists(api.produces)) {
					api.produces = api.produces.split(/\s+/)
				}
				if (Sincerity.Objects.exists(api.consumes)) {
					api.consumes = api.consumes.split(/\s+/)
				}
				if (!Sincerity.Objects.exists(api.apis)) {
					api.apis = []
				}

				var merged = false
				for (var a in swaggerApis) {
					if (swaggerApis[a].resourcePath == api.resourcePath) {
						Sincerity.Objects.merge(swaggerApis[a], api)
						merged = true
						break
					}
				}
				if (!merged) {
					swaggerApis.push(api)
				}
			}
			else if (description.swagger == 'operation') {
				if (!Sincerity.Objects.exists(description.resourcePath)) {
					if (!Sincerity.Objects.exists(swaggerResourcePath)) {
						swaggerResourcePath = getResourcePath(source)
					}
					description.resourcePath = swaggerResourcePath
				}
				if (!Sincerity.Objects.exists(description.path)) {
					throw new CommandException(command, '@swagger operation does not specify @path: ' + Sincerity.JSON.to(description, true))
				}
				if (!Sincerity.Objects.exists(description.method)) {
					throw new CommandException(command, '@swagger operation does not specify @method: ' + Sincerity.JSON.to(description, true))
				}
				if (!Sincerity.Objects.exists(description.nickname)) {
					throw new CommandException(command, '@swagger operation does not specify @nickname: ' + Sincerity.JSON.to(description, true))
				}

				var api = null
				for (var a in swaggerApis) {
					if (swaggerApis[a].resourcePath == description.resourcePath) {
						api = swaggerApis[a]
						break
					}
				}
				if (!api) {
					api = {resourcePath: description.resourcePath}
					swaggerApis.push(api)
				}
				if (!Sincerity.Objects.exists(api.apis)) {
					api.apis = []
				}
				var path = null
				for (var a in api.apis) {
					if (api.apis[a].path == description.path) {
						path = api.apis[a]
						break
					}
				}
				if (!path) {
					path = {path: description.path, operations: []}
					api.apis.push(path)
				}
				if (!Sincerity.Objects.exists(path.operations)) {
					path.operations = []
				}

				var operation = Sincerity.Objects.clone(description)
				delete operation.swagger
				delete operation.resourcePath
				delete operation.path

				if (Sincerity.Objects.exists(operation.parameter)) {
					operation.parameters = Sincerity.Objects.array(operation.parameter)
					delete operation.parameter
					for (var p in operation.parameters) {
						var parameter = operation.parameters[p]

						var match = parameter.match(/\s+/)
						if (!match || (match.index <= 0)) {
							throw new CommandException(command, '@parameter must include at least paramType, name and type: ' + Sincerity.JSON.to(description, true))
						}
						var paramType = parameter.substring(0, match.index)
						if ((paramType != 'path') && (paramType != 'query') && (paramType != 'body') && (paramType != 'header') && (paramType != 'form')) {
							throw new CommandException(command, '@parameter type must be  "path", "query", "body", "header" or "form": ' + Sincerity.JSON.to(description, true))
						}

						parameter = parameter.substring(match.index + match.length)
						match = parameter.match(/\s+/)
						if (!match) {
							throw new CommandException(command, '@parameter must include at least paramType, name and type: ' + Sincerity.JSON.to(description, true))
						}
						var name = parameter.substring(0, match.index)
						parameter = parameter.substring(match.index + match.length)

						match = parameter.match(/\s+/)
						var type, description = null
						if (match) {
							type = parameter.substring(0, match.index)
							description = parameter.substring(match.index + match.length)
						}
						else {
							type = parameter
						}

						operation.parameters[p] = {
							paramType: paramType,
							name: name,
							type: type
						}

						if (description) {
							operation.parameters[p].description = description
						}
					}
				}

				if (!Sincerity.Objects.exists(operation.parameters)) {
					operation.parameters = []
				}

				if (Sincerity.Objects.exists(operation.responseMessage)) {
					operation.responseMessages = Sincerity.Objects.array(operation.responseMessage)
					delete operation.responseMessage
					for (var r in operation.responseMessages) {
						var responseMessage = operation.responseMessages[r]

						var match = responseMessage.match(/\s+/)
						if (!match || (match.index <= 0)) {
							throw new CommandException(command, '@responseMessage must include at least code, type and message: ' + Sincerity.JSON.to(description, true))
						}
						var code = responseMessage.substring(0, match.index)

						responseMessage = responseMessage.substring(match.index + match.length)
						match = responseMessage.match(/\s+/)
						if (!match) {
							throw new CommandException(command, '@responseMessage must include at least code, type and message: ' + Sincerity.JSON.to(description, true))
						}
						var responseModel = responseMessage.substring(0, match.index)
						var message = responseMessage.substring(match.index + match.length)

						operation.responseMessages[r] = {
							code: parseInt(code),
							message: message
						}
						if (responseModel != 'null') {
							operation.responseMessages[r].responseModel = responseModel
						}
					}
				}

				path.operations.push(operation)
			}
			else if (description.swagger == 'model') {
				if (!Sincerity.Objects.exists(description.resourcePath)) {
					if (!Sincerity.Objects.exists(swaggerResourcePath)) {
						throw new CommandException(command, '@swagger model does not specify @resourcePath: ' + Sincerity.JSON.to(description, true))
					}
					description.resourcePath = swaggerResourcePath
				}
				if (!Sincerity.Objects.exists(description.id)) {
					throw new CommandException(command, '@swagger model does not specify @id: ' + Sincerity.JSON.to(description, true))
				}

				var api = null
				for (var a in swaggerApis) {
					if (swaggerApis[a].resourcePath == description.resourcePath) {
						api = swaggerApis[a]
						break
					}
				}
				if (!api) {
					api = {resourcePath: description.resourcePath, models: {}}
					swaggerApis.push(api)
				}
				if (!Sincerity.Objects.exists(api.models)) {
					api.models = {}
				}

				var model = Sincerity.Objects.clone(description)
				delete model.swagger
				delete model.resourcePath

				if (Sincerity.Objects.exists(model.property)) {
					model.properties = {}
					var properties = Sincerity.Objects.array(model.property)
					delete model.property
					for (var p in properties) {
						var property = properties[p]

						var match = property.match(/\s+/)
						if (!match || (match.index <= 0)) {
							throw new CommandException(command, '@property must include at least id and type: ' + Sincerity.JSON.to(description, true))
						}
						var id = property.substring(0, match.index)

						var type = property.substring(match.index + match.length)

						model.properties[id] = {
							type: type
						}
					}
				}

				api.models[model.id] = model
			}
			else {
				throw new CommandException(command, 'Unknown @swagger type: ' + Sincerity.JSON.to(description, true))
			}
		}
	}

	// Output Swagger
	if (swaggerInfo) {
		var basePath = swaggerInfo.basePath
		delete swaggerInfo.basePath

		if (!Sincerity.Objects.exists(swaggerInfo.swaggerVersion)) {
			swaggerInfo.swaggerVersion = '1.2'
		}
		if (!Sincerity.Objects.exists(swaggerInfo.apis)) {
			swaggerInfo.apis = []
		}
		for (var a in swaggerApis) {
			var entry = {
				path: swaggerApis[a].resourcePath
			}
			if (Sincerity.Objects.exists(swaggerApis[a].description)) {
				entry.description = swaggerApis[a].description
			}
			swaggerInfo.apis.push(entry)
		}

		var apiDir = new File(applicationDir, dir)
		apiDir.mkdirs()

		var index = new File(apiDir, 'index.json')
		var writer = Sincerity.Files.openForTextWriting(index)
		try {
			command.sincerity.out.println('Writing Swagger index to: ' + command.sincerity.container.getRelativePath(index))
			writer.write(Sincerity.JSON.to(swaggerInfo, true))
		}
		finally {
			writer.close()
		}

		for (var a in swaggerApis) {
			var api = swaggerApis[a]

			if (!Sincerity.Objects.exists(api.swaggerVersion)) {
				api.swaggerVersion = swaggerInfo.swaggerVersion
			}
			if (!Sincerity.Objects.exists(api.apiVersion) && Sincerity.Objects.exists(swaggerInfo.apiVersion)) {
				api.apiVersion = swaggerInfo.apiVersion
			}
			if (!Sincerity.Objects.exists(api.basePath)) {
				if (!Sincerity.Objects.exists(basePath)) {
					throw new CommandException(command, '@swagger api does not specify @basePath: ' + Sincerity.JSON.to(api, true))
				}
				api.basePath = basePath
			}
			delete api.description

			var path = api.resourcePath
			if (path.endsWith('/')) {
				new File(apiDir, path).mkdirs()
				path += 'index.json'
			}
			else {
				new File(apiDir, path).parentFile.mkdirs()
				path += '.json'
			}

			var file = new File(apiDir, path)
			var writer = Sincerity.Files.openForTextWriting(file)
			try {
				command.sincerity.out.println('Writing Swagger API to: ' + command.sincerity.container.getRelativePath(file))
				writer.write(Sincerity.JSON.to(api, true))
			}
			finally {
				writer.close()
			}
		}
	}
}

function gatherDescriptions(command, source, descriptions) {
	if (source.directory) {
		var sourceFiles = source.listFiles()
		for (var f in sourceFiles) {
			sourceFile = sourceFiles[f]
			if (sourceFile.directory || sourceFile.path.endsWith('.js')) {
				gatherDescriptions(command, sourceFile, descriptions)
			}
		}
	}
	else {
		var content = Sincerity.Files.loadText(source, 'UTF-8')

		// Gather REST description comments
		var regex = /\/\*\*\*([\S\s]*?)\*\//g
		var match = regex.exec(content)
		var found = false
		while (match) {
			found = true
			comment = match[1].replace(/\n\s+\*\s*/g, ' ') // flatten
			var description = parseDescription(comment)
			description._source = source
			descriptions.push(description)
			match = regex.exec(content)
		}

		if (found) {
			command.sincerity.out.println('Found REST description at: ' + command.sincerity.container.getRelativePath(source))
		}
	}
}

function parseDescription(comment) {
	// Parse REST description tags
	var description = {}
	var regex = /@([\S]+)\s+(\S[\S\s]*?)(?:\s@|$)/g
	var match = regex.exec(comment)
	while (match) {
		found = true
		var key = match[1]
		var value = match[2]
		// Cleanup
		value = Sincerity.Objects.trim(value)
		if (Sincerity.Objects.exists(description[key])) {
			description[key] = Sincerity.Objects.array(description[key])
			description[key].push(value)
		}
		else {
			description[key] = value
		}
		regex.lastIndex -= 1 // because the delimiting next "@" was included in previous match
		match = regex.exec(comment)
	}
	return description
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
