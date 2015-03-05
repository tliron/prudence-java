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

package com.threecrickets.prudence.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Date;

import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Header;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Metadata;
import org.restlet.data.Tag;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.RepresentationInfo;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Series;

import com.threecrickets.prudence.util.IoUtil;

/**
 * A serializable, cacheable set of parameters from which
 * {@link StringRepresentation} or {@link ByteArrayRepresentation} instances can
 * be created.
 * <p>
 * Instances are not thread safe.
 * 
 * @author Tal Liron
 * @see Cache
 */
public class CacheEntry implements Externalizable
{
	//
	// Construction
	//

	/**
	 * Constructor. A constructor without arguments is required for
	 * {@link Externalizable}.
	 */
	public CacheEntry()
	{
	}

	/**
	 * Construction with re-encoding.
	 * 
	 * @param cacheEntry
	 *        The cache entry to clone (must be un-encoded)
	 * @param encoding
	 *        The new encoding to use
	 * @throws IOException
	 *         In case of a compression error
	 */
	public CacheEntry( CacheEntry cacheEntry, Encoding encoding ) throws IOException
	{
		this( cacheEntry.string, cacheEntry.mediaType, cacheEntry.language, cacheEntry.characterSet, encoding, cacheEntry.headers, cacheEntry.modificationDate, cacheEntry.tag, cacheEntry.expirationDate,
			cacheEntry.modificationDate );
		setTags( cacheEntry.getTags() );
	}

	/**
	 * Construction with different string.
	 * 
	 * @param cacheEntry
	 *        The cache entry to clone
	 * @param string
	 *        The new string to use
	 * @throws IOException
	 *         In case of a compression error
	 */
	public CacheEntry( CacheEntry cacheEntry, String string ) throws IOException
	{
		this( string, cacheEntry.mediaType, cacheEntry.language, cacheEntry.characterSet, cacheEntry.encoding, cacheEntry.headers, cacheEntry.modificationDate, cacheEntry.tag, cacheEntry.expirationDate,
			cacheEntry.modificationDate );
		setTags( cacheEntry.getTags() );
	}

	/**
	 * Constructor. Compresses string if encoding is provided.
	 * 
	 * @param string
	 *        The string
	 * @param mediaType
	 *        The media type
	 * @param language
	 *        The language
	 * @param characterSet
	 *        The character set
	 * @param encoding
	 *        The encoding
	 * @param headers
	 *        The headers
	 * @param modificationDate
	 *        The modification date or null
	 * @param tag
	 *        The tag
	 * @param expirationDate
	 *        The expiration date
	 * @param documentModificationDate
	 *        The document modification date
	 * @throws IOException
	 *         In case of a compression error
	 */
	public CacheEntry( String string, MediaType mediaType, Language language, CharacterSet characterSet, Encoding encoding, Series<Header> headers, Date modificationDate, Tag tag, Date expirationDate,
		Date documentModificationDate ) throws IOException
	{
		this.mediaType = mediaType;
		this.language = language;
		this.characterSet = characterSet;
		this.encoding = Encoding.IDENTITY.equals( encoding ) ? null : encoding;
		this.headers = headers;
		this.modificationDate = modificationDate != null ? modificationDate : new Date();
		this.tag = tag;
		this.expirationDate = expirationDate;
		this.documentModificationDate = documentModificationDate;

		if( IoUtil.SUPPORTED_COMPRESSION_ENCODINGS.contains( encoding ) )
			bytes = IoUtil.compress( string, encoding, "text" );
		else
			this.string = string;
	}

