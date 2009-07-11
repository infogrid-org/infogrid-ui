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
import org.infogrid.util.InvalidIdentifierException;
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
     * @param props Properties to use to connect to the directory
     * @param ldapContextName name of the LDAP context object in which to search, or null if default
     * @param filter the LDAP filter expression, or null if default
     * @param attributeList the list of attributes to pull out of LDAP. If null, pull out all attributes.
     * @return the created LdapLidLocalPersonaManager
     * @throws NamingException something went wrong when attempting to bind to
     */
    public static LdapLidLocalPersonaManager create(
            Properties props,
            String     ldapContextName,
            String     filter,
            String []  attributeList )
        throws
            NamingException
    {
        return create(
                props,
                ldapContextName,
                filter,
                null,
                attributeList );
    }

    /**
     * Factory method.
     *
     * @param props Properties to use to connect to the directory
     * @param ldapContextName name of the LDAP context object in which to search, or null if default
     * @param filter the LDAP filter expression, or null if default
     * @param controls the SearchControls to use for queries, or null if default
     * @param attributeList the list of attributes to pull out of LDAP. If null, pull out all attributes.
     * @return the created LdapLidLocalPersonaManager
     * @throws NamingException something went wrong when attempting to bind to
     */
    public static LdapLidLocalPersonaManager create(
            Properties     props,
            String         ldapContextName,
            String         filter,
            SearchControls controls,
            String []      attributeList )
        throws
            NamingException
    {
        DirContext dir = new InitialDirContext( props );

        if( ldapContextName == null ) {
            ldapContextName = props.getProperty( javax.naming.Context.PROVIDER_URL );
        }
        if( filter == null || filter.length() == 0 ) {
            filter = DEFAULT_FILTER;
        }
        if( controls == null ) {
            controls = new SearchControls();
            controls.setSearchScope( SearchControls.SUBTREE_SCOPE );
        }

        LdapLidLocalPersonaManager ret = new LdapLidLocalPersonaManager( dir, ldapContextName, filter, controls, attributeList );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param managerDir the DirContext, accessed by the LDAP manager, in which to find the identities
     * @param ldapContextName name of the LDAP context object in which to search
     * @param filter the LDAP filter expression
     * @param controls the SearchControls to use for queries
     * @param attributeList the list of attributes to pull out of LDAP. If null, pull out all attributes.
     */
    protected LdapLidLocalPersonaManager(
            DirContext     managerDir,
            String         ldapContextName,
            String         filter,
            SearchControls controls,
            String []      attributeList )
    {
        theManagerDir       = managerDir;
        theLdapContextName  = ldapContextName;
        theFilter           = filter;
        theControls         = controls;
        theAttributeList    = attributeList;
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
        NamingEnumeration found = null;
        String            s     = identifier.toExternalForm();

        if( s.contains( "?" ) || s.contains( "*" )) {
            throw new InvalidIdentifierException( identifier );
        }
        if( s.length() == 0 ) {
            throw new LidLocalPersonaUnknownException( identifier );
        }

        String filter = MessageFormat.format( theFilter, s );

        try {
            found = theManagerDir.search( theLdapContextName, filter, theControls );

            if( found.hasMore() ) {
                SearchResult current = (SearchResult) found.next();

                HashMap<String,String> attributes = new HashMap<String,String>();

                if( theAttributeList != null ) {
                    for( int i=0 ; i<theAttributeList.length ; ++i ) {
                        Object value = current.getAttributes().get( theAttributeList[i] );
                        if( value != null ) {
                            attributes.put( theAttributeList[i], value.toString() );
                        } else {
                            attributes.put( theAttributeList[i], null );
                        }
                    }
                } else {
                    NamingEnumeration<? extends Attribute> currentAttributes = current.getAttributes().getAll();
                    while( currentAttributes.hasMore() ) {
                        Attribute att = currentAttributes.next();

                        Object value = att.get();
                        if( value != null ) {
                            attributes.put( att.getID(), value.toString() );
                        } else {
                            attributes.put( att.getID(), null );
                        }
                    }
                }
                LidLocalPersona ret = SimpleLidLocalPersona.create( identifier, attributes );

                if( found.hasMore() ) {
                    SearchResult current2 = (SearchResult) found.next();
                    log.error( "More than one result found: ", current, current2 );
                }
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

    /**
     * The list of attributes to pull out of LDAP. If null, pull out all attributes.
     */
    protected String [] theAttributeList;

    /**
     * The default filter.
     */
    protected static final String DEFAULT_FILTER = "(sAMAccountName={0})";
}
