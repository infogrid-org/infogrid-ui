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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.util.InfoGridIterationTag;
import org.infogrid.mesh.IllegalPropertyTypeException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.StringHelper;

/**
 * Tag that iterates over all, or a subset, of a <code>MeshObject</code>'s properties.
 * It plays nicely with {@link PropertyTag PropertyTag} and {@link MeshTypeTag MeshTypeTag}.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshObjectPropertyIterateTag
        extends
            AbstractInfoGridBodyTag
        implements
            InfoGridIterationTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshObjectPropertyIterateTag()
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
        theMeshTypeName         = null;
        thePropertyList         = null;
        thePropertyValueLoopVar = null;
        thePropertyTypeLoopVar  = null;
        theListSeparator        = DEFAULT_LIST_SEPARATOR;
        theSkipNullProperty     = true;

        theIterator   = null;
        theMeshObject = null;

        super.initializeToDefaults();
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
     * Obtain value of the propertyList property.
     *
     * @return value of the propertyList property
     * @see #setPropertyList
     */
    public String getPropertyList()
    {
         return thePropertyList;
    }

    /**
     * Set value of the properties property.
     *
     * @param newValue new value of the properties property
     * @see #getPropertyList
     */
    public void setPropertyList(
            String newValue )
    {
        thePropertyList = newValue;
    }

    /**
     * Obtain value of the listSeparator property.
     *
     * @return value of the listSeparator property
     * @see #setListSeparator
     */
    public String getListSeparator()
    {
        return theListSeparator;
    }

    /**
     * Set value of the listSeparator property.
     *
     * @param newValue new value of the separator property
     * @see #getListSeparator
     */
    public void setListSeparator(
            String newValue )
    {
        theListSeparator = newValue;
    }

    /**
     * Obtain value of the skipNullProperty property.
     *
     * @return value of the skipNullProperty property
     * @see #setSkipNullProperty
     */
    public boolean getSkipNullProperty()
    {
        return theSkipNullProperty;
    }

    /**
     * Set value of the skipNullProperty property.
     *
     * @param newValue new value of the skipNullProperty property
     * @see #getSkipNullProperty
     */
    public void setSkipNullProperty(
            boolean newValue )
    {
        theSkipNullProperty = newValue;
    }
    
    /**
     * Obtain value of the propertyTypeLoopVar property.
     *
     * @return value of the propertyTypeLoopVar property
     * @see #setPropertyTypeLoopVar
     */
    public String getPropertyTypeLoopVar()
    {
        return thePropertyTypeLoopVar;
    }

    /**
     * Set value of the propertyTypeLoopVar property.
     *
     * @param newValue new value of the propertyTypeLoopVar property
     * @see #getPropertyTypeLoopVar
     */
    public void setPropertyTypeLoopVar(
            String newValue )
    {
        thePropertyTypeLoopVar = newValue;
    }

    /**
     * Obtain value of the propertyValueLoopVar property.
     *
     * @return value of the propertyValueLoopVar property
     * @see #setPropertyValueLoopVar
     */
    public String getPropertyValueLoopVar()
    {
        return thePropertyValueLoopVar;
    }

    /**
     * Set value of the propertyValueLoopVar property.
     *
     * @param newValue new value of the propertyValueLoopVar property
     * @see #getPropertyValueLoopVar
     */
    public void setPropertyValueLoopVar(
            String newValue )
    {
        thePropertyValueLoopVar = newValue;
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
        theMeshObject = (MeshObject) lookupOrThrow( theMeshObjectName );

        Collection<PropertyType> propertyTypesToShow = new ArrayList<PropertyType>();
        
        PropertyType [] allPropertyTypes;
        if( theMeshTypeName != null ) {
            EntityType meshType = (EntityType) lookupOrThrow( theMeshTypeName );
            allPropertyTypes = meshType.getAllPropertyTypes();
        } else {
            allPropertyTypes = theMeshObject.getAllPropertyTypes();
        }
        
        if( thePropertyList != null ) {
            String [] requestedProperties = StringHelper.tokenize( thePropertyList, theListSeparator, "" );
            
            for( PropertyType current : allPropertyTypes ) {
                if( ArrayHelper.isIn( current.getName().value(), requestedProperties, true )) {
                    propertyTypesToShow.add( current );
                }
            }
        } else {
            for( PropertyType current : allPropertyTypes ) {
                propertyTypesToShow.add( current );
            }
        }
        
        theIterator = propertyTypesToShow.iterator();

        int ret = iterateOnce();
        return ret;
    }

    /**
     * Our implementation of doAfterBody().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    @Override
    protected int realDoAfterBody()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        if( super.bodyContent != null ) {

            theFormatter.printPrevious( pageContext, theFormatter.isTrue( getFilter()), bodyContent.getString() );
            bodyContent.clearBody();
        }

        int ret = iterateOnce();
        return ret;
    }

    /**
     * Factors out common code for doStartTag and doAfterBody.
     * 
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     */
    protected int iterateOnce()
            throws
                JspException
    {
        while( theIterator.hasNext() ) {

            PropertyType  currentType  = theIterator.next();
            PropertyValue currentValue = null;

            try {
                currentValue = theMeshObject.getPropertyValue( currentType );

                if( currentValue == null && theSkipNullProperty ) {
                    continue;
                }

                if( thePropertyTypeLoopVar != null ) {
                    pageContext.getRequest().setAttribute( thePropertyTypeLoopVar, currentType );
                }
                if( thePropertyValueLoopVar != null ) {
                    pageContext.getRequest().setAttribute( thePropertyValueLoopVar, currentValue );
                }

                return EVAL_BODY_AGAIN;
                
            } catch( NotPermittedException ex ) {
                // skipping this row seems like a reasonable approach
            } catch( IllegalPropertyTypeException ex ) {
                throw new JspException( ex );
            }
        }
        return SKIP_BODY;
    }

    /**
     * Our implementation of doEndTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    @Override
    protected int realDoEndTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        if( thePropertyTypeLoopVar != null ) {
            pageContext.getRequest().removeAttribute( thePropertyTypeLoopVar );
        }
        if( thePropertyValueLoopVar != null ) {
            pageContext.getRequest().removeAttribute( thePropertyValueLoopVar );
        }

        return EVAL_PAGE;
    }

    /**
     * Determine whether this iteration tag has a next element to be returned
     * in the iteration.
     * 
     * @return true if there is a next element
     */
    public boolean hasNext()
    {
        if( theIterator.hasNext() ) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Name of the bean that contains the MeshObject to render.
     */
    protected String theMeshObjectName;

    /**
     * Name of the bean that contains the EntityType to restricted iteration to, if any.
     */
    protected String theMeshTypeName;

    /**
     * String containing the names of all the Properties that shall be shown. If null, show all.
     */
    protected String thePropertyList;

    /**
     * String containing the separator between the names of all the Properties that shall be shown.
     */
    protected String theListSeparator;

    /**
     * If true, we will skip those Properties where the PropertyValue is null.
     */
    protected boolean theSkipNullProperty;

    /**
     * String containing the name of the loop variable that contains the PropertyType.
     */
    protected String thePropertyTypeLoopVar;

    /**
     * String containing the name of the loop variable that contains the PropertyValue.
     */
    protected String thePropertyValueLoopVar;

    /**
     * The MeshObject whose values we show.
     */
    protected MeshObject theMeshObject;

    /**
     * Iterator over the set of PropertyTypes that we show.
     */
    protected Iterator<PropertyType> theIterator;

    /**
     * The default separator String between individual property names in properties.
     */
    public static final String DEFAULT_LIST_SEPARATOR = ",";
}
