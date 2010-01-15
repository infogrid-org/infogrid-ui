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

package org.infogrid.probe.test;

import java.io.IOException;
import org.infogrid.httpd.HttpResponseFactory;
import org.infogrid.meshbase.net.local.LocalNetMeshBase;

/**
 * Factors out more functionality for Yadis discovery tests.
 */
public abstract class AbstractYadisDiscoveryTest
    extends
        AbstractYadisTest
{
    /**
     * Run the test.
     *
     * @throws Exception all sorts of things may happen during a test
     */
    public void run()
        throws
            Exception
    {
        run( theMeshBase1, true );
        run( theMeshBase2, false );
    }

    /**
     * Run one test scenario.
     *
     * @param mb the NetMeshBase to use for this scenario
     * @param mode the mode
     * @throws Exception all sorts of things may happen during a test
     */
    public abstract void run(
            LocalNetMeshBase mb,
            boolean          mode )
        throws
            Exception;

    /**
     * Constructor.
     *
     * @param testClass the class to test
     * @param factory the factory for HttpResponses
     * @throws IOException thrown if the HttpServer could not be started
     */
    protected AbstractYadisDiscoveryTest(
            Class               testClass,
            HttpResponseFactory factory )
        throws
            IOException
    {
        super( testClass, factory );
    }
}
