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
 * This exception indicates that a Module cannot be resolved.
 */
public class ModuleResolutionException
        extends
            ModuleException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
      * Construct one.
      *
      * @param adv the ModuleAdvertisement whose ModuleRequirement could not be met
      * @param req the ModuleRequirement that could not be met
      * @param cause the Throwable that caused this Exception, if any
      */
    public ModuleResolutionException(
             ModuleAdvertisement adv,
             ModuleRequirement   req,
             Throwable           cause )
    {
        super( adv, cause );

        theRequirement = req;
    }

    /**
     * Returns the detail message string of this throwable.
     *
     * @return  the detail message string of this <tt>Throwable</tt> instance
     *          (which may be <tt>null</tt>).
     */
    @Override
    public String getMessage()
    {
        StringBuffer buf = new StringBuffer( 100 ); // fudge
        buf.append( "Could not resolve ModuleRequirement " );
        if( theRequirement != null ) {
            buf.append( theRequirement.getRequiredModuleName() );
            if( theRequirement.getRequiredModuleVersion() != null ) {
                buf.append( ", version: " );
                buf.append( theRequirement.getRequiredModuleVersion() );
            }

        } else {
            buf.append( "null" );
        }

        buf.append( " within Module " );
        if( theModuleAdvertisement != null ) {
            buf.append( theModuleAdvertisement.getModuleName() );
            buf.append( ", version: " );
            buf.append( theModuleAdvertisement.getModuleVersion() );

        } else {
            buf.append( "null" );
        }

        return buf.toString();
    }

    /**
     * The ModuleRequirement that could not be met.
     */
    protected ModuleRequirement theRequirement;
}
