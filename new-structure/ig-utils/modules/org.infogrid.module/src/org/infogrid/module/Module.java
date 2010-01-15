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
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A software Module in the Module Framework. More concrete suclasses
 * represent particular types of software Modules.
 */
public abstract class Module
{
    /**
     * Protected constructor.
     *
     * @param registry the registry of Modules in which we try to find dependent Modules
     * @param moduleJars the JAR files belonging to this Module
     * @param parentClassLoader the class loader of our parent Module
     */
    protected Module(
            ModuleRegistry      registry,
            File []             moduleJars,
            ClassLoader         parentClassLoader )
    {
        theRegistry            = registry;
        theModuleJars          = moduleJars;
        theParentClassLoader   = parentClassLoader;
    }

    /**
     * Obtain the name of this Module. It is the same as the name of its ModuleAdvertisement.
     *
     * @return the name of this Module
     */
    public final String getModuleName()
    {
        return getModuleAdvertisement().getModuleName();
    }

    /**
     * Obtain the version of this Module. It is the same as the version of its ModuleAdvertisement.
     *
     * @return the version of this Module
     */
    public final String getModuleVersion()
    {
        return getModuleAdvertisement().getModuleVersion();
    }

    /**
      * Obtain the ModuleAdvertisement for this Module.
      *
      * @return the ModuleAdvertisement for this Module
      */
    public abstract ModuleAdvertisement getModuleAdvertisement();

    /**
     * Obtain the ModuleRegistry in which we are registered.
     *
     * @return the ModuleRegistry in which we are registered
     */
    public final ModuleRegistry getModuleRegistry()
    {
        return theRegistry;
    }

    /**
     * Obtain a ClassLoader that knows how to access the JARs belonging to this Module.
     * This ClassLoader will delegate to the ClassLoader of the parent Module.
     *
     * @return this Module's ClassLoader
     * @throws MalformedURLException thrown if one of the URLs identifying the Module's JAR files is malformed
     */
    public synchronized final ClassLoader getClassLoader()
        throws
            MalformedURLException
    {
        if( theClassLoader == null ) {
            if( theRegistry.getSoftwareInstallation().useModuleClassLoaders() ) {
                try {
                    theClassLoader = new ModuleClassLoader( this, theParentClassLoader );
                } catch( ModuleNotFoundException ex ) {
                    ex.printStackTrace();
                } catch( ModuleResolutionException ex ) {
                    ex.printStackTrace();
                }
            } else {
                theClassLoader = theParentClassLoader;
            }
        }

        return theClassLoader;
    }

    /**
     * Obtain the JAR files provided by this Module.
     *
     * @return the JAR files provided by this Module
     */
    public final File [] getModuleJars()
    {
        return theModuleJars;
    }

    /**
     * Determine whether this Module is active. A Module is active if it has been
     * activated and not deactivated afterwards.
     *
     * @return if true, this Module is active
     */
    public final boolean isActive()
    {
        return theActivationCount > 0 ;
    }

    /**
     * This recursively activates this Module. First, this method recursively activates all Modules
     * that it this Module depends on, and then it activates itself.
     *
     * @return a context object that is Module-specific, or null if none
     * @throws ModuleResolutionException thrown if a dependent Module cannot be resolved
     * @throws ModuleNotFoundException thrown if a dependent Module cannot be found
     * @throws ModuleActivationException thrown if this Module, or a dependent Module could not be activated
     */
    public final Object activateRecursively()
        throws
            ModuleResolutionException,
            ModuleNotFoundException,
            ModuleActivationException
    {
        return activateRecursively( getDefaultModuleActivator() );
    }

