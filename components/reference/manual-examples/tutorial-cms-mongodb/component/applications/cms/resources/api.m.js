document.require(
	'/prudence/resources/',
	'/sincerity/json/',
	'/mongo-db/')

var collection = new MongoDB.Collection('pages')

function handleInit(conversation) {
	conversation.addMediaTypeByName('application/json')
	var id = 'page.' + conversation.wildcard
	caching.duration = 60000
	caching.tags.add(id)
}

function handleGet(conversation) {
	var id = 'page.' + conversation.wildcard
	var doc = collection.findOne({_id: id})
	var content = doc ? doc.content : 'This page is empty.'
	return Sincerity.JSON.to({content: content})
}

function handlePut(conversation) {
	var id = 'page.' + conversation.wildcard
	var payload = Prudence.Resources.getEntity(conversation, 'json')
	collection.upsert({_id: id}, {$set: {content: content}})
	document.cache.invalidate(id)
	return Sincerity.JSON.to({content: payload.content})
}

function handleDelete(conversation) {
	var id = 'page.' + conversation.wildcard
	collection.remove({_id: id})
	document.cache.invalidate(id)
	return null
}