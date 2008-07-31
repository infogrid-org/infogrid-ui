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

import org.infogrid.probe.xml.XmlProbe;
import org.infogrid.probe.xml.XmlDOMProbe;

import org.infogrid.util.ArrayHelper;
import org.infogrid.util.StringHelper;

import java.io.Serializable;
import java.util.Map;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
  * Represents a directory of Probes. This enables the Probe framework to find the
 * right Probe class to instantiate and run, given a to-be-accessed data source.
  */
public interface ProbeDirectory
{
    /**
     * Obtain the default Probe that reads from a stream. If not other StreamProbe matches
     * a stream data source, this is the one that will be used.
     *
     * @return the descriptor describing the Probe.
     */
    public StreamProbeDescriptor getDefaultStreamProbe();
    
    /**
     * Find an XML DOM Probe by document type.
     *
     * @param documentType the found XML document type
     * @return the descriptor for the Probe that can parse this document type
     */
    public XmlDomProbeDescriptor getXmlDomProbeDescriptorByDocumentType(
            String documentType );

    /**
     * Find an XML DOM Probe by tag type.
     *
     * @param tagType the found XML tag type
     * @return the descriptor for the Probe that can parse this tag type
     */
    public XmlDomProbeDescriptor getXmlDomProbeDescriptorByTagType(
            String tagType );

    /**
     * Find an XML DOM Probe class name by document type.
     *
     * @param documentType the found XML document type
     * @return the name of the Probe class that can parse this document type
     */
    public String getXmlDomProbeClassByDocumentType(
            String documentType );

    /**
     * Find an XML DOM Probe class name by tag type.
     *
     * @param tagType the found XML tag type
     * @return the name of the Probe class that can parse this tag type
     */
    public String getXmlDomProbeClassByTagType(
            String tagType );
    
    /**
     * Find an API Probe class name by URL.
     *
     * @param url the URL for which we are looking
     * @return the name of the Probe class that can access this URL
     */
    public String getApiProbeClassForUrl(
            String url );

    /**
     * Find an API Probe by URL protocol.
     *
     * @param protocol the URL protocol (e.g. jdbc) for which we are looking
     * @return the descriptor of the Probe that can access URLs with this protocol
     */
    public ApiProbeDescriptor getApiProbeDescriptorByProtocol(
            String protocol );

    /**
     * Find an API Probe class name by URL protocol.
     *
     * @param protocol the URL protocol (e.g. jdbc) for which we are looking
     * @return the name of the Probe class that can access URLs with this protocol
     */
    public String getApiProbeClassByProtocol(
            String protocol );

    /**
     * Find an API Probe match by matched URL.
     *
     * @param url the URL for which we are looking
     * @return the descriptor of the Probe that can access this URL
     */
    public MatchDescriptor getApiProbeDescriptorByMatchedUrl(
            String url );

    /**
     * Find an API Probe class name by matched URL.
     *
     * @param url the URL for which we are looking
     * @return the name of the Probe class that can access this URL
     */
    public String getApiProbeClassByMatchedUrl(
            String url );

    /**
     * Find an stream Probe by MIME type.
     *
     * @param mimeType the MIME type for which we are looking, in form "xxx/yyy"
     * @return the descriptor of the Probe that can parse this MIME type
     */
    public StreamProbeDescriptor getStreamProbeDescriptorByMimeType(
            String mimeType );

    /**
     * Find an stream Probe class name by MIME type.
     *
     * @param mimeType the MIME type for which we are looking, in form "xxx/yyy"
     * @return the name of the Probe class that can parse this MIME type
     */
    public String getStreamProbeClassByMimeType(
            String mimeType );
    
