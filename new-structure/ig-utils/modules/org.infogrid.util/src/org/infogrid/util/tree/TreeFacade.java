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

import org.infogrid.util.CursorIterator;

/**
 * A Facade for Trees. It would be nice if the JDK had a general-purpose Tree interface,
 * but it does not. This interface makes it easy to hide tree-like data structures
 * behind it, and enables the use of the same CursorIterator on it.
 * 
 * @param <T> the type of a node in the tree
 */
public interface TreeFacade<T>
{
    /**
     * Determine the top node of the tree.
     * 
     * @return the top node
     */
    public T getTopNode();
    
    /**
     * Determine whether the provided node has child nodes.
     * 
     * @param node the node
     * @return true if the node has children
     */
    public boolean hasChildNodes(
            T node );
    
    /**
     * Obtain the child nodes of a provided node.
     * 
     * @param node the node
     * @return the child nodes
     */
    public T [] getChildNodes(
            T node );
    
    /**
     * Obtain the parent node of the provided node. This returns null for the top
     * node.
     * 
     * @param node the node
     * @return the parent node, or null
     */
    public T getParentNode(
            T node );
    
    /**
     * Obtain the "forward" sibling of the provided node.
     * 
     * @param node the node
     * @return the forward node, or null if none
     */
    public T getForwardSiblingNode(
            T node );
    
    /**
     * Obtain the "backward" sibling of the provided node.
     * 
     * @param node the node
     * @return the backward node, or null if none
     */
    public T getBackwardSiblingNode(
            T node );
    
    /**
     * Obtain a CursorIterator over all nodes in this tree.
     * 
     * @return the CursorIterator
     */
    public CursorIterator<T> iterator();
}
