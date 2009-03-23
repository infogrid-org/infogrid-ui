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

package org.infogrid.meshbase.net.a;

import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.xpriso.XprisoMessage;
import org.infogrid.util.ReturnSynchronizer;
import org.infogrid.util.logging.Log;

/**
 * Subtypes ReturnSynchronizer for the purposes of synchronizing accessLocally calls.
 */
public class AccessLocallySynchronizer
    extends
        ReturnSynchronizer<Long,XprisoMessage>
{
    private static final Log log = Log.getLogInstance( AccessLocallySynchronizer.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param mb the NetMeshBase on whose behalf of this AccessLocallySynchronizer works
     * @return the created AccessLocallySynchronizer
     */
    public static AccessLocallySynchronizer create(
            NetMeshBase mb )
    {
        AccessLocallySynchronizer ret = new AccessLocallySynchronizer( mb );
        if( log.isTraceEnabled() ) {
            log.traceConstructor( ret );
        }
        return ret;
    }

    /**
     * Constructor.
     *
     * @param mb the NetMeshBase on whose behalf of this AccessLocallySynchronizer works
     */
    public AccessLocallySynchronizer(
            NetMeshBase mb )
    {
        super( mb.getIdentifier().toExternalForm() );
    }
}
