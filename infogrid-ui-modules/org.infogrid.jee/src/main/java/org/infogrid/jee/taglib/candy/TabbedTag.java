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
// Copyright 1998-2012 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.candy;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.JeeFormatter;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;

/**
 * <p>Tag to support tabbed navigation.</p>
 * <p>The structure of the generated HTML is as follows:</p>
 * <pre>&lt;div class="org_infogrid_jee_taglib_candy_TabbedTag">
 * &lt;div class="org_infogrid_jee_taglib_candy_TabbedTag-header">
 *  &lt;ul class="org_infogrid_jee_taglib_candy_TabbedTag-header-list">
 *   &lt;li class="org_infogrid_jee_taglib_candy_TabbedTag-header-list-item"></pre>
 * <p>(header of tab 1)</p>
 * <pre>   &lt;/li>
 *    &lt;li class="selected org_infogrid_jee_taglib_candy_TabbedTag-header-list-item"></pre>
 * <p>(header of tab 2, which is selected)</p>
 * <pre>   &lt;/li>
 *   &lt;/ul>
 *  &lt;/div>
 *  &lt;div class="org_infogrid_jee_taglib_candy_TabbedTag-content"></pre>
 * <p>(content of tab 2, which is selected)</p>
 * <pre> &lt;/div>
 *&lt;/div></pre>
 * <p>The content tags of this tag are processed twice: first, to generate the headers,
 *    then, the content. However, content contained in the tags should only be called once.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class TabbedTag
    extends
        AbstractInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public TabbedTag()
    {
        // noop
    }

    /**
     * Release all of our resources.
     */
    @Override
    protected void initializeToDefaults()
    {
        theHtmlClass = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the htmlClass property.
     *
     * @return value of the htmlClass property
     * @see #setHtmlClass
     */
    public String getHtmlClass()
    {
        return theHtmlClass;
    }

    /**
     * Set value of the htmlClass property.
     *
     * @param newValue new value of the htmlClass property
     * @see #getHtmlClass
     */
    public void setHtmlClass(
            String newValue )
    {
        theHtmlClass = newValue;
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
        print( "<div class=\"" );
        print( getClass().getName().replace( '.', '-' ) );
        if( theHtmlClass != null ) {
            print( " " );
            print( theHtmlClass );
        }
        
        if( id != null && id.length() > 0 ) {
            print( "\" id=\"" );
            print( id );
        }
        println( "\">");
        println( " <div class=\"header\">" );
        println( "  <ul class=\"header\">" );

        isHead = true;
        
        return EVAL_BODY_INCLUDE;
    }

    /**
     * Our implementation of doAfterBody().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    @Override
    protected int realDoAfterBody()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        if( super.bodyContent != null ) {
            JeeFormatter formatter = getFormatter();

            formatter.printPrevious( pageContext, formatter.isTrue( getFilter()), bodyContent.getString() );
            bodyContent.clearBody();
        }
        if( isHead ) {
            println( "  </ul>" );
            println( " </div>" );
            println( " <div class=\"content\">" );
            isHead = false;
            
            return EVAL_BODY_AGAIN;
        } else {
            return SKIP_BODY;
        }
    }

    /**
     * Our implementation of doEndTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     */
    @Override
    protected int realDoEndTag()
        throws
            JspException
    {
        if( isHead ) {
            println( "  </ul>" );
        }
        println( " </div>" );
        println( "</div>" );

        return EVAL_PAGE;
    }
    
    /**
     * Enable contained tabs to determine whether the head, or the content section is
     * currently being processed.
     *
     * @return true if this is the head section
     */
    public boolean isHead()
    {
        return isHead;
    }

    /**
     * The "class" attribute (used like HTML does it). Unfortunately, we can't call it "class"
     * because that would interfere with Java's Object.getClass() method.
     */
    protected String theHtmlClass;
    
    /**
     * Captures state. If true, we are in the "head" section where the tabs are displayed.
     * False: in the content section.
     */
    protected boolean isHead;
}
