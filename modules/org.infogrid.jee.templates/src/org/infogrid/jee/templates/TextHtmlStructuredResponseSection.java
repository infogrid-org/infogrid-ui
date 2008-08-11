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

/**
 * A section in a StructuredResponse that contains HTML text.
 */
public class TextHtmlStructuredResponseSection
        extends
            TextStructuredResponseSection
{
    /**
     * Factory method.
     *
     * @param name the name of the section
     * @return the created StructuredResponseSection
     */
    public static TextHtmlStructuredResponseSection create(
            String name )
    {
        TextHtmlStructuredResponseSection ret = new TextHtmlStructuredResponseSection( name );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param name the name of the section
     */
    protected TextHtmlStructuredResponseSection(
            String name )
    {
        super( name );
    }

    /**
     * The single default section for HTML text content. Output will be written into this section
     * unless otherwise specified.
     */
    public static final TextHtmlStructuredResponseSection HTML_HEAD_SECTION
            = TextHtmlStructuredResponseSection.create( "html-head" );
}
