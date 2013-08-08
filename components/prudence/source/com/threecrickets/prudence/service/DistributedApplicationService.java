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
	 * The Hazelcast instance
	 * 
	 * @return The Hazelcast instance
	 * @throws RuntimeException
	 *         If the Hazelcast instance has not been initialized
	 */
	public HazelcastInstance getHazelcast()
	{
		HazelcastInstance hazelcast = Hazelcast.getHazelcastInstanceByName( "com.threecrickets.prudence" );
		if( hazelcast == null )
			throw new RuntimeException( "Cannot find a Hazelcast instance named \"com.threecrickets.prudence\"" );
		return hazelcast;
	}

	/**
	 * A map of all values global to the Prudence Hazelcast cluster.
	 * <p>
	 * This is simply the "com.threecrickets.prudence.distributedGlobals"
	 * Hazelcast map.
	 * 
	 * @return The distributed globals or null
	 */
	public ConcurrentMap<String, Object> getDistributedGlobals()
	{
		return getHazelcast().getMap( "com.threecrickets.prudence.distributedGlobals" );
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

		if( globals == null )
			return null;

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

	//
	// Operations
	//

	/**
	 * Submits a task on the Hazelcast cluster.
	 * 
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
	 *        A {@link Member}, an iterable of {@link Member}, any other object
	 *        (the member key), or null to let Hazelcast decide
	 * @param multi
	 *        Whether the task should be executed on all members in the set
	 * @return A future or map of futures for the task
	 * @see HazelcastInstance#getExecutorService(String)
	 */
	public <T> Object distributedExecuteTask( String applicationName, String documentName, String entryPointName, Object context, Object where, boolean multi )
	{
		if( applicationName == null )
			applicationName = getApplication().getName();

		if( multi )
			return multiTask( new SerializableApplicationTask<T>( applicationName, documentName, entryPointName, context ), where );
		else
			return task( new SerializableApplicationTask<T>( applicationName, documentName, entryPointName, context ), where );
	}

	/**
	 * Submits a task on the Hazelcast cluster.
	 * 
	 * @param applicationName
	 *        The application's full name, or null to default to current
	 *        application's name
	 * @param code
	 *        The code to execute
	 * @param context
	 *        The context made available to the task (must be serializable)
	 * @param where
	 *        A {@link Member}, an iterable of {@link Member}, any other object
	 *        (the member key), or null to let Hazelcast decide
	 * @param multi
	 *        Whether the task should be executed on all members in the set
	 * @return A future or map of futures for the task
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

	/**
	 * Submits a task on the Hazelcast cluster.
	 * 
	 * @param task
	 *        The task
	 * @param where
	 *        A {@link Member}, an iterable of {@link Member}, any other object
	 *        (the member key), or null to let Hazelcast decide
	 * @return A future for the task
	 * @see HazelcastInstance#getExecutorService(String)
	 */
	private <T> Future<T> task( SerializableApplicationTask<T> task, Object where )
	{
		IExecutorService executor = getHazelcast().getExecutorService( "default" );

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
	 *        A {@link Member}, an iterable of {@link Member}, any other object
	 *        (the member key), or null to let Hazelcast decide
	 * @return Future for the task
	 * @see HazelcastInstance#getExecutorService(String)
	 */
	@SuppressWarnings("unchecked")
	private <T> Map<Member, Future<T>> multiTask( SerializableApplicationTask<T> task, Object where )
	{
		IExecutorService executor = getHazelcast().getExecutorService( "default" );

		if( where instanceof Collection )
			return executor.submitToMembers( task, (Collection<Member>) where );
		else if( where instanceof Iterable )
		{
			ArrayList<Member> members = new ArrayList<Member>();
			for( Member member : (Iterable<Member>) where )
				members.add( member );
			return executor.submitToMembers( task, members );
		}
		else
			return null;
	}
}
