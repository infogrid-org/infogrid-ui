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

package org.infogrid.util.tree;

import java.io.File;
import java.util.Arrays;

/**
 * Implements {@link TreeFacade} for files in the local file system.
 */
public class FileTreeFacade
        implements
            TreeFacade<File>
{
    /**
     * Factory method.
     * 
     * @param top the top-most file
     * @return the created FileTreeFacade
     */
    public static FileTreeFacade create(
            File top )
    {
        return new FileTreeFacade( top );
    }

    /**
     * Private constructor, for subclasses only.
     * 
     * @param top the top-most file
     */
    protected FileTreeFacade(
            File top )
    {
        theTop = top;
    }

    /**
     * Determine the top node of the tree.
     * 
     * @return the top node
     */
    public File getTopNode()
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
            File node )
    {
        if( !node.isDirectory() ) {
            return false;
        }
        int length = node.list().length;
        return length > 0;
    }
    
    /**
     * Obtain the child nodes of a provided node.
     * 
     * @param node the node
     * @return the child nodes
     */
    public File [] getChildNodes(
            File node )
    {
        if( !node.isDirectory() ) {
            return new File[0];
        }
        File [] ret = node.listFiles();
        
        // need to order them in the same sequence as defined by forward and back operations
        Arrays.sort( ret );
        return ret;
    }
    
    /**
     * Obtain the parent node of the provided node. This returns null for the top
     * node.
     * 
     * @param node the node
     * @return the parent node, or null
     */
    public File getParentNode(
            File node )
    {
        if( theTop.equals( node )) {
            return null;
        } else {
            return node.getParentFile();
        }
    }
    
    /**
     * Obtain the "forward" sibling of the provided node.
     * 
     * @param node the node
     * @return the forward node, or null if none
     * @see #getBackwardSiblingNode
     */
    public File getForwardSiblingNode(
            File node )
    {
        // This algorithm also works if the given node doesn't exist.
        
        if( theTop.equals( node )) {
            return null;
        }
        File    parent   = node.getParentFile();
        File [] siblings = parent.listFiles();
        
        // we can't make any assumptions about order
        File best = null;
        for( int i=0 ; i<siblings.length ; ++i ) {
            if( node.compareTo( siblings[i] ) < 0 ) {
                // is to the right, good
                if( best == null || best.compareTo(  siblings[i] ) > 0 ) {
                    // this one is better
                    best = siblings[i];
                }
            }
        }
        return best;
    }
    
    /**
     * Obtain the "backward" sibling of the provided node.
     * 
     * @param node the node
     * @return the backward node, or null if none
     * @see #getForwardSiblingNode
     */
    public File getBackwardSiblingNode(
            File node )
    {
        // This algorithm also works if the given node doesn't exist.
        
        if( theTop.equals( node )) {
            return null;
        }
        File    parent   = node.getParentFile();
        File [] siblings = parent.listFiles();
        
        // we can't make any assumptions about order
        File best = null;
        for( int i=0 ; i<siblings.length ; ++i ) {
            if( node.compareTo( siblings[i] ) > 0 ) {
                // is to the left, good
                if( best == null || best.compareTo(  siblings[i] ) < 0 ) {
                    // this one is better
                    best = siblings[i];
                }
            }
        }
        return best;
    }
    
    /**
     * Obtain a CursorIterator over all nodes in this tree.
     * 
     * @return the CursorIterator
     */
    public TreeFacadeCursorIterator<File> iterator()
    {
        return TreeFacadeCursorIterator.create( this, File.class );
    }
    
    /**
     * The top node.
     */
    protected File theTop;
}
