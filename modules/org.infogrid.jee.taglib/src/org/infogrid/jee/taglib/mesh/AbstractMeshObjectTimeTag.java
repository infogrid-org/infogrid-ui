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

package org.infogrid.jee.taglib.mesh;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.mesh.MeshObject;

/**
 * Factors out common functionality of tags that deal with the time when something
 * happened to a <code>MeshObject</code>.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public abstract class AbstractMeshObjectTimeTag
    extends
        AbstractInfoGridTag
{
    /**
     * Constructor.
     */
    protected AbstractMeshObjectTimeTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObjectName = null;
        theTimeZone       = null;
        theLocale         = null;
        theDateStyle      = null;
        theTimeStyle      = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the meshObjectName property.
     *
     * @return value of the meshObjectName property
     * @see #setMeshObjectName
     */
    public final String getMeshObjectName()
    {
        return theMeshObjectName;
    }

    /**
     * Set value of the meshObjectName property.
     *
     * @param newValue new value of the meshObjectName property
     * @see #getMeshObjectName
     */
    public final void setMeshObjectName(
            String newValue )
    {
        theMeshObjectName = newValue;
    }

    /**
     * Obtain value of the timeZone property.
     *
     * @return value of the timeZone property
     * @see #setTimeZone
     */
    public final String getTimeZone()
    {
        return theTimeZone;
    }

    /**
     * Set value of the timeZone property.
     *
     * @param newValue new value of the timeZone property
     * @see #getTimeZone
     */
    public final void setTimeZone(
            String newValue )
    {
        theTimeZone = newValue;
    }

    /**
     * Obtain value of the locale property.
     *
     * @return value of the locale property
     * @see #setLocale
     */
    public final String getLocale()
    {
        return theLocale;
    }

    /**
     * Set value of the locale property.
     *
     * @param newValue new value of the locale property
     * @see #getLocale
     */
    public final void setLocale(
            String newValue )
    {
        theLocale = newValue;
    }

    /**
     * Obtain value of the dateStyle property.
     *
     * @return value of the dateStyle property
     * @see #setDateStyle
     */
    public final String getDateStyle()
    {
        return theDateStyle;
    }

    /**
     * Set value of the dateStyle property.
     *
     * @param newValue new value of the dateStyle property
     * @see #getDateStyle
     */
    public final void setDateStyle(
            String newValue )
    {
        theDateStyle = newValue;
    }

    /**
     * Obtain value of the timeStyle property.
     *
     * @return value of the timeStyle property
     * @see #setTimeStyle
     */
    public final String getTimeStyle()
    {
        return theTimeStyle;
    }

    /**
     * Set value of the dateStyle property.
     *
     * @param newValue new value of the dateStyle property
     * @see #getTimeStyle
     */
    public final void setTimeStyle(
            String newValue )
    {
        theTimeStyle = newValue;
    }

    /**
     * This method is implemented by our respective subclasses, to obtain
     * the time that's applicable to that subclass, such as timeCreated, timeUpdated etc.
     *
     * @param obj the MeshObject
     * @return the time, in Systems.currentTimeMillis() format
     */
    protected abstract long getRespectiveTime(
            MeshObject obj );

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        MeshObject obj = (MeshObject) lookupOrThrow( theMeshObjectName );

        long then = getRespectiveTime( obj );

        String text = timeToString( then );
        
        print( text );

        return SKIP_BODY;
    }

    /**
     * Convert a time, in milliseconds, to a printed String.
     *
     * @param time the time
     * @return the printed duration
     * @throws JspException if a JSP exception has occurred
     */
    public String timeToString(
            long time )
        throws
            JspException
    {
        int    dateStyle;
        int    timeStyle;

        Locale locale = theFormatter.determineLocale( theLocale );
                
        dateStyle = determineStyle( theDateStyle, "DateStyle" );
        timeStyle = determineStyle( theTimeStyle, "TimeStyle" );

        DateFormat format = DateFormat.getDateTimeInstance( dateStyle, timeStyle, locale );
        
        String ret = format.format( new Date( time ));
        
        return ret;
    }

    /**
     * Helper method to determine the correct style integer value from a String.
     *
     * @param s the String
     * @param tagName name of the tag currently processed, for error messages
     * @return the style integer value per DateFormat
     * @throws JspException if a JSP exception has occurred
     */
    protected int determineStyle(
            String s,
            String tagName )
        throws
            JspException
    {
        int ret;
        if( s != null && s.length() > 0 ) {
            if( "FULL".equalsIgnoreCase( s )) {
                ret = DateFormat.FULL;

            } else if( "LONG".equalsIgnoreCase( s )) {
                ret = DateFormat.LONG;

            } else if( "MEDIUM".equalsIgnoreCase( s )) {
                ret = DateFormat.MEDIUM;

            } else if( "SHORT".equalsIgnoreCase( s )) {
                ret = DateFormat.SHORT;

            } else {
                throw new JspException( tagName + " attribute must be one of \"FULL\", \"LONG\", \"MEDIUM\", \"SHORT\"." );
            }
        } else {
            ret = DateFormat.SHORT;
        }
        return ret;
    }
    
    /**
     * Name of the bean that contains the MeshObject that will be rendered.
     */
    protected String theMeshObjectName;
    
    /**
     * Time zone, if any.
     */
    protected String theTimeZone;
    
    /**
     * Locale, if any.
     */
    protected String theLocale;
    
    /**
     * Date style, if any.
     */
    protected String theDateStyle;
    
    /**
     * Time style, if any.
     */
    protected String theTimeStyle;
}
