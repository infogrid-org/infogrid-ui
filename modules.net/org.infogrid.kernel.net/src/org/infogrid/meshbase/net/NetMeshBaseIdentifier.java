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

package org.infogrid.meshbase.net;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.infogrid.meshbase.MeshBaseIdentifier;
import org.infogrid.util.Identifier;

/**
 * A network identifier, such as a URI, XRI or URL. It has two components
 * (which may be the same): the identifier in its canonical form, and a URL-equivalent.
 * For our purposes, we always need to have an URL-equivalent, otherwise the
 * identifier cannot be used for anything useful in InfoGrid. This class performs
 * the conversion.
 */
public class NetMeshBaseIdentifier
        extends
            MeshBaseIdentifier
        implements
            Identifier
{
    /**
     * Constructor.
     * 
     * @param canonicalForm the canonical representation of this identifier
     * @param uri URI representation of this identifier
     * @param isRestful if true, this identifier is REST-fully resolvable
     */
    protected NetMeshBaseIdentifier(
            String  canonicalForm,
            URI     uri,
            boolean isRestful  )
    {
        super( canonicalForm );

        theUri       = uri;
        theUriString = theUri.toString(); // do this here, much better for debugging
        theIsRestful = isRestful;
    }

    /**
     * Determine whether this NetMeshBaseIdentifier is REST-fully resolvable.
     * 
     * @return true if it is
     */
    public boolean isRestfullyResolvable()
    {
        return theIsRestful;
    }

    /**
     * Obtain the URI form of this identifier as URI.
     * 
     * @return the URI form
     */
    public URI toUri()
    {
        return theUri;
    }

    /**
     * Obtain the URL form of this identifier as URL.
     * 
     * @return the URL form
     * @throws MalformedURLException thrown if the URI could not be turned into a URL
     */
    public URL toUrl()
        throws
           MalformedURLException
    {
        return theUri.toURL();
    }

    /**
     * Obtain the URI String form of this identifier.
     *
     * @return the URI String form
     */
    public String getUriString()
    {
        return theUriString;
    }

    /**
     * Obtain the protocol of the URL form.
     *
     * @return the protocol.
     * @throws MalformedURLException thrown if the URI could not be turned into a URL
     */
    public String getUrlProtocol()
        throws
           MalformedURLException
    {
        if( theUrlProtocol == null ) {
            theUrlProtocol = toUrl().getProtocol();
        }
        return theUrlProtocol;
    }

    /**
     * Determine equality.
     *
     * @param other the Object to compare against
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(
            Object other )
    {
        if( !( other instanceof NetMeshBaseIdentifier )) {
            return false;
        }
        NetMeshBaseIdentifier realOther = (NetMeshBaseIdentifier) other;
        
        String here  = getCanonicalForm();
        String there = realOther.getCanonicalForm();
        
        boolean ret = here.equals( there );
        return ret;
    }

    /**
     * Calculate hash value. Make NetBeans happy.
     *
     * @return the hash value
     */
    @Override
    public int hashCode()
    {
        return super.hashCode();
    }

    /**
     * The URI.
     */
    protected URI theUri;

    /**
     * The URI in String form. This is stored here for efficiency reasons.
     */
    protected String theUriString;

    /**
     * The protocol part of the URL form. This is stored here for efficiency reasons.
     */
    protected String theUrlProtocol;

    /**
     * Whether this NetMeshBaseIdentifier is REST-fully resolvable.
     */
    protected boolean theIsRestful;
}
