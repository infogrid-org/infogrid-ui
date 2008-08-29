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

import java.util.ArrayList;
import org.apache.hadoop.fs.Path;
import org.infogrid.util.ResourceHelper;

/**
 * A trivial implementation to map Store keys to FileSystem Paths and vice versa, by
 * mapping all keys into a subdirectory.
 */
public class SubdirectoryKeyPathMapper
        implements KeyPathMapper
{
    /**
     * Factory method.
     * 
     * @param subdir the subdirectory into which to map
     * @return the KeyPathMapper
     */
    public static SubdirectoryKeyPathMapper create(
            Path subdir )
    {
        return new SubdirectoryKeyPathMapper( subdir );
    }

    /**
     * Constructor.
     * 
     * @param subdir the subdirectory into which to map
     */
    protected SubdirectoryKeyPathMapper(
            Path subdir )
    {
        theSubDir = subdir;
    }
    
    /**
     * Map from Store key to FileSystem Path.
     * 
     * @param key the Store key
     * @return the FileSystem Path
     */
    public Path keyToPath(
            String key )
    {
        String subdirString = theSubDir.toString();

        Path ret = new Path( subdirString + "/" + key + FILE_SUFFIX );
        return ret;
    }
    
    /**
     * Map from FileSystem Path to Store key.
     * 
     * @param path the FileSystem Path
     * @return the Store key
     */
    public String pathToKey(
            Path path )
    {
        String pathString   = path.toString();
        String subdirString = theSubDir.toString();
        
        if( pathString.startsWith(  subdirString ) && pathString.endsWith( FILE_SUFFIX )) {
            String ret = pathString.substring(  subdirString.length(), pathString.length() - FILE_SUFFIX.length() );
            return ret;
        } else {
            throw new IllegalArgumentException( "Cannot be mapped: " + path );
        }
    }
    
    /**
     * Obtain the list of paths, as an iterable, that, when deleted recursively, cause the
     * entire HadoopStore to be emptied. For example, a HadoopStore that held all of
     * its files under a single root directory would return that single root directory.
     * 
     * @return iterable over the to-be-deleted files
     */
    public Iterable<Path> rootPaths()
    {
        ArrayList<Path> ret = new ArrayList<Path>( 1 );
        ret.add( theSubDir );
        return ret;
    }

    /**
     * The subdirectory into which to map.
     */
    protected Path theSubDir;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( SubdirectoryKeyPathMapper.class );
    
    /**
     * The suffix to use for files.
     */
    public static final String FILE_SUFFIX = theResourceHelper.getResourceStringOrDefault( "FileSuffix", ".dat" );
}
