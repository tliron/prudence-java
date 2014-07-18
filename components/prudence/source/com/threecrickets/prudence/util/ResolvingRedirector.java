/**
 * Copyright 2009-2014 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.util;

import java.util.HashSet;
import java.util.Set;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Redirector;
import org.restlet.routing.Template;

/**
 * A {@link Redirector} that uses {@link ResolvingTemplate}, and also makes
 * header cleaning optional.
 * <p>
 * See <a
 * href="https://github.com/restlet/restlet-framework-java/issues/798">this
 * issue</a>.
 * 
 * @author Tal Liron
 */
public class ResolvingRedirector extends Redirector
{
	//
	// Construction
	//

	/**
	 * Constructor. The context
	 * 
	 * @param context
	 *        The context
	 * @param targetTemplate
	 *        The target URI template
	 * @param mode
	 *        The redirection mode
	 * @param isCleaning
	 *        Whether we are cleaning headers for server-side redirection
	 */
	public ResolvingRedirector( Context context, String targetTemplate, int mode, boolean isCleaning )
	{
		super( context, targetTemplate, mode );
		this.isCleaning = isCleaning;
		describe();
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 * @param targetTemplate
	 *        The target URI template
	 * @param isCleaning
	 *        Whether we are cleaning headers for server-side redirection
	 */
	public ResolvingRedirector( Context context, String targetTemplate, boolean isCleaning )
	{
		super( context, targetTemplate );
		this.isCleaning = isCleaning;
		describe();
	}

	//
	// Attributes
	//

	/**
	 * The redirection mode as string.
	 * 
	 * @return The mode
	 * @see #getMode()
	 */
	public String getModeAsString()
	{
		switch( getMode() )
		{
			case MODE_CLIENT_FOUND:
				return "MODE_CLIENT_FOUND";
			case MODE_CLIENT_PERMANENT:
				return "MODE_CLIENT_PERMANENT";
			case MODE_CLIENT_SEE_OTHER:
				return "MODE_CLIENT_SEE_OTHER";
			case MODE_CLIENT_TEMPORARY:
				return "MODE_CLIENT_TEMPORARY";
			case MODE_SERVER_INBOUND:
				return "MODE_SERVER_INBOUND";
			case MODE_SERVER_OUTBOUND:
				return "MODE_SERVER_OUTBOUND";
			default:
				return "" + getMode();
		}
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + ": \"" + getTargetTemplate() + "\", " + getModeAsString();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// Redirector
	//

	@Override
	protected Reference getTargetRef( Request request, Response response )
	{
		// This is essentially the original Restlet code modified to use
		// ResolvingTemplate.

		// Create the template
		Template rt = new ResolvingTemplate( this.targetTemplate );
		rt.setLogger( getLogger() );

		// Return the formatted target URI
		return new Reference( request.getResourceRef(), rt.format( request, response ) );
	}

	@Override
	protected void serverRedirect( Restlet next, Reference targetRef, Request request, Response response )
	{
		// This is essentially the original Restlet code modified to use
		// ResolvingTemplate and allow for cleaning to be optional.

		// It also includes an important check for recursive server-side
		// redirects.

		@SuppressWarnings("unchecked")
		Set<String> serverRedirectHistory = (Set<String>) request.getAttributes().get( "ttttt" );
		if( serverRedirectHistory == null )
		{
			serverRedirectHistory = new HashSet<String>();
			@SuppressWarnings("unchecked")
			Set<String> existing = (Set<String>) request.getAttributes().putIfAbsent( "ttttt", serverRedirectHistory );
			if( existing != null )
				serverRedirectHistory = existing;
		}

		String targetRefString = targetRef.toString();
		if( serverRedirectHistory.contains( targetRefString ) )
		{
			String message = "Recursive server redirection to " + targetRef;
			getLogger().warning( message );
			throw new ResourceException( Status.SERVER_ERROR_INTERNAL, message );
		}

		serverRedirectHistory.add( targetRefString );

		if( next == null )
			getLogger().warning( "No next Restlet provided for server redirection to " + targetRef );
		else
		{
			// Save the base URI if it exists as we might need it for
			// redirections
			Reference resourceRef = request.getResourceRef();
			Reference baseRef = resourceRef.getBaseRef();

			// Reset the protocol and let the dispatcher handle the protocol
			request.setProtocol( null );

			// Update the request to cleanly go to the target URI
			request.setResourceRef( targetRef );
			if( isCleaning )
				request.getAttributes().remove( HeaderConstants.ATTRIBUTE_HEADERS );
			next.handle( request, response );

			// Allow for response rewriting and clean the headers
			response.setEntity( rewrite( response.getEntity() ) );
			if( isCleaning )
				response.getAttributes().remove( HeaderConstants.ATTRIBUTE_HEADERS );
			request.setResourceRef( resourceRef );

			// In case of redirection, we may have to rewrite the redirect URI
			if( response.getLocationRef() != null )
			{
				Template rt = new ResolvingTemplate( this.targetTemplate );
				rt.setLogger( getLogger() );
				int matched = rt.parse( response.getLocationRef().toString(), request );

				if( matched > 0 )
				{
					String remainingPart = (String) request.getAttributes().get( "rr" );

					if( remainingPart != null )
						response.setLocationRef( baseRef.toString() + remainingPart );
				}
			}
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * True if we are cleaning the headers in the request and response.
	 */
	private final boolean isCleaning;

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Three Crickets" );
		setName( getClass().getSimpleName() );
		setDescription( "A redirector that uses ResolvingTemplate" );
	}
}
