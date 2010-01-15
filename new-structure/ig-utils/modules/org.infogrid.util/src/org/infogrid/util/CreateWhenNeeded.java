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
 * Helper class that instantiates an object when needed, but only once.
 *
 * @param <T> the type of instantiated object
 */
public abstract class CreateWhenNeeded<T>
{
    /**
     * Smartly create the object. The first time this is invoked, the object
     * will be instantiated. All subsequent times, the same instance will
     * be returned.
     *
     * @return the instantiated object
     * @throws CreateWhenNeededException wrapper exception if something went wrong; the original exception is carried as the cause
     */
    public synchronized T obtain()
        throws
            CreateWhenNeededException
    {
        if( theObject == null ) {
            try {
                theObject = instantiate();

            } catch( Throwable t ) {
                throw new CreateWhenNeededException( this, t );
            }
        }
        return theObject;
    }

    /**
     * Determine whether the object has indeed been instantiated so far.
     *
     * @return true the object has been instantiated
     */
    public boolean hasBeenCreated()
    {
        return theObject != null;
    }

    /**
     * Instantiation method.
     *
     * @return the instantiated object
     * @throws Throwable a problem occurred
     */
    protected abstract T instantiate()
        throws
            Throwable;

    /**
     * The instance, once it is allocated.
     */
    private T theObject;
}
