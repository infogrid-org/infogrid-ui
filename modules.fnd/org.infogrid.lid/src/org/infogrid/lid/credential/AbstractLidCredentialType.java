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

package org.infogrid.lid.credential;

import org.infogrid.util.AbstractLocalizedObject;
import org.infogrid.util.LocalizedObject;
import org.infogrid.util.LocalizedObjectFormatter;
import org.infogrid.util.ResourceHelper;

/**
 * Factors out functionality common to LidCredentialTypes.
 */
public abstract class AbstractLidCredentialType
        extends
            AbstractLocalizedObject
        implements
            LidCredentialType
{
    /**
     * Constructor for subclasses only.
     */
    protected AbstractLidCredentialType()
    {
        // nothing
    }

    /**
     * Determine the computable full-qualified name of this LidCredentialType.
     *
     * @return the computable full name
     */
    public String getFullName()
    {
        return getClass().getName();
    }

    /**
     * Determine the correct internationalized string that can be shown to the
     * user.
     *
     * @param formatter the formatter to use for data objects to be displayed as part of the message
     * @return the internationalized string
     */
    public String getLocalizedMessage(
            LocalizedObjectFormatter formatter )
    {
        return ResourceHelper.getInstance( getClass() ).getResourceString( LocalizedObject.MESSAGE_PARAMETER );
    }
}
