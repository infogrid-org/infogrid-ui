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

package org.infogrid.jee.shell.http;

import org.infogrid.util.AbstractLocalizedException;

/**
 * Thrown when something went wrong in the
 * {@link org.infogrid.jee.shell.http.HttpShellFilter HttpShellFilter}. For
 * details, consider the cause of the exception using <code>getCause()</code>.
 */
public final class HttpShellException
    extends
        AbstractLocalizedException
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor. This is the only constructor provided as this must be invoked with
     * a delegate Throwable.
     *
     * @param cause the underlying cause
     */
    public HttpShellException(
            Throwable cause )
    {
        super( null, cause ); // use this constructor, in order to avoid that Throwable calls cause.toString().
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    public Object [] getLocalizationParameters()
    {
        return new Object[] { getCause() };
    }
}
