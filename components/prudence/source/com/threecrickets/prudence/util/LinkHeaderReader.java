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

import org.restlet.data.Header;
import org.restlet.data.Parameter;
import org.restlet.engine.header.HeaderReader;
import org.restlet.util.Series;

/**
 * Reads a Link response header.
 * <p>
 * See <a href="https://tools.ietf.org/html/rfc5988#section-5.1">RFC 5988</a>.
 * 
 * @author Tal Liron
 */
public class LinkHeaderReader extends HeaderReader<LinkHeader>
{
	//
	// Construction
	//

	public LinkHeaderReader( String header, Series<Header> headers )
	{
		super( header );
		this.headers = headers;
	}

	//
	// Operations
	//

	/**
	 * Reads a URI reference
	 * 
	 * @return The URI reference
	 * @throws IOException
	 *         In case of an I/O or parsing error
	 */
	public String readUriReference() throws IOException
	{
		// Read opening delimiter
		int next = read();
		if( next != '<' )
			throw new IOException( "Invalid URI reference. No opening delimiter '<'." );

		// Read value until end or delimiter
		StringBuilder sb = new StringBuilder();
		next = read();

		while( ( next != -1 ) && ( next != '>' ) )
		{
			sb.append( (char) next );
			next = read();
		}

		// Verify closing delimiter
		if( next != '>' )
			throw new IOException( "Invalid URI reference. No closing delimiter '>'." );

		return sb.toString();
	}

	/**
	 * Reads a link header, but just the reference, ignoring the additional
	 * parameters.
	 * 
	 * @return The link header
	 * @throws IOException
	 *         In case of an I/O or parsing error
	 */
	public LinkHeader readValueOnlyReference() throws IOException
	{
		String reference = readUriReference();
		return new LinkHeader( reference, headers );
	}

	//
	// HeaderReader
	//

	@Override
	public LinkHeader readValue() throws IOException
	{
		LinkHeader linkHeader = readValueOnlyReference();

		while( true )
		{
			if( !skipParameterSeparator() )
				break;

			Parameter parameter = readParameter();
			if( parameter == null )
				throw new IOException( "Invalid Link header. Parameter separator is not followed by a parameter." );

			linkHeader.getParameters().put( parameter.getName(), parameter.getValue() );
		}

		return linkHeader;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private final Series<Header> headers;
}
