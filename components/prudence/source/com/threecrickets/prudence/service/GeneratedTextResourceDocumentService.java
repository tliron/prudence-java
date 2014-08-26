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

package com.threecrickets.prudence.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Request;
import org.restlet.data.Encoding;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;

import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.cache.CacheEntry;
import com.threecrickets.prudence.internal.CachingUtil;
import com.threecrickets.prudence.internal.GeneratedTextDeferredRepresentation;
import com.threecrickets.prudence.internal.attributes.GeneratedTextResourceAttributes;
import com.threecrickets.prudence.util.CaptureWriter;
import com.threecrickets.prudence.util.PrudenceScriptletPlugin;
import com.threecrickets.prudence.util.StackedWriter;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.scripturian.parser.ProgramParser;
import com.threecrickets.scripturian.parser.ScriptletsParser;

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
	 * @param cachingUtil
	 *        The caching utilities
	 */
	public GeneratedTextResourceDocumentService( GeneratedTextResource resource, ExecutionContext executionContext, Representation entity, Variant preferences,
		CachingUtil<GeneratedTextResource, GeneratedTextResourceAttributes> cachingUtil )
	{
		super( resource, resource.getAttributes(), new GeneratedTextResourceConversationService( resource, entity, preferences, resource.getAttributes().getDefaultCharacterSet() ), cachingUtil );
		this.executionContext = executionContext;
		cachingService = new GeneratedTextResourceCachingService( resource, this, conversationService, cachingUtil );
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
		cachingService = new GeneratedTextResourceCachingService( resource, this, conversationService, cachingUtil );

		// Initialize execution context
		executionContext.getServices().put( attributes.getDocumentServiceName(), this );
		executionContext.getServices().put( attributes.getCachingServiceName(), cachingService );
		executionContext.getServices().put( attributes.getApplicationServiceName(), applicationService );
		executionContext.getServices().put( attributes.getConversationServiceName(), conversationService );

		conversationService.isDeferred = true;
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
	 *         In case of a Scripturian parsing error
	 * @throws ExecutionException
	 *         In case of a Scripturian execution error
	 * @throws DocumentException
	 *         In case of a Scripturian document retrieval error
	 * @throws IOException
	 *         In case of a Scripturian writing or cache entry compression error
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
	 *         In case of a Scripturian parsing error
	 * @throws ExecutionException
	 *         In case of a Scripturian execution error
	 * @throws DocumentException
	 *         In case of a Scripturian document retrieval error
	 * @throws IOException
	 *         In case of a Scripturian writing or cache entry compression error
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
			documentDescriptor = attributes.createDocumentOnce( documentName, ScriptletsParser.NAME, true, includeExtraSources || ( currentDocumentDescriptor != null ), false );

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
			Representation representation = generateText( documentDescriptor, allowEncoding );
			representation.setDisposition( conversationService.getDisposition() );
			return representation;
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
		getStackedWriter().push( new CaptureWriter( name ) );
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
		Writer currentWriter = getStackedWriter().pop();
		if( currentWriter instanceof CaptureWriter )
		{
			CaptureWriter captureWriter = (CaptureWriter) currentWriter;
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

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	@Override
	protected DocumentDescriptor<Executable> getDocumentDescriptor( String documentName ) throws ParsingException, DocumentException
	{
		documentName = attributes.validateDocumentName( documentName );
		return attributes.createDocumentOnce( documentName, ProgramParser.NAME, false, false, true );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Writer stack attribute for an {@link Request}.
	 */
	private static final String STACKED_WRITER_ATTRIBUTE = GeneratedTextResourceDocumentService.class.getCanonicalName() + ".stackedWriter";

	/**
	 * The application service.
	 */
	private final ApplicationService applicationService = ApplicationService.create();

	/**
	 * The caching service.
	 */
	private final GeneratedTextResourceCachingService cachingService;

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
	private StackedWriter getStackedWriter()
	{
		ConcurrentMap<String, Object> attributes = resource.getRequest().getAttributes();
		StackedWriter writerStack = (StackedWriter) attributes.get( STACKED_WRITER_ATTRIBUTE );
		if( writerStack == null )
		{
			writerStack = new StackedWriter();
			attributes.put( STACKED_WRITER_ATTRIBUTE, writerStack );
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
				CachingUtil.getTags( documentDescriptor.getDocument(), null, true ).addAll( propagatedCacheTags );
			pushDocumentDescriptor( currentDocumentDescriptor );
		}

		return cleanedCacheTags;
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
	 * @throws ParsingException
	 *         In case of a Scripturian parsing error
	 * @throws ExecutionException
	 *         In case of a Scripturian execution error
	 * @throws IOException
	 *         In case of a Scripturian writing or cache entry compression error
	 */
	private Representation generateText( DocumentDescriptor<Executable> documentDescriptor, boolean allowEncoding ) throws ParsingException, ExecutionException, IOException
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

			return new CacheEntry( pureLiteral, conversationService.getMediaType(), conversationService.getLanguage(), conversationService.getCharacterSet(), encoding, conversationService.getResponseHeaders(), 0L, null,
				CachingUtil.getExpirationTimestamp( executable, null ), executable.getDocumentTimestamp() ).represent();
		}

		int startPosition = 0;
		Request request = resource.getRequest();

		// Make sure we have a valid writer if not deferred
		if( !conversationService.isDeferred )
		{
			if( writer == null )
			{
				StringWriter stringWriter = new StringWriter();
				Writer bufferedWriter = new BufferedWriter( stringWriter );
				writerBuffer = stringWriter.getBuffer();
				StackedWriter stackedWriter = getStackedWriter();
				stackedWriter.push( bufferedWriter );
				executionContext.setWriter( stackedWriter );
				writer = stackedWriter;
			}
			else
			{
				writer.flush();
				startPosition = writerBuffer.length();
			}

			// Try fetching from cache
			if( CachingUtil.mayFetch( request, executable, null ) )
			{
				Representation representation = cachingUtil.fetchRepresentation( documentDescriptor, null, ScriptletsParser.NAME, request, encoding, writer, conversationService );
				if( representation != null )
					return representation;
			}
		}

		// Reset caching attributes
		CachingUtil.setDuration( executable, null, 0 );
		CachingUtil.setOnlyGet( executable, null, false );
		CachingUtil.setKeyTemplate( executable, null, attributes.getDefaultCachingKeyTemplate() );
		CachingUtil.getTags( executable, null, true ).clear();

		try
		{
			executionContext.getServices().put( attributes.getDocumentServiceName(), this );
			executionContext.getServices().put( attributes.getCachingServiceName(), cachingService );
			executionContext.getServices().put( attributes.getApplicationServiceName(), applicationService );
			executionContext.getServices().put( attributes.getConversationServiceName(), conversationService );

			// Execute!
			executable.execute( executionContext, this, attributes.getExecutionController() );

			// Propagate cache tags up the stack
			Set<String> cacheTags = CachingUtil.getTags( executable, null, false );
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

				long expirationTimestamp = CachingUtil.getExpirationTimestamp( executable, null );

				// Get the buffer from when we executed the executable
				CacheEntry cacheEntry = new CacheEntry( writerBuffer.substring( startPosition ), conversationService.getMediaType(), conversationService.getLanguage(), conversationService.getCharacterSet(), null,
					conversationService.getResponseHeaders(), 0L, null, expirationTimestamp, executable.getDocumentTimestamp() );

				// Disable encoding for small representations
				if( cacheEntry.getSize() < attributes.getEncodeSizeThreshold() )
					encoding = null;

				// Encoded version?
				CacheEntry encodedCacheEntry = encoding != null ? new CacheEntry( cacheEntry, encoding ) : cacheEntry;

				// Cache successful requests
				if( ( expirationTimestamp > 0 ) && resource.getResponse().getStatus().isSuccess() )
					cachingUtil.store( encodedCacheEntry, cacheEntry, documentDescriptor, null, ScriptletsParser.NAME, cacheTags, conversationService );

				// Make sure we're including the entire buffer for the
				// representation
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