/**
 * Copyright 2009-2010 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.util;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Redirector;
import org.restlet.routing.Route;
import org.restlet.routing.Template;

/**
 * @author Tal Liron
 */
@SuppressWarnings("deprecation")
public class PrudenceRouter extends FallbackRouter
{
	//
	// Construction
	//

	/**
	 * @param context
	 */
	public PrudenceRouter( Context context )
	{
		super( context );
	}

	/**
	 * @param context
	 * @param cacheDuration
	 */
	public PrudenceRouter( Context context, int cacheDuration )
	{
		super( context, cacheDuration );
	}

	//
	// Operations
	//

	/**
	 * @param pathTemplate
	 * @param targetClassName
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Route attach( String pathTemplate, String targetClassName ) throws ClassNotFoundException
	{
		return attach( pathTemplate, getClass().getClassLoader().loadClass( targetClassName ) );
	}

	/**
	 * @param pathTemplate
	 * @param targetClassName
	 * @return
	 * @throws ClassNotFoundException
	 */
	public Route attachBase( String pathTemplate, String targetClassName ) throws ClassNotFoundException
	{
		Route route = attach( pathTemplate, targetClassName );
		route.setMatchingMode( Template.MODE_STARTS_WITH );
		return route;
	}

	/**
	 * @param pathTemplate
	 * @param target
	 * @return
	 */
	public Route attachBase( String pathTemplate, Restlet target )
	{
		Route route = attach( pathTemplate, target );
		route.setMatchingMode( Template.MODE_STARTS_WITH );
		return route;
	}

	/**
	 * @param pathTemplate
	 * @param rewrittenPathTemplate
	 * @return
	 */
	public Route rewrite( String pathTemplate, String rewrittenPathTemplate )
	{
		return attach( pathTemplate, new Redirector( getContext(), rewrittenPathTemplate, Redirector.MODE_SERVER_DISPATCHER ) );
	}
}