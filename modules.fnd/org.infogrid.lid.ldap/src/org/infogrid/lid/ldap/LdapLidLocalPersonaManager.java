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

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Properties;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.InitialDirContext;
import org.infogrid.lid.AbstractReadOnlyLidLocalPersonaManager;
import org.infogrid.lid.local.LidLocalPersona;
import org.infogrid.lid.local.LidLocalPersonaUnknownException;
import org.infogrid.lid.local.SimpleLidLocalPersona;
import org.infogrid.util.Identifier;
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
     * @param ldapContextName name of the LDAP context object in which to search
     * @return the created LdapLidLocalPersonaManager
     * @throws NamingException something went wrong when attempting to bind to
     */
    public static LdapLidLocalPersonaManager create(
            String     managerDn,
            String     managerPassword,
            Properties props,
            String     ldapContextName )
        throws
            NamingException
    {
        return create( managerDn, managerPassword, props, ldapContextName, null, null );
    }

    /**
     * Factory method.
     *
     * @param managerDn the distinguished name of the LDAP manager
     * @param managerPassword the password of the LDAP manager
     * @param props Properties to use when creating LDAP/JNDI InitialContext
     * @param ldapContextName name of the LDAP context object in which to search
     * @param filter the LDAP filter expression
     * @param controls the SearchControls to use for queries
     * @return the created LdapLidLocalPersonaManager
     * @throws NamingException something went wrong when attempting to bind to
     */
    public static LdapLidLocalPersonaManager create(
            String         managerDn,
            String         managerPassword,
            Properties     props,
            String         ldapContextName,
            String         filter,
            SearchControls controls )
        throws
            NamingException
    {
        Properties managerProps = (Properties) props.clone();

        managerProps.put( javax.naming.Context.SECURITY_PRINCIPAL,      managerDn );
        managerProps.put( javax.naming.Context.SECURITY_CREDENTIALS,    managerPassword );

        DirContext dir = new InitialDirContext( managerProps );

        LdapLidLocalPersonaManager ret = new LdapLidLocalPersonaManager( dir, ldapContextName, filter, controls );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param managerDir the DirContext, accessed by the LDAP manager, in which to find the identities
     * @param ldapContextName name of the LDAP context object in which to search
     * @param filter the LDAP filter expression
     * @param controls the SearchControls to use for queries
     */
    protected LdapLidLocalPersonaManager(
            DirContext     managerDir,
            String         ldapContextName,
            String         filter,
            SearchControls controls )
    {
        theManagerDir       = managerDir;
        theLdapContextName  = ldapContextName;
        theFilter           = filter;
        theControls         = controls;
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
        NamingEnumeration found = null;
        String            s     = identifier.toExternalForm();

        String filter = MessageFormat.format( theFilter, s );

        try {
            found = theManagerDir.search( theLdapContextName, filter, theControls );

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
                LidLocalPersona ret = SimpleLidLocalPersona.create( identifier, attributes );
                
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
     * Name of the LDAP context to search.
     */
    protected String theLdapContextName;

    /**
     * The LDAP filter expression to use.
     */
    protected String theFilter;
    
    /**
     * The search controls.
     */
    protected SearchControls theControls;
}
