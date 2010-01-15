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
import java.util.Date;
import java.util.Map;

/**
 * This ModuleAdvertisement is for standard Modules. Standard Modules contain regular code.
 */
public class StandardModuleAdvertisement
        extends ModuleAdvertisement
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Factory method that constructs a ModuleAdvertisement from specified data.
     *
     * @param moduleName name of the StandardModuleAdvertisement to create
     * @param moduleVersion version of the StandardModuleAdvertisement to create
     * @param moduleUserNames user-displayable name of the StandardModuleAdvertisement to create, keyed by locale
     * @param moduleUserDescriptions the description shown to the user of the to-be-created Module, keyed by the locale
     * @param moduleBuildDate the time when this Module was built
     * @param license license of the StandardModuleAdvertisement to create
     * @param file the File that contains this ModuleAdvertisement, if available
     * @param buildTimeModuleRequirements ModuleRequirements of this Module at build-time
     * @param runTimeModuleRequirements ModuleRequirements of this Module at run-time
     * @param moduleJars JAR files provided by this Module
     * @param localParameterValues the parameter-value paris of parameters that cannot be overridden by inherited values
     * @param localParameterDefaults the parameter-value paris of parameters that are overridden by inherited values
     * @param capabilities capabilities in this Module
     * @param activationClassName name of the class contained in this Module that contains the Module's activation method
     * @param activationMethodName name of the activation method within class activationClassName
     * @param deactivationMethodName name of the deactivation method within class activationClassName
     * @param configurationClassName name of the class contained in this Module that contains the Module's configuration method
     * @param configurationMethodName name of the configuration method within class configurationClassName
     * @param runClassName name of the class contained in this Module that contains the Module's run method
     * @param runMethodName name of the run method within class runClassName
     * @return the newly created StandardModuleAdvertisement
     */
    public static StandardModuleAdvertisement create1(
            String               moduleName,
            String               moduleVersion,
            Map<String,String>   moduleUserNames,
            Map<String,String>   moduleUserDescriptions,
            Date                 moduleBuildDate,
            ModuleLicense        license,
            File                 file,
            ModuleRequirement [] buildTimeModuleRequirements,
            ModuleRequirement [] runTimeModuleRequirements,
            File              [] moduleJars,
            Map<String,String>   localParameterValues,
            Map<String,String>   localParameterDefaults,
            ModuleCapability  [] capabilities,
            String               activationClassName,
            String               activationMethodName,
            String               deactivationMethodName,
            String               configurationClassName,
            String               configurationMethodName,
            String               runClassName,
            String               runMethodName )
    {
        if( moduleName == null || moduleName.length() == 0 ) {
            throw new IllegalArgumentException( "StandardModule cannot have empty name" );
        }
        if( moduleJars == null || moduleJars.length == 0 ) {
            throw new IllegalArgumentException( "StandardModule must provide at least one jar" );
        }
        return new StandardModuleAdvertisement(
                moduleName,
                moduleVersion,
                moduleUserNames,
                moduleUserDescriptions,
                moduleBuildDate,
                license,
                file,
                buildTimeModuleRequirements,
                runTimeModuleRequirements,
                moduleJars,
                localParameterValues,
                localParameterDefaults,
                capabilities,
                activationClassName,
                activationMethodName,
                deactivationMethodName,
                configurationClassName,
                configurationMethodName,
                runClassName,
                runMethodName );
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
      * @param file the File that contains this ModuleAdvertisement, if available
      * @param buildTimeModuleRequirements the ModuleRequirements of this Module at build time
      * @param runTimeModuleRequirements the ModuleRequirements of this Module at run time
      * @param moduleJars JAR files provided by this Module
      * @param localParameterValues the parameter-value paris of parameters that cannot be overridden by inherited values
      * @param localParameterDefaults the parameter-value paris of parameters that are overridden by inherited values
      * @param capabilities capabilities in this Module
      * @param activationClassName name of the class contained in this Module that contains the Module's activation method
      * @param activationMethodName name of the activation method within class activationClassName
      * @param deactivationMethodName name of the deactivation method within class deactivationClassName
      * @param configurationClassName name of the class contained in this Module that contains the Module's configuration method
      * @param configurationMethodName name of the configuration method within class configurationClassName
      * @param runClassName name of the class contained in this Module that contains the Module's run method
      * @param runMethodName name of the run method within class runClassName
      */
    protected StandardModuleAdvertisement(
            String               moduleName,
            String               moduleVersion,
            Map<String,String>   moduleUserNames,
            Map<String,String>   moduleUserDescriptions,
            Date                 moduleBuildDate,
            ModuleLicense        license,
            File                 file,
            ModuleRequirement [] buildTimeModuleRequirements,
            ModuleRequirement [] runTimeModuleRequirements,
            File              [] moduleJars,
            Map<String,String>   localParameterValues,
            Map<String,String>   localParameterDefaults,
            ModuleCapability  [] capabilities,
            String               activationClassName,
            String               activationMethodName,
            String               deactivationMethodName,
            String               configurationClassName,
            String               configurationMethodName,
            String               runClassName,
            String               runMethodName )
    {
        super(  moduleName,
                moduleVersion,
                moduleUserNames,
                moduleUserDescriptions,
                moduleBuildDate,
                license,
                file,
                buildTimeModuleRequirements,
                runTimeModuleRequirements,
                moduleJars,
                localParameterValues,
                localParameterDefaults );

        theSupportedCapabilities   = capabilities;
        theActivationClassName     = activationClassName;
        theActivationMethodName    = activationMethodName;
        theDeactivationMethodName  = deactivationMethodName;
        theConfigurationClassName  = configurationClassName;
        theConfigurationMethodName = configurationMethodName;
        theRunClassName            = runClassName;
        theRunMethodName           = runMethodName;
    }

    /**
     * Obtain the capabilities of this Module.
     *
     * @return the set of capabilities of the Module described by this ModuleAdvertisement
     */
    public final ModuleCapability [] getSupportedCapabilities()
    {
        return theSupportedCapabilities;
    }

    /**
     * Obtain the name of the class whose activation method must be invoked before this Module
     * can be used. If this returns null, it means the Module has no activation method.
     *
     * @return name of the activation class contained in this Module, or null if none
     */
    public final String getActivationClassName()
    {
        return theActivationClassName;
    }

    /**
     * Obtain the name of the method in the activation class that must be invoked before
     * this Module can be used.
     *
     * @return name of the method inside getActivationClassName(), or null if none
     */
    public final String getActivationMethodName()
    {
        return theActivationMethodName;
    }

    /**
     * Obtain the name of the method in the activation class that must be invoked after
     * this Module has been used.
     *
     * @return name of the method inside getDeactivationClassName(), or null if none
     */
    public final String getDeactivationMethodName()
    {
        return theDeactivationMethodName;
    }

    /**
     * Obtain the name of the class whose configure method must be invoked before this Module
     * can be used. If this returns null, it means the Module has no configuration method.
     *
     * @return name of the configuration class contained in this Module, or null if none
     */
    public final String getConfigurationClassName()
    {
        return theConfigurationClassName;
    }

    /**
     * Obtain the name of the method in the configuration class that must be invoked before
     * this Module can be used.
     *
     * @return name of the method inside getConfigurationClassName(), or null if none
     */
    public final String getConfigurationMethodName()
    {
        return theConfigurationMethodName;
    }

    /**
     * Obtain the name of the class that contains the Module's run method.
     * If this returns null, it means the Module has no run method.
     *
     * @return name of the run class contained in this Module, or null if none
     */
    public final String getRunClassName()
    {
        return theRunClassName;
    }

    /**
     * Obtain the name of the method in the run class that needs to be invoked in order
     * to run this Module.
     *
     * @return name of the method inside getRunClassName(), or null if none
     */
    public final String getRunMethodName()
    {
        return theRunMethodName;
    }

    /**
     * Find the capabilities of this Module supporting the provided interface.
     *
     * @param interfaceName name of an interface contained in this Module or a parent Module
     * @return the ModuleCapabilities providing this interface
     */
    public final ModuleCapability [] findCapabilitiesByInterface(
            String interfaceName )
    {
        ModuleCapability [] almost = new ModuleCapability[ theSupportedCapabilities.length ];
        
        int count = 0;
        for( int i=0 ; i<theSupportedCapabilities.length ; ++i ) {
            String [] supportedInterfaces = theSupportedCapabilities[i].getInterfaceNames();
            for( int j=0 ; j<supportedInterfaces.length ; ++j ) {
                if (interfaceName.equals(supportedInterfaces[j])) {
                    almost[count++] = theSupportedCapabilities[i];
                }
            }
        }
        ModuleCapability [] ret;
        if( count < almost.length ) {
            ret = new ModuleCapability[ count ];
            System.arraycopy( almost, 0, ret, 0, count );
        } else {
            ret = almost;
        }
        return ret;
    }

    /**
     * Find a capability of this Module supporting all of the provided interfaces. Return null if none.
     * Return the first one if multiple ones are found.
     *
     * @param interfaceNames names of interfaces contained in this Module or a parent Module
     * @return a ModuleCapability providing all of these interfaces, or null if none is available or known
     */
    public final ModuleCapability findCapabilityByInterfaces(
            String [] interfaceNames )
    {
        outer: for( int i=0 ; i<theSupportedCapabilities.length ; ++i ) {
            String [] supportedInterfaces = theSupportedCapabilities[i].getInterfaceNames();

            for( int j=0 ; j<interfaceNames.length ; ++j ) {
                boolean found2 = false;
                for( int k=0 ; k<supportedInterfaces.length ; ++k ) {
                    if( interfaceNames[j].equals( supportedInterfaces[k] )) {
                        found2 = true;
                        break;
                    }
                }
                if( !found2 ) {
                    continue outer;
                }
            }
            return theSupportedCapabilities[i]; // I think this is right -- the "continue outer" provides the other path
        }
        return null;
    }

    /**
     * Create a Module from this ModuleAdvertisement. This is not supposed to be invoked
     * by the user of this framework.
     *
     * @param registry the ModuleRegistry in which the Module will be created
     * @param jars the set of JAR files for this Module
     * @param loader the ClassLoader of our parent Module
     * @return Module the newly created Module
     */
    protected Module createModule(
            ModuleRegistry registry,
            File []        jars,
            ClassLoader    loader )
    {
        return new StandardModule( this, registry, jars, loader );
    }

    /**
     * Create XML that represents this ModuleAdvertisement.
     *
     * @return XML format
     */
    public String getAsXml()
    {
        return getAsXml( "standardmodule" );
    }

    /**
     * Obtain the activate, config, and run information as XML, if any.
     *
     * @return the activate, config and run information as XML fragment, or null
     */
    String getActivateConfigRunAsXml()
    {
        StringBuffer buf = new StringBuffer( 64 ); // fudge
        if( theActivationClassName != null && theActivationClassName.length() > 0 ) {
            buf.append( "<activationclass>" );
            buf.append( theActivationClassName );
            buf.append( "</activationclass>\n" );
        }

        if( theConfigurationClassName != null && theConfigurationClassName.length() > 0 ) {
            buf.append( "<configurationclass>" );
            buf.append( theConfigurationClassName );
            buf.append( "</configurationclass>\n" );
        }

        if( theRunClassName != null && theRunClassName.length() > 0 ) {
            buf.append( "<runclass>" );
            buf.append( theRunClassName );
            buf.append( "</runclass>\n" );
        }
        if( buf.length() > 0 ) {
            return buf.toString();
        } else {
            return null;
        }
    }

    /**
     * Obtain the supported capabilities as XML, if any.
     *
     * @return the supported capabilities as XML fragment, or null
     */
    String getSupportedCapabilitiesAsXml()
    {
        if( theSupportedCapabilities != null ) {
            StringBuffer buf = new StringBuffer( theSupportedCapabilities.length * 64 ); // fudge

            for( int i=0 ; i<theSupportedCapabilities.length ; ++i ) {
                buf.append( " <capability>" );
                if( theSupportedCapabilities[i].theInterfaceNames != null ) {
                    for( int j=0 ; j<theSupportedCapabilities[i].theInterfaceNames.length ; ++j ) {
                        buf.append( "  <interface>" );
                        buf.append( theSupportedCapabilities[i].theInterfaceNames[j] );
                        buf.append( "</interface>\n" );
                    }
                }
                if( theSupportedCapabilities[i].theImplementationName != null && theSupportedCapabilities[i].theImplementationName.length() > 0 ) {
                    buf.append( "  <implementation>" );
                    buf.append( theSupportedCapabilities[i].theImplementationName );
                    buf.append( "</implementation>\n" );
                }
                if( theSupportedCapabilities[i].theArgumentCombinations != null ) {
                    for( int j=0 ; j<theSupportedCapabilities[i].theArgumentCombinations.length ; ++j ) {
                        buf.append( "  <argumentcombination>\n" );
                        if( theSupportedCapabilities[i].theArgumentCombinations[j].theArguments != null ) {
                            for( int k=0 ; k<theSupportedCapabilities[i].theArgumentCombinations[j].theArguments.length ; ++k ) {
                                buf.append( "   <arg>" );
                                buf.append( theSupportedCapabilities[i].theArgumentCombinations[j].theArguments[k] );
                                buf.append( "</arg>\n" );
                            }
                        }
                        buf.append( "  </argumentcombination>\n" );
                    }
                }
                buf.append( " </capability>\n" );
            }
            return buf.toString();
        } else {
            return null;
        }
    }

    /**
     * For debugging.
     *
     * @return string representation of this object
     */
    @Override
    public String toString()
    {
        StringBuffer ret = new StringBuffer( 100 );
        ret.append( "<" );
        ret.append( super.toString() );
        ret.append( "{ name: ");
        ret.append( theModuleName );
        ret.append( ", actClass: " );
        ret.append( theActivationClassName );
        ret.append( ", actMethod: " );
        ret.append( theActivationMethodName );
        ret.append( ", deactMethod: " );
        ret.append( theDeactivationMethodName );
        ret.append( ", configClass: " );
        ret.append( theConfigurationClassName );
        ret.append( ", configMethod: " );
        ret.append( theConfigurationMethodName );
        ret.append( ", runClass: " );
        ret.append( theRunClassName );
        ret.append( ", runMethod: " );
        ret.append( theRunMethodName );
        ret.append( ", caps: { " ); // do not use ArrayHelper, not in the bootloader
        for( int i=0 ; i<theSupportedCapabilities.length ; ++i ) {
            if( i>0 ) {
                ret.append( ", " );
            }
            ret.append( theSupportedCapabilities[i] );
        }
        ret.append( " } }>" );
        return ret.toString();
    }

    /**
     * Convert content to a printable String. Similarly to toString, but not using
     * our funny hierarchical syntax.
     *
     * @return string representation of this object
     */
    @Override
    public String contentToString()
    {
        StringBuffer buf = new StringBuffer( 100 );
        buf.append( super.contentToString() );

        for( int i=0 ; i<theSupportedCapabilities.length ; ++i ) {
            buf.append( "\n    capability: " );
            String [] intf = theSupportedCapabilities[i].getInterfaceNames();
            for( int j=0 ; j<intf.length ; ++j ) {
                buf.append( "\n        interface: " );
                buf.append( intf[j] );
            }
            buf.append( "\n        implementation: " );
            buf.append( theSupportedCapabilities[i].getImplementationName() );
        }
        return buf.toString();
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
        buf.append( "StandardModuleAdvertisement.create1(\n" );
        buf.append( tabs( tabs+1 ));
        buf.append( super.getJavaConstructorStringCommons( tabs+1 ) );
        buf.append( ",\n" );
        buf.append( tabs( tabs+1 ));
        if( theSupportedCapabilities == null ) {
            buf.append( "null" );
        } else {
            buf.append( "new ModuleCapability[] {" );
            for( int i=0 ; i<theSupportedCapabilities.length ; ++i ) {
                if( i>0 ) {
                    buf.append( ",\n" );
                } else {
                    buf.append( "\n" );
                }
                buf.append( theSupportedCapabilities[i].getJavaConstructorString( tabs+2 ));
            }
            buf.append( "\n" );
            buf.append( tabs( tabs+1 ));
            buf.append( "}" );
        }
        buf.append( ",\n" );
        buf.append( tabs( tabs+1 ));
        buf.append( getJavaConstructorStringString( tabs+1, theActivationClassName ));
        buf.append( ",\n" );
        buf.append( tabs( tabs+1 ));
        buf.append( getJavaConstructorStringString( tabs+1, theActivationMethodName ));
        buf.append( ",\n" );
        buf.append( tabs( tabs+1 ));
        buf.append( getJavaConstructorStringString( tabs+1, theDeactivationMethodName ));
        buf.append( ",\n" );
        buf.append( tabs( tabs+1 ));
        buf.append( getJavaConstructorStringString( tabs+1, theConfigurationClassName ));
        buf.append( ",\n" );
        buf.append( tabs( tabs+1 ));
        buf.append( getJavaConstructorStringString( tabs+1, theConfigurationMethodName ));
        buf.append( ",\n" );
        buf.append( tabs( tabs+1 ));
        buf.append( getJavaConstructorStringString( tabs+1, theRunClassName ));
        buf.append( ",\n" );
        buf.append( tabs( tabs+1 ));
        buf.append( getJavaConstructorStringString( tabs+1, theRunMethodName ));
        buf.append( " )" );
        return buf.toString();
    }

    /**
     * The capabilities that this Module is supporting.
     */
    protected ModuleCapability [] theSupportedCapabilities;

    /**
     * The name of the class in this Module which provides a method to
     * activate this Module.
     */
    protected String theActivationClassName;

    /**
     * The name of the method in theActivationClassName which
     * activates this Module.
     */
    protected String theActivationMethodName = "activate";

    /**
     * The name of the method in theActivationClassName which
     * deactivates this Module.
     */
    protected String theDeactivationMethodName = "deactivate";

    /**
     * The name of the class in this Module which provides a method to
     * configure this Module.
     */
    protected String theConfigurationClassName;

    /**
     * The name of the method in theConfigurationClassName which
     * configures this Module.
     */
    protected String theConfigurationMethodName = "configure";

    /**
     * The name of the class in this Module which provides a method to
     * run this Module.
     */
    protected String theRunClassName;

    /**
     * The name of the method in theRunClassName which
     * runs this Module.
     */
    protected String theRunMethodName = "main";
}
