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

package com.threecrickets.prudence.util;

import org.restlet.data.ChallengeScheme;
import org.restlet.engine.Engine;
import org.restlet.engine.security.AuthenticatorHelper;

/**
 * Utility class for registering custom Restlet authentication schemes.
 * 
 * @author Tal Liron
 */
public class CustomAuthenticatorHelper extends AuthenticatorHelper
{
	//
	// Static operations
	//

	/**
	 * Makes sure an authenticator is registered with the current Restlet
	 * engine.
	 * 
	 * @param challengeScheme
	 *        The challenge scheme
	 * @param clientSide
	 *        Indicates if client side authentication is supported
	 * @param serverSide
	 *        Indicates if server side authentication is supported
	 */
	public static void ensureRegistered( ChallengeScheme challengeScheme, boolean clientSide, boolean serverSide )
	{
		ensureRegistered( challengeScheme, clientSide, serverSide, Engine.getInstance() );
	}

	/**
	 * Makes sure an authenticator is registered with the current Restlet
	 * engine.
	 * 
	 * @param challengeSchemeName
	 *        The challenge scheme name
	 * @param clientSide
	 *        Indicates if client side authentication is supported
	 * @param serverSide
	 *        Indicates if server side authentication is supported
	 */
	public static void ensureRegistered( String challengeSchemeName, boolean clientSide, boolean serverSide )
	{
		ensureRegistered( challengeSchemeName, clientSide, serverSide, Engine.getInstance() );
	}

	/**
	 * Makes sure an authenticator is registered..
	 * 
	 * @param challengeScheme
	 *        The challenge scheme
	 * @param clientSide
	 *        Indicates if client side authentication is supported
	 * @param serverSide
	 *        Indicates if server side authentication is supported
	 * @param engine
	 *        The Restlet engine
	 */
	public static void ensureRegistered( ChallengeScheme challengeScheme, boolean clientSide, boolean serverSide, Engine engine )
	{
		if( engine.findHelper( challengeScheme, clientSide, serverSide ) == null )
			engine.getRegisteredAuthenticators().add( new CustomAuthenticatorHelper( challengeScheme, clientSide, serverSide ) );
	}

	/**
	 * Makes sure an authenticator is registered..
	 * 
	 * @param challengeSchemeName
	 *        The challenge scheme name
	 * @param clientSide
	 *        Indicates if client side authentication is supported
	 * @param serverSide
	 *        Indicates if server side authentication is supported
	 * @param engine
	 *        The Restlet engine
	 */
	public static void ensureRegistered( String challengeSchemeName, boolean clientSide, boolean serverSide, Engine engine )
	{
		ensureRegistered( ChallengeScheme.valueOf( challengeSchemeName ), clientSide, serverSide, engine );
	}

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param challengeScheme
	 *        The challenge scheme
	 * @param clientSide
	 *        Indicates if client side authentication is supported
	 * @param serverSide
	 *        Indicates if server side authentication is supported
	 */
	public CustomAuthenticatorHelper( ChallengeScheme challengeScheme, boolean clientSide, boolean serverSide )
	{
		super( challengeScheme, clientSide, serverSide );
	}

	/**
	 * Constructor.
	 * 
	 * @param challengeSchemeName
	 *        The challenge scheme name
	 * @param clientSide
	 *        Indicates if client side authentication is supported
	 * @param serverSide
	 *        Indicates if server side authentication is supported
	 */
	public CustomAuthenticatorHelper( String challengeSchemeName, boolean clientSide, boolean serverSide )
	{
		super( ChallengeScheme.valueOf( challengeSchemeName ), clientSide, serverSide );
	}

	//
	// Operations
	//

	/**
	 * Makes sure our authenticator is registered with the current Restlet
	 * engine.
	 */
	public void ensureRegistered()
	{
		ensureRegistered( Engine.getInstance() );
	}

	/**
	 * Makes sure our authenticator is registered.
	 * 
	 * @param engine
	 *        The Restlet engine
	 */
	public void ensureRegistered( Engine engine )
	{
		if( engine.findHelper( getChallengeScheme(), isClientSide(), isServerSide() ) == null )
			engine.getRegisteredAuthenticators().add( this );
	}
}
