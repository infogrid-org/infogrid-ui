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

package org.infogrid.util;

/**
 * Saves a bit of code to map the simplified factory method onto the more general one.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 * @param <A> the type of argument
 */
public abstract class AbstractSmartFactory<K,V,A>
        extends
            AbstractFactory<K,V,A>
        implements
            SmartFactory<K,V,A>
{
    /**
     * Factory method. This method will only be successful if the SmartFactory does not have
     * an object with the key yet; otherwise it throws an ObjectExistsAlreadyFactoryException.
     * This is equivalent to specifying a null argument.
     *
     * @param key the key information required for object creation, if any
     * @return the created object
     * @throws ObjectExistsAlreadyFactoryException an object with this key existed already
     * @throws FactoryException catch-all Exception, consider its cause
     */
    public V obtainNewFor(
            K key )
        throws
            ObjectExistsAlreadyFactoryException,
            FactoryException
    {
        return obtainNewFor( key, null );
    }

    /**
     * Determine whether the number of key-value pairs in this SmartFactory is zero.
     *
     * @return true if there are no key-value paris in this SmartFactory
     */
    public boolean isEmpty()
    {
        return size() == 0;
    }
}
