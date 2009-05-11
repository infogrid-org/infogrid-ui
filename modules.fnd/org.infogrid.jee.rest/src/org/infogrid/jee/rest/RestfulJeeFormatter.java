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

package org.infogrid.jee.rest;

import java.lang.reflect.Method;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import org.infogrid.jee.JeeFormatter;
import org.infogrid.jee.app.InfoGridWebApp;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.servlet.InitializationFilter;
import org.infogrid.jee.templates.servlet.TemplatesFilter;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.text.MeshStringRepresentationContext;
import org.infogrid.mesh.text.SimpleMeshStringRepresentationContext;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshObjectIdentifierFactory;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.text.ModelPrimitivesStringRepresentationParameters;
import org.infogrid.model.primitives.text.SimpleModelPrimitivesStringRepresentationParameters;
import org.infogrid.model.traversal.SequentialCompoundTraversalSpecification;
import org.infogrid.model.traversal.StayRightHereTraversalSpecification;
import org.infogrid.model.traversal.TraversalSpecification;
import org.infogrid.modelbase.MeshTypeIdentifierFactory;
import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.modelbase.MeshTypeWithIdentifierNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.SimpleStringRepresentationParameters;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationContext;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringRepresentationParseException;
import org.infogrid.util.text.StringifierException;

/**
 * Collection of utility methods that are useful with InfoGrid JEE applications
 * and aware of InfoGrid REST conventions.
 */