    /**
     * This recursively activates this Module. First, this method recursively activates all Modules
     * that it this Module depends on, and then it activates itself.
     *
     * @param activator a ModuleActivator instance that knows how to activate this Module
     * @return a context object that is Module-specific, or null if none
     * @throws ModuleResolutionException thrown if a dependent Module cannot be resolved
     * @throws ModuleNotFoundException thrown if a dependent Module cannot be found
     * @throws ModuleActivationException thrown if this Module, or a dependent Module could not be activated
     */
    public final Object activateRecursively(
            ModuleActivator activator )
        throws
            ModuleResolutionException,
            ModuleNotFoundException,
            ModuleActivationException
    {
        if( theActivationCount == 0 ) {
            boolean success = false;
            try {
                ModuleErrorHandler.informModuleActivateRecursivelyStart( this );

                Module [] dependencies            = theRegistry.determineDependencies( this );
                Object [] dependentContextObjects = new Object[ dependencies.length ];

                for( int i=0 ; i<dependencies.length ; ++i ) {
                    ModuleActivator childActivator = activator.dependentModuleActivator( dependencies[i] );
                    try {
                        dependentContextObjects[i] = dependencies[i].activateRecursively( childActivator ); // FIXME? Arguments?
                    } catch( Exception ex ) {
                        throw new ModuleActivationException( getModuleAdvertisement(), ex );
                    }
                }
                theContextObject = activator.activate( dependencies, dependentContextObjects );
                // this may throw an exception
                
                success = true;

            } finally {
                if( success ) {
                    ModuleErrorHandler.informModuleActivateRecursivelySucceeded( this );
                } else {
                    ModuleErrorHandler.informModuleActivateRecursivelyFailed( this );
                }
                
            }
        }
        ++theActivationCount;
        return theContextObject;
    }

    /**
     * This recursively deactivates this Module. First, this method deactivates itself, and then
     * it recursively activates all Modules that this Module depends on
     *
     * @throws ModuleResolutionException thrown if a dependent Module cannot be resolved
     * @throws ModuleNotFoundException thrown if a dependent Module cannot be found
     * @throws ModuleActivationException throws if the Module could not be activated
     */
    public final void deactivateRecursively()
        throws
            ModuleResolutionException,
            ModuleNotFoundException,
            ModuleActivationException
    {
        deactivateRecursively( getDefaultModuleActivator() );
    }

    /**
     * This recursively deactivates this Module. First, this method deactivates itself, and then
     * it recursively activates all Modules that this Module depends on
     *
     * @param activator a ModuleActivator instance that knows how to deactivate instead of the default one specified in the ModuleAdvertisement
     * @throws ModuleResolutionException thrown if a dependent Module cannot be resolved
     * @throws ModuleNotFoundException thrown if a dependent Module cannot be found
     * @throws ModuleActivationException throws if the Module could not be activated
     */
    public final void deactivateRecursively(
            ModuleActivator activator )
        throws
            ModuleResolutionException,
            ModuleNotFoundException,
            ModuleActivationException
    {
        --theActivationCount;
        if( theActivationCount == 0 ) {
            Module [] dependencies = theRegistry.determineDependencies( this );

            activator.deactivate( dependencies );

            for( int i=0 ; i<dependencies.length ; ++i ) {
                ModuleActivator childDeactivator = activator.dependentModuleActivator( dependencies[i] );
                dependencies[i].deactivateRecursively( childDeactivator );
            }
            // this may throw an exception
        }
    }

