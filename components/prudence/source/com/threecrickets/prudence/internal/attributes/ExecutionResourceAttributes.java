package com.threecrickets.prudence.internal.attributes;

import java.util.concurrent.ConcurrentMap;

import org.restlet.Context;

import com.threecrickets.prudence.ExecutionResource;

/**
 * @author Tal Liron
 */
public class ExecutionResourceAttributes extends NonVolatileContextualAttributes
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 */
	public ExecutionResourceAttributes( ExecutionResource resource )
	{
		super( resource.getClass().getCanonicalName() );
		this.resource = resource;
	}

	//
	// Attributes
	//

	/**
	 * The name of the global variable with which to access the conversation
	 * service. Defaults to "conversation".
	 * <p>
	 * This setting can be configured by setting an attribute named
	 * <code>conversationServiceName</code> in the application's {@link Context}.
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

	//
	// ContextualAttributes
	//

	public ConcurrentMap<String, Object> getAttributes()
	{
		if( attributes == null )
			attributes = resource.getContext().getAttributes();

		return attributes;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The resource.
	 */
	private final ExecutionResource resource;

	/**
	 * The attributes.
	 */
	private ConcurrentMap<String, Object> attributes;

	/**
	 * The name of the global variable with which to access the conversation
	 * service.
	 */
	private String conversationServiceName;
}
