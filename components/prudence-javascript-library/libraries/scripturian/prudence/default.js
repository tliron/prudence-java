//
// This file is part of the Prudence Foundation Library
//
// Copyright 2009-2013 Three Crickets LLC.
//
// The contents of this file are subject to the terms of the LGPL version 3.0:
// http://www.gnu.org/copyleft/lesser.html
//
// Alternatively, you can obtain a royalty free commercial license with less
// limitations, transferable or non-transferable, directly from Three Crickets
// at http://threecrickets.com/
//

/**
 * @namespace
 * 
 * @author Tal Liron
 * @version 1.0
 */
var Prudence = {}

/**
 * The same "application" namespace is shared between all code in a single application.
 * <p>
 * Note that there is always a <i>single</i> application instance per application per component, even if the application
 * is attached to several virtual hosts and servers.
 * 
 * @name application
 * @namespace
 * @see <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/service/ApplicationService.html">The Prudence API documentation</a>
 */

/**
 * Arguments sent to the Sincerity command line.
 * <p>
 * <i>Availability: only available for Sincerity programs and plugins.</i>
 * 
 * @name application.arguments
 * @type String[]
 */

/**
 * Application globals are general purpose attributes accessible by any code in the application.
 * <p>
 * Names can be any string, but the convention is to use "." paths to allow for unique "namespaces"
 * that would not overlap with future extensions, expansions or third-party libraries. For example,
 * use "myapp.data.sourceName" rather than "dataSourceName" to avoid conflict.
 * <p>
 * Implementation note: Prudence's application globals are identical to Restlet application attributes.
 * 
 * @name application.globals
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/concurrent/ConcurrentMap.html">java.util.concurrent.ConcurrentMap</a>&lt;String, Object&gt;
 * @see application#sharedGlobals
 */

/**
 * These are similar to {@link application#globals}, but are shared by all Prudence applications
 * running in the component.
 * <p>
 * Implementation note: Prudence's application globals are identical to Restlet component attributes.
 * Note that some secure Prudence deployments may disable sharing between applications, in which case there
 * will be no shared globals. See the note in the {@link application#component} documentation.
 * 
 * @name application.sharedGlobals
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/concurrent/ConcurrentMap.html">java.util.concurrent.ConcurrentMap</a>&lt;String, Object&gt;
 * @see application#distributionGlobals
 * @see executable#globals
 */
 
/**
 * These are similar to {@link application#sharedGlobals}, but are shared by all members of the Prudence
 * cluster to which we belong.
 * <p>
 * Note that values stored here <i>must be serializable</i>. Depending on your
 * object implementation, this may mean having to manually serialize/deserialize the value into a string
 * (see {@link Sincerity.JSON} and {@link Sincerity.XML}) or another serializable format in order to store
 * it as a distributed global.
 * <p>
 * Implementation note: Prudence's distributed globals are identical to a Hazelcast map named
 * "com.threecrickets.prudence.distributedGlobals", which you can configure in
 * "/configuration/hazelcast/prudence/default.js".
 * <p>
 * <i>Availability: only available if Prudence's distributed component is installed.</i>
 *
 * @name application.distributedGlobals
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/concurrent/ConcurrentMap.html">java.util.concurrent.ConcurrentMap</a>&lt;String, Object&gt;
 */

/**
 * This is a reference to the root filesystem directory of the current application. From here
 * you can easily access application files. For example:
 * <pre>
 * var separator = java.io.File.pathSeparator
 * var dataFilePath = application.root.path + separator + 'data' + separator + 'myfile.dat'
 * </pre>
 * An alternative method:
 * <pre>
 * var dataDir = new java.io.File(application.root, 'data')
 * var dataFile = new java.io.File(dataDir, 'myfile.dat')
 * </pre>
 * 
 * @name application.root
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/io/File.html">java.io.File</a>
 */

/**
 * This is a reference to the root filesystem directory of the current Sincerity container. From here
 * you can easily access any container file. See {@link application#root} for usage examples.
 * 
 * @name application.containerRoot
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/io/File.html">java.io.File</a>
 */

/**
 * Provides low-level access to the underlying Restlet application instance for the current application.
 * <p>
 * This can be useful for accessing some information defined in the application's settings.js, for example:
 * <pre>
 * print(application.application.owner + ', ')
 * print(application.application.author + ', ')
 * print(application.application.statusService.contactEmail)
 * </pre>
 * 
 * @name application.application
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/Application.html">org.restlet.Application</a>
 */

/**
 * This is a reference to the underlying Restlet component instance for the current application.
 * <p>
 * From here you can access virtual hosts, servers and clients.
 * <p>
 * Note that Prudence breaks Restlet's basic security model in allowing you this access. If
 * you wish to disable this feature, edit "/component/services/singleton/default.js", though
 * note that this would also disable the {@link application#sharedGlobals} feature. It would
 * not, however, affect {@link application#distributedGlobals}.
 * 
 * @name application.component
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/Component.html">org.restlet.Component</a>
 */

/**
 * Provides low-level access to the current application's logger.
 * <p>
 * If you're using JavaScript, you may prefer to use {@link Prudence.Logger#getLogger} instead, which
 * adds some JavaScript sugar to the low-level logger.
 * <p>
 * The logger name is "prudence.X", where "X" is defined by app.settings.logger in the application's
 * settings.js, which defaults to the application's subdirectory name.
 * <p>
 * You can configure this logger in "/configuration/logging/". Because this logger is a sub-logger of "prudence", it
 * will by default inherit the "prudence" logger's configuration.
 * 
 * @name application.logger
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/logging/Logger.html">java.util.logging.Logger</a>
 */

/**
 * Provides low-level access to a sub-logger of your {@link application#logger}.
 * <p>
 * If you're using JavaScript, you may prefer to use {@link Prudence.Logger#getLogger} instead, which
 * adds some JavaScript sugar to the low-level logger.
 * <p>
 * The logger name is the base application.logger name with "." and the "name" param appended to it.
 * For example, getSubLogger('backup') in an application named "myapp" would return a logger
 * named "prudence.myapp.backup".
 * <p>
 * You can configure this logger in "/configuration/logging/". Because this logger is a sub-logger of "prudence.myapp", it
 * will by default inherit the "prudence.myapp" logger's configuration.
 * 
 * @name application.getSubLogger
 * @function
 * @param {String} name The name to append to base logger's name
 * @returns {<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/logging/Logger.html">java.util.logging.Logger</a>} The sub-logger
 */

/**
 * Provides access to the MIME type mapped to a filename extension, taken from the
 * mapping table of the current application.
 * <p>
 * This is useful for querying additional information about the MIME type. For example:
 * <pre>
 * var mediaType = application.getMediaTypeByExtension('html')
 * print(mediaType.name + ': ' + mediaType.description)
 * </pre>
 * Note that each application has its own extension mapping table, which can
 * be configured in its settings.js, under "settings.mediaTypes".
 *
 * @name application.getMediaTypeByExtension
 * @function
 * @param {String} extension The extension
 * @returns {<a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/MediaType.html">org.restlet.data.MediaType</a>} The mapped media type, or null if the extension is not mapped
 */

/**
 * Starts a background task in the current or other application in the component.
 * Supports scheduled and repeating tasks.
 * <p>
 * For other ways to start tasks, see
 * {@link application#codeTask},
 * {@link application#distributedExecuteTask}, and
 * {@link application#distributedCodeTask}.
 * <p>
 * If you're using JavaScript, you may prefer to use {@link Prudence.Tasks#task} instead, which
 * adds some JavaScript sugar to this low-level function.
 * <p>
 * If the optional entry "entryPointName" param is <i>not</i> used, the document will be simply executed,
 * similarly to {@link document#execute}. If the param <i>is</i> used, then the document will be executed
 * only once (to be re-executed if the file is edited), and the entry point function called. Thus, using
 * an entry point may result in better performance for tasks that are called very often, as well
 * as allowing you to include many tasks in a single file.
 * <p>
 * The "context" param also works a bit differently according to "entryPointName": if you <i>don't</i>
 * use an entry point, then "context" will be available as {@link document#context}. If you <i>do</i> use
 * as entry point, then the context will be sent as the argument to the entry point function.
 * <p>
 * Note for other languages: Though entry point names should be specified in camel case,
 * as with manual resources they will converted to the appropriate language convention.
 * For example, an entry point named "handleTask" will be called as "handle_task" in Python or Ruby,
 * and "handle-task" in Clojure.
 * <p> 
 * The returned "future" instance can be used to block and wait for the task to be completed,
 * though it's critical that you are aware of how this would affect your scalability. An
 * example of blocking:
 * <pre>
 * application.task(null, '/database/cleanup/', null, {mode: 'full'}, 0, 0, false).get()
 * </pre>
 * <p>
 * Implementation note: Tasks run in a thread pool shared by all applications in the component,
 * but the current application for the executing code is set appropriately according to
 * the "applicationName" param used here.
 * 
 * @name application.executeTask
 * @function
 * @param {String} applicationName The application's <i>full name</i> as defined in its settings.js, or null to run a task in the current application
 * @param {String} documentName The document URI relative to the application's "/libraries/" subdirectory
 * @param {String} entryPointName The function to call in the document, or null to run the entire document
 * @param {Object} context The arbitrary context made available to the task, or null
 * @param {Number} delay Initial delay in milliseconds, or 0 to run the task as soon as possible
 * @param {Number} repeatEvery Repeat delay in milliseconds, or zero for no repetition
 * @param {Boolean} fixedRepeat Whether repetitions are at fixed times, or if the repeat delay begins when the task ends;
 *                  this value us used only if the repeat delay is greater than zero; note that "true" may cause a new
 *                  instance of the task to be spawned again before the previous one completes
 * @returns {<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/concurrent/Future.html">java.util.concurrent.Future</a>} A placeholder for the task's status
 */

