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

package org.infogrid.jee.taglib.templates;

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.templates.BinaryStructuredResponseSection;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.templates.StructuredResponseSection;
import org.infogrid.jee.templates.TextStructuredResponseSection;

/**
 * Inlines a named section of a StructuredResponse into the output.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class InlineTag
        extends
             AbstractInfoGridTag    
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public InlineTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theName = null;

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
        StructuredResponse structured = (StructuredResponse) lookup( StructuredResponse.STRUCTURED_RESPONSE_ATTRIBUTE_NAME );
        if( structured == null ) {
            throw new JspException( "Cannot find StructuredResponse in the request context" );
        }

        StructuredResponseSection section = structured.findSectionByName( theName );
        if( section instanceof TextStructuredResponseSection ) {
            TextStructuredResponseSection realSection = (TextStructuredResponseSection) section;

            String content = structured.getSectionContent( realSection );
            print( content );

        } else if( section instanceof BinaryStructuredResponseSection ) {
            BinaryStructuredResponseSection realSection = (BinaryStructuredResponseSection) section;

            byte [] content = structured.getSectionContent( realSection );

            try {
                pageContext.getOut().flush();

                ServletOutputStream o = pageContext.getResponse().getOutputStream();
                o.write( content );

            } catch( IOException ex ) {
                throw new JspException( ex );
            }

        } else {
            if( theFormatter.isFalse( getIgnore() )) {
                throw new JspException( "Cannot find ResponseSection named " + theName );
            }
        }

        return SKIP_BODY;
    }

    /**
     * The name of the section in the StructuredResponse that will be inlined.
     */
    protected String theName;
}
