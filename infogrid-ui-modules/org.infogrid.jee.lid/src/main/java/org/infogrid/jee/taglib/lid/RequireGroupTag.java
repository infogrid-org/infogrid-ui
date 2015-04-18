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
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.lid.account.LidAccount;
import org.infogrid.lid.servlet.LidPipelineServlet;
import org.infogrid.util.Identifier;

/**
 * Throw exception and thus end processing if the current user is not member of a given group.
 * This is used to protect JSP files from being incorrectly invokable for the wrong user.
 */
public class RequireGroupTag
    extends
        AbstractInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theGroup = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the group property.
     *
     * @return value of the group property
     * @see #setGroup
     */
    public final String getGroup()
    {
        return theGroup;
    }

    /**
     * Set value of the group property.
     *
     * @param newValue new value of the group property
     * @see #getGroup
     */
    public final void setGroup(
            String newValue )
    {
        theGroup = newValue;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    @Override
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        if( evaluateTest() ) {
            return SKIP_BODY;

        } else {
            throw new JspException( new RequireGroupTagException( theGroup ));
        }
    }

    /**
     * Determine whether or not to continue processing this page.
     *
     * @return true if the page shall continue to be processed
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected boolean evaluateTest()
        throws
            JspException,
            IgnoreException
    {
        // look for group membership via the LidAccount
        LidAccount me = (LidAccount) pageContext.getRequest().getAttribute( LidPipelineServlet.ACCOUNT_ATTRIBUTE_NAME );
        if( me != null ) {
            Identifier [] meMemberId = me.getGroupIdentifiers();

            if( meMemberId != null ) {
                for( Identifier current : meMemberId ) {
                    String currentString = current.toExternalForm();

                    if( currentString.equals( theGroup )) {
                        return true;
                    }
                }
            }
            String [] meMemberString = me.getGroupNames();
            if( meMemberString != null ) {
                for( String currentString : meMemberString ) {

                    if( currentString.equals( theGroup )) {
                        return true;
                    }
                }
            }
        }

        // look for group membership via the request attribute
        String [] groups = (String []) pageContext.getRequest().getAttribute( LidPipelineServlet.USER_GROUPS_ATTRIBUTE_NAME );
        if( groups != null ) {
            for( String currentString : groups ) {

                if( currentString.equals( theGroup )) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Name of the required group.
     */
    protected String theGroup;
}