/**
 * Starts a background task in the current or other application in the component.
 * Supports scheduled and repeating tasks.
 * <p>
 * Identical in use to {@link application#executeTask}, except that instead of specifiying a documentName
 * to execute, you provide the explicit source code. Note that the source code must be specified
 * as a scriptlet, for example:
 * <pre>
 * application.codeTask(null, '&lt;% application.logger.info(document.context) %&gt;', null, 'Task!', 0, 10000, true)
 * </pre>
 * <p>
 * The advantage of the scriptlet format is that you can run code in any supported programming language, for example:
 * <pre>
 * application.codeTask(null, '&lt;%python application.logger.info(document.context if document.context else "No context") %&gt;', null, 'Task!', 0, 10000, true)
 * </pre>
 * <p>
 * For other ways to start tasks, see
 * {@link application#executeTask},
 * {@link application#distributedExecuteTask}, and
 * {@link application#distributedCodeTask}.
 * <p>
 * If you're using JavaScript, you may prefer to use {@link Prudence.Tasks#task} instead, which
 * adds some JavaScript sugar to this low-level function.
 * <p>
 * Implementation note #1: Your provided source code is turned into an "on-the-fly" document and compiled
 * and cached similarly to code provided in a file, so performance is about identical. Note, however, that these
 * "on-the-fly" documents stay in memory: it is not a good idea to send <i>generated</i> source code to
 * this API, because each version will permanently use up some memory. If you need flexible task behavior, then
 * it's best to use static source code that responds differently according to values sent in params.context.
 * <p>
 * Implementation note #2: Tasks run in a thread pool shared by all applications in the component,
 * but the current application for the executing code is set appropriately according to
 * the "applicationName" param used here.
 * 
 * @name application.codeTask
 * @function
 * @param {String} applicationName The application's <i>full name</i> as defined in its settings.js, or null to run a task in the current application
 * @param {String} code The code to execute
 * @param {String} entryPointName The function to call in the document, or null to run the entire document
 * @param {Object} context The context made available to the task, or null
 * @param {Number} delay Initial delay in milliseconds, or 0 to run the task as soon as possible
 * @param {Number} repeatEvery Repeat delay in milliseconds, or zero for no repetition
 * @param {Boolean} fixedRepeat Whether repetitions are at fixed times, or if the repeat delay begins when the task ends
 * @returns {<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/concurrent/Future.html">java.util.concurrent.Future</a>} A placeholder for the task's status
 */

/**
 * Starts a background task anywhere in the cluster, in the current or other application in the component.
 * <p>
 * Similar in use to {@link application#executeTask}, except that the task may be executed in a different
 * JVM running in the Prudence cluster. This introduces two limitations: 1) if you send a param.context,
 * it must be JVM-serializable; if you're using JavaScript, it may be easiest to serialize manually
 * (see {@link Sincerity.JSON} and {@link Sincerity.XML}); 2) there is no task scheduling/repetition feature.
 * <p>
 * You can control where in the cluster the task is run using the "where" and "multi" params. See the Hazelcast
 * documentation for more information.
 * <p>
 * For other ways to start tasks, see
 * {@link application#executeTask},
 * {@link application#codeTask}, and
 * {@link application#distributedCodeTask}.
 * <p>
 * If you're using JavaScript, you may prefer to use {@link Prudence.Tasks#task} instead, which
 * adds some JavaScript sugar to this low-level function.
 * <p>
 * Implementation note: Tasks run in a thread pool shared by all applications in the component,
 * but the current application for the executing code is set appropriately according to
 * the "applicationName" param used here.
 * <p>
 * <i>Availability: only available if Prudence's distributed component is installed.</i>
 * 
 * @name application.distributedExecuteTask
 * @function
 * @param {String} applicationName The application's <i>full name</i> as defined in its settings.js, or null to run a task in the current application
 * @param {String} documentName The document URI relative to the application's "/libraries/" subdirectory
 * @param {String} entryPointName The function to call in the document, or null to run the entire document
 * @param {Object} context The context made available to the task (must be serializable), or null
 * @param {String|<a href="http://www.hazelcast.com/javadoc/index.html?com/hazelcast/core/Member.html">com.hazelcast.core.Member</a>|<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/lang/Iterable.html">java.lang.Iterable</a>&lt;<a href="http://www.hazelcast.com/javadoc/index.html?com/hazelcast/core/Member.html">com.hazelcast.core.Member</a>&gt;} where Where in the cluster to run the task, or null to let Halzecast decide
 * @param {Boolean} multi Whether the task should be executed on all members in the cluster; only used if "where" is an Iterable
 * @returns {<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/concurrent/Future.html">java.util.concurrent.Future</a>} A placeholder for the task's status
 */

/**
 * Starts a background task anywhere in the cluster, in the current or other application in the component.
 * <p>
 * See the documentation for {@link application#distributedExecuteTask} and {@link application#codeTask} for
 * usage.
 * <p>
 * For other ways to start tasks, see
 * {@link application#executeTask},
 * {@link application#codeTask}, and
 * {@link application#distributedExecuteTask}.
 * <p>
 * If you're using JavaScript, you may prefer to use {@link Prudence.Tasks#task} instead, which
 * adds some JavaScript sugar to this low-level function.
 * <p>
 * Implementation note: Tasks run in a thread pool shared by all applications in the component,
 * but the current application for the executing code is set appropriately according to
 * the "applicationName" param used here.
 * <p>
 * <i>Availability: only available if Prudence's distributed component is installed.</i>
 * 
 * @name application.distributedCodeTask
 * @function
 * @param {String} applicationName The application's <i>full name</i> as defined in its settings.js, or null to run a task in the current application
 * @param {String} code The code to execute
 * @param {String} entryPointName The function to call in the document, or null to run the entire document
 * @param {Object} context The context made available to the task (must be serializable), or null
 * @param {String|<a href="http://www.hazelcast.com/javadoc/index.html?com/hazelcast/core/Member.html">com.hazelcast.core.Member</a>|<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/lang/Iterable.html">java.lang.Iterable</a>&lt;<a href="http://www.hazelcast.com/javadoc/index.html?com/hazelcast/core/Member.html">com.hazelcast.core.Member</a>&gt;} where Where in the cluster to run the task, or null to let Halzecast decide
 * @param {Boolean} multi Whether the task should be executed on all members in the cluster; only used if "where" is an Iterable
 * @returns {<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/concurrent/Future.html">java.util.concurrent.Future</a>} A placeholder for the task's status
 */

/**
 * Provides low-level access to the current Hazelcast instance.
 * <p>
 * Useful for leveraging Hazelcast features beyond what Prudence automatically provides.
 * For example, you can define your own distrubuted maps, multi-maps, queues, locks, etc.
 * <p>
 * Hazelcast configuration is in "/configuration/hazelcast/prudence/default.js".
 * 
 * @name application.hazelcast
 * @type <a href="http://www.hazelcast.com/javadoc/index.html?com/hazelcast/core/HazelcastInstance.html">com.hazelcast.core.HazelcastInstance</a>
 */

/**
 * Provides low-level access to the component's executor service, a shared thread pool
 * used to execute asynchronous tasks.
 * <p>
 * To use it, you would have to have or create java.lang.Runnable or java.util.concurrent.Callable instances.
 * You would likely prefer to use the {@link application#executeTask} family of functions instead,
 * which let you implement your tasks using familiar Prudence code.
 * 
 * @name application.executor
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/concurrent/ExecutorService.html">java.util.concurrent.ExecutorService</a>
 */

