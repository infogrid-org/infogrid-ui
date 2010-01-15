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
 * Makes implementing FactoryCreatedObjects a tiny bit simpler.
 * 
 * @param <K> the type of key
 * @param <V> the type of value
 * @param <A> the type of argument
 */
public abstract class AbstractFactoryCreatedObject<K,V,A>
        implements
            FactoryCreatedObject<K,V,A>
{
    /**
     * Constructor for subclasses only.
     */
    protected AbstractFactoryCreatedObject()
    {
        // nothing right now
    }

    /**
     * Enable a Factory to indicate to the FactoryCreatedObject that it was
     * it that created it.
     *
     * @param factory the Factory that created the FactoryCreatedObject
     */
    public void setFactory(
            Factory<K,V,A> factory )
    {
        theFactory = factory;
    }

    /**
     * Obtain the Factory that created this FactoryCreatedObject. In case of
     * chained factories that delegate to each other, this method is
     * supposed to return the outermost factory invoked by the application programmer.
     *
     * @return the Factory that created the FactoryCreatedObject
     */
    public Factory<K,V,A> getFactory()
    {
        return theFactory;
    }
    
    /**
     * Subclasses can invoke this method whenever they are updated. This will cause
     * the Factory to be notified of the update.
     */
    protected void factoryCreatedObjectUpdated()
    {
        Factory<K,V,A> factory = theFactory; // trick allows us to not use a synchronized
        if( factory != null ) {
            factory.factoryCreatedObjectUpdated( this );
        }
    }

    /**
     * The Factory that created this FactoryCreatedObject.
     */
    private Factory<K,V,A> theFactory;
}
