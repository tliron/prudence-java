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

import java.util.concurrent.ConcurrentMap;

import org.restlet.Response;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.util.Series;

/**
 * Utility methods for Restlet.
 * <p>
 * <i>"Restlet" is a registered trademark of <a
 * href="http://www.restlet.org/about/legal">Restlet S.A.S.</a>.</i>
 * 
 * @author Tal Liron
 */
public abstract class RestletUtil
{
	//
	// Static operations
	//

	/**
	 * Gets the response headers, making sure to create them if they don't
	 * exist.
	 * 
	 * @param response
	 *        The response
	 * @return The headers
	 */
	public static Series<Header> getResponseHeaders( Response response )
	{
		ConcurrentMap<String, Object> attributes = response.getAttributes();
		@SuppressWarnings("unchecked")
		Series<Header> headers = (Series<Header>) attributes.get( HeaderConstants.ATTRIBUTE_HEADERS );
		if( headers == null )
		{
			headers = new Series<Header>( Header.class );
			@SuppressWarnings("unchecked")
			Series<Header> existing = (Series<Header>) attributes.putIfAbsent( HeaderConstants.ATTRIBUTE_HEADERS, headers );
			if( existing != null )
				headers = existing;
		}
		return headers;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private RestletUtil()
	{
	}
}