/**
 * The "document" namespace has two distinct uses. First, it represents the file you are in: this document. This is where you can
 * access the document's attributes and possibly change them. Many of these attributes have to do with caching. The second use of
 * this namespace is for accessing <i>any</i> document, e.g. {@link document#executeOnce}, {@link document#markExecuted}.
 * Prudence combines these two uses into one semantic namespace, but it's useful to understand that they are functionally separate.
 * <p>
 * Note that in the case of scriptlet resources, the term "this document" is meant include all scriptlets on the page, even if they are
 * written in different languages.
 * <p>
 * Implementation note: All the properties and methods in the "document" namespace always know which document they are accessed from.
 * This means that passing the "document" namespace as an argument to a function in another document will not let that other document
 * access attributes for the calling document. In fact, all documents in the current thread share the same "document" namespace, which
 * internally gets and sets attributes for the document in which it is accessed, so passing the "document" namespace as a value is
 * superfluous. If you do have a need for a function in one document to manipulate the "document" attributes of another document, you will
 * have to return the values and have <i>code in the owning document</i> explicitly set the attributes there.
 *
 * @name document
 * @namespace
 * @see <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/service/DocumentService.html">The Prudence API documentation</a>
 */

/**
 * Executes a Scripturian document in the current thread.
 * <p>
 * Once executed, the document becomes a "dependent" of the current document, such that if the
 * dependent file is updated, it would cause the current file to be seen as updated, too. This
 * recursive mechanism ensures that documents are re-executed if any file in their dependency
 * tree is changed. The {@link document#addDependency} and {@link document#invalidate} APIs give
 * you manual control over this mechanism.
 * <p>
 * The document can be in any supported language, to be determined by its filename extension.
 * <p>
 * By default, the URI is relative to either the application's "/libraries/" subdirectory, the container's "/libraries/scripturian/"
 * directory, or the Sincerity installation's "/libraries/scripturian/" directory. You can furthermore configure
 * additional paths in the application's "settings.js" file, in app.settings.code.libraries.
 * <p>
 * By default, URIs do not include the filename extension and must end in a trailing slash. For example:
 * "/sincerity/json/" could refer to "/libraries/scripturian/sincerity/json.js" in the Sincerity installation.
 * If the URI refers to a directory, a file named "default.*" in that directory will be used. The "default"
 * file can be in any supported language. 
 * <p>
 * Will throw an exception if the document is not found.
 * 
 * @name document.execute
 * @function
 * @param {String} documentName The document URI
 * @see document#executeOnce
 */

/**
 * Identical to {@link document#execute}, except that it will not execute the document if it was already
 * executed in the current thread.
 * <p>
 * This behavior is identical to "import" or "require" mechanisms in many programming languages.
 * <p>
 * You can use {@link document#markExecuted} to change the internal flag used to mark execution.
 * 
 * @name document.executeOnce
 * @function
 * @param {String} documentName The document URI
 */


/**
 * Identical to {@link document#executeOnce}, but applies to all function arguments.
 * <p>
 * This behavior is identical to "import" or "require" mechanisms in many programming languages.
 * <p>
 * You can use {@link document#markExecuted} to change the internal flag used to mark execution.
 * 
 * @name document.require
 * @function
 */

/**
 * Allows you to change the value of the internal flag marking whether a document was executed in
 * this thread or not.
 * <p>
 * See {@link document#execute} for a discussion of how the document URI is interpreted.
 * 
 * @name document.markExecuted
 * @function
 * @param {String} documentName The document URI
 * @param {Boolean} flag Whether the document will be marked as executed
 * @see document#executeOnce
 */

/**
 * Manually makes a document a dependant of the current document, such that if the
 * that file is updated, it would cause the current file to be seen as updated, too.
 * <p>
 * This allows you to affect the dependency tree without executing the document.
 * <p>
 * See {@link document#execute} for a discussion of how the document URI is interpreted.
 * 
 * @name document.addDependency
 * @function
 * @param {String} documentName The document URI
 * @see document#addFileDependency
 */

/**
 * Similar {@link document#addDependency}, except that the argument is not a document URI,
 * but rather a relative file path. Thus the full filename extension should be used,
 * without a trailling slash.
 * 
 * @name document.addFileDependency
 * @function
 * @param {String} path The relative file path
 */

/**
 * Marks a document as invalid, affecting any dependency trees that include that document.
 * <p>
 * See {@link document#execute} for a discussion of how the document URI is interpreted.
 * 
 * @name document.invalidate
 * @function
 * @param {String} documentName The document URI
 */

/**
 * Like {@link document#invalidate}, but invalidates the current document.
 * 
 * @name document.invalidateCurrent
 * @function
 */

/**
 * Initializes a low-level RESTful client request to any URI.
 * <p>
 * To use a specific protocol you need a client connector to support it, as defined in your 
 * "/component/clients/" directory. The internal Restlet "riap" and "clap" are automatically
 * supported, though you may prefer to use {@link document#internal} instead for such requests.
 * <p>
 * If you're using JavaScript, you may prefer to use {@link Prudence.Resources#request} instead, which
 * adds some JavaScript sugar to this low-level function.
 * 
 * @name document.external
 * @function
 * @param {String} uri The full URI
 * @param {String} mediaTypeName The preferred MIME type or null
 * @returns {<a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/resource/ClientResource.html">org.restlet.resource.ClientResource</a>} The client resource API
 */

/**
 * Initializes a low-level RESTful client request to internal URIs for the current application.
 * Use {@link document#internalOther} to access other applications in the component.
 * <p>
 * If you're using JavaScript, you may prefer to use {@link Prudence.Resources#request} instead, which
 * adds some JavaScript sugar to this low-level function.
 *
 * @name document.internal
 * @function
 * @param {String} uri The relative URI
 * @param {String} mediaTypeName The preferred MIME type or null
 * @returns {<a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/resource/ClientResource.html">org.restlet.resource.ClientResource</a>} The client resource API
 */

/**
 * Initializes a low-level RESTful client request to internal URIs for any application in the component.
 * For the current application, you can use {@link document#internal} instead.
 * <p>
 * If you're using JavaScript, you may prefer to use {@link Prudence.Resources#request} instead, which
 * adds some JavaScript sugar to this low-level function.
 * 
 * @name document.internalOther
 * @function
 * @param {String} applicationInternalName The internal name of the application (defaults to its subdirectory name)
 * @param {String} uri The relative URI
 * @param {String} mediaTypeName The preferred MIME type or null
 * @returns {<a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/resource/ClientResource.html">org.restlet.resource.ClientResource</a>} The client resource API
 */

/**
 * Low-level access the Scripturian document source for the current document.
 * 
 * @name document.source
 * @type <a href="http://threecrickets.com/api/java/scripturian/index.html?com/threecrickets/scripturian/document/DocumentSource.html">com.threecrickets.scripturian.document.DocumentSource</a><com.threecrickets.scripturian.Executable>
 */

/**
 * Low-level access the Scripturian document descriptor for the current document.
 *
 * @name document.descriptor
 * @type <a href="http://threecrickets.com/api/java/scripturian/index.html?com/threecrickets/scripturian/document/DocumentDescriptor.html">com.threecrickets.scripturian.document.DocumentDescriptor</a>&lt;<a href="http://threecrickets.com/api/java/scripturian/index.html?com/threecrickets/scripturian/Executable.html">com.threecrickets.scripturian.Executable</a>&gt;
 */

/**
 * Pass-through documents are normally configured in your application's "routing.js", but can
 * also be accessed and manipulated at runtime via this API.
 * 
 * <i>Availability: only available for manual and scriptlet resources.</i>
 *
 * @name document.passThroughDocuments
 * @function
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/Set.html">java.util.Set</a>&ltString&gt;
 */

/**
 * See {@link application#executeTask} for more information.
 * <p>
 * <i>Availability: only available for tasks.</i>
 * 
 * @name document.context
 * @type Object
 */

/**
 * Direct access to the cache backend.
 * <p>
 * Useful for calling invalidate(), reset() or prune() on the cache.
 * <p>
 * <i>Availability: only available for scriptlet resources.</i>
 * 
 * @name document.cache
 * @type <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/cache/Cache.html">com.threecrickets.prudence.cache.Cache</a>
 */

/**
 * Similar in some respects to {@link document#execute}, except that
 * is specifically meant for scriptlet resources, and will
 * include the output of those resources into the current location.
 * <p>
 * Calling this API is identical to using the "&lt;%& .. %&gt;" Scripturian tag.
 * <p>
 * By default, the URI is relative to either the application's "/libraries/scriptlet-resources/" subdirectory
 * or the container's "/libraries/prudence-scriptlet-resources/" directory.
 * <p>
 * By default, URIs do not include the filename extension and must end in a trailing slash. For example:
 * "/site/header/" could refer to "/libraries/scriptlet-resources/site/header.html" in the current application.
 * If the URI refers to a directory, a file named "index.*" in that directory will be used.
 * <p>
 * Will throw an exception if the document is not found.
 * <p>
 * <i>Availability: only available for scriptlet resources.</i>
 * 
 * @name document.include
 * @function
 * @param {String} uri The document URI
 * @returns {<a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/representation/Representation.html">org.restlet.representation.Representation</a>} The representation
 */

