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

package org.infogrid.util;

/**
 * This interface is implemented by objects that support the smart factory pattern.
 * This has many methods that are almost the same as <code>java.util.Map</code>.
 * However, I don't like that that interface isn't type-safe with respect to keys,
 * and so we are currently not inheriting from there.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 * @param <A> the type of argument
 */
public interface SmartFactory<K,V,A>
        extends
            Factory<K,V,A>,
            WritableNameServer<K,V>
{
    /**
     * Factory method. This method will only be successful if the SmartFactory does not have
     * an object with the key yet; otherwise it throws an ObjectExistsAlreadyFactoryException.
     *
     * @param key the key information required for object creation, if any
     * @param argument any argument-style information required for object creation, if any
     * @return the created object
     * @throws ObjectExistsAlreadyFactoryException an object with this key existed already
     * @throws FactoryException catch-all Exception, consider its cause
     */
    public abstract V obtainNewFor(
            K key,
            A argument )
        throws
            ObjectExistsAlreadyFactoryException,
            FactoryException;

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
    public abstract V obtainNewFor(
            K key )
        throws
            ObjectExistsAlreadyFactoryException,
            FactoryException;
}
