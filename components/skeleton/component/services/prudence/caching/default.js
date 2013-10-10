
//
// Create a chain cache and add all caches (in alphabetical order)
// from the "/backends/" subdirectory.
//

document.require('/sincerity/container/')

var chainCache = new com.threecrickets.prudence.cache.ChainCache()
component.context.attributes.put('com.threecrickets.prudence.cache', chainCache)

Sincerity.Container.executeAll('backends')
