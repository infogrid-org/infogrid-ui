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

package org.infogrid.jee.taglib.viewlet;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.set.TraversalPathSet;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.viewlet.Viewlet;

/**
 * Factors out common code for tags that determine whether a MeshObject has been
 * "reached" by the Viewlet.
 */
public abstract class AbstractIsReachedObjectViewletTag
    extends
        AbstractInfoGridBodyTag
{
    /**
     * Constructor.
     */
    public AbstractIsReachedObjectViewletTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theObject        = null;
        theTraversalPath = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the object property.
     *
     * @return value of the object property
     * @see #setObject
     */
    public final MeshObject getObject()
    {
        return theObject;
    }

    /**
     * Set value of the object property.
     *
     * @param newValue new value of the object property
     * @see #getObject
     */
    public final void setObject(
            MeshObject newValue )
    {
        theObject = newValue;
    }

    /**
     * Obtain value of the traversalPath property.
     *
     * @return value of the traversalPath property
     * @see #setTraversalPath
     */
    public final TraversalPath getTraversalPath()
    {
        return theTraversalPath;
    }

    /**
     * Set value of the traversalPath property.
     *
     * @param newValue new value of the traversalPath property
     * @see #getTraversalPath
     */
    public final void setTraversalPath(
            TraversalPath newValue )
    {
        theTraversalPath = newValue;
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
        Viewlet vl = (Viewlet) lookupOrThrow( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );

        boolean eval;
        if( theObject != null ) {
            if( theTraversalPath != null ) {
                throw new JspException( "Must not specify both object and traversalPath properties" );
            }
            eval = evaluateTest( theObject, vl.getViewedMeshObjects().getReachedObjects() );
        } else if( theTraversalPath != null ) {
            eval = evaluateTest( theTraversalPath, vl.getViewedMeshObjects().getTraversalPathSet() );
        } else {
            throw new JspException( "Must specify either object or traversalPath properties" );
        }
        if( eval ) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Evaluate the condition. If it returns true, we output
     * the Nodes contained in this Node. This is abstract as concrete
     * subclasses of this class need to have the ability to determine what
     * their evaluation criteria are.
     *
     * @param test the MeshObject to evaluate
     * @param set the MeshObjectSet to evaluate
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     */
    protected abstract boolean evaluateTest(
            MeshObject    test,
            MeshObjectSet set )
        throws
            JspException;

    /**
     * Evaluate the condition. If it returns true, we output
     * the Nodes contained in this Node. This is abstract as concrete
     * subclasses of this class need to have the ability to determine what
     * their evaluation criteria are.
     *
     * @param test the TraversalPath to evaluate
     * @param set the TraversalPathSet to evaluate
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     */
    protected abstract boolean evaluateTest(
            TraversalPath    test,
            TraversalPathSet set )
        throws
            JspException;

    /**
     * The MeshObject to test.
     */
    protected MeshObject theObject;

    /**
     * The TraversalPath to test.
     */
    protected TraversalPath theTraversalPath;
}
