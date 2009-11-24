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

package org.infogrid.lid.local.regex;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.lid.local.AbstractLidLocalPersonaManager;
import org.infogrid.lid.local.LidLocalPersona;
import org.infogrid.lid.local.LidLocalPersonaExistsAlreadyException;
import org.infogrid.lid.local.LidLocalPersonaUnknownException;
import org.infogrid.lid.local.SimpleLidLocalPersona;
import org.infogrid.util.Identifier;

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
     * @return the created RegexLidLocalPersonaManager
     */
    public static RegexLidLocalPersonaManager create(
            String userNameRegex )
    {
        RegexLidLocalPersonaManager ret = new RegexLidLocalPersonaManager(
                Pattern.compile( userNameRegex ));
        
        return ret;
    }

    /**
     * Factory method.
     * 
     * @param userNameRegex the user name regular expression
     * @return the created RegexLidLocalPersonaManager
     */
    public static RegexLidLocalPersonaManager create(
            Pattern userNameRegex )
    {
        RegexLidLocalPersonaManager ret = new RegexLidLocalPersonaManager(
                userNameRegex );
        
        return ret;
    }

    /**
     * Private constructor, use factory method.
     * 
     * @param userNameRegex the user name regular expression
     */
    protected RegexLidLocalPersonaManager(
            Pattern userNameRegex )
    {
        theUserNameRegex = userNameRegex;
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
        throw new UnsupportedOperationException();
    }

    /**
     * Obtain a LidLocalPersona, given its identifier.
     *
     * @param identifier the identifier for which the LidLocalPersona will be retrieved
     * @return the found LidLocalPersona
     * @throws LidLocalPersonaUnknownException thrown if no LidLocalPersona exists with this identifier
     */
    public LidLocalPersona find(
            Identifier identifier )
        throws
            LidLocalPersonaUnknownException
    {
        if( isUser( identifier )) {
            HashMap<String,String> attributes  = new HashMap<String,String>();
            attributes.put( LidLocalPersona.IDENTIFIER_ATTRIBUTE_NAME, identifier.toExternalForm() );
            attributes.put( "FirstName",  "John" );
            attributes.put( "LastName",   "Doe" );
            attributes.put( "Profession", "Mythical Man" );
            
            LidLocalPersona ret = SimpleLidLocalPersona.create( identifier, attributes );
        
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
            Identifier identifier )
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
            Identifier userName )
    {
        if( userName == null ) {
            return false;
        }
        if( theUserNameRegex == null ) {
            return false; // no parameter, always say no
        }
        Matcher userNameMatcher = theUserNameRegex.matcher( userName.toExternalForm() );
        if( !userNameMatcher.matches() ) {
            return false;
        }
        return true;
    }
    
    /**
     * The user name regular expression.
     */
    protected Pattern theUserNameRegex;
}
