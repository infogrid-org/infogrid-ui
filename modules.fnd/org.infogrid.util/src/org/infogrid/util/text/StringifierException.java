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

package org.infogrid.util.text;

import org.infogrid.util.AbstractLocalizedException;

/**
 * Superclass for Exceptions raised by Stringifiers.
 */
public abstract class StringifierException
        extends
            AbstractLocalizedException
{
    /**
     * Constructor.
     *
     * @param source the Stringifier that raised this exception
     */
    public StringifierException(
            Stringifier source )
    {
        theSource = source;
    }

    /**
     * Constructor.
     *
     * @param source the Stringifier that raised this exception
     * @param cause the underlying cause
     */
    public StringifierException(
            Stringifier source,
            Throwable   cause )
    {
        super( cause );
        theSource = source;
    }

    /**
     * The Stringifier that raised this exception.
     */    
    protected Stringifier theSource;
}
