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

package org.infogrid.jee.taglib.util;

import java.io.IOException;
import java.util.Set;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.util.ArrayHelper;

/**
 * <p>Declares a JSP overlay as a subroutine, potentially with parameters.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class JspoTag
    extends
        AbstractInfoGridTag

{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public JspoTag()
    {
        // noop
    }

    /**
     * Release all of our resources.
     */
    @Override
    protected void initializeToDefaults()
    {
        theCallRecord = null;

        super.initializeToDefaults();
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
        theCallRecord = (CallJspoRecord) pageContext.getRequest().getAttribute( CallJspXRecord.CALL_JSPX_RECORD_ATTRIBUTE_NAME );
        if( theCallRecord == null ) {
            throw new JspException( "This JSP overlay must only be invoked using CallJspo" );
        }
        if( theCallRecord.getIsDisabled() ) {
            return SKIP_BODY;
        } else {
            return EVAL_BODY_INCLUDE; // contains parameter declarations
        }
    }

    /**
     * Our implementation of doEndTag(), to be provided by subclasses.
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
        try {
            if( !theCallRecord.getIsDisabled() ) {
                Set<String> remainingParameters = theCallRecord.getRemainingParameters();
                if( !remainingParameters.isEmpty() ) {
                    throw new JspException(
                            "Parameters passed were not declared: "
                            + ArrayHelper.collectionToString( remainingParameters, ", " ) + "." );
                }
            }
            return EVAL_PAGE;
        } finally {
            theCallRecord.restoreAttributes( pageContext.getRequest() );
        }
    }

    /**
     * The current call record.
     */
    CallJspoRecord theCallRecord;
}
