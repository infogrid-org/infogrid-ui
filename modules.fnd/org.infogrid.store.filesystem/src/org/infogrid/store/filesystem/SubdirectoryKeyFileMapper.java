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
import java.util.ArrayList;
import org.infogrid.util.ResourceHelper;

/**
 * A trivial implementation to map Store keys to paths in the local file system and vice versa, by
 * mapping all keys into a subdirectory.
 * 
 * FIXME: this needs to do some kind of escaping to handle keys that contain FILE_SUFFIX.
 */
public class SubdirectoryKeyFileMapper
        implements
            KeyFileMapper
{
    /**
     * Factory method.
     * 
     * @param subdir the subdirectory into which to map
     * @return the created SubdirectoryKeyFileMapper
     */
    public static SubdirectoryKeyFileMapper create(
            File subdir )
    {
        return new SubdirectoryKeyFileMapper( subdir );
    }

    /**
     * Constructor.
     * 
     * @param subdir the subdirectory into which to map
     */
    protected SubdirectoryKeyFileMapper(
            File subdir )
    {
        theSubDir = subdir;
    }
    
    /**
     * Map from Store key to path in the local file sytem.
     * 
     * @param key the Store key
     * @return the path in the file system
     */
    public File keyToFile(
            String key )
    {
        String subdirString = theSubDir.getPath();
        File ret = new File( subdirString + "/" + key + FILE_SUFFIX );
        return ret;
    }
    
    /**
     * Map from path in the file system to Store key.
     * 
     * @param path the path in the file system
     * @return the Store key
     * @throws IllegalArgumentException thrown if the provided file system path cannot be mapped to a Store key
     */
    public String fileToKey(
            File path )
        throws
            IllegalArgumentException
    {
        String pathString   = path.getPath();
        String subdirString = theSubDir.getPath();
        
        if( pathString.startsWith(  subdirString ) && pathString.endsWith( FILE_SUFFIX)) {
            String ret = pathString.substring(  subdirString.length(), pathString.length() - FILE_SUFFIX.length() );
            return ret;
        } else {
            throw new IllegalArgumentException( "Cannot be mapped: " + path );
        }
    }

    /**
     * Obtain the list of files, as an iterable, that, when deleted recursively, cause the
     * entire FilesystemStore to be emptied. For example, a FilesystemStore that held all of
     * its files under a single root directory would return that single root directory.
     * 
     * @return iterable over the to-be-deleted files
     */
    public Iterable<File> rootFiles()
    {
        ArrayList<File> ret = new ArrayList<File>( 1 );
        ret.add( theSubDir );
        return ret;
    }

    /**
     * The subdirectory into which to map.
     */
    protected File theSubDir;
    
    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( SubdirectoryKeyFileMapper.class );
    
    /**
     * The suffix to use for files.
     */
    public static final String FILE_SUFFIX = theResourceHelper.getResourceStringOrDefault( "FileSuffix", ".dat" );
}
