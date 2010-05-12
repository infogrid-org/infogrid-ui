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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.mesh;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.viewlet.JeeMeshObjectsToView;
import org.infogrid.mesh.MeshObject;
import org.infogrid.model.traversal.TraversalPath;
import org.infogrid.model.traversal.TraversalTranslator;
import org.infogrid.model.traversal.TraversalTranslatorException;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;
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
    private static final Log  log              = Log.getLogInstance( MeshObjectLinkTag.class ); // our own, private logger

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
        theMeshObject           = null;
        theAddArguments         = null;
        theTarget               = null;
        theTitle                = null;
        theStringRepresentation = null;
        theTraversalPathName    = null;

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
     * Set value of the meshObjectName property.
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
     * Obtain value of the meshObject property.
     *
     * @return value of the meshObject property
     * @see #setMeshObject
     */
    public MeshObject getMeshObject()
    {
        return theMeshObject;
    }

    /**
     * Set value of the meshObject property.
     *
     * @param newValue new value of the meshObject property
     * @see #getMeshObject
     */
    public void setMeshObject(
            MeshObject newValue )
    {
        theMeshObject = newValue;
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
     * Obtain value of the traversalPathName property.
     *
     * @return value of the traversalPathName property
     * @see #setTraversalPathName
     */
    public String getTraversalPathName()
    {
        return theTraversalPathName;
    }

    /**
     * Set value of the traversalPathName property.
     *
     * @param newValue new value of the traversalPathName property
     * @see #getTraversalPathName
     */
    public void setTraversalPathName(
            String newValue )
    {
        theTraversalPathName = newValue;
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
        MeshObject obj;
        if( theMeshObject != null ) {
            obj = theMeshObject;
        } else {
            obj = (MeshObject) lookupOrThrow( theMeshObjectName );
        }

        if( obj != null ) { // make be ignore="true"
            String addArguments = constructAddArguments();

            try {
                String text = ((RestfulJeeFormatter)theFormatter).formatMeshObjectLinkStart( pageContext, obj, addArguments, theTarget, theTitle, theStringRepresentation );
                print( text );

            } catch( StringifierException ex ) {
                throw new JspException( ex );
            }
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
        MeshObject obj;
        if( theMeshObject != null ) {
            obj = theMeshObject;
        } else {
            obj = (MeshObject) lookupOrThrow( theMeshObjectName );
        }

        if( obj != null ) { // make be ignore="true"
            try {
                String text = ((RestfulJeeFormatter)theFormatter).formatMeshObjectLinkEnd( pageContext, obj, theStringRepresentation );
                print( text );

            } catch( StringifierException ex ) {
                throw new JspException( ex );
            }
        }

        return EVAL_PAGE;
    }

    /**
     * Helper method to construct the additionalArguments value to pass on.
     *
     * @return the value
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected String constructAddArguments()
        throws
            JspException,
            IgnoreException
    {
        if( theTraversalPathName == null ) {
            return theAddArguments;
        }

        TraversalTranslator translator = InfoGridWebApp.getSingleton().getApplicationContext().findContextObject( TraversalTranslator.class );
        if( translator == null ) {
            return theAddArguments;
        }

        TraversalPath path = (TraversalPath) lookupOrThrow( theTraversalPathName );

        try {
            String [] args = translator.translateTraversalPath( theMeshObject, path );
            if( args == null || args.length == 0 ) {
                return theAddArguments;
            }
            StringBuilder buf = new StringBuilder();
            if( theAddArguments != null ) {
                buf.append( theAddArguments );
            }
            for( int i=0 ; i<args.length ; ++i ) {
                buf.append( "&" ).append( JeeMeshObjectsToView.LID_TRAVERSAL_ARGUMENT_NAME ).append( "=" ).append( HTTP.encodeToValidUrlArgument( args[i] ));
            }
            return buf.toString();

        } catch( TraversalTranslatorException ex ) {
            log.error( ex );
            return theAddArguments;
        }
    }

    /**
     * Name of the bean that holds the MeshObject (mutually exclusive with theMeshObject).
     */
    protected String theMeshObjectName;

    /**
     * The MeshObject (mutually exclusive with theMeshObjectName).
     */
    protected MeshObject theMeshObject;

    /**
     * The arguments to append to the URL, separated by &.
     */
    protected String theAddArguments;

    /**
     * The HTML frame to target, if any.
     */
    protected String theTarget;

    /**
     * The title of the link, if any.
     */
    protected String theTitle;

    /**
     * Name of the String representation.
     */
    protected String theStringRepresentation;

    /**
     * Traversal path, in String form.
     */
    protected String theTraversalPathName;
}
