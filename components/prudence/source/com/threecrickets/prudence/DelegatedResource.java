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

package com.threecrickets.prudence;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.representation.CharacterRepresentation;
import org.restlet.representation.ObjectRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.RepresentationInfo;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.prudence.cache.CacheEntry;
import com.threecrickets.prudence.internal.CachingUtil;
import com.threecrickets.prudence.internal.JygmentsDocumentFormatter;
import com.threecrickets.prudence.internal.attributes.DelegatedResourceAttributes;
import com.threecrickets.prudence.service.ApplicationService;
import com.threecrickets.prudence.service.ConversationStoppedException;
import com.threecrickets.prudence.service.DelegatedResourceCachingService;
import com.threecrickets.prudence.service.DelegatedResourceConversationService;
import com.threecrickets.prudence.service.DelegatedResourceDocumentService;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentFormatter;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.DocumentNotFoundException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * A Restlet resource that delegates functionality to a Scripturian
 * {@link Executable} via entry points. The entry points must be global
 * functions, closures, or whatever other technique the language engine uses to
 * for entry points.
 * <p>
 * Supported entry points are:
 * <ul>
 * <li><code>handleInit()</code></li>
 * <li><code>handleGet()</code></li>
 * <li><code>handleGetInfo()</code></li>
 * <li><code>handlePost()</code></li>
 * <li><code>handlePut()</code></li>
 * <li><code>handleDelete()</code></li>
 * <li><code>handleOptions()</code></li>
 * </ul>
 * <p>
 * A <code>conversation</code> service is sent as an argument to all entry
 * points. Additionally, <code>document</code>, <code>caching</code>, and
 * <code>application</code> services are available as global variables. See
 * {@link DelegatedResourceConversationService},
 * {@link DelegatedResourceDocumentService},
 * {@link DelegatedResourceCachingService}, and {@link ApplicationService}.
 * <p>
 * Before using this resource, make sure to configure a valid document source in
 * the application's {@link Context} as
 * <code>com.threecrickets.prudence.DelegatedResource.documentSource</code>.
 * This document source is exposed to the executable as
 * <code>document.source</code>.
 * <p>
 * For a simpler delegate, see {@link DelegatedHandler}.
 * <p>
 * Summary of settings configured via the application's {@link Context}:
 * <ul>
 * <li>
 * <code>com.threecrickets.prudence.cache:</code> {@link Cache}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.applicationServiceName</code>
 * : Defaults to "application".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.cacheKeyPatternHandlers</code>
 * : {@link ConcurrentMap}&lt;String, String&gt;</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.cachingServiceName</code>
 * : Defaults to "caching".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.defaultCacheKeyPattern:</code>
 * {@link String}, defaults to "{ri}|{dn}".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.defaultCharacterSet:</code>
 * {@link CharacterSet}, defaults to {@link CharacterSet#UTF_8}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.defaultLanguageTag:</code>
 * {@link String}, defaults to "javascript".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.debug:</code>
 * {@link Boolean}, defaults to false.</li>
 * <li><code>com.threecrickets.prudence.DelegatedResource.defaultName:</code>
 * {@link String}, defaults to "default".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.documentFormatter:</code>
 * {@link DocumentFormatter}. Defaults to a {@link JygmentsDocumentFormatter}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.documentServiceName</code>
 * : Defaults to "document".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.documentSource:</code>
 * {@link DocumentSource}. <b>Required.</b></li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.encodeSizeThreshold:</code>
 * {@link Integer}, defaults to 1024.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForDelete:</code>
 * {@link String}, defaults to "handleDelete".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForGet:</code>
 * {@link String}, defaults to "handleGet".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForGetInfo:</code>
 * {@link String}, defaults to "handleGetInfo".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForInit:</code>
 * {@link String}, defaults to "handleInit".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForOptions:</code>
 * {@link String}, defaults to "handleOptions".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForPost:</code>
 * {@link String}, defaults to "handlePost".</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.entryPointNameForPut:</code>
 * {@link String}, defaults to "handlePut".</li>
 * <li><code>com.threecrickets.prudence.DelegatedResource.errorWriter:</code>
 * {@link Writer}, defaults to standard error.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.executionController:</code>
 * {@link ExecutionController}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.fileUploadDirectory:</code>
 * {@link File}. Defaults to "uploads" under the application root.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.fileUploadSizeThreshold:</code>
 * {@link Integer}, defaults to zero.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatesResource.libraryDocumentSources:</code>
 * {@link Iterable} of {@link DocumentSource} of {@link Executable}.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.languageManager:</code>
 * {@link LanguageManager}, defaults to a new instance.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.negotiateEncoding:</code>
 * defaults to a true.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.prepare:</code>
 * {@link Boolean}, defaults to true.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.sourceViewable:</code>
 * {@link Boolean}, defaults to false.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.trailingSlashRequired:</code>
 * {@link Boolean}, defaults to true.</li>
 * <li>
 * <code>com.threecrickets.prudence.DelegatedResource.writer:</code>
 * {@link Writer}, defaults to standard output.</li>
 * </ul>
 * <p>
 * <i>"Restlet" is a registered trademark of <a
 * href="http://www.restlet.org/about/legal">Restlet S.A.S.</a>.</i>
 * 
 * @author Tal Liron
 */
public class DelegatedResource extends ServerResource
{
	//
	// Constants
	//

	/**
	 * Document name attribute for a {@link Request}.
	 */
	public static final String DOCUMENT_NAME_ATTRIBUTE = DelegatedResource.class.getCanonicalName() + ".documentName";

	//
	// Attributes
	//

	/**
	 * The attributes as configured in the {@link Application} context.
	 * 
	 * @return The attributes
	 */
	public DelegatedResourceAttributes getAttributes()
	{
		return attributes;
	}

	//
	// ServerResource
	//

	/**
	 * Initializes the resource, and delegates to the <code>handleInit()</code>
	 * entry point in the executable.
	 */
	@Override
	protected void doInit() throws ResourceException
	{
		super.doInit();
		setAnnotated( false );

		if( attributes.isSourceViewable() )
		{
			Request request = getRequest();
			Form query = request.getResourceRef().getQueryAsForm();
			if( TRUE.equals( query.getFirstValue( SOURCE ) ) )
				// Bypass doInit delegation
				return;
		}

		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, null, null, attributes.getDefaultCharacterSet() );
		enter( attributes.getEntryPointNameForInit(), true, conversationService );
	}

	/**
	 * Delegates to the <code>handleGet()</code> entry point in the executable.
	 * 
	 * @return The optional result entity
	 * @throws ResourceException
	 *         In case of a handling error
	 */
	@Override
	public Representation get() throws ResourceException
	{
		return get( null );
	}

	/**
	 * Delegates to the <code>handleGet()</code> entry point in the executable.
	 * 
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 *         In case of a handling error
	 */
	@Override
	public Representation get( Variant variant ) throws ResourceException
	{
		if( attributes.isSourceViewable() )
		{
			Request request = getRequest();
			Form query = request.getResourceRef().getQueryAsForm();
			if( TRUE.equals( query.getFirstValue( SOURCE ) ) )
			{
				String documentName = request.getResourceRef().getRemainingPart( true, false );
				documentName = attributes.validateDocumentName( documentName );
				int lineNumber = -1;
				String line = query.getFirstValue( HIGHLIGHT );
				if( line != null )
				{
					try
					{
						lineNumber = Integer.parseInt( line );
					}
					catch( NumberFormatException x )
					{
					}
				}
				try
				{
					DocumentDescriptor<Executable> documentDescriptor = attributes.getDocumentSource().getDocument( documentName );
					DocumentFormatter<Executable> documentFormatter = attributes.getDocumentFormatter();
					if( documentFormatter != null )
						return new StringRepresentation( documentFormatter.format( documentDescriptor, documentName, lineNumber ), MediaType.TEXT_HTML );
					else
						return new StringRepresentation( documentDescriptor.getSourceCode() );
				}
				catch( DocumentNotFoundException x )
				{
					throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
				}
				catch( DocumentException x )
				{
					throw new ResourceException( x );
				}
			}
		}

		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, null, variant, attributes.getDefaultCharacterSet() );
		Representation cached = fetchCachedRepresentation( conversationService );
		if( cached != null )
			return cached;
		Object r = enter( attributes.getEntryPointNameForGet(), false, conversationService );
		return getRepresentation( r, conversationService );
	}

	/**
	 * Delegates to the <code>handleGetInfo()</code> entry point in the
	 * executable.
	 * 
	 * @return The optional result info
	 * @throws ResourceException
	 *         In case of a handling error
	 */
	@Override
	public RepresentationInfo getInfo() throws ResourceException
	{
		return getInfo( null );
	}

	/**
	 * Delegates to the <code>handleGetInfo()</code> entry point in the
	 * executable.
	 * 
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result info
	 * @throws ResourceException
	 *         In case of a handling error
	 */
	@Override
	public RepresentationInfo getInfo( Variant variant ) throws ResourceException
	{
		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, null, variant, attributes.getDefaultCharacterSet() );

		if( CachingUtil.mayFetch( getRequest(), executable, getDispatchedSuffix() ) )
		{
			CacheEntry cacheEntry = cachingUtil.fetchCacheEntry( getDispatchedSuffix(), false, conversationService );
			if( cacheEntry != null )
				return cacheEntry.getInfo();
		}

		try
		{
			Object r = enter( this.attributes.getEntryPointNameForGetInfo(), false, conversationService );
			return getRepresentationInfo( r, conversationService );
		}
		catch( ResourceException x )
		{
			if( x.getCause() instanceof NoSuchMethodException )
				return get( variant );
			else
				throw x;
		}
	}

	/**
	 * Delegates to the <code>handlePost()</code> entry point in the executable.
	 * 
	 * @param entity
	 *        The posted entity
	 * @return The optional result entity
	 * @throws ResourceException
	 *         In case of a handling error
	 */
	@Override
	public Representation post( Representation entity ) throws ResourceException
	{
		return post( entity, null );
	}

	/**
	 * Delegates to the <code>handlePost()</code> entry point in the executable.
	 * 
	 * @param entity
	 *        The posted entity
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 *         In case of a handling error
	 */
	@Override
	public Representation post( Representation entity, Variant variant ) throws ResourceException
	{
		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, entity, variant, attributes.getDefaultCharacterSet() );
		Representation cached = fetchCachedRepresentation( conversationService );
		if( cached != null )
			return cached;
		Object r = enter( attributes.getEntryPointNameForPost(), false, conversationService );
		return getRepresentation( r, conversationService );
	}

	/**
	 * Delegates to the <code>handlePut()</code> entry point in the executable.
	 * 
	 * @param entity
	 *        The posted entity
	 * @return The optional result entity
	 * @throws ResourceException
	 *         In case of a handling error
	 */
	@Override
	public Representation put( Representation entity ) throws ResourceException
	{
		return put( entity, null );
	}

	/**
	 * Delegates to the <code>handlePut()</code> entry point in the executable.
	 * 
	 * @param entity
	 *        The posted entity
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 *         In case of a handling error
	 */
	@Override
	public Representation put( Representation entity, Variant variant ) throws ResourceException
	{
		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, entity, variant, attributes.getDefaultCharacterSet() );
		Representation cached = fetchCachedRepresentation( conversationService );
		if( cached != null )
			return cached;
		Object r = enter( attributes.getEntryPointNameForPut(), false, conversationService );
		return getRepresentation( r, conversationService );
	}

	/**
	 * Delegates to the <code>handleDelete()</code> entry point in the
	 * executable.
	 * 
	 * @return The optional result entity
	 * @throws ResourceException
	 *         In case of a handling error
	 */
	@Override
	public Representation delete() throws ResourceException
	{
		return delete( null );
	}

	/**
	 * Delegates to the <code>handleDelete()</code> entry point in the
	 * executable.
	 * 
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 *         In case of a handling error
	 */
	@Override
	public Representation delete( Variant variant ) throws ResourceException
	{
		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, null, variant, attributes.getDefaultCharacterSet() );
		Representation cached = fetchCachedRepresentation( conversationService );
		if( cached != null )
			return cached;
		Object r = enter( attributes.getEntryPointNameForDelete(), false, conversationService );
		return getRepresentation( r, conversationService );
	}

	/**
	 * Delegates to the <code>handleOptions()</code> entry point in the
	 * executable.
	 * 
	 * @return The optional result entity
	 * @throws ResourceException
	 *         In case of a handling error
	 */
	@Override
	public Representation options() throws ResourceException
	{
		return options( null );
	}

	/**
	 * Delegates to the <code>handleOptions()</code> entry point in the
	 * executable.
	 * 
	 * @param variant
	 *        The variant of the response entity
	 * @return The optional result entity
	 * @throws ResourceException
	 *         In case of a handling error
	 */
	@Override
	public Representation options( Variant variant ) throws ResourceException
	{
		DelegatedResourceConversationService conversationService = new DelegatedResourceConversationService( this, null, variant, attributes.getDefaultCharacterSet() );
		Representation cached = fetchCachedRepresentation( conversationService );
		if( cached != null )
			return cached;
		Object r = enter( attributes.getEntryPointNameForOptions(), false, conversationService );
		return getRepresentation( r, conversationService );
	}

	@Override
	public void doRelease()
	{
		super.doRelease();
		ExecutionContext.disconnect();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Entry point validity cache attribute for an {@link Executable}.
	 */
	private static final String ENTRY_POINT_VALIDITY_CACHE_ATTRIBUTE = DelegatedResource.class.getCanonicalName() + ".entryPointValidityCache";

	/**
	 * Constant.
	 */
	private static final String SOURCE = "source";

	/**
	 * Constant.
	 */
	private static final String HIGHLIGHT = "highlight";

	/**
	 * Constant.
	 */
	private static final String TRUE = "true";

	/**
	 * The attributes as configured in the {@link Application} context.
	 */
	private final DelegatedResourceAttributes attributes = new DelegatedResourceAttributes( this );

	/**
	 * Caching utilities.
	 */
	private final CachingUtil<DelegatedResource, DelegatedResourceAttributes> cachingUtil = new CachingUtil<DelegatedResource, DelegatedResourceAttributes>( this, attributes );

	/**
	 * The existing document descriptor.
	 */
	private DocumentDescriptor<Executable> documentDescriptor;

	/**
	 * The existing executable.
	 */
	private Executable executable;

	/**
	 * The dispatched suffix.
	 */
	private String dispatchedSuffix;

	/**
	 * Whether we fetched the dispatched suffix.
	 */
	private boolean fetchedDispatchedSuffix;

	/**
	 * The dispatched suffix.
	 * 
	 * @return The dispatched suffix or null
	 */
	private String getDispatchedSuffix()
	{
		if( !fetchedDispatchedSuffix )
		{
			dispatchedSuffix = CachingUtil.getDispatchedSuffix( getRequest() );
			fetchedDispatchedSuffix = true;
		}
		return dispatchedSuffix;
	}

	/**
	 * Returns a representation based on the object. If the object is not
	 * already a representation, creates a new string representation based on
	 * the container's attributes.
	 * 
	 * @param object
	 *        An object
	 * @param conversationService
	 *        The conversation service
	 * @return A representation
	 * @throws ResourceException
	 *         In case of a compression error
	 */
	private Representation getRepresentation( Object object, DelegatedResourceConversationService conversationService ) throws ResourceException
	{
		Response response = getResponse();
		Encoding encoding = conversationService.getEncoding();
		long expirationTimestamp = CachingUtil.getExpirationTimestamp( executable, getDispatchedSuffix() );
		CacheEntry cacheEntry = null;
		Representation representation = null;
		boolean configure = true;

		if( object == null )
		{
			return null;
		}
		else if( object instanceof CharacterRepresentation )
		{
			representation = (Representation) object;
			try
			{
				cacheEntry = new CacheEntry( representation, conversationService.getHeaders(), conversationService.getTag(), executable.getDocumentTimestamp(), expirationTimestamp );
			}
			catch( IOException x )
			{
				throw new ResourceException( x );
			}
			configure = false;
		}
		else if( object instanceof Representation )
		{
			representation = (Representation) object;
			configure = false;
		}
		else if( object instanceof Number )
		{
			// Returning a number means setting the status
			response.setStatus( Status.valueOf( ( (Number) object ).intValue() ) );

			return null;
		}
		else
		{
			MediaType mediaType = conversationService.getMediaType();

			if( MediaType.APPLICATION_JAVA.includes( mediaType ) )
			{
				// Wrap in an object representation
				representation = new ObjectRepresentation<Serializable>( (Serializable) object, mediaType );
				Language language = conversationService.getLanguage();
				if( language != null )
					representation.getLanguages().add( language );
				representation.setCharacterSet( conversationService.getCharacterSet() );
			}
			else if( object instanceof byte[] )
			{
				cacheEntry = new CacheEntry( (byte[]) object, mediaType, conversationService.getLanguage(), conversationService.getCharacterSet(), null, conversationService.getHeaders(), conversationService.getTag(),
					executable.getDocumentTimestamp(), expirationTimestamp );
			}
			else
			{
				// Convert to string
				try
				{
					cacheEntry = new CacheEntry( object.toString(), mediaType, conversationService.getLanguage(), conversationService.getCharacterSet(), null, conversationService.getHeaders(),
						conversationService.getTag(), executable.getDocumentTimestamp(), expirationTimestamp );
				}
				catch( IOException x )
				{
					throw new ResourceException( x );
				}
			}
		}

		if( cacheEntry != null )
		{
			try
			{
				// Encoded version?
				CacheEntry encodedCacheEntry = encoding != null ? new CacheEntry( cacheEntry, encoding ) : cacheEntry;

				// Cache successful requests
				if( ( expirationTimestamp > 0 ) && response.getStatus().isSuccess() )
					cachingUtil
						.storeCacheEntry( encodedCacheEntry, cacheEntry, documentDescriptor, getDispatchedSuffix(), false, CachingUtil.getCacheTags( executable, getDispatchedSuffix(), false ), conversationService );

				cacheEntry = encodedCacheEntry;
			}
			catch( IOException x )
			{
				throw new ResourceException( x );
			}

			configure = false;
		}

		if( representation == null )
			representation = cacheEntry.represent();

		if( configure )
		{
			representation.setTag( conversationService.getTag() );
			representation.setExpirationDate( conversationService.getExpirationDate() );
			representation.setModificationDate( conversationService.getModificationDate() );
			representation.setDisposition( conversationService.getDisposition() );
		}

		return representation;
	}

	/**
	 * Returns a representation info based on the object. If the object is not
	 * already a representation info, creates a new representation info based on
	 * the container's attributes.
	 * 
	 * @param object
	 *        An object
	 * @param conversationService
	 *        The conversation service
	 * @return A representation info
	 */
	private RepresentationInfo getRepresentationInfo( Object object, DelegatedResourceConversationService conversationService )
	{
		RepresentationInfo representationInfo;
		if( object == null )
			return null;
		else if( object instanceof RepresentationInfo )
			return (RepresentationInfo) object;
		else if( object instanceof Date )
		{
			representationInfo = new RepresentationInfo( conversationService.getMediaType(), (Date) object );
			representationInfo.setTag( conversationService.getTag() );
			return representationInfo;
		}
		else if( object instanceof Number )
		{
			representationInfo = new RepresentationInfo( conversationService.getMediaType(), new Date( ( (Number) object ).longValue() ) );
			representationInfo.setTag( conversationService.getTag() );
			return representationInfo;
		}
		else if( object instanceof Tag )
		{
			representationInfo = new RepresentationInfo( conversationService.getMediaType(), (Tag) object );
			representationInfo.setModificationDate( conversationService.getModificationDate() );
			return representationInfo;
		}
		else if( object instanceof String )
		{
			representationInfo = new RepresentationInfo( conversationService.getMediaType(), Tag.parse( (String) object ) );
			representationInfo.setModificationDate( conversationService.getModificationDate() );
			return representationInfo;
		}
		else
			throw new ResourceException( Status.SERVER_ERROR_INTERNAL, "cannot convert " + object.getClass().toString() + " to a RepresentationInfo" );
	}

	/**
	 * A cache for entry point validity.
	 * 
	 * @param executable
	 *        The executable
	 * @return The entry point validity cache
	 */
	private ConcurrentMap<String, Boolean> getEntryPointValidityCache( Executable executable )
	{
		ConcurrentMap<String, Object> attributes = executable.getAttributes();
		@SuppressWarnings("unchecked")
		ConcurrentMap<String, Boolean> entryPointValidityCache = (ConcurrentMap<String, Boolean>) attributes.get( ENTRY_POINT_VALIDITY_CACHE_ATTRIBUTE );
		if( entryPointValidityCache == null )
		{
			entryPointValidityCache = new ConcurrentHashMap<String, Boolean>();
			@SuppressWarnings("unchecked")
			ConcurrentMap<String, Boolean> existing = (ConcurrentMap<String, Boolean>) attributes.putIfAbsent( ENTRY_POINT_VALIDITY_CACHE_ATTRIBUTE, entryPointValidityCache );
			if( existing != null )
				entryPointValidityCache = existing;
		}

		return entryPointValidityCache;
	}

	/**
	 * Attempts to fetch the cached representation.
	 * 
	 * @param conversationService
	 *        The conversation service
	 * @return The cached representation
	 * @throws ResourceException
	 *         In case of a handling error
	 */
	private Representation fetchCachedRepresentation( DelegatedResourceConversationService conversationService ) throws ResourceException
	{
		Request request = getRequest();
		if( CachingUtil.mayFetch( request, executable, getDispatchedSuffix() ) )
			return cachingUtil.fetchRepresentation( documentDescriptor, getDispatchedSuffix(), false, request, conversationService.getEncoding(), null, conversationService );
		return null;
	}

	/**
	 * Enters the executable.
	 * 
	 * @param entryPointName
	 *        Name of entry point
	 * @param isInit
	 *        Whether this is the init entry point
	 * @param conversationService
	 *        The conversation service
	 * @return The result of the entry
	 * @throws ResourceException
	 *         In case of an entering error
	 * @see Executable#enter(Object, String, Object...)
	 */
	private Object enter( String entryPointName, boolean isInit, DelegatedResourceConversationService conversationService ) throws ResourceException
	{
		String documentName = cachingUtil.getValidDocumentName( getRequest() );
		boolean isPassThrough = attributes.getPassThroughDocuments().contains( "/" + documentName );
		ConcurrentMap<String, Boolean> entryPointValidityCache = null;

		try
		{
			documentDescriptor = attributes.createDocumentOnce( documentName, false, true, true, isPassThrough );
			executable = documentDescriptor.getDocument();
			Object enteringKey = getApplication().hashCode();

			DelegatedResourceDocumentService documentService = new DelegatedResourceDocumentService( this, documentDescriptor, conversationService, cachingUtil );

			if( executable.getEnterableExecutionContext( enteringKey ) == null )
			{
				ExecutionContext executionContext = new ExecutionContext( attributes.getWriter(), attributes.getErrorWriter() );
				attributes.addLibraryLocations( executionContext );

				executionContext.getServices().put( attributes.getDocumentServiceName(), documentService );
				executionContext.getServices().put( attributes.getCachingServiceName(), new DelegatedResourceCachingService( this, documentService, conversationService, cachingUtil ) );
				executionContext.getServices().put( attributes.getApplicationServiceName(), ApplicationService.create() );

				try
				{
					if( !executable.makeEnterable( enteringKey, executionContext, this, attributes.getExecutionController() ) )
						executionContext.release();
				}
				catch( ParsingException x )
				{
					executionContext.release();
					throw new ResourceException( x );
				}
				catch( ExecutionException x )
				{
					executionContext.release();
					throw new ResourceException( x );
				}
				catch( IOException x )
				{
					executionContext.release();
					throw new ResourceException( x );
				}
			}

			// Check for validity, if cached
			entryPointValidityCache = getEntryPointValidityCache( executable );
			Boolean isValid = entryPointValidityCache.get( entryPointName );
			if( ( isValid != null ) && !isValid.booleanValue() )
				throw new NoSuchMethodException( entryPointName );

			if( isInit )
			{
				// Reset caching attributes
				String suffix = getDispatchedSuffix();
				CachingUtil.setCacheDuration( executable, suffix, 0 );
				CachingUtil.setCacheOnlyGet( executable, suffix, true );
				CachingUtil.setCacheKeyPattern( executable, suffix, attributes.getDefaultCacheKeyPattern() );
				CachingUtil.getCacheTags( executable, suffix, true ).clear();
			}

			// Enter!
			Object r = executable.enter( enteringKey, entryPointName, conversationService );

			return r;
		}
		catch( DocumentNotFoundException x )
		{
			throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
		}
		catch( DocumentException x )
		{
			throw new ResourceException( x );
		}
		catch( ParsingException x )
		{
			throw new ResourceException( x );
		}
		catch( ExecutionException x )
		{
			if( ConversationStoppedException.isConversationStopped( getRequest() ) )
			{
				getLogger().fine( "conversation.stop() was called" );
				return null;
			}
			else
				throw new ResourceException( x );
		}
		catch( NoSuchMethodException x )
		{
			// We are invalid
			if( entryPointValidityCache != null )
				entryPointValidityCache.put( entryPointName, false );

			throw new ResourceException( x );
		}
		finally
		{
			try
			{
				attributes.getWriter().flush();
				attributes.getErrorWriter().flush();
			}
			catch( IOException x )
			{
			}
		}
	}
}