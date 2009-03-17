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

import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;

/**
 * Default implementation of NetMeshObjectAccessSpecification.
 */
public class DefaultNetMeshObjectAccessSpecification
        implements
            NetMeshObjectAccessSpecification,
            CanBeDumped
{
    private final static long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor. Use factory class to instantiate.
     *
     * @param factory the factory that created this object
     * @param accessPath the sequence of network locations to traverse to find one where we can access the MeshObject
     * @param remoteIdentifier the identifier of the MeshObject there, if different from the default
     * @param scope the ScopeSpecification for the access
     */
    protected DefaultNetMeshObjectAccessSpecification(
            NetMeshObjectAccessSpecificationFactory factory,
            NetMeshBaseAccessSpecification []       accessPath,
            NetMeshObjectIdentifier                 remoteIdentifier,
            ScopeSpecification                      scope )
    {
        theFactory          = factory;
        theAccessPath       = accessPath != null ? accessPath : new NetMeshBaseAccessSpecification[0];
        theRemoteIdentifier = remoteIdentifier;
        
        for( int i=0 ; i<theAccessPath.length ; ++i ) {
            if( theAccessPath[i] == null ) {
                throw new IllegalArgumentException( "No AccessPath component in NetMeshObjectAccessSpecification must be null" );
            }
        }
        if( remoteIdentifier == null ) {
            throw new NullPointerException();
        }
        theScopeSpecification = scope;
    }

    /**
     * Obtain the factory that created this object.
     *
     * @return the factory
     */
    public NetMeshObjectAccessSpecificationFactory getFactory()
    {
        return theFactory;
    }

    /**
     * Obtain the NetMeshBaseAccessSpecification path.
     * 
     * @return the path we traverse to the MeshObject we want to access. May be of length 0.
     */
    public NetMeshBaseAccessSpecification [] getAccessPath()
    {
        return theAccessPath;
    }

    /**
     * Obtain the Identifier of the NetMeshObject that we are looking for in the remote NetMeshBase.
     * Calculate it if it is the default.
     *
     * @return the Identifier of the NetMeshObject that we are looking for
     */
    public NetMeshObjectIdentifier getNetMeshObjectIdentifier()
    {
        return theRemoteIdentifier;
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
     * Obtain an externalized version of this NetMeshObjectAccessSpecification.
     * 
     * @return external form of this NetMeshObjectAccessSpecification similar to URL.toExternalForm()
     */
    public String toExternalForm()
    {
        StringBuilder almostRet = new StringBuilder( 100 ); // fudge number

        String sep = "";
        for( int i=0 ; i<theAccessPath.length ; ++i ) {
            almostRet.append( sep );
            almostRet.append( theAccessPath[i].toExternalForm() );
            sep = "!";
        }
        if( theRemoteIdentifier != null ) {
            almostRet.append( "#" );
            almostRet.append( escapeHash( theRemoteIdentifier.toExternalForm() ));
        }
        if( theScopeSpecification != null ) {
            almostRet.append( "?" );
            almostRet.append( SCOPE_KEYWORD ).append( "=" );
            almostRet.append( HTTP.encodeToValidUrlArgument( theScopeSpecification.toExternalForm() ));
        }

        return almostRet.toString();
    }

    /**
     * Helper method to escape the hash sign.
     *
     * @param s String with hash
     * @return String with escaped hash
     */
    protected static String escapeHash(
            String s )
    {
        int           len = s.length();
        StringBuilder ret = new StringBuilder( len + 10 ); // fudge
        for( int i=0 ; i<len ; ++i ) {
            char c = s.charAt( i );
            switch( c ) {
                case '#':
                    ret.append( ESCAPED_HASH );
                    break;
                default:
                    ret.append( c );
                    break;
            }            
        }
        return ret.toString();
    }

    /**
     * Helper method to descape the hash site.
     *
     * @param s String with escaped hash
     * @return String with regular hash
     */
    protected static String descapeHash(
            String s )
    {
        int           len = s.length();
        StringBuilder ret = new StringBuilder( len );
        
        int startAt = 0;
        int foundAt;
        while( ( foundAt = s.indexOf( ESCAPED_HASH, startAt )) >= 0 ) {
            String sub = s.substring( startAt, foundAt );
            ret.append( sub );
            ret.append( '#' );
            foundAt += ESCAPED_HASH.length();
        }
        String sub = s.substring( startAt );
        ret.append( sub );

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
        if( !( other instanceof NetMeshObjectAccessSpecification )) {
            return false;
        }
        NetMeshObjectAccessSpecification realOther = (NetMeshObjectAccessSpecification) other;
        
        if( theAccessPath.length != realOther.getAccessPath().length ) {
            return false;
        }
        for( int i=0 ; i<theAccessPath.length ; ++i ) {
            if( !theAccessPath[i].equals( realOther.getAccessPath()[i] )) {
                return false;
            }
        }
        if( getNetMeshObjectIdentifier() != null ) {
            if( !getNetMeshObjectIdentifier().equals( realOther.getNetMeshObjectIdentifier() )) {
                return false;
            }
        } else if( realOther.getNetMeshObjectIdentifier() != null ) {
            return false;
        }
        if( theScopeSpecification != null ) {
            if( !theScopeSpecification.equals( realOther.getScopeSpecification() )) {
                return false;
            }
        } else if( realOther.getScopeSpecification() != null ) {
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
        for( int i=0 ; i<theAccessPath.length ; ++i ) {
            ret ^= theAccessPath[i].hashCode() >> i;
        }
        if( theRemoteIdentifier != null ) {
            ret ^= theRemoteIdentifier.hashCode();
        }
        if( theScopeSpecification != null ) {
            ret ^= theScopeSpecification.hashCode();
        }
        return ret;
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "theAccessPath",
                    "theRemoteIdentifier",
                    "theScopeSpecification"
                },
                new Object[] {
                    theAccessPath,
                    theRemoteIdentifier,
                    theScopeSpecification
                });
    }

    /**
     * Convert to String form, for debugging.
     *
     * @return String form
     */
    @Override
    public String toString()
    {
        return toExternalForm();
    }

    /**
     * The factory that created this object.
     */
    protected NetMeshObjectAccessSpecificationFactory theFactory;

    /**
     * The NetMeshBaseIdentifier path.
     */
    protected NetMeshBaseAccessSpecification [] theAccessPath;

    /**
     * If a non-default MeshObject shall be accessed, this captures the Identifier
     * of that MeshObject at the remote location.
     */
    protected NetMeshObjectIdentifier theRemoteIdentifier;

    /**
     * The Scope of access.
     */
    protected ScopeSpecification theScopeSpecification;

    /**
     * The escaped hash sign.
     */
    private static final String ESCAPED_HASH = "&#35;";
}
