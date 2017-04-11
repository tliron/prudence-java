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

package com.threecrickets.prudence.util;

import java.util.HashSet;
import java.util.Set;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.routing.Filter;
import org.restlet.util.Series;

/**
 * A {@link Filter} that adds <a href="http://www.w3.org/TR/cors/">Cross-Origin
 * Resource Sharing (CORS)</a> response headers.
 * <p>
 * This class should be unnecessary once
 * <a href="https://github.com/restlet/restlet-framework-java/issues/1109">
 * Restlet properly supports Access-Control-Max-Age</a>.
 * <p>
 * <i>"Restlet" is a registered trademark of
 * <a href="http://restlet.com/legal">Restlet S.A.S.</a>.</i>
 * 
 * @author Tal Liron
 */
public class CorsFilter extends Filter
{
	//
	// Constants
	//

	/**
	 * A far future max age (10 years)
	 */
	public static int FAR_FUTURE = 10 * 365 * 24 * 60 * 60;

	/**
	 * The maximum age header.
	 */
	public static final String MAX_AGE_HEADER = "Access-Control-Max-Age";

	//
	// Construction
	//

	/**
	 * Constructor.
	 */
	public CorsFilter()
	{
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 */
	public CorsFilter( Context context )
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
	public CorsFilter( Context context, Restlet next )
	{
		super( context, next );
		describe();
	}

	//
	// Attributes
	//

	/**
	 * The allowed origin.
	 * 
	 * @return The allowed origin
	 * @see #setAllowOrigin(String)
	 */
	public String getAllowOrigin()
	{
		return allowOrigin;
	}

	/**
	 * @param allowOrigin
	 *        The allowed origin
	 * @see #getAllowOrigin()
	 */
	public void setAllowOrigin( String allowOrigin )
	{
		this.allowOrigin = allowOrigin;
	}

	/**
	 * The allowed methods.
	 * 
	 * @return The allowed methods
	 */
	public Set<Method> getAllowMethods()
	{
		return allowMethods;
	}

	/**
	 * The allowed headers.
	 * 
	 * @return The allowed headers
	 */
	public Set<String> getAllowHeaders()
	{
		return allowHeaders;
	}

	/**
	 * The maximum age.
	 * 
	 * @return The maximum age
	 * @see #setMaxAge(int)
	 */
	public int getMaxAge()
	{
		return maxAge;
	}

	/**
	 * @param maxAge
	 *        The maximum age
	 * @see #getMaxAge()
	 */
	public void setMaxAge( int maxAge )
	{
		this.maxAge = maxAge;
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " -> " + getNext();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// Filter
	//

	@Override
	protected void afterHandle( Request request, Response response )
	{
		Series<Header> headers = RestletUtil.getResponseHeaders( response );
		if( allowOrigin != null )
			response.setAccessControlAllowOrigin( allowOrigin );
		if( !allowMethods.isEmpty() )
			response.setAccessControlAllowMethods( allowMethods );
		if( !allowHeaders.isEmpty() )
			response.setAccessControlAllowHeaders( allowHeaders );
		if( maxAge > 0 )
			headers.add( new Header( MAX_AGE_HEADER, Integer.toString( maxAge ) ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The allowed origin.
	 */
	private String allowOrigin;

	/**
	 * The allowed methods.
	 */
	private Set<Method> allowMethods = new HashSet<Method>();

	/**
	 * The allowed headers.
	 */
	private Set<String> allowHeaders = new HashSet<String>();

	/**
	 * The maximum age.
	 */
	private int maxAge;

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Three Crickets" );
		setName( getClass().getSimpleName() );
		setDescription( "A filter that adds Cross-Origin Resource Sharing (CORS) response headers" );
	}
}
