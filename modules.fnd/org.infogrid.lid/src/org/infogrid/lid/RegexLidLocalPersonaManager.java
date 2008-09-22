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
import org.infogrid.lid.credential.LidPasswordCredentialType;
import org.infogrid.util.FactoryException;

/**
 * A LidLocalPersonaManager that compares user name and password against regular expressions.
 * This is not likely to be useful in a production scenario, but can help during development
 * or testing.
 */
public class RegexLidLocalPersonaManager
        extends
            AbstractLidLocalPersonaManager
{
    /**
     * Factory method.
     * 
     * @param userNameRegex the user name regular expression
     * @param passwordRegex the password regular expression
     * @return the created RegexLidLocalPersonaManager
     */
    public static RegexLidLocalPersonaManager create(
            String userNameRegex,
            String passwordRegex )
    {
        RegexLidLocalPersonaManager ret = new RegexLidLocalPersonaManager(
                Pattern.compile( userNameRegex ),
                Pattern.compile( passwordRegex ));
        
        return ret;
    }

    /**
     * Factory method.
     * 
     * @param userNameRegex the user name regular expression
     * @param passwordRegex the password regular expression
     * @return the created RegexLidLocalPersonaManager
     */
    public static RegexLidLocalPersonaManager create(
            Pattern userNameRegex,
            Pattern passwordRegex )
    {
        RegexLidLocalPersonaManager ret = new RegexLidLocalPersonaManager(
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
    protected RegexLidLocalPersonaManager(
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
     * @return the LocalPersona that was created
     * @throws LidLocalPersonaExistsAlreadyException thrown if a LidLocalPersona with this identifier exists already
     * @throws UnsupportedOperationException thrown if this LidIdentityManager does not permit the creation of new LidLocalPersonas
     * @throws FactoryException if the creation of a LidLocalPersona failed for some other reason
     */
    public LidLocalPersona createLocalPersona(
            String                        identifier,
            Map<String,String>            attributes,
            Map<LidCredentialType,String> credentials )
        throws
            LidLocalPersonaExistsAlreadyException,
            UnsupportedOperationException,
            FactoryException
    {
        throw new UnsupportedOperationException();
    }

//    /**
//     * Determine whether a a credential is valid for a given identifier.
//     *
//     * @param identifier the identifier for which the credential will be checked
//     * @param type the type of credential to be checked
//     * @param credential the credential to be checked
//     * @throws LidLocalPersonaUnknownException thrown if no LidLocalPersona exists with this identifier
//     * @throws LidInvalidCredentialException thrown if the credential was invalid
//     */
//    public void checkCredential(
//            String            identifier,
//            LidCredentialType type,
//            String            credential )
//        throws
//            LidLocalPersonaUnknownException,
//            LidInvalidCredentialException
//    {
//        LidLocalPersona persona = get( identifier );
//
//        if( persona == null ) {
//            throw new LidLocalPersonaUnknownException( identifier );
//        }
//
//        if( credential == null ) {
//            // assume empty credential
//            credential = "";
//        }
//        if( thePasswordRegex == null ) {
//            throw new LidInvalidCredentialException( identifier, type ); // no parameter, always say no
//        }
//
//        Matcher passwordMatcher = thePasswordRegex.matcher( credential );
//        if( !passwordMatcher.matches() ) {
//            throw new LidInvalidCredentialException( identifier, type );
//        }
//    }
//
//    /**
//     * Change the credential associated with a given identifier.
//     *
//     * @param identifier the identifier for which the credential will be changed
//     * @param type the type of credential to be changed
//     * @param credential the new credential
//     * @throws UnsupportedOperationException thrown if this LidIdentityManager does not permit the changing of passwords
//     * @throws LidLocalPersonaUnknownException thrown if no LidLocalPersona exists with this identifier
//     */
//    public void changeCredential(
//            String            identifier,
//            LidCredentialType type,
//            String            credential )
//        throws
//            UnsupportedOperationException,
//            LidLocalPersonaUnknownException
//    {
//        throw new UnsupportedOperationException();
//    }

    /**
     * Obtain a LidLocalPersona, given its identifier.
     *
     * @param identifier the identifier for which the LidLocalPersona will be retrieved
     * @return the found LidLocalPersona
     * @throws LidLocalPersonaUnknownException thrown if no LidLocalPersona exists with this identifier
     */
    public LidLocalPersona get(
            String identifier )
        throws
            LidLocalPersonaUnknownException
    {
        if( isUser( identifier )) {
            HashMap<String,String>            attributes  = new HashMap<String,String>();
            HashMap<LidCredentialType,String> credentials = new HashMap<LidCredentialType,String>();
        
            attributes.put( LidLocalPersona.IDENTIFIER_ATTRIBUTE_NAME, identifier );
            credentials.put( LidPasswordCredentialType.create(), identifier + "pass" );
            
            LidLocalPersona ret = LidLocalPersonaVO.create( identifier, attributes, credentials );
        
            return ret;

        } else {
            throw new LidLocalPersonaUnknownException( identifier );
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
            String identifier )
        throws
            UnsupportedOperationException,
            LidLocalPersonaUnknownException
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
