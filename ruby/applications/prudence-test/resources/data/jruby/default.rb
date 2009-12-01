#
# This script implements and handles a REST resource. Simply put, it is a state,
# addressed by a URL, that responds to verbs. Verbs represent logical operations
# on the state, such as create, read, update and delete (CRUD). They are primitive
# communications, which include very minimal session and no transaction state. As such,
# they are very straightforward to implement, and can lead to very scalable
# applications. 
#
# The exact URL of this resource depends on its its filename and/or its location in
# your directory structure. See your settings.rb for more information.
#

require 'java'
import java.util.concurrent.locks.ReentrantReadWriteLock
import org.restlet.data.MediaType
import org.restlet.representation.Variant
import org.restlet.ext.json.JsonRepresentation
import org.json.JSONObject

# Include the context library

$document.container.include 'test/jruby/context'
include $static_module

# State
#
# These make sure that our state is properly stored in the context,
# so that we always use the same state, even if this script is recompiled.

def get_state_lock
	return get_context_attribute('jruby.stateLock') do
		ReentrantReadWriteLock.new
	end
end

def get_state
	return get_context_attribute('jruby.state') do
		{'name' => 'Coraline', 'media' => 'Film', 'rating' => 'A+', 'characters' => ['Coraline', 'Wybie', 'Mom', 'Dad']}
	end
end

def set_state value
	$document.container.resource.context.attributes['jruby.state'] = value
end

$state = get_state()
$state_lock = get_state_lock()

# This method is called when the resource is initialized. We will use it to set
# general characteristics for the resource.
	
def handle_init

	# The order in which we add the variants is their order of preference.
	# Note that clients often include a wildcard (such as "*/*") in the
	# "Accept" attribute of their request header, specifying that any media type
	# will do, in which case the first one we add will be used.

	$document.container.variants.add Variant.new MediaType::APPLICATION_JSON
	$document.container.variants.add Variant.new MediaType::TEXT_PLAIN
	
end

# This method is called for the GET verb, which is expected to behave as a
# logical "read" of the resource's state.
#
# The expectation is that it return one representation, out of possibly many, of the
# resource's state. Returned values can be of any explicit sub-class of
# org.restlet.resource.Representation. Other types will be automatically converted to
# string representation using the client's requested media type and character set.
# These, and the language of the representation (defaulting to None), can be read and
# changed via $document.container.media_type, $document.container.character_set, and
# $document.container.language.
#
# Additionally, you can use $document.container.variant to interrogate the client's provided
# list of supported languages and encoding.

def handle_get

	r = nil
	state_lock = get_state_lock()
	state = get_state()

	state_lock.read_lock.lock
	begin
		r = JSONObject.new state
	ensure
		state_lock.read_lock.unlock
	end

	# Return a representation appropriate for the requested media type
	# of the possible options we created in handle_init()

	if $document.container.media_type == MediaType::APPLICATION_JSON
		return JsonRepresentation.new r
	end

	return r
	
end

# This method is called for the POST verb, which is expected to behave as a
# logical "update" of the resource's state.
#
# The expectation is that document.container.entity represents an update to the state,
# that will affect future calls to handle_get(). As such, it may be possible
# to accept logically partial representations of the state.
#
# You may optionally return a representation, in the same way as handle_get().
# Because Ruby methods return the last statement's value by default,
# you must explicitly return a None if you do not want to return a representation
# to the client.

def handle_post

	update = JSONObject.new $document.container.entity.text
	state_lock = get_state_lock()
	state = get_state()
	
	# Update our state
	state_lock.write_lock.lock
	begin
		for key in update.keys
			state[key] = update.get key
		end
	ensure
		state_lock.write_lock.unlock
	end
	
	return handle_get
	
end

# This method is called for the PUT verb, which is expected to behave as a
# logical "create" of the resource's state.
#
# The expectation is that document.container.entity represents an entirely new state,
# that will affect future calls to handle_get(). Unlike handle_post(),
# it is expected that the representation be logically complete.
#
# You may optionally return a representation, in the same way as handle_get().
# Because Ruby methods return the last statement's value by default,
# you must explicitly return a nil if you do not want to return a representation
# to the client.

def handle_put

	update = JSONObject.new $document.container.entity.text

	state = {}	
	for key in update.keys
		state[key] = update.get key
	end
	
	set_state state
	
	return handle_get
	
end

# This method is called for the DELETE verb, which is expected to behave as a
# logical "delete" of the resource's state.
#
# The expectation is that subsequent calls to handle_get() will fail. As such,
# it doesn't make sense to return a representation, and any returned value will
# ignored. Still, it's a good idea to return nil to avoid any passing of value.

def handle_delete

	set_state({})

	return nil
	
end

# For our methods to be invocable from the outside as entry points, we must store them
# as closures in global variables named for the entry points.

$handleInit = method :handle_init
$handleGet = method :handle_get
$handlePost = method :handle_post
$handlePut = method :handle_put
$handleDelete = method :handle_delete 