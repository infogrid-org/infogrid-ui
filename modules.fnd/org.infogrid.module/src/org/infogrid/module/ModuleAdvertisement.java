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

package org.infogrid.module;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * An advertisement for a software Module. More specific subclasses
 * are instantiable, this is an abstract superclass.
 */
public abstract class ModuleAdvertisement
        implements
            ModuleXmlTags,
            Serializable
{
    /**
      * Factory method that constructs a (subtype of) ModuleAdvertisement from its serialized form.
      *
      * @param theStream where to read the serialized ModuleAdvertisement from
      * @return the read ModuleAdvertisement
      * @throws IOException thrown if an error occurred during read from the stream
      * @throws ClassNotFoundException if the specific subclass of ModuleAdvertisement was not found, or any other needed class to instantiate object
      */
    public static ModuleAdvertisement create(
            ObjectInputStream theStream )
        throws
            IOException,
            ClassNotFoundException
    {
        return (ModuleAdvertisement) theStream.readObject();
    }

    /**
      * Private constructor, use factory method.
      *
      * @param moduleName the programmatic name of the to-be-created Module
      * @param moduleVersion the version of the to-be-created Module
      * @param moduleUserNames the name shown to the user of the to-be-created Module, keyed by the locale
      * @param moduleUserDescriptions the description shown to the user of the to-be-created Module, keyed by the locale
      * @param moduleBuildDate the time when this Module was built
      * @param license the license for the to-be-created Module
      * @param buildTimeModuleRequirements the ModuleRequirements of this Module at build time
      * @param runTimeModuleRequirements the ModuleRequirements of this Module at run time
      * @param moduleJarsBaseUrl the base URL from where to download the JARs of this Module
      * @param moduleJars names of the JAR files provided by this Module
      * @param localParameterValues the parameter-value paris of parameters that cannot be overridden by inherited values
      * @param localParameterDefaults the parameter-value paris of parameters that are overridden by inherited values
      */
    protected ModuleAdvertisement(
            String               moduleName,
            String               moduleVersion,
            Map<String,String>   moduleUserNames,
            Map<String,String>   moduleUserDescriptions,
            Date                 moduleBuildDate,
            ModuleLicense        license,
            ModuleRequirement [] buildTimeModuleRequirements,
            ModuleRequirement [] runTimeModuleRequirements,
            URL                  moduleJarsBaseUrl,
            String            [] moduleJars,
            Map<String,String>   localParameterValues,
            Map<String,String>   localParameterDefaults )
    {
        theModuleName                  = moduleName;
        theModuleVersion               = moduleVersion;
        theModuleUserNames             = moduleUserNames;
        theModuleUserDescriptions      = moduleUserDescriptions;
        theModuleBuildDate             = moduleBuildDate;
        theModuleLicense               = license;
        theBuildTimeModuleRequirements = buildTimeModuleRequirements;
        theRunTimeModuleRequirements   = runTimeModuleRequirements;
        theModuleJarsBaseUrl           = moduleJarsBaseUrl;
        theModuleJars                  = moduleJars;
        theLocalParameterValues        = localParameterValues;
        theLocalParameterDefaults      = localParameterDefaults;
    }

    /**
      * Obtain the fully-qualified, unique name of the Module.
      *
      * @return the name for this Module
      */
    public final String getModuleName()
    {
        return theModuleName;
    }

    /**
     * Obtain the internationalized name of the Module in the current locale.
     *
     * @return the internationalized name of the Module, in the current locale
     */
    public String getModuleUserName()
    {
        return getModuleUserName( Locale.getDefault() );
    }

    /**
     * Obtain the internationalized name of the Module in a specified locale.
     *
     * @param loc the locale
     * @return the internationalized name of the Module, in the specified locale
     */
    public String getModuleUserName(
            Locale loc )
    {
        String ret = null;
        if( theModuleUserNames != null ) {
            String key1 = loc.getCountry();
            if( loc.getLanguage() != null ) {
                String key2 = key1 + "." + loc.getLanguage();
                if( loc.getVariant() != null ) {
                    String key3 = key2 + "." + loc.getVariant();
            
                    ret = theModuleUserNames.get( key3 );
                }
                if( ret == null ) {
                    ret = theModuleUserNames.get( key2 );
                }
            }
            if( ret == null ) {
                ret = theModuleUserNames.get( key1 );
            }
        }
        if( ret == null ) {
            ret = theModuleName; // reasonable default
        }
        return ret;
    }

    /**
     * Obtain the internationalized description of the Module in a specified locale.
     *
     * @param loc the locale
     * @return the internationalized description of the Module, in the specified locale
     */
    public String getModuleUserDescription(
            Locale loc )
    {
        String ret = null;
        if( theModuleUserDescriptions != null ) {
            String key1 = loc.getCountry();
            if( loc.getLanguage() != null ) {
                String key2 = key1 + "." + loc.getLanguage();
                if( loc.getVariant() != null ) {
                    String key3 = key2 + "." + loc.getVariant();
            
                    ret = theModuleUserDescriptions.get( key3 );
                }
                if( ret == null ) {
                    ret = theModuleUserDescriptions.get( key2 );
                }
            }
            if( ret == null ) {
                ret = theModuleUserDescriptions.get( key1 );
            }
        }
        return ret;
    }

    /**
     * Obtain the Map of internationalized names of the Module.
     *
     * @return the Map of internationalized names of the Module, keyed by locale
     */
    public Map<String,String> getModuleUserNames()
    {
        return theModuleUserNames;
    }

    /**
     * Obtain the Map of internationalized descriptions of the Module.
     *
     * @return the Map of internationalized descriptions of the Module, keyed by locale
     */
    public Map<String,String> getModuleUserDescriptions()
    {
        return theModuleUserDescriptions;
    }

    /**
     * Obtain the version of this Module.
     *
     * @return the version for of this Module
     */
    public final String getModuleVersion()
    {
        return theModuleVersion;
    }

    /**
     * Obtain the time at which this Module was built.
     *
     * @return the time at which this Module was built.
     */
    public final Date getModuleBuildDate()
    {
        return theModuleBuildDate;
    }

    /**
     * Obtain the license for this Module.
     *
     * @return the license for this Module
     */
    public final ModuleLicense getModuleLicense()
    {
        return theModuleLicense;
    }

    /**
     * Obtain the list of requirements for other Modules that this Module depends on
     * at build time.
     *
     * @return the list of ModuleRequirements of this Module at build time
     */
    public final ModuleRequirement [] getBuildTimeModuleRequirements()
    {
        return theBuildTimeModuleRequirements;
    }

    /**
     * Obtain the list of requirements for other Modules that this Module depends on
     * at run time.
     *
     * @return the list of ModuleRequirements of this Module at run time
     */
    public final ModuleRequirement [] getRunTimeModuleRequirements()
    {
        return theRunTimeModuleRequirements;
    }

    /**
     * Obtain the list of JAR files that this Module provides. These are file names, including the
     * .jar extension, but without any paths.
     *
     * @return list of JAR file names without path information
     * @see #getProvidesJarsBaseDownloadUrl
     */
    public final String [] getProvidesJars()
    {
        return theModuleJars;
    }

    /**
     * Obtain the base URL from which to download JAR files for this Module. The
     * actual URL is the concatenation of the return value of this method and the
     * name of the JAR file as returned by getProvidesJars().
     *
     * @return the base URL from which to download JAR files for this Module
     * @see #getProvidesJars
     */
    public final URL getProvidesJarsBaseDownloadUrl()
    {
        return theModuleJarsBaseUrl;
    }

    /**
     * Obtain the list of parameter-value pairs that this Module inherits to all of its
     * dependent Modules, irrespective of what parameter-value pairs are provided to
     * this Module from Modules depending on this Module.
     *
     * @return the parameter-value pairs, or null
     */
    public final Map<String,String> getLocalParameterValues()
    {
        return theLocalParameterValues;
    }

    /**
     * Obtain the list of parameter-value pairs that this Module inherits to all of its
     * dependent Modules if no other parameter-value pairs are provided to
     * this Module from Modules depending on this Module.
     *
     * @return the parameter-value pairs, or null
     */
    public final Map<String,String> getLocalParameterDefaults()
    {
        return theLocalParameterDefaults;
    }

    /**
     * Two ModuleAdvertisements are the same if they have the same name and version.
     *
     * @param other the object to test against
     * @return if they are equal, return true
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( other == null ) {
            return false;
        }
        if( getClass() != other.getClass() ) {
            return false;
        }
        ModuleAdvertisement realOther = (ModuleAdvertisement) other;

        if( ! theModuleName.equals( realOther.theModuleName )) {
            return false;
        }
        if( theModuleVersion != null ) {
            return theModuleVersion.equals( realOther.theModuleVersion );
        } else {
            return realOther.theModuleVersion == null;
        }
    }

    /**
     * We determine the hashcode by looking at the Module's name.
     *
     * @return a hash code
     */
    @Override
    public int hashCode()
    {
        return theModuleName.hashCode();
    }

    /**
     * Convert content to a printable String. Similarly to toString, but not using
     * our funny hierarchical syntax.
     *
     * @return string representation of this object
     */
    public String contentToString()
    {
        StringBuffer buf = new StringBuffer( 32 );
        buf.append( getClass().getName() );
        buf.append( " " );
        buf.append( getModuleName() );

        return buf.toString();
    }

    /**
     * Create a Module from this ModuleAdvertisement. This is not supposed to be invoked
     * by the user of this framework.
     *
     * @param registry the ModuleRegistry in which the to-be-created Module will look for dependent Modules
     * @param moduleDirectory the Module's directory
     * @param jars the JAR files of this Module
     * @param parentClassLoader the ClassLoader of our parent Module
     * @return the created Module
     */
    protected abstract Module createModule(
            ModuleRegistry registry,
            File           moduleDirectory,
            File []        jars,
            ClassLoader    parentClassLoader );

    /**
     * Create a String expression in the Java language that, when executed, creates
     * an instance of (a subclass of) ModuleAdvertisement that is equal to this
     * ModuleAdvertisement.
     *
     * @param tabs the number of tabs to insert
     * @return the String expression
     */
    public abstract String getJavaConstructorString(
            int tabs );

    /**
     * This method factors out the common parameters in the constructor Strings for
     * the subclasses of this class.
     *
     * @param tabs the number of tabs to insert
     * @return constructor String in Java
     */
    protected String getJavaConstructorStringCommons(
            int tabs )
    {
        StringBuffer buf = new StringBuffer();
        buf.append( getJavaConstructorStringString( tabs, getModuleName() ));
        buf.append( ",\n" );
        buf.append( tabs( tabs ));
        buf.append( getJavaConstructorStringString( tabs, getModuleVersion() ));
        buf.append( ",\n" );
        buf.append( tabs( tabs ));
        buf.append( getJavaConstructorStringMap( tabs, getModuleUserNames() ));
        buf.append( ",\n" );
        buf.append( tabs( tabs ));
        buf.append( getJavaConstructorStringMap( tabs, getModuleUserDescriptions() ));
        buf.append( ",\n" );
        buf.append( tabs( tabs ));
        if( getModuleBuildDate() != null ) {
            buf.append( "new java.util.Date( " ).append( getModuleBuildDate().getTime() ).append( " ),\n" );
        } else {
            buf.append( "null,\n" );
        }
        buf.append( tabs( tabs ));
        buf.append( "null,\n" ); // license -- FIXME
        buf.append( tabs( tabs ));
        buf.append( getJavaConstructorStringModuleRequirements( tabs, getBuildTimeModuleRequirements()) );
        buf.append( ",\n" );
        buf.append( tabs( tabs ));
        buf.append( getJavaConstructorStringModuleRequirements( tabs, getRunTimeModuleRequirements()) );
        buf.append( ",\n" );

        URL u = getProvidesJarsBaseDownloadUrl();
        buf.append( tabs( tabs ));
        if( u != null ) {
            buf.append( "new URL( \"" );
            buf.append( u.toExternalForm() );
            buf.append( "\" )" );
        } else {
            buf.append( "null" );
        }
        buf.append( ",\n" );
        buf.append( tabs( tabs ));
        buf.append( "new String[] {\n" );
        for( int i=0 ; i<getProvidesJars().length ; ++i ) {
            if( i>0 ) {
                buf.append( ",\n" );
            }
            buf.append( tabs( tabs+1 ));
            buf.append( "\"" );
            buf.append( getProvidesJars()[i] );
            buf.append( "\"" );
        }
        buf.append( "\n" );
        buf.append( tabs( tabs ));
        buf.append( "},\n" );
        buf.append( tabs( tabs ));
        buf.append( getJavaConstructorStringMap( tabs, theLocalParameterValues ));
        buf.append( ",\n" );
        buf.append( tabs( tabs ));
        buf.append( getJavaConstructorStringMap( tabs, theLocalParameterDefaults ));

        return buf.toString();
    }

    /**
     * Helper method to create a constructor String for an array of ModuleRequirements.
     *
     * @param tabs the number of tabs to insert
     * @param reqs the ModuleRequirements for which we want to create a Java constructor String
     * @return constructor String in Java
     */
    protected String getJavaConstructorStringModuleRequirements(
            int                  tabs,
            ModuleRequirement [] reqs )
    {
        StringBuffer buf = new StringBuffer();
        if( reqs == null ) {
            buf.append( "null" );
        } else {
            buf.append( "new ModuleRequirement[] {" );
            for( int i=0 ; i<reqs.length ; ++i ) {
                if( i>0 ) {
                    buf.append( ",\n" );
                } else {
                    buf.append( "\n" );
                }
                buf.append( tabs( tabs+1 ));
                buf.append( reqs[i].getJavaConstructorString( tabs+1 ));
            }
            buf.append( "\n" );
            buf.append( tabs( tabs ));
            buf.append( "}" );
        }
        return buf.toString();
    }

    /**
     * Helper method to create a constructor String for a Map.
     *
     * @param tabs the number of tabs to insert
     * @param m the Map for which we want to create a Java constructor String
     * @return constructor String in Java
     */
    static String getJavaConstructorStringMap(
            int                           tabs,
            Map<String, ? extends Object> m )
    {
        StringBuffer buf = new StringBuffer();
        if( m != null ) {
            Iterator<String> iter = m.keySet().iterator();
            if( iter.hasNext() ) {
                buf.append( "ModuleAdvertisement.createMapFromStringArrays( new String[][] {\n" );
                while( iter.hasNext() ) {
                    String key   = iter.next();
                    Object value = m.get( key );

                    buf.append( tabs( tabs+1 ));
                    buf.append( "{\n" );
                    buf.append( tabs( tabs+2 ));
                    if( key != null ) {
                        buf.append( "\"" ).append( stringToJavaString( key )).append( "\"" );
                    } else {
                        buf.append( "null" );
                    }
                    buf.append( ",\n" );
                    buf.append( tabs( tabs+2 ));
                    if( value != null ) {
                        if( value instanceof String ) {
                            buf.append( "\"" ).append( stringToJavaString( (String) value )).append( "\"" );
                        } else {
                            throw new IllegalArgumentException( "Cannot create constructor with non-String Map value" );
                        }
                    } else {
                        buf.append( "null" );
                    }
                    buf.append( "\n" );
                    buf.append( tabs( tabs+1 )).append( "}" );

                    if( iter.hasNext() ) {
                        buf.append( ",\n" );
                    }
                }
                buf.append( "\n" );
                buf.append( tabs( tabs ));
                buf.append( "})" );
            } else {
                buf.append( "null" );
            }
        } else {
            buf.append( "null" );
        }
        return buf.toString();
    }

    /**
     * Helper method to create a constructor String for a String.
     *
     * @param tabs the number of tabs to insert
     * @param input the String
     * @return constructor String in Java
     */
    static String getJavaConstructorStringString(
            int    tabs,
            String input )
    {
        StringBuffer buf = new StringBuffer();
        if( input == null ) {
            buf.append( "null" );
        } else {
            buf.append( "\"" );
            buf.append( stringToJavaString( input ));
            buf.append( "\"" );
        }
        return buf.toString();
    }

    /**
     * Escape new-line characters in Strings into Java-escaped strings.
     * 
     * @param raw the raw String
     * @return the escaped String
     */
    static String stringToJavaString(
            String raw )
    {
        String ret = raw.replaceAll( "\n", "\\\\n" ); // regex apparently likes slashes
        return ret;
    }

    /**
     * This method is invoked by the generated constructor String to create a Map from a String[][].
     *
     * @param input the array of key-value pairs that constitute the content of the Map
     * @return the created Map
     */
    public static Map<String,String> createMapFromStringArrays(
            String [][] input )
    {
        if( input == null || input.length == 0 ) {
            return null;
        }
        HashMap<String,String> ret = new HashMap<String,String>( input.length );
        for( int i=0 ; i<input.length ; ++i ) {
            ret.put( input[i][0], input[i][1] );
        }
        return ret;
    }

    /**
     * Create XML that represents this ModuleAdvertisement.
     *
     * @return XML format
     */
    public abstract String getAsXml();

    /**
     * Append an opening XML tag.
     *
     * @param tag the tag
     * @param buf the buffer to append to
     */
    protected void appendOpenTag(
            String tag,
            StringBuilder buf )
    {
        buf.append( "<" );
        buf.append( tag );
        buf.append( ">" );
    }

    /**
     * Append a closing XML tag.
     *
     * @param tag the tag
     * @param buf the buffer to append to
     */
    protected void appendCloseTag(
            String tag,
            StringBuilder buf )
    {
        buf.append( "</" );
        buf.append( tag );
        buf.append( ">" );
    }

    /**
     * Create XML that represents this ModuleAdvertisement.
     *
     * @param tagName name of the outermost XML tag
     * @return XML format
     */
    String getAsXml(
            String tagName )
    {
        StringBuilder buf = new StringBuilder( 256 );

        buf.append( "<" ).append( tagName ).append( ">\n" );

        if( theModuleName != null && theModuleName.length() > 0 ) {
            appendOpenTag( NAME_TAG, buf );
            buf.append( theModuleName );
            appendCloseTag( NAME_TAG, buf );
            buf.append( '\n' );
        }
        if( theModuleVersion != null && theModuleVersion.length() > 0 ) {
            appendOpenTag( VERSION_TAG, buf );
            buf.append( theModuleVersion );
            appendCloseTag( VERSION_TAG, buf );
            buf.append( '\n' );
        }
        if( theModuleBuildDate != null ) {
            appendOpenTag( BUILD_TIME_TAG, buf );
            buf.append( theDefaultDateFormat.format( theModuleBuildDate ));
            appendCloseTag( BUILD_TIME_TAG, buf );
            buf.append( '\n' );
        } else {
            appendOpenTag( BUILD_TIME_TAG, buf );
            buf.append( theDefaultDateFormat.format( new Date() )); // now
            appendCloseTag( BUILD_TIME_TAG, buf );
            buf.append( '\n' );
        }

        if( theModuleUserNames != null && !theModuleUserNames.isEmpty() ) {
            Iterator<String> iter = theModuleUserNames.keySet().iterator();
            while( iter.hasNext() ) {
                String locale = iter.next();
                String value  = theModuleUserNames.get( locale );

                if( locale != null && locale.length() > 0 ) {
                    buf.append( "<" ).append( USERNAME_TAG ).append( " locale=\"" );
                    buf.append( locale );
                    buf.append( "\">");
                } else {
                    appendOpenTag( USERNAME_TAG, buf );
                }
                buf.append( value );
                appendCloseTag( USERNAME_TAG, buf );
                buf.append( '\n' );
            }
        }

        if( theModuleUserDescriptions != null && !theModuleUserDescriptions.isEmpty() ) {
            Iterator<String> iter = theModuleUserDescriptions.keySet().iterator();
            while( iter.hasNext() ) {
                String locale = iter.next();
                String value  = theModuleUserDescriptions.get( locale );

                if( locale != null && locale.length() > 0 ) {
                    buf.append( "<" ).append( USERDESCRIPTION_TAG ).append( " locale=\"" );
                    buf.append( locale );
                    buf.append( "\">");
                } else {
                    appendOpenTag( USERDESCRIPTION_TAG, buf );
                }
                buf.append( value );
                appendCloseTag( USERDESCRIPTION_TAG, buf );
                buf.append( '\n' );
            }
        }

        String activateConfigRunXml = getActivateConfigRunAsXml();
        if( activateConfigRunXml != null ) {
            buf.append( activateConfigRunXml );
        }
        
        if( theModuleJars != null && theModuleJars.length > 0 ) {
            buf.append( "<" ).append( PROVIDES_TAG );
            if( theModuleJarsBaseUrl != null ) { // intentionally do not check for empty string
                buf.append( " baseurl=\"" );
                buf.append( theModuleJarsBaseUrl.toExternalForm() );
                buf.append( "\"" );
            }
            buf.append( ">\n" );
            for( int i=0 ; i<theModuleJars.length ; ++i ) {
                appendOpenTag( JAR_TAG, buf );
                buf.append( theModuleJars[i] );
                appendCloseTag( JAR_TAG, buf );
            }
            buf.append( '\n' );
            appendCloseTag( PROVIDES_TAG, buf );
        }

        if(    ( theBuildTimeModuleRequirements != null && theBuildTimeModuleRequirements.length > 0 )
            || ( theRunTimeModuleRequirements   != null && theRunTimeModuleRequirements.length   > 0 ))
        {
            appendOpenTag( DEPENDENCIES_TAG, buf );

            if( theBuildTimeModuleRequirements != null ) {
                for( int i=0 ; i<theBuildTimeModuleRequirements.length ; ++i ) {
                    buf.append( theBuildTimeModuleRequirements[i].getAsXml( "buildtime" ));
                    buf.append( '\n' );
                }
            }
            if( theRunTimeModuleRequirements != null ) {
                for( int i=0 ; i<theRunTimeModuleRequirements.length ; ++i ) {
                    buf.append( theRunTimeModuleRequirements[i].getAsXml( "runtime" ));
                    buf.append( '\n' );
                }
            }
            appendCloseTag( DEPENDENCIES_TAG, buf );
            buf.append( '\n' );
        }

        String supportedCapabilitiesXml = getSupportedCapabilitiesAsXml();
        if( supportedCapabilitiesXml != null ) {
            buf.append( supportedCapabilitiesXml );
            buf.append( '\n' );
        }

        if(    ( theLocalParameterDefaults != null && theLocalParameterDefaults.size() > 0 )
            || ( theLocalParameterValues   != null && theLocalParameterValues.size() > 0 ))
        {
            buf.append( ">\n" );
            if( theLocalParameterDefaults != null ) {
                Iterator<String> iter = theLocalParameterDefaults.keySet().iterator();
                while( iter.hasNext() ) {
                    String key = iter.next();
                    String val = theLocalParameterDefaults.get( key );
                    buf.append( "   <" ).append( PARAMETER_TAG ).append( " name=\"" ).append( key ).append( "\" ");
                    buf.append( "default =\"" ).append( val ).append( "\"/>\n" );
                }
            }
            if( theLocalParameterValues != null ) {
                Iterator<String> iter = theLocalParameterValues.keySet().iterator();
                while( iter.hasNext() ) {
                    String key = iter.next();
                    String val = theLocalParameterValues.get( key );
                    buf.append( "   <" ).append( PARAMETER_TAG ).append( " name=\"" ).append( key ).append( "\" ");
                    buf.append( "value =\"" ).append( val ).append( "\"/>\n" );
                }
            }
        }

        buf.append( "</" ).append( tagName ).append( ">\n" );
        return buf.toString();
    }

    /**
     * Helper method to construct N tabs.
     *
     * @param n the number of tabs to construct
     * @return StringBuffer with the tabs
     */
    protected static StringBuffer tabs(
            int n )
    {
        final String oneTab = "        ";
        StringBuffer ret = new StringBuffer( n*oneTab.length() );
        for( int i=0 ; i<n ; ++i ) {
            ret.append( oneTab );
        }
        return ret;
    }

    /**
     * Obtain the activate, config, and run information as XML, if any.
     *
     * @return the activate, config and run information as XML fragment, or null
     */
    abstract String getActivateConfigRunAsXml();
    
    /**
     * Obtain the supported capabilities as XML, if any.
     *
     * @return the supported capabilities as XML fragment, or null
     */
    abstract String getSupportedCapabilitiesAsXml();

    /**
     * The name of the module.
     */
    protected String theModuleName;

    /**
     * The version of the module.
     */
    protected String theModuleVersion;

    /**
     * The time when this Module was built.
     */
    protected Date theModuleBuildDate;

    /**
     * The user-visible names for this Module, keyed by the locale.
     */
    protected Map<String,String> theModuleUserNames;

    /**
     * The user-visible descriptions for this Module, keyed by the locale.
     */
    protected Map<String,String> theModuleUserDescriptions;

    /**
     * The requirements for other modules that we may have at build time.
     */
    protected ModuleRequirement [] theBuildTimeModuleRequirements;

    /**
     * The requirements for other modules that we may have at run time.
     */
    protected ModuleRequirement [] theRunTimeModuleRequirements;

    /**
     * The license of the module.
     */
    protected ModuleLicense theModuleLicense;

    /**
     * The set of JARs that this module provides.
     */
    protected String [] theModuleJars;

    /**
     * The base URL from which to download the Module JARs if needed.
     */
    protected URL theModuleJarsBaseUrl;

    /**
     * The parameter-value pairs that cannot be overriden by inherited parameter-value pairs.
     */
    protected Map<String,String> theLocalParameterValues;

    /**
     * The parameter-value pairs that may be overriden by inherited parameter-value pairs.
     */
    protected Map<String,String> theLocalParameterDefaults;
}
