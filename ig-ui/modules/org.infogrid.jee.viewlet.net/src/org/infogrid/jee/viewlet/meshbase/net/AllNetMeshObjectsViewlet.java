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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.viewlet.meshbase.net;

import java.text.ParseException;
import java.util.Iterator;
import org.infogrid.jee.viewlet.DefaultJeeViewedMeshObjects;
import org.infogrid.jee.viewlet.DefaultJeeViewletFactoryChoice;
import org.infogrid.jee.viewlet.JeeMeshObjectsToView;
import org.infogrid.jee.viewlet.JeeViewedMeshObjects;
import org.infogrid.jee.viewlet.meshbase.AllMeshObjectsViewlet;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.net.NetMeshObject;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.util.CursorIterator;
import org.infogrid.util.FilteringCursorIterator;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.StringHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.ViewedMeshObjects;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * A Viewlet that shows all NetMeshObjects in a NetMeshBase.
 */
public class AllNetMeshObjectsViewlet
        extends
            AllMeshObjectsViewlet
{
    private static final Log log = Log.getLogInstance( AllNetMeshObjectsViewlet.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     * @param c the application context
     * @return the created PropertySheetViewlet
     */
    public static AllNetMeshObjectsViewlet create(
            MeshBase mb,
            Context  c )
    {
        DefaultJeeViewedMeshObjects viewed = new DefaultJeeViewedMeshObjects( mb );
        AllNetMeshObjectsViewlet    ret    = new AllNetMeshObjectsViewlet( viewed, c );

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
        return new DefaultJeeViewletFactoryChoice( toView, AllNetMeshObjectsViewlet.class, matchQuality ) {
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
    protected AllNetMeshObjectsViewlet(
            JeeViewedMeshObjects viewed,
            Context              c )
    {
        super( viewed, c );
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

        NetMeshBase meshBase = (NetMeshBase) getSubject().getMeshBase(); // derive from the subject, so we can do any MeshBase

        // first reset
        theMaxLength                 = DEFAULT_MAXLENGTH;
        theHomeNetMeshBaseIdentifier = null;

        ViewedMeshObjects viewed = getViewedMeshObjects();
        String filterId = (String) viewed.getViewletParameter( HOME_NETMESHBASE_VIEWLET_PARAM );

        if( filterId != null ) {
            filterId = filterId.trim();
        }
        if( filterId != null && filterId.length() > 0 ) {
            try {
                theHomeNetMeshBaseIdentifier = meshBase.getMeshBaseIdentifierFactory().guessFromExternalForm( filterId );

            } catch( ParseException ex ) {
                throw new CannotViewException.InvalidParameter( this, HOME_NETMESHBASE_VIEWLET_PARAM, filterId, toView );
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
        CursorIterator<MeshObject> ret = super.determineCursorIterator();

        if( theHomeNetMeshBaseIdentifier != null ) {
            ret = FilteringCursorIterator.create(
                    ret,
                    new FilteringCursorIterator.Filter<MeshObject>() {
                            public boolean accept(
                                    MeshObject obj )
                            {
                                NetMeshObject realObj = (NetMeshObject) obj;
                                Proxy         home    = realObj.getProxyTowardsHomeReplica();

                                if( home == null ) {
                                    return realObj.getMeshBase().getIdentifier().equals( theHomeNetMeshBaseIdentifier );
                                } else {
                                    return home.getPartnerMeshBaseIdentifier().equals( theHomeNetMeshBaseIdentifier );
                                }
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
    @Override
    public boolean isFiltered()
    {
        if( super.isFiltered() ) {
            return true;
        }
        if( theHomeNetMeshBaseIdentifier != null ) {
            return true;
        }
        return false;
    }

    /**
     * Obtain the NetMeshBaseIdentifier by which we filter, if any.
     *
     * @return the Proxy, or null if none
     */
    public NetMeshBaseIdentifier getFilterHomeNetMeshBaseIdentifier()
    {
        return theHomeNetMeshBaseIdentifier;
    }

    /**
     * Obtain an HTML fragment that can be inserted into a HTML select statement that gives the selectable Proxies.
     *
     * @return the HTML fragment
     */
    public String getShowHomeNetMeshBaseHtml()
    {
        NetMeshBase           mb   = ((NetMeshBase)getSubject().getMeshBase());
        NetMeshBaseIdentifier mbId = mb.getIdentifier();

        StringBuilder ret = new StringBuilder();
        ret.append( "<option" );
        if( theHomeNetMeshBaseIdentifier == null ) {
            ret.append( " selected=\"selected\"" );
        }
        ret.append( " value=\"\">All NetMeshObjects</option>\n" );
        ret.append( "<option" );
        if( mbId.equals( theHomeNetMeshBaseIdentifier )) {
            ret.append( " selected=\"selected\"" );
        }
        ret.append( " value=\"" ).append( mbId.toExternalForm() ).append( "\">NetMeshObjects with local home</option>\n" );

        Iterator<Proxy> proxyIter = mb.proxies();
        while( proxyIter.hasNext() ) {
            Proxy                 p         = proxyIter.next();
            NetMeshBaseIdentifier partnerId = p.getPartnerMeshBaseIdentifier();

            ret.append( " <option" );
            if(    partnerId.equals( theHomeNetMeshBaseIdentifier )
                || mbId.equals( theHomeNetMeshBaseIdentifier ))
            {
                ret.append( " selected=\"selected\"" );
            }
            ret.append( " value=\"" ).append( partnerId.toExternalForm()).append( "\">" );
            ret.append( StringHelper.potentiallyShorten( partnerId.toExternalForm(), theMaxLength ));
            ret.append( "</option>\n" );
        }
        return ret.toString();
    }

    /**
     * The Identifier of the NetMeshBase by which we filter, if any.
     */
    protected NetMeshBaseIdentifier theHomeNetMeshBaseIdentifier;

    /**
     * The maximum length of identifiers to show.
     */
    protected int theMaxLength = 25;

    /**
     * Default maximum length of identifiers to show.
     */
    public static final int DEFAULT_MAXLENGTH = ResourceHelper.getInstance( AllNetMeshObjectsViewlet.class ).getResourceIntegerOrDefault( "DefaultMaxLength", 40 );

    /**
     * Viewlet parameter containing the partner Identifier of the Proxy.
     */
    public static final String HOME_NETMESHBASE_VIEWLET_PARAM = "home-netmeshbase";
}
