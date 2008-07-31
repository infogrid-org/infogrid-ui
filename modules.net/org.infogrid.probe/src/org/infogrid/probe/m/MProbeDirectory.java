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

package org.infogrid.probe.m;

import org.infogrid.probe.AbstractProbeDirectory;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Simple in-memory implementation of ProbeDirectory.
 */
public class MProbeDirectory
        extends
            AbstractProbeDirectory
{
    /**
     * Factory method to create an empty MProbeDirectory.
     *
     * @return the created MProbeDirectory
     */
    public static MProbeDirectory create()
    {
        return new MProbeDirectory(
                new ArrayList<XmlDomProbeDescriptor>(),
                new ArrayList<StreamProbeDescriptor>(),
                new ArrayList<ApiProbeDescriptor>(),
                new ArrayList<ExactMatchDescriptor>(),
                new ArrayList<PatternMatchDescriptor>(),
                null );
    }

    /**
     * Factory method to create a MProbeDirectory with the specified content.
     *
     * @param xmlDomProbes the set of XmlDOMProbeDescriptors to initialize with
     * @param streamProbes the set of StreamProbeDescriptors to initialize with
     * @param apiProbes the set of ApiProbeDescriptor to initialize with
     * @param exactMatches the set of ExactMatchDescriptors to initialize with
     * @param patternMatches the set of PatternMatchDescriptors to initialize with
     * @param defaultStreamProbe identifies the default stream Probe to initialize with
     * @return the created MProbeDirectory
     */
    public static MProbeDirectory create(
            Collection<XmlDomProbeDescriptor>  xmlDomProbes,
            Collection<StreamProbeDescriptor>  streamProbes,
            Collection<ApiProbeDescriptor>     apiProbes,
            Collection<ExactMatchDescriptor>   exactMatches,
            Collection<PatternMatchDescriptor> patternMatches,
            StreamProbeDescriptor              defaultStreamProbe )
    {
        return new MProbeDirectory(
                new ArrayList<XmlDomProbeDescriptor>(),
                new ArrayList<StreamProbeDescriptor>(),
                new ArrayList<ApiProbeDescriptor>(),
                new ArrayList<ExactMatchDescriptor>(),
                new ArrayList<PatternMatchDescriptor>(),
                null );
    }

    /**
     * Constructor.
     * 
     * @param xmlDomProbes the set of XmlDOMProbeDescriptors to initialize with
     * @param streamProbes the set of StreamProbeDescriptors to initialize with
     * @param apiProbes the set of ApiProbeDescriptor to initialize with
     * @param exactMatches the set of ExactMatchDescriptors to initialize with
     * @param patternMatches the set of PatternMatchDescriptors to initialize with
     * @param defaultStreamProbe identifies the default stream Probe to initialize with
     */
    protected MProbeDirectory(
            Collection<XmlDomProbeDescriptor>  xmlDomProbes,
            Collection<StreamProbeDescriptor>  streamProbes,
            Collection<ApiProbeDescriptor>     apiProbes,
            Collection<ExactMatchDescriptor>   exactMatches,
            Collection<PatternMatchDescriptor> patternMatches,
            StreamProbeDescriptor              defaultStreamProbe )
    {
        super( xmlDomProbes, streamProbes, apiProbes, exactMatches, patternMatches, defaultStreamProbe );
    }
}