	/**
	 * Constructor.
	 * 
	 * @param bytes
	 *        The bytes
	 * @param mediaType
	 *        The media type
	 * @param language
	 *        The language
	 * @param characterSet
	 *        The character set
	 * @param encoding
	 *        The encoding
	 * @param headers
	 *        The headers
	 * @param modificationDate
	 *        The modification date or null
	 * @param tag
	 *        The tag
	 * @param expirationDate
	 *        The expiration date
	 * @param documentModificationDate
	 *        The document modification date
	 */
	public CacheEntry( byte[] bytes, MediaType mediaType, Language language, CharacterSet characterSet, Encoding encoding, Series<Header> headers, Date modificationDate, Tag tag, Date expirationDate,
		Date documentModificationDate )
	{
		this.bytes = bytes;
		this.mediaType = mediaType;
		this.language = language;
		this.characterSet = characterSet;
		this.encoding = Encoding.IDENTITY.equals( encoding ) ? null : encoding;
		this.headers = headers;
		this.modificationDate = modificationDate != null ? modificationDate : new Date();
		this.tag = tag;
		this.expirationDate = expirationDate;
		this.documentModificationDate = documentModificationDate;
	}

	/**
	 * Constructor.
	 * 
	 * @param string
	 *        The string
	 * @param mediaType
	 *        The media type
	 * @param language
	 *        The language
	 * @param characterSet
	 *        The character set
	 * @param encoding
	 *        The encoding
	 * @param headers
	 *        The headers
	 * @param modificationTimestamp
	 *        The modification timestamp or 0
	 * @param tag
	 *        The tag
	 * @param expirationTimestamp
	 *        The expiration timestamp or 0 for no expiration
	 * @param documentModificationTimestamp
	 *        The document modification timestamp
	 * @throws IOException
	 *         In case of a compression error
	 */
	public CacheEntry( String string, MediaType mediaType, Language language, CharacterSet characterSet, Encoding encoding, Series<Header> headers, long modificationTimestamp, Tag tag, long expirationTimestamp,
		long documentModificationTimestamp ) throws IOException
	{
		this( string, mediaType, language, characterSet, encoding, headers, modificationTimestamp > 0 ? new Date( modificationTimestamp ) : null, tag, expirationTimestamp > 0 ? new Date( expirationTimestamp ) : null,
			new Date( documentModificationTimestamp ) );
	}

	/**
	 * Constructor.
	 * 
	 * @param bytes
	 *        The bytes
	 * @param mediaType
	 *        The media type
	 * @param language
	 *        The language
	 * @param characterSet
	 *        The character set
	 * @param encoding
	 *        The encoding
	 * @param headers
	 *        The headers
	 * @param modificationTimestamp
	 *        The modification timestamp or 0
	 * @param tag
	 *        The tag
	 * @param expirationTimestamp
	 *        The expiration timestamp or 0 for no expiration
	 * @param documentModificationTimestamp
	 *        The document modification timestamp
	 */
	public CacheEntry( byte[] bytes, MediaType mediaType, Language language, CharacterSet characterSet, Encoding encoding, Series<Header> headers, long modificationTimestamp, Tag tag, long expirationTimestamp,
		long documentModificationTimestamp )
	{
		this( bytes, mediaType, language, characterSet, encoding, headers, modificationTimestamp > 0 ? new Date( modificationTimestamp ) : null, tag, expirationTimestamp > 0 ? new Date( expirationTimestamp ) : null,
			new Date( documentModificationTimestamp ) );
	}

	/**
	 * Constructor.
	 * 
	 * @param representation
	 *        The representation
	 * @param headers
	 *        The headers
	 * @param modificationTimestamp
	 *        The modification timestamp or 0
	 * @param tag
	 *        The tag
	 * @param expirationTimestamp
	 *        The expiration timestamp or 0 for no expiration
	 * @param documentModificationTimestamp
	 *        The document modification timestamp
	 * @throws IOException
	 *         In case of a compression error
	 */
	public CacheEntry( Representation representation, Series<Header> headers, long modificationTimestamp, Tag tag, long expirationTimestamp, long documentModificationTimestamp ) throws IOException
	{
		this( representation.getText(), representation.getMediaType(), representation.getLanguages().get( 0 ), representation.getCharacterSet(), representation.getEncodings().get( 0 ), headers, modificationTimestamp,
			tag, expirationTimestamp, documentModificationTimestamp );
	}

