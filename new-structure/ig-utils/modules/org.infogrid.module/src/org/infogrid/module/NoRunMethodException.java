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
 * This exception indicates that a Module's specified run method was invoked, but
 * that the method could not be found.
 */
public class NoRunMethodException
        extends
            ModuleException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
      * Constructor.
      *
      * @param adv the ModuleAdvertisement whose Module we wanted to run
      * @param runClassName the name of the class whose run method we wanted to execute, or null if not known
      * @param runMethodName the name of the method within the runClassName that we wanted to execute, or null if not known
      * @param cause the Throwable that caused this Exception
      */
    public NoRunMethodException(
             ModuleAdvertisement adv,
             String              runClassName,
             String              runMethodName,
             Throwable           cause )
    {
        super( adv, cause );

        theRunClassName  = runClassName;
        theRunMethodName = runMethodName;
    }

    /**
     * For debugging.
     *
     * @return this object in printable format
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer( 100 );
        buf.append( "NoRunMethodException: Module: " );
        if( theModuleAdvertisement != null ) {
            buf.append( theModuleAdvertisement.getModuleName() );
            buf.append( ", version: " );
            if( theModuleAdvertisement.getModuleVersion() != null ) {
                buf.append( theModuleAdvertisement.getModuleVersion() );
            } else {
                buf.append( "?" );
            }
        } else {
            buf.append( "null" );
        }

        buf.append( ", cannot execute run method " );
        buf.append( theRunMethodName );
        buf.append( " in class " );
        buf.append( theRunClassName );
        return buf.toString();
    }

    /**
     * Name of the class whose run method we were trying to run.
     */
    protected String theRunClassName;

    /**
     * Name of the method within theRunClassName that we were trying to run.
     */
    protected String theRunMethodName;
}
