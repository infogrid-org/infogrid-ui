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
import org.infogrid.model.primitives.PropertyValue;

/**
 * <p>This tag compares similarly to strcmp.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class CompareTag
    extends
        AbstractPropertyCompareTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public CompareTag()
    {
        // noop
    }

    /**
     * Determine whether or not to process the content of this Tag. The content of this
     * tag shall be processed if {see @link AbstractPropertyCompareTag#compare compare()} does not return 0.
     *
     * @return true if the content of this tag shall be processed.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected boolean evaluateTest()
        throws
            JspException,
            IgnoreException
    {
        PropertyValue value1 = determinePropertyValue( theMeshObject,  theMeshObjectName,  thePropertyTypeName,  thePropertyType,  "propertyTypeName",  "propertyType" );
        PropertyValue value2 = determinePropertyValue( theMeshObject2, theMeshObject2Name, thePropertyType2Name, thePropertyType2, "propertyType2Name", "propertyType2" );

        int comparison1 = PropertyValue.compare( value1, value2 );
        int comparison2;

        if( theValueName != null ) {
            comparison2 = (Integer) lookup( theValueName );
        } else if( theValue != null ) {
            comparison2 = Integer.parseInt( theValue );
        } else {
            throw new JspException( "Must specify either valueName or value" );
        }

        if( comparison1 > 0 ) {
            return comparison2 > 0;

        } else if( comparison1 == 0 ) {
            return comparison2 == 0;

        } else {
            return comparison2 < 0;
        }
    }
}
