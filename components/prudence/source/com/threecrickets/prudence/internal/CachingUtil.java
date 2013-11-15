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

package com.threecrickets.prudence.internal;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.Reference;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Template;
import org.restlet.util.Series;

import com.threecrickets.prudence.DelegatedCacheKeyPatternHandler;
import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.prudence.cache.CacheEntry;
import com.threecrickets.prudence.internal.attributes.ResourceContextualAttributes;
import com.threecrickets.prudence.service.ResourceConversationServiceBase;
import com.threecrickets.prudence.util.CapturingRedirector;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * Caching utilities.
 * 
 * @author Tal Liron
 * @param <R>
 *        The resource
 * @param <A>
 *        The resource attributes
 */
public class CachingUtil<R extends ServerResource, A extends ResourceContextualAttributes>
{
	//
	// Static operations
	//

	/**
	 * The cache duration.
	 * 
	 * @param executable
	 *        The executable
	 * @return The cache duration
	 */
	public static long getCacheDuration( Executable executable )
	{
		Long cacheDuration = (Long) executable.getAttributes().get( CACHE_DURATION_ATTRIBUTE );
		return cacheDuration == null ? 0 : cacheDuration;
	}

	/**
	 * @param executable
	 *        The executable
	 * @param cacheDuration
	 *        The cache duration
	 * @see #getCacheDuration(Executable)
	 */
	public static void setCacheDuration( Executable executable, long cacheDuration )
	{
		executable.getAttributes().put( CACHE_DURATION_ATTRIBUTE, cacheDuration );
	}

	/**
	 * The cache expiration timestamp for the executable.
	 * 
	 * @return The cache expiration timestamp
	 * @see #getCacheDuration(Executable)
	 */
	public static long getExpirationTimestamp( Executable executable )
	{
		long cacheDuration = getCacheDuration( executable );
		if( cacheDuration <= 0 )
			return 0;
		else
			return executable.getLastExecutedTimestamp() + cacheDuration;
	}

	/**
	 * The cache key pattern.
	 * 
	 * @param executable
	 *        The executable
	 * @return The cache key pattern
	 */
	public static String getCacheKeyPattern( Executable executable )
	{
		return (String) executable.getAttributes().get( CACHE_KEY_PATTERN_ATTRIBUTE );
	}

	/**
	 * @param executable
	 *        The executable
	 * @param cacheKeyPattern
	 *        The cache key pattern
	 * @see #setCacheDuration(Executable, long)
	 */
	public static void setCacheKeyPattern( Executable executable, String cacheKeyPattern )
	{
		executable.getAttributes().put( CACHE_KEY_PATTERN_ATTRIBUTE, cacheKeyPattern );
	}

	/**
	 * The cache key pattern handlers.
	 * 
	 * @param executable
	 *        The executable
	 * @param create
	 *        Whether to create a handler map if it doesn't exist
	 * @return The handler map or null
	 */
	public static ConcurrentMap<String, String> getCacheKeyPatternHandlers( Executable executable, boolean create )
	{
		@SuppressWarnings("unchecked")
		ConcurrentMap<String, String> handlers = (ConcurrentMap<String, String>) executable.getAttributes().get( CACHE_KEY_PATTERN_HANDLERS_ATTRIBUTE );
		if( handlers == null && create )
		{
			handlers = new ConcurrentHashMap<String, String>();
			@SuppressWarnings("unchecked")
			ConcurrentMap<String, String> existing = (ConcurrentMap<String, String>) executable.getAttributes().putIfAbsent( CACHE_KEY_PATTERN_HANDLERS_ATTRIBUTE, handlers );
			if( existing != null )
				handlers = existing;
		}

		return handlers;
	}

	/**
	 * Adds encoding suffix to a cache key.
	 * 
	 * @param cacheKey
	 *        The cache key
	 * @param encoding
	 *        The encoding or null
	 * @return The cache key for the encoding
	 */
	public static String getCacheKeyForEncoding( String cacheKey, Encoding encoding )
	{
		if( encoding != null )
			return cacheKey + "|" + encoding.getName();
		else
			return cacheKey;
	}

	/**
	 * The cache tags.
	 * 
	 * @param executable
	 *        The executable
	 * @param create
	 *        Whether to create a cache tag set if it doesn't exist
	 * @return The cache tags or null
	 */
	public static Set<String> getCacheTags( Executable executable, boolean create )
	{
		@SuppressWarnings("unchecked")
		Set<String> cacheTags = (Set<String>) executable.getAttributes().get( CACHE_TAGS_ATTRIBUTE );
		if( cacheTags == null && create )
		{
			cacheTags = new CopyOnWriteArraySet<String>();
			@SuppressWarnings("unchecked")
			Set<String> existing = (Set<String>) executable.getAttributes().putIfAbsent( CACHE_TAGS_ATTRIBUTE, cacheTags );
			if( existing != null )
				cacheTags = existing;
		}

		return cacheTags;
	}

