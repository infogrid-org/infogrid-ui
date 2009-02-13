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

package org.infogrid.model.primitives.text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.infogrid.model.primitives.EnumeratedValue;
import org.infogrid.model.primitives.MultiplicityValue;
import org.infogrid.model.primitives.PropertyValue;
import org.infogrid.model.primitives.StringValue;
import org.infogrid.util.OneElementIterator;
import org.infogrid.util.ZeroElementCursorIterator;
import org.infogrid.util.logging.Log;
import org.infogrid.util.text.ArrayStringifier;
import org.infogrid.util.text.HtmlStringStringifier;
import org.infogrid.util.text.SimpleStringRepresentation;
import org.infogrid.util.text.Stringifier;
import org.infogrid.util.text.StringifierParseException;
import org.infogrid.util.text.StringifierParsingChoice;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringifierValueParsingChoice;

/**
 * Extends the default definitions in StringRepresentation to be aware of the model primitives defined
 * in this package.
 */
public class SimpleModelPrimitivesStringRepresentation
        extends
            SimpleStringRepresentation
{
    private static final Log log = Log.getLogInstance( SimpleModelPrimitivesStringRepresentation.class  ); // our own, private logger


    /**
     * Smart factory method, using the default StringifierMap.
     *
     * @param name the name of the StringRepresentation
     * @return the created StringRepresentation
     */
    public static synchronized SimpleModelPrimitivesStringRepresentation create(
            String name )
    {
        return create( name, DEFAULT_MODEL_PRIMITIVES_STRINGIFIER_MAP );
    }

    /**
     * Smart factory method, using the default StringifierMap.
     *
     * @param name the name of the StringRepresentation
     * @param delegate the StringRepresentation to use if this instance cannot perform the operation
     * @return the created StringRepresentation
     */
    public static synchronized SimpleModelPrimitivesStringRepresentation create(
            String               name,
            StringRepresentation delegate )
    {
        return create( name, DEFAULT_MODEL_PRIMITIVES_STRINGIFIER_MAP );
    }

    /**
     * Smart factory method.
     *
     * @param name the name of the StringRepresentation
     * @param map the StringifierMap to use
     * @return the created StringRepresentation
     */
    public static synchronized SimpleModelPrimitivesStringRepresentation create(
            String                                    name,
            Map<String,Stringifier<? extends Object>> map )
    {
        SimpleModelPrimitivesStringRepresentation ret = new SimpleModelPrimitivesStringRepresentation( name, map, null );
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
        SimpleModelPrimitivesStringRepresentation ret = new SimpleModelPrimitivesStringRepresentation( name, map, delegate );
        return ret;
    }
    
    /**
     * Constructor.
     *
     * @param name the name of the StringRepresentation
     * @param map the map of Stringifiers
     * @param delegate the StringRepresentation to use if this instance cannot perform the operation
     */
    protected SimpleModelPrimitivesStringRepresentation(
            String                                    name,
            Map<String,Stringifier<? extends Object>> map,
            StringRepresentation                      delegate )
    {
        super( name, map, delegate );
    }

    /**
     * The default map for the compound stringifier.
     */
    protected static final Map<String,Stringifier<? extends Object>> DEFAULT_MODEL_PRIMITIVES_STRINGIFIER_MAP;

    static {
        HashMap<String,Stringifier<? extends Object>> map = new HashMap<String,Stringifier<? extends Object>>();
        
        for( String current : DEFAULT_STRINGIFIER_MAP.keySet() ) {
            map.put( current, DEFAULT_STRINGIFIER_MAP.get( current ));
        }

        map.put( "multiplicity",         new MyMultiplicityValueStringStringifier() );
        map.put( "enumarray",            ArrayStringifier.create( new EnumeratedValueStringifier(), ", " ));
        map.put( "htmlenumarray",        ArrayStringifier.create( new EnumeratedValueStringifier(), "<ul><li>", "</li><li>", "</li></ul>", "" ));
        map.put( "escapehashhtmlstring", new MyEscapeHashHtmlStringStringifier() );
        map.put( "java",                 new JavaStringifier() );

        DEFAULT_MODEL_PRIMITIVES_STRINGIFIER_MAP = map;
    }
    
    /**
     * Stringifies EnumeratedValues.
     */
    public static class EnumeratedValueStringifier
            implements
                Stringifier<EnumeratedValue>
    {
        /**
         * Format an Object using this Stringifier. This may be null.
         *
         * @param soFar the String so far, if any
         * @param arg the Object to format, or null
         * @return the formatted String
         */
        public String format(
                String          soFar,
                EnumeratedValue arg )
        {
            String ret = arg.getUserVisibleName().value();
            return ret;
        }

        /**
         * Format an Object using this Stringifier. This may be null.
         *
         * @param soFar the String so far, if any
         * @param arg the Object to format, or null
         * @return the formatted String
         * @throws ClassCastException thrown if this Stringifier could not format the provided Object
         *         because the provided Object was not of a type supported by this Stringifier
         */
        public String attemptFormat(
                String soFar,
                Object arg )
            throws
                ClassCastException
        {
            return format( soFar, (EnumeratedValue) arg );
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
            String s   = super.format( soFar, arg );
            String ret = s.replaceAll( "#", "%23" );

            return ret;
        }

        /**
         * Parse out the Object in rawString that were inserted using this Stringifier.
         *
         * @param rawString the String to parse
         * @return the found Object
         * @throws StringifierParseException thrown if a parsing problem occurred
         */
        @Override
        public String unformat(
                String rawString )
            throws
                StringifierParseException
        {
            String s   = rawString.replaceAll( "%23", "%" );
            String ret = super.unformat( s );

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
         * @param soFar the String so far, if any
         * @param arg the Object to format, or null
         * @return the formatted String
         */
        public String format(
                String        soFar,
                PropertyValue arg )
        {
            String ret = arg.getJavaConstructorString( "loader", "type" );
            return ret;
        }

        /**
         * Format an Object using this Stringifier. This may be null.
         *
         * @param soFar the String so far, if any
         * @param arg the Object to format, or null
         * @return the formatted String
         * @throws ClassCastException thrown if this Stringifier could not format the provided Object
         *         because the provided Object was not of a type supported by this Stringifier
         */
        public String attemptFormat(
                String soFar,
                Object arg )
            throws
                ClassCastException
        {
            if( arg == null ) {
                return "null";
            } else if( arg instanceof PropertyValue ) {
                return format( soFar, (PropertyValue) arg );
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
    
    /**
     * StringStringifier for multiplicity values.
     */
    static class MyMultiplicityValueStringStringifier
            implements
                Stringifier<MultiplicityValue>
    {
        /**
         * Factory method.
         *
         * @return the created StringStringifier
         */
        public static MyMultiplicityValueStringStringifier create()
        {
            return new MyMultiplicityValueStringStringifier();
        }

        /**
         * No-op constructor. Use factory method.
         */
        protected MyMultiplicityValueStringStringifier()
        {
            // no op
        }

        /**
         * Format an Object using this Stringifier.
         *
         * @param soFar the String so far, if any
         * @param arg the Object to format, or null
         * @return the formatted String
         */
        public String format(
                String            soFar,
                MultiplicityValue arg )
        {
            StringBuilder ret = new StringBuilder();

            if( arg.getMinimum() == MultiplicityValue.N ) {
                ret.append( MultiplicityValue.N_SYMBOL );
            } else {
                ret.append( String.valueOf( arg.getMinimum() ));
            }
            ret.append( ".." );
            if( arg.getMaximum() == MultiplicityValue.N ) {
                ret.append( MultiplicityValue.N_SYMBOL );
            } else {
                ret.append( String.valueOf( arg.getMaximum() ));
            }
            return ret.toString();
        }

        /**
         * Format an Object using this Stringifier. This may be null.
         *
         * @param soFar the String so far, if any
         * @param arg the Object to format, or null
         * @return the formatted String
         * @throws ClassCastException thrown if this Stringifier could not format the provided Object
         *         because the provided Object was not of a type supported by this Stringifier
         */
        public String attemptFormat(
                String soFar,
                Object arg )
            throws
                ClassCastException
        {
            if( arg instanceof MultiplicityValue ) {
                return format( soFar, (MultiplicityValue) arg );
            } else {
                return (String) arg;
            }
        }

        /**
         * Parse out the Object in rawString that were inserted using this Stringifier.
         *
         * @param rawString the String to parse
         * @return the found Object
         * @throws StringifierParseException thrown if a parsing problem occurred
         */
        public MultiplicityValue unformat(
                String rawString )
            throws
                StringifierParseException
        {
            int dots = rawString.indexOf( ".." );
            if( dots < 0 ) {
                throw new StringifierParseException( this, rawString );
            }
            String first  = rawString.substring( 0, dots );
            String second = rawString.substring( dots+2 );
            
            if( first.length() == 0 || second.length() == 0 ) {
                throw new StringifierParseException( this, rawString );
            }

            int min;
            int max;
            
            if( MultiplicityValue.N_SYMBOL.equals( first )) {
                min = MultiplicityValue.N;
            } else {
                min = Integer.parseInt( first );
            }
            if( MultiplicityValue.N_SYMBOL.equals( second )) {
                max = MultiplicityValue.N;
            } else {
                max = Integer.parseInt( second );
            }
            
            MultiplicityValue ret = MultiplicityValue.create( min, max );
            return ret;
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
        public Iterator<StringifierParsingChoice<MultiplicityValue>> parsingChoiceIterator(
                final String  rawString,
                final int     startIndex,
                final int     endIndex,
                final int     max,
                final boolean matchAll )
        {
            try {
                MultiplicityValue found = unformat( rawString.substring( startIndex, endIndex ));

                StringifierValueParsingChoice<MultiplicityValue> choice = new StringifierValueParsingChoice<MultiplicityValue>(
                            startIndex,
                            endIndex,
                            found);

                OneElementIterator<StringifierParsingChoice<MultiplicityValue>> ret
                        = OneElementIterator.<StringifierParsingChoice<MultiplicityValue>>create( choice );
                
                return ret;
                
            } catch( StringifierParseException ex ) {
                return ZeroElementCursorIterator.create();
            }
        }
    }
}