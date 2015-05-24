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

package org.infogrid.jee.taglib.viewlet;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.jee.viewlet.JeeViewletState;

/**
 * Collects functionality common to tags that evaluate Viewlet state.
 */
public abstract class AbstractViewletStateConditionTag
        extends
            AbstractInfoGridTag
{
    /**
     * Constructor.
     */
    public AbstractViewletStateConditionTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theViewletState = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the viewletState property.
     *
     * @return value of the viewletState property
     * @see #setViewletState
     */
    public final String getViewletState()
    {
        return theViewletState;
    }

    /**
     * Set value of the viewletState property.
     *
     * @param newValue new value of the viewletState property
     * @see #getViewletState
     */
    public final void setViewletState(
            String newValue )
    {
        theViewletState = newValue;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    @Override
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        if( evaluateTest() ) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Determine the current Viewlet state.
     *
     * @return the Viewlet state
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected JeeViewletState evaluate()
        throws
            JspException,
            IgnoreException
    {
        JeeViewlet currentViewlet = (JeeViewlet) lookupOrThrow( JeeViewlet.VIEWLET_ATTRIBUTE_NAME );

        JeeViewletState ret = currentViewlet.getViewletState();
        return ret;
    }

    /**
     * Evaluate the condition. If it returns true, we include, in the output,
     * the content contained in this tag. This is abstract as concrete
     * subclasses of this class need to have the ability to determine what
     * their evaluation criteria are.
     *
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected abstract boolean evaluateTest()
        throws
            JspException,
            IgnoreException;

    /**
     * The ViewletState to test against.
     */
    protected String theViewletState;
}
