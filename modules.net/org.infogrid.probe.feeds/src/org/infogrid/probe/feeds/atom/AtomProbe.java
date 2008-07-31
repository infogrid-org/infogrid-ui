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

package org.infogrid.probe.feeds.atom;

import java.io.IOException;
import java.net.URISyntaxException;
import org.infogrid.mesh.EntityBlessedAlreadyException;
import org.infogrid.mesh.EntityNotBlessedException;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.IllegalPropertyValueException;
import org.infogrid.mesh.IsAbstractException;
import org.infogrid.mesh.MeshObjectIdentifierNotUniqueException;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.mesh.RelatedAlreadyException;
import org.infogrid.mesh.RoleTypeBlessedAlreadyException;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.net.CoherenceSpecification;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.NetMeshBaseLifecycleManager;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.BlobValue;
import org.infogrid.model.Feeds.FeedsSubjectArea;
import org.infogrid.module.ModuleException;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.feeds.AbstractFeedProbe;
import org.infogrid.util.logging.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 */
public class AtomProbe
        extends
            AbstractFeedProbe
{
    private static final Log log = Log.getLogInstance( AtomProbe.class ); // our own, private logger
    
    /**
     * Constructor.
     */
    public AtomProbe()
    {
    }

    /**
     * Read from the DOM and instantiate corresponding MeshObjects.
     * 
     * 
     * 
     * @param networkId the NetMeshBaseIdentifier that is being accessed
     * @param mb the interface through which the Probe instantiates MeshObjects
     * @throws DoNotHaveLockException a Probe can declare to throw this Exception,
     *         which makes programming easier, but if it actually threw it, that would be a programming error
     * @throws IdeMeshObjectIdentifierNotUniqueExceptionobe throws this Exception, it indicates that the
     *         Probe developer incorrectly assigned duplicate Identifiers to created MeshObject
     * @throws RelationshipExistsAlreadyException if a Probe throws this Exception, it indicates that the
     *         Probe developer incorrectly attempted to create another RelationshipType instance between
     *         the same two Entities.
     * @throws TransactionException a Probe can declare to throw this Exception,
     *         which makes programming easier, but if it actually threw it, that would be a programming error
     * @throws ProbeException a Probe error occurred per the possible subclasses defined in ProbeException
     * @throws IOException an input/output error occurred during execution of the Probe
     */
    public void parseDocument(
            NetMeshBaseIdentifier      networkId,
            CoherenceSpecification coherence,
            Document               theDocument,
            StagingMeshBase        mb )
        throws
            IsAbstractException,
            EntityBlessedAlreadyException,
            EntityNotBlessedException,
            RelatedAlreadyException,
            RoleTypeBlessedAlreadyException,
            NotRelatedException,
            MeshObjectIdentifierNotUniqueException,
            IllegalPropertyTypeException,
            IllegalPropertyValueException,
            TransactionException,
            NotPermittedException,
            ProbeException,
            IOException,
            ModuleException,
            URISyntaxException
    {
        Element atomNode = theDocument.getDocumentElement();
        if ( atomNode.getLocalName() != "feed" ) {
            throw new ProbeException.SyntaxError( networkId, "Not an Atom file", null );
        }

        NetMeshObject home = mb.getHomeObject();
        home.bless( FeedsSubjectArea.ATOMFEED ); // this is an Atpm feed

        NetMeshBaseLifecycleManager life = mb.getMeshBaseLifecycleManager();
        
        String feedTitle       = getChildNodeValue( atomNode, "title" );
        String feedDescription = getChildNodeValue( atomNode, "description" );
        
        home.setPropertyValue( FeedsSubjectArea.FEED_TITLE,       BlobValue.createOrNull( feedTitle,       "text/plain" ));
        home.setPropertyValue( FeedsSubjectArea.FEED_DESCRIPTION, BlobValue.createOrNull( feedDescription, "text/plain" ));

        handleInfoGridFeedExtensions( networkId, theDocument, atomNode, home );
        
        NodeList entryNodes = atomNode.getElementsByTagNameNS( "http://purl.org/atom/ns#", "entry" );
        if( entryNodes.getLength() == 0 ) {
            // try without the name space, not all Atom will be well-formatted
            entryNodes = atomNode.getElementsByTagName( "entry" );
        }
        for ( int j=0 ; j<entryNodes.getLength() ; j++ ) {
            Node entryNode = entryNodes.item( j );

            if( !( entryNode instanceof Element )) {
                continue;
            }

            Element realItemNode = (Element) entryNode;

            String entryGuid        = getChildNodeValue(     realItemNode, "id" );
            String entryTitle       = getChildNodeValue(     realItemNode, "title" );
            String entryContent     = getChildNodeValue(     realItemNode, "content" );
            String entryContentMime = getChildNodeAttribute( realItemNode, "content", "type" );

            if( entryGuid == null ) {
                entryGuid = String.valueOf( j ); // FIXME? Is this a good default?
            }
            if( entryContentMime == null || entryContentMime.length() == 0 ) {
                entryContentMime = "text/plain";
            } else if( "text".equals( entryContentMime )) {
                entryContentMime = "text/plain";
            } else if( "html".equals( entryContentMime )) {
                entryContentMime = "text/html";
            } else if( "xhtml".equals( entryContentMime )) {
                entryContentMime = "application/xhtml+xml";
            }
            
            NetMeshObject item = createExtendedInfoGridFeedEntryObject(
                    networkId,
                    theDocument,
                    realItemNode,
                    mb.getMeshObjectIdentifierFactory().fromExternalForm( entryGuid ),
                    FeedsSubjectArea.ATOMFEEDITEM,
                    mb );

            item.setPropertyValue( FeedsSubjectArea.FEEDITEM_TITLE,   BlobValue.createOrNull( entryTitle,   "text/plain" ));
            item.setPropertyValue( FeedsSubjectArea.FEEDITEM_CONTENT, BlobValue.createOrNull( entryContent, entryContentMime ));
            
            try {
                home.relate( item );
            } catch( RelatedAlreadyException ex ) {
                // ignore
                if( log.isDebugEnabled() ) {
                    log.info( ex );
                }
            }
            try {
                home.blessRelationship( FeedsSubjectArea.FEED_CONTAINS_FEEDITEM.getSource(), item );
            } catch( RoleTypeBlessedAlreadyException ex ) {
                // ignore
                if( log.isDebugEnabled() ) {
                    log.info( ex );
                }
            }
        }
    }
}
