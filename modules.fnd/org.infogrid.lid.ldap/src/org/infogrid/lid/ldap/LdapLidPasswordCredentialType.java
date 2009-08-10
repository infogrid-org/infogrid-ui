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

package org.infogrid.lid.ldap;

import java.util.Properties;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.infogrid.lid.credential.AbstractLidPasswordCredentialType;
import org.infogrid.lid.credential.LidInvalidCredentialException;
import org.infogrid.util.HasIdentifier;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;

/**
 * A password LidCredentialType that is validated against LDAP.
 */
public class LdapLidPasswordCredentialType
    extends
        AbstractLidPasswordCredentialType
{
    private static final Log log = Log.getLogInstance( LdapLidPasswordCredentialType.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param passwordDirProps properties for directory access
     * @param identifierSuffix to append to the identifier when attempting to check a password, if any.
     * @return the created LdapLidPasswordCredentialType
     */
    public static LdapLidPasswordCredentialType create(
            Properties passwordDirProps,
            String     identifierSuffix )
    {
        LdapLidPasswordCredentialType ret = new LdapLidPasswordCredentialType( passwordDirProps, identifierSuffix );
        return ret;
    }

    /**
     * Constructor, for subclasses only, use factory method.
     *
     * @param passwordDirProps properties for directory access
     * @param identifierSuffix to append to the identifier when attempting to check a password, if any.
     */
    protected LdapLidPasswordCredentialType(
            Properties passwordDirProps,
            String     identifierSuffix )
    {
        thePasswordDirProps = passwordDirProps;
        theIdentifierSuffix = identifierSuffix;
    }

    /**
     * Determine whether the request contains a valid LidCredentialType of this type
     * for the given subject.
     *
     * @param request the request
     * @param subject the subject
     * @throws LidInvalidCredentialException thrown if the contained LidCdedentialType is not valid for this subject
     */
    public void checkCredential(
            SaneRequest   request,
            HasIdentifier subject )
        throws
            LidInvalidCredentialException
    {
        String givenPassword = request.getArgument( "lid-credential" );

        Properties props = (Properties) thePasswordDirProps.clone();

        String identifier;
        if( theIdentifierSuffix != null ) {
            identifier = subject.getIdentifier().toExternalForm() + theIdentifierSuffix;
        } else {
            identifier = subject.getIdentifier().toExternalForm();
        }

        props.put( javax.naming.Context.SECURITY_PRINCIPAL,   identifier );
        props.put( javax.naming.Context.SECURITY_CREDENTIALS, givenPassword );

        DirContext passwordDir; // for debugging
        try {
            passwordDir = new InitialDirContext( props );
            return;

        } catch( NamingException ex ) {
            if( log.isDebugEnabled() ) {
                log.debug( ex );
            }
            throw new LidInvalidCredentialException( subject.getIdentifier(), this, ex );
        }
    }

    /**
     * The Properties to use when attempting to check a password.
     */
    protected Properties thePasswordDirProps;

    /**
     * Suffix to append to the identifier when attempting to check a password, if any.
     */
    protected String theIdentifierSuffix;
}
