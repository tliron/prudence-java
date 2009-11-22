#
# Important!
#
# JRuby handles each evaluation by wrapping it in a special "main" object. Unfortunately for Scripturian, this
# means that we are not in the global scope: each scriptlet is run inside its own "main" object. For scriptlets
# to share resources, we need to explicitly access global variables.
#
# A straightforward way to do this is to wrap the global parts of each scriptlet in an anonymous module
# stored in a global variable, as we do here. This module can then be accessed by or included into other
# scriptlets, just like any other module. One limitation of this technique is that it's not that simple to
# "reopen" the module for modification in other scriptlets. The simplest usage would thus be to have each
# scriptlet have its own module or modules. That's actually good practice! 
#
# Another way is to store methods as closures in global variables. For example:
#
# def mymethod value
# 	print value*3
# end
# $mymethod = method :mymethod
#
# Or even store raw lambdas:
#
# $mymethod = lambda do |value|
# 	print value*3
# end
#
# If you do this, don't forget that closures must be called using ".call". For example:
#
# $mymethod.call 5
#

$static_module = Module.new do

	# Helper to access the context attributes

	def get_context_attribute name
		value = $document.container.resource.context.attributes[name]
		if value == nil
			value = yield

			# Note: another thread might have changed our value in the meantime.
			# We'll make sure there is no duplication.

			existing = $document.container.resource.context.attributes.put_if_absent name, value
			if existing != nil
				value = existing
			end

		end

		return value
	end

end