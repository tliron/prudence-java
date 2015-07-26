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
import org.restlet.data.Reference;
import org.restlet.routing.Filter;

/**
 * A {@link Filter} that applies the X-Forwarded-Proto HTTP header to the
 * request's references.
 * <p>
 * This <i>de facto</i> standard header is widely used by reverse proxies to
 * specify the scheme for which the request is forwarded. For example, a reverse
 * proxy might be accepting and terminating SSL (https) requests, but will
 * forward them to an unencrypted (http) server. This filter will make sure that
 * the request references ({@link Request#getResourceRef()},
 * {@link Request#getHostRef()}, {@link Request#getRootRef()},
 * {@link Request#getOriginalRef()}) will have the correct URI scheme for the
 * client's protocol.
 * 
 * @author Tal Liron
 * @see <a href=
 *      "http://docs.aws.amazon.com/ElasticLoadBalancing/latest/DeveloperGuide/x-forwarded-headers.html">
 *      Amazon AWS documentation</a>
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/List_of_HTTP_header_fields>Wikipedia:
 *      List of HTTP header fields</a>
 */
public class XForwardedProtoFilter extends Filter
{
	//
	// Constants
	//

	public static final String X_FORWARDED_PROTO_HEADER = "X-Forwarded-Proto";

	//
	// Construction
	//

	/**
	 * Constructor.
	 */
	public XForwardedProtoFilter()
	{
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 */
	public XForwardedProtoFilter( Context context )
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
	public XForwardedProtoFilter( Context context, Restlet next )
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
		String forwardedProto = request.getHeaders().getFirstValue( X_FORWARDED_PROTO_HEADER, true );
		if( forwardedProto != null )
		{
			Reference reference;

			reference = request.getResourceRef();
			if( reference != null )
			{
				reference.setScheme( forwardedProto );
				request.setResourceRef( reference );
			}

			reference = request.getHostRef();
			if( reference != null )
			{
				reference.setScheme( forwardedProto );
				request.setHostRef( reference );
			}

			reference = request.getRootRef();
			if( reference != null )
			{
				reference.setScheme( forwardedProto );
				request.setRootRef( reference );
			}

			reference = request.getOriginalRef();
			if( reference != null )
			{
				reference.setScheme( forwardedProto );
				request.setOriginalRef( reference );
			}
		}

		return CONTINUE;
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
		setDescription( "A filter that applies the X-Forwarded-Proto HTTP header to the request's references" );
	}
}
