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
 * <p>This Exception is an abstract superclass over all Exceptions thrown by the ObjectStore.</p>
 *
 * <p>Unfortunately, Java does not allow the declaration of parameterized Exceptions, and
 * thus we have to use <code>Object</code> instead of a more specific type.</p>
 */
public abstract class ObjectStoreException
        extends
            Exception
{
    /**
     * Constructor.
     *
     * @param objectStore the ObjectStore that threw the Exception
     * @param key the key in the ObjectStore related to which the Exception occurred
     * @param message a message
     * @param cause the underlying cause for this Exception
     */
    protected ObjectStoreException(
            ObjectStore objectStore,
            Object      key,
            String      message,
            Throwable   cause )
    {
        super( message, cause );

        theObjectStore = objectStore;
        theKey         = key;
    }

    /**
     * Obtain the ObjectStore that caused this Exception.
     *
     * @return the ObjectStore
     */
    public ObjectStore getObjectStore()
    {
        return theObjectStore;
    }
    
    /**
     * Obtain the key in the ObjectStore related to which the Exception occurred
     *
     * @return the key
     */
    public Object getKey()
    {
        return theKey;
    }

    /**
     * The ObjectStore that threw this Exception.
     */
    protected ObjectStore theObjectStore;

    /**
     * The key that caused this Exception.
     */
    protected Object theKey;
}