	/**
	 * Deserializing construction.
	 * 
	 * @param bytes
	 *        An array of bytes
	 * @throws IOException
	 *         In case of a reading error
	 * @throws ClassNotFoundException
	 *         In case an unknown class has been serialized
	 * @see #toBytes()
	 */
	public CacheEntry( byte[] bytes ) throws IOException, ClassNotFoundException
	{
		ByteArrayInputStream byteStream = new ByteArrayInputStream( bytes );
		try
		{
			ObjectInputStream stream = new ObjectInputStream( byteStream );
			try
			{
				readExternal( stream );
			}
			finally
			{
				stream.close();
			}
		}
		finally
		{
			byteStream.close();
		}
	}

	//
	// Attributes
	//

	public String[] getTags()
	{
		return tags;
	}

	public void setTags( String[] tags )
	{
		this.tags = tags;
	}

	/**
	 * @return The string
	 */
	public String getString()
	{
		return string;
	}

	/**
	 * @return The bytes
	 */
	public byte[] getBytes()
	{
		return bytes;
	}

	/**
	 * @return The length in bytes of either the string or the bytes
	 */
	public int getSize()
	{
		if( bytes != null )
			return bytes.length;
		else
			return string.getBytes().length;
	}

	/**
	 * @return The media type
	 */
	public MediaType getMediaType()
	{
		return mediaType;
	}

	/**
	 * @return The language
	 */
	public Language getLanguage()
	{
		return language;
	}

	/**
	 * @return The character set
	 */
	public CharacterSet getCharacterSet()
	{
		return characterSet;
	}

	/**
	 * @return The encoding
	 */
	public Encoding getEncoding()
	{
		return encoding;
	}

	/**
	 * @return The headers
	 */
	public Series<Header> getHeaders()
	{
		return headers;
	}

	/**
	 * @return The tag
	 */
	public Tag getTag()
	{
		return tag;
	}

	/**
	 * @return The document modification date
	 */
	public Date getDocumentModificationDate()
	{
		return documentModificationDate;
	}

	/**
	 * @return The entry modification date
	 */
	public Date getModificationDate()
	{
		return modificationDate;
	}

	/**
	 * @return The expiration date
	 */
	public Date getExpirationDate()
	{
		return expirationDate;
	}

	//
	// Operations
	//

	/**
	 * Serialize into a byte array.
	 * <p>
	 * Note that unlike {@link IoUtil#serialize(Object)}, this will not include
	 * the type header.
	 * 
	 * @return An array of bytes
	 * @throws IOException
	 *         In case of a serialization error
	 * @see #CacheEntry(byte[])
	 */
	public byte[] toBytes() throws IOException
	{
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		try
		{
			ObjectOutputStream stream = new ObjectOutputStream( byteStream );
			try
			{
				writeExternal( stream );
			}
			finally
			{
				stream.close();
			}
		}
		finally
		{
			byteStream.close();
		}

		return byteStream.toByteArray();
	}

	/**
	 * Creates a {@link StringRepresentation} or a
	 * {@link ByteArrayRepresentation}.
	 * 
	 * @return A {@link Representation}
	 */
	public Representation represent()
	{
		Representation representation;

		if( bytes != null )
		{
			representation = new ByteArrayRepresentation( bytes, mediaType );
			if( language != null )
				representation.getLanguages().add( language );
			representation.setCharacterSet( characterSet );
			if( encoding != null )
				representation.getEncodings().add( encoding );
		}
		else
			representation = new StringRepresentation( string, mediaType, language, characterSet );

		representation.setModificationDate( modificationDate );
		representation.setExpirationDate( expirationDate );
		return representation;
	}

	/**
	 * Create a {@link RepresentationInfo}.
	 * 
	 * @return A {@link RepresentationInfo}
	 */
	public RepresentationInfo getInfo()
	{
		return new RepresentationInfo( mediaType, modificationDate );
	}

