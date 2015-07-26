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

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.engine.adapter.HttpServerHelper;
import org.restlet.routing.Filter;

/**
 * A {@link Filter} that applies the X-Forwarded-Proto, X-Forwarded-Host, and
 * X-Forwarded-Port HTTP headers to the request's references.
 * <p>
 * These <i>de facto</i> standard headers are widely used by reverse proxies to
 * specify the scheme and port for which the request is forwarded. For example,
 * a reverse proxy might be accepting and terminating SSL (https) requests, but
 * will forward them to an unencrypted (http) server. This filter will make sure
 * that the request references ({@link Request#getResourceRef()},
 * {@link Request#getRootRef()}, {@link Request#getOriginalRef()}) will have the
 * correct URI scheme and port for the client's protocol.
 * <p>
 * Note that {@link Request#getHostRef()} is left intact, as it is independently
 * determined by the Host HTTP header.
 * <p>
 * Support for X-Forwarded-For is handled elsewhere in Restlet. See
 * {@link HttpServerHelper} and {@link ClientInfo}.
 * 
 * @author Tal Liron
 * @see <a href="https://en.wikipedia.org/wiki/List_of_HTTP_header_fields">
 *      Wikipedia: List of HTTP header fields</a>
 */
public class ForwardedFilter extends Filter
{
	//
	// Constants
	//

	/**
	 * The X-Forwarded-Proto HTTP header.
	 */
	public static final String X_FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";

	/**
	 * The X-Forwarded-Host HTTP header.
	 */
	public static final String X_FORWARDED_HOST_HEADER = "X-Forwarded-Host";

	/**
	 * The X-Forwarded-Port HTTP header.
	 */
	public static final String X_FORWARDED_PORT_HEADER = "X-Forwarded-Port";

	//
	// Construction
	//

	/**
	 * Constructor.
	 */
	public ForwardedFilter()
	{
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 */
	public ForwardedFilter( Context context )
	{
		super( context );
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 * @param next
	 *        The next restlet
	 */
	public ForwardedFilter( Context context, Restlet next )
	{
		super( context, next );
		describe();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// Filter
	//

	@Override
	protected int beforeHandle( Request request, Response response )
	{
		String forwardedScheme = request.getHeaders().getFirstValue( X_FORWARDED_PROTO_HEADER );
		String forwardedDomain = request.getHeaders().getFirstValue( X_FORWARDED_HOST_HEADER );
		String forwardedPortString = request.getHeaders().getFirstValue( X_FORWARDED_PORT_HEADER );
		if( ( forwardedScheme != null ) || ( forwardedDomain != null ) || ( forwardedPortString != null ) )
		{
			int forwardedPort = -1;

			if( forwardedDomain != null )
			{
				int colon = forwardedDomain.indexOf( ':' );
				if( colon != -1 )
				{
					// Parse host:port
					try
					{
						forwardedPort = Integer.parseInt( forwardedDomain.substring( 0, colon ) );
					}
					catch( NumberFormatException x )
					{
					}
					forwardedDomain = forwardedDomain.substring( 0, colon );
				}
			}

			if( forwardedPortString != null )
				// Note: will override port in X-Forwarded-Host
				forwardedPort = Integer.parseInt( forwardedPortString );

			Reference reference;

			reference = request.getResourceRef();
			if( reference != null )
			{
				apply( reference, forwardedScheme, forwardedDomain, forwardedPort );
				request.setResourceRef( reference );
			}

			reference = request.getRootRef();
			if( reference != null )
			{
				apply( reference, forwardedScheme, forwardedDomain, forwardedPort );
				request.setRootRef( reference );
			}

			reference = request.getOriginalRef();
			if( reference != null )
			{
				apply( reference, forwardedScheme, forwardedDomain, forwardedPort );
				request.setOriginalRef( reference );
			}
		}

		return CONTINUE;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static void apply( Reference reference, String forwardedScheme, String forwardedDomain, int forwardedPort )
	{
		if( forwardedScheme != null )
			reference.setScheme( forwardedScheme );
		if( forwardedDomain != null )
			reference.setHostDomain( forwardedDomain );
		if( forwardedPort != -1 )
			if( Protocol.valueOf( reference.getScheme() ).getDefaultPort() != forwardedPort )
				reference.setHostPort( forwardedPort );
	}

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Three Crickets" );
		setName( getClass().getSimpleName() );
		setDescription( "A filter that applies the X-Forwarded-Proto, X-Forwarded-Host, and X-Forwarded-Port HTTP headers to the request's references" );
	}
}
