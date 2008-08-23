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

package org.infogrid.model.primitives;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.ArrayStringifier;
import org.infogrid.util.text.HtmlStringStringifier;
import org.infogrid.util.text.Stringifier;
import org.infogrid.util.text.StringifierParseException;
import org.infogrid.util.text.StringifierParsingChoice;
import org.infogrid.util.text.StringRepresentation;

/**
 * Extends the default definitions in StringRepresentation to be aware of the model primitives defined
 * in this package.
 */
public abstract class ModelPrimitivesStringRepresentation
        extends
            StringRepresentation
{
    private static final Log log = Log.getLogInstance( ModelPrimitivesStringRepresentation.class ); // our own, private logger

    /**
     * Smart factory method, using the default StringifierMap.
     *
     * @param tagName the tag name.
     * @return the created StringRepresentation
     */
    public static synchronized StringRepresentation create(
            String tagName )
    {
        return create( tagName, DEFAULT_MODEL_PRIMITIVES_STRINGIFIER_MAP );
    }

    /**
     * Non-functional constructor to make Java happy.
     * 
     * @param tagName the tag name.
     * @param map the map of Stringifiers to use
     */
    protected ModelPrimitivesStringRepresentation(
            String                                    tagName,
            Map<String,Stringifier<? extends Object>> map )
    {
        super( tagName, map );
    }

    /**
     * The default map for the compound stringifier.
     */
    public static final Map<String,Stringifier<? extends Object>> DEFAULT_MODEL_PRIMITIVES_STRINGIFIER_MAP;

    static {
        HashMap<String,Stringifier<? extends Object>> map = new HashMap<String,Stringifier<? extends Object>>();
        
        for( String current : DEFAULT_STRINGIFIER_MAP.keySet() ) {
            map.put( current, DEFAULT_STRINGIFIER_MAP.get( current ));
        }

        map.put( "enumarray",            ArrayStringifier.create( new EnumeratedValueStringifier(), ", " ));
        map.put( "htmlenumarray",        ArrayStringifier.create( new EnumeratedValueStringifier(), "<ul><li>", "</li><li>", "</li></ul>", "" ));
        map.put( "escapehashhtmlstring", new MyEscapeHashHtmlStringStringifier() );
        map.put( "java",                 new JavaStringifier() );

        DEFAULT_MODEL_PRIMITIVES_STRINGIFIER_MAP = map;
    }
    
    /**
     * Represent as plain text.
     */
    public static final StringRepresentation TEXT_PLAIN = create( "Plain", DEFAULT_MODEL_PRIMITIVES_STRINGIFIER_MAP );
    
    /**
     * Represent as Html text.
     */
    public static final StringRepresentation TEXT_HTML = create( "Html", DEFAULT_MODEL_PRIMITIVES_STRINGIFIER_MAP );

    /**
     * Stringifies EnumeratedValues.
     */
    public static class EnumeratedValueStringifier
            implements Stringifier<EnumeratedValue>
    {
        /**
         * Format an Object using this Stringifier. This may be null.
         *
         * @param arg the Object to format, or null
         * @return the formatted String
         */
        public String format(
                EnumeratedValue arg )
        {
            String ret = arg.getUserVisibleName().value();
            return ret;
        }

        /**
         * Format an Object using this Stringifier. This may be null.
         *
         * @param arg the Object to format, or null
         * @return the formatted String
         * @throws ClassCastException thrown if this Stringifier could not format the provided Object
         *         because the provided Object was not of a type supported by this Stringifier
         */
        public String attemptFormat(
                Object arg )
            throws
                ClassCastException
        {
            return format( (EnumeratedValue) arg );
        }

        /**
         * Parse out the Object in rawString that were inserted using this Stringifier.
         *
         * @param rawString the String to parse
         * @return the found Object
         * @throws StringifierParseException thrown if a parsing problem occurred
         */
        public EnumeratedValue unformat(
                String rawString )
            throws
                StringifierParseException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * Obtain an iterator that iterates through all the choices that exist for this Stringifier to
         * parse the String.
         *
         * @param rawString the String to parse
         * @param startIndex the position at which to parse rawString
         * @param endIndex the position at which to end parsing rawString
         * @param max the maximum number of choices returned by the Iterator.
         * @param matchAll if true, only return those matches that match the entire String from startIndex to endIndex.
         *                 If false, return other matches that only match the beginning of the String.
         * @return the Iterator
         */
        public Iterator<StringifierParsingChoice<EnumeratedValue>> parsingChoiceIterator(
                final String  rawString,
                final int     startIndex,
                final int     endIndex,
                final int     max,
                final boolean matchAll )
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Overrides StringStringifier to make Strings suitable for appending as arguments to URLs.
     */
    static class MyEscapeHashHtmlStringStringifier
            extends
                HtmlStringStringifier
    {
        /**
         * Escape HTML characters in String. Inspired by at http://www.rgagnon.com/javadetails/java-0306.html
         *
         * @param s the unescaped String
         * @return the escaped String
         */
        @Override
        protected String stringToHtml(
                String s )
        {
            String ret = super.stringToHtml( s );
            ret = ret.replaceAll( "#", "%23" );

            return ret;
        }

        /**
         * Unescape HTML escape characters in String.
         *
         * @param s the escaped String
         * @return the unescaped String
         */
        @Override
        protected String htmlToString(
                String s )
        {
            s = s.replaceAll( "%23", "%" );
            String ret = super.htmlToString( s );

            return ret;
        }
    }
    
    /**
     * A Stringifier to stringify PropertyValues into Java syntax. The reverse is currently NOT supported.
     * (FIXME. But: beware of code injection attacks)
     */
    static class JavaStringifier
            implements
                Stringifier<PropertyValue>
    {
        /**
         * Format an Object using this Stringifier.
         *
         * @param arg the Object to format, or null
         * @return the formatted String
         */
        public String format(
                PropertyValue arg )
        {
            String ret = arg.getJavaConstructorString( "loader", "type" );
            return ret;
        }

        /**
         * Format an Object using this Stringifier. This may be null.
         *
         * @param arg the Object to format, or null
         * @return the formatted String
         * @throws ClassCastException thrown if this Stringifier could not format the provided Object
         *         because the provided Object was not of a type supported by this Stringifier
         */
        public String attemptFormat(
                Object arg )
            throws
                ClassCastException
        {
            if( arg == null ) {
                return "null";
            } else if( arg instanceof PropertyValue ) {
                return format( (PropertyValue) arg );
            } else if( arg instanceof String ) {
                return StringValue.encodeAsJavaString( (String) arg );
            } else if( arg instanceof Number ) {
                return String.valueOf( arg );
            } else {
                log.error( "Cannot format " + arg + " (class: " + arg.getClass().getName() + ")" );
                return "null";
            }
        }

        /**
         * Parse out the Object in rawString that were inserted using this Stringifier.
         *
         * @param rawString the String to parse
         * @return the found Object
         * @throws StringifierParseException thrown if a parsing problem occurred
         */
        public PropertyValue unformat(
                String rawString )
            throws
                StringifierParseException
        {
            throw new UnsupportedOperationException();
        }

        /**
         * Obtain an iterator that iterates through all the choices that exist for this Stringifier to
         * parse the String. The iterator returns zero elements if the String could not be parsed
         * by this Stringifier.
         *
         * @param rawString the String to parse
         * @param startIndex the position at which to parse rawString
         * @param endIndex the position at which to end parsing rawString
         * @param max the maximum number of choices to be returned by the Iterator.
         * @param matchAll if true, only return those matches that match the entire String from startIndex to endIndex.
         *                 If false, return other matches that only match the beginning of the String.
         * @return the Iterator
         */
        public Iterator<StringifierParsingChoice<PropertyValue>> parsingChoiceIterator(
                String  rawString,
                int     startIndex,
                int     endIndex,
                int     max,
                boolean matchAll )
        {
            throw new UnsupportedOperationException();
        }
    }
}