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

package org.infogrid.meshbase.net;

import org.infogrid.util.StringHelper;
import org.infogrid.util.http.HTTP;

import java.net.URISyntaxException;

/**
 * Specifies how to access a NetMeshBase.
 */
public class NetMeshBaseAccessSpecification
{
    /**
     * Factory method.
     *
     * @param identifier identifies the NetMeshBase to access
     * @return the created NetMeshBaseAccessSpecification
     */
    public static NetMeshBaseAccessSpecification create(
            NetMeshBaseIdentifier identifier )
    {
        return new NetMeshBaseAccessSpecification( identifier, DEFAULT_SCOPE, DEFAULT_COHERENCE );
    }

    /**
     * Factory method.
     *
     * @param identifier identifies the NetMeshBase to access
     * @param scope the ScopeSpecification for the access
     * @return the created NetMeshBaseAccessSpecification
     */
    public static NetMeshBaseAccessSpecification create(
            NetMeshBaseIdentifier  identifier,
            ScopeSpecification     scope )
    {
        return new NetMeshBaseAccessSpecification( identifier, scope, DEFAULT_COHERENCE );
    }

    /**
     * Factory method.
     *
     * @param identifier identifies the NetMeshBase to access
     * @param coherence the CoherenceSpecification for the access
     * @return the created NetMeshBaseAccessSpecification
     */
    public static NetMeshBaseAccessSpecification create(
            NetMeshBaseIdentifier  identifier,
            CoherenceSpecification coherence )
    {
        return new NetMeshBaseAccessSpecification( identifier, DEFAULT_SCOPE, coherence );
    }

    /**
     * Factory method.
     *
     * @param identifier identifies the NetMeshBase to access
     * @param scope the ScopeSpecification for the access
     * @param coherence the CoherenceSpecification for the access
     * @return the created NetMeshBaseAccessSpecification
     */
    public static NetMeshBaseAccessSpecification create(
            NetMeshBaseIdentifier  identifier,
            ScopeSpecification     scope,
            CoherenceSpecification coherence )
    {
        return new NetMeshBaseAccessSpecification( identifier, scope, coherence );
    }

