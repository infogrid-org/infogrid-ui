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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.mesh.set;

import java.io.IOException;
import java.util.Iterator;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.core.LoopTagStatus;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridBodyTag;
import org.infogrid.jee.taglib.util.InfoGridIterationTag;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.set.ByPropertyValueSorter;
import org.infogrid.mesh.set.DefaultMeshObjectSorter;
import org.infogrid.mesh.set.MeshObjectSet;
import org.infogrid.mesh.set.MeshObjectSorter;
import org.infogrid.model.primitives.PropertyType;

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
        theStatusVar         = null;
        theOrderBy           = null;
        theReverse           = null;
        theCounter           = 0;
        theSet               = null;
        theIterator          = null;
        theCurrent           = null;

        theStatus = null;

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
     * Obtain value of the statusVar property.
     *
     * @return value of the statusVar property
     * @see #setStatusVar
     */
    public final String getStatusVar()
    {
        return theStatusVar;
    }

    /**
     * Set value of the statusVar property.
     *
     * @param newValue new value of the statusVar property
     * @see #getStatusVar
     */
    public final void setStatusVar(
            String newValue )
    {
        theStatusVar = newValue;
    }

    /**
     * Obtain value of the orderBy property.
     *
     * @return value of the orderBy property
     * @see #setOrderBy
     */
    public final String getOrderBy()
    {
        return theOrderBy;
    }

    /**
     * Set value of the orderBy property.
     *
     * @param newValue new value of the orderBy property
     * @see #getOrderBy
     */
    public final void setOrderBy(
            String newValue )
    {
        theOrderBy = newValue;
    }

    /**
     * Obtain value of the reverse property.
     *
     * @return value of the reverse property
     * @see #setReverse
     */
    public final String getReverse()
    {
        return theReverse;
    }

    /**
     * Set value of the reverse property.
     *
     * @param newValue new value of the reverse property
     * @see #getReverse
     */
    public final void setReverse(
            String newValue )
    {
        theReverse = newValue;
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
        if( theSet == null ) {
            // can be, if ignore=true
            return SKIP_BODY;
        }

        boolean          reverse = theFormatter.isTrue( theReverse );
        MeshObjectSorter sorter;
        if( theOrderBy != null ) {
            PropertyType orderBy = (PropertyType) findMeshTypeByIdentifier( theOrderBy );

            sorter = ByPropertyValueSorter.create( orderBy, reverse );

        } else {
            if( !reverse ) {
                sorter = DefaultMeshObjectSorter.BY_USER_VISIBLE_STRING;
            } else {
                sorter = DefaultMeshObjectSorter.BY_REVERSE_USER_VISIBLE_STRING;
            }
        }

        theSet = theSet.getFactory().createOrderedImmutableMeshObjectSet( theSet, sorter );

        if( theSet.isEmpty() ) {
            theStatus = Status.PROCESS_NO_CONTENT_ROW;

        } else {
            theIterator = theSet.iterator();
            theCurrent  = theIterator.next();

            if( theMeshObjectLoopVar != null ) {
                pageContext.getRequest().setAttribute( theMeshObjectLoopVar, theCurrent );
            }
            if( theStatusVar != null ) {
                LoopTagStatus status = new MyLoopTagStatus();
                pageContext.getRequest().setAttribute( theStatusVar, status );
            }

            if( theIterator.hasNext() ) {
                theStatus = Status.PROCESS_HEADER_AND_FIRST_ROW;
            } else {
                theStatus = Status.PROCESS_SINGLE_ROW;
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
        if( bodyContent != null ) {
            theFormatter.printPrevious( pageContext, theFormatter.isTrue( getFilter()), bodyContent.getString() );
            bodyContent.clearBody();
        }

        if( theStatus == Status.PROCESS_NO_CONTENT_ROW ) {
            return SKIP_BODY;
        }
        if( theStatus == Status.PROCESS_FOOTER_AND_LAST_ROW ) {
            return SKIP_BODY;
        }
        if( theStatus == Status.PROCESS_SINGLE_ROW ) {
            return SKIP_BODY;
        }

        theCurrent = theIterator.next();

        ++theCounter;

        if( theMeshObjectLoopVar != null ) {
            pageContext.getRequest().setAttribute( theMeshObjectLoopVar, theCurrent );
        }
        if( theStatusVar != null ) {
            LoopTagStatus status = new MyLoopTagStatus();
            pageContext.getRequest().setAttribute( theStatusVar, status );
        }

        if( theIterator.hasNext() ) {
            theStatus = Status.PROCESS_MIDDLE_ROW;
        } else {
            theStatus = Status.PROCESS_FOOTER_AND_LAST_ROW;
        }

        return EVAL_BODY_AGAIN;
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
            pageContext.getRequest().removeAttribute( theMeshObjectLoopVar );
        }
        if( theStatusVar != null ) {
            pageContext.getRequest().removeAttribute( theStatusVar );
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
        if( theStatus == Status.PROCESS_NO_CONTENT_ROW ) {
            return false;
        }
        if( theStatus == Status.PROCESS_FOOTER_AND_LAST_ROW ) {
            return false;
        }
        return true;
    }

    /**
     * Allow enclosed tags to determine whether, during this iteration, the
     * header should be displayed.
     * 
     * @return true if the header should be displayed
     */
    public boolean displayHeader()
    {
        if( theStatus == Status.PROCESS_HEADER_AND_FIRST_ROW ) {
            return true;
        }
        if( theStatus == Status.PROCESS_SINGLE_ROW ) {
            return true;
        }
        return false;
    }

    /**
     * Allow enclosed tags to determine whether, during this iteration, the
     * footer should be displayed.
     *
     * @return true if the footer should be displayed
     */
    public boolean displayFooter()
    {
        if( theStatus == Status.PROCESS_FOOTER_AND_LAST_ROW ) {
            return true;
        }
        if( theStatus == Status.PROCESS_SINGLE_ROW ) {
            return true;
        }
        return false;
    }

    /**
     * Allow enclosed tags to determine whether, during this iteration, a
     * content row should be displayed.
     *
     * @return true if a content row should be displayed
     */
    public boolean displayContentRow()
    {
        return    theStatus == Status.PROCESS_HEADER_AND_FIRST_ROW
               || theStatus == Status.PROCESS_FOOTER_AND_LAST_ROW
               || theStatus == Status.PROCESS_MIDDLE_ROW
               || theStatus == Status.PROCESS_SINGLE_ROW;
    }

    /**
     * Allow enclosed tags to determine whether, during this iteration, the
     * no content row should be displayed.
     *
     * @return true if the no content row should be displayed
     */
    public boolean displayNoContentRow()
    {
        return theStatus == Status.PROCESS_NO_CONTENT_ROW;
    }

    /**
     * String containing the name of the loop variable that contains the current MeshObject.
     */
    private String theMeshObjectLoopVar;

    /**
     * String containing the name of the loop variable that contains the LoopTagStatus.
     */
    private String theStatusVar;

    /**
     * String containing the identifier of the PropertyType by which we sort the set, if any.
     */
    protected String theOrderBy;

    /**
     * String containing a boolean flag indicating whether we should iterate in the reverse sort order.
     */
    protected String theReverse;

    /**
     * Counts the number of iterations performed so far.
     */
    protected int theCounter;

    /**
     * The MeshObjectSet that we iterate over.
     */
    private MeshObjectSet theSet;

    /**
     * Iterator over the MeshObjectSet.
     */
    private Iterator<MeshObject> theIterator;

    /**
     * The current or most recently returned MeshObject.
     */
    protected MeshObject theCurrent;

    /**
     * Status of the iteration.
     */
    protected Status theStatus;

    /**
     * Processing status.
     */
    protected static enum Status
    {
        PROCESS_NO_CONTENT_ROW,        // no rows at all
        PROCESS_HEADER_AND_FIRST_ROW,  // the first row, there is at least one more
        PROCESS_MIDDLE_ROW,            // neither the first nor the last row
        PROCESS_FOOTER_AND_LAST_ROW,   // the last row, there was at least one before
        PROCESS_SINGLE_ROW             // the one and only row
    }

    /**
     * LoopTagStatus implementation for this class.
     */
    class MyLoopTagStatus
            implements
                LoopTagStatus
    {
        public Integer getBegin()
        {
            return null;
        }
        public int getCount()
        {
            return theCounter+1;
        }
        public Object getCurrent()
        {
            return theCurrent;
        }
        public Integer getEnd()
        {
            return null;
        }
        public int getIndex()
        {
            return theCounter;
        }
        public Integer getStep()
        {
            return null;
        }
        public boolean isFirst()
        {
            return theStatus == Status.PROCESS_HEADER_AND_FIRST_ROW;
        }
        public boolean isLast()
        {
            return theStatus == Status.PROCESS_FOOTER_AND_LAST_ROW;
        }
    }
}
