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

import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.internal.CachingUtil;
import com.threecrickets.prudence.internal.attributes.GeneratedTextResourceAttributes;

/**
 * Caching service exposed to executables.
 * 
 * @author Tal Liron
 * @see GeneratedTextResource
 */
public class GeneratedTextResourceCachingService extends CachingServiceBase<GeneratedTextResource, GeneratedTextResourceAttributes, GeneratedTextResourceConversationService>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 * @param documentService
	 *        The document service
	 * @param conversationService
	 *        The conversation service
	 * @param cachingUtil
	 *        The caching utilities
	 */
	public GeneratedTextResourceCachingService( GeneratedTextResource resource, DocumentService<GeneratedTextResourceAttributes> documentService, GeneratedTextResourceConversationService conversationService,
		CachingUtil<GeneratedTextResource, GeneratedTextResourceAttributes> cachingUtil )
	{
		super( resource, resource.getAttributes(), documentService, conversationService, cachingUtil );
	}

	//
	// CachingServiceBase
	//

	/**
	 * Casts the cache key template for the current executable and encoding.
	 * 
	 * @return The cache key or null
	 */
	public String getKey()
	{
		String key = CachingUtil.getExistingKey( resource.getRequest(), false );
		if( key == null )
			key = cachingUtil.castKey( documentService.getDescriptor(), null, true, conversationService, conversationService.getEncoding() );
		return key;
	}
}