/**
 * Redirects the output of the scriptlet resource to a {@link conversation#locals} string variable.
 * Redirection will continue until {@link document#endCapture} is called.
 * <p>
 * Calling this API is identical to using the "&lt;%{{ .. %&gt;" Scripturian tag.
 * <p>
 * Captures are very useful for creating page templates: first all the different blocks can be captured,
 * and then they can be assembled together using "&lt;%== .. %&gt;" tags.
 * For example:
 * <pre>
 * &lt;%{{ title }}%&gt;
 * This is my title!
 * &lt;%}}%&gt;
 * 
 * &lt;h1&gt;&lt;== title %&gt;&lt;/h1&gt;
 * </pre>
 * <p>
 * <i>Availability: only available for scriptlet resources.</i>
 * 
 * @name document.startCapture
 * @function
 * @param {String} name The name of the conversation.local into which the captured text will go
 */

/**
 * See {@link document#startCapture}.
 * <p>
 * Calling this API is identical to using the "&lt;%}}%&gt;" Scripturian tag, though when used as
 * a tag use the return value is discarded.
 * <p>
 * <i>Availability: only available for scriptlet resources.</i>
 * 
 * @name document.endCapture
 * @function
 * @returns {String} The contents of the capture
 */

/**
 * The "caching" namespace represents the caching attributes of the current resource. For scriptlet resources,
 * it would always be the entire document (the file). For manual resources, it would be the entire document
 * for mapped resources, but would be the individual dispatched ID for dispatched resources.
 * 
 * @name caching
 * @namespace
 * @see <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/service/CachingServiceBase.html">The Prudence API documentation</a>
 */

/**
 * This is the amount of time in milliseconds that the current document will be cached.
 * If this value is zero (the default), caching is disabled.
 * <p>
 * You can set this value to a either a number or a string: see {@link Sincerity.Localization#toMilliseconds}.
 * For example, '1.5m' is 90000 milliseconds. Note, though, they when you read the value,
 * it will always be numeric (a long data type).
 * 
 * <i>Availability: only available for scriptlet resources.</i>
 * 
 * @name caching.duration
 * @type Number
 */

/**
 * When this is true, then <i>only</i> "GET" requests will be cached.
 * Defaults to false.
 * 
 * @name caching.onlyGet
 * @type Boolean
 */

/**
 * The cache key template is used to generate the cache key for the current document
 * by injecting attributes based on the current conversation. See {@link caching#key} to
 * see the actually generate cache key. Defaults to "{ri}|{dn}".
 * <p>
 * See <a href="http://threecrickets.com/prudence/manual/uri-space/#injecting-conversation-attributes">the Prudence Manual</a>
 * for a list of possible injected attributes.
 * <p>
 * You can additionally inject your own special values via {@link caching#keyTemplateHandlers}.
 * <p>
 * This value has no effect if {@link caching#duration} is zero.
 * <p>
 * <i>Availability: only available for scriptlet resources.</i>
 * 
 * @name caching.keyTemplate
 * @type String
 */

/**
 * This read-only value contains the actual cache key used for the current
 * document in the current conversation. You do not set the cache key directly,
 * but instead you can set the {@link caching#keyTemplate}.
 * <p>
 * <i>Availability: only available for scriptlet resources.</i>
 * 
 * @name caching.key
 * @type String
 */

/**
 * Lets you add any number of cache tags to the current document.
 * You can then invalidate all cache entry with a certain tag using
 * {@link document#cache}.invalidate().
 * <p>
 * This value has no effect if {@link caching#duration} is zero.
 * <p>
 * <i>Availability: only available for scriptlet resources.</i>
 * 
 * @name caching.tags
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/Set.html">java.util.Set</a>&lt;String&gt;
 */

/**
 * Cache key template handlers let you inject your own attributes into
 * the {@link caching#keyTemplate}.
 * <p>
 * <i>Availability: only available for scriptlet resources.</i>
 * 
 * @name caching.keyTemplateHandlers
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/concurrent/ConcurrentMap.html">java.util.concurrent.ConcurrentMap</a>&lt;String, String&gt;
 */

/**
 * The "conversation" namespace represents the request received from the user as well your response to it. Because Prudence is RESTful, conversations
 * encapsulate exactly a <i>single</i> request and its response.
 * <p>
 * Implementing higher level "session" management, covering an arbitrary number of such conversations, is not handled directly by Prudence.
 * Such schemes are usually handled using cookies included in the conversation that identify it as part of the "session", while on the
 * server maintaining a session table in a persistent (database) backend.
 * <p>
 * In the namespace you can access various aspects of the request: the URI, formatting preferences, client information, and the payload
 * sent with the request (the "entity"). You can likewise set response characteristics.
 * 
 * Note that in scriptlet resources "conversation" is available as a global namespace. In manual resources and handlers it is sent to the
 * handling entry points (handleGet, handlePost, etc.) as an argument. Usage is however identical in both cases.
 * 
 * @name conversation
 * @namespace
 * @see <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/service/ConversationService.html">The Prudence API documentation</a>
 */

/**
 * Conversation locals are general purpose attributes accessible by any code in the application.
 * <p>
 * Names can be any string, but the convention is to use "." paths to allow for unique "namespaces"
 * that would not overlap with future extensions, expansions or third-party libraries. For example,
 * use "myapp.data.sourceName" rather than "dataSourceName" to avoid conflict.
 * <p>
 * Implementation note: Prudence's conversation locals are identical to Restlet request attributes.
 * 
 * @name conversation.locals
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/concurrent/ConcurrentMap.html">java.util.concurrent.ConcurrentMap</a>&lt;String, Object&gt;
 */

/**
 * Provides access to the URI (Restlet reference) for the current request.
 * <p>
 * A few useful attributes:
 * <ul>
 * <li>conversation.reference.identifier: the complete URI</li>
 * <li>conversation.reference.path: the URI, not including the domain name and the query matrix</li>
 * <li>conversation.reference.segments: a list of URI segments in the path</li>
 * <li>conversation.reference.lastSegment: the last segment in the URI path</li>
 * <li>conversation.reference.fragment: the URI fragment (whatever follows "#")</li>
 * <li>conversation.reference.query: the URI query (whatever follows "?"); you might prefer to use {@link conversation#query} instead</li>
 * <li>conversation.reference.remainingPart: for wildcard URI templates, this is the wildcard value</li>
 * <li>conversation.reference.relativeRef: a relative URI pointing to the base URI (usually the application root URI on the current virtual host)</li>
 * </ul>
 *
 * @name conversation.reference
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/Reference.html">org.restlet.data.Reference</a>
 */

/**
 * Provides access to the remaining part of the URI (the "wildcard") for the current request
 * (not including the query).
 * <p>
 * This is a shortcut to calling {@link conversation#reference}.remainingPart(true,false).
 * 
 * @name conversation.wildcard
 * @type {String}
 */

/**
 * This is a URI path relative to the base URI, which is usually the application root URI on the current virtual host.
 * <p>
 * Exposing relative URIs to your clients (for example, in HTML) makes your application "portable," in the sense that you
 * can attach it to any root URI without have to rewrite all your URIs. Moreover, if your application can be attached
 * to several virtual hosts at the same time, relative URIs will guarantee that the correct URI is reached for requests
 * arriving from all hosts.
 * <p>
 * It's also, importantly, less error-prone than hardcoding URIs. For example, here is snippet from a textual HTML resource:
 * <pre>
 * Click &lt;a href="&lt;%= conversation.base %&gt;/contact/"&gt;here&lt;/a&gt; to contact us.
 * </pre>
 * The above snippet can be used in <i>any</i> textual resource, no matter its URI, and will alwaus refer to the "/contact/"
 * URI starting at the root URI of the application.
 * <p>
 * Note that it can also be useful to use the base as part of your {@link caching#keyTemplate}, where it is
 * available as "{cb}".
 *
 * @name conversation.base
 * @type String
 */

/**
 * Provides access to values sent via the URI query (the part of the URI after the "?"). Values
 * are already URI-decoded for you, for example: "?name=bill%20gates" will appear here as
 * "bill gates".
 * <p>
 * Implementation note: This is a read-only map of query parameter names to values. In case the URI has multiple
 * values for the same name, only the <i>first one</i> is mapped. If you need access to all values, use {@link conversation#queryAll} instead.
 * This is really a shortcut to calling conversation.queryAll.getValuesMap().
 * 
 * @name conversation.query
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/Map.html">java.util.Map</a>&lt;String, String&gt;
 */

