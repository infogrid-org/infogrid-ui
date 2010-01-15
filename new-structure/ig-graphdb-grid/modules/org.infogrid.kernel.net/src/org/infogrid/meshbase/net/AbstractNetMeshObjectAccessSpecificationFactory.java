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

import java.net.URI;
import org.infogrid.mesh.net.NetMeshObjectIdentifier;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;

/**
 * Factors out common behaviors of NetMeshObjectAccessSpecificationFactory implementations.
 */
public abstract class AbstractNetMeshObjectAccessSpecificationFactory
        implements
            NetMeshObjectAccessSpecificationFactory
{
    private static final Log log = Log.getLogInstance( AbstractNetMeshObjectAccessSpecificationFactory.class ); // our own, private logger

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
     * Factory method to obtain a NetMeshObjectAccessSpecification to a locally available MeshObject. This
     * is a degenerate form of NetMeshObjectAccessSpecification, but useful for API consistency.
     * 
     * @param remoteIdentifier Identifier of the local NetMeshObject
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtainToLocalObject(
            NetMeshObjectIdentifier remoteIdentifier )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                this,
                new NetMeshBaseAccessSpecification[0],
                remoteIdentifier );
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
        try {
            return new DefaultNetMeshObjectAccessSpecification(
                    this,
                    new NetMeshBaseAccessSpecification[] {
                            theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName ),
                    },
                    theMeshObjectIdentifierFactory.fromExternalForm( oneElementName, null ) );

        } catch( StringRepresentationParseException ex ) {
            log.error( ex );
            return null;
        }
    }

    /**
     * Factory method to obtain a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * requesting a non-default NetMeshObject.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param remoteIdentifier Identifier of the remote non-default NetMeshObject
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier    oneElementName,
            NetMeshObjectIdentifier  remoteIdentifier )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                this,
                new NetMeshBaseAccessSpecification[] {
                        theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName ) },
                remoteIdentifier );
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
        try {
            return new DefaultNetMeshObjectAccessSpecification(
                    this,
                    new NetMeshBaseAccessSpecification[] {
                            theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName, scope ) },
                    theMeshObjectIdentifierFactory.fromExternalForm( oneElementName, null ));

        } catch( StringRepresentationParseException ex ) {
            log.error( ex );
            return null;
        }
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
        try {
            return new DefaultNetMeshObjectAccessSpecification(
                    this,
                    new NetMeshBaseAccessSpecification[] {
                            theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName, coherence ) },
                    theMeshObjectIdentifierFactory.fromExternalForm( oneElementName, null ));

        } catch( StringRepresentationParseException ex ) {
            log.error( ex );
            return null;
        }
    }

    /**
     * Factory method to obtain a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * requesting a non-default NetMeshObject,
     * specifying a non-default ScopeSpecification.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param remoteIdentifier Identifier of the remote non-default NetMeshObject
     * @param scope the ScopeSpecification
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier   oneElementName,
            NetMeshObjectIdentifier remoteIdentifier,
            ScopeSpecification      scope )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                this,
                new NetMeshBaseAccessSpecification[] {
                        theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName, scope ) },
                remoteIdentifier );
    }

    /**
     * Factory method to obtain a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * requesting a non-default MeshObject,
     * specifying a non-default CoherenceSpecification.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param remoteIdentifier Identifier of the remote non-default MeshObject
     * @param coherence the CoherenceSpecification
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier   oneElementName,
            NetMeshObjectIdentifier remoteIdentifier,
            CoherenceSpecification  coherence )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                this,
                new NetMeshBaseAccessSpecification[] {
                        theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName, coherence ) },
                remoteIdentifier );
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
        try {
            return new DefaultNetMeshObjectAccessSpecification(
                    this,
                    new NetMeshBaseAccessSpecification[] {
                            theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName, scope, coherence ) },
                    theMeshObjectIdentifierFactory.fromExternalForm( oneElementName, null ));

        } catch( StringRepresentationParseException ex ) {
            log.error( ex );
            return null;
        }
    }

    /**
     * Factory method to obtain a single-element NetMeshObjectAccessSpecification from a NetMeshBaseIdentifier,
     * requesting a non-default MeshObject,
     * specifying a non-default ScopeSpecification and a non-default CoherenceSpecification.
     * 
     * @param oneElementName the NetMeshBaseIdentifier
     * @param remoteIdentifier Identifier of the remote non-default MeshObject
     * @param scope the ScopeSpecification
     * @param coherence the CoherenceSpecification
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier   oneElementName,
            NetMeshObjectIdentifier remoteIdentifier,
            ScopeSpecification      scope,
            CoherenceSpecification  coherence )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                this,
                new NetMeshBaseAccessSpecification[] {
                        theNetMeshBaseAccessSpecificationFactory.obtain( oneElementName, scope, coherence ) },
                remoteIdentifier );
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
        try {
            return new DefaultNetMeshObjectAccessSpecification(
                    this,
                    elements,
                    theMeshObjectIdentifierFactory.fromExternalForm( elements[ elements.length-1 ].getNetMeshBaseIdentifier(), null ));

        } catch( StringRepresentationParseException ex ) {
            log.error( ex );
            return null;
        }
    }

    /**
     * Factory method to obtain a multi-element NetMeshObjectAccessSpecification from
     * a sequence of NetMeshBaseAccessSpecifications,
     * requesting a non-default MeshObject.
     * 
     * @param elements the NetMeshBaseAccessSpecifications, in sequence
     * @param remoteIdentifier Identifier of the remote non-default MeshObject
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseAccessSpecification [] elements,
            NetMeshObjectIdentifier           remoteIdentifier )
    {
        return new DefaultNetMeshObjectAccessSpecification(
                this,
                elements,
                remoteIdentifier );
    }

    /**
     * Factory method to obtain a multi-element NetMeshObjectAccessSpecification
     * from a series of NetMeshBaseIdentifiers.
     * 
     * @param elements the NetMeshBaseIdentifiers, in sequence
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier [] elements )
    {
        try {
            return new DefaultNetMeshObjectAccessSpecification(
                    this,
                    createSeveral( elements ),
                    theMeshObjectIdentifierFactory.fromExternalForm( elements[ elements.length-1 ], null ));

        } catch( StringRepresentationParseException ex ) {
            log.error( ex );
            return null;
        }
    }

    /**
     * Factory method to obtain a multi-element NetMeshObjectAccessSpecification from
     * a series of NetMeshBaseIdentifiers,
     * requesting a non-default MeshObject.
     * 
     * @param elements the NetMeshBaseIdentifiers, in sequence
     * @param remoteIdentifier Identifier of the remote non-default MeshObject
     * @return created NetMeshObjectAccessSpecification
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            NetMeshBaseIdentifier [] elements,
            NetMeshObjectIdentifier  remoteIdentifier )
    {
        if( remoteIdentifier == null ) {
            return obtain( elements );

        } else if( elements == null || elements.length == 0 ) {
            return obtainToLocalObject( remoteIdentifier );

        } else {
            return new DefaultNetMeshObjectAccessSpecification(
                    this,
                    createSeveral( elements ),
                    remoteIdentifier );
        }
    }
    
    /**
     * Convenience factory method to obtain a single-element NetMeshObjectAccessSpecification
     * by converting a URI into a NetMeshBaseIdentifier, requesting its home MeshObject.
     * 
     * @param remoteLocation the URI
     * @return created NetMeshObjectAccessSpecification
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            URI remoteLocation )
        throws
            StringRepresentationParseException
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
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            URI                    remoteLocation,
            CoherenceSpecification coherence )
        throws
            StringRepresentationParseException
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
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            URI                remoteLocation,
            ScopeSpecification scope )
        throws
            StringRepresentationParseException
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
     * @throws StringRepresentationParseException thrown if the syntax could not be parsed
     */
    public DefaultNetMeshObjectAccessSpecification obtain(
            URI                    remoteLocation,
            ScopeSpecification     scope,
            CoherenceSpecification coherence )
        throws
            StringRepresentationParseException
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
                    NetMeshBaseAccessSpecification.DEFAULT_COHERENCE );
        }
        return ret;
    }

    /**
     * Convert a String into a NetMeshObjectAccessSpecification.
     *
     * @param raw the String
     * @return the created NetMeshObjectAccessSpecification
     * @throws StringRepresentationParseException thrown if the String could not be parsed
     */
    public NetMeshObjectAccessSpecification fromExternalForm(
            String raw )
        throws
            StringRepresentationParseException
    {
        if( raw == null ) {
            return null;
        }

        // hash means it's the MeshObjectIdentifier's local part, so ignore that for a while.
        int    hash = raw.indexOf( '#' );
        String core = hash >= 0 ? raw.substring( 0, hash ) : raw;

        String lastSegment;
        NetMeshBaseAccessSpecification [] pathElements;
        if( core != null && core.length() > 0 ) {
            String [] pathElementStrings = core.split( "!" );

            pathElements = new NetMeshBaseAccessSpecification[ pathElementStrings.length-1 ]; // treat last one different
            for( int i=0 ; i<pathElements.length ; ++i ) {
                pathElements[i] = theNetMeshBaseAccessSpecificationFactory.fromExternalForm( pathElementStrings[i] );
            }
            lastSegment = pathElementStrings[ pathElementStrings.length-1 ];

        } else {
            pathElements = new NetMeshBaseAccessSpecification[0];
            lastSegment  = null;
        }

        if( lastSegment != null ) {
            if( hash >= 0 ) {
                lastSegment += raw.substring( hash );
            }
        } else {
            lastSegment = raw.substring( hash );
        }

        NetMeshObjectIdentifier object = theMeshObjectIdentifierFactory.fromExternalForm( lastSegment );

        NetMeshObjectAccessSpecification ret = new DefaultNetMeshObjectAccessSpecification(
                this,
                pathElements,
                object );
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
