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
// Copyright 1998-2011 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.meshbase.net;

import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;

import org.infogrid.meshbase.net.NetMeshBase;
import org.infogrid.meshbase.net.proxy.Proxy;

import org.infogrid.util.CursorIterator;

import javax.servlet.jsp.JspException;

/**
 * Factors out common functionality of Tags that look at the number of Proxies of a NetMeshBase.
 */
public abstract class AbstractIfNetMeshBaseHasProxiesTag
    extends
        AbstractInfoGridBodyTag
{
    /**
     * Constructor.
     */
    public AbstractIfNetMeshBaseHasProxiesTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theNetMeshBaseName = null;
        
        super.initializeToDefaults();
    }
    
    /**
     * Obtain value of the mesBaseName property.
     * 
     * @return value of the mesBaseName property
     * @see #setMeshBaseName
     */
    public final String getMeshBaseName()
    {
        return theNetMeshBaseName;
    }

    /**
     * Set value of the mesBaseName property.
     * 
     * @param newValue new value of the mesBaseName property
     * @see #getMeshBaseName
     */
    public final void setMeshBaseName(
            String newValue )
    {
        theNetMeshBaseName = newValue;
    }
    
    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    @Override
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException
    {
        if( evaluateTest() ) {
            return EVAL_BODY_INCLUDE;
        } else {
            return SKIP_BODY;
        }
    }
    
    /**
     * Evaluate the condition. If it returns true, the content of this tag is processed.
     *
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected abstract boolean evaluateTest()
        throws
            JspException,
            IgnoreException;

    /**
     * Evaluate the condition. If it returns true, we include output
     * the Nodes contained in this Node.
     *
     * @return true in order to output the Nodes contained in this Node.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected boolean evaluate()
        throws
            JspException,
            IgnoreException
    {
        NetMeshBase mb = (NetMeshBase) lookupOrThrow( theNetMeshBaseName );

        CursorIterator<Proxy> iter = mb.proxies();
        boolean ret;
        if( iter.hasNext() ) {
            ret = true;
        } else if( iter.hasPrevious() ) {
            ret = true;
        } else {
            ret = false;
        }
        return ret;
    }
    
    /**
     * String containing the name of the bean that is the NetMeshbase.
     */
    protected String theNetMeshBaseName;
}
