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

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;

/**
 * A {@link Route} that always scores 0.0 for captured requests. Useful for
 * making sure that captured requests are captured again.
 * 
 * @author Tal Liron
 * @see CapturingRouter
 */
public class CapturingRoute extends TemplateRoute
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param next
	 *        The next restlet
	 */
	public CapturingRoute( Restlet next )
	{
		super( next );
		describe();
	}

	/**
	 * Constructor.
	 * 
	 * @param router
	 *        The router
	 * @param uriTemplate
	 *        The URI template
	 * @param next
	 *        The next restlet
	 */
	public CapturingRoute( Router router, String uriTemplate, Restlet next )
	{
		super( router, uriTemplate, next );
		describe();
	}

	/**
	 * Constructor.
	 * 
	 * @param router
	 *        The router
	 * @param template
	 *        The URI template
	 * @param next
	 *        The next restlet
	 */
	public CapturingRoute( Router router, Template template, Restlet next )
	{
		super( router, template, next );
		describe();
	}

	//
	// Route
	//

	@Override
	public float score( Request request, Response response )
	{
		if( CapturingRedirector.getCapturedReference( request ) != null )
			return 0f;

		return super.score( request, response );
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
		setDescription( "A route that always scored 0.0 for captured requests" );
	}
}