	//
	// Externalizable
	//

	public void readExternal( ObjectInput in ) throws IOException, ClassNotFoundException
	{
		int tagsLength = in.readInt();
		tags = new String[tagsLength];
		for( int i = 0; i < tagsLength; i++ )
		{
			String tag = IoUtil.readUtf8( in );
			tags[i] = tag;
		}

		if( in.readBoolean() )
		{
			bytes = new byte[in.readInt()];
			in.readFully( bytes );
		}
		else
			string = IoUtil.readUtf8( in );

		mediaType = MediaType.valueOf( in.readUTF() );
		language = Language.valueOf( in.readUTF() );
		characterSet = CharacterSet.valueOf( in.readUTF() );
		encoding = Encoding.valueOf( in.readUTF() );

		int headersLength = in.readInt();
		if( headersLength > 0 )
		{
			headers = new Series<Header>( Header.class );
			for( int i = 0; i < headersLength; i++ )
			{
				String name = IoUtil.readUtf8( in );
				String value = IoUtil.readUtf8( in );
				headers.add( new Header( name, value ) );
			}
		}

		String tagValue = in.readUTF();
		tag = tagValue != null ? Tag.parse( tagValue ) : null;
		modificationDate = new Date( in.readLong() );
		expirationDate = new Date( in.readLong() );
		documentModificationDate = new Date( in.readLong() );
	}

	public void writeExternal( ObjectOutput out ) throws IOException
	{
		if( tags == null )
			out.writeInt( 0 );
		else
		{
			out.writeInt( tags.length );
			for( String tag : tags )
				IoUtil.writeUtf8( out, tag );
		}

		if( bytes != null )
		{
			out.writeBoolean( true );
			out.writeInt( bytes.length );
			out.write( bytes );
		}
		else
		{
			out.writeBoolean( false );
			IoUtil.writeUtf8( out, string );
		}

		out.writeUTF( nonNull( mediaType ) );
		out.writeUTF( nonNull( language ) );
		out.writeUTF( nonNull( characterSet ) );
		out.writeUTF( nonNull( encoding ) );

		if( headers == null )
			out.writeInt( 0 );
		else
		{
			out.writeInt( headers.size() );
			for( Header header : headers )
			{
				IoUtil.writeUtf8( out, header.getName() );
				IoUtil.writeUtf8( out, header.getValue() );
			}
		}

		IoUtil.writeUtf8( out, tag != null ? tag.format() : "" );
		out.writeLong( modificationDate.getTime() );
		out.writeLong( expirationDate.getTime() );
		out.writeLong( documentModificationDate.getTime() );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final long serialVersionUID = 1L;

	/**
	 * The cache tags.
	 */
	private String[] tags;

	/**
	 * The stored bytes.
	 */
	private byte[] bytes;

	/**
	 * The stored string.
	 */
	private String string;

	/**
	 * The media type.
	 */
	private MediaType mediaType;

	/**
	 * The language.
	 */
	private Language language;

	/**
	 * The character set.
	 */
	private CharacterSet characterSet;

	/**
	 * The encoding.
	 */
	private Encoding encoding;

	/**
	 * The headers.
	 */
	private Series<Header> headers;

	/**
	 * The tag.
	 */
	private Tag tag;

	/**
	 * The document modification date.
	 */
	private Date documentModificationDate;

	/**
	 * The entry modification date.
	 */
	private Date modificationDate;

	/**
	 * The expiration date.
	 */
	private Date expirationDate;

	/**
	 * Makes sure to return a non-null string.
	 * 
	 * @param metadata
	 *        The metadata or null
	 * @return A string
	 */
	private static String nonNull( Metadata metadata )
	{
		return metadata == null ? "" : nonNull( metadata.getName() );
	}

	/**
	 * Makes sure to return a non-null string.
	 * 
	 * @param string
	 *        The string or null
	 * @return A string
	 */
	private static String nonNull( String string )
	{
		return string == null ? "" : string;
	}
}