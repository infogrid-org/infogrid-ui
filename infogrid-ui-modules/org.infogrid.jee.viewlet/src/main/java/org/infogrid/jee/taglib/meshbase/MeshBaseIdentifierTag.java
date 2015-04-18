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

package org.infogrid.jee.taglib.meshbase;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridBodyTag;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.util.text.StringifierException;

/**
 * <p>Tag that displays the identifier of a NetMeshBase.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshBaseIdentifierTag
    extends
        AbstractRestInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshBaseIdentifierTag()
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
        theStringRepresentation = null;
        theMaxLength            = -1;
        theColloquial           = true;

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
     */
    public void setMaxLength(
            int newValue )
    {
        theMaxLength = newValue;
    }
    
    /**
     * Obtain value of the colloquial property.
     *
     * @return value of the colloquial property
     * @see #setColloquial
     */
    public boolean getColloquial()
    {
        return theColloquial;
    }

    /**
     * Set value of the colloquial property.
     *
     * @param newValue new value of the colloquial property
     */
    public void setColloquial(
            boolean newValue )
    {
        theColloquial = newValue;
    }

    /**
     * Do the start tag operation.
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        MeshBase mb = (MeshBase) lookupOrThrow( theMeshBaseName );

        try {
            String text = getFormatter().formatMeshBaseIdentifier( pageContext, mb, theStringRepresentation, theMaxLength, theColloquial );
            print( text );

        } catch( StringifierException ex ) {
            throw new JspException( ex );
        }

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Name of the bean that holds the MeshBase.
     */
    protected String theMeshBaseName;
    /**
     * Name of the String representation.
     */
    protected String theStringRepresentation;
    
    /**
     * The maximum length of an emitted String.
     */
    protected int theMaxLength;

    /**
     * Should the value be outputted in colloquial form.
     */
    protected boolean theColloquial;
}
