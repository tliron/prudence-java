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

package com.threecrickets.prudence.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import org.restlet.Application;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.ILock;
import com.hazelcast.core.Member;
import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.SerializableApplicationTask;

/**
 * Application service exposed to executables, with the addition of support for
 * distributed tasks.
 * 
 * @author Tal Liron
 * @see DelegatedResource
 * @see GeneratedTextResource
 */
public class DistributedApplicationService extends ApplicationService
{
	//
	// Constants
	//

	/**
	 * Hazelcast instance name attribute for an {@link Application}.
	 */
	public static final String HAZELCAST_INSTANCE_NAME = "com.threecrickets.prudence.hazelcast.instanceName";

	/**
	 * Hazelcast distributed globals name attribute for an {@link Application}.
	 */
	public static final String HAZELCAST_DISTRIBUTED_GLOBALS_MAP_NAME = "com.threecrickets.prudence.hazelcast.distributedGlobalsMapName";

	/**
	 * Hazelcast shared distributed globals name attribute for an
	 * {@link Application}.
	 */
	public static final String HAZELCAST_DISTRIBUTED_SHARED_GLOBALS_MAP_NAME = "com.threecrickets.prudence.hazelcast.distributedSharedGlobalsMapName";

	/**
	 * Hazelcast executor name attribute for an {@link Application}.
	 */
	public static final String HAZELCAST_EXECUTOR_NAME = "com.threecrickets.prudence.hazelcast.executorName";

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param application
	 *        The application
	 */
	public DistributedApplicationService( Application application )
	{
		super( application );
	}

	//
	// Attributes
	//

	/**
	 * The Hazelcast instance.
	 * <p>
	 * The name can be configured via the
	 * "com.threecrickets.prudence.hazelcastInstanceName" application context
	 * attribute, and defaults to "com.threecrickets.prudence".
	 * 
	 * @return The Hazelcast instance
	 * @throws RuntimeException
	 *         If the Hazelcast instance has not been initialized
	 */
	public HazelcastInstance getHazelcast()
	{
		String hazelcastInstanceName = getHazelcastInstanceName();
		HazelcastInstance hazelcast = Hazelcast.getHazelcastInstanceByName( hazelcastInstanceName );
		if( hazelcast == null )
			throw new RuntimeException( "Cannot find a Hazelcast instance named \"" + hazelcastInstanceName + "\"" );
		return hazelcast;
	}

	/**
	 * The Hazelcast executor service.
	 * <p>
	 * The name can be configured via the
	 * "com.threecrickets.prudence.hazelcast.executorName" application context
	 * attribute, and defaults to "default".
	 * 
	 * @return The Hazelcast executor service
	 * @throws RuntimeException
	 *         If the Hazelcast executor service has not been found
	 */
	public IExecutorService getHazelcastExecutorService()
	{
		String hazelcastExecutorName = getHazelcastExecutorName();
		IExecutorService executor = getHazelcast().getExecutorService( hazelcastExecutorName );
		if( executor == null )
			throw new RuntimeException( "Cannot find a Hazelcast executor service named \"" + hazelcastExecutorName + "\"" );
		return executor;
	}

	/**
	 * A map of all values global to the Prudence Hazelcast cluster for this
	 * application.
	 * <p>
	 * This is a Hazelcast distributed map, the name of which can be configured
	 * via the "com.threecrickets.prudence.hazelcast.distributedGlobalsMapName"
	 * application context attribute, and defaults to
	 * "com.threecrickets.prudence.distributedGlobals".
	 * 
	 * @return The distributed globals or null
	 */
	public ConcurrentMap<String, Object> getDistributedGlobals()
	{
		String hazelcastMapName = getDistributedGlobalsHazelcastMapName();
		ConcurrentMap<String, Object> map = getHazelcast().getMap( hazelcastMapName );
		if( map == null )
			throw new RuntimeException( "Cannot find a Hazelcast map named \"" + hazelcastMapName + "\"" );
		return map;
	}

