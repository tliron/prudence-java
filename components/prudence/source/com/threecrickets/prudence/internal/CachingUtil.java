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

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Template;
import org.restlet.util.Series;

import com.threecrickets.prudence.DelegatedCacheKeyTemplateHandler;
import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.prudence.cache.CacheEntry;
import com.threecrickets.prudence.internal.attributes.ResourceContextualAttributes;
import com.threecrickets.prudence.service.ResourceConversationServiceBase;
import com.threecrickets.prudence.util.CapturingRedirector;
import com.threecrickets.prudence.util.IoUtil;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.DocumentNotFoundException;
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
	// Static operations
	//

	/**
	 * The cache duration.
	 * 
	 * @param executable
	 *        The executable
	 * @param suffix
	 *        The optional attribute suffix
	 * @return The cache duration
	 */
	public static long getCacheDuration( Executable executable, String suffix )
	{
		Long cacheDuration = (Long) executable.getAttributes().get( suffix == null ? CACHE_DURATION_ATTRIBUTE : CACHE_DURATION_ATTRIBUTE + suffix );
		return cacheDuration == null ? 0 : cacheDuration;
	}

	/**
	 * @param executable
	 *        The executable
	 * @param suffix
	 *        The optional attribute suffix
	 * @param cacheDuration
	 *        The cache duration
	 * @see #getCacheDuration(Executable)
	 */
	public static void setCacheDuration( Executable executable, String suffix, long cacheDuration )
	{
		executable.getAttributes().put( suffix == null ? CACHE_DURATION_ATTRIBUTE : CACHE_DURATION_ATTRIBUTE + suffix, cacheDuration );
	}

	/**
	 * The cache expiration timestamp for the executable.
	 * 
	 * @param executable
	 *        The executable
	 * @param suffix
	 *        The optional attribute suffix
	 * @return The cache expiration timestamp
	 * @see #getCacheDuration(Executable)
	 */
	public static long getExpirationTimestamp( Executable executable, String suffix )
	{
		long cacheDuration = getCacheDuration( executable, suffix );
		if( cacheDuration <= 0 )
			return 0;
		else
			return executable.getLastUsedTimestamp() + cacheDuration;
	}

	/**
	 * Whether to cache only GET requests
	 * 
	 * @param executable
	 *        The executable
	 * @param suffix
	 *        The optional attribute suffix
	 * @return Whether to cache only GET requests
	 */
	public static boolean getCacheOnlyGet( Executable executable, String suffix )
	{
		Boolean cacheOnlyGet = (Boolean) executable.getAttributes().get( suffix == null ? CACHE_ONLY_GET_ATTRIBUTE : CACHE_ONLY_GET_ATTRIBUTE + suffix );
		return cacheOnlyGet != null ? cacheOnlyGet : false;
	}

	/**
	 * @param executable
	 *        The executable
	 * @param suffix
	 *        The optional attribute suffix
	 * @param cacheOnlyGet
	 *        Whether to cache only GET requests
	 * @see #getCacheOnlyGet()
	 */
	public static void setCacheOnlyGet( Executable executable, String suffix, boolean cacheOnlyGet )
	{
		executable.getAttributes().put( suffix == null ? CACHE_ONLY_GET_ATTRIBUTE : CACHE_ONLY_GET_ATTRIBUTE + suffix, cacheOnlyGet );
	}

	/**
	 * The cache key template.
	 * 
	 * @param executable
	 *        The executable
	 * @param suffix
	 *        The optional attribute suffix
	 * @return The cache key template
	 */
	public static String getCacheKeyTemplate( Executable executable, String suffix )
	{
		return (String) executable.getAttributes().get( suffix == null ? CACHE_KEY_TEMPLATE_ATTRIBUTE : CACHE_KEY_TEMPLATE_ATTRIBUTE + suffix );
	}

	/**
	 * @param executable
	 *        The executable
	 * @param suffix
	 *        The optional attribute suffix
	 * @param cacheKeyTemplate
	 *        The cache key template
	 * @see #setCacheDuration(Executable, long)
	 */
	public static void setCacheKeyTemplate( Executable executable, String suffix, String cacheKeyTemplate )
	{
		executable.getAttributes().put( suffix == null ? CACHE_KEY_TEMPLATE_ATTRIBUTE : CACHE_KEY_TEMPLATE_ATTRIBUTE + suffix, cacheKeyTemplate );
	}

	/**
	 * The cache key template handlers.
	 * 
	 * @param executable
	 *        The executable
	 * @param suffix
	 *        The optional attribute suffix
	 * @param create
	 *        Whether to create a handler map if it doesn't exist
	 * @return The handler map or null
	 */
	public static ConcurrentMap<String, String> getCacheKeyTemplateHandlers( Executable executable, String suffix, boolean create )
	{
		String key = suffix == null ? CACHE_KEY_TEMPLATE_HANDLERS_ATTRIBUTE : CACHE_KEY_TEMPLATE_HANDLERS_ATTRIBUTE + suffix;
		@SuppressWarnings("unchecked")
		ConcurrentMap<String, String> handlers = (ConcurrentMap<String, String>) executable.getAttributes().get( key );
		if( handlers == null && create )
		{
			handlers = new ConcurrentHashMap<String, String>();
			@SuppressWarnings("unchecked")
			ConcurrentMap<String, String> existing = (ConcurrentMap<String, String>) executable.getAttributes().putIfAbsent( key, handlers );
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
		return encoding != null ? cacheKey + '|' + encoding.getName() : cacheKey + '|';
	}

	/**
	 * The cache tags.
	 * 
	 * @param executable
	 *        The executable
	 * @param suffix
	 *        The optional attribute suffix
	 * @param create
	 *        Whether to create a cache tag set if it doesn't exist
	 * @return The cache tags or null
	 */
	public static Set<String> getCacheTags( Executable executable, String suffix, boolean create )
	{
		String key = suffix == null ? CACHE_TAGS_ATTRIBUTE : CACHE_TAGS_ATTRIBUTE + suffix;
		@SuppressWarnings("unchecked")
		Set<String> cacheTags = (Set<String>) executable.getAttributes().get( key );
		if( cacheTags == null && create )
		{
			cacheTags = new CopyOnWriteArraySet<String>();
			@SuppressWarnings("unchecked")
			Set<String> existing = (Set<String>) executable.getAttributes().putIfAbsent( key, cacheTags );
			if( existing != null )
				cacheTags = existing;
		}

		return cacheTags;
	}

	/**
	 * The dispatched attribute suffix for a request.
	 * 
	 * @param request
	 *        The request
	 * @return The attribute suffix
	 */
	public static String getDispatchedSuffix( Request request )
	{
		String dispatchedId = (String) request.getAttributes().get( DISPATCHED_ID_ATTRIBUTE );
		return dispatchedId == null ? null : "." + dispatchedId;
	}

	/**
	 * The existing document descriptor.
	 * 
	 * @param request
	 *        The request
	 * @param clear
	 *        Whether to clear it
	 * @return The existing document descriptor
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
	 * @return The existing cache key
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
	 * @return The existing cache entry
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
	 * Whether we may fetch from the cache.
	 * 
	 * @param request
	 *        The request
	 * @param executable
	 *        The executable
	 * @param suffix
	 *        The optional attribute suffix
	 * @return Whether we may fetch from the cache
	 */
	public static boolean mayFetch( Request request, Executable executable, String suffix )
	{
		if( executable != null )
			return getCacheOnlyGet( executable, suffix ) ? request.getMethod().equals( Method.GET ) : true;
		else
			return request.getMethod().equals( Method.GET );
	}

	//
	// Static operations
	//

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
	 *        The attributes
	 */
	public CachingUtil( R resource, A attributes )
	{
		this.resource = resource;
		this.attributes = attributes;
		prefix = resource.getClass().getCanonicalName();
	}

	//
	// Attributes
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
	 * @throws ResourceException
	 */
	public String getValidDocumentName( Request request ) throws ResourceException
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

	//
	// Operations
	//

	/**
	 * Fetches the cache entry for a document, if it exists and is valid.
	 * <p>
	 * After this call, you can call
	 * {@link #getExistingDocumentDescriptor(boolean)}.
	 * <p>
	 * If successful, you can also call {@link #getExistingCacheKey(boolean)}
	 * and {@link #getExistingCacheEntry(boolean)}.
	 * 
	 * @param suffix
	 *        The optional attribute suffix
	 * @param isTextWithScriptlets
	 *        Whether the document is text with scriptlets
	 * @param conversationService
	 *        The conversation service
	 * @return The cache entry
	 * @throws ResourceException
	 */
	public CacheEntry fetchCacheEntry( String suffix, boolean isTextWithScriptlets, ResourceConversationServiceBase<R> conversationService ) throws ResourceException
	{
		Request request = resource.getRequest();
		String documentName = getValidDocumentName( request );

		try
		{
			DocumentDescriptor<Executable> documentDescriptor = attributes.createDocumentOnce( documentName, isTextWithScriptlets, true, false, false );

			// Cache the document descriptor in the request
			ConcurrentMap<String, Object> attributes = request.getAttributes();
			attributes.put( DOCUMENT_DESCRIPTOR_ATTRIBUTE, documentDescriptor );

			String cacheKey = castCacheKey( documentDescriptor, suffix, isTextWithScriptlets, conversationService );
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
		}
		catch( ParsingException x )
		{
			throw new ResourceException( x );
		}
		catch( DocumentNotFoundException x )
		{
			throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
		}
		catch( DocumentException x )
		{
			throw new ResourceException( x );
		}

		return null;
	}

	/**
	 * Fetches a cached representation, re-encoding it if necessary.
	 * 
	 * @param documentDescriptor
	 *        The document descriptor
	 * @param suffix
	 *        The optional attribute suffix
	 * @param isTextWithScriptlets
	 *        Whether the document is text with scriptlets
	 * @param request
	 *        The request
	 * @param encoding
	 *        The encoding or null
	 * @param writer
	 *        The writer or null
	 * @param conversationService
	 *        The conversation service
	 * @return The cached, re-encoded representation or null if not found
	 * @throws ResourceException
	 */
	public Representation fetchRepresentation( DocumentDescriptor<Executable> documentDescriptor, String suffix, boolean isTextWithScriptlets, Request request, Encoding encoding, Writer writer,
		ResourceConversationServiceBase<R> conversationService ) throws ResourceException
	{
		Executable executable = documentDescriptor.getDocument();

		// See if a valid cache entry has already been cached in the request
		CacheEntry cacheEntry = getExistingCacheEntry( request, true );
		String cacheKey = getExistingCacheKey( request, true );
		if( ( cacheEntry != null ) && ( cacheKey != null ) )
			return reencode( cacheEntry, encoding, cacheKey, executable, suffix, writer );

		// Attempt to use cache
		cacheKey = castCacheKey( documentDescriptor, suffix, isTextWithScriptlets, conversationService );
		if( cacheKey != null )
		{
			Cache cache = attributes.getCache();
			if( cache != null )
			{
				// Try cache key for encoding first
				String cacheKeyForEncoding = getCacheKeyForEncoding( cacheKey, encoding );
				cacheEntry = cache.fetch( cacheKeyForEncoding );
				if( cacheEntry == null )
					cacheEntry = cache.fetch( cacheKey );

				// Make sure the document is not newer than the cache
				// entry
				if( ( cacheEntry != null ) && ( executable.getDocumentTimestamp() <= cacheEntry.getDocumentModificationDate().getTime() ) )
					return reencode( cacheEntry, encoding, cacheKey, executable, suffix, writer );
			}
		}

		return null;
	}

	/**
	 * Stores an encoded entry in the cache, optionally also storing an
	 * un-encoded entry.
	 * 
	 * @param encodedCacheEntry
	 *        The encoded cache entry
	 * @param cacheEntry
	 *        The un-encoded cache entry or null
	 * @param documentDescriptor
	 *        The document descriptor
	 * @param suffix
	 *        The optional attribute suffix
	 * @param isTextWithScriptlets
	 *        Whether the document is text with scriptlets
	 * @param cacheTags
	 *        The cache tags or null
	 * @param conversationService
	 *        The conversation service
	 * @throws ResourceException
	 */
	public void storeCacheEntry( CacheEntry encodedCacheEntry, CacheEntry cacheEntry, DocumentDescriptor<Executable> documentDescriptor, String suffix, boolean isTextWithScriptlets, Set<String> cacheTags,
		ResourceConversationServiceBase<R> conversationService ) throws ResourceException
	{
		Executable executable = documentDescriptor.getDocument();

		String cacheKey = castCacheKey( documentDescriptor, suffix, isTextWithScriptlets, conversationService );
		if( cacheKey != null )
		{
			// Cache!
			Cache cache = attributes.getCache();
			if( cache != null )
			{
				String[] tags = null;
				if( cacheTags != null )
					tags = cacheTags.toArray( new String[] {} );

				Encoding encoding = encodedCacheEntry.getEncoding();
				String cacheKeyForEncoding = getCacheKeyForEncoding( cacheKey, encoding );
				encodedCacheEntry.setTags( tags );
				cache.store( cacheKeyForEncoding, encodedCacheEntry );

				// Cache un-encoded entry separately
				if( encoding != null )
				{
					cacheEntry.setTags( tags );
					cache.store( cacheKey, cacheEntry );
				}

				addCachingDebugHeaders( "miss", encodedCacheEntry, cacheKey, executable, suffix );
			}
		}
	}

	/**
	 * Calls all installed cache key template handlers for the cache key
	 * template.
	 * 
	 * @param template
	 *        The cache key template template
	 * @param executable
	 *        The executable
	 * @param suffix
	 *        The optional attribute suffix
	 */
	public void callCacheKeyTemplateHandlers( Template template, Executable executable, String suffix )
	{
		Map<String, String> resourceCacheKeyTemplateHandlers = attributes.getCacheKeyTemplateHandlers();
		Map<String, String> documentCacheKeyTemplateHandlers = getCacheKeyTemplateHandlers( executable, suffix, false );

		// Make sure we have handlers
		if( ( ( resourceCacheKeyTemplateHandlers == null ) || resourceCacheKeyTemplateHandlers.isEmpty() ) && ( ( documentCacheKeyTemplateHandlers == null ) || documentCacheKeyTemplateHandlers.isEmpty() ) )
			return;

		// Merge all handlers
		Map<String, String> cacheKeyTemplateHandlers = new HashMap<String, String>();
		if( resourceCacheKeyTemplateHandlers != null )
			cacheKeyTemplateHandlers.putAll( resourceCacheKeyTemplateHandlers );
		if( documentCacheKeyTemplateHandlers != null )
			cacheKeyTemplateHandlers.putAll( documentCacheKeyTemplateHandlers );

		// Group variables together per handler
		Map<String, Set<String>> delegatedHandlers = new HashMap<String, Set<String>>();
		List<String> variableNames = template.getVariableNames();
		for( Map.Entry<String, String> entry : cacheKeyTemplateHandlers.entrySet() )
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

				DelegatedCacheKeyTemplateHandler delegatedHandler = new DelegatedCacheKeyTemplateHandler( documentName, resource.getContext() );
				delegatedHandler.handleCacheKeyTemplate( variableNamesArray );
			}
		}
	}

	/**
	 * Casts the cache key template for an executable.
	 * 
	 * @param documentDescriptor
	 *        The document descriptor
	 * @param suffix
	 *        The optional attribute suffix
	 * @param isTextWithScriptlets
	 *        Whether the document is text with scriptlets
	 * @param conversationService
	 *        The conversation service
	 * @return The cache key or null
	 */
	public String castCacheKey( DocumentDescriptor<Executable> documentDescriptor, String suffix, boolean isTextWithScriptlets, ResourceConversationServiceBase<R> conversationService )
	{
		Executable executable = documentDescriptor.getDocument();
		String cacheKeyTemplate = getCacheKeyTemplate( executable, suffix );
		if( cacheKeyTemplate == null )
			return null;

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
		Template template = new Template( cacheKeyTemplate );
		CacheKeyTemplateResolver<R> resolver = new CacheKeyTemplateResolver<R>( documentDescriptor, resource, conversationService, request, response );

		// Cache key template handlers
		callCacheKeyTemplateHandlers( template, executable, suffix );

		// Temporarily use captive reference as the resource reference
		Reference captiveReference = CapturingRedirector.getCapturedReference( request );
		Reference resourceReference = request.getResourceRef();
		if( captiveReference != null )
			request.setResourceRef( captiveReference );

		try
		{
			// Cast it
			String key = template.format( resolver );
			String mediaType = conversationService.getMediaTypeName();
			key += mediaType != null ? '|' + mediaType : '|';
			return key;
		}
		finally
		{
			// Return to original reference
			if( captiveReference != null )
				request.setResourceRef( resourceReference );
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
	 * @param suffix
	 *        The optional attribute suffix
	 */
	public void addCachingDebugHeaders( String event, CacheEntry cacheEntry, String cacheKey, Executable executable, String suffix )
	{
		if( !attributes.isDebugCaching() )
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

		Set<String> cacheTags = getCacheTags( executable, suffix, false );
		if( cacheTags != null )
		{
			for( String cacheTag : cacheTags )
				headers.add( new Header( CACHE_TAGS_HEADER, cacheTag ) );
		}

		// Apply headers
		if( headers != null )
			resource.getResponse().getAttributes().put( HeaderConstants.ATTRIBUTE_HEADERS, headers );
	}

	/**
	 * Represents a cache entry, making sure to re-encode it (and store the
	 * re-encoded entry in the cache) if necessary.
	 * 
	 * @param cacheEntry
	 *        The cache entry
	 * @param encoding
	 *        The encoding
	 * @param cacheKey
	 *        The cache key
	 * @param executable
	 *        The executable
	 * @param suffix
	 *        The optional attribute suffix
	 * @param writer
	 *        The writer
	 * @return The representation
	 * @throws ResourceException
	 */
	public Representation reencode( CacheEntry cacheEntry, Encoding encoding, String cacheKey, Executable executable, String suffix, Writer writer ) throws ResourceException
	{
		try
		{
			if( ( cacheEntry.getEncoding() == null ) && ( encoding != null ) && ( cacheEntry.getSize() >= attributes.getEncodeSizeThreshold() ) )
			{
				// Re-encode it
				cacheEntry = new CacheEntry( cacheEntry, encoding );

				// Cache re-encoded entry
				Cache cache = attributes.getCache();
				if( cache != null )
				{
					cacheKey = getCacheKeyForEncoding( cacheKey, encoding );
					Set<String> cacheTags = getCacheTags( executable, suffix, false );
					if( cacheTags != null )
						cacheEntry.setTags( cacheTags.toArray( new String[] {} ) );
					cache.store( cacheKey, cacheEntry );
				}
			}

			// We want to write this, too, for includes
			if( ( writer != null ) && ( cacheEntry.getString() != null ) )
				writer.write( cacheEntry.getString() );
		}
		catch( IOException x )
		{
			throw new ResourceException( x );
		}

		addCachingDebugHeaders( "hit", cacheEntry, cacheKey, executable, suffix );

		return cacheEntry.represent();
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
	 * Cache only GET attribute for an {@link Executable}.
	 */
	private static final String CACHE_ONLY_GET_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".cacheOnlyGet";

	/**
	 * Cache key template attribute for an {@link Executable}.
	 */
	private static final String CACHE_KEY_TEMPLATE_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".cacheKeyTemplate";

	/**
	 * Cache key template handlers attribute for an {@link Executable}.
	 */
	private static final String CACHE_KEY_TEMPLATE_HANDLERS_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".cacheKeyTemplateHandlers";

	/**
	 * Cache tags attribute for an {@link Executable}.
	 */
	private static final String CACHE_TAGS_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".cacheTags";

	/**
	 * The dispatched ID attribute for a {@link Request}.
	 */
	private static final String DISPATCHED_ID_ATTRIBUTE = "com.threecrickets.prudence.dispatcher.id";

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
