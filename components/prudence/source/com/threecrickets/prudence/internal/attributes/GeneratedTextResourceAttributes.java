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

package com.threecrickets.prudence.internal.attributes;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;
import org.restlet.resource.ResourceException;

import com.threecrickets.prudence.DelegatedScriptletPlugin;
import com.threecrickets.prudence.GeneratedTextResource;
import com.threecrickets.prudence.util.InstanceUtil;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ParsingContext;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.DocumentNotFoundException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.scripturian.parser.ScriptletPlugin;
import com.threecrickets.scripturian.parser.ScriptletsParser;

/**
 * @author Tal Liron
 */
public class GeneratedTextResourceAttributes extends ResourceContextualAttributes
{
	//
	// Resource
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 */
	public GeneratedTextResourceAttributes( GeneratedTextResource resource )
	{
		super( resource );
	}

	//
	// Attributes
	//

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used. This allows you to
	 * use the directory structure to create nice URLs without relying on
	 * filenames. Defaults to "index".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>defaultName</code> in the application's {@link Context}.
	 * 
	 * @return The default name
	 */
	public String getDefaultIncludedName()
	{
		if( defaultIncludedName == null )
		{
			defaultIncludedName = (String) getAttributes().get( prefix + ".defaultIncludedName" );

			if( defaultIncludedName == null )
				defaultIncludedName = "index";
		}

		return defaultIncludedName;
	}

	/**
	 * The name of the global variable with which to access the conversation
	 * service. Defaults to "conversation".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>conversationServiceName</code> in the application's {@link Context}
	 * .
	 * 
	 * @return The conversation service name
	 */
	public String getConversationServiceName()
	{
		if( conversationServiceName == null )
		{
			conversationServiceName = (String) getAttributes().get( prefix + ".conversationServiceName" );

			if( conversationServiceName == null )
				conversationServiceName = "conversation";
		}

		return conversationServiceName;
	}

	/**
	 * The scriptlet plugins to use during parsing.
	 * 
	 * @return The scriptlet plugins or null
	 * @see DelegatedScriptletPlugin
	 */
	@SuppressWarnings("unchecked")
	public ConcurrentMap<String, ScriptletPlugin> getScriptletPlugins()
	{
		if( scriptletPlugins == null )
		{
			scriptletPlugins = new ConcurrentHashMap<String, ScriptletPlugin>();

			Map<String, Object> potentialScriptletPlugins = (ConcurrentMap<String, Object>) getAttributes().get( prefix + ".scriptletPlugins" );
			if( potentialScriptletPlugins != null )
			{
				for( Map.Entry<String, Object> entry : potentialScriptletPlugins.entrySet() )
				{
					Object scriptletPlugin = entry.getValue();
					if( scriptletPlugin instanceof ScriptletPlugin )
						scriptletPlugins.put( entry.getKey(), (ScriptletPlugin) scriptletPlugin );
					else
						// Create delegated scriptlet plugin
						scriptletPlugins.put( entry.getKey(), new DelegatedScriptletPlugin( scriptletPlugin.toString(), resource.getContext() ) );
				}
			}
		}

		return scriptletPlugins;
	}

	/**
	 * Gets a document descriptor from our supported document sources.
	 * 
	 * @param documentName
	 *        The document name
	 * @param includeExtraSources
	 *        Whether to include {@link #getExtraDocumentSources()} in the
	 *        search
	 * @return A document descriptor
	 * @throws DocumentException
	 *         In case of a Scripturian document retrieval error
	 */
	public DocumentDescriptor<Executable> getDocument( String documentName, boolean includeExtraSources ) throws DocumentException
	{
		DocumentSource<Executable> documentSource = getDocumentSource();
		Iterator<DocumentSource<Executable>> iterator = null;
		while( true )
		{
			try
			{
				if( documentSource == null )
					throw new DocumentNotFoundException( documentName );

				return documentSource.getDocument( documentName );
			}
			catch( DocumentNotFoundException x )
			{
				if( ( ( iterator == null ) || !iterator.hasNext() ) && includeExtraSources )
				{
					Iterable<DocumentSource<Executable>> sources = getExtraDocumentSources();
					iterator = sources != null ? sources.iterator() : null;
					includeExtraSources = false;
				}

				if( ( iterator == null ) || !iterator.hasNext() )
					throw new DocumentNotFoundException( documentName );

				documentSource = iterator.next();
			}
		}
	}

	//
	// NonVolatileContextualAttributes
	//

	@Override
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

	//
	// ContextualAttributes
	//

	@Override
	public String validateDocumentName( String documentName ) throws ResourceException
	{
		return validateDocumentName( documentName, getDefaultIncludedName() );
	}

	@Override
	public DocumentDescriptor<Executable> createDocumentOnce( String documentName, String parserName, boolean includeMainSource, boolean includeExtraSources, boolean includeLibrarySources )
		throws ParsingException, DocumentException
	{
		ParsingContext parsingContext = new ParsingContext();
		parsingContext.setLanguageManager( getLanguageManager() );
		parsingContext.setParserManager( getParserManager() );
		parsingContext.setDefaultLanguageTag( getDefaultLanguageTag() );
		parsingContext.setPrepare( isPrepare() );
		parsingContext.setDebug( isDebug() );
		parsingContext.getAttributes().put( ScriptletsParser.PLUGINS_ATTRIBUTE, getScriptletPlugins() );
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

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * If the URL points to a directory rather than a file, and that directory
	 * contains a file with this name, then it will be used.
	 */
	private String defaultIncludedName;

	/**
	 * The name of the global variable with which to access the conversation
	 * service.
	 */
	private String conversationServiceName;

	/**
	 * The scriptlet plugins used during parsing.
	 */
	private ConcurrentMap<String, ScriptletPlugin> scriptletPlugins;
}
