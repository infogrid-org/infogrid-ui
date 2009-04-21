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

import java.util.Map;
import org.infogrid.util.ArrayFacade;
import org.infogrid.util.DelegatingMap;
import org.infogrid.util.ResourceHelper;
import org.infogrid.util.StringHelper;
import org.infogrid.util.logging.CanBeDumped;
import org.infogrid.util.logging.Dumper;
import org.infogrid.util.logging.Log;

/**
 * Enumerates the different ways of representing Objects as Strings.
 */
public class SimpleStringRepresentation
        implements
            StringRepresentation,
            CanBeDumped
{
    private static final Log log = Log.getLogInstance( SimpleStringRepresentation.class ); // our own, private logger

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
        theName                = name;
        theLocalStringifierMap = map;
        theDelegate            = delegate;

        if( delegate != null ) {
            theRecursiveStringifierMap = DelegatingMap.create( theLocalStringifierMap, delegate.getRecursiveStringifierMap() );
        }
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
     * entry entryName and this StringRepresentation.
     * 
     * @param classOfFormattedObject the class of the to-be-formatted object
     * @param entry the entry in the ResourceHelper (but qualified by the prefix of this StringRepresentation)
     * @param args the arguments for the entry in the ResourceHelper
     * @return the formatted String
     */
    public String formatEntry(
            Class<? extends HasStringRepresentation> classOfFormattedObject,
            String                                   entry,
            StringRepresentationParameters           pars,
            Object...                                args )
    {
        ResourceHelper rh           = ResourceHelper.getInstance( classOfFormattedObject, true );
        String         formatString = rh.getResourceStringOrDefault( theName + entry, null );

        if( formatString != null ) {
            try {
                AnyMessageStringifier stringifier = AnyMessageStringifier.create( formatString, getRecursiveStringifierMap() );

                String ret = stringifier.format( null, ArrayFacade.<Object>create( args ), pars );
                return ret;

            } catch( StringifierException ex ) {
                log.error( ex );
                return null;
            }
        }
        if( theDelegate != null ) {
            return theDelegate.formatEntry( classOfFormattedObject, entry, pars, args );
        }
        String ret = rh.getResourceString( theName + entry ); // will emit warning
        if( pars != null ) {
            Number n = (Number) pars.get( StringRepresentationParameters.MAX_LENGTH );
            if( n != null ) {
                ret = StringHelper.potentiallyShorten( ret, n.intValue() );
            }
        }
        return ret;
    }

    /**
     * Parse an entry that has been formatted using this StringRepresentation.
     *
     * @param classOfFormattedObject the class of the formatted object
     * @param entry the entry (prefixed by theName) of the resource
     * @param s the to-be-parsed String
     * @return the found values
     * @throws StringRepresentationParseException thrown if the String could not be parsed.
     */
    public Object [] parseEntry(
            Class<? extends HasStringRepresentation> classOfFormattedObject,
            String                                   entry,
            String                                   s )
        throws
            StringRepresentationParseException
    {
        ResourceHelper        rh           = ResourceHelper.getInstance( classOfFormattedObject );
        String                formatString = rh.getResourceStringOrDefault( theName + entry, null );

        if( formatString != null ) {
            try {
                AnyMessageStringifier stringifier = AnyMessageStringifier.create( formatString, getRecursiveStringifierMap() );

                Object [] ret = stringifier.unformat( s ).getArray();
                return ret;

            } catch( StringifierParseException ex ) {
                throw new StringRepresentationParseException( s, formatString, ex );

            } catch( CompoundStringifierCompileException ex ) {
                log.error( ex );
            }
        }
        if( theDelegate != null ) {
            return theDelegate.parseEntry( classOfFormattedObject, entry, s );
        }
        Object ignore = rh.getResourceString( theName + entry ); // will emit warning
        return new Object[0];
    }

    /**
     * Format a Throwable about which nothing else is known.
     * 
     * @param t the Throwable
     * @param context the StringRepresentationContext to use
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     */
    public String formatThrowable(
            Throwable                      t,
            StringRepresentationContext    context,
            StringRepresentationParameters pars )
    {
        String ret;
        if( t instanceof HasStringRepresentation ) {
            ret = formatHasStringRepresentationThrowable( (HasStringRepresentation) t, context, pars );

        } else {
            ret = formatNoStringRepresentationThrowable( t, context, pars );
        }
        return ret;
    }

    /**
     * Format a Throwable that has a StringRepresentation per declared interface.
     * 
     * @param t the Throwable
     * @param context the StringRepresentationContext to use
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     */
    public String formatHasStringRepresentationThrowable(
            HasStringRepresentation        t,
            StringRepresentationContext    context,
            StringRepresentationParameters pars )
    {
        String ret = t.toStringRepresentation( this, context, pars );
        return ret;
    }

    /**
     * Format a Throwable that does not have a StringRepresentation per declared interface.
     * By default, we format 
     * @param t the Throwable
     * @param context the StringRepresentationContext to use
     * @param pars collects parameters that may influence the String representation
     * @return String representation
     */
    public String formatNoStringRepresentationThrowable(
            Throwable                      t,
            StringRepresentationContext    context,
            StringRepresentationParameters pars )
    {
        try {
            String                formatString = theResourceHelper.getResourceString( theName + "ThrowableMessage" );
            AnyMessageStringifier stringifier  = AnyMessageStringifier.create( formatString, getRecursiveStringifierMap() );

            String message          = t.getMessage();
            String localizedMessage = t.getLocalizedMessage();
            
            if( message == null ) {
                message = t.getClass().getName();
            }
            if( localizedMessage == null ) {
                localizedMessage = t.getClass().getName();
            }

            Object [] args = { t.getClass(), message, localizedMessage, t };
            String ret = stringifier.format( null, ArrayFacade.<Object>create( args ), pars );
            return ret;

        } catch( StringifierException ex ) {
            log.error( ex );
            return null;
        }
    }

    /**
     * Obtain the local StringifierMap. This enables modification of the map.
     *
     * @return the stringifier map
     */
    public Map<String,Stringifier<? extends Object>> getLocalStringifierMap()
    {
        return theLocalStringifierMap;

    }

    /**
     * Obtain the StringifierMap that recursively contains the maps of all delegates.
     *
     * @return the stringifier map
     */
    public Map<String,Stringifier<? extends Object>> getRecursiveStringifierMap()
    {
        if( theRecursiveStringifierMap != null ) {
            return theRecursiveStringifierMap;
        } else {
            return theLocalStringifierMap;
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
     * Dump this object.
     *
     * @param d the Dumper to dump to
     */
    public void dump(
            Dumper d )
    {
        d.dump( this,
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
     * Convert to String, for debugging.
     *
     * @return String form
     */
    @Override
    public String toString()
    {
        return super.toString() + "{ name: " + theName + " }";
    }

    /**
     * The name of the StringRepresentation, which also used as prefix in the ResourceHelper.
     */
    protected String theName;

    /**
     * The local StringifierMap to be used with this StringRepresentation.
     */
    protected Map<String,Stringifier<? extends Object>> theLocalStringifierMap;

    /**
     * The StringifierMap to be used with this StringRepresentation.
     */
    protected DelegatingMap<String,Stringifier<? extends Object>> theRecursiveStringifierMap;

    /**
     * The delegate StringRepresentation, if any.
     */
    protected StringRepresentation theDelegate;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( SimpleStringRepresentation.class );
}
