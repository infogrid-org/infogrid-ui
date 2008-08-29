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
import org.infogrid.util.FactoryException;

/**
 * A LidIdentityManager that contains a fixed set of hard-coded users. This is
 * useful for cases such as "administrator accounts".
 * This implementation ignores all callerIdentifiers and callerCredentials.
 */
public class HardcodedLidIdentityManager
        implements
            LidIdentityManager
{
    /**
     * Factory method.
     * 
     * @param localPersonas the known LidLocalPersonas with their credentials
     * @return the created HardcodedLidIdentityManager
     */
    public static HardcodedLidIdentityManager create(
            Map<String,LidLocalPersonaWithCredentials> localPersonas )
    {
        HardcodedLidIdentityManager ret = new HardcodedLidIdentityManager( localPersonas );
        
        return ret;
    }

    /**
     * Private constructor, use factory method.
     * 
     * @param localPersonas the known LidLocalPersonas with their credentials
     */
    protected HardcodedLidIdentityManager(
            Map<String,LidLocalPersonaWithCredentials> localPersonas )
    {
        theLocalPersonas = localPersonas;
    }

    /**
     * Create a LidLocalPersona.
     *
     * @param identifier the identifier for the to-be-created LidLocalPersona
     * @param attributes the attributes for the to-be-created LidLocalPersona
     * @param credentials the credentials for the to-be-created LidLocalPersona
     * @param callerIdentifier the identifier of the caller
     * @param callerCredential the credential of the caller
     * @return the LocalPersona that was created
     * @throws LidLocalPersonaExistsAlreadyException thrown if a LidLocalPersona with this identifier exists already
     * @throws UnsupportedOperationException thrown if this LidIdentityManager does not permit the creation of new LidLocalPersonas
     * @throws LidInvalidCredentialException thrown if the caller credential was invalid
     * @throws LidNotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
     * @throws FactoryException if the creation of a LidLocalPersona failed for some other reason
     */
    public LidLocalPersona createLocalPersona(
            String                        identifier,
            Map<String,String>            attributes,
            Map<LidCredentialType,String> credentials,
            String                        callerIdentifier,
            String                        callerCredential )
        throws
            LidLocalPersonaExistsAlreadyException,
            UnsupportedOperationException,
            LidInvalidCredentialException,
            LidNotPermittedException,
            FactoryException
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
    public void checkCredential(
            String            identifier,
            LidCredentialType type,
            String            credential )
        throws
            LidLocalPersonaUnknownException,
            LidInvalidCredentialException
    {
        LidLocalPersonaWithCredentials with = theLocalPersonas.get( identifier );
        if( with == null ) {
            throw new LidLocalPersonaUnknownException( identifier );
        }

        String storedCredential = with.getCredentialFor( type );
        type.checkCredential( identifier, credential, storedCredential );
    }

    /**
     * Change the credential associated with a given identifier.
     *
     * @param identifier the identifier for which the credential will be changed
     * @param type the type of credential to be changed
     * @param credential the new credential
     * @param callerIdentifier the identifier of the caller
     * @param callerCredential the credential of the caller
     * @throws UnsupportedOperationException thrown if this LidIdentityManager does not permit the changing of passwords
     * @throws LidNotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
     * @throws LidInvalidCredentialException thrown if the caller credential was invalid
     * @throws LidLocalPersonaUnknownException thrown if no LidLocalPersona exists with this identifier
     */
    public void changeCredential(
            String            identifier,
            LidCredentialType type,
            String            credential,
            String            callerIdentifier,
            String            callerCredential )
        throws
            UnsupportedOperationException,
            LidNotPermittedException,
            LidInvalidCredentialException,
            LidLocalPersonaUnknownException
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
     * @throws LidNotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
     * @throws LidInvalidCredentialException thrown if the caller credential was invalid
     */
    public LidLocalPersona get(
            String identifier,
            String callerIdentifier,
            String callerCredential )
        throws
            LidLocalPersonaUnknownException,
            LidNotPermittedException,
            LidInvalidCredentialException
    {
        LidLocalPersonaWithCredentials with = theLocalPersonas.get( identifier );
        if( with != null ) {
            return with.getPersona();
        } else {
            return null;
        }
    }

    /**
     * Delete a LidLocalPersona, given its identifier. Pass in identifier and credentials of the caller.
     * 
     * @param identifier the identifier of the LidLocalPersona that will be deleted
     * @param callerIdentifier the identifier of the caller
     * @param callerCredential the credential of the caller
     * @throws UnsupportedOperationException thrown if this LidIdentityManager does not permit the deletion of LidLocalPersonas
     * @throws LidLocalPersonaUnknownException thrown if no LidLocalPersona exists with this identifier
     * @throws LidNotPermittedException thrown if the caller did not have sufficient permissions to perform this operation
     * @throws LidInvalidCredentialException thrown if the caller credential was invalid
     */
    public void delete(
            String identifier,
            String callerIdentifier,
            String callerCredential )
        throws
            UnsupportedOperationException,
            LidLocalPersonaUnknownException,
            LidNotPermittedException,
            LidInvalidCredentialException
    {
        throw new UnsupportedOperationException();
    }

    /**
     * The set of known users, keyed by their identifiers.
     */
    protected Map<String,LidLocalPersonaWithCredentials> theLocalPersonas;
}