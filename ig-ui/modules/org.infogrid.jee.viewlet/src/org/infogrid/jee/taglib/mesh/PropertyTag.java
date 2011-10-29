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
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridTag;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.TypedMeshObjectFacade;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.text.StringifierException;

/**
 * Tag that renders a property of a <code>MeshObject</code>.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class PropertyTag
        extends
            AbstractRestInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public PropertyTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObject           = null;
        theMeshObjectName       = null;
        theMeshObjectVarName    = null;
        thePropertyTypeName     = null;
        thePropertyType         = null;
        theNullString           = null;
        theStringRepresentation = null;
        theMaxLength            = -1;
        theColloquial           = true;
        theAllowNull            = true;
        theState                = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the meshObject property.
     *
     * @return value of the meshObject property
     * @see #setMeshObject
     */
    public final Object getMeshObject()
    {
        return theMeshObject;
    }

    /**
     * Set value of the meshObject property.
     *
     * @param newValue new value of the meshObject property
     * @see #getMeshObject
     */
    public final void setMeshObject(
            Object newValue )
    {
        theMeshObject = newValue;
    }

    /**
     * Obtain value of the meshObjectName property.
     *
     * @return value of the meshObjectName property
     * @see #setMeshObjectName
     */
    public final String getMeshObjectName()
    {
        return theMeshObjectName;
    }

    /**
     * Set value of the meshObjectName property.
     *
     * @param newValue new value of the meshObjectName property
     * @see #getMeshObjectName
     */
    public final void setMeshObjectName(
            String newValue )
    {
        theMeshObjectName = newValue;
    }

    /**
     * Obtain value of the meshObjectVarName property.
     *
     * @return value of the meshObjectVarName property
     * @see #setMeshObjectVarName
     */
    public final String getMeshObjectVarName()
    {
        return theMeshObjectVarName;
    }

    /**
     * Set value of the meshObjectVarName property.
     *
     * @param newValue new value of the meshObjectVarName property
     * @see #getMeshObjectVarName
     */
    public final void setMeshObjectVarName(
            String newValue )
    {
        theMeshObjectVarName = newValue;
    }

    /**
     * Obtain value of the propertyTypeName property.
     *
     * @return value of the propertyTypeName property
     * @see #setPropertyTypeName
     */
    public final String getPropertyTypeName()
    {
        return thePropertyTypeName;
    }

    /**
     * Set value of the propertyTypeName property.
     *
     * @param newValue new value of the propertyTypeName property
     * @see #getPropertyTypeName
     */
    public final void setPropertyTypeName(
            String newValue )
    {
        thePropertyTypeName = newValue;
    }
    
    /**
     * Obtain value of the propertyType property.
     *
     * @return value of the propertyType property
     * @see #setPropertyType
     */
    public final String getPropertyType()
    {
        return thePropertyType;
    }

    /**
     * Set value of the propertyType property.
     *
     * @param newValue new value of the propertyType property
     * @see #getPropertyType
     */
    public final void setPropertyType(
            String newValue )
    {
        thePropertyType = newValue;
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
     * @see #getColloquial
     */
    public void setColloquial(
            boolean newValue )
    {
        theColloquial = newValue;
    }

    /**
     * Obtain value of the allowNull property.
     *
     * @return value of the allowNull property
     * @see #setAllowNull
     */
    public boolean getAllowNull()
    {
        return theAllowNull;
    }

    /**
     * Set value of the allowNull property.
     *
     * @param newValue new value of the allowNull property
     * @see #getAllowNull
     */
    public void setAllowNull(
            boolean newValue )
    {
        theAllowNull = newValue;
    }

    /**
     * Obtain value of the state property.
     *
     * @return value of the state property
     * @see #setState
     */
    public String getState()
    {
        return theState;
    }

    /**
     * Set value of the state property.
     *
     * @param newValue new value of the state property
     * @see #getState
     */
    public void setState(
            String newValue )
    {
        // make first letter uppercase
        if( newValue != null && newValue.length() > 0 && !Character.isUpperCase( newValue.charAt( 0 ))) {
            StringBuilder buf = new StringBuilder();
            buf.append( Character.toUpperCase( newValue.charAt( 0 )));
            buf.append( newValue.substring( 1 ));
            theState = buf.toString();
        } else {
            theState = newValue;
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
        MeshObject   obj  = null;
        PropertyType type = null;

        if( theMeshObject != null ) {
            if( theMeshObjectName != null ) {
                throw new JspException( "Must not specify both meshObject and meshObjectName" );
            }
            if( theMeshObjectVarName != null ) {
                throw new JspException( "Must not specify both meshObject and meshObjectVarName" );
            }

            if( theMeshObject instanceof MeshObject ) {
                obj = (MeshObject) theMeshObject;
            } else if( theMeshObject instanceof TypedMeshObjectFacade ) {
                obj = ((TypedMeshObjectFacade)theMeshObject).get_Delegate();
            } else if( theMeshObject instanceof MeshObjectIdentifier ) {
                if( theFormatter.isTrue( getIgnore() )) {
                    obj = ((RestfulJeeFormatter)theFormatter).findMeshObject( (MeshObjectIdentifier)theMeshObject );
                } else {
                    obj = ((RestfulJeeFormatter)theFormatter).findMeshObjectOrThrow( (MeshObjectIdentifier)theMeshObject );
                }
            } else if( theMeshObject instanceof String ) {
                if( theFormatter.isTrue( getIgnore() )) {
                    obj = ((RestfulJeeFormatter)theFormatter).findMeshObject( (String) theMeshObject );
                } else {
                    obj = ((RestfulJeeFormatter)theFormatter).findMeshObjectOrThrow( (String) theMeshObject );
                }
            } else {
                throw new JspException( "Unexpected type " + theMeshObject.getClass().getName() + ": " + theMeshObject );
            }

        } else if( theMeshObjectName != null ) {
            if( theMeshObjectVarName != null ) {
                throw new JspException( "Must not specify both meshObjectName and meshObjectVarName" );
            }
            if( theFormatter.isTrue( getIgnore() )) {
                obj = lookupMeshObject( theMeshObjectName );
            } else {
                obj = lookupMeshObjectOrThrow( theMeshObjectName );
            }
        } else if( theMeshObjectVarName == null ) {
            throw new JspException( "Must specify either meshObject, meshObjectName or meshObjectVarName" );
        }
        if( obj == null ) {
            // if we get here, ignore is necessarily true
            return SKIP_BODY;
        }
        
        if( thePropertyType != null ) {
            if( thePropertyTypeName != null ) {
                throw new JspException( "Must not specify both propertyTypeName and propertyType" );
            }
            
            type = findPropertyTypeOrThrow( obj, thePropertyType );

        } else if( thePropertyTypeName != null ) {
            type = (PropertyType) lookupOrThrow( thePropertyTypeName );

        } else {
            throw new JspException( "Must specify either propertyTypeName or propertyType" );
        }
        if( theMeshObjectVarName != null && !theMeshObjectVarName.startsWith( "shell." )) {
            throw new JspException( "meshObjectVarName must be specified as 'shell.xxx', not " + theMeshObjectVarName ); // see HttpShellKeywords.PREFIX, which cannot be imported to avoid creating a dependency
        }

        Integer varCounter = (Integer) lookup( VARIABLE_COUNTER_NAME );
        if( varCounter == null ) {
            varCounter = 0;
        }

        String realStringRep;

        if( theState == null ) {
            realStringRep = theStringRepresentation;

        } else if( theStringRepresentation == null ) {
            realStringRep = theState;

        } else {
            realStringRep = theState + theStringRepresentation;
        }

        String editVar;
        if( theMeshObjectVarName != null ) {
            editVar = theMeshObjectVarName;
        } else {
            editVar = String.format( VARIABLE_PATTERN, varCounter );
        }

        try {
            String text = formatProperty(
                        pageContext,
                        obj,
                        type,
                        editVar,
                        varCounter,
                        theNullString,
                        realStringRep,
                        theMaxLength,
                        theColloquial,
                        theAllowNull );
            print( text );

        } catch( StringifierException ex ) {
            throw new JspException( ex );
        } catch( IllegalPropertyTypeException ex ) {
            throw new JspException( ex );
        } catch( NotPermittedException ex ) {
            throw new JspException( ex );
        }

        pageContext.getRequest().setAttribute( VARIABLE_COUNTER_NAME, varCounter.intValue()+1 );
        
        return SKIP_BODY;
    }

    /**
     * The MeshObject to render.
     */
    protected Object theMeshObject;

    /**
     * String containing the name of the bean that is the MeshObject whose property we render.
     */
    protected String theMeshObjectName;

    /**
     * String containing the name of the shell variable referring to the MeshObject whose property we render.
     */
    protected String theMeshObjectVarName;

    /**
     * String containing the name of the bean that is the PropertyType.
     */
    protected String thePropertyTypeName;

    /**
     * Identifier of the PropertyType. This is mutually exclusive with thePropertyTypeName.
     */
    protected String thePropertyType;
    
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
     * If editing, should the Property be allowed to be null.
     */
    protected boolean theAllowNull;

    /**
     * The state of the tag, e.g. edit vs. view.
     */
    protected String theState;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( PropertyTag.class );

    /**
     * Name of the request variable that we use internally to count up variables.
     */
    public static final String VARIABLE_COUNTER_NAME = PropertyTag.class.getName().replace( '.', '_' ) + "-varCounter";

    /**
     * Format String to generate variable names.
     */
    public static final String VARIABLE_PATTERN = theResourceHelper.getResourceStringOrDefault(
            "VariablePattern",
            "shell.propertyTagMeshObject%d" );
}
