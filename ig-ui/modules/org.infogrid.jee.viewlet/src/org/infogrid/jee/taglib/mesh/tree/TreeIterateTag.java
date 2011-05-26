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
// Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.mesh.tree;

import java.io.IOException;
import java.util.Stack;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridBodyTag;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.DefaultMeshObjectSorter;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.set.MeshObjectSorter;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.logging.Log;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.ToStringDumper;

/**
 * <p>Iterates over a Tree.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class TreeIterateTag
    extends
        AbstractRestInfoGridBodyTag
    implements
        CanBeDumped
{
    private static final Log  log              = Log.getLogInstance( TreeIterateTag.class ); // our own, private logger
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public TreeIterateTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theStartObject            = null;
        theStartObjectName        = null;
        theTraversalSpecification = null;
        theTraversals             = null;
        theMeshObjectLoopVar      = null;
        theLevelVar               = null;
        theTraversalPathLoopVar   = null;
        theState                  = STATE.INIT;
        theCurrentNode            = null;
        if( theStack == null ) {
            theStack = new Stack<CursorIterator<MeshObject>>();
        } else {
            theStack.clear();
        }

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the startObject property.
     *
     * @return value of the startObject property
     * @see #setStartObject
     */
    public final Object getStartObject()
    {
        return theStartObject;
    }

    /**
     * Set value of the startObject property.
     *
     * @param newValue new value of the startObject property
     * @see #getStartObject
     */
    public final void setStartObject(
            Object newValue )
    {
        theStartObject = newValue;
    }

    /**
     * Obtain value of the startObjectName property.
     *
     * @return value of the startObjectName property
     * @see #setStartObjectName
     */
    public final String getStartObjectName()
    {
        return theStartObjectName;
    }

    /**
     * Set value of the startObjectName property.
     *
     * @param newValue new value of the startObjectName property
     * @see #getStartObjectName
     */
    public final void setStartObjectName(
            String newValue )
    {
        theStartObjectName = newValue;
    }

    /**
     * Obtain value of the traversalSpecification property.
     *
     * @return value of the traversalSpecification property
     * @see #setTraversalSpecification
     */
    public final String getTraversalSpecification()
    {
        return theTraversalSpecification;
    }

    /**
     * Set value of the traversalSpecification property.
     *
     * @param newValue new value of the traversalSpecification property
     * @see #getTraversalSpecification
     */
    public final void setTraversalSpecification(
            String newValue )
    {
        theTraversalSpecification = newValue;
    }


    /**
     * Obtain value of the meshObjectLoopVar property.
     *
     * @return value of the meshObjectLoopVar property
     * @see #setMeshObjectLoopVar
     */
    public final String getMeshObjectLoopVar()
    {
        return theMeshObjectLoopVar;
    }

    /**
     * Set value of the meshObjectLoopVar property.
     *
     * @param newValue new value of the meshObjectLoopVar property
     * @see #getMeshObjectLoopVar
     */
    public final void setMeshObjectLoopVar(
            String newValue )
    {
        theMeshObjectLoopVar = newValue;
    }

    /**
     * Obtain value of the levelVar property.
     *
     * @return value of the levelVar property
     * @see #setLevelVar
     */
    public final String getLevelVar()
    {
        return theLevelVar;
    }

    /**
     * Set value of the levelVar property.
     *
     * @param newValue new value of the levelVar property
     * @see #getLevelVar
     */
    public final void setLevelVar(
            String newValue )
    {
        theLevelVar = newValue;
    }

    /**
     * Obtain value of the traversalPathLoopVar property.
     *
     * @return value of the traversalPathLoopVar property
     * @see #setTraversalPathLoopVar
     */
    public final String getTraversalPathLoopVar()
    {
        return theTraversalPathLoopVar;
    }

    /**
     * Set value of the traversalPathLoopVar property.
     *
     * @param newValue new value of the traversalPathLoopVar property
     * @see #getTraversalPathLoopVar
     */
    public final void setTraversalPathLoopVar(
            String newValue )
    {
        theTraversalPathLoopVar = newValue;
    }

    /**
     * Enable related tags to determine the current level.
     *
     * @return the current level
     */
    public int getCurrentLevel()
    {
        return theStack.size();
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "realDoStartTag" );
        }
        theSetSorter = DefaultMeshObjectSorter.BY_USER_VISIBLE_STRING;
        
        try {
            MeshObject start = lookupMeshObjectOrThrow( "startObject", theStartObject, "startObjectName", theStartObjectName );
            theTraversals    = findTraversalSpecificationSequenceOrThrow( start, theTraversalSpecification );

            if( theTraversals == null || theTraversals.length == 0 ) {
                return SKIP_BODY;
            }

            MeshObjectSet currentChildren = start.traverse( theTraversals[0] );
            if( currentChildren.isEmpty() ) {
                return SKIP_BODY;
            }
            currentChildren = currentChildren.getFactory().createOrderedImmutableMeshObjectSet( currentChildren, theSetSorter );

            CursorIterator<MeshObject> iter = currentChildren.iterator();
            theCurrentNode = iter.next();
            theStack.push( iter );

            theState = STATE.PROCESS_NODE_BEFORE;

            if( theMeshObjectLoopVar != null ) {
                setRequestAttribute( theMeshObjectLoopVar, theCurrentNode );
            }
            if( theLevelVar != null ) {
                setRequestAttribute( theLevelVar, theStack.size() );
            }
            if( theTraversalPathLoopVar != null ) {
                TraversalPath traversalPath = constructCurrentTraversalPath();
                setRequestAttribute( theTraversalPathLoopVar, traversalPath );
            }
            return EVAL_BODY_INCLUDE;

        } finally {
            if( log.isTraceEnabled() ) {
                log.traceMethodCallExit( this, "realDoStartTag" );
            }
            // pageContext.getOut().print( "<pre><b>realDoStartTag():</b>\n" + this + "</pre>\n" );
        }
    }

    /**
     * Invoked after the Body tag has been invoked.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an error occurred
     * @throws IOException thrown if an I/O Exception occurred
     */
    @Override
    protected int realDoAfterBody()
        throws
            JspException,
            IOException
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "realDoAfterBody" );
        }
        try {
            if( super.bodyContent != null ) {
                theFormatter.printPrevious( pageContext, theFormatter.isTrue( getFilter()), bodyContent.getString() );
                bodyContent.clearBody();
            }

            // some data that helps us decide what to do
            MeshObjectSet              currentChildren;
            CursorIterator<MeshObject> currentIter     = theStack.peek();

            if(    ( theCurrentNode != null )
                && ( theStack.size() < theTraversals.length ))
            {
                currentChildren = theCurrentNode.traverse( theTraversals[ theStack.size() ] );
                currentChildren = currentChildren.getFactory().createOrderedImmutableMeshObjectSet( currentChildren, theSetSorter );
            } else {
                currentChildren = null;
            }

            // determine the next state
            STATE theNewState;
            switch( theState ) {
                case PROCESS_NODE_BEFORE:
                    if( currentChildren != null && !currentChildren.isEmpty() ) {
                        theNewState = STATE.GO_DOWN;

                    } else {
                        theNewState = STATE.PROCESS_NODE_AFTER;
                    }
                    break;

                case GO_DOWN:
                    theNewState = STATE.PROCESS_NODE_BEFORE; // know for sure, otherwise we would not have gone down
                    break;

                case GO_UP:
                    theNewState = STATE.PROCESS_NODE_AFTER;
                    break;

                case PROCESS_NODE_AFTER:
                    if( currentIter.hasNext() ) {
                        theNewState = STATE.PROCESS_NODE_BEFORE;
                    } else if( theStack.size() > 1 ) {
                        theNewState = STATE.GO_UP;
                    } else {
                        theNewState = STATE.EXIT;
                    }
                    break;
                    
                default:
                    throw new JspException( "Unexpected state: " + theState );
            }

            // go to the next state
            switch( theNewState ) {
                case PROCESS_NODE_BEFORE:
                    theCurrentNode = currentIter.next();
                    break;

                case GO_DOWN:
                    currentIter = currentChildren.iterator();
                    theStack.push( currentIter );
                    theCurrentNode = null;
                    break;

                case GO_UP:
                    theStack.pop();
                    if( theStack.isEmpty() ) {
                        return SKIP_BODY;
                    }
                    theCurrentNode = null;
                    break;

                case PROCESS_NODE_AFTER:
                    theCurrentNode = currentIter.peekPrevious();
                    break;

                case EXIT:
                    return SKIP_BODY;

                default:
                    throw new JspException( "Unexpected new state: " + theNewState );
            }
            theState = theNewState;

            // set page request variables
            if( theMeshObjectLoopVar != null ) {
                setRequestAttribute( theMeshObjectLoopVar, theCurrentNode );
            }
            if( theLevelVar != null ) {
                setRequestAttribute( theLevelVar, theStack.size() );
            }
            if( theTraversalPathLoopVar != null ) {
                TraversalPath traversalPath  = constructCurrentTraversalPath();
                setRequestAttribute( theTraversalPathLoopVar, traversalPath );
            }

            return EVAL_BODY_AGAIN;

        } finally {
            if( log.isTraceEnabled() ) {
                log.traceMethodCallExit( this, "realDoAfterBody" );
            }
            // pageContext.getOut().print( "<pre><b>realDoAfterBodyTag():</b>\n" + this + "</pre>\n" );
        }
    }

    /**
     * Our implementation of doEndTag().
     *
     * @return evaluate or skip body
     */
    @Override
    protected int realDoEndTag()
    {
        if( log.isTraceEnabled() ) {
            log.traceMethodCallEntry( this, "realDoEndTag", this );
        }
        try {
            theState = STATE.EXIT;

            return EVAL_PAGE;

        } finally {
            if( log.isTraceEnabled() ) {
                log.traceMethodCallExit( this, "realDoEndTag" );
            }
        }
    }

    /**
     * Enable a contained TreeIterateNodeBeforeChildrenTag to determine whether it is its turn.
     *
     * @return true if the TreeIterateNodeBeforeChildrenTag is supposed to be processed during this turn
     */
    public boolean processNodeBeforeChildrenTag()
    {
        return theState == STATE.PROCESS_NODE_BEFORE;
    }

    /**
     * Enable a contained TreeIterateNodeAfterChildrenTag to determine whether it is its turn.
     *
     * @return true if the TreeIterateNodeAfterChildrenTag is supposed to be processed during this turn
     */
    public boolean processNodeAfterChildrenTag()
    {
        return theState == STATE.PROCESS_NODE_AFTER;
    }

    /**
     * Enable a contained TreeIterateDownTag to determine whether it is its turn.
     *
     * @return true if the TreeIterateDownTag is supposed to be processed during this turn
     */
    public boolean processDownTag()
    {
        return theState == STATE.GO_DOWN;
    }

    /**
     * Enable a contained TreeIterateUpTag to determine whether it is its turn.
     *
     * @return true if the TreeIterateUpTag is supposed to be processed during this turn
     */
    public boolean processUpTag()
    {
        return theState == STATE.GO_UP;
    }

    /**
     * Construct the current TraversalPath.
     *
     * @return the current TraversalPath
     */
    protected TraversalPath constructCurrentTraversalPath()
    {
        TraversalPath ret = null;

        if( theCurrentNode != null ) {
            // we don't know whether the current node came from "peekPrevious()" or "peekNext()", so we
            // deal with it directly and skip that step when walking the stack. It's faster that way anyway.
            ret = TraversalPath.create( theTraversals[ theStack.size()-1 ], theCurrentNode, ret );
            for( int i=theStack.size()-2 ; i>=0 ; --i ) {
                CursorIterator<MeshObject> current = theStack.get( i );
                if( current.hasPrevious() ) {
                    ret = TraversalPath.create( theTraversals[i], current.peekPrevious(), ret );
                }
            }
        }
        return ret;
    }

    /**
     * Convert to String representation, for debugging.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        ToStringDumper dumper = ToStringDumper.create();
        dump( dumper );
        return dumper.getBuffer();
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String[] {
                    "stack",
                    "current",
                    "state"
                },
                new Object[] {
                    theStack,
                    theCurrentNode != null ? theCurrentNode.getIdentifier().toExternalForm() : null,
                    theState
                } );
    }

    /**
     * The start MeshObject.
     */
    protected Object theStartObject;

    /**
     * Name of the bean that contains the start MeshObject.
     */
    protected String theStartObjectName;
    
    /**
     * The TraversalSpecification to take.
     */
    protected String theTraversalSpecification;

    /**
     * String containing the name of the loop variable that contains the current MeshObject.
     */
    private String theMeshObjectLoopVar;

    /**
     * String containing the name of the variable that contains the current depth level in the iteration.
     */
    protected String theLevelVar;

    /**
     * String containing the name of the variable that contains the current TraversalPath in the iteration.
     */
    protected String theTraversalPathLoopVar;

    /**
     * Parsed TraversalSpecifications, in sequence.
     */
    protected TraversalSpecification [] theTraversals;

    /**
     * The Stack that helps us traverse a tree without having recursion at our disposal.
     */
    protected Stack<CursorIterator<MeshObject>> theStack;

    /**
     * The currently processed MeshObject.
     */
    protected MeshObject theCurrentNode;

    /**
     * The sorter to use for the MeshObjects on any given level.
     */
    protected MeshObjectSorter theSetSorter;

    /**
     * The current state.
     */
    protected STATE theState;

    protected static enum STATE {
        INIT,
        PROCESS_NODE_BEFORE,
        GO_DOWN,
        GO_UP,
        PROCESS_NODE_AFTER,
        EXIT
    };
}
