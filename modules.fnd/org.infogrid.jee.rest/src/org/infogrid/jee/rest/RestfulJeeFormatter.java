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

package org.infogrid.jee.rest;

import java.lang.reflect.Method;
import java.net.URISyntaxException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import org.infogrid.jee.JeeFormatter;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.servlet.InitializationFilter;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshObjectIdentifierFactory;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.text.SimpleModelPrimitivesStringRepresentation;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.text.SimpleModelPrimitivesStringRepresentationDirectory;
import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.util.text.StringRepresentationDirectory;

/**
 * Collection of utility methods that are useful with InfoGrid JEE applications
 * and aware of InfoGrid REST conventions.
 */
public class RestfulJeeFormatter
        extends
            JeeFormatter
{
    /**
     * Factory method.
     * 
     * @return the created JeeFormatter
     */
    public static RestfulJeeFormatter create()
    {
        SimpleModelPrimitivesStringRepresentationDirectory stringRepDir = SimpleModelPrimitivesStringRepresentationDirectory.create();
        return new RestfulJeeFormatter( stringRepDir );
    }
    
    /**
     * Factory method.
     * 
     * @param stringRepDir the StringRepresentationDirectory to use
     * @return the created JeeFormatter
     */
    public static RestfulJeeFormatter create(
            StringRepresentationDirectory stringRepDir )
    {
        return new RestfulJeeFormatter( stringRepDir );
    }
    
    /**
     * Private constructor for subclasses only, use factory method.
     * 
     * @param stringRepDir the StringRepresentationDirectory to use
     */
    protected RestfulJeeFormatter(
            StringRepresentationDirectory stringRepDir )
    {
        super( stringRepDir );
    }

    /**
     * <p>Return the value of the (non-nested) property of the specified
     * name, for the specified bean, with no type conversions. If no such object is found,
     * return <code>null</code> instead.</p>
     * 
     * @param obj object whose property is to be extracted
     * @param propertyName possibly nested name of the property to be extracted
     * @return the found value, or null
     * @throws NullPointerException if the given object was null
     * @throws IllegalArgumentException thrown if an illegal propertyName was given
     */
    @Override
    public Object getSimpleProperty(
            Object obj,
            String propertyName )
    {
        if( obj == null ) {
            throw new NullPointerException( "Object cannot be null" );
        }

        if( propertyName == null ) {
            throw new IllegalArgumentException( "Property name cannot be null" );
        }
        if( propertyName.length() == 0 ) {
            throw new IllegalArgumentException( "Property name cannot be empty" );
        }
        
        // try the getter method first. if that fails, try MeshObject-specific methods.
        
        String getterName = "get" + Character.toUpperCase( propertyName.charAt( 0 )) + propertyName.substring( 1 );

        try {
            Method getterMethod = obj.getClass().getMethod( getterName, (Class []) null );
            Object ret          = getterMethod.invoke( obj, (Object []) null );

            return ret;

        } catch( Throwable ex ) {
            // ignore
        }
        
        // If this is a MeshObject, attempt to do PropertyType simpleLookup
        if( obj instanceof MeshObject ) {
            MeshObject realObj = (MeshObject) obj;
            
            if( "timeCreated".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeCreated();
            } else if( "timeUpdated".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeUpdated();
            } else if( "timeRead".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeRead();
            } else if( "timeExpires".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeExpires();
            }
            
            try {
                Object ret = realObj.getPropertyValueByName( propertyName );
                return ret;

            } catch( MeshTypeNotFoundException ex ) {
                // this didn't work. Ignore and proceed as normal
            } catch( NotPermittedException ex ) {
                return "[access denied]";
            }
        }
        return null;
    }

    /**
     * <p>Return the value of the (non-nested) property of the specified
     * name, for the specified bean, with no type conversions. If no such object is found,
     * throw a <code>JspException</code>.</p>
     * 
     * @param obj object whose property is to be extracted
     * @param propertyName possibly nested name of the property to be extracted
     * @return the found value
     * @throws NullPointerException if the given object was null
     * @throws IllegalArgumentException thrown if an illegal propertyName was given
     * @throws JspException thrown if the object's property could not be found
     */
    @Override
    public Object getSimplePropertyOrThrow(
            Object obj,
            String propertyName )
        throws
            JspException
    {
        if( obj == null ) {
            throw new NullPointerException( "Object cannot be null" );
        }

        if( propertyName == null ) {
            throw new IllegalArgumentException( "Property name cannot be null" );
        }
        if( propertyName.length() == 0 ) {
            throw new IllegalArgumentException( "Property name cannot be empty" );
        }

        // If this is a MeshObject, attempt to do PropertyType simpleLookup
        if( obj instanceof MeshObject ) {
            MeshObject realObj = (MeshObject) obj;
            
            if( "timeCreated".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeCreated();
            } else if( "timeUpdated".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeUpdated();
            } else if( "timeRead".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeRead();
            } else if( "timeExpires".equalsIgnoreCase( propertyName )) {
                return realObj.getTimeExpires();
            }
            
            try {
                Object ret = realObj.getPropertyValueByName( propertyName );
                return ret;

            } catch( MeshTypeNotFoundException ex ) {
                // this didn't work. Ignore and proceed as normal
            } catch( NotPermittedException ex ) {
                return "[access denied]";
            }
        }
        
        // construct getter method name
        String getterName = "get" + Character.toUpperCase( propertyName.charAt( 0 )) + propertyName.substring( 1 );

        try {
            Method getterMethod = obj.getClass().getMethod( getterName, (Class []) null );
            Object ret          = getterMethod.invoke( obj, (Object []) null );

            return ret;

        } catch( Throwable ex ) {
            throw new JspException( "Cannot call getter method for property " + propertyName + " on object " + obj );
        }
    }
    
    /**
     * Find a PropertyType on a MeshObject by name.
     *
     * @param obj the MeshObject
     * @param name the name of the PropertyType
     * @return the found PropertyType
     * @throws NullPointerException thrown if the MeshObject or the name of the PropertyType were null
     */
    public PropertyType findPropertyType(
            MeshObject obj,
            String     name )
    {
        // tolerate lowercase first characters.
        
        if( obj == null ) {
            throw new NullPointerException( "MeshObject cannot be null" );
        } 
        if( name == null || name.length() == 0 ) {
            throw new NullPointerException( "PropertyType name cannot be empty" );
        }

        // Try by identifier first
        PropertyType [] allTypes = obj.getAllPropertyTypes();
        for( PropertyType current : allTypes ) {
            if( current.getIdentifier().toExternalForm().equals( name )) {
                return current;
            }
        }
        
        // Now try by shor name
        char firstChar = name.charAt( 0 );
        String capitalizedName;
        if( Character.isUpperCase( firstChar )) {
            capitalizedName = name;
        } else {
            capitalizedName = new StringBuilder( name.length() ).append( Character.toUpperCase( firstChar )).append( name.substring( 1 )).toString();
        }

        for( PropertyType current : allTypes ) {
            if( current.getName().toString().equals( capitalizedName )) {
                return current;
            }
        }
        return null;
    }

    /**
     * Find a PropertyType on a MeshObject by name.
     *
     * @param obj the MeshObject
     * @param name the name of the PropertyType
     * @return the found PropertyType
     * @throws NullPointerException thrown if the MeshObject or the name of the PropertyType were null
     * @throws JspException thrown if the PropertyType could not be found
     */
    public PropertyType findPropertyTypeOrThrow(
            MeshObject obj,
            String     name )
        throws
            JspException
    {
        PropertyType ret = findPropertyType( obj, name );
        if( ret == null ) {
            throw new JspException( "Could not find property " + name + " on MeshObject " + obj );
        }
        return ret;
    }


    /**
     * Format a PropertyValue.
     *
     * @param pageContext the PageContext object for this page
     * @param value the PropertyValue
     * @param nullString the String to display of the value is null
     * @param stringRepresentation the StringRepresentation for PropertyValues
     * @return the String to display
     */
    public String formatPropertyValue(
            PageContext        pageContext,
            PropertyValue      value,
            String             nullString,
            String             stringRepresentation )
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );
        
        String ret = PropertyValue.toStringRepresentationOrNull( value, rep, context );
        if( ret != null ) {
            return ret;
        } else {
            return nullString;
        }
    }

    /**
     * Format an Identifier.
     *
     * @param pageContext the PageContext object for this page
     * @param identifier the Identifier to format
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatMeshTypeIdentifier(
            PageContext        pageContext,
            MeshTypeIdentifier identifier,
            String             stringRepresentation )
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );
 
        String ret = identifier.toStringRepresentation( rep, context );
        
        return ret;
    }

    /**
     * Format the start of the identifier of a MeshObject.
     *
     * @param pageContext the PageContext object for this page
     * @param mesh the MeshObject whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatMeshObjectIdentifierStart(
            PageContext        pageContext,
            MeshObject         mesh,
            String             stringRepresentation )
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        String ret = mesh.toStringRepresentation( rep, context );
        return ret;
    }

    /**
     * Format the end of the identifier of a MeshObject.
     *
     * @param pageContext the PageContext object for this page
     * @param mesh the MeshObject whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatMeshObjectIdentifierEnd(
            PageContext        pageContext,
            MeshObject         mesh,
            String             stringRepresentation )
    {
        return ""; // nothing
    }

    /**
     * Format the start of the link to a MeshObject.
     *
     * @param pageContext the PageContext object for this page
     * @param mesh the MeshObject whose identifier is to be formatted
     * @param rootPath alternate root path to use, if any
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatMeshObjectLinkStart(
            PageContext        pageContext,
            MeshObject         mesh,
            String             rootPath,
            String             stringRepresentation )
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        String ret = mesh.toStringRepresentationLinkStart( rep, context );
        return ret;
    }

    /**
     * Format the end of the link to a MeshObject.
     *
     * @param pageContext the PageContext object for this page
     * @param mesh the MeshObject whose identifier is to be formatted
     * @param rootPath alternate root path to use, if any
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatMeshObjectLinkEnd(
            PageContext        pageContext,
            MeshObject         mesh,
            String             rootPath,
            String             stringRepresentation )
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        String ret = mesh.toStringRepresentationLinkEnd( rep, context );
        return ret;
}

    /**
     * Recreate an MeshObjectIdentifier from a String.
     * 
     * @param factory the MeshObjectIdentifierFactory to use
     * @param representation the StringRepresentation of the to-be-parsed String
     * @param s the String
     * @return the MeshObjectIdentifier
     * @throws URISyntaxException thrown if a syntax error occurred
     */
    public MeshObjectIdentifier fromMeshObjectIdentifier(
            MeshObjectIdentifierFactory factory,
            StringRepresentation        representation,
            String                      s )
        throws
            URISyntaxException
    {
        if( s == null ) {
            return null;
        }
        MeshObjectIdentifier ret = factory.fromStringRepresentation( representation, s );

        return ret;
    }

    /**
     * Format the start of the identifier of a MeshBase.
     *
     * @param pageContext the PageContext object for this page
     * @param base the MeshBase whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatMeshBaseIdentifierStart(
            PageContext        pageContext,
            MeshBase           base,
            String             stringRepresentation )
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        String ret = base.toStringRepresentation( rep, context );
        return ret;
    }

    /**
     * Format the end of the identifier of a MeshBase.
     *
     * @param pageContext the PageContext object for this page
     * @param base the MeshBase whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatMeshBaseIdentifierEnd(
            PageContext        pageContext,
            MeshBase           base,
            String             stringRepresentation )
    {
        return ""; // nothing
    }

    /**
     * Format the start of a link to a MeshBase.
     *
     * @param pageContext the PageContext object for this page
     * @param base the MeshBase whose identifier is to be formatted
     * @param rootPath alternate root path to use, if any
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatMeshBaseLinkStart(
            PageContext        pageContext,
            MeshBase           base,
            String             rootPath,
            String             stringRepresentation )
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        String ret = base.toStringRepresentationLinkStart( rep, context );
        return ret;
    }

    /**
     * Format the end of a link to a MeshBase.
     *
     * @param pageContext the PageContext object for this page
     * @param base the MeshBase whose identifier is to be formatted
     * @param rootPath alternate root path to use, if any
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     */
    public String formatMeshBaseLinkEnd(
            PageContext        pageContext,
            MeshBase           base,
            String             rootPath,
            String             stringRepresentation )
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        String ret = base.toStringRepresentationLinkEnd( rep, context );
        return ret;
    }

    /**
     * Determine the correct StringRepresentation, by correcting any supplied value and/or
     * picking a reasonable default.
     * 
     * @param in the original value
     * @return the StringRepresentation
     */
    @Override
    public StringRepresentation determineStringRepresentation(
            String in )
    {
        String sanitized;
        
        if( in == null || in.length() == 0 ) {
            sanitized = "Html";
        } else {
            StringBuilder temp = new StringBuilder( in.length() );
            temp.append( Character.toUpperCase( in.charAt( 0 )));
            temp.append( in.substring( 1 ).toLowerCase() );
            sanitized = temp.toString();
        }
        StringRepresentation ret = SimpleModelPrimitivesStringRepresentation.create( sanitized );
        return ret;
    }

    /**
     * Determine whether the fully-qualified context path that cane be determined from a
     * PageContext identifies this MeshBase as the default MeshBase.
     * 
     * @param base the MeshBase whose identifier is to be formatted
     * @return true if this MeshBase is the default MeshBase
     */
    public boolean isDefaultMeshBase(
            MeshBase base )
    {
        // FIXME this can be simpler right?
        String mbIdentifier = base.getIdentifier().toExternalForm();
        
        MeshBase defaultMb           = InfoGridWebApp.getSingleton().getApplicationContext().findContextObjectOrThrow( MeshBase.class );
        String   defaultMbIdentifier = defaultMb.getIdentifier().toExternalForm();
        
        if( defaultMbIdentifier.equals( mbIdentifier )) {
            return true;
        } else {
            return false;
        }
    }
}
