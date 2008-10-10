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
// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.store.hadoop;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.infogrid.store.AbstractStore;
import org.infogrid.store.IterableStore;
import org.infogrid.store.StoreKeyDoesNotExistException;
import org.infogrid.store.StoreKeyExistsAlreadyException;
import org.infogrid.store.StoreValue;
import org.infogrid.store.util.SimpleStoreValueMapper;
import org.infogrid.store.util.StoreValueMapper;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.StringHelper;
import org.infogrid.util.logging.Log;
import org.infogrid.util.tree.TreeFacade;
import org.infogrid.util.tree.TreeFacadeCursorIterator;

/**
 * Hadoop implementation of the Store interface.
 */
public class HadoopStore
        extends
            AbstractStore
        implements
            IterableStore
{
    private static final Log log = Log.getLogInstance( HadoopStore.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param fs the Hadoop FileSystem
     * @param subDir the subdirectory in the Hadoop FileSystem that becomes the top mapping directory. 
     * @return the created HadoopStore
     */
    public static HadoopStore create(
            FileSystem fs,
            Path       subDir )
    {
        if( subDir != null ) {
            subDir = subDir.makeQualified( fs );
        }

        return new HadoopStore(
                fs,
                subDir,
                new SubdirectoryKeyPathMapper( subDir ),
                SimpleStoreValueMapper.create() );
    }

    /**
     * Factory method.
     *
     * @param fs the Hadoop FileSystem 
     * @param subDir the subdirectory in the file system that becomes the top mapping directory
     * @param keyMapper maps Store keys into Hadoop FileSystem paths
     * @param storeValueMapper maps StoreValues to file content, and vice versa
     * @return the created HadoopStore
     */
    public static HadoopStore create(
            FileSystem       fs,
            Path             subDir,
            KeyPathMapper    keyMapper,
            StoreValueMapper storeValueMapper )
    {
        return new HadoopStore( fs, subDir, keyMapper, storeValueMapper );
    }

    /**
     * Constructor.
     *
     * @param fs the Hadoop FileSystem 
     * @param subDir the subdirectory in the file system that becomes the top mapping directory
     * @param keyMapper maps Store keys into Hadoop FileSystem paths
     * @param storeValueMapper maps StoreValues to file content, and vice versa
     */
    protected HadoopStore(
            FileSystem       fs,
            Path             subDir,
            KeyPathMapper    keyMapper,
            StoreValueMapper storeValueMapper )
    {
        theFileSystem       = fs;
        theSubDir           = subDir;
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
     * nothing. This operation is equivalent to {@see #initializeHard} if and only if
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
     * Obtain the Hadoop FileSystem in which the data is stored.
     * 
     * @return the FileSystem
     */
    public FileSystem getFileSystem()
    {
        return theFileSystem;
    }

    /**
     * Obtain the top-level directory underneath which all data is stored.
     * 
     * @return the top-level directory
     */
    public Path getTopDirectory()
    {
        return theSubDir;
    }

    /**
     * Obtain the KeyFileMapper.
     * 
     * @return the KeyFileMapper
     */
    public KeyPathMapper getKeyFileMapper()
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
            String key  = toStore.getKey();
            Path   file = theKeyMapper.keyToPath( key );
            
            if( theFileSystem.exists( file )) {
                throw new StoreKeyExistsAlreadyException( this, key );
            }
            
            // do we need to create parent directories here?
            //file.getParentFile().mkdirs();
            //file.createNewFile();
            
            OutputStream stream = theFileSystem.create( file );
            theStoreValueMapper.writeStoreValue( toStore, stream );
            stream.close();
            
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
            String key  = toUpdate.getKey();
            Path   file = theKeyMapper.keyToPath( key );
            
            if( !theFileSystem.exists( file )) {
                throw new StoreKeyDoesNotExistException( this, key );
            }
            
            OutputStream stream = theFileSystem.create( file );
            theStoreValueMapper.writeStoreValue( toUpdate, stream );
            stream.close();
            
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

            String key  = toStoreOrUpdate.getKey();
            Path   file = theKeyMapper.keyToPath( key );
            
            ret = theFileSystem.exists( file );

            // same operation for create and update
            OutputStream stream = theFileSystem.create( file );
            theStoreValueMapper.writeStoreValue( toStoreOrUpdate, stream );
            stream.close();

            return ret;

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
            Path file = theKeyMapper.keyToPath( key );
            
            if( !theFileSystem.exists( file )) {
                throw new StoreKeyDoesNotExistException( this, key );
            }
            
            InputStream stream = theFileSystem.open( file );
            ret = theStoreValueMapper.readStoreValue( stream );
            stream.close();

            return ret;

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
            Path file = theKeyMapper.keyToPath( key );
            
            if( !theFileSystem.exists( file )) {
                throw new StoreKeyDoesNotExistException( this, key );
            }
            theFileSystem.delete( file, false );

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

        TreeFacade<Path>     facade = HadoopFsPathTreeFacade.create( theFileSystem, getTopDirectory() );
        CursorIterator<Path> iter   = TreeFacadeCursorIterator.create( facade, Path.class );

        // we delete them backwards, so we get the directories last
        iter.moveToAfterLast();
        while( iter.hasPrevious() ) {
            Path current = iter.previous();
            if( !theSubDir.equals( current )) {
                if( theFileSystem.getFileStatus( current ).isDir() ) {
                    if( !theFileSystem.delete( current, false ) ) {
                        log.warn( "Could not delete file " + current );
                    }
                } else {
                    String key = theKeyMapper.pathToKey( current );

                    if( key.startsWith( startsWith )) {
                        if( !theFileSystem.delete( current, false ) ) {
                            log.warn( "Could not delete file " + current );
                        }
                    }
                }
            }
        }
    }

    /**
     * Obtain an Iterator over the content of this Store.
     *
     * @return the Iterator
     */
    public HadoopStoreIterator iterator()
    {
        return HadoopStoreIterator.create( this );
    }

    /**
     * Obtain an Iterator over the content of this Store.
     *
     * @return the Iterator
     */
    public HadoopStoreIterator getIterator()
    {
        return iterator();
    }


    /**
     * Determine the number of StoreValues in this Store.
     *
     * @return the number of StoreValues in this Store
     * @throws IOException thrown if an I/O error occurred
     */
    public int size()
        throws
            IOException
    {
        return size( "" );
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
        PathFilter filter = new PathFilter() {
            public boolean accept(
                    Path candidate )
            {
                String  key = theKeyMapper.pathToKey( candidate );
                boolean ret = key.startsWith( startsWith );
                return ret;
            }
        };

        FileStatus [] stati = theFileSystem.listStatus( theSubDir, filter );
        
        return stati.length;
    }

    /**
     * Determine whether this Store is empty.
     *
     * @return true if this Store is empty
     * @throws IOException thrown if an I/O error occurred
     */
    public boolean isEmpty()
        throws
            IOException
    {
        return size() == 0;
    }

    /**
     * Convert to String, for debugging.
     *
     * @return String form
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                    "fileSystem",
                    "subDir",
                    "keyMapper",
                    "storeValueMapper"
                },
                new Object[] {
                    theFileSystem,
                    theSubDir,
                    theKeyMapper,
                    theStoreValueMapper
                });
    }


    /**
     * The Hadoop FileSystem.
     */
    protected FileSystem theFileSystem;
    
    /**
     * The subdirectory underneath which all data is stored.
     */
    protected Path theSubDir;

    /**
     * Maps Store keys to FileSystem Paths and vice versa.
     */
    private KeyPathMapper theKeyMapper;

    /**
     * Maps StoreValues to file content, and vice versa.
     */
    protected StoreValueMapper theStoreValueMapper;
}
