document.require(
	'/models/user/',
	'/prudence/resources/',
	'/sincerity/objects/')
​
function handleInit(conversation) {
	conversation.addMediaTypeByName('text/html')
}
​
function handleGet(conversation) {
	var name = conversation.locals.get('name')
	var person = Models.getPerson(name)
	return getView('user/comments', {
		username: person.getUsername(),
		comments: person.getComments().get()
	})
}
​
function handlePost(conversation) {
	var name = conversation.locals.get('name')
	var comment = conversation.form.get('comment')
	var person = Models.getPerson(name)
	person.getComments().add(comment)
	return getView('user/comments', {
		username: person.getUsername(),
		comments: person.getComments().get()
	})
}
​
function getView(view, context) {
	var page = Prudence.Resources.request({
		uri: '/views/' + view + '/',
		internal: true,
		method: 'post',
		mediaType: 'text/*',
		payload: {
			type: 'json',
			value: context
		}
	})
	return Sincerity.Objects.exists(page) ? page : 404
}