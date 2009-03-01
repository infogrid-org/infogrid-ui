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

package org.infogrid.jee.taglib.mesh.set;

import java.io.IOException;
import java.util.Iterator;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridBodyTag;
import org.infogrid.jee.taglib.util.InfoGridIterationTag;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSet;

/**
 * Factors out common code for tags that iterate over the content of a <code>MeshObjectSet</code>.
 */
public abstract class AbstractMeshObjectSetIterateTag
    extends
        AbstractRestInfoGridBodyTag
    implements
        InfoGridIterationTag
{
    /**
     * Constructor.
     */
    protected AbstractMeshObjectSetIterateTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObjectLoopVar = null;
        theSet               = null;
        theIterator          = null;
        isFirstIteration     = false;

        super.initializeToDefaults();
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
        theSet = determineMeshObjectSet();

        theIterator      = theSet.iterator();
        isFirstIteration = true;

        MeshObject current = null;
        if( theIterator.hasNext() ) {
            current = theIterator.next();
        }
        if( theMeshObjectLoopVar != null ) {
            if( current != null ) {
                pageContext.setAttribute( theMeshObjectLoopVar, current );
            } else {
                pageContext.removeAttribute( theMeshObjectLoopVar );
            }
        }
        
        return EVAL_BODY_AGAIN; // we may have to do this at least once, for the header, even if the set is empty
    }

    /**
     * This method is defined by subclasses to provide the MeshObjectSet over which we iterate.
     *
     * @return the set to iterate over
     * @throws JspException if a JSP exception has occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected abstract MeshObjectSet determineMeshObjectSet()
        throws
            JspException,
            IgnoreException;

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
        isFirstIteration = false;

        if( super.bodyContent != null ) {

            theFormatter.printPrevious( pageContext, theFormatter.isTrue( getFilter()), bodyContent.getString() );
            bodyContent.clearBody();
        }

        if( theIterator.hasNext() ) {
            MeshObject current = theIterator.next();

            if( theMeshObjectLoopVar != null ) {
                pageContext.setAttribute( theMeshObjectLoopVar, current );
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
        if( theMeshObjectLoopVar != null ) {
            pageContext.removeAttribute( theMeshObjectLoopVar );
        }
        return EVAL_PAGE;
    }
    
    /**
     * Allow enclosed tags to determine whether we are iterating over an empty
     * set.
     * 
     * @return true if the set is empty
     */
    public boolean processesEmptySet()
    {
        return theSet.isEmpty();
    }

    /**
     * Allow enclosed tags to determine whether, during this iteration, the
     * header should be displayed.
     * 
     * @return true if the header should be displayed
     */
    public boolean displayHeader()
    {
        return isFirstIteration; // for now
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
     * String containing the name of the loop variable that contains the currentMeshObject.
     */
    private String theMeshObjectLoopVar;

    /**
     * The MeshObjectSet that we iterate over.
     */
    private MeshObjectSet theSet;

    /**
     * Iterator over the MeshObjectSet.
     */
    private Iterator<MeshObject> theIterator;
    
    /**
     *  True if this is the first iteration.
     */
    private boolean isFirstIteration;
}
