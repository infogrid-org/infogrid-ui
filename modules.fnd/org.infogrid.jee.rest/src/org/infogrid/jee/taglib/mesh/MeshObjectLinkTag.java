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

package org.infogrid.jee.taglib.mesh;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.util.text.StringifierException;

/**
 * <p>Tag that links / hyperlinks to a MeshObject.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshObjectLinkTag
    extends
        AbstractInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshObjectLinkTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObjectName       = null;
        theRootPath             = null;
        theAddArguments         = null;
        theTarget               = null;
        theStringRepresentation = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the meshObjectName property.
     *
     * @return value of the meshObjectName property
     * @see #setMeshObjectName
     */
    public String getMeshObjectName()
    {
        return theMeshObjectName;
    }

    /**
     * Set value of the meshObjectBean property.
     *
     * @param newValue new value of the meshObjectName property
     * @see #getMeshObjectName
     */
    public void setMeshObjectName(
            String newValue )
    {
        theMeshObjectName = newValue;
    }

    /**
     * Obtain value of the rootPath property.
     *
     * @return value of the rootPath property
     * @see #setRootPath
     */
    public String getRootPath()
    {
        return theRootPath;
    }

    /**
     * Set value of the rootPath property.
     *
     * @param newValue new value of the rootPath property
     * @see #getRootPath
     */
    public void setRootPath(
            String newValue )
    {
        theRootPath = newValue;
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
        MeshObject obj = (MeshObject) lookupOrThrow( theMeshObjectName );

        try {
            String text = ((RestfulJeeFormatter)theFormatter).formatMeshObjectLinkStart( pageContext, obj, theRootPath, theAddArguments, theTarget, theStringRepresentation );
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
        MeshObject obj = (MeshObject) lookupOrThrow( theMeshObjectName );

        try {
            String text = ((RestfulJeeFormatter)theFormatter).formatMeshObjectLinkEnd( pageContext, obj, theRootPath, theStringRepresentation );
            print( text );

        } catch( StringifierException ex ) {
            throw new JspException( ex );
        }

        return EVAL_PAGE;
    }
    
    /**
     * Name of the bean that holds the MeshObject (mutually exclusive with theIdentifier).
     */
    protected String theMeshObjectName;
    
    /**
     * The HTTP path prepended to the HREF, e.g. http://example.com/foo/bar/?obj=
     */
    protected String theRootPath;
    
    /**
     * The arguments to append to the URL, separated by &.
     */
    protected String theAddArguments;

    /**
     * The HTML frame to target, if any.
     */
    protected String theTarget;

    /**
     * Name of the String representation.
     */
    protected String theStringRepresentation;
}
