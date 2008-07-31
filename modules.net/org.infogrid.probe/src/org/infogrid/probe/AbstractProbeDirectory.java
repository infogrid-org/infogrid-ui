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

import java.util.Collection;

/**
 * Factors out functionality common to many ProbeDirectory implementations.
 */
public abstract class AbstractProbeDirectory
        implements
            ProbeDirectory
{
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
    protected AbstractProbeDirectory(
            Collection<XmlDomProbeDescriptor>  xmlDomProbes,
            Collection<StreamProbeDescriptor>  streamProbes,
            Collection<ApiProbeDescriptor>     apiProbes,
            Collection<ExactMatchDescriptor>   exactMatches,
            Collection<PatternMatchDescriptor> patternMatches,
            StreamProbeDescriptor              defaultStreamProbe )
    {
        theXmlDomProbes   = xmlDomProbes;
        theStreamProbes   = streamProbes;
        theApiProbes      = apiProbes;
        theExactMatches   = exactMatches;
        thePatternMatches = patternMatches;
        
        theStreamDefaultProbe = defaultStreamProbe;
    }

    /**
     * Add a Probe to this ProbeDirectory that reads from a stream.
     *
     * @param desc the descriptor describing the Probe.
     */
    public synchronized void addStreamProbe(
            StreamProbeDescriptor desc )
    {
        theStreamProbes.add( desc );
    }

    /**
     * Set the default Probe that reads from a stream. If no other Probe matches
     * a stream data source, this is the one that will be used.
     *
     * @param desc the descriptor describing the Probe.
     * @see #getDefaultStreamProbe
     */
    public void setDefaultStreamProbe(
            StreamProbeDescriptor desc )
    {
        theStreamDefaultProbe = desc;
    }

    /**
     * Obtain the default Probe that reads from a stream. If not other Probe matches
     * a stream data source, this is the one that will be used.
     *
     * @return the descriptor describing the Probe.
     * @see #setDefaultStreamProbe
     */
    public StreamProbeDescriptor getDefaultStreamProbe()
    {
        return theStreamDefaultProbe;
    }

    /**
     * Internal helper to check that there is no overlap in handling XML
     * document types.
     *
     * @param docTypes the potential new XML document types
     * @param tagTypes the potential new XML root tags
     * @throws IllegalArgumentException thrown if an XML document type is handled already
     */
    protected void ensureNoXmlOverlap(
            String [] docTypes,
            String [] tagTypes )
        throws
            IllegalArgumentException
    {
        // check the XML Dom probes
        for( XmlDomProbeDescriptor current : theXmlDomProbes ) {

            // check the document types
            for( int j=current.theDocumentTypes.length-1 ; j>=0 ; --j ) {
                for( int k=docTypes.length-1 ; k>=0 ; --k ) {
                    if( current.theDocumentTypes[j].equals( docTypes[k] )) {
                        throw new IllegalArgumentException( "XML document type " + docTypes[k] + " handled already" );
                    }
                }
            }
            // check the tag types
            for( int j=current.theTagTypes.length-1 ; j>=0 ; --j ) {
                for( int k=tagTypes.length-1 ; k>=0 ; --k ) {
                    if( current.theTagTypes[j].equals( tagTypes[k] )) {
                        throw new IllegalArgumentException( "XML tag type " + tagTypes[k] + " handled already" );
                    }
                }
            }
        }
    }

    /**
     * Add a Probe to this ProbeDirectory that expects an XML Document Model as input.
     *
     * @param desc the descriptor describing the Probe
     * @throws IllegalArgumentException thrown if an XML document type is handled already
     */
    public synchronized void addXmlDomProbe(
            XmlDomProbeDescriptor desc )
        throws
            IllegalArgumentException
    {
        ensureNoXmlOverlap( desc.theDocumentTypes, desc.theTagTypes );

        theXmlDomProbes.add( desc );
    }

    /**
     * Add a Probe to this ProbeDirectory that will access an API to determine its input.
     *
     * @param desc the descriptor describing the Probe.
     * @throws IllegalArgumentException thrown if a protocol is handled already
     */
    public synchronized void addApiProbe(
            ApiProbeDescriptor desc )
        throws
            IllegalArgumentException
    {
        for( ApiProbeDescriptor current : theApiProbes ) {
            for( String j : current.theProtocols ) {
                for( String k : desc.theProtocols ) {
                    if( j.equals( k )) {
                        throw new IllegalArgumentException( "Protocol " + k + " handled already" );
                    }
                }
            }
        }
        theApiProbes.add( desc );
    }
    
    /**
     * Add an exact match to this ProbeDirectory between a data source and a URL.
     *
     * @param desc the descriptor of the match
     * @throws IllegalArgumentException thrown if this MatchDescriptor matches the same URL as a previous MatchDescriptor
     */
    public synchronized void addExactUrlMatch(
            ExactMatchDescriptor desc )
    {
        for( ExactMatchDescriptor current : theExactMatches ) {
            if( current.theUrl.equals( desc.theUrl )) {
                throw new IllegalArgumentException( "URL matched already: " + desc.theUrl );
            }
        }
        theExactMatches.add( desc );
    }

    /**
     * Add a pattern match to this ProbeDirectory between a data source and a URL.
     *
     * @param desc the descriptor of the match
     * @throws IllegalArgumentException thrown if this MatchDescriptor matches the same URL as a previous MatchDescriptor
     */
    public synchronized void addPatternUrlMatch(
            PatternMatchDescriptor desc )
    {
        for( PatternMatchDescriptor current : thePatternMatches ) {
            if( current.theUrlPattern.equals( desc.theUrlPattern )) {
                throw new IllegalArgumentException( "URL pattern matched already: " + desc.theUrlPattern );
            }
        }
        thePatternMatches.add( desc );
    }

    /**
     * Find an XML DOM Probe by document type.
     *
     * @param documentType the found XML document type
     * @return the descriptor for the Probe that can parse this document type
     */
    public XmlDomProbeDescriptor getXmlDomProbeDescriptorByDocumentType(
            String documentType )
    {
        for( XmlDomProbeDescriptor current : theXmlDomProbes ) {
            if( current.canProcessDocumentType( documentType )) {
                return current;
            }
        }
        return null;
    }

    /**
     * Find an XML DOM Probe by tag type.
     *
     * @param tagType the found XML tag type
     * @return the descriptor for the Probe that can parse this tag type
     */
    public XmlDomProbeDescriptor getXmlDomProbeDescriptorByTagType(
            String tagType )
    {
        for( XmlDomProbeDescriptor current : theXmlDomProbes ) {
            if( current.canProcessTagType( tagType )) {
                return current;
            }
        }
        return null;
    }

    /**
     * Find an XML DOM Probe class name by document type.
     *
     * @param documentType the found XML document type
     * @return the name of the Probe class that can parse this document type
     */
    public String getXmlDomProbeClassByDocumentType(
            String documentType )
    {
        XmlDomProbeDescriptor almostRet = getXmlDomProbeDescriptorByDocumentType( documentType );
        if( almostRet != null ) {
            return almostRet.getProbeClassName();
        }
        return null;
    }

    /**
     * Find an XML DOM Probe class name by tag type.
     *
     * @param tagType the found XML tag type
     * @return the name of the Probe class that can parse this tag type
     */
    public String getXmlDomProbeClassByTagType(
            String tagType )
    {
        XmlDomProbeDescriptor almostRet = getXmlDomProbeDescriptorByTagType( tagType );
        if( almostRet != null ) {
            return almostRet.getProbeClassName();
        }
        return null;
    }
    
    /**
     * Find an API Probe class name by URL.
     *
     * @param url the URL for which we are looking
     * @return the name of the Probe class that can access this URL
     */
    public String getApiProbeClassForUrl(
            String url )
    {
        String ret = getApiProbeClassByMatchedUrl( url );
        if( ret == null ) {
            int c = url.indexOf( ':' );
            if( c > 0 ) {
                ret = getApiProbeClassByProtocol( url.substring( 0, c ));
            }
        }
        return ret;
    }

    /**
     * Find an API Probe by URL protocol.
     *
     * @param protocol the URL protocol (e.g. jdbc) for which we are looking
     * @return the descriptor of the Probe that can access URLs with this protocol
     */
    public ApiProbeDescriptor getApiProbeDescriptorByProtocol(
            String protocol )
    {
        for( ApiProbeDescriptor current : theApiProbes ) {
            if( current.canProcessProtocol( protocol )) {
                return current;
            }
        }
        return null;
    }

    /**
     * Find an API Probe class name by URL protocol.
     *
     * @param protocol the URL protocol (e.g. jdbc) for which we are looking
     * @return the name of the Probe class that can access URLs with this protocol
     */
    public String getApiProbeClassByProtocol(
            String protocol )
    {
        ApiProbeDescriptor almostRet = getApiProbeDescriptorByProtocol( protocol );
        if( almostRet != null ) {
            return almostRet.getProbeClassName();
        }
        return null;
    }

    /**
     * Find an API Probe match by matched URL.
     *
     * @param url the URL for which we are looking
     * @return the descriptor of the Probe that can access this URL
     */
    public MatchDescriptor getApiProbeDescriptorByMatchedUrl(
            String url )
    {
        for( ExactMatchDescriptor current : theExactMatches ) {
            if( current.matches( url )) {
                return current;
            }
        }
        for( PatternMatchDescriptor current : thePatternMatches ) {
            if( current.matches( url )) {
                return current;
            }
        }
        return null;
    }

    /**
     * Find an API Probe class name by matched URL.
     *
     * @param url the URL for which we are looking
     * @return the name of the Probe class that can access this URL
     */
    public String getApiProbeClassByMatchedUrl(
            String url )
    {
        MatchDescriptor almostRet = getApiProbeDescriptorByMatchedUrl( url );
        if( almostRet != null ) {
            return almostRet.getProbeClassName();
        }
        return null;
    }

    /**
     * Find an stream Probe by MIME type.
     *
     * @param mimeType the MIME type for which we are looking, in form "xxx/yyy"
     * @return the descriptor of the Probe that can parse this MIME type
     */
    public StreamProbeDescriptor getStreamProbeDescriptorByMimeType(
            String mimeType )
    {
        if( mimeType != null ) {

            for( StreamProbeDescriptor current : theStreamProbes ) {
                if( current.canProcessMime( mimeType )) {
                    return current;
                }
            }
        }
        return null;
    }

    /**
     * Find an stream Probe class name by MIME type.
     *
     * @param mimeType the MIME type for which we are looking, in form "xxx/yyy"
     * @return the name of the Probe class that can parse this MIME type
     */
    public String getStreamProbeClassByMimeType(
            String mimeType )
    {
        String ret = null;

        StreamProbeDescriptor almostRet = getStreamProbeDescriptorByMimeType( mimeType );
        if( almostRet != null ) {
            ret = almostRet.getProbeClassName();
        } else {
            almostRet = getDefaultStreamProbe();
            if( almostRet != null ) {
                ret = theStreamDefaultProbe.getProbeClassName();
            }
        }
        return ret;
    }

    /**
     * Determine whether a Probe class by this name is an ApiProbe.
     *
     * @param className name of the class
     * @return return true if this is an ApiProbe
     */
    public boolean isApiProbe(
            String className )
    {
        return isIn( className, theApiProbes );
    }

    /**
     * Determine whether a Probe class by this name is a StreamProbe.
     *
     * @param className name of the class
     * @return return true if this is a StreamProbe
     */
    public boolean isStreamProbe(
            String className )
    {
        return isIn( className, theStreamProbes );
    }

    /**
     * Determine whether a Probe class by this name is an XmlDomProbe.
     *
     * @param className name of the class
     * @return return true if this is an XmlDomProbe
     */
    public boolean isXmlDomProbe(
            String className )
    {
        return isIn( className, theXmlDomProbes );
    }

    /**
     * Internal helper method to look up a Probe by its class name in an ArrayList.
     *
     * @param className the key into the Collection
     * @param array the Collection
     * @return returns true if found
     */
    private boolean isIn(
            String                                className,
            Collection<? extends ProbeDescriptor> array )
    {
        for( ProbeDescriptor current : array ) {
            if( current.getProbeClassName().equals( className )) {
                return true;
            }
        }
        return false;
    }

    /**
     * The default Probe for streams, if any.
     */
    protected StreamProbeDescriptor theStreamDefaultProbe = null;

    /**
      * The Probes that we know of that are based on XML and need a DOM as input.
      */
    protected Collection<XmlDomProbeDescriptor> theXmlDomProbes;

    /**
      * The Probes that we know of that are not based on XML but read a stream.
      */
    protected Collection<StreamProbeDescriptor> theStreamProbes;

    /**
     * The Probes that we know of that are using APIs.
     */
    protected Collection<ApiProbeDescriptor> theApiProbes;

    /**
     * The explicitly defined exact matches between URLs and ApiProbe classes.
     */
    protected Collection<ExactMatchDescriptor> theExactMatches;

    /**
     * The explicitly defined pattern matches between URLs and ApiProbe classes.
     */
    protected Collection<PatternMatchDescriptor> thePatternMatches;
}
