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
     * Obtain the StringRepresentationDirectory in which this StringRepresentation is defined.
     *
     * @return the StringRepresentationDirectory
     */
    public StringRepresentationDirectory getStringRepresentationDirectory();

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
     * @param pars collects parameters that may influence the String representation
     * @param args the arguments for the entry in the ResourceHelper
     * @return the formatted String
     */
    public String formatEntry(
            Class<? extends HasStringRepresentation> classOfFormattedObject,
            String                                   entry,
            StringRepresentationParameters           pars,
            Object...                                args );

    /**
     * Parse an entry that has been formatted using this StringRepresentation.
     *
     * @param classOfFormattedObject the class of the formatted object
     * @param entry the entry (prefixed by theName) of the resource
     * @param s the to-be-parsed String
     * @param factory optional factory object that may be required to instantiate one or more of the values. This is highly
     *        dependent on the context of use of this method.
     * @return the found values
     * @throws StringRepresentationParseException thrown if the String could not be parsed.
     */
    public Object [] parseEntry(
            Class<? extends HasStringRepresentation> classOfFormattedObject,
            String                                   entry,
            String                                   s,
            StringifierUnformatFactory               factory )
        throws
            StringRepresentationParseException;

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
            StringRepresentationParameters pars );

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
