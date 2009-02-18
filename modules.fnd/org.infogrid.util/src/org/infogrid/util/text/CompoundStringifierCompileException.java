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

package org.infogrid.util.text;

import org.infogrid.util.ResourceHelper;

/**
 * Thrown if a CompoundStringifier fails to parse the expression that
 * describes how the CompoundStringifier is composed from parts.
 * Concrete subtypes are defined as inner classes.
 */
public abstract class CompoundStringifierCompileException
        extends
            StringifierException
{
    /**
     * Constructor.
     *
     * @param source the Stringifier that raised this exception
     */
    protected CompoundStringifierCompileException(
            Stringifier source )
    {
        super( source );
    }
    
    /**
     * Use this ResourceHelper for all subclasses.
     *
     * @return the ResourceHelper to use
     */
    @Override
    protected ResourceHelper findResourceHelperForLocalizedMessage()
    {
        return ResourceHelper.getInstance( CompoundStringifierCompileException.class );
    }

    /**
     * All subclasses have a common ResourceHelper for all inner classes.
     *
     * @return the key
     */
    @Override
    protected String findStringRepresentationParameter()
    {
        return findParameterViaEnclosingClass( STRING_REPRESENTATION_KEY );
    }

    /**
     * All subclasses have a common ResourceHelper for all inner classes.
     *
     * @return the key
     */
    @Override
    protected String findStringRepresentationLinkStartParameter()
    {
        return findParameterViaEnclosingClass( STRING_REPRESENTATION_LINK_START_KEY );
    }

    /**
     * All subclasses have a common ResourceHelper for all inner classes.
     *
     * @return the key
     */
    @Override
    protected String findStringRepresentationLinkEndParameter()
    {
        return findParameterViaEnclosingClass( STRING_REPRESENTATION_LINK_END_KEY );
    }

    /**
     * Indicates that brackets were unbalanced in the expression.
     */
    public static class UnbalancedBrackets
            extends
                CompoundStringifierCompileException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param source the Stringifier that raised this exception
         * @param formatString the format String that contains the unbalanced brackets
         */
        public UnbalancedBrackets(
                Stringifier source,
                String      formatString )
        {
            super( source );
            
            theFormatString = formatString;
        }
        
        /**
         * Obtain resource parameters for the internationalization.
         *
         * @return the resource parameters
         */    
        public Object [] getLocalizationParameters()
        {
            return new Object[] { theSource, theFormatString };
        }
        
        /**
         * Obtain as String representation.
         *
         * @return String representation
         */
        @Override
        public String toString()
        {
            return getClass().getName()
                    + ": unbalanced brackets in format String '"
                    + theFormatString
                    + "'.";
        }

        /**
         * The format String that had the problem.
         */
        protected String theFormatString;
    }

    /**
     * Indicates that a parameter definition in an expression was incomplete.
     */
    public static class IncompleteParameter
            extends
                CompoundStringifierCompileException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param source the Stringifier that raised the Exception
         * @param parameterExpression the expression that was incomplete
         */
        public IncompleteParameter(
                Stringifier source,
                String      parameterExpression )
        {
            super( source );
            
            theParameterExpression = parameterExpression;
        }
        
        /**
         * Obtain the expression that was incomplete.
         *
         * @return the expression that was incomplete
         */
        public String getParameterExpression()
        {
            return theParameterExpression;
        }

        /**
         * Obtain resource parameters for the internationalization.
         *
         * @return the resource parameters
         */    
        public Object [] getLocalizationParameters()
        {
            return new Object[] { theSource, theParameterExpression };
        }
        
        /**
         * Obtain as String representation.
         *
         * @return String representation
         */
        @Override
        public String toString()
        {
            return getClass().getName()
                    + ": incomplete parameter use in expression '"
                    + theParameterExpression
                    + "'.";
        }

        /**
         * The expression that was incomplete.
         */
        protected String theParameterExpression;
    }

    /**
     * Indicates that a symbolic child name, in an expression, was undefined.
     */
    public static class SymbolicChildNameUndefined
            extends
                CompoundStringifierCompileException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param source the Stringifier that raised the Exception
         * @param parameterExpression the expression that contained the undefined symbolic child
         * @param childName the undefined symbolic child name
         */
        public SymbolicChildNameUndefined(
                Stringifier source,
                String      parameterExpression,
                String      childName )
        {
            super( source );
            
            theParameterExpression = parameterExpression;
            theChildName           = childName;
        }
        
        /**
         * Obtain the expression that contained the undefined child.
         *
         * @return the expression that contained the undefined child
         */
        public String getParameterExpression()
        {
            return theParameterExpression;
        }

        /**
         * Obtain the symbolic child name name that was undefined.
         *
         * @return the symbolic child name that was undefined
         */
        public String getUndefinedSymbolicChildName()
        {
            return theChildName;
        }

        /**
         * Obtain resource parameters for the internationalization.
         *
         * @return the resource parameters
         */    
        public Object [] getLocalizationParameters()
        {
            return new Object[] { theSource, theParameterExpression, theChildName };
        }

        /**
         * Obtain as String representation.
         *
         * @return String representation
         */
        @Override
        public String toString()
        {
            return getClass().getName()
                    + ": child '"
                    + theChildName
                    + "' undefined in expression '"
                    + theParameterExpression
                    + "'.";
        }

        /**
         * The expression that contained the undefined child.
         */
        protected String theParameterExpression;

        /**
         * The symbolic child name that was undefined.
         */
        protected String theChildName;
    }
}
