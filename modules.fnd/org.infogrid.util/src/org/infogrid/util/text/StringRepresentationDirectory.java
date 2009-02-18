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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.util.text;

import org.infogrid.util.SmartFactory;

/**
 * A directory of StringRepresentations.
 */
public interface StringRepresentationDirectory
        extends
            SmartFactory<String,StringRepresentation,StringRepresentation>
{
    /**
     * Obtain the fallback. This fallback is known to exist even if the factory method failed.
     * 
     * @return the fallback StringRepresentation
     */
    public StringRepresentation getFallback();
    
    /**
     * Name of the StringRepresentation, contained in this StringRepresentationDirectory, that contains
     * the default text/plain formatting.
     */
    public static final String TEXT_PLAIN_NAME = "Plain";
    
    /**
     * Name of the StringRepresentation, contained in this StringRepresentationDirectory, that contains
     * the default text/html formatting.
     */
    public static final String TEXT_HTML_NAME = "Html";

    /**
     * Name of the StringRepresentation, which may be contained in this StringRepresentationDirectory,
     * that contains the default Java formatting.
     */
    public static final String TEXT_JAVA_NAME = "Java";

    /**
     * Name of the StringRepresentation, which may be contained in this StringRepresentationDirectory,
     * that contains the default JavaDoc formatting.
     *
     * Use lowercase d in Javadoc to avoid name collisions with entries in resource files that
     * belong to the "Java" formatting and start with "Doc".
     */
    public static final String TEXT_JAVADOC_NAME = "Javadoc";
}
