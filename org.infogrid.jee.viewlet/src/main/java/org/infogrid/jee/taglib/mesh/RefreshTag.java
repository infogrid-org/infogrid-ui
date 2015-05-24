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

package org.infogrid.jee.taglib.mesh;

import javax.servlet.jsp.JspException;
import java.io.IOException;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.TextStructuredResponseSection;
import org.infogrid.util.http.SaneRequest;
   
/**
 * <p>Generates a consistent refresh button.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class RefreshTag
        extends
             AbstractInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public RefreshTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        super.initializeToDefaults();

        theTarget = null;
    }

    /**
     * Set the target property.
     *
     * @param newValue the new value
     * @see #getTarget
     */
    public void setTarget(
            String newValue )
    {
        theTarget = newValue;
    }

    /**
     * Obtain the target property.
     *
     * @return the value
     * @see #setTarget
     */
    public String getTarget()
    {
        return theTarget;
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
        StructuredResponse theResponse = (StructuredResponse) lookupOrThrow( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
        SaneRequest        saneRequest = (SaneRequest)        lookupOrThrow( SaneServletRequest.SANE_SERVLET_REQUEST_ATTRIBUTE_NAME );

        String href = saneRequest.getAbsoluteFullUri();
        href = getFormatter().filter( href );
        
        StringBuilder buf = new StringBuilder();
        buf.append( "<div class=\"" ).append( getClass().getName().replace( '.', '-' )).append( "\">" );
        buf.append( "<a href=\"" ).append( href );
        if( theTarget != null ) {
            buf.append( "\" target=\"" ).append( theTarget );
        }
        buf.append( "\">" );

        print( buf.toString() );

        StringBuilder css = new StringBuilder();
        css.append( "<link rel=\"stylesheet\" href=\"" );
        css.append( saneRequest.getContextPath() );
        css.append( "/v/" );
        css.append( getClass().getName().replace( '.' , '/' ));
        css.append( ".css" );
        css.append( "\" />\n" );

        TextStructuredResponseSection headSection = theResponse.obtainTextSection( StructuredResponse.HTML_HEAD_SECTION );
        headSection.appendContent( css.toString() );

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Our implementation of doEndTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IOException thrown if an I/O Exception occurred
     */
    @Override
    protected int realDoEndTag()
        throws
            JspException,
            IOException
    {
        print( "</a></div>" );

        return EVAL_PAGE;
    }

    /**
     * The target, if any.
     */
    protected String theTarget;
}
