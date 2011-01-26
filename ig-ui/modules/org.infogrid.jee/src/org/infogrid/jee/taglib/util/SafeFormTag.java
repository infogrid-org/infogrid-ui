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
// Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.util;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
        
/**
 * <p>Generates an HTML form with a token that reduces CSRF attacks.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class SafeFormTag
        extends
             AbstractInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public SafeFormTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        id               = null; // inherited
        theStyle         = null;
        theTitle         = null;
        theLang          = null;
        theDir           = null;
        theOnclick       = null;
        theOnDblClick    = null;
        theOnMouseDown   = null;
        theOnMouseUp     = null;
        theOnMouseOver   = null;
        theOnMouseMove   = null;
        theOnMouseOut    = null;
        theOnKeyDown     = null;
        theOnKeyUp       = null;
        theAction        = null;
        theMethod        = null;
        theEnctype       = null;
        theAccept        = null;
        theName          = null;
        theOnsubmit      = null;
        theOnReset       = null;
        theAcceptCharset = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain the HTML accept attribute.
     * 
     * @return the attribute
     */
    public String getAccept()
    {
        return theAccept;
    }

    /**
     * Set the HTML accept attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setAccept(
            String newValue )
    {
        theAccept = newValue;
    }

    /**
     * Obtain the HTML accept-charset attribute.
     * 
     * @return the attribute
     */
    public String getAcceptCharset()
    {
        return theAcceptCharset;
    }

    /**
     * Set the HTML accept-charset attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setAcceptCharset(
            String newValue )
    {
        theAcceptCharset = newValue;
    }

    /**
     * Obtain the HTML action attribute.
     * 
     * @return the attribute
     */
    public String getAction()
    {
        return theAction;
    }

    /**
     * Set the HTML action attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setAction(
            String newValue )
    {
        theAction = newValue;
    }

    /**
     * Obtain the HTML dir attribute.
     * 
     * @return the attribute
     */
    public String getDir()
    {
        return theDir;
    }

    /**
     * Set the HTML dir attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setDir(
            String newValue )
    {
        theDir = newValue;
    }

    /**
     * Obtain the HTML enctype attribute.
     * 
     * @return the attribute
     */
    public String getEnctype()
    {
        return theEnctype;
    }

    /**
     * Set the HTML enctype attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setEnctype(
            String newValue )
    {
        theEnctype = newValue;
    }

    /**
     * Obtain the HTML lang attribute.
     * 
     * @return the attribute
     */
    public String getLang()
    {
        return theLang;
    }

    /**
     * Set the HTML lang attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setLang(
            String newValue )
    {
        theLang = newValue;
    }

    /**
     * Obtain the HTML method attribute.
     * 
     * @return the attribute
     */
    public String getMethod()
    {
        return theMethod;
    }

    /**
     * Set the HTML method attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setMethod(
            String newValue )
    {
        theMethod = newValue;
    }

    /**
     * Obtain the HTML name attribute.
     * 
     * @return the attribute
     */
    public String getName()
    {
        return theName;
    }

    /**
     * Set the HTML name attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setName(
            String newValue )
    {
        theName = newValue;
    }

    /**
     * Obtain the HTML ondblclick attribute.
     * 
     * @return the attribute
     */
    public String getOnDblClick()
    {
        return theOnDblClick;
    }

    /**
     * Set the HTML ondblclick attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setOnDblClick(
            String newValue )
    {
        theOnDblClick = newValue;
    }

    /**
     * Obtain the HTML onkeydown attribute.
     * 
     * @return the attribute
     */
    public String getOnKeyDown()
    {
        return theOnKeyDown;
    }

    /**
     * Set the HTML onkeydown attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setOnKeyDown(
            String newValue )
    {
        theOnKeyDown = newValue;
    }

    /**
     * Obtain the HTML onkeyup attribute.
     * 
     * @return the attribute
     */
    public String getOnKeyUp()
    {
        return theOnKeyUp;
    }

    /**
     * Set the HTML onkeyup attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setOnKeyUp(
            String newValue )
    {
        theOnKeyUp = newValue;
    }

    /**
     * Obtain the HTML onmousedown attribute.
     * 
     * @return the attribute
     */
    public String getOnMouseDown()
    {
        return theOnMouseDown;
    }

    /**
     * Set the HTML onmousedown attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setOnMouseDown(
            String newValue )
    {
        theOnMouseDown = newValue;
    }

    /**
     * Obtain the HTML onmousemove attribute.
     * 
     * @return the attribute
     */
    public String getOnMouseMove()
    {
        return theOnMouseMove;
    }

    /**
     * Set the HTML onmousemove attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setOnMouseMove(
            String newValue )
    {
        theOnMouseMove = newValue;
    }

    /**
     * Obtain the HTML onmouseout attribute.
     * 
     * @return the attribute
     */
    public String getOnMouseOut()
    {
        return theOnMouseOut;
    }

    /**
     * Set the HTML onmouseout attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setOnMouseOut(
            String newValue )
    {
        theOnMouseOut = newValue;
    }

    /**
     * Obtain the HTML onmouseover attribute.
     * 
     * @return the attribute
     */
    public String getOnMouseOver()
    {
        return theOnMouseOver;
    }

    /**
     * Set the HTML onmouseover attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setOnMouseOver(
            String newValue )
    {
        theOnMouseOver = newValue;
    }

    /**
     * Obtain the HTML onmouseup attribute.
     * 
     * @return the attribute
     */
    public String getOnMouseUp()
    {
        return theOnMouseUp;
    }

    /**
     * Set the HTML onmouseup attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setOnMouseUp(
            String newValue )
    {
        theOnMouseUp = newValue;
    }

    /**
     * Obtain the HTML onreset attribute.
     * 
     * @return the attribute
     */
    public String getOnReset()
    {
        return theOnReset;
    }

    /**
     * Set the HTML onreset attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setOnReset(
            String newValue )
    {
        theOnReset = newValue;
    }

    /**
     * Obtain the HTML onclick attribute.
     * 
     * @return the attribute
     */
    public String getOnclick()
    {
        return theOnclick;
    }

    /**
     * Set the HTML onclick attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setOnclick(
            String newValue )
    {
        theOnclick = newValue;
    }

    /**
     * Obtain the HTML onsubmit attribute.
     * 
     * @return the attribute
     */
    public String getOnsubmit()
    {
        return theOnsubmit;
    }

    /**
     * Set the HTML onsubmit attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setOnsubmit(
            String newValue )
    {
        theOnsubmit = newValue;
    }

    /**
     * Obtain the HTML style attribute.
     * 
     * @return the attribute
     */
    public String getStyle()
    {
        return theStyle;
    }

    /**
     * Set the HTML style attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setStyle(
            String newValue )
    {
        theStyle = newValue;
    }

    /**
     * Obtain the HTML title attribute.
     * 
     * @return the attribute
     */
    public String getTitle()
    {
        return theTitle;
    }

    /**
     * Set the HTML title attribute.
     * 
     * @param newValue the new value for the attribute
     */
    public void setTitle(
            String newValue )
    {
        theTitle = newValue;
    }

    /**
     * Append an HTML attribute if its value is not empty.
     * 
     * @param att the attribute name
     * @param value the value
     * @throws JspException catch-all exception for any error condition
     */
    protected void appendIfNeeded(
            String att,
            String value )
        throws
            JspException
    {
        if( value != null && value.length() > 0 ) {
            StringBuilder buf = new StringBuilder();
            buf.append( " " );
            buf.append( att );
            buf.append( "=\"" );
            buf.append( value );
            buf.append( "\"" );
            print( buf.toString() );
        }
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
        print( "<form" );
        appendIfNeeded( "id",             id );
        appendIfNeeded( "style",          theStyle );
        appendIfNeeded( "title",          theTitle );
        appendIfNeeded( "lang",           theLang );
        appendIfNeeded( "dir",            theDir );
        appendIfNeeded( "onclick",        theOnclick );
        appendIfNeeded( "ondblclick",     theOnDblClick );
        appendIfNeeded( "mousedown",      theOnMouseDown );
        appendIfNeeded( "mouseup",        theOnMouseUp );
        appendIfNeeded( "mouseover",      theOnMouseOver );
        appendIfNeeded( "mousemove",      theOnMouseMove );
        appendIfNeeded( "mouseout",       theOnMouseOut );
        appendIfNeeded( "keydown",        theOnKeyDown );
        appendIfNeeded( "keyup",          theOnKeyUp );
        appendIfNeeded( "action",         theAction );
        appendIfNeeded( "method",         theMethod );
        appendIfNeeded( "enctype",        theEnctype );
        appendIfNeeded( "accept",         theAccept );
        appendIfNeeded( "name",           theName );
        appendIfNeeded( "onsubmit",       theOnsubmit );
        appendIfNeeded( "onreset",        theOnReset );
        appendIfNeeded( "accept-charset", theAcceptCharset );
        print( ">" );

        if( "POST".equalsIgnoreCase( theMethod )) {
            String toInsert = SafeFormHiddenInputTag.hiddenInputTagString( pageContext );
            if( toInsert != null ) {
                print( toInsert );
            }
        }
        
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
        print( "</form>" );

        return EVAL_PAGE;
    }
    
    /**
     * The style attribute.
     */
    protected String theStyle;
    
    /**
     * The title attribute.
     */
    protected String theTitle;
    
    /**
     * The lang attribute.
     */
    protected String theLang;
    
    /**
     * The dir attribute.
     */
    protected String theDir;
    
    /**
     * The onclick attribute.
     */
    protected String theOnclick;
    
    /**
     * The ondblclick attribute.
     */
    protected String theOnDblClick;
    
    /**
     * The onmousedown attribute.
     */
    protected String theOnMouseDown;
    
    /**
     * The onmouseup attribute.
     */
    protected String theOnMouseUp;
    
    /**
     * The onmouseover attribute.
     */
    protected String theOnMouseOver;
    
    /**
     * The onmousemove attribute.
     */
    protected String theOnMouseMove;
    
    /**
     * The onmouseout attribute.
     */
    protected String theOnMouseOut;
    
    /**
     * The onkeydown attribute.
     */
    protected String theOnKeyDown;
    
    /**
     * The onkeyup attribute.
     */
    protected String theOnKeyUp;
    
    /**
     * The action attribute.
     */
    protected String theAction;
    
    /**
     * The method attribute.
     */
    protected String theMethod;
    
    /**
     * The enctype attribute.
     */
    protected String theEnctype;
    
    /**
     * The accept attribute.
     */
    protected String theAccept;
    
    /**
     * The name attribute.
     */
    protected String theName;
    
    /**
     * The onsubmit attribute.
     */
    protected String theOnsubmit;
    
    /**
     * The onreset attribute.
     */
    protected String theOnReset;
    
    /**
     * The accept-charset attribute.
     */
    protected String theAcceptCharset;
}