    /**
     * Abstract superclass for all Probe descriptors.
     */
    public abstract static class ProbeDescriptor
        implements
            Serializable
    {
        /**
         * Constructor.
         *
         * @param className the name of the class implementing the Probe
         * @param clazz the actual Class we are supposed to instantiate (this is optional)
         * @param parameters the parameters for the Probe, if any
         */
        public ProbeDescriptor(
                String                 className,
                Class<? extends Probe> clazz,
                Map<String,Object>     parameters )
        {
            theClassName             = className;
            theClazz                 = clazz;
            theParameters            = parameters;
        }

        /**
         * Obtain the class name.
         *
         * @return the class name
         */
        public final String getProbeClassName()
        {
            return theClassName;
        }

        /**
         * Obtain the given class, if one was given.
         *
         * @return the Probe class, if one was given
         */
        public final Class<? extends Probe> getProbeClass()
        {
            return theClazz;
        }

        /**
         * Obtain the Probe parameters, if any.
         *
         * @return the Probe parametes, if any
         */
        public final Map<String,Object> getParameters()
        {
            return theParameters;
        }

        /**
         * Set Probe parameters (if not set in the constructor). This returns the this pointer,
         * in order to make it as convenient as possible to add this to a ProbeDirectory.
         *
         * @param parameters the Probe parameters
         * @return the this pointer
         */
        public final ProbeDescriptor setParameters(
                Map<String,Object> parameters )
        {
            theParameters = parameters;
            return this;
        }

        /**
         * The Probe class name.
         */
        protected String theClassName;

        /**
         * The Probe class (if any).
         */
        protected Class<? extends Probe> theClazz;

        /**
         * The Probe parameters (if any).
         */
        protected Map<String,Object> theParameters;
    }

