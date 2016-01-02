--
-- This file is part of the Prudence Foundation Library
--
-- Copyright 2009-2016 Three Crickets LLC.
--
-- The contents of this file are subject to the terms of the LGPL version 3.0:
-- http://www.gnu.org/copyleft/lesser.html
--
-- Alternatively, you can obtain a royalty free commercial license with less
-- limitations, transferable or non-transferable, directly from Three Crickets
-- at http://threecrickets.com/
--

resources = {}

document:executeOnce(application:getGlobals():get('com.threecrickets.prudence.dispatcher.lua.resources'))

function handle (conversation, method)
	local id = conversation:getLocals():get('com.threecrickets.prudence.dispatcher.id')
	local resource = resources[id]
	if resource == nil then
		conversation:setStatusCode(404)
		return nil
	end
	method = resource[method]
	if method == nil then
		conversation:setStatusCode(405)
		return nil
	end
	return method(resource, conversation)
end

function handle_init (conversation)
	handle(conversation, 'handle_init')
end

function handle_get (conversation)
	return handle(conversation, 'handle_get')
end

function handle_get_info (conversation)
	return handle(conversation, 'handle_get_info')
end

function handle_post (conversation)
	return handle(conversation, 'handle_post')
end

function handle_put (conversation)
	return handle(conversation, 'handle_put')
end

function handle_delete (conversation)
	return handle(conversation, 'handle_delete')
end
