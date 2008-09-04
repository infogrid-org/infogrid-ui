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

import org.infogrid.util.ArrayHelper;
import org.infogrid.util.StringHelper;

import java.util.ArrayList;
import java.util.Map;

/**
 * <p>A CompoundStringifier employing a syntax for a format string similar to <code>java.text.MessageFormat</code>.</p>
 * <p>An example best illustates the use of this class:</p>
 * <pre> HashMap<String,Stringifier<? extends Object>> map = new HashMap<String,Stringifier<? extends Object>>();
 * map.put( "int",    IntegerStringifier.create() );
 * map.put( "string", StringStringifier.create() );
 * MessageStringifier<Object> str = AnyMessageStringifier.create( "Abc {0,int} def{2,string}  ghi kl{0,int}mno {1,int}", map );
 *
 * String formatted = str.format( new Object[] { 0, 111, "222" } );</pre>
 * <p>Here, <code>formatted</code> will now be "Abc 0 def222  ghi kl0mno 111".</p>
 * <p>Continuing:</p>
 * <pre> Object [] extracted = str.unformat( formatted ).getArray();</pre>
 * <p>Now <code>extracted<code< will be <code>Object[] { Integer(0), Integer(111), "222" }</code>.</p>
 * <p>Continuing:</p>
 * <pre> Iterator<StringifierParsingChoice<ArrayFacade<Object>>> iter = str.parsingChoiceIterator( formatted, 0, formatted.length, formatted.length, false );
 * while( iter.hasNext() ) {
 *     StringifierParsingChoice<ArrayFacade<Object>> childChoice = iter.next();
 *     ArrayFacade<? extends Object> array = childChoice.unformat();
 * }</pre>
 * <p>Given that <code>false</code> was specified as the last parameter in the factory method, this iterator will return
 *    other matches of parsing the String (and potentially leaving characters at the end). If <code>true</code> had been
 *    specified, only one match would be returned (the same as the one found using <code>unformat</code> because this
 *    particular format String can only be parsed in one way without leaving out characters.</p>
 * 
 * @param <T> the type of the Objects to be stringified
 */
public abstract class MessageStringifier<T>
        extends
            CompoundStringifier<T>
{
    /**
     * Constructor, for subclasses only.
     *
     * @param formatString the formatString
     * @param childStringifiers the child Stringifiers, keyed by a String per {@link CompoundStringifier CompoundStringifier}.
     */
    protected MessageStringifier(
            String                               formatString,
            Map<String,Stringifier<? extends T>> childStringifiers )
    {
        theFormatString      = formatString;
        theChildStringifiers = childStringifiers;
    }

    /**
     * Determine the components of this CompoundStringifier.
     * 
     * @return the components of this CompoundStringifier
     * @throws CompoundStringifierCompileException thrown if the compilation failed.
     */
    protected CompoundStringifierComponent<T> [] compileIntoComponents()
        throws
            CompoundStringifierCompileException
    {
        ArrayList<CompoundStringifierComponent<T>> ret = new ArrayList<CompoundStringifierComponent<T>>();
        
        int           len           = theFormatString.length();
        int           bracketCount  = 0;
        StringBuffer  currentBuffer = new StringBuffer();
        
        for( int i=0 ; i<len ; ++i ) {
            char c = theFormatString.charAt( i );
            switch( c ) {
                case OPEN_BRACKET:
                    if( bracketCount == 0 ) {
                        if( currentBuffer.length() > 0 ) {
                            ret.add( new ConstantStringifierComponent<T>( currentBuffer.toString() ));
                            currentBuffer = new StringBuffer();
                        }
                    } else {
                        currentBuffer.append( c );
                    }
                    ++bracketCount;
                    break;
                    
                case CLOSE_BRACKET:
                    --bracketCount;
                    if( bracketCount == 0 ) {
                        ret.add( expressionToComponent( currentBuffer.toString() ));
                        currentBuffer = new StringBuffer();
                    } else if( bracketCount < 0 ) {
                        throw new CompoundStringifierCompileException.UnbalancedBrackets( this, theFormatString );
                    } else {
                        currentBuffer.append( c );
                    }
                    break;
                    
                default:
                    currentBuffer.append( c );
                    break;
            }
        }
        if( currentBuffer.length() > 0 ) {
            ret.add( new ConstantStringifierComponent<T>( currentBuffer.toString() ));
        }
        return copyIntoNewArrayWithoutWarning( ret );
    }

    /**
     * Factored out so we don't get compiler warnings.
     * 
     * @param values the values to copy into new array
     * @return the new array
     */
    @SuppressWarnings(value={"unchecked"})
    protected CompoundStringifierComponent<T> [] copyIntoNewArrayWithoutWarning(
            ArrayList<CompoundStringifierComponent<T>> values )
    {
        return (CompoundStringifierComponent<T> []) ArrayHelper.copyIntoNewArray( values, CompoundStringifierComponent.class );
    }

    /**
     * Parse an expression into a CompoundComponent.
     *
     * @param expression the expression
     * @return the created CompoundComponent
     * @throws CompoundStringifierCompileException thrown if the expression could not be compiled
     */
    protected CompoundStringifierComponent<T> expressionToComponent(
            String expression )
        throws
            CompoundStringifierCompileException
    {
        String [] subExpressions = expression.split( ",");
        if( subExpressions.length < 2 ) {
            throw new CompoundStringifierCompileException.IncompleteParameter( this, expression );
        }
        int    placeholderIndex = Integer.parseInt( subExpressions[0] );
        String childName        = subExpressions[1].trim();

        Stringifier<? extends T> child = theChildStringifiers.get( childName );
        if( child == null ) {
            throw new CompoundStringifierCompileException.SymbolicChildNameUndefined( this, expression, childName );
        }
        
        return new CompoundStringifierPlaceholder<T>( child, placeholderIndex );
    }

    /**
     * Convert to String form, for debugging.
     *
     * @return String representation
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
                new String[] {
                    "theFormatString",
                },
                new Object[] {
                    theFormatString,
                } );
    }

    /**
     * The format String.
     */
    protected String theFormatString;

    /**
     * Map between symbolic names in the format String, and actual Stringifiers.
     */
    protected Map<String,Stringifier<? extends T>> theChildStringifiers;

    /**
     * The characters in the formatString that indicate the opening of a placeholder.
     */
    public static final char OPEN_BRACKET = '{';

    /**
     * The characters in the formatString that indicate the closing of a placeholder.
     */
    public static final char CLOSE_BRACKET = '}';

    /**
     * The characters in the formatString that separate the components of a placeholder.
     */
    public static final String SEPARATOR = ",";
}
