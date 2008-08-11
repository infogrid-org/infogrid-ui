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

package org.infogrid.probe.feeds.TEST;

import java.util.Iterator;
import org.infogrid.mesh.MeshObject;
import org.infogrid.meshbase.IterableMeshBase;
import org.infogrid.meshbase.net.IterableNetMeshBase;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.modelbase.MeshTypeNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.logging.Log;
import org.infogrid.util.context.Context;
import org.infogrid.util.context.SimpleContext;

/**
 *
 */
public abstract class AbstractFeedTest
        extends
            AbstractTest
{
    private static final Log log = Log.getLogInstance( AbstractFeedTest.class );

    /**
     * Constructor.
     */
    protected AbstractFeedTest(
            Class testClass )
        throws
            MeshTypeNotFoundException
    {
        super( localFile( AbstractFeedTest.class, "/ResourceHelper" ),
               localFile( AbstractFeedTest.class, "/Log.properties" ));
    }

    /**
     * Helper method to count MeshObjects.
     *
     * @param base the MeshBase containing th MeshObjects
     * @param mylog if given, log the found objects there
     * @return the number of MeshObjects in the MeshBase
     */
    protected static int countMeshObjects(
            IterableNetMeshBase base,
            Log                 mylog )
    {
        int ret = countFromIterator( base.iterator(), mylog );
        return ret;
    }

    /**
     * Count the number of Objects found by iterating over an Iterator.
     *
     * @param iter the Iterator
     * @param mylog if given, log the found objects there
     * @return the number of Objects found
     */
    public static <T> int countFromIterator(
            Iterator<T> iter,
            Log         mylog )
    {
        int ret = 0;
        StringBuilder buf = new StringBuilder(); // do this instead of logging directly, that way we don't changing the threading behavior
        while( iter.hasNext() ) {
            T current = iter.next();

            ++ret;
            buf.append( "found " + current );
        }

        if( mylog != null ) {
            mylog.info( "found " + ret + " elements:\n" + buf );
        }
        return ret;
    }

    /**
     * Dump the content of a MeshBase to log.debug().
     *
     * @param mb the MeshBase whose content we want to dump
     * @param prefix a string to prepend
     * @param mylog the Log to dump to
     */
    protected final void dumpMeshBase(
            IterableMeshBase mb,
            String           prefix,
            Log              mylog )
        throws
            Exception
    {
        if( mylog.isDebugEnabled() ) {
            StringBuffer buf = new StringBuffer( prefix );
            for( MeshObject current : mb ) {
                buf.append( "\n  " );
                buf.append( current.getIdentifier() );
                buf.append( " (created: " );
                buf.append( current.getTimeCreated() );
                buf.append( " updated: " );
                buf.append( current.getTimeUpdated() );
                buf.append( " read: " );
                buf.append( current.getTimeRead() );
                buf.append( ")" );

                if( true ) {
                    buf.append( "\n    Types:" );
                    for( EntityType et : current.getTypes() ) {
                        buf.append( "\n        " );
                        buf.append( et.getName().value() );
                    }
                }
                if( true ) {
                    buf.append( "\n    Properties:" );
                    for( PropertyType pt : current.getAllPropertyTypes() ) {
                        buf.append( "\n        " );
                        buf.append( pt.getName().value() );
                        buf.append( ": " );
                        buf.append( current.getPropertyValue( pt ));
                    }
                }
            }
            mylog.debug( buf.toString() );
        }
    }

    /**
     * The root context for these tests.
     */
    protected static final Context rootContext = SimpleContext.createRoot( "root-context" );

    /**
     * The ModelBase.
     */
    protected static ModelBase theModelBase = ModelBaseSingleton.getSingleton();

    /**
     * The ProbeDirectory.
     */
    protected MProbeDirectory theProbeDirectory = MProbeDirectory.create();
}
