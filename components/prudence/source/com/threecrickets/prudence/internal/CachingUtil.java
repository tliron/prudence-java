/**
 * Copyright 2009-2014 Three Crickets LLC.
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
import java.util.Date;
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
import org.restlet.data.CacheDirective;
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

import com.threecrickets.prudence.DelegatedCachingKeyTemplatePlugin;
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
import com.threecrickets.scripturian.parser.ProgramParser;

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
	 * Constant.
	 */
	public static final int CLIENT_CACHING_MODE_DISABLED = 0;

	/**
	 * Constant.
	 */
	public static final int CLIENT_CACHING_MODE_CONDITIONAL = 1;

	/**
	 * Constant.
	 */
	public static final int CLIENT_CACHING_MODE_OFFLINE = 2;

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
	 * Converts a string to a milliseconds long, interpreting 'ms', 's', 'm',
	 * 'h' and 'd' suffixes. Numbers are simply rounded to an integer.
	 * <p>
	 * Fractions can be used, and are rounded to the nearest millisecond, for
	 * example: "1.5d".
	 * 
	 * @param value
	 *        The value
	 * @return The milliseconds
	 */
	public static long toMilliseconds( Object value )
	{
		if( value == null )
			return 0L;
		if( value instanceof Number )
			return ( (Number) value ).longValue();
		else
		{
			String s = value.toString();
			if( s.endsWith( "ms" ) )
				return (long) Float.parseFloat( s.substring( 0, s.length() - 2 ) );
			else if( s.endsWith( "s" ) )
				return (long) ( Float.parseFloat( s.substring( 0, s.length() - 1 ) ) * 1000f );
			else if( s.endsWith( "m" ) )
				return (long) ( Float.parseFloat( s.substring( 0, s.length() - 1 ) ) * 60000f );
			else if( s.endsWith( "h" ) )
				return (long) ( Float.parseFloat( s.substring( 0, s.length() - 1 ) ) * 3600000f );
			else if( s.endsWith( "d" ) )
				return (long) ( Float.parseFloat( s.substring( 0, s.length() - 1 ) ) * 86400000f );
			else
				return (long) Float.parseFloat( s );
		}
	}

	/**
	 * Converts a string to a bytes integer, interpreting 'b', 'kb', 'mb', 'gb'
	 * and 'tb' suffixes. Numbers are simply rounded to an integer.
	 * <p>
	 * Fractions can be used, and are rounded to the nearest byte, for example:
	 * "1.5mb".
	 * 
	 * @param value
	 *        The value
	 * @return The bytes
	 */
	public static int toBytes( Object value )
	{
		if( value == null )
			return 0;
		if( value instanceof Number )
			return ( (Number) value ).intValue();
		else
		{
			String s = value.toString();
			if( s.endsWith( "kb" ) )
				return (int) ( Float.parseFloat( s.substring( 0, s.length() - 2 ) ) * 1024f );
			else if( s.endsWith( "mb" ) )
				return (int) ( Float.parseFloat( s.substring( 0, s.length() - 2 ) ) * 1048576f );
			else if( s.endsWith( "gb" ) )
				return (int) ( Float.parseFloat( s.substring( 0, s.length() - 2 ) ) * 1073741824f );
			else if( s.endsWith( "tb" ) )
				return (int) ( Float.parseFloat( s.substring( 0, s.length() - 2 ) ) * 1099511627776f );
			else if( s.endsWith( "b" ) )
				return (int) Float.parseFloat( s.substring( 0, s.length() - 1 ) );
			else
				return (int) Float.parseFloat( s );
		}

	}

	/**
	 * The cache duration.
	 * 
	 * @param executable
	 *        The executable
	 * @param suffix
	 *        The optional attribute suffix
	 * @return The cache duration
	 */
	public static long getDuration( Executable executable, String suffix )
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
	public static void setDuration( Executable executable, String suffix, long cacheDuration )
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
		long cacheDuration = getDuration( executable, suffix );
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
	public static boolean getOnlyGet( Executable executable, String suffix )
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
	public static void setOnlyGet( Executable executable, String suffix, boolean cacheOnlyGet )
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
	public static String getKeyTemplate( Executable executable, String suffix )
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
	public static void setKeyTemplate( Executable executable, String suffix, String cacheKeyTemplate )
	{
		executable.getAttributes().put( suffix == null ? CACHE_KEY_TEMPLATE_ATTRIBUTE : CACHE_KEY_TEMPLATE_ATTRIBUTE + suffix, cacheKeyTemplate );
	}

	/**
	 * The cache key template plugins.
	 * 
	 * @param executable
	 *        The executable
	 * @param suffix
	 *        The optional attribute suffix
	 * @param create
	 *        Whether to create a handler map if it doesn't exist
	 * @return The plugin map or null
	 */
	public static ConcurrentMap<String, String> getKeyTemplatePlugins( Executable executable, String suffix, boolean create )
	{
		String key = suffix == null ? CACHE_KEY_TEMPLATE_PLUGINS_ATTRIBUTE : CACHE_KEY_TEMPLATE_PLUGINS_ATTRIBUTE + suffix;
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
	public static Set<String> getTags( Executable executable, String suffix, boolean create )
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
	public static String getExistingKey( Request request, boolean clear )
	{
		ConcurrentMap<String, Object> attributes = request.getAttributes();
		if( clear )
			return (String) attributes.remove( CACHE_KEY_ATTRIBUTE );
		else
			return (String) attributes.get( CACHE_KEY_ATTRIBUTE );
	}

	/**
	 * The existing cache key for the encoding.
	 * 
	 * @param request
	 *        The request
	 * @param clear
	 *        Whether to clear it
	 * @return The existing cache key for the encoding
	 */
	public static String getExistingKeyForEncoding( Request request, boolean clear )
	{
		ConcurrentMap<String, Object> attributes = request.getAttributes();
		if( clear )
			return (String) attributes.remove( CACHE_KEY_FOR_ENCODING_ATTRIBUTE );
		else
			return (String) attributes.get( CACHE_KEY_FOR_ENCODING_ATTRIBUTE );
	}

	/**
	 * The existing valid cache entry.
	 * 
	 * @param request
	 *        The request
	 * @param clear
	 *        Whether to clear it
	 * @return The existing cache entry
	 */
	public static CacheEntry getExistingValidEntry( Request request, boolean clear )
	{
		ConcurrentMap<String, Object> attributes = request.getAttributes();
		if( clear )
			return (CacheEntry) attributes.remove( VALID_CACHE_ENTRY_ATTRIBUTE );
		else
			return (CacheEntry) attributes.get( VALID_CACHE_ENTRY_ATTRIBUTE );
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
			return getOnlyGet( executable, suffix ) ? request.getMethod().equals( Method.GET ) : true;
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
	 * @param parserName
	 *        The parser to use, or null for the default parser
	 * @param includeExtraSources
	 *        Whether to include the extra document sources
	 * @param conversationService
	 *        The conversation service
	 * @return The cache entry
	 * @throws ResourceException
	 */
	public CacheEntry fetchCacheEntry( String suffix, String parserName, boolean includeExtraSources, ResourceConversationServiceBase<R> conversationService ) throws ResourceException
	{
		Cache cache = attributes.getCache();
		if( cache == null )
			return null;

		Request request = resource.getRequest();
		String documentName = getValidDocumentName( request );
		boolean isPassThrough = attributes.getPassThroughDocuments().contains( "/" + documentName );
		boolean isCaptured = CapturingRedirector.getCapturedReference( request ) != null;

		try
		{
			DocumentDescriptor<Executable> documentDescriptor = attributes.createDocumentOnce( documentName, parserName, true, true, isPassThrough || isCaptured );

			ConcurrentMap<String, Object> attributes = request.getAttributes();
			attributes.put( DOCUMENT_DESCRIPTOR_ATTRIBUTE, documentDescriptor );

			CacheEntry cacheEntry = null;

			Encoding encoding = conversationService.getEncoding();
			if( encoding != null )
			{
				// Try encoded cache entry first
				String cacheKeyForEncoding = castKey( documentDescriptor, suffix, parserName, conversationService, encoding );
				if( cacheKeyForEncoding != null )
				{
					attributes.put( CACHE_KEY_FOR_ENCODING_ATTRIBUTE, cacheKeyForEncoding );
					cacheEntry = cache.fetch( cacheKeyForEncoding );
				}
			}

			if( cacheEntry == null )
			{
				// Try un-encoded cache entry
				String cacheKey = castKey( documentDescriptor, suffix, parserName, conversationService, null );
				if( cacheKey != null )
				{
					attributes.put( CACHE_KEY_ATTRIBUTE, cacheKey );
					cacheEntry = cache.fetch( cacheKey );
				}
			}

			// Make sure the document is not newer than the cache entry
			if( ( cacheEntry != null ) && ( documentDescriptor.getDocument().getDocumentTimestamp() <= cacheEntry.getDocumentModificationDate().getTime() ) )
			{
				// Is the cache entry in the right encoding?
				if( encoding == null ? cacheEntry.getEncoding() == null : encoding.equals( cacheEntry.getEncoding() ) )
					attributes.put( VALID_CACHE_ENTRY_ATTRIBUTE, cacheEntry );

				return cacheEntry;
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
	 * @param parseName
	 *        The parser to use, or null for the default parser
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
	public Representation fetchRepresentation( DocumentDescriptor<Executable> documentDescriptor, String suffix, String parserName, Request request, Encoding encoding, Writer writer,
		ResourceConversationServiceBase<R> conversationService ) throws ResourceException
	{
		Cache cache = attributes.getCache();
		if( cache == null )
			return null;

		Executable executable = documentDescriptor.getDocument();

		// Saved values (from fetchCacheEntry)
		String cacheKey = getExistingKey( request, true );
		String cacheKeyForEncoding = getExistingKeyForEncoding( request, true );
		CacheEntry cacheEntry = getExistingValidEntry( request, true );

		if( ( cacheEntry == null ) && ( encoding != null ) )
		{
			// Try encoded cache entry first
			if( cacheKeyForEncoding == null )
				cacheKeyForEncoding = castKey( documentDescriptor, suffix, parserName, conversationService, encoding );
			if( cacheKeyForEncoding != null )
			{
				cacheEntry = cache.fetch( cacheKeyForEncoding );
				if( cacheEntry != null )
					cacheKey = cacheKeyForEncoding;
			}
		}

		if( cacheEntry == null )
		{
			// Try un-encoded cache entry
			if( cacheKey == null )
				cacheKey = castKey( documentDescriptor, suffix, parserName, conversationService, null );
			if( cacheKey != null )
				cacheEntry = cache.fetch( cacheKey );
		}

		// Make sure the document is not newer than the cache entry
		if( ( cacheEntry != null ) && ( executable.getDocumentTimestamp() <= cacheEntry.getDocumentModificationDate().getTime() ) )
		{
			try
			{
				if( ( writer != null ) && ( cacheEntry.getString() != null ) )
					writer.write( cacheEntry.getString() );
			}
			catch( IOException x )
			{
				throw new ResourceException( x );
			}

			// Encode?
			if( ( encoding != null ) && ( cacheEntry.getEncoding() == null ) )
			{
				try
				{
					cacheEntry = new CacheEntry( cacheEntry, encoding );
					if( cacheKeyForEncoding == null )
						cacheKeyForEncoding = castKey( documentDescriptor, suffix, parserName, conversationService, encoding );
					if( cacheKeyForEncoding != null )
					{
						cacheKey = cacheKeyForEncoding;
						cache.store( cacheKey, cacheEntry );
					}
				}
				catch( IOException x )
				{
					throw new ResourceException( x );
				}

				addDebugHeaders( "hit;encode", cacheEntry, cacheKey, executable, suffix );
			}
			else
				addDebugHeaders( "hit", cacheEntry, cacheKey, executable, suffix );

			return cacheEntry.represent();
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
	 * @param parseName
	 *        The parser to use, or null for the default parser
	 * @param cacheTags
	 *        The cache tags or null
	 * @param conversationService
	 *        The conversation service
	 * @throws ResourceException
	 */
	public void store( CacheEntry encodedCacheEntry, CacheEntry cacheEntry, DocumentDescriptor<Executable> documentDescriptor, String suffix, String parserName, Set<String> cacheTags,
		ResourceConversationServiceBase<R> conversationService ) throws ResourceException
	{
		Cache cache = attributes.getCache();
		if( cache == null )
			return;

		Executable executable = documentDescriptor.getDocument();
		Request request = resource.getRequest();
		String cacheKey = getExistingKey( request, true );
		String cacheKeyForEncoding = getExistingKeyForEncoding( request, true );

		if( cacheKey == null )
			cacheKey = castKey( documentDescriptor, suffix, parserName, conversationService, null );

		if( cacheKey != null )
		{
			String[] tags = null;
			if( cacheTags != null )
				tags = cacheTags.toArray( new String[] {} );

			cacheEntry.setTags( tags );
			cache.store( cacheKey, cacheEntry );

			// Cache encoded entry separately
			Encoding encoding = encodedCacheEntry.getEncoding();
			if( encoding != null )
			{
				if( cacheKeyForEncoding == null )
					cacheKeyForEncoding = castKey( documentDescriptor, suffix, parserName, conversationService, encoding );

				if( cacheKeyForEncoding != null )
				{
					encodedCacheEntry.setTags( tags );
					cache.store( cacheKeyForEncoding, encodedCacheEntry );

					cacheEntry = encodedCacheEntry;
					cacheKey = cacheKeyForEncoding;
				}
			}
		}

		addDebugHeaders( "miss", cacheEntry, cacheKey, executable, suffix );
	}

	/**
	 * Calls all installed cache key template plugins for the cache key
	 * template.
	 * 
	 * @param template
	 *        The cache key template
	 * @param executable
	 *        The executable
	 * @param suffix
	 *        The optional attribute suffix
	 */
	public void callKeyTemplatePlugins( Template template, Executable executable, String suffix )
	{
		Map<String, String> resourceKeyTemplatePlugins = attributes.getCachingKeyTemplatePlugins();
		Map<String, String> documentKeyTemplatePlugins = getKeyTemplatePlugins( executable, suffix, false );

		// Make sure we have plugins
		if( ( ( resourceKeyTemplatePlugins == null ) || resourceKeyTemplatePlugins.isEmpty() ) && ( ( documentKeyTemplatePlugins == null ) || documentKeyTemplatePlugins.isEmpty() ) )
			return;

		// Merge all plugins
		Map<String, String> keyTemplatePlugins = new HashMap<String, String>();
		if( resourceKeyTemplatePlugins != null )
			keyTemplatePlugins.putAll( resourceKeyTemplatePlugins );
		if( documentKeyTemplatePlugins != null )
			keyTemplatePlugins.putAll( documentKeyTemplatePlugins );

		// Group variables together per plugin
		Map<String, Set<String>> plugins = new HashMap<String, Set<String>>();
		List<String> variableNames = template.getVariableNames();
		for( Map.Entry<String, String> entry : keyTemplatePlugins.entrySet() )
		{
			String name = entry.getKey();
			String documentName = entry.getValue();

			if( variableNames.contains( name ) )
			{
				Set<String> variables = plugins.get( documentName );
				if( variables == null )
				{
					variables = new HashSet<String>();
					plugins.put( documentName, variables );
				}

				variables.add( name );
			}
		}

		// Call plugins
		if( !plugins.isEmpty() )
		{
			for( Map.Entry<String, Set<String>> entry : plugins.entrySet() )
			{
				String documentName = entry.getKey();
				String[] variableNamesArray = entry.getValue().toArray( new String[] {} );

				DelegatedCachingKeyTemplatePlugin plugin = new DelegatedCachingKeyTemplatePlugin( documentName, resource.getContext() );
				plugin.handleInterpolation( variableNamesArray );
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
	 * @param parserName
	 *        The parser to use, or null for the default parser
	 * @param conversationService
	 *        The conversation service
	 * @param encoding
	 *        The encoding
	 * @return The cache key or null
	 */
	public String castKey( DocumentDescriptor<Executable> documentDescriptor, String suffix, String parserName, ResourceConversationServiceBase<R> conversationService, Encoding encoding )
	{
		Executable executable = documentDescriptor.getDocument();
		String cacheKeyTemplate = getKeyTemplate( executable, suffix );
		if( cacheKeyTemplate == null )
			return null;

		Request request = resource.getRequest();
		Response response = resource.getResponse();

		if( !ProgramParser.NAME.equals( parserName ) )
		{
			// Set initial media type according to the document's tag (might
			// be used by resolver)
			if( conversationService.getMediaType() == null )
				conversationService.setMediaTypeExtension( documentDescriptor.getTag() );
		}

		// Template and its resolver
		Template template = new Template( cacheKeyTemplate );
		CachingKeyTemplateResolver<R> resolver = new CachingKeyTemplateResolver<R>( documentDescriptor, resource, conversationService, encoding, request, response );

		// Cache key template plugins
		callKeyTemplatePlugins( template, executable, suffix );

		Reference captiveReference = CapturingRedirector.getCapturedReference( request );
		Reference resourceReference = request.getResourceRef();

		try
		{
			// Temporarily use captive reference as the resource reference
			if( captiveReference != null )
				request.setResourceRef( captiveReference );

			// Cast it
			return template.format( resolver );
		}
		finally
		{
			// Return to original reference
			if( captiveReference != null )
				request.setResourceRef( resourceReference );
		}
	}

	/**
	 * Sets the client-side caching headers according to the configured caching
	 * mode.
	 * 
	 * @param representation
	 *        The representation
	 * @param response
	 *        The response
	 */
	public void setClientCachingHeaders( Representation representation, Response response )
	{
		List<CacheDirective> cacheDirectives = response.getCacheDirectives();
		switch( attributes.getClientCachingMode() )
		{
			case CLIENT_CACHING_MODE_DISABLED:
			{
				// Remove all conditional and offline caching headers,
				// explicitly setting "no-cache"
				representation.setModificationDate( null );
				representation.setExpirationDate( null );
				representation.setTag( null );
				cacheDirectives.clear();
				cacheDirectives.add( CacheDirective.noCache() );
				break;
			}

			case CLIENT_CACHING_MODE_CONDITIONAL:
				// Leave conditional headers intact, but remove offline
				// caching headers, explicitly setting "no-cache"
				representation.setExpirationDate( null );
				cacheDirectives.clear();
				cacheDirectives.add( CacheDirective.noCache() );
				break;

			case CLIENT_CACHING_MODE_OFFLINE:
			{
				// Add offline caching headers based on conditional
				// headers
				Date expirationDate = representation.getExpirationDate();
				if( expirationDate != null )
				{
					long maxAge = ( expirationDate.getTime() - System.currentTimeMillis() );
					if( maxAge > 0 )
					{
						long maxClientCachingDuration = attributes.getMaxClientCachingDuration();
						if( maxClientCachingDuration != -1L )
							// Limit the cache duration
							maxAge = Math.min( maxAge, maxClientCachingDuration );

						cacheDirectives.clear();
						cacheDirectives.add( CacheDirective.maxAge( (int) ( maxAge / 1000L ) ) );
					}
				}
				break;
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
	 * @param suffix
	 *        The optional attribute suffix
	 */
	public void addDebugHeaders( String event, CacheEntry cacheEntry, String cacheKey, Executable executable, String suffix )
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

		Set<String> cacheTags = getTags( executable, suffix, false );
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
	 * Cache only GET attribute for an {@link Executable}.
	 */
	private static final String CACHE_ONLY_GET_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".cacheOnlyGet";

	/**
	 * Cache key template attribute for an {@link Executable}.
	 */
	private static final String CACHE_KEY_TEMPLATE_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".cacheKeyTemplate";

	/**
	 * Cache key template plugins attribute for an {@link Executable}.
	 */
	private static final String CACHE_KEY_TEMPLATE_PLUGINS_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".cacheKeyTemplatePlugins";

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
	 * Cache key for encoding attribute for a {@link Request}.
	 */
	private static final String CACHE_KEY_FOR_ENCODING_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".cacheKeyForEncoding";

	/**
	 * Valid cache entry attribute for a {@link Request}.
	 */
	private static final String VALID_CACHE_ENTRY_ATTRIBUTE = CachingUtil.class.getCanonicalName() + ".validCacheEntry";

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
