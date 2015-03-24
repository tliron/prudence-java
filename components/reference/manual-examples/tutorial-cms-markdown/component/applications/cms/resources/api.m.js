document.require(
	'/prudence/resources/',
	'/sincerity/json/')

function handleInit(conversation) {
	conversation.addMediaTypeByName('application/json')
	var id = 'page.' + conversation.wildcard
	caching.duration = 60000
	caching.tags.add(id)
}

function handleGet(conversation) {
	var id = 'page.' + conversation.wildcard
	var content = application.globals.get(id) || 'This page is empty.'
	return Sincerity.JSON.to({content: content})
}

function handlePut(conversation) {
	var id = 'page.' + conversation.wildcard
	var payload = Prudence.Resources.getEntity(conversation, 'json')
	application.globals.put(id, payload.content)
	document.cache.invalidate(id)
	return Sincerity.JSON.to({content: payload.content})
}

function handleDelete(conversation) {
	var id = 'page.' + conversation.wildcard
	application.globals.remove(id)
	document.cache.invalidate(id)
	return null
}