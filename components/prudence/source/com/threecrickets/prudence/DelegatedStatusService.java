/**
 * Copyright 2009-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.routing.Redirector;
import org.restlet.service.StatusService;

import com.threecrickets.prudence.internal.CachingUtil;
import com.threecrickets.prudence.util.CapturingRedirector;
import com.threecrickets.prudence.util.DebugRepresentation;
import com.threecrickets.prudence.util.RestletUtil;

/**
 * Allows delegating the handling of errors to specified restlets.
 * 
 * @author Tal Liron
 * @see DebugRepresentation
 */
public class DelegatedStatusService extends StatusService
{
	//
	// Constants
	//

	/**
	 * Response attribute to signify to upstream instances that the status has
	 * already been handled for a {@link Response}.
	 */
	public static final String PASSTHROUGH_ATTRIBUTE = DelegatedStatusService.class.getCanonicalName() + ".passThrough";

	/**
	 * The default debug header.
	 */
	public static final String DEFAULT_DEBUG_HEADER = "X-Debug";

	//
	// Construction
	//

	/**
	 * Constructor using default debug header.
	 * 
	 * @param sourceCodeUri
	 *        The source code viewer URI or null
	 */
	public DelegatedStatusService( String sourceCodeUri )
	{
		this( sourceCodeUri, DEFAULT_DEBUG_HEADER );
	}

	/**
	 * Constructor.
	 * 
	 * @param sourceCodeUri
	 *        The source code viewer URI or null
	 * @param debugHeader
	 *        The debug header or null
	 */
	public DelegatedStatusService( String sourceCodeUri, String debugHeader )
	{
		super();
		setOverwriting( true );
		this.sourceCodeUri = sourceCodeUri;
		this.debugHeader = debugHeader;
	}

	/**
	 * Constructor without support for source code viewing.
	 */
	public DelegatedStatusService()
	{
		this( null );
	}

	//
	// Attributes
	//

	/**
	 * A map of status codes to target restlets. If no handler is mapped for a
	 * status, the default handling will kick in. (Modifiable by concurrent
	 * threads.)
	 * 
	 * @return The error handlers
	 */
	public ConcurrentMap<Integer, Restlet> getHandlers()
	{
		return handlers;
	}

	/**
	 * Whether we should show debugging information on errors.
	 * 
	 * @return True if debugging
	 * @see #setDebugging(boolean)
	 */
	public boolean isDebugging()
	{
		return isDebugging;
	}

	/**
	 * Whether we should show debugging information on errors.
	 * 
	 * @param isDebugging
	 *        True if debugging
	 * @see #isDebugging()
	 */
	public void setDebugging( boolean isDebugging )
	{
		this.isDebugging = isDebugging;
	}

	/**
	 * Whether we are a fallback status service.
	 * 
	 * @return True if we are a fallback status service
	 * @see #setFallback(boolean)
	 */
	public boolean isFallback()
	{
		return isFallback;
	}

	/**
	 * Whether we are a fallback status service.
	 * 
	 * @param isFallback
	 *        True if we are a fallback status service
	 * @see #isFallback()
	 */
	public void setFallback( boolean isFallback )
	{
		this.isFallback = isFallback;
	}

	//
	// Operations
	//

	/**
	 * Sets the handler for a status code.
	 * 
	 * @param statusCode
	 *        The status code
	 * @param errorHandler
	 *        The error handler
	 */
	public void setHandler( int statusCode, Restlet errorHandler )
	{
		handlers.put( statusCode, errorHandler );
	}

	/**
	 * Captures (internally redirects) a status code to a URI within an
	 * application. You can use template variables in the URI.
	 * <p>
	 * This is handled via a {@link CapturingRedirector} with mode
	 * {@link Redirector#MODE_SERVER_OUTBOUND}.
	 * 
	 * @param statusCode
	 *        The status code
	 * @param application
	 *        The internal application name, or null to use current application
	 * @param internalUriTemplate
	 *        The internal URI template to which we will redirect
	 */
	public void capture( int statusCode, String application, String internalUriTemplate )
	{
		Context context = getContext();
		if( context == null )
			throw new RuntimeException( "No context set for DelegatedStatusService" );

		if( !internalUriTemplate.startsWith( "/" ) )
			internalUriTemplate = "/" + internalUriTemplate;
		String targetUriTemplate;
		if( application == null )
			targetUriTemplate = "riap://application" + internalUriTemplate;
		else
			targetUriTemplate = "riap://component/" + application + internalUriTemplate;
		setHandler( statusCode, new CapturingRedirector( context, targetUriTemplate ) );
	}

	/**
	 * Removes the handler for a status code.
	 * 
	 * @param status
	 *        The status code
	 */
	public void removeHandler( int status )
	{
		handlers.remove( status );
	}

	//
	// StatusService
	//

	@Override
	public Representation toRepresentation( Status status, Request request, Response response )
	{
		if( isEnabled() )
		{
			ConcurrentMap<String, Object> attributes = response.getAttributes();

			Object passthrough = attributes.get( PASSTHROUGH_ATTRIBUTE );
			if( ( passthrough != null ) && (Boolean) passthrough )
				// Pass through
				return response.getEntity();

			Restlet handler = handlers.get( status.getCode() );
			if( handler != null )
			{
				// Reset the response
				response.setStatus( Status.SUCCESS_OK );
				response.setEntity( null );

				// Clean up saved information
				CachingUtil.clearExistingValidDocumentName( request );

				// Delegate
				handler.handle( request, response );

				// Return the status
				response.setStatus( status );

				Representation representation = response.getEntity();
				if( representation != null )
				{
					// Avoid client caching, which would require other
					// interchanges with the client that we can't handle from
					// here
					representation.setExpirationDate( null );
					representation.setModificationDate( null );
					representation.setTag( null );
				}

				attributes.put( PASSTHROUGH_ATTRIBUTE, true );
				return representation;
			}

			if( isFallback )
				// Fallbacks don't override the entity if there are no handlers
				return response.getEntity();

			if( isDebugging() && ( status.getThrowable() != null ) )
			{
				// Use the debug representation for exceptions
				attributes.put( PASSTHROUGH_ATTRIBUTE, true );
				if( debugHeader != null )
					RestletUtil.getResponseHeaders( response ).set( debugHeader, "error" );
				return createDebugRepresentation( status, request, response );
			}
		}

		return super.toRepresentation( status, request, response );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * Create a debug representation for a conversation.
	 * <p>
	 * Override this to install a custom implementation.
	 * 
	 * @param status
	 *        The status
	 * @param request
	 *        The request
	 * @param response
	 *        The response
	 * @return A debug representation
	 */
	protected Representation createDebugRepresentation( Status status, Request request, Response response )
	{
		return new DebugRepresentation( status, request, response, sourceCodeUri );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Our map of status codes to handlers.
	 */
	private final ConcurrentMap<Integer, Restlet> handlers = new ConcurrentHashMap<Integer, Restlet>();

	/**
	 * The source code URI or null.
	 */
	private final String sourceCodeUri;

	/**
	 * The debug header.
	 */
	private final String debugHeader;

	/**
	 * Whether we are debugging.
	 */
	private volatile boolean isDebugging;

	/**
	 * Whether we are a fallback status service.
	 */
	private volatile boolean isFallback;
}
