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

package org.infogrid.store.prefixing;

import org.infogrid.store.StoreKeyDoesNotExistException;

/**
 * Thrown to indicate that a key does not exist already in the <code>Store</code>, for an operation
 * that requires that the key exists already. This subclass is used by
 * <code>PrefixingStores</code>.
*/
public class PrefixingStoreKeyDoesNotExistException
        extends
            StoreKeyDoesNotExistException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param store the PrefixingStore that threw this Exception
     * @param localKey the key that does not exist in the PrefixingStore
     * @param cause the StoreKeyExistsAlreadyException thrown by the Store the PrefixingStore delegates to 
     */
    public PrefixingStoreKeyDoesNotExistException(
            PrefixingStore                store,
            String                        localKey,
            StoreKeyDoesNotExistException cause )
    {
        super( store, localKey, cause );
    }

    /**
     * Obtain the PrefixingStore in which the key did not exist.
     *
     * @return the PrefixingStore
     */
    @Override
    public PrefixingStore getStore()
    {
        return (PrefixingStore) super.getStore();
    }

    /**
     * Obtain the underlying exception, which we know to be a StoreKeyDoesNotExistException.
     *
     * @return the underlying StoreKeyDoesNotExistException
     */
    @Override
    public StoreKeyDoesNotExistException getCause()
    {
        return (StoreKeyDoesNotExistException) super.getCause();
    }
}
