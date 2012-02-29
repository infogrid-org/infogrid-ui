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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.rest.RestfulJeeFormatter;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.taglib.rest.AbstractRestInfoGridTag;
import org.infogrid.mesh.MeshObject;
import org.infogrid.model.primitives.MeshType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.RelationshipType;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.primitives.SubjectArea;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.Pair;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.text.StringifierException;

/**
 * <p>Tag that displays one or more RoleTypes that a Relationship still may be blessed with.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class RelationshipBlessableByTag
    extends
        AbstractRestInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public RelationshipBlessableByTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theStartObject        = null;
        theStartObjectName    = null;
        theStartObjectVarName = null;
        theNeighbor           = null;
        theNeighborName       = null;
        theNeighborVarName    = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the startObject property.
     *
     * @return value of the startObject property
     * @see #setStartObject
     */
    public final Object getStartObject()
    {
        return theStartObject;
    }

    /**
     * Set value of the startObject property.
     *
     * @param newValue new value of the startObject property
     * @see #getStartObject
     */
    public final void setStartObject(
            Object newValue )
    {
        theStartObject = newValue;
    }

    /**
     * Obtain value of the startObjectName property.
     *
     * @return value of the startObjectName property
     * @see #setStartObjectName
     */
    public String getStartObjectName()
    {
        return theStartObjectName;
    }

    /**
     * Set value of the startObjectName property.
     *
     * @param newValue new value of the startObjectName property
     * @see #getStartObjectName
     */
    public void setStartObjectName(
            String newValue )
    {
        theStartObjectName = newValue;
    }

    /**
     * Obtain value of the startObjectVarName property.
     *
     * @return value of the startObjectVarName property
     * @see #setStartObjectVarName
     */
    public final String getStartObjectVarName()
    {
        return theStartObjectVarName;
    }

    /**
     * Set value of the startObjectVarName property.
     *
     * @param newValue new value of the startObjectVarName property
     * @see #getStartObjectVarName
     */
    public final void setStartObjectVarName(
            String newValue )
    {
        theStartObjectVarName = newValue;
    }

    /**
     * Obtain value of the neighbor property.
     *
     * @return value of the neighbor property
     * @see #setNeighbor
     */
    public final Object getNeighbor()
    {
        return theNeighbor;
    }

    /**
     * Set value of the neighbor property.
     *
     * @param newValue new value of the neighbor property
     * @see #getNeighbor
     */
    public final void setNeighbor(
            Object newValue )
    {
        theNeighbor = newValue;
    }

    /**
     * Obtain value of the neighborName property.
     *
     * @return value of the neighborName property
     * @see #setNeighborName
     */
    public String getNeighborName()
    {
        return theNeighborName;
    }

    /**
     * Set value of the neighborName property.
     *
     * @param newValue new value of the neighborName property
     * @see #getNeighborName
     */
    public void setNeighborName(
            String newValue )
    {
        theNeighborName = newValue;
    }

    /**
     * Obtain value of the neighborVarName property.
     *
     * @return value of the neighborVarName property
     * @see #setNeighborVarName
     */
    public final String getNeighborVarName()
    {
        return theNeighborVarName;
    }

    /**
     * Set value of the neighborVarName property.
     *
     * @param newValue new value of the neighborVarName property
     * @see #getNeighborVarName
     */
    public final void setNeighborVarName(
            String newValue )
    {
        theNeighborVarName = newValue;
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
        MeshObject start    = null;
        MeshObject neighbor = null;

        if( theStartObjectVarName != null ) {
            if( theStartObject != null ) {
                throw new JspException( "Must not specify both startObject and startObjectVarName" );
            }
            if( theStartObjectName != null ) {
                throw new JspException( "Must not specify both startObjectName and startObjectVarName" );
            }
        } else {
            start = lookupMeshObjectOrThrow( "startObject", theStartObject, "startObjectName", theStartObjectName );
        }

        if( theNeighborVarName != null ) {
            if( theNeighbor != null ) {
                throw new JspException( "Must not specify both neighbor and neighborVarName" );
            }
            if( theNeighborName != null ) {
                throw new JspException( "Must not specify both neighborName and neighborVarName" );
            }
        } else {
            neighbor = lookupMeshObjectOrThrow( "neighbor", theNeighbor, "neighborName", theNeighborName );
        }

        ModelBase      mb  = ModelBaseSingleton.getSingleton();
        SubjectArea [] sas = ArrayHelper.copyIntoNewArray( mb.subjectAreaIterator(), SubjectArea.class );
        Arrays.sort( sas, theByUserVisibleNameComparator );

        ArrayList<Pair<SubjectArea,ArrayList<RoleType>>> candidates = new ArrayList<Pair<SubjectArea,ArrayList<RoleType>>>();
        if( start != null && neighbor != null && !start.isRelated( neighbor )) {
            throw new JspException( "Start and neighbor MeshObjects must already be related" );
        }

        for( SubjectArea sa : sas ) {
            ArrayList<RoleType> candidatesInSa = new ArrayList<RoleType>();

            RelationshipType [] relTypes = sa.getRelationshipTypes();
            Arrays.sort( relTypes, theByUserVisibleNameComparator );

            for( RelationshipType relType : relTypes ) {
                if( relType.getIsAbstract().value() ) {
                    continue;
                }

                for( RoleType roleType : new RoleType[] { relType.getSource(), relType.getDestination() } ) {
                    RoleType invRoleType = roleType.getInverseRoleType();
                    
                    if( start != null ) {
                        if( roleType.getEntityType() != null ) {
                            if( !start.isBlessedBy( roleType.getEntityType() )) {
                                continue;
                            }
                        }
                    }
                    if( neighbor != null ) {
                        if( invRoleType.getEntityType() != null ) {
                            if( !neighbor.isBlessedBy( invRoleType.getEntityType() )) {
                                continue;
                            }
                        }
                    }
                    candidatesInSa.add( roleType );
                }
            }
            if( !candidatesInSa.isEmpty() ) {
                candidates.add( new Pair<SubjectArea,ArrayList<RoleType>>( sa, candidatesInSa ));
            }
        }

        Integer varCounter = (Integer) lookup( VARIABLE_COUNTER_NAME );
        if( varCounter == null ) {
            varCounter = 0;
        }

        String startObjectEditVar;
        String neighborEditVar;

        if( theStartObjectVarName != null ) {
            if( !theStartObjectVarName.startsWith( SHELL_VAR_PREFIX )) {
                throw new JspException( "HttpShell variables must start with \"" + SHELL_VAR_PREFIX + "\". Variable name given: " + theStartObjectVarName );
            }
            startObjectEditVar = theStartObjectVarName;
        } else {
            startObjectEditVar = String.format( VARIABLE_PATTERN, varCounter );
            ++varCounter;
        }

        if( theNeighborVarName != null ) {
            if( !theNeighborVarName.startsWith( SHELL_VAR_PREFIX )) {
                throw new JspException( "HttpShell variables must start with \"" + SHELL_VAR_PREFIX + "\". Variable name given: " + theNeighborVarName );
            }
            neighborEditVar = theNeighborVarName;
        } else {
            neighborEditVar = String.format( VARIABLE_PATTERN, varCounter );
            ++varCounter;
        }
        pageContext.getRequest().setAttribute( VARIABLE_COUNTER_NAME, varCounter );

        try {
            if( start != null ) {
                String identifier = ((RestfulJeeFormatter)theFormatter).formatMeshObjectIdentifier( pageContext, start, LINK_STRING_REPRESENTATION, -1 );

                print( "<input type=\"hidden\" name=\"" );
                print( startObjectEditVar );
                print( "\" value=\"" );
                print( identifier );
                println( "\"/>" );
            }
            if( neighbor != null ) {
                String identifier = ((RestfulJeeFormatter)theFormatter).formatMeshObjectIdentifier( pageContext, neighbor, LINK_STRING_REPRESENTATION, -1 );

                print( "<input type=\"hidden\" name=\"" );
                print( neighborEditVar );
                print( "\" value=\"" );
                print( identifier );
                println( "\"/>" );
            }
        } catch( StringifierException ex ) {
            throw new JspException( ex );
        }

        String shortStartObjectEditVar    = startObjectEditVar.substring( SHELL_VAR_PREFIX.length() );
        String shortNeighborObjectEditVar = neighborEditVar.substring(    SHELL_VAR_PREFIX.length() );

        if( !candidates.isEmpty() ) {
            print( "<select name=\"shell." );
            print( shortStartObjectEditVar );
            print (".to." );
            print( shortNeighborObjectEditVar );
            println( ".blessRole\" class=\"org-infogrid-jee-taglib-mesh-MeshObjectBlessableByTag\">" );

            for( Pair<SubjectArea,ArrayList<RoleType>> pair : candidates ) {
                SubjectArea sa = pair.getName();

                print( " <optgroup label=\"" );
                print( sa.getUserVisibleName().value() );
                println( "\">" );

                for( RoleType roleType : pair.getValue() ) {
                    print( " <option value=\"" );
                    print( roleType.toExternalForm() );

                    if( start != null && neighbor != null && start.isRelated( roleType, neighbor ) ) {
                        print( "\" disabled=\"disabled" );
                    }
                    print( "\">" );
                    print( roleType.getUserVisibleName().value() );
                    println( "</option>" );
                }
                println( "</optgroup>" );
            }
        }
        println( "</select>" );

        return EVAL_PAGE;
    }

    /**
     * Name of the bean that holds the start MeshObject.
     */
    protected String theStartObjectName;

    /**
     * The start MeshObject.
     */
    protected Object theStartObject;

    /**
     * String containing the name of the shell variable referring to the start MeshObject.
     */
    protected String theStartObjectVarName;

    /**
     * Name of the bean that holds the neighbor MeshObject.
     */
    protected String theNeighborName;

    /**
     * The neighbor MeshObject.
     */
    protected Object theNeighbor;

    /**
     * String containing the name of the shell variable referring to the neighbor MeshObject.
     */
    protected String theNeighborVarName;

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
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( RelationshipBlessableByTag.class );

    /**
     * Name of the request variable that we use internally to count up variables.
     */
    public static final String VARIABLE_COUNTER_NAME = RelationshipBlessableByTag.class.getName().replace( '.', '_' ) + "-varCounter";

    /**
     * Format String to generate variable names.
     */
    public static final String VARIABLE_PATTERN = theResourceHelper.getResourceStringOrDefault(
            "VariablePattern",
            "shell.relationshipBlessableByTagMeshObject%d" );

    /**
     * Name of the StringRepresentation for links.
     */
    public static final String LINK_STRING_REPRESENTATION = theResourceHelper.getResourceStringOrDefault(
            "LinkStringRepresentation",
            "HttpShell" );

    /**
     * Variable name prefix for HttpShell variables.
     */
    public static final String SHELL_VAR_PREFIX = "shell.";
}
