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

/**
 * The tags used for the MeshObjectSetProbe XML format.
 */
public interface MeshObjectSetProbeTags
{
    /** Tag indicating a MeshObjectSet */
    public static final String MESHOBJECT_SET_TAG = "MeshObjectSet";
    
    /** Tag indicating a MeshObject. */
    public static final String MESHOBJECT_TAG = "MeshObject";

    /** Tag indicating the time the MeshObject was created. */
    public static final String TIME_CREATED_TAG = "timeCreated";

    /** Tag indicating the time the MeshObject was updated. */
    public static final String TIME_UPDATED_TAG = "timeUpdated";

    /** Tag indicating the time the MeshObject was read. */
    public static final String TIME_READ_TAG = "timeRead";

    /** Tag indicating the time the MeshObject will auto-delete. */
    public static final String TIME_AUTO_DELETES_TAG = "timeAutoDeletes";

    /** Tag indicating the identity, aka Identifier of a MeshObject. */
    public static final String IDENTIFIER_TAG = "ID";

    /** Tag indicating meta, aka type information. */
    public static final String MESH_TYPE_TAG = "MeshType";

    /** Tag indicating meta, aka type information. */
    public static final String TYPE_TAG = "type";

    /** Tag indicating a PropertyType. */
    public static final String PROPERTY_TYPE_TAG = "Property";
    
    /** Tag indicating a RoleType. */
    public static final String ROLE_TYPE_TAG = "Role";
    
    /** Tag indicating a Relationship. */
    public static final String RELATIONSHIP_TAG = "Relationship";

    
    /** Tag indicating that a PropertyValue is null. */
    public static final String NULL_VALUE = "null";

    /** Tag indicating that this is a BlobValue. */
    public static final String BLOB_VALUE_TAG = "BlobValue";

    /** Tag indicating that this is the mime attribute of a BlobValue. */
    public static final String BLOB_VALUE_MIME_TAG = "mime";

    /** Tag indicating that this is the loadFrom attribute of a BlobValue. */
    public static final String BLOB_VALUE_LOAD_TAG = "url";
    
            
    /** Tag indicating that this is a BooleanValue. */
    public static final String BOOLEAN_VALUE_TAG = "BooleanValue";
    
    /** Tag indicating that a BooleanValue is TRUE. */
    public static final String BOOLEAN_VALUE_TRUE_TAG = "TRUE";
    
    /** Tag indicating that a BooleanValue is FALSE. */
    public static final String BOOLEAN_VALUE_FALSE_TAG = "FALSE";
    
    /** Tag indicating that this is a ColorValue. */
    public static final String COLOR_VALUE_TAG = "ColorValue";
    
    /** Tag indicating the red component of a ColorValue */
    public static final String COLOR_VALUE_RED_TAG = "red";
    
    /** Tag indicating the green component of a ColorValue */
    public static final String COLOR_VALUE_GREEN_TAG = "green";
    
    /** Tag indicating the blue component of a ColorValue */
    public static final String COLOR_VALUE_BLUE_TAG = "blue";
    
    /** Tag indicating the alpha component of a ColorValue */
    public static final String COLOR_VALUE_ALPHA_TAG = "alpha";
    
    /** Tag indicating that this is an EnumeratedValue. */
    public static final String ENUMERATED_VALUE_TAG = "EnumeratedValue";
    
    /** Tag indicating that this is an ExtentValue. */
    public static final String EXTENT_VALUE_TAG = "ExtentValue";

    /** Tag indicating that this is the Width component of an ExtentValue. */
    public static final String EXTENT_VALUE_WIDTH_TAG = "w";

    /** Tag indicating that this is the Height component of an ExtentValue. */
    public static final String EXTENT_VALUE_HEIGHT_TAG = "w";

    /** Tag indicating that this is an IntegerValue. */
    public static final String INTEGER_VALUE_TAG = "IntegerValue";
    
    /** Tag indicating that this is a FloatValue. */
    public static final String FLOAT_VALUE_TAG = "FloatValue";
    
    /** Tag indicating that this is a MultiplicityValue. */
    public static final String MULTIPLICITY_VALUE_TAG = "MultiplicityValue";

    /** Tag indicating that this is the minimum component of a MultiplicityValue. */
    public static final String MULTIPLICITY_VALUE_MIN_TAG = "min";
    
    /** Tag indicating that this is the maximum component of a MultiplicityValue. */
    public static final String MULTIPLICITY_VALUE_MAX_TAG = "max";
    
    /** Tag indicating that this is a PointValue. */
    public static final String POINT_VALUE_TAG = "PointValue";

    /** Tag indicating that this is the X component of a PointValue. */
    public static final String POINT_VALUE_X_TAG = "x";

    /** Tag indicating that this is the Y component of a PointValue. */
    public static final String POINT_VALUE_Y_TAG = "y";

    /** Tag indicating that this is a StringValue. */
    public static final String STRING_VALUE_TAG = "StringValue";

    /** Tag indicating that this is a TimePeriodValue. */
    public static final String TIME_PERIOD_TAG = "TimePeriodValue";

    /** Tag indicating that this is the year component of a TimePeriodValue. */
    public static final String TIME_PERIOD_YEAR_TAG = "year";

    /** Tag indicating that this is the month component of a TimePeriodValue. */
    public static final String TIME_PERIOD_MONTH_TAG = "month";

    /** Tag indicating that this is the day component of a TimePeriodValue. */
    public static final String TIME_PERIOD_DAY_TAG = "day";

    /** Tag indicating that this is the hour component of a TimePeriodValue. */
    public static final String TIME_PERIOD_HOUR_TAG = "hour";

    /** Tag indicating that this is the minute component of a TimePeriodValue. */
    public static final String TIME_PERIOD_MINUTE_TAG = "minute";

    /** Tag indicating that this is the second component of a TimePeriodValue. */
    public static final String TIME_PERIOD_SECOND_TAG = "second";

    /** Tag indicating that this is a TimeStampValue. */
    public static final String TIME_STAMP_TAG = "TimeStampValue";

    /** Tag indicating that this is the year component of a TimeStampValue. */
    public static final String TIME_STAMP_YEAR_TAG = "year";

    /** Tag indicating that this is the month component of a TimeStampValue. */
    public static final String TIME_STAMP_MONTH_TAG = "month";

    /** Tag indicating that this is the day component of a TimeStampValue. */
    public static final String TIME_STAMP_DAY_TAG = "day";

    /** Tag indicating that this is the hour component of a TimeStampValue. */
    public static final String TIME_STAMP_HOUR_TAG = "hour";

    /** Tag indicating that this is the minute component of a TimeStampValue. */
    public static final String TIME_STAMP_MINUTE_TAG = "minute";

    /** Tag indicating that this is the second component of a TimeStampValue. */
    public static final String TIME_STAMP_SECOND_TAG = "second";    

    
    /** Tag indicating an active Proxy. */
    public static final String PROXY_TOWARDS_HOME_TAG = "towardsHome";
    
    /** Tag indicating the NetworkIdentifier for a Proxy. */
    public static final String PROXY_NETWORK_IDENTIFIER_TAG = "ID";
    
    /** Tag indicating whether this replica gives up the lock when asked. */
    public static final String GIVE_UP_LOCK_TAG = "GiveUpLock";

    /** Tag saying "yes". */
    public static final String YES_TAG = "yes";

    /**
     * XML namespace for InfoGrid tags.
     */
    public static final String INFOGRID_NAMESPACE = "http://infogrid.org/xmlns/1";
}
