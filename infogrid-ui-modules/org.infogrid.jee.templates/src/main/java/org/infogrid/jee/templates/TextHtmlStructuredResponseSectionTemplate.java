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

package org.infogrid.jee.templates;

/**
 * A section in a StructuredResponse that contains HTML text.
 */
public class TextHtmlStructuredResponseSectionTemplate
        extends
            TextStructuredResponseSectionTemplate
{
    /**
     * Factory method.
     *
     * @param name the name of the section
     * @return the created StructuredResponseSection
     */
    public static TextHtmlStructuredResponseSectionTemplate create(
            String name )
    {
        TextHtmlStructuredResponseSectionTemplate ret = new TextHtmlStructuredResponseSectionTemplate( name, DEFAULT_MAX_PROBLEMS, DEFAULT_MAX_INFO_MESSAGES );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param name the name of the section
     * @param maxProblems the maximum number of problems to store in sections of this type
     * @param maxInfoMessages the maximum number of informational messages to store in sections of this type
     * @return the created StructuredResponseSectionTemplate
     */
    public static TextHtmlStructuredResponseSectionTemplate create(
            String name,
            int    maxProblems,
            int    maxInfoMessages )
    {
        TextHtmlStructuredResponseSectionTemplate ret = new TextHtmlStructuredResponseSectionTemplate( name, maxProblems, maxInfoMessages );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param name the name of the section
     * @param maxProblems the maximum number of problems to store in sections of this type
     * @param maxInfoMessages the maximum number of informational messages to store in sections of this type
     */
    protected TextHtmlStructuredResponseSectionTemplate(
            String name,
            int    maxProblems,
            int    maxInfoMessages )
    {
        super( name, maxProblems, maxInfoMessages );
    }
}
