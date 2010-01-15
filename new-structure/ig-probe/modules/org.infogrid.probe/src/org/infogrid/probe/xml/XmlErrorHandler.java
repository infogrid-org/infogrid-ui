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

package org.infogrid.probe.xml;

import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.probe.ProbeException;

import org.infogrid.util.logging.Log;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
  * Our default error listener for the XML parser.
  */
public class XmlErrorHandler
        implements
            ErrorHandler
{
    /**
     * Constructor.
     * 
     * @param id the NetMeshBaseIdentifier we attempted to access
     * @param l the logger where to log errors to
     */
    public XmlErrorHandler(
            NetMeshBaseIdentifier id,
            Log                   l )
    {
        theNetworkIdentifier = id;
        log                  = l;
    }

    /**
     * Callback from ErrorHandler.
     *
     * @param ex the Exception that occurred
     */
    public void warning(
            SAXParseException ex )
    {
        ++theNumberOfErrors;

        if( firstException == null ) {
            firstException = ex;
        }

        if( log.isInfoEnabled() ) {
            log.info( ex );
        }
    }

    /**
     * Callback from ErrorHandler.
     *
     * @param ex the Exception that occurred
     */
    public void error(
            SAXParseException ex )
    {
        ++theNumberOfErrors;

        if( firstException == null ) {
            firstException = ex;
        }

        if( log.isInfoEnabled() ) {
            log.info( ex );
        }
    }

    /**
     * Callback from ErrorHandler.
     *
     * @param ex the Exception that occurred
     */
    public void fatalError(
            SAXParseException ex )
    {
        ++theNumberOfErrors;

        if( firstException == null ) {
            firstException = ex;
        }

        if( log.isInfoEnabled() ) {
            log.info( ex );
        }
    }

    /**
      * Obtain the current number of errors logged.
      *
      * @return the number of errors logged
      */
    public int numberOfErrors()
    {
        return theNumberOfErrors;
    }

    /**
     * Obtain the errors as an Exception.
     *
     * @return the errors as an Exception, or null if none
     */
    public ProbeException.SyntaxError getAsException()
    {
        switch( theNumberOfErrors )
        {
        case 0:
            return null;

        case 1:
            return new ProbeException.SyntaxError( theNetworkIdentifier, firstException );

        default:
            return new ProbeException.SyntaxError( theNetworkIdentifier, String.valueOf( theNumberOfErrors ) + " errors. First error: " + firstException, null );
        }
    }

    /**
     * The NetMeshBaseIdentifier whose access caused this error.
     */
    protected NetMeshBaseIdentifier theNetworkIdentifier;

    /**
     * Error counter.
     */
    protected int theNumberOfErrors = 0;

    /**
     * The first Throwable.
     */
    protected Throwable firstException = null;

    /**
     * Where we log our errors.
     */
    protected Log log;
}
