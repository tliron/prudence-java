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

package com.threecrickets.prudence.service;

import com.threecrickets.prudence.ExecutionResource;
import com.threecrickets.prudence.internal.attributes.ExecutionResourceAttributes;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.document.DocumentDescriptor;

/**
 * Document service exposed to executables.
 * 
 * @author Tal Liron
 * @see ExecutionResource
 */
public class ExecutionResourceDocumentService extends DocumentService<ExecutionResourceAttributes>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 * @param documentDescriptor
	 *        The initial document descriptor
	 */
	public ExecutionResourceDocumentService( ExecutionResource resource, DocumentDescriptor<Executable> documentDescriptor )
	{
		super( resource.getAttributes() );
		pushDocumentDescriptor( documentDescriptor );
	}
}