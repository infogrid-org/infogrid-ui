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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.candy;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.JeeFormatter;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.util.InfoGridIterationTag;
import org.infogrid.util.CursorIterator;

/**
 * A Tag that can iterate over a very large set of objects by paging them, one tab per page.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class TabbedCursorIteratorTag
    extends
        AbstractInfoGridBodyTag
    implements
        InfoGridIterationTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public TabbedCursorIteratorTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theCursorIteratorName = null;
        theCursorIterator     = null;
        theLoopVar            = null;
        isFirstOnPage         = false;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the cursorIterator property.
     *
     * @return value of the cursorIterator property
     * @see #setCursorIteratorName
     */
    public final String getCursorIteratorName()
    {
        return theCursorIteratorName;
    }

    /**
     * Set value of the cursorIterator property.
     *
     * @param newValue new value of the cursorIterator property
     * @see #getCursorIteratorName
     */
    public final void setCursorIteratorName(
            String newValue )
    {
        theCursorIteratorName = newValue;
    }

    /**
     * Obtain value of the loopVar property.
     *
     * @return value of the loopVar property
     * @see #setLoopVar
     */
    public final String getLoopVar()
    {
        return theLoopVar;
    }

    /**
     * Set value of the loopVar property.
     *
     * @param newValue new value of the loopVar property
     * @see #getLoopVar
     */
    public final void setLoopVar(
            String newValue )
    {
        theLoopVar = newValue;
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
        determineCursorIterator();
        
        isFirstOnPage = true;

        Object current = null;
        if( theCursorIterator.hasNext() ) {
            current = theCursorIterator.next();
        }
        if( theLoopVar != null ) {
            if( current != null ) {
                setRequestAttribute( theLoopVar, current );
            } else {
                pageContext.getRequest().removeAttribute( theLoopVar );
            }
        }
        
        return EVAL_BODY_AGAIN; // we may have to do this at least once, for the header, even if the set is empty
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
        isFirstOnPage = false;

        if( super.bodyContent != null ) {
            JeeFormatter formatter = getFormatter();

            formatter.printPrevious( pageContext, formatter.isTrue( getFilter()), bodyContent.getString() );
            bodyContent.clearBody();
        }

        if( theCursorIterator.hasNext() ) {
            Object current = theCursorIterator.next();

            if( theLoopVar != null ) {
               setRequestAttribute( theLoopVar, current );
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
     * Allow enclosed tags to determine whether we are iterating over an empty
     * set.
     * 
     * @return true if the set is empty
     */
    public boolean processesEmptySet()
    {
        if( theCursorIterator.hasNext() ) {
            return false;
        }
        if( theCursorIterator.hasPrevious() ) {
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
        return isFirstOnPage; // for now
    }

    /**
     * Obtain the CursorIterator over which we iterate.
     *
     * @return the CursorIterator to iterate over
     * @throws JspException if a JSP exception has occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected CursorIterator<?> determineCursorIterator()
        throws
            JspException,
            IgnoreException
    {
        if( theCursorIterator == null ) {
            theCursorIterator = (CursorIterator<?>) lookupOrThrow( theCursorIteratorName );
        }
        return theCursorIterator;
    }

    /**
     * Determine whether this iteration tag has a next element to be returned
     * in the iteration.
     * 
     * @return true if there is a next element
     */
    public boolean hasNext()
    {
        if( theCursorIterator.hasNext() ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Name of the bean that contains the CursorIterator to iterate over.
     */
    protected String theCursorIteratorName;

    /**
     * The actual CursorIterator, once we have determined it.
     */
    protected CursorIterator<?> theCursorIterator;
    
    /**
     * String containing the name of the loop variable that contains the current Object.
     */
    private String theLoopVar;

    /**
     *  True if this is the first object on this page.
     */
    private boolean isFirstOnPage;
}
