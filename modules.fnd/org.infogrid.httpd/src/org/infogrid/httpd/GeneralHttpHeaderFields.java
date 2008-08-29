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

package org.infogrid.httpd;

/**
 * An HTTP General Header field. We don't do Cache-Control at this time (FIXME?).
 */
public interface GeneralHttpHeaderFields
{
    /* We don't do Cache-Control. */

    /** Connection. */
    public static final String CONNECTION_TAG = "Connection";

    /** Connection close. */
    public static final String CONNECTION_TAG_CLOSE = "close";

    /** Date. */
    public static final String DATE_TAG = "Date";

    /** Pragma. */
    public static final String PRAGMA_TAG = "Pragma";

    /** Trailer. */
    public static final String TRAILER_TAG = "Trailer";

    /** Transfer-Encoding. */
    public static final String TRANSFER_ENCODING_TAG = "Transfer-Encoding";

    /** Upgrade. */
    public static final String UPGRADE_TAG = "Upgrade";

    /** Via. */
    public static final String VIA_TAG = "Via";

    /** Warning. */
    public static final String WARNING_TAG = "Warning";

    /**
      * Separator between key and value.
      */
    public static final String SEPARATOR = ": ";

    /**
     * Semicolon separator (eg for cookies).
     */
    public static final String SEMI_SEPARATOR = ";";

    /**
     * Blank separator (eg for cookies).
     */
    public static final String BLANK_SEPARATOR = " ";

    /**
     * Carriage-return.
     */
    public static final String CR = "\n";

    /**
     * Equals symbol.
     */
    public static final String EQUALS = "=";
}
