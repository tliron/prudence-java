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
 * <li><code>handleCacheKeyTemplate(conversation, variables)</code></li>
 * </ul>
 * 
 * @author Tal Liron
 */
public class DelegatedCacheKeyTemplateHandler extends DelegatedHandler
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
	public DelegatedCacheKeyTemplateHandler( String documentName, Context context )
	{
		super( documentName, context );
	}

	//
	// Attributes
	//

	/**
	 * @return The entry point name for <code>handleCacheKeyTemplate()</code>
	 * @see #setEntryPointNameForCacheKeyTemplate(String)
	 */
	public String getEntryPointNameForCacheKeyTemplate()
	{
		return entryPointNameForCacheKeyTemplate;
	}

	/**
	 * @param entryPointNameForCacheKeyTemplate
	 *        The entry point name for <code>handleCacheKeyTemplate()</code>
	 * @see #getEntryPointNameForCacheKeyTemplate()
	 */
	public void setEntryPointNameForCacheKeyTemplate( String entryPointNameForCacheKeyTemplate )
	{
		this.entryPointNameForCacheKeyTemplate = entryPointNameForCacheKeyTemplate;
	}

	//
	// Operations
	//

	/**
	 * Calls the <code>handleCacheKeyTemplate</code> entry point.
	 * 
	 * @param variables
	 *        The cache key template variables to handle
	 */
	public void handleCacheKeyTemplate( String[] variables )
	{
		handle( entryPointNameForCacheKeyTemplate, (Object) variables );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The name of the <code>handleCacheKeyTemplate()</code> entry point in the
	 * executable.
	 */
	private volatile String entryPointNameForCacheKeyTemplate = "handleCacheKeyTemplate";
}
