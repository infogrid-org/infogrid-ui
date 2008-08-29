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

package org.infogrid.util;

import java.io.IOException;

/**
 * Subclass of <code>java.io.IOException</code> that can carry a <code>Throwable</code> as a
 * payload. Unfortunately Java IOException's constructor does not allow us to specify
 * a cause directly, so this class is a workaround.
 */
public class DelegatingIOException
        extends
            IOException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     *
     * @param cause the cause
     */
    public DelegatingIOException(
            Throwable cause )
    {
        super.initCause( cause ); // stupid API
    }

    /**
     * Constructor.
     *
     * @param msg the message
     * @param cause the cause
     */
    public DelegatingIOException(
            String    msg,
            Throwable cause )
    {
        super( msg );

        super.initCause( cause ); // stupid API
    }
}
