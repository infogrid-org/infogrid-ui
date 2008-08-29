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

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 * This is the abstract superclass for all things that HTTP calls "Entity".
 */
public abstract class HttpEntity
{
    /**
     * Obtain the allowed operations on this Entity. This default implementation
     * says we can do GET and HEAD; subclasses may override.
     *
     * @return an Iterator returning String
     */
    public Iterator allowIterator()
    {
        return theAllowedDefaultMethods.iterator();
    }

    /**
     * Obtain the MIME type of this entity.
     *
     * @return String representing the MIME type of this entity, such as "text/html"
     */
    public abstract String getMime();

    /**
     * Obtain the content of this entity as an InputStream.
     *
     * @return the InputStream from which the content of this entity can be read
     * @throws IOException thrown if a read error occurred
     */
    public abstract InputStream getAsStream()
        throws
            IOException;

    /**
     * Determine whether this entity is readable.
     *
     * @return true if this entity is readable
     */
    public abstract boolean canRead();

    /**
     * Determine when this Entity was last modified. This may return null,
     * indicating that such Date is not known. This default implementation
     * returns null.
     *
     * @return the Date the Entity was last modified, or null
     */
    public Date getLastModified()
    {
        return null;
    }

    /**
     * Determine when this Entity will expire. This may return null,
     * indicating that such Date is not known. This default implementation
     * returns null.
     *
     * @return the Date the Entity will expire, or null
     */
    public Date getExpires()
    {
        return null;
    }

    /**
     * Obtain the content length. Return -1 if not known. This is the length of
     * the Entity after stream, which is not stripped down by Range requests (as the
     * Entity has a certain length which is independent of Range requests).
     *
     * @return the content length, or -1
     */
    public int getContentLength()
    {
        return -1;
    }

    /**
     * Obtain the Entity's language. By default, this returns null, indicating
     * that we don't know.
     *
     * @return the Entity's language
     */
    public String getContentLanguage()
    {
        return null;
    }

    /**
     * Obtain the Entity's content location. By default, this returns null,
     * indicating that we don't know.
     *
     * @return the Entity's content location
     */
    public String getContentLocation()
    {
        return null;
    }

    /**
     * Contains the HTTP methods on this HttpEntity that are allowed by default.
     */
    private static final Collection<String> theAllowedDefaultMethods;
    static {
        theAllowedDefaultMethods = new ArrayList<String>( 2 );
        theAllowedDefaultMethods.add( "GET" );
        theAllowedDefaultMethods.add( "HEAD" );
    }
}
