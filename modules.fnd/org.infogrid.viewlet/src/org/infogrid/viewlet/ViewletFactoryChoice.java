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

package org.infogrid.viewlet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.infogrid.util.context.Context;

/**
 * <p>A choice for instantiating a Viewlet by a ViewletFactory. See
 *    description at {@link ViewletFactory ViewletFactory}.</p>
 * <p>Two implementations are provided in this package:</p>
 * <ul>
 *  <li>{@link DefaultViewletFactoryChoice DefaultViewletFactoryChoice}: references a
 *      <code>Viewlet</code> specifying its <code>Class</code>;</li>
 *  <li>{@link InModuleViewletFactoryChoice InModuleViewletFactoryChoice}: references
 *      a <code>Viewlet</code> by referening a <code>ModuleCapability</code> in a
 *      <code>Module</code> that may or may not have been loaded.</li>
 * </ul>
 * <p>It is a common occurrence that applications implement their own implementations,
 *    e.g. to distinguish &quot;create in current window&quot; from &quot;create in
 *    new window&quot;.</p>
 */
public abstract class ViewletFactoryChoice
{
    /**
     * No-op constructor, for subclasses only.
     */
    protected ViewletFactoryChoice()
    {
        // no op
    }

    /**
     * Obtain a user-visible String to display to the user for this ViewletFactoryChoice.
     *
     * @return the user-visible String
     */
    public abstract String getUserVisibleName();

    /**
     * Obtain the computable name of the Viewlet.
     * 
     * @return the Viewlet's name
     */
    public abstract String getName();

    /**
      * Obtain the names of the interfaces provided by this ViewletFactoryChoice.
      *
      * @return the names of the interfaces provided by this ViewletFactoryChoice.
      */
    public abstract String [] getInterfaceNames();

    /**
     * Obtain the name of the Viewlet's implementation.
     *
     * @return the implementation name
     */
    public abstract String getImplementationName();

    /**
     * Obtain a measure of the match quality. 0 means &quot;perfect match&quot;,
     * while larger numbers mean increasingly worse match quality.
     *
     * @param toView the MeshObjectsToView to match against
     * @return the match quality
     */
    public abstract double getMatchQualityFor(
            MeshObjectsToView toView );

    /**
     * Instantiate a ViewletFactoryChoice into a Viewlet. The caller still must call
     * {org.infogrid.viewlet.Viewlet#view Viewlet.view} after having called
     * this method.
     * 
     * @param toView the MeshObjectsToView; only used for error reporting
     * @param c the Context to use
     * @return the instantiated Viewlet
     * @throws CannotViewException if, against expectations, the Viewlet corresponding
     *         to this ViewletFactoryChoice could not view the MeshObjectsToView after
     *         all. This usually indicates a programming error.
     */
    public abstract Viewlet instantiateViewlet(
            MeshObjectsToView        toView,
            Context                  c )
        throws
            CannotViewException;
    
    /**
     * Helper method to instantiate a ViewletFactoryChoice into a Viewlet. The use of this
     * method is optional by implementations.
     * 
     * @param toView the MeshObjectsToView; only used for error reporting
     * @param viewletClass the Viewlet Class to instantiate
     * @param c the Context to use
     * @return the instantiated Viewlet
     * @throws CannotViewException if, against expectations, the Viewlet corresponding
     *         to this ViewletFactoryChoice could not view the MeshObjectsToView after
     *         all. This usually indicates a programming error.
     */
    protected Viewlet instantiateViewlet(
            MeshObjectsToView        toView,
            Class<? extends Viewlet> viewletClass,
            Context                  c )
        throws
            CannotViewException
    {
        try {
            Method factoryMethod = viewletClass.getMethod(
                    "create",
                    Context.class );

            Object ret = factoryMethod.invoke(
                    null, // static method
                    c );

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
     * This need a good equals implementation, in order to prevent that we present the
     * same ViewletFactoryChoice more often than once.
     *
     * @param other the Object to compare to
     * @return true if the Objects are equal
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( other instanceof ViewletFactoryChoice ) {
            ViewletFactoryChoice realOther = (ViewletFactoryChoice) other;
            
            boolean ret = getImplementationName().equals( realOther.getImplementationName() );
            return ret;
        }
        return false;
    }

    /**
     * Hash code.
     * 
     * @return the hash code
     */
    @Override
    public int hashCode()
    {
        int ret = getImplementationName().hashCode();
        return ret;
    }

    /**
     * User-selected match quality, expressing "the user wanted this one".
     */
    public static final double USER_SELECTED_MATCH_QUALITY = 0.0;
    
    /**
     * Best match quality, expressing "perfect match".
     */
    public static final double PERFECT_MATCH_QUALITY = 1.0;

    /**
     * Good match quality, expressing "not perfect but better than average".
     */
    public static final double GOOD_MATCH_QUALITY = 10.0;

    /**
     * Default match quality if none was given.
     */
    public static final double AVERAGE_MATCH_QUALITY = 100.0;

    /**
     * Bad match quality, expressing "worse than average".
     */
    public static final double BAD_MATCH_QUALITY = 1000.0;

    /**
     * Worst match quality, expressing "there is a match, but it is very bad."
     */
    public static final double WORST_MATCH_QUALITY = Double.MAX_VALUE;
}
