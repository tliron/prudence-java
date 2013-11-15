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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Request;
import org.restlet.data.Encoding;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.prudence.cache.CacheEntry;
import com.threecrickets.prudence.internal.CachingUtil;
import com.threecrickets.prudence.internal.CaptureWriter;
import com.threecrickets.prudence.internal.GeneratedTextDeferredRepresentation;
import com.threecrickets.prudence.internal.attributes.GeneratedTextResourceAttributes;
import com.threecrickets.prudence.util.PrudenceScriptletPlugin;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * Document service exposed to executables.
 * 
 * @author Tal Liron
 */
public class GeneratedTextResourceDocumentService extends ResourceDocumentServiceBase<GeneratedTextResource, GeneratedTextResourceAttributes, GeneratedTextResourceConversationService>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 * @param executionContext
	 *        The execution context
	 * @param entity
	 *        The entity
	 * @param preferences
	 *        The negotiated client preferences or null
	 */
	public GeneratedTextResourceDocumentService( GeneratedTextResource resource, ExecutionContext executionContext, Representation entity, Variant preferences,
		CachingUtil<GeneratedTextResource, GeneratedTextResourceAttributes> cachingUtil )
	{
		super( resource, resource.getAttributes(), new GeneratedTextResourceConversationService( resource, entity, preferences, resource.getAttributes().getDefaultCharacterSet() ), cachingUtil );
		this.executionContext = executionContext;
	}

	/**
	 * Construction by cloning, with new execution context (for deferred
	 * execution).
	 * 
	 * @param documentService
	 *        The document service to clone
	 */
	public GeneratedTextResourceDocumentService( GeneratedTextResourceDocumentService documentService )
	{
		super( documentService.resource, documentService.attributes, documentService.conversationService, documentService.cachingUtil );
		pushDocumentDescriptor( documentService.getDescriptor() );
		executionContext = new ExecutionContext();
		attributes.addLibraryLocations( executionContext );

		// Initialize execution context
		executionContext.getServices().put( attributes.getDocumentServiceName(), this );
		executionContext.getServices().put( attributes.getApplicationServiceName(), applicationService );
		executionContext.getServices().put( attributes.getConversationServiceName(), conversationService );

		conversationService.isDeferred = true;
	}

	//
	// Attributes
	//

	/**
	 * Whether to cache non-idempotent requests. Defaults to true,
	 * 
	 * @return Whether to cache non-idempotent requests
	 */
	public boolean getCacheNonIdempotent()
	{
		Boolean cacheNonIdempotent = (Boolean) getDescriptor().getDocument().getAttributes().get( CACHE_NON_IDEMPOTENT_ATTRIBUTE );
		return cacheNonIdempotent == null ? true : cacheNonIdempotent;
	}

	/**
	 * @param cacheNonIdempotent
	 *        Whether to cache non-idempotent requests
	 * @see #getCacheNonIdempotent()
	 */
	public void setCacheNonIdempotent( boolean cacheNonIdempotent )
	{
		getDescriptor().getDocument().getAttributes().put( CACHE_NON_IDEMPOTENT_ATTRIBUTE, cacheNonIdempotent );
	}

	//
	// Operations
	//

	/**
	 * Includes a text document into the current location. The document may be a
	 * "text-with-scriptlets" executable, in which case its output could be
	 * dynamically generated.
	 * 
	 * @param documentName
	 *        The document name
	 * @return A representation of the document's output
	 * @throws ParsingException
	 * @throws ExecutionException
	 * @throws DocumentException
	 * @throws IOException
	 */
	public Representation include( String documentName ) throws ParsingException, ExecutionException, DocumentException, IOException
	{
		return include( documentName, false );
	}

	/**
	 * Includes a text document into the current location. The document may be a
	 * "text-with-scriptlets" executable, in which case its output could be
	 * dynamically generated.
	 * 
	 * @param documentName
	 *        The document name
	 * @param includeExtraSources
	 *        Whether to force looking for the document in the extra document
	 *        sources (otherwise is only allowed for non-initial documents)
	 * @return A representation of the document's output
	 * @throws ParsingException
	 * @throws ExecutionException
	 * @throws DocumentException
	 * @throws IOException
	 */
	public Representation include( String documentName, boolean includeExtraSources ) throws ParsingException, ExecutionException, DocumentException, IOException
	{
		documentName = attributes.validateDocumentName( documentName, attributes.getDefaultIncludedName() );

		// This will be null if we're the initial document
		DocumentDescriptor<Executable> currentDocumentDescriptor = getDescriptor();

		DocumentDescriptor<Executable> documentDescriptor = null;
		boolean allowEncoding = false;

		// For initial documents, see if a document descriptor is cached for us
		// in the request
		if( currentDocumentDescriptor == null )
		{
			documentDescriptor = CachingUtil.getExistingDocumentDescriptor( resource.getRequest(), true );
			allowEncoding = true;
		}

		if( documentDescriptor == null )
			documentDescriptor = attributes.createDocumentOnce( documentName, true, true, includeExtraSources || ( currentDocumentDescriptor != null ), false );

		if( currentDocumentDescriptor == null )
		{
			// Set initial media type according to the document's tag
			if( conversationService.getMediaType() == null )
				conversationService.setMediaTypeExtension( documentDescriptor.getTag() );
		}
		else
		{
			// Add dependency
			currentDocumentDescriptor.getDependencies().add( documentDescriptor );
		}

		// Execute
		pushDocumentDescriptor( documentDescriptor );
		try
		{
			return generateText( documentDescriptor, allowEncoding );
		}
		finally
		{
			popDocumentDescriptor();
		}
	}

	/**
	 * Start capturing the generated text output, until {@link #endCapture()} is
	 * called. The captured text will automatically be stored as a string in a
	 * conversation.local.
	 * <p>
	 * Note that captures can be nested, but you do need to call endCapture as
	 * many times as you called startCapture if you want the regular output to
	 * continue.
	 * 
	 * @param name
	 *        The name of the conversation.local into which the captured text
	 *        will go
	 * @see PrudenceScriptletPlugin
	 */
	public void startCapture( String name )
	{
		getWriterStack().add( 0, executionContext.getWriter() );
		executionContext.setWriter( new CaptureWriter( name ) );
	}

	/**
	 * Ends a capture started with {@link #startCapture(String)}, storing the
	 * captured text in a conversation.local, as well as returning it.
	 * <p>
	 * Note that captures can be nested, but you do need to call endCapture as
	 * many times as you called startCapture if you want the regular output to
	 * continue.
	 * 
	 * @return The captured text
	 * @see PrudenceScriptletPlugin
	 */
	public String endCapture()
	{
		Writer currentWriter = executionContext.getWriter();
		if( currentWriter instanceof CaptureWriter )
		{
			CaptureWriter captureWriter = (CaptureWriter) currentWriter;

			Writer lastWriter = getWriterStack().remove( 0 );
			if( lastWriter != null )
				executionContext.setWriter( lastWriter );

			String r = captureWriter.toString();
			ConcurrentMap<String, Object> attributes = resource.getRequest().getAttributes();
			Object existing = attributes.get( captureWriter.name );
			if( existing != null )
				r = existing.toString() + r;
			attributes.put( captureWriter.name, r );
			return r;
		}

		return null;
	}

	/**
	 * Casts the cache key pattern for the current executable and encoding.
	 * 
	 * @return The cache key or null
	 */
	public String getCacheKey()
	{
		return CachingUtil.getCacheKeyForEncoding( cachingUtil.castCacheKey( getDescriptor(), true, conversationService ), conversationService.getEncoding() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected DocumentDescriptor<Executable> getDocumentDescriptor( String documentName ) throws ParsingException, DocumentException
	{
		documentName = attributes.validateDocumentName( documentName );
		return attributes.createDocumentOnce( documentName, false, false, false, true );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Writer stack attribute for an {@link Request}.
	 */
	private static final String WRITER_STACK_ATTRIBUTE = GeneratedTextResourceDocumentService.class.getCanonicalName() + ".writerStack";

	/**
	 * Cache non-idempotent attribute for an {@link Executable}.
	 */
	private static final String CACHE_NON_IDEMPOTENT_ATTRIBUTE = GeneratedTextResourceDocumentService.class.getCanonicalName() + ".cacheNonIdempotent";

	/**
	 * The application service.
	 */
	private final ApplicationService applicationService = ApplicationService.create();

	/**
	 * The execution context.
	 */
	private final ExecutionContext executionContext;

	/**
	 * Buffer used for caching.
	 */
	private StringBuffer writerBuffer;

	/**
	 * The writer stack used for nesting in {@link #startCapture(String)} and
	 * {@link #endCapture()}.
	 * 
	 * @return The writer stack
	 */
	@SuppressWarnings("unchecked")
	private CopyOnWriteArrayList<Writer> getWriterStack()
	{
		ConcurrentMap<String, Object> attributes = resource.getRequest().getAttributes();
		CopyOnWriteArrayList<Writer> writerStack = (CopyOnWriteArrayList<Writer>) attributes.get( WRITER_STACK_ATTRIBUTE );
		if( writerStack == null )
		{
			writerStack = new CopyOnWriteArrayList<Writer>();
			attributes.put( WRITER_STACK_ATTRIBUTE, writerStack );
		}
		return writerStack;
	}

	/**
	 * Copies the cache tags for the current executable, if it has any, to the
	 * entire executable stack.
	 * 
	 * @param cacheTags
	 *        The cache tags
	 * @return The cleaned cache tags
	 */
	private Set<String> propagateCacheTags( Set<String> cacheTags )
	{
		ArrayList<String> propagatedCacheTags = new ArrayList<String>( cacheTags.size() );
		Set<String> cleanedCacheTags = new HashSet<String>( cacheTags.size() );

		for( String cacheTag : cacheTags )
		{
			// Don't propagate underscored cache tags
			if( cacheTag.startsWith( "_" ) )
				// But remove the underscore...
				cleanedCacheTags.add( cacheTag.substring( 1 ) );
			else
			{
				propagatedCacheTags.add( cacheTag );
				cleanedCacheTags.add( cacheTag );
			}
		}

		if( !propagatedCacheTags.isEmpty() )
		{
			DocumentDescriptor<Executable> currentDocumentDescriptor = popDocumentDescriptor();
			for( DocumentDescriptor<Executable> documentDescriptor : documentDescriptorStack )
				CachingUtil.getCacheTags( documentDescriptor.getDocument(), true ).addAll( propagatedCacheTags );
			pushDocumentDescriptor( currentDocumentDescriptor );
		}

		return cleanedCacheTags;
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
	 * @param writer
	 *        The writer
	 * @return The representation
	 * @throws IOException
	 */
	private Representation reencode( CacheEntry cacheEntry, Encoding encoding, String cacheKey, Executable executable, Writer writer ) throws IOException
	{
		if( ( cacheEntry.getEncoding() == null ) && ( encoding != null ) && ( cacheEntry.getSize() >= attributes.getEncodeSizeThreshold() ) )
		{
			// Re-encode it
			cacheEntry = new CacheEntry( cacheEntry, encoding );

			// Cache re-encoded entry
			Cache cache = attributes.getCache();
			if( cache != null )
			{
				cacheKey = CachingUtil.getCacheKeyForEncoding( cacheKey, encoding );
				Set<String> cacheTags = CachingUtil.getCacheTags( executable, false );
				if( cacheTags != null )
					cacheEntry.setTags( cacheTags.toArray( new String[] {} ) );
				cache.store( cacheKey, cacheEntry );
			}
		}

		// We want to write this, too, for includes
		if( ( writer != null ) && ( cacheEntry.getString() != null ) )
			writer.write( cacheEntry.getString() );

		cachingUtil.addCachingDebugHeaders( "hit", cacheEntry, cacheKey, executable );

		return cacheEntry.represent();
	}

	/**
	 * Generates and possibly caches a textual representation. The returned
	 * representation is either a {@link StringRepresentation}, a
	 * {@link ByteArrayRepresentation} or a
	 * {@link GeneratedTextDeferredRepresentation}. Text in the former cases
	 * could be the result of either execution or retrieval from the cache.
	 * 
	 * @param documentDescriptor
	 *        The document descriptor
	 * @param allowEncoding
	 *        Whether to allow encoding
	 * @return A representation, either generated by the executable or fetched
	 *         from the cache
	 * @throws IOException
	 * @throws ParsingException
	 * @throws ExecutionException
	 */
	private Representation generateText( DocumentDescriptor<Executable> documentDescriptor, boolean allowEncoding ) throws IOException, ParsingException, ExecutionException
	{
		Executable executable = documentDescriptor.getDocument();
		Writer writer = executionContext.getWriter();
		Encoding encoding = allowEncoding ? conversationService.getEncoding() : null;

		// Optimized handling for pure literals
		String pureLiteral = executable.getAsPureLiteral();
		if( pureLiteral != null )
		{
			// We want to write this, too, for includes
			if( writer != null )
				writer.write( pureLiteral );

			return new CacheEntry( pureLiteral, conversationService.getMediaType(), conversationService.getLanguage(), conversationService.getCharacterSet(), encoding, conversationService.getHeaders(),
				executable.getDocumentTimestamp(), CachingUtil.getExpirationTimestamp( executable ) ).represent();
		}

		int startPosition = 0;
		Request request = resource.getRequest();

		// Make sure we have a valid writer if not deferred
		if( !conversationService.isDeferred )
		{
			if( writer == null )
			{
				StringWriter stringWriter = new StringWriter();
				writerBuffer = stringWriter.getBuffer();
				writer = new BufferedWriter( stringWriter );
				executionContext.setWriter( writer );
			}
			else
			{
				writer.flush();
				startPosition = writerBuffer.length();
			}

			// See if a valid cache entry has already been cached in the request
			CacheEntry cacheEntry = CachingUtil.getExistingCacheEntry( request, true );
			String cacheKey = CachingUtil.getExistingCacheKey( request, true );
			if( ( cacheEntry != null ) && ( cacheKey != null ) )
				return reencode( cacheEntry, encoding, cacheKey, executable, writer );

			// Attempt to use cache for idempotent requests
			if( request.getMethod().isIdempotent() || getCacheNonIdempotent() )
			{
				cacheKey = cachingUtil.castCacheKey( documentDescriptor, true, conversationService );
				if( cacheKey != null )
				{
					Cache cache = this.attributes.getCache();
					if( cache != null )
					{
						// Try cache key for encoding first
						String cacheKeyForEncoding = CachingUtil.getCacheKeyForEncoding( cacheKey, encoding );
						cacheEntry = cache.fetch( cacheKeyForEncoding );
						if( cacheEntry == null )
							cacheEntry = cache.fetch( cacheKey );

						// Make sure the document is not newer than the cache
						// entry
						if( ( cacheEntry != null ) && ( executable.getDocumentTimestamp() <= cacheEntry.getDocumentModificationDate().getTime() ) )
							return reencode( cacheEntry, encoding, cacheKey, executable, writer );
					}
				}
			}
		}

		setCacheDuration( 0 );
		setCacheKeyPattern( attributes.getDefaultCacheKeyPattern() );
		getCacheTags().clear();

		try
		{
			executionContext.setWriter( writer );
			executionContext.getServices().put( attributes.getDocumentServiceName(), this );
			executionContext.getServices().put( attributes.getApplicationServiceName(), applicationService );
			executionContext.getServices().put( attributes.getConversationServiceName(), conversationService );

			// Execute!
			executable.execute( executionContext, this, attributes.getExecutionController() );

			// Propagate cache tags up the stack
			Set<String> cacheTags = CachingUtil.getCacheTags( executable, false );
			if( ( cacheTags != null ) && !cacheTags.isEmpty() )
				cacheTags = propagateCacheTags( cacheTags );

			// Did the executable ask us to defer?
			if( conversationService.defer )
			{
				conversationService.defer = false;

				// Note that this will cause the executable to execute
				// again!
				GeneratedTextResourceDocumentService documentService = new GeneratedTextResourceDocumentService( this );
				return new GeneratedTextDeferredRepresentation( documentService.resource, executable, documentService.executionContext, documentService, documentService.conversationService );
			}

			if( conversationService.isDeferred )
			{
				// Nothing to return in deferred mode
				return null;
			}
			else
			{
				writer.flush();

				long expirationTimestamp = CachingUtil.getExpirationTimestamp( executable );

				// Get the buffer from when we executed the executable
				CacheEntry cacheEntry = new CacheEntry( writerBuffer.substring( startPosition ), conversationService.getMediaType(), conversationService.getLanguage(), conversationService.getCharacterSet(), null,
					conversationService.getHeaders(), executable.getDocumentTimestamp(), expirationTimestamp );

				// Disable encoding for small representations
				if( cacheEntry.getSize() < attributes.getEncodeSizeThreshold() )
					encoding = null;

				// Encoded version
				CacheEntry encodedCacheEntry = new CacheEntry( cacheEntry, encoding );

				// Cache successful idempotent requests
				if( ( expirationTimestamp > 0 ) && resource.getResponse().getStatus().isSuccess() && ( request.getMethod().isIdempotent() || getCacheNonIdempotent() ) )
				{
					String cacheKey = cachingUtil.castCacheKey( documentDescriptor, true, conversationService );
					if( cacheKey != null )
					{
						// Cache!
						Cache cache = attributes.getCache();
						if( cache != null )
						{
							String[] tags = null;
							if( cacheTags != null )
								tags = cacheTags.toArray( new String[] {} );

							String cacheKeyForEncoding = CachingUtil.getCacheKeyForEncoding( cacheKey, encoding );
							encodedCacheEntry.setTags( tags );
							cache.store( cacheKeyForEncoding, encodedCacheEntry );

							// Cache un-encoded entry separately
							if( encoding != null )
							{
								cacheEntry.setTags( tags );
								cache.store( cacheKey, cacheEntry );
							}

							cachingUtil.addCachingDebugHeaders( "miss", encodedCacheEntry, cacheKey, executable );
						}
					}
				}

				// Make sure we're including the entire buffer
				if( startPosition > 0 )
					encodedCacheEntry = new CacheEntry( encodedCacheEntry, writerBuffer.toString() );

				return encodedCacheEntry.represent();
			}
		}
		catch( ExecutionException x )
		{
			// Did the executable ask us to defer?
			if( conversationService.defer )
			{
				// Note that we will allow exceptions in an executable that
				// ask us to defer! In fact, throwing an exception is a good
				// way for the executable to signal that it's done and is
				// ready to defer.

				conversationService.defer = false;

				// Note that this will cause the executable to run again!
				GeneratedTextResourceDocumentService documentService = new GeneratedTextResourceDocumentService( this );
				return new GeneratedTextDeferredRepresentation( documentService.resource, executable, documentService.executionContext, documentService, documentService.conversationService );
			}
			else
				throw x;
		}
		finally
		{
			writer.flush();
			executionContext.getErrorWriterOrDefault().flush();
		}
	}
}