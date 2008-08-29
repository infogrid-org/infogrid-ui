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

package org.infogrid.module;

import java.text.DateFormat;

/**
 * Collects the tag names in the ModuleAdvertisements' XML format.
 */
public interface ModuleXmlTags
{
    /**
     * The default DateFormat.
     */
    public static DateFormat theDefaultDateFormat = DateFormat.getInstance();

    /**
     * XML tag: dependencies.
     */
    public static final String DEPENDENCIES_TAG = "dependencies";

    /**
     * XML tag: requires.
     */
    public static final String REQUIRES_TAG = "requires";

    /**
     * XML tag: capability.
     */
    public static final String CAPABILITY_TAG = "capability";

    /**
     * XML tag: argumentcombination.
     */
    public static final String ARGUMENTCOMBINATION_TAG = "argumentcombination";

    /**
     * XML tag: standardmodule.
     */
    public static final String STANDARDMODULE_TAG = "standardmodule";

    /**
     * XML tag: modelmodule.
     */
    public static final String MODELMODULE_TAG = "modelmodule";

    /**
     * XML tag: name.
     */
    public static final String NAME_TAG = "name";

    /**
     * XML tag: version.
     */
    public static final String VERSION_TAG = "version";

    /**
     * XML tag: username.
     */
    public static final String USERNAME_TAG = "username";

    /**
     * XML tag: description.
     */
    public static final String USERDESCRIPTION_TAG = "userdescription";

    /**
     * XML tag: build time
     */
    public static final String BUILD_TIME_TAG = "built";
            
    /**
     * XML tag: provides.
     */
    public static final String PROVIDES_TAG = "provides";

    /**
     * XML tag: jar.
     */
    public static final String JAR_TAG = "jar";

    /**
     * XML tag: activationclass.
     */
    public static final String ACTIVATIONCLASS_TAG = "activationclass";

    /**
     * XML tag: configurationclass.
     */
    public static final String CONFIGURATIONCLASS_TAG = "configurationclass";

    /**
     * XML tag: runclass.
     */
    public static final String RUNCLASS_TAG = "runclass";

    /**
     * XML tag: interface.
     */
    public static final String INTERFACE_TAG = "interface";

    /**
     * XML tag: implementation.
     */
    public static final String IMPLEMENTATION_TAG = "implementation";

    /**
     * XML tag: arg.
     */
    public static final String ARG_TAG = "arg";

    /**
     * XML tag: parameter.
     */
    public static final String PARAMETER_TAG = "parameter";

    /**
     * XML parameter: name.
     */
    public static final String NAME_PAR = "name";

    /**
     * XML parameter: version.
     */
    public static final String VERSION_PAR = "version";

    /**
     * XML parameter: mode.
     */
    public static final String MODE_PAR = "mode";

    /**
     * XML parameter value: both.
     */
    public static final String MODE_PAR_BOTH = "both";

    /**
     * XML parameter value: buildtime.
     */
    public static final String MODE_PAR_BUILDTIME = "buildtime";

    /**
     * XML parameter value: runtime.
     */
    public static final String MODE_PAR_RUNTIME = "runtime";

    /**
     * XML parameter: default.
     */
    public static final String DEFAULT_PAR = "default";

    /**
     * XML parameter: value.
     */
    public static final String VALUE_PAR = "value";

    /**
     * XML parameter: baseurl
     */
    public static final String BASE_URL_PAR = "baseurl";

    /**
     * XML parameter: locale
     */
    public static final String LOCALE_PAR = "locale";    
}
