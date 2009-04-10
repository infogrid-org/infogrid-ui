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

package org.infogrid.jee.viewlet;

/**
 * Many Viewlets can be in several states, such as "display" vs. "edit" vs. "preview".
 * This type captures that state.
 */
public interface JeeViewletState
{
    /**
     * Obtain the name of this state.
     *
     * @return the name of this state
     */
    public String getName();

    /**
     * Default URL parameter name containing the viewlet state.
     */
    public static final String VIEWLET_STATE_PAR_NAME = "viewlet-state";
}
