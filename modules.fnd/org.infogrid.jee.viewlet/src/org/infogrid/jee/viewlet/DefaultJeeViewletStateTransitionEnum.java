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
 * Default implementation of JeeViewletSTateTransition.
 */
public enum DefaultJeeViewletStateTransitionEnum
        implements
            JeeViewletStateTransition
{
    DO_EDIT( "do-edit" ) {
        /**
         * Obtain the desired state after this transition has been taken.
         *
         * @return the desired state after this transition has been taken
         */
        public JeeViewletState getNextState()
        {
            return DefaultJeeViewletStateEnum.EDIT;
        }
    },
    DO_PREVIEW( "do-preview" ) {
        /**
         * Obtain the desired state after this transition has been taken.
         *
         * @return the desired state after this transition has been taken
         */
        public JeeViewletState getNextState()
        {
            return DefaultJeeViewletStateEnum.PREVIEW;
        }
    },
    DO_COMMIT( "do-commit" ) {
        /**
         * Obtain the desired state after this transition has been taken.
         *
         * @return the desired state after this transition has been taken
         */
        public JeeViewletState getNextState()
        {
            return DefaultJeeViewletStateEnum.VIEW;
        }
    },
    DO_CANCEL( "do-cancel" ) {
        /**
         * Obtain the desired state after this transition has been taken.
         *
         * @return the desired state after this transition has been taken
         */
        public JeeViewletState getNextState()
        {
            return DefaultJeeViewletStateEnum.VIEW;
        }
    };
    
    /**
     * Constructur.
     *
     * @param transitionName name of the transition which the Viewlet is about to make
     */
    private DefaultJeeViewletStateTransitionEnum(
            String transitionName )
    {
        theTransitionName = transitionName;
    }

    /**
     * Obtain the name of this transition.
     *
     * @return the name of this transition
     */
    public String getName()
    {
        return theTransitionName;
    }


    /**
     * Obtain the correct member of this enum, given this incoming request.
     *
     * @param request the incoming request
     * @return the DefaultJeeViewletStateEnum
     */
    public static DefaultJeeViewletStateTransitionEnum fromRequest(
            RestfulRequest request )
    {
        String value = request.getSaneRequest().getPostArgument( VIEWLET_STATE_TRANSITION_PAR_NAME );
        // this must be a post argument, while the state is determined from a regular argument
        if( value != null ) {
            for( DefaultJeeViewletStateTransitionEnum candidate : DefaultJeeViewletStateTransitionEnum.values() ) {
                if( candidate.theTransitionName.equals( value )) {
                    return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Name of the transition.
     */
    protected String theTransitionName;
}