/**
 * Provides access to values sent via the URI query (the part of the URI after the "?"). Values
 * are already URI-decoded for you, for example: "?name=bill%20gates" will appear here as
 * "bill gates".
 * <p>
 * It is usually easier to use the {@link conversation#query} API. Use this API only if you need to handle
 * multiple values of the same parameter name.
 * <p>
 * Useful methods:
 * <ul>
 * <li>conversation.queryAll.getValuesArray: returns an array of strings representing all values for a name, or an empty array if there are no values</li>
 * <li>conversation.queryAll.getFirstValue: returns the first value of a name, or null if there are no values</li>
 * </ul>
 * 
 * @name conversation.queryAll
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/Form.html">org.restlet.data.Form</a>
 */

/**
 * This is the data payload sent by the client.
 * <p>
 * Note that if the payload is from a web form (the "application/x-www-form-urlencoded" MIME type; usually sent from an HTML
 * &lt;form&gt; entity), the convenient {@link conversation#form} API exists for parsing this data.
 * <p>
 * For other kinds of data, you will have to parse the it yourself. Before attempting to parse entity data on your own, consider
 * looking through the Restlet API and its extensive set of plugins for tools to help you parse representations. Plugins exist
 * for many common Internet formats.
 * <p>
 * Useful attributes:
 * <ul>
 * <li>conversation.entity.size: the size of the data in bytes, or -1 if unknown</li>
 * <li>*conversation.entity.text: the data as text (only useful if the data is textual)</li>
 * <li>*conversation.entity.reader: an open JVM <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/io/Reader.html">Reader</a> to the data (only useful if the data is textual)</li>
 * <li>*conversation.entity.stream: an open JVM <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/io/InputStream.html">InputStream</a> to the data (useful for binary data)</li>
 * </ul>
 * <p>
 * Implementation note: Client data is provided as a stream that can only be "consumed" once. Attributes that cause consumption are marked
 * with a "*" above. Note that conversation.entity.text is one of them! So, if you need to access conversation.entity.text more than once, store
 * it to a variable first.
 * <p>
 * <i>Availability: only present in PUT and POST handling.</i>
 * 
 * @name conversation.entity
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/representation/Representation.html">org.restlet.representation.Representation</a>
  */

/**
 * Convenient API for accessing {@link conversation#entity} when the payload is a web form (the "application/x-www-form-urlencoded" MIME type;
 * usually sent from an HTML &lt;form&gt; entity).
 * <p>
 * Values are either strings or instances of
 * <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/FileParameter.html">com.threecrickets.util.FileParameter</a>
 * in the case of uploaded files. Make sure to check the type accordingly.
 * <p>
 * Useful attributes for FileParameter:
 * <ul>
 * <li>data: the raw data as an array of bytes, for uploaded files stored in memory</li>
 * <li>file: a JVM File instance for uploaded files stored on disk; uploaded files are stored in the application's "/uploads/" subdirectory by default</li>
 * <li>size: in bytes</li>
 * <li>mediaTypeName: the MIME type set by the client, such as "image/jpeg"</li>
 * <li>mediaType: the <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/MediaType.html">MediaType</a> mapped to the mediaTypeName, if available</li>
 * </ul>
 * <p>
 * Note that either "data" or "file" are valid, but not both. One will always be null. You can control how uploaded data is managed
 * via your application's setting.js.
 * <p>
 * Implementation note: This is a read-only map of form field names to values. In case the form has multiple
 * values for the same field, only the <i>first one</i> is mapped. If you need access to all values, use {@link conversation#formAll} instead.
 * This is really a shortcut to calling conversation.formAll.getValuesMap().
 * 
 * @name conversation.form
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/Map.html">java.util.Map</a>&lt;String, Object&gt;
 */

/**
 * Convenient API for accessing {@link conversation#entity} when the payload is a web form (the "application/x-www-form-urlencoded" MIME type;
 * usually sent from an HTML &lt;form&gt; entity).
 * <p>
 * It is usually easier to use the {@link conversation#form} API. Use this API only if you need to handle
 * multiple values of the same form field name.
 * <p>
 * Useful methods:
 * <ul>
 * <li>conversation.queryAll.getValuesArray: returns an array of values (string or FileParamter instances) representing all values for a name, or an empty array if there are no values</li>
 * <li>conversation.queryAll.getFirstValue: returns the first value of a name, or null if there are no values</li>
 * </ul>
 *
 * @name conversation.formAll
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/Form.html">org.restlet.data.Form</a>
 */

/**
 * Convenient API to set the response payload to a textual representation.
 * 
 * @name conversation.setResponseText
 * @param {String} text The text
 * @param {String} mediaType Can be null
 * @param {String} language Can be null
 * @param {String} characterSet Can be null
 * @returns <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/representation/StringRepresentation.html">org.restlet.representation.StringRepresentation</a>
 * @see conversation#setResponseBinary
 */

/**
 * Convenient API to set the response payload to a binary representation.
 * 
 * @name conversation.setResponseBinary
 * @param {byte[]} byteArray The binary data
 * @param {String} mediaType Can be null
 * @returns <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/representation/ByteArrayRepresentation.html">org.restlet.representation.ByteArrayRepresentation</a>
 * @see conversation#setResponseText
 * @see Sincerity.JVM#newArray
 */

/**
 * Low-level access to the response status code.
 * <p>
 * You may prefer to use {@link conversation#statusCode} instead.
 * 
 * @name conversation.status
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/Status.html">org.restlet.data.Status</a>
 */

/**
 * The <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html">HTTP-compatible status</a> code sent to the client.
 * <p>
 * If you're using JavaScript, you may use the {@link Prudence.Resources.Status} namespaces for constant values.
 * <p>
 * Note that if the status code is an error code (400 or above), then the response will by default be handled by the error
 * handler. The error handler will do one of three things: 1) show a custom error page you defined in your application's
 * settings.js; 2) show a default error page; or 3) in the special case of 500 errors, and if you've enabled debugging in
 * your application's settings.js, will display a debug page.
 * <p>
 * If you want to return an error code <i>but also</i> control the response, then set {@link conversation#statusPassthrough}
 * to true in order to disable the error handler.
 * <p>
 * Prudence will automatically set the status code to 500 ("internal server error") in case of an unhandled exception in your code.
 * Combined with the enabled debugging in settings.js, this allows you to more easily debug unhandled exceptions.
 * 
 * @name conversation.statusCode
 * @type Number
 */

/**
 * Low-level access to the response encoding.
 * <p>
 * You may prefer to use {@link conversation#encodingName} instead.
 * 
 * @name conversation.encoding
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/Encoding.html">org.restlet.data.Encoding</a>
 */

/**
 * The response encoding. Will initially be set according to content negotiation between the client and the server.
 * Supported values are "zip", "gzip", "deflate", "compress" and "identity".
 * <p>
 * This property is used different in manual and scriptlet resources.
 * <p>
 * In manual resources, you can modify this value, and it will set the appropriate response header. However, it is up to you to
 * return a value that is properly encoded.
 * <p>
 * In scriptlet resources, this value is read-only. Prudence will automatically compress the output text in the appropriate
 * encoding, and moreover cache the compressed output to avoid re-compression. This powerful feature can help boost
 * scalability. 
 * 
 * @name conversation.encodingName
 * @type String
 */

/**
 * Low-level access to the response character set.
 * <p>
 * You may prefer to use {@link conversation#characterSetName} or {@link conversation#characterSetShortName} instead.
 * 
 * @name conversation.characterSet
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/CharacterSet.html">org.restlet.data.CharacterSet</a>
 */

/**
 * The response character set. Will initially be set according to content negotiation between the client and the server.
 * <p>
 * If the client did not specify a preferred character set, then the character set will be set according to the default determined
 * by the "com.threecrickets.prudence.GeneratedTextResource.defaultCharacterSet" application global. It not explicitly set, it will
 * be UTF-8.
 * <p>
 * Supported values are defined by ISO's UCS (Universal Character Set) standard. Examples include "ISO-8859-1" (for Latin 1),
 * "UTF-8" (for 8-Bit Unicode Transformation Format), and "US-ASCII" for ASCII. You can also use shortcuts for these
 * via the {@link conversation#characterSetShortName} API.
 * 
 * @name conversation.characterSetName
 * @type String
 */

/**
 * The response character set. Similar to {@link conversation#characterSetName}, but allows the use of shortcuts
 * for the longer UTC names.
 * <p>
 * Shortcuts include "ascii", "utf8" and "win" (for the Windows 1252 character set).
 * 
 * @name conversation.characterSetShortName
 * @type String
 */

/**
 * Low-level access to the response language.
 * <p>
 * You may prefer to use {@link conversation#languageName} instead. However, this particular API is useful
 * due to the "include" method, which lets you easily test language sub-types. For example, the "en-us"
 * language is included in the "en" language.
 * 
 * @name conversation.language
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/Language.html">org.restlet.data.Language</a>
 */

