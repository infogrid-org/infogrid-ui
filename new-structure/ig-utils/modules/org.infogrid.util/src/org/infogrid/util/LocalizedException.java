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

package org.infogrid.util;

import java.io.PrintStream;
import java.io.PrintWriter;
import org.infogrid.util.text.HasStringRepresentation;

/**
 * <p>Marker interface for Exceptions that know how to internationalize themselves.
 *    Given that Exceptions carry all their data, it is a lot easier to to
 *    ask the Exception how to internationalize itself, then to write outside
 *    code to do so.</p>
 *
 * <p>Unfortunately in Java, interfaces may not inherit from classes, so we have to
 *    copy-paste all the public method declarations from the Exception class.</p>
 */
public interface LocalizedException
        extends
            HasStringRepresentation
            // Exception -- we would love to say this here, but Java won't let us
{
    /**
     * Copied from Throwable.
     *
     * @return the message
     */
    public String getMessage();

    /**
     * Copied from Throwable.
     *
     * @return the localized message
     */
    public String getLocalizedMessage();

    /**
     * Copied from Throwable.
     *
     * @return the cause
     */
    public Throwable getCause();

    /**
     * Copied from Throwable.
     */
    public void printStackTrace();

    /**
     * Copied from Throwable.
     *
     * @param s <code>PrintStream</code> to use for output
     */
    public void printStackTrace(
            PrintStream s );

    /**
     * Copied from Throwable.
     *
     * @param s <code>PrintWriter</code> to use for output
     */
    public void printStackTrace(
            PrintWriter s );

    /**
     * Copied from Throwable.
     *
     * @return an array of stack trace elements representing the stack trace
     *         pertaining to this throwable.
     */
    public StackTraceElement[] getStackTrace();

    /**
     * When converting this object to its string representation, use this key by default.
     */
    public static final String STRING_REPRESENTATION_KEY = "String";

    /**
     * When converting this object to the link start string representation, use this key by default.
     */
    public static final String STRING_REPRESENTATION_LINK_START_KEY = "LinkStartString";

    /**
     * When converting this object to the link end string representation, use this key by default.
     */
    public static final String STRING_REPRESENTATION_LINK_END_KEY = "LinkEndString";
}
