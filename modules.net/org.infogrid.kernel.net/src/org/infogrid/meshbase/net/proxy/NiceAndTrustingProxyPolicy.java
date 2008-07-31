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

package org.infogrid.meshbase.net.proxy;

import org.infogrid.meshbase.net.CoherenceSpecification;

/**
 * A ProxyPolicy that generally does what it is being asked for and tries to
 * be nice to others. This may not be too wise in a production environment that
 * isn't otherwise tightly controlled or secured.
 */
public class NiceAndTrustingProxyPolicy
        extends
            AbstractProxyPolicy
{
    /**
     * Factory method.
     * 
     * @param coherence the CoherenceSpecification used by this ProxyPolicy
     * @return the created DefaultProxyPolicy
     */
    public static NiceAndTrustingProxyPolicy create(
            CoherenceSpecification coherence )
    {
        return new NiceAndTrustingProxyPolicy( coherence, true );
    }
    
    /**
     * Factory method.
     * 
     * @param coherence the CoherenceSpecification used by this ProxyPolicy
     * @param pointsReplicasToItself if true, new Replicas will be created by a branch from the local Replica
     * @return the created DefaultProxyPolicy
     */
    public static NiceAndTrustingProxyPolicy create(
            CoherenceSpecification coherence,
            boolean                pointsReplicasToItself )
    {
        return new NiceAndTrustingProxyPolicy( coherence, pointsReplicasToItself );
    }
    
    /**
     * Constructor.
     * 
     * @param coherence the CoherenceSpecification used by this ProxyPolicy
     * @param pointsReplicasToItself if true, new Replicas will be created by a branch from the local Replica
     */
    protected NiceAndTrustingProxyPolicy(
            CoherenceSpecification coherence,
            boolean                pointsReplicasToItself )
    {
        super( coherence, pointsReplicasToItself );
    }
}