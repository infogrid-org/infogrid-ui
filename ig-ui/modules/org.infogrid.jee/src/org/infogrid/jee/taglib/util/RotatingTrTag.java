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

import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.jstl.core.LoopTagStatus;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.util.ArrayHelper;

/**
 * <p>Rotates through specified HTML class names for each row in a table.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class RotatingTrTag
        extends
             AbstractInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public RotatingTrTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theStatusVar         = null;
        theHtmlClasses       = null;
        theSeparator         = ",";
        theFirstRowHtmlClass = null;
        theLastRowHtmlClass  = null;
        
        super.initializeToDefaults();
    }

    /**
     * Obtain value of the statusVar property.
     *
     * @return value of the statusVar property
     * @see #setStatusVar
     */
    public final String getStatusVar()
    {
        return theStatusVar;
    }

    /**
     * Set value of the statusVar property.
     *
     * @param newValue new value of the statusVar property
     * @see #getStatusVar
     */
    public final void setStatusVar(
            String newValue )
    {
        theStatusVar = newValue;
    }

    /**
     * Obtain value of the htmlClasses property.
     *
     * @return value of the htmlClasses property
     * @see #setHtmlClasses
     */
    public final String getHtmlClasses()
    {
        return theHtmlClasses;
    }

    /**
     * Set value of the htmlClasses property.
     *
     * @param newValue new value of the htmlClasses property
     * @see #getHtmlClasses
     */
    public final void setHtmlClasses(
            String newValue )
    {
        theHtmlClasses = newValue;
    }

    /**
     * Obtain value of the separator property.
     *
     * @return value of the separator property
     * @see #setSeparator
     */
    public final String getSeparator()
    {
        return theSeparator;
    }

    /**
     * Set value of the separator property.
     *
     * @param newValue new value of the separator property
     * @see #getSeparator
     */
    public final void setSeparator(
            String newValue )
    {
        theSeparator = newValue;
    }

    /**
     * Obtain value of the firstRowHtmlClass property.
     *
     * @return value of the firstRowHtmlClass property
     * @see #setFirstRowHtmlClass
     */
    public final String getFirstRowHtmlClass()
    {
        return theFirstRowHtmlClass;
    }

    /**
     * Set value of the firstRowHtmlClass property.
     *
     * @param newValue new value of the firstRowHtmlClass property
     * @see #getFirstRowHtmlClass
     */
    public final void setFirstRowHtmlClass(
            String newValue )
    {
        theFirstRowHtmlClass = newValue;
    }

    /**
     * Obtain value of the lastRowHtmlClass property.
     *
     * @return value of the lastRowHtmlClass property
     * @see #setLastRowHtmlClass
     */
    public final String getLastRowHtmlClass()
    {
        return theLastRowHtmlClass;
    }

    /**
     * Set value of the lastRowHtmlClass property.
     *
     * @param newValue new value of the lastRowHtmlClass property
     * @see #getLastRowHtmlClass
     */
    public final void setLastRowHtmlClass(
            String newValue )
    {
        theLastRowHtmlClass = newValue;
    }

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
        if( theStatusVar == null || theStatusVar.length() == 0 ) {
            throw new JspException( "Missing varStatus attribute" );
        }
        if( theHtmlClasses == null || theHtmlClasses.length() == 0 ) {
            throw new JspException( "Missing classNames attribute" );
        }
        if( theParsedClassNames == null ) {
            ArrayList<String> found = new ArrayList<String>();
            StringTokenizer   token = new StringTokenizer( theHtmlClasses, theSeparator );
            while( token.hasMoreTokens() ) {
                found.add( token.nextToken() );
            }
            theParsedClassNames = ArrayHelper.copyIntoNewArray( found, String.class );
        }
        LoopTagStatus varStatus = (LoopTagStatus) lookupOrThrow( theStatusVar );
        int           iteration = varStatus.getCount();
        boolean       isFirst   = varStatus.isFirst();
        boolean       isLast    = varStatus.isLast();
        
        --iteration; // starts with 1
        int index = iteration % theParsedClassNames.length;
        
        String thisClassName = theParsedClassNames[ index ];
        thisClassName        = thisClassName.trim();

        StringBuilder classesString = new StringBuilder();
        String        sep           = "";
        
        if( thisClassName != null && thisClassName.length() > 0 ) {
            classesString.append( sep );
            classesString.append( thisClassName );
            sep = " ";
        }
        if( isFirst && theFirstRowHtmlClass != null && theFirstRowHtmlClass.length() > 0 ) {
            classesString.append( sep );
            classesString.append( theFirstRowHtmlClass );
            sep = " ";
        }
        if( isLast && theLastRowHtmlClass != null && theLastRowHtmlClass.length() > 0 ) {
            classesString.append( sep );
            classesString.append( theLastRowHtmlClass );
            sep = " ";
        }

        print( "<tr" );
        if( classesString.length() > 0 ) {
            print( " class=\"" );
            print( classesString.toString() );
            print( "\"" );
        }
        print( ">" );
        
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Our implementation of doEndTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IOException thrown if an I/O Exception occurred
     */
    @Override
    protected int realDoEndTag()
        throws
            JspException,
            IOException
    {
        print( "</tr>" );

        return EVAL_PAGE;
    }

    /**
     * Name of the bean that contains the varStatus from the enclosing j:forSearch.
     */
    protected String theStatusVar;
    
    /**
     * Name of the HTML classes to rotate through.
     */
    protected String theHtmlClasses;
    
    /**
     * Name of the separator of the HTML classes.
     */
    protected String theSeparator;
    
    /**
     * First-row HTML class.
     */
    protected String theFirstRowHtmlClass;
    
    /**
     * Last-row HTML class.
     */
    protected String theLastRowHtmlClass;

    /**
     * Buffer of the parsed classNames.
     */
    protected String [] theParsedClassNames;
}
