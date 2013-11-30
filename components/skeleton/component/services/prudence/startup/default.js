
document.require('/sincerity/localization/')

var startupTasks = []

initializers.push(function() {
	var fixedExecutor = java.util.concurrent.Executors.newFixedThreadPool(java.lang.Runtime.runtime.availableProcessors() * 2 + 1)
	if (startupTasks.length > 0) {
		var futures = []
		var startTime = java.lang.System.currentTimeMillis()
		if (sincerity.verbosity >= 1) {
			println('Executing ' + startupTasks.length + ' startup tasks...')
		}
		for (var t in startupTasks) {
			futures.push(fixedExecutor.submit(startupTasks[t]))
		}
		for (var f in futures) {
			try {
				futures[f].get()
			} catch(x) {}
		}
		if (sincerity.verbosity >= 1) {
			println('Finished all startup tasks in ' + Sincerity.Localization.formatDuration(java.lang.System.currentTimeMillis() - startTime))
		}
	}
})
