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

package org.infogrid.jee.taglib.lid;

import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.lid.account.LidAccount;
import org.infogrid.lid.servlet.LidPipelineServlet;
import org.infogrid.util.Identifier;

/**
 * Collects functionality common to testing for group membership of the current user.
 */
public abstract class AbstractGroupMemberTag
    extends
        AbstractInfoGridBodyTag
{
    /**
     * Constructor.
     */
    protected AbstractGroupMemberTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theGroups = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the groups property.
     *
     * @return value of the groups property
     * @see #setGroups
     */
    public final String getGroups()
    {
        return theGroups;
    }

    /**
     * Set value of the groups property.
     *
     * @param newValue new value of the groups property
     * @see #getGroups
     */
    public final void setGroups(
            String newValue )
    {
        theGroups = newValue;
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
        if( evaluateTest() ) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Determine whether or not to process the content of this Tag.
     *
     * @return true if the content of this tag shall be processed.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected abstract boolean evaluateTest()
        throws
            JspException,
            IgnoreException;

    /**
     * Determine whether the current user is a member of at least one of the specified groups.
     *
     * @return true if the current user is a member of at least one of the specified groups.
     */
    protected boolean isMember()
    {
        String [] toTest = theGroups.split( SEPARATOR );
        for( int i=0 ; i<toTest.length ; ++i ) {
            toTest[i] = toTest[i].trim();
        }
        return isMember( pageContext.getRequest(), toTest );
    }

    /**
     * Determine whether the current user is a member of at least one of the specified groups.
     * This is factored out so the same code can be used somewhere else, too.
     *
     * @return true if the current user is a member of at least one of the specified groups.
     */
    public static boolean isMember(
            ServletRequest request,
            String []      toTest )
    {
        // look for group membership via the LidAccount
        LidAccount me = (LidAccount) request.getAttribute( LidPipelineServlet.ACCOUNT_ATTRIBUTE_NAME );
        if( me != null ) {
            Identifier [] meMemberId = me.getGroupIdentifiers();

            if( meMemberId != null ) {
                for( Identifier current : meMemberId ) {
                    String currentString = current.toExternalForm();

                    for( String current2 : toTest ) {
                        if( currentString.equals( current2 )) {
                            return true;
                        }
                    }
                }
            }
            String [] meMemberString = me.getGroupNames();
            if( meMemberString != null ) {
                for( String currentString : meMemberString ) {

                    for( String current2 : toTest ) {
                        if( currentString.equals( current2 )) {
                            return true;
                        }
                    }
                }
            }
        }

        // look for group membership via the request attribute
        String [] groups = (String []) request.getAttribute( LidPipelineServlet.USER_GROUPS_ATTRIBUTE_NAME );
        if( groups != null ) {
            for( String currentString : groups ) {

                for( String current2 : toTest ) {
                    if( currentString.equals( current2 )) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determine whether the current user is a member of the specified group.
     * This is factored out so the same code can be used somewhere else, too.
     *
     * @return true if the current user is a member of the specified group.
     */
    public static boolean isMember(
            ServletRequest request,
            String         toTest )
    {
        // look for group membership via the LidAccount
        LidAccount me = (LidAccount) request.getAttribute( LidPipelineServlet.ACCOUNT_ATTRIBUTE_NAME );
        if( me != null ) {
            Identifier [] meMemberId = me.getGroupIdentifiers();

            if( meMemberId != null ) {
                for( Identifier current : meMemberId ) {
                    String currentString = current.toExternalForm();

                    if( currentString.equals( toTest )) {
                        return true;
                    }
                }
            }
            String [] meMemberString = me.getGroupNames();
            if( meMemberString != null ) {
                for( String currentString : meMemberString ) {

                    if( currentString.equals( toTest )) {
                        return true;
                    }
                }
            }
        }

        // look for group membership via the request attribute
        String [] groups = (String []) request.getAttribute( LidPipelineServlet.USER_GROUPS_ATTRIBUTE_NAME );
        if( groups != null ) {
            for( String currentString : groups ) {

                if( currentString.equals( toTest )) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * The group members attribute.
     */
    protected String theGroups;

    /**
     * String separating the groups in theGroups.
     */
    public static final String SEPARATOR = ",";
}
