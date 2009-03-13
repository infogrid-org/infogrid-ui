//
// This file is part of InfoGrid(tm). You may not use this file except in
// compliance with the InfoGrid license. The InfoGrid license and important
// disclaimers are contained in the file LICENSE.InfoGrid.txt that you should
// have received with InfoGrid. If you have not received LICENSE.InfoGrid.txt
// or you do not consent to all aspects of the license and the disclaimers,
// no license is granted; do not use this file.
// 
// For more information about InfoGrid go to http://infogrid.org/
//
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.store.jets3t;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.infogrid.store.AbstractIterableStore;
import org.infogrid.store.StoreKeyDoesNotExistException;
import org.infogrid.store.StoreKeyExistsAlreadyException;
import org.infogrid.store.StoreValue;
import org.infogrid.store.util.SimpleStoreValueMapper;
import org.infogrid.store.util.StoreValueMapper;
import org.infogrid.util.DelegatingIOException;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;
import org.jets3t.service.S3ObjectsChunk;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;

/**
 * Amazon S3 implementation of the Store interface.
 */
public class JetS3tStore
        extends
            AbstractIterableStore
        implements
            CanBeDumped
{
    private static final Log log = Log.getLogInstance( JetS3tStore.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param service the S3 service to use
     * @param bucket the S3 bucket to use
     * @param prefix the S3 key prefix identifying the directory
     * @param delimiter the S3 key path separator
     * @return the created JetS3tStore
     */
    public static JetS3tStore create(
            S3Service service,
            S3Bucket  bucket,
            String    prefix,
            String    delimiter )
    {
        if( service == null ) {
            throw new IllegalArgumentException( "Cannot have null service" );
        }
        if( bucket == null ) {
            throw new IllegalArgumentException( "Cannot have null bucket" );
        }
        if( prefix == null ) {
            prefix = "";
        }
        if( prefix.length() > 0 && !prefix.endsWith( delimiter )) {
            prefix = prefix + delimiter;
        }
        return new JetS3tStore(
                service,
                bucket,
                prefix,
                delimiter,
                new SubdirectoryKeyS3KeyMapper( prefix ),
                SimpleStoreValueMapper.create() );
    }

    /**
     * Factory method.
     *
     * @param service the S3 service to use
     * @param bucket the S3 bucket to use
     * @param prefix the S3 key prefix identifying the directory
     * @param delimiter the S3 key path separator
     * @param keyMapper maps Store keys to and from S3 keys
     * @param storeValueMapper maps StoreValues to file content, and vice versa
     * @return the created JetS3tStore
     */
    public static JetS3tStore create(
            S3Service        service,
            S3Bucket         bucket,
            String           prefix,
            String           delimiter,
            KeyS3KeyMapper   keyMapper,
            StoreValueMapper storeValueMapper )
    {
        if( service == null ) {
            throw new IllegalArgumentException( "Cannot have null service" );
        }
        if( bucket == null ) {
            throw new IllegalArgumentException( "Cannot have null bucket" );
        }
        if( prefix == null ) {
            prefix = "";
        }
        if( prefix.length() > 0 && !prefix.endsWith( delimiter )) {
            prefix = prefix + delimiter;
        }
        return new JetS3tStore(
                service,
                bucket,
                prefix,
                delimiter,
                keyMapper,
                storeValueMapper );
    }

    /**
     * Constructor.
     *
     * @param service the S3 service to use
     * @param bucket the S3 bucket to use
     * @param prefix the S3 key prefix identifying the directory
     * @param delimiter the S3 key path separator
     * @param keyMapper maps Store keys to and from S3 keys
     * @param storeValueMapper maps StoreValues to file content, and vice versa
     */
    protected JetS3tStore(
            S3Service        service,
            S3Bucket         bucket,
            String           prefix,
            String           delimiter,
            KeyS3KeyMapper   keyMapper,
            StoreValueMapper storeValueMapper )
    {
        theService          = service;
        theBucket           = bucket;
        thePrefix           = prefix;
        theDelimiter        = delimiter;
        theKeyMapper        = keyMapper;
        theStoreValueMapper = storeValueMapper;
        
        if( log.isDebugEnabled() ) {
            log.debug( "Created " + this );
        }
    }

    /**
     * Initialize the Store. If the Store was initialized earlier, this will delete all
     * contained information. This operation is similar to unconditionally formatting a hard drive.
     * 
     * @throws IOException thrown if an I/O error occurred
     */
    public void initializeHard()
            throws
                IOException
    {
        deleteAll();
    }
    
    /**
     * Initialize the Store if needed. If the Store was initialized earlier, this will do
     * nothing. This operation is equivalent to {@link #initializeHard} if and only if
     * the Store had not been initialized earlier.
     * 
     * @throws IOException thrown if an I/O error occurred
     */
    public void initializeIfNecessary()
            throws
                IOException
    {
        // do nothing
    }
    
    /**
     * Obtain the S3 Service.
     * 
     * @return the S3 Service
     */
    public S3Service getService()
    {
        return theService;
    }

    /**
     * Obtain the S3 bucket.
     * 
     * @return the S3 bucket
     */
    public S3Bucket getBucket()
    {
        return theBucket;
    }

    /**
     * Obtain the S3 key prefix identifying the directory underneath which all data is stored.
     * 
     * @return the top-level directory
     */
    public String getPrefix()
    {
        return thePrefix;
    }

    /**
     * Obtain the S3 key path separator.
     * 
     * @return the S3 key path separator
     */
    public String getDelimiter()
    {
        return theDelimiter;
    }

    /**
     * Obtain the KeyS3KeyMapper.
     * 
     * @return the KeyS3KeyMapper
     */
    public KeyS3KeyMapper getKeyS3KeyMapper()
    {
        return theKeyMapper;
    }
    
    /**
     * Obtain the StoreValueMapper.
     * 
     * @return the StoreValueMapper
     */
    public StoreValueMapper getStoreValueMapper()
    {
        return theStoreValueMapper;
    }

    /**
     * Put a data element into the Store for the first time. Throw an Exception if a data
     * element has already been store using the same key.
     *
     * @param toStore the StoreValue to store
     * @throws StoreKeyExistsAlreadyException thrown if a data element is already stored in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #update if a data element with this key exists already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    public void put(
            StoreValue toStore )
        throws
            StoreKeyExistsAlreadyException,
            IOException
    {
        if( log.isInfoEnabled() ) {
            log.info( this + ".put( " + toStore + " )" );
        }
        try {
            String key   = toStore.getKey();
            String s3Key = theKeyMapper.keyToS3Key( key );
            
            S3Object obj    = theService.getObject( theBucket, s3Key );
            long     length = obj.getContentLength();
            
            if( length > 0 ) {
                throw new StoreKeyExistsAlreadyException( this, key );
            }

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            theStoreValueMapper.writeStoreValue( toStore, outStream );
            outStream.close();

            InputStream inStream = new ByteArrayInputStream( outStream.toByteArray() );
            obj.setDataInputStream( inStream );
            obj.setContentType( MIME );
            
            theService.putObject( theBucket, obj );

        } catch( S3ServiceException ex ) {
            throw new DelegatingIOException( ex );

        } finally {
            firePutPerformed( toStore );
        }
    }

    /**
     * Update a data element that already exists in the Store, by overwriting it with a new value. Throw an
     * Exception if a data element with this key does not exist already.
     *
     * @param toUpdate the StoreValue to update
     * @throws StoreKeyDoesNotExistException thrown if no data element exists in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #putOrUpdate if a data element with this key may exist already
     */
    public void update(
            StoreValue toUpdate )
        throws
            StoreKeyDoesNotExistException,
            IOException
    {
        if( log.isInfoEnabled() ) {
            log.info( this + ".update( " + toUpdate + " )" );
        }

        try {
            String key   = toUpdate.getKey();
            String s3Key = theKeyMapper.keyToS3Key( key );
            
            S3Object obj    = theService.getObject( theBucket, s3Key );
            long     length = obj.getContentLength();
            
            if( length == 0 ) {
                throw new StoreKeyDoesNotExistException( this, key );
            }
            
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            theStoreValueMapper.writeStoreValue( toUpdate, outStream );
            outStream.close();

            InputStream inStream = new ByteArrayInputStream( outStream.toByteArray() );
            obj.setDataInputStream( inStream );
            obj.setContentType( MIME );
            
            theService.putObject( theBucket, obj );
            
        } catch( S3ServiceException ex ) {
            throw new DelegatingIOException( ex );

        } finally {
            fireUpdatePerformed( toUpdate );
        }
    }

    /**
     * Put (if does not exist already) or update (if it does exist) a data element in the Store.
     *
     * @param toStoreOrUpdate the StoreValue to store or update
     * @return true if the value was updated, false if it was put
     * @throws IOException thrown if an I/O error occurred
     *
     * @see #put if a data element with this key does not exist already
     * @see #update if a data element with this key exists already
     */
    public boolean putOrUpdate(
            StoreValue toStoreOrUpdate )
        throws
            IOException
    {

        if( log.isInfoEnabled() ) {
            log.info( this + ".putOrUpdate( " + toStoreOrUpdate + " )" );
        }
        boolean ret = false; // good default?
        try {
            String key   = toStoreOrUpdate.getKey();
            String s3Key = theKeyMapper.keyToS3Key( key );
            
            S3Object obj    = theService.getObject( theBucket, s3Key );
            long     length = obj.getContentLength();
            
            ret = length > 0;
            
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            theStoreValueMapper.writeStoreValue( toStoreOrUpdate, outStream );
            outStream.close();

            InputStream inStream = new ByteArrayInputStream( outStream.toByteArray() );
            obj.setDataInputStream( inStream );
            obj.setContentType( MIME );
            
            theService.putObject( theBucket, obj );

            return ret;

        } catch( S3ServiceException ex ) {
            throw new DelegatingIOException( ex );

        } finally {
            if( ret ) {
                fireUpdatePerformed( toStoreOrUpdate );
            } else {
                firePutPerformed( toStoreOrUpdate );
            }
        }        
    }

    /**
     * Obtain a data element and associated meta-data from the Store, given a key.
     *
     * @param key the key to the data element in the Store
     * @return the StoreValue stored in the Store for this key; this encapsulates data element and meta-data
     * @throws StoreKeyDoesNotExistException thrown if currently there is no data element in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     */
    public StoreValue get(
            String key )
        throws
            StoreKeyDoesNotExistException,
            IOException
    {
        if( log.isInfoEnabled() ) {
            log.info( this + ".get( " + key + " )" );
        }

        StoreValue ret = null;
        try {
            String s3Key = theKeyMapper.keyToS3Key( key );
            
            S3Object obj    = theService.getObject( theBucket, s3Key );
            long     length = obj.getContentLength();
            
            if( length == 0 ) {
                throw new StoreKeyDoesNotExistException( this, key );
            }

            InputStream stream = obj.getDataInputStream();
            stream             = new BufferedInputStream( stream );
            ret = theStoreValueMapper.readStoreValue( stream );
            stream.close();

            return ret;

        } catch( S3ServiceException ex ) {
            throw new DelegatingIOException( ex );

        } finally {
            if( ret != null ) {
                fireGetPerformed( ret );
            } else {
                fireGetFailed( key );
            }
        }
    }
    
    /**
     * Delete the StoreValue that is stored using this key.
     *
     * @param key the key to the data element in the Store
     * @throws StoreKeyDoesNotExistException thrown if currently there is no data element in the Store using this key
     * @throws IOException thrown if an I/O error occurred
     */
    public void delete(
            String key )
        throws
            StoreKeyDoesNotExistException,
            IOException
    {
        if( log.isInfoEnabled() ) {
            log.info( this + ".delete( " + key + " )" );
        }
        try {
            String s3Key = theKeyMapper.keyToS3Key( key );

            S3Object obj    = theService.getObject( theBucket, s3Key );
            long     length = obj.getContentLength();
            
            if( length == 0 ) {
                throw new StoreKeyDoesNotExistException( this, key );
            }
            theService.deleteObject( theBucket, s3Key );
            
        } catch( S3ServiceException ex ) {
            throw new DelegatingIOException( ex );

        } finally {
            fireDeletePerformed( key );
        }
    }

    /**
     * Remove all data in this Store whose key starts with this string.
     *
     * @param startsWith the String the key starts with
     * @throws IOException thrown if an I/O error occurred
     */
    public void deleteAll(
            String startsWith )
        throws
            IOException
    {
        if( log.isDebugEnabled() ) {
            log.debug( this + ".deleteAll()" );
        }

        String s3StartsWith = theKeyMapper.keyToS3Key( startsWith );

        try {
            String priorLastKey = null;
            while( true ) {
                S3ObjectsChunk chunk = theService.listObjectsChunked(
                        theBucket.getName(),
                        s3StartsWith,
                        null,
                        1024, // fudge number
                        priorLastKey );

                for( S3Object current : chunk.getObjects()) {
                    theService.deleteObject( theBucket, current.getKey() );
                }

                priorLastKey = chunk.getPriorLastKey();
                if( priorLastKey == null ) {
                    break;
                }
            }
        } catch( S3ServiceException ex ) {
            throw new DelegatingIOException( ex );
        }
    }

    /**
     * Obtain an Iterator over the content of this Store.
     *
     * @return the Iterator
     */
    public JetS3tStoreIterator iterator()
    {
        return JetS3tStoreIterator.create( this );
    }

    /**
     * Determine the number of StoreValues in this Store whose key starts with this String.
     *
     * @param startsWith the String the key starts with
     * @return the number of StoreValues in this Store whose key starts with this String
     * @throws IOException thrown if an I/O error occurred
     */
    public int size(
            final String startsWith )
        throws
            IOException
    {
        String s3StartsWith = theKeyMapper.keyToS3Key( startsWith );

        try {
            int    ret          = 0;
            String priorLastKey = null;
            while( true ) {
                S3ObjectsChunk chunk = theService.listObjectsChunked(
                        theBucket.getName(),
                        s3StartsWith,
                        null,
                        1024, // fudge number
                        priorLastKey );

                ret += chunk.getObjects().length;

                priorLastKey = chunk.getPriorLastKey();
                if( priorLastKey == null ) {
                    break;
                }
            }
            return ret;

        } catch( S3ServiceException ex ) {
            throw new DelegatingIOException( ex );
        }
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "service",
                    "bucket",
                    "prefix",
                    "delimiter",
                    "keyMapper",
                    "storeValueMapper"
                },
                new Object[] {
                    theService,
                    theBucket,
                    thePrefix,
                    theDelimiter,
                    theKeyMapper,
                    theStoreValueMapper
                });
    }

    /**
     * The S3 Service.
     */
    protected S3Service theService;

    /**
     * The S3 Bucket.
     */
    protected S3Bucket theBucket;
    
    /**
     * The prefix.
     */
    protected String thePrefix;

    /**
     * The path separator.
     */
    protected String theDelimiter;

    /**
     * Maps Store keys to S3 keys and vice versa.
     */
    private KeyS3KeyMapper theKeyMapper;

    /**
     * Maps StoreValues to file content, and vice versa.
     */
    protected StoreValueMapper theStoreValueMapper;
    
    /**
     * The MIME type to use.
     */
    protected static final String MIME = "application/x-InfoGrid-" + JetS3tStore.class.getName();
}
