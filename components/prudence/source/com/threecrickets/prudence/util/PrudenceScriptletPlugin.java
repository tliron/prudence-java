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

package com.threecrickets.prudence.util;

import com.threecrickets.prudence.service.GeneratedTextResourceDocumentService;
import com.threecrickets.scripturian.LanguageAdapter;
import com.threecrickets.scripturian.internal.ScripturianUtil;
import com.threecrickets.scripturian.parser.ScriptletPlugin;

/**
 * A {@link ScriptletPlugin} that supports a few special tags:
 * <ul>
 * <li><b>==</b>: outputs a conversation.local with the context as the name</li>
 * <li><b>{</b>: calls
 * {@link GeneratedTextResourceDocumentService#startCapture(String)} with the
 * content as the name argument</li>
 * <li><b>}</b>: calls {@link GeneratedTextResourceDocumentService#endCapture()}
 * </li>
 * <li><b>[</b>: like "{", but conditional: will only start capturing if the
 * conversation.local is not already defined</li>
 * <li><b>]</b>: like "}", but will close the "{{{" conditional, and also output
 * the resulting capture</li>
 * </ul>
 * 
 * @author Tal Liron
 */
public class PrudenceScriptletPlugin implements ScriptletPlugin
{
	//
	// ScriptletPlugin
	//

	public String getScriptlet( String code, LanguageAdapter languageAdapter, String content )
	{
		if( "==".equals( code ) )
		{
			String language = (String) languageAdapter.getAttributes().get( LanguageAdapter.LANGUAGE_NAME );
			String name = ScripturianUtil.doubleQuotedLiteral( content.trim() );
			if( JAVASCRIPT.equals( language ) )
				return "print(conversation.locals.get(" + name + ")||\"\");";
			else if( PYTHON.equals( language ) )
				return "sys.stdout.write(conversation.locals.get(" + name + ") or \"\");";
			else if( RUBY.equals( language ) )
				return "print($conversation.locals.get(" + name + ")||\"\");";
			else if( GROOVY.equals( language ) )
				return "print(conversation.locals.get(" + name + ")||\"\");";
			else if( CLOJURE.equals( language ) )
				return "(print (or (.. conversation getLocals (get " + name + ")) \"\"))";
			else if( PHP.equals( language ) )
				return "print($conversation->locals->get(" + name + ")||\"\");";
			else if( LUA.equals( language ) )
				return "print(conversation:getLocals():get(" + name + ")or\"\");";
		}
		else if( "{".equals( code ) )
		{
			String language = (String) languageAdapter.getAttributes().get( LanguageAdapter.LANGUAGE_NAME );
			String name = ScripturianUtil.doubleQuotedLiteral( content.trim() );
			if( JAVASCRIPT.equals( language ) )
				return "document.startCapture(" + name + ");";
			else if( PYTHON.equals( language ) )
				return "document.startCapture(" + name + ");";
			else if( RUBY.equals( language ) )
				return "$document.start_capture(" + name + ");";
			else if( GROOVY.equals( language ) )
				return "document.startCapture(" + name + ");";
			else if( CLOJURE.equals( language ) )
				return "(.startCapture document " + name + ")";
			else if( PHP.equals( language ) )
				return "$document->startCapture(" + name + ");";
			else if( LUA.equals( language ) )
				return "document:startCapture(" + name + ");";
		}
		else if( "}".equals( code ) )
		{
			String language = (String) languageAdapter.getAttributes().get( LanguageAdapter.LANGUAGE_NAME );
			if( JAVASCRIPT.equals( language ) )
				return "document.endCapture();";
			else if( PYTHON.equals( language ) )
				return "document.endCapture();";
			else if( RUBY.equals( language ) )
				return "$document.end_capture();";
			else if( GROOVY.equals( language ) )
				return "document.endCapture();";
			else if( CLOJURE.equals( language ) )
				return "(.endCapture document)";
			else if( PHP.equals( language ) )
				return "$document->endCapture();";
			else if( LUA.equals( language ) )
				return "document:endCapture();";
		}
		else if( "[".equals( code ) )
		{
			String language = (String) languageAdapter.getAttributes().get( LanguageAdapter.LANGUAGE_NAME );
			String name = ScripturianUtil.doubleQuotedLiteral( content.trim() );
			if( JAVASCRIPT.equals( language ) )
				return "if(null!==conversation.locals.get(" + name + ")){print(conversation.locals.get(" + name + "))}else{document.startCapture(" + name + ");";
		}
		else if( "]".equals( code ) )
		{
			String language = (String) languageAdapter.getAttributes().get( LanguageAdapter.LANGUAGE_NAME );
			if( JAVASCRIPT.equals( language ) )
				return "print(document.endCapture())}";
		}
		return "";
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final String JAVASCRIPT = "JavaScript";

	private static final String PYTHON = "Python";

	private static final String RUBY = "Ruby";

	private static final String GROOVY = "Groovy";

	private static final String CLOJURE = "Clojure";

	private static final String PHP = "PHP";

	private static final String LUA = "Lua";
}
