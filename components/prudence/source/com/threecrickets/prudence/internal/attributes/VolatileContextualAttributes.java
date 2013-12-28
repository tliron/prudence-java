/**
 * Copyright 2009-2014 Three Crickets LLC.
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
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.data.CharacterSet;

import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.prudence.internal.JygmentsDocumentFormatter;
import com.threecrickets.prudence.util.InstanceUtil;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.ParserManager;
import com.threecrickets.scripturian.document.DocumentFormatter;
import com.threecrickets.scripturian.document.DocumentSource;

/**
 * All contextual attributes are here use JVM "volatile" storage, making
 * instances of this class thread-safe.
 * 
 * @author Tal Liron
 * @see NonVolatileContextualAttributes
 */
public abstract class VolatileContextualAttributes extends ContextualAttributes
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
	public VolatileContextualAttributes( String prefix )
	{
		super( prefix );
	}

	//
	// ContextualAttributes
	//

	public Writer getWriter()
	{
		if( writer == null )
		{
			writer = (Writer) getAttributes().get( prefix + ".writer" );

			if( writer == null )
				writer = new OutputStreamWriter( System.out );
		}

		return writer;
	}

	public Writer getErrorWriter()
	{
		if( errorWriter == null )
		{
			errorWriter = (Writer) getAttributes().get( prefix + ".errorWriter" );

			if( errorWriter == null )
				errorWriter = new OutputStreamWriter( System.err );
		}

		return errorWriter;
	}

	@SuppressWarnings("unchecked")
	public DocumentSource<Executable> getDocumentSource()
	{
		if( documentSource == null )
		{
			documentSource = (DocumentSource<Executable>) getAttributes().get( prefix + ".documentSource" );

			if( documentSource == null )
				throw new RuntimeException( "Attribute " + prefix + ".documentSource must be set in context" );
		}

		return documentSource;
	}

	@SuppressWarnings("unchecked")
	public Iterable<DocumentSource<Executable>> getExtraDocumentSources()
	{
		if( extraDocumentSources == null )
			extraDocumentSources = (Iterable<DocumentSource<Executable>>) getAttributes().get( prefix + ".extraDocumentSources" );

		return extraDocumentSources;
	}

	@SuppressWarnings("unchecked")
	public Iterable<DocumentSource<Executable>> getLibraryDocumentSources()
	{
		if( libraryDocumentSources == null )
			libraryDocumentSources = (Iterable<DocumentSource<Executable>>) getAttributes().get( prefix + ".libraryDocumentSources" );

		return libraryDocumentSources;
	}

	public String getDocumentServiceName()
	{
		if( documentServiceName == null )
		{
			documentServiceName = (String) getAttributes().get( prefix + ".documentServiceName" );

			if( documentServiceName == null )
				documentServiceName = "document";
		}

		return documentServiceName;
	}

	public String getApplicationServiceName()
	{
		if( applicationServiceName == null )
		{
			applicationServiceName = (String) getAttributes().get( prefix + ".applicationServiceName" );

			if( applicationServiceName == null )
				applicationServiceName = "application";
		}

		return applicationServiceName;
	}

	public boolean isSourceViewable()
	{
		if( sourceViewable == null )
		{
			sourceViewable = (Boolean) getAttributes().get( prefix + ".sourceViewable" );

			if( sourceViewable == null )
				sourceViewable = false;
		}

		return sourceViewable;
	}

	@SuppressWarnings("unchecked")
	public DocumentFormatter<Executable> getDocumentFormatter()
	{
		if( documentFormatter == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
			String key = prefix + ".documentFormatter";
			documentFormatter = (DocumentFormatter<Executable>) attributes.get( key );

			if( documentFormatter == null )
			{
				documentFormatter = new JygmentsDocumentFormatter<Executable>();

				DocumentFormatter<Executable> existing = (DocumentFormatter<Executable>) attributes.putIfAbsent( key, documentFormatter );
				if( existing != null )
					documentFormatter = existing;
			}
		}

		return documentFormatter;
	}

	public CharacterSet getDefaultCharacterSet()
	{
		if( defaultCharacterSet == null )
		{
			defaultCharacterSet = (CharacterSet) getAttributes().get( prefix + ".defaultCharacterSet" );

			if( defaultCharacterSet == null )
				defaultCharacterSet = CharacterSet.UTF_8;
		}

		return defaultCharacterSet;
	}

	public File getFileUploadDirectory()
	{
		if( fileUploadDirectory == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
			String key = prefix + ".fileUploadDirectory";
			fileUploadDirectory = (File) attributes.get( key );

			if( fileUploadDirectory == null )
			{
				File root = (File) attributes.get( InstanceUtil.ROOT_ATTRIBUTE );
				fileUploadDirectory = new File( root, "uploads" );

				File existing = (File) attributes.putIfAbsent( key, fileUploadDirectory );
				if( existing != null )
					fileUploadDirectory = existing;
			}
		}

		return fileUploadDirectory;
	}

	public int getFileUploadSizeThreshold()
	{
		if( fileUploadSizeThreshold == null )
		{
			Number number = (Number) getAttributes().get( prefix + ".fileUploadSizeThreshold" );

			if( number != null )
				fileUploadSizeThreshold = number.intValue();

			if( fileUploadSizeThreshold == null )
				fileUploadSizeThreshold = 0;
		}

		return fileUploadSizeThreshold;
	}

	//
	// DocumentExecutionAttributes
	//

	public LanguageManager getLanguageManager()
	{
		if( languageManager == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
			String key = prefix + ".languageManager";
			languageManager = (LanguageManager) attributes.get( key );

			if( languageManager == null )
			{
				languageManager = new LanguageManager();

				LanguageManager existing = (LanguageManager) attributes.putIfAbsent( key, languageManager );
				if( existing != null )
					languageManager = existing;
			}
		}

		return languageManager;
	}

	public ParserManager getParserManager()
	{
		if( parserManager == null )
		{
			ConcurrentMap<String, Object> attributes = getAttributes();
			String key = prefix + ".parserManager";
			parserManager = (ParserManager) attributes.get( key );

			if( parserManager == null )
			{
				parserManager = new ParserManager();

				ParserManager existing = (ParserManager) attributes.putIfAbsent( key, parserManager );
				if( existing != null )
					parserManager = existing;
			}
		}

		return parserManager;
	}

	/**
	 * The default language tag name to be used if the script doesn't specify
	 * one. Defaults to "javascript".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>defaultLanguageTag</code> in the application's {@link Context}.
	 * 
	 * @return The default language tag
	 */
	public String getDefaultLanguageTag()
	{
		if( defaultLanguageTag == null )
		{
			defaultLanguageTag = (String) getAttributes().get( prefix + ".defaultLanguageTag" );

			if( defaultLanguageTag == null )
				defaultLanguageTag = "javascript";
		}

		return defaultLanguageTag;
	}

	public boolean isPrepare()
	{
		if( prepare == null )
		{
			prepare = (Boolean) getAttributes().get( prefix + ".prepare" );

			if( prepare == null )
				prepare = true;
		}

		return prepare;
	}

	public boolean isDebug()
	{
		if( debug == null )
		{
			debug = (Boolean) getAttributes().get( prefix + ".debug" );

			if( debug == null )
				debug = false;
		}

		return debug;
	}

	public ExecutionController getExecutionController()
	{
		if( executionController == null )
			executionController = (ExecutionController) getAttributes().get( prefix + ".executionController" );

		return executionController;
	}

	public String getDefaultName()
	{
		if( defaultName == null )
		{
			defaultName = (String) getAttributes().get( prefix + ".defaultName" );

			if( defaultName == null )
				defaultName = "default";
		}

		return defaultName;
	}

	public boolean isTrailingSlashRequired()
	{
		if( trailingSlashRequired == null )
		{
			trailingSlashRequired = (Boolean) getAttributes().get( prefix + ".trailingSlashRequired" );

			if( trailingSlashRequired == null )
				trailingSlashRequired = true;
		}

		return trailingSlashRequired;
	}

	public Cache getCache()
	{
		if( cache == null )
			cache = (Cache) getAttributes().get( InstanceUtil.CACHE_ATTRIBUTE );

		return cache;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	/**
	 * The {@link Writer} used by the {@link Executable}.
	 */
	protected volatile Writer writer = new OutputStreamWriter( System.out );

	/**
	 * Same as {@link #writer}, for standard error.
	 */
	protected volatile Writer errorWriter = new OutputStreamWriter( System.err );

	/**
	 * The document source.
	 */
	protected volatile DocumentSource<Executable> documentSource;

	/**
	 * The extra document sources.
	 */
	protected volatile Iterable<DocumentSource<Executable>> extraDocumentSources;

	/**
	 * Executables might use these {@link DocumentSource} instances for
	 * importing libraries.
	 */
	protected volatile Iterable<DocumentSource<Executable>> libraryDocumentSources;

	/**
	 * The name of the global variable with which to access the document
	 * service.
	 */
	protected volatile String documentServiceName;

	/**
	 * The name of the global variable with which to access the application
	 * service.
	 */
	protected volatile String applicationServiceName;

	/**
	 * This is so we can see the source code for scripts by adding
	 * <code>?source=true</code> to the URL.
	 */
	protected volatile Boolean sourceViewable;

	/**
	 * The document formatter.
	 */
	protected volatile DocumentFormatter<Executable> documentFormatter;

	/**
	 * The default character set to be used if the client does not specify it.
	 */
	protected volatile CharacterSet defaultCharacterSet;

	/**
	 * The directory in which to place uploaded files.
	 */
	protected volatile File fileUploadDirectory;

	/**
	 * The size in bytes beyond which uploaded files will be stored to disk.
	 */
	protected volatile Integer fileUploadSizeThreshold;

	/**
	 * Cache used for caching mode.
	 */
	protected volatile Cache cache;

	/**
	 * The {@link LanguageManager} used to create the language adapters.
	 */
	protected volatile LanguageManager languageManager;

	/**
	 * The {@link ParserManager} used to create the executable parsers.
	 */
	protected volatile ParserManager parserManager;

	/**
	 * The default language tag to be used if the executable doesn't specify
	 * one.
	 */
	protected volatile String defaultLanguageTag;

	/**
	 * Whether to prepare executables.
	 */
	protected volatile Boolean prepare;

	/**
	 * Whether to enable debug for executables.
	 */
	protected volatile Boolean debug;

	/**
	 * An optional {@link ExecutionController} to be used with the scripts.
	 */
	protected volatile ExecutionController executionController;

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used.
	 */
	protected volatile String defaultName;

	/**
	 * Whether or not trailing slashes are required for all requests.
	 */
	protected volatile Boolean trailingSlashRequired;
}
