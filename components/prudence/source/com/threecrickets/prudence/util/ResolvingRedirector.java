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

import java.util.HashSet;
import java.util.Set;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Redirector;
import org.restlet.routing.Template;

/**
 * A {@link Redirector} that uses {@link ResolvingTemplate}. It also includes a
 * mechanism to help protect against recursive redirects.
 * 
 * @author Tal Liron
 */
public class ResolvingRedirector extends Redirector
{
	//
	// Constants
	//

	/**
	 * Server redirect history attribute for a {@link Request}.
	 */
	public static final String SERVER_REDIRECT_HISTORY_ATTRIBUTE = ResolvingRedirector.class.getCanonicalName() + ".serverRedirectHistory";

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
	 */
	public ResolvingRedirector( Context context, String targetTemplate, int mode )
	{
		super( context, targetTemplate, mode );
		describe();
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 * @param targetTemplate
	 *        The target URI template
	 */
	public ResolvingRedirector( Context context, String targetTemplate )
	{
		super( context, targetTemplate );
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
		if( new Reference( this.targetTemplate ).isRelative() )
			// Be sure to keep the resource's base reference.
			return new Reference( request.getResourceRef(), rt.format( request, response ) );

		return new Reference( rt.format( request, response ) );
	}

	@Override
	protected void serverRedirect( Restlet next, Reference targetRef, Request request, Response response )
	{
		validateNotRecursiveServerRedirect( targetRef, request, response );

		// This is essentially the original Restlet code modified to use
		// ResolvingTemplate.

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
			rewrite( request );
			next.handle( request, response );

			// Allow for response rewriting and clean the headers
			response.setEntity( rewrite( response.getEntity() ) );
			rewrite( response );
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
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Three Crickets" );
		setName( getClass().getSimpleName() );
		setDescription( "A redirector that uses ResolvingTemplate" );
	}

	/**
	 * Throw an exception if there is a recursive server-side redirect.
	 * 
	 * @param targetRef
	 *        The target reference
	 * @param request
	 *        The request
	 * @param response
	 *        The response
	 */
	private void validateNotRecursiveServerRedirect( Reference targetRef, Request request, Response response )
	{
		@SuppressWarnings("unchecked")
		Set<String> serverRedirectHistory = (Set<String>) request.getAttributes().get( SERVER_REDIRECT_HISTORY_ATTRIBUTE );
		if( serverRedirectHistory == null )
		{
			serverRedirectHistory = new HashSet<String>();
			@SuppressWarnings("unchecked")
			Set<String> existing = (Set<String>) request.getAttributes().putIfAbsent( SERVER_REDIRECT_HISTORY_ATTRIBUTE, serverRedirectHistory );
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
	}
}
