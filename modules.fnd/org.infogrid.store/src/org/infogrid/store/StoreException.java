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

package org.infogrid.store;

/**
 * Abstract superclass for Exceptions thrown by <code>Store</code>.
 */
public abstract class StoreException
        extends
            Exception
{
    /**
     * Constructor.
     *
     * @param store the Store that threw the Exception
     * @param key the key in the Store that caused the Exception
     * @param message a message
     * @param cause the underlying cause for this Exception
     */
    protected StoreException(
            Store     store,
            String    key,
            String    message,
            Throwable cause )
    {
        super( message, cause );

        theStore = store;
        theKey   = key;
    }

    /**
     * Obtain the Store that caused this Exception.
     *
     * @return the Store
     */
    public Store getStore()
    {
        return theStore;
    }
    
    /**
     * Obtain the key related to which the Exception occurred.
     *
     * @return the key
     */
    public String getKey()
    {
        return theKey;
    }

    /**
     * The Store that threw this Exception.
     */
    protected Store theStore;

    /**
     * The key related to which this Exception occurred.
     */
    protected String theKey;
}
