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

package org.infogrid.jee.taglib.candy;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;

/**
 * Common functionality for {@link TabHeaderTag TabHeaderTag} and {@link TabContentTag TabContentTag}.
 */
public abstract class AbstractTabChild
    extends
        AbstractInfoGridBodyTag
{
    /**
     * Constructor.
     */
    public AbstractTabChild()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
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
     * Find the enclosing TabTag, or throw JspException.
     *
     * @param startTag the tag at which to start the search
     * @return the found TabTag
     * @throws JspException thrown if the TabTag could not be found
     */
    protected TabTag findEnclosingTabTagOrThrow(
            Tag startTag )
        throws
            JspException
    {
        Tag current = startTag;
        while( current != null ) {
            if( current instanceof TabTag ) {
                return (TabTag) current;
            }
            current = current.getParent();
        }
        throw new JspException( "Cannot find enclosing TabTag, starting from " + startTag );
    }

    /**
     * Find the enclosing TabbedTag, or throw JspException.
     *
     * @param startTag the tag at which to start the search
     * @return the found TabbedTag
     * @throws JspException thrown if the TabbedTag could not be found
     */
    protected TabbedTag findEnclosingTabbedTagOrThrow(
            Tag startTag )
        throws
            JspException
    {
        Tag current = startTag;
        while( current != null ) {
            if( current instanceof TabbedTag ) {
                return (TabbedTag) current;
            }
            current = current.getParent();
        }
        throw new JspException( "Cannot find enclosing TabbedTag, starting from " + startTag );
    }

    /**
     * The "class" attribute (used like HTML does it). Unfortunately, we can't call it "class"
     * because that would interfere with Java's Object.getClass() method.
     */
    protected String theHtmlClass;
}
