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
public class CacheKeyTemplateResolver<R extends ServerResource> extends Resolver<Object>
{
	//
	// Construction
	//

	public CacheKeyTemplateResolver( DocumentDescriptor<Executable> documentDescriptor, R resource, ResourceConversationServiceBase<R> conversationService )
	{
		this( documentDescriptor, resource, conversationService, resource.getRequest(), resource.getResponse() );
	}

	public CacheKeyTemplateResolver( DocumentDescriptor<Executable> documentDescriptor, R resource, ResourceConversationServiceBase<R> conversationService, Request request, Response response )
	{
		this( documentDescriptor, resource, conversationService, Resolver.createResolver( request, response ) );
	}

	public CacheKeyTemplateResolver( DocumentDescriptor<Executable> documentDescriptor, R resource, ResourceConversationServiceBase<R> conversationService, Resolver<?> callResolver )
	{
		this.documentDescriptor = documentDescriptor;
		this.resource = resource;
		this.conversationService = conversationService;
		this.callResolver = callResolver;
	}

	//
	// Resolver
	//

	@Override
	public Object resolve( String name )
	{
		if( name.equals( DOCUMENT_NAME_VARIABLE ) )
			return documentDescriptor.getDefaultName();
		else if( name.equals( APPLICATION_NAME_VARIABLE ) )
			return resource.getApplication().getName();
		else if( name.equals( CONVERSATION_BASE_VARIABLE ) )
			return conversationService.getBase();

		return callResolver.resolve( name );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String DOCUMENT_NAME_VARIABLE = "dn";

	private static final String APPLICATION_NAME_VARIABLE = "an";

	private static final String CONVERSATION_BASE_VARIABLE = "cb";

	private final DocumentDescriptor<Executable> documentDescriptor;

	private final R resource;

	private final ResourceConversationServiceBase<R> conversationService;

	private final Resolver<?> callResolver;
}
