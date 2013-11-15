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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CacheDirective;
import org.restlet.data.ClientInfo;
import org.restlet.data.Conditions;
import org.restlet.data.Cookie;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Product;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.data.Warning;
import org.restlet.representation.StringRepresentation;

import com.threecrickets.prudence.DelegatedStatusService;
import com.threecrickets.prudence.SourceCodeResource;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.GlobalScope;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;
import com.threecrickets.scripturian.exception.PreparationException;
import com.threecrickets.scripturian.exception.StackFrame;

/**
 * An HTML representation of lots of Prudence and Restlet state useful for
 * debugging.
 * <p>
 * <i>"Restlet" is a registered trademark of <a
 * href="http://www.restlet.org/about/legal">Restlet S.A.S.</a>.</i>
 * 
 * @author Tal Liron
 * @see DelegatedStatusService
 */
public class DebugRepresentation extends StringRepresentation
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param status
	 *        The status
	 * @param request
	 *        The request
	 * @param response
	 *        The response
	 */
	public DebugRepresentation( Status status, Request request, Response response, String sourceCodeUri )
	{
		super( null, MediaType.TEXT_HTML );

		Throwable throwable = status.getThrowable();
		ClientInfo clientInfo = request.getClientInfo();
		Conditions conditions = request.getConditions();

		StringBuilder html = new StringBuilder();

		html.append( "<html>\n" );
		html.append( "<head>\n" );
		html.append( "<title>Prudence Debug Page</title>\n" );
		html.append( "<style>\n" );
		html.append( "  body { font-family: Ubuntu, Lucida Sans Unicode, Lucida Grande, Verdana, Geneva, Tahoma, Arial, Helvetica, sans-serif; font-size: 15px }\n" );
		html.append( "  h1, h2 { font-family: Georgia, Utopia, Palatino, Times New Roman, Times, serif; color: #994; font-style: italic }\n" );
		html.append( "  h1 { font-size: 15px + 10px; text-align: center }\n" );
		html.append( "  h2 { font-size: 15px + 2px; margin-top: 28px; margin-bottom: 14px }\n" );
		html.append( "  h3 { font-size: 15px; font-weight: bold; margin-top: 24px; margin-bottom: 6px }\n" );
		html.append( "  div.name { font-style: italic }\n" );
		html.append( "  div.value { padding-left: 22px }\n" );
		html.append( "</style>\n" );
		html.append( "</head>\n" );
		html.append( "<body>\n" );

		html.append( "<h1>Prudence Debug Page</h1>\n" );

		Iterable<StackFrame> stack = null;
		Executable executable = null;

		if( throwable instanceof ExecutionException )
		{
			html.append( "<h2>Cause: Scripturian Execution Error</h2>\n" );
			ExecutionException executionException = (ExecutionException) throwable;
			stack = executionException.getStack();
			executable = executionException.getExecutable();
		}
		else if( throwable instanceof PreparationException )
		{
			html.append( "<h2>Cause: Scripturian Preparation Error</h2>\n" );
			PreparationException preparationException = (PreparationException) throwable;
			stack = preparationException.getStack();
			executable = preparationException.getExecutable();
		}
		else if( throwable instanceof ParsingException )
		{
			html.append( "<h2>Cause: Scripturian Parsing Error</h2>\n" );
			ParsingException parsingException = (ParsingException) throwable;
			stack = parsingException.getStack();
		}
		else
		{
			html.append( "<h2>Cause: " );
			appendSafe( html, throwable.getClass().getCanonicalName() );
			html.append( "</h2>\n" );
		}

		html.append( "<h3>" );
		appendSafe( html, throwable.getMessage() );
		html.append( "</h3>\n" );
		html.append( "<div id=\"error\">\n" );

		if( stack != null )
		{
			for( StackFrame stackFrame : stack )
			{
				html.append( "  " );
				int lineNumber = stackFrame.getLineNumber();
				if( ( sourceCodeUri != null ) && sourceCodeUri.length() > 0 )
				{
					html.append( "<a href=\"" );
					html.append( request.getRootRef() );
					html.append( sourceCodeUri );
					html.append( '?' );
					html.append( SourceCodeResource.DOCUMENT );
					html.append( '=' );
					html.append( Reference.encode( stackFrame.getDocumentName() ) );
					if( lineNumber >= 0 )
					{
						html.append( '&' );
						html.append( SourceCodeResource.HIGHLIGHT );
						html.append( '=' );
						html.append( Reference.encode( String.valueOf( lineNumber ) ) );
					}
					html.append( "\">" );
				}
				appendSafe( html, stackFrame.getDocumentName() );
				if( stackFrame.getLineNumber() >= 0 )
				{
					html.append( " @ " );
					html.append( stackFrame.getLineNumber() );
				}
				if( stackFrame.getColumnNumber() >= 0 )
				{
					html.append( "," );
					html.append( stackFrame.getColumnNumber() );
				}
				if( ( sourceCodeUri != null ) && sourceCodeUri.length() > 0 )
					html.append( "</a>" );
				html.append( "<br />\n" );
			}
		}

		html.append( "</div>\n" );

		if( !request.getWarnings().isEmpty() )
		{
			html.append( "<h2>Warnings</h2>\n" );
			html.append( "<div id=\"warnings\">\n" );
			for( Warning warning : request.getWarnings() )
			{
				appendName( html, warning.getDate() );
				appendValue( html, warning.getText(), " from ", warning.getAgent(), " (", warning.getStatus(), ")" );
			}
			html.append( "</div>\n" );
		}

		html.append( "<h2>Request</h2>\n" );

		appendName( html, "Time" );
		appendValue( html, request.getDate() );
		appendName( html, "Protocol" );
		appendValue( html, request.getProtocol() );
		appendName( html, "Method" );
		appendValue( html, request.getMethod() );

		html.append( "<h3>URIs</h3>\n" );
		html.append( "<div id=\"uris\">\n" );
		appendName( html, "Resource" );
		appendValue( html, request.getResourceRef() );
		if( request.getReferrerRef() != null )
		{
			appendName( html, "Referrer" );
			appendValue( html, request.getReferrerRef() );
		}
		html.append( "<br />\n" );
		Reference captiveReference = CapturingRedirector.getCapturedReference( request );
		if( captiveReference != null )
		{
			appendName( html, "Captive" );
			appendValue( html, captiveReference );
		}
		if( !request.getResourceRef().equals( request.getOriginalRef() ) )
		{
			appendName( html, "Original" );
			appendValue( html, request.getOriginalRef() );
		}
		appendName( html, "Root" );
		appendValue( html, request.getRootRef() );
		appendName( html, "Host" );
		appendValue( html, request.getHostRef() );
		html.append( "</div>\n" );

		Form form = request.getResourceRef().getQueryAsForm();
		if( !form.isEmpty() )
		{
			html.append( "<h3>Query Parameters</h3>\n" );
			html.append( "<div id=\"query-parameters\">\n" );
			for( Parameter parameter : form )
			{
				appendName( html, parameter.getName() );
				appendValue( html, parameter.getValue() );
			}
			html.append( "</div>\n" );
		}

		html.append( "<h3>Preferences</h3>\n" );
		html.append( "<div id=\"preferences\">\n" );
		appendName( html, "Media Types" );
		appendValue( html, clientInfo.getAcceptedMediaTypes() );
		appendName( html, "Encodings" );
		appendValue( html, clientInfo.getAcceptedEncodings() );
		appendName( html, "Character Sets" );
		appendValue( html, clientInfo.getAcceptedCharacterSets() );
		appendName( html, "Languages" );
		appendValue( html, clientInfo.getAcceptedLanguages() );
		html.append( "</div>\n" );

		if( conditions.hasSome() )
		{
			html.append( "<h3>Conditions</h3>\n" );
			html.append( "<div id=\"conditions\">\n" );
			if( conditions.getModifiedSince() != null )
			{
				appendName( html, "Modified Since" );
				appendValue( html, conditions.getModifiedSince() );
			}
			if( conditions.getUnmodifiedSince() != null )
			{
				appendName( html, "Unmodified Since" );
				appendValue( html, conditions.getUnmodifiedSince() );
			}
			if( conditions.getRangeDate() != null )
			{
				appendName( html, "Range Date" );
				appendValue( html, conditions.getRangeDate() );
			}
			if( !conditions.getMatch().isEmpty() )
			{
				appendName( html, "Match Tags" );
				appendValue( html, conditions.getMatch() );
			}
			if( !conditions.getNoneMatch().isEmpty() )
			{
				appendName( html, "None-Match Tags" );
				appendValue( html, conditions.getNoneMatch() );
			}
			if( conditions.getRangeTag() != null )
			{
				appendName( html, "Range Tag" );
				appendValue( html, conditions.getRangeTag() );
			}
			html.append( "</div>\n" );
		}

		if( !request.getCacheDirectives().isEmpty() )
		{
			boolean has = false;
			for( CacheDirective cacheDirective : request.getCacheDirectives() )
			{
				if( ( cacheDirective.getValue() != null ) && ( cacheDirective.getValue().length() > 0 ) )
				{
					has = true;
					break;
				}
			}
			if( has )
			{
				html.append( "<h3>Cache Directives</h3>\n" );
				html.append( "<div id=\"cache-directives\">\n" );
				for( CacheDirective cacheDirective : request.getCacheDirectives() )
				{
					if( ( cacheDirective.getValue() != null ) && ( cacheDirective.getValue().length() > 0 ) )
					{
						appendName( html, cacheDirective.getName() );
						appendValue( html, cacheDirective.getValue() );
					}
				}
				html.append( "</div>\n" );
			}
		}

		html.append( "<h3>Client</h3>\n" );
		html.append( "<div id=\"client\">\n" );
		appendName( html, "Address" );
		appendValue( html, clientInfo.getAddress(), " port ", clientInfo.getPort() );
		appendName( html, "Upstream Address" );
		appendValue( html, clientInfo.getUpstreamAddress() );
		if( !clientInfo.getForwardedAddresses().isEmpty() )
		{
			appendName( html, "Forwarded Addresses" );
			appendValue( html, clientInfo.getForwardedAddresses() );
		}
		appendName( html, "Agent" );
		appendValue( html, clientInfo.getAgentName(), " ", clientInfo.getAgentVersion() );
		html.append( "<br />\n" );
		appendName( html, "Products\n" );
		for( Product product : clientInfo.getAgentProducts() )
		{
			if( product.getComment() != null )
				appendValue( html, product.getName(), " ", product.getVersion(), " (", product.getComment(), ")" );
			else
				appendValue( html, product.getName(), " ", product.getVersion() );
		}
		if( clientInfo.getFrom() != null )
		{
			appendName( html, "From" );
			appendValue( html, clientInfo.getFrom() );
		}
		html.append( "</div>\n" );

		if( !request.getCookies().isEmpty() )
		{
			html.append( "<h3>Cookies</h3>\n" );
			html.append( "<div id=\"cookies\">\n" );
			for( Cookie cookie : request.getCookies() )
			{
				appendName( html, cookie.getName() );
				if( cookie.getDomain() != null )
					appendValue( html, cookie.getValue(), " (", cookie.getVersion(), ") for ", cookie.getDomain(), " ", cookie.getPath() );
				else
					appendValue( html, cookie.getValue(), " (", cookie.getVersion(), ")" );
			}
			html.append( "</div>\n" );
		}

		if( request.isEntityAvailable() )
		{
			html.append( "<h3>Payload</h3>\n" );
			html.append( "<div id=\"payload\">\n" );
			appendSafe( html, request.getEntity() );
			html.append( "</div>\n" );
		}

		ConcurrentMap<String, Object> attributes = request.getAttributes();
		if( !attributes.isEmpty() )
		{
			html.append( "<h2>conversation.locals</h2>\n" );
			html.append( "<div id=\"conversation-locals\">\n" );
			appendMap( html, attributes );
			html.append( "</div>\n" );
		}

		attributes = response.getAttributes();
		if( !attributes.isEmpty() )
		{
			html.append( "<h2>Response Attributes</h2>\n" );
			html.append( "<div id=\"response-attributes\">\n" );
			appendMap( html, attributes );
			html.append( "</div>\n" );
		}

		Application application = Application.getCurrent();
		if( application != null )
		{
			html.append( "<h2>Application</h2>\n" );
			html.append( "<div id=\"application\">\n" );
			String name = application.getName();
			if( name != null )
			{
				appendName( html, "Name" );
				appendValue( html, name );
			}
			String description = application.getDescription();
			if( description != null )
			{
				appendName( html, "Description" );
				appendValue( html, description );
			}
			String owner = application.getOwner();
			if( owner != null )
			{
				appendName( html, "Owner" );
				appendValue( html, owner );
			}
			String author = application.getAuthor();
			if( author != null )
			{
				appendName( html, "Author" );
				appendValue( html, author );
			}
			appendName( html, "Class" );
			appendValue( html, application.getClass().getName() );
			html.append( "</div>\n" );

			if( !application.getContext().getAttributes().isEmpty() )
			{
				html.append( "<h2>application.globals</h2>\n" );
				html.append( "<div id=\"application-globals\">\n" );
				appendMap( html, application.getContext().getAttributes() );
				html.append( "</div>\n" );
			}
		}

		Component component = InstanceUtil.getComponent();
		if( component != null )
		{
			if( !component.getContext().getAttributes().isEmpty() )
			{
				html.append( "<h2>application.sharedGlobals</h2>\n" );
				html.append( "<div id=\"application-shared-globals\">\n" );
				appendMap( html, component.getContext().getAttributes() );
				html.append( "</div>\n" );
			}
		}

		if( !GlobalScope.getInstance().getAttributes().isEmpty() )
		{
			html.append( "<h2>executable.globals</h2>\n" );
			html.append( "<div id=\"executable-globals\">\n" );
			appendMap( html, GlobalScope.getInstance().getAttributes() );
			html.append( "</div>\n" );
		}

		if( executable != null )
		{
			html.append( "<h2>Executable Attributes</h2>\n" );
			html.append( "<h3>" );
			html.append( executable.getDocumentName() );
			html.append( "</h3>\n" );
			html.append( "<div id=\"executable-attributes\">\n" );
			appendMap( html, executable.getAttributes() );
			html.append( "</div>\n" );
		}

		ExecutionContext executionContext = ExecutionContext.getCurrent();
		if( executionContext != null )
		{
			html.append( "<h2>Execution Context Attributes</h2>\n" );
			html.append( "<div id=\"execution-context-attributes\">\n" );
			appendMap( html, executionContext.getAttributes() );
			html.append( "</div>\n" );
		}

		if( throwable.getStackTrace() != null )
		{
			html.append( "<h2>Machine Stack Trace</h2>\n" );
			html.append( "<div id=\"machine-stack-trace\">\n" );
			while( throwable != null )
			{
				html.append( "  <h3>" );
				appendSafe( html, throwable.getClass().getCanonicalName() );
				html.append( "</h3>\n" );
				for( StackTraceElement stackTraceElement : throwable.getStackTrace() )
				{
					html.append( "    " );
					appendSafe( html, stackTraceElement.getClassName() );
					html.append( '.' );
					appendSafe( html, stackTraceElement.getMethodName() );
					if( stackTraceElement.getFileName() != null )
					{
						html.append( " (" );
						appendSafe( html, stackTraceElement.getFileName() );
						html.append( ':' );
						html.append( stackTraceElement.getLineNumber() );
						html.append( ')' );
					}
					html.append( "<br />\n" );
				}

				if( throwable == throwable.getCause() )
					// Avoid endless loops
					break;

				throwable = throwable.getCause();
			}
			html.append( "</div>\n" );
		}

		html.append( "</body>\n" );
		html.append( "</html>\n" );

		setText( html.toString() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Escapes HTML.
	 * 
	 * @param html
	 *        The HTML builder
	 * @param string
	 *        Value to append
	 */
	private static void appendSafe( StringBuilder html, Object string )
	{
		if( string != null )
			html.append( string.toString().replace( "<", "&lt;" ).replace( ">", "&gt;" ) );
	}

	/**
	 * A div with CSS class "name".
	 * 
	 * @param html
	 *        The HTML builder
	 * @param string
	 *        Value to append
	 */
	private static void appendName( StringBuilder html, Object string )
	{
		html.append( "  <div class=\"name\">" );
		appendSafe( html, string );
		html.append( "</div> " );
	}

	/**
	 * A div with CSS class "value".
	 * 
	 * @param html
	 *        The HTML builder
	 * @param strings
	 *        Values to append
	 */
	private static void appendValue( StringBuilder html, Object... strings )
	{
		html.append( "<div class=\"value\">" );
		for( Object string : strings )
			appendSafe( html, string );
		html.append( "</div>\n" );
	}

	/**
	 * Appends all values in a map.
	 * 
	 * @param html
	 *        The HTML builder
	 * @param map
	 *        The map to append
	 */
	private static void appendMap( StringBuilder html, Map<String, Object> map )
	{
		for( Map.Entry<String, Object> entry : CollectionUtil.sortedMap( map ).entrySet() )
		{
			appendName( html, entry.getKey() );
			if( entry.getValue() instanceof Collection<?> )
			{
				html.append( "\n" );
				for( Object o : (Collection<?>) entry.getValue() )
				{
					html.append( "    " );
					if( o instanceof Parameter )
					{
						Parameter parameter = (Parameter) o;
						appendValue( html, parameter.getName(), " = ", parameter.getValue() );
					}
					else
						appendValue( html, o );
				}
			}
			else
			{
				appendValue( html, entry.getValue() );
			}
		}
	}
}
