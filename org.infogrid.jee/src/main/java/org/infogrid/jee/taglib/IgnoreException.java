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

package org.infogrid.jee.taglib;

/**
 * Thrown if this tag should be ignored per the ignore property
 * (which was popularized by Apache Struts). Using this Exception makes the API much
 * simpler.
 */
public class IgnoreException
        extends
            Exception
{
    private static final long serialVersionUID = 1L; // helps with serialization and to get rid of pesky NetBeans warnings
    
    /**
     * Parameter-less constructor.
     */
    public IgnoreException()
    {
        // noop, only here for JavaDoc purposes.
    }
}
