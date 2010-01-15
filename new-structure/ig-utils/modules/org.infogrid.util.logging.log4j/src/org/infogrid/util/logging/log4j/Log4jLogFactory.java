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

package org.infogrid.util.logging.log4j;

import org.infogrid.util.logging.BufferingDumperFactory;
import org.infogrid.util.logging.Log;
import org.infogrid.util.logging.LogFactory;

/**
 * Implements of <code>LogFactory</code> for the log4j Logger.
 */
public class Log4jLogFactory
        extends
            LogFactory
{
    /**
     * Obtain a Logger and specify a dumper factory instead of the default.
     *
     * @param name name of the Log object that we are looking for
     * @param dumperFactory the factory for dumpers
     * @return the Log object
     */
    public Log create(
            String                 name,
            BufferingDumperFactory dumperFactory )
    {
        return new Log4jLog( name, dumperFactory );
    }

    /**
     * <p>Show a message to the user interface. This requires an implementation
     *    of the LogFactory that is appropriate for the user interface used by the
     *    application, e.g. AWT dialogs or HTML pages.</p>
     * <p>As an application developer, you should never have to invoke this; instead,
     *    use {@link Log#showMessage Log.showMessage}. However, if you implement a new
     *    LogFactory, obviously you have to develop an implementation of this method.</p>
     *
     * @param logger the logger from which this invocation is delegated
     * @param parentComponent the component to which the error message is reported, e.g.
     *        an AWT component, or an HTML page
     * @param message the error message
     * @param title the title of the dialog
     */
    public void showMessage(
            Log    logger,
            Object parentComponent,
            String message,
            String title)
    {
        // by default, do nothing
    }
}
