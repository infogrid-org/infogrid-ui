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
// Copyright 1998-2008 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.util;

import java.awt.geom.Dimension2D;

/**
  * It seems incredible, but there does not seem to be a "double"
  * implementation of Dimension2D in the JDK, so here is ours.
  * FIXME as soon as Sun provides this.
  */
public class DoubleDimension
        extends
            Dimension2D
        implements
           Cloneable
{
    /**
     * Construct one.
     *
     * @param w width
     * @param h height
     */
    public DoubleDimension(
            double w,
            double h )
    {
        width = w;
        height = h;
    }

    /**
     * Obtain the width.
     *
     * @return the width
     */
    public double getWidth()
    {
        return width;
    }

    /**
     * Obtain the height.
     *
     * @return the height
     */
    public double getHeight()
    {
        return height;
    }

    /**
     * Set the size.
     *
     * @param w new width
     * @param h new height
     */
    public void setSize(
            double w,
            double h )
    {
        width = w;
        height = h;
    }

    /**
     * Override clone().
     *
     * @return cloned Object
     */
    @Override
    public Object clone()
    {
        return new DoubleDimension( width, height );
    }

    /**
     * The width.
     */
    protected double width;

    /**
     * The height.
     */
    protected double height;
}
