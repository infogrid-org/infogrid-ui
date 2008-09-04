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

package org.infogrid.viewlet;

import org.infogrid.mesh.MeshObjectIdentifier;

import org.infogrid.module.ModuleRegistry;

import org.infogrid.util.FactoryException;
import org.infogrid.util.LocalizedException;
import org.infogrid.util.LocalizedObjectFormatter;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.StringHelper;

/**
 * Thrown when a Viewlet cannot view the MeshObjectsToView that have been
 * given to it. Use the inner classes to be specific about what is going on.
 */
public abstract class CannotViewException
        extends
            FactoryException
        implements
            LocalizedException
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
     * Obtain localized message, per JDK 1.5.
     *
     * @return localized message
     */
    @Override
    public String getLocalizedMessage()
    {
        return getLocalizedMessage( null );
    }

    /**
     * For debugging.
     *
     * @return String representation of this object.
     */
    @Override
    public String toString()
    {
        return StringHelper.objectLogString(
                this,
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
     * The ResourceHelper for all classes in this file.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( CannotViewException.class );
    
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
         * Determine the correct internationalized string that can be shown to the
         * user when the LocalizedException is thrown.
         *
         * @param formatter the formatter to use for data objects to be displayed as part of the message
         * @return the internationalized string
         */
        public String getLocalizedMessage(
                LocalizedObjectFormatter formatter )
        {
            String ret;
            if( theObjectsToView.getViewletTypeName() == null ) {
                if( formatter != null ) {
                    ret = theResourceHelper.getResourceStringWithArguments(
                            "ViewletClassNotCompatibleWithSubjectMessage",
                            formatter.asLocalizedString( theViewlet.getUserVisibleName() ),
                            formatter.asLocalizedString( theObjectsToView.getSubject() ));
                } else {
                    ret = theResourceHelper.getResourceStringWithArguments(
                            "ViewletClassNotCompatibleWithSubjectMessage",
                            theViewlet.getUserVisibleName(),
                            theObjectsToView.getSubject() );
                }
            } else {
                if( formatter != null ) {
                    ret = theResourceHelper.getResourceStringWithArguments(
                            "ViewletClassNotCompatibleWithTypeMessage",
                            formatter.asLocalizedString( theViewlet.getUserVisibleName() ),
                            formatter.asLocalizedString( theObjectsToView.getSubject() ),
                            formatter.asLocalizedString( theObjectsToView.getViewletTypeName() ));
                } else {
                    ret = theResourceHelper.getResourceStringWithArguments(
                            "ViewletClassNotCompatibleWithTypeMessage",
                            theViewlet.getUserVisibleName(),
                            theObjectsToView.getSubject(),
                            theObjectsToView.getViewletTypeName() );
                }            
            }
            return ret;
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
         * Determine the correct internationalized string that can be shown to the
         * user when the LocalizedException is thrown.
         *
         * @param formatter the formatter to use for data objects to be displayed as part of the message
         * @return the internationalized string
         */
        public String getLocalizedMessage(
                LocalizedObjectFormatter formatter )
        {
            String ret;
            if( formatter != null ) {
                ret = theResourceHelper.getResourceStringWithArguments(
                        "ObjectTypeNotAllowedMessage",
                        formatter.asLocalizedString( theViewlet.getUserVisibleName() ),
                        formatter.asLocalizedString( theObjectsToView.getSubject() ));
            } else {
                ret = theResourceHelper.getResourceStringWithArguments(
                        "ObjectTypeNotAllowedMessage",
                        theViewlet.getUserVisibleName(),
                        theObjectsToView.getSubject() );
            }
            return ret;
        }
    }

    /**
     * No Viewlet could be found with the the required Viewlet type.
     */
    public static class NoViewletFound
            extends
                CannotViewException
    {
        private static final long serialVersionUID = 1L; // helps with serialization

        /**
         * Constructor.
         *
         * @param o which MeshObjectsToView it could not view
         */
        public NoViewletFound(
                MeshObjectsToView o )
        {
            super( null, o, null, null );
            
            theModuleRegistry = null;
        }

        /**
         * Constructor.
         *
         * @param o which MeshObjectsToView it could not view
         * @param registry the ModuleRegistry in which the Viewlet was not found
         */
        public NoViewletFound(
                MeshObjectsToView o,
                ModuleRegistry    registry )
        {
            super( null, o, null, null );
            
            theModuleRegistry = registry;
        }

        /**
         * For debugging.
         *
         * @return String representation of this object.
         */
        @Override
        public String toString()
        {
            return StringHelper.objectLogString(
                    this,
                    new String[] {
                        "viewlet",
                        "objectsToView",
                        "moduleRegistry content"
                    },
                    new Object[] {
                        theViewlet,
                        theObjectsToView,
                        theModuleRegistry
            } );
        }
    
        /**
         * Determine the correct internationalized string that can be shown to the
         * user when the LocalizedException is thrown.
         *
         * @param formatter the formatter to use for data objects to be displayed as part of the message
         * @return the internationalized string
         */
        public String getLocalizedMessage(
                LocalizedObjectFormatter formatter )
        {
            String ret;
            if( theObjectsToView.getViewletTypeName() == null ) {
                if( formatter != null ) {
                    ret = theResourceHelper.getResourceStringWithArguments(
                            "NoViewletFoundWithoutTypeMessage",
                            formatter.asLocalizedString( theObjectsToView.getSubject() ));
                } else {
                    ret = theResourceHelper.getResourceStringWithArguments(
                            "NoViewletFoundWithoutTypeMessage",
                            theObjectsToView.getSubject() );
                }
            } else {
                if( formatter != null ) {
                    ret = theResourceHelper.getResourceStringWithArguments(
                            "NoViewletFoundWithTypeMessage",
                            formatter.asLocalizedString( theObjectsToView.getSubject() ));
                } else {
                    ret = theResourceHelper.getResourceStringWithArguments(
                            "NoViewletFoundWithTypeMessage",
                            theObjectsToView.getSubject() );
                }
            }
            return ret;
        }
        
        /**
         * The ModuleRegistry that did not contain a suitable Viewlet.
         */
        protected transient ModuleRegistry theModuleRegistry;
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
         * Determine the correct internationalized string that can be shown to the
         * user when the LocalizedException is thrown.
         *
         * @param formatter the formatter to use for data objects to be displayed as part of the message
         * @return the internationalized string
         */
        public String getLocalizedMessage(
                LocalizedObjectFormatter formatter )
        {
            String ret;
            if( formatter != null ) {
                ret = theResourceHelper.getResourceStringWithArguments(
                        "InvalidViewletMessage",
                        formatter.asLocalizedString( theViewlet.getUserVisibleName() ),
                        formatter.asLocalizedString( theViewlet.getClass().getName() ));
            } else {
                ret = theResourceHelper.getResourceStringWithArguments(
                        "InvalidViewletMessage",
                        theViewlet.getUserVisibleName(),
                        theViewlet.getClass().getName() );
            }
            return ret;
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
         * @param name the name of the Parameter that was missing
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
         * Determine the correct internationalized string that can be shown to the
         * user when the LocalizedException is thrown.
         *
         * @param formatter the formatter to use for data objects to be displayed as part of the message
         * @return the internationalized string
         */
        public String getLocalizedMessage(
                LocalizedObjectFormatter formatter )
        {
            String ret;
            if( formatter != null ) {
                ret = theResourceHelper.getResourceStringWithArguments(
                        "ParameterMissingMessage",
                        formatter.asLocalizedString( theViewlet.getUserVisibleName() ),
                        formatter.asLocalizedString( theName ));
            } else {
                ret = theResourceHelper.getResourceStringWithArguments(
                        "ParameterMissingMessage",
                        theViewlet.getUserVisibleName(),
                        theName );
            }
            return ret;
        }
        
        /**
         * Name of the missing parameter.
         */
        protected String theName;
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
         * Determine the correct internationalized string that can be shown to the
         * user when the LocalizedException is thrown.
         *
         * @param formatter the formatter to use for data objects to be displayed as part of the message
         * @return the internationalized string
         */
        public String getLocalizedMessage(
                LocalizedObjectFormatter formatter )
        {
            String ret;
            if( formatter != null ) {
                ret = theResourceHelper.getResourceStringWithArguments( "NoSubjectMessage", formatter.asLocalizedString( theIdentifier ));
            } else {
                ret = theResourceHelper.getResourceStringWithArguments( "NoSubjectMessage", theIdentifier );
            }
            return ret;
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
            super( v, o, "Internal error", cause );
        }

        /**
         * Determine the correct internationalized string that can be shown to the
         * user when the LocalizedException is thrown.
         *
         * @param formatter the formatter to use for data objects to be displayed as part of the message
         * @return the internationalized string
         */
        public String getLocalizedMessage(
                LocalizedObjectFormatter formatter )
        {
            String ret = theResourceHelper.getResourceString( "InternalErrorMessage" );
            return ret;
        }
    }
}
