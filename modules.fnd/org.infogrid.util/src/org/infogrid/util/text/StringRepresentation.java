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

/**
 * One way of representing Objects as Strings.
 */
public interface StringRepresentation
{
    /**
     * Obtain the name of the StringRepresentation.
     *
     * @return the name
     */
    public String getName();

    /**
     * Determine whether this String matches this StringRepresentation.
     *
     * @param candidate the candidate String
     * @return true if it matches
     */
    public boolean matches(
            String candidate );

    /**
     * Format the parameters according to the rules for classOfFormattedObject,
     * entry entryName and this StringRepresentation
     * 
     * @param classOfFormattedObject the class of the to-be-formatted object
     * @param entry the entry in the ResourceHelper (but qualified by the prefix of this StringRepresentation)
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial if applicable, output in colloquial form
     * @param args the arguments for the entry in the ResourceHelper
     * @return the formatted String
     */
    public String formatEntry(
            Class<? extends HasStringRepresentation> classOfFormattedObject,
            String                                   entry,
            int                                      maxLength,
            boolean                                  colloquial,
            Object...                                args );

    /**
     * Parse an entry that has been formatted using this StringRepresentation.
     *
     * @param classOfFormattedObject the class of the formatted object
     * @param entry the entry (prefixed by theName) of the resource
     * @param s the to-be-parsed String
     * @return the found values
     * @throws StringifierException thrown if the String could not be parsed.
     */
    public Object [] parseEntry(
            Class<? extends HasStringRepresentation> classOfFormattedObject,
            String                                   entry,
            String                                   s )
        throws
            StringifierException;

    /**
     * Format a Throwable about which nothing else is known.
     * 
     * @param t the Throwable
     * @param context the StringRepresentationContext to use
     * @param maxLength maximum length of emitted String. -1 means unlimited.
     * @param colloquial if applicable, output in colloquial form
     * @return String representation
     */
    public String formatThrowable(
            Throwable                   t,
            StringRepresentationContext context,
            int                         maxLength,
            boolean                     colloquial );

    /**
     * Obtain the local StringifierMap. This enables modification of the map.
     *
     * @return the stringifier map
     */
    public Map<String,Stringifier<? extends Object>> getLocalStringifierMap();

    /**
     * Obtain the StringifierMap that recursively contains the maps of all delegates.
     *
     * @return the stringifier map
     */
    public Map<String,Stringifier<? extends Object>> getRecursiveStringifierMap();
}
