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

package org.infogrid.meshbase.net;

import org.infogrid.util.http.HTTP;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * Default implementation of NetMeshBaseAccessSpecificationFactory.
 */
public class DefaultNetMeshBaseAccessSpecificationFactory
        implements
            NetMeshBaseAccessSpecificationFactory
{
    /**
     * Factory method for the factory itself.
     * 
     * @param meshBaseIdentifierFactory factory for MeshBaseIdentifiers
     * @return the created DefaultNetMeshBaseAccessSpecificationFactory
     */
    public static DefaultNetMeshBaseAccessSpecificationFactory create(
            NetMeshBaseIdentifierFactory meshBaseIdentifierFactory )
    {
        DefaultNetMeshBaseAccessSpecificationFactory ret
                = new DefaultNetMeshBaseAccessSpecificationFactory( meshBaseIdentifierFactory );
        
        return ret;
    }
    
    /**
     * Constructor.
     * 
     * @param meshBaseIdentifierFactory factory for MeshBaseIdentifiers
     */
    protected DefaultNetMeshBaseAccessSpecificationFactory(
            NetMeshBaseIdentifierFactory meshBaseIdentifierFactory )
    {
        theMeshBaseIdentifierFactory = meshBaseIdentifierFactory;
    }

    /**
     * Factory method.
     *
     * @param identifier identifies the NetMeshBase to access
     * @return the created NetMeshBaseAccessSpecification
     */
    public NetMeshBaseAccessSpecification obtain(
            NetMeshBaseIdentifier identifier )
    {
        return new DefaultNetMeshBaseAccessSpecification(
                identifier,
                NetMeshBaseAccessSpecification.DEFAULT_SCOPE,
                NetMeshBaseAccessSpecification.DEFAULT_COHERENCE );
    }

    /**
     * Factory method.
     *
     * @param identifier identifies the NetMeshBase to access
     * @param coherence the CoherenceSpecification for the access
     * @return the created NetMeshBaseAccessSpecification
     */
    public NetMeshBaseAccessSpecification obtain(
            NetMeshBaseIdentifier  identifier,
            CoherenceSpecification coherence )
    {
        return new DefaultNetMeshBaseAccessSpecification(
                identifier,
                NetMeshBaseAccessSpecification.DEFAULT_SCOPE,
                coherence );
    }

    /**
     * Factory method.
     *
     * @param identifier identifies the NetMeshBase to access
     * @param scope the ScopeSpecification for the access
     * @return the created NetMeshBaseAccessSpecification
     */
    public NetMeshBaseAccessSpecification obtain(
            NetMeshBaseIdentifier  identifier,
            ScopeSpecification     scope )
    {
        return new DefaultNetMeshBaseAccessSpecification(
                identifier,
                scope,
                NetMeshBaseAccessSpecification.DEFAULT_COHERENCE );
    }

    /**
     * Factory method.
     *
     * @param identifier identifies the NetMeshBase to access
     * @param scope the ScopeSpecification for the access
     * @param coherence the CoherenceSpecification for the access
     * @return the created NetMeshBaseAccessSpecification
     */
    public NetMeshBaseAccessSpecification obtain(
            NetMeshBaseIdentifier  identifier,
            ScopeSpecification     scope,
            CoherenceSpecification coherence )
    {
        return new DefaultNetMeshBaseAccessSpecification(
                identifier,
                scope,
                coherence );
    }

    /**
     * Recreate a NetMeshBaseAccessSpecification from an external form.
     *
     * @param raw the external form
     * @return the created NetMeshBaseAccessSpecification
     * @throws StringRepresentationParseException thrown if a parsing error occurred
     */
    public DefaultNetMeshBaseAccessSpecification fromExternalForm(
            String raw )
        throws
            StringRepresentationParseException
    {
        int q = raw.indexOf( '?' );
        if( q < 0 ) {
            NetMeshBaseIdentifier netMeshBase = theMeshBaseIdentifierFactory.fromExternalForm( raw );
            return new DefaultNetMeshBaseAccessSpecification(
                    netMeshBase,
                    NetMeshBaseAccessSpecification.DEFAULT_SCOPE,
                    NetMeshBaseAccessSpecification.DEFAULT_COHERENCE );
        }
        // we need to comb through the URL
        String [] pairs           = raw.substring( q+1 ).split( "&" );
        String    coherenceString = null;
        String    scopeString     = null;

        StringBuilder remainder = new StringBuilder( raw.length() );
        remainder.append( raw.substring( 0, q ));

        char sep = '?';

        for( int i=0 ; i<pairs.length ; ++i ) {
            if( scopeString == null && pairs[i].startsWith( NetMeshBaseAccessSpecification.SCOPE_KEYWORD + "=" )) {
                scopeString = pairs[i].substring( NetMeshBaseAccessSpecification.SCOPE_KEYWORD.length() + 1 );
                scopeString = HTTP.decodeUrlArgument( scopeString );

            } else if( coherenceString == null && pairs[i].startsWith( NetMeshBaseAccessSpecification.COHERENCE_KEYWORD + "=" )) {
                coherenceString = pairs[i].substring( NetMeshBaseAccessSpecification.COHERENCE_KEYWORD.length() + 1 );
                coherenceString = HTTP.decodeUrlArgument( coherenceString );

            } else {
                remainder.append( sep );
                remainder.append( pairs[i] );
                sep = '&';
            }
        }
        return new DefaultNetMeshBaseAccessSpecification(
                theMeshBaseIdentifierFactory.fromExternalForm( remainder.toString() ),
                scopeString     != null ? ScopeSpecification.fromExternalForm(     scopeString     ) : NetMeshBaseAccessSpecification.DEFAULT_SCOPE,
                coherenceString != null ? CoherenceSpecification.fromExternalForm( coherenceString ) : NetMeshBaseAccessSpecification.DEFAULT_COHERENCE );
    }

    /**
     * Convert this StringRepresentation back to a NetMeshBaseAccessSpecification.
     *
     * @param representation the StringRepresentation in which this String is represented
     * @param s the String to parse
     * @return the created NetMeshBaseAccessSpecification
     * @throws StringRepresentationParseException thrown if a parsing error occurred
     */
    public NetMeshBaseAccessSpecification fromStringRepresentation(
            StringRepresentation representation,
            String               s )
        throws
            StringRepresentationParseException
    {
        throw new UnsupportedOperationException(); // FIXME
    }
    
    /**
     * Factory for MeshBaseIdentifiers.
     */
    protected NetMeshBaseIdentifierFactory theMeshBaseIdentifierFactory;
}
