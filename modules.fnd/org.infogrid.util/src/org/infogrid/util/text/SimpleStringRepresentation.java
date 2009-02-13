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

package org.infogrid.util.text;

import java.util.HashMap;
import java.util.Map;
import org.infogrid.util.ArrayFacade;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.StringHelper;
import org.infogrid.util.http.HTTP;
import org.infogrid.util.logging.Log;

/**
 * Enumerates the different ways of representing Objects as Strings.
 */
public class SimpleStringRepresentation
        implements
            StringRepresentation
{
    private static final Log log = Log.getLogInstance( SimpleStringRepresentation.class ); // our own, private logger

    /**
     * Smart factory method, using the default StringifierMap.
     *
     * @param name the name of the StringRepresentation
     * @return the created StringRepresentation
     */
    public static synchronized SimpleStringRepresentation create(
            String name )
    {
        return create( name, DEFAULT_STRINGIFIER_MAP );
    }

    /**
     * Smart factory method, using the default StringifierMap.
     *
     * @param name the name of the StringRepresentation
     * @param delegate the StringRepresentation to use if this instance cannot perform the operation
     * @return the created StringRepresentation
     */
    public static synchronized SimpleStringRepresentation create(
            String               name,
            StringRepresentation delegate )
    {
        return create( name, DEFAULT_STRINGIFIER_MAP );
    }

    /**
     * Smart factory method.
     *
     * @param name the name of the StringRepresentation
     * @param map the StringifierMap to use
     * @return the created StringRepresentation
     */
    public static synchronized SimpleStringRepresentation create(
            String                                    name,
            Map<String,Stringifier<? extends Object>> map )
    {
        SimpleStringRepresentation ret = new SimpleStringRepresentation( name, map, null );
        return ret;
    }

    /**
     * Smart factory method.
     *
     * @param name the name of the StringRepresentation
     * @param map the StringifierMap to use
     * @param delegate the StringRepresentation to use if this instance cannot perform the operation
     * @return the created StringRepresentation
     */
    public static synchronized SimpleStringRepresentation create(
            String                                    name,
            Map<String,Stringifier<? extends Object>> map,
            StringRepresentation                      delegate )
    {
        SimpleStringRepresentation ret = new SimpleStringRepresentation( name, map, delegate );
        return ret;
    }

    /**
     * Constructor.
     *
     * @param name the name of the StringRepresentation
     * @param map the map of Stringifiers
     * @param delegate the StringRepresentation to use if this instance cannot perform the operation
     */
    protected SimpleStringRepresentation(
            String                                    name,
            Map<String,Stringifier<? extends Object>> map,
            StringRepresentation                      delegate )
    {
        theName           = name;
        theStringifierMap = map;
        theDelegate       = delegate;
    }
    
    /**
     * Obtain the name of the StringRepresentation.
     *
     * @return the name
     */
    public String getName()
    {
        return theName;
    }

    /**
     * Determine whether this String matches this StringRepresentation.
     *
     * @param candidate the candidate String
     * @return true if it matches
     */
    public boolean matches(
            String candidate )
    {
        if( toString().equals( candidate )) {
            return true;
        }
        if( theName.equals( candidate )) {
            return true;
        }
        return false;
    }

    /**
     * Format the parameters according to the rules for classOfFormattedObject,
     * entry entryName and this StringRepresentation
     * 
     * @param classOfFormattedObject the class of the to-be-formatted object
     * @param entry the entry in the ResourceHelper (but qualified by the prefix of this StringRepresentation)
     * @param args the arguments for the entry in the ResourceHelper
     * @return the formatted String
     */
    public String formatEntry(
            Class<? extends HasStringRepresentation> classOfFormattedObject,
            String                                   entry,
            Object...                                args )
    {
        ResourceHelper rh = ResourceHelper.getInstance( classOfFormattedObject, true );
        try {
            String                formatString = rh.getResourceString( theName + entry );
            AnyMessageStringifier stringifier  = AnyMessageStringifier.create( formatString, theStringifierMap );

            String ret = stringifier.format( null, ArrayFacade.<Object>create( args ));
            return ret;

        } catch( StringifierException ex ) {
            log.error( ex );
            return null;
        }
    }

    /**
     * Parse an entry that has been formatted using this StringRepresentation.
     *
     * @param classOfFormattedObject the class of the formatted object
     * @param entry the entry (prefixed by theName) of the resource
     * @param s the to-be-parsed String
     * @return the found values
     * @throws StringifierException thrown if the String could not be parsed.
     */
    public Object [] parseEntry(
            Class<? extends HasStringRepresentation> classOfFormattedObject,
            String                                   entry,
            String                                   s )
        throws
            StringifierException
    {
        ResourceHelper        rh           = ResourceHelper.getInstance( classOfFormattedObject );
        String                formatString = rh.getResourceString( theName + entry );
        AnyMessageStringifier stringifier  = AnyMessageStringifier.create( formatString, theStringifierMap );

        Object [] ret = stringifier.unformat( s ).getArray();
        return ret;
    }

    /**
     * Format a Throwable about which nothing else is known.
     * 
     * @param t the Throwable
     * @param context the StringRepresentationContext to use
     * @return String representation
     */
    public String formatThrowable(
            Throwable                   t,
            StringRepresentationContext context )
    {
        String ret;
        if( t instanceof HasStringRepresentation ) {
            ret = formatHasStringRepresentationThrowable( (HasStringRepresentation) t, context );

        } else {
            ret = formatNoStringRepresentationThrowable( t, context );
        }
        return ret;
    }

    /**
     * Format a Throwable that has a StringRepresentation per declared interface.
     * 
     * @param t the Throwable
     * @param context the StringRepresentationContext to use
     * @return String representation
     */
    public String formatHasStringRepresentationThrowable(
            HasStringRepresentation     t,
            StringRepresentationContext context )
    {
        String ret = t.toStringRepresentation( this, context );
        return ret;
    }

    /**
     * Format a Throwable that does not have a StringRepresentation per declared interface.
     * By default, we format 
     * @param t the Throwable
     * @param context the StringRepresentationContext to use
     * @return String representation
     */
    public String formatNoStringRepresentationThrowable(
            Throwable                   t,
            StringRepresentationContext context )
    {
        try {
            String                formatString = theResourceHelper.getResourceString( theName + "ThrowableMessage" );
            AnyMessageStringifier stringifier  = AnyMessageStringifier.create( formatString, theStringifierMap );

            String message          = t.getMessage();
            String localizedMessage = t.getLocalizedMessage();
            
            if( message == null ) {
                message = t.getClass().getName();
            }
            if( localizedMessage == null ) {
                localizedMessage = t.getClass().getName();
            }

            Object [] args = { message, localizedMessage, t };
            String ret = stringifier.format( null, ArrayFacade.<Object>create( args ));
            return ret;

        } catch( StringifierException ex ) {
            log.error( ex );
            return null;
        }
    }

    /**
     * Determine the delegate StringRepresentation, if any.
     * 
     * @return the delegate StringRepresentation
     */
    public StringRepresentation getDelegate()
    {
        return theDelegate;
    }

    /**
     * Convert to String representation, for debugging.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                    "theName",
                    "theDelegate"
                },
                new Object[] {
                    theName,
                    theDelegate
                } );
    }

    /**
     * The name of the StringRepresentation, which also used as prefix in the ResourceHelper.
     */
    protected String theName;

    /**
     * The StringifierMap to be used with this StringRepresentation.
     */
    protected Map<String,Stringifier<? extends Object>> theStringifierMap;

    /**
     * The delegate StringRepresentation, if any.
     */
    protected StringRepresentation theDelegate;

    /**
     * The default map for the compound stringifier.
     */
    protected static final Map<String,Stringifier<? extends Object>> DEFAULT_STRINGIFIER_MAP;

    static {
        HashMap<String,Stringifier<? extends Object>> map = new HashMap<String,Stringifier<? extends Object>>();
                
        map.put( "int",            LongStringifier.create() );
        map.put( "int2",           LongStringifier.create( 2 ) );
        map.put( "int4",           LongStringifier.create( 4 ) );
        map.put( "string",         StringStringifier.create() );
        map.put( "htmlstring",     new MyHtmlStringStringifier() );
        map.put( "urlstring",      new MyUrlStringStringifier() );
        map.put( "double",         DoubleStringifier.create() );
        map.put( "stacktrace",     StacktraceStringifier.create() );
        map.put( "htmlstacktrace", HtmlStacktraceStringifier.create() );
        map.put( "urlappend",      UrlAppendStringifier.create() );
        DEFAULT_STRINGIFIER_MAP = map;
    }

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( SimpleStringRepresentation.class );
    
    /**
     * Overrides HtmlStringStringifier to make sure we aren't emitting asterisk-slash (the Java
     * end of comment string.
     */
    static class MyHtmlStringStringifier
            extends
                HtmlStringStringifier
    {
        /**
         * Format an Object using this Stringifier. This may be null.
         *
         * @param soFar the String so far, if any
         * @param arg the Object to format, or null
         * @return the formatted String
         */
        @Override
        public String format(
                String soFar,
                String arg )
        {
            String raw = super.format( soFar, arg );

            String ret = raw.replaceAll( "\\*/", "&#42;/" );
            return ret;
        }
    }

    /**
     * Overrides StringStringifier to make Strings suitable for appending as arguments to URLs.
     */
    static class MyUrlStringStringifier
            extends
                StringStringifier
        {
        /**
         * Overridable method to possibly escape a String first.
         *
         * @param s the String to be escaped
         * @return the escaped String
         */
        @Override
        protected String escape(
                String s )
        {
            String ret = HTTP.encodeToValidUrlArgument( s );
            return ret;
        }

        /**
         * Overridable method to possibly unescape a String first.
         *
         * @param s the String to be unescaped
         * @return the unescaped String
         */
        @Override
        protected String unescape(
                String s )
        {
            String ret = HTTP.decodeUrlArgument( s );
            return ret;
        }        
    }
}
