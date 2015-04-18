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

package org.infogrid.jee.defaultapp;

import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;

/**
 * The InfoGridWebApp when using the defaultapp template.
 */
public class DefaultInfoGridWebApp
        extends
            InfoGridWebApp
{
    /**
     * Constructor.
     *
     * @param firstRequest the first incoming request
     * @param applicationContext the main application Context. This context holds all the
     *        well-known objects needed by the application
     */
    public DefaultInfoGridWebApp(
            SaneRequest firstRequest,
            Context     applicationContext )
    {
        super( firstRequest, applicationContext );
    }
}
