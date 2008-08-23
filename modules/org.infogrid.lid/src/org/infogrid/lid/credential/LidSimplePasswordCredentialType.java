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


package org.infogrid.lid.credential;

import org.infogrid.lid.LidInvalidCredentialException;
import org.infogrid.util.LocalizedObject;
import org.infogrid.util.LocalizedObjectFormatter;
import org.infogrid.util.ResourceHelper;

/**
 * Represents a a password credential type.
 */
public class LidSimplePasswordCredentialType
        extends
            LidCredentialType
{
    /**
     * Perform a check of the validity of a presented credential.
     * 
     * @param identifier the identifier for which credential was presented
     * @param presented the presented credential
     * @param stored the stored credential
     * @throws LidInvalidCredentialException thrown if the credential was invalid
     */
    public void checkCredential(
            String identifier,
            String presented,
            String stored )
        throws
            LidInvalidCredentialException
    {
        if( presented == null ) {
            if( stored != null ) {
                throw new LidInvalidCredentialException( identifier );
            } else {
                return;
            }
        } else if( !presented.equals( stored )) {
            throw new LidInvalidCredentialException( identifier );
        } else {
            return;
        }
    }

    /**
     * Determine the correct internationalized string that can be shown to the
     * user.
     *
     * @param formatter the formatter to use for data objects to be displayed as part of the message
     * @return the internationalized string
     */
    public String getLocalizedMessage(
            LocalizedObjectFormatter formatter )
    {
        return theResourceHelper.getResourceString( LocalizedObject.MESSAGE_PARAMETER );
    }
 
    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( LidSimplePasswordCredentialType.class  );
}

