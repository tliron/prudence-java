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
import org.restlet.Restlet;
import org.restlet.data.Status;

/**
 * A restlet that always sets a specific status and does nothing else.
 * 
 * @author Tal Liron
 */
public class StatusRestlet extends Restlet
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param status
	 *        The status to set
	 */
	public StatusRestlet( Status status )
	{
		describe();
		this.status = status;
	}

	/**
	 * Constructor.
	 * 
	 * @param statusCode
	 *        The status code to set
	 */
	public StatusRestlet( int statusCode )
	{
		this( Status.valueOf( statusCode ) );
	}

	//
	// Restlet
	//

	@Override
	public void handle( Request request, Response response )
	{
		response.setStatus( status );
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
	// Private

	/**
	 * The status to set.
	 */
	private final Status status;

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Three Crickets" );
		setName( getClass().getSimpleName() );
		setDescription( "A restlet that sets a specific status" );
	}
}
