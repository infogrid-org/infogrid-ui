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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.viewlet.module;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import org.diet4j.core.Module;
import org.diet4j.core.ModuleException;
import org.diet4j.core.ModuleMeta;
import org.diet4j.core.ModuleNotFoundException;
import org.diet4j.core.ModuleRegistry;
import org.diet4j.core.ModuleResolutionException;
import org.infogrid.mesh.MeshObject;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.context.Context;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;
import org.infogrid.viewlet.AbstractViewletFactoryChoice;
import org.infogrid.viewlet.CannotViewException;
import org.infogrid.viewlet.MeshObjectsToView;
import org.infogrid.viewlet.Viewlet;

/**
 * ViewletFactoryChoice referencing a ModuleCapability in a Module.
 */
public class InModuleViewletFactoryChoice
        extends
            AbstractViewletFactoryChoice
{
    private static final Log log = Log.getLogInstance( InModuleViewletFactoryChoice.class ); // our own, private logger

    /**
     * Constructor.
     *
     * @param toView the MeshObjectsToView for which this is a choice
     * @param reg the ModuleRegistry against which to resolve the StandardModuleAdvertisement
     * @param ad the StandardModuleAdvertisement that contains the candidate
     * @param cap the ModuleCapability that was found
     */
    public InModuleViewletFactoryChoice(
            MeshObjectsToView  toView,
            ModuleRegistry     reg,
            ModuleMeta         ad,
            ModuleCapability   cap )
    {
        super( toView );

        theModuleRegistry = reg;
        theMeta             = ad;
        theCapability     = cap;
    }

    /**
      * Obtain the names of the interfaces provided by this ViewletFactoryChoice.
      *
      * @return the names of the interfaces provided by this ViewletFactoryChoice.
      */
    public String [] getInterfaceNames()
    {
        return theCapability.getInterfaceNames();
    }

    /**
     * Obtain the name of the Viewlet's implementation.
     *
     * @return the implementation name
     */
    public String getImplementationName()
    {
        return theCapability.getImplementationName();
    }

    /**
     * Obtain the advertisement of the Module that contained a candidate.
     *
     * @return the ModuleAdvertisement
     */
    public ModuleMeta getModuleMeta()
    {
        return theMeta;
    }

    /**
     * Obtain the found ModuleCapability.
     *
     * @return the found ModuleCapability
     */
    public ModuleCapability getModuleCapability()
    {
        return theCapability;
    }

    /**
     * Obtain a measure of the match quality. 0 means &quot;perfect match&quot;,
     * while larger numbers mean increasingly worse match quality.
     *
     * @param toView the MeshObjectsToView to match against
     * @return the match quality
     * @see ViewletFactoryChoice#PERFECT_MATCH_QUALITY
     * @see ViewletFactoryChoice#AVERAGE_MATCH_QUALITY
     * @see ViewletFactoryChoice#WORST_MATCH_QUALITY
     */
    public double getMatchQualityFor(
            MeshObjectsToView toView )
    {
        MeshObject                             subject = toView.getSubject();
        ModuleCapability.ArgumentCombination[] combo   = theCapability.getSupportedArgumentCombinations();

        if( combo == null || combo.length == 0 ) {
           // treat it as if it was one level supertypes away (FIXME?)
            return 1;

        } else {
            int bestDistance = Integer.MAX_VALUE;

            for( int j = 0; j<combo.length; ++j ) {
                String[] args = combo[j].getArguments();

                if( args.length != 1 ) {
                    continue;
                }
                
                for( EntityType type : subject.getTypes() ) {
                    int distance = type.getDistanceToSupertype( args[0] );

                    if( distance < bestDistance ) {
                        bestDistance  = distance;
                    }
                }
            }
            return bestDistance;
        }
    }

    /**
     * Instantiate a ViewletFactoryChoice into a Viewlet.
     * 
     * @return the instantiated Viewlet
     */
    @SuppressWarnings( { "unchecked" } )
    public Viewlet instantiateViewlet()
        throws
            CannotViewException
    {
        try {
            Module      viewletModule = theModuleRegistry.resolve(theMeta, true );
            ClassLoader viewletClassLoader = viewletModule.getClassLoader();
            Class       viewletClass = Class.forName( theCapability.getImplementationName(), true, viewletClassLoader );

            return instantiateViewlet( (Class<? extends Viewlet>) viewletClass );

        } catch( ClassNotFoundException ex ) {
            throw new org.infogrid.viewlet.CannotViewException.InternalError( null, getMeshObjectsToView(), ex );
            
        } catch( ModuleResolutionException ex ) {
            log.error( ex );
            throw new org.infogrid.viewlet.CannotViewException.InternalError( null, getMeshObjectsToView(), ex );
            
        } catch( ModuleNotFoundException ex ) {
            log.error( ex );
            throw new org.infogrid.viewlet.CannotViewException.InternalError( null, getMeshObjectsToView(), ex );
            
        } catch( MalformedURLException ex ) {
            log.error( ex );
            throw new org.infogrid.viewlet.CannotViewException.InternalError( null, getMeshObjectsToView(), ex );
            
        }
    }

    /**
     * Helper method to instantiate a ViewletFactoryChoice into a Viewlet. The use of this
     * method is optional by implementations.
     *
     * @param viewletClass the Viewlet Class to instantiate
     * @return the instantiated Viewlet
     * @throws CannotViewException if, against expectations, the Viewlet corresponding
     *         to this ViewletFactoryChoice could not view the MeshObjectsToView after
     *         all. This usually indicates a programming error.
     */
    protected Viewlet instantiateViewlet(
            Class<? extends Viewlet> viewletClass )
        throws
            CannotViewException
    {
        MeshObjectsToView toView = getMeshObjectsToView();

        try {
            Method factoryMethod = viewletClass.getMethod(
                    "create",
                    Context.class );

            Object ret = factoryMethod.invoke(
                    null, // static method
                    toView.getMeshBase(), // meshbase
                    toView.getContext() ); // context

            return (Viewlet) ret;

        } catch( NoSuchMethodException ex ) {
            throw new CannotViewException.InternalError( null, toView, ex );
        } catch( IllegalAccessException ex ) {
            throw new CannotViewException.InternalError( null, toView, ex );
        } catch( InvocationTargetException ex ) {
            throw new CannotViewException.InternalError( null, toView, ex );
        }
    }

    /**
     * Obtain a String representation of this instance that can be shown to the user.
     *
     * @param rep the StringRepresentation
     * @param pars collects parameters that may influence the String representation. Always provided.
     * @return String representation
     * @throws StringifierException thrown if there was a problem when attempting to stringify
     */
    public String toStringRepresentation(
            StringRepresentation           rep,
            StringRepresentationParameters pars )
        throws
            StringifierException
    {
        String userVisibleName = getImplementationName(); // default

        try {
            Module m = theModuleRegistry.resolve(theMeta );
            userVisibleName = ResourceHelper.getInstance( theCapability.getImplementationName(), m.getClassLoader() ).getResourceStringOrDefault( "UserVisibleName", getImplementationName() );

        } catch( MalformedURLException ex ) {
            log.warn( ex );

        } catch( ModuleException ex ) {
            log.warn( ex );
        }

        String ret = rep.formatEntry(
                getClass(), // dispatch to the right subtype
                StringRepresentation.DEFAULT_ENTRY,
                pars,
        /* 0 */ this,
        /* 1 */ getImplementationName(),
        /* 2 */ userVisibleName );

        return ret;
    }

    /**
     * The ModuleRegistry.
     */
    protected ModuleRegistry theModuleRegistry;

    /**
     * The advertisement of the Module that contained a candidate.
     */
    protected ModuleMeta theMeta;

    /**
     * The capability that indicated a candidate.
     */
    protected ModuleCapability theCapability;
}
