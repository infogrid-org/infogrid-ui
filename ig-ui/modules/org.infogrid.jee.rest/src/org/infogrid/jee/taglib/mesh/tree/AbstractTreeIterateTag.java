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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.mesh.tree;

import org.infogrid.jee.taglib.rest.AbstractRestInfoGridBodyTag;
import org.infogrid.util.logging.Log;

/**
 * <p>Factors out common functionality of tags related to the traversal of trees.</p>
 */
public abstract class AbstractTreeIterateTag
    extends
        AbstractRestInfoGridBodyTag
{
    private static final Log  log              = Log.getLogInstance( TreeIterateDownTag.class ); // our own, private logger
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    protected AbstractTreeIterateTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        super.initializeToDefaults();

        theMinLevel = -1;
        theMaxLevel = Integer.MAX_VALUE;
    }

    /**
     * Set the minLevel property.
     *
     * @param newValue new value for the property
     * @see #getMinLevel
     */
    public void setMinLevel(
            int newValue )
    {
        theMinLevel = newValue;
    }

    /**
     * Obtain the minLevel property.
     *
     * @return the value of the property
     * @see #setMinLevel(int)
     */
    public int getMinLevel()
    {
        return theMinLevel;
    }

    /**
     * Set the maxLevel property.
     *
     * @param newValue new value for the property
     * @see #getMinLevel
     */
    public void setMaxLevel(
            int newValue )
    {
        theMaxLevel = newValue;
    }

    /**
     * Obtain the maxLevel property.
     *
     * @return the value of the property
     * @see #setMinLevel(int)
     */
    public int getMaxLevel()
    {
        return theMaxLevel;
    }

    /**
     * Helper method to determine whether this level is within the bounds.
     *
     * @param candidate the candidate level
     * @return true if the candidate level is within the bounds
     */
    protected boolean isWithinBounds(
            int candidate )
    {
        if( candidate < theMinLevel ) {
            return false;
        }
        if( candidate >= theMaxLevel ) {
            return false;
        }
        return true;
    }
    /**
     * The minimum level on which this tag processes.
     */
    protected int theMinLevel;

    /**
     * The maximum level on which this tag processes.
     */
    protected int theMaxLevel;
}
