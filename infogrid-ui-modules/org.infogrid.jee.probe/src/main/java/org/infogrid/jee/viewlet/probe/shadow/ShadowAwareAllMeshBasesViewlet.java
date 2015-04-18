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

package org.infogrid.jee.viewlet.probe.shadow;

import java.text.ParseException;
import javax.servlet.ServletException;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.viewlet.DefaultJeeViewedMeshObjects;
import org.infogrid.jee.viewlet.DefaultJeeViewletFactoryChoice;
import org.infogrid.jee.viewlet.JeeMeshObjectsToView;
import org.infogrid.jee.viewlet.JeeViewedMeshObjects;
import org.infogrid.jee.viewlet.meshbase.AllMeshBasesViewlet;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.meshbase.MeshBaseNameServer;
import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.meshbase.sweeper.Sweeper;
import org.infogrid.probe.manager.ActiveProbeManager;
import org.infogrid.probe.manager.ProbeManager;
import org.infogrid.probe.shadow.ShadowMeshBase;
import org.infogrid.util.Invocable;
import org.infogrid.util.context.Context;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.InvalidViewletActionException;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * Adds the ability to start/stop ShadowMeshBases to AllMeshBasesViewlet.
 */
public class ShadowAwareAllMeshBasesViewlet
    extends
        AllMeshBasesViewlet
{
    private static final Log log = Log.getLogInstance( ShadowAwareAllMeshBasesViewlet.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param mb the MeshBase from which the viewed MeshObjects are taken
     * @param c the application context
     * @return the created PropertySheetViewlet
     */
    public static AllMeshBasesViewlet create(
            MeshBase mb,
            Context  c )
    {
        DefaultJeeViewedMeshObjects viewed = new DefaultJeeViewedMeshObjects( mb );
        AllMeshBasesViewlet         ret    = new ShadowAwareAllMeshBasesViewlet( viewed, c );

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
        return new DefaultJeeViewletFactoryChoice( toView, ShadowAwareAllMeshBasesViewlet.class, matchQuality ) {
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
     * @param viewed the AbstractViewedMeshObjects implementation to use
     * @param c the application context
     */
    protected ShadowAwareAllMeshBasesViewlet(
            JeeViewedMeshObjects viewed,
            Context              c )
    {
        super( viewed, c );
    }

    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and the SafeUnsafePostFilter determined that the incoming POST was safe.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the POST execution of the servlet, e.g. the evaluation of POST commands.</p>
     * <p>Subclasses will often override this.</p>
     *
     * @param request the incoming request
     * @param response the response to be assembled
     * @return if true, the result of the viewlet processing has been deposited into the response object
     *         already and regular processing will be skipped. If false, regular processing continues.
     * @throws ServletException thrown if an error occurred
     * @see #performBeforeGet
     * @see #performBeforeUnsafePost
     * @see #performAfter
     */
    @Override
    public boolean performBeforeSafePost(
            SaneRequest        request,
            StructuredResponse response )
        throws
            ServletException
    {
        boolean ret = defaultPerformPost( request, response );

        String meshBaseName   = request.getPostedArgument( FORM_MESHBASE_NAME );
        String runNowAction   = request.getPostedArgument( FORM_RUNNOWACTION_NAME );
        String stopAction     = request.getPostedArgument( FORM_STOPACTION_NAME );
        String sweepNowAction = request.getPostedArgument( FORM_SWEEPNOWACTION_NAME );
        String killAction     = request.getPostedArgument( FORM_KILLACTION_NAME );

        boolean doRunNow   = false;
        boolean doStop     = false;
        boolean doSweepNow = false;
        boolean doKill     = false;

        if( runNowAction != null && runNowAction.length() > 0 ) {
            doRunNow = true;

        } else if( stopAction != null && stopAction.length() > 0 ) {
            doStop = true;

        } else if( sweepNowAction != null && sweepNowAction.length() > 0 ) {
            doSweepNow = true;

        } else if( killAction != null && killAction.length() > 0 ) {
            doKill = true;

        } else {
            return ret; // silently fail
        }

        Context c = getContext();

        NetMeshBase           mainMeshBase       = c.findContextObjectOrThrow( NetMeshBase.class );
        NetMeshBaseIdentifier meshBaseIdentifier;

        try {
            meshBaseIdentifier = mainMeshBase.getMeshBaseIdentifierFactory().guessFromExternalForm( meshBaseName );

        } catch( ParseException ex ) {
            log.warn( ex );
            return ret; // silently fail
        }

        @SuppressWarnings( "unchecked" )
        MeshBaseNameServer<MeshBaseIdentifier,MeshBase> ns  = c.findContextObjectOrThrow( MeshBaseNameServer.class );

        MeshBase found = ns.get( meshBaseIdentifier );
        if( found == null ) {
            log.warn( "MeshBase not found: " + meshBaseIdentifier.toExternalForm() );
            return ret; // silently fail
        }

        if( found instanceof ShadowMeshBase ) {
            ShadowMeshBase realFound = (ShadowMeshBase) found;

            ProbeManager probeMgr = realFound.getProbeManager();

            if( doKill ) {
                // this needs to work even if XPRISO communication is screwed
                Proxy towards = mainMeshBase.getProxyFor( meshBaseIdentifier );
                towards.die( true );

                probeMgr.remove( meshBaseIdentifier, new Invocable<ShadowMeshBase,Void>() {
                        public Void invoke(
                                ShadowMeshBase shadow )
                        {
                            shadow.die( true );

                            return null;
                        }
                });

            } else if( probeMgr instanceof ActiveProbeManager ) {
                ActiveProbeManager realProbeMgr = (ActiveProbeManager) probeMgr;

                try {
                    if( doRunNow ) {
                        realProbeMgr.doUpdateNow( realFound );

                    } else if( doStop ) {
                        realProbeMgr.disableFutureUpdates( realFound );

                    } else {
                        response.reportProblem( new InvalidViewletActionException( this ));
                    }
                } catch( Throwable t ) {
                    log.warn( t );
                    response.reportProblem( t );
                }

            } else {
                try {
                    if( doRunNow ) {
                        realFound.doUpdateNow();
                    } else {
                        response.reportProblem( new InvalidViewletActionException( this ));
                    }

                } catch( Throwable t ) {
                    log.warn( t );
                    response.reportProblem( t );
                }
            }
        } else {
            NetMeshBase realFound = (NetMeshBase) found;

            if( doSweepNow ) {
                Sweeper sweep = realFound.getSweeper();
                if( sweep != null ) {
                    sweep.sweepAllNow();
                } else {
                    response.reportProblem( new NoSweeperException( this ));
                }
            } else {
                response.reportProblem( new InvalidViewletActionException( this ));
            }
        }
        return ret;
    }

    /**
     * Name of the HTML Form element that contains the name of the affected MeshBase.
     */
    public static final String FORM_MESHBASE_NAME = "MeshBase";

    /**
     * Name of the HTML input element that indicates to run the ShadowMeshBase now.
     */
    public static final String FORM_RUNNOWACTION_NAME = "RunNowAction";

    /**
     * Name of the HTML input element that indicates to stop the ShadowMeshBase now.
     */
    public static final String FORM_STOPACTION_NAME = "StopAction";

    /**
     * Name of the HTML input element that indicates to sweep a non-ShadowMeshBase now.
     */
    public static final String FORM_SWEEPNOWACTION_NAME = "SweepNowAction";

    /**
     * Name of the HTML input element that indicates to kill off a ShadowMeshBase now.
     */
    public static final String FORM_KILLACTION_NAME = "KillAction";
}
