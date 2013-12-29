
document.require('/sincerity/templates/')

var logger = application.getSubLogger('statistics')

function getCounter() {
	return application.getGlobal('counter', new java.util.concurrent.atomic.AtomicInteger())
}

function handleBefore(conversation) {
	logger.fine('Statistics filter will check this request')
	return 'continue'
}

function handleAfter(conversation) {
	if (conversation.request.method.name == 'POST') {
		var counter = getCounter()
		var count = counter.incrementAndGet()
		logger.info('Counted {0} POSTs so far'.cast(count))
	}
}
