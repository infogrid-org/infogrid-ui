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
// Copyright 1999-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.viewlet;

import org.infogrid.util.ResourceHelper;
import org.infogrid.util.http.SaneUrl;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParameters;

/**
 * Default implementation of JeeViewletState.
 */
public enum DefaultJeeViewletStateEnum
        implements
            JeeViewletState
{
    VIEW(    "view",    true ),
    EDIT(    "edit",    false ),
    PREVIEW( "preview", false );

    /**
     * Constructor.
     *
     * @param stateName state of the state in which the Viewlet is
     */
    private DefaultJeeViewletStateEnum(
            String  stateName,
            boolean isDefault )
    {
        theStateName = stateName;
        theIsDefault = isDefault;
    }

    /**
     * Obtain the name of this state.
     *
     * @return the name of this state
     */
    public String getName()
    {
        return theStateName;
    }

    /**
     * If true, this is the default state of the JeeViewlet.
     *
     * @return true if default
     */
    public boolean isDefaultState()
    {
        return theIsDefault;
    }

    /**
     * Obtain the correct member of this enum, given this incoming request.
     *
     * @param request the incoming request
     * @return the DefaultJeeViewletStateEnum
     */
    public static DefaultJeeViewletStateEnum fromRequest(
            SaneUrl request )
    {
        String value = request.getUrlArgument( VIEWLET_STATE_PAR_NAME );
        if( value != null ) {
            for( DefaultJeeViewletStateEnum candidate : DefaultJeeViewletStateEnum.values() ) {
                if( candidate.theStateName.equals( value )) {
                    return candidate;
                }
            }
        }
        return VIEW;
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     */
    public String toStringRepresentation(
            StringRepresentation           rep,
            StringRepresentationParameters pars )
    {
        return theResourceHelper.getResourceString( toString() + "_VALUE" );
    }

    /**
     * Obtain the start part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     */
    public String toStringRepresentationLinkStart(
            StringRepresentation           rep,
            StringRepresentationParameters pars )
    {
        return "";
    }

    /**
     * Obtain the end part of a String representation of this object that acts
     * as a link/hyperlink and can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     */
    public String toStringRepresentationLinkEnd(
            StringRepresentation           rep,
            StringRepresentationParameters pars )
    {
        return "";
    }

    /**
     * Name of the state.
     */
    protected String theStateName;

    /**
     * Is the state the default state.
     */
    protected boolean theIsDefault;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( DefaultJeeViewletStateEnum.class );
}
