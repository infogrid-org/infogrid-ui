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

import java.util.NoSuchElementException;
import org.infogrid.util.AbstractCursorIterator;
import org.infogrid.util.CursorIterator;

/**
 * <p>Knows how to walk a {@link TreeFacade}. This implements a depth-first algorithm
 * which returns nodes on the way down.</p>
 * 
 * <p>To include or exclude certain nodes, chain this
 *    CursorIterator with a {@link org.infogrid.util.FilteringCursorIterator}.</p>
 * 
 * @param <T> the type of tree node
 */
public class TreeFacadeCursorIterator<T>
        extends
            AbstractCursorIterator<T>
{
    /**
     * Factory method.
     * 
     * @param facade the tree to traverse
     * @param arrayComponentType the <code>Class</code> that should be used to create arrays for return values
     * @return the created TreeFacadeCursorIterator
     * @param <T> the type of tree node
     */
    public static <T> TreeFacadeCursorIterator<T> create(
            TreeFacade<T> facade,
            Class<T>      arrayComponentType )
    {
        TreeFacadeCursorIterator<T> ret = new TreeFacadeCursorIterator<T>( facade, facade.getTopNode(), arrayComponentType );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param facade the tree to traverse
     * @param current the start node
     * @param arrayComponentType the <code>Class</code> that should be used to create arrays for return values
     */
    protected TreeFacadeCursorIterator(
            TreeFacade<T> facade,
            T             current,
            Class<T>      arrayComponentType )
    {
        super( arrayComponentType );

        theFacade  = facade;
        theCurrent = current;
    }
    
    /**
     * Returns <tt>true</tt> if the iteration has at least N more elements in the forward direction.
     *
     * @param n the number of elements for which to check
     * @return <tt>true</tt> if the iterator has at least N more elements in the forward direction.
     * @see #hasNext()
     * @see #hasPrevious()
     * @see #hasPrevious(int)
     */
    public boolean hasNext(
            int n )
    {
        T here = theCurrent;
        for( int i=0 ; i<n ; ++i ) {
            if( here == null ) {
                return false;
            }
            try {
                T next = goForward( here );
                here = next;
            } catch( NoSuchElementException ex ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <tt>true</tt> if the iteration has at least N more elements in the backward direction.
     *
     * @param n the number of elements for which to check
     * @return <tt>true</tt> if the iterator has at least N more elements in the backward direction.
     * @see #hasNext()
     * @see #hasPrevious()
     * @see #hasNext(int)
     */
    public boolean hasPrevious(
            int n )
    {
        T here = theCurrent;
        for( int i=0 ; i<n ; ++i ) {
            try {
                T next = goBackward( here );
                if( next == null ) {
                    return false;
                }
                here = next;
            } catch( NoSuchElementException ex ) {
                return false;
            }
        }
        return true;
        
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     * @see #previous()
     */
    public T next()
        throws
            NoSuchElementException
    {
        T ret = theCurrent;
        theCurrent = goForward( theCurrent );
        return ret;
    }

    /**
     * Returns the previous element in the iteration.
     *
     * @return the previous element in the iteration.
     * @throws NoSuchElementException iteration has no more elements.
     * @see #next()
     */
    public T previous()
        throws
            NoSuchElementException
    {
        theCurrent = goBackward( theCurrent );
        return theCurrent;
    }

    /**
     * Clone this position.
     *
     * @return identical new instance
     */
    public TreeFacadeCursorIterator<T> createCopy()
    {
        return new TreeFacadeCursorIterator<T>( theFacade, theCurrent, theArrayComponentType );
    }
    
    /**
     * Set this CursorIterator to the position represented by the provided CursorIterator.
     *
     * @param position the position to set this CursorIterator to
     * @throws IllegalArgumentException thrown if the provided CursorIterator does
     *         not work on the same CursorIterable, or the implementations were incompatible.
     */
    public void setPositionTo(
            CursorIterator<T> position )
        throws
            IllegalArgumentException
    {
        if( position.hasNext() ) {
            T here = position.next();
            
            theCurrent = here;
        } else {
            theCurrent = null;
        }
    }

    /**
     * Given this current node, return the next node in the iteration.
     * 
     * @param start the current node
     * @return the next node
     * @throws NoSuchElementException iteration has no more elements.
     * @see #goBackward
     */
    protected T goForward(
            T start )
        throws
            NoSuchElementException
    {
        if( start == null ) {
            throw new NoSuchElementException();
        }
        
        // has child, so return child
        if( theFacade.hasChildNodes( start )) {
            T ret = theFacade.getChildNodes( start )[0];
            return ret;
        }
        
        // no child, but sibling, so return sibling
        T ret = theFacade.getForwardSiblingNode( start );
        if( ret != null ) {
            return ret;
        }
        
        // neither child nor sibling, so move up and to the next sibling. We might have to do
        // this several times
        T here = start;
        while( true ) {
            if( theFacade.getTopNode().equals( here )) {
                // we are done
                return null;
            }
            here = theFacade.getParentNode( here );
            ret = theFacade.getForwardSiblingNode( here );
            if( ret != null ) {
                return ret;
            }
        }
    }

    /**
     * Given this current node, return the previous node in the iteration.
     * 
     * @param start the current node
     * @return the previous node
     * @throws NoSuchElementException iteration has no more elements.
     * @see #goForward
     */
    protected T goBackward(
            T start )
    {
        T here;
        if( start != null ) {
            // find the last child of the previous sibling
            here = start;
            if( theFacade.getTopNode().equals( here )) {
                throw new NoSuchElementException();
            }

            T sibling = theFacade.getBackwardSiblingNode( here );
            if( sibling != null ) {
                here = sibling;
            } else {
                T ret = theFacade.getParentNode( here );
                return ret;
            }
        } else {
            here = theFacade.getTopNode();
        }        
        while( theFacade.hasChildNodes( here )) {
            T [] children = theFacade.getChildNodes( here );
            here = children[ children.length-1 ];
        }
        return here;
    }

    /**
     * Move the cursor to just before the first element, i.e. return the first element when
     * {@link #next next} is invoked right afterwards.
     *
     * @return the number of steps that were taken to move. Positive number means
     *         forward, negative backward
     */
    public int moveToBeforeFirst()
    {
        int ret = 0;
        while( hasPrevious() ) {
            previous();
            --ret;
        }
        return ret;
    }

    /**
     * Move the cursor to just after the last element, i.e. return the last element when
     * {@link #previous previous} is invoked right afterwards.
     *
     * @return the number of steps that were taken to move. Positive number means
     *         forward, negative backward
     */
    public int moveToAfterLast()
    {
        int ret = 0;
        while( hasNext() ) {
            next();
            ++ret;
        }
        return ret;
    }

    /**
     * The tree to traverse.
     */
    protected TreeFacade<T> theFacade;

    /**
     * The current position.
     */
    protected T theCurrent;
}
