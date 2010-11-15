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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.shell.http;

/**
 * Implemented by classes that can be invoked after the HttpShell is done with its processing.
 */
public abstract class AbstractHttpShellHandler
        implements
            HttpShellHandler
{
    /**
     * Obtain the name of the handler.
     *
     * @return the name1
     */
    public String getName()
    {
        return getClass().getName();
    }
}
