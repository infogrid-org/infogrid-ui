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
 * This Exception is thrown if there is something wrong with this installation.
 */
public class SoftwareInstallationException
        extends
            Exception
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
      * Create one with a message.
      *
      * @param message the message
      */
    public SoftwareInstallationException(
            String message )
    {
        super( message );
    }

    /**
      * Create one with a message and an Exception.
      *
      * @param message the message
      * @param cause the underlying Exception
      */
    public SoftwareInstallationException(
            String    message,
            Exception cause )
    {
        super( message, cause );
    }
}
