/**
 * Copyright 2009-2017 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.util;

import java.util.concurrent.locks.Lock;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * A <a href="http://www.hazelcast.com/">Hazelcast</a>-backed lock source.
 * <p>
 * Uses a Hazelcast map and a Hazelcast multimap, defaulting to the names
 * "com.threecrickets.prudence.prudence.cache" and
 * "com.threecrickets.prudence.prduence.cacheTags" respectively. Refer to
 * Hazelcast documentation for instructions on how to configure them.
 * 
 * @author Tal Liron
 */
public class HazelcastLockSource implements LockSource
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 */
	public HazelcastLockSource()
	{
		this( null );
	}

	/**
	 * Constructor.
	 * 
	 * @param hazelcast
	 *        The hazelcast instance or null to use the instance named
	 *        "com.threecrickets.prudence"
	 */
	public HazelcastLockSource( HazelcastInstance hazelcast )
	{
		if( hazelcast == null )
		{
			hazelcast = Hazelcast.getHazelcastInstanceByName( "com.threecrickets.prudence" );
			if( hazelcast == null )
				throw new RuntimeException( "Cannot find a Hazelcast instance named \"com.threecrickets.prudence\"" );
		}
		this.hazelcast = hazelcast;
	}

	//
	// LockSource
	//

	public Lock getReadLock( String key )
	{
		return hazelcast.getLock( key );
	}

	public Lock getWriteLock( String key )
	{
		return hazelcast.getLock( key );
	}

	public void discard( String key )
	{
		hazelcast.getLock( key ).destroy();
	}

	public void discardAll()
	{
		// Not possible in Hazelcast
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The Hazelcast instance.
	 */
	private final HazelcastInstance hazelcast;
}
