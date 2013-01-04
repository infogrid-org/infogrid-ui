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
// Copyright 1998-2013 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.util;

import org.infogrid.util.AbstractLocalizedException;

/**
 * Thrown when a JSP page included via CallJspf or CallJspo fails to compile. This facilitates
 * better error reporting than the default.
 */
public class CalledJspCompileException
    extends
        AbstractLocalizedException
{
    /**
     * Constructor.
     * 
     * @param page the path of the page
     * @param cause the underlying exception
     */
    public CalledJspCompileException(
            String    page,
            Throwable cause )
    {
        super( cause );
        
        thePage = page;
    }
    
    /**
     * Obtain the page that could not be compiled.
     * 
     * @return the page
     */
    public String getPage()
    {
        return thePage;
    }
    
    /**
     * Obtain resource parameters for the internationalization.
     *
     * @return the resource parameters
     */
    @Override
    public Object [] getLocalizationParameters()
    {
        return new Object[] { thePage, getCause().getMessage() };
    }

    /**
     * The page that could not be compiled.
     */
    protected String thePage;
}
