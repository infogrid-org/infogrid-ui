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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.util.FactoryException;

/**
 * A LidIdentityManager that compares user name and password against regular expressions.
 * This is not likely to be useful in a production scenario, but can help during development
 * or testing.
 */
public class RegexLidIdentityManager
        implements
            LidIdentityManager
{
    /**
     * Factory method.
     * 
     * @param userNameRegex the user name regular expression
     * @param passwordRegex the password regular expression
     * @return the created RegexLidIdentityManager
     */
    public static RegexLidIdentityManager create(
            String userNameRegex,
            String passwordRegex )
    {
        RegexLidIdentityManager ret = new RegexLidIdentityManager(
                Pattern.compile( userNameRegex ),
                Pattern.compile( passwordRegex ));
        
        return ret;
    }

    /**
     * Factory method.
     * 
     * @param userNameRegex the user name regular expression
     * @param passwordRegex the password regular expression
     * @return the created RegexLidIdentityManager
     */
    public static RegexLidIdentityManager create(
            Pattern userNameRegex,
            Pattern passwordRegex )
    {
        RegexLidIdentityManager ret = new RegexLidIdentityManager(
                userNameRegex,
                passwordRegex );
        
        return ret;
    }

    /**
     * Private constructor, use factory method.
     * 
     * @param userNameRegex the user name regular expression
     * @param passwordRegex the password regular expression
     */
    protected RegexLidIdentityManager(
            Pattern userNameRegex,
            Pattern passwordRegex )
    {
        theUserNameRegex = userNameRegex;
        thePasswordRegex = passwordRegex;
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
        LidLocalPersona persona = null;
        try {
            persona = get( identifier, identifier, credential );
        } catch( LidNotPermittedException ex ) {
            // ignore
        }

        if( persona == null ) {
            throw new LidLocalPersonaUnknownException( identifier );
        }

        if( credential == null ) {
            // assume empty credential
            credential = "";
        }
        if( thePasswordRegex == null ) {
            throw new LidInvalidCredentialException( identifier ); // no parameter, always say no
        }

        Matcher passwordMatcher = thePasswordRegex.matcher( credential );
        if( !passwordMatcher.matches() ) {
            throw new LidInvalidCredentialException( identifier );
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
        if( isUser( identifier )) {
            HashMap<String,String> attributes = new HashMap<String,String>();
        
            LidLocalPersona ret = LidLocalPersona.create( identifier, attributes );
        
            return ret;

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
     * Determine whether a record with the given username exists.
     * 
     * @param userName the user name
     * @return true if a record exists
     */
    public boolean isUser(
            String userName )
    {
        if( userName == null ) {
            return false;
        }
        if( theUserNameRegex == null ) {
            return false; // no parameter, always say no
        }
        Matcher userNameMatcher = theUserNameRegex.matcher( userName );        
        if( !userNameMatcher.matches() ) {
            return false;
        }
        return true;
    }
    
    /**
     * The user name regular expression.
     */
    protected Pattern theUserNameRegex;
    
    /**
     * The password regular expression.
     */
    protected Pattern thePasswordRegex;
}
