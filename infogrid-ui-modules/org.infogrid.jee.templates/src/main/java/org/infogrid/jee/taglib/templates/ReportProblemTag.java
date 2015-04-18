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

package org.infogrid.jee.taglib.templates;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.templates.StructuredResponse;

/**
 * <p>Report an error via JSP.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class ReportProblemTag
    extends
        AbstractInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public ReportProblemTag()
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
     * Our implementation of doStartTag(), to be provided by subclasses.
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
        return EVAL_BODY_BUFFERED;
    }

    /**
     * Our implementation of doAfterBody(), to be provided by subclasses.
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
        BodyContent body = getBodyContent();
        if( body == null ) {
            return SKIP_BODY;
        }
        String bodyString = body.getString();
        if( bodyString == null ) {
            return SKIP_BODY;
        }
        bodyString = bodyString.trim();

        if( bodyString.length() == 0 ) {
            return SKIP_BODY;
        }
        
        StructuredResponse response = (StructuredResponse) lookup( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
        if( response == null ) {
            return SKIP_BODY;
        }
        response.reportProblem( new ReportException( bodyString ));

        return SKIP_BODY; // this is the default, subclasses may override
    }
}