    /**
     * This recursively configures this Module. First, this method recursively configures all Modules
     * that it this Module depends on, and then it configures itself.
     *
     * @param parameters the set of parameters for this Module
     * @param whereParametersSpecifiedMap maps which Modules specified each parameter
     * @throws ModuleResolutionException thrown if a dependent Module cannot be resolved
     * @throws ModuleNotFoundException thrown if a dependent Module cannot be found
     * @throws ModuleConfigurationException thrown if this Module could not be configured
     */
    public final void configureRecursively(
            Map<String,? extends Object> parameters,
            Map<String,Module>           whereParametersSpecifiedMap )
        throws
            ModuleResolutionException,
            ModuleNotFoundException,
            ModuleConfigurationException
    {
        if( !isConfigured ) {
            Module []            dependencies = theRegistry.determineDependencies( this );
            ModuleAdvertisement  moduleAd     = getModuleAdvertisement();
            ModuleRequirement [] requirements = moduleAd.getRunTimeModuleRequirements();

            Map<String,? extends Object> localParameters = merge(
                    parameters,
                    moduleAd.getLocalParameterValues(),
                    moduleAd.getLocalParameterDefaults() );
            Map<String,Module> localWhereParametersSpecifiedMap = merge(
                    whereParametersSpecifiedMap,
                    moduleAd.getLocalParameterValues()   != null ? moduleAd.getLocalParameterValues().keySet()   : null,
                    moduleAd.getLocalParameterDefaults() != null ? moduleAd.getLocalParameterDefaults().keySet() : null );

            for( int i=0 ; i<dependencies.length ; ++i ) {
                Map<String,? extends Object> childParameters = merge(
                        localParameters,
                        requirements[i].getLocalParameterValues(),
                        requirements[i].getLocalParameterDefaults() );
                Map<String,Module> childWhereParametersSpecifiedMap = merge(
                        localWhereParametersSpecifiedMap,
                        requirements[i].getLocalParameterValues()   != null ? requirements[i].getLocalParameterValues().keySet()   : null,
                        requirements[i].getLocalParameterDefaults() != null ? requirements[i].getLocalParameterDefaults().keySet() : null );

                dependencies[i].configureRecursively( childParameters, childWhereParametersSpecifiedMap );
            }
            configure( localParameters, localWhereParametersSpecifiedMap );

            isConfigured = true;
        }
    }

    /**
     * Configure this Module. Do not call this directly, call configureRecursively() instead.
     *
     * @param parameters the set of parameters for this Module
     * @param whereParametersSpecifiedMap maps which Modules specified each parameter
     * @throws ModuleConfigurationException thrown if this Module could not be configured
     */
    protected abstract void configure(
            Map<String,? extends Object> parameters,
            Map<String,Module>           whereParametersSpecifiedMap )
        throws
            ModuleConfigurationException;

    /**
     * Run this Module as a root Module.
     *
     * @param arguments arguments to run, similar to the arguments of a standard main(...) method
     * @throws ClassNotFoundException thrown if the specified run class cannot be found
     * @throws StandardModuleRunException thrown if the specified run method threw an Exception
     * @throws NoRunMethodException thrown if a suitable run method cannot be found
     * @throws InvocationTargetException thrown if the run method throws an Exception
     */
    public final void run(
            String [] arguments )
        throws
            ClassNotFoundException,
            StandardModuleRunException,
            NoRunMethodException,
            InvocationTargetException
    {
        run( null, null, arguments );
    }

    /**
     * Run this Module as a root Module.
     *
     * @param overriddenRunClassName optional name of the class to run instead of the default one specified in the ModuleAdvertisement
     * @param overriddenRunMethodName optional name of the method in the class to run instead of the default one specified in the ModuleAdvertisement
     * @param arguments arguments to run, similar to the arguments of a standard main(...) method
     * @throws ClassNotFoundException thrown if the specified run class cannot be found
     * @throws StandardModuleRunException thrown if the specified run method threw an Exception
     * @throws NoRunMethodException thrown if a suitable run method cannot be found
     * @throws InvocationTargetException thrown if the run method throws an Exception
     */
    public abstract void run(
            String    overriddenRunClassName,
            String    overriddenRunMethodName,
            String [] arguments )
        throws
            ClassNotFoundException,
            StandardModuleRunException,
            NoRunMethodException,
            InvocationTargetException;

    /**
     * Obtain a ModuleActivator for the Module that this ModuleAdvertisement corresponds to,
     * corresponding to the activation parameters given in the ModuleAdvertisement.
     * 
     * @return the ModuleActivator
     */
    public abstract ModuleActivator getDefaultModuleActivator();

    /**
     * Determine Module equality. Two Modules are the same if they have the same
     * Module Advertisement.
     *
     * @param other other object to compare this Module to
     * @return returns true if the objects are the same
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( other instanceof Module ) {
            return getModuleAdvertisement().equals( ((Module)other).getModuleAdvertisement() );
        }
        return false;
    }

    /**
     * Determine hash code. We use the hash code of our ModuleAdvertisement.
     *
     * @return the hash code
     */
    @Override
    public int hashCode()
    {
        return getModuleAdvertisement().hashCode();
    }

