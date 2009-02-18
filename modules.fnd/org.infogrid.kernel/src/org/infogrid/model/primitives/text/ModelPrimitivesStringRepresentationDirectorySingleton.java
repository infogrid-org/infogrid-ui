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

package org.infogrid.model.primitives.text;

import java.util.HashMap;
import java.util.Map;
import org.infogrid.util.CachingMap;
import org.infogrid.util.text.ArrayStringifier;
import org.infogrid.util.text.SimpleStringRepresentation;
import org.infogrid.util.text.StringRepresentation;
import org.infogrid.util.text.StringRepresentationDirectory;
import org.infogrid.util.text.StringRepresentationDirectorySingleton;
import org.infogrid.util.text.Stringifier;

/**
 * Extends StringRepresentationDirectorySingleton with the appropriate additional definitions
 * useful in an InfoGrid kernel context.
 */
public class ModelPrimitivesStringRepresentationDirectorySingleton
        extends
            StringRepresentationDirectorySingleton
{
    /**
     * Private constructor, use static singleton methods.
     *
     * @param storage the storage to use for this instance
     * @param fallback the fallback StringRepresentation, if any
     */
    protected ModelPrimitivesStringRepresentationDirectorySingleton(
            CachingMap<String,StringRepresentation> storage,
            StringRepresentation                    fallback )
    {
        super( storage, fallback );
    }

    /**
     * Initialize. This needs to be called to add the local definition to the defaults set in
     * the org.infogrid.util module.
     */
    public static void initialize()
    {
        if( theIsInitialized ) {
            return;
        }
        instantiateDefaultSingleton();

        StringRepresentation plain = theSingleton.get( TEXT_PLAIN_NAME );
        StringRepresentation html  = theSingleton.get( TEXT_HTML_NAME );

        Map<String,Stringifier<? extends Object>> plainMap = plain.getLocalStringifierMap();
        Map<String,Stringifier<? extends Object>> htmlMap  = html.getLocalStringifierMap();

        Map<String,Stringifier<? extends Object>> javadocMap = new HashMap<String,Stringifier<? extends Object>>();
        Map<String,Stringifier<? extends Object>> javaMap    = new HashMap<String,Stringifier<? extends Object>>();

    // old tags for new StringRepresentations

        // int
        // javadoc: same as html
        // java: same as plain

        // int2
        // javadoc: same as html
        // java: same as plain

        // int4
        // javadoc: same as html
        // java: same as plain

        // double
        // javadoc: same as html
        javaMap.put(    "double",         JavaDoubleStringifier.create() );

        // string
        javadocMap.put( "string",         JavadocHtmlStringStringifier.create() );
        javaMap.put(    "string",         JavaStringStringifier.create() );

        // stacktrace
        // javadoc: same as html
        // java: same as plain

        // urlappend
        // javadoc: same as html
        // java: same as plain

        // list
        // javadoc: same as html
        // java: FIXME?

        // new tags for all

        // enumarray
        plainMap.put(   "enumarray",      ArrayStringifier.create( EnumeratedValueStringifier.create(), ", " ));
        htmlMap.put(    "enumarray",      ArrayStringifier.create( EnumeratedValueStringifier.create(), "<ul><li>", "</li><li>", "</li></ul>", "" ));
        // javadoc: same as html
        // FIXME Java enumarray

        plainMap.put(   "multiplicity",   MultiplicityValueStringStringifier.create() );
        // html: same as plain
        // javadoc: same as html
        javaMap.put(    "multiplicity",   JavaPropertyValueStringifier.create());

        plainMap.put(   "escapehashstring", EscapeHashHtmlStringStringifier.create() );
        // html: same as plain
        // javadoc: same as html
        // java: same as plain

        plainMap.put(   "type",           DataTypeStringifier.create() );
        // html: same as plain
        // javadoc: same as html
        // java: same as plain

        plainMap.put(   "pv",             PropertyValueStringifier.create( plain, null ) );
        // html: same as plain
        // javadoc: same as html
        javaMap.put(    "pv",             JavaPropertyValueStringifier.create());


        SimpleStringRepresentation javadoc = SimpleStringRepresentation.create(
                StringRepresentationDirectory.TEXT_JAVADOC_NAME,
                javadocMap,
                html );
        SimpleStringRepresentation java = SimpleStringRepresentation.create(
                StringRepresentationDirectory.TEXT_JAVA_NAME,
                javaMap,
                plain );

        // after the fact due to dependency


        theSingleton.put( javadoc.getName(), javadoc  );
        theSingleton.put(    java.getName(), java  );
    }

    /**
     * Has this class been initialized.
     */
    protected static boolean theIsInitialized = false;
}
