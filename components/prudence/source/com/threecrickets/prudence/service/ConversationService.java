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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.ClientInfo;
import org.restlet.data.Disposition;
import org.restlet.data.Form;
import org.restlet.data.Language;
import org.restlet.data.LocalReference;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Series;

import com.threecrickets.prudence.DelegatedStatusService;
import com.threecrickets.prudence.util.CapturingRedirector;
import com.threecrickets.prudence.util.ConversationCookie;
import com.threecrickets.prudence.util.FileParameter;
import com.threecrickets.prudence.util.FormWithFiles;

/**
 * Conversation service exposed to executables.
 * 
 * @author Tal Liron
 */
public class ConversationService
{
	//
	// Constants
	//

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param fileUploadSizeThreshold
	 *        The size in bytes beyond which uploaded files will be stored to
	 *        disk
	 * @param fileUploadDirectory
	 *        The directory in which to place uploaded files
	 */
	public ConversationService( int fileUploadSizeThreshold, File fileUploadDirectory )
	{
		this.fileUploadSizeThreshold = fileUploadSizeThreshold;
		this.fileUploadDirectory = fileUploadDirectory;
	}

	//
	// Attributes
	//

	/**
	 * The resource reference.
	 * 
	 * @return The reference
	 */
	public Reference getReference()
	{
		if( reference == null )
		{
			Request request = getRequest();
			reference = CapturingRedirector.getCapturedReference( request );
			if( reference == null )
				reference = request.getResourceRef();
		}
		return reference;
	}

	/**
	 * The resource reference's remaining part (not including the query).
	 * 
	 * @return The remaining part
	 */
	public String getWildcard()
	{
		if( wildcard == null )
			wildcard = getReference().getRemainingPart( true, false );
		return wildcard;
	}

	/**
	 * The conversation cookies.
	 * <p>
	 * This value is cached locally.
	 * 
	 * @return The conversation cookies
	 */
	public Collection<ConversationCookie> getCookies()
	{
		if( conversationCookies == null )
			conversationCookies = ConversationCookie.wrapCookies( getRequest().getCookies(), getResponse().getCookieSettings() );
		return conversationCookies;
	}

	/**
	 * Gets a conversation cookie by name.
	 * 
	 * @param name
	 *        The cookie name
	 * @return The conversation cookie or null
	 */
	public ConversationCookie getCookie( String name )
	{
		for( ConversationCookie cookie : getCookies() )
			if( cookie.getName().equals( name ) )
				return cookie;
		return null;
	}

	/**
	 * Returns a new conversation cookie instance if the cookie doesn't exist
	 * yet, or the existing cookie if it does. Note that the cookie will not be
	 * saved into the response until you call {@link ConversationCookie#save()}.
	 * 
	 * @param name
	 *        The cookie name
	 * @return A new cookie or the existing cookie
	 */
	public ConversationCookie createCookie( String name )
	{
		return ConversationCookie.createCookie( name, getResponse().getCookieSettings(), getCookies() );
	}

	/**
	 * The response status.
	 * 
	 * @return The response status
	 * @see #setStatus(Status)
	 */
	public Status getStatus()
	{
		return getResponse().getStatus();
	}

	/**
	 * The response status.
	 * 
	 * @param status
	 *        The response status
	 * @see #getStatus()
	 */
	public void setStatus( Status status )
	{
		getResponse().setStatus( status );
	}

	/**
	 * The response status code.
	 * 
	 * @return The response status code
	 * @see #setStatusCode(int)
	 */
	public int getStatusCode()
	{
		return getResponse().getStatus().getCode();
	}

	/**
	 * The response status code.
	 * 
	 * @param statusCode
	 *        The response status code
	 * @see #getStatusCode()
	 */
	public void setStatusCode( int statusCode )
	{
		getResponse().setStatus( Status.valueOf( statusCode ) );
	}

	/**
	 * When true, the {@link DelegatedStatusService} will not intercept errors.
	 * <p>
	 * Note that this will only have an effect if the
	 * {@link DelegatedStatusService} is installed in the application.
	 * 
	 * @return The passthrough status
	 * @see #setStatusPassthrough(boolean)
	 */
	public boolean getStatusPassthrough()
	{
		Boolean passthrough = (Boolean) getResponse().getAttributes().get( DelegatedStatusService.PASSTHROUGH_ATTRIBUTE );
		return passthrough != null ? passthrough : false;
	}

	/**
	 * @param passthrough
	 *        The passthrough status
	 * @see #getStatusPassthrough()
	 */
	public void setStatusPassthrough( boolean passthrough )
	{
		ConcurrentMap<String, Object> attributes = getResponse().getAttributes();
		if( passthrough )
			attributes.put( DelegatedStatusService.PASSTHROUGH_ATTRIBUTE, true );
		else
			attributes.remove( DelegatedStatusService.PASSTHROUGH_ATTRIBUTE );
	}

