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

package org.infogrid.jee.taglib.viewlet;

import java.io.IOException;
import java.util.Iterator;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.util.InfoGridIterationTag;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.rest.RestfulRequest;
import org.infogrid.util.ArrayCursorIterator;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactory;
import org.infogrid.viewlet.ViewletFactoryChoice;
import org.infogrid.util.context.Context;

/**
 * Iterates over all ViewletFactoryAlternatives for a given Subject.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class ViewletAlternativesIterateTag
    extends
        AbstractInfoGridBodyTag
    implements
        InfoGridIterationTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public ViewletAlternativesIterateTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theLoopVar         = null;
        theWorstAcceptable = null;
        
        super.initializeToDefaults();
    }

    /**
     * Set the LoopVar property.
     *
     * @param newValue the new value
     */
    public void setLoopVar(
            String newValue )
    {
        theLoopVar = newValue;
    }

    /**
     * Obtain the LoopVar property.
     *
     * @return the LoopVar property
     */
    public String getLoopVar()
    {
        return theLoopVar;
    }

    /**
     * Set the WorstAcceptable property.
     *
     * @param newValue the new value
     */
    public void setWorstAcceptable(
            String newValue )
    {
        theWorstAcceptable = newValue;
    }

    /**
     * Obtain the WorstAcceptable property.
     *
     * @return the WorstAcceptable property
     */
    public String getWorstAcceptable()
    {
        return theWorstAcceptable;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        Viewlet        currentViewlet = (Viewlet) lookupOrThrow( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );
        RestfulRequest restful        = (RestfulRequest) lookupOrThrow( RestfulRequest.RESTFUL_REQUEST_ATTRIBUTE_NAME );

        MeshObject subject = currentViewlet.getSubject();
        Context    c       = currentViewlet.getContext();

        ViewletFactory    factory = c.findContextObjectOrThrow( ViewletFactory.class );
        MeshObjectsToView toView  = MeshObjectsToView.create( subject, restful );

        ViewletFactoryChoice [] candidates = factory.determineFactoryChoicesOrderedByMatchQuality( toView );
        int max = candidates.length;

        if( theWorstAcceptable != null ) {
            double worst = Double.parseDouble( theWorstAcceptable );

            for( int i=0 ; i<candidates.length ; ++i ) {
                if( candidates[i].getMatchQualityFor( toView ) > worst ) {
                    max = i;
                    break;
                }
            }
        }

        theIterator = ArrayCursorIterator.create( candidates, 0, 0, max );

        int ret = iterateOnce();
        return ret;
    }

    /**
     * Our implementation of doAfterBody().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    @Override
    protected int realDoAfterBody()
        throws
            JspException,
            IgnoreException,
            IOException
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
     * @throws JspException thrown if an evaluation error occurred
     */
    protected int iterateOnce()
            throws
                JspException
    {
        while( theIterator.hasNext() ) {

            ViewletFactoryChoice current  = theIterator.next();

            if( theLoopVar != null ) {
                pageContext.getRequest().setAttribute( theLoopVar, current );
            }
            return EVAL_BODY_AGAIN;
        }
        return SKIP_BODY;
    }

    /**
     * Our implementation of doEndTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    @Override
    protected int realDoEndTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        if( theLoopVar != null ) {
            pageContext.getRequest().removeAttribute( theLoopVar );
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
     * String containing the name of the loop variable that contains the ViewletFactoryChoice.
     */
    protected String theLoopVar;

    /**
     * String containing a number that is the worst acceptable match quality.
     */
    protected String theWorstAcceptable;

    /**
     * Iterator over the set of ViewletFactoryChoice.
     */
    protected Iterator<ViewletFactoryChoice> theIterator;
}
