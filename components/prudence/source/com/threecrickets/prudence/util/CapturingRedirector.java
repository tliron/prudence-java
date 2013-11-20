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

package com.threecrickets.prudence.util;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.routing.Redirector;

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
	 * Request attribute of the captive {@link Reference} for a {@link Request}.
	 * 
	 * @see #getCapturedReference(Request)
	 * @see #setCapturedReference(Request, Reference)
	 */
	public static final String CAPTURED_REFERENCE = CapturingRedirector.class.getCanonicalName() + ".capturedReference";

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
	 *        The captive reference
	 * @see #getCapturedReference(Request)
	 */
	public static void setCapturedReference( Request request, Reference capturedReference )
	{
		if( capturedReference != null )
			request.getAttributes().put( CAPTURED_REFERENCE, capturedReference );
		else
			request.getAttributes().remove( CAPTURED_REFERENCE );
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
	 * @param alwaysUseHostAsBase
	 *        Whether to always set the base reference to the host root URI
	 */
	public CapturingRedirector( Context context, String targetTemplate, boolean alwaysUseHostAsBase )
	{
		this( context, targetTemplate, alwaysUseHostAsBase, MODE_SERVER_OUTBOUND );
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
	 * @param useHostAsBase
	 *        Whether to set the base reference to the host root URI
	 */
	public CapturingRedirector( Context context, String targetTemplate, boolean useHostAsBase, int mode )
	{
		super( context, targetTemplate, mode, false );
		describe();
		this.useHostAsBase = useHostAsBase;
	}

	//
	// Redirector
	//

	@Override
	public void handle( Request request, Response response )
	{
		if( getCapturedReference( request ) == null )
		{
			Reference resourceRef = request.getResourceRef();

			// TODO: are we still using this feature?
			if( useHostAsBase )
				resourceRef = new Reference( new Reference( resourceRef.getHostIdentifier() + "/" ), resourceRef );

			setCapturedReference( request, resourceRef );
		}

		super.handle( request, response );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Whether to set the base reference to the host root URI.
	 */
	private final boolean useHostAsBase;

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
