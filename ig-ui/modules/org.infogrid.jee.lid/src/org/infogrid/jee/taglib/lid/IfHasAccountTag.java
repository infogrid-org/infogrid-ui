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

package org.infogrid.jee.taglib.lid;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.lid.account.LidAccount;

/**
 * Evaluate body if the current user has a local account.
 */
public class IfHasAccountTag
    extends
        AbstractHasAccountTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theStatus = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the status property.
     *
     * @return value of the status property
     * @see #setStatus
     */
    public final String getStatus()
    {
        return theStatus;
    }

    /**
     * Set value of the status property.
     *
     * @param newValue new value of the status property
     * @see #getStatus
     */
    public final void setStatus(
            String newValue )
    {
        theStatus = newValue;
    }


    /**
     * Determine whether or not to process the content of this Tag.
     *
     * @return true if the content of this tag shall be processed.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected boolean evaluateTest()
        throws
            JspException,
            IgnoreException
    {
        LidAccount account = getAccount();
        if( account == null ) {
            return false;
        }
        // have account, now let's check for status
        if( theStatus == null ) {
            return true;
        }
        String [] status = theStatus.split( STATUS_SEPARATOR );
        for( int i=0 ; i<status.length ; ++i ) {
            String  current  = status[i].trim().toLowerCase();
            boolean positive = true;

            if( current.startsWith( "!" )) {
                positive = false;
                current = current.substring( 1 ).trim();
            }

            if( "created".equals( current )) {
                if( positive ^ account.getAccountStatus() == LidAccount.LidAccountStatus.CREATED ) {
                    return false;
                }
            } else if( "active".equals( current )) {
                if( positive ^ account.getAccountStatus() == LidAccount.LidAccountStatus.ACTIVE ) {
                    return false;
                }
            } else if( "disabled".equals( current )) {
                if( positive ^ account.getAccountStatus() == LidAccount.LidAccountStatus.DISABLED ) {
                    return false;
                }
            } else if( "obsoleted".equals( current )) {
                if( positive ^ account.getAccountStatus() == LidAccount.LidAccountStatus.OBSOLETED ) {
                    return false;
                }
            } else {
                throw new JspException( "Unknown LidAccount status name '" + current + "'" );
            }
        }
        return true;
    }

    /**
     * The required account status.
     */
    protected String theStatus;

    /**
     * String separating the components in the status attribute.
     */
    public static final String STATUS_SEPARATOR = ",";
}
