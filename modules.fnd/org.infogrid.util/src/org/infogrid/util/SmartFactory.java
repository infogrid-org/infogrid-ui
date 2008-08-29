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
     * Invoked only by objects that have been created by this SmartFactory, this enables
     * the created objects to indicate to the SmartFactory that they have been updated.
     * Depending on the implementation of the SmartFactory, that may cause the
     * SmartFactory to write changes to disk, for example.
     *
     * @param object the FactoryCreatedObject
     */
    public void factoryCreatedObjectUpdated(
            FactoryCreatedObject<K,V,A> object );    
}
