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

package org.infogrid.jee.taglib.viewlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import org.infogrid.jee.sane.SaneServletRequest;
import org.infogrid.jee.taglib.AbstractInfoGridBodyTag;
import org.infogrid.jee.taglib.IgnoreException;
import org.infogrid.jee.viewlet.JeeViewlet;
import org.infogrid.jee.viewlet.JeeViewletFactoryChoice;
import org.infogrid.util.http.SaneRequest;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.SimpleStringRepresentationParameters;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;
import org.infogrid.viewlet.ViewletFactoryChoice;

/**
 * <p>Tag that links / hyperlinks to a MeshObject.</p>
 * @see <a href="package-summary.html">Details in package documentation</a>
 */
public class ViewletAlternativeLinkTag
    extends
        AbstractInfoGridBodyTag
{
    private static final long serialVersionUID = 1L; // helps with serialization
    private static final Log  log              = Log.getLogInstance( ViewletAlternativeLinkTag.class ); // our own, private logger

    /**
     * Constructor.
     */
    public ViewletAlternativeLinkTag()
    {
    }

    /**
     * Initialize.
     */
    @Override
    protected void initializeToDefaults()
    {
        theViewletAlternativeName = null;
        thePane                   = null;
        theAddArguments           = null;
        theTarget                 = null;
        theTitle                  = null;
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
     * Obtain value of the pane property.
     *
     * @return value of the pane property
     * @see #setPane
     */
    public String getPane()
    {
        return thePane;
    }

    /**
     * Set value of the pane property.
     *
     * @param newValue new value of the pane property
     * @see #getPane
     */
    public void setPane(
            String newValue )
    {
        thePane = newValue;
    }

    /**
     * Obtain value of the addArguments property.
     *
     * @return value of the addArguments property
     * @see #setAddArguments
     */
    public String getAddArguments()
    {
        return theAddArguments;
    }

    /**
     * Set value of the addArguments property.
     *
     * @param newValue new value of the addArguments property
     * @see #getAddArguments
     */
    public void setAddArguments(
            String newValue )
    {
        theAddArguments = newValue;
    }

    /**
     * Obtain value of the target property.
     *
     * @return value of the target property
     * @see #setTarget
     */
    public String getTarget()
    {
        return theTarget;
    }

    /**
     * Set value of the target property.
     *
     * @param newValue new value of the target property
     * @see #getTarget
     */
    public void setTarget(
            String newValue )
    {
        theTarget = newValue;
    }

    /**
     * Obtain value of the title property.
     *
     * @return value of the title property
     * @see #setTitle
     */
    public String getTitle()
    {
        return theTitle;
    }

    /**
     * Set value of the title property.
     *
     * @param newValue new value of the title property
     * @see #getTitle
     */
    public void setTitle(
            String newValue )
    {
        theTitle = newValue;
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
            StringRepresentation rep = getFormatter().determineStringRepresentation( theStringRepresentation );

            SaneRequest saneRequest = SaneServletRequest.create( (HttpServletRequest) pageContext.getRequest() );

            SimpleStringRepresentationParameters pars = SimpleStringRepresentationParameters.create();
            pars.put( StringRepresentationParameters.WEB_ABSOLUTE_CONTEXT_KEY, saneRequest.getAbsoluteContextUri() );
            pars.put( StringRepresentationParameters.WEB_RELATIVE_CONTEXT_KEY, saneRequest.getContextPath() );
            pars.put( StringRepresentationParameters.LINK_TARGET_KEY,          theTarget );
            pars.put( StringRepresentationParameters.LINK_TITLE_KEY,           theTitle );
            pars.put( JeeViewlet.PANE_STRING_REPRESENTATION_PARAMETER_KEY,     thePane );
            pars.put( JeeViewletFactoryChoice.VIEWEDMESHOBJECTS_STACK_KEY,     saneRequest.getAttribute( IncludeViewletTag.PARENT_STACK_ATTRIBUTE_NAME ));

            try {
                String text = choice.toStringRepresentationLinkStart( rep, pars );
                print( text );

            } catch( StringifierException ex ) {
                throw new JspException( ex );
            }
            return EVAL_BODY_INCLUDE;

        } else {
            return SKIP_BODY;
        }
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
        ViewletFactoryChoice choice  = (ViewletFactoryChoice) lookupOrThrow( theViewletAlternativeName );

        if( choice != null ) { // may happen if ignore="true"
            StringRepresentation rep = getFormatter().determineStringRepresentation( theStringRepresentation );
            try {

                String text = choice.toStringRepresentationLinkEnd( rep, StringRepresentationParameters.EMPTY );
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
     * Name of the pane.
     */
    protected String thePane;

    /**
     * The arguments to append to the URL, separated by &.
     */
    protected String theAddArguments;

    /**
     * The HTML frame to target, if any.
     */
    protected String theTarget;

    /**
     * The title of the link, if any.
     */
    protected String theTitle;

    /**
     * Name of the String representation.
     */
    protected String theStringRepresentation;
}
