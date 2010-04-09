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

package org.infogrid.jee.templates;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.http.SaneRequestUtils;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;

/**
 * Encapsulates the content of an HTTP response in structured form.
 */
public class StructuredResponse
        implements
            HasHeaderPreferences,
            CanBeDumped
{
    private static final Log log = Log.getLogInstance( StructuredResponse.class ); // our own, private logger

    /**
     * Factory method.
     *
     * @param delegate the underlying HttpServletResponse
     * @param servletContext the ServletContext in which the StructuredResponse is created
     * @return the created StructuredResponse
     */
    public static StructuredResponse create(
            HttpServletResponse delegate,
            ServletContext      servletContext )
    {
        StructuredResponse ret = new StructuredResponse( delegate, servletContext, DEFAULT_MAX_PROBLEMS );
        return ret;
    }

    /**
     * Factory method.
     *
     * @param delegate the underlying HttpServletResponse
     * @param servletContext the ServletContext in which the StructuredResponse is created
     * @param maxProblems the maxmimum number of problems to report in this StructuredResponse
     * @return the created StructuredResponse
     */
    public static StructuredResponse create(
            HttpServletResponse delegate,
            ServletContext      servletContext,
            int                 maxProblems )
    {
        StructuredResponse ret = new StructuredResponse( delegate, servletContext, maxProblems );
        return ret;
    }

    /**
     * Constructor for subclasses only, use factory method.
     *
     * @param delegate the underlying HttpServletResponse
     * @param servletContext the ServletContext in which the StructuredResponse is created
     * @param maxProblems the maxmimum number of problems to report in this StructuredResponse
     */
    protected StructuredResponse(
            HttpServletResponse delegate,
            ServletContext      servletContext,
            int                 maxProblems )
    {
        theDelegate       = delegate;
        theServletContext = servletContext;
        theMaxProblems    = maxProblems;
    }
    
    /**
     * Obtain the underlying HttpServletResponse.
     * 
     * @return the delegate
     */
    public HttpServletResponse getDelegate()
    {
        return theDelegate;
    }

    /**
     * Obtain the default text section.
     * 
     * @return the default text section
     */
    public TextStructuredResponseSection getDefaultTextSection()
    {
        return obtainTextSection( TEXT_DEFAULT_SECTION );
    }

    /**
     * Obtain the default binary section.
     * 
     * @return the default binary section
     */
    public BinaryStructuredResponseSection getDefaultBinarySection()
    {
        return obtainBinarySection( BINARY_DEFAULT_SECTION );
    }

    /**
     * Obtain a text section; if the section does not exist, create it.
     * 
     * @param template the section type
     * @return the section
     */
    public TextStructuredResponseSection obtainTextSection(
            TextStructuredResponseSectionTemplate template )
    {
        if( template == null ) {
            throw new NullPointerException( "Cannot obtain null section" );
        }
        TextStructuredResponseSection ret = theTextSections.get( template );
        if( ret == null ) {
            ret = TextStructuredResponseSection.create( template );
            theTextSections.put( template, ret );
        }
        return ret;
    }

    /**
     * Obtain a binary section; if the section does not exist, create it.
     * 
     * @param template the section type
     * @return the section
     */
    public BinaryStructuredResponseSection obtainBinarySection(
            BinaryStructuredResponseSectionTemplate template )
    {
        if( template == null ) {
            throw new NullPointerException( "Cannot obtain null section" );
        }
        BinaryStructuredResponseSection ret = theBinarySections.get( template );
        if( ret == null ) {
            ret = BinaryStructuredResponseSection.create( template );
            theBinarySections.put( template, ret );
        }
        return ret;
    }

    /**
     * Get a text section; if the section does not exist, return null.
     *
     * @param template the section type
     * @return the section, or null
     */
    public TextStructuredResponseSection getTextSection(
            TextStructuredResponseSectionTemplate template )
    {
        if( template == null ) {
            throw new NullPointerException( "Cannot obtain null section" );
        }
        TextStructuredResponseSection ret = theTextSections.get( template );
        return ret;
    }

    /**
     * Obtain a binary section; if the section does not exist, return null.
     *
     * @param template the section type
     * @return the section, or null
     */
    public BinaryStructuredResponseSection getBinarySection(
            BinaryStructuredResponseSectionTemplate template )
    {
        if( template == null ) {
            throw new NullPointerException( "Cannot obtain null section" );
        }
        BinaryStructuredResponseSection ret = theBinarySections.get( template );
        return ret;
    }

    /**
     * Obtain a text section by name; if the section does not exist, create it.
     * 
     * @param name the name of the section
     * @return the section
     */
    public TextStructuredResponseSection obtainTextSectionByName(
            String name )
    {
        TextStructuredResponseSectionTemplate template = obtainTextSectionTemplateByName( name );
        if( template == null ) {
            return null;
        }
        TextStructuredResponseSection ret = obtainTextSection( template );
        return ret;
    }

    /**
     * Obtain a binary section by name; if the section does not exist, create it.
     * 
     * @param name the name of the section
     * @return the section
     */
    public BinaryStructuredResponseSection obtainBinarySectionByName(
            String name )
    {
        BinaryStructuredResponseSectionTemplate template = obtainBinarySectionTemplateByName( name );
        if( template == null ) {
            return null;
        }
        BinaryStructuredResponseSection ret = obtainBinarySection( template );
        return ret;
    }

    /**
     * Obtain a text section template by name; if it does not exist, create it.
     *
     * @param name the name of the section
     * @return the section template
     */
    public TextStructuredResponseSectionTemplate obtainTextSectionTemplateByName(
            String name )
    {
        for( TextStructuredResponseSectionTemplate current : theTextSections.keySet() ) {
            if( name.equals( current.getSectionName() )) {
                return current;
            }
        }
        TextStructuredResponseSectionTemplate ret
                = createTextStructuredResponseSectionTemplate( name );
        return ret;
    }

    /**
     * Obtain a binary section template by name; if it does not exist, create it.
     *
     * @param name the name of the section
     * @return the section template
     */
    public BinaryStructuredResponseSectionTemplate obtainBinarySectionTemplateByName(
            String name )
    {
        for( BinaryStructuredResponseSectionTemplate current : theBinarySections.keySet() ) {
            if( name.equals( current.getSectionName() )) {
                return current;
            }
        }
        BinaryStructuredResponseSectionTemplate ret
                = createBinaryStructuredResponseSectionTemplate( name );
        return ret;
    }

    /**
     * Get a text section template by name; if it does not exist, return null.
     *
     * @param name the name of the section
     * @return the section template, if any
     */
    public TextStructuredResponseSectionTemplate getTextSectionTemplateByName(
            String name )
    {
        for( TextStructuredResponseSectionTemplate current : theTextSections.keySet() ) {
            if( name.equals( current.getSectionName() )) {
                return current;
            }
        }
        return null;
    }

    /**
     * Get a binary section template by name; if it does not exist, return null.
     *
     * @param name the name of the section
     * @return the section template, if any
     */
    public BinaryStructuredResponseSectionTemplate getBinarySectionTemplateByName(
            String name )
    {
        for( BinaryStructuredResponseSectionTemplate current : theBinarySections.keySet() ) {
            if( name.equals( current.getSectionName() )) {
                return current;
            }
        }
        return null;
    }

    /**
     * Obtain an Iterator over the text section templates currently used.
     *
     * @return the Iterator
     */
    public Iterator<TextStructuredResponseSectionTemplate> textSectionTemplateIterator()
    {
        return theTextSections.keySet().iterator();
    }

    /**
     * Obtain an Iterator over the binary section templates currently used.
     *
     * @return the Iterator
     */
    public Iterator<BinaryStructuredResponseSectionTemplate> binarySectionTemplateIterator()
    {
        return theBinarySections.keySet().iterator();
    }

    /**
     * Create a TextStructuredResponseSectionTemplate for which only a name is known.
     *
     * @param name the name of the to-be-created TextStructuredResponseSectionTemplate
     * @return the created TextStructuredResponseSectionTemplate
     */
    protected TextStructuredResponseSectionTemplate createTextStructuredResponseSectionTemplate(
            String name )
    {
        TextStructuredResponseSectionTemplate ret;
        if( TEXT_DEFAULT_SECTION.getSectionName().equals( name )) {
            ret = TEXT_DEFAULT_SECTION;
        } else {
            ret = TextStructuredResponseSectionTemplate.create( name );
        }
        return ret;
    }

    /**
     * Create a BinaryStructuredResponseSectionTemplate for which only a name is known.
     *
     * @param name the name of the to-be-created BinaryStructuredResponseSectionTemplate
     * @return the created BinaryStructuredResponseSectionTemplate
     */
    protected BinaryStructuredResponseSectionTemplate createBinaryStructuredResponseSectionTemplate(
            String name )
    {
        BinaryStructuredResponseSectionTemplate ret;
        if( BINARY_DEFAULT_SECTION.getSectionName().equals( name )) {
            ret = BINARY_DEFAULT_SECTION;
        } else {
            ret = BinaryStructuredResponseSectionTemplate.create( name );
        }
        return ret;
    }

    /**
     * Helper method to find the cause of an Exception.
     * Unfortunately JspExceptions do getRootCause() instead of getCause().
     *
     * @param t the incoming Throwable whose cause we need to determine
     * @return the found cause, or null
     */
    protected Throwable findCause(
            Throwable t )
    {
        Throwable ret;
        if( t instanceof ServletException ) {
            ret = ((ServletException)t).getRootCause();
        } else {
            ret = t.getCause();
        }
        return ret;
    }

    /**
     * Quote HTML angle brackets so we can insert them into a &lt;pre&gt; element.
     *
     * @param in the unquoted String
     * @return the quoted String
     */
    protected String quoteAngleBrackets(
            String in )
    {
        String ret = in.replaceAll( "<", "&lt;" );
        ret = ret.replaceAll( ">", "&gt;" );
        return ret;
    }

    /**
     * Report a problem that should be shown to the user.
     *
     * @param t the Throwable indicating the problem
     */
    public void reportProblem(
            Throwable t )
    {
        if( log.isDebugEnabled() ) { 
            log.debug( "Reporting problem: ", t );
        }
        if( theCurrentProblems.size() <= theMaxProblems ) {
            // make sure we aren't growing this indefinitely
            theCurrentProblems.add( t );

        } else {
            log.error( "Too many problems. Ignored ", t ); // late initialization
        }
    }

    /**
     * Convenience method to report several problems that should be shown to the user.
     *
     * @param ts [] the Throwables indicating the problems
     */
    public void reportProblems(
            Throwable [] ts )
    {
        for( int i=0 ; i<ts.length ; ++i ) {
            reportProblem( ts[i] );
        }
    }
    
    /**
     * Determine whether problems have been reported.
     * 
     * @return true if at least one problem has been reported
     */
    public boolean haveProblemsBeenReported()
    {
        if( !theCurrentProblems.isEmpty() ) {
            return true;
        }
        return false;
    }

    /**
     * Determine whether problems have been reported here and in all contained sections.
     * 
     * @return true if at least one problem has been reported
     */
    public boolean haveProblemsBeenReportedAggregate()
    {
        if( !theCurrentProblems.isEmpty() ) {
            return true;
        }
        
        for( TextStructuredResponseSection current : theTextSections.values() ) {
            if( current.haveProblemsBeenReported() ) {
                return true;
            }
        }
        for( BinaryStructuredResponseSection current : theBinarySections.values() ) {
            if( current.haveProblemsBeenReported() ) {
                return true;
            }
        }

        return false;
    }

    /**
     * Obtain the problems reported so far.
     * 
     * @return problems reported so far, in sequence
     */
    public List<Throwable> problems()
    {
        ArrayList<Throwable> ret =  new ArrayList<Throwable>();
        ret.addAll( theCurrentProblems );

        return ret;
    }

    /**
     * Obtain the problems reported so far here and in all contained sections.
     * 
     * @return problems reported so far
     */
    public List<Throwable> problemsAggregate()
    {
        ArrayList<Throwable> ret =  new ArrayList<Throwable>();
        ret.addAll( theCurrentProblems );

        for( TextStructuredResponseSection current : theTextSections.values() ) {
            ret.addAll( current.problems() );
        }
        for( BinaryStructuredResponseSection current : theBinarySections.values() ) {
            ret.addAll( current.problems() );
        }

        return ret;
    }

    /**
     * Obtain the desired MIME type.
     * 
     * @return the desired MIME type
     */
    public String getMimeType()
    {
        return theMimeType;
    }

    /**
     * Set the desired MIME type.
     *
     * @param newValue the new value
     */
    public void setMimeType(
            String newValue )
    {
        theMimeType = newValue;
    }

    /**
     * Obtain the Cookies.
     * 
     * @return the Cookies
     */
    public Collection<Cookie> getCookies()
    {
        return theCookies;
    }

    /**
     * Add a Cookie.
     *
     * @param toAdd the Cookie to add
     */
    public void addCookie(
            Cookie toAdd )
    {
        theCookies.add( toAdd );
    }

    /**
     * Obtain the location header.
     * 
     * @return the currently set location header
     */
    public String getLocation()
    {
        return theLocation;
    }

    /**
     * Set a redirect location.
     *
     * @param newValue the new value
     */
    public void setLocation(
            String newValue )
    {
        theLocation = newValue;
    }

    /**
     * Obtain the HTTP response code.
     * 
     * @return the HTTP response code
     */
    public int getHttpResponseCode()
    {
        return theHttpResponseCode;
    }

    /**
     * Set the desired HTTP response code.
     *
     * @param newValue the new value
     */
    public void setHttpResponseCode(
            int newValue )
    {
        theHttpResponseCode = newValue;
    }

    /**
     * Obtain the locale.
     * 
     * @return the locale
     */
    public Locale getLocale()
    {
        return theLocale;
    }

    /**
     * Set the locale.
     *
     * @param newValue the new value
     */
    public void setLocale(
            Locale newValue )
    {
        theLocale = newValue;
    }

    /**
     * Obtain the character encoding.
     * 
     * @return the character encoding
     */
    public String getCharacterEncoding()
    {
        return theCharacterEncoding;
    }

    /**
     * Set the character encoding.
     *
     * @param newValue the new value
     */
    public void setCharacterEncoding(
            String newValue )
    {
        theCharacterEncoding = newValue;
    }

    /**
     * Obtain the ServletContext whithin this response is being assembled.
     * 
     * @return the ServletContext
     */
    public ServletContext getServletContext()
    {
        return theServletContext;
    }

    /**
     * Set the name of the template that is being requested. Null represents "default".
     * 
     * @param newValue the name of the template that is being requested
     */
    public void setRequestedTemplateName(
            String newValue )
    {
        theRequestedTemplateName = newValue;
    }
    
    /**
     * Obtain the name of the template that is being requested. Null represents "default".
     * 
     * @return the name of the template that is being requested
     */
    public String getRequestedTemplateName()
    {
        return theRequestedTemplateName;
    }

    /**
     * Add an additional header.
     *
     * @param name name of the header to add
     * @param value value of the header to add
     */
    public void addHeader(
            String name,
            String value )
    {
        addHeader( name, new String[] { value } );
    }

    /**
     * Add an additional header.
     *
     * @param name name of the header to add
     * @param value value of the header to add
     */
    public void addHeader(
            String    name,
            String [] value )
    {
        String [] already = theOutgoingHeaders.put( name, value );
        if( already != null && already.length > 0 ) {
            theOutgoingHeaders.put( name, ArrayHelper.append( already, value, String.class ));
        }
    }

    /**
     * Obtain the additional headers.
     *
     * @return the headers, as Map
     */
    public Map<String,String[]> getHeaders()
    {
        return theOutgoingHeaders;
    }

    /**
     * Determine whether this StructuredResponse is empty.
     * 
     * @return true if it is empty
     */
    public boolean isEmpty()
    {
        if( theHttpResponseCode > 0 && theHttpResponseCode != 200 ) {
            return false;
        }
        if( haveProblemsBeenReported()) {
            return false;
        }
        for( TextStructuredResponseSectionTemplate key : theTextSections.keySet() ) {
            TextStructuredResponseSection value = theTextSections.get(  key );
            if( !value.isEmpty() ) {
                return false;
            }
        }
        for( BinaryStructuredResponseSectionTemplate key : theBinarySections.keySet() ) {
            BinaryStructuredResponseSection value = theBinarySections.get(  key );
            if( !value.isEmpty() ) {
                return false;
            }
        }
        return true;
    }

    /**
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
                new String [] {
                    "theRequestedTemplateName",
                    "theCurrentProblems",
                    "theMimeType",
                    "theCookies",
                    "theLocation",
                    "theHttpResponseCode",
                    "theLocale",
                    "theCharacterEncoding",
                    "theOutgoingHeaders"
                },
                new Object [] {
                    theRequestedTemplateName,
                    theCurrentProblems,
                    theMimeType,
                    theCookies,
                    theLocation,
                    theHttpResponseCode,
                    theLocale,
                    theCharacterEncoding,
                    theOutgoingHeaders
                });
    }

    /**
     * The underlying servlet response.
     */
    protected HttpServletResponse theDelegate;

    /**
     * The sections of the response that are represented as text.
     */
    protected HashMap<TextStructuredResponseSectionTemplate,TextStructuredResponseSection> theTextSections
            = new HashMap<TextStructuredResponseSectionTemplate,TextStructuredResponseSection>();

    /**
     * The sections of the response that are represented as binary.
     */
    protected HashMap<BinaryStructuredResponseSectionTemplate,BinaryStructuredResponseSection> theBinarySections
            = new HashMap<BinaryStructuredResponseSectionTemplate,BinaryStructuredResponseSection>();

    /**
     * The ServletContext within which this response is assembled.
     */
    protected ServletContext theServletContext;

    /**
     * Name of the template that is being requested, if any.
     */
    protected String theRequestedTemplateName = null;

    /**
     * The current problems, in sequence of occurrence.
     */
    protected ArrayList<Throwable> theCurrentProblems = new ArrayList<Throwable>();

    /**
     * The maximum number of problems to store in this type of section.
     */
    protected int theMaxProblems;

    /**
     * The desired MIME type. Currently not used.
     */
    protected String theMimeType;
    
    /**
     * The desired cookies. Currently not used.
     */
    protected Collection<Cookie> theCookies = new ArrayList<Cookie>();

    /**
     * The desired location header.
     */
    protected String theLocation;

    /**
     * The desired HTTP response code.
     */
    protected int theHttpResponseCode = -1;
    
    /**
     * The desired locale.
     */
    protected Locale theLocale;

    /**
     * The desired character encoding.
     */
    protected String theCharacterEncoding;

    /**
     * The outgoing headers.
     */
    protected HashMap<String,String[]> theOutgoingHeaders = new HashMap<String,String[]>();

    /**
     * Name of the request attribute that contains the StructuredResponse. Make sure
     * this constant does not contain any characters that might make some processor
     * interpret it as being an expression.
     */
    public static final String STRUCTURED_RESPONSE_ATTRIBUTE_NAME
            = SaneRequestUtils.classToAttributeName( StructuredResponse.class );

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( StructuredResponse.class );

    /**
     * The default maximum number of problems to store.
     */
    public static final int DEFAULT_MAX_PROBLEMS = theResourceHelper.getResourceIntegerOrDefault( "DefaultMaxProblems", 20 );


    /**
     * The single default section for text content. Output will be written into this section
     * unless otherwise specified.
     */
    public static final TextStructuredResponseSectionTemplate TEXT_DEFAULT_SECTION
            = TextStructuredResponseSectionTemplate.create( "text-default" );

    /**
     * The single default section for binary content. Binary output will be written into this section
     * unless otherwise specified.
     */
    public static final BinaryStructuredResponseSectionTemplate BINARY_DEFAULT_SECTION
            = BinaryStructuredResponseSectionTemplate.create( "binary-default" );

    /**
     * The section representing the head of an HTML document.
     */
    public static final TextHtmlStructuredResponseSectionTemplate HTML_HEAD_SECTION
            = TextHtmlStructuredResponseSectionTemplate.create( "html-head" );

    /**
     * The section representing the messages section of an HTML document.
     */
    public static final TextHtmlStructuredResponseSectionTemplate HTML_MESSAGES_SECTION
            = TextHtmlStructuredResponseSectionTemplate.create( "html-messages" );

    /**
     * The section representing the main menu in an HTML document. Many HTML documents don't have
     * such a section, but it is common enough that we make it explicit here.
     */
    public static final TextHtmlStructuredResponseSectionTemplate HTML_MAIN_MENU_SECTION
            = TextHtmlStructuredResponseSectionTemplate.create( "html-main-menu" );}
