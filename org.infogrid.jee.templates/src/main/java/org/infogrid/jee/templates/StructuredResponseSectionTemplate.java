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

package org.infogrid.jee.templates;

import org.infogrid.util.ResourceHelper;

/**
 * Defines a section in a StructuredResponse.
 */
public abstract class StructuredResponseSectionTemplate
{
    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param name the name of the section
     * @param maxProblems the maximum number of problems to store in sections of this type
     * @param maxInfoMessages the maximum number of informational messages to store in sections of this type
     */
    protected StructuredResponseSectionTemplate(
            String name,
            int    maxProblems,
            int    maxInfoMessages )
    {
        theSectionName     = name;
        theMaxProblems     = maxProblems;
        theMaxInfoMessages = maxInfoMessages;
    }

    /**
     * Obtain the section name.
     * 
     * @return the section name
     */
    public String getSectionName()
    {
        return theSectionName;
    }

    /**
     * Determine the maximum number of problems to store in this type of section.
     * 
     * @return the maximum number of problems
     */
    public int getMaxProblems()
    {
        return theMaxProblems;
    }

    /**
     * Determine the maximum number of informational messages to store in this type of section.
     *
     * @return the maximum number of informational messages
     */
    public int getMaxInfoMessages()
    {
        return theMaxInfoMessages;
    }

    /**
     * Determine equality.
     * 
     * @param other the other Object to compare to
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( other instanceof StructuredResponseSectionTemplate ) {
            StructuredResponseSectionTemplate realOther = (StructuredResponseSectionTemplate) other;
            return theSectionName.equals( realOther.theSectionName );
        }
        return false;
    }

    /**
     * Hash code.
     * 
     * @return hash code
     */
    @Override
    public int hashCode()
    {
        return theSectionName.hashCode();
    }

    /**
     * Name of the section.
     */
    protected String theSectionName;
    
    /**
     * The maximum number of problems to store in this type of section.
     */
    protected int theMaxProblems;

    /**
     * The maximum number of informational messages to store in this type of section.
     */
    protected int theMaxInfoMessages;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( StructuredResponseSectionTemplate.class );

    /**
     * The default maximum number of problems to store.
     */
    public static final int DEFAULT_MAX_PROBLEMS = theResourceHelper.getResourceIntegerOrDefault( "DefaultMaxProblems", 20 );

    /**
     * The default maximum number of informational messages to store.
     */
    public static final int DEFAULT_MAX_INFO_MESSAGES = theResourceHelper.getResourceIntegerOrDefault( "DefaultMaxInfoMessages", 20 );
}
