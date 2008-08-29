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

package org.infogrid.module;

/**
 * This exception is thrown if a ModuleAdvertisement could not be instantiated.
 */
public class ModuleAdvertisementInstantiationException
        extends
            ModuleException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
      * Constructor.
      *
      * @param instantiator the ModuleAdvertisementInstantiator that was unsuccessful
      * @param org the Throwable that was the cause of this ModuleActivationException
      */
    public ModuleAdvertisementInstantiationException(
            ModuleAdvertisementInstantiator instantiator,
            Throwable                       org )
    {
        super( null, org );

        theInstantiator = instantiator;
    }

    /**
     * For debugging.
     *
     * @return a string representation of this object
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer( 100 ); // fudge
        buf.append( "ModuleAdvertisementInstantiationException: could not instantiate through " );
        buf.append( theInstantiator );

        return buf.toString();
    }

    /**
     * The ModuleAdvertisementInstantiator that failed.
     */
    protected ModuleAdvertisementInstantiator theInstantiator;
}
