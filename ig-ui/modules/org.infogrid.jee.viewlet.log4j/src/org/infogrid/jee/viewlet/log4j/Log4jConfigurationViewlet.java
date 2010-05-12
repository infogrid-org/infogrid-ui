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

package org.infogrid.jee.viewlet.log4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import javax.servlet.ServletException;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;
import org.infogrid.util.context.Context;
import org.infogrid.jee.templates.StructuredResponse;
import org.infogrid.jee.viewlet.DefaultJeeViewedMeshObjects;
import org.infogrid.jee.viewlet.DefaultJeeViewletFactoryChoice;
import org.infogrid.jee.viewlet.JeeMeshObjectsToView;
import org.infogrid.jee.viewlet.JeeViewedMeshObjects;
import org.infogrid.jee.viewlet.SimpleJeeViewlet;
import org.infogrid.meshbase.MeshBase;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;
import org.infogrid.util.logging.log4j.Log4jLog;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.Viewlet;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * This Viewlet enables the user to reconfigure Log4j at run-time.
 */
public class Log4jConfigurationViewlet
        extends
            SimpleJeeViewlet
{
    /**
     * Factory method.
     *
     * @param mb the MeshBase from which the MeshObjects are taken
     * @param c the application context
     * @return the created Viewlet
     */
    public static Log4jConfigurationViewlet create(
            MeshBase mb,
            Context  c )
    {
        DefaultJeeViewedMeshObjects viewed = new DefaultJeeViewedMeshObjects( mb );
        Log4jConfigurationViewlet   ret    = new Log4jConfigurationViewlet( viewed, c );

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
        return new DefaultJeeViewletFactoryChoice( toView, Log4jConfigurationViewlet.class, matchQuality ) {
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
    protected Log4jConfigurationViewlet(
            JeeViewedMeshObjects viewed,
            Context              c )
    {
        super( viewed, c );
    }

    /**
     * Obtain an iterator over all the loggers.
     *
     * @return iterator
     */
    public Iterator<Log4jLog> loggerIterator()
    {
        // We like the output to be alphabetically sorted.

        LoggerRepository ret     = LogManager.getLoggerRepository();
        Enumeration      loggers = ret.getCurrentLoggers();

        ArrayList<Log4jLog> all = new ArrayList<Log4jLog>();

        while( loggers.hasMoreElements() ) {
            Logger   found   = (Logger) loggers.nextElement();
            Log4jLog current = (Log4jLog) Log.getLogInstance( found.getName() );
            
            all.add( current );
        }
        
        Collections.sort( all, new Comparator<Log4jLog>() {
            public int compare(
                    Log4jLog one,
                    Log4jLog two )
            {
                int ret = one.getDelegate().getName().compareTo( two.getDelegate().getName() );
                return ret;
            }
        });
        
        return all.iterator();

    }

    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and the FormTokenService determined that the incoming POST was safe.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the POST execution of the servlet, e.g. the evaluation of POST commands.</p>
     *
     * @param request the incoming request
     * @param response the response to be assembled
     * @return if true, the result of the viewlet processing has been deposited into the response object
     *         already and regular processing will be skipped. If false, regular processing continues.
     * @throws ServletException thrown if an error occurred
     */
    @Override
    public boolean performBeforeSafePost(
            SaneRequest        request,
            StructuredResponse response )
        throws
            ServletException
    {
        return performPost( request, response );
    }

    /**
     * <p>Invoked prior to the execution of the Servlet if the POST method has been requested
     *    and no FormTokenService has been used.
     *    It is the hook by which the JeeViewlet can perform whatever operations needed prior to
     *    the POST execution of the servlet.</p>
     *
     * @param request the incoming request
     * @param response the response to be assembled
     * @return if true, the result of the viewlet processing has been deposited into the response object
     *         already and regular processing will be skipped. If false, regular processing continues.
     * @throws ServletException thrown if an error occurred
     */
    @Override
    public boolean performBeforeMaybeSafeOrUnsafePost(
            SaneRequest        request,
            StructuredResponse response )
        throws
            ServletException
    {
        return performPost( request, response );
    }

    /**
     * Perform the post operation.
     *
     * @param request the incoming request
     * @param response the response to be assembled
     * @return if true, the result of the viewlet processing has been deposited into the response object
     *         already and regular processing will be skipped. If false, regular processing continues.
     * @throws ServletException thrown if an error occurred
     */
    protected boolean performPost(
            SaneRequest        request,
            StructuredResponse response )
        throws
            ServletException
    {
        for( String key : request.getPostedArguments().keySet() ) {

            if( !key.startsWith( PREFIX )) {
                continue; // only our arguments
            }
            String name  = key.substring( PREFIX.length() );
            String value = request.getPostedArgument( key );

            Log4jLog l = (Log4jLog) Log.getLogInstance( name );

            try {
                LEVEL level = LEVEL.valueOf( value );

                level.setTo( l );

            } catch( IllegalArgumentException ex ) {
                // unknown value
                l.getDelegate().setLevel( null );
            }
        }
        response.setHttpResponseCode( 303 );
        response.setLocation( request.getAbsoluteFullUri() );
        return true;
    }

    /**
     * Allowed levels for the loggers.
     */
    public static enum LEVEL {
        OFF {
            public void setTo(
                    Log4jLog l )
            {
                l.getDelegate().setLevel( Level.OFF );
            }
        },
        TRACE {
            public void setTo(
                    Log4jLog l )
            {
                l.getDelegate().setLevel( Level.TRACE );
            }
        },
        DEBUG {
            public void setTo(
                    Log4jLog l )
            {
                l.getDelegate().setLevel( Level.DEBUG );
            }
        },
        INFO {
            public void setTo(
                    Log4jLog l )
            {
                l.getDelegate().setLevel( Level.INFO );
            }
        },
        WARN {
            public void setTo(
                    Log4jLog l )
            {
                l.getDelegate().setLevel( Level.WARN );
            }
        },
        ERROR {
            public void setTo(
                    Log4jLog l )
            {
                l.getDelegate().setLevel( Level.ERROR );
            }
        },
        FATAL {
            public void setTo(
                    Log4jLog l )
            {
                l.getDelegate().setLevel( Level.FATAL );
            }
        };

        /**
         * Set this logger to this LEVEL.
         *
         * @param l the logger
         */
        public abstract void setTo(
                Log4jLog l );
    };

    /**
     * The prefix of all POST arguments that this Viewlet cares about.
     */
    public static final String PREFIX = "field-";
}
