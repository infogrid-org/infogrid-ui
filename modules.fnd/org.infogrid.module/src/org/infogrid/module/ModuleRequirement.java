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

import java.io.*;
import java.util.*;

/**
 * This describes the requirement for a Module that another Module depends on.
 */
public class ModuleRequirement
        implements
            Serializable
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
      * Factory method.
      *
      * @param requiredModuleName the name of the required Module, in any version
      * @return the created ModuleRequirement
      */
    public static ModuleRequirement create1(
            String requiredModuleName )
    {
        return new ModuleRequirement( requiredModuleName, null, null, null );
    }

    /**
      * Factory method.
      *
      * @param requiredModuleName the name of the required Module
      * @param requiredModuleVersion the version of the required Module, null if any
      * @return the created ModuleRequirement
      */
    public static ModuleRequirement create1(
            String requiredModuleName,
            String requiredModuleVersion )
    {
        return new ModuleRequirement( requiredModuleName, requiredModuleVersion, null, null );
    }

    /**
      * Factory method.
      *
      * @param requiredModuleName the name of the required Module, in any version
      * @param localParameterValues the parameter-value paris of parameters that cannot be overridden by inherited values
      * @param localParameterDefaults the parameter-value paris of parameters that are overridden by inherited values
      * @return the created ModuleRequirement
      */
    public static ModuleRequirement create1(
            String             requiredModuleName,
            Map<String,String> localParameterValues,
            Map<String,String> localParameterDefaults )
    {
        return new ModuleRequirement( requiredModuleName, null, localParameterValues, localParameterDefaults );
    }

    /**
      * Factory method.
      *
      * @param requiredModuleName the name of the required Module
      * @param requiredModuleVersion the version of the required Module, null if any
      * @param localParameterValues the parameter-value paris of parameters that cannot be overridden by inherited values
      * @param localParameterDefaults the parameter-value paris of parameters that are overridden by inherited values
      * @return the created ModuleRequirement
      */
    public static ModuleRequirement create1(
            String             requiredModuleName,
            String             requiredModuleVersion,
            Map<String,String> localParameterValues,
            Map<String,String> localParameterDefaults )
    {
        return new ModuleRequirement( requiredModuleName, requiredModuleVersion, localParameterValues, localParameterDefaults );
    }

    /**
      * Construct one.
      *
      * @param requiredModuleName the name of the required Module
      * @param requiredModuleVersion the version of the required Module, null if any
      * @param localParameterValues the parameter-value paris of parameters that cannot be overridden by inherited values
      * @param localParameterDefaults the parameter-value paris of parameters that are overridden by inherited values
      */
    protected ModuleRequirement(
            String             requiredModuleName,
            String             requiredModuleVersion,
            Map<String,String> localParameterValues,
            Map<String,String> localParameterDefaults )
    {
        theRequiredModuleName     = requiredModuleName;
        theRequiredModuleVersion  = requiredModuleVersion;
        theLocalParameterValues   = localParameterValues;
        theLocalParameterDefaults = localParameterDefaults;
    }

    /**
      * Obtain the name of the Module that we require.
      *
      * @return the name of the Module that we require
      */
    public final String getRequiredModuleName()
    {
        return theRequiredModuleName;
    }

    /**
     * Obtain the version of the Module that we required.
     *
     * @return the version of the Module that we require
     */
    public final String getRequiredModuleVersion()
    {
        return theRequiredModuleVersion;
    }

    /**
     * Obtain the parameter-value pairs for the Module which resolves this ModuleRequirement
     * that are provided locally by this Module and that cannot be overridden by
     * parameter-value pairs inherited from higher-level Modules.
     *
     * @return the parameter-value pairs, or null
     */
    public final Map<String,String> getLocalParameterValues()
    {
        return theLocalParameterValues;
    }

    /**
     * Obtain the parameter-value pairs for the Module which resolves this ModuleRequirement
     * that are provided locally by this Module and that may be overridden by
     * parameter-value pairs inherited from higher-level Modules.
     *
     * @return the parameter-value pairs, or null
     */
    public final Map<String,String> getLocalParameterDefaults()
    {
        return theLocalParameterDefaults;
    }

    /**
     * Obtain a string representation of this object, for debugging purposes.
     *
     * @return string representation of this object
     */
    @Override
    public String toString()
    {
        return getClass().getName() + ": " + theRequiredModuleName;
    }

    /**
     * Create a String expression in the Java language that, when executed, creates
     * an instance of ModuleRequirement that is equal to this ModuleRequirement.
     *
     * @param tabs the number of tabs to insert
     * @return the String expression
     */
    public String getJavaConstructorString(
            int tabs )
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "ModuleRequirement.create1(\n" );
        buf.append( ModuleAdvertisement.tabs( tabs+1 ));
        buf.append( "\"" );
        buf.append( theRequiredModuleName );
        buf.append( "\"" );
        if( theRequiredModuleVersion != null ) {
            buf.append( ",\n" );
            buf.append( ModuleAdvertisement.tabs( tabs+1 ));
            buf.append( "\"" );
            buf.append( theRequiredModuleVersion );
            buf.append( "\"" );
        }
        if( theLocalParameterValues != null || theLocalParameterDefaults != null ) {
            buf.append( ",\n" );
            buf.append( ModuleAdvertisement.tabs( tabs+1 ));
            buf.append( ModuleAdvertisement.getJavaConstructorStringMap( tabs+1, theLocalParameterValues ));
            buf.append( ",\n" );
            buf.append( ModuleAdvertisement.tabs( tabs+1 ));
            buf.append( ModuleAdvertisement.getJavaConstructorStringMap( tabs+1, theLocalParameterDefaults ));
        }
        buf.append( " )" );
        return buf.toString();
    }

    /**
     * Create an XML fragment that represents this ModuleRequirement.
     *
     * @param mode the mode in which this ModuleRequirement applies
     * @return the String containing the XML fragment
     */
    public String getAsXml(
            String mode )
    {
        StringBuffer buf = new StringBuffer( 128 );
        buf.append( "  <requires name=\"" );
        buf.append( getRequiredModuleName() );
        if( getRequiredModuleVersion() != null && getRequiredModuleVersion().length() > 0 ) {
            buf.append( "\" version=\"" );
            buf.append( getRequiredModuleVersion() );
        }
        if( mode != null && mode.length() > 0 ) {
            buf.append( "\" mode=\"" );
            buf.append( mode );
        }

        buf.append( "\"" );
        if(    ( theLocalParameterDefaults != null && theLocalParameterDefaults.size() > 0 )
            || ( theLocalParameterValues   != null && theLocalParameterValues.size() > 0 ))
        {
            buf.append( ">\n" );
            if( theLocalParameterDefaults != null ) {
                Iterator<String> iter = theLocalParameterDefaults.keySet().iterator();
                while( iter.hasNext() ) {
                    String key = iter.next();
                    String val = theLocalParameterDefaults.get( key );
                    buf.append( "   <parameter name=\"" ).append( key ).append( "\" ");
                    buf.append( "default =\"" ).append( val ).append( "\"/>\n" );
                }
            }
            if( theLocalParameterValues != null ) {
                Iterator<String> iter = theLocalParameterValues.keySet().iterator();
                while( iter.hasNext() ) {
                    String key = iter.next();
                    String val = theLocalParameterValues.get( key );
                    buf.append( "   <parameter name=\"" ).append( key ).append( "\" ");
                    buf.append( "value =\"" ).append( val ).append( "\"/>\n" );
                }
            }
            buf.append( "  </requires>\n" );
        } else {
            buf.append( "/>\n" );
        }
        
        return buf.toString();
    }

    /**
     * The name of the required Module.
     */
    protected String theRequiredModuleName;

    /**
     * The version of the required Module.
     */
    protected String theRequiredModuleVersion;

    /**
     * The parameter-value pairs that cannot be overriden by inherited parameter-value pairs.
     */
    protected Map<String,String> theLocalParameterValues;

    /**
     * The parameter-value pairs that may be overriden by inherited parameter-value pairs.
     */
    protected Map<String,String> theLocalParameterDefaults;
}
