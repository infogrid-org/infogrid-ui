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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.lid.local;

import java.util.Map;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.util.Identifier;
import org.infogrid.util.InvalidIdentifierException;

/**
 * Implements the LidLocalPersonaManager interface by delegating to another LidLocalPersonaManager
 * after translating identifiers. The translation methods must be defined in
 * subclasses.
 */
public abstract class TranslatingLidLocalPersonaManager
        extends
            AbstractLidLocalPersonaManager
{
    /**
     * Constructor for subclasses only.
     * 
     * @param delegate the delegate LidIdentityManager
     */
    protected TranslatingLidLocalPersonaManager(
            LidLocalPersonaManager delegate )
    {
        theDelegate = delegate;
    }

    /**
     * Create a LidLocalPersona.
     *
     * @param identifier the identifier for the to-be-created LidLocalPersona
     * @param attributes the attributes for the to-be-created LidLocalPersona
     * @param credentials the credentials for the to-be-created LidLocalPersona
     * @return the LocalPersona that was created
     * @throws LidLocalPersonaExistsAlreadyException thrown if a LidLocalPersona with this identifier exists already
     * @throws UnsupportedOperationException thrown if this LidIdentityManager does not permit the creation of new LidLocalPersonas
     */
    public LidLocalPersona createLocalPersona(
            Identifier                    identifier,
            Map<String,String>            attributes,
            Map<LidCredentialType,String> credentials )
        throws
            LidLocalPersonaExistsAlreadyException,
            UnsupportedOperationException
    {
        if( identifier == null ) {
            throw new NullPointerException( "identifier must not be null" );
        }
        Identifier delegateIdentifier = translateIdentifierForward( identifier );
        
        try {
            LidLocalPersona delegatePersona = theDelegate.createLocalPersona(
                    delegateIdentifier,
                    attributes,
                    credentials );
            
            LidLocalPersona ret = translatePersonaBackward( delegatePersona );
            return ret;

        } catch( LidLocalPersonaExistsAlreadyException ex ) {

            LidLocalPersona ret = translatePersonaBackward( ex.getPersona() );
            throw new LidLocalPersonaExistsAlreadyException( ret, ex );
        }        
    }

    /**
     * Obtain a LidLocalPersona, given its identifier.
     *
     * @param identifier the identifier for which the LidLocalPersona will be retrieved
     * @return the found LidLocalPersona
     * @throws LidLocalPersonaUnknownException thrown if no LidLocalPersona exists with this identifier
     * @throws InvalidIdentifierException thrown if an invalid Identifier was provided
     */
    public LidLocalPersona find(
            Identifier identifier )
        throws
            LidLocalPersonaUnknownException,
            InvalidIdentifierException
    {
        if( identifier == null ) {
            throw new NullPointerException( "identifier must not be null" );
        }
        Identifier delegateIdentifier = translateIdentifierForward( identifier );

        try {
            LidLocalPersona delegatePersona = theDelegate.find( delegateIdentifier );
            LidLocalPersona ret             = translatePersonaBackward( delegatePersona );
            return ret;

        } catch( LidLocalPersonaUnknownException ex ) {
            throw new LidLocalPersonaUnknownException( identifier, ex );
        }        
    }

    /**
     * Delete a LidLocalPersona, given its identifier.
     * 
     * @param identifier the identifier of the LidLocalPersona that will be deleted
     * @throws UnsupportedOperationException thrown if this LidIdentityManager does not permit the deletion of LidLocalPersonas
     * @throws LidLocalPersonaUnknownException thrown if no LidLocalPersona exists with this identifier
     */
    public void delete(
            Identifier identifier )
        throws
            UnsupportedOperationException,
            LidLocalPersonaUnknownException
    {
        if( identifier == null ) {
            throw new NullPointerException( "identifier must not be null" );
        }
        Identifier delegateIdentifier = translateIdentifierForward( identifier );

        try {
            theDelegate.delete( delegateIdentifier );

        // we don't catch UnsupportedOperationException
        } catch( LidLocalPersonaUnknownException ex ) {
            throw new LidLocalPersonaUnknownException( identifier, ex );
        }
    }
    
    /**
     * Translate the identifier as used by this class into the identifier as used by the delegate.
     * 
     * @param identifier input parameter
     * @return translated identifier
     */
    protected abstract Identifier translateIdentifierForward(
            Identifier identifier );

    /**
     * Translate the identifier as used by the delegate into the identifier as used by this class.
     * 
     * @param identifier input parameter
     * @return translated identifier
     */
    protected abstract Identifier translateIdentifierBackward(
            Identifier identifier );
    
    /**
     * Translate a LidLocalPersona as used by this class into the LidLocalPersona as used by the delegate.
     * 
     * @param persona input parameter
     * @return translated LidLocalPersona
     * @throws LidLocalPersonaUnknownException thrown if the Persona is unknown
     * @throws InvalidIdentifierException thrown if the identifier is invalid
     */
    protected LidLocalPersona translatePersonaForward(
            LidLocalPersona persona )
        throws
            LidLocalPersonaUnknownException,
            InvalidIdentifierException
    {
        if( persona == null ) {
            return null;
        }
        Identifier delegateIdentifier = translateIdentifierForward( persona.getIdentifier() );
        
        LidLocalPersona ret = theDelegate.find( delegateIdentifier );

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
        if( persona == null ) {
            return null;
        }
        Identifier delegateIdentifier = translateIdentifierBackward( persona.getIdentifier() );
        
        LidLocalPersona ret = new TranslatingLidLocalPersona( delegateIdentifier, persona );

        return ret;
    }    

    /**
     * The delegate LidLocalPersonaManager.
     */
    protected LidLocalPersonaManager theDelegate;
}
