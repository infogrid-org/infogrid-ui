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

package org.infogrid.util;

/**
 * Interface implemented by objects that know how to internationalize themselves.
 */
public interface LocalizedObject
{
    /**
     * Determine the correct internationalized string that can be shown to the
     * user. Use a default formatter.
     *
     * @return the internationalized string
     */
    public String getLocalizedMessage();

    /**
     * Determine the correct internationalized string that can be shown to the
     * user.
     *
     * @param formatter the formatter to use for data objects to be displayed as part of the message
     * @return the internationalized string
     */
    public String getLocalizedMessage(
            LocalizedObjectFormatter formatter );
 
    /**
     * Name of the property in the resource file that holds the default MessageFormat
     * String to show to the user for this exception.
     */
    public static final String MESSAGE_PARAMETER = "Message";
}
