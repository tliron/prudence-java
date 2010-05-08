//
// Prudence Component
//

executable.container.execute('defaults/instance/component/');

importClass(
	com.threecrickets.prudence.cache.H2Cache,
	com.threecrickets.prudence.cache.ChainCache);

// Create an H2-database-backed cache chained after the default memory cache
var defaultCache = component.context.attributes.get('com.threecrickets.prudence.cache');
var chainCache = new ChainCache(defaultCache, new H2Cache('cache/prudence/prudence'));
component.context.attributes.put('com.threecrickets.prudence.cache', chainCache);