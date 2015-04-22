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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.util.text.SimpleStringRepresentationParameters;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.util.text.StringifierException;

/**
 * <p>Abstract superclass for all tags performing a match between a <code>PropertyValue</code>
 *    and a regular expression.</p>
 */
public abstract class AbstractPropertyMatchTag
        extends
            AbstractPropertyTestTag
{
    /**
     * Constructor.
     */
    protected AbstractPropertyMatchTag()
    {
        // noop
    }

    /**
     * Initialize all default values. To be invoked by subclasses.
     */
    @Override
    protected void initializeToDefaults()
    {
        theExpression = null;
        
        super.initializeToDefaults();
    }

    /**
     * Obtain the value of the expression property.
     *
     * @return the value of the expression property
     * @see #setExpression
     */
    public String getExpression()
    {
        return theExpression;
    }

    /**
     * Set the value of the expression property.
     *
     * @param newValue the new value of the expression property
     * @see #getExpression
     */
    public void setExpression(
            String newValue )
    {
        theExpression = newValue;
    }

    /**
     * Determine the relative relationship between the comparison operators.
     * This returns values in analogy to the values returned by <code>String.compareTo</code>.
     *
     * @return -1, 0 or +1 depending on the result of the comparison.
     *         2 in case of non-comparable values.
     * @throws JspException thrown if an evaluation error occurred
     * @throws IgnoreException thrown to abort processing without an error
     */
    protected boolean matches()
        throws
            JspException,
            IgnoreException
    {
        PropertyValue found = evaluate();

        StringRepresentation rep = getFormatter().determineStringRepresentation( StringRepresentationDirectory.TEXT_PLAIN_NAME );

        try {
            String foundAsString = PropertyValue.toStringRepresentationOrNull( found, rep, SimpleStringRepresentationParameters.create() );

            Pattern p = Pattern.compile( theExpression );
            Matcher m = p.matcher( foundAsString );

            boolean ret = m.matches();
            return ret;

        } catch( StringifierException ex ) {
            throw new JspException( ex );
        }
    }

    /**
     * The expression property.
     */
    protected String theExpression;
}
