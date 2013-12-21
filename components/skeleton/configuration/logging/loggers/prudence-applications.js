
var applicationsFile = sincerity.container.getFile('component', 'applications')
if (applicationsFile.exists()) {
	var applicationDirs = applicationsFile.listFiles()
	for (var i in applicationDirs) {
		var applicationDir = applicationDirs[i]
		if (!applicationDir.directory || applicationDir.hidden) {
			continue
		}

		var name = applicationDir.name
		
		configuration.logger({
			name: 'prudence.' + name,
			level: 'info',
			appenders: 'file:application.' + name,
			additivity: false
		})
	}
}
