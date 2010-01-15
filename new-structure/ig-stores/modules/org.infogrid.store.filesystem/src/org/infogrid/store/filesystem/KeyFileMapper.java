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

package org.infogrid.store.filesystem;

import java.io.File;

/**
 * Maps Store keys to paths in the local file system and vice versa.
 */
public interface KeyFileMapper
{
    /**
     * Map from Store key to path in the local file sytem.
     * 
     * @param key the Store key
     * @return the path in the file system
     */
    public File keyToFile(
            String key );
    
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
            IllegalArgumentException;
    
    /**
     * Obtain the list of files, as an iterable, that, when deleted recursively, cause the
     * entire FilesystemStore to be emptied. For example, a FilesystemStore that held all of
     * its files under a single root directory would return that single root directory.
     * 
     * @return iterable over the to-be-deleted files
     */
    public Iterable<File> rootFiles();
}
    