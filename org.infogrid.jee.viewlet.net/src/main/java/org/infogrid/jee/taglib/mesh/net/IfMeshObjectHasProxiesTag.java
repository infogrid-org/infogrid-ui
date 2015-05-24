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
// Copyright 1998-2015 by Johannes Ernst
// All rights reserved.
//

package org.infogrid.jee.taglib.mesh.net;

import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.mesh.net.NetMeshObject;
import javax.servlet.jsp.JspException;
import org.infogrid.meshbase.net.proxy.Proxy;

/**
 * <p>Processes body content if a NetMeshObject has a number of Proxies between
 *    a given minimum and a given maximum.</p>
 */
public class IfMeshObjectHasProxiesTag
    extends
        AbstractInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public IfMeshObjectHasProxiesTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theMeshObjectName = null;
        theMin            = null;
        theMax            = null;
        
        super.initializeToDefaults();
    }
    
    /**
     * Obtain value of the meshObjectName property.
     *
     * @return value of the meshObjectName property
     * @see #setMeshObjectName
     */
    public final String getMeshObjectName()
    {
        return theMeshObjectName;
    }

    /**
     * Set value of the meshObjectBean property.
     *
     * @param newValue new value of the meshObjectName property
     * @see #getMeshObjectName
     */
    public final void setMeshObjectName(
            String newValue )
    {
        theMeshObjectName = newValue;
    }
    
    /**
     * Obtain the value of the min property.
     *
     * @return value of the min property
     * @see #setMin
     */
    public final String getMin()
    {
        return theMin;
    }
    
    /**
     * Set value of the min property.
     *
     * @param newValue new value of the min property
     * @see #getMin
     */
    public final void setMin(
            String newValue )
    {
        theMin = newValue;
    }

    /**
     * Obtain the value of the max property.
     *
     * @return value of the max property
     * @see #setMax
     */
    public final String getMax()
    {
        return theMax;
    }
    
    /**
     * Set value of the max property.
     *
     * @param newValue new value of the max property
     * @see #getMax
     */
    public final void setMax(
            String newValue )
    {
        theMax = newValue;
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
        NetMeshObject obj     = (NetMeshObject) lookupOrThrow( theMeshObjectName );
        Proxy []      proxies = obj.getAllProxies();

        int min           = theMin != null ? Integer.parseInt( theMin ) : -1;
        int max           = theMax != null ? Integer.parseInt( theMax ) : -1;
        int numberProxies = proxies != null ? proxies.length : 0;

        int ret;

        if( min >= 0 ) {
            if( max >= 0 ) {
                if( min <= numberProxies ) {
                    if( numberProxies <= max ) {
                        ret = EVAL_BODY_INCLUDE;
                    } else {
                        ret = SKIP_BODY;
                    }
                } else {
                    ret = SKIP_BODY;
                }
            } else if( min <= numberProxies ) {
                ret = EVAL_BODY_INCLUDE;
            } else {
                ret = SKIP_BODY;
            }
        } else if( max >=0 ) {
            if( numberProxies <= max ) {
                ret = EVAL_BODY_INCLUDE;
            } else {
                ret = SKIP_BODY;
            }
        } else {
            ret = EVAL_BODY_INCLUDE;
        }

        return ret;
    }
    
    /**
     * String containing the name of the bean that is the MeshObject whose property we render.
     */
    protected String theMeshObjectName;
    
    /**
     * The minimum number of proxies, if given.
     */
    protected String theMin;
    
    /**
     * The maximum number of proxies, if given.
     */
    protected String theMax;
}
