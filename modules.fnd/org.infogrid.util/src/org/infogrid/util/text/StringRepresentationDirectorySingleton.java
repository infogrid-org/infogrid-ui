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

import java.util.HashMap;
import java.util.Map;
import org.infogrid.util.CachingMap;
import org.infogrid.util.MCachingHashMap;

/**
 * Helps find a StringRepresentationDirectory. The found StringRepresentationDirectory
 * is automatically initialized with a minimum set of entries. Applications may
 * set a different StringRepresentationDirectory if they like.
 */
public class StringRepresentationDirectorySingleton
        extends
            SimpleStringRepresentationDirectory
{
    /**
     * Private constructor, use static singleton methods.
     *
     * @param storage the storage to use for this instance
     * @param fallback the fallback StringRepresentation, if any
     */
    protected StringRepresentationDirectorySingleton(
            CachingMap<String,StringRepresentation> storage,
            StringRepresentation                    fallback )
    {
        super( null, storage, fallback );
        // no delegate factory
    }

    /**
     * Obtain the singleton StringRepresentationDirectory.
     *
     * @return the StringRepresentationDirectory
     */
    public static StringRepresentationDirectory getSingleton()
    {
        if( theSingleton == null ) {
            // never mind threads
            instantiateDefaultSingleton();
        }
        return theSingleton;
    }

    /**
     * Set a different StringRepresentationDirectory as the singleton.
     *
     * @param newValue the new value for the singleton
     */
    public static void setSingleton(
            StringRepresentationDirectory newValue )
    {
        theSingleton = newValue;
    }

    /**
     * Create the default singleton.
     *
     * @return the created default singleton
     */
    protected static StringRepresentationDirectory instantiateDefaultSingleton()
    {
        Map<String,Stringifier<? extends Object>> plainMap   = new HashMap<String,Stringifier<? extends Object>>();
        Map<String,Stringifier<? extends Object>> htmlMap    = new HashMap<String,Stringifier<? extends Object>>();

        plainMap.put(   "int",            LongStringifier.create() );
        // html: same as plain

        plainMap.put(   "int2",           LongStringifier.create( 2 ) );
        // html: same as plain

        plainMap.put(   "int4",           LongStringifier.create( 4 ) );
        // html: same as plain

        plainMap.put(   "double",         DoubleStringifier.create() );
        // html: same as plain

        plainMap.put(   "string",         StringStringifier.create() );
        htmlMap.put(    "string",         HtmlStringStringifier.create() );

        plainMap.put(   "verbatim",       StringStringifier.create() );
        htmlMap.put(    "verbatim",       StringStringifier.create() );

        plainMap.put(   "stacktrace",     StacktraceStringifier.create() );
        htmlMap.put(    "stacktrace",     HtmlStacktraceStringifier.create() );

        plainMap.put(   "urlappend",      UrlAppendStringifier.create() );
        // html: same as plain

        plainMap.put(   "urlarg",         UrlArgStringifier.create() );
        // html: same as plain

        plainMap.put(   "id",             IdentifierStringifier.create() );
        htmlMap.put(    "id",             IdentifierStringifier.create( "<code>", "</code>" ));

        plainMap.put(   "idarray",        ArrayStringifier.create( IdentifierStringifier.create(), ", " ));
        // html: same as plain

        plainMap.put(   "hasid",          HasIdentifierStringifier.create() );
        // html: same as plain

        plainMap.put(   "hasidarray",     ArrayStringifier.create( HasIdentifierStringifier.create(), ", " ));
        // html: same as plain

        plainMap.put(   "class",          ClassStringifier.create() );
        // html: same as plain

        plainMap.put(   "list",           ListStringifier.create( ", " ));
        htmlMap.put(    "list",           ListStringifier.create( "<ul><li>", "</li>\n<li>", "</li></ul>", "<ul class=\"empty\"></ul>" ));

        MCachingHashMap<String,StringRepresentation> storage = MCachingHashMap.create();

        SimpleStringRepresentation plain = SimpleStringRepresentation.create(
                StringRepresentationDirectory.TEXT_PLAIN_NAME,
                plainMap );
        SimpleStringRepresentation html = SimpleStringRepresentation.create(
                StringRepresentationDirectory.TEXT_HTML_NAME,
                htmlMap,
                plain );

        theSingleton = new StringRepresentationDirectorySingleton( storage, plain );

        theSingleton.put(   plain.getName(), plain );
        theSingleton.put(    html.getName(), html  );

        return theSingleton;
    }

    /**
     * The current singleton, initialized to a minimum set of defaults when used for the first time.
     */
    protected static StringRepresentationDirectory theSingleton;
}
