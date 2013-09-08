
require 'data/lua'
json = require 'data/dkjson'

function get_state ()
	return json.decode(get_global('lua.state', function ()
		return json.encode({name='Coraline', media='Film', rating='A+', characters={'Coraline', 'Wybie', 'Mom', 'Dad'}})
	end))
end

function set_state (value)
	application:getGlobals():put('lua.state', json.encode(value))
end

function handle_init (conversation)
    conversation:addMediaTypeByName('application/json')
    conversation:addMediaTypeByName('text/plain')
end

function handle_get (conversation)
	local state = get_state()
	return json.encode(state)
end

function handle_post (conversation)
	local update = json.decode(conversation:getEntity():getText())
	
	-- Update our state
	local state = get_state()
	for k, v in pairs(update) do
		state[k] = v
	end
	set_state(state)

	return handle_get(conversation)
end


function handle_put (conversation)
	local update = json.decode(conversation:getEntity():getText())
	set_state(update)

	return handle_get(conversation)
end

function handle_delete (conversation)
	set_state({})

	return null
end
