
var executor = java.util.concurrent.Executors.newScheduledThreadPool(java.lang.Runtime.runtime.availableProcessors() * 2 + 1)
component.context.attributes.put('com.threecrickets.prudence.executor', executor)
