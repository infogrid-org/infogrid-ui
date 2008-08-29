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

import java.util.Map;
import org.infogrid.lid.credential.LidCredentialType;

/**
 * Factors out common functionality of LidIdentityManagers that only support read and not write operations..
 */
public abstract class AbstractReadOnlyLidIdentityManager
        implements
            LidIdentityManager
{
    /**
     * Create a LidLocalPersona.
     *
     * @param identifier the identifier for the to-be-created LidLocalPersona
     * @param attributes the attributes for the to-be-created LidLocalPersona
     * @param credentials the credentials for the to-be-created LidLocalPersona
     * @param callerIdentifier the identifier of the caller
     * @param callerCredential the credential of the caller
     * @return nothing, as UnsupportedOperationException is always thrown 
     * @throws UnsupportedOperationException always thrown
     */
    public LidLocalPersona createLocalPersona(
            String                        identifier,
            Map<String,String>            attributes,
            Map<LidCredentialType,String> credentials,
            String                        callerIdentifier,
            String                        callerCredential )
        throws
            UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Determine whether a a credential is valid for a given identifier.
     *
     * @param identifier the identifier for which the credential will be checked
     * @param type the type of credential to be checked
     * @param credential the credential to be checked
     * @throws LidLocalPersonaUnknownException thrown if no LidLocalPersona exists with this identifier
     * @throws LidInvalidCredentialException thrown if the credential was invalid
     */
    public abstract void checkCredential(
            String            identifier,
            LidCredentialType type,
            String            credential )
        throws
            LidLocalPersonaUnknownException,
            LidInvalidCredentialException;

    /**
     * Change the credential associated with a given identifier.
     *
     * @param identifier the identifier for which the credential will be changed
     * @param type the type of credential to be changed
     * @param credential the new credential
     * @param callerIdentifier the identifier of the caller
     * @param callerCredential the credential of the caller
     * @throws UnsupportedOperationException always thrown
     */
    public void changeCredential(
            String            identifier,
            LidCredentialType type,
            String            credential,
            String            callerIdentifier,
            String            callerCredential )
        throws
            UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * Obtain a LidLocalPersona, given its identifier. Pass in identifier and credentials of the caller.
     *
     * @param identifier the identifier for which the LidLocalPersona will be retrieved
     * @param callerIdentifier the identifier of the caller
     * @param callerCredential the credential of the caller
     * @return the found LidLocalPersona
     * @throws LidLocalPersonaUnknownException thrown if no LidLocalPersona exists with this identifier
     */
    public abstract LidLocalPersona get(
            String identifier,
            String callerIdentifier,
            String callerCredential )
        throws
            LidLocalPersonaUnknownException;

    /**
     * Delete a LidLocalPersona, given its identifier. Pass in identifier and credentials of the caller.
     * 
     * @param identifier the identifier of the LidLocalPersona that will be deleted
     * @param callerIdentifier the identifier of the caller
     * @param callerCredential the credential of the caller
     * @throws UnsupportedOperationException always thrown
     */
    public void delete(
            String identifier,
            String callerIdentifier,
            String callerCredential )
        throws
            UnsupportedOperationException
    {
        throw new UnsupportedOperationException();
    }
}

