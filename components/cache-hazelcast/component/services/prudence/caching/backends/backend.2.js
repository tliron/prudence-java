
//
// Adds a Hazelcast-based cache to the cache chain.
//

// We must make sure Hazelcast is initialized 
document.require('/component/services/prudence/distributed/')

chainCache.caches.add(new com.threecrickets.prudence.cache.HazelcastCache())
