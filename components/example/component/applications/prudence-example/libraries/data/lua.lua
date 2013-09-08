
-- Helper to access the application globals

function get_global (name, get_default_value)
	local value = application:getGlobals():get(name)
	if value == nil then
		value = get_default_value()

		-- Note: another thread might have changed our value in the meantime.
		-- We'll make sure there is no duplication.

		local existing = application:getGlobals():putIfAbsent(name, value)
		if existing ~= nil then
			value = existing
		end
	end
	return value
end
