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

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;

/**
 * Tag identifying the content portion of a tab.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class TabContentTag
    extends
        AbstractTabChild
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public TabContentTag()
    {
        // noop
    }

    /**
     * Initialize all default values.
     */
    @Override
    protected void initializeToDefaults()
    {
        super.initializeToDefaults();
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
        TabTag    ourTab       = findEnclosingTabTagOrThrow( this );
        TabbedTag ourContainer = findEnclosingTabbedTagOrThrow( ourTab );

        if( ourContainer.isHead() || getFormatter().isFalse( ourTab.getIsSelected() )) {
            return SKIP_BODY;
        } else {
            return EVAL_BODY_INCLUDE;
        }
    }
}
