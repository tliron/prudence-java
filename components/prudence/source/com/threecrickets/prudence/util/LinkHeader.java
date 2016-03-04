/**
 * Copyright 2009-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of the LGPL version 3.0:
 * http://www.gnu.org/copyleft/lesser.html
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.prudence.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.data.Header;
import org.restlet.util.Series;

/**
 * Manages a Link response header.
 * <p>
 * See <a href="https://tools.ietf.org/html/rfc5988#section-5.1">RFC 5988</a>.
 * 
 * @author Tal Liron
 */
public class LinkHeader
{
	//
	// Constants
	//

	/**
	 * The Link header name.
	 */
	public static final String HEADER_LINK = "Link";

	//
	// Static operations
	//

	/**
	 * Returns a new link header instance if it doesn't exist yet, or the
	 * existing link header if it does.
	 * 
	 * @param reference
	 *        The reference
	 * @param headers
	 *        The response headers
	 * @param linkHeaders
	 *        The link headers
	 * @return A new link header or the existing link header
	 */
	public static LinkHeader createLinkHeader( String reference, Series<Header> headers, Collection<LinkHeader> linkHeaders )
	{
		// Look for existing link header
		for( LinkHeader linkHeader : linkHeaders )
			if( linkHeader.getReference().equals( reference ) )
				return linkHeader;

		// Not found, so create a new one
		LinkHeader linkHeader = new LinkHeader( reference, headers );
		linkHeaders.add( linkHeader );
		return linkHeader;
	}

	/**
	 * Creates a collection of link headers based on response headers.
	 * 
	 * @param headers
	 *        The response headers
	 * @return A collection of link headers
	 */
	public static Collection<LinkHeader> wrapLinkHeaders( Series<Header> headers )
	{
		Collection<LinkHeader> linkHeaders = new ArrayList<LinkHeader>();
		for( Header header : headers )
		{
			if( header.getName().equalsIgnoreCase( HEADER_LINK ) )
			{
				try
				{
					LinkHeader linkHeader = new LinkHeaderReader( header.getValue(), headers ).readValue();
					linkHeaders.add( linkHeader );
				}
				catch( IOException x )
				{
					Context.getCurrentLogger().log( Level.WARNING, "Error during " + HEADER_LINK + " header parsing. Header: " + header.getValue(), x );
				}
			}
		}
		return linkHeaders;
	}

	/**
	 * Removes all link headers with the specified reference from the response
	 * headers.
	 * 
	 * @param headers
	 *        The response headers
	 * @param reference
	 *        The reference
	 * @return True if removed
	 */
	public static boolean removeLinkHeaders( Series<Header> headers, String reference )
	{
		boolean removed = false;
		for( Iterator<Header> i = headers.iterator(); i.hasNext(); )
		{
			Header header = i.next();
			if( header.getName().equalsIgnoreCase( HEADER_LINK ) )
			{
				try
				{
					LinkHeader linkHeader = new LinkHeaderReader( header.getValue(), headers ).readValueOnlyReference();
					if( linkHeader.getReference().equals( reference ) )
					{
						i.remove();
						removed = true;
					}
				}
				catch( IOException x )
				{
					Context.getCurrentLogger().log( Level.WARNING, "Error during " + HEADER_LINK + " header parsing. Header: " + header.getValue(), x );
				}
			}
		}
		return removed;
	}

	//
	// Construction
	//

	public LinkHeader( String reference, Series<Header> headers )
	{
		this.reference = reference;
		this.headers = headers;
	}

	//
	// Attributes
	//

	/**
	 * The reference.
	 * 
	 * @return The reference
	 */
	public String getReference()
	{
		return reference;
	}

	/**
	 * The parameters.
	 * 
	 * @return The parameters
	 */
	public Map<String, Object> getParameters()
	{
		return parameters;
	}

	//
	// Operations
	//

	/**
	 * Converts the link header to a header value.
	 * 
	 * @return The header value
	 */
	public String toHeaderValue()
	{
		LinkHeaderWriter writer = new LinkHeaderWriter();
		try
		{
			writer.append( this );
		}
		finally
		{
			try
			{
				writer.close();
			}
			catch( IOException x )
			{
			}
		}
		return writer.toString();
	}

	/**
	 * Saves the link header to the response headers.
	 */
	public void save()
	{
		remove();
		Header header = new Header( HEADER_LINK, toHeaderValue() );
		headers.add( header );
	}

	/**
	 * Removes the link header from the response headers.
	 */
	public void remove()
	{
		removeLinkHeaders( headers, reference );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final String reference;

	private final Series<Header> headers;

	private final Map<String, Object> parameters = new TreeMap<String, Object>();
}
