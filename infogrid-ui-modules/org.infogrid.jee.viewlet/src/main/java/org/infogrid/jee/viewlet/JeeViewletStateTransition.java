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

package org.infogrid.jee.viewlet;

import org.infogrid.util.text.HasStringRepresentation;

/**
 * Many Viewlets can attempt to transition from one state to another, such as "begin edit" vs. "cancel edit".
 * This type captures that transition.
 */
public interface JeeViewletStateTransition
        extends
            HasStringRepresentation
{
    /**
     * Obtain the name of this transition.
     *
     * @return the name of this transition
     */
    public String getName();

    /**
     * Obtain the desired state after this transition has been taken.
     *
     * @return the desired state after this transition has been taken
     */
    public JeeViewletState getNextState();

    /**
     * Default HTTP POST parameter name containing the viewlet state transition.
     */
    public static final String VIEWLET_STATE_TRANSITION_PAR_NAME = "ViewletStateTransition";
}