    /**
     * An entry in the set of XML Probes.
     */
    public abstract static class XmlProbeDescriptor
        extends
            ProbeDescriptor
        implements
            Serializable
    {
        /**
         * Constructor.
         *
         * @param documentTypes the XML document types that this Probe can access
         * @param tagTypes the potential new XML root tags
         * @param className name of the Probe class
         * @param clazz the actual Probe class (optional)
         * @param parameters the parameters for the Probe, if any
         */
        public XmlProbeDescriptor(
                String []                 documentTypes,
                String []                 tagTypes,
                String                    className,
                Class<? extends XmlProbe> clazz,
                Map<String,Object>        parameters )
        {
            super( className, clazz, parameters );

            theDocumentTypes = documentTypes;
            theTagTypes = tagTypes;
        }

        /**
         * Obtain the supported XML document types.
         *
         * @return supported XML document types
         */
        public String [] getDocumentTypes()
        {
            return theDocumentTypes;
        }

        /**
         * Obtain the supported XML tag types.
         *
         * @return supported XML tag types
         */
        public String [] getTagTypes()
        {
            return theTagTypes;
        }

        /**
         * Determine whether this probe can process this XML document type.
         *
         * @param documentType the XML document type that we evaluate
         * @return if true, this Probe can process this XML document type
         */
        public boolean canProcessDocumentType(
                String documentType )
        {
            for( int i=0 ; i<theDocumentTypes.length ; ++i ) {
                if( documentType.equals( theDocumentTypes[i] )) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Determine whether this probe can process this XML tag type.
         *
         * @param tagType the XML document type that we evaluate
         * @return if true, this Probe can process this XML tag type
         */
        public boolean canProcessTagType(
                String tagType )
        {
            for( int i=0 ; i<theTagTypes.length ; ++i ) {
                if( tagType.equals( theTagTypes[i] )) {
                    return true;
                }
            }
            return false;
        }

        /**
         * For debugging.
         *
         * @return this class in a string representation
         */
        @Override
        public String toString()
        {
            StringBuffer buf = new StringBuffer( 100 );
            buf.append( super.toString() );
            buf.append( "{ docType: " );
            buf.append( ArrayHelper.arrayToString( theDocumentTypes ));
            buf.append( "{ tagType: " );
            buf.append( ArrayHelper.arrayToString( theTagTypes ));
            buf.append( ", className: " );
            buf.append( theClassName );
            buf.append( " }" );
            return buf.toString();
        }

        /**
         * The XML document types that we can access.
         */
        protected String [] theDocumentTypes;

        /**
         * The XML tag types that we can access.
         */
        protected String [] theTagTypes;
    }

    /**
     * This is an entry in the set of XML DOM Probes.
     */
    public static class XmlDomProbeDescriptor
            extends
                XmlProbeDescriptor
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Convenience constructor.
         *
         * @param documentType the XML document type that this Probe can access
         * @param tagType the XML tag type that this Probe can access
         * @param className name of the Probe class
         */
        public XmlDomProbeDescriptor(
                String                       documentType,
                String                       tagType,
                String                       className )
        {
            this( new String[] { documentType }, new String[] { tagType }, className, null, null );
        }

        /**
         * Convenience constructor.
         *
         * @param documentTypes the XML document types that this Probe can access
         * @param tagTypes the XML tag types that this Probe can access
         * @param className name of the Probe class
         */
        public XmlDomProbeDescriptor(
                String []                    documentTypes,
                String []                    tagTypes,
                String                       className )
        {
            this( documentTypes, tagTypes, className, null, null );
        }

        /**
         * Convenience constructor.
         *
         * @param documentType the XML document type that this Probe can access
         * @param tagType the XML tag type that this Probe can access
         * @param clazz the actual Probe class
         */
        public XmlDomProbeDescriptor(
                String                       documentType,
                String                       tagType,
                Class<? extends XmlDOMProbe> clazz )
        {
            this( new String[] { documentType }, new String[] { tagType }, clazz.getName(), clazz, null );
        }

        /**
         * Convenience constructor.
         *
         * @param documentTypes the XML document types that this Probe can access
         * @param tagTypes the XML tag types that this Probe can access
         * @param clazz the actual Probe class
         */
        public XmlDomProbeDescriptor(
                String []                    documentTypes,
                String []                    tagTypes,
                Class<? extends XmlDOMProbe> clazz )
        {
            this( documentTypes, tagTypes, clazz.getName(), clazz, null );
        }

        /**
         * Constructor.
         *
         * @param documentTypes the XML document types that this Probe can access
         * @param tagTypes the XML tag types that this Probe can access
         * @param className name of the Probe class
         * @param clazz the actual Probe class (if given)
         * @param parameters the parameters for the Probe, if any
         */
        public XmlDomProbeDescriptor(
                String []                    documentTypes,
                String []                    tagTypes,
                String                       className,
                Class<? extends XmlDOMProbe> clazz,
                Map<String,Object>           parameters )
        {
            super( documentTypes, tagTypes, className, clazz, parameters );
        }

    }

    /**
     * An entry in the list of Non-XML Probes reading from a stream.
     */
    public static class StreamProbeDescriptor
        extends
            ProbeDescriptor
        implements
            Serializable
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Convenience constructor.
         *
         * @param mimeType the MIME type that this Probe can access
         * @param className name of the Probe class
         */
        public StreamProbeDescriptor(
                String                mimeType,
                String                className )
        {
            this( new String[] { mimeType }, className, null, null );
        }

        /**
         * Convenience constructor.
         *
         * @param mimeTypes the MIME types that this Probe can access
         * @param className name of the Probe class
         */
        public StreamProbeDescriptor(
                String []             mimeTypes,
                String                className )
        {
            this( mimeTypes, className, null, null );
        }

        /**
         * Convenience constructor.
         *
         * @param mimeType the MIME type that this Probe can access
         * @param clazz the actual Probe class
         */
        public StreamProbeDescriptor(
                String                             mimeType,
                Class<? extends NonXmlStreamProbe> clazz )
        { 
            this( new String[] { mimeType }, clazz.getName(), clazz, null );
        }

        /**
         * Convenience constructor.
         *
         * @param mimeTypes the MIME types that this Probe can access
         * @param clazz the actual Probe class
         */
        public StreamProbeDescriptor(
                String []                          mimeTypes,
                Class<? extends NonXmlStreamProbe> clazz )
        {
            this( mimeTypes, clazz.getName(), clazz, null );
        }

        /**
         * Constructor.
         *
         * @param mimeTypes the MIME types that this Probe can access
         * @param className name of the Probe class
         * @param clazz the actual Probe class (if given)
         * @param parameters the parameters for the Probe, if any
         */
        public StreamProbeDescriptor(
                String []                          mimeTypes,
                String                             className,
                Class<? extends NonXmlStreamProbe> clazz,
                Map<String,Object>                 parameters )
        {
            super( className, clazz, parameters );

            theMimeTypes = mimeTypes;
        }

        /**
         * Obtain the MIME types that this Probe can access.
         *
         * @return the MIME types that this Probe can access
         */
        public String [] getMimeTypes()
        {
            return theMimeTypes;
        }

        /**
         * Determine whether this Probe can access this MIME type.
         *
         * @param candidateMimeType the MIME type that we evaluate
         * @return if true, this Probe can access this MIME type.
         */
        public boolean canProcessMime(
                String candidateMimeType )
        {
            for( int i=0 ; i<theMimeTypes.length ; ++i ) {
                if( candidateMimeType.equals( theMimeTypes[i] )) {
                    return true;
                }
            }
            return false;
        }

        /**
         * For debugging.
         *
         * @return a string representation of this class
         */
        @Override
        public String toString()
        {
            return StringHelper.objectLogString(
                    this,
                    new String[] {
                        "mime",
                        "className"
                    },
                    new Object[] {
                        theMimeTypes,
                        theClassName
                    });
        }

        /**
         * The MIME types that this Probe can access.
         */
        protected String [] theMimeTypes;
    }

