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

package org.infogrid.probe.vcard.TEST;

import org.infogrid.modelbase.ModelBase;
import org.infogrid.modelbase.ModelBaseSingleton;
import org.infogrid.probe.ProbeDirectory.StreamProbeDescriptor;
import org.infogrid.probe.m.MProbeDirectory;
import org.infogrid.probe.vcard.VCardProbe;
import org.infogrid.testharness.AbstractTest;
import org.infogrid.util.context.Context;
import org.infogrid.util.context.SimpleContext;
import org.infogrid.util.logging.Log;

/**
 * Factors out common functionality of VCardProbeTests.
 */
public abstract class AbstractVCardProbeTest
        extends
            AbstractTest
{
    private static final Log log = Log.getLogInstance( AbstractVCardProbeTest.class );

    /**
     * Constructor.
     */
    protected AbstractVCardProbeTest(
            Class testClass )
    {
        super( localFile( testClass, "/ResourceHelper" ),
               localFile( testClass, "/Log.properties" ));
        
        theProbeDirectory.addStreamProbe( new StreamProbeDescriptor( "text/x-vcard", VCardProbe.class ));
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


