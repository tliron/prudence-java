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

import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.prudence.internal.CachingUtil;
import com.threecrickets.prudence.internal.attributes.DelegatedResourceAttributes;

/**
 * Caching service exposed to executables.
 * 
 * @author Tal Liron
 * @see DelegatedResource
 */
public class DelegatedResourceCachingService extends CachingServiceBase<DelegatedResource, DelegatedResourceAttributes, DelegatedResourceConversationService>
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
	public DelegatedResourceCachingService( DelegatedResource resource, DocumentService<DelegatedResourceAttributes> documentService, DelegatedResourceConversationService conversationService,
		CachingUtil<DelegatedResource, DelegatedResourceAttributes> cachingUtil )
	{
		super( resource, resource.getAttributes(), documentService, conversationService, cachingUtil );
	}

	//
	// CachingServiceBase
	//

	public String getKey()
	{
		return CachingUtil.getKeyForEncoding( cachingUtil.castKey( documentService.getDescriptor(), getSuffix(), false, conversationService ), conversationService.getEncoding() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected String getSuffix()
	{
		return CachingUtil.getDispatchedSuffix( resource.getRequest() );
	}
}