package com.threecrickets.prudence;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

import com.threecrickets.prudence.cache.Cache;
import com.threecrickets.prudence.internal.JygmentsDocumentFormatter;
import com.threecrickets.prudence.internal.attributes.ExecutionResourceAttributes;
import com.threecrickets.prudence.service.ApplicationService;
import com.threecrickets.prudence.service.ExecutionResourceConversationService;
import com.threecrickets.prudence.service.ExecutionResourceDocumentService;
import com.threecrickets.scripturian.Executable;
import com.threecrickets.scripturian.ExecutionContext;
import com.threecrickets.scripturian.ExecutionController;
import com.threecrickets.scripturian.LanguageManager;
import com.threecrickets.scripturian.document.DocumentDescriptor;
import com.threecrickets.scripturian.document.DocumentFormatter;
import com.threecrickets.scripturian.document.DocumentSource;
import com.threecrickets.scripturian.exception.DocumentException;
import com.threecrickets.scripturian.exception.DocumentNotFoundException;
import com.threecrickets.scripturian.exception.ExecutionException;
import com.threecrickets.scripturian.exception.ParsingException;

/**
 * Restlet resource which executes a "text with scriptlets" Scripturian
 * {@link Executable} document for the POST verb and redirects its standard
 * output to a {@link StringRepresentation}.
 * <p>
 * <code>document</code>, <code>application</code> and <code>conversation</code>
 * services are available as global variables in scriptlets. See
 * {@link ExecutionResourceDocumentService}, {@link ApplicationService} and
 * {@link ExecutionResourceConversationService}.
 * <p>
 * Summary of settings configured via the application's {@link Context}:
 * <ul>
 * <li>
 * <code>com.threecrickets.prudence.cache:</code> {@link Cache}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.applicationServiceName</code>
 * : Defaults to "application".</li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.conversationServiceName</code>
 * : Defaults to "conversation".</li>
 * <code>com.threecrickets.prudence.ExecutionResource.debug:</code>
 * {@link Boolean}, defaults to false.</li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.defaultCharacterSet:</code>
 * {@link CharacterSet}, defaults to {@link CharacterSet#UTF_8}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.defaultLanguageTag:</code>
 * {@link String}, defaults to "javascript".</li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.defaultName:</code>
 * {@link String}, defaults to "default".</li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.documentFormatter:</code>
 * {@link DocumentFormatter}. Defaults to a {@link JygmentsDocumentFormatter}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.documentServiceName</code>
 * : Defaults to "document".</li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.documentSource:</code>
 * {@link DocumentSource}. <b>Required.</b></li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.executionController:</code>
 * {@link ExecutionController}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.fileUploadDirectory:</code>
 * {@link File}. Defaults to "uploads" under the application root.</li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.fileUploadSizeThreshold:</code>
 * {@link Integer}, defaults to zero.</li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.languageManager:</code>
 * {@link LanguageManager}, defaults to a new instance.</li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.libraryDocumentSources:</code>
 * {@link Iterable} of {@link DocumentSource} of {@link Executable}.</li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.prepare:</code>
 * {@link Boolean}, defaults to true.</li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.sourceViewable:</code>
 * {@link Boolean}, defaults to false.</li>
 * <li>
 * <code>com.threecrickets.prudence.ExecutionResource.trailingSlashRequired:</code>
 * {@link Boolean}, defaults to true.</li>
 * </ul>
 * <p>
 * <i>"Restlet" is a registered trademark of <a
 * href="http://www.restlet.org/about/legal">Noelios Technologies</a>.</i>
 * 
 * @author Tal Liron
 */
public class ExecutionResource extends ServerResource
{
	//
	// Attributes
	//

	/**
	 * The attributes as configured in the {@link Application} context.
	 * 
	 * @return The attributes
	 */
	public ExecutionResourceAttributes getAttributes()
	{
		return attributes;
	}

	//
	// ServerResource
	//

	/**
	 * Initializes the resource.
	 */
	@Override
	protected void doInit() throws ResourceException
	{
		super.doInit();
		setAnnotated( false );
	}

	@Override
	public Representation post( Representation entity ) throws ResourceException
	{
		return execute( entity, null );
	}

	@Override
	public Representation post( Representation entity, Variant variant ) throws ResourceException
	{
		return execute( entity, variant );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The document name prefix.
	 */
	private static final String DOCUMENT_NAME_PREFIX = "_PRUDENCE_EXECUTION_RESOURCE_";

	/**
	 * The attributes as configured in the {@link Application} context.
	 */
	private final ExecutionResourceAttributes attributes = new ExecutionResourceAttributes( this );

	/**
	 * Calculate a document name based on the code.
	 * 
	 * @param code
	 *        The code
	 * @return The document name
	 */
	private static String getDocumentName( String code )
	{
		// TODO: this is not guaranteed to be unique!

		return DOCUMENT_NAME_PREFIX + code.hashCode();
	}

	/**
	 * Executes code in the conversation.
	 * 
	 * @param entity
	 *        The entity
	 * @param variant
	 *        The variant
	 * @return The code output
	 */
	private StringRepresentation execute( Representation entity, Variant variant )
	{
		try
		{
			String code = entity.getText();
			String documentName = getDocumentName( code );

			DocumentDescriptor<Executable> documentDescriptor = attributes.createDocumentOnce( documentName, code );

			StringWriter output = new StringWriter();
			ExecutionContext executionContext = new ExecutionContext( output, output );
			attributes.addLibraryLocations( executionContext );

			ExecutionResourceConversationService conversationService = new ExecutionResourceConversationService( this, entity, variant, attributes.getDefaultCharacterSet() );

			// Default media type
			conversationService.setMediaType( MediaType.TEXT_PLAIN );

			executionContext.getServices().put( attributes.getDocumentServiceName(), new ExecutionResourceDocumentService( this, documentDescriptor ) );
			executionContext.getServices().put( attributes.getApplicationServiceName(), ApplicationService.create() );
			executionContext.getServices().put( attributes.getConversationServiceName(), conversationService );

			try
			{
				documentDescriptor.getDocument().execute( executionContext, null, attributes.getExecutionController() );

				StringRepresentation representation = new StringRepresentation( output.toString(), conversationService.getMediaType(), conversationService.getLanguage(), conversationService.getCharacterSet() );

				representation.setTag( conversationService.getTag() );
				representation.setExpirationDate( conversationService.getExpirationDate() );
				representation.setModificationDate( conversationService.getModificationDate() );
				representation.setDisposition( conversationService.getDisposition() );

				return representation;
			}
			catch( ExecutionException x )
			{
				if( getResponse().getStatus().isSuccess() )
					// An unintended exception
					throw new ResourceException( x );
				else
					// This was an intended exception, so we will preserve the
					// status code
					return null;
			}
			finally
			{
				executionContext.release();
			}
		}
		catch( DocumentNotFoundException x )
		{
			throw new ResourceException( Status.CLIENT_ERROR_NOT_FOUND );
		}
		catch( ParsingException x )
		{
			throw new ResourceException( x );
		}
		catch( DocumentException x )
		{
			throw new ResourceException( x );
		}
		catch( IOException x )
		{
			throw new ResourceException( x );
		}
	}
}
