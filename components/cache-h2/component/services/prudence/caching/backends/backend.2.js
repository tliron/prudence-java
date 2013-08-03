
//
// Adds an H2-based cache to the cache chain.
//
// The H2 database files will be located in the "/cache/prudence/cache/"
// subdirectory.
//

chainCache.caches.add(new com.threecrickets.prudence.cache.H2Cache(sincerity.container.getCacheFile('prudence', 'cache')))
