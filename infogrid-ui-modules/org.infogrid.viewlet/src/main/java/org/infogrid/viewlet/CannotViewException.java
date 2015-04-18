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
// Copyright 1998-2013 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.viewlet;

import org.infogrid.mesh.MeshObjectIdentifier;
import org.infogrid.util.AbstractLocalizedException;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationParameters;
import org.infogrid.util.text.StringifierException;

/**
 * Thrown when a Viewlet cannot view the MeshObjectsToView that have been
 * given to it. Use the inner classes to be specific about what is going on.
 */
public abstract class CannotViewException
        extends
            AbstractLocalizedException
        implements
            CanBeDumped
{
    /**
     * Constructor.
     *
     * @param v which Viewlet could not view
     * @param o which MeshObjectsToView it could not view
     * @param msg a message describing the Exception
     * @param cause underlying Exception, if any
     */
    protected CannotViewException(
            Viewlet           v,
            MeshObjectsToView o,
            String            msg,
            Throwable         cause )
    {
        super( msg, cause );

        theViewlet       = v;
        theObjectsToView = o;
    }

    /**
     * Make compiler happy.
     *
     * @return nothing
     */
    public Object [] getLocalizationParameters()
    {
        return null;
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
                new String[] {
                    "viewlet",
                    "objectsToView"
                },
                new Object[] {
                    theViewlet,
                    theObjectsToView
        } );
    }

    /**
     * The Viewlet that could not view.
     */
    protected Viewlet theViewlet;

    /**
     * The MeshObjectsToView that the Viewlet could not view.
     */
    protected MeshObjectsToView theObjectsToView;
    
    /**
     * The required Viewlet type and the given Viewlet were not compatible.
     */
    public static class ViewletClassNotCompatible
            extends
                CannotViewException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param v which Viewlet could not view
         * @param o which MeshObjectsToView it could not view
         */
        public ViewletClassNotCompatible(
                Viewlet           v,
                MeshObjectsToView o )
        {
            super( v, o, v.getClass().getName() + " (actual) vs. " + o.getViewletTypeName() + " (required)", null );
        }

        /**
         * Obtain a String representation of this instance that can be shown to the user.
         *
         * @param rep the StringRepresentation
         * @param pars collects parameters that may influence the String representation. Always provided.
         * @return String representation
         * @throws StringifierException thrown if there was a problem when attempting to stringify
         */
        @Override
        public String toStringRepresentation(
                StringRepresentation           rep,
                StringRepresentationParameters pars )
            throws
                StringifierException
        {
            if( theObjectsToView.getViewletTypeName() == null ) {
                return rep.formatEntry(
                        getClass(),
                        "ViewletClassNotCompatibleWithSubjectString",
                        pars,
                        theViewlet.getName(),
                        theViewlet.getUserVisibleName(),
                        theObjectsToView.getSubject());

            } else {
                return rep.formatEntry(
                        getClass(),
                        "ViewletClassNotCompatibleWithTypeString",
                        pars,
                        theViewlet.getName(),
                        theViewlet.getUserVisibleName(),
                        theObjectsToView.getSubject(),
                        theObjectsToView.getViewletTypeName());
            }
        }
    }

    /**
     * The Viewlet could not handle the type of MeshObject given as subject in the MeshObjectsToView.
     */
    public static class ObjectTypeNotAllowed
            extends
                CannotViewException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param v which Viewlet could not view
         * @param o which MeshObjectsToView it could not view
         */
        public ObjectTypeNotAllowed(
                Viewlet           v,
                MeshObjectsToView o )
        {
            super( v, o, "Viewlet: " + v.getClass().getName(), null );
        }

        /**
         * Obtain a String representation of this instance that can be shown to the user.
         *
         * @param rep the StringRepresentation
         * @param pars collects parameters that may influence the String representation. Always provided.
         * @return String representation
         * @throws StringifierException thrown if there was a problem when attempting to stringify
         */
        @Override
        public String toStringRepresentation(
                StringRepresentation           rep,
                StringRepresentationParameters pars )
            throws
                StringifierException
        {
            return rep.formatEntry(
                    getClass(),
                    "ObjectTypeNotAllowedString",
                    pars,
                    theViewlet.getName(),
                    theViewlet.getUserVisibleName(),
                    theObjectsToView.getSubject());
        }
    }

    /**
     * The Viewlet was invalid, for example because it could only be loaded partially.
     */
    public static class InvalidViewlet
            extends
                CannotViewException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param v which Viewlet was invalid
         * @param o which MeshObjectsToView were used
         */
        public InvalidViewlet(
                Viewlet           v,
                MeshObjectsToView o )
        {
            super( v, o, "Viewlet invalid: " + v.getClass().getName(), null );
        }

        /**
         * Obtain a String representation of this instance that can be shown to the user.
         *
         * @param rep the StringRepresentation
         * @param pars collects parameters that may influence the String representation. Always provided.
         * @return String representation
         * @throws StringifierException thrown if there was a problem when attempting to stringify
         */
        @Override
        public String toStringRepresentation(
                StringRepresentation           rep,
                StringRepresentationParameters pars )
            throws
                StringifierException
        {
            return rep.formatEntry(
                    getClass(),
                    "InvalidViewletString",
                    pars,
                    theViewlet.getName(),
                    theViewlet.getUserVisibleName(),
                    theObjectsToView.getSubject());
        }
    }
    
    /**
     * The Viewlet needs a parameter that was not given in the MeshObjectsToView.
     */
    public static class ParameterMissing
            extends
                CannotViewException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param v which Viewlet could not view
         * @param name the name of the parameter that was missing
         * @param o which MeshObjectsToView it could not view
         */
        public ParameterMissing(
                Viewlet           v,
                String            name,
                MeshObjectsToView o )
        {
            super( v, o, "Missing parameter: " + name, null );
            
            theName = name;
        }

        /**
         * Obtain a String representation of this instance that can be shown to the user.
         *
         * @param rep the StringRepresentation
         * @param pars collects parameters that may influence the String representation. Always provided.
         * @return String representation
         * @throws StringifierException thrown if there was a problem when attempting to stringify
         */
        @Override
        public String toStringRepresentation(
                StringRepresentation           rep,
                StringRepresentationParameters pars )
            throws
                StringifierException
        {
            return rep.formatEntry(
                    getClass(),
                    "ParameterMissingString",
                    pars,
                    theViewlet.getName(),
                    theViewlet.getUserVisibleName(),
                    theObjectsToView.getSubject(),
                    theName );
        }
        
        /**
         * Name of the missing parameter.
         */
        protected String theName;
    }

    /**
     * An invalid parameter has been specified in the invocation of this Viewlet.
     */
    public static class InvalidParameter
            extends
                CannotViewException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param v which Viewlet could not view
         * @param name the name of the parameter that was invalid
         * @param value the value of the parameter that was invalid
         * @param o which MeshObjectsToView it could not view
         */
        public InvalidParameter(
                Viewlet           v,
                String            name,
                Object            value,
                MeshObjectsToView o )
        {
            super( v, o, "Invalid parameter: " + name, null );

            theName  = name;
            theValue = value;
        }

        /**
         * Obtain a String representation of this instance that can be shown to the user.
         *
         * @param rep the StringRepresentation
         * @param pars collects parameters that may influence the String representation. Always provided.
         * @return String representation
         * @throws StringifierException thrown if there was a problem when attempting to stringify
         */
        @Override
        public String toStringRepresentation(
                StringRepresentation           rep,
                StringRepresentationParameters pars )
            throws
                StringifierException
        {
            return rep.formatEntry(
                    getClass(),
                    "InvalidParameterString",
                    pars,
                    theViewlet.getName(),
                    theViewlet.getUserVisibleName(),
                    theObjectsToView.getSubject(),
                    theName,
                    theValue );
        }

        /**
         * Name of the invalid parameter.
         */
        protected String theName;

        /*
         * Value of the invalid parameter.
         */
        protected Object theValue;
    }

    /**
     * No subject was provided.
     */
    public static class NoSubject
            extends
                CannotViewException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         * 
         * @param identifier the Identifier of the non-existing Subject.
         */
        public NoSubject(
                MeshObjectIdentifier identifier )
        {
            super( null, null, null, null );
            
            theIdentifier = identifier;
        }

        /**
         * Obtain a String representation of this instance that can be shown to the user.
         *
         * @param rep the StringRepresentation
         * @param pars collects parameters that may influence the String representation. Always provided.
         * @return String representation
         * @throws StringifierException thrown if there was a problem when attempting to stringify
         */
        @Override
        public String toStringRepresentation(
                StringRepresentation           rep,
                StringRepresentationParameters pars )
            throws
                StringifierException
        {
            return rep.formatEntry(
                    getClass(),
                    "NoSubjectString",
                    pars,
                    theViewlet != null ? theViewlet.getName() : null,
                    theViewlet != null ? theViewlet.getUserVisibleName() : null,
                    theIdentifier );
        }
        
        /**
         * Identifier of the non-existing Subject.
         */
        protected MeshObjectIdentifier theIdentifier;
    }

    /**
     * Something unspecified, but bad, has happened.
     */
    public static class InternalError
            extends
                CannotViewException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param v which Viewlet could not view
         * @param o which MeshObjectsToView it could not view
         * @param cause the underlying internal Exception, if any
         */
        public InternalError(
                Viewlet           v,
                MeshObjectsToView o,
                Throwable         cause )
        {
            super( v, o, ( cause != null ) ? ( "Internal error: " + cause.getMessage()) : "Internal Error.", cause );
        }

        /**
         * Constructor.
         *
         * @param v which Viewlet could not view
         * @param o which MeshObjectsToView it could not view
         * @param message a message, if any
         * @param cause the underlying internal Exception, if any
         */
        public InternalError(
                Viewlet           v,
                MeshObjectsToView o,
                String            message,
                Throwable         cause )
        {
            super( v, o, message, cause );
        }

        /**
         * Obtain a String representation of this instance that can be shown to the user.
         *
         * @param rep the StringRepresentation
         * @param pars collects parameters that may influence the String representation. Always provided.
         * @return String representation
         * @throws StringifierException thrown if there was a problem when attempting to stringify
         */
        @Override
        public String toStringRepresentation(
                StringRepresentation           rep,
                StringRepresentationParameters pars )
            throws
                StringifierException
        {
            String msg = getMessage();
            if( msg == null ) {
                msg = "";
            } else if( msg.length() > 0 ) {
                msg = " " + msg;
            }

            return rep.formatEntry(
                    getClass(),
                    "InternalErrorString",
                    pars,
            /* 0 */ theViewlet.getName(),
            /* 1 */ theViewlet.getUserVisibleName(),
            /* 2 */ theObjectsToView.getSubject(),
            /* 3 */ msg );
        }
    }
}
