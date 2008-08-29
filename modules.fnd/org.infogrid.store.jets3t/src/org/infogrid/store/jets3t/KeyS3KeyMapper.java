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

/**
 * Maps Store keys to FileSystem Paths and vice versa.
 */
public interface KeyS3KeyMapper
{
    /**
     * Map from Store key to FileSystem Path.
     * 
     * @param key the Store key
     * @return the FileSystem Path
     */
    public String keyToS3Key(
            String key );
    
    /**
     * Map from FileSystem Path to Store key.
     * 
     * @param path the FileSystem Path
     * @return the Store key
     */
    public String s3KeyToKey(
            String path );

    /**
     * Obtain the list of paths, as an iterable, that, when deleted recursively, cause the
     * entire Store to be emptied. For example, a Store that held all of
     * its files under a single root directory would return that single root directory.
     * 
     * @return iterable over the to-be-deleted files
     */
    public Iterable<String> rootPaths();
}
