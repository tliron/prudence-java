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

package com.threecrickets.prudence.service;

import it.sauronsoftware.cron4j.Scheduler;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.MediaType;

import com.threecrickets.prudence.ApplicationTask;
import com.threecrickets.prudence.ApplicationTaskCollector;
import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.util.InstanceUtil;
import com.threecrickets.prudence.util.LoggingUtil;

/**
 * Application service exposed to executables.
 * 
 * @author Tal Liron
 * @see DelegatedResource
 * @see GeneratedTextResource
 * @see ApplicationTask
 */
public class ApplicationService
{
	//
	// Static operations
	//

	/**
	 * Constructor using the current application.
	 * 
	 * @return A new application service
	 * @see Application#getCurrent()
	 */
	public static ApplicationService create()
	{
		return create( Application.getCurrent() );
	}

	/**
	 * Constructor.
	 * 
	 * @param application
	 *        The application
	 * @return A new application service
	 */
	public static ApplicationService create( Application application )
	{
		try
		{
			ApplicationService.class.getClassLoader().loadClass( "com.hazelcast.core.HazelcastInstance" );
			return new DistributedApplicationService( application );
		}
		catch( ClassNotFoundException x )
		{
		}
		catch( NoClassDefFoundError x )
		{
		}

		return new ApplicationService( application );
	}

	//
	// Attributes
	//

	/**
	 * The underlying application.
	 * 
	 * @return The application
	 */
	public Application getApplication()
	{
		return application;
	}

	/**
	 * The underlying component.
	 * <p>
	 * Note: for this to work, the component must have been explicitly set as
	 * attribute <code>com.threecrickets.prudence.component</code> in the
	 * application's context.
	 * 
	 * @return The component
	 */
	public Component getComponent()
	{
		if( component == null )
			component = (Component) getGlobals().get( InstanceUtil.COMPONENT_ATTRIBUTE );
		return component;
	}

	/**
	 * A map of all values global to the current application.
	 * 
	 * @return The globals
	 */
	public ConcurrentMap<String, Object> getGlobals()
	{
		if( globals == null )
			globals = application.getContext().getAttributes();
		return globals;
	}

	/**
	 * Gets a value global to the current application, atomically setting it to
	 * a default value if it doesn't exist.
	 * 
	 * @param name
	 *        The name of the global
	 * @param defaultValue
	 *        The default value
	 * @return The global's current value
	 */
	public Object getGlobal( String name, Object defaultValue )
	{
		ConcurrentMap<String, Object> globals = getGlobals();
		Object value = globals.get( name );

		if( defaultValue != null )
		{
			value = defaultValue;
			Object existing = globals.putIfAbsent( name, value );
			if( existing != null )
				value = existing;
		}
		else
			globals.remove( name );

		return value;
	}

	/**
	 * Get the locks for the current application.
	 * 
	 * @return The locks
	 */
	@SuppressWarnings("unchecked")
	public ConcurrentMap<String, ReentrantLock> getLocks()
	{
		if( locks == null )
		{
			ConcurrentMap<String, Object> globals = getGlobals();
			locks = (ConcurrentMap<String, ReentrantLock>) globals.get( LOCKS_ATTRIBUTE );
			if( locks == null )
			{
				locks = new ConcurrentHashMap<String, ReentrantLock>();
				ConcurrentMap<String, ReentrantLock> existing = (ConcurrentMap<String, ReentrantLock>) globals.putIfAbsent( LOCKS_ATTRIBUTE, locks );
				if( existing != null )
					locks = existing;
			}
		}

		return locks;
	}

	/**
	 * Gets a lock for the current application.
	 * 
	 * @param name
	 *        The lock name
	 * @return The lock
	 */
	public ReentrantLock getLock( String name )
	{
		ConcurrentMap<String, ReentrantLock> locks = getLocks();
		ReentrantLock lock = locks.get( name );
		if( lock == null )
		{
			lock = new ReentrantLock();
			ReentrantLock existing = locks.putIfAbsent( name, lock );
			if( existing != null )
				lock = existing;
		}

		return lock;
	}

