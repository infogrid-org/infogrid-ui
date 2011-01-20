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

import java.io.IOException;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.lid.account.LidAccount;
import org.infogrid.lid.servlet.LidPipelineServlet;

/**
 * Collects functionality common to testing whether the current user has an account.
 */
public abstract class AbstractHasAccountTag
    extends
        AbstractInfoGridBodyTag
{
    /**
     * Constructor.
     */
    protected AbstractHasAccountTag()
    {
        // noop
    }

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
     * Our implementation of doStartTag(), to be provided by subclasses.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        LidAccount account = (LidAccount) pageContext.getRequest().getAttribute( LidPipelineServlet.ACCOUNT_ATTRIBUTE_NAME );

        if( evaluateTest( account ) ) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Determine whether or not to process the content of this Tag.
     *
     * @param account the account of the user, if any
     * @return true if the content of this tag shall be processed.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected abstract boolean evaluateTest(
            LidAccount account )
        throws
            JspException,
            IgnoreException;

    /**
     * Determine whether or not the given account meets the status specification.
     *
     * @param account the Account to test
     * @return true if it meets the status
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected boolean evaluateAccount(
            LidAccount account )
        throws
            JspException,
            IgnoreException
    {
        // have account, now let's check for status
        if( theStatus == null ) {
            // must be created or active
            if( account.getAccountStatus().equals( LidAccount.LidAccountStatus.CREATED )) {
                return true;

            } else if( account.getAccountStatus().equals( LidAccount.LidAccountStatus.ACTIVE )) {
                return true;

            } else {
                return false;
            }

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
                if( positive ^ account.getAccountStatus().equals( LidAccount.LidAccountStatus.CREATED )) {
                    return false;
                }
            } else if( "appliedfor".equals( current ) ) {
                if( positive ^ account.getAccountStatus() == LidAccount.LidAccountStatus.APPLIEDFOR ) {
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
            } else if( "refused".equals( current )) {
                if( positive ^ account.getAccountStatus() == LidAccount.LidAccountStatus.REFUSED ) {
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
