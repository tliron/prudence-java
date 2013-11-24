
importClass(com.threecrickets.prudence.DelegatedStatusService)

var statusService = component.statusService = new DelegatedStatusService()
statusService.context = component.context.createChildContext()
statusService.fallback = true

// Default custom error pages for all applications
//statusService.capture(404, 'myapp', '/404/', component.context)