	/**
	 * A map of all values global to the Prudence Hazelcast cluster.
	 * <p>
	 * This is a Hazelcast distributed map, the name of which can be configured
	 * via the
	 * "com.threecrickets.prudence.hazelcast.distributedSharedGlobalsMapName"
	 * application context attribute, and defaults to
	 * "com.threecrickets.prudence.distributedGlobals".
	 * 
	 * @return The distributed globals or null
	 */
	public ConcurrentMap<String, Object> getDistributedSharedGlobals()
	{
		String hazelcastMapName = getDistributedSharedGlobalsHazelcastMapName();
		ConcurrentMap<String, Object> map = getHazelcast().getMap( hazelcastMapName );
		if( map == null )
			throw new RuntimeException( "Cannot find a Hazelcast map named \"" + hazelcastMapName + "\"" );
		return map;
	}

	/**
	 * Gets a value global to the Prudence Hazelcast cluster, atomically setting
	 * it to a default value if it doesn't exist.
	 * <p>
	 * If distributed globals are not set up, does nothing and returns null.
	 * 
	 * @param name
	 *        The name of the distributed global
	 * @param defaultValue
	 *        The default value
	 * @return The distributed global's current value
	 */
	public Object getDistributedGlobal( String name, Object defaultValue )
	{
		ConcurrentMap<String, Object> globals = getDistributedGlobals();
		Object value = globals.get( name );

		if( value == null )
		{
			if( defaultValue != null )
			{
				value = defaultValue;
				Object existing = globals.putIfAbsent( name, value );
				if( existing != null )
					value = existing;
			}
			else
				globals.remove( name );
		}

		return value;
	}

	/**
	 * Gets a value global to the Prudence Hazelcast cluster, atomically setting
	 * it to a default value if it doesn't exist.
	 * <p>
	 * If distributed shared globals are not set up, does nothing and returns
	 * null.
	 * 
	 * @param name
	 *        The name of the distributed shared global
	 * @param defaultValue
	 *        The default value
	 * @return The distributed shared global's current value
	 */
	public Object getDistributedSharedGlobal( String name, Object defaultValue )
	{
		ConcurrentMap<String, Object> sharedGlobals = getDistributedSharedGlobals();
		Object value = sharedGlobals.get( name );

		if( value == null )
		{
			if( defaultValue != null )
			{
				value = defaultValue;
				Object existing = sharedGlobals.putIfAbsent( name, value );
				if( existing != null )
					value = existing;
			}
			else
				sharedGlobals.remove( name );
		}

		return value;
	}

	/**
	 * Gets a cluster-wide lock.
	 * <p>
	 * These locks are evicted automatically.
	 * 
	 * @param name
	 *        The distributed shared lock name
	 * @return The distributed shared lock
	 */
	public ILock getDistributedSharedLock( String name )
	{
		return getHazelcast().getLock( name );
	}

	//
	// Operations
	//

	/**
	 * Submits a task on the Hazelcast cluster.
	 * 
	 * @param <T>
	 *        The task result class
	 * @param applicationName
	 *        The application's full name, or null to default to current
	 *        application's name
	 * @param documentName
	 *        The document name
	 * @param entryPointName
	 *        The entry point name or null
	 * @param context
	 *        The context made available to the task (must be serializable)
	 * @param where
	 *        A {@link Member}, a collection of {@link Member}, any other object
	 *        (the member key), or null to let Hazelcast decide (all members for
	 *        multi=true)
	 * @param multi
	 *        Whether the task should be executed on multiple members
	 * @return A future or map of futures for the task
	 * @see HazelcastInstance#getExecutorService(String)
	 */
	@SuppressWarnings("unchecked")
	public <T> Object distributedExecuteTask( String applicationName, String documentName, String entryPointName, Object context, Object where, boolean multi )
	{
		if( applicationName == null )
			applicationName = getApplication().getName();

		if( multi )
			return multiTask( new SerializableApplicationTask<T>( applicationName, documentName, entryPointName, context ), (Iterable<Member>) where );
		else
			return task( new SerializableApplicationTask<T>( applicationName, documentName, entryPointName, context ), where );
	}

