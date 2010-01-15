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

package org.infogrid.viewlet;

import java.util.ArrayList;
import org.infogrid.util.ArrayHelper;

/**
 * Factors out common functionality for ViewletFactoryChoice implementations.
 */
public abstract class AbstractViewletFactoryChoice
        implements
            ViewletFactoryChoice
{
    /**
     * Private constructor, use factory method. Specify match quality.
     *
     * @param matchQuality the match quality
     */
    protected AbstractViewletFactoryChoice(
            double matchQuality )
    {
        theMatchQuality = matchQuality;
    }

    /**
     * Obtain the computable name of the Viewlet.
     *
     * @return the Viewlet's name
     */
    public String getName()
    {
        return getImplementationName();
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
        return theMatchQuality;
    }

    /**
     * Obtain the names of the interfaces provided by this ViewletFactoryChoice.
     *
     * @param viewletClass the viewlet class
     * @return the names of the interfaces provided by this ViewletFactoryChoice.
     */
    public static String [] getInterfaceNames(
            Class viewletClass )
    {
        ArrayList<String> almost = new ArrayList<String>();

        determineClassNames( viewletClass, almost );

        String [] ret = ArrayHelper.copyIntoNewArray( almost, String.class );
        return ret;
    }

    /**
     * Internal helper method that recursively looks up the names of all interface
     * and class names supported by a Class.
     *
     * @param clazz the Class
     * @param found the set of names found
     */
    public static void determineClassNames(
            Class             clazz,
            ArrayList<String> found )
    {
        found.add( clazz.getName() );

        Class toAdd = clazz.getSuperclass();
        if( toAdd != null ) {
            determineClassNames( toAdd, found );
        }
        for( Class intfc : clazz.getInterfaces()) {
            if( !found.contains( intfc.getName() )) {
                determineClassNames( intfc, found );
            }
        }
    }

    /**
     * The match quality.
     *
     * @see ViewletFactoryChoice#PERFECT_MATCH_QUALITY
     * @see ViewletFactoryChoice#AVERAGE_MATCH_QUALITY
     * @see ViewletFactoryChoice#WORST_MATCH_QUALITY
     */
    protected double theMatchQuality;
}
