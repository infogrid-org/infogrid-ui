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

package org.infogrid.jee.taglib.meshbase;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridBodyTag;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.util.text.StringifierException;

/**
 * <p>Tag that links/hyperlinks to a NetMeshBase.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshBaseLinkTag
    extends
        AbstractRestInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshBaseLinkTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshBaseName         = null;
        theAddArguments         = null;
        theTarget               = null;
        theTitle                = null;
        theStringRepresentation = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the meshBase property.
     * 
     * @return value of the meshBase property
     * @see #setMeshBaseName
     */
    public String getMeshBaseName()
    {
        return theMeshBaseName;
    }

    /**
     * Set value of the meshBase property.
     * 
     * @param newValue new value of the meshBase property
     * @see #getMeshBaseName
     */
    public void setMeshBaseName(
            String newValue )
    {
        theMeshBaseName = newValue;
    }

    /**
     * Obtain value of the addArguments property.
     *
     * @return value of the addArguments property
     * @see #setAddArguments
     */
    public String getAddArguments()
    {
        return theAddArguments;
    }

    /**
     * Set value of the addArguments property.
     *
     * @param newValue new value of the addArguments property
     * @see #getAddArguments
     */
    public void setAddArguments(
            String newValue )
    {
        theAddArguments = newValue;
    }

    /**
     * Obtain value of the target property.
     *
     * @return value of the target property
     * @see #setTarget
     */
    public String getTarget()
    {
        return theTarget;
    }

    /**
     * Set value of the target property.
     *
     * @param newValue new value of the target property
     * @see #getTarget
     */
    public void setTarget(
            String newValue )
    {
        theTarget = newValue;
    }

    /**
     * Obtain value of the title property.
     *
     * @return value of the title property
     * @see #setTitle
     */
    public String getTitle()
    {
        return theTitle;
    }

    /**
     * Set value of the title property.
     *
     * @param newValue new value of the title property
     * @see #getTitle
     */
    public void setTitle(
            String newValue )
    {
        theTitle = newValue;
    }

    /**
     * Obtain value of the stringRepresentation property.
     *
     * @return value of the stringRepresentation property
     * @see #setStringRepresentation
     */
    public String getStringRepresentation()
    {
        return theStringRepresentation;
    }

    /**
     * Set value of the stringRepresentation property.
     *
     * @param newValue new value of the stringRepresentation property
     * @see #getStringRepresentation
     */
    public void setStringRepresentation(
            String newValue )
    {
        theStringRepresentation = newValue;
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
        MeshBase mb = (MeshBase) lookupOrThrow( theMeshBaseName );

        try {
            String text = getFormatter().formatMeshBaseLinkStart( pageContext, mb, theAddArguments, theTarget, theTitle, theStringRepresentation );
            print( text );

        } catch( StringifierException ex ) {
            throw new JspException( ex );
        }

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Our implementation of doEndTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    @Override
    protected int realDoEndTag()
        throws
            JspException,
            IgnoreException
    {
        MeshBase mb = (MeshBase) lookupOrThrow( theMeshBaseName );

        try {
            String text = getFormatter().formatMeshBaseLinkEnd( pageContext, mb, theStringRepresentation );
            print( text );

        } catch( StringifierException ex ) {
            throw new JspException( ex );
        }

        return EVAL_PAGE;
    }
    
    /**
     * Name of the bean that holds the MeshBase.
     */
    protected String theMeshBaseName;
    
    /**
     * The arguments to append to the URL, separated by &.
     */
    protected String theAddArguments;

    /**
     * The HTML frame to target, if any.
     */
    protected String theTarget;

    /**
     * The HTML link title, if any.
     */
    protected String theTitle;

    /**
     * Name of the String representation.
     */
    protected String theStringRepresentation;
}
