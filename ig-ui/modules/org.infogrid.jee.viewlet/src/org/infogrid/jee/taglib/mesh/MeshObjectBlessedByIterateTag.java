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
import org.infogrid.model.primitives.EntityType;
import org.infogrid.util.ArrayCursorIterator;

/**
 * Tag that iterates over the <code>MeshTypes</code> with which a <code>MeshObject</code> is blessed.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshObjectBlessedByIterateTag
    extends
        AbstractInfoGridBodyTag
    implements
        InfoGridIterationTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshObjectBlessedByIterateTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObjectName   = null;
        theBlessedByLoopVar = null;
        theIterator         = null;

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
     * Obtain value of the blessedByLoopVar property.
     *
     * @return value of the blessedByLoopVar property
     * @see #setBlessedByLoopVar
     */
    public final String getBlessedByLoopVar()
    {
        return theBlessedByLoopVar;
    }

    /**
     * Set value of the blessedByLoopVar property.
     *
     * @param newValue new value of the blessedByLoopVar property
     * @see #getBlessedByLoopVar
     */
    public final void setBlessedByLoopVar(
            String newValue )
    {
        theBlessedByLoopVar = newValue;
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
        MeshObject obj = (MeshObject) lookupOrThrow( theMeshObjectName );

        theIterator = ArrayCursorIterator.<EntityType>create( obj.getTypes() );

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
            EntityType current = theIterator.next();

            if( theBlessedByLoopVar != null ) {
                pageContext.getRequest().setAttribute( theBlessedByLoopVar, current );
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
        if( theBlessedByLoopVar != null ) {
            pageContext.getRequest().removeAttribute( theBlessedByLoopVar );
        }
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
     * Name of the bean that contains the MeshObject to render.
     */
    protected String theMeshObjectName;
    
    /**
     * String containing the name of the loop variable that contains the current EntityType.
     */
    protected String theBlessedByLoopVar;

    /**
     * Iterator over the EntityTypes.
     */
    protected Iterator<EntityType> theIterator;
}