	/**
	 * The existing document descriptor.
	 * 
	 * @param request
	 *        The request
	 * @param clear
	 *        Whether to clear it
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static DocumentDescriptor<Executable> getExistingDocumentDescriptor( Request request, boolean clear )
	{
		ConcurrentMap<String, Object> attributes = request.getAttributes();
		if( clear )
			return (DocumentDescriptor<Executable>) attributes.remove( DOCUMENT_DESCRIPTOR_ATTRIBUTE );
		else
			return (DocumentDescriptor<Executable>) attributes.get( DOCUMENT_DESCRIPTOR_ATTRIBUTE );
	}

	/**
	 * The existing cache key.
	 * 
	 * @param request
	 *        The request
	 * @param clear
	 *        Whether to clear it
	 * @return
	 */
	public static String getExistingCacheKey( Request request, boolean clear )
	{
		ConcurrentMap<String, Object> attributes = request.getAttributes();
		if( clear )
			return (String) attributes.remove( CACHE_KEY_ATTRIBUTE );
		else
			return (String) attributes.get( CACHE_KEY_ATTRIBUTE );
	}

	/**
	 * The existing cache entry.
	 * 
	 * @param request
	 *        The request
	 * @param clear
	 *        Whether to clear it
	 * @return
	 */
	public static CacheEntry getExistingCacheEntry( Request request, boolean clear )
	{
		ConcurrentMap<String, Object> attributes = request.getAttributes();
		if( clear )
			return (CacheEntry) attributes.remove( CACHE_ENTRY_ATTRIBUTE );
		else
			return (CacheEntry) attributes.get( CACHE_ENTRY_ATTRIBUTE );
	}

	/**
	 * Removes existing valid document names.
	 * 
	 * @param request
	 *        The request
	 */
	public static void clearExistingValidDocumentName( Request request )
	{
		ConcurrentMap<String, Object> attributes = request.getAttributes();
		attributes.remove( DelegatedResource.class.getCanonicalName() + VALID_DOCUMENT_NAME_ATTRIBUTE );
		attributes.remove( GeneratedTextResource.class.getCanonicalName() + VALID_DOCUMENT_NAME_ATTRIBUTE );
	}

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 * @param attributes
	 *        The atrributes
	 */
	public CachingUtil( R resource, A attributes )
	{
		this.resource = resource;
		this.attributes = attributes;
		prefix = resource.getClass().getCanonicalName();
	}

	//
	// Operations
	//

	/**
	 * The valid document name, based on the remaining part (wildcard) of the
	 * reference.
	 * <p>
	 * Cached as a request attribute.
	 * 
	 * @param request
	 *        The request
	 * @return The valid document name
	 */
	public String getValidDocumentName( Request request )
	{
		ConcurrentMap<String, Object> attributes = request.getAttributes();
		String documentName = (String) attributes.get( prefix + VALID_DOCUMENT_NAME_ATTRIBUTE );
		if( documentName == null )
		{
			documentName = request.getResourceRef().getRemainingPart( true, false );
			documentName = this.attributes.validateDocumentName( documentName );
			attributes.put( prefix + VALID_DOCUMENT_NAME_ATTRIBUTE, documentName );
		}
		return documentName;
	}