    /**
     * This class describes an API Probe.
     */
    public static class ApiProbeDescriptor
        extends
            ProbeDescriptor
        implements
            Serializable
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Convenience constructor.
         *
         * @param protocol the URL protocol for this Probe (eg jdbc)
         * @param className the name of the Probe class
         */
        public ApiProbeDescriptor(
                String                protocol,
                String                className )
        {
            this( new String[] { protocol }, className, null, null );
        }

        /**
         * Convenience constructor.
         *
         * @param protocols the URL protocols for this Probe (eg jdbc)
         * @param className the name of the Probe class
         */
        public ApiProbeDescriptor(
                String []             protocols,
                String                className )
        {
            this( protocols, className, null, null );
        }

        /**
         * Convenience constructor.
         *
         * @param protocol the URL protocol for this Probe (eg jdbc)
         * @param clazz the actual class we are supposed to instantiate
         */
        public ApiProbeDescriptor(
                String                    protocol,
                Class<? extends ApiProbe> clazz )
        {
            this( new String[] { protocol }, clazz.getName(), clazz, null );
        }

        /**
         * Convenience constructor.
         *
         * @param protocols the URL protocols for this Probe (eg jdbc)
         * @param clazz the actual class we are supposed to instantiate
         */
        public ApiProbeDescriptor(
                String []                 protocols,
                Class<? extends ApiProbe> clazz )
        {
            this( protocols, clazz.getName(), clazz, null );
        }

        /**
         * Constructor.
         *
         * @param protocols the URL protocols for this Probe (eg jdbc)
         * @param className the name of the Probe class
         * @param clazz the actual class we are supposed to instantiate, if any
         * @param parameters the parameters for the Probe, if any
         */
        public ApiProbeDescriptor(
                String []                 protocols,
                String                    className,
                Class<? extends ApiProbe> clazz,
                Map<String,Object>        parameters )
        {
            super( className, clazz, parameters );

            theProtocols = protocols;
        }

        /**
         * Obtain the protocol types that this Probe can access.
         *
         * @return the set of protocols
         */
        public String [] getProtocols()
        {
            return theProtocols;
        }

        /**
         * Determine whether this Probe can process this protocol.
         *
         * @param protocol the protocol we are evaluating
         * @return if true, this Probe can access the passed-in protocol
         */
        public boolean canProcessProtocol(
                String protocol )
        {
            for( int i=0 ; i<theProtocols.length ; ++i ) {
                if( protocol.equals( theProtocols[i] )) {
                    return true;
                }
            }
            return false;
        }

        /**
         * For debugging.
         *
         * @return a string representation of this class
         */
        @Override
        public String toString()
        {
            StringBuffer buf = new StringBuffer( 100 );
            buf.append( super.toString() );
            buf.append( "{ protocols: " );
            buf.append( ArrayHelper.arrayToString( theProtocols ) );
            buf.append( ", className: " );
            buf.append( theClassName );
            buf.append( " }" );
            return buf.toString();
        }

