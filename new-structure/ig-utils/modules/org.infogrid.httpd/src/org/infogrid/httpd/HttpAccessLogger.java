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

package org.infogrid.httpd;

/**
 * This interface is supported by objects that know how to log successful and
 * unsuccessful HTTP requests and responses.
 */
public interface HttpAccessLogger
{
    /**
     * A Request came in, and we successfully responded.
     *
     * @param theResponse the HttpResponse we log
     */
    public void logComplete(
            HttpResponse theResponse );

    /**
     * A Request came in, but the response was unsuccessful.
     *
     * @param theResponse the HttpResponse we log
     */
    public void logIncomplete(
            HttpResponse theResponse );
}
