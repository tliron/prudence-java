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

package com.threecrickets.prudence.internal.attributes;

import org.restlet.Context;
import org.restlet.resource.ResourceException;

import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.ParserManager;
import com.threecrickets.scripturian.ParsingContext;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * @author Tal Liron
 */
public interface DocumentExecutionAttributes
{
	//
	// Attributes
	//

	/**
	 * The {@link DocumentSource} instance used to fetch documents. This must be
	 * set to a valid value before this class is used!
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>documentSource</code> in the application's {@link Context}.
	 * 
	 * @return The document source
	 */
	public DocumentSource<Executable> getDocumentSource();

	/**
	 * The extra {@link DocumentSource} instances used to fetch documents.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>extraDocumentSources</code> in the application's {@link Context}.
	 * 
	 * @return The extra document sources
	 */
	public Iterable<DocumentSource<Executable>> getExtraDocumentSources();

	/**
	 * The {@link LanguageManager} used to create the language adapters. Uses a
	 * default instance, but can be set to something else.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>languageManager</code> in the application's {@link Context}.
	 * 
	 * @return The language manager
	 */
	public LanguageManager getLanguageManager();

	/**
	 * The {@link ParserManager} used to create the executable parsers. Uses a
	 * default instance, but can be set to something else.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>parserManager</code> in the application's {@link Context}.
	 * 
	 * @return The parser manager
	 */
	public ParserManager getParserManager();

	/**
	 * The default language tag name to be used if the script doesn't specify
	 * one. Defaults to "javascript".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>defaultLanguageTag</code> in the application's {@link Context}.
	 * 
	 * @return The default language tag
	 */
	public String getDefaultLanguageTag();

	/**
	 * Whether to prepare the executables. Preparation increases initialization
	 * time and reduces execution time. Note that not all languages support
	 * preparation as a separate operation. Defaults to true.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>prepare</code> in the application's {@link Context}.
	 * 
	 * @return Whether to prepare executables
	 */
	public boolean isPrepare();

	/**
	 * Whether to enable debug for the executables.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>debug</code> in the application's {@link Context}.
	 * 
	 * @return Whether to enable debug for executables
	 */
	public boolean isDebug();

	/**
	 * An optional {@link ExecutionController} to be used with the executable.
	 * Useful for exposing your own global variables to the executable.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>executionController</code> in the application's {@link Context}.
	 * 
	 * @return The execution controller or null if none used
	 */
	public abstract ExecutionController getExecutionController();

	/**
	 * Executables might use these document sources for importing libraries.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>libraryDocumentSources</code> in the application's {@link Context}.
	 * 
	 * @return The library document sources or null
	 * @see ExecutionContext#getLibraryLocations()
	 */
	public Iterable<DocumentSource<Executable>> getLibraryDocumentSources();

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used. This allows you to
	 * use the directory structure to create nice URLs without relying on
	 * filenames. Defaults to "default".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>defaultName</code> in the application's {@link Context}.
	 * 
	 * @return The default name
	 */
	public String getDefaultName();

	/**
	 * Whether or not trailing slashes are required. Defaults to true.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>trailingSlashRequired</code> in the application's {@link Context}.
	 * 
	 * @return Whether to allow client caching
	 */
	public boolean isTrailingSlashRequired();

	/**
	 * Cache used for caching mode. It is stored in the application's
	 * {@link Context} for persistence across requests.
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>com.threecrickets.prudence.cache</code> in the application's
	 * {@link Context}.
	 * 
	 * @return The cache or null
	 */
	public Cache getCache();

	//
	// Operations
	//

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
	public String validateDocumentName( String documentName );

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
	public String validateDocumentName( String documentName, String defaultDocumentName ) throws ResourceException;

	/**
	 * Calls {@link Executable#createOnce(String, boolean, ParsingContext)},
	 * trying various document sources that we support one at a time.
	 * 
	 * @param documentName
	 *        The document name
	 * @param isTextWithScriptlets
	 *        Whether it's a text-with-scriptlets document
	 * @param includeMainSource
	 *        Whether to include {@link #getDocumentSource()} in the search
	 * @param parserName
	 *        The parser to use, or null for the default parser
	 * @param includeLibrarySources
	 *        Whether to include {@link #getLibraryDocumentSources()} in the
	 *        search
	 * @return A document descriptor with a valid executable as its document
	 * @throws ParsingException
	 *         In case of a Scripturian parsing error
	 * @throws DocumentException
	 *         In case of a Scripturian document retrieval error
	 */
	public DocumentDescriptor<Executable> createDocumentOnce( String documentName, String parserName, boolean includeMainSource, boolean includeExtraSources, boolean includeLibrarySources ) throws ParsingException,
		DocumentException;

	/**
	 * Creates an on-the-fly document, or retrieves it if it already exists.
	 * 
	 * @param documentName
	 *        The document name
	 * @param code
	 *        The code to execute
	 * @return A document descriptor with a valid executable as its document
	 * @throws ParsingException
	 *         In case of a Scripturian parsing error
	 * @throws DocumentException
	 *         In case of a Scripturian document retrieval error
	 */
	public DocumentDescriptor<Executable> createScriptletDocumentOnce( String documentName, String code ) throws ParsingException, DocumentException;
}
