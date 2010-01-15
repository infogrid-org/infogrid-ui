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
// Copyright 1998-2010 by R-Objects Inc. dba NetMesh Inc., Johannes Ernst
// All rights reserved.
//

package org.infogrid.scene;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.EventObject;
import org.infogrid.util.FlexibleListenerSet;
import org.infogrid.util.FlexiblePropertyChangeListenerSet;
import org.infogrid.util.logging.Log;

/**
 * Factors out commonly used functionality for ActiveSceneSet implementations.
 */
public abstract class AbstractActiveSceneSet
    implements
        ActiveSceneSet,
        PropertyChangeListener
{
    private static final Log log = Log.getLogInstance( AbstractActiveSceneSet.class ); // our own, private logger

    /**
     * Private constructor, for subclasses only
     *
     * @param initialContent the initial content of this AbstractSceneSet
     */
    protected AbstractActiveSceneSet(
            Scene [] initialContent )
    {
        currentContent = initialContent;

        // FIXME: this is inefficient -- should only subscribe if we have listeners on our own
        for( int i=0 ; i<currentContent.length ; ++i ) {
            currentContent[i].addWeakPropertyChangeListener( this );
        }
    }

    /**
     * Obtain the current content of the set.
     *
     * @return the current content of this set
     */
    public final Scene [] getScenes()
    {
        return currentContent;
    }

    /**
     * Obtain the number of members currently in the set.
     *
     * @return the number of members currently in the set
     */
    public final int size()
    {
        return currentContent.length;
    }

    /**
     * Determine whether this set is empty.
     *
     * @return true if this set is empty
     */
    public final boolean isEmpty()
    {
        return size() == 0;
    }

    /**
     * Add a listener.
     *
     * @param newListener the listener to be added
     * @see #addSoftActiveSceneSetListener
     * @see #addWeakActiveSceneSetListener
     * @see #removeActiveSceneSetListener
     */
    public void addDirectActiveSceneSetListener(
            ActiveSceneSetListener newListener )
    {
        theSetListeners.addDirect( newListener );
    }

    /**
     * Add a listener.
     *
     * @param newListener the listener to be added
     * @see #addDirectActiveSceneSetListener
     * @see #addWeakActiveSceneSetListener
     * @see #removeActiveSceneSetListener
     */
    public void addSoftActiveSceneSetListener(
            ActiveSceneSetListener newListener )
    {
        theSetListeners.addSoft( newListener );
    }

    /**
     * Add a listener.
     *
     * @param newListener the listener to be added
     * @see #addDirectActiveSceneSetListener
     * @see #addSoftActiveSceneSetListener
     * @see #removeActiveSceneSetListener
     */
    public void addWeakActiveSceneSetListener(
            ActiveSceneSetListener newListener )
    {
        theSetListeners.addWeak( newListener );
    }

    /**
     * Remove a listener.
     *
     * @param oldListener the listener to be removed
     * @see #addDirectActiveSceneSetListener
     * @see #addSoftActiveSceneSetListener
     * @see #addWeakActiveSceneSetListener
     */
    public void removeActiveSceneSetListener(
            ActiveSceneSetListener oldListener )
    {
        theSetListeners.remove( oldListener );
    }

    /**
     * Add a listener listening to PropertyChangeEvents on all contained Scenes.
     *
     * @param newListener the listener to be added
     * @see #addSoftContentPropertyChangeListener
     * @see #addWeakContentPropertyChangeListener
     * @see #removeContentPropertyChangeListener
     */
    public void addDirectContentPropertyChangeListener(
            PropertyChangeListener newListener )
    {
        thePropertyListeners.addDirect( newListener );
    }

    /**
     * Add a listener listening to PropertyChangeEvents on all contained Scenes.
     *
     * @param newListener the listener to be added
     * @see #addDirectContentPropertyChangeListener
     * @see #addWeakContentPropertyChangeListener
     * @see #removeContentPropertyChangeListener
     */
    public void addSoftContentPropertyChangeListener(
            PropertyChangeListener newListener )
    {
        thePropertyListeners.addSoft( newListener );
    }

    /**
     * Add a listener listening to PropertyChangeEvents on all contained Scenes.
     *
     * @param newListener the listener to be added
     * @see #addDirectContentPropertyChangeListener
     * @see #addSoftContentPropertyChangeListener
     * @see #removeContentPropertyChangeListener
     */
    public void addWeakContentPropertyChangeListener(
            PropertyChangeListener newListener )
    {
        thePropertyListeners.addWeak( newListener );
    }

    /**
     * Remove a listener listening to PropertyChangeEvents on all contained Scenes.
     *
     * @param oldListener the listener to be removed
     * @see #addDirectContentPropertyChangeListener
     * @see #addSoftContentPropertyChangeListener
     * @see #addWeakContentPropertyChangeListener
     */
    public void removeContentPropertyChangeListener(
            PropertyChangeListener oldListener )
    {
        thePropertyListeners.remove( oldListener );
    }

    /**
     * This is invoked by subclasses to potentially add a Scene to this set.
     * This method will check whether the Scene is indeed new, and if so,
     * add it to the set (otherwise, it will not add it). This method will also
     * fire an event in case that is necessary.
     *
     * @param newScene the Scene potentially to be added
     * @return true if it was indeed added
     * @see #certainlyAdd
     */
    protected final synchronized boolean potentiallyAdd(
            Scene newScene )
    {
        // linear search (FIXME? Not sure we can do ordering necessary for binary search)
        for( int i=0 ; i<currentContent.length ; ++i ) {
            if( newScene == currentContent[i] ) {
                ++currentContentCounters[i];
                return false;
            }
        }

        certainlyAdd( newScene );
        return true;
    }

    /**
     * This is invoked by subclasses to indicate that we have one more Scene, and
     * it needs to be added for sure.
     *
     * @param newScene the Scene potentially to be added
     * @see #potentiallyAdd
     */
    protected synchronized void certainlyAdd(
            Scene newScene )
    {
        certainlyAdd( 1, newScene, size() );
    }

    /**
     * This is invoked by subclasses to indicate that we have one more Scene, and would like to contain
     * it nTimes.
     *
     * @param nTimes the containment count for the Scene
     * @param newScene the Scene potentially to be added
     */
    protected synchronized void certainlyAdd(
            int   nTimes,
            Scene newScene )
    {
        certainlyAdd( nTimes, newScene, size() );
    }

    /**
     * This is invoked by subclasses to indicate that we have one more Scene, would like to contain
     * it nTimes, and would like it to be added at a particular location.
     *
     * @param nTimes the containment count for the Scene
     * @param newScene the Scene potentially to be added
     * @param where the location of the to-be-added Scene in the content array
     */
    protected synchronized void certainlyAdd(
            int   nTimes,
            Scene newScene,
            int   where )
    {
        Scene [] newContent  = new Scene[ currentContent.length+1 ];
        int []   newCounters = new int[ newContent.length ];

        for( int i=0 ; i<where ; ++i ) {
            newContent[i]  = currentContent[i];
            newCounters[i] = currentContentCounters[i];
        }
        newContent[  where ] = newScene;
        newCounters[ where ] = nTimes-1; // we are off by one

        for( int i=where ; i<currentContent.length ; ++i ) {
            newContent[i+1]  = currentContent[i];
            newCounters[i+1] = currentContentCounters[i];
        }

        newScene.addWeakPropertyChangeListener( this );

        currentContent         = newContent;
        currentContentCounters = newCounters;

        theSetListeners.fireEvent( new SceneAddedEvent( this, newScene ));
    }

    /**
     * This is invoked by subclasses to potentially remove a Scene from this set.
     * This method will check whether this set contains this Scene for another
     * reason. If not, it will remove it from the set and fire events.
     *
     * @param oldScene the Scene to potentially be removed
     * @return true if it was indeed removed
     */
    protected final synchronized boolean potentiallyRemove(
            Scene oldScene )
    {
        // linear search (FIXME? Not sure we can do ordering necessary for binary search)
        int foundIndex = -1;
        for( int i=0 ; i<currentContent.length ; ++i ) {
            if( oldScene == currentContent[i] ) {
                foundIndex = i;
                --currentContentCounters[ foundIndex ];
                break;
            }
        }
        if( foundIndex < 0 ) {
            throw new IllegalArgumentException( "Scene not in set" );
        }

        if( currentContentCounters[ foundIndex ] >= 0 ) {
            return false;
        }

        certainlyRemove( foundIndex );

        return true;
    }

    /**
     * This is invoked by subclasses to indicate that we have one less Scene for sure.
     *
     * @param oldScene the Scene to potentially be removed
     * @return the number of times that the Scene was contained
     */
    protected synchronized int certainlyRemove(
            Scene oldScene )
    {
        for( int i=0 ; i<currentContent.length ; ++i ) {
            if( oldScene == currentContent[i] ) {
                return certainlyRemove( i );
            }
        }
        return 0;
    }

    /**
     * This is invoked by subclasses to remove a Scene at a particular index.
     * We return the number of times that the Scene was contained.
     *
     * @param indexToRemove index of the Scene that we remove
     * @return the number of times that the Scene was contained
     */
    protected synchronized int certainlyRemove(
            int indexToRemove )
    {
        Scene    oldScene    = currentContent[ indexToRemove ];
        int      ret         = currentContentCounters[ indexToRemove ] + 1; // we are off by one
        Scene [] newContent  = new Scene[ currentContent.length-1 ];
        int []   newCounters = new int[ newContent.length ];

        for( int i=0 ; i<indexToRemove ; ++i ) {
            newContent[i]  = currentContent[i];
            newCounters[i] = currentContentCounters[i];
        }
        for( int i=indexToRemove+1 ; i<currentContent.length ; ++i ) {
            newContent[i-1]  = currentContent[i];
            newCounters[i-1] = currentContentCounters[i];
        }

        oldScene.removePropertyChangeListener( this );

        currentContent         = newContent;
        currentContentCounters = newCounters;

        theSetListeners.fireEvent( new SceneRemovedEvent( this, oldScene ));

        return ret;
    }

    /**
     * Callback from one of our contained Scenes that one of its properties changed.
     *
     * @param theEvent the event
     */
    public void propertyChange(
            PropertyChangeEvent theEvent )
    {
        // re-broadcast
        thePropertyListeners.fireEvent( theEvent );
    }

    /**
     * The current content of this set.
     */
    private Scene [] currentContent;

    /**
     * This array has the same length as currentContent, and counts the number of times
     * the corresponding element in currentContent is "contained" in this set. This is to
     * make this set a real set, not something with duplicates.
     *
     * Note that a value of N in this array means that there are N duplicates, not that there
     * are N instances (there are N+1 instances).
     */
    private int [] currentContentCounters;

    /**
     * Listeners for the ActiveSceneSet events.
     */
    protected FlexibleListenerSet<ActiveSceneSetListener,EventObject,Object> theSetListeners = new FlexibleListenerSet<ActiveSceneSetListener,EventObject,Object>() {
        public void fireEventToListener(
                ActiveSceneSetListener listener,
                EventObject            event,
                Object                 parameter )
        {
            if( event instanceof SceneAddedEvent ) {
                listener.sceneAdded( (SceneAddedEvent) event );
            } else if( event instanceof SceneRemovedEvent ) {
                listener.sceneRemoved( (SceneRemovedEvent) event );
            } else {
                log.error( "unexpected event type " + event );
            }
        }
    };

    /**
     * Listeners for content property change events.
     */
    protected FlexiblePropertyChangeListenerSet thePropertyListeners = new FlexiblePropertyChangeListenerSet();
}