	/**
	 * A shortcut to the request.
	 * 
	 * @return The request
	 */
	public Request getRequest()
	{
		return Request.getCurrent();
	}

	/**
	 * A shortcut to the request's client info.
	 * 
	 * @return The client info
	 */
	public ClientInfo getClient()
	{
		return getRequest().getClientInfo();
	}

	/**
	 * A shortcut to the response.
	 * 
	 * @return The response
	 */
	public Response getResponse()
	{
		return Response.getCurrent();
	}

	/**
	 * A shortcut to set the response entity to a {@link StringRepresentation}.
	 * 
	 * @param text
	 *        The text
	 * @param mediaTypeName
	 *        The media type or null to leave unchanged
	 * @param languageName
	 *        The language or null to leave unchanged
	 * @param characterSetName
	 *        The character set or null to leave unchanged
	 * @return The string representation
	 */
	public StringRepresentation setResponseText( String text, String mediaTypeName, String languageName, String characterSetName )
	{
		StringRepresentation representation = new StringRepresentation( text, MediaType.valueOf( mediaTypeName ), Language.valueOf( languageName ), CharacterSet.valueOf( characterSetName ) );
		getResponse().setEntity( representation );
		return representation;
	}

	/**
	 * A shortcut to set the response entity to a
	 * {@link ByteArrayRepresentation}.
	 * 
	 * @param byteArray
	 *        The byte array
	 * @param mediaTypeName
	 *        The media type or null to leave unchanged
	 * @return The byte array representation
	 */
	public ByteArrayRepresentation setResponseBinary( byte[] byteArray, String mediaTypeName )
	{
		ByteArrayRepresentation representation = new ByteArrayRepresentation( byteArray, MediaType.valueOf( mediaTypeName ) );
		getResponse().setEntity( representation );
		return representation;
	}

	/**
	 * Checks if the request was received via the RIAP protocol
	 * 
	 * @return True if the request was received via the RIAP protocol
	 * @see LocalReference
	 */
	public boolean getInternal()
	{
		if( internal == null )
			internal = getReference().getSchemeProtocol().equals( Protocol.RIAP );
		return internal;
	}

	/**
	 * The relative path that would reach the base URI of the application if
	 * appended to the current resource URI.
	 * 
	 * @return The relative path
	 */
	public String getBase()
	{
		if( base == null )
		{
			Reference reference = getReference();
			Reference root = getRequest().getRootRef();

			if( root.getSchemeProtocol().equals( reference.getSchemeProtocol() ) )
				// The root needs a trailing slash
				root = new Reference( root + "/" );
			else
				// We cannot use the root if it is of the wrong protocol
				// (it might be when using InternalRedirector)
				root = reference.getBaseRef();

			// Reverse relative reference
			base = root.getRelativeRef( reference ).getPath();
		}

		return base;
	}

	/**
	 * The relative path that would reach the base URI of the application if
	 * appended to the current resource URI.
	 * 
	 * @return The relative path
	 */
	@Deprecated
	public String getPathToBase()
	{
		return getBase();
	}

	/**
	 * The URI query as a list. Includes duplicate keys.
	 * <p>
	 * This value is cached locally.
	 * 
	 * @return The query form
	 */
	public Form getQueryAll()
	{
		if( queryAll == null )
			queryAll = getRequest().getResourceRef().getQueryAsForm();
		return queryAll;
	}

	/**
	 * The URI query as a map. In the case of duplicate keys, only the first one
	 * will appear.
	 * <p>
	 * This value is cached locally.
	 * 
	 * @return The query map
	 */
	public Map<String, String> getQuery()
	{
		if( query == null )
			query = getQueryAll().getValuesMap();
		return query;
	}

	/**
	 * The form, sent via POST or PUT, as a list. Includes duplicate keys.
	 * Uploaded files will appear as instances of {@link FileParameter}.
	 * <p>
	 * This value is cached locally.
	 * 
	 * @return The form
	 */
	public Form getFormAll()
	{
		if( formAll == null )
		{
			if( getRequest().isEntityAvailable() )
			{
				fileUploadDirectory.mkdirs();
				formAll = new FormWithFiles( getRequest().getEntity(), fileUploadSizeThreshold, fileUploadDirectory );
			}
			else
				formAll = new Form();
		}

		return formAll;
	}

	/**
	 * The form, sent via POST or PUT, as a map. In the case of duplicate keys,
	 * only the last one will appear. Uploaded files will appear as instances of
	 * {@link FileParameter}. Other fields will be plain strings.
	 * <p>
	 * This value is cached locally.
	 * 
	 * @return The form
	 */
	public Map<String, Object> getForm()
	{
		if( form == null )
		{
			form = new HashMap<String, Object>();
			for( Parameter parameter : getFormAll() )
			{
				if( parameter instanceof FileParameter )
					form.put( parameter.getName(), parameter );
				else
					form.put( parameter.getName(), parameter.getValue() );
			}
		}

		return form;
	}

