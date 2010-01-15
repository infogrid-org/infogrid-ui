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
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.Feeds.FeedsSubjectArea;
import org.infogrid.module.ModuleException;
import org.infogrid.probe.ProbeException;
import org.infogrid.probe.StagingMeshBase;
import org.infogrid.probe.feeds.AbstractFeedProbe;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentationParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * A Probe for the RSS file format, with optional InfoGrid extensions.
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
     * <p>Read from the DOM and instantiate corresponding MeshObjects.</p>
     * <p>This method declares
     * many different types of Exceptions; that enables the Probe Framework to handle many
     * possible error conditions out of the box, thereby making Probe programming easier.
     * Note that many of the declared Exceptions, if actually thrown, indicate a programming
     * error in the Probe implementation (e.g. IsAbstractException).</p>
     * <p>The Probe framework invokes this method with an open Transaction on the current Thread;
     * the Probe developer does not have to worry about Transactions.</p>
     * 
     * @param dataSourceIdentifier identifies the data source that is being accessed
     * @param coherenceSpecification the type of data coherence that is requested by the application. Probe
     *         implementors may ignore this parameter, letting the Probe framework choose its own policy.
     *         If the Probe chooses to define its own policy (considering or ignoring this parameter), the
     *         Probe must bless the Probe's HomeObject with a subtype of <code>ProbeUpdateSpecification</code> (defined
     *         in the <code>org.infogrid.model.Probe</code> Subject Area) and suitable Property
     *         values that reflect the policy.
     * @param theDocument the DOM document to be interpreted
     * @param freshMeshBase the StagingMeshBase in which the corresponding MeshObjects are to be instantiated by the Probe.
     *         This StagingMeshBase is empty when passed into this call, except for the home object which always exists
     * @throws EntityBlessedAlreadyException thrown if a MeshObject was incorrectly blessed twice with the same
     *         EntityType. Throwing this typically indicates a programming error.
     * @throws EntityNotBlessedException thrown if a MeshObject was not blessed with a required EntityType.
     *         Throwing this typically indicates a programming error.
     * @throws IllegalPropertyTypeException thrown if a MeshObject did not carry a PropertyType that it needed
     *         to carry. Throwing this typically indicates a programming error.
     * @throws IllegalPropertyValueException thrown if a PropertyValue was assigned to a property that was
     *         outside of the allowed range. Throwing this typically indicates a programming error.
     * @throws IOException an input/output error occurred during execution of the Probe
     * @throws IsAbstractException thrown if an EntityType or a Relationship could not be instantiated because
     *         it was abstract. Throwing this typically indicates a programming error.
     * @throws MeshObjectIdentifierNotUniqueException thrown if the Probe developer incorrectly
     *         assigned duplicate MeshObjectsIdentifiers to created MeshObjects.
     *         Throwing this typically indicates a programming error.
     * @throws ModuleException thrown if a Module required by the Probe could not be loaded
     * @throws NotPermittedException thrown if an operation performed by the Probe was not permitted
     * @throws NotRelatedException thrown if a relationship was supposed to become blessed, but the relationship
     *         did not exist. Throwing this typically indicates a programming error.
     * @throws ProbeException a Probe error occurred per the possible subclasses defined in ProbeException
     * @throws RelatedAlreadyException thrown if the Probe developer incorrectly attempted to
     *         relate two already-related MeshObjects. Throwing this typically indicates a programming error.
     * @throws RoleTypeBlessedAlreadyException thrown if a relationship was incorrectly blessed twice with the same
     *         RelationshipType, in the same direction. Throwing this typically indicates a programming error.
     * @throws TransactionException a Transaction problem occurred. Throwing this typically indicates a programming error.
     * @throws URISyntaxException thrown if a URI was constructed in an invalid way
     * @throws StringRepresentationParseException a StringRepresentation could not be parsed
     */
    public void parseDocument(
            NetMeshBaseIdentifier  dataSourceIdentifier,
            CoherenceSpecification coherenceSpecification,
            Document               theDocument,
            StagingMeshBase        freshMeshBase )
        throws
            EntityBlessedAlreadyException,
            EntityNotBlessedException,
            IllegalPropertyTypeException,
            IllegalPropertyValueException,
            IOException,
            IsAbstractException,
            MeshObjectIdentifierNotUniqueException,
            ModuleException,
            NotPermittedException,
            NotRelatedException,
            ProbeException,
            RelatedAlreadyException,
            RoleTypeBlessedAlreadyException,
            TransactionException,
            URISyntaxException,
            StringRepresentationParseException
    {
        Element rssNode = theDocument.getDocumentElement();
        if ( !"rss".equals( rssNode.getLocalName())) {
            throw new ProbeException.SyntaxError( dataSourceIdentifier, "Not an RSS file", null );
        }

        if (    !"0.91".equals( rssNode.getAttribute( "version" ))
             && !"2.0".equals( rssNode.getAttribute( "version" )) )
        {
            log.warn( "RssProbe.parseDocument() failed version check, continuing anyway" );
        }

        NetMeshObject home = freshMeshBase.getHomeObject();
        home.bless( FeedsSubjectArea.RSSFEED ); // this is an RSS feed

        NodeList channelNodes = rssNode.getElementsByTagName( "channel" );
        
        for ( int i=0 ; i<channelNodes.getLength() ; i++ ) {
            Node channelNode = channelNodes.item( i );

            if( !( channelNode instanceof Element )) {
                continue;
            }
            
            Element realChannelNode = (Element) channelNode;

            handleInfoGridFeedExtensions( dataSourceIdentifier, theDocument, realChannelNode, home );

            String channelTitleTitle  = getChildNodeValue( realChannelNode, "title" );
            String channelDescription = getChildNodeValue( realChannelNode, "description" );

            home.setPropertyValue( FeedsSubjectArea.FEED_TITLE,       FeedsSubjectArea.FEED_TITLE_type.createBlobValueOrNull(       channelTitleTitle,       "text/plain" ));
            home.setPropertyValue( FeedsSubjectArea.FEED_DESCRIPTION, FeedsSubjectArea.FEED_DESCRIPTION_type.createBlobValueOrNull( channelDescription, "text/plain" ));
            
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
                itemGuid = ensureLocalGuid( itemGuid );

                NetMeshObject item = createExtendedInfoGridFeedEntryObject(
                        dataSourceIdentifier,
                        theDocument,
                        realItemNode,
                        freshMeshBase.getMeshObjectIdentifierFactory().fromExternalForm( itemGuid ),
                        FeedsSubjectArea.RSSFEEDITEM,
                        freshMeshBase );
                
                item.setPropertyValue( FeedsSubjectArea.FEEDITEM_TITLE,   FeedsSubjectArea.FEEDITEM_TITLE_type.createBlobValueOrNull(   itemTitle,       "text/plain" ));
                item.setPropertyValue( FeedsSubjectArea.FEEDITEM_CONTENT, FeedsSubjectArea.FEEDITEM_CONTENT_type.createBlobValueOrNull( itemDescription, "text/plain" ));

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
