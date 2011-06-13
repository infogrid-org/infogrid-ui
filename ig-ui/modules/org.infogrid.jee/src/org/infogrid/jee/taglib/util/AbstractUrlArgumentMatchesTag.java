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

package org.infogrid.jee.taglib.util;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.util.http.SaneRequest;

/**
 * Factors out common code for tags that examine a condition derived from
 * the current URL.</code>.
 */
public abstract class AbstractUrlArgumentMatchesTag
    extends
        AbstractInfoGridBodyTag
{
    /**
     * Constructor.
     */
    public AbstractUrlArgumentMatchesTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theName  = null;
        theValue = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the name property.
     *
     * @return value of the name property
     * @see #setName
     */
    public final String getName()
    {
        return theName;
    }

    /**
     * Set value of the name property.
     *
     * @param newValue new value of the name property
     * @see #getName
     */
    public final void setName(
            String newValue )
    {
        theName = newValue;
    }

    /**
     * Obtain value of the value property.
     *
     * @return value of the value property
     * @see #setValue
     */
    public final String getValue()
    {
        return theValue;
    }

    /**
     * Set value of the value property.
     *
     * @param newValue new value of the value property
     * @see #getValue
     */
    public final void setValue(
            String newValue )
    {
        theValue = newValue;
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
        SaneRequest sane    = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );
        boolean     matches = sane.matchUrlArgument( theName, theValue );

        if( evaluateTest( matches ) ) {
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
     * @param flag if true, the current URL contains the argument name with value value
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     */
    protected abstract boolean evaluateTest(
            boolean flag )
        throws
            JspException;

    /**
     * Name of the URL argument to check.
     */
    protected String theName;

    /**
     * Value of the URL argument to check.
     */
    protected String theValue;
}
