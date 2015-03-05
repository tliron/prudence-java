/**
 * Copyright 2009-2015 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.service;

import java.util.Set;

import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.internal.CachingUtil;
import com.threecrickets.prudence.internal.attributes.ResourceContextualAttributes;

/**
 * Document service exposed to executables.
 * 
 * @author Tal Liron
 * @param <R>
 *        The resource
 * @param <A>
 *        The resource attributes
 * @param <C>
 *        The conversation service
 */
public abstract class ResourceDocumentServiceBase<R extends ServerResource, A extends ResourceContextualAttributes, C extends ResourceConversationServiceBase<R>> extends DocumentService<A>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 * @param attributes
	 *        The attributes
	 * @param conversationService
	 *        The conversation service
	 * @param cachingUtil
	 *        The caching utilities
	 */
	public ResourceDocumentServiceBase( R resource, A attributes, C conversationService, CachingUtil<R, A> cachingUtil )
	{
		super( attributes );
		this.resource = resource;
		this.cachingUtil = cachingUtil;
		this.conversationService = conversationService;
	}

	//
	// Attributes
	//

	/**
	 * Pass-through documents can exist in the extra document sources.
	 * 
	 * @return The pass-through document names
	 */
	public Set<String> getPassThroughDocuments()
	{
		return attributes.getPassThroughDocuments();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * The resource.
	 */
	protected final R resource;

	/**
	 * The conversation service.
	 */
	protected final C conversationService;

	/**
	 * Caching utilities;
	 */
	protected final CachingUtil<R, A> cachingUtil;
}
