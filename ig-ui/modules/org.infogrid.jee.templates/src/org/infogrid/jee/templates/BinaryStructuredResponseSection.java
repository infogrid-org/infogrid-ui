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
import java.io.OutputStream;

/**
 * An actual section in a StructuredResponse that contains text.
 */
public class BinaryStructuredResponseSection
        extends
            StructuredResponseSection
{
    /**
     * Factory method.
     *
     * @param sectionTemplate the template that defines this section
     * @return the created BinaryStructuredResponseSection
     */
    public static BinaryStructuredResponseSection create(
            BinaryStructuredResponseSectionTemplate sectionTemplate )
    {
        BinaryStructuredResponseSection ret = new BinaryStructuredResponseSection( sectionTemplate );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param sectionTemplate the template that defines this section
     */
    protected BinaryStructuredResponseSection(
            BinaryStructuredResponseSectionTemplate sectionTemplate )
    {
        super( sectionTemplate );
    }

    /**
     * Obtain the StructuredResponseSectionTemplate that defines this section.
     * 
     * @return the StructuredResponseSectionTemplate
     */
    @Override
    public BinaryStructuredResponseSectionTemplate getSectionTemplate()
    {
        return (BinaryStructuredResponseSectionTemplate) super.getSectionTemplate();
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
        if( theContent != null && theContent.length > 0 ) {
            s.write( theContent );
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
    public byte [] getContent()
    {
        return theContent;
    }

    /**
     * Set the content of this section.
     * 
     * @param newValue the new content of this section
     */
    public void setContent(
            byte [] newValue )
    {
        theContent = newValue;
    }
    
    /**
     * Append to the content of this section.
     * 
     * @param toAppend the content to append to this section
     */
    public void appendContent(
            byte [] toAppend )
    {
        if( theContent == null ) {
            theContent = toAppend;
        } else {
            byte [] old = theContent;
            theContent = new byte[ old.length + toAppend.length ];
            System.arraycopy( old,      0, theContent, 0,          old.length );
            System.arraycopy( toAppend, 0, theContent, old.length, old.length );
        }
    }

    /**
     * Content of this section, if any.
     */
    protected byte [] theContent;
}
