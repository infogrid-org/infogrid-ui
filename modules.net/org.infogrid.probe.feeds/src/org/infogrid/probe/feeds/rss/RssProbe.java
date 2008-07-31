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

package org.infogrid.probe.feeds.rss;

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
 * A Probe that parses RSS feeds.
 */
public class RssProbe
        extends
            AbstractFeedProbe
{
    private static final Log log = Log.getLogInstance( RssProbe.class ); // our own, private logger
    
    /**
     * Constructor.
     */
    public RssProbe()
    {
    }

    /**
     * Read from the DOM and instantiate corresponding MeshObjects.
     * 
     * 
     * 
     * @param networkId the NetMeshBaseIdentifier that is being accessed
     * @param theInputStream the InputStream to read from
     * @param theContentType the content type (MIME) if known
     * @param theFacade the interface through which the Probe instantiates MeshObjects
     * @throws DoNotHaveLockException a Probe can declare to throw this Exception,
     *         which makes programming easier, but if it actually threw it, that would be a programming error
     * @throws IdentMeshObjectIdentifierNotUniqueExceptionws this Exception, it indicates that the
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
            NotRelatedException,
            RoleTypeBlessedAlreadyException,
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
        Element rssNode = theDocument.getDocumentElement();
        if ( rssNode.getLocalName() != "rss" ) {
            throw new ProbeException.SyntaxError( networkId, "Not an RSS file", null );
        }

        if ( !"0.91".equals( rssNode.getAttribute( "version" ) ) &&
             !"2.0".equals( rssNode.getAttribute( "version" ) ) )
        {
            log.warn( "RssProbe.parseDocument() failed version check, continuing anyway" );
        }

        NetMeshObject home = mb.getHomeObject();
        home.bless( FeedsSubjectArea.RSSFEED ); // this is an RSS feed

        NetMeshBaseLifecycleManager life = mb.getMeshBaseLifecycleManager();
        
        NodeList channelNodes = rssNode.getElementsByTagName( "channel" );
        
        for ( int i=0 ; i<channelNodes.getLength() ; i++ ) {
            Node channelNode = channelNodes.item( i );

            if( !( channelNode instanceof Element )) {
                continue;
            }
            
            Element realChannelNode = (Element) channelNode;

            handleInfoGridFeedExtensions( networkId, theDocument, realChannelNode, home );

            String channelTitleTitle  = getChildNodeValue( realChannelNode, "title" );
            String channelDescription = getChildNodeValue( realChannelNode, "description" );

            home.setPropertyValue( FeedsSubjectArea.FEED_TITLE,       BlobValue.createOrNull( channelTitleTitle,  "text/plain" ));
            home.setPropertyValue( FeedsSubjectArea.FEED_DESCRIPTION, BlobValue.createOrNull( channelDescription, "text/plain" ));
            
            NodeList itemNodes = realChannelNode.getElementsByTagName( "item" );
            for ( int j=0 ; j<itemNodes.getLength() ; j++ ) {
                Node itemNode = itemNodes.item( j );

                if( !( itemNode instanceof Element )) {
                    continue;
                }

                Element realItemNode = (Element) itemNode;

                String itemGuid        = getChildNodeValue(     realItemNode, "guid" );
                String itemTitle       = getChildNodeValue(     realItemNode, "title" );
                String itemDescription = getChildNodeValue(     realItemNode, "description" );
                
                if( itemGuid == null || itemGuid.length() == 0 ) {
                    itemGuid = String.valueOf( i ) + "-" + String.valueOf( j ); // FIXME? Is this a good default?
                }

                NetMeshObject item = createExtendedInfoGridFeedEntryObject(
                        networkId,
                        theDocument,
                        realItemNode,
                        mb.getMeshObjectIdentifierFactory().fromExternalForm( itemGuid ),
                        FeedsSubjectArea.RSSFEEDITEM,
                        mb );
                
                item.setPropertyValue( FeedsSubjectArea.FEEDITEM_TITLE,   BlobValue.createOrNull( itemTitle,       "text/plain" ));
                item.setPropertyValue( FeedsSubjectArea.FEEDITEM_CONTENT, BlobValue.createOrNull( itemDescription, "text/plain" ));

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
            break; // only do first channel in the feed for now
        }
    }
}
