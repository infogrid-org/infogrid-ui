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
 * The abstract superclass of all Module Framework-related Exceptions.
 */
public abstract class ModuleException
    extends
        Exception
{
    /**
     * Constructor.
     *
     * @param adv the ModuleAdvertisement of the Module that caused this Exception
     * @param cause the Throwable that caused this Exception
     */
    protected ModuleException(
            ModuleAdvertisement adv,
            Throwable           cause )
    {
        super( null, cause ); // do not create default message

        theModuleAdvertisement = adv;
    }

    /**
     * The ModuleAdvertisement whose Module had a problem.
     */
    protected ModuleAdvertisement theModuleAdvertisement;
}
