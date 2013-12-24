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

document.require(
	'/prudence/logging/',
	'/sincerity/objects/',
	'/sincerity/json/',
	'/sincerity/localization/')

var Prudence = Prudence || {}

/**
 * Flexible, JavaScript-friendly wrapper over Prudence's task API. Can be used to start asynchronous tasks, whether
 * in-process or distributed (via a Hazelcast cluster). JavaScript closures can be sent as-is to execute
 * anywhere!
 * <p>
 * Note: This library modifies the {@link Function} prototype.
 * 
 * @namespace
 * @see Visit the <a href="http://www.hazelcast.com/">Hazelcast site</a>
 * 
 * @author Tal Liron
 * @version 1.0
 */
Prudence.Tasks = Prudence.Tasks || function() {
	/** @exports Public as Prudence.Tasks */
    var Public = {}

	/**
	 * The library's logger.
	 *
	 * @field
	 * @returns {Prudence.Logging.Logger}
	 */
	Public.logger = Prudence.Logging.getLogger('tasks')

	/**
	 * Starts a task in another thread, or even another JVM.
	 * <p>
	 * A JavaScript-friendly wrapper over Prudence's application.task family of APIs.
	 * <p>
	 * The argument can be a full params, or two shortcuts: a string (which becomes params.uri), or a
	 * function (which becomes params.fn).
	 * 
	 * @param params All params will be merged into the context, as key 'prudence.task'
	 * @param {String} [params.uri] The document to execute (you must supply either this, params.code or params.fn)
	 * @param {String} [params.code] The code to execute (you must supply either this, params.uri or params.fn)
	 * @param {Function|String} [params.fn] The function to call (you must supply either this, params.code or params.uri)
	 * @param {String} [params.entryPoint=null] An optional entry point in the document (null or undefined to execute
	 *         the entire document); used in conjunction with params.uri
	 * @param [params.context] The context to send to the task; must be serializable for distributed tasks;
	 *         will be made available there as document.context, or as a function argument if you are using params.fn
	 *         or params.entryPoint
	 * @param {Boolean} [params.json=true] True to serialize params.context into JSON; note that distributed
	 *         tasks must have a serializable params.context, so it's good practice to always serialize, unless
	 *         you really need to optimize performance for in-process tasks
	 * @param {Boolean} [params.pure=false] True to keep params.context pure, without special additions from this
	 *         library; implies params.json=false
	 * @param {Number|String} [params.block=0] If greater than zero, will block for a maximum of duration in milliseconds
	 *         waiting for task to finish execution
	 * @param {Number|String} [params.delay=0] The delay in milliseconds before starting the task (ignored for distributed tasks)
	 * @param {Number|String} [params.repeatEvery=0] How often in milliseconds to repeat the task (see params.fixedRepeat),
	 *         zero means the task is executed only once (ignored for distributed tasks)
	 * @param {Boolean} [params.fixedRepeat=false] True if repetition should be fixed according to params.repeatEvery,
	 *         otherwise the delay until the next repetition would begin only when the task finishes an
	 *         execution
	 * @param {Boolean} [params.distributed=false] True to distribute the task
	 * @param {String} [params.application] Application's full name (defaults to name of current application)
	 * @param [params.where] Where to distribute the task (leave empty to let Hazelcast decide)
	 * @param {Boolean} [params.multi=false] True to distribute task to all members of the cluster
	 * @returns {<a href="http://docs.oracle.com/javase/6/docs/api/index.html?java/util/concurrent/Future.html">java.util.concurrent.Future</a>}
	 */
	Public.task = function(params) {
		if (Sincerity.Objects.isString(params)) {
			params = {uri: params}
		}
		else if (typeof params == 'function') {
			params = {fn: String(params)}
		}
		else {
			params = Sincerity.Objects.clone(params)
		}

		var extraContext = {
			'prudence.task': Sincerity.Objects.clone(params)
		}
		
		if (!params.pure) {
			params.context = params.context || {}
			Sincerity.Objects.merge(params.context, extraContext)
		}

		params.json = params.json === undefined ? (params.pure ? false : true) : params.json
		if (params.json) {
			params.context = params.context ? Sincerity.JSON.to(params.context) : null
		}

		if (params.fn) {
			if (typeof params.fn == 'function') {
				params.fn = String(params.fn)
			}
			if (params.json) {
				params.code = "<% document.require('/sincerity/json/'); var _fn=" + params.fn.trim() + "; _fn(Sincerity.JSON.from(document.context)); %>"
			}
			else {
				params.code = '<% var _fn=' + params.fn.trim() + '; _fn(document.context); %>'
			}
		}

		var future
		params.application = params.application || null 
		if (params.distributed) {
			params.where = params.where || null
			params.multi = params.multi || false
			if (Sincerity.Objects.exists(params.code)) {
				future = application.distributedCodeTask(params.application, params.code, params.context, params.where, params.multi)
			}
			else {
				params.entryPoint = params.entryPoint || null
				future = application.distributedExecuteTask(params.application, params.uri, params.entryPoint, params.context, params.where, params.multi)
			}
		}
		else {
			params.delay = Sincerity.Objects.exists(params.delay) ? Sincerity.Localization.toMilliseconds(params.delay) : 0
			params.repeatEvery = Sincerity.Objects.exists(params.repeatEvery) ? Sincerity.Localization.toMilliseconds(params.repeatEvery) : 0
			params.fixedRepeat = params.fixedRepeat || false
			if (Sincerity.Objects.exists(params.code)) {
				future = application.codeTask(params.application, params.code, params.context, params.delay, params.repeatEvery, params.fixedRepeat)
			}
			else {
				params.entryPoint = params.entryPoint || null
				future = application.executeTask(params.application, params.uri, params.entryPoint, params.context, params.delay, params.repeatEvery, params.fixedRepeat)
			}
		}
		if (Sincerity.Objects.exists(params.block)) {
			var block = Sincerity.Localization.toMilliseconds(params.block)
			future.get(block, java.util.concurrent.TimeUnit.MILLISECONDS)
		}
		
		return future
	}

	/**
	 * Shortcut to get the document.context and possibly deserialize it.
	 * 
	 * @param document The Prudence document
	 * @param {Boolean} [json=true] True to deserialize params.context from JSON
	 * @returns The context
	 */
	Public.getContext = function(document, json) {
		json = json === undefined ? true : json
		return json ? Sincerity.JSON.from(document.context) : document.context
	}
	
	return Public
}()

/**
 * Executes the function as a task, which means that it can execute asynchronously or distributed in the cluster.
 * 
 * @methodOf Function#
 * @returns {java.util.concurrent.Future}
 * @see Prudence.Tasks#task
 */
Function.prototype.task = Function.prototype.task || function(params) {
	params = Sincerity.Objects.clone(params) || {}
	params.fn = this
	Prudence.Tasks.task(params)
}
