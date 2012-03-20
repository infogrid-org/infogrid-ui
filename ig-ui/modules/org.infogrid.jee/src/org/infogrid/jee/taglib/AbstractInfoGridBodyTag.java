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

package org.infogrid.jee.taglib;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;
import org.infogrid.jee.JeeFormatter;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.servlet.AbstractInfoGridServlet;

/**
 * <p>Factors out common functionality for BodyTags. Also redefines the JEE Tag
 * API, which fails to make any sense whatsoever.</p>
 *
 * <p>Support for the following attributes is provided to all subclasses:</p>
 * <table class="infogrid-border">
 *  <thead>
 *   <tr>
 *    <th>Attribute</th>
 *    <th>Meaning</th>
 *    <th>Mandatory?</th>
 *   </tr>
 *  </thead>
 *  <tbody>
 *   <tr>
 *    <td><code>filter</code></td>
 *    <td>Filter output for characters that are sensitive in HTML.</td>
 *    <td>optional</td>
 *   </tr>
 *   <tr>
 *    <td><code>ignore</code></td>
 *    <td>If true, ignore missing beans and simply output nothing. Otherwise, throw a <code>JspException</code></td>
 *    <td>optional</td>
 *   </tr>
 *   <tr>
 *    <td><code>scope</code></td>
 *    <td>The scope (page, request, session, application) in which beans are being looked up</td>
 *    <td>optional</td>
 *   </tr>
 *  </tbody>
 * </table>
 *
 * <p>If you change this file, you MUST also change AbstractInfoGridTag, which largely
 * defines the same methods with the same code.</p>
 */
