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

package com.threecrickets.prudence.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import org.restlet.Application;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.ILock;
import com.hazelcast.core.Member;
import com.hazelcast.core.MemberSelector;
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
	 * Hazelcast default instance name attribute for an {@link Application}.
	 */
	public static final String HAZELCAST_DEFAULT_INSTANCE_NAME = "com.threecrickets.prudence.hazelcast.defaultInstanceName";

	/**
	 * Hazelcast task instance attribute name attribute for an
	 * {@link Application}.
	 */
	public static final String HAZELCAST_TASK_INSTANCE_ATTRIBUTE_NAME = "com.threecrickets.prudence.hazelcast.taskInstanceAttributeName";

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
	 * Hazelcast executor service name attribute for an {@link Application}.
	 */
	public static final String HAZELCAST_EXECUTOR_SERVICE_NAME = "com.threecrickets.prudence.hazelcast.executorServiceName";

	/**
	 * Tags attribute for a Hazelcast {@link Member}.
	 */
	public static final String HAZELCAST_MEMBER_TAGS_ATTRIBUTE = "com.threecrickets.prudence.tags";

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
	 * The Hazelcast default instance.
	 * <p>
	 * The name can be configured via the
	 * "com.threecrickets.prudence.hazelcast.defaultInstanceName" application
	 * context attribute, and defaults to "com.threecrickets.prudence".
	 * 
	 * @return The Hazelcast default instance
	 * @throws RuntimeException
	 *         If the Hazelcast default instance has not been initialized
	 */
	public HazelcastInstance getHazelcastDefaultInstance()
	{
		if( defaultInstance == null )
		{
			String name = getDefaultInstanceName();
			defaultInstance = Hazelcast.getHazelcastInstanceByName( name );
			if( defaultInstance == null )
				throw new RuntimeException( "Can't find a Hazelcast instance named \"" + name + "\"" );
		}

		return defaultInstance;
	}

	/**
	 * The Hazelcast task instance.
	 * <p>
	 * The instance can be set as a component context attribute, which is named
	 * according to the application context attribute
	 * "com.threecrickets.prudence.hazelcast.taskInstanceAttributeName". This
	 * value defaults to "com.threecrickets.prudence.hazelcast.taskInstance".
	 * <p>
	 * If the instance has not been explicitly set, will default to the value of
	 * {@link #getHazelcastDefaultInstance()}.
	 * 
	 * @return The Hazelcast task instance
	 * @throws RuntimeException
	 *         If the Hazelcast instance has not been initialized
	 */
	public HazelcastInstance getHazelcastTaskInstance()
	{
		if( taskInstance == null )
		{
			ConcurrentMap<String, Object> globals = getGlobals();
			String name = (String) globals.get( HAZELCAST_TASK_INSTANCE_ATTRIBUTE_NAME );
			if( name == null )
				name = "com.threecrickets.prudence.hazelcast.taskInstance";

			globals = getSharedGlobals();
			taskInstance = (HazelcastInstance) globals.get( name );

			if( taskInstance == null )
				taskInstance = getHazelcastDefaultInstance();
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
	 * @see #getHazelcastTaskInstance()
	 */
	public IExecutorService getHazelcastExecutorService()
	{
		String name = getExecutorServiceName();
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
		ConcurrentMap<String, Object> map = getHazelcastDefaultInstance().getMap( name );
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
		ConcurrentMap<String, Object> map = getHazelcastDefaultInstance().getMap( hazelcastMapName );
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
		return getHazelcastDefaultInstance().getLock( name );
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
	 *        A {@link Member}, a {@link MemberSelector}, an {@link Iterable} of
	 *        {@link Member}, a string (comma-separated member tags), or null to
	 *        let Hazelcast decide (all members for multi=true)
	 * @param multi
	 *        Whether the task should be executed on multiple members
	 * @return A future (multi=false) or map of members to futures (multi=true)
	 *         for the task
	 * @see HazelcastInstance#getExecutorService(String)
	 */
	public <T> Object distributedCodeTask( String applicationName, String code, Object context, Object where, boolean multi )
	{
		if( applicationName == null )
			applicationName = getApplication().getName();

		if( multi )
			return multiTask( new SerializableApplicationTask<T>( applicationName, code, context ), where );
		else
			return task( new SerializableApplicationTask<T>( applicationName, code, context ), where );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private volatile HazelcastInstance defaultInstance;

	private volatile HazelcastInstance taskInstance;

	private volatile String instanceName;

	private volatile String distributedGlobalsMapName;

	private volatile String distributedSharedGlobalsMapName;

	private volatile String executorName;

	private String getDefaultInstanceName()
	{
		if( instanceName == null )
		{
			ConcurrentMap<String, Object> globals = getGlobals();
			instanceName = (String) globals.get( HAZELCAST_DEFAULT_INSTANCE_NAME );
			if( instanceName == null )
				instanceName = "com.threecrickets.prudence.default";
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

	private String getExecutorServiceName()
	{
		if( executorName == null )
		{
			ConcurrentMap<String, Object> globals = getGlobals();
			executorName = (String) globals.get( HAZELCAST_EXECUTOR_SERVICE_NAME );
			if( executorName == null )
				executorName = "default";
		}
		return executorName;
	}

	/**
	 * Submits a task to the Hazelcast task cluster.
	 * 
	 * @param task
	 *        The task
	 * @param where
	 *        A {@link Member}, a string (comma-separated member tags), or null
	 *        to let Hazelcast decide
	 * @return A future for the task
	 */
	private <T> Future<T> task( SerializableApplicationTask<T> task, Object where )
	{
		IExecutorService executor = getHazelcastExecutorService();

		if( where instanceof String )
		{
			Member member = getTaskMember( HAZELCAST_MEMBER_TAGS_ATTRIBUTE, (String) where );
			if( member != null )
				return executor.submitToMember( task, member );
			else
				return null;
		}
		else if( where instanceof Member )
			return executor.submitToMember( task, (Member) where );
		else
			return executor.submit( task );
	}

	/**
	 * Submits a task to multiple members of the Hazelcast task cluster.
	 * 
	 * @param task
	 *        The task
	 * @param where
	 *        A {@link MemberSelector}, an {@link Iterable} of {@link Member}, a
	 *        string (comma-separated member tags), or null for all members
	 * @return A map of members to futures for the task
	 */
	@SuppressWarnings("unchecked")
	private <T> Map<Member, Future<T>> multiTask( SerializableApplicationTask<T> task, Object where )
	{
		IExecutorService executor = getHazelcastExecutorService();

		if( where instanceof String )
			return executor.submitToMembers( task, new TaggedMembers( HAZELCAST_MEMBER_TAGS_ATTRIBUTE, (String) where ) );
		else if( where instanceof MemberSelector )
			return executor.submitToMembers( task, (MemberSelector) where );
		else if( where instanceof Collection )
			return executor.submitToMembers( task, (Collection<Member>) where );
		else if( where instanceof Iterable )
		{
			ArrayList<Member> members = new ArrayList<Member>();
			for( Member member : (Iterable<Member>) where )
				members.add( member );
			return executor.submitToMembers( task, members );
		}
		else
			return executor.submitToAllMembers( task );
	}

	private Member getTaskMember( String tagsAttribute, String requiredTags )
	{
		String[] requiredTagsArray = requiredTags.split( "," );
		Set<String> requiredTagsSet = new HashSet<String>();
		for( String tag : requiredTagsArray )
			requiredTagsSet.add( tag );

		for( Member member : getHazelcastTaskInstance().getCluster().getMembers() )
		{
			String tags = (String) member.getAttributes().get( tagsAttribute );
			if( tags != null )
			{
				String[] existingsTags = tags.split( "," );
				for( String existingTag : existingsTags )
					if( requiredTagsSet.contains( existingTag ) )
						return member;
			}
		}

		return null;
	}

	private static class TaggedMembers implements MemberSelector
	{
		private TaggedMembers( String tagsAttribute, Set<String> requiredTags )
		{
			this.tagsAttribute = tagsAttribute;
			this.requiredTags = requiredTags;
		}

		private TaggedMembers( String tagsAttribute, String tags )
		{
			this.tagsAttribute = tagsAttribute;
			requiredTags = new HashSet<String>();
			for( String tag : tags.split( "," ) )
				requiredTags.add( tag );
		}

		public boolean select( Member member )
		{
			String tags = (String) member.getAttributes().get( tagsAttribute );
			if( tags != null )
			{
				String[] existingsTags = tags.split( "," );
				for( String existingTag : existingsTags )
					if( requiredTags.contains( existingTag ) )
						return true;
			}
			return false;
		}

		private final String tagsAttribute;

		private final Set<String> requiredTags;
	}
}
