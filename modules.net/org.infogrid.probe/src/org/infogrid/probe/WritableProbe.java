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

package org.infogrid.probe;

import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.meshbase.transaction.ChangeSet;

import org.infogrid.module.ModuleException;

import java.io.IOException;

/**
 * <p>This interface is supported by all Probes that can both
 *    read and write their data. This interface is supported in addition
 *    to whatever other subtype of Probe they support.</p>
 * <p>The sequence of invocations is:</p>
 * <pre>
 *   Probe constructor
 *   for( one or more times ) {
 *       read( ... )
 *       wait for some period of time, depending on the CoherenceSpecification in effect
 *       write( ... )
 *   }
 * </pre>
 * <p>This sequence may be repeated itself many times, even for the same data source:
 * the Probe instance may be garbage collected in between. Garbage collection may also
 * occur between the read and the write call.</p>
 * <p>Note: Probes <b>must not</b> store persistent data in any place other than the
 * MeshObjects that they instantiate.</p>
 */
public interface WritableProbe
        extends
            Probe
{
    /**
     * <p>Write the passed-in changes to the data source. The ChangeSet may be null or empty,
     * indicating that no changes need to be written, but the call is made anyway in order to
     * provide consistent invocation behavior.</p>
     * <p>Note: the MeshBase provided to this call is <i>different</i> from the MeshBase that
     * is being passed into the readXXX call on the Probe instance invoked right after. For
     * each Probe run, a fresh, empty StagingMeshBase is created (which by definition is empty
     * except for the Home Object), while the StagingMeshBase provided to this call contains
     * the content of the last Probe run, plus any modifications since.</p>
     *
     * @param dataSourceIdentifier identifies the data source that is being accessed
     * @param updateSet the set of changes to write, or null
     * @param previousMeshBaseWithUpdates the StagingMeshBase from the previous run, in which the Changes have occurred.
     *        Given the invocation sequence, notice that it is impossible that this null.
     * @throws ProbeException a Probe error occurred per the possible subclasses defined in ProbeException
     * @throws IOException an input/output error occurred during execution of the Probe
     * @throws ModuleException thrown if a Module required by the Probe could not be written
     */
    public void write(
            NetMeshBaseIdentifier dataSourceIdentifier,
            ChangeSet             updateSet,
            StagingMeshBase       previousMeshBaseWithUpdates )
        throws
            ProbeException,
            IOException,
            ModuleException;
}
