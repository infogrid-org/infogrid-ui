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
 * A section in a StructuredResponse that contains binary data.
 */
public class BinaryStructuredResponseSection
        extends
            StructuredResponseSection
{
    /**
     * Factory method.
     *
     * @param name the name of the section
     * @return the created StructuredResponseSection
     */
    public static BinaryStructuredResponseSection create(
            String name )
    {
        BinaryStructuredResponseSection ret = new BinaryStructuredResponseSection( name );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param name the name of the section
     */
    protected BinaryStructuredResponseSection(
            String name )
    {
        super( name );
    }

    /**
     * Stream the StructuredResponse to an HttpResponse.
     * 
     * @param s the OutputStream to write to
     * @param structured the StructuredResponse that holds the data
     * @return true if something was output, false otherwise
     * @throws IOException thrown if an I/O error occurred
     */
    public boolean doOutput(
            OutputStream        s,
            StructuredResponse  structured )
        throws
            IOException
    {
        byte [] sectionContent = structured.getSectionContent( this );
        if( sectionContent == null ) {
            return false;
        }

        s.write( sectionContent );
        
        return true;
    }

    /**
     * The single default section for binary content. Output will be written into this section
     * unless otherwise specified.
     */
    public static final BinaryStructuredResponseSection DEFAULT_SECTION
            = BinaryStructuredResponseSection.create( "default" );
}