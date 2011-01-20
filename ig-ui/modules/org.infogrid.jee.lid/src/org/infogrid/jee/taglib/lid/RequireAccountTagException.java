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
// Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.lid;

import org.infogrid.util.AbstractLocalizedException;

/**
 * Special-purpose exception only thrown by RequireAccountTag to report a problem to
 * the template framework.
 */
public class RequireAccountTagException
        extends
            AbstractLocalizedException
{
    /**
     * Constructor.
     */
    public RequireAccountTagException()
    {
        super( null, null );
    }

    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    public Object [] getLocalizationParameters()
    {
        return new Object[0];
    }
}
