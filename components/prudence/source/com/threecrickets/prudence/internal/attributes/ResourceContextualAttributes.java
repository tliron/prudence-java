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

package com.threecrickets.prudence.internal.attributes;

import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.restlet.Context;
import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.internal.CachingUtil;

/**
 * @author Tal Liron
 */
public class ResourceContextualAttributes extends NonVolatileContextualAttributes
{
	//
	// Construction
	//

	public ResourceContextualAttributes( ServerResource resource )
	{
		super( resource.getClass().getCanonicalName() );
		this.resource = resource;
	}

	//
	// Attributes
	//

	/**
	 * Pass-through documents can exist in {@link #getLibraryDocumentSources()}
	 * as well as in {@link #getDocumentSource()}.
	 * 
	 * @return The pass-through document names
	 */
	@SuppressWarnings("unchecked")
	public Set<String> getPassThroughDocuments()
	{
		if( passThroughDocuments == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
			String key = prefix + ".passThroughDocuments";
			passThroughDocuments = (Set<String>) attributes.get( key );

			if( passThroughDocuments == null )
			{
				passThroughDocuments = new CopyOnWriteArraySet<String>();

				Set<String> existing = (Set<String>) attributes.putIfAbsent( key, passThroughDocuments );
				if( existing != null )
					passThroughDocuments = existing;
			}
		}

		return passThroughDocuments;
	}

	/**
	 * Whether to negotiate encoding by default. Defaults to true.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>negotiateEncoding</code> in the application's {@link Context}.
	 * 
	 * @return Whether to allow content negotiation
	 */
	public boolean isNegotiateEncoding()
	{
		if( negotiateEncoding == null )
		{
			negotiateEncoding = (Boolean) getAttributes().get( prefix + ".negotiateEncoding" );

			if( negotiateEncoding == null )
				negotiateEncoding = true;
		}

		return negotiateEncoding;
	}

	/**
	 * The size in bytes beyond which responses could be encoded. Defaults to
	 * 1024.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>encodeSizeThreshold</code> in the application's {@link Context}.
	 * 
	 * @return Whether to allow content negotiation
	 */
	public int getEncodeSizeThreshold()
	{
		if( encodeSizeThreshold == null )
		{
			Number number = (Number) getAttributes().get( prefix + ".encodeSizeThreshold" );

			if( number != null )
				encodeSizeThreshold = number.intValue();

			if( encodeSizeThreshold == null )
				encodeSizeThreshold = 1024;
		}

		return encodeSizeThreshold;
	}

	/**
	 * Whether to enable caching debugging. Defaults to false.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>debugCaching</code> in the application's {@link Context}.
	 * 
	 * @return Whether to enable caching debugging
	 */
	public boolean isDebugCaching()
	{
		if( debugCaching == null )
		{
			debugCaching = (Boolean) getAttributes().get( prefix + ".debugCaching" );

			if( debugCaching == null )
				debugCaching = true;
		}

		return debugCaching;
	}

	/**
	 * The name of the global variable with which to access the caching service.
	 * Defaults to "caching".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>cachingServiceName</code> in the application's {@link Context}.
	 * 
	 * @return The caching service name
	 */
	public String getCachingServiceName()
	{
		if( cachingServiceName == null )
		{
			cachingServiceName = (String) getAttributes().get( prefix + ".cachingServiceName" );

			if( cachingServiceName == null )
				cachingServiceName = "caching";
		}

		return cachingServiceName;
	}

	/**
	 * The default caching key template to use if the executable doesn't specify
	 * one. Defaults to "{ri}|{dn}|{nmt}|{nl}|{ne}".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>defaultCachingKeyTemplate</code> in the application's
	 * {@link Context}.
	 * 
	 * @return The default caching key template
	 */
	public String getDefaultCachingKeyTemplate()
	{
		if( defaultCachingKeyTemplate == null )
		{
			defaultCachingKeyTemplate = (String) getAttributes().get( prefix + ".defaultCachingKeyTemplate" );

			if( defaultCachingKeyTemplate == null )
				defaultCachingKeyTemplate = "{ri}|{dn}|{nmt}|{nl}|{ne}";
		}

		return defaultCachingKeyTemplate;
	}

	/**
	 * The caching key template plugins.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>cachingKeyTemplatePlugins</code> in the application's
	 * {@link Context}.
	 * 
	 * @return The cache key template plugins or null
	 */
	@SuppressWarnings("unchecked")
	public ConcurrentMap<String, String> getCachingKeyTemplatePlugins()
	{
		if( cachingKeyTemplatePlugins == null )
			cachingKeyTemplatePlugins = (ConcurrentMap<String, String>) getAttributes().get( prefix + ".cachingKeyTemplatePlugins" );

		return cachingKeyTemplatePlugins;
	}

	/**
	 * Whether or not to send information to the client about cache expiration.
	 * Defaults to {@link CachingUtil#CLIENT_CACHING_MODE_CONDITIONAL} .
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>clientCachingMode</code> in the application's {@link Context}.
	 * 
	 * @return The client caching mode
	 */
	public int getClientCachingMode()
	{
		if( clientCachingMode == null )
		{
			Number number = (Number) getAttributes().get( prefix + ".clientCachingMode" );

			if( number != null )
				clientCachingMode = number.intValue();

			if( clientCachingMode == null )
				clientCachingMode = CachingUtil.CLIENT_CACHING_MODE_CONDITIONAL;
		}

		return clientCachingMode;
	}

	/**
	 * The maximum client caching duration in milliseconds. Defaults to -1,
	 * which means no maximum.
	 * <p>
	 * Only has an effect when {@link #getClientCachingMode()} is
	 * {@link GeneratedTextResource#CLIENT_CACHING_MODE_OFFLINE}.
	 * 
	 * @return The maximum client caching duration.
	 */
	public long getMaxClientCachingDuration()
	{
		if( maxClientCachingDuration == null )
		{
			Number number = (Number) getAttributes().get( prefix + ".maxClientCachingDuration" );

			if( number != null )
				maxClientCachingDuration = number.longValue();
			else
				maxClientCachingDuration = -1L;
		}

		return maxClientCachingDuration;
	}

	//
	// ContextualAttributes
	//

	public ConcurrentMap<String, Object> getAttributes()
	{
		if( attributes == null )
			attributes = resource.getContext().getAttributes();

		return attributes;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * The resource.
	 */
	protected final ServerResource resource;

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Pass-through documents can exist in
	 * {@link GeneratedTextResourceAttributes#getLibraryDocumentSources()} as
	 * well as in {@link GeneratedTextResourceAttributes#getDocumentSource()}.
	 */
	private Set<String> passThroughDocuments;

	/**
	 * Whether to negotiate encoding by default.
	 */
	private Boolean negotiateEncoding;

	/**
	 * The size in bytes beyond which responses could be encoded.
	 */
	private Integer encodeSizeThreshold;

	/**
	 * Whether to enable caching debugging.
	 */
	private Boolean debugCaching;

	/**
	 * The name of the global variable with which to access the caching service.
	 */
	private String cachingServiceName;

	/**
	 * The default caching key template to use if the executable doesn't specify
	 * one.
	 */
	private String defaultCachingKeyTemplate;

	/**
	 * The caching key template plugins.
	 */
	private ConcurrentMap<String, String> cachingKeyTemplatePlugins;

	/**
	 * Whether or not to send information to the client about cache expiration.
	 */
	private Integer clientCachingMode;

	/**
	 * The maximum client caching expiration in milliseconds.
	 */
	private Long maxClientCachingDuration;

	/**
	 * The attributes.
	 */
	private ConcurrentMap<String, Object> attributes;
}
