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

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.util.CallResolver;

/**
 * Improves on the default Restlet CallResolver.
 * <p>
 * <i>"Restlet" is a registered trademark of
 * <a href="http://restlet.com/legal">Restlet S.A.S.</a>.</i>
 * 
 * @author Tal Liron
 */
public class EnhancedCallResolver extends CallResolver
{
	//
	// Construction
	//

	public EnhancedCallResolver( Request request, Response response )
	{
		super( request, response );
		this.request = request;
	}

	//
	// Resolver
	//

	@Override
	public Object resolve( String name )
	{
		if( name.equals( "rw" ) )
			return request.getResourceRef().getRemainingPart( false, false );

		return super.resolve( name );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Request request;
}
