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

package org.infogrid.scene;

import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.set.ByTypeMeshObjectSelector;
import org.infogrid.mesh.set.MeshObjectSelector;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.model.primitives.EntityType;
import org.infogrid.util.ResourceHelper;

/**
 * <p>This represents a "class" of SceneAdditionCandidates, on the template rather than
 * the instance level.</p>
 *
 * <p>For example, for the SceneTemplate is "grand opera finale", there may be
 * a SceneTemplateAdditionCandidate "chorus member" (indicating that the chorus could
 * potentially be extended), but none for "bad guy" (because in the finale, chances are
 * that the bad guy has died earlier). Instead of "chorus member" there could be
 * four different ones: "soprano chorus member", "alto chorus member" and so forth.</p>
 *
 * <p>This class is distinct from SceneAdditionCandidate on the instance ("scene") level.
 * Each SceneAdditionCandidate needs to identity a SceneTemplateAdditionCandidate
 * that it "instantiates", but there is no obligation that a particular Scene provides
 * a SceneAdditionCandidate if its SceneTemplate says there is a SceneTemplateAdditionCandidate;
 * whether or not there is is scene ("instance") dependent.</p>
 *
 * <p>This class carries a property that represents how many instances of the getType()
 * MetaEntity may be playing a particular SceneRole in a particular Scene. The Scene
 * shall not offer a SceneAdditionCandidate for a SceneRole whose current players would
 * violate that maximum value.</p>
 */
public class SceneTemplateAdditionCandidate
{
    /**
     * Construct one with no restriction on the number of players of this type.
     *
     * @param destinationType the type of MeshObject
     */
    public SceneTemplateAdditionCandidate(
            EntityType destinationType )
    {
        this( destinationType, UNRESTRICTED_CARDINALITY, true );
    }

    /**
     * Construct one with no restriction on the number of players of this type.
     *
     * @param destinationType the type of MeshObject
     * @param userString a user-visible string
     */
    public SceneTemplateAdditionCandidate(
            EntityType destinationType,
            String     userString )
    {
        this( destinationType, UNRESTRICTED_CARDINALITY, true, userString );
    }

    /**
     * Construct one with a restriction on how many players of the type may participate
     * in the corresponding SceneRole. Also specify whether we are counting participation
     * by also counting subtypes of the destinationType or not.
     *
     * @param destinationType the type of MeshObject
     * @param maxCard the maximum number of players of this type may participate
     * @param countSubtypes if true, we count subtypes, and not just direct instances of destinationType
     */
    public SceneTemplateAdditionCandidate(
            EntityType destinationType,
            int        maxCard,
            boolean    countSubtypes )
    {
        theType               = destinationType;
        theMaximumCardinality = maxCard;
        theCountSubtypes      = countSubtypes;

        if( destinationType != null ) {
            theUserVisibleString = theResourceHelper.getResourceStringWithArguments(
                    "UserVisibleStringArg",
                    new Object[] { destinationType.getUserVisibleName().value() } );

        } else {
            theUserVisibleString = theResourceHelper.getResourceString( "UserVisibleStringNoArg" );
        }
    }

    /**
     * Construct one with a restriction on how many players of the type may participate
     * in the corresponding SceneRole. Also specify whether we are counting participation
     * by also counting subtypes of the destinationType or not.
     *
     * @param destinationType the type of MeshObject
     * @param maxCard the maximum number of players of this type may participate
     * @param countSubtypes if true, we count subtypes, and not just direct instances of destinationType
     * @param userString a user-visible string
     */
    public SceneTemplateAdditionCandidate(
            EntityType destinationType,
            int        maxCard,
            boolean    countSubtypes,
            String     userString )
    {
        theType               = destinationType;
        theMaximumCardinality = maxCard;
        theCountSubtypes      = countSubtypes;
        theUserVisibleString  = userString;
    }

