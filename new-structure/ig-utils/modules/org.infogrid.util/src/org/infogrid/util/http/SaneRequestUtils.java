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

package org.infogrid.util.http;

/**
 * Collects useful utilities related to SaneRequest.
 */
public abstract class SaneRequestUtils
{
    /**
     * Private constructor to keep this abstract.
     */
    private SaneRequestUtils()
    {
        // nothing
    }

    /**
     * Helper method to convert a class name into a suitable attribute name.
     *
     * @param clazz the Class
     * @return the attribute name
     */
    public static String classToAttributeName(
            Class<?> clazz )
    {
        String ret = clazz.getName();
        ret = ret.replaceAll( "\\.", "_" );
        return ret;
    }

    /**
     * Helper method to convert a class name and a local fragment into a suitable attribute name.
     *
     * @param clazz the Class
     * @param fragment the fragment, or local id
     * @return the attribute name
     */
    public static String classToAttributeName(
            Class<?> clazz,
            String   fragment )
    {
        String ret = clazz.getName();
        ret = ret.replaceAll( "\\.", "_" );
        ret = ret + "__" + fragment;
        return ret;
    }

    /**
     * Helper method to parse a URL given in String format, and return the value of the named argument.
     *
     * @param url the to-be-parsed URL
     * @param argName the name of the argument
     * @return the value of the argument in the URL, if any
     */
    public static String getUrlArgument(
            String url,
            String argName )
    {
        int pos = url.indexOf( '?' );
        if( pos == 0 ) {
            return null;
        }

        while( pos < url.length() ) {
            int pos2 = url.indexOf( '&', pos+1 );
            if( pos2 < 0 ) {
                pos2 = url.length();
            }
            String [] temp = url.substring( pos+1, pos2 ).split( "=" );
            String name  = temp[0];
            String value = temp.length > 1 ? temp[1] : "";

            name = HTTP.decodeUrlArgument( name );
            if( argName.equals( name )) {
                String ret = HTTP.decodeUrlArgument( value );
                return ret;
            }
            pos = pos2;
        }
        return null;
    }
}
