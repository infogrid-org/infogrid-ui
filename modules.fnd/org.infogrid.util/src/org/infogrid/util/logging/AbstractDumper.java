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
// Copyright 1998-2009 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.util.logging;

import java.lang.ref.Reference;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.ResourceHelper;

/**
 * Factors out common functionality of Dumper implementations.
 *
 * It's important that all methods invoked directly dump at least themselves, and check
 * about their children before invoking them to be dumped.
 */
public abstract class AbstractDumper
        implements
            Dumper
{
    private static Log log; // log initialized as late as possible

    /**
     * Constructor, for subclasses only.
     *
     * @param maxLevel the number of object levels to dump
     */
    protected AbstractDumper(
            int maxLevel )
    {
        theLevel    = 0;
        theMaxLevel = maxLevel;
    }

    /**
     * Obtain the maximum number of object levels to dump.
     *
     * @return the maximum number
     */
    public int getMaxLevel()
    {
        return theMaxLevel;
    }

    /**
     * Obtain the current object level.
     * 
     * @return the current object level
     */
    public int getLevel()
    {
        return theLevel;
    }

    /**
     * Dump an object. This method is just the dispatcher and does not do any dumping itself.
     *
     * @param obj the object to dump
     */
    public void dump(
            Object obj )
    {
        if( obj == null )  {
            dumpNull();

        } else if( obj instanceof Object[] ) {
            dumpArray( (Object []) obj );

        } else if( obj instanceof Collection ) {
            dumpCollection( (Collection) obj );

        } else if( obj instanceof Map ) {
            dumpMap( (Map) obj );

        } else if( obj instanceof boolean[] ) {
            dumpBooleanArray( (boolean []) obj );

        } else if( obj instanceof byte[] ) {
            dumpByteArray( (byte []) obj );

        } else if( obj instanceof short[] ) {
            dumpShortArray( (short []) obj );

        } else if( obj instanceof int[] ) {
            dumpIntArray( (int []) obj );

        } else if( obj instanceof long[] ) {
            dumpLongArray( (long []) obj );

        } else if( obj instanceof float[] ) {
            dumpFloatArray( (float []) obj );

        } else if( obj instanceof double[] ) {
            dumpDoubleArray( (double []) obj );

        } else if( obj instanceof char[] ) {
            dumpCharArray( (char []) obj );

        } else if( obj instanceof Boolean ) {
            dumpBoolean( (Boolean) obj );

        } else if( obj instanceof Character ) {
            dumpCharacter( (Character) obj );

        } else if( obj instanceof Date ) {
            dumpDate( (Date) obj );

        } else if( obj instanceof Number ) {
            dumpNumber( (Number) obj );

        } else if( obj instanceof CharSequence ) {
            dumpCharSequence( (CharSequence) obj );

        } else if( obj instanceof Reference ) {
            dumpReference( (Reference) obj );

        } else {
            dumpObject( obj );
        }
    }

    /**
     * Dump a regular object.
     *
     * @param obj the object to dump
     */
    public void dumpObject(
            Object obj )
    {
        if( obj == null ) {
            dumpNull();

        } else if( obj instanceof CanBeDumped ) {
            registerAsDumped( obj );

            CanBeDumped realObj = (CanBeDumped) obj;
            realObj.dump( this );

        } else {
            // let's try what we can
            Field [] fields = obj.getClass().getFields();
            ArrayList<String> names  = new ArrayList<String>();
            ArrayList<Object> values = new ArrayList<Object>();

            for( int i=0 ; i<fields.length ; ++i ) {
                if( Modifier.isStatic( fields[i].getModifiers())) {
                    continue; // we don't want static
                }
                try {
                    Object value = fields[i].get( obj );

                    names.add( fields[i].getName() );
                    values.add( value );

                } catch( Throwable t ) {
                    if( getLog().isDebugEnabled() ) {
                        getLog().debug( t );
                    }
                }
            }

            if( !names.isEmpty() ) {
                String [] names2  = ArrayHelper.copyIntoNewArray( names, String.class );
                Object [] values2 = ArrayHelper.copyIntoNewArray( values, Object.class );

                dump( obj, names2, values2 );
            } else {
                dumpWithToString( obj );
            }
        }
    }

    /**
     * Dump an object by dumping its named fields.
     *
     * @param obj the object to dump
     * @param fieldNames the fields of the object to dump
     * @param fieldValues the values of the fields of the object to dump
     */
    public void dump(
            Object    obj,
            String [] fieldNames,
            Object [] fieldValues )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            registerAsDumped( obj );
            dumpObjectId( obj );

            emit( "{" );
            int min = Math.min(
                    fieldNames  != null ? fieldNames.length  : 0,
                    fieldValues != null ? fieldValues.length : 0 );

            try {
                ++theLevel;

                for( int i=0 ; i<min ; ++i ) {
                    Object value = fieldValues[i];

                    if( shouldBeDumpedFull( value )) {
                        emit( '\n' );
                        emit( fieldNames[i] );
                        emit( ": ");
                        dump( value );

                        if( i<min-1 ) {
                            emit( ',' );
                        }

                    } else if( shouldBeDumpedShort( value )) {
                        emit( '\n' );
                        emit( fieldNames[i] );
                        emit( ": ");
                        dump( objectId( value ));

                        if( i<min-1 ) {
                            emit( ',' );
                        }
                    }
                }
            } finally {
                --theLevel;
            }
            if(    ( fieldNames  != null && fieldNames.length  != min )
                || ( fieldValues != null && fieldValues.length != min ))
            {
                getLog().error( "non-matching field names and values in toString method" );
            }
            emit( "\n}" );
        }
    }

    /**
     * Dump a null value.
     */
    public void dumpNull()
    {
        emit( "null" );
    }

    /**
     * Dump an array of objects.
     *
     * @param obj the object to dump
     */
    public void dumpArray(
            Object [] obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            registerAsDumped( obj );
            if( obj.length == 0 ) {
                emit( "[0] = {}" );
            } else {
                emit( '[' );
                emit( String.valueOf( obj.length ));
                emit( "] = {" );

                try {
                    ++theLevel;

                    int max = Math.min( 8, obj.length );

                    for( int j=0 ; j<max ; ++j ) {
                        emit( '\n' );
                        dump( obj[j] );

                        if( j < max-1 ) {
                            emit( "," );
                        }
                    }
                    if( max != obj.length ) {
                        emit( "\n..." );
                    }
                } finally {
                    --theLevel;
                }
                emit( "\n}" );
            }
        }
    }

    /**
     * Dump a Collection.
     *
     * @param obj the object to dump
     */
    public void dumpCollection(
            Collection<?> obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            registerAsDumped( obj );
            if( obj.size() == 0 ) {
                emit( "[0] = {}" );
            } else {
                emit( '[' );
                emit( String.valueOf( obj.size() ));
                emit( "] = {" );

                try {
                    ++theLevel;

                    int max = Math.min( 8, obj.size() );
                    int j   = 0;

                    for( Object current : obj ) {
                        emit( '\n' );
                        dump( current );

                        if( ++j >= max ) {
                            break;
                        }
                    }
                    if( max != obj.size() ) {
                        emit( "\n..." );
                    }
                } finally {
                    --theLevel;
                }
                emit( "\n}" );
            }
        }
    }

    /**
     * Dump a Map.
     *
     * @param obj the object to dump
     */
    public void dumpMap(
            Map<?,?> obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            registerAsDumped( obj );
            emit( obj.getClass().getName() );

            if( obj.size() == 0 ) {
                emit( "[0] = {}" );
            } else {
                emit( '[' );
                emit( String.valueOf( obj.size() ));
                emit( "] = {" );

                try {
                    ++theLevel;

                    int max = Math.min( 8, obj.size() );
                    int j   = 0;
                    for( Object from : obj.keySet() ) {
                        emit( '\n' );

                        Object to = obj.get( from );
                        dump( from );
                        emit( " : " );
                        dump( to );

                        if( ++j >= max ) {
                            break;
                        }
                    }
                    if( max != obj.size() ) {
                        emit( "\n..." );
                    }
                } finally {
                    --theLevel;
                }
                emit( "\n}" );
            }
        }
    }

    /**
     * Dump a boolean array.
     *
     * @param obj the object to dump
     */
    public void dumpBooleanArray(
            boolean [] obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            registerAsDumped( obj );
            emit( "boolean[" );
            emit( String.valueOf( obj.length ));
            emit( "] = { " );

            int max = Math.min( 8, obj.length );
            for( int j=0 ; j<max ; ++j ) {
                emit( obj[j] ? "true" : "false" );
                if( j < max-1 ) {
                    emit( ", " );
                }
            }
            if( max < obj.length ) {
                emit( "..." );
            }
            emit( " }" );
        }
    }

    /**
     * Dump a byte array.
     *
     * @param obj the object to dump
     */
    public void dumpByteArray(
            byte [] obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            registerAsDumped( obj );
            emit( "byte[" );
            emit( String.valueOf( obj.length ));
            emit( "] = " );

            int max = Math.min( 8, obj.length );
            for( int j=0 ; j<max ; ++j ) {
                emit( Character.forDigit( ( obj[j] >> 4 ) & 0xf, 16 ));
                emit( Character.forDigit( obj[j] & 0xf, 16 ));
            }
            if( max < obj.length ) {
                emit( "..." );
            }
        }
    }

    /**
     * Dump a short array.
     *
     * @param obj the object to dump
     */
    public void dumpShortArray(
            short [] obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            registerAsDumped( obj );
            emit( "short[" );
            emit( String.valueOf( obj.length ));
            emit( "] = { " );

            int max = Math.min( 8, obj.length );
            for( int j=0 ; j<max ; ++j ) {
                emit( String.valueOf( obj[j] ));
                if( j < max-1 ) {
                    emit( ", " );
                }
            }
            if( max < obj.length ) {
                emit( "..." );
            }
            emit( " }" );
        }
    }


    /**
     * Dump an integer array.
     *
     * @param obj the object to dump
     */
    public void dumpIntArray(
            int [] obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            registerAsDumped( obj );
            emit( "int[" );
            emit( String.valueOf( obj.length ));
            emit( "] = { " );

            int max = Math.min( 8, obj.length );
            for( int j=0 ; j<max ; ++j ) {
                emit( String.valueOf( obj[j] ));
                if( j < max-1 ) {
                    emit( ", " );
                }
            }
            if( max < obj.length ) {
                emit( "..." );
            }
            emit( " }" );
        }
    }

    /**
     * Dump a long array.
     *
     * @param obj the object to dump
     */
    public void dumpLongArray(
            long [] obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            registerAsDumped( obj );
            emit( "long[" );
            emit( String.valueOf( obj.length ));
            emit( "] = { " );

            int max = Math.min( 8, obj.length );
            for( int j=0 ; j<max ; ++j ) {
                emit( String.valueOf( obj[j] ));
                if( j < max-1 ) {
                    emit( ", " );
                }
            }
            if( max < obj.length ) {
                emit( "..." );
            }
            emit( " }" );
        }
    }


    /**
     * Dump a float array.
     *
     * @param obj the object to dump
     */
    public void dumpFloatArray(
            float [] obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            registerAsDumped( obj );
            emit( "float[" );
            emit( String.valueOf( obj.length ));
            emit( "] = { " );

            int max = Math.min( 8, obj.length );
            for( int j=0 ; j<max ; ++j ) {
                emit( String.valueOf( obj[j] ));
                if( j < max-1 ) {
                    emit( ", " );
                }
            }
            if( max < obj.length ) {
                emit( "..." );
            }
            emit( " }" );
        }
    }

    /**
     * Dump a double array.
     *
     * @param obj the object to dump
     */
    public void dumpDoubleArray(
            double [] obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            registerAsDumped( obj );
            emit( "double[");
            emit( String.valueOf( obj.length ));
            emit( "] = { " );

            int max = Math.min( 8, obj.length );
            for( int j=0 ; j<max ; ++j ) {
                emit( String.valueOf( obj[j] ));
                if( j < max-1 ) {
                    emit( ", " );
                }
            }
            if( max < obj.length ) {
                emit( "..." );
            }
            emit( " }" );
        }
    }

    /**
     * Dump a character array.
     *
     * @param obj the object to dump
     */
    public void dumpCharArray(
            char [] obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            registerAsDumped( obj );
            emit( "char[" );
            emit( String.valueOf( obj.length ));
            emit( "] = { " );

            int max = Math.min( 8, obj.length );
            for( int j=0 ; j<max ; ++j ) {
                emit( '\'' );
                emit( obj[j] );
                emit( '\'' );
                if( j < max-1 ) {
                    emit( ", " );
                }
            }
            if( max < obj.length ) {
                emit( "..." );
            }
            emit( " }" );
        }
    }

    /**
     * Dump a boolean.
     *
     * @param obj the object to dump
     */
    public void dumpBoolean(
            Boolean obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            // don't register as dumped -- it's short enough to do it again
            if( obj.booleanValue() ) {
                emit( "true" );
            } else {
                emit( "false" );
            }
        }
    }

    /**
     * Dump a character.
     *
     * @param obj the object to dump
     */
    public void dumpCharacter(
            Character obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            // don't register as dumped -- it's short enough to do it again
            emit( '\'' );
            emit( obj.charValue() );
            emit( '\'' );
        }
    }

    /**
     * Dump a Date object.
     *
     * @param obj the object to dump
     */
    public void dumpDate(
            Date obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            // don't register as dumped -- it's short enough to do it again
            String ret = theDateFormat.format( obj );
            emit( ret );
        }
    }

    /**
     * Dump a number, e.g. a short, int or long.
     *
     * @param obj the object to dump
     */
    public void dumpNumber(
            Number obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            // don't register as dumped -- it's short enough to do it again
            emit( obj.getClass().getName() );
            emit( ": " );
            emit( obj.toString() );
        }
    }

    /**
     * Dump a character sequence, e.g. String, StringBuffer, StringBuilder.
     *
     * @param obj the object to dump
     */
    public void dumpCharSequence(
            CharSequence obj )
    {
        if( obj == null ) {
            dumpNull();

        } else {
            // don't register as dumped -- it's short enough to do it again
            emit( obj.getClass().getName() );
            emit( ": \"" );
            emit( obj );
            emit( "\"" );
        }
    }

    /**
     * Dump a reference, e.g. a SoftReference, WeakReference, PhantomReference.
     *
     * @param obj the object to dump
     */
    public void dumpReference(
            Reference<?> obj )
    {
        Object found = obj.get();

        dump( obj, new String[] { "ref" }, new Object[] { found } );
    }

    /**
     * Dump an object by using its toString() method.
     *
     * @param obj the object to dump
     */
    public void dumpWithToString(
            Object obj )
    {
        if( obj == null ) {
            dumpNull();
        } else {
            // don't register as dumped -- it's short enough to do it again
            String to = obj.toString();
            emit( to );
        }
    }

    /**
     * Dump an object identifier.
     *
     * @param obj the Object
     */
    public void dumpObjectId(
            Object obj )
    {
        emit( obj.getClass().getName() );
        emit( '@' );
        emit( Integer.toHexString( obj.hashCode() ));
    }

    /**
     * Create an object's object id.
     *
     * @param obj the Object
     * @return the object id
     */
    public String objectId(
            Object obj )
    {
        StringBuilder buf = new StringBuilder();
        buf.append( obj.getClass().getName() );
        buf.append( '@' );
        buf.append( Integer.toHexString( obj.hashCode() ));

        return buf.toString();
    }

    /**
     * Emit a CharSequence that was put together as part of dumping.
     *
     * @param toEmit the String to emit
     */
    protected abstract void emit(
            CharSequence toEmit );

    /**
     * Emit a single character that was put together as part of dumping.
     *
     * @param toEmit the character to emit
     */
    protected abstract void emit(
            char toEmit );

    /**
     * Determine whether an object should be dumped with all details.
     *
     * @param obj the Object to be tested
     * @return true if the Object should be dumped with all details
     */
    protected abstract boolean shouldBeDumpedFull(
            Object obj );

    /**
     * Determine whether an object should be dumped with minimal details.
     *
     * @param obj the Object to be tested
     * @return true if the Object should be dumped with minimum details
     */
    protected abstract boolean shouldBeDumpedShort(
            Object obj );

    /**
     * Register an object as having been dumped already.
     *
     * @param obj the Object to be registered
     */
    protected abstract void registerAsDumped(
            Object obj );

    /**
     * Obtain the logger to use.
     *
     * @return the Log
     */
    protected Log getLog()
    {
         return Log.getLogInstance( getClass() ); // our own, private logger
    }

    /**
     * The maximum level of dumping.
     */
    protected int theMaxLevel;

    /**
     * The level of dumping at which the Dumper is currently operating.
     */
    protected int theLevel = 0;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( AbstractDumper.class );

    /**
     * The date format to use for dumping Dates.
     */
    public static final DateFormat theDateFormat = new SimpleDateFormat(
            theResourceHelper.getResourceStringOrDefault(
                    "DateFormat",
                    "yyyy-MM-dd HH:mm:ss.S" ));
}