	/**
	 * Submits a task on the Hazelcast cluster.
	 * 
	 * @param <T>
	 *        The task result class
	 * @param applicationName
	 *        The application's full name, or null to default to current
	 *        application's name
	 * @param code
	 *        The code to execute
	 * @param context
	 *        The context made available to the task (must be serializable)
	 * @param where
	 *        A {@link Member}, a collection of {@link Member}, any other object
	 *        (the member key), or null to let Hazelcast decide (all members for
	 *        multi=true)
	 * @param multi
	 *        Whether the task should be executed on multiple members
	 * @return A future (multi=false) or map of members to futures (multi=true)
	 *         for the task
	 * @see HazelcastInstance#getExecutorService(String)
	 */
	@SuppressWarnings("unchecked")
	public <T> Object distributedCodeTask( String applicationName, String code, Object context, Object where, boolean multi )
	{
		if( applicationName == null )
			applicationName = getApplication().getName();

		if( multi )
			return multiTask( new SerializableApplicationTask<T>( applicationName, code, context ), (Iterable<Member>) where );
		else
			return task( new SerializableApplicationTask<T>( applicationName, code, context ), where );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private volatile String hazelcastInstanceName;

	private volatile String hazelcastMapName;

	private volatile String hazelcastExecutorName;

	private String getHazelcastInstanceName()
	{
		ConcurrentMap<String, Object> globals = getGlobals();
		if( hazelcastInstanceName == null )
		{
			hazelcastInstanceName = (String) globals.get( HAZELCAST_INSTANCE_NAME );
			if( hazelcastInstanceName == null )
				hazelcastInstanceName = "com.threecrickets.prudence";
		}
		return hazelcastInstanceName;
	}

	private String getDistributedGlobalsHazelcastMapName()
	{
		ConcurrentMap<String, Object> globals = getGlobals();
		if( hazelcastMapName == null )
		{
			hazelcastMapName = (String) globals.get( HAZELCAST_DISTRIBUTED_GLOBALS_MAP_NAME );
			if( hazelcastMapName == null )
				hazelcastMapName = "com.threecrickets.prudence.distributedGlobals";
		}
		return hazelcastMapName;
	}

	private String getDistributedSharedGlobalsHazelcastMapName()
	{
		ConcurrentMap<String, Object> globals = getGlobals();
		if( hazelcastMapName == null )
		{
			hazelcastMapName = (String) globals.get( HAZELCAST_DISTRIBUTED_SHARED_GLOBALS_MAP_NAME );
			if( hazelcastMapName == null )
				hazelcastMapName = "com.threecrickets.prudence.distributedSharedGlobals";
		}
		return hazelcastMapName;
	}

	private String getHazelcastExecutorName()
	{
		ConcurrentMap<String, Object> globals = getGlobals();
		if( hazelcastExecutorName == null )
		{
			hazelcastExecutorName = (String) globals.get( HAZELCAST_EXECUTOR_NAME );
			if( hazelcastExecutorName == null )
				hazelcastExecutorName = "default";
		}
		return hazelcastExecutorName;
	}

	/**
	 * Submits a task on the Hazelcast cluster.
	 * 
	 * @param task
	 *        The task
	 * @param where
	 *        A {@link Member}, any other object (the member key), or null to
	 *        let Hazelcast decide
	 * @return A future for the task
	 * @see HazelcastInstance#getExecutorService(String)
	 */
	private <T> Future<T> task( SerializableApplicationTask<T> task, Object where )
	{
		IExecutorService executor = getHazelcastExecutorService();

		if( where == null )
			return executor.submit( task );
		else if( where instanceof Member )
			return executor.submitToMember( task, (Member) where );
		else
			return executor.submitToKeyOwner( task, where );
	}

	/**
	 * Submits a task on multiple members of the Hazelcast cluster.
	 * 
	 * @param task
	 *        The task
	 * @param where
	 *        A collection of {@link Member}, an iterable of {@link Member}, or
	 *        null for all members
	 * @return A map of members to futures for the task
	 * @see HazelcastInstance#getExecutorService(String)
	 */
	private <T> Map<Member, Future<T>> multiTask( SerializableApplicationTask<T> task, Iterable<Member> where )
	{
		IExecutorService executor = getHazelcastExecutorService();

		if( where instanceof Collection )
			return executor.submitToMembers( task, (Collection<Member>) where );
		else if( where != null )
		{
			ArrayList<Member> members = new ArrayList<Member>();
			for( Member member : (Iterable<Member>) where )
				members.add( member );
			return executor.submitToMembers( task, members );
		}
		else
			return executor.submitToAllMembers( task );
	}
}
