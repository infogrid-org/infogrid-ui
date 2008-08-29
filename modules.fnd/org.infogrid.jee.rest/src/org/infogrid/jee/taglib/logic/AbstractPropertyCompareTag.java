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

package org.infogrid.jee.taglib.logic;

import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.model.primitives.PropertyValue;

import javax.servlet.jsp.JspException;

/**
 * <p>Abstract superclass for all tags performing a comparison of
 *    <code>PropertyValues</code>.</p>
 */
public abstract class AbstractPropertyCompareTag
    extends
        AbstractPropertyTestTag
{
    /**
     * Constructor.
     */
    protected AbstractPropertyCompareTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theValue     = null;
        theValueName = null;
        
        super.initializeToDefaults();
    }

    /**
     * Obtain the value of the value property.
     *
     * @return the value of the value property
     * @see #setValue
     */
    public String getValue()
    {
        return theValue;
    }

    /**
     * Set the value of the value property.
     *
     * @param newValue the new value of the value property
     * @see #getValue
     */
    public void setValue(
            String newValue )
    {
        theValue = newValue;
    }

    /**
     * Obtain the value of the valueName property.
     *
     * @return the value of the valueName property
     * @see #setValueName
     */
    public String getValueName()
    {
        return theValueName;
    }

    /**
     * Set the value of the valueName property.
     *
     * @param newValue the new value of the valueName property
     * @see #getValueName
     */
    public void setValueName(
            String newValue )
    {
        theValueName = newValue;
    }

    /**
     * Determine the relative relationship between the comparison operators.
     * This returns values in analogy to the values returned by <code>String.compareTo</code>.
     *
     * @return -1, 0 or +1 depending on the result of the comparison;
     *         2 in case of non-comparable values.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected int compare()
        throws
            JspException,
            IgnoreException
    {
        PropertyValue found = evaluate();
        
        if( theValueName != null ) {
            PropertyValue comparison = (PropertyValue) lookupOrThrow( theValueName );
            
            return PropertyValue.compare( found, comparison );
            
        } else {
            if( found == null ) {
                if( theValue == null || theValue.equals( "null" )) {
                    return 0;
                } else {
                    return +2; // non-comparable convention: +2
                }
            }

            String stringified = found.toString();
            return stringified.compareTo( theValue );
        }
    }

    /**
     * The value property.
     */
    protected String theValue;
    
    /**
     * The valueName property.
     */
    protected String theValueName;    
}
