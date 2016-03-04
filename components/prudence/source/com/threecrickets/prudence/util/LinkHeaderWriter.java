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

import java.util.Map;

import org.restlet.data.CharacterSet;
import org.restlet.engine.header.HeaderWriter;

/**
 * Writes a Link response header.
 * <p>
 * See <a href="https://tools.ietf.org/html/rfc5988#section-5.1">RFC 5988</a>.
 * 
 * @author Tal Liron
 */
public class LinkHeaderWriter extends HeaderWriter<LinkHeader>
{
	//
	// Operations
	//

	/**
	 * Appends a link between '&lt;' and '&gt;'.
	 * 
	 * @param link
	 *        The link
	 * @return The writer
	 */
	public HeaderWriter<LinkHeader> appendLink( CharSequence link )
	{
		append( '<' );
		appendUriEncoded( link, CharacterSet.UTF_8 ); // TODO: character set?
		append( '>' );
		return this;
	}

	//
	// HeaderWriter
	//

	@Override
	public HeaderWriter<LinkHeader> append( LinkHeader linkHeader )
	{
		appendLink( linkHeader.getReference() );

		for( Map.Entry<String, Object> parameter : linkHeader.getParameters().entrySet() )
		{
			String name = parameter.getKey();
			Object value = parameter.getValue();
			appendValueSeparator();
			appendExtension( name, value != null ? value.toString() : null );
		}

		return this;
	}
}