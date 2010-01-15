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

package org.infogrid.store.jets3t;

import java.util.ArrayList;
import org.infogrid.util.ResourceHelper;

/**
 * A trivial implementation to map Store keys to S3 keys and vice versa, by
 * mapping all keys into a subdirectory.
 */
public class SubdirectoryKeyS3KeyMapper
        implements KeyS3KeyMapper
{
    /**
     * Factory method.
     * 
     * @param prefix the S3 key prefix identifying the directory
     * @return the SubdirectoryKeyS3KeyMapper
     */
    public static SubdirectoryKeyS3KeyMapper create(
            String prefix )
    {
        return new SubdirectoryKeyS3KeyMapper( prefix );
    }

    /**
     * Constructor.
     * 
     * @param prefix the S3 key prefix identifying the directory
     */
    protected SubdirectoryKeyS3KeyMapper(
            String prefix )
    {
        thePrefix = prefix;
    }
    
    /**
     * Map from Store key to FileSystem Path.
     * 
     * @param key the Store key
     * @return the FileSystem Path
     */
    public String keyToS3Key(
            String key )
    {
        StringBuilder ret = new StringBuilder();
        ret.append( thePrefix );
        ret.append( key );
        ret.append( FILE_SUFFIX );
        return ret.toString();
    }
    
    /**
     * Map from FileSystem Path to Store key.
     * 
     * @param path the FileSystem Path
     * @return the Store key
     */
    public String s3KeyToKey(
            String path )
    {
        if( path.startsWith( thePrefix ) && path.endsWith( FILE_SUFFIX )) {
            String ret = path.substring( thePrefix.length(), path.length() - FILE_SUFFIX.length() );
            return ret;
        } else {
            throw new IllegalArgumentException( "Cannot be mapped: " + path );
        }
    }
    
    /**
     * Obtain the list of paths, as an iterable, that, when deleted recursively, cause the
     * entire Store to be emptied. For example, a HadoopStore that held all of
     * its files under a single root directory would return that single root directory.
     * 
     * @return iterable over the to-be-deleted files
     */
    public Iterable<String> rootPaths()
    {
        ArrayList<String> ret = new ArrayList<String>( 1 );
        ret.add( thePrefix );
        return ret;
    }

    /**
     * The S3 key prefix indicating the subdirectory into which to map.
     */
    protected String thePrefix;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( SubdirectoryKeyS3KeyMapper.class );
    
    /**
     * The suffix to use for files.
     */
    public static final String FILE_SUFFIX = theResourceHelper.getResourceStringOrDefault( "FileSuffix", ".dat" );
}
