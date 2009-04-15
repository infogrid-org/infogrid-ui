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

package org.infogrid.jee.taglib.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.util.ResourceHelper;

/**
 * <p>Formats a time stamp.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class TimeStampTag
        extends
             AbstractInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public TimeStampTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theObjectName   = null;
        theFormatString = null;
        theNever        = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the objectName property.
     *
     * @return value of the objectName property
     * @see #setObjectName
     */
    public final String getObjectName()
    {
        return theObjectName;
    }

    /**
     * Set value of the objectName property.
     *
     * @param newValue new value of the objectName property
     * @see #getObjectName
     */
    public final void setObjectName(
            String newValue )
    {
        theObjectName = newValue;
    }

    /**
     * Obtain value of the formatString property.
     *
     * @return value of the formatString property
     * @see #setFormatString
     */
    public final String getFormatString()
    {
        return theFormatString;
    }

    /**
     * Set value of the formatString property.
     *
     * @param newValue new value of the formatString property
     * @see #getFormatString
     */
    public final void setFormatString(
            String newValue )
    {
        theFormatString = newValue;
    }

    /**
     * Obtain value of the never property.
     *
     * @return value of the never property
     * @see #setNever
     */
    public final String getNever()
    {
        return theNever;
    }

    /**
     * Set value of the never property.
     *
     * @param newValue new value of the never property
     * @see #getNever
     */
    public final void setNever(
            String newValue )
    {
        theNever = newValue;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    @Override
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        Object obj = lookup( theObjectName );

        Date theDate = null;
        if( obj instanceof Date ) {
            theDate = (Date) obj;
        } else if( obj instanceof Number ) {
            long realObj = ((Number)obj ).longValue();
            if( realObj > 0L ) {
                theDate = new Date( realObj );
            }
        } else {
            throw new JspException( "Cannot format object " + obj );
        }

        print( "<span class=\"" );
        print( getClass().getName() );
        print( "\">" );

        String formatted;
        if( theDate != null ) {
            DateFormat format;
            if( theFormatString != null ) {
                format = new SimpleDateFormat( theFormatString );
            } else {
                format = SimpleDateFormat.getDateTimeInstance();
            }

            formatted = format.format( theDate );
            print( formatted );

        } else if( theNever != null ) {
            print( theNever );
        } else {
            print( theResourceHelper.getResourceString( "Never" ));
        }
        print( "</span>" );
        
        return SKIP_BODY;
    }

    /**
     * The name of the bean that contains the Object to be rendered.
     */
    protected String theObjectName;

    /**
     * The format String.
     */
    protected String theFormatString;

    /**
     * The never String.
     */
    protected String theNever;
    
    /**
     * Our ResourceHelper.
     */
    protected static ResourceHelper theResourceHelper = ResourceHelper.getInstance( TimeStampTag.class );
}
