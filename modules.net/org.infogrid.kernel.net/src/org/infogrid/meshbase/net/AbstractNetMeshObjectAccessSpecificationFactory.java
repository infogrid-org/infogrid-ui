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

import java.net.URI;
import java.net.URISyntaxException;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;

/**
 * Factors out common behaviors of NetMeshObjectAccessSpecificationFactory implementations.
 */
public abstract class AbstractNetMeshObjectAccessSpecificationFactory
        implements
            NetMeshObjectAccessSpecificationFactory
{
    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param meshObjectIdentifierFactory the factory for MeshObjectIdentifiers
     * @param meshBaseIdentifierFactory the factory for MeshBaseIdentifiers
     * @param netMeshBaseAccessSpecificationFactory the factory for NetMeshBaseAccessSpecifications
     */
    protected AbstractNetMeshObjectAccessSpecificationFactory(
            NetMeshObjectIdentifierFactory        meshObjectIdentifierFactory,
            NetMeshBaseIdentifierFactory          meshBaseIdentifierFactory,
            NetMeshBaseAccessSpecificationFactory netMeshBaseAccessSpecificationFactory )
    {
        theMeshObjectIdentifierFactory           = meshObjectIdentifierFactory;
        theMeshBaseIdentifierFactory             = meshBaseIdentifierFactory;
        theNetMeshBaseAccessSpecificationFactory = netMeshBaseAccessSpecificationFactory;
    }
    
    /**
     * Obtain the underlying NetMeshObjectIdentiferFactory.
     * 
     * @return the NetMeshObjectIdentifierFactory
     */
    public NetMeshObjectIdentifierFactory getNetMeshObjectIdentifierFactory()
    {
        return theMeshObjectIdentifierFactory;
    }

    /**
     * Obtain the underlying NetMeshBaseIdentifierFactory.
     * 
     * @return the NetMeshBaseIdentifierFactory
     */
    public NetMeshBaseIdentifierFactory getNetMeshBaseIdentifierFactory()
    {
        return theMeshBaseIdentifierFactory;
    }
    
    /**
     * Obtain the underlying NetMeshBaseAccessSpecificationFactory.
     * 
     * @return the NetMeshBaseAccessSpecificationFactory
     */
    public NetMeshBaseAccessSpecificationFactory getNetMeshBaseAccessSpecificationFactory()
    {
        return theNetMeshBaseAccessSpecificationFactory;
    }

    /**
     * Convert a String into a NetMeshObjectAccessSpecification.
     * 
     * @param raw the String
     * @return the created NetMeshObjectAccessSpecification
     * @throws URISyntaxException thrown if the String could not be parsed
     */
    public NetMeshObjectAccessSpecification fromExternalForm(
            String raw )
        throws
            URISyntaxException
    {
        if( raw == null ) {
            return null;
        }

        int hash = raw.indexOf( '#' );
        
        String pathString;
        String objectString;
        if( hash >= 0 ) {
            pathString   = raw.substring( 0, hash );
            objectString = raw.substring( hash+1 );

        } else if( raw.indexOf( '.' ) >= 0 ) {
            pathString   = raw;
            objectString = null;
        } else {
            pathString   = null;
            objectString = raw;
        }

        NetMeshBaseAccessSpecification [] pathElements;
        
        if( pathString != null && pathString.length() > 0 ) {
            String [] pathElementStrings = pathString.split( "!" );

            pathElements = new NetMeshBaseAccessSpecification[ pathElementStrings.length ];
            for( int i=0 ; i<pathElements.length ; ++i ) {
                pathElements[i] = theNetMeshBaseAccessSpecificationFactory.fromExternalForm( pathElementStrings[i] );
            }
        } else {
            pathElements = new NetMeshBaseAccessSpecification[0];
        }
        
        NetMeshObjectIdentifier object = null;
        if( objectString != null ) {
            object = theMeshObjectIdentifierFactory.fromExternalForm( objectString );
        }
        
        NetMeshObjectAccessSpecification ret = new DefaultNetMeshObjectAccessSpecification(
                pathElements,
                object );
        return ret;
    }

    /**
     * Factory method to obtain a NetMeshObjectAccessSpecification to a locally available MeshObject. This
     * is a degenerate form of NetMeshObjectAccessSpecification, but useful for API consistency.
     * 
     * @param extName Identifier of the local NetMeshObject
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtainToLocalObject(
            NetMeshObjectIdentifier extName )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        theNetMeshBaseAccessSpecificationFactory.obtain( extName.getNetMeshBaseIdentifier() ) },
                extName );
    }

    /**
     * Factory method to obtain a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * requesting the home object at the NetMeshBaseIdentifier.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier oneElementName )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName ) },
                null );
    }

    /**
     * Factory method to obtain a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * requesting a non-default NetMeshObject.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param extName Identifier of the remote non-default NetMeshObject
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier    oneElementName,
            NetMeshObjectIdentifier  extName )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName ) },
                extName );
    }

    /**
     * Factory method to obtain a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * requesting the home object at the NetMeshBaseIdentifier,
     * specifying a non-default ScopeSpecification.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param scope the ScopeSpecification
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier oneElementName,
            ScopeSpecification    scope )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName, scope ) },
                null );
    }

    /**
     * Factory method to obtain a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * requesting the home object at the NetMeshBaseIdentifier.
     * specifying a non-default CoherenceSpecification.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param coherence the CoherenceSpecification
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier  oneElementName,
            CoherenceSpecification coherence )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName, coherence ) },
                null );
    }

    /**
     * Factory method to obtain a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * requesting a non-default NetMeshObject,
     * specifying a non-default ScopeSpecification.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param extName Identifier of the remote non-default NetMeshObject
     * @param scope the ScopeSpecification
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier   oneElementName,
            NetMeshObjectIdentifier extName,
            ScopeSpecification      scope )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName, scope ) },
                extName );
    }

    /**
     * Factory method to obtain a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * requesting a non-default MeshObject,
     * specifying a non-default CoherenceSpecification.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param extName Identifier of the remote non-default MeshObject
     * @param coherence the CoherenceSpecification
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier   oneElementName,
            NetMeshObjectIdentifier extName,
            CoherenceSpecification  coherence )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName, coherence ) },
                extName );
    }

    /**
     * Factory method to obtain a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * requesting the home object at the NetMeshBaseIdentifier,
     * specifying a non-default ScopeSpecification and a non-default CoherenceSpecification.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param scope the ScopeSpecification
     * @param coherence the CoherenceSpecification
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier  oneElementName,
            ScopeSpecification     scope,
            CoherenceSpecification coherence )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName, scope, coherence ) },
                null );
    }

    /**
     * Factory method to obtain a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * requesting a non-default MeshObject,
     * specifying a non-default ScopeSpecification and a non-default CoherenceSpecification.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param extName Identifier of the remote non-default MeshObject
     * @param scope the ScopeSpecification
     * @param coherence the CoherenceSpecification
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier   oneElementName,
            NetMeshObjectIdentifier extName,
            ScopeSpecification      scope,
            CoherenceSpecification  coherence )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                new NetMeshBaseAccessSpecification[] {
                        theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName, scope, coherence ) },
                extName );
    }

    /**
     * Factory method to obtain a multi-element NetMeshObjectAccessSpecification from
     * a sequence of NetMeshBaseAccessSpecification.
     * 
     * @param elements the NetMeshBaseAccessSpecifications, in sequence
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseAccessSpecification [] elements )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                elements,
                null );
    }

    /**
     * Factory method to obtain a multi-element NetMeshObjectAccessSpecification from
     * a sequence of NetMeshBaseAccessSpecifications,
     * requesting a non-default MeshObject.
     * 
     * @param elements the NetMeshBaseAccessSpecifications, in sequence
     * @param extName Identifier of the remote non-default MeshObject
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseAccessSpecification [] elements,
            NetMeshObjectIdentifier           extName )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                elements,
                extName );
    }

    /**
     * Factory method to obtain a multi-element NetMeshObjectAccessSpecification
     * from a series of NetMeshBaseIdentifiers.
     * 
     * @param elements the NetMeshBaseIdentifier, in sequence
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier [] elements )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                createSeveral( elements ),
                null );
    }

    /**
     * Factory method to obtain a multi-element NetMeshObjectAccessSpecification from
     * a series of NetMeshBaseIdentifiers,
     * requesting a non-default MeshObject.
     * 
     * @param elements the NetMeshBaseIdentifiers, in sequence
     * @param extName Identifier of the remote non-default MeshObject
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier [] elements,
            NetMeshObjectIdentifier  extName )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                createSeveral( elements ),
                extName );
    }
    
    /**
     * Convenience factory method to obtain a single-element NetMeshObjectAccessSpecification
     * by converting a URI into a NetMeshBaseIdentifier, requesting its home MeshObject.
     * 
     * @param remoteLocation the URI
     * @return created NetMeshObjectAccessSpecification
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            URI remoteLocation )
        throws
            URISyntaxException
    {
        NetMeshBaseIdentifier oneElementName = theMeshBaseIdentifierFactory.obtain( remoteLocation );
        return obtain( oneElementName );
    }

    /**
     * Convenience factory method to obtain a single-element NetMeshObjectAccessSpecification
     * by converting a URI into a NetMeshBaseIdentifier, requesting its home MeshObject,
     * specifying a non-default CoherenceSpecification.
     * 
     * @param remoteLocation the URI
     * @param coherence the CoherenceSpecification
     * @return created NetMeshObjectAccessSpecification
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            URI                    remoteLocation,
            CoherenceSpecification coherence )
        throws
            URISyntaxException
    {
        NetMeshBaseIdentifier oneElementName = theMeshBaseIdentifierFactory.obtain( remoteLocation );
        return obtain( oneElementName, coherence );
    }

    /**
     * Convenience factory method to obtain a single-element NetMeshObjectAccessSpecification
     * by converting a URI into a NetMeshBaseIdentifier, requesting its home MeshObject,
     * specifying a non-default ScopeSpecification.
     * 
     * @param remoteLocation the URI
     * @param scope the ScopeSpecification
     * @return created NetMeshObjectAccessSpecification
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            URI                remoteLocation,
            ScopeSpecification scope )
        throws
            URISyntaxException
    {
        NetMeshBaseIdentifier oneElementName = theMeshBaseIdentifierFactory.obtain( remoteLocation );
        return obtain( oneElementName, scope );
    }

    /**
     * Convenience factory method to obtain a single-element NetMeshObjectAccessSpecification
     * by converting a URI into a NetMeshBaseIdentifier, requesting its home MeshObject,
     * specifying a non-default ScopeSpecification and a non-default CoherenceSpecification.
     * 
     * @param remoteLocation the URI
     * @param scope the ScopeSpecification
     * @param coherence the CoherenceSpecification
     * @return created NetMeshObjectAccessSpecification
     * @throws URISyntaxException thrown if the syntax could not be parsed
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            URI                    remoteLocation,
            ScopeSpecification     scope,
            CoherenceSpecification coherence )
        throws
            URISyntaxException
    {
        NetMeshBaseIdentifier oneElementName = theMeshBaseIdentifierFactory.obtain( remoteLocation );
        return obtain( oneElementName, scope, coherence );
    }
    
    /**
     * Convenience factory method.
     *
     * @param identifiers identifies the NetMeshBases to access
     * @return the created NetMeshBaseAccessSpecifications, in sequence
     */
    protected NetMeshBaseAccessSpecification [] createSeveral(
            NetMeshBaseIdentifier [] identifiers )
    {
        if( identifiers == null ) {
            return null;
        }
        NetMeshBaseAccessSpecification [] ret = new NetMeshBaseAccessSpecification[ identifiers.length ];
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = theNetMeshBaseAccessSpecificationFactory.obtain(
                    identifiers[i],
                    NetMeshBaseAccessSpecification.DEFAULT_SCOPE,
                    NetMeshBaseAccessSpecification.DEFAULT_COHERENCE );
        }
        return ret;
    }

    /**
     * The factory for MeshObjectIdentifiers.
     */
    protected NetMeshObjectIdentifierFactory theMeshObjectIdentifierFactory;

    /**
     * The factory for MeshBaseIdentifiers.
     */
    protected NetMeshBaseIdentifierFactory theMeshBaseIdentifierFactory;
    
    /**
     * The factory for NetMeshBaseAccessSpecifications.
     */
    protected NetMeshBaseAccessSpecificationFactory theNetMeshBaseAccessSpecificationFactory;
}
