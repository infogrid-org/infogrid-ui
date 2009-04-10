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
// Copyright 1999-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.viewlet;

import org.infogrid.jee.rest.RestfulRequest;

/**
 * Default implementation of JeeViewletState.
 */
public enum DefaultJeeViewletStateEnum
        implements
            JeeViewletState
{
    VIEW( "view" ),
    EDIT( "edit" ),
    PREVIEW( "preview" );

    /**
     * Constructur.
     *
     * @param stateName state of the state in which the Viewlet is
     */
    private DefaultJeeViewletStateEnum(
            String stateName )
    {
        theStateName = stateName;
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
     * Obtain the correct member of this enum, given this incoming request.
     *
     * @param request the incoming request
     * @return the DefaultJeeViewletStateEnum
     */
    public static DefaultJeeViewletStateEnum fromRequest(
            RestfulRequest request )
    {
        String value = request.getSaneRequest().getPostArgument( VIEWLET_STATE_PAR_NAME );
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
     * Name of the state.
     */
    protected String theStateName;
}
