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

import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.lid.credential.LidInvalidCredentialException;

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
     * @param clientPersona the client LidPersona that was found, if any
     * @param session the client LidSession that was found, if any
     * @param carriedValidCredentialTypes the credential types carried as part of this request that validated successfully, if any
     * @param carriedInvalidCredentialTypes the credential types carried as part of this request that did not validate successfully, if any
     * @param invalidCredentialExceptions the exceptions indicating the problems with the invalid credentials, in the same sequence, if any
     * @param wishesCancelSession the client wishes to cancel the session
     * @param wishesAnonymous the client wishes to become anonymous
     * @return the created SimpleLidClientAuthenticationStatus
     */
    public static SimpleLidClientAuthenticationStatus create(
            LidPersona                       clientPersona,
            LidSession                       session,
            LidCredentialType []             carriedValidCredentialTypes,
            LidCredentialType []             carriedInvalidCredentialTypes,
            LidInvalidCredentialException [] invalidCredentialExceptions,
            boolean                          wishesCancelSession,
            boolean                          wishesAnonymous )
    {
        SimpleLidClientAuthenticationStatus ret = new SimpleLidClientAuthenticationStatus(
                clientPersona,
                session,
                carriedValidCredentialTypes,
                carriedInvalidCredentialTypes,
                invalidCredentialExceptions,
                wishesCancelSession,
                wishesAnonymous );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param clientPersona the client LidPersona that was found, if any
     * @param session the client LidSession that was found, if any
     * @param carriedValidCredentialTypes the credential types carried as part of this request that validated successfully, if any
     * @param carriedInvalidCredentialTypes the credential types carried as part of this request that did not validate successfully, if any
     * @param invalidCredentialExceptions the exceptions indicating the problems with the invalid credentials, in the same sequence, if any
     * @param wishesCancelSession the client wishes to cancel the session
     * @param wishesAnonymous the client wishes to become anonymous
     */
    protected SimpleLidClientAuthenticationStatus(
            LidPersona                       clientPersona,
            LidSession                       session,
            LidCredentialType []             carriedValidCredentialTypes,
            LidCredentialType []             carriedInvalidCredentialTypes,
            LidInvalidCredentialException [] invalidCredentialExceptions,
            boolean                          wishesCancelSession,
            boolean                          wishesAnonymous )
    {
        super(  clientPersona,
                session,
                carriedValidCredentialTypes,
                carriedInvalidCredentialTypes,
                invalidCredentialExceptions,
                wishesCancelSession,
                wishesAnonymous );
    }
}
