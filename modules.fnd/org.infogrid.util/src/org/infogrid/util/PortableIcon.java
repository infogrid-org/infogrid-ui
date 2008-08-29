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

import java.net.URL;

/**
 * <p>A portable representation of Icons, independent of Swing and whatever. It drives me
 * completely nuts that I have to create this -- why in the world is Icon defined in
 * javax.swing instead of, say, java.lang -- but I don't seem to have a choice.</p>
 *
 * <p>A range of concrete subclasses is provided as inner classes.</p>
 */
public abstract class PortableIcon
{
    /**
     * Factory method. Create a PortableIcon from the data at a certain URL.
     *
     * @param u the URL pointing to the data
     * @return the created PortableIcon
     */
    public static PortableIcon create(
            URL u )
    {
        return new FromURL( u );
    }

    /**
     * Factory method. Create a PortableIcon from the data found as the named resource
     * relative to a ClassLoader.
     *
     * @param loader the ClassLoader relative to which the data can be found
     * @param name the name of the resource, relative to the ClassLoader, that holds that data
     * @return the created PortableIcon
     */
    public static PortableIcon create(
            ClassLoader loader,
            String      name )
    {
        URL u = loader.getResource( name );
        if( u == null ) {
            return null;
        }

        return new FromURL( u );
    }

    /**
     * Factory method. Create a PortableIcon from actual image data in a supported format.
     *
     * @param data the data for the PortableIcon
     * @return the created PortableIcon
     */
    public static PortableIcon create(
            byte [] data )
    {
        return new FromBytes( data );
    }

    /**
     * Factory method. Create a PortableIcon that is an overlay of two PortableIcons. It returns
     * one of the two specified PortableIcons directly if the other one is null. And returns null if both of
     * them are null.
     *
     * @param original the original PortableIcon to which we want to create an overlay
     * @param overlay the PortableIcon to be overlayed on the original
     * @param hAlign the horizontal alignment
     * @param vAlign the vertical alignment
     * @return the created PortableIcon, or one of the operands, or null
     */
    public static PortableIcon createOverlay(
            PortableIcon original,
            PortableIcon overlay,
            int          hAlign,
            int          vAlign )
    {
        if( original != null ) {
            if( overlay != null ) {
                return new Overlay( original, overlay, hAlign, vAlign );
            } else {
                return original;
            }
        } else {
            if( overlay != null ) {
                return overlay;
            } else {
                return null;
            }
        }
    }

    /**
     * Constant for alignment specification.
     */
    protected static final int _NORTH = 0x10;

    /**
     * Constant for alignment specification.
     */
    protected static final int _EAST  = 0x20;

    /**
     * Constant for alignment specification.
     */
    protected static final int _SOUTH = 0x40;

    /**
     * Constant for alignment specification.
     */
    protected static final int _WEST  = 0x80;

    /**
     * Constant for alignment specification.
     */
    protected static final int _INSIDE   = 0x1;

    /**
     * Constant for alignment specification.
     */
    protected static final int _OUTSIDE  = 0x2;

    /**
     * Constant for alignment specification.
     */
    protected static final int _STRADDLE = 0x4;

    /**
     * We want the top edge to be aligned.
     */
    public static final int NORTH_INSIDE = _NORTH | _INSIDE;

    /**
     * We want the overlay sitting on top of the top edge.
     */
    public static final int NORTH_STRADDLE = _NORTH | _STRADDLE;

    /**
     * We want the overlay just outside of the top edge.
     */
    public static final int NORTH_OUTSIDE = _NORTH | _OUTSIDE;

    /**
     * We want the left edge to be aligned.
     */
    public static final int WEST_INSIDE = _WEST | _INSIDE;

    /**
     * We want the overlay sitting on top of the left edge.
     */
    public static final int WEST_STRADDLE = _WEST | _STRADDLE;

    /**
     * We want the overlay just outside of the left edge.
     */
    public static final int WEST_OUTSIDE = _WEST | _OUTSIDE;

    /**
     * We want this dimension to be aligned in the center.
     */
    public static final int CENTER = 0;

