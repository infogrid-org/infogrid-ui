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

package org.infogrid.probe.TEST;

import java.util.concurrent.ScheduledExecutorService;
import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.net.local.m.LocalNetMMeshBase;
import org.infogrid.meshbase.net.xpriso.logging.LogXprisoMessageLogger;
import org.infogrid.probe.m.MProbeDirectory;

/**
 * Provides functionality useful for ForwardReferenceTests.
 */
public abstract class AbstractForwardReferenceTest
        extends
            AbstractProbeTest
{
    /**
     * Constructor.
     *
     * @param testClass the Class to be tested
     * @throws Exception all sorts of things may go wrong in a test
     */
    protected AbstractForwardReferenceTest(
            Class testClass )
        throws
            Exception
    {
        super( testClass );

        here = theMeshBaseIdentifierFactory.fromExternalForm( "http://here.local/" ); // this is not going to work for communications
        base = LocalNetMMeshBase.create( here, theModelBase, null, theProbeDirectory, exec, rootContext );

        if( getLog().isDebugEnabled() ) {
            theXprisoMessageLogger = LogXprisoMessageLogger.create( getLog() );
            base.setXprisoMessageLogger( theXprisoMessageLogger );
        }
    }

    /**
     * Clean up after the test.
     */
    @Override
    public void cleanup()
    {
        base.die();
    }

    /**
     * Our ThreadPool.
     */
    protected ScheduledExecutorService exec = createThreadPool( 1 );

    /**
     * The ProbeDirectory.
     */
    protected MProbeDirectory theProbeDirectory = MProbeDirectory.create();

    /**
     * Identifier of the main NetMeshBase.
     */
    protected NetMeshBaseIdentifier here;

    /**
     * The main NetMeshBase.
     */
    protected LocalNetMMeshBase base;

    /**
     * The XprisoMessageLogger to use.
     */
    protected LogXprisoMessageLogger theXprisoMessageLogger;
}