public class RestfulJeeFormatter
        extends
            JeeFormatter
{
    private static final Log log = Log.getLogInstance( RestfulJeeFormatter.class ); // our own, private logger

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
        
        // Now try by short name
        char firstChar = name.charAt( 0 );
        String capitalizedName;
        if( Character.isUpperCase( firstChar )) {
            capitalizedName = name;
        } else {
            capitalizedName = new StringBuilder( name.length() ).append( Character.toUpperCase( firstChar )).append( name.substring( 1 )).toString();
        }

        for( PropertyType current : allTypes ) {
            if( current.getName().equals( (Object) capitalizedName )) { // StringValue vs. String is correct
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
     * Find a TraversalSpecification, or return null.
     *
     * @param name name of the TraversalSpecification
     * @return the found TraversalSpecification, or null
     * @throws MeshTypeWithIdentifierNotFoundException thrown if a MeshTypeIdentifier was not found
     */
    public TraversalSpecification findTraversalSpecification(
            String name )
        throws
            MeshTypeWithIdentifierNotFoundException
    {
        ModelBase                 mb     = InfoGridWebApp.getSingleton().getApplicationContext().findContextObject( ModelBase.class );
        MeshTypeIdentifierFactory idFact = mb.getMeshTypeIdentifierFactory();

        TraversalSpecification ret;

        name = name.trim();

        String [] componentNames = name.split( "!" );

        if( componentNames.length == 1 ) {
            if( ".".equals( name )) {
                ret = StayRightHereTraversalSpecification.create();
            } else {
                MeshTypeIdentifier id = idFact.fromExternalForm( name );

                ret = mb.findRoleTypeByIdentifier( id );
            }
        } else {
            TraversalSpecification [] components = new TraversalSpecification[ componentNames.length ];
            for( int i=0 ; i<componentNames.length ; ++i ) {
                componentNames[i] = componentNames[i].trim();
                
                if( ".".equals( componentNames[i] )) {
                    components[i] = StayRightHereTraversalSpecification.create();
                } else {
                    MeshTypeIdentifier id = idFact.fromExternalForm( componentNames[i] );

                    components[i] = mb.findRoleTypeByIdentifier( id );

                }
            }
            ret = SequentialCompoundTraversalSpecification.create( components );
        }

        return ret;
    }

    /**
     * Find a TraversalSpecification, or throw an Exception.
     *
     * @param name name of the TraversalSpecification
     * @return the found TraversalSpecification
     * @throws JspException thrown if the TraversalSpecification could not be found
     */
    public TraversalSpecification findTraversalSpecificationOrThrow(
            String name )
        throws
            JspException
    {
        name = name.trim();
        if( "*".equals( name )) {
            return null;
        }

        try {
            TraversalSpecification ret = findTraversalSpecification( name );
            return ret;

        } catch( MeshTypeWithIdentifierNotFoundException ex ) {
            throw new JspException( ex );
        }
    }

    /**
     * Find a sequence of TraversalSpecifications, or throw an Exception.
     *
     * @param name name of the TraversalSpecification sequence
     * @return the found sequence of TraversalSpecifications
     * @throws JspException thrown if the TraversalSpecification could not be found
     */
    public TraversalSpecification [] findTraversalSpecificationSequenceOrThrow(
            String name )
        throws
            JspException
    {
        String [] components = name.split( "\\s" );

        TraversalSpecification [] ret = new TraversalSpecification[ components.length ];
        for( int i=0 ; i<components.length ; ++i ) {
            ret[i] = findTraversalSpecificationOrThrow( components[i] );
        }
        return ret;
    }

    /**
     * Format a PropertyValue.
     *
     * @param pageContext the PageContext object for this page
     * @param owningMeshObject the MeshObject that owns this PropertyValue, if any
     * @param propertyType the PropertyType of the PropertyValue, if any
     * @param value the PropertyValue
     * @param editVar name of the HTML form elements to use
     * @param nullString the String to display of the value is null
     * @param stringRepresentation the StringRepresentation for PropertyValues
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial if applicable, output in colloquial form
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatPropertyValue(
            PageContext   pageContext,
            MeshObject    owningMeshObject,
            PropertyType  propertyType,
            PropertyValue value,
            String        editVar,
            String        nullString,
            String        stringRepresentation,
            int           maxLength,
            boolean       colloquial )
        throws
            StringifierException
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );
        
        StringRepresentationParameters pars = constructStringRepresentationParameters( maxLength, colloquial, owningMeshObject, propertyType, nullString, editVar );

        String ret = PropertyValue.toStringRepresentation( value, rep, context, pars );
        return ret;
    }

    /**
     * Format an Identifier.
     *
     * @param pageContext the PageContext object for this page
     * @param identifier the Identifier to format
     * @param stringRepresentation the StringRepresentation to use
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshTypeIdentifier(
            PageContext        pageContext,
            MeshTypeIdentifier identifier,
            String             stringRepresentation,
            int                maxLength )
        throws
            StringifierException
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );
 
        StringRepresentationParameters pars = constructStringRepresentationParameters( maxLength, false );
        String ret = identifier.toStringRepresentation( rep, context, pars );
        
        return ret;
    }

    /**
     * Format the start of a MeshObject.
     *
     * @param pageContext the PageContext object for this page
     * @param mesh the MeshObject that is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial should the value be emitted in colloquial form
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshObjectStart(
            PageContext pageContext,
            MeshObject  mesh,
            String      stringRepresentation,
            int         maxLength,
            boolean     colloquial )
        throws
            StringifierException
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        StringRepresentationParameters pars = constructStringRepresentationParameters( maxLength, colloquial );

        String ret = mesh.toStringRepresentation( rep, context, pars );
        return ret;
    }

    /**
     * Format the end of a MeshObject.
     *
     * @param pageContext the PageContext object for this page
     * @param mesh the MeshObject that is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshObjectEnd(
            PageContext pageContext,
            MeshObject  mesh,
            String      stringRepresentation )
        throws
            StringifierException
    {
        return ""; // nothing
    }

    /**
     * Format the start of the identifier of a MeshObject.
     *
     * @param pageContext the PageContext object for this page
     * @param mesh the MeshObject whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshObjectIdentifierStart(
            PageContext        pageContext,
            MeshObject         mesh,
            String             stringRepresentation,
            int                maxLength )
        throws
            StringifierException
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        HashMap<String,Object> localMap = new HashMap<String,Object>();
        localMap.put( MeshStringRepresentationContext.MESHOBJECT_KEY, mesh );

        SimpleMeshStringRepresentationContext delegateContext
                = SimpleMeshStringRepresentationContext.create( localMap, context );

        StringRepresentationParameters pars = constructStringRepresentationParameters( maxLength, false );
        String ret = mesh.getIdentifier().toStringRepresentation( rep, delegateContext, pars );
        return ret;
    }

    /**
     * Format the end of the identifier of a MeshObject.
     *
     * @param pageContext the PageContext object for this page
     * @param mesh the MeshObject whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshObjectIdentifierEnd(
            PageContext        pageContext,
            MeshObject         mesh,
            String             stringRepresentation )
        throws
            StringifierException
    {
        return ""; // nothing
    }

    /**
     * Format the start of the link to a MeshObject.
     *
     * @param pageContext the PageContext object for this page
     * @param mesh the MeshObject whose identifier is to be formatted
     * @param rootPath alternate root path to use, if any
     * @param addArguments additional arguments to the URL, if any
     * @param target the HTML target, if any
     * @param title the HTML title attribute, if any
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshObjectLinkStart(
            PageContext        pageContext,
            MeshObject         mesh,
            String             rootPath,
            String             addArguments,
            String             target,
            String             title,
            String             stringRepresentation )
        throws
            StringifierException
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        addArguments = potentiallyAddAppContext( (HttpServletRequest) pageContext.getRequest(), addArguments );

        HashMap<String,Object> localMap = new HashMap<String,Object>();
        localMap.put( MeshStringRepresentationContext.MESHOBJECT_KEY, mesh );

        SimpleMeshStringRepresentationContext delegateContext
                = SimpleMeshStringRepresentationContext.create( localMap, context );

        String ret = mesh.getIdentifier().toStringRepresentationLinkStart( addArguments, target, title, rep, delegateContext );
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
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshObjectLinkEnd(
            PageContext        pageContext,
            MeshObject         mesh,
            String             rootPath,
            String             stringRepresentation )
        throws
            StringifierException
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        HashMap<String,Object> localMap = new HashMap<String,Object>();
        localMap.put( MeshStringRepresentationContext.MESHOBJECT_KEY, mesh );

        SimpleMeshStringRepresentationContext delegateContext
                = SimpleMeshStringRepresentationContext.create( localMap, context );

        String ret = mesh.getIdentifier().toStringRepresentationLinkEnd( rep, delegateContext );
        return ret;
}

    /**
     * Recreate an MeshObjectIdentifier from a String.
     * 
     * @param factory the MeshObjectIdentifierFactory to use
     * @param representation the StringRepresentation of the to-be-parsed String
     * @param s the String
     * @return the MeshObjectIdentifier
     * @throws StringRepresentationParseException thrown if a syntax error occurred
     */
    public MeshObjectIdentifier fromMeshObjectIdentifier(
            MeshObjectIdentifierFactory factory,
            StringRepresentation        representation,
            String                      s )
        throws
            StringRepresentationParseException
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
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial should the value be emitted in colloquial form
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshBaseIdentifierStart(
            PageContext        pageContext,
            MeshBase           base,
            String             stringRepresentation,
            int                maxLength,
            boolean            colloquial )
        throws
            StringifierException
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        StringRepresentationParameters pars = constructStringRepresentationParameters( maxLength, colloquial );
        String ret = base.toStringRepresentation( rep, context, pars );
        return ret;
    }

    /**
     * Format the end of the identifier of a MeshBase.
     *
     * @param pageContext the PageContext object for this page
     * @param base the MeshBase whose identifier is to be formatted
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshBaseIdentifierEnd(
            PageContext        pageContext,
            MeshBase           base,
            String             stringRepresentation )
        throws
            StringifierException
    {
        return ""; // nothing
    }

    /**
     * Format the start of a link to a MeshBase.
     *
     * @param pageContext the PageContext object for this page
     * @param base the MeshBase whose identifier is to be formatted
     * @param rootPath alternate root path to use, if any
     * @param addArguments additional arguments to the URL, if any
     * @param target the HTML target, if any
     * @param title title of the HTML link, if any
     * @param stringRepresentation the StringRepresentation to use
     * @return the String to display
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshBaseLinkStart(
            PageContext        pageContext,
            MeshBase           base,
            String             rootPath,
            String             addArguments,
            String             target,
            String             title,
            String             stringRepresentation )
        throws
            StringifierException
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        addArguments = potentiallyAddAppContext( (HttpServletRequest) pageContext.getRequest(), addArguments );

        String ret = base.toStringRepresentationLinkStart( addArguments, target, title, rep, context );
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
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String formatMeshBaseLinkEnd(
            PageContext        pageContext,
            MeshBase           base,
            String             rootPath,
            String             stringRepresentation )
        throws
            StringifierException
    {
        StringRepresentation        rep     = determineStringRepresentation( stringRepresentation );
        StringRepresentationContext context = (StringRepresentationContext) pageContext.getRequest().getAttribute( InitializationFilter.STRING_REPRESENTATION_CONTEXT_PARAMETER );

        String ret = base.toStringRepresentationLinkEnd( rep, context );
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

    /**
     * Helper method to potentially add the application context in link specifications.
     *
     * @param request the incoming request
     * @param addArguments the existing addArguments parameter
     * @return the new addArguments parameter
     */
    public static String potentiallyAddAppContext(
            HttpServletRequest request,
            String             addArguments )
    {
        SaneRequest sane       = SaneServletRequest.create( request );
        String      appContext = sane.getArgument( TemplatesFilter.LID_APPLICATION_CONTEXT_PARAMETER_NAME );

        String ret = addArguments; // by default, do nothing
        if( appContext != null ) {
            // don't override existing parameter, only if not given
            boolean haveAlready = true;
            if( addArguments == null ) {
                haveAlready = false;
            } else {
                String pattern = TemplatesFilter.LID_APPLICATION_CONTEXT_PARAMETER_NAME + "=";
                if( !addArguments.startsWith( pattern ) && addArguments.indexOf( "&" + pattern ) < 0 ) {
                    haveAlready = false;
                }
            }

            if( !haveAlready ) {
                StringBuilder toAdd = new StringBuilder();
                if( addArguments != null && addArguments.length() > 0 ) {
                    toAdd.append( addArguments );
                    toAdd.append( '&' );
                }
                toAdd.append( TemplatesFilter.LID_APPLICATION_CONTEXT_PARAMETER_NAME );
                toAdd.append( '=' );
                toAdd.append( HTTP.encodeToValidUrlArgument( appContext ));

                ret = toAdd.toString();
            }
        }
        return ret;
    }

    /**
     * Helper method to create a StringRepresentationParameters from a
     * maximum length, a colloquial, and owning MeshObject and PropertyType, if needed.
     *
     * @param maxLength the maximum length. -1 means unlimited.
     * @param colloquial if true, emit colloquial representation
     * @param owningMeshObject the MeshObject that owns this PropertyValue, if any
     * @param propertyType the PropertyType of the PropertyValue, if any
     * @param nullString the String to display of the value is null
     * @param editVar name of the HTML form elements to use
     * @return the StringRepresentationParameters, if any
     */
    public StringRepresentationParameters constructStringRepresentationParameters(
            int          maxLength,
            boolean      colloquial,
            MeshObject   owningMeshObject,
            PropertyType propertyType,
            String       nullString,
            String       editVar )
    {
        SimpleStringRepresentationParameters ret = null;
        if( maxLength >= 0 ) {
            if( ret == null ) {
                ret = SimpleModelPrimitivesStringRepresentationParameters.create();
            }
            ret.put( StringRepresentationParameters.MAX_LENGTH, maxLength );
        }
        if( colloquial ) {
            if( ret == null ) {
                ret = SimpleModelPrimitivesStringRepresentationParameters.create();
            }
            ret.put( StringRepresentationParameters.COLLOQUIAL, true );
        }
        if( owningMeshObject != null ) {
            if( ret == null ) {
                ret = SimpleModelPrimitivesStringRepresentationParameters.create();
            }
            ret.put( ModelPrimitivesStringRepresentationParameters.MESH_OBJECT, owningMeshObject );
        }
        if( propertyType != null ) {
            if( ret == null ) {
                ret = SimpleModelPrimitivesStringRepresentationParameters.create();
            }
            ret.put( ModelPrimitivesStringRepresentationParameters.PROPERTY_TYPE, propertyType );
        }
        if( nullString != null ) {
            if( ret == null ) {
                ret = SimpleModelPrimitivesStringRepresentationParameters.create();
            }
            ret.put( StringRepresentationParameters.NULL_STRING, nullString );
        }
        if( editVar != null ) {
            if( ret == null ) {
                ret = SimpleModelPrimitivesStringRepresentationParameters.create();
            }
            ret.put( StringRepresentationParameters.EDIT_VARIABLE, editVar );
        }
        return ret;
    }
}
