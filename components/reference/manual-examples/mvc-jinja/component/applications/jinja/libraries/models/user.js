var Models = Models || {}
​
/**
 * Retrieve a person model from the database.
 */
Models.getPerson = function(name) {
	var person = new Models.Person()
	person.setUsername(name)
	return person
}
​
Models.Person = function() {
	this.getUsername = function() {
		return this.username
	}
​
	this.setUsername = function(username) {
		this.username = username
	}
​
	this.getComments = function() {
		return this.comments
	}
​
	this.comments = new Models.Messages()
}
​
Models.Messages = function() {
	this.get = function() {
		return this.messages
	}
​
	this.add = function(message) {
		this.messages.append(message)
	}
​
	this.messages = ['This is a test.', 'This is also a test.']
}