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

package org.infogrid.meshbase.net.xpriso.logging;

import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.util.logging.BufferingDumperFactory;
import org.infogrid.util.logging.Log;

/**
 * Logs Xpriso messages to a Log instance.
 */
public class LogXprisoMessageLogger
        extends
            AbstractXprisoMessageLogger
{
    /**
     * Factory method.
     *
     * @param log the Log to use
     * @return the created LogXprisoMessageLogger
     */
    public static LogXprisoMessageLogger create(
            Log log )
    {
        XprisoMessageDumperFactory dumperFactory = XprisoMessageDumperFactory.create();

        return new LogXprisoMessageLogger( log, dumperFactory );
    }

    /**
     * Factory method.
     *
     * @param log the Log to use
     * @param dumperFactory the BufferingDumperFactory to use
     * @return the created LogXprisoMessageLogger
     */
    public static LogXprisoMessageLogger create(
            Log                    log,
            BufferingDumperFactory dumperFactory )
    {
        return new LogXprisoMessageLogger( log, dumperFactory );
    }

    /**
     * Private constructor for subclasses only.
     *
     * @param log the Log to use
     * @param dumperFactory the BufferingDumperFactory to use
     */
    protected LogXprisoMessageLogger(
            Log                    log,
            BufferingDumperFactory dumperFactory )
    {
        theLog                  = log;
        theMessageDumperFactory = dumperFactory;
    }

    /**
     * Actual logging method.
     *
     * @param msg the XprisoMessage
     * @param args other arguments
     */
    protected void log(
            XprisoMessage msg,
            Object...     args )
    {
        if( args != null && args.length > 0 ) {
            Object [] realArgs = new Object[ args.length + 2 ];
            realArgs[0] = theMessageDumperFactory;
            realArgs[1] = msg;
            for( int i=0 ; i<args.length ; ++i ) {
                realArgs[i+2] = args[i];
            }
            theLog.info( realArgs );
        } else {
            theLog.info( theMessageDumperFactory, msg );
        }
    }

    /**
     * Logging method for errors.
     *
     * @param msg the XprisoMessage
     * @param error the error message
     */
    protected void logError(
            XprisoMessage msg,
            String        error )
    {
        theLog.error( theMessageDumperFactory, msg, error );
    }

    /**
     * The Log to write to.
     */
    protected Log theLog;

    /**
     * The BufferingDumper to use.
     */
    protected BufferingDumperFactory theMessageDumperFactory;
}
