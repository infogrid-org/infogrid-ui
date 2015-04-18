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

package org.infogrid.jee.taglib.mesh;

import java.util.Arrays;
import java.util.Comparator;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridTag;
import org.infogrid.mesh.MeshObject;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.MeshType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.primitives.SubjectArea;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.ResourceHelper;

/**
 * <p>Tag that displays one or more types that a MeshObject may still become blessed with.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class MeshObjectBlessableByTag
    extends
        AbstractRestInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public MeshObjectBlessableByTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObject           = null;
        theMeshObjectName       = null;
        theMeshObjectVarName    = null;
        theOfferEmptySelection  = false;
        theSupertypeName        = null;
        theSupertype            = null;
        theSubjectAreaName      = null;
        theSubjectArea          = null;

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
     * Obtain value of the offerEmptySelection property.
     *
     * @return value of the offerEmptySelection property
     * @see #setOfferEmptySelection
     */
    public boolean getOfferEmptySelection()
    {
        return theOfferEmptySelection;
    }

    /**
     * Set value of the offerEmptySelection property.
     *
     * @param newValue new value of the offerEmptySelection property
     * @see #getOfferEmptySelection
     */
    public void setOfferEmptySelection(
            boolean newValue )
    {
        theOfferEmptySelection = newValue;
    }

    /**
     * Obtain value of the supertypeName property.
     *
     * @return value of the supertypeName property
     * @see #setSupertypeName
     */
    public String getSupertypeName()
    {
        return theSupertypeName;
    }

    /**
     * Set value of the supertypeName property.
     *
     * @param newValue new value of the supertypeName property
     * @see #getSupertypeName
     */
    public void setSupertypeName(
            String newValue )
    {
        theSupertypeName = newValue;
    }

    /**
     * Obtain value of the supertype property.
     *
     * @return value of the supertype property
     * @see #setSupertype
     */
    public String getSupertype()
    {
        return theSupertype;
    }

    /**
     * Set value of the supertype property.
     *
     * @param newValue new value of the supertype property
     * @see #getSupertype
     */
    public void setSupertype(
            String newValue )
    {
        theSupertype = newValue;
    }

    /**
     * Obtain value of the subjectAreaName property.
     *
     * @return value of the subjectAreaName property
     * @see #setSubjectAreaName
     */
    public String getSubjectAreaName()
    {
        return theSubjectAreaName;
    }

    /**
     * Set value of the subjectAreaName property.
     *
     * @param newValue new value of the subjectAreaName property
     * @see #getSubjectAreaName
     */
    public void setSubjectAreaName(
            String newValue )
    {
        theSubjectAreaName = newValue;
    }

    /**
     * Obtain value of the subjectArea property.
     *
     * @return value of the subjectArea property
     * @see #setSubjectArea
     */
    public String getSubjectArea()
    {
        return theSubjectArea;
    }

    /**
     * Set value of the subjectArea property.
     *
     * @param newValue new value of the subjectArea property
     * @see #getSubjectArea
     */
    public void setSubjectArea(
            String newValue )
    {
        theSubjectArea = newValue;
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
        MeshObject    obj     = null;
        EntityType [] myTypes = null;
        if( theMeshObjectVarName != null ) {
            if( theMeshObject != null ) {
                throw new JspException( "Must not specify both meshObject and meshObjectVarName" );
            }
            if( theMeshObjectName != null ) {
                throw new JspException( "Must not specify both meshObjectName and meshObjectVarName" );
            }
        } else {
            obj     = lookupMeshObjectOrThrow( "meshObject", theMeshObject, "meshObjectName", theMeshObjectName );
            myTypes = obj.getTypes();
        }

        EntityType  typeLimit;
        SubjectArea saLimit;

        if( theSupertypeName != null ) {
            if( theSupertype != null ) {
                throw new JspException( "Must specify either supertypeName or supertype, but not both" );
            } else {
                typeLimit = (EntityType) lookup( theSupertypeName );
            }
        } else if( theSupertype != null ) {
            typeLimit = (EntityType) findMeshTypeByIdentifierOrThrow( theSupertype );
        } else {
            typeLimit = null;
        }

        if( theSubjectAreaName != null ) {
            if( theSubjectArea != null ) {
                throw new JspException( "Must specify either subjectAreaName or subjectArea, but not both" );
            } else {
                saLimit = (SubjectArea) lookup( theSubjectAreaName );
            }
        } else if( theSupertype != null ) {
            saLimit = (SubjectArea) findMeshTypeByIdentifierOrThrow( theSupertype );
        } else {
            saLimit = null;
        }

        if( typeLimit != null && saLimit != null ) {
            throw new JspException( "Limit either by SubjectArea or supertype, but not both" );
        }

        Integer varCounter = (Integer) lookup( VARIABLE_COUNTER_NAME );
        if( varCounter == null ) {
            varCounter = 0;
        }
        
        String editVar;
        if( theMeshObjectVarName != null ) {
            editVar = theMeshObjectVarName;
        } else {
            editVar = String.format( VARIABLE_PATTERN, varCounter );
            pageContext.getRequest().setAttribute( VARIABLE_COUNTER_NAME, varCounter.intValue()+1 );
        }

        if( obj != null ) {
            print( "<input type=\"hidden\" name=\"" );
            print( editVar );
            print( "\" value=\"" );
            print( obj.getIdentifier().toExternalForm() );
            println( "\"/>" );
        }

        print( "<select name=\"" );
        print( editVar );
        println( ".bless\" class=\"org-infogrid-jee-taglib-mesh-MeshObjectBlessableByTag\">" );

        if( theOfferEmptySelection ) {
            println( " <option value=\"\">None</option>" );
        }

        if( saLimit != null ) {
            // don't need to print optiongroups
            EntityType [] saTypes = saLimit.getEntityTypes();

            for( int i=0 ; i<saTypes.length ; ++i ) {
                if( saTypes[i].getIsAbstract().value()) {
                    continue;
                }

                print( " <option value=\"" );
                print( saTypes[i].toExternalForm() );
                if( ArrayHelper.isIn( saTypes[i], myTypes, true )) {
                    print( "\" disabled=\"disabled" );
                }
                print( "\">" );
                print( saTypes[i].getUserVisibleName().value() );
                println( "</option>" );
            }

        } else {
            // need to list all EntityTypes from the ModelBase, and select the ones that we currently filter by.
            // We'll try alphabetically

            ModelBase mb = ModelBaseSingleton.getSingleton();
            SubjectArea [] sas = ArrayHelper.copyIntoNewArray( mb.subjectAreaIterator(), SubjectArea.class );
            Arrays.sort( sas, theByUserVisibleNameComparator );

            for( SubjectArea sa : sas ) {
                print( " <optgroup label=\"" );
                print( sa.getUserVisibleName().value() );
                println( "\">" );

                EntityType [] saTypes = sa.getEntityTypes();

                for( int i=0 ; i<saTypes.length ; ++i ) {
                    if( saTypes[i].getIsAbstract().value()) {
                        continue;
                    }

                    print( " <option value=\"" );
                    print( saTypes[i].toExternalForm() );
                    if( myTypes != null ) {
                        if( ArrayHelper.isIn( saTypes[i], myTypes, true )) {
                            print( "\" disabled=\"disabled" );
                        }
                    }
                    print( "\">" );
                    print( saTypes[i].getUserVisibleName().value() );
                    println( "</option>" );
                }
                println( "</optgroup>" );
            }
        }
        println( "</select>" );
        
        return EVAL_PAGE;
    }

    /**
     * Name of the bean that holds the MeshObject.
     */
    protected String theMeshObjectName;

    /**
     * The MeshObject.
     */
    protected Object theMeshObject;

    /**
     * String containing the name of the shell variable referring to the MeshObject.
     */
    protected String theMeshObjectVarName;

    /**
     * If true, also offer a selection that will not bless anything.
     */
    protected boolean theOfferEmptySelection;

    /**
     * Name of the bean that holds the supertype EntityType (mutually exclusive with theSupertype, theSubjectAreaName, theSubjectArea).
     */
    protected String theSupertypeName;

    /**
     * The EntityType's identifier (mutually exclusive with theSupertypeName, theSubjectAreaName, theSubjectArea).
     */
    protected String theSupertype;

    /**
     * Name of the bean that holds the boundary SubjectArea (mutually exclusive with theSubjectArea, theSupertypeName, theSupertype).
     */
    protected String theSubjectAreaName;

    /**
     * The SubjectArea's identifier (mutually exclusive with theSubjectAreaName, theSupertypeName, theSupertype).
     */
    protected String theSubjectArea;

    /**
     * Sorter by UserVisibleName.
     */
    protected static final Comparator<MeshType> theByUserVisibleNameComparator = new Comparator<MeshType>() {
        public int compare(
                MeshType one,
                MeshType two )
        {
            StringValue name1 = one.getUserVisibleName();
            StringValue name2 = two.getUserVisibleName();

            return PropertyValue.compare( name1, name2 );
        }
    };

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( MeshObjectBlessableByTag.class );

    /**
     * Name of the request variable that we use internally to count up variables.
     */
    public static final String VARIABLE_COUNTER_NAME = MeshObjectBlessableByTag.class.getName().replace( '.', '_' ) + "-varCounter";

    /**
     * Format String to generate variable names.
     */
    public static final String VARIABLE_PATTERN = theResourceHelper.getResourceStringOrDefault(
            "VariablePattern",
            "shell.meshObjectBlessableByTagMeshObject%d" );
}
