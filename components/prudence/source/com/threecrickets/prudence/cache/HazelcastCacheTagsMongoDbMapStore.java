/**
 * Copyright 2009-2013 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.cache;

import org.restlet.Component;

/**
 * A Hazelcast persistence implementation over <a
 * href="http://www.mongodb.org/">MongoDB</a>.
 * <p>
 * The MongoDB client must be stored as
 * "com.threecrickets.prudence.cache.HazelcastMongoDbMapStore.mongoDb" in the
 * {@link Component} 's context.
 * <p>
 * The MongoDB database will be "prudence" and the collection will be
 * "hazelcast_cache_tags".
 * <p>
 * TOOD: unfortunately, this class will not be used: MultiMaps do not support
 * MapStore in Hazelcast.
 * 
 * @author Tal Liron
 * @param <K>
 *        Key
 * @param <V>
 *        Value
 */
public class HazelcastCacheTagsMongoDbMapStore<K, V> extends HazelcastMongoDbMapStore<K, V>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 */
	public HazelcastCacheTagsMongoDbMapStore()
	{
		super( "hazelcast_cache_tags" );
	}
}
