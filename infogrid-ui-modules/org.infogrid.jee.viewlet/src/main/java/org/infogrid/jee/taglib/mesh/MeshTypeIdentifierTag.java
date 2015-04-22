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
import org.infogrid.model.primitives.MeshType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.util.text.StringifierException;

/**
 * Tag that displays the user-visible String of a <code>MeshType</code>.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshTypeIdentifierTag
        extends
            AbstractRestInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshTypeIdentifierTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshTypeName         = null;
        theStringRepresentation = null;
        theMaxLength            = -1;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the meshTypeName property.
     *
     * @return value of the meshTypeName property
     * @see #setMeshTypeName
     */
    public final String getMeshTypeName()
    {
        return theMeshTypeName;
    }

    /**
     * Set value of the meshTypeName property.
     *
     * @param newValue new value of the meshTypeName property
     * @see #getMeshTypeName
     */
    public final void setMeshTypeName(
            String newValue )
    {
        theMeshTypeName = newValue;
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
     * Obtain value of the maxLength property.
     *
     * @return value of the maxLength property
     * @see #setMaxLength
     */
    public int getMaxLength()
    {
        return theMaxLength;
    }

    /**
     * Set value of the maxLength property.
     *
     * @param newValue new value of the maxLength property
     * @see #getMaxLength
     */
    public void setMaxLength(
            int newValue )
    {
        theMaxLength = newValue;
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
        Object found = lookupOrThrow( theMeshTypeName );
 
        MeshTypeIdentifier theIdentifier;
        if( found instanceof MeshType ) {
            theIdentifier = ((MeshType)found).getIdentifier();
        } else if( found instanceof MeshTypeIdentifier ) {
            theIdentifier = (MeshTypeIdentifier) found;
        } else if( getFormatter().isTrue( getIgnore() )) {
            throw new ClassCastException( "Found object named " + theMeshTypeName + " is neither a MeshType nor a MeshTypeIdentifier: " + found );
        } else {
            theIdentifier = null;
        }

        try {
            String text;
            if( theIdentifier != null ) {
                text = getFormatter().formatMeshTypeIdentifier( pageContext, theIdentifier, theStringRepresentation, theMaxLength );
            } else {
                text = "";
            }
            print( text );

        } catch( StringifierException ex ) {
            throw new JspException( ex );
        }

        return SKIP_BODY;
    }

    /**
     * String containing the name of the bean that is the MeshType.
     */
    protected String theMeshTypeName;

    /**
     * Name of the String representation.
     */
    protected String theStringRepresentation;
    
    /**
     * The maximum length of an emitted String.
     */
    protected int theMaxLength;
}
