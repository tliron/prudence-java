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

package com.threecrickets.prudence.cache;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bson.Document;
import org.bson.types.Binary;
import org.restlet.Component;

import com.hazelcast.core.MapStore;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.threecrickets.prudence.util.InstanceUtil;
import com.threecrickets.prudence.util.IoUtil;

/**
 * A Hazelcast persistence implementation over
 * <a href="http://www.mongodb.org/">MongoDB</a>.
 * <p>
 * The MongoDB client must be stored as
 * "com.threecrickets.prudence.cache.HazelcastMongoDbMapStore.mongoDb" in the
 * {@link Component} 's context.
 * <p>
 * The MongoDB database will be "prudence".
 * 
 * @author Tal Liron
 * @param <K>
 *        Key
 * @param <V>
 *        Value
 */
public abstract class HazelcastMongoDbMapStore<K, V> implements MapStore<K, V>
{
	//
	// Constants
	//

	/**
	 * MongoDB default connection attribute for a {@link Component}.
	 */
	public static final String MONGODB_CLIENT_ATTRIBUTE = HazelcastCache.class.getCanonicalName() + ".mongoDb";

	//
	// Construction
	//

	/**
	 * Constructor.
	 * 
	 * @param collectionName
	 *        The name of the MongoDB collection used for the store
	 */
	public HazelcastMongoDbMapStore( String collectionName )
	{
		this.collectionName = collectionName;
	}

	//
	// Attributes
	//

	/**
	 * The MongoDB collection used for the store.
	 * 
	 * @return The MongoDB collection
	 */
	public MongoCollection<Document> getCollection()
	{
		if( collection == null )
		{
			Component component = InstanceUtil.getComponent();
			if( component != null )
			{
				MongoClient client = (MongoClient) component.getContext().getAttributes().get( MONGODB_CLIENT_ATTRIBUTE );
				if( client != null )
				{
					MongoDatabase database = client.getDatabase( "prudence" );
					if( database != null )
						collection = database.getCollection( collectionName );
				}
			}
		}

		if( collection == null )
			throw new RuntimeException( "MongoDB client must be configured in order to use HazelcastMongoDbMapStore" );

		return collection;
	}

	//
	// MapStore
	//

	public V load( K key )
	{
		MongoCollection<Document> collection = getCollection();
		Document query = new Document();

		query.put( "_id", key );

		Document value = collection.find( query ).first();
		if( value != null )
		{
			@SuppressWarnings("unchecked")
			V fromBinary = (V) fromBinary( (byte[]) value.get( "value" ) );
			return fromBinary;
		}
		return null;
	}

	public Map<K, V> loadAll( Collection<K> keys )
	{
		MongoCollection<Document> collection = getCollection();
		Document query = new Document();
		Document in = new Document();

		query.put( "_id", in );
		in.put( "$in", keys );

		HashMap<K, V> map = new HashMap<K, V>();
		for( Document value : collection.find( query ) )
		{
			@SuppressWarnings("unchecked")
			K key = (K) value.get( "_id" );
			@SuppressWarnings("unchecked")
			V fromBinary = (V) fromBinary( (byte[]) value.get( "value" ) );
			map.put( key, fromBinary );
		}

		return map;
	}

	public Set<K> loadAllKeys()
	{
		MongoCollection<Document> collection = getCollection();
		Document projection = new Document();

		projection.put( "_id", 1 );

		Set<K> keys = new HashSet<K>();
		for( Document value : collection.find().projection( projection ) )
		{
			@SuppressWarnings("unchecked")
			K id = (K) value.get( "_id" );
			keys.add( id );
		}

		return keys;
	}

	public void store( K key, V value )
	{
		MongoCollection<Document> collection = getCollection();
		Document query = new Document();
		Document update = new Document();
		Document set = new Document();

		query.put( "_id", key );
		update.put( "$set", set );
		set.put( "value", toBinary( value ) );

		collection.updateOne( query, update, new UpdateOptions().upsert( true ) );
	}

	public void storeAll( Map<K, V> map )
	{
		for( Map.Entry<K, V> entry : map.entrySet() )
			store( entry.getKey(), entry.getValue() );
	}

	public void delete( K key )
	{
		MongoCollection<Document> collection = getCollection();
		Document query = new Document();

		query.put( "_id", key );

		collection.deleteOne( query );
	}

	public void deleteAll( Collection<K> keys )
	{
		MongoCollection<Document> collection = getCollection();
		Document query = new Document();
		Document in = new Document();

		query.put( "_id", in );
		in.put( "$in", keys );

		collection.deleteMany( query );
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	/**
	 * Binary type.
	 */
	private static final byte BINARY_TYPE = 0;

	/**
	 * The name of the MongoDB collection used for the store.
	 */
	private final String collectionName;

	/**
	 * The MongoDB collection used for the store.
	 */
	private volatile MongoCollection<Document> collection;

	/**
	 * Serialize an object into a BSON binary.
	 * 
	 * @param o
	 *        The object
	 * @return The binary
	 */
	private static Binary toBinary( Object o )
	{
		try
		{
			return new Binary( BINARY_TYPE, IoUtil.serialize( o ) );
		}
		catch( IOException x )
		{
			throw new RuntimeException( x );
		}
	}

	/**
	 * Deserialize an object from a BSON binary.
	 * 
	 * @param <V>
	 * @param binary
	 *        The binary or null
	 * @return The object or null
	 */
	private static Object fromBinary( byte[] binary )
	{
		if( binary == null )
			return null;

		try
		{
			return IoUtil.deserialize( binary );
		}
		catch( IOException x )
		{
			throw new RuntimeException( x );
		}
		catch( ClassNotFoundException x )
		{
			throw new RuntimeException( x );
		}
	}
}
