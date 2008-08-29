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

package org.infogrid.lid;

import org.infogrid.util.context.AbstractObjectInContext;
import org.infogrid.util.context.Context;

/**
 * Factors out common functionality of LidServices.
 */
public abstract class AbstractLidService
        extends
            AbstractObjectInContext
        implements
            LidService
{
    /**
     * Constructor for subclasses only, use factory method.
     * 
     * @param lidProfileName name of the LID profile of this LidService
     * @param lidProfileVersion version of the LID profile of this LidService
     * @param yadisFragment Yadis fragment of this LidService
     * @param c the context in which this <code>ObjectInContext</code> runs.
     */
    protected AbstractLidService(
            String  lidProfileName,
            String  lidProfileVersion,
            String  yadisFragment,
            Context c )
    {
        super( c );

        theLidProfileName    = lidProfileName;
        theLidProfileVersion = lidProfileVersion;
        theYadisFragment     = yadisFragment;
    }

    /**
     * Obtain the LID profile name of this LidService.
     * 
     * @return the profile name
     */
    public final String getLidProfileName()
    {
        return theLidProfileName;
    }
            
    /**
     * Obtain the LID profile version of this LidService.
     * 
     * @return the profile version
     */
    public final String getLidProfileVersion()
    {
        return theLidProfileVersion;
    }
    
    /**
     * Obtain the Yadis fragment to be inserted into an XRDS file.
     * 
     * @return the Yadis fragment
     */
    public String getParameterizedYadisFragment()
    {
        return theYadisFragment;
    }
    
    /**
     * LID profile name.
     */
    protected String theLidProfileName;
    
    /**
     * LID profile version.
     */
    protected String theLidProfileVersion;
    
    /**
     * Yadis fragment.
     */
    protected String theYadisFragment;
}