    /**
     * <p>This factory method allows us to "instantiate" this SceneTemplateAdditionCandidate
     * into a SceneAdditionCandidate.</p>
     *
     * <p>This needs to be overridden by those subclasses of SceneTemplateAdditionCandidate
     * that want to provide their own implementation of SceneAdditionCandidate</p>
     *
     * @param theMatch the Scene for which we instantiate the SceneTemplateAdditionCandidate into a SceneAdditionCandidate
     * @param theRole the SceneTemplateRole for which we do this
     * @return the created SceneAdditionCandidate
     */
    public SceneAdditionCandidate createSceneAdditionCandidate(
            Scene             theMatch,
            SceneTemplateRole theRole )
    {
        return new SceneAdditionCandidate( theMatch, theRole, this );
    }

    /**
     * Obtain a string that can be shown to the user identifying this SceneTemplateAdditionCandidate.
     *
     * @return a user-visible string identifying this SceneTemplateAdditionCandidate
     */
    public final String getUserVisibleString()
    {
        return theUserVisibleString;
    }

    /**
     * Obtain the type of MeshObject that would be instantiated.
     *
     * @return the type of MeshObject that would be instantiated
     */
    public final EntityType getType()
    {
        return theType;
    }

    /**
     * Obtain the maximum number of instances of getType() that may participate in
     * a particular SceneRole in a particular Scene. May return UNRESTRICTED_CARDINALITY.
     *
     * @return the maximum number of instances of getType() that may participate
     */
    public final int getMaximumCardinality()
    {
        return theMaximumCardinality;
    }

    /**
     * Determine whether we count subtypes when we determine cardinality.
     *
     * @return true if we also count subtypes, and not just direct instances
     */
    public final boolean isCountingSubtypesForCardinality()
    {
        return theCountSubtypes;
    }

    /**
     * Obtain a MeshObjectSelector that reflects type and subtyping-count rule.
     *
     * @return the MeshObjectSelector
     */
    public final MeshObjectSelector getMeshObjectSelector()
    {
        return ByTypeMeshObjectSelector.create( theType, theCountSubtypes );
    }

    /**
     * Our "instance" (the SceneAdditionCandidate) tells us that we should instantiate.
     * This will be invoked unless the respective method has been overridden in the SceneAdditionCandidate.
     * This default implementation will throw an exceptions, so if SceneAdditionCandidate has not been
     * overridden, this should be overridden.
     *
     * @param candidate the SceneAdditionCandidate that wants to be instantiated
     * @param life the ModelObjectLifecycleManager to use for instantiation
     * @return the instantiated RootEntity
     * @throws UnknownHowException thrown if this method has not (sufficiently) been overridden
     * @throws IllegalArgumentException thrown if candidate is not applicable to this method
     * @throws TransactionException thrown if this method was invoked outside of proper Transaction boundaries
     * @throws DoNotHaveLockException if an operation was performed that did not have appropriate update rights
     */
    protected MeshObject instantiate(
            SceneAdditionCandidate   candidate,
            MeshBaseLifecycleManager life )
        throws
            UnknownHowException,
            IllegalArgumentException,
            TransactionException,
            NotPermittedException
    {
        throw new UnknownHowException();
    }

    /**
     * The type of MeshObject that would be instantiated if a SceneAdditionCandidate
     * with this SceneTemplateAdditionCandidate was instantiated.
     */
    protected EntityType theType;

    /**
     * The maximum number of instances of theType participating in a certain SceneRole
     * in a certain Scene. May be UNRESTRICTED_CARDINALITY.
     */
    protected int theMaximumCardinality;

    /**
     * Do we count subtypes when counting cardinality.
     */
    protected boolean theCountSubtypes;

    /**
     * The string we show to the user.
     */
    protected String theUserVisibleString;

    /**
     * Our ResourceHelper.
     */
    private static final ResourceHelper theResourceHelper = ResourceHelper.getInstance( SceneTemplateAdditionCandidate.class );

    /**
     * Indicates "no upper limit" for the cardinality.
     */
    public static final int UNRESTRICTED_CARDINALITY = -1;
}