/**
 * The response language. Will initially be set according to content negotiation between the client and the server.
 * <p>
 * Supported values are defined by the IETF standard locale names. Examples include "en" for English, "en-us" for USA English and
 * "fr" for French.
 * 
 * @name conversation.languageName
 * @type String
 */

/**
 * Low-level access to the response media (MIME) type.
 * <p>
 * You may prefer to use {@link conversation#mediaTypeName} or {@link conversation#mediaTypeExtension} instead.
 * 
 * @name conversation.mediaType
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/MediaType.html">org.restlet.data.MediaType</a>
 */

/**
 * The response media (MIME) type. Will initially be set according to content negotiation between the client and the server.
 * <p>
 * Supported value are defined by the MIME (Multipurpose Internet Mail Extensions) standard. Examples include: "text/plain",
 * "text/html", "application/json", and "application/x-www-form-urlencoded". You can also use shortcuts for these
 * via the {@link conversation#mediaTypeExtension} API.
 *
 * @name conversation.mediaTypeName
 * @type String
 */

/**
 * The response media (MIME) type. Similar to {@link conversation#mediaTypeName}, but allows the use of shortcuts
 * for the longer MIME names.
 * <p>
 * These shortcuts are in fact identical to the filename extension mapping for the application. For example,
 * "txt" is usually equivalent to MIME "text/plain" and "xml" is usually equivalent to "application/xml".
 * The exact mapping is configured in your application's settings.js, under "settings.mediaTypes".
 *
 * @name conversation.mediaTypeExtension
 * @type String
 */

/**
 * Lets clients know how to treat your response representation.
 * <p>
 * To change the disposition, set its "type" property. Supported values are "none" (the default), "inline"
 * and "attachment". An example of setting an attachment disposition:
 * <pre>
 * conversation.disposition.type = 'attachment'
 * conversation.disposition.filename = 'revenue.csv'
 * </pre>
 * 
 * @name conversation.disposition
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/Disposition.html">org.restlet.data.Disposition</a>
 */

/**
 * Allows you to set extra response headers.
 * <p>
 * Note that these are <i>extra</i> headers you can set <i>in addition</i> to those managed by Prudence
 * via the rest of the APIs in this namespace. You <i>cannot</i> use this API to override the headers set
 * by Prudence, because Prudence will override your values. For example, if you want to change the "Last-Modified"
 * header, use the {@link conversation#modificationTimestamp} API.
 * <p>
 * Also note that extra headers you add are cached together with the rest of the response characteristics.
 * (See {@link caching#duration})
 * <p>
 * Example:
 * <pre>
 * conversation.headers.add(new org.restlet.engine.header.Header('X-Pingback', 'http://mysite.org/pingback/'))
 * </pre>
 * 
 * @name conversation.headers
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/util/Series.html">org.restlet.util.Series</a>&lt;<a href="http://restlet.org/learn/javadocs/2.2/jse/api/jse/engine/index.html?org/restlet/engine/header/Header.html">org.restlet.engine.header.Header</a>&gt;
 */

/**
 * Provides low-level access to the content preferences negotiated between those announced as supported by the client
 * and those supported by your resource.
 * <p>
 * Note that this property is read-only. Usually you will not have to use it; instead, use the read/write properties
 * derived from this property:
 * <p>
 * {@link conversation#mediaType},
 * {@link conversation#language},
 * {@link conversation#characterSet},
 * {@link conversation#mediaType},
 * {@link conversation#tag},
 * {@link conversation#expirationDate},
 * {@link conversation#modificationDate}, and
 * {@link conversation#disposition}.
 * <p>
 * Note that if you change the above properties, the conversation.negotiated property will <i>not</i> change, and
 * instead reflect the original preferences negotiated with the client.
 * <p>
 * Note for manual resources: This property will be null during handleInit, which occurs before format
 * negotiation.
 * 
 * @name conversation.negotiated
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/representation/Variant.html">org.restlet.representation.Variant</a>
 */

/**
 * A collection of all cookies included in the client request.
 * <p>
 * You may prefer to use the {@link conversation#getCookie} API to retrieve a specific cookie you know about.
 * To add a new cookie to the collection, use {@link conversation#createCookie}.
 * <p>
 * If you change a cookie's attributes, make sure to call save() on it in order to ask the client to change
 * it. Or call remove() to ask a client to delete the cookie. A few useful cookie attributes:
 * <ul>
 * <li>cookie.name: (read only)</li>
 * <li>cookie.version: (integer) per a specific cookie.name</li>
 * <li>cookie.value: textual, or text-encoded binary data (note that most clients have strict limits on how much total data is
 * allowed to be stored in all cookies per domain)</li>
 * <li>cookie.domain: the client should only use the cookie with this domain and its subdomains (web browsers will not let you
 * set a cookie for a domain which is not the domain of the request or a subdomain of it)</li>
 * <li>cookie.path: the client should only send the cookie for URIs in the domain that begin with this path ("/" would mean to use
 * it with all URIs)</li>
 * </ul>
 * The following attributes are not received from the client, but you can set them for sending change requests <i>to</i> the client:
 * <ul>
 * <li>cookie.maxAge: age in seconds, after which the client should delete the cookie; maxAge=0 deletes the cookie immediately,
 * while maxAge=-1 (the default) asks the client to keep the cookie only for the duration of the “session” (this is defined by
 * the client; for most web browsers this means that the cookie will be deleted when the browser is closed)</li>
 * <li>cookie.secure: true if the cookie is meant to be used only in secure (https) connections (defaults to false)</li>
 * <li>cookie.accessRestricted: true if the cookie is meant to be used only in authenticated connections (defaults to false)</li>
 * <li>cookie.comment: some clients store this, some discard it</li>
 * </ul>
 * Note that you can only <i>ask</i> a client to change, store or delete cookies, and should not rely on your request
 * being satisfied. It's entirely up to the client to decide what to do with your request. For example, many web
 * browsers allow users to turn off cookie support entirely or filter out certain cookies.
 * 
 * @name conversation.cookies
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/Collection.html">java.util.Collection</a>&lt;<a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/ConversationCookie.html">com.threecrickets.prudence.util.ConversationCookie</a>&gt;
 */

/**
 * Gets a cookie by its name, or returns null if it doesn't exist. See {@link conversation#cookies}.
 * 
 * @name conversation.getCookie
 * @function
 * @param {String} name The cookie name
 * @returns {<a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/ConversationCookie.html">com.threecrickets.prudence.util.ConversationCookie</a>} The cookie, or null if it does not exist
 */

/**
 * Returns a new cookie instance if the cookie doesn't exist yet, or the existing cookie instance if it does. The cookie is added to
 * {@link conversation#cookies}.
 * <p>
 * For new cookies, be sure to call save() on the cookie in order to send it in the response, thus asking the client to create it,
 * or remove() if you want to cancel the creation (in which case nothing will be sent in the response).
 *
 * @name conversation.createCookie
 * @function
 * @param {String} name The cookie name
 * @returns {<a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/util/ConversationCookie.html">com.threecrickets.prudence.util.ConversationCookie</a>} The cookie
 */

/**
 * Provides low-level access to the underlying Restlet request instance for the current conversation.
 *
 * @name conversation.request
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/Request.html">org.restlet.Request</a>
 */

/**
 * Provides low-level access to the underlying Restlet client info instance for the current conversation.
 *
 * @name conversation.client
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/ClientInfo.html">org.restlet.data.ClientInfo</a>
 */

/**
 * Provides low-level access to the underlying Restlet response instance for the current conversation.
 *
 * @name conversation.response
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/Response.html">org.restlet.Response</a>
 */

/**
 * Provides low-level access to the underlying Restlet resource instance for the current conversation.
 *
 * @name conversation.resource
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/resource/ServerResource.html">org.restlet.resource.ServerResource</a>
 */

/**
 * True if the conversation is internal, meaning that it is using the "riap" protocol.
 * See {@link document#internal} for more information.
 * <p>
 * Testing for internal conversations can allow you to optimize for special cases,
 * for example bypassing permission check. If you do so, be aware of security concerns
 * in other parts of your application, which could allow a hacker to inject a
 * "riap://" URI somehow and bypass your defences.
 * 
 * @name conversation.internal
 * @type Boolean
 */

/**
 * If true disables the error handler. If false the error handler will take over your response for error
 * status codes (400 and above).
 * <p>
 * See {@link conversation#statusCode} for usage.
 * 
 * @name conversation.statusPassthrough
 * @type Boolean
 */

