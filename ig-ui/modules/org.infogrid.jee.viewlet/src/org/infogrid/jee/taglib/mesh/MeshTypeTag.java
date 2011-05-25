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

package org.infogrid.jee.taglib.mesh;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridTag;
import org.infogrid.model.primitives.DataType;
import org.infogrid.model.primitives.L10PropertyValueMap;
import org.infogrid.model.primitives.MeshType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.util.text.StringifierException;

/**
 * Tag that displays the user-visible String of a <code>MeshType</code>.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshTypeTag
        extends
            AbstractRestInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshTypeTag()
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
        thePropertyName         = null;
        theNullString           = "";
        theStringRepresentation = null;
        theMaxLength            = -1;
        theLocale               = null;
        theExactLocaleMatch     = null;
        theColloquial           = true;

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
     * Obtain value of the propertyName property.
     *
     * @return value of the propertyName property
     * @see #setPropertyName
     */
    public final String getPropertyName()
    {
        return thePropertyName;
    }

    /**
     * Set value of the propertyName property.
     *
     * @param newValue new value of the propertyName property
     * @see #getPropertyName
     */
    public final void setPropertyName(
            String newValue )
    {
        thePropertyName = newValue;
    }

    /**
     * Obtain value of the nullString property.
     *
     * @return value of the nullString property
     * @see #setNullString
     */
    public String getNullString()
    {
        return theNullString;
    }

    /**
     * Set value of the nullString property.
     *
     * @param newValue new value of the nullString property
     * @see #getNullString
     */
    public void setNullString(
            String newValue )
    {
        theNullString = newValue;
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
     * Obtain value of the locale property.
     *
     * @return value of the locale property
     * @see #setLocale
     */
    public String getLocale()
    {
        return theLocale;
    }

    /**
     * Set value of the locale property.
     *
     * @param newValue new value of the locale property
     * @see #getLocale
     */
    public void setLocale(
            String newValue )
    {
        theLocale = newValue;
    }

    /**
     * Obtain value of the exactLocaleMatch property.
     *
     * @return value of the exactLocaleMatch property
     * @see #setExactLocaleMatch
     */
    public String getExactLocaleMatch()
    {
        return theExactLocaleMatch;
    }

    /**
     * Set value of the exactLocaleMatch property.
     *
     * @param newValue new value of the exactLocaleMatch property
     * @see #getExactLocaleMatch
     */
    public void setExactLocaleMatch(
            String newValue )
    {
        theExactLocaleMatch = newValue;
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
        PropertyValue value = null;
        String        text  = null;

        if( thePropertyName != null ) {
            Object found = lookupOrThrow( theMeshTypeName, thePropertyName );

            if( found == null ) {
                value = null;
                
            } else if( found instanceof PropertyValue ) {
                value = (PropertyValue) found;
                
            } else if( found instanceof L10PropertyValueMap ) {
                L10PropertyValueMap map = (L10PropertyValueMap) found;
                
                if( theLocale == null ) {
                    value = map.getDefault();
                } else if( theFormatter.isTrue( theExactLocaleMatch )) {
                    value = map.getExact( theLocale );
                } else {
                    value = map.get( theLocale );
                }

            } else if( found instanceof DataType ) {
                try {
                    text = formatDataType(
                            pageContext,
                            (DataType) found,
                            theStringRepresentation,
                            theMaxLength );

                } catch( StringifierException ex ) {
                    throw new JspException( ex );
                }

            } else {
                throw new ClassCastException( "Found object named " + theMeshTypeName + " is neither a PropertyValue nor an L10Map: " + found );
            }

        } else {
            Object found = lookupOrThrow( theMeshTypeName );

            MeshType type = (MeshType) found;

            value = type.getUserVisibleName();
            if( value == null ) {
                value = type.getName();
            }
        }
        if( text == null ) {
            // a bit of a funny structure, but the best I can do
            try {
                text = formatPropertyValue(
                        pageContext,
                        value,
                        theNullString,
                        theStringRepresentation,
                        theMaxLength,
                        theColloquial );

            } catch( StringifierException ex ) {
                throw new JspException( ex );
            }
        }
        print( text );
        
        return SKIP_BODY;
    }

    /**
     * String containing the name of the bean that is the MeshType.
     */
    protected String theMeshTypeName;

    /**
     * Name of the property of the MeshType to show.
     */
    protected String thePropertyName;

    /**
     * The String that is shown if a value is null.
     */
    protected String theNullString;

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
    
    /**
     * Locale.
     */
    protected String theLocale;
    
    /**
     * Is an exact locale match required.
     */
    protected String theExactLocaleMatch;
}
