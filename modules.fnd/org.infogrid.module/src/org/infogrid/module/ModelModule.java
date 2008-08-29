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
import java.lang.reflect.*;
import java.net.*;
import java.util.Map;

/**
 * A Module representing a Model (aka schema).
 */
public class ModelModule
        extends
            Module
{
    /**
     * Package-private constructor. This is only instantiated from within this package.
     *
     * @param adv the Module Advertisement describing this Module
     * @param registry the registry of Modules in which we try to find dependent Modules
     * @param moduleDirectory the Module's directory
     * @param moduleJars the JAR files belonging to this Module
     * @param parentClassLoader the class loader of our parent Module
     */
    ModelModule(
            ModelModuleAdvertisement adv,
            ModuleRegistry           registry,
            File                     moduleDirectory,
            File []                  moduleJars,
            ClassLoader              parentClassLoader )
    {
        super( registry, moduleDirectory, moduleJars, parentClassLoader );

        theModuleAdvertisement = adv;
    }

    /**
      * Obtain the ModuleAdvertisement for this Module.
      *
      * @return the ModuleAdvertisement for this Module
      */
    public final ModelModuleAdvertisement getModuleAdvertisement()
    {
        return theModuleAdvertisement;
    }

    /**
     * Configure this Module. Do not call this directly, call configureRecursively() instead.
     * This method does nothing here.
     *
     * @param parameters the set of parameters for this Module
     */
    protected void configure(
            Map<String,? extends Object> parameters )
    {
        // NO OP
    }

    /**
     * Run this Module as a root Module. Given that this is a MetamodelModule, we can't do this
     * and throw an exception
     *
     * @param overriddenRunClassName optional name of the class to run instead of the default one specified in the ModuleAdvertisement
     * @param overriddenRunMethodName optional name of the method in the class to run instead of the default one specified in the ModuleAdvertisement
     * @param arguments arguments to run, similar to the arguments of a standard main(...) method
     * @throws NoRunMethodException always throws this exception
     */
    public void run(
            String    overriddenRunClassName,
            String    overriddenRunMethodName,
            String [] arguments )
        throws
            NoRunMethodException
    {
        throw new NoRunMethodException( theModuleAdvertisement, overriddenRunClassName, overriddenRunMethodName, null );
    }

    /**
     * Obtain a ModuleActivator for the Module that this ModuleAdvertisement corresponds to,
     * corresponding to the activation parameters given in the ModuleAdvertisement.
     * 
     * @return the ModelModuleActivator
     */
    public ModelModuleActivator getDefaultModuleActivator()
    {
        return new DefaultModelModuleActivator( this );
    }

    /**
     * The ModuleAdvertisment for this Module.
     */
    protected ModelModuleAdvertisement theModuleAdvertisement;
}
