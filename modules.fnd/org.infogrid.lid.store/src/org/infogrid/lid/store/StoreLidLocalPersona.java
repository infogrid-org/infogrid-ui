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

package org.infogrid.lid.store;

import java.util.Map;
import org.infogrid.lid.AbstractLidLocalPersona;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.lid.credential.LidInvalidCredentialException;
import org.infogrid.util.StringHelper;
import org.infogrid.util.http.SaneRequest;

/**
 * Implementation of LidLocalPersona for the LidLocalPersonaManager.
 */
public class StoreLidLocalPersona
        extends
            AbstractLidLocalPersona
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     * 
     * @param identifier the unique identifier of the persona, e.g. their identity URL
     * @param attributes attributes of the persona, e.g. first name
     * @param credentials the credentials available to authenticate this LidLocalPersona
     */
    protected StoreLidLocalPersona(
            String                        identifier,
            Map<String,String>            attributes,
            Map<LidCredentialType,String> credentials )
    {
        super( identifier, attributes, credentials.keySet() );

        theCredentials = credentials;
    }

    /**
     * Perform a check of the validity of a presented credential.
     * 
     * @param credType the LidCredentialType to check
     * @param request the incoming request carrying the presented credential
     * @throws LidInvalidCredentialException thrown if the credential was invalid
     */
    public void checkCredential(
            LidCredentialType credType,
            SaneRequest       request )
        throws
            LidInvalidCredentialException
    {
        if( !theCredentialTypes.contains( credType ) ) {
            throw new LidInvalidCredentialException( theIdentifier, credType );
        }
        if( !request.matchArgument( "lid-credtype", "simple-password" ) ) {
            throw new LidInvalidCredentialException( theIdentifier, credType );
        }
        String givenPassword   = request.getArgument( "lid-credential" );
        String correctPassword = theCredentials.get( credType );

        int result = StringHelper.compareTo( givenPassword, correctPassword );
        if( result != 0 ) {
            throw new LidInvalidCredentialException( theIdentifier, credType );
        }
    }

    /**
     * Obtain a specific credential.
     * 
     * @param type the LidCredentialType for which the credential is to be obtained
     * @return the credential, or null
     */
    public String getCredentialFor(
            LidCredentialType type )
    {
        return theCredentials.get( type );
    }
    /**
     * Our credentials.
     */
    protected Map<LidCredentialType,String> theCredentials;
}
