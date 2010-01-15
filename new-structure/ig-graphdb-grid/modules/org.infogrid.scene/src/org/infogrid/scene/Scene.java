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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.infogrid.mesh.MeshObject;
import org.infogrid.mesh.NotPermittedException;
import org.infogrid.mesh.set.MeshObjectSelector;
import org.infogrid.meshbase.MeshBaseLifecycleManager;
import org.infogrid.meshbase.transaction.MeshObjectStateEvent;
import org.infogrid.meshbase.transaction.TransactionException;
import org.infogrid.util.ArrayHelper;
import org.infogrid.util.FlexiblePropertyChangeListenerSet;
import org.infogrid.util.logging.Log;

/**
 * <p>A Scene is a collection of related MeshObjects that play certain SceneRoles.
 * A SceneTemplate defines the structure and semantics of a Scene, while the Scene
 * matches the SceneTemplate with actual MeshObjects.</p>
 *
 * <p>Some (simple) SceneTemplates may decide to instantiate this directly instead of
 * subclasses, which is more common.</p>
 */
public class Scene
    implements
        PropertyChangeListener // so we can listen to IsMatched property changes on the SceneRoles
{
    private static final Log log = Log.getLogInstance( Scene.class ); // our own, private logger

    /**
     * Construct a Scene from a collection of SceneRoles. This should only be invoked from within
     * the factory method provided by the appropriate subclass of SceneTemplate.
     *
     * @param directory the SceneDirectory that we reference from this Scene
     * @param template our "type"
     * @param rootRoleMatch the match for the root SceneTemplateRole
     * @param entityRoleMatches the matches for the entity SceneTemplateRoles
     * @param sceneRoleMatches the matches for the scene SceneTemplateRoles
     * @param dependentSceneFactories pass in all DependentSceneFactories that we depend on in order to prevent their garbage collection while we are still around
     */
    public Scene(
            SceneDirectory                  directory,
            SceneTemplate                   template,
            SceneRole.Root                  rootRoleMatch,
            SceneRole.PlayedByMeshObject [] entityRoleMatches,
            SceneRole.PlayedByScene []      sceneRoleMatches,
            DependentSceneFactory []        dependentSceneFactories )
    {
        if( directory == null ) {
            throw new IllegalArgumentException();
        }
        if( template == null ) {
            throw new IllegalArgumentException();
        }
        if( rootRoleMatch == null ) {
            throw new IllegalArgumentException();
        }
        if( !rootRoleMatch.isMatched() ) {
            throw new IllegalArgumentException();
        }

        theDirectory     = directory;
        theTemplate      = template;
        theRootRoleMatch = rootRoleMatch;

        if( entityRoleMatches != null ) { // make life easier by not carrying null pointers around
            theEntityRoleMatches = entityRoleMatches;
        } else {
            theEntityRoleMatches = new SceneRole.PlayedByMeshObject[0];
        }

        if( sceneRoleMatches != null ) { // make life easier by not carrying null pointers around
            theSceneRoleMatches = sceneRoleMatches;
        } else {
            theSceneRoleMatches = new SceneRole.PlayedByScene[0];
        }

        theDependentSceneFactories = dependentSceneFactories;

        theRootRoleMatch.getRootPlayer().addWeakPropertyChangeListener( this ); // we want to know when the root dies

        theMatchedCount = 1; // root role

        for( int i=0 ; i<theEntityRoleMatches.length ; ++i ) {
            theEntityRoleMatches[i].addPropertyChangeListener( this ); // we want to know when IsMatched changes
            if( theEntityRoleMatches[i].isMatched() ) {
                ++theMatchedCount;
            }
        }
        for( int i=0 ; i<theSceneRoleMatches.length ; ++i ) {
            theSceneRoleMatches[i].addPropertyChangeListener( this ); // we want to know when IsMatched changes
            if( theSceneRoleMatches[i].isMatched() ) {
                ++theMatchedCount;
            }
        }
    }

    /**
     * Obtain the SceneDirectory that we use.
     *
     * @return the SceneDirectory that we use
     */
    public SceneDirectory getSceneDirectory()
    {
        return theDirectory;
    }

    /**
     * Obtain the SceneTemplate of which we are an instance.
     *
     * @return the SceneTemplate of which we are an instance
     */
    public SceneTemplate getSceneTemplate()
    {
        return theTemplate;
    }

    /**
     * Obtain the matched SceneRole for the root SceneTemplateRole in this Scene.
     *
     * @return the matched root SceneRole
     */
    public final SceneRole.Root getRootMatch()
    {
        return theRootRoleMatch;
    }

    /**
     * Obtain the matched SceneRole for this entity SceneTemplateRole in this Scene.
     *
     * @param role the SceneTemplateRole that we are looking for
     * @return the matched entity SceneRole
     */
    public final SceneRole.PlayedByMeshObject getMatchFor(
            SceneTemplateRole.PlayedByMeshObject role )
    {
        for( int i=0 ; i<theEntityRoleMatches.length ; ++i ) {
            if( role == theEntityRoleMatches[i].getTemplateRole()) {
                return theEntityRoleMatches[i];
            }
        }
        return null;
    }

    /**
     * Obtain the matched SceneRole for this scne SceneTemplateRole in this Scene.
     *
     * @param role the SceneTemplateRole that we are looking for
     * @return the matched scene SceneRole
     */
    public final SceneRole.PlayedByScene getMatchFor(
            SceneTemplateRole.PlayedByScene role )
    {
        for( int i=0 ; i<theSceneRoleMatches.length ; ++i ) {
            if( role == theSceneRoleMatches[i].getTemplateRole()) {
                return theSceneRoleMatches[i];
            }
        }
        return null;
    }

    /**
     * Determine potential additions to this Scene by SceneTemplateRole.
     * We determine them straight from our SceneTemplateAdditionCandidates.
     *
     * @param role the SceneTemplateRole for which we are looking for addition candidates
     * @return the set of SceneAdditionCandidates for this SceneTemplateRole, may be empty
     */
    public final SceneAdditionCandidate [] getAdditionCandidates(
            SceneTemplateRole.PlayedByMeshObject role )
    {
        SceneTemplateAdditionCandidate [] templateAdditionCandidates = theTemplate.getPotentialAdditionCandidates( role );

        int count = 0;
        SceneAdditionCandidate [] ret = new SceneAdditionCandidate[ templateAdditionCandidates.length ];

        for( int i=0 ; i<templateAdditionCandidates.length ; ++i ) {
            SceneAdditionCandidate newCandidate = null;
            if( templateAdditionCandidates[i].getMaximumCardinality() == SceneTemplateAdditionCandidate.UNRESTRICTED_CARDINALITY ) {
                newCandidate = templateAdditionCandidates[i].createSceneAdditionCandidate( this, role );

            } else {
                MeshObject []      currentPlayers = getMatchFor( role ).getPlayers().getMeshObjects();
                MeshObjectSelector selector       = templateAdditionCandidates[i].getMeshObjectSelector();

                int card = 0;
                for( int j=0 ; j<currentPlayers.length ; ++j ) {
                    if( selector.accepts( currentPlayers[j] )) {
                        ++card;
                    }
                }
                if( card < templateAdditionCandidates[i].getMaximumCardinality() ) {
                    newCandidate = templateAdditionCandidates[i].createSceneAdditionCandidate( this, role );
                }
            }
            if( newCandidate != null ) {
                ret[count++] = newCandidate;
            }
        }

        if( count < ret.length ) {
            ret = ArrayHelper.copyIntoNewArray( ret, 0, count, SceneAdditionCandidate.class );
        }

        return ret;
    }

    /**
     * <p>Instantiate a SceneAdditionCandidate.</p>
     *
     * <p>This returns the primary RootEntity of the addition. This is generally
     * the RootEntity playing the root SceneTemplateRole in a new Scene, or the RootEntity
     * playing a new SceneTemplateRole in this Scene.</p>
     *
     * <p>Per default, this throws IllegalArgumentException as we return an empty
     * set of SceneAdditionCandidates when asked which we have.</p>
     *
     * @param candidate the SceneAdditionCandidate that we'd like to see instantiated
     * @param life the MeshObjectLifecycleManager that we use to instantiate MeshObjects
     * @return one MeshObject that was instantiated as part of the SceneAdditionCandidate instantiation; the
     *        Scene developer chooses which one to return
     * @throws UnknownHowException thrown if the SceneAdditionCandidate is not sufficiently parameterized and the Scene does not know how to instantiate it
     * @throws IllegalArgumentException thrown if illegal / inconsistent parameters were given
     * @throws TransactionException thrown if this method is invoked outside of a proper Transaction
     * @throws DoNotHaveLockException thrown if a replica that needed to be updated did not have update rights, and could not obtain them
     */
    public final MeshObject instantiateAdditionCandidate(
            SceneAdditionCandidate   candidate,
            MeshBaseLifecycleManager life )
        throws
            UnknownHowException,
            IllegalArgumentException,
            TransactionException,
            NotPermittedException
    {
        if( candidate == null ) {
            throw new IllegalArgumentException();
        }
        if( candidate.getScene() != this ) {
            throw new IllegalArgumentException();
        }
        if( !candidate.isSufficientlyParameterized() ) {
            throw new UnknownHowException();
        }

        return candidate.instantiate( life );
    }

    /**
     * Determine whether these matched SceneRoles are a subset of, or equal to
     * the current Scene's SceneRoles.
     *
     * @param testRootRole the test root SceneRole that we test against
     * @param testEntityRoles the test entity SceneRoles that we test against
     * @param testSceneRoles the test scene SceneRoles that we test against
     * @return true if the provided SceneRoles are a subset of, or equal to the current Scene's SceneRoles
     */
     public boolean hasRolesMatching(
             SceneRole.Root                  testRootRole,
             SceneRole.PlayedByMeshObject [] testEntityRoles,
             SceneRole.PlayedByScene []      testSceneRoles )
    {
        if( !testRootRole.equals( theRootRoleMatch )) {
            return false;
        }
        if( testEntityRoles != null && testEntityRoles.length > theEntityRoleMatches.length ) {
            return false;
        }
        if( testSceneRoles != null && testSceneRoles.length > theSceneRoleMatches.length ) {
            return false;
        }
        
        if( testEntityRoles != null ) {
            for( int i=0 ; i<testEntityRoles.length ; ++i ) {
                boolean found = false;
                for( int j=0 ; j<theEntityRoleMatches.length ; ++j ) {
                    if( testEntityRoles[i].equals( theEntityRoleMatches[j] )) {
                        found = true;
                        break;
                    }
                }
                if( !found ) {
                    return false;
                }
            }
        }

        if( testSceneRoles != null ) {
            for( int i=0 ; i<testSceneRoles.length ; ++i ) {
                boolean found = false;
                for( int j=0 ; j<theSceneRoleMatches.length ; ++j ) {
                    if( testSceneRoles[i].equals( theSceneRoleMatches[j] )) {
                        found = true;
                        break;
                    }
                }
                if( !found ) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Determine whether this Scene is matched. This is a bound property, so one can
     * listen to PropertyChangeEvents.
     *
     * @return true if this Scene is currently matched
     */
    public boolean isMatched()
    {
        return theMatchedCount == 1 + theEntityRoleMatches.length + theSceneRoleMatches.length;
    }

    /**
     * If this Scene is not matched, do whatever necessary to match it. If the Scene is
     * matched at the time of invocation, do nothing.
     *
     * @param theLifecycleManager the MeshObjectLifecycleManager used to instantiate the MeshObjects required for matching the Scene
     * @throws UnknownHowException thrown if the Scene does not know how to complete itself
     */
    public synchronized void ensureMatched(
            MeshBaseLifecycleManager theLifecycleManager )
        throws
            UnknownHowException
    {
        if( isMatched() ) {
            return;
        }

        throw new UnknownHowException();
    }

    /**
     * <p>Delete the MeshObjects that make this a Scene. This does not delete this instance of
     * class Scene (we could always re-create that through a SceneDirectory), but it
     * deletes one or more of the underlying MeshObjects in a way so that the Scene does not exist
     * any more in this place in the MeshObjects graph. </p>
     *
     * <p>Scenes do not need to implement this, the default implementation throws a UnknownHowException.</p>
     *
     * @param theLifecycleManager the MeshBaseLifecycleManager used to instantiate the MeshObjects required for matching the Scene
     * @throws UnknownHowException thrown if the Scene does not know how to do this
     * @throws TransactionException thrown if this call is made outside of the boundaries of a proper Transaction
     * @throws NotPermittedException thrown if a caller had insufficient privileges to perform this operation
     */
    public void deleteScene(
            MeshBaseLifecycleManager theLifecycleManager )
        throws
            UnknownHowException,
            TransactionException,
            NotPermittedException
    {
        throw new UnknownHowException();
    }

    /**
     * Determine whether this Scene is alive or has died. A Scene is automatically
     * dead if the MeshObject playing its root SceneTemplateRole is dead.
     *
     * @return true if this Scene is dead
     */
    public boolean isDead()
    {
        return isDead;
    }

    /**
     * Callback from one of our SceneRoles indicating a change of one of its properties.
     * We are looking for the IS_MATCHED_PROPERTY.
     *
     * @param theEvent the event
     */
    public void propertyChange(
            PropertyChangeEvent theEvent )
    {
        if( SceneRole.IS_MATCHED_PROPERTY.equals( theEvent.getPropertyName() )) {
            int nRoles = 1 + theEntityRoleMatches.length + theSceneRoleMatches.length;

            Boolean newValue = (Boolean) theEvent.getNewValue();
            if( newValue.booleanValue() ) {
                ++theMatchedCount;
                if( theMatchedCount == nRoles ) {
                    thePropertyChangeListeners.fireEvent( new PropertyChangeEvent( this, IS_MATCHED_PROPERTY, Boolean.FALSE, Boolean.TRUE ));
                }

            } else {
                --theMatchedCount;
                if( theMatchedCount == nRoles-1 ) {
                    thePropertyChangeListeners.fireEvent( new PropertyChangeEvent( this, IS_MATCHED_PROPERTY, Boolean.TRUE, Boolean.FALSE ));
                }
            }

        } else if( theEvent instanceof MeshObjectStateEvent ) {
            Object source = theEvent.getSource();

            log.assertLog( theRootRoleMatch.getRootPlayer() == source, "event from unexpected source" );

            if( ((MeshObject)source).getIsDead() ) {
                // now we die
                isDead = true;
                thePropertyChangeListeners.fireEvent( new PropertyChangeEvent( this, IS_DEAD_PROPERTY, Boolean.FALSE, Boolean.TRUE ));
            }
        }
    }

    /**
     * Add a PropertyChangeListener.
     *
     * @param newListener the new PropertyChangeListener
     * @see #addSoftPropertyChangeListener
     * @see #addWeakPropertyChangeListener
     * @see #removePropertyChangeListener
     */
    public void addDirectPropertyChangeListener(
            PropertyChangeListener newListener )
    {
        thePropertyChangeListeners.addDirect( newListener );
    }

    /**
     * Add a PropertyChangeListener.
     *
     * @param newListener the new PropertyChangeListener
     * @see #addDirectPropertyChangeListener
     * @see #addWeakPropertyChangeListener
     * @see #removePropertyChangeListener
     */
    public void addSoftPropertyChangeListener(
            PropertyChangeListener newListener )
    {
        thePropertyChangeListeners.addSoft( newListener );
    }

    /**
     * Add a PropertyChangeListener.
     *
     * @param newListener the new PropertyChangeListener
     * @see #addDirectPropertyChangeListener
     * @see #addSoftPropertyChangeListener
     * @see #removePropertyChangeListener
     */
    public void addWeakPropertyChangeListener(
            PropertyChangeListener newListener )
    {
        thePropertyChangeListeners.addWeak( newListener );
    }

    /**
     * Remove a PropertyChangeListener.
     *
     * @param oldListener the to-be-removed PropertyChangeListener
     * @see #addDirectPropertyChangeListener
     * @see #addSoftPropertyChangeListener
     * @see #addWeakPropertyChangeListener
     */
    public synchronized void removePropertyChangeListener(
            PropertyChangeListener oldListener )
    {
        thePropertyChangeListeners.remove( oldListener );
    }

    /**
     * Convert to string form, for debugging.
     *
     * @return this instance is string form
     */
    @Override
    public String toString()
    {
        StringBuffer buf = new StringBuffer( 100 );
        buf.append( "<" );
        buf.append( super.toString() );
        buf.append( "{ type: " );
        buf.append( theTemplate.getClass().getName() );
        buf.append( ", root: " );
        buf.append( getRootMatch().getRootPlayer() );
        buf.append( " }>" );
        return buf.toString();
    }

    /**
     * The SceneDirectory in which this Scene lives.
     */
    protected SceneDirectory theDirectory;

    /**
     * The SceneTemplate of which this Scene is an instance.
     */
    protected SceneTemplate theTemplate;

    /**
     * The match for the root SceneTemplateRole.
     */
    protected SceneRole.Root theRootRoleMatch;

    /**
     * The matches for the non-MeshObject SceneTemplateRoles that are played by MeshObject.
     */
    protected SceneRole.PlayedByMeshObject [] theEntityRoleMatches;

    /**
     * The matches for the non-root scene SceneTemplateRoles that are played by Scenes.
     */
    protected SceneRole.PlayedByScene [] theSceneRoleMatches;

    /**
     * The number of SceneTemplateRoles that are currently matched. This helps
     * us with determining whether we have to update our isMatched Property.
     */
    private int theMatchedCount;

    /**
     * References to the DepedentSceneFactories that we use. This pointer
     * is used to prevent garbage collection of those DepedentSceneFactories while
     * we are still around.
     */
    private DependentSceneFactory [] theDependentSceneFactories;

    /**
     * Our PropertyChangeListeners.
     */
    private FlexiblePropertyChangeListenerSet thePropertyChangeListeners = new FlexiblePropertyChangeListenerSet();

    /**
     * True if we are dead.
     */
    private boolean isDead;

    /**
     * Name of the IsMatched property, indicating whether or not this Scene is currently matched correctly
     * according to its multiplicity requirements.
     */
    public static final String IS_MATCHED_PROPERTY = "IsMatched";

    /**
     * Name of the IsDead property indicating whether or not this Scene has died.
     */
    public static final String IS_DEAD_PROPERTY = "IsDead";
}