	/**
	 * Fetches the cache entry for a document, if it exists and is valid.
	 * <p>
	 * After this call, you can call
	 * {@link #getExistingDocumentDescriptor(boolean)}.
	 * <p>
	 * If successful, you can also call {@link #getExistingCacheKey(boolean)}
	 * and {@link #getExistingCacheEntry(boolean)}.
	 * 
	 * @param documentName
	 *        The document name
	 * @param isTextWithScriptlets
	 *        Whether the document is text with scriptlets
	 * @param conversationService
	 *        The conversation service
	 * @return The cache entry
	 * @throws ParsingException
	 * @throws DocumentException
	 */
	public CacheEntry fetchCacheEntry( String documentName, boolean isTextWithScriptlets, ResourceConversationServiceBase<R> conversationService ) throws ParsingException, DocumentException
	{
		DocumentDescriptor<Executable> documentDescriptor = attributes.createDocumentOnce( documentName, isTextWithScriptlets, true, false, false );

		// Cache the document descriptor in the request
		ConcurrentMap<String, Object> attributes = resource.getRequest().getAttributes();
		attributes.put( DOCUMENT_DESCRIPTOR_ATTRIBUTE, documentDescriptor );

		String cacheKey = castCacheKey( documentDescriptor, isTextWithScriptlets, conversationService );
		if( cacheKey != null )
		{
			Cache cache = this.attributes.getCache();
			if( cache != null )
			{
				Encoding encoding = conversationService.getEncoding();

				// Try cache key for encoding first
				String cacheKeyForEncoding = getCacheKeyForEncoding( cacheKey, encoding );
				CacheEntry cacheEntry = cache.fetch( cacheKeyForEncoding );
				if( cacheEntry == null )
					cacheEntry = cache.fetch( cacheKey );

				if( cacheEntry != null )
				{
					// Make sure the document is not newer than the cache
					// entry
					if( documentDescriptor.getDocument().getDocumentTimestamp() <= cacheEntry.getDocumentModificationDate().getTime() )
					{
						// Cache the cache entry and key in the request
						attributes.put( CACHE_KEY_ATTRIBUTE, cacheKey );
						attributes.put( CACHE_ENTRY_ATTRIBUTE, cacheEntry );

						return cacheEntry;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Calls all installed cache key pattern handlers for the cache key pattern.
	 * 
	 * @param template
	 *        The cache key pattern template
	 * @param executable
	 *        The executable
	 */
	public void callCacheKeyPatternHandlers( Template template, Executable executable )
	{
		Map<String, String> resourceCacheKeyPatternHandlers = attributes.getCacheKeyPatternHandlers();
		Map<String, String> documentCacheKeyPatternHandlers = getCacheKeyPatternHandlers( executable, false );

		// Make sure we have handlers
		if( ( ( resourceCacheKeyPatternHandlers == null ) || resourceCacheKeyPatternHandlers.isEmpty() ) && ( ( documentCacheKeyPatternHandlers == null ) || documentCacheKeyPatternHandlers.isEmpty() ) )
			return;

		// Merge all handlers
		Map<String, String> cacheKeyPatternHandlers = new HashMap<String, String>();
		if( resourceCacheKeyPatternHandlers != null )
			cacheKeyPatternHandlers.putAll( resourceCacheKeyPatternHandlers );
		if( documentCacheKeyPatternHandlers != null )
			cacheKeyPatternHandlers.putAll( documentCacheKeyPatternHandlers );

		// Group variables together per handler
		Map<String, Set<String>> delegatedHandlers = new HashMap<String, Set<String>>();
		List<String> variableNames = template.getVariableNames();
		for( Map.Entry<String, String> entry : cacheKeyPatternHandlers.entrySet() )
		{
			String name = entry.getKey();
			String documentName = entry.getValue();

			if( variableNames.contains( name ) )
			{
				Set<String> variables = delegatedHandlers.get( documentName );
				if( variables == null )
				{
					variables = new HashSet<String>();
					delegatedHandlers.put( documentName, variables );
				}

				variables.add( name );
			}
		}

		// Call handlers
		if( !delegatedHandlers.isEmpty() )
		{
			for( Map.Entry<String, Set<String>> entry : delegatedHandlers.entrySet() )
			{
				String documentName = entry.getKey();
				String[] variableNamesArray = entry.getValue().toArray( new String[] {} );

				DelegatedCacheKeyPatternHandler delegatedHandler = new DelegatedCacheKeyPatternHandler( documentName, resource.getContext() );
				delegatedHandler.handleCacheKeyPattern( variableNamesArray );
			}
		}
	}

	/**
	 * Casts the cache key pattern for an executable.
	 * 
	 * @param documentDescriptor
	 *        The document descriptor
	 * @param isTextWithScriptlets
	 *        Whether the document is text with scriptlets
	 * @param conversationService
	 *        The conversation service
	 * @return The cache key or null
	 */
	public String castCacheKey( DocumentDescriptor<Executable> documentDescriptor, boolean isTextWithScriptlets, ResourceConversationServiceBase<R> conversationService )
	{
		String cacheKeyPattern = getCacheKeyPattern( documentDescriptor.getDocument() );
		if( cacheKeyPattern == null )
			return null;
		else
		{
			Request request = resource.getRequest();
			Response response = resource.getResponse();

			if( isTextWithScriptlets )
			{
				// Set initial media type according to the document's tag (might
				// be used by resolver)
				if( conversationService.getMediaType() == null )
					conversationService.setMediaTypeExtension( documentDescriptor.getTag() );
			}

			// Template and its resolver
			Template template = new Template( cacheKeyPattern );
			CacheKeyPatternResolver<R> resolver = new CacheKeyPatternResolver<R>( documentDescriptor, resource, conversationService, request, response );

			// Cache key pattern handlers
			callCacheKeyPatternHandlers( template, documentDescriptor.getDocument() );

			// Use captive reference as the resource reference
			Reference captiveReference = CapturingRedirector.getCapturedReference( request );
			Reference resourceReference = request.getResourceRef();
			if( captiveReference != null )
				request.setResourceRef( captiveReference );

			try
			{
				// Cast it
				return template.format( resolver );
			}
			finally
			{
				// Return regular reference
				if( captiveReference != null )
					request.setResourceRef( resourceReference );
			}
		}
	}

	/**
	 * Adds the caching debug headers if enabled.
	 * 
	 * @param cacheEntry
	 *        The cache entry
	 * @param cacheKey
	 *        The cache key
	 * @param executable
	 *        The executable
	 */
	public void addCachingDebugHeaders( String event, CacheEntry cacheEntry, String cacheKey, Executable executable )
	{
		if( !attributes.isDebug() )
			return;

		Series<Header> headers = cacheEntry.getHeaders();

		if( headers == null )
			headers = new Series<Header>( Header.class );
		else
		{
			// Copy headers
			Series<Header> newHeaders = new Series<Header>( Header.class );
			for( Header header : headers )
				newHeaders.add( header );
			headers = newHeaders;
		}

		// Override headers set by includes
		headers.removeAll( CACHE_HEADER );
		headers.removeAll( CACHE_KEY_HEADER );
		headers.removeAll( CACHE_EXPIRATION_HEADER );
		headers.removeAll( CACHE_TAGS_HEADER );

		SimpleDateFormat format = new SimpleDateFormat( CACHE_EXPIRATION_HEADER_FORMAT );
		format.setTimeZone( TimeZone.getTimeZone( "GMT" ) );

		headers.add( new Header( CACHE_HEADER, event ) );
		headers.add( new Header( CACHE_KEY_HEADER, cacheKey ) );
		headers.add( new Header( CACHE_EXPIRATION_HEADER, format.format( cacheEntry.getExpirationDate() ) ) );

		Set<String> cacheTags = getCacheTags( executable, false );
		if( cacheTags != null )
		{
			for( String cacheTag : cacheTags )
				headers.add( new Header( CACHE_TAGS_HEADER, cacheTag ) );
		}

		// Apply headers
		if( headers != null )
			resource.getResponse().getAttributes().put( HeaderConstants.ATTRIBUTE_HEADERS, headers );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final R resource;

	/**
	 * The attributes.
	 */
	private final A attributes;

	/**
	 * Prefix for executable attributes.
	 */
	private final String prefix;

	/**
	 * Cache duration attribute for an {@link Executable}.
	 */
	private static final String CACHE_DURATION_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".cacheDuration";

	/**
	 * Cache key pattern attribute for an {@link Executable}.
	 */
	private static final String CACHE_KEY_PATTERN_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".cacheKeyPattern";

	/**
	 * Cache key pattern handlers attribute for an {@link Executable}.
	 */
	private static final String CACHE_KEY_PATTERN_HANDLERS_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".cacheKeyPatternHandlers";

	/**
	 * Cache tags attribute for an {@link Executable}.
	 */
	private static final String CACHE_TAGS_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".cacheTags";

	/**
	 * Valid document name attribute for a {@link Request}.
	 */
	public static final String VALID_DOCUMENT_NAME_ATTRIBUTE = ".validDocumentName";

	/**
	 * Document descriptor attribute for a {@link Request}.
	 */
	private static final String DOCUMENT_DESCRIPTOR_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".documentDescriptor";

	/**
	 * Cache key attribute for a {@link Request}.
	 */
	private static final String CACHE_KEY_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".cacheKey";

	/**
	 * Cache entry attribute for a {@link Request}.
	 */
	private static final String CACHE_ENTRY_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".cacheEntry";

	/**
	 * Cache header.
	 */
	private static final String CACHE_HEADER = "X-Cache";

	/**
	 * Cache key header.
	 */
	private static final String CACHE_KEY_HEADER = "X-Cache-Key";

	/**
	 * Cache tags header.
	 */
	private static final String CACHE_TAGS_HEADER = "X-Cache-Tags";

	/**
	 * Cache expiration header.
	 */
	private static final String CACHE_EXPIRATION_HEADER = "X-Cache-Expiration";

	/**
	 * Cache expiration header date-time format.
	 */
	private static final String CACHE_EXPIRATION_HEADER_FORMAT = "EEE, dd MMM yyyy HH:mm:ss z";
}
