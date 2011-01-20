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

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.lid.account.LidAccount;

/**
 * Evaluate body if the current user does not have a local account.
 */
public class NotIfHasAccountTag
    extends
        AbstractHasAccountTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Determine whether or not to process the content of this Tag.
     *
     * @param account the account of the user, if any
     * @return true if the content of this tag shall be processed.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected boolean evaluateTest(
            LidAccount account )
        throws
            JspException,
            IgnoreException
    {
        if( account == null ) {
            return true;
        }
        return !this.evaluateAccount( account );
    }
}
