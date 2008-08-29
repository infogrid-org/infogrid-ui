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

package org.infogrid.jee.templates;

import java.io.IOException;
import java.io.Writer;

/**
 * A section in a StructuredResponse that contains text.
 */
public class TextStructuredResponseSection
        extends
            StructuredResponseSection
{
    /**
     * Factory method.
     *
     * @param name the name of the section
     * @return the created StructuredResponseSection
     */
    public static TextStructuredResponseSection create(
            String name )
    {
        TextStructuredResponseSection ret = new TextStructuredResponseSection( name );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param name the name of the section
     */
    protected TextStructuredResponseSection(
            String name )
    {
        super( name );
    }

    /**
     * Stream the StructuredResponse to an HttpResponse.
     * 
     * @param w the Writer to write to
     * @param structured the StructuredResponse that holds the data
     * @return true if something was output, false otherwise
     * @throws IOException thrown if an I/O error occurred
     */
    public boolean doOutput(
            Writer             w,
            StructuredResponse structured )
        throws
            IOException
    {
        String sectionContent = structured.getSectionContent( this );
        if( sectionContent == null ) {
            return false;
        }

        w.append( sectionContent );
        
        return true;
    }

    /**
     * The single default section for text content. Output will be written into this section
     * unless otherwise specified.
     */
    public static final TextStructuredResponseSection DEFAULT_SECTION
            = TextStructuredResponseSection.create( "default" );
}
