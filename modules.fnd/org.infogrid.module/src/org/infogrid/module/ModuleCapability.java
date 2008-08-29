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

package org.infogrid.module;

import java.io.*;

/**
 * One capability of a Module. A ModuleCapability is a pair of a set of
 * interface names and exactly one implementation name. An interface name must be a valid
 * name for a Java interface or class, and an implementation name must be a valid name for
 * a Java class that is a subclass of (or supports the interface of) all of the
 * interface names.
 */
public class ModuleCapability
        implements
            Serializable
{
    private static final long serialVersionUID = 1L; // helps with serialization

    /**
     * Factory method.
     *
     * @param interfaceNames the names of the Java interfaces provided by this ModuleCapability
     * @param implementationName the name of the Java implementation class supporting the interfaces
     * @param argumentCombinations the combinations of arguments supported by the implementation class
     * @return the created ModuleCapability
     */
    public static ModuleCapability create1(
            String []              interfaceNames,
            String                 implementationName,
            ArgumentCombination [] argumentCombinations )
    {
        return new ModuleCapability( interfaceNames, implementationName, argumentCombinations );
    }

    /**
     * Factory method for an ArgumentCombination. This is here rather than in the inner class
     * because the difficulty of creating static methods in inner classes.
     *
     * @param arguments the arguments that are allowed in this ArgumentCombination
     * @return the created ArgumentCombination
     */
    public static ArgumentCombination createArgumentCombination1(
            String [] arguments )
    {
        return new ArgumentCombination( arguments );
    }

    /**
     * Private constructor, use factory method.
     *
     * @param interfaceNames the names of the Java interfaces provided by this ModuleCapability
     * @param implementationName the name of the Java implementation class supporting the interfaces
     * @param argumentCombinations the combinations of arguments supported by the implementation class
     */
    protected ModuleCapability(
            String []              interfaceNames,
            String                 implementationName,
            ArgumentCombination [] argumentCombinations )
    {
        theInterfaceNames       = interfaceNames;
        theImplementationName   = implementationName;
        theArgumentCombinations = argumentCombinations;
    }

    /**
      * Obtain the names of the interfaces provided by this ModuleCapability.
      *
      * @return the names of the interfaces provided by this ModuleCapability.
      */
    public final String [] getInterfaceNames()
    {
        return theInterfaceNames;
    }

    /**
     * Obtain the name of the implementation provided by this ModuleCapability.
     *
     * @return the name of the implementation provided by this ModuleCapability.
     */
    public final String getImplementationName()
    {
        return theImplementationName;
    }

    /**
     * Obtain the allowed argument combinations for this ModuleCapability.
     * The specific semantics of Module arguments are defined by specific Modules,
     * there is no global set of argument semantics at this time.
     *
     * @return the set of allowed ArgumentCombinations, if any have been defined
     */
    public final ArgumentCombination [] getSupportedArgumentCombinations()
    {
        return theArgumentCombinations;
    }

    /**
     * Return this object in string format, for debugging purposes.
     *
     * @return this object in string format
     */
    @Override
    public String toString()
    {
        StringBuffer ret = new StringBuffer( 100 );
        ret.append( "<" );
        ret.append( super.toString() );
        ret.append( "{ intfcs: { "); // do not use ArrayHelper, not in the bootloader
        for( int i=0 ; i<theInterfaceNames.length ; ++i ) {
            if (i > 0) {
                ret.append(", ");
            }
            ret.append( theInterfaceNames[i] );
        }
        ret.append( "} , impl: " );
        ret.append( theImplementationName );
        ret.append( ", combos: { " ); // do not use ArrayHelper, not in the bootloader
        for( int i=0 ; i<theArgumentCombinations.length ; ++i ) {
            if( i>0 ) {
                ret.append( ", " );
            }
            ret.append( "{ " );
            for( int j=0 ; j<theArgumentCombinations[i].theArguments.length ; ++j ) {
                if( j>0 ) {
                    ret.append( ", " );
                }
                ret.append( theArgumentCombinations[i].theArguments[j] );
            }

            ret.append( " } " );
        }
        ret.append( " } }>" );
        return ret.toString();
    }

    /**
     * Create a String expression in the Java language that, when executed, creates
     * an instance of ModuleCapability that is equal to this ModuleCapability.
     *
     * @param tabs the number of tabs to insert
     * @return the String expression
     */
    public String getJavaConstructorString(
            int tabs )
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "ModuleCapability.create1(\n" );
        buf.append( ModuleAdvertisement.tabs( tabs+1 ));
        if( theInterfaceNames == null ) {
            buf.append( "null" );
        } else {
            buf.append( "new String[] {\n" );
            for( int i=0 ; i<theInterfaceNames.length ; ++i ) {
                if( i>0 ) {
                    buf.append( ",\n" );
                }

                buf.append( ModuleAdvertisement.getJavaConstructorStringString( tabs+2, theInterfaceNames[i] ));
            }
            buf.append( " }" );
        }
        buf.append( ",\n");
        buf.append( ModuleAdvertisement.getJavaConstructorStringString( tabs+1, theImplementationName ));
        buf.append( ",\n");
        buf.append( ModuleAdvertisement.tabs( tabs+1 ));
        if( theArgumentCombinations == null ) {
            buf.append( "null" );
        } else {
            buf.append( "new ModuleCapability.ArgumentCombination[] {\n" );
            for( int i=0 ; i<theArgumentCombinations.length ; ++i ) {
                if( i>0 ) {
                    buf.append( ",\n" );
                }
                buf.append( ModuleAdvertisement.tabs( tabs+2 ));
                buf.append( "ModuleCapability.createArgumentCombination1( new String[] { " );
                for( int j=0 ; j<theArgumentCombinations[i].theArguments.length ; ++j ) {
                    if( j>0 ) {
                        buf.append( ",\n" );
                    }

                    buf.append( ModuleAdvertisement.getJavaConstructorStringString( tabs+2, theArgumentCombinations[i].theArguments[j] ));
                }
                buf.append( " } ) " );
            }
            buf.append( " }" );
        }
        buf.append( " )");
        return buf.toString();
    }

    /**
     * The interface names.
     */
    protected String [] theInterfaceNames;

    /**
     * The implementation name.
     */
     protected String theImplementationName;

    /**
     * The allowed set of argument combinations.
     */
     protected ArgumentCombination [] theArgumentCombinations;

    /**
     * This is one possible argument combination to this ModuleCapability.
     */
    public static class ArgumentCombination
        implements
            Serializable
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Private constructor, use factory method in outer class.
         *
         * @param arguments the arguments that are allowed in this ArgumentCombination
         */
        protected ArgumentCombination(
                String [] arguments )
        {
            theArguments = arguments;
        }

        /**
         * Obtain the arguments that are allowed in this ArgumentCombination.
         *
         * @return the arguments that are allowed in this ArgumentCombination
         */
        public String [] getArguments()
        {
            return theArguments;
        }

        /**
         * The allowed arguments.
         */
        protected String [] theArguments;
    }
}
