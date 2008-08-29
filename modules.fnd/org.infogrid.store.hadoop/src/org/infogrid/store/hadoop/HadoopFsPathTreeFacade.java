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

package org.infogrid.store.hadoop;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.infogrid.util.logging.Log;
import org.infogrid.util.tree.TreeFacade;
import org.infogrid.util.tree.TreeFacadeCursorIterator;

/**
 * Implements <code>TreeFacade</code> for files stored by Hadoop.
 */
public class HadoopFsPathTreeFacade
        implements
            TreeFacade<Path>
{
    private static final Log log = Log.getLogInstance( HadoopFsPathTreeFacade.class); // our own, private logger

    /**
     * Factory method.
     * 
     * @param fs the Hadoop FileSystem
     * @param top the top-most file
     * @return the created HadoopFsPathTreeFacade
     */
    public static HadoopFsPathTreeFacade create(
            FileSystem fs,
            Path       top )
    {
        return new HadoopFsPathTreeFacade( fs, top );
    }

    /**
     * Private constructor, for subclasses only.
     * 
     * @param fs the Hadoop FileSystem
     * @param top the top-most file
     */
    protected HadoopFsPathTreeFacade(
            FileSystem fs,
            Path       top )
    {
        theFileSystem = fs;
        
        if( top != null ) {
            theTop = top.makeQualified( theFileSystem );
        } else {
            theTop = top;
        }
    }

    /**
     * Obtain the Hadoop FileSystem in which the data is stored.
     * 
     * @return the FileSystem
     */
    public FileSystem getFileSystem()
    {
        return theFileSystem;
    }

    /**
     * Determine the top node of the tree.
     * 
     * @return the top node
     */
    public Path getTopNode()
    {
        return theTop;
    }
    
    /**
     * Determine whether the provided node has child nodes.
     * 
     * @param node the node
     * @return true if the node has children
     */
    public boolean hasChildNodes(
            Path node )
    {
        try {
            if( !theFileSystem.getFileStatus( node ).isDir() ) {
                return false;
            }
            int length = theFileSystem.globStatus( node ).length;
            return length > 0;

        } catch( FileNotFoundException ex ) {
            // ignore, happens if the store is empty and unitialized
        } catch( IOException ex ) {
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
    public Path [] getChildNodes(
            Path node )
    {
        try {
            if( !theFileSystem.getFileStatus( node ).isDir() ) {
                return new Path[0];
            }
            FileStatus [] almost = theFileSystem.globStatus( new Path( node.toString() + "/*" ));

            Path [] ret = new Path[ almost.length ];
            for( int i=0 ; i<ret.length ; ++i ) {
                ret[i] = almost[i].getPath();
            }
            return ret;

        } catch( IOException ex ) {
            log.error( ex );
            return new Path[0];
        }
    }
    
    /**
     * Obtain the parent node of the provided node. This returns null for the top
     * node.
     * 
     * @param node the node
     * @return the parent node, or null
     */
    public Path getParentNode(
            Path node )
    {
        node = node.makeQualified( theFileSystem );
        
        if( theTop.equals( node )) {
            return null;
        } else {
            // need our own implementation of File.getParentFile();
            
            String nodeString = node.toString();
            int slash = nodeString.lastIndexOf( '/' );
            if( slash <= 0 ) {
                return null;
            }
            String parentString = nodeString.substring( 0, slash );
            return new Path( parentString );
        }
    }
    
    /**
     * Obtain the "forward" sibling of the provided node.
     * 
     * @param node the node
     * @return the forward node, or null if none
     * @see #getBackwardSiblingNode
     */
    public Path getForwardSiblingNode(
            Path node )
    {
        // This algorithm also works if the given node doesn't exist.
        
        Path qualifiedNode = node.makeQualified( theFileSystem );
        
        if( theTop.equals( qualifiedNode )) {
            return null;
        }
        Path parent = getParentNode( qualifiedNode );
        if( parent == null ) {
            return null;
        }
        try {
            FileStatus [] siblings = theFileSystem.globStatus( new Path( parent.toString() + "/*" ));

            // we can't make any assumptions about order
            Path best = null;
            for( int i=0 ; i<siblings.length ; ++i ) {
                if( qualifiedNode.compareTo( siblings[i].getPath() ) < 0 ) {
                    // is to the right, good
                    if( best == null || best.compareTo( siblings[i].getPath() ) > 0 ) {
                        // this one is better
                        best = siblings[i].getPath();
                    }
                }
            }
            return best;

        } catch( IOException ex ) {
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
    public Path getBackwardSiblingNode(
            Path node )
    {
        // This algorithm also works if the given node doesn't exist.
        
        Path qualifiedNode = node.makeQualified( theFileSystem );

        if( theTop.equals( qualifiedNode )) {
            return null;
        }
        Path parent = getParentNode( qualifiedNode );
        if( parent == null ) {
            return null;
        }
        try {
            FileStatus [] siblings = theFileSystem.globStatus( new Path( parent.toString() + "/*" ));

            // we can't make any assumptions about order
            Path best = null;
            for( int i=0 ; i<siblings.length ; ++i ) {
                if( qualifiedNode.compareTo( siblings[i].getPath() ) > 0 ) {
                    // is to the left, good
                    if( best == null || best.compareTo( siblings[i].getPath() ) < 0 ) {
                        // this one is better
                        best = siblings[i].getPath();
                    }
                }
            }
            return best;

        } catch( IOException ex ) {
            log.error( ex );
            return null;
        }
    }
    
    /**
     * Obtain a CursorIterator over all nodes in this tree.
     * 
     * @return the CursorIterator
     */
    public TreeFacadeCursorIterator<Path> iterator()
    {
        return TreeFacadeCursorIterator.create( this, Path.class );
    }
    
    /**
     * The Hadoop FileSystem.
     */
    protected FileSystem theFileSystem;

    /**
     * The top node.
     */
    protected Path theTop;
}
