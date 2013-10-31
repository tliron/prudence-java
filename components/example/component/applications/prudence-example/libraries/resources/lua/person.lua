
Person = {}

function Person:new ()
	o = {}
	setmetatable(o, self)
	self.__index = self
	return o
end

function Person:handle_init (conversation)
	conversation:addMediaTypeByName('text/html')
	conversation:addMediaTypeByName('text/plain')
end

function Person:handle_get (conversation)
	local id = conversation:getLocals():get('id')
	return string.format('I am person %s, formatted as "%s", encased in Lua', id, conversation:getMediaTypeName())
end