    /**
     * We want the bottom edge to be aligned.
     */
    public static final int SOUTH_INSIDE = _SOUTH | _INSIDE;

    /**
     * We want the overlay sitting on top of the bottom edge.
     */
    public static final int SOUTH_STRADDLE = _SOUTH | _STRADDLE;

    /**
     * We want the overlay just outside of the bottom edge.
     */
    public static final int SOUTH_OUTSIDE = _SOUTH | _OUTSIDE;

    /**
     * We want the right edge to be aligned.
     */
    public static final int EAST_INSIDE = _EAST | _INSIDE;

    /**
     * We want the overlay sitting on top of the right edge.
     */
    public static final int EAST_STRADDLE = _EAST | _STRADDLE;

    /**
     * We want the overlay just outside of the right edge.
     */
    public static final int EAST_OUTSIDE = _EAST | _OUTSIDE;

    /**
     * Implementation class for PortableIcon which carries its data as a byte array.
     */
    public static class FromBytes
        extends
            PortableIcon
    {
        /**
         * Constructor with a byte array with the data.
         *
         * @param data the actual icon data
         */
        public FromBytes(
                byte [] data )
        {
            if( data == null ) {
                throw new NullPointerException();
            }
            if( data.length == 0 ) {
                throw new IllegalArgumentException();
            }
            theData = data;
        }

        /**
         * Obtain the bytes that constitute the icon.
         *
         * @return the bytes, or null if not known
         */
        public byte [] getBytes()
        {
            return theData;
        }

        /**
         * The actual data.
         */
        protected byte [] theData;
    }

    /**
     * Implementation class for PortableIcon which loads the icon data from a URL.
     */
    public static class FromURL
        extends
            PortableIcon
    {
        /**
         * Constructor with a URL that points to the data.
         *
         * @param u the URL pointing to the data
         */
        public FromURL(
                URL u )
        {
            if( u == null ) {
                throw new NullPointerException();
            }
            theUrl = u;
        }

        /**
         * Obtain the URL from which the icon shall be loaded.
         *
         * @return the URL, or null if not known
         */
        public URL getURL()
        {
            return theUrl;
        }

        /**
         * The URL at which the data can be found.
         */
        protected URL theUrl;
    }

    /**
     * This is a PortableIcon that is made from two other PortableIcons.
     */
    public static class Overlay
            extends
                PortableIcon
    {
        /**
         * Private constructor, use factory method.
         *
         * @param original the original Icon to which we want to create an overlay
         * @param overlay the Icon to be overlayed on the original
         * @param hAlign the horizontal alignment
         * @param vAlign the vertical alignment
         */
        protected Overlay(
                PortableIcon original,
                PortableIcon overlay,
                int          hAlign,
                int          vAlign )
        {
            if( original == null ) {
                throw new NullPointerException();
            }
            if( overlay == null ) {
                throw new NullPointerException();
            }
            theOriginal = original;
            theOverlay  = overlay;
            theHAlign   = hAlign;
            theVAlign   = vAlign;
        }

        /**
         * Obtain the original PortableIcon that we "modify".
         *
         * @return the original PortableIcon
         */
        public PortableIcon getOriginal()
        {
            return theOriginal;
        }

        /**
         * Obtain the overlay PortableIcon that we we use to "modify".
         *
         * @return the overlay PortableIcon
         */
        public PortableIcon getOverlay()
        {
            return theOverlay;
        }

        /**
         * Obtain the horizontal alignment value.
         *
         * @return the horizontal alignment value
         */
        public int getHorizontalAlign()
        {
            return theHAlign;
        }

        /**
         * Obtain the vertical alignment value.
         *
         * @return the vertical alignment value
         */
        public int getVerticalAlign()
        {
            return theVAlign;
        }

        /**
         * The regular Icon that we "modify".
         */
        protected PortableIcon theOriginal;

        /**
         * The overlay Icon that we use to "modify".
         */
        protected PortableIcon theOverlay;

        /**
         * The alignment between the two Icons in horizontal direction.
         */
        protected int theHAlign;

        /**
         * The alignment between the two Icons in vertical direction.
         */
        protected int theVAlign;
    }
}
