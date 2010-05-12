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

package org.infogrid.jee.taglib.viewlet;

import javax.servlet.jsp.JspException;
import org.infogrid.jee.taglib.AbstractInfoGridTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringifierException;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * <p>Tag that displays a ViewletFactoryChoice.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class ViewletAlternativeTag
    extends
        AbstractInfoGridTag
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Constructor.
     */
    public ViewletAlternativeTag()
    {
        // noop
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theViewletAlternativeName = null;
        theStringRepresentation   = null;

        super.initializeToDefaults();
    }

    /**
     * Obtain value of the viewletAlternativeName property.
     *
     * @return value of the viewletAlternativeName property
     * @see #setViewletAlternativeName
     */
    public String getViewletAlternativeName()
    {
        return theViewletAlternativeName;
    }

    /**
     * Set value of the viewletAlternativeName property.
     *
     * @param newValue new value of the viewletAlternativeName property
     * @see #getViewletAlternativeName
     */
    public void setViewletAlternativeName(
            String newValue )
    {
        theViewletAlternativeName = newValue;
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
        ViewletFactoryChoice choice  = (ViewletFactoryChoice) lookupOrThrow( theViewletAlternativeName );

        if( choice != null ) { // may happen if ignore="true"
            StringRepresentation rep = theFormatter.determineStringRepresentation( theStringRepresentation );
            try {

                String text = choice.toStringRepresentation( rep, null );
                print( text );

            } catch( StringifierException ex ) {
                throw new JspException( ex );
            }
        }
        return EVAL_PAGE;
    }

    /**
     * Name of the bean that holds the ViewletFactoryChoice
     */
    protected String theViewletAlternativeName;

    /**
     * Name of the String representation.
     */
    protected String theStringRepresentation;
}
