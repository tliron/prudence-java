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

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;

import com.threecrickets.prudence.service.GeneratedTextResourceDocumentService;

/**
 * A writer that wraps a stack of writers.
 * <p>
 * Push/pop operations are not thread safe.
 * 
 * @author Tal Liron
 * @see GeneratedTextResourceDocumentService#startCapture(String)
 */
public class StackedWriter extends Writer
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param writer
	 *        The first writer
	 */
	public StackedWriter( Writer writer )
	{
		super();
		push( writer );
	}

	/**
	 * Constructor.
	 */
	public StackedWriter()
	{
		super();
	}

	//
	// Attributes
	//

	/**
	 * The current writer.
	 * 
	 * @return The current writer
	 */
	public Writer getCurrent()
	{
		return current;
	}

	//
	// Operations
	//

	/**
	 * Changes the current writer while adding the current writer to the stack.
	 * 
	 * @param writer
	 *        The writer
	 */
	public void push( Writer writer )
	{
		if( current != null )
			stack.add( 0, current );
		current = writer;
	}

	/**
	 * Changes the current writer to the last one added to the stack.
	 * 
	 * @return The writer before we popped
	 */
	public Writer pop()
	{
		Writer last = current;
		current = stack.remove( 0 );
		return last;
	}

	//
	// Writer
	//

	@Override
	public void write( char[] cbuf, int off, int len ) throws IOException
	{
		current.write( cbuf, off, len );
	}

	@Override
	public void flush() throws IOException
	{
		current.flush();
	}

	@Override
	public void close() throws IOException
	{
		current.close();
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The writer stack.
	 */
	private final LinkedList<Writer> stack = new LinkedList<Writer>();

	/**
	 * The current writer.
	 */
	private Writer current;
}
