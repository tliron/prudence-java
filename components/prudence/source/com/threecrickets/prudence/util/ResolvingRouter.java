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

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.routing.Variable;

/**
 * A {@link Router} that uses {@link ResolvingTemplate} for all routes.
 * <p>
 * See <a
 * href="https://github.com/restlet/restlet-framework-java/issues/798">this
 * issue</a>.
 * 
 * @author Tal Liron
 */
public class ResolvingRouter extends Router
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 */
	public ResolvingRouter()
	{
		super();
		describe();
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 */
	public ResolvingRouter( Context context )
	{
		super( context );
		describe();
	}

	//
	// Object
	//

	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	//
	// Router
	//

	@Override
	protected TemplateRoute createRoute( String uriPattern, Restlet target, int matchingMode )
	{
		TemplateRoute result = new TemplateRoute( this, new ResolvingTemplate( uriPattern, matchingMode, Variable.TYPE_URI_SEGMENT, "", true, false ), target );
		result.setMatchingQuery( getDefaultMatchingQuery() );
		return result;
	}

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Three Crickets" );
		setName( getClass().getSimpleName() );
		setDescription( "A router that uses ResolvingTemplate for all routes" );
	}
}
