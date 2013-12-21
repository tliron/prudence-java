
var applicationsFile = sincerity.container.getFile('component', 'applications')
if (applicationsFile.exists()) {
	var applicationDirs = applicationsFile.listFiles()
	for (var i in applicationDirs) {
		var applicationDir = applicationDirs[i]
		if (!applicationDir.directory || applicationDir.hidden) {
			continue
		}

		var name = applicationDir.name

		var logFile = sincerity.container.getLogsFile('application-' + name + '.log')
		logFile.parentFile.mkdirs()

		configuration.rollingFileAppender({
			name: 'file:application.' + name,
			layout: {
				pattern: '%d: %-5p [%c] %m%n'
			},
			fileName: String(logFile),
			filePattern: String(logFile) + '.%i',
			policy: {
				size: '5MB'
			},
			strategy: {
				minIndex: '1',
				maxIndex: '9'
			}
		})
	}
}
