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

package com.threecrickets.prudence.service;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.cache.Cache;
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

	/**
	 * The cache.
	 * 
	 * @return The cache
	 */
	public Cache getCache()
	{
		return attributes.getCache();
	}

	/**
	 * The cache duration. Defaults to 0.
	 * 
	 * @return The cache duration in milliseconds
	 * @see #setCacheDuration(long)
	 */
	public long getCacheDuration()
	{
		return CachingUtil.getCacheDuration( getDescriptor().getDocument() );
	}

	/**
	 * @param cacheDuration
	 *        The cache duration in milliseconds
	 * @see #getCacheDuration()
	 */
	public void setCacheDuration( long cacheDuration )
	{
		CachingUtil.setCacheDuration( getDescriptor().getDocument(), cacheDuration );
	}

	/**
	 * Whether to cache only GET requests
	 * 
	 * @return Whether to cache only GET requests
	 */
	public boolean getCacheOnlyGet()
	{
		return CachingUtil.getCacheOnlyGet( getDescriptor().getDocument() );
	}

	/**
	 * @param cacheOnlyGet
	 *        Whether to cache only GET requests
	 * @see #getCacheOnlyGet()
	 */
	public void setCacheOnlyGet( boolean cacheOnlyGet )
	{
		CachingUtil.setCacheOnlyGet( getDescriptor().getDocument(), cacheOnlyGet );
	}

	/**
	 * The cache key pattern.
	 * 
	 * @return The cache key pattern
	 * @see #setCacheKeyPattern(String)
	 */
	public String getCacheKeyPattern()
	{
		return CachingUtil.getCacheKeyPattern( getDescriptor().getDocument() );
	}

	/**
	 * @param cacheKeyPattern
	 *        The cache key pattern
	 * @see #getCacheKeyPattern()
	 */
	public void setCacheKeyPattern( String cacheKeyPattern )
	{
		CachingUtil.setCacheKeyPattern( getDescriptor().getDocument(), cacheKeyPattern );
	}

	/**
	 * The cache key pattern handlers.
	 * 
	 * @return The cache key pattern handlers
	 */
	public ConcurrentMap<String, String> getCacheKeyPatternHandlers()
	{
		return CachingUtil.getCacheKeyPatternHandlers( getDescriptor().getDocument(), true );
	}

	/**
	 * @return The cache tags
	 */
	public Set<String> getCacheTags()
	{
		return CachingUtil.getCacheTags( getDescriptor().getDocument(), true );
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
