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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.module;

import java.net.URL;

/**
 * This exception is thrown if we cannot find a Module whose ModuleAdvertisement we have.
 * It may also be thrown if a Module is missing resources (such as a JAR file).
 */
public class ModuleNotFoundException
        extends
            ModuleException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
      * Constructor.
      *
      * @param adv the ModuleAdvertisement whose Module we could not find
      * @param cause the cause of this ModuleNotFoundException (e.g. IOException)
      */
    public ModuleNotFoundException(
            ModuleAdvertisement adv,
            Throwable           cause )
    {
        super( adv, cause );
    }

    /**
     * For debugging.
     *
     * @return string representation of this object
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer( 100 ); // fudge
        buf.append( "ModuleNotFoundException: could not resolve Module " );

        buf.append( theModuleAdvertisement.getModuleName() );
        if( theModuleAdvertisement.getModuleVersion() != null ) {
            buf.append( theModuleAdvertisement.getModuleVersion() );
        } else {
            buf.append( "?" );
        }
        buf.append( ", was looking for JAR files: " );

        String [] theJarFiles = theModuleAdvertisement.getProvidesJars();
        URL       theBaseUrl  = theModuleAdvertisement.getProvidesJarsBaseDownloadUrl();

        for( int i=0 ; i<theJarFiles.length ; ++i ) {
            if( i > 0 ) {
                buf.append( ", " );
            }
            buf.append( theJarFiles[i] );
        }
        if( theBaseUrl != null ) {
            buf.append( " at URL " );
            buf.append( theBaseUrl.toExternalForm() );
        }
        return buf.toString();
    }
}
