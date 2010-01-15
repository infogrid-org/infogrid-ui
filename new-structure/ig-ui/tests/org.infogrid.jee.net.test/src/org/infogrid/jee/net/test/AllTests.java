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

package org.infogrid.jee.net.test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.infogrid.module.ModuleRequirement;
import org.infogrid.testharness.AbstractTestGroup;
import org.infogrid.testharness.AbstractTestGroup.TestSpec;
import org.infogrid.testharness.tomcat.TomcatProxy;
import org.infogrid.testharness.tomcat.TomcatProxyConfiguredByProperties;
import org.infogrid.util.logging.Log;

/**
 * Runs all tests in this package.
 */
public class AllTests
        extends
            AbstractTestGroup
{
    /**
     * Main program.
     *
     * @param args command-line arguments
     * @throws Exception all kinds of things may happen in tests
     */
    public static void main(
            String [] args )
        throws
            Exception
    {
        if( args.length != 1 ) {
            throw new IllegalArgumentException( "Must specify Tomcat properties file name on command-line" );
        }
        String tomcatPropertiesFile = args[0];
        ModuleRequirement toTest = ModuleRequirement.create1( "org.infogrid.jee.net.testapp" );
        TomcatProxy       tomcat = TomcatProxyConfiguredByProperties.create( new File( tomcatPropertiesFile )) ;

        URL appUrl;
        try {
            appUrl = tomcat.deployModule( toTest );
        } catch( IOException ex ) {
            Log.getLogInstance( AllTests.class ).error( "Cannot deploy module " + toTest, ex );
            System.exit( 1 );
            return; // make compiler happy
        }

        TestSpec [] tests = {
                new TestSpec( AllMeshObjectsTest1.class, appUrl.toExternalForm() ),
                new TestSpec( AllMeshObjectsTest2.class, appUrl.toExternalForm() ),
        };

        runTests( tests );
    }
}