    /**
     * Convenience factory method.
     *
     * @param identifiers identifies the NetMeshBases to access
     * @return the created NetMeshBaseAccessSpecifications, in sequence
     */
    public static NetMeshBaseAccessSpecification [] create(
            NetMeshBaseIdentifier [] identifiers )
    {
        if( identifiers == null ) {
            return null;
        }
        NetMeshBaseAccessSpecification [] ret = new NetMeshBaseAccessSpecification[ identifiers.length ];
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = new NetMeshBaseAccessSpecification( identifiers[i], DEFAULT_SCOPE, DEFAULT_COHERENCE );
        }
        return ret;
    }

    /**
     * Factory method based on an external form representation.
     *
     * @param s the external form
     * @return the created NetMeshBaseAccessSpecification
     * @throws URISyntaxException thrown if the external form could not be parsed
     */
    public static NetMeshBaseAccessSpecification fromExternalForm(
            String s )
        throws
            URISyntaxException
    {
        int q = s.indexOf( '?' );
        if( q < 0 ) {
            NetMeshBaseIdentifier netMeshBase = NetMeshBaseIdentifier.fromExternalForm( s );
            return new NetMeshBaseAccessSpecification( netMeshBase, DEFAULT_SCOPE, DEFAULT_COHERENCE );
        }
        // we need to comb through the URL
        String [] pairs           = s.substring( q+1 ).split( "&" );
        String    scopeString     = null;
        String    coherenceString = null;

        StringBuilder remainder = new StringBuilder( s.length() );
        remainder.append( s.substring( 0, q ));

        char sep = '?';

        for( int i=0 ; i<pairs.length ; ++i ) {
            if( scopeString == null && pairs[i].startsWith( SCOPE_KEYWORD + "=" )) {
                scopeString = pairs[i].substring( SCOPE_KEYWORD.length() + 1 );
                scopeString = HTTP.decodeUrlArgument( scopeString );
            } else if( coherenceString == null && pairs[i].startsWith( COHERENCE_KEYWORD + "=" )) {
                coherenceString = pairs[i].substring( COHERENCE_KEYWORD.length() + 1 );
                coherenceString = HTTP.decodeUrlArgument( coherenceString );
            } else {
                remainder.append( sep );
                remainder.append( pairs[i] );
                sep = '&';
            }
        }
        return new NetMeshBaseAccessSpecification(
                NetMeshBaseIdentifier.fromExternalForm( remainder.toString() ),
                scopeString != null ? ScopeSpecification.fromExternalForm( scopeString ) : DEFAULT_SCOPE ,
                coherenceString != null ? CoherenceSpecification.fromExternalForm( coherenceString ) : DEFAULT_COHERENCE );
    }

    /**
     * Constructor.
     *
     * @param netMeshBase identifies the NetMeshBase to access
     * @param scope the ScopeSpecification for the access
     * @param coherence the CoherenceSpecification for the access
     */
    protected NetMeshBaseAccessSpecification(
            NetMeshBaseIdentifier  netMeshBase,
            ScopeSpecification     scope,
            CoherenceSpecification coherence )
    {
        theNetMeshBaseIdentifier  = netMeshBase;
        theScopeSpecification     = scope;
        theCoherenceSpecification = coherence;
    }
    
    /**
     * Obtain the NetMeshBaseIdentifier.
     *
     * @return the NetMeshBaseIdentifier
     */
    public NetMeshBaseIdentifier getNetMeshBaseIdentifier()
    {
        return theNetMeshBaseIdentifier;
    }
    
    /**
     * Obtain the ScopeSpecification, if any.
     *
     * @return the ScopeSpecification
     */
    public ScopeSpecification getScopeSpecification()
    {
        return theScopeSpecification;
    }

    /**
     * Obtain the CoherenceSpecification, if any.
     *
     * @return the CoherenceSpecification
     */
    public CoherenceSpecification getCoherenceSpecification()
    {
        return theCoherenceSpecification;
    }

    /**
     * Convert NetMeshBaseAccessSpecification into an external form.
     *
     * @return the external form
     */
    public String toExternalForm()
    {
        StringBuilder ret = new StringBuilder();
        ret.append( theNetMeshBaseIdentifier.toExternalForm() );

        char sep = '?';
        
        if( theScopeSpecification != null ) {
            ret.append( sep );
            ret.append( SCOPE_KEYWORD ).append( "=" );
            ret.append( HTTP.encodeToValidUrlArgument( theScopeSpecification.toExternalForm() ));
            sep = '&';
        }
        if( theCoherenceSpecification != null ) {
            ret.append( sep );
            ret.append( COHERENCE_KEYWORD ).append( "=" );
            ret.append( HTTP.encodeToValidUrlArgument( theCoherenceSpecification.toExternalForm() ));
            sep = '&';
        }
        return ret.toString();
    }

    /**
     * Determine equality.
     *
     * @param other the Object to compare against
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( !( other instanceof NetMeshBaseAccessSpecification )) {
            return false;
        }
        NetMeshBaseAccessSpecification realOther = (NetMeshBaseAccessSpecification) other;

        if( !theNetMeshBaseIdentifier.equals( realOther.theNetMeshBaseIdentifier )) {
            return false;
        }
        if( theScopeSpecification != null ) {
            if( !theScopeSpecification.equals( realOther.theScopeSpecification )) {
                return false;
            }
        } else if( realOther.theScopeSpecification != null ) {
            return false;
        }
        if( theCoherenceSpecification != null ) {
            if( !theCoherenceSpecification.equals( realOther.theCoherenceSpecification )) {
                return false;
            }
        } else if( realOther.theCoherenceSpecification != null ) {
            return false;
        }
        return true;
    }

    /**
     * Determine hash code.
     * 
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        int ret = 0;
        if( theNetMeshBaseIdentifier != null ) {
            ret ^= theNetMeshBaseIdentifier.hashCode();
        }
        if( theScopeSpecification != null ) {
            ret ^= theScopeSpecification.hashCode();
        }
        if( theCoherenceSpecification != null ) {
            ret ^= theCoherenceSpecification.hashCode();
        }
        return ret;
    }

    /**
     * Convert to String form, for debugging.
     *
     * @return String form
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                        "netMeshBase",
                        "scope",
                        "coherence"
                },
                new Object[] {
                        theNetMeshBaseIdentifier,
                        theScopeSpecification,
                        theCoherenceSpecification
                });
    }

    /**
     * The Identifier of the NetMeshBase.
     */
    protected NetMeshBaseIdentifier theNetMeshBaseIdentifier;
    
    /**
     * The Scope of access.
     */
    protected ScopeSpecification theScopeSpecification;
    
    /**
     * The requested Coherence.
     */
    protected CoherenceSpecification theCoherenceSpecification;

    /**
     * The default scope for the object graph that we want.
     */
    public static final ScopeSpecification DEFAULT_SCOPE = null;
    
    /**
     * The default coherence for the object graph that we want.
     */
    public static final CoherenceSpecification DEFAULT_COHERENCE = null;
    
    /**
     * URL parameter keyword indicating the scope parameter.
     */
    public static final String SCOPE_KEYWORD = "lid-scope";
    
    /**
     * URL parameter keyword indicating the coherence parameter.
     */
    public static final String COHERENCE_KEYWORD = "lid-coherence";
 }
