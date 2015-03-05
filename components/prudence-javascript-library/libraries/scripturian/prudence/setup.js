//
// This file is part of the Prudence Foundation Library
//
// Copyright 2009-2015 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

document.require(
	'/prudence/lazy/',
	'/sincerity/classes/',
	'/sincerity/objects/',
	'/sincerity/templates/',
	'/sincerity/localization/',
	'/sincerity/files/',
	'/sincerity/jvm/',
	'/restlet/')

var Prudence = Prudence || {}

importClass(com.threecrickets.sincerity.exception.SincerityException)

/**
 * Handles the bootstrapping of Prudence applications using a convenient DSL.
 * This library is meant to be used in the bootstrapping scripts of the
 * Sincerity Restlet skeleton. 
 * 
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */
Prudence.Setup = Prudence.Setup || function() {
	/** @exports Public as Prudence.Setup */
	var Public = {}
	
	Public.cleanUri = function(uri) {
		// No doubles
		uri = String(uri).replace(/\/\//g, '/')
		if ((uri == '') || (uri[0] != '/')) {
			// Always at the beginning
			uri = '/' + uri
		}
		return uri
	}

	Public.cleanBaseUri = function(uri) {
		// No doubles
		uri = String(uri).replace(/\/\//g, '/')
		if ((uri == '') || (uri[0] != '/')) {
			// Always at the beginning
			uri = '/' + uri
		}
		var length = uri.length
		if ((length > 0) && (uri[length - 1] == '/')) {
			// No trailing slash
			uri = uri.substring(0, length - 1)
		}
		return uri
	}

	/**
	 * Allows creation of a <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/PrudenceApplication.html">PrudenceApplication</a>
	 * instance according to rich configuration properties.
	 * <p>
	 * Before calling {@link Prudence.Setup.Application#create}, you should set the various properties that will be used
	 * to create and configure the application.
	 * 
	 * @class
	 * @name Prudence.Setup.Application
	 * @property {<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/io/File.html">java.io.File</a>} [root=Sincerity.Container.here]
	 * 
	 * @property {Object} [settings] Application settings
	 * 
	 * @property {Object} [settings.description] A human-readable description of your application
	 * @property {String} [settings.description.name] The application's name (short)
	 * @property {String} [settings.description.description] The application's description (long)
	 * @property {String} [settings.description.author] The application's author (person, organization or company)
	 * @property {String} [settings.description.owner] The application's project's name
	 * 
	 * @property {Object} [settings.errors] Error handling settings
	 * @property {Boolean} [settings.errors.debug=false] When true, enabled the special debugging HTML page in case of 500 errors
	 *                     (usually caused by unhandled exceptions in your code)
	 * @property {String} [settings.errors.debugHeader='X-Debug'] Set this HTTP header when there is an error
	 * @property {String} [settings.errors.homeUrl] Shows this URL on the default error page
	 * @property {String} [settings.errors.contactEmail] Shows this contact email on the default error page
	 * 
	 * @property {Object} [settings.code] Programming language settings
	 * @property {String[]} [settings.code.libraries='libraries'] A list of base paths from which {@link document#execute} (and also
	 *                      the programming languages' internal import facilities) will look for libraries;
	 *                      the <i>first</i> library in this list is special: it is used to look for handlers and tasks;
	 *                      paths are relative to the application directory
	 * @property {Number|String} [settings.code.minimumTimeBetweenValidityChecks=1000] Time in milliseconds between validity checks on files
	 *                    containing source code (libraries, manual resources and scriptlet resources)
	 * @property {String} [settings.code.defaultDocumentName='default'] Used for {@link document#execute} when a directory name is given
	 * @property {String} [settings.code.defaultExtension='js'] Used for {@link document#execute} as the preference when multiple documents
	 *                    of the same name exist
	 * @property {String} [settings.code.defaultLanguageTag='javascript'] Used for scriptlets as the default language when
	 *                    not specified in the scriptlet tag
	 * @property {Boolean} [settings.code.sourceViewable=false] When true enabled the source code viewing facility
	 *                     (can work in conjunction with the debug page when settings.errors.debug is true)
	 * @property {String} [settings.code.sourceViewer='/source-code/'] Only used when settings.code.sourceViewable=true
	 * 
	 * @property {Object} [settings.templates] Templates settings
	 * @property {Boolean} [settings.templates.debug=false] When true, outputs the generated template code under "/cache/scripturian/"
	 * @property {String} [settings.templates.parser='scriptlets'] Allows to set the parser to use for templates
	 * @property {Object} [settings.templates.plugins] Parser plugins
	 * 
	 * @property {Object} [settings.caching] Caching settings
	 * @property {Boolean} [settings.caching.debug=false] When true, adds caching debug headers to responses
	 * @property {String} [settings.caching.defaultKeyTemplate='{ri}|{dn}|{nmt}|{nl}|{ne}'] Allows you to change the default {@link caching#keyTemplate}
	 * @property {Object} [settings.caching.keyTemplatePlugins] Allows you to configure application-wide {@link caching#keyTemplatePlugins}
	 * 
	 * @property {Object} [settings.compression] Compression settings
	 * @property {Number|String} [settings.compression.sizeThreshold=1024] The size in bytes beyond which responses may be compressed
	 * @property {String[]} [settings.compression.exclude] Additional media types excluded from compression
	 * @property {Object} [settings.uploads] File upload settings
	 * @property {String} [settings.uploads.root='uploads'] Path in which to store uploaded files; the path is relative to the
	 *                    application directory
	 * @property {Number|String} [settings.uploads.sizeThreshold=0] The size in bytes beyond which uploaded files are
	 *                    permanently stored; below this threshold they are simply stored in memory
	 * 
	 * @property {Object} [settings.mediaTypes] A dict matching filename extensions to media (MIME) types
	 * 
	 * @property {Object} [settings.distributed] Distributed settings
	 * @property {String} [settings.distributed.applicationInstance='com.threecrickets.prudence'] Hazelcast application instance name
	 * @property {String} [settings.distributed.globals='com.threecrickets.prudence.distributedGlobals.[root.name]'] Hazelcast distributed globals map name
	 * @property {String} [settings.distributed.sharedGlobals='com.threecrickets.prudence.distributedSharedGlobals'] Hazelcast distributed shared globals map name
	 * @property {String} [settings.distributed.taskInstanceSharedGlobal='com.threecrickets.prudence.hazelcast.taskInstance'] The name of the shared global in which the Hazelcast task instance is stored
	 * @property {String} [settings.distributed.executorService='default'] Hazelcast executor service name
	 * 
	 * @property {String} [settings.logger=root.name]
	 * 
	 * @property {Object} [globals] These values will be available as {@link application#globals} when the application
	 *                    is running; not that this dict will be flattened using {@link Sincerity.Objects#flatten}
	 * 
	 * @property {Object} [hosts] A dict matching virtual host names to the root URIs on which the application
	 *                    will be available; the special 'internal' host name is used for internal requests (via
	 *                    the RIAP protocol); the default value for "hosts" is {internal: root.name}
	 * 
	 * @property {Object} [routes] Dict matching URI templates to target configurations;
	 *                    the targets are created only when {@link Prudence.Setup.Application#create} is called,
	 *                    and are usually instances of {@link Prudence.Setup.Restlet} sub-classes;
	 *                    see the Prudence Manual for more details
	 * 
	 * @property {Object} [errors] A dict matching HTTP error codes to URIs
	 * 
	 * @property {Object} [dispatchers] A dict matching dispatcher names (usually programming language names)
	 *                    to library names
	 * 
	 * @property {String[]} [preheat] A list of URIs that will be "pre-heated" (via internal requests) as soon
	 *                      as the application starts
	 */
	Public.Application = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Application */
		var Public = {}
		
		/** @ignore */
		Public._construct = function() {
			this.root = Sincerity.Container.here
			this.settings = {}
			this.globals = {}
			this.hosts = {}
			this.routes = {}
			this.errors = {}
			this.dispatchers = {}
			this.preheat = []
		}

		/**
		 * Creates a <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/PrudenceApplication.html">PrudenceApplication</a> instance
		 * based on the configured properties,
		 * in conjunction with <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/ApplicationTaskCollector.html">ApplicationTaskCollector</a>,
		 * <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/DelegatedStatusService.html">DelegatedStatusService</a>,
		 * and <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/PreheatTask.html">PreheatTask</a>.
		 * 
		 * @returns {<a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/PrudenceApplication.html">com.threecrickets.prudence.PrudenceApplication</a>}
		 */
		Public.create = function(component) {
			importClass(
				com.threecrickets.prudence.PrudenceApplication,
				com.threecrickets.prudence.ApplicationTaskCollector,
				com.threecrickets.prudence.DelegatedStatusService,
				com.threecrickets.prudence.util.LoggingUtil,
				com.threecrickets.prudence.util.PreheatTask,
				com.threecrickets.prudence.service.ApplicationService,
				com.threecrickets.prudence.util.InstanceUtil,
				com.threecrickets.prudence.util.PrudenceScriptletPlugin,
				org.restlet.resource.Finder,
				org.restlet.routing.Template,
				org.restlet.routing.Redirector,
				org.restlet.data.Reference,
				org.restlet.data.MediaType,
				java.util.concurrent.ConcurrentHashMap,
				java.util.concurrent.CopyOnWriteArrayList,
				java.io.File)

			this.component = component

			// Flatten globals
			this.globals = Prudence.Lazy.flatten(this.globals)

			// Ensure all settings exist
			this.settings.description = Sincerity.Objects.ensure(this.settings.description, {})
			this.settings.errors = Sincerity.Objects.ensure(this.settings.errors, {})
			this.settings.code = Sincerity.Objects.ensure(this.settings.code, {})
			this.settings.templates = Sincerity.Objects.ensure(this.settings.templates, {})
			this.settings.caching = Sincerity.Objects.ensure(this.settings.caching, {})
			this.settings.compression = Sincerity.Objects.ensure(this.settings.compression, {})
			this.settings.uploads = Sincerity.Objects.ensure(this.settings.code, {})
			this.settings.mediaTypes = Sincerity.Objects.ensure(this.settings.mediaTypes, {})
			this.settings.distributed = Sincerity.Objects.ensure(this.settings.distributed, {})
			
			// Sensible default settings
			if (this.settings.errors.debugHeader !== null) {
				this.settings.errors.debugHeader = 'X-Debug'
			}
			this.settings.code.minimumTimeBetweenValidityChecks = Sincerity.Objects.ensure(this.settings.code.minimumTimeBetweenValidityChecks, 1000)
			this.settings.code.defaultDocumentName = Sincerity.Objects.ensure(this.settings.code.defaultDocumentName, 'default')
			this.settings.code.defaultExtension = Sincerity.Objects.ensure(this.settings.code.defaultExtension, 'js')
			this.settings.code.defaultLanguageTag = Sincerity.Objects.ensure(this.settings.code.defaultLanguageTag, 'javascript')
			this.settings.code.sourceViewer = Sincerity.Objects.ensure(this.settings.code.sourceViewer, '/source-code/')
			this.settings.caching.defaultKeyTemplate = Sincerity.Objects.ensure(this.settings.caching.defaultKeyTemplate, '{ri}|{dn}|{nmt}|{nl}|{ne}')
			this.settings.logger = Sincerity.Objects.ensure(this.settings.logger, this.root.name)
			this.settings.distributed.applicationInstance = Sincerity.Objects.ensure(this.settings.distributed.applicationInstance, 'com.threecrickets.prudence.application')
			this.settings.distributed.globals = Sincerity.Objects.ensure(this.settings.distributed.globals, 'com.threecrickets.prudence.distributedGlobals.' + this.root.name)
			this.settings.distributed.sharedGlobals = Sincerity.Objects.ensure(this.settings.distributed.sharedGlobals, 'com.threecrickets.prudence.distributedSharedGlobals')
			this.settings.distributed.taskInstanceSharedGlobal = Sincerity.Objects.ensure(this.settings.distributed.taskInstanceSharedGlobal, 'com.threecrickets.prudence.hazelcast.taskInstance')
			this.settings.distributed.executorService = Sincerity.Objects.ensure(this.settings.distributed.executorService, 'default')
			this.settings.templates.parser = Sincerity.Objects.ensure(this.settings.templates.parser, 'scriptlets')

			var prudenceScriptletPlugin = new PrudenceScriptletPlugin()
			this.settings.templates.plugins = Sincerity.Objects.ensure(this.settings.templates.plugins, {})
			this.settings.templates.plugins['=='] = Sincerity.Objects.ensure(this.settings.templates.plugins['=='], prudenceScriptletPlugin)
			this.settings.templates.plugins['{'] = Sincerity.Objects.ensure(this.settings.templates.plugins['{'], prudenceScriptletPlugin)
			this.settings.templates.plugins['}'] = Sincerity.Objects.ensure(this.settings.templates.plugins['}'], prudenceScriptletPlugin)
			this.settings.templates.plugins['['] = Sincerity.Objects.ensure(this.settings.templates.plugins['['], prudenceScriptletPlugin)
			this.settings.templates.plugins[']'] = Sincerity.Objects.ensure(this.settings.templates.plugins[']'], prudenceScriptletPlugin)
			this.settings.templates.plugins['.'] = Sincerity.Objects.ensure(this.settings.templates.plugins['.'], prudenceScriptletPlugin)

			this.settings.compression.sizeThreshold = Sincerity.Objects.ensure(this.settings.compression.sizeThreshold, 1024)

			this.settings.uploads.sizeThreshold = Sincerity.Objects.ensure(this.settings.uploads.sizeThreshold, 0)
			this.settings.uploads.root = Sincerity.Objects.ensure(this.settings.uploads.root, 'uploads')
			if (!(this.settings.uploads.root instanceof File)) {
				this.settings.uploads.root = Sincerity.Files.build(this.root, this.settings.uploads.root)
			}

			this.settings.code.minimumTimeBetweenValidityChecks = Sincerity.Localization.toMilliseconds(this.settings.code.minimumTimeBetweenValidityChecks)
			this.settings.uploads.sizeThreshold = Sincerity.Localization.toBytes(this.settings.uploads.sizeThreshold)
			this.settings.compression.sizeThreshold = Sincerity.Localization.toBytes(this.settings.compression.sizeThreshold)

			// Hazelcast
			this.globals['com.threecrickets.prudence.hazelcast.applicationInstanceName'] = this.settings.distributed.applicationInstance
			this.globals['com.threecrickets.prudence.hazelcast.distributedGlobalsMapName'] = this.settings.distributed.globals
			this.globals['com.threecrickets.prudence.hazelcast.distributedSharedGlobalsMapName'] = this.settings.distributed.sharedGlobals
			this.globals['com.threecrickets.prudence.hazelcast.taskInstanceAttributeName'] = this.settings.distributed.taskInstanceSharedGlobal
			this.globals['com.threecrickets.prudence.hazelcast.executorServiceName'] = this.settings.distributed.executorService

			// Create instance
			this.context = component.context.createChildContext()
			this.instance = new PrudenceApplication(this.context)

			this.context.attributes.put(InstanceUtil.ROOT_ATTRIBUTE, this.root)

			// Logger
			this.context.logger = LoggingUtil.getRestletLogger(this.settings.logger)
			
			// Description
			if (Sincerity.Objects.exists(this.settings.description.name)) {
				this.instance.name = this.settings.description.name
			}
			if (Sincerity.Objects.exists(this.settings.description.description)) {
				this.instance.description = this.settings.description.description
			}
			if (Sincerity.Objects.exists(this.settings.description.author)) {
				this.instance.author = this.settings.description.author
			}
			if (Sincerity.Objects.exists(this.settings.description.owner)) {
				this.instance.owner = this.settings.description.owner
			}

			if (sincerity.verbosity >= 1) {
				println('Setting up application: "{0}"'.cast(this.instance.name))
			}

			// Media types
			for (var extension in this.settings.mediaTypes) {
				var type = this.settings.mediaTypes[extension]
				if (Sincerity.Objects.isString(type)) {
					type = MediaType.valueOf(type)
				}
				this.instance.metadataService.addExtension(extension, type)
			}

			// Trailing-slash redirector
			this.addTrailingSlashRedirector = new Redirector(this.context, '{ri}/', Redirector.MODE_CLIENT_PERMANENT)

			// Default internal host to subdirectory name
			if (!Sincerity.Objects.exists(this.hosts.internal)) {
				this.hosts.internal = String(this.root.name)
			}
			
			// Attach to hosts
			if (sincerity.verbosity >= 2) {
				println('  Hosts:')
			}
			for (var name in this.hosts) {
				var host = Restlet.getHost(component, name)
				if (!Sincerity.Objects.exists(host)) {
					throw new SincerityException('Unknown host: ' + name)
				}
				var uri = Module.cleanBaseUri(this.hosts[name])
				if (name == 'internal') {
					// Internal should not have a slash at the beginning
					this.internalName = uri.substring(1)
				}
				if (sincerity.verbosity >= 2) {
					println('    "{0}/" on "{1}"'.cast(uri, name))
				}
				if (uri != '') {
					host.attach(uri, this.addTrailingSlashRedirector).matchingMode = Template.MODE_EQUALS
				}
				host.attach(uri, this.instance)
			}

			// Status service
			if (sincerity.verbosity >= 2) {
				println('  Status service:')
			}
			this.instance.statusService = new DelegatedStatusService(this.settings.code.sourceViewable ? this.settings.code.sourceViewer : null, this.settings.errors.debugHeader)
			this.instance.statusService.context = this.context.createChildContext()
			this.instance.statusService.debugging = true == this.settings.errors.debug
			if (sincerity.verbosity >= 2) {
				if (this.settings.errors.debugHeader !== null) {
					println('    Debug header: "{0}"'.cast(this.settings.errors.debugHeader))
				}
				else {
					println('    Debug header: null')
				}
			}
			if (Sincerity.Objects.exists(this.settings.errors.homeUrl)) {
				if (sincerity.verbosity >= 2) {
					println('    Home URL: "{0}"'.cast(this.settings.errors.homeUrl))
				}
				this.instance.statusService.homeRef = new Reference(this.settings.errors.homeUrl)
			}
			if (Sincerity.Objects.exists(this.settings.errors.contactEmail)) {
				if (sincerity.verbosity >= 2) {
					println('    Contact email: "{0}"'.cast(this.settings.errors.contactEmail))
				}
				this.instance.statusService.contactEmail = this.settings.errors.contactEmail
			}
			
			// Libraries
			this.libraryDocumentSources = new CopyOnWriteArrayList()

			// Container library
			var containerLibraryDocumentSource = component.context.attributes.get('com.threecrickets.prudence.containerLibraryDocumentSource')
			if (!Sincerity.Objects.exists(containerLibraryDocumentSource)) {
				var library = sincerity.container.getLibrariesFile('scripturian')
				containerLibraryDocumentSource = this.createDocumentSource(library)
				var existing = component.context.attributes.put('com.threecrickets.prudence.containerLibraryDocumentSource', containerLibraryDocumentSource)
				if (Sincerity.Objects.exists(existing)) {
					containerLibraryDocumentSource = existing
				}
			}

			if (sincerity.verbosity >= 2) {
				println('  Libraries:')
			}
			if (Sincerity.Objects.exists(this.settings.code.libraries)) {
				for (var i in this.settings.code.libraries) {
					var library = this.settings.code.libraries[i]
					
					if (!(library instanceof File)) {
						library = Sincerity.Files.build(this.root, library)
					}
					
					if (sincerity.verbosity >= 2) {
						println('    Library: "{0}"'.cast(sincerity.container.getRelativePath(library)))
					}
					var documentSource = this.createDocumentSource(library)
					this.libraryDocumentSources.add(documentSource)
					
					if (i == 0) {
						// We'll use the first library for handlers and tasks
						var extraDocumentSources = new CopyOnWriteArrayList()
						extraDocumentSources.add(containerLibraryDocumentSource)
						
						// Handlers
						Sincerity.Objects.merge(this.globals, Sincerity.Objects.flatten({
							'com.threecrickets.prudence.DelegatedHandler': {
								documentSource: documentSource,
								extraDocumentSources: extraDocumentSources,
								libraryDocumentSources: this.libraryDocumentSources,
								defaultName: this.settings.code.defaultDocumentName,
								defaultLanguageTag: this.settings.code.defaultLanguageTag,
								languageManager: executable.languageManager,
								parserManager: executable.parserManager,
								sourceViewable: this.settings.code.sourceViewable,
								fileUploadDirectory: this.settings.uploads.root,
								fileUploadSizeThreshold: this.settings.uploads.sizeThreshold
							}
						}))
						if (sincerity.verbosity >= 2) {
							println('    Handlers: "{0}"'.cast(sincerity.container.getRelativePath(library)))
						}

						// Tasks
						Sincerity.Objects.merge(this.globals, Sincerity.Objects.flatten({
							'com.threecrickets.prudence.ApplicationTask': {
								documentSource: documentSource,
								extraDocumentSources: extraDocumentSources,
								libraryDocumentSources: this.libraryDocumentSources,
								defaultName: this.settings.code.defaultDocumentName,
								defaultLanguageTag: this.settings.code.defaultLanguageTag,
								languageManager: executable.languageManager,
								parserManager: executable.parserManager,
								sourceViewable: this.settings.code.sourceViewable,
								fileUploadDirectory: this.settings.uploads.root,
								fileUploadSizeThreshold: this.settings.uploads.sizeThreshold
							}
						}))
						if (sincerity.verbosity >= 2) {
							println('    Tasks: "{0}"'.cast(sincerity.container.getRelativePath(library)))
						}
					}
				}
			}
			
			if (sincerity.verbosity >= 2) {
				println('    Container library: "{0}"'.cast(sincerity.container.getRelativePath(containerLibraryDocumentSource.basePath)))
			}
			this.libraryDocumentSources.add(containerLibraryDocumentSource)

			// Sincerity library
			var sincerityLibraryDocumentSource = component.context.attributes.get('com.threecrickets.prudence.sincerityLibraryDocumentSource')
			if (!Sincerity.Objects.exists(sincerityLibraryDocumentSource)) {
				var library = sincerity.getHomeFile('libraries', 'scripturian')
				sincerityLibraryDocumentSource = this.createDocumentSource(library)
				var existing = component.context.attributes.put('com.threecrickets.prudence.sincerityLibraryDocumentSource', sincerityLibraryDocumentSource)
				if (Sincerity.Objects.exists(existing)) {
					sincerityLibraryDocumentSource = existing
				}
			}
			if (sincerityLibraryDocumentSource.basePath != containerLibraryDocumentSource.basePath) {
				if (sincerity.verbosity >= 2) {
					println('    Sincerity library: "{0}"'.cast(sincerity.container.getRelativePath(sincerityLibraryDocumentSource.basePath)))
				}
				this.libraryDocumentSources.add(sincerityLibraryDocumentSource)
			}
			
			// Caching key template plugins
			this.cachingKeyTemplatePlugins = new ConcurrentHashMap()
			if (Sincerity.Objects.exists(this.settings.caching.keyTemplatePlugins) && !Sincerity.Objects.isEmpty(this.settings.caching.keyTemplatePlugins)) {
				if (sincerity.verbosity >= 2) {
					println('  Caching key template plugins:')
				}
				for (var variable in this.settings.caching.keyTemplatePlugins) {
					var plugin = this.settings.caching.keyTemplatePlugins[variable]
					this.cachingKeyTemplatePlugins.put(variable, plugin)
					if (sincerity.verbosity >= 2) {
						println('    "{0}" -> "{1}"'.cast(variable, plugin))
					}
				}
			}

			if (sincerity.verbosity >= 2) {
				println('  Routes:')
			}

			// Viewable document sources
			if (true == this.settings.code.sourceViewable) {
				this.sourceViewableDocumentSources = new CopyOnWriteArrayList()
				this.globals['com.threecrickets.prudence.SourceCodeResource.documentSources'] = this.sourceViewableDocumentSources
			}
			
			this.hidden = []

			// Inbound root (a router)
			this.instance.inboundRoot = this.createRestlet({type: 'router', routes: this.routes}, uri)
			
			// Outbound root
			// (By default, Restlet's ApplicationHelper will wrap the client dispatcher with filters
			// created by all the services, which includes the DecoderService, which would interfere
			// with our capturing mechanism)
			this.instance.outboundRoot = this.context.clientDispatcher

			// Source viewer
			if (true == this.settings.code.sourceViewable) {
				var sourceViewer = new Finder(this.context, Sincerity.JVM.getClass('com.threecrickets.prudence.SourceCodeResource'))
				this.instance.inboundRoot.attach(this.settings.code.sourceViewer, sourceViewer).matchingMode = Template.MODE_EQUALS
				if (sincerity.verbosity >= 2) {
					println('    "{0}" -> "{1}"'.cast(this.settings.code.sourceViewer, sourceViewer['class'].simpleName))
				}
			}

			// Internal access to DelegatedResource
			if (Sincerity.Objects.exists(this.delegatedResource)) {
				this.instance.inboundRoot.attachBase(this.delegatedResourceInternalUri, this.delegatedResource)
				this.hidden.push(this.delegatedResourceInternalUri + '*')
			}

			// Errors
			if (Sincerity.Objects.exists(this.errors) && !Sincerity.Objects.isEmpty(this.errors)) {
				if (sincerity.verbosity >= 2) {
					println('  Errors:')
				}
				for (var code in this.errors) {
					var uri = this.errors[code]
					if (uri.endsWith('!')) {
						uri = uri.substring(0, uri.length - 1)
						this.hidden.push(uri)
					}
					if (sincerity.verbosity >= 2) {
						println('    {0} -> "{1}"'.cast(code, uri))
					}
					this.instance.statusService.capture(code, this.internalName, uri, this.context)
				}
			}

			// Pass-through dispatchers
			if (Sincerity.Objects.exists(this.manualPassThroughDocuments)) {
				var exists = false
				for (var name in this.dispatchers) {
					if (name != 'default') {
						exists = true
						break
					}
				}
				if (exists) {
					if (sincerity.verbosity >= 2) {
						println('  Dispatchers:')
					}
					for (var name in this.dispatchers) {
						if (name != 'default') {
							var dispatcher = this.getDispatcher(name)
							this.manualPassThroughDocuments.add(dispatcher.dispatcher)
							if (sincerity.verbosity >= 2) {
								println('    "{0}" -> "{1}"'.cast(name, dispatcher.uri))
							}
						}
					}
				}
			}

			// Hidden URIs
			if (this.hidden.length > 0) {
				if (sincerity.verbosity >= 2) {
					println('  Hidden routes:')
				}
				for (var h in this.hidden) {
					var uri = this.hidden[h]
					if (sincerity.verbosity >= 2) {
						println('    "{0}"'.cast(uri))
					}
					var length = uri.length
					var last = uri[length - 1]
					if (last == '*') {
						uri = uri.substring(0, length - 1)
						this.instance.inboundRoot.hide(uri, Template.MODE_STARTS_WITH)
					}
					else {
						this.instance.inboundRoot.hide(uri)
					}
				}
			}
			
			// crontab
			var crontab = Sincerity.Files.build(this.root, 'crontab')
			if (crontab.exists() && !crontab.directory) {
				var scheduler = component.context.attributes.get('com.threecrickets.prudence.scheduler')
				if (Sincerity.Objects.exists(scheduler)) {
					if (sincerity.verbosity >= 2) {
						println('  Crontab:')
						println('    "{0}"'.cast(sincerity.container.getRelativePath(crontab)))
					}
					var taskCollector = new ApplicationTaskCollector(crontab, this.instance)
					scheduler.addTaskCollector(taskCollector)
					this.globals['com.threecrickets.prudence.taskCollector'] = taskCollector
				}
				else {
					throw new SincerityException('Could not set up crontab because there is no scheduler: ' + crontab) 
				}
			}

			// Use common cache, if exists
			var cache = component.context.attributes.get('com.threecrickets.prudence.cache')
			if (Sincerity.Objects.exists(cache)) {
				this.globals['com.threecrickets.prudence.cache'] = cache
			}

			// Allow access to component
			if (!Sincerity.Objects.ensure(this.settings.isolate, false)) {
				// (This can be considered a security breach, because it allows applications to access other applications)
				this.globals['com.threecrickets.prudence.component'] = component
			}
			
			// Apply globals
			for (var name in this.globals) {
				var value = this.globals[name]
				if (Sincerity.Objects.exists(value)) {
					this.context.attributes.put(name, value)
				}
			}

			// Preheat tasks
			var internal = String(this.hosts.internal).replace(/\//g, '')
			for (var p in this.preheat) {
				var uri = this.preheat[p]
				startupTasks.push(new PreheatTask(internal, uri, this.instance, this.settings.logger))
			}

			// Add to application list
			var applications = component.context.attributes.get('com.threecrickets.prudence.applications')
			if (!Sincerity.Objects.exists(applications)) {
				applications = new CopyOnWriteArrayList()
				var existing = component.context.attributes.putIfAbsent('com.threecrickets.prudence.applications', applications)
				if (Sincerity.Objects.exists(existing)) {
					applications = existing
				}
			}
			applications.add(this.instance)
			
			// Startup task
			if (Sincerity.Files.build(this.root, 'libraries', 'startup').exists()) {
				var applicationService = ApplicationService.create(this.instance)
				initializers.push(function() {
					applicationService.executeTask(null, '/startup/', null, 'initial', 0, 0, false)
				})
			}

			return this.instance
		}
		
		Public.createRestlet = function(restlet, uri) {
			if (Sincerity.Objects.isArray(restlet)) {
				return new Module.Chain({restlets: restlet}).create(this, uri)
			}
			else if (Sincerity.Objects.isString(restlet)) {
				if ((restlet == 'hidden') || (restlet == '!')) {
					return restlet
				}
				else if (restlet[0] == '!') {
					return new Module.Status({code: parseInt(restlet.substring(1))}).create(this, uri)
				}
				else if (restlet[0] == '/') {
					return new Module.Capture({uri: restlet}).create(this, uri)
				}
				else if (restlet[0] == '>') {
					restlet = restlet.substring(1)
					return new Module.Redirect({uri: restlet}).create(this, uri)
				}
				else if (restlet[0] == '@') {
					restlet = restlet.substring(1)
					var colon = restlet.indexOf(':')
					if (colon != -1) {
						var dispatcher = restlet.substring(0, colon)
						var id = restlet.substring(colon + 1)
						return new Module.Dispatch({id: id, dispatcher: dispatcher}).create(this, uri)
					}
					else {
						return new Module.Dispatch({id: restlet}).create(this, uri)
					}
				}
				else if (restlet[0] == '$') {
					restlet = restlet.substring(1)
					return new Module.Resource({'class': restlet}).create(this, uri)
				}
				else {
					var type = Module[Sincerity.Objects.capitalize(restlet)]
					if (Sincerity.Objects.exists(type)) {
						return this.createRestlet({type: restlet}, uri)
					}
					else {
						throw new SincerityException('Unknown routing type: ' + restlet)
						/*var colon = restlet.indexOf(':')
						if (colon != -1) {
							var dispatcher = restlet.substring(0, colon)
							var id = restlet.substring(colon + 1)
							return new Module.Dispatch({id: id, dispatcher: dispatcher}).create(this, uri)
						}
						else {
							return new Module.Dispatch({id: restlet}).create(this, uri)
						}*/
					}
				}
			}
			else if (Sincerity.Objects.isString(restlet.type)) {
				var type = Module[Sincerity.Objects.capitalize(restlet.type)]
				delete restlet.type
				if (Sincerity.Objects.exists(type)) {
					restlet = new type(restlet)
					if (!Sincerity.Objects.exists(restlet.create)) {
						return null
					}
					return restlet.create(this, uri)
				}
			}
			else {
				return restlet.create(this, uri)
			}
		}
		
		Public.createDocumentSource = function(root, preExtension, defaultDocumentName, defaultExtension) {
			importClass(
				com.threecrickets.scripturian.document.DocumentFileSource)

			return new DocumentFileSource(
				'container/' + sincerity.container.getRelativePath(root) + '/',
				root,
				Sincerity.Objects.ensure(defaultDocumentName, this.settings.code.defaultDocumentName),
				Sincerity.Objects.ensure(defaultExtension, this.settings.code.defaultExtension),
				Sincerity.Objects.ensure(preExtension, null),
				this.settings.code.minimumTimeBetweenValidityChecks
			)
		}
		
		Public.defrost = function(documentSource, parserName, plugins) {
			if (true == this.settings.code.defrost) {
				importClass(
						com.threecrickets.scripturian.util.DefrostTask,
						com.threecrickets.scripturian.ParsingContext)

				var parsingContext = new ParsingContext()
				parsingContext.documentSource = documentSource
				parsingContext.languageManager = executable.languageManager
				parsingContext.defaultLanguageTag = this.settings.code.defaultLanguageTag
				parsingContext.parserManager = executable.parserManager
				parsingContext.defaultParser = parserName
				parsingContext.prepare = true
				parsingContext.debug = this.settings.templates.debug ? true : false
				if (Sincerity.Objects.exists(plugins)) {
					parsingContext.attributes.put('com.threecrickets.scripturian.parser.ScriptletsParser.plugins', plugins)
				}
				var tasks = DefrostTask.createMany(parsingContext)
				for (var t in tasks) {
					startupTasks.push(tasks[t])
				}
			}
		}
		
		Public.getDispatcher = function(name) {
			if (!Sincerity.Objects.exists(name)) {
				// Use default
				name = this.dispatchers['default']
				if (!Sincerity.Objects.exists(name)) {
					// No default, so default to 'javascript'
					name = 'javascript'
				}
			}
			
			var dispatcher = Sincerity.Objects.ensure(this.dispatchers[name], '/dispatched/')
			if (Sincerity.Objects.isString(dispatcher)) {
				dispatcher = {resources: dispatcher}
			}
			dispatcher.dispatcher = Sincerity.Objects.ensure(dispatcher.dispatcher, '/prudence/dispatchers/{0}/'.cast(name))
			dispatcher.uri = Module.cleanUri(this.delegatedResourceInternalUri + dispatcher.dispatcher)
			for (var key in dispatcher) {
				if ((key != 'dispatcher') && (key != 'uri')) {
					this.globals['com.threecrickets.prudence.dispatcher.{0}.{1}'.cast(name, key)] = dispatcher[key]
				}
			}
			this.dispatchers[name] = dispatcher
			return dispatcher
		}
		
		Public.createEncoder = function(next) {
			importClass(
					com.threecrickets.prudence.util.CustomEncoder,
					org.restlet.data.MediaType)

			var encoder = new CustomEncoder(this.instance)
			encoder.encoderService.minimumSize = this.settings.compression.sizeThreshold
			var ignoredMediaTypes = encoder.encoderService.ignoredMediaTypes
			ignoredMediaTypes.add(MediaType.APPLICATION_JAVA)
			var exclude = this.settings.compression.exclude
			if (Sincerity.Objects.exists(exclude)) {
				for (var e in exclude) {
					var mediaType = MediaType.valueOf(exclude[e])
					ignoredMediaTypes.add(mediaType)
				}
			}
			encoder.next = next
			return encoder
		}
		
		return Public
	}(Public))

   	/**
   	 * Base class for Prudence-based restlets.
   	 * 
	 * @class
	 * @name Prudence.Setup.Restlet
	 */
	Public.Restlet = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Restlet */
		var Public = {}
		
		Public.create = function(app, uri) {
			return null
		}
		
		return Public
	}(Public))

	/**
	 * Maps every file in a subdirectory to a static RESTful resource.
	 * <p>
	 * Each file represents a single resource.
	 * These resources are called "static" because their representations never change. They are always equal
	 * exactly to the contents of the file. This functionality is identical to that of most static web
	 * servers.
	 * <p>
	 * Note that it also performs as well as most web servers, such that it would usually be unnecessary to
	 * install a separate web server just for static files. However, for true Internet scalability, it would be
	 * best to serve your static files with a CDN (Content Delivery Network) that specializes in and is highly
	 * optimized for serving static files.
	 * <p>
	 * By default, this class will not map directories (they will return 404 errors, even if they exist).
	 * However, this can be changed if you set "listingAllowed" to true, in which case a friendly human-readable
	 * HTML page will be generated for each directory, with a hyperlinked list of its contents.
	 * <p>
	 * You will always want use a wildcard URI template with this class, because it will attempt to match
	 * all URIs to files or directories.
	 * <p>
	 * The class behaves a bit differently according to the size of the "roots" array param. If more than
	 * one element is in the array, then in fact a {@link Prudence.Setup.Chain} is automatically created
	 * around multiple instances. If only one element is in the array, then the chain is avoided. Use
	 * the "root" param as an alternative for specifying a single element. 
	 * <p>
	 * The mapped URI for each file will be its path (relative to the root) appended to the URI
	 * template. For example, if the URI template is "/archive/*", and the root is "/usr/share/web/",
	 * and the file is "/user/share/web/images/logo.png", then the final URI would be "/archive/images/logo.png".
	 * <p>
	 * When "negotiate" is true (the default), then Prudence will handle HTTP content negotiation for you.
	 * The preferred media (MIME) type will be determined by the filename extension. For example, a ".png"
	 * file will have the MIME type "image/png".
	 * Note that each application has its own extension mapping table, which can
	 * be configured in its settings.js, under "settings.mediaTypes".
	 * <p>
	 * Implementation note: Internally handled by a <a href="http://restlet.org/learn/javadocs/2.3/jse/api/index.html?org/restlet/resource/Directory.html">Directory</a> instance.
	 * When "compress" is set to true, inserts a <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/CustomEncoder.html">CustomEncoder</a>
	 * filter before the Directory.
	 * 
	 * @class
	 * @name Prudence.Setup.Static
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param [config]
	 * @param {String|<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/io/File.html">java.io.File</a>} [config.root="resources" subdirectory] The path from which files are searched
	 * @param {String[]|<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/io/File.html">java.io.File</a>[]} [config.roots="resources" container "/libraries/web/" subdirectories] The paths from which files are searched
	 * @param {Boolean} [config.listingAllowed=false] If true will automatically generate HTML pages with directory contents for all mapped subdirectories
	 * @param {Boolean} [config.negotiate=true] If true will automatically handle content negotiation; the preferred media (MIME) type will be determined by the filename extension
	 * @param {Boolean} [config.compress=true] If true will automatically compress files in gzip, zip, deflate or compress encoding if requested by the client (requires "negotiate" to be true)
	 * @param {Number} [config.cacheDuration=settings.code.minimumTimeBetweenValidityChecks]
	 */
	Public.Static = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Static */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['root', 'roots', 'listingAllowed', 'negotiate', 'compress', 'cacheDuration']

		Public.create = function(app, uri) {
			importClass(
				com.threecrickets.prudence.util.Fallback)
				
			this.compress = Sincerity.Objects.ensure(this.compress, true)
			
			if (Sincerity.Objects.exists(this.root)) {
				this.roots = [this.root]
			}
			else {
				this.roots = Sincerity.Objects.ensure(this.roots, ['resources', sincerity.container.getLibrariesFile('web')])
			}
			
			if (this.roots.length == 1) {
				// Single directory
				return createDirectory.call(this, app, this.roots[0])
			}
			else {
				// Chained directories
				this.cacheDuration = Sincerity.Objects.ensure(this.cacheDuration, app.settings.code.minimumTimeBetweenValidityChecks)
				
				var fallback = new Fallback(app.context, this.cacheDuration)
				
				for (var i in this.roots) {
					var restlet = createDirectory.call(this, app, this.roots[i])
					fallback.addTarget(restlet)					
				}
				
				return fallback
			}
		}
		
		//
		// Private
		//
		
		function createDirectory(app, root) {
			importClass(
					org.restlet.resource.Directory,
					java.io.File)

			if (!(root instanceof File)) {
				root = Sincerity.Files.build(app.root, root)
			}

			var restlet = new Directory(app.context, root.toURI().toString())
			restlet.listingAllowed = Sincerity.Objects.ensure(this.listingAllowed, false)
			restlet.negotiatingContent = Sincerity.Objects.ensure(this.negotiate, true)
			
			if (this.compress) {
				// Put a custom encoder before the directory
				restlet = app.createEncoder(restlet)
			}
			
			return restlet
		}
		
		return Public
	}(Public))

	/**
	 * Maps specially marked files in a subdirectory to manual resources.
	 * <p>
	 * Each file represents a single resource.
	 * They are text files containing code in one of the supported programming languages. The language
	 * is determined by the extension: .js for JavaScript, .py for Python, .clj for Clojure, etc.
	 * A set of well-defined global functions (or closures in some languages) is used as entry points for Prudence
	 * to hook into your resource implementation.
	 * <p>
	 * You will almost always want use a wildcard URI template with this class, because it will attempt to match
	 * all URIs to files. However, only files with a ".m." pre-extension will be matched. For example:
	 * "default.m.js", "service.m.js", "profile.m.py". Files without this pre-extension will not be
	 * matched, and will return a 404 error even if they exist. It is thus possible to combine
	 * this class with {@link Prudence.Resources.Static} via a {@link Prudence.Resources.Chain},
	 * though note that this class must be before the static instance in the chain.
	 * <p>
	 * The mapped URI for each file will be its path (relative to the root) appended to the URI
	 * template, <i>without</i> the filename extension but <i>with</i> a trailing slash.
	 * For example, if the URI template is "/info/*", and the root is "/usr/share/web/",
	 * and the file is "/user/share/web/main/about.t.hml", then the final URI would be "/info/main/about/".
	 * <p>
	 * Directories will be mapped only if they contain a file named "default" (with any extension), which
	 * will be used to represent the directory. For example, "/usr/share/web/main/default.m.js" will be mapped
	 * to the URI "/info/main/". Note that the extension does not have to be ".js", and can be of any
	 * supported programming language.
	 * <p>
	 * Important limitation: <i>All</i> uses of this class in the same application share the same
	 * configuration. Only the first found configuration will take hold and will be shared by
	 * other instances. If you try to configure more than one instance of this class, an exception will
	 * be thrown.
	 * <p>
	 * Implementation note: Internally handled by <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/DelegatedResource.html">DelegatedResource</a>
	 * via a <a href="http://restlet.org/learn/javadocs/2.3/jse/api/index.html?org/restlet/resource/Finder.html">Finder</a> instance.
	 * 
	 * @class
	 * @name Prudence.Setup.Manual
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param [config]
	 * @param {String|<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/io/File.html">java.io.File</a>} [config.root="resources" subdirectory] The path from which files are searched
	 * @param {String[]} [config.passThroughs] These documents, though not in the root, will still be exposed (see also {@link document#passThroughDocuments})
	 * @param {String} [config.preExtension='m'] The pre-extension
	 * @param {Boolean} [config.trailingSlashRequired=true] Whether trailing slashses are required
	 * @param {String} [config.internalUri='/_manual/'] The base URI for the internal host
	 * @param {String} [config.clientCachingMode='conditional'] Supports three modes: 'conditional', 'offline', 'disabled'
	 * @param {Number|String} [config.maxClientCachingDuration=-1] In milliseconds, where -1 means no maximum
	 * @param {Boolean} [config.compress=true] If true will automatically compress files in gzip, zip, deflate or compress encoding if requested by the client
	 */
	Public.Manual = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Manual */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['root', 'passThroughs', 'preExtension', 'trailingSlashRequired', 'internalUri', 'clientCachingMode', 'maxClientCachingDuration', 'compress']

		Public.create = function(app, uri) {
			if (!Sincerity.Objects.exists(app.delegatedResource)) {
				importClass(
					com.threecrickets.prudence.internal.CachingUtil,
					org.restlet.resource.Finder,
					java.util.concurrent.CopyOnWriteArraySet,
					java.io.File)

				this.root = Sincerity.Objects.ensure(this.root, 'resources')
				if (!(this.root instanceof File)) {
					this.root = Sincerity.Files.build(app.root, this.root)
				}

				if (Sincerity.Objects.isString(this.clientCachingMode)) {
					if (this.clientCachingMode == 'disabled') {
						this.clientCachingMode = CachingUtil.CLIENT_CACHING_MODE_DISABLED
					}
					else if (this.clientCachingMode == 'conditional') {
						this.clientCachingMode = CachingUtil.CLIENT_CACHING_MODE_CONDITIONAL
					}
					else if (this.clientCachingMode == 'offline') {
						this.clientCachingMode = CachingUtil.CLIENT_CACHING_MODE_OFFLINE
					}
					else {
						throw new SincerityException('Unsupported clientCachingMode: ' + this.clientCachingMode)
					}
				}
				else if (!Sincerity.Objects.exists(this.clientCachingMode)) {
					this.clientCachingMode = CachingUtil.CLIENT_CACHING_MODE_CONDITIONAL
				}

				this.maxClientCachingDuration = Sincerity.Objects.ensure(this.maxClientCachingDuration, -1)
				this.preExtension = Sincerity.Objects.ensure(this.preExtension, 'm')
				this.trailingSlashRequired = Sincerity.Objects.ensure(this.trailingSlashRequired, true)

				this.compress = Sincerity.Objects.ensure(this.compress, true)
				
				app.delegatedResourceInternalUri = Sincerity.Objects.ensure(this.internalUri, '/_manual/')

				if (sincerity.verbosity >= 2) {
					println('    Manual:')
					println('      Root: "{0}"'.cast(sincerity.container.getRelativePath(this.root)))
				}
				
				app.manualPassThroughDocuments = new CopyOnWriteArraySet()

				var delegatedResource = {
					documentSource: app.createDocumentSource(this.root, this.preExtension),
					libraryDocumentSources: app.libraryDocumentSources,
					passThroughDocuments: app.manualPassThroughDocuments,
					defaultName: app.settings.code.defaultDocumentName,
					defaultLanguageTag: app.settings.code.defaultLanguageTag,
					trailingSlashRequired: this.trailingSlashRequired,
					clientCachingMode: this.clientCachingMode,
					maxClientCachingDuration: this.maxClientCachingDuration, 
					languageManager: executable.languageManager,
					parserManager: executable.parserManager,
					sourceViewable: app.settings.code.sourceViewable,
					negotiateEncoding: this.compress,
					encodeSizeThreshold: app.settings.compression.sizeThreshold,
					fileUploadDirectory: app.settings.uploads.root,
					fileUploadSizeThreshold: app.settings.uploads.sizeThreshold,
					debugCaching: app.settings.caching.debug ? true : false,
					defaultCachingKeyTemplate: app.settings.caching.defaultKeyTemplate,
					cachingKeyTemplatePlugins: app.cachingKeyTemplatePlugins
				}

				// Pass-throughs
				if (Sincerity.Objects.exists(this.passThroughs)) {
					for (var i in this.passThroughs) {
						if (sincerity.verbosity >= 2) {
							println('      Pass through: "{0}"'.cast(this.passThroughs[i]))
						}
						delegatedResource.passThroughDocuments.add(this.passThroughs[i])
					}
				}

				// Viewable source
				if (true == app.settings.code.sourceViewable) {
					app.sourceViewableDocumentSources.add(delegatedResource.documentSource)
					app.sourceViewableDocumentSources.addAll(app.libraryDocumentSources)
				}

				// Defrost
				app.defrost(delegatedResource.documentSource, 'program')

				// Merge globals
				Sincerity.Objects.merge(app.globals, Sincerity.Objects.flatten({'com.threecrickets.prudence.DelegatedResource': delegatedResource}))

				app.delegatedResource = new Finder(app.context, Sincerity.JVM.getClass('com.threecrickets.prudence.DelegatedResource'))
			}
			else if (Sincerity.Objects.exists(this.root) || Sincerity.Objects.exists(this.passThroughs) || Sincerity.Objects.exists(this.preExtension) || Sincerity.Objects.exists(this.pretrailingSlashRequired) || Sincerity.Objects.exists(this.internalUri)) {
				throw new SincerityException('You can configure a Manual only once per application')
			}

			return app.delegatedResource
		}
		
		return Public
	}(Public))

	/**
	 * Maps specially marked files in a subdirectory to template resources.
	 * <p>
	 * Each file represents a single resource.
	 * These resources work best with textual formats such as HTML, XML, JSON and
	 * plain text. They combine "raw" textual output with specially delimited code segments
	 * called "scriptlets" that are executed for every user request. This functionality
	 * is identical in many ways to PHP, JSP and ASP, but Prudence goes beyond those
	 * platforms by providing fine-tuned integrated caching, and supporting several
	 * programming and templating languages.
	 * <p>
	 * You will almost always want use a wildcard URI template with this class, because it will attempt to match
	 * all URIs to files. However, only files with a ".t." pre-extension will be matched. For example:
	 * "index.t.html", "sitemap.t.xml", "info.t.json". Files without this pre-extension will not be
	 * matched, and will return a 404 error even if they exist. It is thus possible to combine
	 * this class with {@link Prudence.Resources.Static} via a {@link Prudence.Resources.Chain},
	 * though note that this class must be before the static instance in the chain.
	 * <p>
	 * The mapped URI for each file will be its path (relative to the root) appended to the URI
	 * template, <i>without</i> the filename extension but <i>with</i> a trailing slash.
	 * For example, if the URI template is "/info/*", and the root is "/usr/share/web/",
	 * and the file is "/user/share/web/main/about.t.hml", then the final URI would be "/info/main/about/".
	 * <p>
	 * Directories will be mapped only if they contain a file named "index" (with any extension), which
	 * will be used to represent the directory. For example, "/usr/share/web/main/index.t.html" will be mapped
	 * to the URI "/info/main/". Note that the extension does not have to be ".html", and can be of any
	 * textual format.
	 * <p>
	 * Prudence supports powerful server-side caching for template resources. See {@link conversation#cacheDuration}
	 * for the most important API for enabling caching. Client-side caching is implemented to match the
	 * server-side cache, and can work in three modes, according to the "clientCachingMode" param.
	 * <p>
	 * Prudence will handle HTTP content negotiation for you.
	 * The preferred media (MIME) type will be determined by the filename extension. For example, a ".t.html"
	 * file will have the MIME type "text/html".
	 * Note that each application has its own extension mapping table, which can
	 * be configured in its settings.js, under "settings.mediaTypes".
	 * <p>
	 * Important limitation: <i>All</i> uses of this class in the same application share the same
	 * configuration. Only the first found configuration will take hold and will be shared by
	 * other instances. If you try to configure more than one instance of this class, an exception will
	 * be thrown.
	 * <p>
	 * Implementation note: Internally handled by <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/GeneratedTextResource.html">GeneratedTextResource</a>
	 * via a <a href="http://restlet.org/learn/javadocs/2.3/jse/api/index.html?org/restlet/resource/Finder.html">Finder</a> instance.
	 * 
	 * @class
	 * @name Prudence.Setup.Templates
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param [config]
	 * @param {String|<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/io/File.html">java.io.File</a>} [config.root='resources'] The path from which files are searched
	 * @param {String|<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/io/File.html">java.io.File</a>} [config.includeRoot='libraries/includes']
	 * @param {String[]} [config.passThroughs] These documents, though not in the root, will still be exposed (see also {@link document#passThroughDocuments})
	 * @param {String} [config.preExtension='t'] The pre-extension
	 * @param {Boolean} [config.trailingSlashRequired=true] Whether trailing slashses are required
	 * @param {String} [config.defaultDocumentName='index'] The default document name for directories
	 * @param {String} [config.defaultExtension='html'] The preferred filename extension
	 * @param {String} [config.clientCachingMode='conditional'] Supports three modes: 'conditional', 'offline', 'disabled'
	 * @param {Number|String} [config.maxClientCachingDuration=-1] In milliseconds, where -1 means no maximum
	 * @param {Boolean} [config.compress=true] If true will automatically compress files in gzip, zip, deflate or compress encoding if requested by the client (requires "negotiate" to be true)
	 * @param {Object} [config.plugins] Template plugins
	 */
	Public.Templates = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Templates */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['root', 'includeRoot', 'passThroughs', 'preExtension', 'trailingSlashRequired', 'defaultDocumentName', 'defaultExtension', 'clientCachingMode', 'maxClientCachingDuration', 'compress', 'plugins']

		Public.create = function(app, uri) {
			if (!Sincerity.Objects.exists(app.generatedTextResource)) {
				importClass(
					com.threecrickets.prudence.GeneratedTextResource,
					com.threecrickets.prudence.util.PhpExecutionController,
					com.threecrickets.prudence.internal.CachingUtil,
					org.restlet.resource.Finder,
					java.util.concurrent.CopyOnWriteArrayList,
					java.util.concurrent.CopyOnWriteArraySet,
					java.util.concurrent.ConcurrentHashMap,
					java.io.File)
					
				this.root = Sincerity.Objects.ensure(this.root, 'resources')
				if (!(this.root instanceof File)) {
					this.root = Sincerity.Files.build(app.root, this.root)
				}
				
				this.includeRoot = Sincerity.Objects.ensure(this.includeRoot, ['libraries', 'includes'])
				if (!(this.includeRoot instanceof File)) {
					this.includeRoot = Sincerity.Files.build(app.root, this.includeRoot)
				}

				if (Sincerity.Objects.isString(this.clientCachingMode)) {
					if (this.clientCachingMode == 'disabled') {
						this.clientCachingMode = CachingUtil.CLIENT_CACHING_MODE_DISABLED
					}
					else if (this.clientCachingMode == 'conditional') {
						this.clientCachingMode = CachingUtil.CLIENT_CACHING_MODE_CONDITIONAL
					}
					else if (this.clientCachingMode == 'offline') {
						this.clientCachingMode = CachingUtil.CLIENT_CACHING_MODE_OFFLINE
					}
					else {
						throw new SincerityException('Unsupported clientCachingMode: ' + this.clientCachingMode)
					}
				}
				else if (!Sincerity.Objects.exists(this.clientCachingMode)) {
					this.clientCachingMode = CachingUtil.CLIENT_CACHING_MODE_CONDITIONAL
				}

				this.maxClientCachingDuration = Sincerity.Objects.ensure(this.maxClientCachingDuration, -1)
				this.defaultDocumentName = Sincerity.Objects.ensure(this.defaultDocumentName, 'index')
				this.defaultExtension = Sincerity.Objects.ensure(this.defaultExtension, 'html')
				this.preExtension = Sincerity.Objects.ensure(this.preExtension, 't')
				this.trailingSlashRequired = Sincerity.Objects.ensure(this.trailingSlashRequired, true)

				this.maxClientCachingDuration = Sincerity.Localization.toMilliseconds(this.maxClientCachingDuration)

				this.compress = Sincerity.Objects.ensure(this.compress, true)
				
				if (sincerity.verbosity >= 2) {
					println('    Templates:')
					println('      Root: "{0}"'.cast(sincerity.container.getRelativePath(this.root)))
				}

				var generatedTextResource = {
					documentSource: app.createDocumentSource(this.root, this.preExtension, this.defaultDocumentName, this.defaultExtenion),
					extraDocumentSources: new CopyOnWriteArrayList(),
					libraryDocumentSources: app.libraryDocumentSources,
					passThroughDocuments: new CopyOnWriteArraySet(),
					trailingSlashRequired: this.trailingSlashRequired,
					clientCachingMode: this.clientCachingMode,
					maxClientCachingDuration: this.maxClientCachingDuration, 
					defaultIncludedName: this.defaultDocumentName,
					executionController: new PhpExecutionController(), // Adds PHP predefined variables
					languageManager: executable.languageManager,
					parserManager: executable.parserManager,
					sourceViewable: app.settings.code.sourceViewable,
					negotiateEncoding: this.compress,
					encodeSizeThreshold: app.settings.compression.sizeThreshold,
					fileUploadDirectory: app.settings.uploads.root,
					fileUploadSizeThreshold: app.settings.uploads.sizeThreshold,
					scriptletPlugins: new ConcurrentHashMap(),
					debug: app.settings.templates.debug ? true : false,
					debugCaching: app.settings.caching.debug ? true : false,
					defaultCachingKeyTemplate: app.settings.caching.defaultKeyTemplate,
					cachingKeyTemplatePlugins: app.cachingKeyTemplatePlugins
				}

				// Libraries
				if (Sincerity.Objects.exists(this.includeRoot)) {
					if (sincerity.verbosity >= 2) {
						println('      Includes: "{0}"'.cast(sincerity.container.getRelativePath(this.includeRoot)))
					}
					generatedTextResource.extraDocumentSources.add(app.createDocumentSource(this.includeRoot, null, this.defaultDocumentName, this.defaultExtenion))
				}

				// Common libraries
				if (!Sincerity.Objects.exists(app.commonTemplatesDocumentSource)) {
					var library = sincerity.container.getFile('libraries', 'prudence-includes')
					app.commonTemplatesDocumentSource = app.createDocumentSource(library, null, this.defaultDocumentName, this.defaultExtenion)
					app.commonTemplatesDocumentSource = app.commonTemplatesDocumentSource
				}

				if (sincerity.verbosity >= 2) {
					println('      Common includes: "{0}"'.cast(sincerity.container.getRelativePath(app.commonTemplatesDocumentSource.basePath)))
				}
				generatedTextResource.extraDocumentSources.add(app.commonTemplatesDocumentSource)

				// Viewable source
				if (true == app.settings.code.sourceViewable) {
					app.sourceViewableDocumentSources.add(generatedTextResource.documentSource)
					app.sourceViewableDocumentSources.addAll(generatedTextResource.extraDocumentSources)
				}
				
				// Pass-throughs
				if (Sincerity.Objects.exists(this.passThroughs)) {
					for (var i in this.passThroughs) {
						if (sincerity.verbosity >= 2) {
							println('      Pass through: "{0}"'.cast(this.passThroughs[i]))
						}
						generatedTextResource.passThroughDocuments.add(this.passThroughs[i])
					}
				}

				// Scriptlet plugins
				for (var code in app.settings.templates.plugins) {
					if (sincerity.verbosity >= 2) {
						println('      Scriptlet plugin: "{0}" -> "{1}"'.cast(code, app.settings.templates.plugins[code]))
					}
					generatedTextResource.scriptletPlugins.put(code, app.settings.templates.plugins[code])
				}
				
				// Defrost
				app.defrost(generatedTextResource.documentSource, app.settings.templates.parser, generatedTextResource.scriptletPlugins)

				// Merge globals
				Sincerity.Objects.merge(app.globals, Sincerity.Objects.flatten({'com.threecrickets.prudence.GeneratedTextResource': generatedTextResource}))
				
				app.generatedTextResource = new Finder(app.context, Sincerity.JVM.getClass('com.threecrickets.prudence.GeneratedTextResource'))
			}
			else if (Sincerity.Objects.exists(this.root) || Sincerity.Objects.exists(this.includeRoot) || Sincerity.Objects.exists(this.passThroughs) || Sincerity.Objects.exists(this.preExtension) || Sincerity.Objects.exists(this.pretrailingSlashRequired) || Sincerity.Objects.exists(this.defaultDocumentName) || Sincerity.Objects.exists(this.defaultExtension) || Sincerity.Objects.exists(this.clientCachingMode) || Sincerity.Objects.exists(thismaxClientCachingDuration)) {
				throw new SincerityException('You can configure a Templates only once per application')
			}
			
			return app.generatedTextResource
		}
		
		return Public
	}(Public))

	/**
	 * This powerful (and dangerous) resource executes all POST payloads as Scripturian text-with-scriptlets
	 * documents. The standard output of the script will be returned as a response.
	 * <p>
	 * Because it allows execution of arbitrary code, you very likely do not want this resource publicly
	 * exposed. If you use it, make sure to protect its URL on publicly available machines!
	 * <p>
	 * The default response media type is "text/plain", but you can modify it with the {@link conversation#mediaType} API.
	 * <p>
	 * Example use with curl command line:
	 * <pre>
	 * curl -d "<% println(1+2) %>" "http://localhost:8080/myapp/execute/"
	 * </pre>
	 * Note that if you use curl with a file, you need to send it as binary, otherwise curl
	 * will strip your newlines:
	 * <pre>
	 * curl --data-binary @myscriptfile "http://localhost:8080/myapp/execute/"
	 * </pre>
	 * <p>
	 * Implementation note: Internally handled by <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/ExecutionResource.html">ExecutionResource</a>
	 * via a <a href="http://restlet.org/learn/javadocs/2.3/jse/api/index.html?org/restlet/resource/Finder.html">Finder</a> instance.
	 * 
	 * @class
	 * @name Prudence.Setup.Execute
	 * @augments Prudence.Setup.Restlet
	 */
	Public.Execute = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Execute */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		Public.create = function(app, uri) {
			if (!Sincerity.Objects.exists(app.executionResource)) {
				importClass(
					com.threecrickets.prudence.util.PhpExecutionController,
					org.restlet.resource.Finder)
					
				var executionResource = {
					documentSource: app.libraryDocumentSources.get(0),
					libraryDocumentSources: app.libraryDocumentSources,
					defaultName: app.settings.code.defaultDocumentName,
					defaultLanguageTag: app.settings.code.defaultLanguageTag,
					trailingSlashRequired: this.trailingSlashRequired,
					executionController: new PhpExecutionController(), // Adds PHP predefined variables
					languageManager: executable.languageManager,
					parserManager: executable.parserManager,
					sourceViewable: app.settings.code.sourceViewable,
					fileUploadDirectory: app.settings.uploads.root,
					fileUploadSizeThreshold: app.settings.uploads.sizeThreshold
				}

				// Merge globals
				Sincerity.Objects.merge(app.globals, Sincerity.Objects.flatten({'com.threecrickets.prudence.ExecutionResource': executionResource}))
				
				app.executionResource = new Finder(app.context, Sincerity.JVM.getClass('com.threecrickets.prudence.ExecutionResource'))
			}
			
			return app.executionResource
		}
		
		return Public
	}(Public))

	/**
	 * Routes to a resource implementation via a dispatcher, which is a manual resource in
	 * charge of forwarding entry point calls to the appropriate handlers.
	 * <p>
	 * This means that your application <i>must</i> have a {@link Prudence.Setup.Manual} configured for
	 * dispatching to work.
	 * <p>
	 * Prudence supports a special short-form notation for configuring this class: '@dispatcher:id'
	 * or just '@id' (where "dispatcher" defaults to "javascript"). For example, "@clojure:record"
	 * is equivalent to {type: 'dispatch', id: 'record', dispatcher: 'clojure'}.
	 * <p>
	 * This class will automatically inject a "prudence.dispatcher.id" local to the dispatcher, but
	 * you can inject your own custom locals using the "locals" param.
	 * <p>
	 * The dispatcher configured here must be defined in the app.dispatchers dict. If not specified, it will
	 * default to "javascript", for which the default document name is "/prudence/dispatchers/javascript/".
	 * Indeed, Prudence comes with default dispatchers for all supported programming languages. However, you
	 * are free to create your own custom dispatcher and specify it in app.dispatchers.
	 * <p>
	 * Implementation note: Internally handled by a <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/CapturingRedirector.html">CapturingRedirector</a> instance
	 * with an <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/Injector.html">Injector</a>
	 * before it.
	 *
	 * @class
	 * @name Prudence.Setup.Dispatch
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param config
	 * @param {String} config.id The dispatch ID
	 * @param {String} [config.dispatcher] The dispatcher; defaults to app.dispatchers['default'] or 'javascript'
	 * @param {Object} [config.locals] The values to inject into conversation.locals
	 */
	Public.Dispatch = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Dispatch */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['id', 'dispatcher', 'locals']

		Public.create = function(app, uri) {
			importClass(
				com.threecrickets.prudence.util.Injector,
				com.threecrickets.prudence.util.CapturingRedirector,
				org.restlet.routing.Template)
				
			/*if (!Sincerity.Objects.exists(app.delegatedResource)) {
				throw new SincerityException('A Manual must be attached before a Dispatch can be created')
	   		}*/
				
			var dispatcher = app.getDispatcher(this.dispatcher)
	   		var capture = new CapturingRedirector(app.context, 'riap://application' + dispatcher.uri + '?{rq}')
			var injector = new Injector(app.context, capture)
			injector.values.put('com.threecrickets.prudence.dispatcher.id', new Template(this.id))

			// Extra locals
			if (Sincerity.Objects.exists(this.locals)) {
				for (var i in this.locals) {
					injector.values.put(i, this.locals[i])
				}
			}
   
			return injector
		}
		
		return Public
	}(Public))

	/**
	 * A server-side redirector that preserves the original URI used by the client (called the "captured URI").
	 * <p>
	 * Because the new request created by the redirector is actually an internal request, it can reach
	 * hidden URIs. It thus allows you to "cloak" an existing URI while still allowing access to the resource via
	 * a new one. A common use case is to expose a URI template instead of a URI that is not a template. 
	 * <p>
	 * If the target URI template ends with a "!", it is specially interpreted to mean that the "hidden"
	 * param is true (and the "!" is not actually include in the target). Hiding is actually
	 * not handled by this class, but rather the {@link Prudence.Setup.Router}, but is available
	 * here as convenient shortcut for the commonly used capture-and-hide paradigm. 
	 * Note that you should not use the capture-and-hide trick with targets that include URI template
	 * variables, because Prudence can only hide exact URI matches. Use explicit hiding instead
	 * for the URIs you want hidden.
	 * <p>
	 * Prudence supports a special short-form notation for configuring this class: a configuration
	 * string starting with a "/" will be considered as a URI for a capture. For example, '/target/!'
	 * is equivalent to {type: 'capture', uri: '/target/', hidden: true}.
	 * <p>
	 * Note that the target URI is in the URI template format, and can use variables from the
	 * request. Not only that, but you can also use a host of Restlet-specific special variables:
	 * see the <a href="http://restlet.org/learn/javadocs/2.3/jse/api/index.html?org/restlet/util/Resolver.html">Resolver</a>
	 * documentation for a complete list.
	 * <p>
	 * Optionally supports value injection via the "locals" param.
	 * <p>
	 * Implementation note: Internally handled by a <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/CapturingRedirector.html">CapturingRedirector</a> instance.
	 * If "locals" is defined, inserts an <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/Injector.html">Injector</a>
	 * filter before the CapturingRedirector.
	 *
	 * @class
	 * @name Prudence.Setup.Capture
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param config
	 * @param {String} config.uri The target URI template
	 * @param {String} [config.application] The application to capture to, or null for the current application
	 * @param {Boolean} [config.hidden=false] Whether to also hide the target URI template (defaults to true when the URI ends with '!')
	 * @param {Object} [config.locals] The values to inject into conversation.locals
	 */
	Public.Capture = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Capture */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['uri', 'application', 'hidden', 'locals']

		Public.create = function(app, uri) {
			importClass(
				com.threecrickets.prudence.util.Injector,
				com.threecrickets.prudence.util.CapturingRedirector)

			if (this.uri.endsWith('!')) {
				this.uri = this.uri.substring(0, this.uri.length - 1)
				this.hidden = true
			}
			
	   		var capture = Sincerity.Objects.exists(this.application) ?
	   			new CapturingRedirector(app.context, 'riap://component/' + this.application + this.uri + '?{rq}') :
	   			new CapturingRedirector(app.context, 'riap://application' + this.uri + '?{rq}')

			if (Sincerity.Objects.exists(this.locals)) {
				var injector = new Injector(app.context, capture)

				for (var i in this.locals) {
					injector.values.put(i, this.locals[i])
				}
				
				capture = injector
			}
			
			if (true == this.hidden) {
				app.hidden.push(this.uri)
			}
   
			return capture
		}
		
		return Public
	}(Public))

	/**
	 * Routes client requests according to URI templates.
	 * <p>
	 * The "routes" param is a dict mapping URI templates to nested route type configurations.
	 * <p>
	 * Prudence using this class (with its default configuration) for the basis of app.routes.
	 * However, you may nest further router configratations under app.routes to gain more control
	 * over routing.
	 * <p>
	 * By default, if multiple URI templates in the "routes" param match a request, the best one is
	 * selected. Generally, URIs that do not use template variables will score higher than URI
	 * templates. You can change the selection behavior via the "routingMode" param:
	 * <ul>
	 * <li><b>best</b>: select the best matching route</li>
	 * <li><b>first</b>: select the first matching route</li>
	 * <li><b>last</b>: select the last matching route</li>
	 * <li><b>random</b>: randomly select one of the matching routes</li>
	 * <li><b>next</b>: keep track of selected matching routes, and use the next one in order per each request (round-robbin)</li>
	 * </ul>
	 * <p>
	 * Implementation note: Internally handled by a <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/PrudenceRouter.html">PrudenceRouter</a> instance.
	 *
	 * @class
	 * @name Prudence.Setup.Router
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param [config]
	 * @param {Object} [config.routes] A dict matching URI templates to route configurations
	 * @param {String} [config.routingMode='best'] Either 'best', 'first', 'last', 'random' or 'next'
	 * @param {Number} [config.cacheDuration=settings.code.minimumTimeBetweenValidityChecks] Time in milliseconds for cached fallback results
	 */
	Public.Router = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Router */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['routes', 'routingMode', 'cacheDuration']

		Public.create = function(app, uri) {
			importClass(
				com.threecrickets.prudence.PrudenceRouter,
				org.restlet.routing.Template,
				org.restlet.routing.Router)
			
			this.cacheDuration = Sincerity.Objects.ensure(this.cacheDuration, app.settings.code.minimumTimeBetweenValidityChecks)

			var router = new PrudenceRouter(app.context, this.cacheDuration)
			
			if (Sincerity.Objects.isString(this.routingMode)) {
				if (this.routingMode == 'best') {
					this.routingMode = Router.MODE_BEST_MATCH					
				}
				else if (this.routingMode == 'custom') {
					this.routingMode = Router.MODE_CUSTOM_MATCH					
				}
				else if (this.routingMode == 'first') {
					this.routingMode = Router.MODE_FIRST_MATCH					
				}
				else if (this.routingMode == 'last') {
					this.routingMode = Router.MODE_LAST_MATCH					
				}
				else if (this.routingMode == 'next') {
					this.routingMode = Router.MODE_NEXT_MATCH					
				}
				else if (this.routingMode == 'random') {
					this.routingMode = Router.MODE_RANDOM_MATCH					
				}
			}
			if (!Sincerity.Objects.exists(this.routingMode)) {
				this.routingMode = Router.MODE_BEST_MATCH					
			}
			
			router.routingMode = this.routingMode
			
			// Create and attach restlets
			if (Sincerity.Objects.exists(this.routes)) {
				for (var uri in this.routes) {
					var restlet = this.routes[uri]

					var attachBase = false
					var length = uri.length
					if (length > 1) {
						var last = uri[length - 1]
						if (last == '*') {
							uri = uri.substring(0, length - 1)
							attachBase = true
						}
					}
					
					uri = Module.cleanUri(uri)

					restlet = app.createRestlet(restlet, uri)
					if (Sincerity.Objects.exists(restlet)) {
						if ((restlet == 'hidden') || (restlet == '!')) {
							if (sincerity.verbosity >= 2) {
								println('    "{0}" hidden'.cast(uri))
							}
							var length = uri.length
							var last = uri[length - 1]
							if (last == '*') {
								uri = uri.substring(0, length - 1)
								router.hide(uri, Template.MODE_STARTS_WITH)
							}
							else {
								router.hide(uri)
							}
						}
						else if (attachBase) {
							if (sincerity.verbosity >= 2) {
								println('    "{0}*" -> {1}'.cast(uri, restlet))
							}
							router.attachBase(uri, restlet)
						}
						else {
							if (sincerity.verbosity >= 2) {
								println('    "{0}" -> {1}'.cast(uri, restlet))
							}
							router.attach(uri, restlet).matchingMode = Template.MODE_EQUALS
						}
					}
					else {
						throw new SincerityException('Unsupported route type for route "{0}"'.cast(uri))
					}
				}
			}

			return router
		}
		
		return Public
	}(Public))
	
	/**
	 * Tries multiple route types in order. The first route type that does not return a "not found" (404)
	 * error will be used for the client request.
	 * <p>
	 * This class is often used with a wildcard URI template, but can be used with and URI template,
	 * and even exact URIs.
	 * <p> 
	 * The route will be cached for each particular successful URI in to improve performance and scalability
	 * by avoiding retrying the routes in order again. The duration of this cache can be configured via the
	 * "cacheDuration" param. Note that caching is enabled by default, but this behavior may not always be
	 * appropriate! For example, if resources may appear and disappear per request, you may want to disable
	 * caching and try all chained routes in order for every request. In that case, set "cacheDuration"
	 * to zero.  
	 * <p>
	 * Prudence supports a special short-form notation for configuring this class: JavaScript
	 * arrays are considered as chains. For example, ['scriptlet', 'static'] is equivalent to
	 * {type: 'chain': restlets: ['scriptlet', 'static']}.
	 * <p>
	 * Implementation note: Internally handled by a <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/Fallback.html">Fallback</a> instance.
	 *
	 * @class
	 * @name Prudence.Setup.Chain
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param [config]
	 * @param {Array} [config.restlets] The route configurations to chain
	 * @param {Number} [config.cacheDuration=settings.code.minimumTimeBetweenValidityChecks] Time in milliseconds for cached fallback results
	 */
	Public.Chain = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Chain */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['restlets', 'cacheDuration']

		Public.create = function(app, uri) {
			importClass(com.threecrickets.prudence.util.Fallback)
			
			this.cacheDuration = Sincerity.Objects.ensure(this.cacheDuration, app.settings.code.minimumTimeBetweenValidityChecks)
			
			var fallback = new Fallback(app.context, this.cacheDuration)
			
			if (Sincerity.Objects.exists(this.restlets)) {
				for (var i in this.restlets) {
					var restlet = app.createRestlet(this.restlets[i], uri)
					if (Sincerity.Objects.exists(restlet)) {
						fallback.addTarget(restlet)					
					}
				}
			}
			
			return fallback
		}
		
		return Public
	}(Public))
	
	/**
	 * Asks the client to redirect its request (repeat it) to another URI.
	 * <p>
	 * The target URI may be a URI template, in which case variables will be filled based on the request.
	 * This allows you to elegantly configure dynamic redirection.
	 * <p>
	 * Note that you should prefer use <i>absolute URIs</i> for the target, never relative URIs.
	 * For example, use 'http://mysite.org/myapp/new/uri/' rather than '/new/uri/'.
	 * Relative URIs are handled inconsistently by clients: some treat them as relative to
	 * the requested URI, some relative to the domain name, and others may attempt to treat
	 * any URI as if it was an absolute URI, leading to an error.
	 * <p>
	 * Dynamic redirection thus is especially useful if you want to redirect within your Prudence
	 * component's URI-space, because you do not have to hardcode the absolute target URI. For example,
	 * in a target URI such as '{oi}/new/uri/', '{oi}' may expand to 'http://mysite.org/myapp', giving
	 * you a dynamically constructed absolute URI. This will work correctly even if your application is
	 * attached to multiple virtual hosts.
	 * <p>
	 * The default redirection mode is 'permanent', which uses HTTP status code 301.
	 * You can set the "mode" param to any of the following, either by string or by the
	 * HTTP status code:
	 * <ul>
	 * <li>permanent: HTTP status code 301</li>
	 * <li>found: HTTP status code 302</li>
	 * <li>seeOther: HTTP status code 303</li>
	 * <li>temporary: HTTP status code 307</li>
	 * </ul> 
	 * Note that you do not have control over whether the client will repeat its request:
	 * most web browsers will honor this status, but some clients may not. 
	 * Furthermore, clients may or may not cache the 'permament' (301) redirection
	 * information.
	 * <p>
	 * Prudence supports a special short-form notation for configuring this class: any string
	 * beginning with '>' followed by the target URI. For example, '>{oi}/new/uri/' is equivalent
	 * to {type: 'redirect', 'uri': '{oi}/new/uri/'}.
	 * <p>
	 * Note that Prudence also supports programmatic redirection in manual and scriptlet resources
	 * via the {@link conversation#redirectPermament}, {@link conversation#redirectSeeOther} and
	 * {@link conversation#redirectTemporary}.
	 * <p>
	 * Implementation note: Internally handled by a <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/ResolvingRedirector.html">ResolvingRedirector</a> instance.
	 *
	 * @class
	 * @name Prudence.Setup.Redirect
	 * @augments Prudence.Setup.Restlet
	 *
	 * @param config
	 * @param {String} config.uri The target URI (must be absolute, not relative)
	 * @param {String|Number} [config.mode='permanent'] Either 'permanent'/301, 'found'/302, 'seeOther'/303 or 'temporary'/307
	 */
	Public.Redirect = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Redirect */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['uri', 'mode']

		Public.create = function(app, uri) {
			importClass(
				com.threecrickets.prudence.util.ResolvingRedirector)
			
			this.mode = Sincerity.Objects.ensure(this.mode, 'permanent')
			if ((this.mode == 301) || (this.mode == 'permanent')) {
				this.mode = ResolvingRedirector.MODE_CLIENT_PERMANENT
			}
			else if ((this.mode == 302) || (this.mode == 'found')) {
				this.mode = ResolvingRedirector.MODE_CLIENT_FOUND
			}
			else if ((this.mode == 303) || (this.mode == 'seeOther')) {
				this.mode = ResolvingRedirector.MODE_CLIENT_SEE_OTHER
			}
			else if ((this.mode == 307) || (this.mode == 'temporary')) {
				this.mode = ResolvingRedirector.MODE_CLIENT_TEMPORARY
			}
			else if (this.mode == 'serverOutbound') {
				this.mode = ResolvingRedirector.MODE_SERVER_OUTBOUND
			}
			else if (this.mode == 'serverInbound') {
				this.mode = ResolvingRedirector.MODE_SERVER_INBOUND
			}

	   		var redirector = new ResolvingRedirector(app.context, this.uri, this.mode, false)

			return redirector
		}
		
		return Public
	}(Public))

	/**
	 * Asks the client to permanently redirect the request (repeat it) to the incoming URI with
	 * a trailing slash added to it. This effect is identical to {type: 'redirect', uri: '{ri}/'}.
	 * <p>
	 * Commonly used as a courtesy for clients who may erroneously not include a trailing
	 * slash even when a trailing slash is required by your URI-space.
	 * <p>
	 * Sends HTTP status code 301 to the client.
	 * <p>
	 * Note that you do not have control over whether the client will repeat its request:
	 * most web browsers will honor this status, but some clients may not.
	 * Furthermore, clients may or may not cache the permament redirection
	 * information.
	 * <p>
	 * Implementation note: Internally handled by a <a href="http://restlet.org/learn/javadocs/2.3/jse/api/index.html?org/restlet/routing/Redirector.html">Redirector</a>
	 * singleton instance shared by all usages in the application.
	 * 
	 * @class
	 * @name Prudence.Setup.AddSlash
	 * @augments Prudence.Setup.Restlet 
	 */
	Public.AddSlash = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.AddSlash */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		Public.create = function(app, uri) {
			return app.addTrailingSlashRedirector
		}
		
		return Public
	}(Public))

	/**
	 * This class lets you hook low-level implementations of Restlet resources to Prudence.
	 * <p>
	 * The "class" param (note that "class" is a reserved word in JavaScript and must be placed
	 * in quotes for a dict key) must be the full classname of a JVM class that inherits
	 * from <a href="http://restlet.org/learn/javadocs/2.3/jse/api/index.html?org/restlet/resource/ServerResource.html">org.restlet.resource.ServerResource</a>.
	 * <p>
	 * These classes are often written in Java, but can be written in any language that can
	 * produce JVM classes, such as Groovy, Clojure, etc. 
	 * <p>
	 * Prudence supports a special short-form notation for configuring this class: any string
	 * beginning with '$' followed by the class name. For example, '$org.myapp.PersonResource' is equivalent
	 * to {type: 'resource', 'class': 'org.myapp.PersonResource'}.
	 * <p>
	 * Implementation note: Internally handled by a <a href="http://restlet.org/learn/javadocs/2.3/jse/api/index.html?org/restlet/resource/Finder.html">Finder</a> instance.
	 *
	 * @class
	 * @name Prudence.Setup.Resource
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param config
	 * @param {String} config.class The JVM class name (must inherit from http://restlet.org/learn/javadocs/2.3/jse/api/index.html?org/restlet/resource/ServerResource.html">ServerResource</a>)
	 */
	Public.Resource = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Resource */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['class']

		Public.create = function(app, uri) {
			importClass(org.restlet.resource.Finder)
			
			var theClass = Sincerity.JVM.getClass(this['class'])
			if (null === theClass) {
				throw new SincerityException('Cannot load class: ' + this['class'])
			}
			return new Finder(app.context, theClass)
		}
		
		return Public
	}(Public))

	/**
	 * Filters execute arbitrary code for each request, potentially modifying the request and/or
	 * the response, and can additionally control whether the request continues to the next route
	 * type or not.
	 * <p>
	 * The "next" param <i>must</i> be provided and is a nested route type.
	 * Several filters can be thus chained together.
	 * <p>
	 * The "library" param specifies the filter implementation, which is a document
	 * name relative to the application's "/libraries/" subdirectory.
	 * It is a text file containing code in one of the supported programming languages. The language
	 * is determined by the extension: .js for JavaScript, .py for Python, .clj for Clojure, etc.
	 * A set of well-defined global functions (or closures in some languages) is used as entry points for Prudence
	 * to hook into your filter implementation.
	 * <p>
	 * Implementation note: Internally handled by a <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/DelegatedFilter.html">DelegatedFilter</a> instance.
	 *
	 * @class
	 * @name Prudence.Setup.Filter
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param config
	 * @param {String} config.library The filter library's document URI
	 * @param {Object} config.next The next route configuration
	 */
	Public.Filter = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Filter */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['library', 'next']

		Public.create = function(app, uri) {
			importClass(com.threecrickets.prudence.DelegatedFilter)
			
			this.next = app.createRestlet(this.next, uri)
			var filter = new DelegatedFilter(app.context, this.next, this.library)
			
			return filter
		}
		
		return Public
	}(Public))

	/**
	 * A simple filter that injects preset values into {@link conversation#locals}
	 * before continuing to the next route type.
	 * <p>
	 * This is useful for Inversion of Control (IoC): you can use these conversation.locals
	 * to alter the behavior of nested route types directly in your routing.js.
	 * <p>
	 * Implementation note: Internally handled by an <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/Injector.html">Injector</a> instance.
	 *
	 * @class
	 * @name Prudence.Setup.Injector
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param config
	 * @param {Object} [config.locals] The values to inject into conversation.locals
	 * @param {Object} config.next The next route configuration
	 */
	Public.Injector = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Injector */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['locals', 'next']

		Public.create = function(app, uri) {
			importClass(com.threecrickets.prudence.util.Injector)
					
			this.next = app.createRestlet(this.next, uri)
			var injector = new Injector(app.context, this.next)

			// Locals
			if (Sincerity.Objects.exists(this.locals)) {
				for (var i in this.locals) {
					injector.values.put(i, this.locals[i])
				}
			}
   
			return injector
		}
		
		return Public
	}(Public))

	/**
	 * A powerful filter that unifies and optionally minifies
	 * client-side JavaScript files on demand, allowing for improved performance
	 * and scalability.
	 * <p>
	 * By default, JavaScript files are expected to be found under the "/resources/scripts/"
	 * subdirectory of your application, as well as the "/libraries/web/scripts/" subdirectory
	 * of your Sincerity container. These locations can be changed via the "roots" param.
	 * <p>
	 * Note that the first entry in the "roots" is special: it is where the unified/minified files
	 * will be stored.
	 * <p>
	 * This class works by intercepting requested URIs. If the URI ends in ".js.min", then the
	 * equivalent ".js" file will be minified. If the URI ends in "/all.js", then all ".js" files
	 * in the directory will be unified (concatenated in filename alphabetical order). Finally,
	 * "/all.js.min" both unifies and minifies. After doing this work, the request will continue
	 * to the route type specified by the "next" param.
	 * <p>
	 * For the resulting file to actually be served to the client, you will likely want a
	 * {@link Prudence.Setup.Static} nested in "next". 
	 * <p>
	 * Once a ".js.min" is created by this filter, it will not be recreated unless the source file(s)
	 * have changed. Change is tracked according to the timestamp of files.
	 * <p>
	 * Implementation note: Internally handled by a <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/JavaScriptUnifyMinifyFilter.html">JavaScriptUnifyMinifyFilter</a> instance.
	 * Compression is done via <a href="http://www.inconspicuous.org/projects/jsmin/jsmin.java">John Reilly's Java port</a> of
	 * Douglas Crockford's <a href="http://www.crockford.com/javascript/jsmin.html">JSMin</a>.
	 *
	 * @class
	 * @name Prudence.Setup.JavaScriptUnifyMinify
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param config
	 * @param {String[]} config.roots The root directories
	 * @param {Object} config.next The next route configuration
	 */
	Public.JavaScriptUnifyMinify = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.JavaScriptUnifyMinify */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['roots', 'next']

		Public.create = function(app, uri) {
			importClass(
				com.threecrickets.prudence.util.JavaScriptUnifyMinifyFilter,
				java.io.File)
   
			this.roots = Sincerity.Objects.array(this.roots)
			if (!Sincerity.Objects.exists(this.roots) || (this.roots.length == 0)) {
				this.roots = [Sincerity.Files.build(app.root, 'resources', 'scripts'), sincerity.container.getLibrariesFile('web', 'scripts')]
			}
			var target = this.roots[0]
			if (!(target instanceof File)) {
				target = Sincerity.Files.build(app.root, target)
			}

			this.next = app.createRestlet(this.next, uri)
			var filter = new JavaScriptUnifyMinifyFilter(app.context, this.next, target, app.settings.code.minimumTimeBetweenValidityChecks)

			if (sincerity.verbosity >= 2) {
				println('    JavaScriptUnifyMinify: "{0}"'.cast(target))
			}
			for (var r in this.roots) {
				var root = this.roots[r]
				if (!(root instanceof File)) {
					root = Sincerity.Files.build(app.root, root)
				}
				filter.sourceDirectories.add(root)
				if (sincerity.verbosity >= 2) {
					println('      Directory: "{0}"'.cast(sincerity.container.getRelativePath(root)))
				}
			}

			return filter
		}
		
		return Public
	}(Public))

	/**
	 * A powerful filter that unifies and optionally minifies
	 * CSS files on demand, allowing for improved performance
	 * and scalability.
	 * <p>
	 * By default, CSS files are expected to be found under the "/resources/style/"
	 * subdirectory of your application, as well as the "/libraries/web/style/" subdirectory
	 * of your Sincerity container. These locations can be changed via the "roots" param.
	 * <p>
	 * Note that the first entry in the "roots" is special: it is where the unified/minified files
	 * will be stored.
	 * <p>
	 * This class works by intercepting requested URIs. If the URI ends in ".css.min", then the
	 * equivalent ".css" file will be minified. If the URI ends in "/all.css", then all ".css" files
	 * in the directory will be unified (concatenated in filename alphabetical order). Finally,
	 * "/all.css.min" both unifies and minifies. After doing this work, the request will continue
	 * to the route type specified by the "next" param.
	 * <p>
	 * For the resulting file to actually be served to the client, you will likely want a
	 * {@link Prudence.Setup.Static} nested in "next". 
	 * <p>
	 * Once a ".css.min" is created by this filter, it will not be recreated unless the source file(s)
	 * have changed. Change is tracked according to the timestamp of files.
	 * <p>
	 * Implementation note: Internally handled by a <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/CssScriptUnifyMinifyFilter.html">CssUnifyMinifyFilter</a> instance.
	 * Compression is done via <a href="http://barryvan.github.com/CSSMin/">CSSMin</a>.
	 * 
	 * @class
	 * @name Prudence.Setup.CssUnifyMinify
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param config
	 * @param {String[]} config.roots The root directories
	 * @param {Object} config.next The next route configuration
	 * @see Prudence.Setup.Zuss
	 */
	Public.CssUnifyMinify = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.CssUnifyMinify */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['roots', 'next']

		Public.create = function(app, uri) {
			importClass(
				com.threecrickets.prudence.util.CssUnifyMinifyFilter,
				java.io.File)
   
			this.roots = Sincerity.Objects.array(this.roots)
			if (!Sincerity.Objects.exists(this.roots) || (this.roots.length == 0)) {
				this.roots = [Sincerity.Files.build(app.root, 'resources', 'style'), sincerity.container.getLibrariesFile('web', 'style')]
			}
			var target = this.roots[0]
			if (!(target instanceof File)) {
				target = Sincerity.Files.build(app.root, target)
			}

			this.next = app.createRestlet(this.next, uri)
			var filter = new CssUnifyMinifyFilter(app.context, this.next, target, app.settings.code.minimumTimeBetweenValidityChecks)

			if (sincerity.verbosity >= 2) {
				println('    CssUnifyMinify: "{0}"'.cast(target))
			}
			for (var r in this.roots) {
				var root = this.roots[r]
				if (!(root instanceof File)) {
					root = Sincerity.Files.build(app.root, root)
				}
				filter.sourceDirectories.add(root)
				if (sincerity.verbosity >= 2) {
					println('      Directory: "{0}"'.cast(sincerity.container.getRelativePath(root)))
				}
			}

			return filter
		}
		
		return Public
	}(Public))

	/**
	 * A powerful filter that compiles <a href="http://lesscss.org/">LESS</a>
	 * files to CSS and optionally minifies them on demand. LESS is a powerful CSS meta-language, similar to
	 * <a href="http://sass-lang.com/">Sass</a>, that can greatly
	 * improve the clarity and reusability of your CSS.
	 * <p>
	 * By default, LESS files are expected to be found under the "/resources/style/"
	 * subdirectory of your application, as well as the "/libraries/web/style/" subdirectory
	 * of your Sincerity container. These locations can be changed via the "roots" param.
	 * <p>
	 * Note that the first entry in the "roots" is special: it is where the compiled/minified files
	 * will be stored.
	 * <p>
	 * This class works by intercepting requested URIs. If the URI ends in ".css", then it will
	 * attempt to find a file with the same name but with a ".less" extension. If found, it will
	 * be compiled to ".css". If the URI ends in ".css.min", then it will compile and then
	 * minify the resulting CSS. After doing this work, the request will continue
	 * to the route type specified by the "next" param.
	 * <p>
	 * For the resulting file to actually be served to the client, you will likely want a
	 * {@link Prudence.Setup.Static} nested in "next". 
	 * <p>
	 * Once a ".css" or ".css.min" is created by this filter, it will not be recreated unless the source file
	 * has changed. Change is tracked according to the timestamp of files.
	 * <p>
	 * Implementation note: Internally handled by a <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/LessFilter.html">LessFilter</a> instance.
	 * Compression is done via <a href="http://barryvan.github.com/CSSMin/">CSSMin</a>.
	 * 
	 * @class
	 * @name Prudence.Setup.Less
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param config
	 * @param {String[]} config.roots The root directories
	 * @param {com.github.sommeri.less4j.LessCompiler} [compiler=new com.github.sommeri.less4j.core.DefaultLessCompiler()] The compiler
	 * @param {Object} config.next The next route configuration
	 * @see Prudence.Setup.CssUnifyMinify
	 */
	Public.Less = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Less */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['roots', 'next' ,'compiler']

		Public.create = function(app, uri) {
			importClass(
				com.threecrickets.prudence.util.LessFilter,
				java.io.File)
   
			this.roots = Sincerity.Objects.array(this.roots)
			if (!Sincerity.Objects.exists(this.roots) || (this.roots.length == 0)) {
				this.roots = [Sincerity.Files.build(app.root, 'resources', 'style'), sincerity.container.getLibrariesFile('web', 'style')]
			}
			var target = this.roots[0]
			if (!(target instanceof File)) {
				target = Sincerity.Files.build(app.root, target)
			}

			this.next = app.createRestlet(this.next, uri)
			var filter
			if (Sincerity.Objects.exists(this.compiler)) {
				filter = new LessFilter(app.context, this.next, target, app.settings.code.minimumTimeBetweenValidityChecks, this.compiler)
			}
			else {
				filter = new LessFilter(app.context, this.next, target, app.settings.code.minimumTimeBetweenValidityChecks)
			}
			
			if (sincerity.verbosity >= 2) {
				println('    LESS: "{0}"'.cast(sincerity.container.getRelativePath(target)))
			}
			for (var r in this.roots) {
				var root = this.roots[r]
				if (!(root instanceof File)) {
					root = Sincerity.Files.build(app.root, root)
				}
				filter.sourceDirectories.add(root)
				if (sincerity.verbosity >= 2) {
					println('      Directory: "{0}"'.cast(sincerity.container.getRelativePath(root)))
				}
			}
			
			return filter
		}
		
		return Public
	}(Public))

	/**
	 * A powerful filter that compiles <a href="https://github.com/tomyeh/ZUSS">ZUSS</a>
	 * files to CSS and optionally minifies them on demand. ZUSS is a powerful CSS meta-language, similar to
	 * <a href="http://lesscss.org/">LESS</a> and <a href="http://sass-lang.com/">Sass</a>, that can greatly
	 * improve the clarity and reusability of your CSS.
	 * <p>
	 * By default, ZUSS files are expected to be found under the "/resources/style/"
	 * subdirectory of your application, as well as the "/libraries/web/style/" subdirectory
	 * of your Sincerity container. These locations can be changed via the "roots" param.
	 * <p>
	 * Note that the first entry in the "roots" is special: it is where the compiled/minified files
	 * will be stored.
	 * <p>
	 * This class works by intercepting requested URIs. If the URI ends in ".css", then it will
	 * attempt to find a file with the same name but with a ".zuss" extension. If found, it will
	 * be compiled to ".css". If the URI ends in ".css.min", then it will compile and then
	 * minify the resulting CSS. After doing this work, the request will continue
	 * to the route type specified by the "next" param.
	 * <p>
	 * For the resulting file to actually be served to the client, you will likely want a
	 * {@link Prudence.Setup.Static} nested in "next". 
	 * <p>
	 * Once a ".css" or ".css.min" is created by this filter, it will not be recreated unless the source file
	 * has changed. Change is tracked according to the timestamp of files.
	 * <p>
	 * Implementation note: Internally handled by a <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/ZussFilter.html">ZussFilter</a> instance.
	 * Compression is done via <a href="http://barryvan.github.com/CSSMin/">CSSMin</a>.
	 * 
	 * @class
	 * @name Prudence.Setup.Zuss
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param config
	 * @param {String[]} config.roots The root directories
	 * @param {<a href="http://www.zkoss.org/javadoc/latest/zuss/index.html?org/zkoss/zuss/Resolver.html">org.zkoss.zuss.Resolver</a>} [resolver=new <a href="http://www.zkoss.org/javadoc/latest/zuss/index.html?org/zkoss/zuss/impl/out/BuiltinResolver.html">BuiltinResolver</a>] The resolver
	 * @param {Object} config.next The next route configuration
	 * @see Prudence.Setup.CssUnifyMinify
	 */
	Public.Zuss = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Zuss */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['roots', 'next' ,'resolver']

		Public.create = function(app, uri) {
			importClass(
				com.threecrickets.prudence.util.ZussFilter,
				java.io.File)
   
			this.roots = Sincerity.Objects.array(this.roots)
			if (!Sincerity.Objects.exists(this.roots) || (this.roots.length == 0)) {
				this.roots = [Sincerity.Files.build(app.root, 'resources', 'style'), sincerity.container.getLibrariesFile('web', 'style')]
			}
			var target = this.roots[0]
			if (!(target instanceof File)) {
				target = Sincerity.Files.build(app.root, target)
			}

			this.next = app.createRestlet(this.next, uri)
			var filter
			if (Sincerity.Objects.exists(this.resolver)) {
				filter = new ZussFilter(app.context, this.next, target, app.settings.code.minimumTimeBetweenValidityChecks, this.resolver)
			}
			else {
				filter = new ZussFilter(app.context, this.next, target, app.settings.code.minimumTimeBetweenValidityChecks)
			}
			
			if (sincerity.verbosity >= 2) {
				println('    ZUSS: "{0}"'.cast(sincerity.container.getRelativePath(target)))
			}
			for (var r in this.roots) {
				var root = this.roots[r]
				if (!(root instanceof File)) {
					root = Sincerity.Files.build(app.root, root)
				}
				filter.sourceDirectories.add(root)
				if (sincerity.verbosity >= 2) {
					println('      Directory: "{0}"'.cast(sincerity.container.getRelativePath(root)))
				}
			}
			
			return filter
		}
		
		return Public
	}(Public))

	/**
	 * A filter that adds client-side cache control headers to
	 * responses according to their media (MIME) types. These headers ask the client to
	 * cache or not to cache the response. When cached, the client will not send <i>any</i> request
	 * to the server (not even a conditional request), amounting to the best possible boost
	 * for performance and scalability.  
	 * <p>
	 * It is common in web applications to ask the client to cache media resources (images such as PNG and
	 * JPEG), as well as client-side JavaScript and CSS, because these files are not expected to change much
	 * throughout the life of the application.
	 * <p>
	 * Note that clients are not guaranteed to honor these headers.
	 * <p>
	 * Though you can control these headers programmatically in manual and scriptlet resources
	 * using the {@link conversation#maxAge} API, you cannot do the same for
	 * static resources, which do not execute code.
	 * <p>
	 * By default, this filter will do nothing (which is safest). To enable it,
	 * you can set the "mediaTypes" param to a dict mapping MIME types to a duration value.
	 * The following duration values are supported:
	 * <ul>
	 * <li>Negative number: do nothing.</li>
	 * <li>Zero: explicitly ask the client never to cache (useful as a security precaution
	 * for sensitive data).</li>
	 * <li>Positive integers: the maximum cache age in seconds.</li>
	 * <li>'farFuture': a string constant that translates to 10 years maximum cache age (a common convention
	 * implying that the response should be cached "indefinitely")</li>
	 * </ul>
	 * Example: {type: 'cacheControl', mediaTypes: {'image/*': 'farFuture',
	 * 'text/css': 3600}, next: 'static'} 
	 * <p>
	 * You can also set the "default" param to one of these values. If the response MIME type
	 * does not match anything in the "mediaTypes" param, then "default" will be used.
	 * <p>
	 * Implementation note: Internally handled by a <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/CacheControlFilter.html">CacheControlFilter</a> instance.
	 * 
	 * @class
	 * @name Prudence.Setup.CacheControl
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param config
	 * @param {String} [config.mediaTypes] A dict of MIME types mapped to cache durations in millseconds
	 * @param {Number|String} [config.default=-1] The default cache duration if the MIME type is not in mediaTypes
	 * @param {Object} config.next The next route configuration
	 */
	Public.CacheControl = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.CacheControl */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['mediaTypes', 'default', 'next']

		Public.create = function(app, uri) {
			importClass(
				com.threecrickets.prudence.util.CacheControlFilter,
				org.restlet.data.MediaType,
				java.io.File)
   
			this.root = Sincerity.Objects.ensure(this.root, 'resources')
			if (!(this.root instanceof File)) {
				this.root = Sincerity.Files.build(app.root, this.root)
			}
			
			this['default'] = Sincerity.Objects.ensure(this['default'], -1)
			if (this['default'] == 'farFuture') {
				this['default'] = CacheControlFilter.FAR_FUTURE
			}
			
			this.next = app.createRestlet(this.next, uri)
			var filter = new CacheControlFilter(app.context, this.next, this['default'])
			
			if (sincerity.verbosity >= 2) {
				println('    CacheControl:')
			}
			if (Sincerity.Objects.exists(this.mediaTypes)) {
				for (var mediaType in this.mediaTypes) {
					var maxAge = this.mediaTypes[mediaType]
					if (maxAge == 'farFuture') {
						maxAge = CacheControlFilter.FAR_FUTURE
					}
					else if (Sincerity.Objects.isString(maxAge)) {
						maxAge = Sincerity.Localization.toMilliseconds(maxAge) / 1000
					}
					mediaType = MediaType.valueOf(mediaType)
					filter.maxAgeForMediaType.put(mediaType, maxAge)
					if (sincerity.verbosity >= 2) {
						println('      Max age for {0} -> {1}'.cast(mediaType, maxAge))
					}
				}
			}
			if (sincerity.verbosity >= 2) {
				println('      Default max age -> {0}'.cast(this['default']))
			}
			
			return filter
		}
		
		return Public
	}(Public))

	/**
	 * A filter that acts as a "guard", returning HTTP stauts 401 ("unauthorized") if the client
	 * does not have the proper credentials.
	 * <p>
	 * Relies on HTTP <a href="http://en.wikipedia.org/wiki/Basic_access_authentication">basic authentication (BA)</a>,
	 * which is unencrypted. It is thus strongly recommended that you use it only with HTTPS.
	 * <p>
	 * Most web browsers will cache the credentials for the duration of the session (until the browser is
	 * closed), so that the prompt (the "realm" param) would usually only appear once to the user during that time.
	 * <p>
	 * Implementation note: Internally handled by a <a href="http://restlet.org/learn/javadocs/2.3/jse/api/index.html?org/restlet/security/ChallengeAuthenticator.html">ChallengeAuthenticator</a> instance.
	 * 
	 * @class
	 * @name Prudence.Setup.BasicHttpAuthenticator
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param config
	 * @param {Object} config.credentials Maps usernames to passwords
	 * @param {String} config.realm The security prompt
	 * @param {Object} config.next The next route configuration
	 */
	Public.BasicHttpAuthenticator = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.BasicHttpAuthenticator */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['credentials', 'realm', 'next']

		Public.create = function(app, uri) {
			importClass(
				org.restlet.security.ChallengeAuthenticator,
				org.restlet.security.MapVerifier,
				org.restlet.data.ChallengeScheme)
			
			var authenticator = new ChallengeAuthenticator(app.context, ChallengeScheme.HTTP_BASIC, this.realm)

			// Credentials
			var verifier = new MapVerifier()
			for (var username in this.credentials) {
				verifier.localSecrets.put(username, new java.lang.String(this.credentials[username]).toCharArray())
			}
			authenticator.verifier = verifier

			this.next = app.createRestlet(this.next, uri)
			authenticator.next = this.next
			
			return authenticator
		}
		
		return Public
	}(Public))

	/**
	 * A filter that adds <a href="http://www.w3.org/TR/cors/">Cross-Origin
	 * Resource Sharing (CORS)</a> response headers.
	 * 
	 * @class
	 * @name Prudence.Setup.Cors
	 * @augments Prudence.Setup.Restlet
	 * 
	 * @param config
	 * @param {String} [allowOrigin] The allowed origin (can be "*")
	 * @param {String[]} [allowMethods] The allowed methods
	 * @param {String[]} [allowHeaders] The allowed headers
	 * @param {Number|String} [maxAge] The maximum age
	 * @param {Object} config.next The next route configuration
	 */
	Public.Cors = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Cors */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['allowOrigin', 'allowMethods', 'allowHeaders', 'maxAge', 'next']

		Public.create = function(app, uri) {
			importClass(
				com.threecrickets.prudence.util.CorsFilter)
			
			var cors = new CorsFilter(app.context)
			
			if (Sincerity.Objects.exists(this.allowOrigin)) {
				cors.allowOrigin = this.allowOrigin
			}
			if (Sincerity.Objects.exists(this.allowMethods)) {
				this.allowMethods = Sincerity.Objects.array(this.allowMethods)
				cors.allowMethods.addAll(Sincerity.JVM.toList(this.allowMethods))
			}
			if (Sincerity.Objects.exists(this.allowHeaders)) {
				this.allowHeaders = Sincerity.Objects.array(this.allowHeaders)
				cors.allowHeaders.addAll(Sincerity.JVM.toList(this.allowHeaders))
			}
			if (Sincerity.Objects.exists(this.maxAge)) {
				if (this.maxAge == 'farFuture') {
					this.maxAge = CorsFilter.FAR_FUTURE
				}
				else if (Sincerity.Objects.isString(this.maxAge)) {
					this.maxAge = Sincerity.Localization.toMilliseconds(this.maxAge) / 1000
				}
				cors.maxAge = this.maxAge
			}

			this.next = app.createRestlet(this.next, uri)
			cors.next = this.next
			
			return cors
		}
		
		return Public
	}(Public))

	/**
	 * Simply sets a hard-coded HTTP status code, doing nothing else. 
	 * 
	 * @class
	 * @name Prudence.Setup.Status
	 * @augments Prudence.Setup.Restlet
	 * @param config
	 * @param {Number} [config.code=200] The HTTP status code
	 */
	Public.Status = Sincerity.Classes.define(function(Module) {
		/** @exports Public as Prudence.Setup.Status */
		var Public = {}
		
		/** @ignore */
		Public._inherit = Module.Restlet

		/** @ignore */
		Public._configure = ['code']

		Public.create = function(app, uri) {
			importClass(com.threecrickets.prudence.util.StatusRestlet)
			this.code = Sincerity.Objects.ensure(this.code, 200)
			return new StatusRestlet(this.code)
		}
		
		return Public
	}(Public))

	return Public
}()
