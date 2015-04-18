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

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;

/**
 * Sets the value of a parameter to a Jspo call to a particular value for this invocation.
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class CallJspoParamTag
    extends
        AbstractInfoGridTag

{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public CallJspoParamTag()
    {
        // noop
    }

    /**
     * Release all of our resources.
     */
    @Override
    protected void initializeToDefaults()
    {
        theName  = null;
        theValue = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the name property.
     *
     * @return value of the name property
     * @see #setName
     */
    public String getName()
    {
        return theName;
    }

    /**
     * Set value of the name property.
     *
     * @param newValue new value of the name property
     * @see #getName
     */
    public void setName(
            String newValue )
    {
        theName = newValue;
    }

    /**
     * Obtain value of the value property.
     *
     * @return value of the value property
     * @see #setValue
     */
    public Object getValue()
    {
        return theValue;
    }

    /**
     * Set value of the value property.
     *
     * @param newValue new value of the value property
     * @see #getValue
     */
    public void setValue(
            Object newValue )
    {
        theValue = newValue;
    }

    /**
     * Our implementation of doStartTag().
     *
     * @return evaluate or skip body
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     * @throws IOException thrown if an I/O Exception occurred
     */
    protected int realDoStartTag()
        throws
            JspException,
            IgnoreException,
            IOException
    {
        Tag parentTag = findAncestorTag( CallJspoTag.class );
        if( parentTag == null ) {
            throw new JspException( "CallJspoParamTag must be contained in a CallJspoTag" );
        }
        CallJspXRecord record = (CallJspXRecord) pageContext.getRequest().getAttribute( CallJspXRecord.CALL_JSPX_RECORD_ATTRIBUTE_NAME );
        if( record == null ) {
            throw new JspException( "CallJspfParamTag cannot find CallJspfRecord for this call" );
        }
        if( theName == null || theName.length() == 0 ) {
            throw new JspException( "Attribute name of CallJspfParamTag must not be empty" );
        }
        if( !record.setParameterValue( theName, theValue )) {
            throw new JspException( "Cannot set parameter " + theName + " more than once" );
        }
        return SKIP_BODY;
    }

    /**
     * Name of the parameter.
     */
    protected String theName;

    /**
     * Value of the parameter.
     */
    protected Object theValue;
}
