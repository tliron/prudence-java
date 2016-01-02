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

package com.threecrickets.prudence.service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.restlet.data.CacheDirective;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Tag;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;

import com.threecrickets.prudence.DelegatedResource;
import com.threecrickets.prudence.internal.CachingUtil;

/**
 * Conversation service exposed to executables.
 * 
 * @author Tal Liron
 * @see DelegatedResource
 */
public class DelegatedResourceConversationService extends ResourceConversationServiceBase<DelegatedResource>
{
	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *        The resource
	 * @param entity
	 *        The entity or null
	 * @param preferences
	 *        The negotiated client preferences or null
	 * @param defaultCharacterSet
	 *        The character set to use if unspecified by variant
	 */
	public DelegatedResourceConversationService( DelegatedResource resource, Representation entity, Variant preferences, CharacterSet defaultCharacterSet )
	{
		super( resource, entity, preferences, defaultCharacterSet, CachingUtil.SUPPORTED_ENCODINGS, resource.getAttributes().getFileUploadSizeThreshold(), resource.getAttributes().getFileUploadDirectory() );
	}

	//
	// Attributes
	//

	/**
	 * The expiration date.
	 * 
	 * @return The date or null if not set
	 * @see #setExpirationDate(Date)
	 */
	public Date getExpirationDate()
	{
		return expirationDate;
	}

	/**
	 * @param expirationDate
	 *        The date or null
	 * @see #getExpirationDate()
	 */
	public void setExpirationDate( Date expirationDate )
	{
		this.expirationDate = expirationDate;
	}

	/**
	 * @return The timestamp or 0 if not set
	 * @see #setExpirationDate(Date)
	 */
	public long getExpirationTimestamp()
	{
		return expirationDate != null ? expirationDate.getTime() : 0L;
	}

	/**
	 * @param expirationTimestamp
	 *        The timestamp
	 * @see #setExpirationDate(Date)
	 */
	public void setExpirationTimestamp( long expirationTimestamp )
	{
		this.expirationDate = new Date( expirationTimestamp );
	}

	/**
	 * The modification date.
	 * 
	 * @return The date or null if not set
	 * @see #setModificationDate(Date)
	 */
	public Date getModificationDate()
	{
		return modificationDate;
	}

	/**
	 * @param modificationDate
	 *        The date or null
	 * @see #getModificationDate()
	 */
	public void setModificationDate( Date modificationDate )
	{
		this.modificationDate = modificationDate;
	}

	/**
	 * @return The timestamp or 0 if not set
	 * @see #setModificationDate(Date)
	 */
	public long getModificationTimestamp()
	{
		return modificationDate != null ? modificationDate.getTime() : 0L;
	}

	/**
	 * @param modificationTimestamp
	 *        The timestamp
	 * @see #setModificationDate(Date)
	 */
	public void setModificationTimestamp( long modificationTimestamp )
	{
		this.modificationDate = modificationTimestamp != 0 ? new Date( modificationTimestamp ) : null;
	}

	/**
	 * The tag.
	 * 
	 * @return The tag or null if not set
	 * @see #setTag(Tag)
	 */
	public Tag getTag()
	{
		return tag;
	}

	/**
	 * @param tag
	 *        The tag or null
	 * @see #getTag()
	 */
	public void setTag( Tag tag )
	{
		this.tag = tag;
	}

	/**
	 * @return The HTTP-formatted tag or null if not set
	 * @see #getTag()
	 */
	public String getTagHttp()
	{
		return tag != null ? tag.format() : null;
	}

	/**
	 * @param tag
	 *        The HTTP-formatted tag or null
	 * @see #setTag(Tag)
	 */
	public void setTagHttp( String tag )
	{
		this.tag = tag != null ? Tag.parse( tag ) : null;
	}

	/**
	 * The "max-age" cache control header.
	 * 
	 * @return The max age in seconds, or -1 if not set
	 * @see #setMaxAge(int)
	 */
	public int getMaxAge()
	{
		for( CacheDirective cacheDirective : getResource().getResponse().getCacheDirectives() )
			if( cacheDirective.getName().equals( HeaderConstants.CACHE_MAX_AGE ) )
				return Integer.parseInt( cacheDirective.getValue() );

		return -1;
	}

	/**
	 * @param maxAge
	 *        The max age in seconds, or -1 to explicitly set a "no-cache" cache
	 *        control header
	 * @see #getMaxAge()
	 */
	public void setMaxAge( int maxAge )
	{
		for( Iterator<CacheDirective> i = getResource().getResponse().getCacheDirectives().iterator(); i.hasNext(); )
			if( i.next().getName().equals( HeaderConstants.CACHE_MAX_AGE ) )
				i.remove();

		if( maxAge != -1 )
			getResource().getResponse().getCacheDirectives().add( CacheDirective.maxAge( maxAge ) );
		else
			getResource().getResponse().getCacheDirectives().add( CacheDirective.noCache() );
	}

	//
	// Operations
	//

	/**
	 * Adds a media type to the list of variants.
	 * 
	 * @param mediaType
	 *        The media type
	 */
	public void addMediaType( MediaType mediaType )
	{
		addVariant( mediaType, null );
	}

	/**
	 * Adds a media type to the list of variants.
	 * 
	 * @param mediaType
	 *        The media type
	 * @param language
	 *        The language
	 */
	public void addMediaTypeWithLanguage( MediaType mediaType, Language language )
	{
		addVariant( mediaType, language );
	}

	/**
	 * Adds a media type to the list of variants.
	 * 
	 * @param mediaTypeName
	 *        The media type name
	 */
	public void addMediaTypeByName( String mediaTypeName )
	{
		addVariant( MediaType.valueOf( mediaTypeName ), null );
	}

	/**
	 * Adds a media type to the list of variants.
	 * 
	 * @param mediaTypeName
	 *        The media type name
	 * @param languageName
	 *        The language name
	 */
	public void addMediaTypeByNameWithLanguage( String mediaTypeName, String languageName )
	{
		addVariant( MediaType.valueOf( mediaTypeName ), Language.valueOf( languageName ) );
	}

	/**
	 * Adds a media type to the list of variants.
	 * 
	 * @param mediaTypeExtension
	 *        The media type extension
	 */
	public void addMediaTypeByExtension( String mediaTypeExtension )
	{
		addVariant( getResource().getApplication().getMetadataService().getMediaType( mediaTypeExtension ), null );
	}

	/**
	 * Adds a media type to the list of variants.
	 * 
	 * @param mediaTypeExtension
	 *        The media type extension
	 * @param languageName
	 *        The language name
	 */
	public void addMediaTypeByExtensionWithLanguage( String mediaTypeExtension, String languageName )
	{
		addVariant( getResource().getApplication().getMetadataService().getMediaType( mediaTypeExtension ), Language.valueOf( languageName ) );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * The expiration date.
	 */
	private Date expirationDate;

	/**
	 * The modification date.
	 */
	private Date modificationDate;

	/**
	 * The tag.
	 */
	private Tag tag;

	private void addVariant( MediaType mediaType, Language language )
	{
		DelegatedResource resource = getResource();
		List<Variant> variants = resource.getVariants();
		if( resource.getAttributes().isNegotiateEncoding() )
		{
			for( Encoding encoding : CachingUtil.SUPPORTED_ENCODINGS )
			{
				Variant variant = new Variant( mediaType, language );
				variant.getEncodings().add( encoding );
				variants.add( variant );
			}
		}
		else
			variants.add( new Variant( mediaType, language ) );
	}
}
