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
 * Implements the LidIdentityManager interface by delegating to another LidIdentity
 * Manager after translating identifiers. The translation methods must be defined in
 * subclasses.
 */
public abstract class TranslatingLidIdentityManager
        implements
            LidIdentityManager
{
    /**
     * Constructor for subclasses only.
     * 
     * @param delegate the delegate LidIdentityManager
     */
    protected TranslatingLidIdentityManager(
            LidIdentityManager delegate )
    {
        theDelegate = delegate;
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
        String delegateIdentifier       = translateIdentifierForward( identifier );
        String delegateCallerIdentifier = translateIdentifierForward( callerIdentifier );
        
        try {
            LidLocalPersona delegatePersona = theDelegate.createLocalPersona(
                    delegateIdentifier,
                    attributes,
                    credentials,
                    delegateCallerIdentifier,
                    callerCredential );
            
            LidLocalPersona ret = translatePersonaBackward( delegatePersona );
            return ret;

        } catch( LidLocalPersonaExistsAlreadyException ex ) {

            LidLocalPersona ret = translatePersonaBackward( ex.getPersona() );
            throw new LidLocalPersonaExistsAlreadyException( ret, ex );

        } catch( LidNotPermittedException ex ) {
            throw new LidNotPermittedException( ex );

        } catch( LidInvalidCredentialException ex ) {
            throw new LidInvalidCredentialException( callerIdentifier, ex );
            
        } catch( FactoryException ex ) {
            throw new FactoryException( ex );
        }        
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
        String delegateIdentifier = translateIdentifierForward( identifier );

        try {
            theDelegate.checkCredential( delegateIdentifier, type, credential );

        } catch( LidLocalPersonaUnknownException ex ) {
            throw new LidLocalPersonaUnknownException( identifier, ex );

        } catch( LidInvalidCredentialException ex ) {
            throw new LidInvalidCredentialException( identifier, ex );
        }        
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
        String delegateIdentifier       = translateIdentifierForward( identifier );
        String delegateCallerIdentifier = translateIdentifierForward( callerIdentifier );

        try {
            theDelegate.changeCredential(
                    delegateIdentifier,
                    type,
                    credential,
                    delegateCallerIdentifier,
                    callerCredential );
            
        } catch( LidLocalPersonaUnknownException ex ) {
            throw new LidLocalPersonaUnknownException( identifier, ex );

        } catch( LidNotPermittedException ex ) {
            throw new LidNotPermittedException( ex );

        } catch( LidInvalidCredentialException ex ) {
            throw new LidInvalidCredentialException( callerIdentifier, ex );
        }        
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
        String delegateIdentifier       = translateIdentifierForward( identifier );
        String delegateCallerIdentifier = translateIdentifierForward( callerIdentifier );

        try {
            LidLocalPersona delegatePersona = theDelegate.get( delegateIdentifier, delegateCallerIdentifier, callerCredential );
            LidLocalPersona ret             = translatePersonaBackward( delegatePersona );
            return ret;

        } catch( LidLocalPersonaUnknownException ex ) {
            throw new LidLocalPersonaUnknownException( identifier, ex );

        } catch( LidNotPermittedException ex ) {
            throw new LidNotPermittedException( ex );

        } catch( LidInvalidCredentialException ex ) {
            throw new LidInvalidCredentialException( callerIdentifier, ex );
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
        String delegateIdentifier       = translateIdentifierForward( identifier );
        String delegateCallerIdentifier = translateIdentifierForward( callerIdentifier );

        try {
            theDelegate.delete( delegateIdentifier, delegateCallerIdentifier, callerCredential );

        // we don't catch UnsupportedOperationException
        } catch( LidLocalPersonaUnknownException ex ) {
            throw new LidLocalPersonaUnknownException( identifier, ex );

        } catch( LidNotPermittedException ex ) {
            throw new LidNotPermittedException( ex );

        } catch( LidInvalidCredentialException ex ) {
            throw new LidInvalidCredentialException( callerIdentifier, ex );
        }
    }
    
    /**
     * Translate the identifier as used by this class into the identifier as used by the delegate.
     * 
     * @param identifier input parameter
     * @return translated identifier
     */
    protected abstract String translateIdentifierForward(
            String identifier );

    /**
     * Translate the identifier as used by the delegate into the identifier as used by this class.
     * 
     * @param identifier input parameter
     * @return translated identifier
     */
    protected abstract String translateIdentifierBackward(
            String identifier );
    
    /**
     * Translate a LidLocalPersona as used by this class into the LidLocalPersona as used by the delegate.
     * 
     * @param persona input parameter
     * @return translated LidLocalPersona
     */
    protected LidLocalPersona translatePersonaForward(
            LidLocalPersona persona )
    {
        String delegateIdentifier = translateIdentifierForward( persona.getIdentifier() );
        
        LidLocalPersona ret = LidLocalPersona.create( delegateIdentifier, persona.getAttributes() );

        return ret;
    }

    /**
     * Translate a LidLocalPersona as used by the delegate into the LidLocalPersona as used by this class.
     * 
     * @param persona input parameter
     * @return translated LidLocalPersona
     */
    protected LidLocalPersona translatePersonaBackward(
            LidLocalPersona persona )
    {
        String delegateIdentifier = translateIdentifierBackward( persona.getIdentifier() );
        
        LidLocalPersona ret = LidLocalPersona.create( delegateIdentifier, persona.getAttributes() );

        return ret;
    }

    /**
     * The delegate LidIdentityManager.
     */
    protected LidIdentityManager theDelegate;
}
