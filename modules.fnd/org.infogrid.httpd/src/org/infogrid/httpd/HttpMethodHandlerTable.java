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

package org.infogrid.httpd;

import java.util.HashMap;
import java.util.Iterator;

/**
 * A table of HTTP/WebDav methods and their handlers. This table is empty upon
 * construction and must be filled by the developer with MethodHandlers for those
 * HTTP methods that need to be supported.
 */
public class HttpMethodHandlerTable
{
    /**
     * Insert a new method by providing its HttpMethodHandler.
     *
     * @param handler the MethodHandler
     */
    public void put(
            HttpMethodHandler handler )
    {
        if( theTable.get( handler.getMethodName() ) != null ) {
            throw new IllegalArgumentException( "Handler for method " + handler.getMethodName() + " installed already" );
        }

        theTable.put( handler.getMethodName(), handler );
    }

    /**
     * Obtain a MethodHandler for a method identified by its name from the HTTP header.
     *
     * @param method the name of the method
     * @return the MethodHandler for this method, or null if not found
     */
    public HttpMethodHandler get(
            String method )
    {
        return theTable.get( method );
    }

    /**
     * Obtain an Iterator over all methods in this table. This iterator returns
     * the name of the method as a String. The get method can be used to obtain
     * the MethodHandler for this method.
     *
     * @return the Iterator
     */
    public Iterator<String> methodIterator()
    {
        return theTable.keySet().iterator();
    }

    /**
     * The actual content of the table.
     */
    protected HashMap<String,HttpMethodHandler> theTable = new HashMap<String,HttpMethodHandler>( 20 );
}
