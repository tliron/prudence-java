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

package com.threecrickets.prudence.util;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.restlet.routing.VirtualHost;

/**
 * A {@link Filter} that sets the virtual host as a request attribute.
 * <p>
 * See
 * <a href="https://github.com/restlet/restlet-framework-java/issues/1045">this
 * issue</a>.
 * 
 * @author Tal Liron
 */
public class VirtualHostInjector extends Filter
{
	//
	// Constants
	//

	/**
	 * The request attribute in which to store the virtual host.
	 */
	public final static String ATTRIBUTE = VirtualHost.class.getCanonicalName();

	//
	// Static operations
	//

	/**
	 * The virtual host for the request.
	 * 
	 * @param request
	 *        The request
	 * @return The virtual host
	 */
	public static VirtualHost getVirtualHost( Request request )
	{
		return (VirtualHost) request.getAttributes().get( ATTRIBUTE );
	}

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 * @param virtualHost
	 *        The virtual host
	 */
	public VirtualHostInjector( Context context, VirtualHost virtualHost )
	{
		super( context );
		this.virtualHost = virtualHost;
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 * @param next
	 *        The next restlet
	 * @param virtualHost
	 *        The virtual host
	 */
	public VirtualHostInjector( Context context, Restlet next, VirtualHost virtualHost )
	{
		super( context, next );
		this.virtualHost = virtualHost;
	}

	//
	// Filter
	//

	@Override
	protected int beforeHandle( Request request, Response response )
	{
		request.getAttributes().put( ATTRIBUTE, virtualHost );
		return Filter.CONTINUE;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final VirtualHost virtualHost;
}
