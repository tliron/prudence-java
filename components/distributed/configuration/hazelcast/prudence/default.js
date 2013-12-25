
config.instanceName = 'com.threecrickets.prudence'

// Distributed globals

map = new MapConfig()
map.name = 'com.threecrickets.prudence.distributedSharedGlobals'
map.backupCount = 1
config.addMapConfig(map)

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

// Caching backend

var map = new MapConfig()
var nearCache = new NearCacheConfig()
map.name = 'com.threecrickets.prudence.cache'
nearCache.maxSize = 5000
nearCache.evictionPolicy = 'LRU'
nearCache.timeToLiveSeconds = 0
nearCache.maxIdleSeconds = 0
nearCache.invalidateOnChange = true
map.nearCacheConfig = nearCache
config.addMapConfig(map)

var multiMap = new MultiMapConfig()
multiMap.name = 'com.threecrickets.prudence.cacheTagMap'
multiMap.valueCollectionType = 'SET'
config.addMultiMapConfig(multiMap)
