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

package org.infogrid.module;

import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This helper program takes XML advertisements and instantiates them.
 *
 * FIXME: support XML namespaces.
 */
public class ModuleAdvertisementXmlParser
        extends
            DefaultHandler
        implements
            ModuleXmlTags
{
    /**
     * Constructor.
     */
    public ModuleAdvertisementXmlParser()
    {
        theFactory = SAXParserFactory.newInstance();
        theFactory.setValidating( false );
    }

    /**
     * Parse the XML file.
     *
     * @param theStream the stream from which we read the ModuleAdvertisement
     * @param fileName the file name that is being parsed, for error reporting purposes
     * @return the read ModuleAdvertisement (may be subclass)
     * @throws IOException an input/output error occurred
     * @throws ModuleConfigurationException a configuration error occurred during parsing or setup
     */
    public synchronized ModuleAdvertisement readAdvertisement(
            InputStream theStream,
            String      fileName )
        throws
            IOException,
            ModuleConfigurationException
    {
        // initialize first
        initialize();

        // now do it
        try {
            SAXParser theParser = theFactory.newSAXParser();

            theParser.parse( theStream, this );

        } catch( ParserConfigurationException ex ) {
            throw new ModuleConfigurationException( null, "Could not parse file " + fileName, ex );
        } catch( SAXException ex ) {
            throw new ModuleConfigurationException( null, "Could not parse file " + fileName, ex );
        }

        ModuleAdvertisement ret;

        if( STANDARDMODULE_TAG.equals( keyword )) {
            ret = StandardModuleAdvertisement.create1(
                    name,
                    version,
                    usernames,
                    userdescriptions,
                    buildDate,
                    null, // FIXME, license
                    (ModuleRequirement []) copyIntoNewArray( buildTimeDependencies,   ModuleRequirement.class ),
                    (ModuleRequirement []) copyIntoNewArray( runTimeDependencies,     ModuleRequirement.class ),
                    jarsBaseUrl,
                    (String [])            copyIntoNewArray( jars,                    String.class ),
                    moduleParameterValues,
                    moduleParameterDefaults,
                    (ModuleCapability [])  copyIntoNewArray( capabilities,            ModuleCapability.class ),
                    activationClassName,
                    activationMethodName,
                    deactivationMethodName,
                    configurationClassName,
                    configurationMethodName,
                    runClassName,
                    runMethodName );

        } else if( MODELMODULE_TAG.equals( keyword )) {
            ret = ModelModuleAdvertisement.create1(
                    name,
                    version,
                    usernames,
                    userdescriptions,
                    buildDate,
                    null, // FIXME, license
                    (ModuleRequirement []) copyIntoNewArray( buildTimeDependencies,   ModuleRequirement.class ),
                    (ModuleRequirement []) copyIntoNewArray( runTimeDependencies,     ModuleRequirement.class ),
                    jarsBaseUrl,
                    (String [])            copyIntoNewArray( jars,                    String.class ),
                    moduleParameterValues,
                    moduleParameterDefaults );

        } else {
            throw new IllegalArgumentException( "Unexpected type of module: " + keyword );
        }
        return ret;
    }

    /**
     * Initialize local variables.
     */
    protected void initialize()
    {
        lastString                        = null;
        keyword                           = null;
        name                              = null;
        version                           = null;
        usernames                         = new HashMap<String,String>();
        userdescriptions                  = new HashMap<String,String>();
        buildDate                         = null;
        buildTimeDependencies             = new ArrayList<ModuleRequirement>();
        runTimeDependencies               = new ArrayList<ModuleRequirement>();
        jarsBaseUrl                       = null;
        jars                              = new ArrayList<String>();
        moduleParameterValues             = null;
        moduleParameterDefaults           = null;
        requirementParameterValues        = null;
        requirementParameterDefaults      = null;
        currentModuleRequirementBuildTime = false;
        currentModuleRequirementRunTime   = false;
        currentModuleRequirementName      = null;
        currentModuleRequirementVersion   = null;
        inDependenciesSection             = false;
        activationClassName               = null;
        activationMethodName              = "activate";
        deactivationMethodName            = "deactivate";
        configurationClassName            = null;
        configurationMethodName           = "configure";
        runClassName                      = null;
        runMethodName                     = "main";
        argumentCombinations              = null;
        arguments                         = null;
        capabilities                      = new ArrayList<ModuleCapability>();
        interfaceNames                    = null;
        implementationName                = null;

        theLocator = new MyLocator();
    }

    /**
     * Receive a Locator object for document events.
     *
     * @param locator A locator for all SAX document events.
     * @see org.xml.sax.ContentHandler#setDocumentLocator
     * @see org.xml.sax.Locator
     */
    @Override
    public void setDocumentLocator(
            Locator locator )
    {
        theLocator.update(
                locator.getPublicId(),
                locator.getSystemId(),
                locator.getLineNumber(),
                locator.getColumnNumber());
    }

    /**
     * Callback indicating that a new XML element starts.
     *
     * @param namespaceURI The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param sName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param qName The qualified name (with prefix), or the
     *        empty string if qualified names are not available.
     * @param attrs The attributes attached to the element.  If
     *        there are no attributes, it shall be an empty
     *        Attributes object.
     * @throws SAXException if an error occurred
     * @see #endElement
     */
    @Override
    public void startElement(
            String     namespaceURI,
            String     sName,
            String     qName,
            Attributes attrs )
        throws
            SAXException
    {
        lastString = null;

        if( STANDARDMODULE_TAG.equals( qName ) ) {
            keyword = qName;

        } else if( MODELMODULE_TAG.equals( qName ) ) {
            keyword = qName;

        } else if( NAME_TAG.equals( qName )) {
            // no op

        } else if( VERSION_TAG.equals( qName )) {
            // no op

        } else if( USERNAME_TAG.equals( qName )) {
            locale = attrs.getValue( LOCALE_PAR );

        } else if( USERDESCRIPTION_TAG.equals( qName )) {
            locale = attrs.getValue( LOCALE_PAR );

        } else if( BUILD_TIME_TAG.equals( qName )) {
            // no op

        } else if( PROVIDES_TAG.equals( qName )) {
            String jarsBase = attrs.getValue( BASE_URL_PAR );
            if( jarsBase != null ) {
                try {
                    jarsBaseUrl = new URL( jarsBase );
                } catch( MalformedURLException ex ) {
                    throw new SAXParseException( "MalformedURL: " + jarsBase, theLocator );
                }
            }

        } else if( JAR_TAG.equals( qName )) {
            // no op

        } else if( ACTIVATIONCLASS_TAG.equals( qName )) {
            // no op

        } else if( CONFIGURATIONCLASS_TAG.equals( qName )) {
            // no op

        } else if( RUNCLASS_TAG.equals( qName )) {
            // no op

        } else if( DEPENDENCIES_TAG.equals( qName )) {

            inDependenciesSection = true;

        } else if( REQUIRES_TAG.equals( qName )) {
            requirementParameterValues   = new HashMap<String,String>();
            requirementParameterDefaults = new HashMap<String,String>();

            currentModuleRequirementName = attrs.getValue( NAME_PAR );
            if( currentModuleRequirementName == null ) {
                throw new SAXParseException( "ModuleRequirement cannot have empty name", theLocator );
            }
            currentModuleRequirementVersion = attrs.getValue( VERSION_PAR ); // may or may not be given

            String mode = attrs.getValue( MODE_PAR );
            if( mode == null || "".equals( mode ) || MODE_PAR_BOTH.equalsIgnoreCase( mode )) {
                currentModuleRequirementBuildTime = true;
                currentModuleRequirementRunTime   = true;

            } else if( MODE_PAR_BUILDTIME.equalsIgnoreCase( mode )) {
                currentModuleRequirementBuildTime = true;
                currentModuleRequirementRunTime   = false;

            } else if( MODE_PAR_RUNTIME.equalsIgnoreCase( mode )) {
                currentModuleRequirementBuildTime = false;
                currentModuleRequirementRunTime   = true;

            } else {
                 throw new SAXParseException( "Cannot understand mode of dependency: " + mode, theLocator );
            }

        } else if( CAPABILITY_TAG.equals( qName )) {
            implementationName   = null;
            argumentCombinations = new ArrayList<ModuleCapability.ArgumentCombination>();
            interfaceNames       = new ArrayList<String>();

        } else if( INTERFACE_TAG.equals( qName )) {
            // no op

        } else if( IMPLEMENTATION_TAG.equals( qName )) {
            // no op

        } else if( ARGUMENTCOMBINATION_TAG.equals( qName )) {
            arguments = new ArrayList<String>();

        } else if( ARG_TAG.equals( qName )) {
            // no op

        } else if( PARAMETER_TAG.equals( qName )) {
            String parName    = attrs.getValue( NAME_PAR );
            String parDefault = attrs.getValue( DEFAULT_PAR );
            String parValue   = attrs.getValue( VALUE_PAR );

            if( parName == null ) {
                throw new SAXParseException( "No name was given in the parameter tag", theLocator );

            } else if( !inDependenciesSection ) {
                if( parDefault == null ) {
                    if( parValue != null ) {
                        if( moduleParameterValues == null ) {
                            moduleParameterValues = new HashMap<String,String>();
                        }
                        moduleParameterValues.put( parName, parValue );

                    } else {
                        throw new SAXParseException( "No value or default was given for parameter " + parName, theLocator );

                    }
                } else if( parValue == null ) {
                    if( moduleParameterDefaults == null ) {
                        moduleParameterDefaults = new HashMap<String,String>();
                    }
                    moduleParameterDefaults.put( parName, parDefault );

                } else {
                    throw new SAXParseException( "Specify either value or default, not both, for parameter " + parName, theLocator );
                }

            } else {
                // this is for a Module Requirement
                if( parDefault == null ) {
                    if( parValue != null ) {
                        if( requirementParameterValues == null ) {
                            requirementParameterValues = new HashMap<String,String>();
                        }
                        requirementParameterValues.put( parName, parValue );

                    } else {
                        throw new SAXParseException( "No value or default was given for parameter " + parName, theLocator );
                    }

                } else if( parValue == null ) {
                    if( requirementParameterDefaults == null ) {
                        requirementParameterDefaults = new HashMap<String,String>();
                    }
                    requirementParameterDefaults.put( parName, parDefault );

                } else {
                    throw new SAXParseException( "Specify either value or default, not both, for parameter " + parName, theLocator );
                }
            }
        } else {
            throw new SAXParseException( "Don't know anything about opening tag " + qName, theLocator );
        }
    }

    /**
     * Callback indicating that we found some characters.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param length The number of characters to read from the array.
     */
    @Override
    public void characters(
            char [] ch,
            int     start,
            int     length )
    {
        if( lastString == null ) {
            lastString = new String( ch, start, length );
        } else {
            lastString = lastString + new String( ch, start, length );
        }
    }

    /**
     * Callback indicating that an XML element starts.
     *
     * @param namespaceURI The Namespace URI, or the empty string if the
     *        element has no Namespace URI or if Namespace
     *        processing is not being performed.
     * @param sName The local name (without prefix), or the
     *        empty string if Namespace processing is not being
     *        performed.
     * @param qName The qualified name (with prefix), or the
     *        empty string if qualified names are not available.
     * @throws SAXException a parse error occurred
     */
    @Override
    public void endElement(
            String namespaceURI,
            String sName,
            String qName )
        throws
            SAXException
    {
        if( STANDARDMODULE_TAG.equals( qName ) ) {
            keyword = qName;

        } else if( MODELMODULE_TAG.equals( qName ) ) {
            keyword = qName;

        } else if( NAME_TAG.equals( qName )) {
            name = lastString;

        } else if( VERSION_TAG.equals( qName )) {
            version = lastString;

        } else if( USERNAME_TAG.equals( qName )) {
            usernames.put( locale, lastString );

        } else if( USERDESCRIPTION_TAG.equals( qName )) {
            userdescriptions.put( locale, lastString );

        } else if( BUILD_TIME_TAG.equals( qName )) {
            try {
                buildDate = theDefaultDateFormat.parse( lastString );
            } catch( ParseException ex ) {
                throw new SAXParseException( "Date value cannot be parsed: " + ex, theLocator );
            }

        } else if( PROVIDES_TAG.equals( qName )) {
            // no op

        } else if( JAR_TAG.equals( qName )) {
            jars.add( lastString );

        } else if( ACTIVATIONCLASS_TAG.equals( qName )) {
            activationClassName = lastString;

        } else if( CONFIGURATIONCLASS_TAG.equals( qName )) {
            configurationClassName = lastString;

        } else if( RUNCLASS_TAG.equals( qName )) {
            runClassName = lastString;

        } else if( DEPENDENCIES_TAG.equals( qName )) {
            inDependenciesSection = false;

        } else if( REQUIRES_TAG.equals( qName )) {
            if( currentModuleRequirementName != null ) {
                ModuleRequirement req = ModuleRequirement.create1(
                        currentModuleRequirementName,
                        currentModuleRequirementVersion,
                        requirementParameterValues,
                        requirementParameterDefaults );

                if( currentModuleRequirementBuildTime ) {
                    buildTimeDependencies.add( req );
                }
                if( currentModuleRequirementRunTime ) {
                    // this is NOT an "else", it can be both
                    runTimeDependencies.add( req );
                }
            }

        } else if( CAPABILITY_TAG.equals( qName )) {
            ModuleCapability currentCapability = ModuleCapability.create1(
                    (String[]) copyIntoNewArray(
                            interfaceNames,
                            String.class ),
                    implementationName,
                    (ModuleCapability.ArgumentCombination[]) copyIntoNewArray(
                            argumentCombinations,
                            ModuleCapability.ArgumentCombination.class ));

            capabilities.add( currentCapability );

            interfaceNames       = null;
            implementationName   = null;
            argumentCombinations = null;

        } else if( INTERFACE_TAG.equals( qName )) {
            interfaceNames.add( lastString );
        } else if( IMPLEMENTATION_TAG.equals( qName )) {
            implementationName = lastString;
        } else if( ARGUMENTCOMBINATION_TAG.equals( qName )) {

            argumentCombinations.add(
                    ModuleCapability.createArgumentCombination1(
                            (String []) copyIntoNewArray(
                                    arguments,
                                    String.class )) );

            arguments = null;

        } else if( ARG_TAG.equals( qName )) {
            arguments.add( lastString );
        } else if( PARAMETER_TAG.equals( qName )) {
            // FIXME
        } else {
            throw new SAXParseException( "Don't know anything about closing tag " + qName, theLocator );
        }
        lastString = null;
    }

    /**
     * Copy elements into a new Array of a certain type.
     *
     * @param theCollection the Collection from which to take the data
     * @param arrayComponentType the type of array that shall be allocated
     * @return the new Array of type arrayComponentType filled with the data from theCollection
     */
    protected static Object [] copyIntoNewArray(
            Collection theCollection,
            Class      arrayComponentType )
    {
        Object [] ret = (Object []) Array.newInstance(
                arrayComponentType,
                theCollection.size() );

        Iterator theIter = theCollection.iterator();
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = theIter.next();
        }
        return ret;

    }

    /**
     * The ParserFactory that we use.
     */
    protected SAXParserFactory theFactory;

    /**
     * The last string we found during parsing.
     */
    protected String lastString;

    /**
     * The outermost keyword indicating what type of Module it is.
     */
    protected String keyword;

    /**
     * The name element.
     */
    protected String name;

    /**
     * The version element.
     */
    protected String version;

    /**
     * The locale attribute.
     */
    protected String locale;

    /**
     * The user names, keyed by locale.
     */
    protected Map<String,String> usernames;

    /**
     * The user descriptions, keyed by locale.
     */
    protected Map<String,String> userdescriptions;
    
    /**
     * The time when the Module was built.
     */
    protected Date buildDate;

    /**
     * The build-time dependencies, as ArrayList<ModuleRequirement>.
     */
    protected ArrayList<ModuleRequirement> buildTimeDependencies = new ArrayList<ModuleRequirement>();

    /**
     * The run-time dependencies, as ArrayList<ModuleRequirement>.
     */
    protected ArrayList<ModuleRequirement> runTimeDependencies = new ArrayList<ModuleRequirement>();

    /**
     * The URL at which the JARs can be found.
     */
    protected URL jarsBaseUrl;

    /**
     * The JAR files that we provide, as ArrayList<String>.
     */
    protected ArrayList<String> jars = new ArrayList<String>();

    /**
     * The parameter-value pairs for this Module that cannot be overridden.
     */
    protected Map<String,String> moduleParameterValues;

    /**
     * The parameter-value pairs for this Module that may be overridden.
     */
    protected Map<String,String> moduleParameterDefaults;

    /**
     * The parameter-value pairs for this ModuleRequirement that cannot be overridden.
     */
    protected Map<String,String> requirementParameterValues;

    /**
     * The parameter-value pairs for this ModuleRequirement that may be overridden.
     */
    protected Map<String,String> requirementParameterDefaults;

    /**
     * Is the current ModuleRequirement a build-time requirement.
     */
    protected boolean currentModuleRequirementBuildTime;

    /**
     * Is the current ModuleRequirement a run-time requirement.
     */
    protected boolean currentModuleRequirementRunTime;

    /**
     * The name of the current ModuleRequirement.
     */
    protected String currentModuleRequirementName;

    /**
     * The version of the current ModuleRequirement.
     */
    protected String currentModuleRequirementVersion;

    /**
     * True when we are inside the dependencies section
     */
    protected boolean inDependenciesSection = false;

    /**
     * Name of the activation class.
     */
    protected String activationClassName;

    /**
     * Name of the activation method.
     */
    protected String activationMethodName = "activate";

    /**
     * Name of the deactivation method.
     */
    protected String deactivationMethodName = "deactivate";

    /**
     * Name of the configuration class.
     */
    protected String configurationClassName;

    /**
     * Name of the configuration method.
     */
    protected String configurationMethodName = "configure";

    /**
     * Name of the run class.
     */
    protected String runClassName;

    /**
     * Name of the run method.
     */
    protected String runMethodName = "main";

    /**
     * The set of ArgumentCombinations that we are currently parsing. Only important during parsing.
     */
    protected ArrayList<ModuleCapability.ArgumentCombination> argumentCombinations;

    /**
     * The list of arguments that we are currently parsing. Only important during parsing.
     */
    protected ArrayList<String> arguments;

    /**
     * The list of ModuleCapabilities of this Module. Only important during parsing.
     */
    protected ArrayList<ModuleCapability> capabilities = new ArrayList<ModuleCapability>();

    /**
     * The current set of interface names of this capability. Only important during parsing.
     */
    protected ArrayList<String> interfaceNames;

    /**
     * The implementation name of the currently parsed capability. Only important during parsing.
     */
    protected String implementationName;

    /**
     * Where are we in parsing the stream.
     */
    protected MyLocator theLocator;

    /**
     * Simple Locator implementation.
     */
    protected static class MyLocator
        implements
            Locator
    {
        /**
         * Return the public identifier for the current document event.
         *
         * @return A string containing the public identifier, or
         *         null if none is available.
         * @see #getSystemId
         */
        public String getPublicId()
        {
            return thePublicId;
        }

        /**
         * Return the system identifier for the current document event.
         *
         * @return A string containing the system identifier, or null
         *         if none is available.
         * @see #getPublicId
         */
        public String getSystemId()
        {
            return theSystemId;
        }

        /**
         * Return the line number where the current document event ends.
         *
         * @return The line number, or -1 if none is available.
         * @see #getColumnNumber
         */
        public int getLineNumber()
        {
            return theLineNumber;
        }

        /**
         * Return the column number where the current document event ends.
         *
         * @return The column number, or -1 if none is available.
         * @see #getLineNumber
         */
        public int getColumnNumber()
        {
            return theColumnNumber;
        }

        /**
         * This allows us to update what is held by this object.
         *
         * @param newPublicId the new public ID
         * @param newSystemId the new system ID
         * @param newLineNumber the new line number
         * @param newColumnNumber the new column number
         */
        void update(
                String newPublicId,
                String newSystemId,
                int newLineNumber,
                int newColumnNumber )
        {
            thePublicId     = newPublicId;
            theSystemId     = newSystemId;
            theLineNumber   = newLineNumber;
            theColumnNumber = newColumnNumber;
        }

        /**
         * The XML public ID.
         */
        protected String thePublicId;

        /**
         * The XML system ID.
         */
        protected String theSystemId;

        /**
         * The current line number.
         */
        protected int theLineNumber;

        /**
         * The current column number.
         */
        protected int theColumnNumber;
    }
}
