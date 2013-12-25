
/*
 * These are used by the application.distributedGlobals and
 * application.distributedSharedGlobals APIs in Prudence.
 */

// Distributed shared globals

map = new MapConfig()
map.name = 'com.threecrickets.prudence.distributedSharedGlobals'
map.backupCount = 1
config.addMapConfig(map)

// Distributed globals

var applicationsFile = sincerity.container.getFile('component', 'applications')
if (applicationsFile.exists()) {
	var applicationDirs = applicationsFile.listFiles()
	for (var i in applicationDirs) {
		var applicationDir = applicationDirs[i]
		if (!applicationDir.directory || applicationDir.hidden) {
			continue
		}

		var name = applicationDir.name

		map = new MapConfig()
		map.name = 'com.threecrickets.prudence.distributedGlobals.' + name
		map.backupCount = 1
		config.addMapConfig(map)
	}
}
