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

import org.restlet.Context;

/**
 * <p>
 * A {@link DelegatedHandler} with the following supported entry points:
 * <ul>
 * <li><code>handleInterpolation(conversation, variables)</code></li>
 * </ul>
 * 
 * @author Tal Liron
 */
public class DelegatedCachingKeyTemplatePlugin extends DelegatedHandler
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param documentName
	 *        The document name
	 * @param context
	 *        The context used to configure the handler
	 */
	public DelegatedCachingKeyTemplatePlugin( String documentName, Context context )
	{
		super( documentName, context );
	}

	//
	// Attributes
	//

	/**
	 * @return The entry point name for <code>handleInterpolation()</code>
	 * @see #setEntryPointNameForHandleInterpolation(String)
	 */
	public String getEntryPointNameForHandleInterpolation()
	{
		return entryPointNameForHandleInterpolation;
	}

	/**
	 * @param entryPointNameForHandleInterpolation
	 *        The entry point name for <code>handleInterpolation()</code>
	 * @see #getEntryPointNameForHandleInterpolation()
	 */
	public void setEntryPointNameForHandleInterpolation( String entryPointNameForHandleInterpolation )
	{
		this.entryPointNameForHandleInterpolation = entryPointNameForHandleInterpolation;
	}

	//
	// Operations
	//

	/**
	 * Calls the <code>handleInterpolation</code> entry point.
	 * 
	 * @param variables
	 *        The caching key template variables to handle
	 */
	public void handleInterpolation( String[] variables )
	{
		handleWithConversation( entryPointNameForHandleInterpolation, (Object) variables );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The name of the <code>handleInterpolation()</code> entry point in the
	 * executable.
	 */
	private volatile String entryPointNameForHandleInterpolation = "handleInterpolation";
}
