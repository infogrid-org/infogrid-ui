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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.meshbase.net;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.rest.net.NetRestfulJeeFormatter;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.meshbase.net.proxy.Proxy;
import org.infogrid.util.text.StringifierException;

/**
 * <p>Tag that displays a Proxy.</p>
 */
public class ProxyTag
    extends
        AbstractInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public ProxyTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theProxyName            = null;
        theStringRepresentation = null;
        theMaxLength            = -1;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the proxyName property.
     *
     * @return value of the proxyName property
     * @see #setProxyName
     */
    public String getProxyName()
    {
        return theProxyName;
    }

    /**
     * Set value of the proxyName property.
     *
     * @param newValue new value of the proxyName property
     * @see #getProxyName
     */
    public void setProxyName(
            String newValue )
    {
        theProxyName = newValue;
    }

    /**
     * Obtain value of the stringRepresentation property.
     *
     * @return value of the stringRepresentation property
     * @see #setStringRepresentation
     */
    public String getStringRepresentation()
    {
        return theStringRepresentation;
    }

    /**
     * Set value of the stringRepresentation property.
     *
     * @param newValue new value of the stringRepresentation property
     * @see #getStringRepresentation
     */
    public void setStringRepresentation(
            String newValue )
    {
        theStringRepresentation = newValue;
    }

    /**
     * Obtain value of the maxLength property.
     *
     * @return value of the maxLength property
     * @see #setMaxLength
     */
    public int getMaxLength()
    {
        return theMaxLength;
    }

    /**
     * Set value of the maxLength property.
     *
     * @param newValue new value of the maxLength property
     */
    public void setMaxLength(
            int newValue )
    {
        theMaxLength = newValue;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        Proxy p = (Proxy) lookupOrThrow( theProxyName );

        try {
            String text = ((NetRestfulJeeFormatter)theFormatter).formatProxyStart( pageContext, p, theStringRepresentation, theMaxLength );
            print( text );

        } catch( StringifierException ex ) {
            throw new JspException( ex );
        }

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Our implementation of doEndTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    @Override
    protected int realDoEndTag()
        throws
            JspException,
            IgnoreException
    {
        Proxy p = (Proxy) lookupOrThrow( theProxyName );

        String text = ((NetRestfulJeeFormatter)theFormatter).formatProxyEnd( pageContext, p, theStringRepresentation );

        print( text );

        return EVAL_PAGE;
    }

    /**
     * Name of the bean that holds the Proxy.
     */
    protected String theProxyName;

    /**
     * Name of the String representation.
     */
    protected String theStringRepresentation;

    /**
     * The maximum length of an emitted String.
     */
    protected int theMaxLength;
}
