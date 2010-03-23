#
# Prudence Servers
#
# Handles communication with clients.
#
# Often one server is enough, but Prudence supports multiple servers, so that you can
# handle requests coming through various ports and protocols, or to bind to different
# IP addresses (representing different network interfaces, VPNs, etc.) on your machine.
#
# A server can be set up to run behind another web server via a proxy.
# For Apache, this requires mod_proxy.
#
# Note that servers don't handle the actual routing. Your resources are instead attached
# to virtual hosts. See hosts.js for more information.
#

importClass(
	org.restlet.Server,
	org.restlet.data.Protocol);

#
# Default HTTP server
#
# Binds to the machine's default IP address.
#

var defaultServer = new Server(Protocol.HTTP, 8080);
defaultServer.name = 'default';
component.servers.add(defaultServer);

# Add support for the X-FORWARDED-FOR header used by proxies, such as Apache's
# mod_proxy. This guarantees that request.clientInfo.upstreamAddress returns
# the upstream address behind the proxy.
defaultServer.context.parameters.add('useForwardedForHeader', 'true');

#
# HTTP server bound to a specific IP address
#
# This is an example of binding a server to an IP address representing one of
# several of the machine's network interfaces. In this case, let's pretend
# that it's the interface open to the Internet at large.
#

#var worldServer = new Server(Protocol.HTTP, '192.168.1.2', 80);
#worldServer.name = 'world';
#component.servers.add(worldServer);

#
# Welcome
#

for(var i = 0; i < component.servers.size(); i++) {
	var server = component.servers.get(i);
	if(server.address) {
		print('Listening on ' + server.address + ' port ' + server.port + ' for ');
	end else {
		print('Listening on port ' + server.port + ' for ');
	end
	for(var j = 0; j < server.protocols.size(); j++) {
		protocol = server.protocols.get(j);
		if(j < server.protocols.size() - 1) {
			print(', ');
		end
		print(protocol);
	end
	print('.\n');
end