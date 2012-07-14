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
// Copyright 1998-2012 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.util;

/**
 * Captures a JSPF call.
 */
class CallJspoRecord
    extends
        CallJspXRecord
{
    /**
     * Constructor.
     *
     * @param page name of the page that is being invoked
     * @param disabled true if the popup is disabled
     */
    public CallJspoRecord(
            String  page,
            boolean disabled )
    {
        super( page );
        
        theDisabled = disabled;
    }
    
    /**
     * Determine whether the popup is disabled.
     * 
     * @return true if the popup is disabled
     */
    public boolean getIsDisabled()
    {
        return theDisabled;
    }

    /**
     * Is the popup disabled.
     */
    protected boolean theDisabled;
}