/**
 * Abruptly ends the conversation, returning whatever is left of the response to the client.
 * <p>
 * Especially useful in scriptlet resources as a way to avoid execution of the rest of the scriptlet code.
 * <p>
 * Implementation note: This method works by throwing a special <a href="http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/service/ConversationStoppedException.html">com.threecrickets.prudence.service.ConversationStoppedException</a>
 * which is caught and specially handled by Prudence.
 * 
 * @name conversation.stop
 * @function
 */

/**
 * Low-level access to the expiration date.
 * <p>
 * You may prefer to use {@link conversation#expirationTimestamp} instead.
 * <p>
 * <i>Availability: only available for manual resources.</i>
 *
 * @name conversation.expirationDate
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/Date.html">java.util.Date</a>
 */

/**
 * Clients are asked not to use cached versions of the response entity after this timestamp and also not to perform conditional
 * requests until then.
 * <p>
 * Most web browsers will respect this value, allowing you to both speed up web page load time considerably,
 * as well as to save bandwidth and server resources. It is highly recommended to make good use of this feature!
 * <p>
 * It is not always feasible to calculate an expiration timestamp, in which case it may be possible to use
 * {@link conversation#modificationTimestamp} instead. The latter does require a conditional request,
 * but is the next best feature.
 * <p>
 * You can also use {@link conversation#maxAge} instead of this property, which also adds a few extra features.
 * If you set both, most clients will consider conversation.maxAge to supercede this property.
 * It's often a good idea to set both in order to support all kinds of clients.
 * <p>
 * The value is a long integer representing the number of milliseconds since January 1, 1970, 00:00:00 GMT ("Unix time").
 * Note that if you set this property, you <i>cannot unset it by setting it to 0</i>, because that would technically
 * refer to Jan 1 1970. Instead, set {@link conversation#expirationDate} to null.
 * <p>
 * <i>Availability: only available for manual resources.</i>
 *
 * @name conversation.expirationTimestamp
 * @type Number
 */

/**
 * Clients are asked not to use cached versions of the response entity after this number of seconds has passed and also not
 * to perform conditional requests until then.
 * <p>
 * Most web browsers will respect this value, allowing you to both speed up web page load time considerably,
 * as well as to save bandwidth and server resources. It is highly recommended to make good use of this feature!
 * <p>
 * It is not always feasible to calculate a max age, in which case it may be possible to use
 * {@link conversation#modificationTimestamp} or {@link conversation@tagHttp} instead. The latter properties do require
 * a conditional request, but represent the next best feature.
 * <p>
 * You can also use {@link conversation#expirationTimestamp} instead of this property.
 * If you set both, most clients will consider this property to supercede conversation.expirationTimestamp.
 * It's often a good idea to set both in order to support all kinds of clients.
 * <p>
 * The value is an integer representing the number of seconds since the client received the response, however
 * a value of -1 is special: it signifies that Prudence should use the "no-cache" HTTP directive instead of "max-age".
 * Though it may seem as if it would have the same effect as setting "max-age" to 0, some clients interpret "no-cache"
 * more explicitly and make sure not to store <i>any</i> local copy of the response. Thus, a value of -1 should be
 * preferred over 0 for added security if you indeed do not want to make use the client cache.
 * <p>
 * Implementation note: Unlike most other properties, once you set the max age for the conversation, you cannot "unset" it,
 * you can only change it to another value.
 * <p>
 * <i>Availability: only available for manual resources.</i>
 *
 * @name conversation.maxAge
 * @type Number
 */

/**
 * Low-level access to the modification timestamp.
 * <p>
 * You may prefer to use {@link conversation#expirationTimestamp} instead.
 * <p>
  * <i>Availability: only available for manual resources.</i>
 *
 * @name conversation.modificationDate
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/Date.html">java.util.Date</a>
 */

/**
 * Modification timestamp used by clients for conditional requests.
 * <p>
 * Before retrieving the entire response, the client can check this timestamp in the header and compare it to
 * its cached modification timestamp. If the new modification timestamp is not newer, it can safely terminate the
 * request and use its cached response. This allows you to both speed up web page load time considerably,
 * as well as to save bandwidth and server resources. It is highly recommended to make good use of this feature!
 * <p>
 * It is even better to use {@link conversation#expirationTimestamp}, which would allow you to avoid
 * even the conditional request. However, expiration timestamps are not always possible to calculate,
 * in which case conditional requests are the next best feature.
 * <p>
 * If you cannot calculate a modification timestamp, it may be possible to come up with a tag for your
 * response and use {@link conversation#tagHttp} instead, which would allow for the same effect.
 * <p>
 * The value is a long integer representing the number of milliseconds since January 1, 1970, 00:00:00 GMT ("Unix time").
 * Note that if you set this property, you <i>cannot unset it by setting it to 0</i>, because that would technically
 * refer to Jan 1 1970. Instead, set {@link conversation#modificationDate} to null.
 * <p>
 * <i>Availability: only available for manual resources.</i>
 *
 * @name conversation.modificationTimestamp
 * @type Number
 */

/**
 * Low-level access to the tag.
 * <p>
 * You may prefer to use {@link conversation#tagHttp} instead.
 * <p>
 * <i>Availability: only available for manual resources.</i>
 *
 * @name conversation.tag
 * @type <a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/Tag.html">org.restlet.data.Tag</a>
 */

/**
 * Tag used by clients for conditional requests.
 * <p>
 * Before retrieving the entire response, the client can check this tag in the header and compare it to
 * its cached tag. If the new tag is identical, it can safely terminate the
 * request and use its cached response. This allows you to both speed up web page load time considerably,
 * as well as to save bandwidth and server resources. It is highly recommended to make good use of this feature!
 * <p>
 * "Weak" and "strong" tags are supported.
 * A matching weak tag means "equivalence" rather than "identity". "Equivalent" means that the cached value may not be identical,
 * but is still "good enough" to be used instead of downloading the new resource. Some clients are able to differentiate
 * between weak and strong tags and may insist on strong matches. Use of weak tags allows you to further finetune your
 * conditional requests for maximal possible scalability.
 * <p>
 * It is even better to use {@link conversation#expirationTimestamp}, which would allow you to avoid
 * even the conditional request. However, expiration timestamps are not always possible to calculate,
 * in which case conditional requests are the next best feature.
 * <p>
 * Common ways for coming with a tag include hashing functions (which can be resource intensive, and thus
 * may have adverse effects on scalability) as well as simple version numbers.
 * If the tag you came up with is simply a modification timestamp of your
 * response, then it's better to use {@link conversation#modificationTimestamp} instead.
 * <p>
 * The value is a string in the <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.11">HTTP entity tag format</a> (ETag):
 * <ul>
 * <li>Strong tags are surrounded by double-quotes. Example: conversation.tagHttp = '"123456789abc"'
 * <li>Weak tags begin with "W/" and follow with a double-quoted string. Example: conversation.tagHttp = 'W/"123456789abc"'
 * </ul>
 * <p>
 * <i>Availability: only available for manual resources.</i>
 *
 * @name conversation.tagHttp
 * @type String
 */

/**
 * Asks the client to redirect its request (repeat it) to another URI
 * via HTTP status code 301 ("permanent redirection").
 * <p>
 * Note that you should prefer to use <i>absolute URIs</i> for the target, never relative URIs.
 * For example, use 'http://mysite.org/myapp/new/uri/' rather than '/new/uri/'.
 * Relative URIs are handled inconsistently by clients: some treat them as relative to
 * the requested URI, some relative to the domain name, and others may attempt to treat
 * any URI as if it was an absolute URI, leading to an error.
 * <p>
 * If you want to redirect within your Prudence component's URI-space, you may want to
 * use {@link conversation#reference} in order to get the current reference, especially
 * the "baseRef" attribute, which is usually the current application's root URI on
 * the virtual host. For example:
 * <code>
 * conversation.redirectPermament(conversation.reference.baseRef + '/new/uri/')
 * </code> 
 * Note that you do not have control over whether the client will repeat its request:
 * most web browsers will honor this status, but some clients may not. 
 * Furthermore, clients may or may not cache the 'permament' redirection
 * information.
 * <p>
 * Prudence also supports statically configured routing via routing.js.
 * See {@link Prudence.Routing.Redirect}. 
 * 
 * @name conversation.redirectPermament
 * @function
 * @param {String} uri
 */

