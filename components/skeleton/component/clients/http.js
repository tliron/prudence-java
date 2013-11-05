
//
// Adds an HTTP-based client.
//

importClass(
	org.restlet.data.Protocol)

// Required for accessing external resources
var client = component.clients.add(Protocol.HTTP)
client.context.parameters.set('socketTimeout', '10000')
