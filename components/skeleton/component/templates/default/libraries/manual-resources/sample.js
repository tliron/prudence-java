
document.executeOnce('/sincerity/classes/')
document.executeOnce('/sincerity/templates/')

ExampleResource = Sincerity.Classes.define(function() {
	var Public = {}
	
	Public.handleInit = function(conversation) {
		conversation.addMediaTypeByName('text/html')
		conversation.addMediaTypeByName('text/plain')
	}

	Public.handleGet = function(conversation) {
		if (conversation.mediaTypeName == 'text/html') {
			return '<html><body><p>This is a manual resource example</p></body></html>'
		}
		else {
			return 'This is a manual resource example'
		}
	}

	return Public
}())