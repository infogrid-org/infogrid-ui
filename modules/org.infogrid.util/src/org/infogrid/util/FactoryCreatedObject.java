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
 * This interface may be implemented by objects created by {@link Factory Factories}.
 * The method {@link #getFactoryKey} returns the key that was used to create this
 * object by the Factory. This key should be used by {@link SmartFactory SmartFactories}
 * to store the <code>FactoryCreatedObject<code>. While it typically is the same, this
 * key may be different from the key specified in the <code>SmartFactory</code>'s
 * {@link SmartFactory#obtainFor obtainFor} method.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 * @param <A> the type of argument
 */
public interface FactoryCreatedObject<K,V,A>
{
    /**
     * Obtain the key for that was used to create this object by the Factory.
     *
     * @return the key
     */
    public K getFactoryKey();
    
    /**
     * Enable a Factory to indicate to the FactoryCreatedObject that it was
     * it that created it.
     *
     * @param factory the Factory that created the FactoryCreatedObject
     */
    public void setFactory(
            Factory<K,V,A> factory );

    /**
     * Obtain the Factory that created this FactoryCreatedObject. In case of
     * chained factories that delegate to each other, this method is
     * supposed to return the outermost factory invoked by the application programmer.
     *
     * @return the Factory that created the FactoryCreatedObject
     */
    public Factory<K,V,A> getFactory();
}
