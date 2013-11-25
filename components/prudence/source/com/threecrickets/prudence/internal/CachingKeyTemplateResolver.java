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

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.resource.ServerResource;
import org.restlet.util.Resolver;

import com.threecrickets.prudence.DelegatedCachingKeyTemplatePlugin;
import com.threecrickets.prudence.service.ResourceConversationServiceBase;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;

/**
 * Resolves a few special Prudence variables.
 * 
 * @author Tal Liron
 * @see DelegatedCachingKeyTemplatePlugin
 */
public class CachingKeyTemplateResolver<R extends ServerResource> extends Resolver<Object>
{
	//
	// Construction
	//

	public CachingKeyTemplateResolver( DocumentDescriptor<Executable> documentDescriptor, R resource, ResourceConversationServiceBase<R> conversationService, Encoding encoding )
	{
		this( documentDescriptor, resource, conversationService, encoding, resource.getRequest(), resource.getResponse() );
	}

	public CachingKeyTemplateResolver( DocumentDescriptor<Executable> documentDescriptor, R resource, ResourceConversationServiceBase<R> conversationService, Encoding encoding, Request request, Response response )
	{
		this( documentDescriptor, resource, conversationService, encoding, Resolver.createResolver( request, response ) );
	}

	public CachingKeyTemplateResolver( DocumentDescriptor<Executable> documentDescriptor, R resource, ResourceConversationServiceBase<R> conversationService, Encoding encoding, Resolver<?> callResolver )
	{
		this.documentDescriptor = documentDescriptor;
		this.resource = resource;
		this.conversationService = conversationService;
		this.callResolver = callResolver;
		this.encoding = encoding;
	}

	//
	// Resolver
	//

	public Object resolve( String name )
	{
		if( name.equals( DOCUMENT_NAME_VARIABLE ) )
			return documentDescriptor.getDefaultName();
		else if( name.equals( APPLICATION_NAME_VARIABLE ) )
			return resource.getApplication().getName();
		else if( name.equals( CONVERSATION_BASE_VARIABLE ) )
			return conversationService.getBase();
		else if( name.equals( NEGOTIATED_MEDIA_TYPE ) )
			return conversationService.getMediaTypeName();
		else if( name.equals( NEGOTIATED_LANGUAGE ) )
			return conversationService.getLanguageName();
		else if( name.equals( NEGOTIATED_ENCODING ) )
			return encoding != null ? encoding.getName() : "";

		return callResolver.resolve( name );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String DOCUMENT_NAME_VARIABLE = "dn";

	private static final String APPLICATION_NAME_VARIABLE = "an";

	private static final String CONVERSATION_BASE_VARIABLE = "cb";

	private static final String NEGOTIATED_MEDIA_TYPE = "nmt";

	private static final String NEGOTIATED_LANGUAGE = "nl";

	private static final String NEGOTIATED_ENCODING = "ne";

	private final DocumentDescriptor<Executable> documentDescriptor;

	private final R resource;

	private final ResourceConversationServiceBase<R> conversationService;

	private final Encoding encoding;

	private final Resolver<?> callResolver;
}
