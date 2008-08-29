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

package org.infogrid.store.jets3t;

import org.infogrid.util.logging.Log;
import org.infogrid.util.tree.TreeFacade;
import org.infogrid.util.tree.TreeFacadeCursorIterator;
import org.jets3t.service.S3ObjectsChunk;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;

/**
 * Implements {@link TreeFacade} for files stored in S3.
 */
public class JetS3tTreeFacade
        implements
            TreeFacade<S3Object>
{
    private static final Log log = Log.getLogInstance( JetS3tTreeFacade.class); // our own, private logger

    /**
     * Factory method.
     * 
     * @param store the JetS3tStore
     * @return the created JetS3tTreeFacade
     */
    public static JetS3tTreeFacade create(
            JetS3tStore store )
    {
        return new JetS3tTreeFacade(
                store.getService(),
                store.getBucket(),
                store.getPrefix(),
                store.getDelimiter() );
    }

    /**
     * Private constructor, for subclasses only.
     * 
     * @param service the S3 service to use
     * @param bucket the S3 bucket to use
     * @param prefix the S3 key prefix identifying the directory
     * @param delimiter the S3 key path separator
     */
    protected JetS3tTreeFacade(
            S3Service service,
            S3Bucket  bucket,
            String    prefix,
            String    delimiter )
    {
        theService   = service;
        theBucket    = bucket;
        thePrefix    = prefix;
        theDelimiter = delimiter;
    }

    /**
     * Obtain the S3 Service.
     * 
     * @return the S3 Service
     */
    public S3Service getService()
    {
        return theService;
    }

    /**
     * Obtain the S3 Bucket in which the data is stored.
     * 
     * @return the Bucket
     */
    public S3Bucket getS3Bucket()
    {
        return theBucket;
    }

    /**
     * Obtain the S3 key prefix identifying the directory underneath which all data is stored.
     * 
     * @return the top-level directory
     */
    public String getPrefix()
    {
        return thePrefix;
    }

    /**
     * Obtain the S3 key path separator.
     * 
     * @return the S3 key path separator
     */
    public String getDelimiter()
    {
        return theDelimiter;
    }

    /**
     * Determine the top node of the tree.
     * 
     * @return the top node
     */
    public S3Object getTopNode()
    {
        try {
            return theService.getObject( theBucket, thePrefix );

        } catch( S3ServiceException ex ) {
            log.error( ex );
            return null;
        }
    }

    /**
     * Determine whether the provided node has child nodes.
     * 
     * @param node the node
     * @return true if the node has children
     */
    public boolean hasChildNodes(
            S3Object node )
    {
        try {
            int length = theService.listObjects( theBucket, theDelimiter, thePrefix ).length;
            return length > 0;

        } catch( S3ServiceException ex ) {
            log.error( ex );
        }
        return false;
    }
    
    /**
     * Obtain the child nodes of a provided node.
     * 
     * @param node the node
     * @return the child nodes
     */
    public S3Object [] getChildNodes(
            S3Object node )
    {
        try {
            S3Object [] ret = theService.listObjects( theBucket, theDelimiter, thePrefix );

            return ret;

        } catch( S3ServiceException ex ) {
            log.error( ex );
            return new S3Object[0];
        }
    }
    
    /**
     * Obtain the parent node of the provided node. This returns null for the top
     * node.
     * 
     * @param node the node
     * @return the parent node, or null
     */
    public S3Object getParentNode(
            S3Object node )
    {
        String key = node.getKey();
        
        if( thePrefix.equals( key )) {
            return null;

        } else {
            // need our own implementation of File.getParentFile();
            
            int slash = key.lastIndexOf( theDelimiter );
            if( slash <= 0 ) {
                return null;
            }
            String parentString = key.substring( 0, slash+1 );

            try {
                return theService.getObject( theBucket, parentString );

            } catch( S3ServiceException ex ) {
                log.error( ex );
            }
            return null;
        }
    }
    
    /**
     * Obtain the "forward" sibling of the provided node.
     * 
     * @param node the node
     * @return the forward node, or null if none
     * @see #getBackwardSiblingNode
     */
    public S3Object getForwardSiblingNode(
            S3Object node )
    {
        S3Object parent = getParentNode( node );
        if( parent == null ) {
            return null;
        }
        
        try {
            S3ObjectsChunk chunk = theService.listObjectsChunked(
                    theBucket.getName(),
                    thePrefix,
                    theDelimiter,
                    1,
                    node.getKey());

            S3Object [] objects = chunk.getObjects();
            if( objects.length == 0 ) {
                return null;
            } else {
                return objects[1];
            }
        } catch( S3ServiceException ex ) {
            log.error( ex );
            return null;
        }
    }
    
    /**
     * Obtain the "backward" sibling of the provided node.
     * 
     * @param node the node
     * @return the backward node, or null if none
     * @see #getForwardSiblingNode
     */
    public S3Object getBackwardSiblingNode(
            S3Object node )
    {
        S3Object parent = getParentNode( node );
        if( parent == null ) {
            return null;
        }
        
        try {
            S3Object [] objects = theService.listObjects(
                    theBucket,
                    thePrefix,
                    theDelimiter );
            for( int i=0 ; i<objects.length ; ++i ) {
                if( objects[i].equals( node )) {
                    // found, take the one before it.
                    if( i==0 ) {
                        return null;
                    } else {
                        return objects[i-1];
                    }
                }
            }
        } catch( S3ServiceException ex ) {
            log.error( ex );
        }
        return null;
    }
    
    /**
     * Obtain a CursorIterator over all nodes in this tree.
     * 
     * @return the CursorIterator
     */
    public TreeFacadeCursorIterator<S3Object> iterator()
    {
        return TreeFacadeCursorIterator.create( this, S3Object.class );
    }
    
    /**
     * The S3 Service.
     */
    protected S3Service theService;

    /**
     * The S3 Bucket.
     */
    protected S3Bucket theBucket;

    /**
     * The prefix.
     */
    protected String thePrefix;

    /**
     * The path separator.
     */
    protected String theDelimiter;
}