public abstract class AbstractInfoGridBodyTag
    extends
        BodyTagSupport
{
    /**
     * Constructor.
     */
    protected AbstractInfoGridBodyTag()
    {
        initializeToDefaults(); // may invoke subclass invocation
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    protected void initializeToDefaults()
    {
        theFilter = null;
        theIgnore = null;
        theScope  = null; // means "search"
    }

    /**
     * Obtain value of the filter property.
     *
     * @return value of the filter property
     * @see #setFilter
     */
    public final String getFilter()
    {
        return theFilter;
    }

    /**
     * Set value of the filter property.
     *
     * @param newValue new value of the filter property
     * @see #getFilter
     */
    public final void setFilter(
            String newValue )
    {
        theFilter = newValue;
    }

    /**
     * Obtain value of the ignore property.
     *
     * @return value of the ignore property
     * @see #setIgnore
     */
    public final String getIgnore()
    {
        return theIgnore;
    }

    /**
     * Set value of the ignore property.
     *
     * @param newValue new value of the ignore property
     * @see #getIgnore
     */
    public final void setIgnore(
            String newValue )
    {
        theIgnore = newValue;
    }

    /**
     * Obtain value of the scope property.
     *
     * @return value of the scope property
     * @see #setScope
     */
    public final String getScope()
    {
        return theScope;
    }

    /**
     * Set value of the scope property.
     *
     * @param newValue new value of the scope property
     * @see #getScope
     */
    public final void setScope(
            String newValue )
    {
        theScope = newValue;
    }

    /**
     * Do the start tag operation. This method is final; subclasses implement the
     * {@link #realDoStartTag realDoStartTag} method.
     *
     * @return indicate how to continue processing
     * @throws JspException thrown if a processing error occurred
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public final int doStartTag()
        throws
            JspException
    {
        try {
            int ret = realDoStartTag();
            
            return ret;
            
        } catch( IgnoreException ex ) {
            return SKIP_BODY;

        } catch( IOException ex ) {
            throw new JspException( "Exception while processing " + getClass().getName() + ".doStartTag", ex );
        }
    }

    /**
     * Our implementation of doStartTag(), to be provided by subclasses.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected abstract int realDoStartTag()
        throws
            JspException,
            IgnoreException,
            IOException;

    /**
     * Do after a body inclusion. This method is final; subclasses implement the
     * {@link #realDoEndTag realDoAfterBody} method.
     *
     * @return indicate how to continue processing
     * @throws JspException thrown if a processing error occurred
     * @see javax.servlet.jsp.tagext.BodyTagSupport#doAfterBody()
     */
    @Override
    public final int doAfterBody()
        throws
            JspException
    {
        try {
            int ret = realDoAfterBody();
            
            return ret;
            
        } catch( IgnoreException ex ) {
            return EVAL_BODY_AGAIN;
        } catch( IOException ex ) {
            throw new JspException( "Exception while processing " + getClass().getName() + ".doAfterBody", ex );
        }
    }

    /**
     * Our implementation of doAfterBody(), to be provided by subclasses.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected int realDoAfterBody()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        return SKIP_BODY; // this is the default, subclasses may override
    }

    /**
     * Do the end tag operation. This method is final; subclasses implement the
     * {@link #realDoEndTag realDoEndTag} method.
     *
     * @return indicate how to continue processing
     * @throws JspException thrown if a processing error occurred
     * @see javax.servlet.jsp.tagext.TagSupport#doEndTag()
     */
    @Override
    public final int doEndTag()
        throws
            JspException
    {
        try {
            int ret = realDoEndTag();
            
            initializeToDefaults();

            if( thePushedRequestAttributes != null ) {
                ServletRequest r = pageContext.getRequest();
                for( Entry<String,Object> current : thePushedRequestAttributes.entrySet() ) {
                    String key   = current.getKey();
                    Object value = current.getValue();
                    if( value != MARKER ) {
                        r.setAttribute( key, value );
                    } else {
                        r.removeAttribute( key );
                    }
                }
                thePushedRequestAttributes = null;
            }

            return ret;
            
        } catch( IgnoreException ex ) {
            return EVAL_PAGE;

        } catch( IOException ex ) {
            throw new JspException( "Exception while processing " + getClass().getName() + ".doEndTag", ex );
        }
    }

    /**
     * Our implementation of doEndTag(), to be provided by subclasses.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected int realDoEndTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        return EVAL_PAGE; // reasonable default
    }

    /**
     * Look up a bean in the scope given by the scope attribute.
     *
     * @param name name of the bean
     * @return the bean
     * @throws JspException thrown if a processing error occurred
     */
    protected final Object lookup(
            String name )
        throws
            JspException
    {
        if( name == null || name.length() == 0 ) {
            throw new JspException( "Cannot look up bean with empty name" );
        }
        Object ret = getFormatter().nestedLookup( pageContext, name, theScope );
        return ret;
    }

    /**
     * Look up a bean in the scope given by the scope attribute, and
     * throw an exception if not found.
     *
     * @param name name of the bean
     * @return the bean
     * @throws JspException if the bean was not found and the ignore attribute was not set
     * @throws IgnoreException thrown if the bean could not be found but the ignore attribute was set
     */
    protected final Object lookupOrThrow(
            String name )
        throws
            JspException,
            IgnoreException
    {
        if( name == null || name.length() == 0 ) {
            throw new JspException( "Cannot look up bean with empty name" );
        }
        
        Object ret;
        if( getFormatter().isTrue( theIgnore )) {
            ret = getFormatter().nestedLookup( pageContext, name, theScope );
        } else {
            ret = getFormatter().nestedLookupOrThrow( pageContext, name, theScope );
        }
        return ret;
    }

    /**
     * Look up the property of a bean in the specified scope, and throw an exception if not found.
     *
     * @param name name of the bean
     * @param propertyName name of the property of the bean
     * @return the bean's property
     * @throws JspException if the bean was not found and the ignore attribute was not set
     * @throws IgnoreException thrown if the bean could not be found but the ignore attribute was set
     */
    protected final Object lookupOrThrow(
            String name,
            String propertyName )
        throws
            JspException,
            IgnoreException
    {
        if( name == null || name.length() == 0 ) {
            throw new JspException( "Cannot look up bean with empty name" );
        }
        
        Object ret;
        if( getFormatter().isTrue( theIgnore )) {
            ret = getFormatter().nestedLookup( pageContext, name, propertyName, theScope );
        } else {
            ret = getFormatter().nestedLookupOrThrow( pageContext, name, propertyName, theScope );
        }
        return ret;
    }

    /**
     * Print out some text.
     *
     * @param text the text
     * @throws JspException
     */
    protected final void print(
            String text )
        throws
            JspException
    {
        if( text != null ) {
            getFormatter().print( pageContext, getFormatter().isTrue( theFilter ), text );
        }
    }

    /**
     * Print out some text, followed by a line feed.
     *
     * @param text the text
     * @throws JspException
     */
    protected final void println(
            String text )
        throws
            JspException
    {
        if( text != null ) {
            getFormatter().println( pageContext, getFormatter().isTrue( theFilter ), text );
        }
    }

    /**
     * May be overridden by subclasses.
     */
    protected void internalRelease()
    {
        // noop on this level
    }

    /**
     * Release resources.
     */
    @Override
    public final void release()
    {
        internalRelease();
    }

    /**
     * Set a request attribute, saving the old value. When the tag ends,
     * automatically restore the old value.
     *
     * @param name name of the request attribute
     * @param value value of the request attribute
     * @return the current value of the request attribute
     */
    protected Object setRequestAttribute(
            String name,
            Object value )
    {
        if( thePushedRequestAttributes == null ) {
            thePushedRequestAttributes = new HashMap<String,Object>();
        }
        // cases:
        // 1. no outer request attribute by this name
        // 2. outer request attribute by this name exists
        //
        // a. this is the first time we set a local value
        // b. this is the second (or later) time we set it

        Object ret = thePushedRequestAttributes.get( name );
        if( ret == null ) {
            // first time

            ret = pageContext.getRequest().getAttribute( name );
            if( ret == null ) {
                ret = MARKER;
            }
            thePushedRequestAttributes.put( name, ret );
        }
        if( ret == MARKER ) {
            ret = null;
        }
        pageContext.getRequest().setAttribute( name, value );

        return ret;
    }

    /**
     * Find an ancestor tag of a particular type.
     *
     * @param clazz class name of the ancestor tag
     * @return the ancestor tag, or null if not found
     */
    protected Tag findAncestorTag(
            Class<? extends Tag> clazz )
    {
        Tag ret = getParent();
        while( ret != null && !clazz.isInstance( ret )) {
            ret = ret.getParent();
        }
        return ret;
    }

    /**
     * Find the InfoGridWebApp object.
     *
     * @return the InfoGridWebApp object
     */
    protected InfoGridWebApp getInfoGridWebApp()
    {
        InfoGridWebApp app = (InfoGridWebApp) pageContext.getServletContext().getAttribute( AbstractInfoGridServlet.INFOGRID_WEB_APP_NAME );
        return app;
    }

    /**
     * Get the formatter to use.
     *
     * @return the formatter
     */
    protected JeeFormatter getFormatter()
    {
        if( theFormatter == null ) {
            InfoGridWebApp app = getInfoGridWebApp();
            theFormatter       = app.getApplicationContext().findContextObjectOrThrow( JeeFormatter.class );

        }
        return theFormatter;
    }

    /**
     * Filter the rendered output for characters that are sensitive in HTML?
     */
    private String theFilter;

    /**
     * Should we ignore missing beans and simply output nothing?
     */
    private String theIgnore;

    /**
     * The scope to be searched to retrieve beans.
     */
    private String theScope;
    
    /**
     * The formatter to use.
     */
    private JeeFormatter theFormatter;

    /**
     * The buffered request attributes that the tag temporarily overrides.
     */
    protected HashMap<String,Object> thePushedRequestAttributes;

    /**
     * Marker object to indicate that the request attribute value is null.
     */
    private static final Object MARKER = new Object();
}
