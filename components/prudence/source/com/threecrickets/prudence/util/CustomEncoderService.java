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

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.MediaType;
import org.restlet.service.EncoderService;

/**
 * Provides a workaround for
 * <a href="https://github.com/restlet/restlet-framework-java/issues/791">
 * Restlet issue 791</a>.
 * 
 * @author Tal Liron
 */
public class CustomEncoderService extends EncoderService
{
	//
	// Construction
	//

	public CustomEncoderService()
	{
		super();
		acceptedMediaTypes = new CopyOnWriteArrayList<MediaType>( super.getAcceptedMediaTypes() );
		ignoredMediaTypes = new CopyOnWriteArrayList<MediaType>( super.getIgnoredMediaTypes() );
	}

	//
	// EncoderService
	//

	@Override
	public List<MediaType> getAcceptedMediaTypes()
	{
		return acceptedMediaTypes;
	}

	@Override
	public List<MediaType> getIgnoredMediaTypes()
	{
		return ignoredMediaTypes;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final List<MediaType> acceptedMediaTypes;

	private final List<MediaType> ignoredMediaTypes;
}
