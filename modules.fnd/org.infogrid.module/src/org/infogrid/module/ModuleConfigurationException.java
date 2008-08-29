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
 * This exception is thrown if Module configuration was unsuccessful.
 */
public class ModuleConfigurationException
        extends
            ModuleException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
      * Constructor.
      *
      * @param adv the ModuleAdvertisement for the Module whose configuration failed.
      * @param msg an optional message
      */
    public ModuleConfigurationException(
            ModuleAdvertisement adv,
            String              msg )
    {
        this( adv, msg, null );
    }

    /**
      * Constructor.
      *
      * @param adv the ModuleAdvertisement for the Module whose configuration failed.
      * @param org the Throwable that was the cause of this ModuleConfigurationException
      */
    public ModuleConfigurationException(
            ModuleAdvertisement adv,
            Throwable           org )
    {
        this( adv, null, org );
    }

    /**
      * Constructor.
      *
      * @param adv the ModuleAdvertisement for the Module whose configuration failed.
      * @param msg an optional message
      * @param cause the Throwable that was the cause of this ModuleConfigurationException
      */
    public ModuleConfigurationException(
            ModuleAdvertisement adv,
            String              msg,
            Throwable           cause )
    {
        super( adv, cause );

        theMessage = msg;
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
        buf.append( "ModuleActivationException: could not configure Module " );
        if( theModuleAdvertisement != null ) {
            buf.append( theModuleAdvertisement.getModuleName() );
            buf.append( ", version " );
            if( theModuleAdvertisement.getModuleVersion() != null ) {
                buf.append( theModuleAdvertisement.getModuleVersion() );
            } else {
                buf.append( "?" );
            }
        } else {
            buf.append( "null" );
        }

        if( theMessage != null ) {
            buf.append( ", message: " );
            buf.append( theMessage );
        }
        return buf.toString();
    }

    /**
     * A message, if any.
     */
    protected String theMessage;
}
