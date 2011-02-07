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

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.StructuredResponseSection;

/**
 * Common functionality for tags that check whether informational messages have been reported.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public abstract class AbstractInfoMessagesTag
        extends
            AbstractSectionTestTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public AbstractInfoMessagesTag()
    {
        // noop
    }

    /**
     * Determine whether any informational messages have been reported.
     *
     * @return true errors have been reported
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected boolean haveInformationalMessagesBeenReportedAggregate()
        throws
            JspException,
            IgnoreException
    {
        boolean ret;

        StructuredResponseSection section = evaluate();
        if( section != null ) {
            ret = section.haveInfoMessagesBeenReported();
        } else {
            StructuredResponse response = (StructuredResponse) lookup( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
            if( response != null ) {
                ret = response.haveInfoMessagesBeenReportedAggregate();
            } else {
                ret = false;
            }
        }
        return ret;
    }
}
