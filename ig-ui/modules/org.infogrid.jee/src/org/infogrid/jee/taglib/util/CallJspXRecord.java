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

package org.infogrid.jee.taglib.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import org.infogrid.util.ClassInstanceHelper;
import org.infogrid.util.http.SaneRequestUtils;

/**
 * Captures a JSPF call.
 */
class CallJspXRecord
{
    /**
     * Constructor.
     *
     * @param page name of the page that is being invoked
     */
    public CallJspXRecord(
            String page )
    {
        thePage = page;
    }

    /**
     * Set the value of a named parameter.
     *
     * @param name the name of the parameter
     * @param value the value of the parameter
     * @return if false, the value had been set already
     */
    public boolean setParameterValue(
            String name,
            Object value )
    {
        if( theParameters.containsKey( name )) {
            return false;
        }
        theParameters.put( name, value );
        return true;
    }

    /**
     * Invoked by the JspfParamTag, this "requests" the value of a given parameter
     * to be checked for correct type, and to be put into the request context as
     * an attribute.
     *
     * @param name name of the parameter
     * @param type name of the required class of the parameter
     * @param isOptional if true, this parameter is not required
     * @param defaultValue if provided parameter is not given, use this value instead
     * @throws JspException thrown if an error occurred
     */
    public void processParameterValue(
            ServletRequest request,
            String         name,
            String         type,
            boolean        isOptional,
            Object         defaultValue )
        throws
            JspException
    {
        if( theOldAttributes.containsKey( name )) {
            throw new JspException( "Parameter " + name + " must not be declared more than once" );
        }

        Object value;
        if( theParameters.containsKey( name )) {
            value = theParameters.get( name );

        } else if( isOptional ) {
            value = defaultValue; // which may or may not have been given

        } else {
            throw new JspException( "Parameter " + name + " missing in call" );
        }

        if( value != null && type != null && type.length() > 0 ) {
            // check for correct type
            if( !ClassInstanceHelper.conforms( value, type )) {
                throw new JspException( "Parameter " + name + " has a value that is not of type " + type );
            }
        }

        theOldAttributes.put( name, request.getAttribute( name ));
        request.setAttribute( name, value );
    }

    /**
     * Obtain the set of parameter names that were specified in this call, but which were not
     * declared. That would be an error.
     *
     * @return the set, hopefully empty
     */
    public Set<String> getRemainingParameters()
    {
        HashSet<String> ret = new HashSet<String>();

        for( String current : theParameters.keySet() ) {
            if( !theOldAttributes.containsKey( current )) {
                ret.add( current );
            }
        }
        return ret;
    }

    /**
     * Restore the request attributes that were overwritten due to these parameters.
     *
     * @param request the current ServletRequest
     */
    public void restoreAttributes(
            ServletRequest request )
    {
        for( Entry<String,Object> current : theOldAttributes.entrySet() ) {
            request.setAttribute( current.getKey(), current.getValue() );
        }
    }

    /**
     * Get the parameters.
     *
     * @return the set of parameters
     */
    public Set<Entry<String,Object>> getParameters()
    {
        return theParameters.entrySet();
    }

    /**
     * Name of the page that is being invoked.
     */
    protected String thePage;

    /**
     * The given parameters and their values.
     */
    protected Map<String,Object> theParameters = new HashMap<String,Object>();

    /**
     * The parameters that have been set as request attributes. The values are the old
     * values of the request attributes with those names.
     */
    protected Map<String,Object> theOldAttributes = new HashMap<String,Object>();

    /**
     * Name of request attribute that contains the call record.
     */
    public static final String CALL_JSPX_RECORD_ATTRIBUTE_NAME
            = SaneRequestUtils.classToAttributeName( CallJspfTag.class, "call-record" );
}
