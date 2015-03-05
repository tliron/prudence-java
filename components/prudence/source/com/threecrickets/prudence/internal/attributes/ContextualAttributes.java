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

package com.threecrickets.prudence.internal.attributes;

import java.io.File;
import java.io.Writer;
import java.util.Iterator;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ParsingContext;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentFileSource;
import com.threecrickets.scripturian.document.DocumentFormatter;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.DocumentNotFoundException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.scripturian.parser.ScriptletsParser;

/**
 * @author Tal Liron
 */
public abstract class ContextualAttributes implements DocumentExecutionAttributes
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param prefix
	 *        The prefix for attribute keys
	 */
	public ContextualAttributes( String prefix )
	{
		this.prefix = prefix;
	}

	//
	// Attributes
	//

	/**
	 * The contextual attributes.
	 * 
	 * @return The attributes
	 */
	public abstract ConcurrentMap<String, Object> getAttributes();

	/**
	 * The {@link Writer} used by the {@link Executable}. Defaults to standard
	 * output.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>writer</code> in the application's {@link Context}.
	 * 
	 * @return The writer
	 */
	public abstract Writer getWriter();

	/**
	 * Same as {@link #getWriter()}, for standard error. Defaults to standard
	 * error.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>errorWriter</code> in the application's {@link Context}.
	 * 
	 * @return The error writer
	 */
	public abstract Writer getErrorWriter();

	/**
	 * The name of the global variable with which to access the document
	 * service. Defaults to "document".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>documentServiceName</code> in the application's {@link Context}.
	 * 
	 * @return The document service name
	 */
	public abstract String getDocumentServiceName();

	/**
	 * The name of the global variable with which to access the application
	 * service. Defaults to "application".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>applicationServiceName</code> in the application's {@link Context}.
	 * 
	 * @return The application service name
	 */
	public abstract String getApplicationServiceName();

	/**
	 * This is so we can see the source code for documents by adding
	 * <code>?source=true</code> to the URL. You probably wouldn't want this for
	 * most applications. Defaults to false.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>sourceViewable</code> in the application's {@link Context}.
	 * 
	 * @return Whether to allow viewing of source code
	 */
	public abstract boolean isSourceViewable();

	/**
	 * An optional {@link DocumentFormatter} to use for representing source
	 * code.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>documentFormatter</code> in the application's {@link Context}.
	 * 
	 * @return The document formatter or null
	 * @see #isSourceViewable()
	 */
	public abstract DocumentFormatter<Executable> getDocumentFormatter();

	/**
	 * The default character set to be used if the client does not specify it.
	 * Defaults to {@link CharacterSet#UTF_8}.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>defaultCharacterSet</code> in the application's {@link Context}.
	 * 
	 * @return The default character set
	 */
	public abstract CharacterSet getDefaultCharacterSet();

	/**
	 * The directory in which to place uploaded files. Defaults to "uploads"
	 * under the application root.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>fileUploadDirectory</code> in the application's {@link Context}.
	 * 
	 * @return The file upload directory or null
	 */
	public abstract File getFileUploadDirectory();

	/**
	 * The size in bytes beyond which uploaded files will be stored to disk.
	 * Defaults to zero, meaning that all uploaded files will be stored to disk.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>fileUploadSizeThreshold</code> in the application's {@link Context}.
	 * 
	 * @return The file upload size threshold
	 */
	public abstract int getFileUploadSizeThreshold();

	//
	// Operations
	//

	/**
	 * Adds the library locations to the execution context.
	 * 
	 * @param executionContext
	 *        The execution context
	 * @see #getLibraryDocumentSources()
	 */
	public void addLibraryLocations( ExecutionContext executionContext )
	{
		Iterable<DocumentSource<Executable>> sources = getLibraryDocumentSources();
		if( sources != null )
		{
			for( DocumentSource<Executable> source : sources )
			{
				if( source instanceof DocumentFileSource<?> )
				{
					File libraryDirectory = ( (DocumentFileSource<Executable>) source ).getBasePath();
					if( libraryDirectory != null )
						executionContext.getLibraryLocations().add( libraryDirectory.toURI() );
				}
			}
		}
	}

	//
	// DocumentExecutionAttributes
	//

	public DocumentDescriptor<Executable> createDocumentOnce( String documentName, String parserName, boolean includeMainSource, boolean includeExtraSources, boolean includeLibrarySources ) throws ParsingException,
		DocumentException
	{
		ParsingContext parsingContext = new ParsingContext();
		parsingContext.setLanguageManager( getLanguageManager() );
		parsingContext.setParserManager( getParserManager() );
		parsingContext.setDefaultLanguageTag( getDefaultLanguageTag() );
		parsingContext.setPrepare( isPrepare() );
		parsingContext.setDebug( isDebug() );
		if( includeMainSource )
			parsingContext.setDocumentSource( getDocumentSource() );

		Iterator<DocumentSource<Executable>> iterator = null;
		while( true )
		{
			try
			{
				if( parsingContext.getDocumentSource() == null )
					throw new DocumentNotFoundException( documentName );

				return Executable.createOnce( documentName, parserName, parsingContext );
			}
			catch( DocumentNotFoundException x )
			{
				if( ( ( iterator == null ) || !iterator.hasNext() ) && includeExtraSources )
				{
					Iterable<DocumentSource<Executable>> sources = getExtraDocumentSources();
					iterator = sources != null ? sources.iterator() : null;
					includeExtraSources = false;
				}

				if( ( ( iterator == null ) || !iterator.hasNext() ) && includeLibrarySources )
				{
					Iterable<DocumentSource<Executable>> sources = getLibraryDocumentSources();
					iterator = sources != null ? sources.iterator() : null;
					includeLibrarySources = false;
				}

				if( ( iterator == null ) || !iterator.hasNext() )
					throw new DocumentNotFoundException( documentName );

				parsingContext.setDocumentSource( iterator.next() );
			}
		}
	}

	public DocumentDescriptor<Executable> createScriptletDocumentOnce( String documentName, String code ) throws ParsingException, DocumentException
	{
		DocumentSource<Executable> documentSource = getDocumentSource();
		try
		{
			return documentSource.getDocument( documentName );
		}
		catch( DocumentNotFoundException x )
		{
			ParsingContext parsingContext = new ParsingContext();
			parsingContext.setLanguageManager( getLanguageManager() );
			parsingContext.setParserManager( getParserManager() );
			parsingContext.setDefaultLanguageTag( getDefaultLanguageTag() );
			parsingContext.setPrepare( isPrepare() );
			parsingContext.setDebug( isDebug() );
			parsingContext.setDocumentSource( documentSource );

			Executable executable = new Executable( documentName, System.currentTimeMillis(), code, ScriptletsParser.NAME, parsingContext );
			documentSource.setDocument( documentName, code, "", executable );
			return documentSource.getDocument( documentName );
		}
	}

	/**
	 * Throws an exception if the document name is invalid. Uses
	 * {@link #getDefaultName()} if no name is given, and respects
	 * {@link #isTrailingSlashRequired()}.
	 * 
	 * @param documentName
	 *        The document name
	 * @return The valid document name
	 * @throws ResourceException
	 */
	public String validateDocumentName( String documentName ) throws ResourceException
	{
		return validateDocumentName( documentName, getDefaultName() );
	}

	/**
	 * Throws an exception if the document name is invalid. Uses the default
	 * given document name if no name is given, and respects
	 * {@link #isTrailingSlashRequired()}.
	 * 
	 * @param documentName
	 *        The document name
	 * @param defaultDocumentName
	 *        The default document name
	 * @return The valid document name
	 * @throws ResourceException
	 */
	public String validateDocumentName( String documentName, String defaultDocumentName ) throws ResourceException
	{
		if( isTrailingSlashRequired() )
			if( ( documentName != null ) && ( documentName.length() != 0 ) && !documentName.endsWith( "/" ) )
				throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );

		if( ( documentName == null ) || ( documentName.length() == 0 ) || ( documentName.equals( "/" ) ) )
		{
			documentName = defaultDocumentName;
			if( isTrailingSlashRequired() && !documentName.endsWith( "/" ) )
				documentName += "/";
		}

		return documentName;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * The prefix for attribute keys.
	 */
	protected final String prefix;
}
