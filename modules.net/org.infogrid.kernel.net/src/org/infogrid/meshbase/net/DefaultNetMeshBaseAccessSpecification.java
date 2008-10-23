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

/**
 * Default implementation of NetMeshBaseAccessSpecification.
 */
public class DefaultNetMeshBaseAccessSpecification
        implements
            NetMeshBaseAccessSpecification
{
    /**
     * Constructor.
     *
     * @param netMeshBase identifies the NetMeshBase to access
     * @param scope the ScopeSpecification for the access
     * @param coherence the CoherenceSpecification for the access
     */
    protected DefaultNetMeshBaseAccessSpecification(
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

        if( !theNetMeshBaseIdentifier.equals( realOther.getNetMeshBaseIdentifier() )) {
            return false;
        }
        if( theScopeSpecification != null ) {
            if( !theScopeSpecification.equals( realOther.getScopeSpecification() )) {
                return false;
            }
        } else if( realOther.getScopeSpecification() != null ) {
            return false;
        }
        if( theCoherenceSpecification != null ) {
            if( !theCoherenceSpecification.equals( realOther.getCoherenceSpecification() )) {
                return false;
            }
        } else if( realOther.getCoherenceSpecification() != null ) {
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
}
