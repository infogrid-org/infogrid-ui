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

import org.infogrid.util.ArrayFacade;
import org.infogrid.util.ResourceHelper;

import org.infogrid.util.logging.Log;

import org.infogrid.util.http.HTTP;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumerates the different ways of representing Objects as Strings.
 */
public class StringRepresentation
{
    private static final Log log = Log.getLogInstance( StringRepresentation.class ); // our own, private logger

    /**
     * Smart factory method, using the default StringifierMap.
     *
     * @param tagName the tag name.
     * @return the created StringRepresentation
     */
    public static synchronized StringRepresentation create(
            String tagName )
    {
        return create( tagName, DEFAULT_STRINGIFIER_MAP );
    }

    /**
     * Smart factory method.
     *
     * @param tagName the tag name.
     * @param map the StringifierMap to use
     * @return the created StringRepresentation
     */
    public static synchronized StringRepresentation create(
            String                                    tagName,
            Map<String,Stringifier<? extends Object>> map )
    {
        StringRepresentation ret = theRepresentations.get( tagName );

        if( ret == null ) {
            ret = new StringRepresentation( tagName, map );
            theRepresentations.put( tagName, ret );

        } else if( ret.theStringifierMap != map ) {
            log.warn( "Smart factory method invoked with different stringifier map" );
        }

        return ret;
    }

    /**
     * Constructor.
     *
     * @param prefix name of the prefix in the resource file.
     * @param map the map of Stringifiers
     */
    protected StringRepresentation(
            String                                    prefix,
            Map<String,Stringifier<? extends Object>> map )
    {
        thePrefix         = prefix;
        theStringifierMap = map;
    }
    
    /**
     * Obtain the name of the StringRepresentation.
     *
     * @return the name
     */
    public String getName()
    {
        return thePrefix;
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
        if( thePrefix.equals( candidate )) {
            return true;
        }
        return false;
    }

    /**
     * Format the parameters using the ResourceHelper rh's entry entryName and this
     * StringRepresentation.
     * 
     * @param rh the ResourceHelper to use
     * @param entry the entry in the ResourceHelper (but qualified by the prefix of this StringRepresentation)
     * @param args the arguments for the entry in the ResourceHelper
     * @return the formatted String
     */
    public String formatEntry(
            ResourceHelper rh,
            String         entry,
            Object...      args )
    {
        try {
            String                formatString = rh.getResourceString( thePrefix + entry );
            AnyMessageStringifier stringifier  = AnyMessageStringifier.create( formatString, theStringifierMap );

            String ret = stringifier.format( ArrayFacade.<Object>create( args ));
            return ret;

        } catch( StringifierException ex ) {
            log.error( ex );
            return null;
        }
    }

    /**
     * Parse an entry that has been formatted using this StringRepresentation.
     *
     * @param rh the ResourceHelper in which to find the MessageFormat
     * @param entry the entry (prefixed by thePrefix) of the resource
     * @param s the to-be-parsed String
     * @return the found values
     * @throws StringifierException thrown if the String could not be parsed.
     */
    public Object [] parseEntry(
            ResourceHelper rh,
            String         entry,
            String         s )
        throws
            StringifierException
    {
        String                formatString = rh.getResourceString( thePrefix + entry );
        AnyMessageStringifier stringifier  = AnyMessageStringifier.create( formatString, theStringifierMap );

        Object [] ret = stringifier.unformat( s ).getArray();
        return ret;
    }

    /**
     * The prefix in the ResourceHelper.
     */
    protected String thePrefix;

    /**
     * The StringifierMap to be used with this StringRepresentation.
     */
    protected Map<String,Stringifier<? extends Object>> theStringifierMap;

    /**
     * The known StringRepresentations.
     */
    protected static HashMap<String,StringRepresentation> theRepresentations
            = new HashMap<String,StringRepresentation>();

    /**
     * The default map for the compound stringifier.
     */
    public static final Map<String,Stringifier<? extends Object>> DEFAULT_STRINGIFIER_MAP;

    static {
        HashMap<String,Stringifier<? extends Object>> map = new HashMap<String,Stringifier<? extends Object>>();
                
        map.put( "int",            LongStringifier.create() );
        map.put( "int2",           LongStringifier.create( 2 ) );
        map.put( "int4",           LongStringifier.create( 4 ) );
        map.put( "string",         StringStringifier.create() );
        map.put( "htmlstring",     new MyHtmlStringStringifier() );
        map.put( "urlstring",      new MyUrlStringStringifier() );
        map.put( "double",         DoubleStringifier.create() );

        DEFAULT_STRINGIFIER_MAP = map;
    }

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
         * @param arg the Object to format, or null
         * @return the formatted String
         */
        @Override
        public String format(
                String arg )
        {
            String raw = super.format( arg );

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
