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

/**
 * A ProbeDirectory that can be modified.
 */
public interface WritableProbeDirectory
    extends
        ProbeDirectory
{
    /**
     * Add a Probe to this ProbeDirectory that reads from a stream.
     *
     * @param desc the descriptor describing the Probe.
     */
    public void addStreamProbe(
            StreamProbeDescriptor desc );

    /**
     * Set the default Probe that reads from a stream. If no other Probe matches
     * a stream data source, this is the one that will be used.
     *
     * @param desc the descriptor describing the Probe.
     * @see #getDefaultStreamProbe
     */
    public void setDefaultStreamProbe(
            StreamProbeDescriptor desc );

    /**
     * Add a Probe to this ProbeDirectory that expects an XML Document Model as input.
     *
     * @param desc the descriptor describing the Probe
     * @throws IllegalArgumentException thrown if an XML document type is handled already
     */
    public void addXmlDomProbe(
            XmlDomProbeDescriptor desc )
        throws
            IllegalArgumentException;

    /**
     * Add a Probe to this ProbeDirectory that will access an API to determine its input.
     *
     * @param desc the descriptor describing the Probe.
     * @throws IllegalArgumentException thrown if a protocol is handled already
     */
    public void addApiProbe(
            ApiProbeDescriptor desc )
        throws
            IllegalArgumentException;
    
    /**
     * Add an exact match to this ProbeDirectory between a data source and a URL.
     *
     * @param desc the descriptor of the match
     * @throws IllegalArgumentException thrown if this MatchDescriptor matches the same URL as a previous MatchDescriptor
     */
    public void addExactUrlMatch(
            ExactMatchDescriptor desc );

    /**
     * Add a pattern match to this ProbeDirectory between a data source and a URL.
     *
     * @param desc the descriptor of the match
     * @throws IllegalArgumentException thrown if this MatchDescriptor matches the same URL as a previous MatchDescriptor
     */
    public void addPatternUrlMatch(
            PatternMatchDescriptor desc );
}
