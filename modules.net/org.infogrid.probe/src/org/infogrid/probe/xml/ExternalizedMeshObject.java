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

import org.infogrid.meshbase.net.NetMeshBaseIdentifier;
import org.infogrid.model.primitives.MeshTypeIdentifier;
import org.infogrid.model.primitives.PropertyValue;

import java.util.ArrayList;

/**
 * This is a temporary buffer for a to-be-deserialized MeshObject.
 */
public class ExternalizedMeshObject
{
    /**
     * Set the Identifier.
     *
     * @param newValue the new value
     */
    public void setIdentifier(
            String newValue )
    {
        theIdentifier = newValue;
    }

    /**
     * Get the Identifier.
     *
     * @return the Identifier
     */
    public String getIdentifier()
    {
        return theIdentifier;
    }
    
    /**
     * Set the TimeCreated.
     *
     * @param newValue the new value
     */
    public void setTimeCreated(
            long newValue )
    {
        theTimeCreated = newValue;
    }
    
    /**
     * Get the TimeCreated.
     *
     * @return the TimeCreated
     */
    public long getTimeCreated()
    {
        return theTimeCreated;
    }
    
    /**
     * Set the TimeUpdated.
     *
     * @param newValue the new value
     */
    public void setTimeUpdated(
            long newValue )
    {
        theTimeUpdated = newValue;
    }
    
    /**
     * Get the TimeUpdated.
     *
     * @return the TimeUpdated
     */
    public long getTimeUpdated()
    {
        return theTimeUpdated;
    }
    
    /**
     * Set the TimeRead.
     *
     * @param newValue the new value
     */
    public void setTimeRead(
            long newValue )
    {
        theTimeRead = newValue;
    }
    
    /**
     * Get the TimeRead.
     *
     * @return the TimeRead
     */
    public long getTimeRead()
    {
        return theTimeRead;
    }
    
    /**
     * Set the TimeAutoDeletes
     *
     * @param newValue the new value
     */
    public void setTimeAutoDeletes(
            long newValue )
    {
        theTimeAutoDeletes = newValue;
    }
    
    /**
     * Get the TimeRead.
     *
     * @return the TimeRead
     */
    public long getTimeAutoDeletes()
    {
        return theTimeAutoDeletes;
    }
    
   /**
     * Add a MeshType, using its Identifier.
     * 
     * @param identifier the MeshType's Identifier
     */
    public void addMeshType(
            MeshTypeIdentifier identifier )
    {
        theMeshTypes.add( identifier );
    }

    /**
     * Get the MeshTypes' Identifiers.
     *
     * @return the MeshTypes' Identifiers.
     */
    public MeshTypeIdentifier[] getMeshTypes()
    {
        MeshTypeIdentifier [] ret = theMeshTypes.toArray( new MeshTypeIdentifier[ theMeshTypes.size() ]);
        return ret;
    }

    /**
     * Add a PropertyType, using its Identifier.
     * 
     * @param identifier the PropertyType's Identifier
     */
    public void addPropertyType(
            MeshTypeIdentifier identifier )
    {
        thePropertyTypes.add( identifier );
    }

    /**
     * Get the PropertyTypes' Identifiers.
     *
     * @return the PropertyTypes' Identifiers.
     */
    public MeshTypeIdentifier[] getPropertyTypes()
    {
        MeshTypeIdentifier [] ret = thePropertyTypes.toArray( new MeshTypeIdentifier[ thePropertyTypes.size() ]);
        return ret;
    }

    /**
     * Add a PropertyValue.
     *
     * @param newValue the PropertyValue.
     */
    public void addPropertyValue(
            PropertyValue newValue )
    {
        thePropertyValues.add( newValue );
    }

    /**
     * Get the PropertyValues.
     *
     * @return the PropertyValues
     */
    public PropertyValue [] getPropertyValues()
    {
        PropertyValue [] ret = thePropertyValues.toArray( new PropertyValue[ thePropertyValues.size() ]);
        return ret;
    }

    /**
     * Add a Relationship.
     * 
     * @param otherIdentifier the Identifier of the related MeshObject
     */
    public void addRelationship(
            String otherIdentifier )
    {
        theRelationships.add( new ExternalizedRelationship( otherIdentifier ));
    }

    /**
     * Get the Relationships.
     *
     * @return the Relationships
     */
    public ExternalizedRelationship [] getRelationships()
    {
        ExternalizedRelationship [] ret = theRelationships.toArray( new ExternalizedRelationship[ theRelationships.size() ]);
        return ret;
    }

    /**
     * Obtain the currently parsed Relationship.
     *
     * @return the currently parsed Relationship.
     */
    public ExternalizedRelationship getCurrentRelationship()
    {
        return theRelationships.get( theRelationships.size()-1 );
    }

