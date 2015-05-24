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

package org.infogrid.jee.viewlet.meshbase;

import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.jee.viewlet.AbstractPagingCursorIterableViewlet;
import org.infogrid.jee.viewlet.DefaultJeeViewedMeshObjects;
import org.infogrid.jee.viewlet.DefaultJeeViewletFactoryChoice;
import org.infogrid.jee.viewlet.JeeMeshObjectsToView;
import org.infogrid.jee.viewlet.JeeViewedMeshObjects;
import org.infogrid.mesh.MeshObject;
import org.infogrid.meshbase.IterableMeshBase;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.MeshType;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.model.primitives.SubjectArea;
import org.infogrid.modelbase.MeshTypeWithIdentifierNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.FilteringCursorIterator;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.ViewedMeshObjects;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * A Viewlet that shows all MeshObjects in a MeshBase.
 */ 
public class AllMeshObjectsViewlet
        extends
            AbstractPagingCursorIterableViewlet
{
    private static final Log log = Log.getLogInstance( AllMeshObjectsViewlet.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     * @param c the application context
     * @return the created PropertySheetViewlet
     */
    public static AllMeshObjectsViewlet create(
            MeshBase mb,
            Context  c )
    {
        DefaultJeeViewedMeshObjects viewed = new DefaultJeeViewedMeshObjects( mb );
        AllMeshObjectsViewlet       ret    = new AllMeshObjectsViewlet( viewed, c );

        viewed.setViewlet( ret );

        return ret;
    }

    /**
     * Factory method for a ViewletFactoryChoice that instantiates this Viewlet.
     *
     * @param toView the MeshObjectsToView for which this is a choice
     * @param matchQuality the match quality
     * @return the ViewletFactoryChoice
     */
    public static ViewletFactoryChoice choice(
            JeeMeshObjectsToView toView,
            double               matchQuality )
    {
        return new DefaultJeeViewletFactoryChoice( toView, AllMeshObjectsViewlet.class, matchQuality ) {
                public Viewlet instantiateViewlet()
                    throws
                        CannotViewException
                {
                    return create( getMeshObjectsToView().getMeshBase(), getMeshObjectsToView().getContext() );
                }
        };
    }

    /**
     * Constructor. This is protected: use factory method or subclass.
     *
     * @param viewed the JeeViewedMeshObjects to use
     * @param c the application context
     */
    protected AllMeshObjectsViewlet(
            JeeViewedMeshObjects viewed,
            Context              c )
    {
        super( viewed, DEFAULT_PAGE_SIZE, c );
    }

    /**
      * The Viewlet is being instructed to view certain objects, which are packaged as MeshObjectsToView.
      *
      * @param toView the MeshObjects to view
      * @throws CannotViewException thrown if this Viewlet cannot view these MeshObjects
      */
    @Override
    public void view(
            MeshObjectsToView toView )
        throws
            CannotViewException
    {
        super.view( toView );

        IterableMeshBase meshBase = (IterableMeshBase) getSubject().getMeshBase(); // derive from the subject, so we can do any MeshBase
        ModelBase        mb       = meshBase.getModelBase();

        // first reset
        theIdRegex     = null;
        theEntityTypes = null;

        ViewedMeshObjects viewed = getViewedMeshObjects();
        String idRegexString = (String) viewed.getViewletParameter( ID_REGEX_VIEWLET_PARAM );
        String typesString   = (String) viewed.getViewletParameter( ENTITY_TYPES_VIEWLET_PARAM );

        if( idRegexString != null ) {
            idRegexString = idRegexString.trim();
        }
        if( idRegexString != null && idRegexString.length() > 0 ) {
            theIdRegex = Pattern.compile( idRegexString );
        }
        if( typesString != null ) {
            typesString = typesString.trim();
        }
        if( typesString != null && typesString.length() > 0 ) {
            try {
                String [] typesStrings = typesString.split( "," );
                theEntityTypes = new EntityType[ typesStrings.length ];
                int count = 0;
                for( int i=0 ; i<typesStrings.length ; ++i ) {
                    String current = typesStrings[i].trim();
                    if( current.length() > 0 ) {
                        theEntityTypes[count++] = mb.findEntityTypeByIdentifier( mb.getMeshTypeIdentifierFactory().fromExternalForm( typesStrings[i] ));
                    }
                }
                if( count == 0 ) {
                    theEntityTypes = null;
                } else if( count < theEntityTypes.length ) {
                    theEntityTypes = ArrayHelper.copyIntoNewArray( theEntityTypes, 0, count, EntityType.class );
                }
            } catch( MeshTypeWithIdentifierNotFoundException ex ) {
                log.warn( ex ); // perhaps there's a better way of doing this
            }
        }
    }

    /**
     * Determine the correct CursorIterator.
     *
     * @return the CursorIterator
     */
    @Override
    protected CursorIterator<MeshObject> determineCursorIterator()
    {
        IterableMeshBase meshBase = (IterableMeshBase) getSubject().getMeshBase(); // derive from the subject, so we can do any MeshBase
        
        CursorIterator<MeshObject> ret = meshBase.iterator();
        if( theIdRegex != null || theEntityTypes != null ) {
            ret = FilteringCursorIterator.create(
                    ret,
                    new FilteringCursorIterator.Filter<MeshObject>() {
                            public boolean accept(
                                    MeshObject obj )
                            {
                                if( obj == null ) {
                                    return false; 
                                }
                                if( theIdRegex != null ) {
                                    Matcher m = theIdRegex.matcher( obj.getIdentifier().toExternalForm() );
                                    if( !m.find()) {
                                        return false;
                                    }
                                }
                                if( theEntityTypes == null ) {
                                    return true;
                                }
                                for( int i=0 ; i<theEntityTypes.length ; ++i ) {
                                    if( obj.isBlessedBy( theEntityTypes[i] )) {
                                        return true;
                                    }
                                }
                                return false;
                            }
                    },
                    ret.getArrayComponentType() );
        }
        return ret;
    }

    /**
     * Return true if filtering is in effect.
     *
     * @return true if filtering is in effect
     */
    public boolean isFiltered()
    {
        if( theIdRegex != null ) {
            return true;
        }
        return theEntityTypes != null;
    }

    /**
     * Obtain the current regex in effect for the Identifier.
     *
     * @return the regex, or null if none
     */
    public String getIdRegex()
    {
        if( theIdRegex == null ) {
            return null;
        } else {
            return theIdRegex.toString();
        }
    }

    /**
     * Obtain a comma-separated list of all the EntityType identifiers by which we filter.
     * Null means we do not filter.
     *
     * @return the EntityType identifiers
     */
    public String getShowTypes()
    {
        if( theEntityTypes == null ) {
            return null;
        }

        StringBuilder buf = new StringBuilder();
        String        sep = "";

        for( int i=0 ; i<theEntityTypes.length ; ++i ) {
            buf.append( sep );
            buf.append( theEntityTypes[i].getIdentifier().toExternalForm() );
            sep = ",";
        }
        return buf.toString();
    }

    /**
     * Obtain an HTML fragment that can be inserted into a HTML select statement that gives the selectable EntityTypes.
     *
     * @return the HTML fragment
     */
    public String getShowTypesHtml()
    {
        StringBuilder ret = new StringBuilder();
        ret.append( "<option" );
        if( theEntityTypes == null ) {
            ret.append( " selected=\"selected\"" );
        }
        ret.append( " value=\"\">All EntityTypes</option>\n" );

        // need to list all EntityTypes from the ModelBase, and select the ones that we currently filter by.
        // We'll try alphabetically

        ModelBase mb = getSubject().getMeshBase().getModelBase();
        SubjectArea [] sas = ArrayHelper.copyIntoNewArray( mb.subjectAreaIterator(), SubjectArea.class );
        Arrays.sort( sas, theByUserVisibleNameComparator );

        for( SubjectArea sa : sas ) {
            ret.append( "<optgroup label=\"" ).append( sa.getUserVisibleName().value() ).append( "\">\n" );

            EntityType [] entityTypes = sa.getEntityTypes();
            Arrays.sort( entityTypes, theByUserVisibleNameComparator );

            for( EntityType et : entityTypes ) {
                ret.append( " <option" );
                if( theEntityTypes != null && ArrayHelper.isIn( et, theEntityTypes, false )) {
                    ret.append( " selected=\"selected\"" );
                }
                ret.append( " value=\"" ).append( et.getIdentifier().toExternalForm()).append( "\">" );
                ret.append( et.getUserVisibleName().value() );
                ret.append( "</option>\n" );
            }

            ret.append( "</optgroup>\n" );
        }
        return ret.toString();
    }

    /**
     * Default page size.
     */
    public static final int DEFAULT_PAGE_SIZE = ResourceHelper.getInstance( AllMeshObjectsViewlet.class ).getResourceIntegerOrDefault(
            "DefaultPageSize",
            20 );

    /**
     * The regular expression for identifiers currently in effect.
     */
    protected Pattern theIdRegex;

    /**
     * The EntityTypes that we filter currently in effect. If this is null, it means "return all".
     */
    protected EntityType [] theEntityTypes;

    /**
     * Viewlet parameter containing the regex for the Identifiers.
     */
    public static final String ID_REGEX_VIEWLET_PARAM = "identifier-regex";

    /**
     * Viewlet parameter containing the list of the EntityTypes for the filter.
     */
    public static final String ENTITY_TYPES_VIEWLET_PARAM = "show-types";

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
}
