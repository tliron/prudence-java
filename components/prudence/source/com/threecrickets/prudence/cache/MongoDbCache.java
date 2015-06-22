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

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.BSONObject;
import org.bson.Document;
import org.bson.types.Binary;
import org.restlet.data.CharacterSet;
import org.restlet.data.Encoding;
import org.restlet.data.Header;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Tag;
import org.restlet.util.Series;

import com.mongodb.BasicDBList;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

/**
 * A <a href="http://www.mongodb.org/">MongoDB</a>-backed cache.
 * <p>
 * Uses a dedicated MongoDB collection to store the cache, creating it if it
 * doesn't exist.
 * <p>
 * Supports storing entries as either binary dumps or detailed documents. Binary
 * dumps take less space and are slightly more efficient, while detailed
 * documents are far easier to debug. Binary mode is off by default.
 * <p>
 * Note that MongoDB's indexing facility allows for very high performance
 * invalidation and pruning.
 * 
 * @author Tal Liron
 */
public class MongoDbCache implements Cache
{
	//
	// Construction
	//

	/**
	 * Construction for localhost connection, database "prudence", collection
	 * "cache".
	 * 
	 * @throws UnknownHostException
	 *         In case localhost could not be found
	 */
	public MongoDbCache() throws UnknownHostException
	{
		this( new MongoClient() );
	}

	/**
	 * Construction for database "prudence", collection "cache".
	 * 
	 * @param client
	 *        The MongoDB client
	 */
	public MongoDbCache( MongoClient client )
	{
		this( client, "prudence" );
	}

	/**
	 * Construction for collection "cache".
	 * 
	 * @param client
	 *        The MongoDB client
	 * @param databaseName
	 *        The MongoDB database name
	 */
	public MongoDbCache( MongoClient client, String databaseName )
	{
		this( client, databaseName, "cache" );
	}

	/**
	 * Constructor.
	 * 
	 * @param client
	 *        The MongoDB client
	 * @param databaseName
	 *        The MongoDB database name
	 * @param collectionName
	 *        The name of the collection to use for the cache
	 */
	public MongoDbCache( MongoClient client, String databaseName, String collectionName )
	{
		this.client = client;
		MongoDatabase database = client.getDatabase( databaseName );
		cacheCollection = database.getCollection( collectionName );
		try
		{
			cacheCollection.createIndex( TAG_INDEX );
			cacheCollection.createIndex( EXPIRATION_DATE_INDEX );
			up();
		}
		catch( com.mongodb.MongoSocketException x )
		{
			down();
		}
	}

	//
	// Attributes
	//

	/**
	 * Whether to store entries by serializing them into BSON binaries.
	 * 
	 * @return A boolean
	 * @see #setBinary
	 */
	public boolean isBinary()
	{
		return isBinary;
	}

	/**
	 * @param isBinary
	 *        A boolean
	 * @see #isBinary
	 */
	public void setBinary( boolean isBinary )
	{
		this.isBinary = isBinary;
	}

	//
	// Cache
	//

	public void store( String key, CacheEntry entry )
	{
		logger.fine( "Store: " + key );

		Document query = new Document();
		query.put( "_id", key );

		Document document = new Document();
		Document set = new Document();
		document.put( "$set", set );

		// Note: In binary mode, the expirationDate is also inside the binary
		// dump, however we need it outside the opaque binary, too, to allow for
		// fast pruning
		set.put( "expirationDate", entry.getExpirationDate() );

		String[] tags = entry.getTags();
		if( ( tags != null ) && ( tags.length > 0 ) )
			set.put( "tags", tags );

		if( isBinary )
		{
			try
			{
				Binary binary = new Binary( BINARY_TYPE, entry.toBytes() );
				set.put( "binary", binary );
			}
			catch( IOException x )
			{
				logger.log( Level.WARNING, "Could not serialize binary", x );
			}
		}
		else
		{
			String string = entry.getString();
			if( string != null )
				set.put( "string", string );

			byte[] bytes = entry.getBytes();
			if( bytes != null )
			{
				Binary binary = new Binary( BINARY_TYPE, bytes );
				set.put( "bytes", binary );
			}

			MediaType mediaType = entry.getMediaType();
			if( mediaType != null )
				set.put( "mediaType", mediaType.getName() );

			Language language = entry.getLanguage();
			if( language != null )
				set.put( "language", language.getName() );

			Encoding encoding = entry.getEncoding();
			if( encoding != null )
				set.put( "encoding", encoding.getName() );

			CharacterSet characterSet = entry.getCharacterSet();
			if( characterSet != null )
				set.put( "characterSet", characterSet.getName() );

			Date modificationDate = entry.getModificationDate();
			if( modificationDate != null )
				set.put( "modificationDate", modificationDate );

			Tag tag = entry.getTag();
			if( tag != null )
				set.put( "tag", tag.format() );

			Date documentModificationDate = entry.getDocumentModificationDate();
			if( documentModificationDate != null )
				set.put( "documentModificationDate", documentModificationDate );

			Series<Header> headers = entry.getHeaders();
			if( headers != null )
			{
				BasicDBList list = new BasicDBList();
				for( Header header : headers )
				{
					Document object = new Document();
					object.put( "name", header.getName() );
					object.put( "value", header.getValue() );
					list.add( object );
				}
				set.put( "headers", list );
			}
		}

		// Upsert
		try
		{
			cacheCollection.updateOne( query, document, new UpdateOptions().upsert( true ) );
			up();
		}
		catch( com.mongodb.MongoSocketException x )
		{
			down();
		}
	}

