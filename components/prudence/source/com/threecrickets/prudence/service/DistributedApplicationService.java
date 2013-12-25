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
import org.restlet.Component;

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
	 * Hazelcast application instance name attribute for an {@link Application}.
	 */
	public static final String HAZELCAST_APPLICATION_INSTANCE_NAME = "com.threecrickets.prudence.hazelcast.applicationInstanceName";

	/**
	 * Hazelcast task instance attribute for a {@link Component}.
	 */
	public static final String HAZELCAST_TASK_INSTANCE = "com.threecrickets.prudence.hazelcast.taskInstance";

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
	 * The Hazelcast application instance.
	 * <p>
	 * The name can be configured via the
	 * "com.threecrickets.prudence.hazelcast.applicationInstanceName"
	 * application context attribute, and defaults to
	 * "com.threecrickets.prudence".
	 * 
	 * @return The Hazelcast application instance
	 * @throws RuntimeException
	 *         If the Hazelcast application instance has not been initialized
	 */
	public HazelcastInstance getHazelcastApplicationInstance()
	{
		if( applicationInstance == null )
		{
			String name = getInstanceName();
			applicationInstance = Hazelcast.getHazelcastInstanceByName( name );
			if( applicationInstance == null )
				throw new RuntimeException( "Can't find a Hazelcast instance named \"" + name + "\"" );
		}

		return applicationInstance;
	}

	public HazelcastInstance getHazelcastTaskInstance()
	{
		if( taskInstance == null )
		{
			ConcurrentMap<String, Object> globals = getSharedGlobals();
			taskInstance = (HazelcastInstance) globals.get( HAZELCAST_TASK_INSTANCE );
			if( taskInstance == null )
				taskInstance = getHazelcastApplicationInstance();
		}

		return taskInstance;
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
		String name = getExecutorName();
		IExecutorService executor = getHazelcastTaskInstance().getExecutorService( name );
		if( executor == null )
			throw new RuntimeException( "Cannot find a Hazelcast executor service named \"" + name + "\"" );
		return executor;
	}

	/**
	 * A map of all values global to the Prudence cluster for this application.
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
		String name = getDistributedGlobalsMapName();
		ConcurrentMap<String, Object> map = getHazelcastApplicationInstance().getMap( name );
		if( map == null )
			throw new RuntimeException( "Cannot find a Hazelcast map named \"" + name + "\"" );
		return map;
	}

	/**
	 * A map of all values global to the Prudence cluster.
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
		String hazelcastMapName = getDistributedSharedGlobalsMapName();
		ConcurrentMap<String, Object> map = getHazelcastApplicationInstance().getMap( hazelcastMapName );
		if( map == null )
			throw new RuntimeException( "Cannot find a Hazelcast map named \"" + hazelcastMapName + "\"" );
		return map;
	}

	/**
	 * Gets a value global to the Prudence cluster, atomically setting it to a
	 * default value if it doesn't exist.
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
	 * Gets a value global to the Prudence cluster, atomically setting it to a
	 * default value if it doesn't exist.
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
		return getHazelcastApplicationInstance().getLock( name );
	}

	//
	// Operations
	//

	/**
	 * Submits a task on the tasks cluster.
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
	 * Submits a task on the tasks cluster.
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

	private volatile HazelcastInstance applicationInstance;

	private volatile HazelcastInstance taskInstance;

	private volatile String instanceName;

	private volatile String distributedGlobalsMapName;

	private volatile String distributedSharedGlobalsMapName;

	private volatile String executorName;

	private String getInstanceName()
	{
		if( instanceName == null )
		{
			ConcurrentMap<String, Object> globals = getGlobals();
			instanceName = (String) globals.get( HAZELCAST_APPLICATION_INSTANCE_NAME );
			if( instanceName == null )
				instanceName = "com.threecrickets.prudence.application";
		}
		return instanceName;
	}

	private String getDistributedGlobalsMapName()
	{
		if( distributedGlobalsMapName == null )
		{
			ConcurrentMap<String, Object> globals = getGlobals();
			distributedGlobalsMapName = (String) globals.get( HAZELCAST_DISTRIBUTED_GLOBALS_MAP_NAME );
			if( distributedGlobalsMapName == null )
				distributedGlobalsMapName = "com.threecrickets.prudence.distributedGlobals"; // no!
		}
		return distributedGlobalsMapName;
	}

	private String getDistributedSharedGlobalsMapName()
	{
		if( distributedSharedGlobalsMapName == null )
		{
			ConcurrentMap<String, Object> globals = getGlobals();
			distributedSharedGlobalsMapName = (String) globals.get( HAZELCAST_DISTRIBUTED_SHARED_GLOBALS_MAP_NAME );
			if( distributedSharedGlobalsMapName == null )
				distributedSharedGlobalsMapName = "com.threecrickets.prudence.distributedSharedGlobals";
		}
		return distributedSharedGlobalsMapName;
	}

	private String getExecutorName()
	{
		if( executorName == null )
		{
			ConcurrentMap<String, Object> globals = getGlobals();
			executorName = (String) globals.get( HAZELCAST_EXECUTOR_NAME );
			if( executorName == null )
				executorName = "default";
		}
		return executorName;
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
