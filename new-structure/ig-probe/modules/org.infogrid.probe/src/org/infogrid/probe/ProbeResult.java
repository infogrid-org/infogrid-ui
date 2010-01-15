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

package org.infogrid.probe;

/**
 * Helper class to return two values from a Probe run.
 */
public class ProbeResult
{
    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param updated if true, the Shadow was updated during the probe run
     * @param usedWritableProbe if true, the probe run used a WritableProbe
     * @param usedProbeClass the Probe class that was used, or null
     */
    public ProbeResult(
            boolean                updated,
            boolean                usedWritableProbe,
            Class<? extends Probe> usedProbeClass )
    {
        theUpdated           = updated;
        theUsedWritableProbe = usedWritableProbe;
        theUsedProbeClass    = usedProbeClass;
    }
    
    /**
     * Determine whether the Shadow was updated during the probe run.
     * 
     * @return true or false
     */
    public boolean getUpdated()
    {
        return theUpdated;
    }
    
    /**
     * Determine whether the Probe run used a WritableProbe.
     * 
     * @return true or false
     */
    public boolean getUsedWritableProbe()
    {
        return theUsedWritableProbe;
    }

    /**
     * Determine which Probe class was used. This may return null in case
     * no Probe was found.
     *
     * @return the Probe class
     */
    public Class<? extends Probe> getUsedProbeClass()
    {
        return theUsedProbeClass;
    }

    /**
     * If true, the Shadow was updated during the Probe run.
     */
    protected boolean theUpdated;
    
    /**
     * If true, the Probe run used a WritableProbe.
     */
    protected boolean theUsedWritableProbe;

    /**
     * The Probe class that was used in this run.
     */
    protected Class<? extends Probe> theUsedProbeClass;
}
