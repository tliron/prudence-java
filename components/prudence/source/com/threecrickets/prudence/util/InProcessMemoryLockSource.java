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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * An in-process (heap) lock source. Internally uses a {@link ConcurrentHashMap}
 * .
 * <p>
 * Note that this implementation does not check for overall heap consumption nor
 * free system memory.
 * 
 * @author Tal Liron
 */
public class InProcessMemoryLockSource implements LockSource
{
	//
	// LockSource
	//

	public Lock getReadLock( String key )
	{
		return getLock( key ).readLock();
	}

	public Lock getWriteLock( String key )
	{
		return getLock( key ).writeLock();
	}

	public void discard( String key )
	{
		locks.remove( key );
	}

	public void discardAll()
	{
		locks.clear();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * A pool of read/write locks per key.
	 */
	private final ConcurrentMap<String, ReadWriteLock> locks = new ConcurrentHashMap<String, ReadWriteLock>();

	private ReadWriteLock getLock( String key )
	{
		ReadWriteLock lock = locks.get( key );
		if( lock == null )
		{
			lock = new ReentrantReadWriteLock();
			ReadWriteLock existing = locks.putIfAbsent( key, lock );
			if( existing != null )
				lock = existing;
		}
		return lock;
	}
}
