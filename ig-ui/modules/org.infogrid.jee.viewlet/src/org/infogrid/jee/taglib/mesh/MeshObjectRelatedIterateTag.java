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

package org.infogrid.jee.taglib.mesh;

import java.io.IOException;
import java.util.Iterator;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.util.InfoGridIterationTag;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.model.traversal.TraversalSpecification;

/**
 * Tag that iterates over the set of <code>MeshObjects</code> found by traversing a
 * <code>TraversalSpecification</code> from a given start <code>MeshObject</code>.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshObjectRelatedIterateTag
    extends
        AbstractInfoGridBodyTag
    implements
        InfoGridIterationTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshObjectRelatedIterateTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObjectName             = null;
        theTraversalSpecificationName = null;
        theRelatedLoopVar            = null;
        theIterator                   = null;

        super.initializeToDefaults();
    }
    
    /**
     * Obtain value of the meshObjectName property.
     *
     * @return value of the meshObjectName property
     * @see #setMeshObjectName
     */
    public final String getMeshObjectName()
    {
        return theMeshObjectName;
    }

    /**
     * Set value of the meshObjectName property.
     *
     * @param newValue new value of the meshObjectName property
     * @see #getMeshObjectName
     */
    public final void setMeshObjectName(
            String newValue )
    {
        theMeshObjectName = newValue;
    }

    /**
     * Obtain value of the traversalSpecificationName property.
     *
     * @return value of the traversalSpecificationName property
     * @see #setTraversalSpecificationName
     */
    public final String getTraversalSpecificationName()
    {
        return theTraversalSpecificationName;
    }

    /**
     * Set value of the traversalSpecificationName property.
     *
     * @param newValue new value of the traversalSpecificationName property
     * @see #getTraversalSpecificationName
     */
    public final void setTraversalSpecificationName(
            String newValue )
    {
        theTraversalSpecificationName = newValue;
    }

    /**
     * Obtain value of the relatedLoopVar property.
     *
     * @return value of the relatedLoopVar property
     * @see #setRelatedLoopVar
     */
    public final String getRelatedLoopVar()
    {
        return theRelatedLoopVar;
    }

    /**
     * Set value of the relatedLoopVar property.
     *
     * @param newValue new value of the relatedLoopVar property
     * @see #getRelatedLoopVar
     */
    public final void setRelatedLoopVar(
            String newValue )
    {
        theRelatedLoopVar = newValue;
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
        MeshObject             obj       = (MeshObject) lookupOrThrow( theMeshObjectName );
        TraversalSpecification traversal = (TraversalSpecification) lookupOrThrow( theTraversalSpecificationName );
        
        MeshObjectSet found = obj.traverse( traversal );
        theIterator = found.iterator();

        int ret = iterateOnce();
        return ret;
    }

    /**
     * Invoked after the Body tag has been invoked.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an error occurred
     */
    @Override
    protected int realDoAfterBody()
        throws
            JspException
    {
        if( super.bodyContent != null ) {

            theFormatter.printPrevious( pageContext, theFormatter.isTrue( getFilter()), bodyContent.getString() );
            bodyContent.clearBody();
        }

        int ret = iterateOnce();
        return ret;
    }
    
    /**
     * Factors out common code for doStartTag and doAfterBody.
     * 
     * @return evaluate or skip body
     */
    protected int iterateOnce()
    {
        if( theIterator.hasNext() ) {
            MeshObject current = theIterator.next();

            if( theRelatedLoopVar != null ) {
                setRequestAttribute( theRelatedLoopVar, current );
            }

            return EVAL_BODY_AGAIN;

        } else {
            return SKIP_BODY;
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
        // no need to remove request attributes; superclass will do that

        return EVAL_PAGE;
    }

    /**
     * Determine whether this iteration tag has a next element to be returned
     * in the iteration.
     * 
     * @return true if there is a next element
     */
    public boolean hasNext()
    {
        if( theIterator.hasNext() ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Name of the bean that contains the MeshObject to start the traversal from.
     */
    protected String theMeshObjectName;
    
    /**
     * Name of the bean that contains the TraversalSpecification.
     */
    protected String theTraversalSpecificationName;

    /**
     * String containing the name of the loop variable that contains the current neighbor.
     */
    protected String theRelatedLoopVar;

    /**
     * Iterator over the MeshObjects.
     */
    protected Iterator<MeshObject> theIterator;
}
