/**
 * Copyright 2009-2015 Three Crickets LLC.
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

/**
 * A generic source for {@link Lock} instances.
 * 
 * @author Tal Liron
 */
public interface LockSource
{
	/**
	 * Gets a unique lock for a key.
	 * 
	 * @param key
	 *        The key
	 * @return The lock
	 */
	public Lock getReadLock( String key );

	/**
	 * Gets a unique lock for a key.
	 * 
	 * @param key
	 *        The key
	 * @return The lock
	 */
	public Lock getWriteLock( String key );

	/**
	 * Discards the lock for a key.
	 * 
	 * @param key
	 *        The key
	 */
	public void discard( String key );

	/**
	 * Discards all locks.
	 */
	public void discardAll();
}
