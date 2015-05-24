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

package org.infogrid.jee;

import java.util.Iterator;
import org.infogrid.util.http.SaneRequestUtils;

/**
 * An object with this type may be found in the request context. It allows
 * applications to report problems during the processing of an incoming JEE request,
 * without having to abort processing. They can be processed later and inserted
 * as error messages into a fully-assembled HTML page, for example.
 */
public interface ProblemReporter
{
    /**
     * Report a problem that should be shown to the user.
     *
     * @param t the Throwable indicating the problem
     */
    public void reportProblem(
            Throwable t );

    /**
     * Convenience method to report several problems that should be shown to the user.
     *
     * @param ts [] the Throwables indicating the problems
     */
    public void reportProblems(
            Throwable [] ts );

    /**
     * Determine whether problems have been reported.
     *
     * @return true if at least one problem has been reported
     */
    public boolean haveProblemsBeenReported();

    /**
     * Obtain the problems reported so far.
     *
     * @return problems reported so far, in sequence
     */
    public Iterator<Throwable> problems();

    /**
     * Report an informational message that should be shown to the user.
     *
     * @param t the Throwable indicating the message
     */
    public void reportInfoMessage(
            Throwable t );

    /**
     * Convenience method to report several informational messages that should be shown to the user.
     *
     * @param ts [] the Throwables indicating the informational messages
     */
    public void reportInfoMessages(
            Throwable [] ts );

    /**
     * Determine whether informational messages have been reported.
     *
     * @return true if at least one informational message has been reported
     */
    public boolean haveInfoMessagesBeenReported();

    /**
     * Obtain the informational messages reported so far.
     *
     * @return informational messages reported so far, in sequence
     */
    public Iterator<Throwable> infoMessages();

    /**
     * Name of a Request-level attribute that contains an object of this type, if any.
     */
    public static final String PROBLEM_REPORTER_ATTRIBUTE_NAME
            = SaneRequestUtils.classToAttributeName( ProblemReporter.class );
}
