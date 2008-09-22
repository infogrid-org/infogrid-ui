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

import java.util.HashMap;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import org.infogrid.lid.AbstractReadOnlyLidLocalPersonaManager;
import org.infogrid.lid.LidLocalPersona;
import org.infogrid.lid.LidLocalPersonaUnknownException;
import org.infogrid.lid.LidLocalPersonaVO;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.util.logging.Log;

/**
 * A LidLocalPersonaManager implemented using LDAP.
 * 
 * <p>Invoke similar to this:</p>
 * <pre>
 * Properties prop = new Properties();
 * prop.put( Context.PROVIDER_URL,            ldapUrl );
 * prop.put( "java.naming.ldap.version",      "3" );
 * prop.put( Context.REFERRAL,                "ignore" )
 * prop.put( Context.SECURITY_AUTHENTICATION, "simple" );
 * prop.put( Context.SECURITY_PRINCIPAL,      managerDn );
 * prop.put( Context.SECURITY_CREDENTIALS,    managerPassword );
 * prop.put( Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory" );
 * DirContext dir = new InitialDirContext( prop );
 * </pre>
 * <p>When running against ActiveDirectory, an additional property may be needed:</p>
 * <pre>
 * </pre>
 * 
 */
public class LdapLidLocalPersonaManager
        extends
            AbstractReadOnlyLidLocalPersonaManager
{
    private static final Log log = Log.getLogInstance( LdapLidLocalPersonaManager.class  );

    /**
     * Factory method.
     *
     * @param dir the DirContext in which to find the identities
     * @param filter the LDAP filter expression
     * @param controls the SearchControls to use for queries
     * @return the created LdapLidLocalPersonaManager
     */
    public static LdapLidLocalPersonaManager create(
            DirContext     dir,
            String         filter,
            SearchControls controls )
    {
        LdapLidLocalPersonaManager ret = new LdapLidLocalPersonaManager( dir, filter, controls );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param dir the DirContext in which to find the identities
     * @param filter the LDAP filter expression
     * @param controls the SearchControls to use for queries
     */
    protected LdapLidLocalPersonaManager(
            DirContext     dir,
            String         filter,
            SearchControls controls )
    {
        theDir      = dir;
        theFilter   = filter;
        theControls = controls;
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
//        
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
        NamingEnumeration found = null;
        try {
            found = theDir.search( identifier, theFilter, theControls );

            if( found.hasMore() ) {
                SearchResult current = (SearchResult) found.next();

                HashMap<String,String> attributes = new HashMap<String,String>();
                
                NamingEnumeration<? extends Attribute> currentAttributes = current.getAttributes().getAll();
                while( currentAttributes.hasMore() ) {
                    Attribute att = currentAttributes.next();

                    Object value = att.get();
                    if( value != null ) {
                        attributes.put( att.getID(), value.toString() );
                    }
                }
                
                HashMap<LidCredentialType,String> credentials = new HashMap<LidCredentialType,String>();
                
                LidLocalPersona ret = LidLocalPersonaVO.create( identifier, attributes, credentials );
                
                return ret;
            }
            throw new LidLocalPersonaUnknownException( identifier );

        } catch( NamingException ex ) {
            log.error( ex );
            throw new LidLocalPersonaUnknownException( identifier );
            
        } finally {
            if( found != null ) {
                try {
                    found.close();
                } catch( Throwable ex2 ) {
                    // nothing
                }
            }
        }
    }

    /**
     * The DirContext in which to find the identities.
     */
    protected DirContext theDir;

    /**
     * The LDAP filter expression to use.
     */
    protected String theFilter;
    
    /**
     * The search controls.
     */
    protected SearchControls theControls;
}
