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
 * Defines a section in a StructuredResponse that contains text.
 */
public class TextStructuredResponseSectionTemplate
        extends
            StructuredResponseSectionTemplate
{
    /**
     * Factory method.
     *
     * @param name the name of the section
     * @return the created StructuredResponseSectionTemplate
     */
    public static TextStructuredResponseSectionTemplate create(
            String name )
    {
        TextStructuredResponseSectionTemplate ret = new TextStructuredResponseSectionTemplate( name, DEFAULT_MAX_PROBLEMS );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param name the name of the section
     * @param maxProblems the maximum number of problems to store in sections of this type
     * @return the created StructuredResponseSectionTemplate
     */
    public static TextStructuredResponseSectionTemplate create(
            String name,
            int    maxProblems )
    {
        TextStructuredResponseSectionTemplate ret = new TextStructuredResponseSectionTemplate( name, maxProblems );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param name the name of the section
     * @param maxProblems the maximum number of problems to store in sections of this type
     */
    protected TextStructuredResponseSectionTemplate(
            String name,
            int    maxProblems )
    {
        super( name, maxProblems );
    }

    /**
     * The single default section for text content. Output will be written into this section
     * unless otherwise specified.
     */
    public static final TextStructuredResponseSectionTemplate DEFAULT_SECTION
            = TextStructuredResponseSectionTemplate.create( "default" );
}
