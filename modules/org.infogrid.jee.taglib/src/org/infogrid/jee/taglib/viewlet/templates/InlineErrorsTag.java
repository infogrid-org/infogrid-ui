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
// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.viewlet.templates;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.templates.JspStructuredResponseTemplate;
import org.infogrid.jee.templates.StructuredResponse;

/**
 * Inlines the reported errors into the output.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class InlineErrorsTag
        extends
             AbstractInfoGridTag    
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public InlineErrorsTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theStringRepresentation = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the stringRepresentation property.
     *
     * @return value of the stringRepresentation property
     * @see #setStringRepresentation
     */
    public final String getStringRepresentation()
    {
        return theStringRepresentation;
    }

    /**
     * Set value of the stringRepresentation property.
     *
     * @param newValue new value of the stringRepresentation property
     * @see #getStringRepresentation
     */
    public final void setStringRepresentation(
            String newValue )
    {
        theStringRepresentation = newValue;
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
        StructuredResponse structured = (StructuredResponse) lookup( JspStructuredResponseTemplate.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
        if( structured == null ) {
            throw new JspException( "Cannot find StructuredResponse in the request context" );
        }

        String content;
        if( "html".equalsIgnoreCase( theStringRepresentation )) {
            content = structured.getErrorContentAsHtml();
        } else {
            content = structured.getErrorContentAsPlain();
        }
        
        print( content );
        return SKIP_BODY;
    }

    /**
     * The type of StringRepresentation.
     */
    protected String theStringRepresentation;
}
