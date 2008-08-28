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
 * This exception indicates that a ModuleRequirement could not be resolved into
 * a single resolution candidate.
 */
public class ModuleResolutionCandidateNotUniqueException
        extends
            ModuleException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
      * Construct one.
      *
      * @param req the ModuleRequirement that could not be met
      * @param foundAdvertisements the zero or more than one ModuleAdvertisements found
      */
    public ModuleResolutionCandidateNotUniqueException(
             ModuleRequirement      req,
             ModuleAdvertisement [] foundAdvertisements )
    {
        super( null, null );

        theRequirement = req;
        theFound       = foundAdvertisements;
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
        buf.append( " unambiguously: found " );
        if( theFound != null ) {
            buf.append(  theFound.length );
        } else {
            buf.append( "null" );
        }
        buf.append( "." );
        return buf.toString();
    }

    /**
     * The ModuleRequirement that could not be met unambiguously.
     */
    protected ModuleRequirement theRequirement;
    
    /**
     * The ModuleAdvertisements that were found.
     */
    protected ModuleAdvertisement [] theFound;
}
