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

package org.infogrid.lid;

import java.util.Collection;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.lid.credential.LidInvalidCredentialException;
import org.infogrid.util.ArrayHelper;

/**
 * A simple implementation of LidClientAuthenticationStatus.
 */
public class SimpleLidClientAuthenticationStatus
        extends
            AbstractLidClientAuthenticationStatus
{
    /**
     * Factory method.
     * 
     * @param clientIdentifier the normalized identifier provided by the client, if any
     * @param clientPersona the client LidPersona that was found, if any
     * @param session the client LidSession that was found, if any
     * @param carriedValidCredentialTypes the credential types carried as part of this request that validated successfully, if any
     * @param carriedInvalidCredentialTypes the credential types carried as part of this request that did not validate successfully, if any
     * @param invalidCredentialExceptions the exceptions indicating the problems with the invalid credentials, in the same sequence, if any
     * @param sessionClientIdentifier the normalized identifier of the client according to a currently valid session
     * @param sessionClientPersona the client LidPersona according to the currently valid session
     * @param wishesCancelSession the client wishes to cancel the session
     * @return the created SimpleLidClientAuthenticationStatus
     */
    public static SimpleLidClientAuthenticationStatus create(
            String                           clientIdentifier,
            LidPersona                       clientPersona,
            LidSession                       session,
            LidCredentialType []             carriedValidCredentialTypes,
            LidCredentialType []             carriedInvalidCredentialTypes,
            LidInvalidCredentialException [] invalidCredentialExceptions,
            String                           sessionClientIdentifier,
            LidPersona                       sessionClientPersona,
            boolean                          wishesCancelSession )
    {
        SimpleLidClientAuthenticationStatus ret = new SimpleLidClientAuthenticationStatus(
                clientIdentifier,
                clientPersona,
                session,
                carriedValidCredentialTypes,
                carriedInvalidCredentialTypes,
                invalidCredentialExceptions,
                sessionClientIdentifier,
                sessionClientPersona,
                wishesCancelSession );
        
        return ret;
    }

    /**
     * Factory method.
     *
     * @param clientIdentifier the normalized identifier provided by the client, if any
     * @param clientPersona the client LidPersona that was found, if any
     * @param session the client LidSession that was found, if any
     * @param carriedValidCredentialTypes the credential types carried as part of this request that validated successfully, if any
     * @param carriedInvalidCredentialTypes the credential types carried as part of this request that did not validate successfully, if any
     * @param invalidCredentialExceptions the exceptions indicating the problems with the invalid credentials, in the same sequence, if any
     * @param sessionClientIdentifier the normalized identifier of the client according to a currently valid session
     * @param sessionClientPersona the client LidPersona according to the currently valid session
     * @param wishesCancelSession the client wishes to cancel the session
     * @return the created SimpleLidClientAuthenticationStatus
     */
    public static SimpleLidClientAuthenticationStatus create(
            String                                    clientIdentifier,
            LidPersona                                clientPersona,
            LidSession                                session,
            Collection<LidCredentialType>             carriedValidCredentialTypes,
            Collection<LidCredentialType>             carriedInvalidCredentialTypes,
            Collection<LidInvalidCredentialException> invalidCredentialExceptions,
            String                                    sessionClientIdentifier,
            LidPersona                                sessionClientPersona,
            boolean                                   wishesCancelSession )
    {
        SimpleLidClientAuthenticationStatus ret = SimpleLidClientAuthenticationStatus.create(
                clientIdentifier,
                clientPersona,
                session,
                carriedValidCredentialTypes != null
                        ? ArrayHelper.copyIntoNewArray( carriedValidCredentialTypes,   LidCredentialType.class )
                        : null,
                carriedInvalidCredentialTypes != null
                        ? ArrayHelper.copyIntoNewArray( carriedInvalidCredentialTypes, LidCredentialType.class )
                        : null,
                invalidCredentialExceptions != null
                        ? ArrayHelper.copyIntoNewArray( invalidCredentialExceptions,   LidInvalidCredentialException.class )
                        : null,
                sessionClientIdentifier,
                sessionClientPersona,
                wishesCancelSession );

        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param clientIdentifier the normalized identifier provided by the client, if any
     * @param clientPersona the client LidPersona that was found, if any
     * @param session the client LidSession that was found, if any
     * @param carriedValidCredentialTypes the credential types carried as part of this request that validated successfully, if any
     * @param carriedInvalidCredentialTypes the credential types carried as part of this request that did not validate successfully, if any
     * @param invalidCredentialExceptions the exceptions indicating the problems with the invalid credentials, in the same sequence, if any
     * @param sessionClientIdentifier the normalized identifier of the client according to a currently valid session
     * @param sessionClientPersona the client LidPersona according to the currently valid session
     * @param wishesCancelSession the client wishes to cancel the session
     */
    protected SimpleLidClientAuthenticationStatus(
            String                           clientIdentifier,
            LidPersona                       clientPersona,
            LidSession                       session,
            LidCredentialType []             carriedValidCredentialTypes,
            LidCredentialType []             carriedInvalidCredentialTypes,
            LidInvalidCredentialException [] invalidCredentialExceptions,
            String                           sessionClientIdentifier,
            LidPersona                       sessionClientPersona,
            boolean                          wishesCancelSession )
    {
        super(  clientIdentifier,
                clientPersona,
                session,
                carriedValidCredentialTypes,
                carriedInvalidCredentialTypes,
                invalidCredentialExceptions,
                sessionClientIdentifier,
                sessionClientPersona,
                wishesCancelSession );
    }
}
