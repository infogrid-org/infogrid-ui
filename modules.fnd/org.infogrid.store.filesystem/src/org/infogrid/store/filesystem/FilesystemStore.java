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

package org.infogrid.store.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.infogrid.store.AbstractIterableStore;
import org.infogrid.store.StoreKeyDoesNotExistException;
import org.infogrid.store.StoreKeyExistsAlreadyException;
import org.infogrid.store.StoreValue;
import org.infogrid.store.util.SimpleStoreValueMapper;
import org.infogrid.store.util.StoreValueMapper;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;
import org.infogrid.util.tree.FileTreeFacade;
import org.infogrid.util.tree.TreeFacade;
import org.infogrid.util.tree.TreeFacadeCursorIterator;

/**
 * Filesystem implementation of the Store interface.
 */
public class FilesystemStore
        extends
            AbstractIterableStore
        implements
            CanBeDumped
{
    private static final Log log = Log.getLogInstance( FilesystemStore.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param subDir the subdirectory in the file system that becomes the top mapping directory. 
     * @return the created FilesystemStore
     */
    public static FilesystemStore create(
            File subDir )
    {
        return new FilesystemStore(
                subDir,
                SubdirectoryKeyFileMapper.create( subDir ),
                SimpleStoreValueMapper.create() );
    }

    /**
     * Factory method.
     *
     * @param subDir the subdirectory in the file system that becomes the top mapping directory
     * @param keyMapper maps Store keys into file system paths
     * @param storeValueMapper maps StoreValues to file content, and vice versa
     * @return the created FilesystemStore
     */
    public static FilesystemStore create(
            File             subDir,
            KeyFileMapper    keyMapper,
            StoreValueMapper storeValueMapper )
    {
        return new FilesystemStore( subDir, keyMapper, storeValueMapper );
    }

    /**
     * Constructor.
     *
     * @param subDir the subdirectory in the file system that becomes the top mapping directory
     * @param keyMapper maps Store keys into file system paths
     * @param storeValueMapper maps StoreValues to file content, and vice versa
     */
    protected FilesystemStore(
            File             subDir,
            KeyFileMapper    keyMapper,
            StoreValueMapper storeValueMapper )
    {
        theSubDir           = subDir;
        theKeyMapper        = keyMapper;
        theStoreValueMapper = storeValueMapper;
        
        if( log.isTraceCallEnabled() ) {
            log.traceMethodCallEntry( this, "constructor" );
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
        if( !theSubDir.exists() ) {
            theSubDir.mkdirs();

        } else if( !theSubDir.isDirectory() ) {
            throw new IOException( "Cannot initialize FilesystemStore at " + theSubDir.getAbsolutePath() + ": file is in the way" );

        } else {
            File [] found = theSubDir.listFiles();
            for( int i = 0 ; i<found.length ; ++i ) {
                deleteRecursively( found[i] );
            }
        }
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
        if( !theSubDir.exists() ) {
            theSubDir.mkdirs();
            
        } else if( !theSubDir.isDirectory() ) {
            throw new IOException( "Cannot initialize FilesystemStore at " + theSubDir.getAbsolutePath() + ": file is in the way" );
        }
    }
    
    /**
     * Obtain the top-level directory underneath which all data is stored.
     * 
     * @return the top-level directory
     */
    public File getTopDirectory()
    {
        return theSubDir;
    }

    /**
     * Obtain the KeyFileMapper.
     * 
     * @return the KeyFileMapper
     */
    public KeyFileMapper getKeyFileMapper()
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
            File   file = theKeyMapper.keyToFile( key );
            
            if( file.exists() ) {
                throw new StoreKeyExistsAlreadyException( this, key );
            }
            file.getParentFile().mkdirs();
            file.createNewFile();
            
            OutputStream stream = new FileOutputStream( file );
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
            File   file = theKeyMapper.keyToFile( key );
            
            if( !file.exists() ) {
                throw new StoreKeyDoesNotExistException( this, key );
            }
            
            OutputStream stream = new FileOutputStream( file );
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
            File   file = theKeyMapper.keyToFile( key );
            
            ret = file.exists();
            
            if( !ret ) {
                file.createNewFile();
            }
            OutputStream stream = new FileOutputStream( file );
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
            final String key )
        throws
            StoreKeyDoesNotExistException,
            IOException
    {
        if( log.isInfoEnabled() ) {
            log.info( this + ".get( " + key + " )" );
        }

        StoreValue ret = null;
        try {
            File file = theKeyMapper.keyToFile( key );
            
            if( !file.exists()) {
                throw new StoreKeyDoesNotExistException( this, key );
            }
            
            InputStream stream = new FileInputStream( file );
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
            final String key )
        throws
            StoreKeyDoesNotExistException,
            IOException
    {
        if( log.isInfoEnabled() ) {
            log.info( this + ".delete( " + key + " )" );
        }
        try {
            File file = theKeyMapper.keyToFile( key );
            
            if( !file.exists()) {
                throw new StoreKeyDoesNotExistException( this, key );
            }
            file.delete();

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
            final String startsWith )
        throws
            IOException
    {
        if( log.isTraceCallEnabled() ) {
            log.traceMethodCallEntry( this, "deleteAll" );
        }

        TreeFacade<File>     facade = FileTreeFacade.create( getTopDirectory() );
        CursorIterator<File> iter   = TreeFacadeCursorIterator.create( facade, File.class );

        // we delete them backwards, so we get the directories last
        iter.moveToAfterLast();
        while( iter.hasPrevious() ) {
            File current = iter.previous();
            if( !theSubDir.equals( current )) {
                if( current.isDirectory() ) {
                    if( !current.delete() ) {
                        log.warn( "Could not delete file " + current );
                    }
                } else {
                    String key = theKeyMapper.fileToKey( current );

                    if( key.startsWith( startsWith )) {
                        if( !current.delete() ) {
                            log.warn( "Could not delete file " + current );
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper method to recursively delete a File.
     * 
     * @param root the root file
     * @throws IOException thrown if one or more files could not be deleted
     */
    public static void deleteRecursively(
            File root )
        throws
            IOException
    {
        if( root.isDirectory() ) {
            for( File current : root.listFiles() ) {
                deleteRecursively( current );
            }
        }
    
        if( !root.delete()) {
            throw new IOException( "Unable to delete file " + root );
        }
    }

    /**
     * Obtain an Iterator over the content of this Store.
     *
     * @return the Iterator
     */
    public FilesystemStoreIterator iterator()
    {
        return FilesystemStoreIterator.create( this );
    }

    /**
     * Determine the number of StoreValues in this Store whose key starts with this String.
     *
     * @param startsWith the String the key starts with
     * @return the number of StoreValues in this Store whose key starts with this String
     */
    public int size(
            final String startsWith )
    {
        FilesystemStoreIterator iter = iterator();
        int                     ret  = 0;
        
        while( iter.hasNext() ) {
            StoreValue found = iter.next();
            
            if( found.getKey().startsWith( startsWith )) {
                ++ret;
            }
        }
        return ret;
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
                    "subDir",
                    "keyMapper",
                    "storeValueMapper"
                },
                new Object[] {
                    theSubDir,
                    theKeyMapper,
                    theStoreValueMapper
                });
    }

    /**
     * The subdirectory underneath which all data is stored.
     */
    protected File theSubDir;

    /**
     * Maps Store keys to paths in the local file system and vice versa.
     */
    protected KeyFileMapper theKeyMapper;
    
    /**
     * Maps StoreValues to file content, and vice versa.
     */
    protected StoreValueMapper theStoreValueMapper;
}
