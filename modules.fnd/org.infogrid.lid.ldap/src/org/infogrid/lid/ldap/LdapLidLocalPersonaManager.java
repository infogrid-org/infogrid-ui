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
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import org.infogrid.lid.AbstractReadOnlyLidLocalPersonaManager;
import org.infogrid.lid.LidLocalPersona;
import org.infogrid.lid.LidLocalPersonaUnknownException;
import org.infogrid.lid.AbstractLidLocalPersona;
import org.infogrid.lid.credential.LidCredentialType;
import org.infogrid.lid.credential.LidInvalidCredentialException;
import org.infogrid.lid.credential.LidPasswordCredentialType;
import org.infogrid.util.http.SaneRequest;
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
     * @param managerDn the distinguished name of the LDAP manager
     * @param managerPassword the password of the LDAP manager
     * @param props Properties to use when creating LDAP/JNDI InitialContext
     * @return the created LdapLidLocalPersonaManager
     * @throws NamingException something went wrong when attempting to bind to
     */
    public static LdapLidLocalPersonaManager create(
            String     managerDn,
            String     managerPassword,
            Properties props )
        throws
            NamingException
    {
        return create( managerDn, managerPassword, props, null, null );
    }

    /**
     * Factory method.
     *
     * @param managerDn the distinguished name of the LDAP manager
     * @param managerPassword the password of the LDAP manager
     * @param props Properties to use when creating LDAP/JNDI InitialContext
     * @param filter the LDAP filter expression
     * @param controls the SearchControls to use for queries
     * @return the created LdapLidLocalPersonaManager
     * @throws NamingException something went wrong when attempting to bind to
     */
    public static LdapLidLocalPersonaManager create(
            String         managerDn,
            String         managerPassword,
            Properties     props,
            String         filter,
            SearchControls controls )
        throws
            NamingException
    {
        Properties passwordDirProps = (Properties) props.clone();

        props.put( javax.naming.Context.SECURITY_PRINCIPAL,      managerDn );
        props.put( javax.naming.Context.SECURITY_CREDENTIALS,    managerPassword );

        DirContext dir = new InitialDirContext( props );

        LdapLidLocalPersonaManager ret = new LdapLidLocalPersonaManager( dir, filter, controls, passwordDirProps );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param managerDir the DirContext, accessed by the LDAP manager, in which to find the identities
     * @param filter the LDAP filter expression
     * @param controls the SearchControls to use for queries
     * @param passwordDirProps the Properties to use when attempting to check a password
     */
    protected LdapLidLocalPersonaManager(
            DirContext     managerDir,
            String         filter,
            SearchControls controls,
            Properties     passwordDirProps )
    {
        theManagerDir       = managerDir;
        theFilter           = filter;
        theControls         = controls;
        thePasswordDirProps = passwordDirProps;
    }

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
            found = theManagerDir.search( identifier, theFilter, theControls );

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
                LidLocalPersona ret = new LdapLidLocalPersona( identifier, attributes );
                
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
    protected DirContext theManagerDir;

    /**
     * The LDAP filter expression to use.
     */
    protected String theFilter;
    
    /**
     * The search controls.
     */
    protected SearchControls theControls;

    /**
     * The Properties to use when attempting to check a password.
     */
    protected Properties thePasswordDirProps;
    
    /**
     * The Set of LidCredentialTypes available for LidLocalPersonas hosted by this LidLocalPersonaManager.
     */
    protected static final Set<LidCredentialType> CREDENTIAL_TYPES = new HashSet<LidCredentialType>();
    static {
            CREDENTIAL_TYPES.add( LidPasswordCredentialType.create());
    };

    /**
     * Implementation of LidLocalPersona for this LidLocalPersonaManager.
     */
    class LdapLidLocalPersona
            extends
                AbstractLidLocalPersona
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         * 
         * @param identifier the unique identifier of the persona, e.g. their identity URL
         * @param attributes attributes of the persona, e.g. first name
         */
        protected LdapLidLocalPersona(
                String                        identifier,
                Map<String,String>            attributes )
        {
            super( identifier, attributes, CREDENTIAL_TYPES );
        }

        /**
         * Perform a check of the validity of a presented credential.
         * 
         * @param credType the LidCredentialType to check
         * @param request the incoming request carrying the presented credential
         * @throws LidInvalidCredentialException thrown if the credential was invalid
         */
        public void checkCredential(
                LidCredentialType credType,
                SaneRequest       request )
            throws
                LidInvalidCredentialException
        {
            if( !theCredentialTypes.contains( credType )) {
                throw new LidInvalidCredentialException( theIdentifier, credType );
            }
            if( !request.matchArgument( "lid-credtype", "simple-password" )) {
                throw new LidInvalidCredentialException( theIdentifier, credType );
            }
            String givenPassword = request.getArgument( "lid-credential" );
            
            Properties props = (Properties) thePasswordDirProps.clone();

            props.put( javax.naming.Context.SECURITY_PRINCIPAL,      theIdentifier );
            props.put( javax.naming.Context.SECURITY_CREDENTIALS,    givenPassword );

            DirContext passwordDir; // for debugging
            try {
                passwordDir = new InitialDirContext( props );

            } catch( NamingException ex ) {
                if( log.isDebugEnabled() ) {
                    log.debug( ex );
                }
                throw new LidInvalidCredentialException( theIdentifier, credType );
            }
        }
    }            
}
