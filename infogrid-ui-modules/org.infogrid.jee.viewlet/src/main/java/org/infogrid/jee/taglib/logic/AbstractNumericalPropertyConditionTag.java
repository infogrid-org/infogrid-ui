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

package org.infogrid.jee.taglib.logic;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.model.primitives.CurrencyValue;
import org.infogrid.model.primitives.FloatValue;
import org.infogrid.model.primitives.IntegerValue;
import org.infogrid.model.primitives.PropertyValue;

/**
 * <p>Abstract superclass for all tags evaluating a numerical property.
 */
public abstract class AbstractNumericalPropertyConditionTag
    extends
        AbstractPropertyTestTag
{
    /**
     * Constructor.
     */
    protected AbstractNumericalPropertyConditionTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        super.initializeToDefaults();
    }

    /**
     * Determine -1, 0 or +1 for negative, zero or positive.
     *
     * @return -1, 0 or +1 depending on the result of the evaluation
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected int evaluateToInt()
        throws
            JspException,
            IgnoreException
    {
        PropertyValue found = evaluate();

        if( found == null ) {
            return 0;
        }
        if( found instanceof IntegerValue ) {
            IntegerValue realFound = (IntegerValue) found;
            long         realValue = realFound.value();
            
            if( realValue > 0L ) {
                return +1;
            } else if( realValue == 0L ) {
                return 0;
            } else {
                return -1;
            }

        } else if( found instanceof FloatValue ) {
            FloatValue realFound = (FloatValue) found;
            double     realValue = realFound.value();
            
            if( realValue > 0. ) {
                return +1;
            } else if( realValue == 0. ) {
                return 0;
            } else {
                return -1;
            }
        } else if( found instanceof CurrencyValue ) {
            CurrencyValue realFound = (CurrencyValue) found;
            
            if( realFound.isPositive()) {
                return +1;
            } else if( realFound.isFree() ) {
                return 0;
            } else {
                return -1;
            }
        } else {
            throw new JspException( "Not a numeric property value: " + found );
        }
    }
}