    /**
     * Set the PropertyValue that is currently being parsed.
     *
     * @param newPropertyValue the PropertyValue
     */
    public void setCurrentPropertyValue(
            PropertyValue newPropertyValue )
    {
        theCurrentPropertyValue = newPropertyValue;
    }

    /**
     * Obtain the PropertyValue that is currently being parsed.
     *
     * @return the PropertyValue
     */
    public PropertyValue getCurrentPropertyValue()
    {
        return theCurrentPropertyValue;
    }
    
    /**
     * Set the NetMeshBaseIdentifier towards the home replica.
     * 
     * @param newValue the NetMeshBaseIdentifier
     */
    public void setProxyTowardsHome(
            NetMeshBaseIdentifier newValue )
    {
        theProxyTowardsHome = newValue;
    }

    /**
     * Obtain NetMeshBaseIdentifier towards the home replica.
     * 
     * @return the NetworkIdentifiers toward the home replica
     */
    public NetMeshBaseIdentifier getProxyTowardsHome()
    {
        return theProxyTowardsHome;
    }

    /**
     * Determine whether we are giving up the lock.
     *
     * @return true if we give up the lock
     */
    public boolean getGiveUpLock()
    {
        return theGiveUpLock;
    }

    /**
     * Set whether we are giving up the lock.
     *
     * @param newValue the new value
     */
    public void setGiveUpLock(
            boolean newValue )
    {
        theGiveUpLock = newValue;
    }

    /**
     * Determine whether we are giving up the home replica status.
     *
     * @return true if we give up the home replica status
     */
    public boolean getGiveUpHome()
    {
        return theGiveUpHome;
    }

    /**
     * Set whether we are giving up the home replica status.
     *
     * @param newValue the new home replica status
     */
    public void setGiveUpHome(
            boolean newValue )
    {
        theGiveUpHome = newValue;
    }

    /**
     * The Identifier.
     */
    protected String theIdentifier;
    
    /**
     * The time created.
     */
    protected long theTimeCreated = -1L;
    
    /**
     * The time udpated.
     */
    protected long theTimeUpdated = -1L;
    
    /**
     * The time read.
     */
    protected long theTimeRead = -1L;
    
    /**
     * The time auto-deletes.
     */
    protected long theTimeAutoDeletes = -1L;

    /**
     * The Identifiers of the MeshTypes.
     */
    protected ArrayList<MeshTypeIdentifier> theMeshTypes = new ArrayList<MeshTypeIdentifier>();
    
    /**
     * The Identifiers of the PropertyTypes.
     */
    protected ArrayList<MeshTypeIdentifier> thePropertyTypes = new ArrayList<MeshTypeIdentifier>();
    
    /**
     * The PropertyValues that go with the PropertyTypes.
     */
    protected ArrayList<PropertyValue> thePropertyValues = new ArrayList<PropertyValue>();

    /**
     * The Relationships in which this ExternalizedMeshObject participates.
     */
    protected ArrayList<ExternalizedRelationship> theRelationships = new ArrayList<ExternalizedRelationship>();
    
    /**
     * The NetworkIdentifiers of the Proxies.
     */
    protected NetMeshBaseIdentifier theProxyTowardsHome = null;
 
    /**
     * The PropertyValue that is currently being parsed.
     */
    protected PropertyValue theCurrentPropertyValue;

    /**
     * The GiveUpLock property.
     */
    protected boolean theGiveUpLock;

    /**
     * The GiveUpLock property.
     */
    protected boolean theGiveUpHome;

    /**
     * Buffered Relationship.
     */
    public static class ExternalizedRelationship
    {
        /**
         * Constructor.
         * 
         * @param identifier the Identifier of the other side of the relationship
         */
        public ExternalizedRelationship(
                String identifier )
        {
            theIdentifier = identifier;
        }

        /**
         * Obtain the Identifier of the MeshObject at the other end.
         *
         * @return the Identifier of the MeshObject at the other end
         */
        public String getIdentifier()
        {
            return theIdentifier;
        }

        /**
         * Get the Identifiers of the RoleTypes played by this relationship.
         *
         * @return the Identifier of the RoleTypes
         */
        public MeshTypeIdentifier[] getRoleTypes()
        {
            MeshTypeIdentifier [] ret = theRoleTypes.toArray( new MeshTypeIdentifier[ theRoleTypes.size() ]);
            return ret;
        }

        /**
         * Add the Identifier of a RoleType.
         * 
         * @param identifier the Identifier of the RoleType
         */
        public void addRoleType(
                MeshTypeIdentifier identifier )
        {
            theRoleTypes.add( identifier );
        }
        
        /**
         * The Identifier of the MeshObject on the other side.
         */
        protected String theIdentifier;

        /**
         * The Identifiers of the RoleTypes.
         */
        protected ArrayList<MeshTypeIdentifier> theRoleTypes = new ArrayList<MeshTypeIdentifier>();
    }
}
