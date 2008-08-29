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
import java.net.URL;
import java.util.Date;
import java.util.Map;

/**
 * This ModuleAdvertisement is for Models, aka Schemas.
 */
public class ModelModuleAdvertisement
        extends ModuleAdvertisement
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Factory method that constructs a ModuleAdvertisement from specified data.
     *
     * @param moduleName the programmatic name of the to-be-created Module
     * @param moduleVersion the version of the to-be-created Module
     * @param moduleUserNames the name shown to the user of the to-be-created Module, keyed by locale
     * @param moduleUserDescriptions the description shown to the user of the to-be-created Module, keyed by the locale
     * @param moduleBuildDate the time when this Module was built
     * @param license the license for the to-be-created Module
     * @param buildTimeModuleRequirements the ModuleRequirements of this Module at build time
     * @param runTimeModuleRequirements the ModuleRequirements of this Module at run time
     * @param moduleJarsBaseUrl the base URL from where to download the JARs of this Module
     * @param moduleJars names of the JAR files provided by this Module
     * @param localParameterValues the parameter-value paris of parameters that cannot be overridden by inherited values
     * @param localParameterDefaults the parameter-value paris of parameters that are overridden by inherited values
     * @return the created ModelModuleAdvertisement
     */
    public static ModelModuleAdvertisement create1(
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
        if( moduleName == null || moduleName.length() == 0 ) {
            throw new IllegalArgumentException( "ModelModule cannot have empty name" );
        }
        if( moduleJars == null || moduleJars.length == 0 ) {
            throw new IllegalArgumentException( "ModelModule must provide at least one jar" );
        }
        return new ModelModuleAdvertisement(
                moduleName,
                moduleVersion,
                moduleUserNames,
                moduleUserDescriptions,
                moduleBuildDate,
                license,
                buildTimeModuleRequirements,
                runTimeModuleRequirements,
                moduleJarsBaseUrl,
                moduleJars,
                localParameterValues,
                localParameterDefaults );
    }

    /**
      * Private constructor, use factory method.
      *
      * @param moduleName the programmatic name of the to-be-created Module
      * @param moduleVersion the version of the to-be-created Module
      * @param moduleUserNames the name shown to the user of the to-be-created Module, keyed by locale
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
    protected ModelModuleAdvertisement(
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
        super(  moduleName,
                moduleVersion,
                moduleUserNames,
                moduleUserDescriptions,
                moduleBuildDate,
                license,
                buildTimeModuleRequirements,
                runTimeModuleRequirements,
                moduleJarsBaseUrl,
                moduleJars,
                localParameterValues,
                localParameterDefaults );
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
    protected Module createModule(
            ModuleRegistry registry,
            File           moduleDirectory,
            File []        jars,
            ClassLoader    parentClassLoader )
    {
        return new ModelModule( this, registry, moduleDirectory, jars, parentClassLoader );
    }

    /**
     * Create XML that represents this ModuleAdvertisement.
     *
     * @return XML format
     */
    public String getAsXml()
    {
        return getAsXml( "modelmodule" );
    }

    /**
     * Obtain the activate, config, and run information as XML, if any.
     *
     * @return the activate, config and run information as XML fragment, or null
     */
    String getActivateConfigRunAsXml()
    {
        return null;
    }
    
    /**
     * Obtain the supported capabilities as XML, if any.
     *
     * @return the supported capabilities as XML fragment, or null
     */
    String getSupportedCapabilitiesAsXml()
    {
        return null;
    }

    /**
     * Return this object in string format for debugging.
     *
     * @return this object in string format
     */
    @Override
    public String toString()
    {
        StringBuffer ret = new StringBuffer( 100 );
        ret.append( "<" );
        ret.append( super.toString() );
        ret.append( "{ name: ");
        ret.append( theModuleName );
        ret.append( " }>" );
        return ret.toString();
    }

    /**
     * Create a String expression in the Java language that, when executed, creates
     * an instance of (a subclass of) ModuleAdvertisement that is equal to this
     * ModuleAdvertisement.
     *
     * @return the String expression
     */
    public String getJavaConstructorString(
            int tabs )
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "ModelModuleAdvertisement.create1(\n" );
        buf.append( tabs( tabs+1 ));
        buf.append( super.getJavaConstructorStringCommons( tabs+1 ) );
        buf.append( " )" );
        return buf.toString();
    }
}