	public CacheEntry fetch( String key )
	{
		Document query = new Document();
		query.put( "_id", key );
		try
		{
			Document document = cacheCollection.find( query ).first();
			up();
			if( document != null )
			{
				Date expirationDate = (Date) document.get( "expirationDate" );
				if( expirationDate.before( new Date() ) )
				{
					cacheCollection.deleteOne( query );
					logger.fine( "Stale entry: " + key );
					return null;
				}

				try
				{
					CacheEntry cacheEntry = null;

					byte[] bytes = (byte[]) document.get( "binary" );
					if( bytes != null )
					{
						return new CacheEntry( bytes );
					}
					else
					{
						String string = (String) document.get( "string" );
						bytes = (byte[]) document.get( "bytes" );
						MediaType mediaType = MediaType.valueOf( (String) document.get( "mediaType" ) );
						Language language = Language.valueOf( (String) document.get( "language" ) );
						Encoding encoding = Encoding.valueOf( (String) document.get( "encoding" ) );
						CharacterSet characterSet = CharacterSet.valueOf( (String) document.get( "characterSet" ) );
						Date modificationDate = (Date) document.get( "modificationDate" );
						String tagValue = (String) document.get( "tag" );
						Tag tag = tagValue != null ? Tag.parse( tagValue ) : null;
						Date documentModificationDate = (Date) document.get( "documentModificationDate" );

						Series<Header> headers = null;
						Object storedHeaders = document.get( "headers" );
						if( storedHeaders instanceof Collection )
						{
							headers = new Series<Header>( Header.class );
							for( Object storedHeader : (Collection<?>) storedHeaders )
							{
								if( storedHeader instanceof BSONObject )
								{
									BSONObject storedHeaderBson = (BSONObject) storedHeader;
									Object name = storedHeaderBson.get( "name" );
									Object value = storedHeaderBson.get( "value" );
									if( ( name != null ) && ( value != null ) )
										headers.add( new Header( name.toString(), value.toString() ) );
								}
							}
						}

						if( string != null )
							cacheEntry = new CacheEntry( string, mediaType, language, characterSet, encoding, headers, modificationDate, tag, expirationDate, documentModificationDate );
						else
							cacheEntry = new CacheEntry( bytes, mediaType, language, characterSet, encoding, headers, modificationDate, tag, expirationDate, documentModificationDate );
					}

					logger.fine( "Fetched: " + key );
					return cacheEntry;
				}
				catch( IOException x )
				{
					logger.log( Level.WARNING, "Could not deserialize cache entry", x );
				}
				catch( ClassNotFoundException x )
				{
					logger.log( Level.WARNING, "Could not deserialize cache entry", x );
				}
			}
			else
				logger.fine( "Did not fetch: " + key );
		}
		catch( com.mongodb.MongoSocketException x )
		{
			down();
		}

		return null;
	}

	public void invalidate( String tag )
	{
		Document query = new Document();
		query.put( "tags", tag );

		try
		{
			cacheCollection.deleteMany( query );
			logger.fine( "Invalidated: " + tag );
			up();
		}
		catch( com.mongodb.MongoSocketException x )
		{
			down();
		}
	}

	public void prune()
	{
		Document query = new Document();
		Document lt = new Document();
		query.put( "$lt", lt );
		lt.put( "expirationDate", new Date() );

		try
		{
			cacheCollection.deleteMany( query );
			logger.fine( "Pruned" );
		}
		catch( com.mongodb.MongoSocketException x )
		{
			down();
		}
	}

	public void reset()
	{
		try
		{
			cacheCollection.deleteMany( new Document() );
			up();
		}
		catch( com.mongodb.MongoSocketException x )
		{
			down();
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Options for ensuring the tag index on the cache collection.
	 */
	private static final Document TAG_INDEX = new Document();

	/**
	 * Options for ensuring the expiration date index on the cache collection.
	 */
	private static final Document EXPIRATION_DATE_INDEX = new Document();

	static
	{
		TAG_INDEX.put( "tags", 1 );
		EXPIRATION_DATE_INDEX.put( "expirationDate", 1 );
	}

	/**
	 * Binary type.
	 */
	private static final byte BINARY_TYPE = 0;

	/**
	 * The logger.
	 */
	private final Logger logger = Logger.getLogger( this.getClass().getCanonicalName() );

	/**
	 * The MongoDB client used for the cache.
	 */
	private final MongoClient client;

	/**
	 * The MongoDB collection used for the cache.
	 */
	private final MongoCollection<Document> cacheCollection;

	/**
	 * Whether to store entries by serializing them into BSON binaries.
	 */
	private volatile boolean isBinary = false;

	/**
	 * Whether MongoDB has last been seen as up.
	 */
	private AtomicBoolean up = new AtomicBoolean();

	/**
	 * Call when MongoDB is up.
	 */
	private void up()
	{
		if( up.compareAndSet( false, true ) )
			logger.info( "Up! " + client );
	}

	/**
	 * Call when MongoDB is down.
	 */
	private void down()
	{
		if( up.compareAndSet( true, false ) )
			logger.severe( "Down! " + client );
	}
}