	/**
	 * The representation's disposition.
	 * 
	 * @return A disposition
	 */
	public Disposition getDisposition()
	{
		return disposition;
	}

	/**
	 * The request headers
	 * 
	 * @return The request headers
	 */
	@SuppressWarnings("unchecked")
	public Series<Header> getRequestHeaders()
	{
		if( requestHeaders == null )
		{
			ConcurrentMap<String, Object> attributes = getRequest().getAttributes();
			requestHeaders = (Series<Header>) attributes.get( HeaderConstants.ATTRIBUTE_HEADERS );
			if( requestHeaders == null )
			{
				requestHeaders = new Series<Header>( Header.class );
				attributes.put( HeaderConstants.ATTRIBUTE_HEADERS, requestHeaders );
			}
		}
		return requestHeaders;
	}

	/**
	 * The extra response headers
	 * 
	 * @return The extra response headers
	 */
	@SuppressWarnings("unchecked")
	public Series<Header> getResponseHeaders()
	{
		if( responseHeaders == null )
		{
			ConcurrentMap<String, Object> attributes = getResponse().getAttributes();
			responseHeaders = (Series<Header>) attributes.get( HeaderConstants.ATTRIBUTE_HEADERS );
			if( responseHeaders == null )
			{
				responseHeaders = new Series<Header>( Header.class );
				attributes.put( HeaderConstants.ATTRIBUTE_HEADERS, responseHeaders );
			}
		}
		return responseHeaders;
	}

	/**
	 * Whether the client asked for do-not-track.
	 * 
	 * @return Whether the client asked for do-not-track
	 */
	public boolean getDoNotTrack()
	{
		if( doNotTrack == null )
		{
			Header dnt = getRequestHeaders().getFirst( "DNT" );
			doNotTrack = ( dnt != null ) && dnt.getValue().equals( "1" );
		}
		return doNotTrack;
	}

	/**
	 * The request attributes.
	 * 
	 * @return The locals
	 */
	public ConcurrentMap<String, Object> getLocals()
	{
		return getRequest().getAttributes();
	}

	/**
	 * Permanent client-side redirection.
	 * 
	 * @param uri
	 *        The URI
	 */
	public void redirectPermanent( String uri )
	{
		getResponse().redirectPermanent( uri );
	}

	/**
	 * See-other client-side redirection.
	 * 
	 * @param uri
	 *        The URI
	 */
	public void redirectSeeOther( String uri )
	{
		getResponse().redirectSeeOther( uri );
	}

	/**
	 * Temporary client-side redirection.
	 * 
	 * @param uri
	 *        The URI
	 */
	public void redirectTemporary( String uri )
	{
		getResponse().redirectTemporary( uri );
	}

	//
	// Operations
	//

	/**
	 * Abruptly ends the conversation.
	 * <p>
	 * Works by throwing a {@link ConversationStoppedException}.
	 * 
	 * @return Always throws an exception, so nothing is ever returned (some
	 *         language engines require a return value defined in the signature)
	 */
	public boolean stop()
	{
		throw new ConversationStoppedException( getRequest() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The size in bytes beyond which uploaded files will be stored to disk.
	 */
	private final int fileUploadSizeThreshold;

	/**
	 * The directory in which to place uploaded files.
	 */
	private final File fileUploadDirectory;

	/**
	 * The resource reference.
	 */
	private Reference reference;

	/**
	 * The resource reference's remaining part (not including the query).
	 */
	private String wildcard;

	/**
	 * The URI query as a map.
	 */
	private Map<String, String> query;

	/**
	 * The URI query as a list.
	 */
	private Form queryAll;

	/**
	 * The form, sent via POST or PUT, as a map.
	 */
	private Form formAll;

	/**
	 * The form, sent via POST or PUT, as a list.
	 */
	private Map<String, Object> form;

	/**
	 * The request headers.
	 */
	private Series<Header> requestHeaders;

	/**
	 * The extra response headers.
	 */
	private Series<Header> responseHeaders;

	/**
	 * Whether the client asked for do-not-track.
	 */
	private Boolean doNotTrack;

	/**
	 * The conversation cookies.
	 */
	private Collection<ConversationCookie> conversationCookies;

	/**
	 * The representation's disposition.
	 */
	private Disposition disposition = new Disposition();

	/**
	 * True if the request was received via the RIAP protocol.
	 */
	private Boolean internal;

	/**
	 * The relative path that would reach the base URI of the application.
	 */
	private String base;
}