/**
 * Asks the client to perform a new "GET" request to a URI
 * via HTTP status code 303 ("see other").
 * <p>
 * Note that you should prefer to use <i>absolute URIs</i> for the target, never relative URIs.
 * For example, use 'http://mysite.org/myapp/new/uri/' rather than '/new/uri/'.
 * Relative URIs are handled inconsistently by clients: some treat them as relative to
 * the requested URI, some relative to the domain name, and others may attempt to treat
 * any URI as if it was an absolute URI, leading to an error.
 * <p>
 * If you want to redirect within your Prudence component's URI-space, you may want to
 * use {@link conversation#reference} in order to get the current reference, especially
 * the "baseRef" attribute, which is usually the current application's root URI on
 * the virtual host. For example:
 * <code>
 * conversation.redirectSeeOther(conversation.reference.baseRef + '/new/uri/')
 * </code> 
 * Note that you do not have control over whether the client will repeat its request:
 * most web browsers will honor this status, but some clients may not. 
 * <p>
 * Prudence also supports statically configured routing via routing.js.
 * See {@link Prudence.Routing.Redirect}. 
 * 
 * @name conversation.redirectSeeOther
 * @function
 * @param {String} uri
 */

/**
 * Asks the client to redirect its request (repeat it) to another URI
 * via HTTP status code 307 ("temporary redirection").
 * <p>
 * Note that you should prefer to use <i>absolute URIs</i> for the target, never relative URIs.
 * For example, use 'http://mysite.org/myapp/new/uri/' rather than '/new/uri/'.
 * Relative URIs are handled inconsistently by clients: some treat them as relative to
 * the requested URI, some relative to the domain name, and others may attempt to treat
 * any URI as if it was an absolute URI, leading to an error.
 * <p>
 * If you want to redirect within your Prudence component's URI-space, you may want to
 * use {@link conversation#reference} in order to get the current reference, especially
 * the "baseRef" attribute, which is usually the current application's root URI on
 * the virtual host. For example:
 * <code>
 * conversation.redirectTemporary(conversation.reference.baseRef + '/new/uri/')
 * </code> 
 * Note that you do not have control over whether the client will repeat its request:
 * most web browsers will honor this status, but some clients may not. 
 * <p>
 * Prudence also supports statically configured routing via routing.js.
 * See {@link Prudence.Routing.Redirect}. 
 * 
 * @name conversation.redirectTemporary
 * @function
 * @param {String} uri
 */

/**
 * Adds a server-supported media (MIME) type for content negotiation.
 * <p>
 * It is a shortcut, equivalent to calling: conversation.resource.variants.add(mediaType).
 * <p>
 * This is a low-level version, you may prefer to use {@link conversation#addMediaTypeByName} instead.
 * <p>
 * <i>Availability: only available for manual resources.</i>
 *
 * @name conversation.addMediaType
 * @function
 * @param {<a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/MediaType.html">org.restlet.data.MediaType</a>} mediaType
 * @see conversation#addMediaTypeWithLanguage
 */

/**
 * A variant of {@link conversation#addMediaType} that supports an additional "language" param.
 * 
 * @name conversation.addMediaTypeWithLanguage
 * @function
 * @param {<a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/MediaType.html">org.restlet.data.MediaType</a>} mediaType
 * @type {<a href="http://restlet.org/learn/javadocs/2.2/jse/api/index.html?org/restlet/data/Language.html">org.restlet.data.Language</a>} language
 */

/**
 * Adds a server-supported media (MIME) type for content negotiation.
 * <p>
 * It is only useful to call this during the handleInit of a manual resource, where it will add
 * the media type to an internal table of supported media types for your resource.
 * <p>
 * If you add a supported media type, then you should also be prepared to return representations
 * in the appropriate format in your other handlers. After content is negotiated with the client,
 * {@link conversation#mediaTypeName} will reflect the final media type selected in handleGet,
 * handlePost, etc., so you can use a simple "if" statement there to select the proper represenation
 * format.
 * <p>
 * <i>Order matters.</i> During content negotiation, if several media types are equally valid,
 * then the first one you added of the final compatible list will be selected. (Note that the
 * order used by the client is also taken into account.) It is thus best to add the most
 * efficient/useful media types first.
 * <p>
 * It is important to emphasize the fact that the supported media type table <i>is not static</i>.
 * Your handleInit can add different media types according to dynamically changing situations.
 * For example, let's imagine that your resource supports both streaming video and non-streaming video
 * files, but that streaming video is only supported when certain conditions are met: the streaming
 * subsystem is up and running, the user has the right permissions, etc. You can thus wrap your
 * conversation.addMediaTypeByName calls in "if" statements, and limit content negotiation accordingly.
 * <p>
 * Supported value are defined by the MIME (Multipurpose Internet Mail Extensions) standard. Examples include: "text/plain",
 * "text/html", "application/json", and "application/x-www-form-urlencoded". You can also use shortcuts for these
 * via the {@link conversation#addMediaTypeByExtension} API.
 * <p>
 * Example:
 * <pre>
 * function handleInit(conversation) {
 *   conversation.addMediaTypeByName('application/json')
 *   conversation.addMediaTypeByName('application/xml')
 *   conversation.addMediaTypeByName('text/plain')
 * }
 * </pre>
 * <i>Availability: only available for manual resources.</i>
 *
 * @name conversation.addMediaTypeByName
 * @function
 * @param {String} mediaTypeName The MIME type name
 * @see conversation#addMediaTypeByNameWithLanguage
 */

/**
 * A variant of {@link conversation#addMediaTypeByName} that supports an additional "languageName" param.
 * 
 * @name conversation.addMediaTypeByNameWithLanguage
 * @function
 * @param {String} mediaTypeName The MIME type name
 * @param {String} languageName The language name
 */

/**
 * Identicaly to {@link conversation#addMediaTypeByName}, but accepts shorter application-specific
 * shortcuts instead.
 * <p>
 * These shortcuts are in fact identical to the filename extension mapping for the application. For example,
 * "txt" is usually equivalent to MIME "text/plain" and "xml" is usually equivalent to "application/xml".
 * The exact mapping is configured in your application's settings.js, under "settings.mediaTypes".
 * <p>
 * <i>Availability: only available for manual resources.</i>
 *
 * @name conversation.addMediaTypeByExtension
 * @function
 * @param {String} mediaTypeExtension The MIME type extension
 * @see conversation#addMediaTypeByExtensionWithLanguage
 */

/**
 * A variant of {@link conversation#addMediaTypeByExtension} that supports an additional "languageName" param.
 * 
 * @name conversation.addMediaTypeByExtensionWithLanguage
 * @function
 * @param {String} mediaTypeExtension The MIME type extension
 * @param {String} languageName The language name
 */

/**
 * The "executable" is the low-level Scripturian equivalent of "this document" (see {@link document}). It is rarely used
 * in Prudence code, however it could be useful.
 * 
 * @name executable
 * @namespace
 * @see <a href="http://threecrickets.com/api/java/scripturian/index.html?com/threecrickets/scripturian/service/ExecutableService.html">The Scripturian API documentation</a>
 */

/**
 * Executable globals are general purpose attributes accessible by any code in the JVM.
 * <p>
 * These are similar to {@link application#sharedGlobals}, in that they can be shared between Prudence applications,
 * except that they are in fact global to the entire JVM. This can be useful if you are using Scripturian elsewhere in
 * your Sincerity container, outside of Prudence, and need to share state.
 * <p>
 * Names can be any string, but the convention is to use "." paths to allow for unique "namespaces"
 * that would not overlap with future extensions, expansions or third-party libraries. For example,
 * use "myapp.data.sourceName" rather than "dataSourceName" to avoid conflict.
 * <p>
 * Implementation note: Prudence's application globals are identical to Restlet application attributes.
 * 
 * @name executable.globals
 * @type <a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/concurrent/ConcurrentMap.html">java.util.concurrent.ConcurrentMap</a>&lt;String, Object&gt;
 */

/**
 * Low-level access to the Scripturian execution context used to execute this executable.
 * <p>
 * Some useful attributes:
 * <ul>
 * <li>executable.context.writer: direct access to the output writer (writes to a memory buffer in scriptlet resources, and to standard output otherwise)</li>
 * <li>executable.context.exposedVariables: access to the namespaces ("application", "document", "executable", "conversation")</li>
 * <li>executable.context.attributes: for internal use by the language engines</li>
 * </ul>
 * 
 * @name executable.context
 * @type <a href="http://threecrickets.com/api/java/scripturian/index.html?com/threecrickets/scripturian/ExecutionContext.html">com.threecrickets.scripturian.ExecutionContext</a>
 */

/**
 * Low-level access to the Scripturian language manager used to create this executable.
 * <p>
 * Here you can query which languages are supported by the current Sincerity container, specifically: executable.manager.adapters.
 * 
 * @name executable.manager
 * @type <a href="http://threecrickets.com/api/java/scripturian/index.html?com/threecrickets/scripturian/LanguageManager.html">com.threecrickets.scripturian.LanguageManager</a>
 */

/**
 * In Prudence, this identical to the {@link document} namespace. For example, "document.include" is the same as "executable.container.include".
 * <p>
 * (Internally, Prudence uses this equivalence to hook the include scriptlet, a Scripturian feature, into Prudence's document.include functionality.)
 * 
 * @name executable.container
 */
