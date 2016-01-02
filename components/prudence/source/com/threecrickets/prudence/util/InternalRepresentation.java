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

import org.restlet.data.MediaType;
import org.restlet.representation.EmptyRepresentation;

/**
 * A representation used to transfer internal objects.
 * 
 * @author Tal Liron
 */
public class InternalRepresentation extends EmptyRepresentation
{
	//
	// Constants
	//

	public static final MediaType MEDIA_TYPE = MediaType.register( "application/internal", "Internal" );

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param object
	 *        The object to transfer
	 */
	public InternalRepresentation( Object object )
	{
		super();
		this.object = object;
		setMediaType( MEDIA_TYPE );
	}

	//
	// Attributes
	//

	/**
	 * The transferred object.
	 * 
	 * @return The transferred object
	 */
	public Object getObject()
	{
		return object;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The transferred object.
	 */
	private final Object object;
}
