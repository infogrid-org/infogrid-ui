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

package org.infogrid.util.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * A PrintWriter suitable to write to a Log.
 */
public abstract class LogPrintWriter
    extends
        PrintWriter
{
    /**
     * Constructor.
     */
    public LogPrintWriter()
    {
        super( new StringWriter() );
    }

    /**
     * Flush the stream.
     */
    @Override
    public void flush()
    {
        super.flush();

        StringWriter w;
        synchronized( lock ) {
            w = (StringWriter) out;
            out = new StringWriter();
        }

        log( w.toString() );
    }

    /**
     * Define this to write to the right log and channel.
     * 
     * @param buffer the content to write
     */
    public abstract void log(
            String buffer );
}