	/**
	 * A map of all values global to all running applications.
	 * <p>
	 * Note that this could be null if shared globals are not set up.
	 * 
	 * @return The shared globals or null
	 * @see #getComponent()
	 */
	public ConcurrentMap<String, Object> getSharedGlobals()
	{
		if( sharedGlobals == null )
		{
			Component component = getComponent();
			if( component != null )
				sharedGlobals = component.getContext().getAttributes();
		}

		return sharedGlobals;
	}

	/**
	 * Gets a value global to all running applications, atomically setting it to
	 * a default value if it doesn't exist.
	 * <p>
	 * If shared globals are not set up, does nothing and returns null.
	 * 
	 * @param name
	 *        The name of the shared global
	 * @param defaultValue
	 *        The default value
	 * @return The shared global's current value
	 * @see #getComponent()
	 */
	public Object getSharedGlobal( String name, Object defaultValue )
	{
		ConcurrentMap<String, Object> sharedGlobals = getSharedGlobals();

		if( sharedGlobals == null )
			return null;

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
	 * Get the locks shared by all running applications.
	 * 
	 * @return The shared locks
	 */
	@SuppressWarnings("unchecked")
	public ConcurrentMap<String, ReentrantLock> getSharedLocks()
	{
		if( sharedLocks == null )
		{
			ConcurrentMap<String, Object> sharedGlobals = getSharedGlobals();
			if( sharedGlobals == null )
				return null;
			sharedLocks = (ConcurrentMap<String, ReentrantLock>) sharedGlobals.get( LOCKS_ATTRIBUTE );
			if( sharedLocks == null )
			{
				sharedLocks = new ConcurrentHashMap<String, ReentrantLock>();
				ConcurrentMap<String, ReentrantLock> existing = (ConcurrentMap<String, ReentrantLock>) sharedGlobals.putIfAbsent( LOCKS_ATTRIBUTE, sharedLocks );
				if( existing != null )
					sharedLocks = existing;
			}
		}

		return sharedLocks;
	}

	/**
	 * Gets a lock shared by all running applications.
	 * 
	 * @param name
	 *        The shared lock name
	 * @return The shared lock
	 */
	public ReentrantLock getSharedLock( String name )
	{
		ConcurrentMap<String, ReentrantLock> sharedLocks = getSharedLocks();
		if( sharedLocks == null )
			return null;
		ReentrantLock lock = sharedLocks.get( name );
		if( lock == null )
		{
			lock = new ReentrantLock();
			ReentrantLock existing = sharedLocks.putIfAbsent( name, lock );
			if( existing != null )
				lock = existing;
		}

		return lock;
	}

	/**
	 * The application's logger.
	 * 
	 * @return The logger
	 * @see #getSubLogger(String)
	 */
	public Logger getLogger()
	{
		if( logger == null )
			logger = LoggingUtil.getLogger( application );
		return logger;
	}

	/**
	 * A logger with a name appended with a "." to the application's logger
	 * name. This allows inheritance of configuration.
	 * 
	 * @param name
	 *        The sub-logger name
	 * @return The logger
	 * @see #getLogger()
	 */
	public Logger getSubLogger( String name )
	{
		return LoggingUtil.getSubLogger( getLogger(), name );
	}

	/**
	 * The application root directory.
	 * 
	 * @return The root directory
	 */
	public File getRoot()
	{
		if( root == null )
			root = (File) getGlobals().get( InstanceUtil.ROOT_ATTRIBUTE );
		return root;
	}

	/**
	 * The container root directory.
	 * 
	 * @return The container root directory
	 */
	public File getContainerRoot()
	{
		if( containerRoot == null )
			containerRoot = new File( System.getProperty( "sincerity.container.root" ) );
		return containerRoot;
	}

	/**
	 * Get a media type by its MIME type name.
	 * 
	 * @param name
	 *        The MIME type name
	 * @return The media type
	 */
	public MediaType getMediaTypeByName( String name )
	{
		return MediaType.valueOf( name );
	}

	/**
	 * Get a media type by its extension.
	 * 
	 * @param extension
	 *        The extension
	 * @return The media type
	 */
	public MediaType getMediaTypeByExtension( String extension )
	{
		return application.getMetadataService().getMediaType( extension );
	}

	/**
	 * Gets the shared executor service, creating one if it doesn't exist.
	 * <p>
	 * If shared globals are not set up, gets the application's executor
	 * service.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.executor</code> in the component's
	 * {@link Context}.
	 * 
	 * @return The executor service
	 */
	public ExecutorService getExecutor()
	{
		if( executor == null )
		{
			ConcurrentMap<String, Object> attributes = getSharedGlobals();

			if( attributes != null )
			{
				executor = (ExecutorService) attributes.get( InstanceUtil.EXECUTOR_ATTRIBUTE );

				if( executor == null )
				{
					executor = Executors.newScheduledThreadPool( Runtime.getRuntime().availableProcessors() * 2 + 1 );

					ExecutorService existing = (ExecutorService) attributes.putIfAbsent( InstanceUtil.EXECUTOR_ATTRIBUTE, executor );
					if( existing != null )
						executor = existing;
				}
			}
			else
				executor = InstanceUtil.getComponent().getTaskService();
		}

		return executor;
	}

	/**
	 * Gets the task collector, if there is one.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.taskCollector</code> in the
	 * application's {@link Context}.
	 * 
	 * @return The task collector
	 */
	public ApplicationTaskCollector getTaskCollector()
	{
		if( taskCollector == null )
		{
			ConcurrentMap<String, Object> attributes = getGlobals();
			if( attributes != null )
				taskCollector = (ApplicationTaskCollector) attributes.get( TASK_COLLECTOR_ATTRIBUTE );
		}
		return taskCollector;
	}

	/**
	 * Gets the shared scheduler.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.scheduler</code> in the component's
	 * {@link Context}.
	 * 
	 * @return The executor service
	 */
	public Scheduler getScheduler()
	{
		if( scheduler == null )
		{
			ConcurrentMap<String, Object> attributes = getSharedGlobals();

			if( attributes != null )
				scheduler = (Scheduler) attributes.get( InstanceUtil.SCHEDULER_ATTRIBUTE );
		}

		return scheduler;
	}

	//
	// Operations
	//

	/**
	 * Submits or schedules an {@link ApplicationTask} on the the shared
	 * executor service.
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
	 *        The context made available to the task
	 * @param delay
	 *        Initial delay in milliseconds, or zero for ASAP
	 * @param repeatEvery
	 *        Repeat delay in milliseconds, or zero for no repetition
	 * @param fixedRepeat
	 *        Whether repetitions are at fixed times, or if the repeat delay
	 *        begins when the task ends
	 * @return A future for the task
	 * @see #getExecutor()
	 */
	public <T> Future<T> executeTask( String applicationName, String documentName, String entryPointName, Object context, int delay, int repeatEvery, boolean fixedRepeat )
	{
		Application application = applicationName == null ? this.application : InstanceUtil.getApplication( applicationName );
		ApplicationTask<T> task = new ApplicationTask<T>( application, documentName, entryPointName, context );
		return task( task, delay, repeatEvery, fixedRepeat );
	}

	/**
	 * Submits or schedules an {@link ApplicationTask} on the the shared
	 * executor service.
	 * 
	 * @param <T>
	 *        The task result class
	 * @param applicationName
	 *        The application's full name, or null to default to current
	 *        application's name
	 * @param code
	 *        The code to execute
	 * @param context
	 *        The context made available to the task
	 * @param delay
	 *        Initial delay in milliseconds, or zero for ASAP
	 * @param repeatEvery
	 *        Repeat delay in milliseconds, or zero for no repetition
	 * @param fixedRepeat
	 *        Whether repetitions are at fixed times, or if the repeat delay
	 *        begins when the task ends
	 * @return A future for the task
	 * @see #getExecutor()
	 */
	public <T> Future<T> codeTask( String applicationName, String code, Object context, int delay, int repeatEvery, boolean fixedRepeat )
	{
		Application application = applicationName == null ? this.application : InstanceUtil.getApplication( applicationName );
		ApplicationTask<T> task = new ApplicationTask<T>( application, code, context );
		return task( task, delay, repeatEvery, fixedRepeat );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Constructor.
	 * 
	 * @param application
	 *        The application
	 */
	protected ApplicationService( Application application )
	{
		this.application = application;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The task collector attribute in the application's context.
	 */
	private static final String TASK_COLLECTOR_ATTRIBUTE = "com.threecrickets.prudence.taskCollector";

	/**
	 * The locks attribute in the application or the component's context.
	 */
	private static final String LOCKS_ATTRIBUTE = "com.threecrickets.prudence.locks";

	/**
	 * The application.
	 */
	private final Application application;

	/**
	 * The component.
	 */
	private Component component;

	/**
	 * The globals.
	 */
	private ConcurrentMap<String, Object> globals;

	/**
	 * The locks.
	 */
	private ConcurrentMap<String, ReentrantLock> locks;

	/**
	 * The shared globals.
	 */
	private ConcurrentMap<String, Object> sharedGlobals;

	/**
	 * The shared locks.
	 */
	private ConcurrentMap<String, ReentrantLock> sharedLocks;

	/**
	 * The executor service.
	 */
	private ExecutorService executor;

	/**
	 * The task collector.
	 */
	private ApplicationTaskCollector taskCollector;

	/**
	 * The scheduler.
	 */
	private Scheduler scheduler;

	/**
	 * The logger.
	 */
	private Logger logger;

	/**
	 * The application root directory.
	 */
	private File root;

	/**
	 * The container root directory.
	 */
	private File containerRoot;

	/**
	 * Submits or schedules an {@link ApplicationTask} on the the shared
	 * executor service.
	 * 
	 * @param task
	 *        The task
	 * @param delay
	 *        Initial delay in milliseconds, or zero for ASAP
	 * @param repeatEvery
	 *        Repeat delay in milliseconds, or zero for no repetition
	 * @param fixedRepeat
	 *        Whether repetitions are at fixed times, or if the repeat delay
	 *        begins when the task ends
	 * @return A future for the task
	 */
	private <T> Future<T> task( ApplicationTask<T> task, int delay, int repeatEvery, boolean fixedRepeat )
	{
		ExecutorService executor = getExecutor();
		if( ( delay > 0 ) || ( repeatEvery > 0 ) )
		{
			if( !( executor instanceof ScheduledExecutorService ) )
				throw new RuntimeException( "Executor must implement the ScheduledExecutorService interface to allow for delayed tasks" );

			ScheduledExecutorService scheduledExecutor = (ScheduledExecutorService) executor;
			if( repeatEvery > 0 )
			{
				if( fixedRepeat )
				{
					@SuppressWarnings("unchecked")
					ScheduledFuture<T> future = (ScheduledFuture<T>) scheduledExecutor.scheduleAtFixedRate( task, delay, repeatEvery, TimeUnit.MILLISECONDS );
					return future;
				}
				else
				{
					@SuppressWarnings("unchecked")
					ScheduledFuture<T> future = (ScheduledFuture<T>) scheduledExecutor.scheduleWithFixedDelay( task, delay, repeatEvery, TimeUnit.MILLISECONDS );
					return future;
				}
			}
			else
				return scheduledExecutor.schedule( (Callable<T>) task, delay, TimeUnit.MILLISECONDS );
		}
		else
			return executor.submit( (Callable<T>) task );
	}
}
