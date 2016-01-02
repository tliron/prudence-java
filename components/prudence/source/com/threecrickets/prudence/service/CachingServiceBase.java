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

package com.threecrickets.prudence.service;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.internal.CachingUtil;
import com.threecrickets.prudence.internal.attributes.ResourceContextualAttributes;

/**
 * Caching service exposed to executables.
 * 
 * @author Tal Liron
 * @param <R>
 *        The resource
 * @param <A>
 *        The resource attributes
 * @param <C>
 *        The conversation service
 */
public abstract class CachingServiceBase<R extends ServerResource, A extends ResourceContextualAttributes, C extends ResourceConversationServiceBase<R>>
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
	 * @param documentService
	 *        The document service
	 * @param conversationService
	 *        The conversation service
	 * @param cachingUtil
	 *        The caching utilities
	 */
	public CachingServiceBase( R resource, A attributes, DocumentService<A> documentService, C conversationService, CachingUtil<R, A> cachingUtil )
	{
		this.resource = resource;
		this.attributes = attributes;
		this.documentService = documentService;
		this.conversationService = conversationService;
		this.cachingUtil = cachingUtil;

	}

	//
	// Attributes
	//

	/**
	 * The cache duration. Defaults to 0.
	 * 
	 * @return The cache duration in milliseconds
	 * @see #setDuration(Object)
	 */
	public Object getDuration()
	{
		return CachingUtil.getDuration( documentService.getDescriptor().getDocument(), getSuffix() );
	}

	/**
	 * @param cacheDuration
	 *        The cache duration in milliseconds
	 * @see #getDuration()
	 */
	public void setDuration( Object cacheDuration )
	{
		CachingUtil.setDuration( documentService.getDescriptor().getDocument(), getSuffix(), CachingUtil.toMilliseconds( cacheDuration ) );
	}

	/**
	 * Whether to cache only GET requests
	 * 
	 * @return Whether to cache only GET requests
	 */
	public boolean getOnlyGet()
	{
		return CachingUtil.getOnlyGet( documentService.getDescriptor().getDocument(), getSuffix() );
	}

	/**
	 * @param cacheOnlyGet
	 *        Whether to cache only GET requests
	 * @see #getOnlyGet()
	 */
	public void setOnlyGet( boolean cacheOnlyGet )
	{
		CachingUtil.setOnlyGet( documentService.getDescriptor().getDocument(), getSuffix(), cacheOnlyGet );
	}

	/**
	 * The cache key template.
	 * 
	 * @return The cache key template
	 * @see #setKeyTemplate(String)
	 */
	public String getKeyTemplate()
	{
		return CachingUtil.getKeyTemplate( documentService.getDescriptor().getDocument(), getSuffix() );
	}

	/**
	 * @param cacheKeyTemplate
	 *        The cache key template
	 * @see #getKeyTemplate()
	 */
	public void setKeyTemplate( String cacheKeyTemplate )
	{
		CachingUtil.setKeyTemplate( documentService.getDescriptor().getDocument(), getSuffix(), cacheKeyTemplate );
	}

	/**
	 * The cache key template plugins.
	 * 
	 * @return The cache key template plugins
	 */
	public ConcurrentMap<String, String> getKeyTemplatePlugins()
	{
		return CachingUtil.getKeyTemplatePlugins( documentService.getDescriptor().getDocument(), getSuffix(), true );
	}

	/**
	 * @return The cache tags
	 */
	public Set<String> getTags()
	{
		return CachingUtil.getTags( documentService.getDescriptor().getDocument(), getSuffix(), true );
	}

	/**
	 * Casts the cache key template for the current executable and encoding.
	 * 
	 * @return The cache key or null
	 */
	public abstract String getKey();

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * The resource.
	 */
	protected final R resource;

	/**
	 * The attributes.
	 */
	protected final A attributes;

	/**
	 * The document service.
	 */
	protected final DocumentService<A> documentService;

	/**
	 * The conversation service.
	 */
	protected final C conversationService;

	/**
	 * The caching utilities.
	 */
	protected final CachingUtil<R, A> cachingUtil;

	/**
	 * The optional attribute suffix.
	 * 
	 * @return The attribute suffix or null
	 */
	protected String getSuffix()
	{
		return null;
	}
}
