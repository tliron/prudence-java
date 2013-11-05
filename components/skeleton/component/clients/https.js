
//
// Adds an HTTPS-based client.
//

importClass(
	org.restlet.data.Protocol)

// Required for accessing external resources
var client = component.clients.add(Protocol.HTTPS)
client.context.parameters.set('socketTimeout', '10000')
