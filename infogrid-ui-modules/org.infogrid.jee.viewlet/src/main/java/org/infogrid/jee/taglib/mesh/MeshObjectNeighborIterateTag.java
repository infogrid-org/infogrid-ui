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
// Copyright 1998-2012 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.mesh;

import java.io.IOException;
import java.util.Iterator;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.JeeFormatter;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridBodyTag;
import org.infogrid.jee.taglib.util.InfoGridIterationTag;
import org.infogrid.mesh.MeshObject;

/**
 * Tag that iterates over the current neighbor <code>MeshObjects</code> of a given
 * <code>MeshObject</code>.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshObjectNeighborIterateTag
    extends
        AbstractRestInfoGridBodyTag
    implements
        InfoGridIterationTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshObjectNeighborIterateTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObject      = null;
        theMeshObjectName  = null;
        theNeighborLoopVar = null;
        theIterator        = null;

        super.initializeToDefaults();
    }
    
    /**
     * Obtain value of the meshObject property.
     *
     * @return value of the meshObject property
     * @see #setMeshObject
     */
    public final Object getMeshObject()
    {
        return theMeshObject;
    }

    /**
     * Set value of the meshObject property.
     *
     * @param newValue new value of the meshObject property
     * @see #getMeshObject
     */
    public final void setMeshObject(
            Object newValue )
    {
        theMeshObject = newValue;
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
     * Obtain value of the neighborLoopVar property.
     *
     * @return value of the neighborLoopVar property
     * @see #setNeighborLoopVar
     */
    public final String getNeighborLoopVar()
    {
        return theNeighborLoopVar;
    }

    /**
     * Set value of the neighborLoopVar property.
     *
     * @param newValue new value of the neighborLoopVar property
     * @see #getNeighborLoopVar
     */
    public final void setNeighborLoopVar(
            String newValue )
    {
        theNeighborLoopVar = newValue;
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
        MeshObject obj = lookupMeshObjectOrThrow( "meshObject", theMeshObject, "meshObjectName", theMeshObjectName );

        theIterator = obj.traverseToNeighborMeshObjects().iterator();

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
            JeeFormatter formatter = getFormatter();

            formatter.printPrevious( pageContext, formatter.isTrue( getFilter() ), bodyContent.getString() );
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

            if( theNeighborLoopVar != null ) {
                setRequestAttribute( theNeighborLoopVar, current );
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
     * The MeshObject.
     */
    protected Object theMeshObject;

    /**
     * Name of the bean that contains the MeshObject.
     */
    protected String theMeshObjectName;
    
    /**
     * String containing the name of the loop variable that contains the current neighbor.
     */
    protected String theNeighborLoopVar;

    /**
     * Iterator over the MeshObjects.
     */
    protected Iterator<MeshObject> theIterator;
}
