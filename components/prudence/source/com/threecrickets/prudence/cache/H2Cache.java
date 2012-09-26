/**
 * Copyright 2009-2012 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.cache;

import org.h2.jdbcx.JdbcDataSource;

import com.threecrickets.prudence.util.InProcessMemoryLockSource;
import com.threecrickets.prudence.util.LockSource;

/**
 * An <a href="http://www.h2database.com/">H2 database</a> cache.
 * 
 * @author Tal Liron
 */
public class H2Cache extends SqlCache
{
	//
	// Construction
	//

	/**
	 * Construction with a max entry count of 1000 entries and 10 connections in
	 * the pool, and an {@link InProcessMemoryLockSource}.
	 * 
	 * @param path
	 *        The H2 database path
	 * @param lockSource
	 *        The lock source
	 */
	public H2Cache( String path )
	{
		this( path, 1000, 10, new InProcessMemoryLockSource() );
	}

	/**
	 * Construction with a max entry count of 1000 entries and 10 connections in
	 * the pool.
	 * 
	 * @param path
	 *        The H2 database path
	 * @param lockSource
	 *        The lock source
	 */
	public H2Cache( String path, LockSource lockSource )
	{
		this( path, 1000, 10, lockSource );
	}

	/**
	 * Constructor.
	 * 
	 * @param path
	 *        The H2 database path
	 * @param maxSize
	 *        The max entry count
	 * @param poolSize
	 *        The number of connections in the pool
	 * @param lockSource
	 *        The lock source
	 */
	public H2Cache( String path, int maxSize, int poolSize, LockSource lockSource )
	{
		super( createDataSource( path ), maxSize, poolSize, lockSource );

		validateTables( false );
		// debug=true;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static JdbcDataSource createDataSource( String path )
	{
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL( "jdbc:h2:" + path + ";MVCC=TRUE" );
		return dataSource;
	}
}
