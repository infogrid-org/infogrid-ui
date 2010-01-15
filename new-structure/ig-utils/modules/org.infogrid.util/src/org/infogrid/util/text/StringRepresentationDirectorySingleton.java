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
     */
    protected StringRepresentationDirectorySingleton()
    {
        super();
    }

    /**
     * Obtain the singleton StringRepresentationDirectory.
     *
     * @return the StringRepresentationDirectory
     */
    public static StringRepresentationDirectorySingleton getSingleton()
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
            StringRepresentationDirectorySingleton newValue )
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
        HashMap<String,Stringifier<? extends Object>> plainMap = new HashMap<String,Stringifier<? extends Object>>();
        HashMap<String,Stringifier<? extends Object>> htmlMap  = new HashMap<String,Stringifier<? extends Object>>();
        HashMap<String,Stringifier<? extends Object>> urlMap   = new HashMap<String,Stringifier<? extends Object>>();

        plainMap.put(   "int",            LongStringifier.create() );
        // html: same as plain
        // url:  same as plain

        plainMap.put(   "int2",           LongStringifier.create( 2 ) );
        // html: same as plain
        // url:  same as plain

        plainMap.put(   "int4",           LongStringifier.create( 4 ) );
        // html: same as plain
        // url:  same as plain

        plainMap.put(   "hex",            LongStringifier.create( -1, 16 ) );
        // html: same as plain
        // url:  same as plain

        plainMap.put(   "hex2",           LongStringifier.create( 2, 16 ) );
        // html: same as plain
        // url:  same as plain

        plainMap.put(   "hex4",           LongStringifier.create( 4, 16 ) );
        // html: same as plain
        // url:  same as plain

        plainMap.put(   "float",          FloatStringifier.create() );
        // html: same as plain
        // url:  same as plain

        plainMap.put(   "double",         DoubleStringifier.create() );
        // html: same as plain
        // url:  same as plain

        plainMap.put(   "string",         StringStringifier.create() );
        htmlMap.put(    "string",         HtmlStringStringifier.create() );
        // url:  same as plain

        plainMap.put(   "verbatim",       StringStringifier.create() );
        htmlMap.put(    "verbatim",       StringStringifier.create() );
        // url:  same as plain

        plainMap.put(   "stacktrace",     StacktraceStringifier.create() );
        htmlMap.put(    "stacktrace",     HtmlStacktraceStringifier.create() );
        // url:  same as plain

        plainMap.put(   "urlappend",      UrlAppendStringifier.create() );
        // html: same as plain
        // url:  same as plain

        plainMap.put(   "urlarg",         UrlArgStringifier.create() );
        // html: same as plain
        // url:  same as plain

        plainMap.put(   "id",             IdentifierStringifier.create() );
        // html: same as plain
        // url:  same as plain

        plainMap.put(   "idarray",        ArrayStringifier.create( IdentifierStringifier.create(), ", " ));
        // html: same as plain
        // url:  same as plain

        plainMap.put(   "hasid",          HasIdentifierStringifier.create() );
        // html: same as plain
        // url:  same as plain

        plainMap.put(   "hasidarray",     ArrayStringifier.create( HasIdentifierStringifier.create(), ", " ));
        // html: same as plain
        // url:  same as plain

        plainMap.put(   "class",          ClassStringifier.create() );
        // html: same as plain
        // url:  same as plain

        plainMap.put(   "list",           ListStringifier.create( ", " ));
        htmlMap.put(    "list",           ListStringifier.create( "<li>", "</li>\n<li>", "</li>", "" ));
        // url:  same as plain

        plainMap.put(   "htmlescaped",    HtmlStringStringifier.create() );
        plainMap.put(   "javascriptescaped", JavaScriptStringStringifier.create() );

        theSingleton = new StringRepresentationDirectorySingleton(); // not the factory method here

        SimpleStringRepresentation plain = SimpleStringRepresentation.create(
                theSingleton,
                StringRepresentationDirectory.TEXT_PLAIN_NAME,
                plainMap );

        @SuppressWarnings("unchecked")
        SimpleStringRepresentation editPlain = SimpleStringRepresentation.create(
                theSingleton,
                StringRepresentationDirectory.EDIT_TEXT_PLAIN_NAME,
                (HashMap<String,Stringifier<? extends Object>>) plainMap.clone() );

        SimpleStringRepresentation html = SimpleStringRepresentation.create(
                theSingleton,
                StringRepresentationDirectory.TEXT_HTML_NAME,
                htmlMap,
                plain );

        @SuppressWarnings("unchecked")
        SimpleStringRepresentation editHtml = SimpleStringRepresentation.create(
                theSingleton,
                StringRepresentationDirectory.EDIT_TEXT_HTML_NAME,
                (HashMap<String,Stringifier<? extends Object>>) htmlMap.clone(),
                plain );

        SimpleStringRepresentation url = SimpleStringRepresentation.create(
                theSingleton,
                StringRepresentationDirectory.TEXT_URL_NAME,
                urlMap,
                plain );

        theSingleton.put(     plain.getName(), plain );
        theSingleton.put( editPlain.getName(), editPlain );
        theSingleton.put(      html.getName(), html );
        theSingleton.put(  editHtml.getName(), editHtml );
        theSingleton.put(       url.getName(), url );

        theSingleton.setFallback( plain );

        return theSingleton;
    }

    /**
     * The current singleton, initialized to a minimum set of defaults when used for the first time.
     */
    protected static StringRepresentationDirectorySingleton theSingleton;
}
