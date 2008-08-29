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

package org.infogrid.store.object;

/**
 * <p>Thrown to indicate that a key does not exist in the ObjectStore, for an operation
 * that requires that the key exists already.</p>
 *
 * <p>Unfortunately, Java does not allow the declaration of parameterized Exceptions, and
 * thus we have to use <code>Object</code> instead of a more specific type.</p>
 */
public class ObjectStoreKeyDoesNotExistException
        extends
            ObjectStoreException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param objectStore the ObjectStore in which the key did not exist
     * @param key the key that does not exist in the ObjectStore
     */
    public ObjectStoreKeyDoesNotExistException(
            ObjectStore objectStore,
            Object      key )
    {
        super( objectStore, key, "Key does not exist: " + key, null );
    }
    
    /**
     * Constructor.
     *
     * @param objectStore the ObjectStore in which the key did not exist
     * @param key the key that does not exist in the ObjectStore
     * @param cause the underlying cause
     */
    public ObjectStoreKeyDoesNotExistException(
            ObjectStore objectStore,
            Object      key,
            Throwable   cause )
    {
        super( objectStore, key, "Key does not exist: " + key, cause );
    }
}
