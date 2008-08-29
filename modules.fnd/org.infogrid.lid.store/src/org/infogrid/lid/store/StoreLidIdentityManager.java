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
import org.infogrid.lid.AttributesCredentials;
import org.infogrid.lid.LidIdentityManager;
import org.infogrid.lid.LidInvalidCredentialException;
import org.infogrid.lid.LidLocalPersona;
import org.infogrid.lid.LidLocalPersonaExistsAlreadyException;
import org.infogrid.lid.LidLocalPersonaUnknownException;
import org.infogrid.lid.LidLocalPersonaWithCredentials;
import org.infogrid.lid.LidNotPermittedException;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.store.Store;
import org.infogrid.store.util.StoreBackedSwappingHashMap;
import org.infogrid.util.AbstractFactory;
import org.infogrid.util.Factory;
import org.infogrid.util.FactoryException;
import org.infogrid.util.PatientSmartFactory;
import org.infogrid.util.SmartFactory;

/**
 * Store implementation of LidIdentityManager.
 */
public class StoreLidIdentityManager
        implements
            LidIdentityManager
{
    /**
     * Factory method.
     *
     * @param store the Store to store to
     * @param credentialTypeClassLoader the ClassLoader to use to instantiate LidCredentialTypes
     * @return the created StoreLidIdentityManager
     */
    public static StoreLidIdentityManager create(
            Store       store,
            ClassLoader credentialTypeClassLoader )
    {
        LidLocalPersonaWithCredentialsMapper mapper = new LidLocalPersonaWithCredentialsMapper( credentialTypeClassLoader );
        
        Factory<String,LidLocalPersonaWithCredentials,AttributesCredentials> delegateFactory
                = new AbstractFactory<String,LidLocalPersonaWithCredentials,AttributesCredentials>() {
                    public LidLocalPersonaWithCredentials obtainFor(
                            String                identifier,
                            AttributesCredentials attCred )
                    {
                        LidLocalPersonaWithCredentials ret = LidLocalPersonaWithCredentials.create(
                                identifier,
                                attCred.getAttributes(),
                                attCred.getCredentials() );
                        return ret;
                    }
                };

        StoreBackedSwappingHashMap<String,LidLocalPersonaWithCredentials> storage = StoreBackedSwappingHashMap.createWeak( mapper, store );
        
        SmartFactory<String,LidLocalPersonaWithCredentials,AttributesCredentials> smartFactory
                = new PatientSmartFactory<String,LidLocalPersonaWithCredentials,AttributesCredentials>( delegateFactory, storage );
        
        StoreLidIdentityManager ret = new StoreLidIdentityManager( smartFactory );
        return ret;
    }

    /**
     * Constructor, use factory method.
     * 
     * @param delegateFactory the underlying SmartFactory
     */
    protected StoreLidIdentityManager(
            SmartFactory<String,LidLocalPersonaWithCredentials,AttributesCredentials> delegateFactory )
    {
        theDelegateFactory = delegateFactory;
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
        AttributesCredentials attCred = new AttributesCredentials( identifier, attributes, credentials );
        
        LidLocalPersonaWithCredentials with = theDelegateFactory.obtainFor( identifier, attCred );
        return with.getPersona();
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
        LidLocalPersonaWithCredentials with = theDelegateFactory.get( identifier );
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
        LidLocalPersonaWithCredentials with = theDelegateFactory.get( identifier );
        if( with == null ) {
            throw new LidLocalPersonaUnknownException( identifier );
        }
        with.setCredentialFor( type, credential );
        theDelegateFactory.factoryCreatedObjectUpdated( with );
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
    public LidLocalPersona get(
            String identifier,
            String callerIdentifier,
            String callerCredential )
        throws
            LidLocalPersonaUnknownException
    {
        LidLocalPersonaWithCredentials with = theDelegateFactory.get( identifier );
        if( with == null ) {
            throw new LidLocalPersonaUnknownException( identifier );
        }
        return with.getPersona();
    }

    /**
     * Delete a LidLocalPersona, given its identifier. Pass in identifier and credentials of the caller.
     * 
     * @param identifier the identifier of the LidLocalPersona that will be deleted
     * @param callerIdentifier the identifier of the caller
     * @param callerCredential the credential of the caller
     * @throws UnsupportedOperationException thrown if this LidIdentityManager does not permit the deletion of LidLocalPersonas
     * @throws LidLocalPersonaUnknownException thrown if no LidLocalPersona exists with this identifier
     */
    public void delete(
            String identifier,
            String callerIdentifier,
            String callerCredential )
        throws
            UnsupportedOperationException,
            LidLocalPersonaUnknownException
    {
        LidLocalPersonaWithCredentials found = theDelegateFactory.remove( identifier );
        if( found == null ) {
            throw new LidLocalPersonaUnknownException( identifier );
        }
    }

    /**
     * The underlying SmartFactory. This is hidden so we can do access control and
     * expose the API we want.
     */
    protected SmartFactory<String,LidLocalPersonaWithCredentials,AttributesCredentials> theDelegateFactory;
}

