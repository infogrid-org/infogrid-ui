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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.mesh;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridTag;
import org.infogrid.mesh.MeshObject;
import org.infogrid.util.ResourceHelper;

/**
 * Factors out common functionality of tags that deal with the duration since something
 * happened to a <code>MeshObject</code>.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public abstract class AbstractMeshObjectDurationTag
    extends
        AbstractRestInfoGridTag
{
    /**
     * Constructor.
     */
    protected AbstractMeshObjectDurationTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObject     = null;
        theMeshObjectName = null;
        theDurationFormat = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the meshObject property.
     *
     * @return value of the meshObject property
     * @see #setMeshObject
     */
    public final Object getMeshObject()
    {
        return theMeshObject;
    }

    /**
     * Set value of the meshObject property.
     *
     * @param newValue new value of the meshObject property
     * @see #getMeshObject
     */
    public final void setMeshObject(
            Object newValue )
    {
        theMeshObject = newValue;
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
     * Obtain value of the durationFormat property.
     *
     * @return value of the durationFormat property
     * @see #setDurationFormat
     */
    public final String getDurationFormat()
    {
        return theDurationFormat;
    }

    /**
     * Set value of the durationFormat property.
     *
     * @param newValue new value of the durationFormat property
     * @see #getDurationFormat
     */
    public final void setDurationFormat(
            String newValue )
    {
        theDurationFormat = newValue;
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
        MeshObject obj = lookupMeshObjectOrThrow( "meshObject", theMeshObject, "meshObjectName", theMeshObjectName );

        long then  = getRespectiveTime( obj );
        long now   = System.currentTimeMillis();
        long delta = now - then;

        String text = durationToString( delta );
        
        print( text );

        return SKIP_BODY;
    }

    /**
     * Convert a duration, in milliseconds, to a printed duration String.
     *
     * @param delta the duration
     * @return the printed duration
     */
    public String durationToString(
            long delta )
    {
        long yearsTotal;

        long monthsFromYear;
        long monthsTotal;

        long daysFromMonth;
        long daysFromYear;
        long daysTotal;

        long hoursFromDay;
        long hoursFromMonth;
        long hoursFromYear;
        long hoursTotal;
                
        long minutesFromHour;
        long minutesFromDay;
        long minutesFromMonth;
        long minutesFromYear;
        long minutesTotal;
                
        long secondsFromMinute;
        long secondsFromHour;
        long secondsFromDay;
        long secondsFromMonth;
        long secondsFromYear;
        long secondsTotal;
                
        long   millisFromSecond;
        long   millisTotal;
        double fractionalSeconds;

        millisTotal = delta;
        
        secondsTotal     = millisTotal / 1000L;
        millisFromSecond = millisTotal - secondsTotal*1000L;

        minutesTotal      = secondsTotal / 60L;
        secondsFromMinute = secondsTotal - minutesTotal*60L;

        hoursTotal        = minutesTotal / 60L;
        minutesFromHour   = minutesTotal - hoursTotal*60L;

        daysTotal         = hoursTotal / 24L;
        hoursFromDay      = hoursTotal - daysTotal*24L;
        
        monthsTotal       = daysTotal / 30L;
        daysFromMonth     = daysTotal - monthsTotal*30L;
        
        yearsTotal        = daysTotal / 365L;
        monthsFromYear    = monthsTotal - yearsTotal*12L;

        daysFromYear      = daysTotal - yearsTotal*365L;

        hoursFromMonth    = hoursTotal - monthsTotal   * (     30L*24L);
        hoursFromYear     = hoursTotal - yearsTotal    * (365L*30L*24L);

        minutesFromDay    = minutesTotal - daysTotal   * (     24L*60L);
        minutesFromMonth  = minutesTotal - monthsTotal * ( 30L*24L*60L);
        minutesFromYear   = minutesTotal - yearsTotal  * (365L*24L*60L);

        secondsFromHour   = secondsTotal - hoursTotal  * (         60L*60L);
        secondsFromDay    = secondsTotal - daysTotal   * (     24L*60L*60L);
        secondsFromMonth  = secondsTotal - monthsTotal * ( 30L*24L*60L*60L);
        secondsFromYear   = secondsTotal - yearsTotal  * (365L*24L*60L*60L);
        
        fractionalSeconds = (double) secondsFromMinute + ( millisFromSecond / 1000e0 );
        
        String durationFormat;
        if( theDurationFormat != null && theDurationFormat.length() > 0 ) {
            durationFormat = theDurationFormat;
        } else {
            durationFormat = DEFAULT_DURATION_FORMAT;
        }
        
        String ret = String.format(
                durationFormat,

                yearsTotal,          // { 1}

                monthsFromYear,      // { 2}
                monthsTotal,         // { 3}

                daysFromMonth,       // { 4}
                daysFromYear,        // { 5}
                daysTotal,           // { 6}

                hoursFromDay,        // { 7}
                hoursFromMonth,      // { 8}
                hoursFromYear,       // { 9}
                hoursTotal,          // {10}
                
                minutesFromHour,     // {11}
                minutesFromDay,      // {12}
                minutesFromMonth,    // {13}
                minutesFromYear,     // {14}
                minutesTotal,        // {15}
                
                secondsFromMinute,   // {16}
                secondsFromHour,     // {17}
                secondsFromDay,      // {18}
                secondsFromMonth,    // {19}
                secondsFromYear,     // {20}
                secondsTotal,        // {21}
                
                millisFromSecond,    // {22}
                millisTotal,         // {23}
                fractionalSeconds ); // {24}

        return ret;
    }

    /**
     * The MeshObject to render.
     */
    protected Object theMeshObject;

    /**
     * Name of the bean that contains the MeshObject to render.
     */
    protected String theMeshObjectName;
    
    /**
     * The format String for formatting of the duration.
     */
    protected String theDurationFormat;
    
    /**
     * The ResourceHelper.
     */
    protected static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( AbstractMeshObjectDurationTag.class );
    /**
     * The default duration format.
     */
    protected static final String DEFAULT_DURATION_FORMAT = theResourceHelper.getResourceString( "DefaultDurationFormat" );
}
