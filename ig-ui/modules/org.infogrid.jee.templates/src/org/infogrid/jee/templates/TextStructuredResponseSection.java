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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.templates;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * An actual section in a StructuredResponse that contains text.
 */
public class TextStructuredResponseSection
        extends
            StructuredResponseSection
{
    /**
     * Factory method.
     *
     * @param sectionTemplate the template that defines this section
     * @return the created TextStructuredResponseSection
     */
    public static TextStructuredResponseSection create(
            TextStructuredResponseSectionTemplate sectionTemplate )
    {
        TextStructuredResponseSection ret = new TextStructuredResponseSection( sectionTemplate );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param sectionTemplate the template that defines this section
     */
    protected TextStructuredResponseSection(
            TextStructuredResponseSectionTemplate sectionTemplate )
    {
        super( sectionTemplate );
    }

    /**
     * Obtain the StructuredResponseSectionTemplate that defines this section.
     * 
     * @return the StructuredResponseSectionTemplate
     */
    @Override
    public TextStructuredResponseSectionTemplate getSectionTemplate()
    {
        return (TextStructuredResponseSectionTemplate) super.getSectionTemplate();
    }

    /**
     * Determine whether this section is empty.
     * 
     * @return true if this section is empty
     */
    public boolean isEmpty()
    {
        if( theHttpResponseCode > 0 && theHttpResponseCode != 200 ) {
            return false;
        }
        if( theContent != null ) {
            return false;
        }
        if( !theOutgoingCookies.isEmpty() ) {
            return false;
        }
        if( !theCurrentProblems.isEmpty() ) {
            return false;
        }
        return true;
    }
    
    /**
     * Stream this StructuredResponseSection to an OutputStream.
     * 
     * @param s the OutputStream to write to
     * @return true if something was output, false otherwise
     * @throws IOException thrown if an I/O error occurred
     */
    public boolean doOutput(
            OutputStream s )
        throws
            IOException
    {
        if( theContent != null && theContent.length() > 0 ) {
            OutputStreamWriter w;
            if( theCharacterEncoding != null ) {
                w = new OutputStreamWriter( s, theCharacterEncoding );
            } else {
                w = new OutputStreamWriter( s );
            }
            w.write( theContent );
            w.flush();
            return true;

        } else {
            return false;
        }
    }


    /**
     * Obtain the current content of this section.
     * 
     * @return the current content of this section, or null
     */
    public String getContent()
    {
        return theContent;
    }

    /**
     * Set the content of this section.
     * 
     * @param newValue the new content of this section
     */
    public void setContent(
            String newValue )
    {
        theContent = newValue;
    }
    
    /**
     * Append to the content of this section.
     * 
     * @param toAppend the content to append to this section
     */
    public void appendContent(
            String toAppend )
    {
        if( theContent == null ) {
            theContent = toAppend;
        } else {
            theContent += toAppend;
        }
    }

    /**
     * Determine whether this section containsContent this content fragment already.
     *
     * @param testContent the content fragment to test
     * @return true if this section containsContent the testContent already
     */
    public boolean containsContent(
            String testContent )
    {
        int found = theContent.indexOf( testContent );
        return found >= 0;
    }

    /**
     * Content of this section, if any.
     */
    protected String theContent;
}
