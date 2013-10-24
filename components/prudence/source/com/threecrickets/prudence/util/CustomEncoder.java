/**
 * Copyright 2009-2013 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.util;

import java.util.ArrayList;
import java.util.List;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.Encoding;
import org.restlet.engine.application.Encoder;
import org.restlet.service.EncoderService;

/**
 * An encoder supporting a list of default encodings.
 * <p>
 * By default it uses its own {@link CustomEncoderService}, instead of that of
 * the application, allowing specific customizations, for example custom
 * settings for {@link EncoderService#getDefaultIgnoredMediaTypes()}.
 * 
 * @author Tal Liron
 * @see IoUtil#SUPPORTED_COMPRESSION_ENCODINGS
 */
public class CustomEncoder extends Encoder
{
	//
	// Constants
	//

	/**
	 * Global list of supported encodings.
	 */
	public static final List<Encoding> SUPPORTED_ENCODINGS = new ArrayList<Encoding>();

	static
	{
		SUPPORTED_ENCODINGS.addAll( IoUtil.SUPPORTED_COMPRESSION_ENCODINGS );
		SUPPORTED_ENCODINGS.add( Encoding.IDENTITY );
	}

	//
	// Construction
	//

	/**
	 * Constructor that encodes responses.
	 * 
	 * @param application
	 *        The application
	 */
	public CustomEncoder( Application application )
	{
		this( application, false, true );
	}

	/**
	 * Constructor.
	 * 
	 * @param application
	 *        The application
	 * @param encodingRequest
	 *        Indicates if the request entities should be encoded
	 * @param encodingResponse
	 *        Indicates if the response entities should be encoded
	 */
	public CustomEncoder( Application application, boolean encodingRequest, boolean encodingResponse )
	{
		this( application.getContext(), encodingRequest, encodingResponse, new CustomEncoderService() );
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 * @param encodingRequest
	 *        Indicates if the request entities should be encoded
	 * @param encodingResponse
	 *        Indicates if the response entities should be encoded
	 * @param encoderService
	 *        The encoder service
	 */
	public CustomEncoder( Context context, boolean encodingRequest, boolean encodingResponse, EncoderService encoderService )
	{
		super( context, encodingRequest, encodingResponse, encoderService );
		describe();
	}

	//
	// Encoder
	//

	@Override
	public List<Encoding> getSupportedEncodings()
	{
		return SUPPORTED_ENCODINGS;
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
	// Private

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Three Crickets" );
		setName( getClass().getSimpleName() );
		setDescription( "An encoder that supports the default encodings" );
	}
}
