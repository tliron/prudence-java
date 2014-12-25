
/*
 * These are used by the Hazelcast cache backend in Prudence:
 * 
 * http://threecrickets.com/api/java/prudence/index.html?com/threecrickets/prudence/cache/HazelcastCache.html
 */

// Cache

var map = new MapConfig()
map.name = 'com.threecrickets.prudence.cache'
var nearCache = new NearCacheConfig()
nearCache.maxSize = 5000
nearCache.evictionPolicy = EvictionPolicy.LRU
nearCache.timeToLiveSeconds = 0
nearCache.maxIdleSeconds = 0
nearCache.invalidateOnChange = true
map.nearCacheConfig = nearCache
config.addMapConfig(map)

// Cache tags

var multiMap = new MultiMapConfig()
multiMap.name = 'com.threecrickets.prudence.cacheTagMap'
multiMap.valueCollectionType = MultiMapConfig.ValueCollectionType.SET
config.addMultiMapConfig(multiMap)
