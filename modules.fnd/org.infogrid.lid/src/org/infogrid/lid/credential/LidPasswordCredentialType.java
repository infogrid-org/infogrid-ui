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

import org.infogrid.lid.LidLocalPersona;
import org.infogrid.util.AbstractLocalizedObject;
import org.infogrid.util.LocalizedObject;
import org.infogrid.util.LocalizedObjectFormatter;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.StringHelper;
import org.infogrid.util.http.SaneRequest;

/**
 * Represents a a password credential type.
 */
public class LidPasswordCredentialType
        extends
            AbstractLocalizedObject
        implements
            LidCredentialType
{
    /**
     * Factory method.
     * 
     * @return the created LidPasswordCredentialType
     */
    public static synchronized LidPasswordCredentialType create()
    {
        if( theSingleton == null ) {
            theSingleton = new LidPasswordCredentialType();
        }
        return theSingleton;
    }

    /**
     * Constructor for subclasses only.
     */
    protected LidPasswordCredentialType()
    {
        // nothing
    }

    /**
     * Determine the computable full-qualified name of this LidCredentialType.
     * 
     * @return the computable full name
     */
    public String getFullName()
    {
        return getClass().getName();
    }

//    /**
//     * Perform a check of the validity of a presented credential.
//     * 
//     * @param identifier the identifier for which credential was presented
//     * @param request the incoming request carrying the presented credential
//     * @param persona what is known locally about the persona
//     * @throws LidInvalidCredentialException thrown if the credential was invalid
//     */
//    public void checkCredential(
//            String      identifier,
//            SaneRequest request,
//            LidLocalPersona  persona )
//        throws
//            LidInvalidCredentialException
//    {
//        String givenCredential   = request.getArgument( "lid-credential" );
//        String correctCredential = persona.getCredentialFor( this );
//        
//        int result = StringHelper.compareTo( givenCredential, correctCredential );
//        if( result != 0 ) {
//            throw new LidInvalidCredentialException( identifier, this );
//        }
//    }
//
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
    private static final ResourceHelper theResourceHelper
            = ResourceHelper.getInstance( LidPasswordCredentialType.class );
    
    /**
     * The singleton instance of this class.
     */
    private static LidPasswordCredentialType theSingleton;
}
