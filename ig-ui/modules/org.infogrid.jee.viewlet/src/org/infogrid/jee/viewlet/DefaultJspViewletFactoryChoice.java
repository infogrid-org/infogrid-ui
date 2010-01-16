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

package org.infogrid.jee.viewlet;

import java.util.ArrayList;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.ResourceHelper;
import org.infogrid.viewlet.DefaultViewletFactoryChoice;

/**
 * <p>A ViewletFactoryChoice that instantiates the SimpleJeeViewlet as default, pretending to
 *    be a Viewlet class with a certain name, called the <code>pseudoClassName</code>. This
 *    is identical to creating a DefaultViewletFactoryChoice with a Viewlet class named
 *    pseudoClassName that does not add any functionality itself.</p>
 * <p>The main purpose of this class is to avoid having to write empty Viewlet classes.</p>
 */
public abstract class DefaultJspViewletFactoryChoice
        extends
            DefaultViewletFactoryChoice
{
    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param pseudoClassName the name of the (non-exististing) Viewlet class
     * @param matchQuality the match quality
     */
    protected DefaultJspViewletFactoryChoice(
            String         pseudoClassName,
            double         matchQuality )
    {
        super( SimpleJeeViewlet.class, matchQuality );

        thePseudoClassName = pseudoClassName;
    }
    
    /**
     * Obtain a user-visible String to display to the user for this ViewletFactoryChoice.
     *
     * @return the user-visible String
     */
    @Override
    public String getUserVisibleName()
    {
        ResourceHelper rh  = ResourceHelper.getInstance( thePseudoClassName, DefaultJspViewletFactoryChoice.class.getClassLoader()  );

        String ret = rh.getResourceStringOrDefault( "UserVisibleName", thePseudoClassName );

        return ret;
    }

    /**
     * Obtain the computable name of the Viewlet.
     * 
     * @return the Viewlet's name
     */
    @Override
    public String getName()
    {
        return thePseudoClassName;
    }

    /**
      * Obtain the names of the interfaces provided by this ViewletFactoryChoice.
      *
      * @return the names of the interfaces provided by this ViewletFactoryChoice.
      */
    @Override
    public String [] getInterfaceNames()
    {
        ArrayList<String> almost = new ArrayList<String>();

        almost.add( thePseudoClassName );

        determineClassNames( theViewletClass, almost );

        String [] ret = ArrayHelper.copyIntoNewArray( almost, String.class );
        return ret;
    }

    /**
     * The name of the Viewlet class this would have been if it had been created as a separate
     * Viewlet class.
     */
    protected String thePseudoClassName;
}