        /**
         * The protocol types that this probe can process.
         */
        protected String [] theProtocols;
    }

    /**
     * This class matches a data source to a Probe.
     */
    public abstract static class MatchDescriptor
        extends
            ProbeDescriptor
        implements
            Serializable
    {
        /**
         * Constructor.
         *
         * @param className the Probe class that is being matched
         * @param clazz the actual Probe class (if given)
         * @param parameters the parameters for the Probe, if any
         */
        public MatchDescriptor(
                String                 className,
                Class<? extends Probe> clazz,
                Map<String,Object>     parameters )
        {
            super( className, clazz,  parameters );
        }
        
        /**
         * Determine whether the provided URL matches this match.
         *
         * @param url the provided URL
         * @return true if there is a match
         */
        public abstract boolean matches(
                String url );
    }

    /**
     * This class matches a specific data source to a Probe.
     */
    public static class ExactMatchDescriptor
        extends
            MatchDescriptor
        implements
            Serializable
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Convenience constructor.
         *
         * @param url the URL that shall be matched
         * @param className the Probe class that is being matched
         */
        public ExactMatchDescriptor(
                String                url,
                String                className )
        {
            this( url, className, null, null );
        }

        /**
         * Convenience constructor.
         *
         * @param url the URL that shall be matched
         * @param clazz the actual Probe class that is being matched
         */
        public ExactMatchDescriptor(
                String                 url,
                Class<? extends Probe> clazz )
        {
            this( url, clazz.getName(), clazz, null );
        }

        /**
         * Constructor.
         *
         * @param url the URL that shall be matched
         * @param className the Probe class that is being matched
         * @param clazz the actual Probe class (if given)
         * @param parameters the parameters for the Probe, if any
         */
        public ExactMatchDescriptor(
                String                 url,
                String                 className,
                Class<? extends Probe> clazz,
                Map<String,Object>     parameters )
        {
            super( className, clazz, parameters );

            theUrl = url;
        }

        /**
         * Obtain the matched URL.
         *
         * @return the matched URL.
         */
        public String getURL()
        {
            return theUrl;
        }

        /**
         * Determine whether the provided URL matches this match.
         *
         * @param url the provided URL
         * @return true if there is a match
         */
        public boolean matches(
                String url )
        {
            return theUrl.equals( url );
        }

        /**
         * The url for which we define a specific Probe.
         */
        protected String theUrl;
    }

    /**
     * This class matches a pattern of data sources to a Probe.
     */
    public static class PatternMatchDescriptor
        extends
            MatchDescriptor
        implements
            Serializable
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Convenience constructor.
         *
         * @param urlPattern the URL pattern that shall be matched
         * @param className the Probe class that is being matched
         */
        public PatternMatchDescriptor(
                Pattern               urlPattern,
                String                className )
        {
            this( urlPattern, className, null, null );
        }

        /**
         * Convenience constructor.
         *
         * @param urlPattern the URL pattern that shall be matched
         * @param clazz the actual Probe class that is being matched
         */
        public PatternMatchDescriptor(
                Pattern                urlPattern,
                Class<? extends Probe> clazz )
        {
            this( urlPattern, clazz.getName(), clazz, null );
        }

        /**
         * Constructor.
         *
         * @param urlPattern the URL pattern that shall be matched
         * @param className the Probe class that is being matched
         * @param clazz the actual Probe class (if given)
         * @param parameters the parameters for the Probe, if any
         */
        public PatternMatchDescriptor(
                Pattern                urlPattern,
                String                 className,
                Class<? extends Probe> clazz,
                Map<String,Object>     parameters )
        {
            super( className, clazz, parameters );

            theUrlPattern = urlPattern;
        }

        /**
         * Obtain the URL pattern
         *
         * @return the URL pattern
         */
        public Pattern getUrlPattern()
        {
            return theUrlPattern;
        }

        /**
         * Determine whether the provided URL matches this match.
         *
         * @param url the provided URL
         * @return true if there is a match
         */
        public boolean matches(
                String url )
        {
            Matcher m = theUrlPattern.matcher( url );
            if( m.matches() ) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * The url pattern for which we define a specific Probe.
         */
        protected Pattern theUrlPattern;
    }
}
