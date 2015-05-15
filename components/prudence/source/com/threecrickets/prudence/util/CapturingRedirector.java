/**
 * Copyright 2009-2015 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.util;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.routing.Redirector;

import com.threecrickets.prudence.internal.CachingUtil;

/**
 * A {@link Redirector} that keeps track of the captured reference.
 * 
 * @author Tal Liron
 * @see CapturingRouter
 */
public class CapturingRedirector extends ResolvingRedirector
{
	//
	// Constants
	//

	/**
	 * Request attribute of the captured {@link Reference} for a {@link Request}
	 * .
	 * 
	 * @see #getCapturedReference(Request)
	 * @see #setCapturedReference(Request, Reference)
	 */
	public static final String CAPTURED_REFERENCE = CapturingRedirector.class.getCanonicalName() + ".capturedReference";

	/**
	 * Request attribute of the captured root {@link Reference} for a
	 * {@link Request}.
	 * 
	 * @see #getCapturedRootReference(Request)
	 * @see #setCapturedRootReference(Request, Reference)
	 */
	public static final String CAPTURED_ROOT_REFERENCE = CapturingRedirector.class.getCanonicalName() + ".capturedRootReference";

	//
	// Static attributes
	//

	/**
	 * The captured reference.
	 * 
	 * @param request
	 *        The request
	 * @return The captured reference
	 * @see #setCapturedReference(Request, Reference)
	 */
	public static Reference getCapturedReference( Request request )
	{
		return (Reference) request.getAttributes().get( CAPTURED_REFERENCE );
	}

	/**
	 * The captured reference.
	 * 
	 * @param request
	 *        The request
	 * @param capturedReference
	 *        The captured reference
	 * @see #getCapturedReference(Request)
	 */
	public static void setCapturedReference( Request request, Reference capturedReference )
	{
		if( capturedReference != null )
			request.getAttributes().put( CAPTURED_REFERENCE, capturedReference );
		else
			request.getAttributes().remove( CAPTURED_REFERENCE );
	}

	/**
	 * The captured root reference.
	 * 
	 * @param request
	 *        The request
	 * @return The captured root reference
	 * @see #setCapturedRootReference(Request, Reference)
	 */
	public static Reference getCapturedRootReference( Request request )
	{
		return (Reference) request.getAttributes().get( CAPTURED_ROOT_REFERENCE );
	}

	/**
	 * The captured root reference.
	 * 
	 * @param request
	 *        The request
	 * @param capturedRootReference
	 *        The captured root reference
	 * @see #getCapturedRootReference(Request)
	 */
	public static void setCapturedRootReference( Request request, Reference capturedRootReference )
	{
		if( capturedRootReference != null )
			request.getAttributes().put( CAPTURED_ROOT_REFERENCE, capturedRootReference );
		else
			request.getAttributes().remove( CAPTURED_ROOT_REFERENCE );
	}

	//
	// Construction
	//

	/**
	 * Construction for {@link Redirector#MODE_SERVER_OUTBOUND}.
	 * 
	 * @param context
	 *        The context
	 * @param targetTemplate
	 *        The target URI template
	 */
	public CapturingRedirector( Context context, String targetTemplate )
	{
		this( context, targetTemplate, MODE_SERVER_OUTBOUND );
	}

	/**
	 * Constructor.
	 * 
	 * @param context
	 *        The context
	 * @param targetTemplate
	 *        The target URI template
	 * @param mode
	 *        The redirection mode
	 */
	public CapturingRedirector( Context context, String targetTemplate, int mode )
	{
		super( context, targetTemplate, mode );
		setHeadersCleaning( false );
		describe();
	}

	//
	// Redirector
	//

	@Override
	public void handle( Request request, Response response )
	{
		// Clean up saved information
		CachingUtil.clearExistingValidDocumentName( request );

		if( getCapturedReference( request ) == null )
			setCapturedReference( request, request.getResourceRef() );
		if( getCapturedRootReference( request ) == null )
			setCapturedRootReference( request, request.getRootRef() );

		super.handle( request, response );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Add description.
	 */
	private void describe()
	{
		setOwner( "Prudence" );
		setAuthor( "Three Crickets" );
		setName( getClass().getSimpleName() );
		setDescription( "A redirector that keeps track of the captured reference" );
	}
}