    /**
     * For debugging.
     *
     * @return string representation of this object
     */
    @Override
    public String toString()
    {
        return getClass().getName() + ": " + getModuleAdvertisement().getModuleName();
    }

    /**
     * A helper method to create a new Map from a base map, an overriding
     * parameter-value map and an optional parameter-value map.
     *
     * @param parent we take our content from this Map
     * @param overrides the content of this Map overrides everything else
     * @param defaults the content of this Map is used if nothing else matches
     * @return the merged Map
     * @param <T1> key type of the Map
     * @param <T2> value type of the Map
     */
    private static <T1,T2> Map<T1,? extends T2> merge(
            Map<T1,? extends T2> parent,
            Map<T1,? extends T2> overrides,
            Map<T1,? extends T2> defaults )
    {
        // return, unchanged, if there is only one
        if( parent == null ) {
            if( overrides == null ) {
                return defaults;
            } else if( defaults == null ) {
                return overrides;
            } else {
                HashMap<T1,T2> ret = new HashMap<T1,T2>();
                ret.putAll( defaults );
                ret.putAll( overrides );
                return ret;
            }
        } else if( overrides == null ) {
            if( defaults == null ) {
                return parent;
            } else {
                HashMap<T1,T2> ret = new HashMap<T1,T2>();
                ret.putAll( defaults );
                ret.putAll( parent );
                return ret;
            }
        } else if( defaults == null ) {
            HashMap<T1,T2> ret = new HashMap<T1,T2>();
            ret.putAll( parent );
            ret.putAll( overrides );
            return ret;
        } else {
            HashMap<T1,T2> ret = new HashMap<T1,T2>();
            ret.putAll( defaults );
            ret.putAll( parent );
            ret.putAll( overrides );
            return ret;
        }
        // return new MergeMap( parent, overrides, defaults );
    }

    /**
     * Helper method to create a new Map from a base map, a set of keys and
     * an overriding set of keys, the latter two of which are supposed to
     * reference this Module.
     */
    private Map<String,Module> merge(
            Map<String,Module> parent,
            Set<String>        overrides,
            Set<String>        defaults )
    {
        Map<String,Module> ret;

        if( parent == null ) {
            if( overrides == null ) {
                if( defaults == null ) {
                    ret = null;
                } else {
                    ret = new HashMap<String,Module>();
                    for( String name : defaults ) {
                        ret.put( name, this );
                    }
                }
            } else if( defaults == null ) {
                ret = new HashMap<String,Module>();
                for( String name : overrides ) {
                    ret.put( name, this );
                }
            } else {
                ret = new HashMap<String,Module>();
                for( String name : defaults ) {
                    ret.put( name, this );
                }
                for( String name : overrides ) {
                    ret.put( name, this );
                }
            }
        } else {
            ret = new HashMap<String,Module>();
            ret.putAll( parent );
            if( defaults != null ) {
                for( String name : defaults ) {
                    ret.put( name, this );
                }
            }
            if( overrides != null ) {
                for( String name : overrides ) {
                    ret.put( name, this );
                }
            }
        }
        return ret;
    }

    /**
     * The ModuleRegistry in which this Module is registered.
     */
    protected ModuleRegistry theRegistry;

    /**
     * The JAR files that contain the code for this Module.
     */
    protected File [] theModuleJars;

    /**
     * This Module's ClassLoader. Allocated as needed.
     */
    protected ClassLoader theClassLoader = null;

    /**
     * The parent ClassLoader of this Module's ClassLoader.
     */
    protected ClassLoader theParentClassLoader;

    /**
     * Once this Module has been activated, this is set to a non-zero value. Zero means
     * the Module is deactivated. A non-zero value N indicates that N other Modules
     * depend on this Module. In other words, other than keeping track whether or not
     * a Module is initialized, we also track how many Modules depend on it and thus
     * implement a form of "garbage-collection" policy.
     */
    private int theActivationCount = 0;

    /**
     * Once this Module has been configured, this is set to true.
     */
    private boolean isConfigured = false;

    /**
     * Activation of this Module may create a context object, which is buffered here.
     */
    private Object theContextObject;
}
