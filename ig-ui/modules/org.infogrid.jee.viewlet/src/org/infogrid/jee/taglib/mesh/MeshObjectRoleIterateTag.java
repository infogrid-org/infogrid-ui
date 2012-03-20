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
import org.infogrid.mesh.NotRelatedException;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.util.ArrayCursorIterator;

/**
 * Tag that iterates over the <code>RoleTypes</code> in which a start <code>MeshObject</code>
 * participates with a destination <code>MeshObject</code>.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshObjectRoleIterateTag
    extends
        AbstractRestInfoGridBodyTag
    implements
        InfoGridIterationTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshObjectRoleIterateTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theStartObject           = null;
        theStartObjectName       = null;
        theDestinationObject     = null;
        theDestinationObjectName = null;
        theRoleTypeLoopVar       = null;
        theIterator              = null;

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
     * Obtain value of the destinationObjectName property.
     *
     * @return value of the destinationObjectName property
     * @see #setDestinationObjectName
     */
    public final String getDestinationObjectName()
    {
        return theDestinationObjectName;
    }

    /**
     * Set value of the destinationObjectName property.
     *
     * @param newValue new value of the destinationObjectName property
     * @see #getDestinationObjectName
     */
    public final void setDestinationObjectName(
            String newValue )
    {
        theDestinationObjectName = newValue;
    }

    /**
     * Obtain value of the roleTypeLoopVar property.
     *
     * @return value of the roleTypeLoopVar property
     * @see #setRoleTypeLoopVar
     */
    public final String getRoleTypeLoopVar()
    {
        return theRoleTypeLoopVar;
    }

    /**
     * Set value of the roleTypeLoopVar property.
     *
     * @param newValue new value of the roleTypeLoopVar property
     * @see #getRoleTypeLoopVar
     */
    public final void setRoleTypeLoopVar(
            String newValue )
    {
        theRoleTypeLoopVar = newValue;
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
        MeshObject start       = lookupMeshObjectOrThrow( "startObject",       theStartObject,       "startObjectName",       theStartObjectName );
        MeshObject destination = lookupMeshObjectOrThrow( "destinationObject", theDestinationObject, "destinationObjectName", theDestinationObjectName );

        try {
            RoleType [] types = start.getRoleTypes( destination );
            theIterator = ArrayCursorIterator.<RoleType>create( types );

            int ret = iterateOnce();
            return ret;

        } catch( NotRelatedException ex ) {
            throw new JspException( ex );
        }
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

            formatter.printPrevious( pageContext, formatter.isTrue( getFilter()), bodyContent.getString() );
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
            RoleType current = theIterator.next();

            if( theRoleTypeLoopVar != null ) {
                setRequestAttribute( theRoleTypeLoopVar, current );
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
     * The start MeshObject.
     */
    protected Object theStartObject;

    /**
     * Name of the bean that contains the start MeshObject.
     */
    protected String theStartObjectName;

    /**
     * The destination MeshObject.
     */
    protected Object theDestinationObject;

    /**
     * Name of the bean that contains the destination MeshObject.
     */
    protected String theDestinationObjectName;
    
    /**
     * String containing the name of the loop variable that contains the current RoleType.
     */
    protected String theRoleTypeLoopVar;

    /**
     * Iterator over the RoleTypes.
     */
    protected Iterator<RoleType> theIterator;
}
        
