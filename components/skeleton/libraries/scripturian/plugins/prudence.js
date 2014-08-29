
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
		case 'documentation':
			documentation(command)
			break
	}
}

function help(command) {
	command.sincerity.out.println('prudence help                         Show this help')
	command.sincerity.out.println('prudence version                      Show the installed Prudence version')
	command.sincerity.out.println('prudence create [name] [[template]]   Create a skeleton for a new Prudence application using [name] as the directory name')
	command.sincerity.out.println('prudence documentation [name]         Generate REST API documentation for Prudence application using [name] as the directory name')
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

function documentation(command) {
	if (command.arguments.length < 2) {
		throw new BadArgumentsCommandException(command, 'name')
	}
	var name = command.arguments[1]

	var applicationDir = new File(sincerity.container.getFile('component', 'applications', name))
	if (!applicationDir.exists() || !applicationDir.directory) {
		throw new CommandException(command, 'The application does not exist: ' + applicationDir)
	}

	items = []
	gatherDocumentation(command, applicationDir, items)

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

	for (var i in items) {
		var item = items[i]
		var source = item._source
		delete item._source

		// Parse Swagger
		if (Sincerity.Objects.exists(item.swagger)) {
			if (item.swagger == 'info') {
				swaggerInfo = Sincerity.Objects.clone(item)
				delete swaggerInfo.swagger
			}
			else if (item.swagger == 'api') {
				if (!Sincerity.Objects.exists(item.resourcePath)) {
					item.resourcePath = getResourcePath(source)
				}

				// Will be the default for following operations and models
				swaggerResourcePath = item.resourcePath

				var api = Sincerity.Objects.clone(item)
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
			else if (item.swagger == 'operation') {
				if (!Sincerity.Objects.exists(item.resourcePath)) {
					if (!Sincerity.Objects.exists(swaggerResourcePath)) {
						throw new CommandException(command, '@swagger operation does not specify @resourcePath: ' + Sincerity.JSON.to(item, true))
					}
					item.resourcePath = swaggerResourcePath
				}
				if (!Sincerity.Objects.exists(item.path)) {
					throw new CommandException(command, '@swagger operation does not specify @path: ' + Sincerity.JSON.to(item, true))
				}
				if (!Sincerity.Objects.exists(item.method)) {
					throw new CommandException(command, '@swagger operation does not specify @method: ' + Sincerity.JSON.to(item, true))
				}
				if (!Sincerity.Objects.exists(item.nickname)) {
					throw new CommandException(command, '@swagger operation does not specify @nickname: ' + Sincerity.JSON.to(item, true))
				}

				var api = null
				for (var a in swaggerApis) {
					if (swaggerApis[a].resourcePath == item.resourcePath) {
						api = swaggerApis[a]
						break
					}
				}
				if (!api) {
					api = {resourcePath: item.resourcePath}
					swaggerApis.push(api)
				}
				if (!Sincerity.Objects.exists(api.apis)) {
					api.apis = []
				}
				var path = null
				for (var a in api.apis) {
					if (api.apis[a].path == item.path) {
						path = api.apis[a]
						break
					}
				}
				if (!path) {
					path = {path: item.path, operations: []}
					api.apis.push(path)
				}
				if (!Sincerity.Objects.exists(path.operations)) {
					path.operations = []
				}

				var operation = Sincerity.Objects.clone(item)
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
							throw new CommandException(command, '@parameter must include at least paramType, name and type: ' + Sincerity.JSON.to(item, true))
						}
						var paramType = parameter.substring(0, match.index)
						if ((paramType != 'path') && (paramType != 'query') && (paramType != 'body') && (paramType != 'header') && (paramType != 'form')) {
							throw new CommandException(command, '@parameter type must be  "path", "query", "body", "header" or "form": ' + Sincerity.JSON.to(item, true))
						}

						parameter = parameter.substring(match.index + match.length)
						match = parameter.match(/\s+/)
						if (!match) {
							throw new CommandException(command, '@parameter must include at least paramType, name and type: ' + Sincerity.JSON.to(item, true))
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
							throw new CommandException(command, '@responseMessage must include at least code, type and message: ' + Sincerity.JSON.to(item, true))
						}
						var code = responseMessage.substring(0, match.index)

						responseMessage = responseMessage.substring(match.index + match.length)
						match = responseMessage.match(/\s+/)
						if (!match) {
							throw new CommandException(command, '@responseMessage must include at least code, type and message: ' + Sincerity.JSON.to(item, true))
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
			else if (item.swagger == 'model') {
				if (!Sincerity.Objects.exists(item.resourcePath)) {
					if (!Sincerity.Objects.exists(swaggerResourcePath)) {
						throw new CommandException(command, '@swagger model does not specify @resourcePath: ' + Sincerity.JSON.to(item, true))
					}
					item.resourcePath = swaggerResourcePath
				}
				if (!Sincerity.Objects.exists(item.id)) {
					throw new CommandException(command, '@swagger model does not specify @id: ' + Sincerity.JSON.to(item, true))
				}

				var api = null
				for (var a in swaggerApis) {
					if (swaggerApis[a].resourcePath == item.resourcePath) {
						api = swaggerApis[a]
						break
					}
				}
				if (!api) {
					api = {resourcePath: item.resourcePath, models: {}}
					swaggerApis.push(api)
				}
				if (!Sincerity.Objects.exists(api.models)) {
					api.models = {}
				}

				var model = Sincerity.Objects.clone(item)
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
							throw new CommandException(command, '@property must include at least id and type: ' + Sincerity.JSON.to(item, true))
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
				throw new CommandException(command, 'Unknown @swagger type: ' + Sincerity.JSON.to(item, true))
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

		var apiDir = new File(applicationDir, 'api-docs')
		apiDir.mkdirs()

		var index = new File(apiDir, 'index.json')
		var writer = Sincerity.Files.openForTextWriting(index)
		try {
			command.sincerity.out.println('Writing Swagger info to: ' + command.sincerity.container.getRelativePath(index))
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

function gatherDocumentation(command, source, items) {
	if (source.directory) {
		var sourceFiles = source.listFiles()
		for (var f in sourceFiles) {
			sourceFile = sourceFiles[f]
			if (sourceFile.directory || sourceFile.path.endsWith('.js')) {
				gatherDocumentation(command, sourceFile, items)
			}
		}
	}
	else {
		var content = Sincerity.Files.loadText(source, 'UTF-8')

		// Gather REST documentation comments
		var regex = /\/\*\*\*([\s\S]*?)\*\//g
		var match = regex.exec(content)
		var found = false
		while (match) {
			found = true
			var item = parseDocumentation(match[1])
			item._source = source
			items.push(item)
			match = regex.exec(content)
		}

		if (found) {
			command.sincerity.out.println('Found documentation at: ' + command.sincerity.container.getRelativePath(source))
		}
	}
}

function parseDocumentation(comment) {
	// Parse REST documentation tags
	var item = {}
	var regex = /@([\S]+) *([^@]*)/g
	var match = regex.exec(comment)
	while (match) {
		found = true
		var key = match[1]
		var value = match[2]
		// Cleanup
		value = value.replace(/(\n|\s+\*)/g, '')
		value = Sincerity.Objects.trim(value)
		if (Sincerity.Objects.exists(item[key])) {
			item[key] = Sincerity.Objects.array(item[key])
			item[key].push(value)
		}
		else {
			item[key] = value
		}
		match = regex.exec(comment)
	}
	return item
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
