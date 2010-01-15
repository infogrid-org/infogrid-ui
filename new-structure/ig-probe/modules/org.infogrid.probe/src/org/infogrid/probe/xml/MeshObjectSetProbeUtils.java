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

package org.infogrid.probe.xml;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.PropertyType;
import org.infogrid.model.primitives.RoleType;
import org.infogrid.modelbase.MeshTypeWithIdentifierNotFoundException;
import org.infogrid.modelbase.ModelBase;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Utility functions.
 */
public abstract class MeshObjectSetProbeUtils
{
    /**
     * This helper takes a String escaped according to escape and undos the escape.
     * FIXME This is not complete at all.
     *
     * @param enc the escaped String
     * @return the original String
     * @see #escape
     */
    @SuppressWarnings( "fallthrough" )
    static String descape(
            String enc )
    {
        StringBuffer ret = new StringBuffer( enc.length() ); // same length

        for( int i=0 ; i<enc.length() ; ++i ) {
            char c = enc.charAt( i );
            switch( c ) {
                case '&':
                    if( enc.charAt( i+1 ) == '#' ) {
                        i += 3; // we simply believe that the next two are going to be #x. FIXME?
                        String s = enc.substring( i );
                        int colonIndex = s.indexOf( ';' );
                        int v = Integer.parseInt( s.substring( 0, colonIndex ), 16 );

                        ret.append( new Character( (char) v ));

                        i += colonIndex;
                        break;
                    }
                    // else run-through

                default:
                    ret.append( c );
                    break;
            }
        }
        return ret.toString();
    }

    /**
     * Helper to parse an ISO time to a long.
     * 
     * @param timeString the String containing the ISO time
     * @return the long, in System.currentTimeMillis() format
     * @throws IllegalArgumentException thrown in case of a syntax error
     */
    static long parseTime(
            String timeString )
        throws
            IllegalArgumentException
    {
        Matcher m = theTimeStringPattern.matcher( timeString );
        if( !m.matches() ) {
            throw new IllegalArgumentException( "Invalid time format: '" + timeString + "'" );
        }

        int year   = Integer.parseInt( m.group( 1 ));
        int month  = Integer.parseInt( m.group( 2 ));
        int day    = Integer.parseInt( m.group( 3 ));
        int hour   = Integer.parseInt( m.group( 4 ));
        int minute = Integer.parseInt( m.group( 5 ));
        int second = Integer.parseInt( m.group( 6 ));
        double milli = Double.parseDouble( m.group( 7 ));

        if(    year   < 1900 || month  < 1  || month > 12 || day    < 1  || day > 31
            || hour   < 0    || hour   > 23 || minute < 0 || minute > 59
            || second < 0    || second > 62 || milli < 0  || milli  >= 1000 )
        {
            // leap seconds can go to 62, according to perldoc TimeDate
            throw new IllegalArgumentException( "Time element out of range '" + timeString + "'" );
        }

        Calendar cal = new GregorianCalendar( TimeZone.getTimeZone( "GMT" ));
        cal.set( year, month-1, day, hour, minute, second ); // month is 0-based
        cal.set( Calendar.MILLISECOND, (int) milli );
        long ret = cal.getTimeInMillis();

        return ret;
    }
    

    /**
     * Helper method to look up EntityTypes.
     *
     * @param identifiers the identifiers of the EntityTypes to be found
     * @param modelBase the ModelBase in which to find the EntityTypes
     * @return the found EntityTypes
     * @throws MeshTypeWithIdentifierNotFoundException thrown if an EntityType with one of the Identifiers cannot be found
     */
    protected static EntityType [] lookupEntityTypes(
            MeshTypeIdentifier[] identifiers,
            ModelBase            modelBase )
        throws
            MeshTypeWithIdentifierNotFoundException
    {
        EntityType [] ret = new EntityType[ identifiers.length ];
        for( int i=0 ; i<identifiers.length ; ++i ) {
            ret[i] = modelBase.findEntityTypeByIdentifier( identifiers[i] );
        }
        return ret;
    }
    
    /**
     * Helper method to look up a PropertyType.
     * 
     * @param identifier the identifier of the PropertyType to be found
     * @param modelBase the ModelBase in which to find the PropertyType
     * @return the found PropertyType
     * @throws MeshTypeWithIdentifierNotFoundException thrown if a PropertyType with the Identifier cannot be found
     */
    protected static PropertyType lookupPropertyType(
            MeshTypeIdentifier identifier,
            ModelBase          modelBase )
        throws
            MeshTypeWithIdentifierNotFoundException
    {
        PropertyType ret = modelBase.findPropertyTypeByIdentifier( identifier );
        return ret;
    }
    
    /**
     * Helper method to look up RoleTypes.
     *
     * @param identifiers list of identifiers of the RoleTypes to be found
     * @param modelBase the ModelBase in which to find the RoleTypes
     * @return the found RoleTypes
     * @throws MeshTypeWithIdentifierNotFoundException thrown if a RoleType with one of the Identifiers cannot be found
     */
    protected static RoleType [] lookupRoleTypes(
            ArrayList<MeshTypeIdentifier> identifiers,
            ModelBase                     modelBase )
        throws
            MeshTypeWithIdentifierNotFoundException
    {
        RoleType [] ret = new RoleType[ identifiers.size() ];
        for( int i=0 ; i<ret.length ; ++i ) {
            ret[i] = modelBase.findRoleTypeByIdentifier( identifiers.get( i ) );
        }
        return ret;
    }
    
    /**
     * Helper method to obtain the text attribute of a node.
     * 
     * @param attrs XML element
     * @param name name of the element containing the text
     * @return the text contained in the named element
     */
    protected static String getTextContent(
            NamedNodeMap attrs,
            String       name )
    {
        Node n = attrs.getNamedItem( name );
        if( n != null ) {
            return n.getTextContent();
        } else {
            return null;
        }
    }
 
    /**
     * The pattern to parse an ISO time.
     */
    protected static final Pattern theTimeStringPattern = Pattern.compile(
            "^(\\d{4})-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})Z$" );
}
