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

package org.infogrid.util;

import org.infogrid.util.logging.Log;

/**
  * <p>This manager acts as a synchronizer to enable a defined "quit".
  * There is typically only one QuitManager per Java VM.</p>
  *
  * <p>Objects implementing the {@link QuitListener} interface register with the
  * QuitManager. When a quit is requested (by an invocation of {@link #initiateQuit}),
  * the QuitManager will call {@link QuitListener#prepareForQuit} on all registered QuitListeners
  * followed by a final {@link QuitListener#die} on all of them.</p>
  */
public class QuitManager
{
    private static final Log log = Log.getLogInstance( QuitManager.class); // our own, private logger

    /**
     * Factory method.
     *
     * @return the created QuitManager
     */
    public static QuitManager create()
    {
        return new QuitManager();
    }

    /**
     * Constructor, for subclasses only. Use factory method.
     */
    protected QuitManager()
    {
    }

    /**
     * This is only called by the program's main Thread, which is being suspended in this method
     * until some other Thread initiates a quit.
     *
     * @throws InterruptedException if another Thread sent an interrupt
     */
    public void waitForQuit()
        throws
            InterruptedException
    {
        synchronized( this ) {
            if( !haveInitiatedQuit ) {
               this.wait();
            }
        }
    }

    /**
      * This call causes the quit procedure to be initiated.
      */
    public void initiateQuit()
    {
        synchronized( this ) {
            if( haveInitiatedQuit ) {
                return;
            }

            haveInitiatedQuit = true;
        }

        theQuitListeners.fireEvent( null, EventType.PREPARE_FOR_QUIT );
        theQuitListeners.fireEvent( null, EventType.DIE );

        synchronized( this ) {
            this.notifyAll();
        }
    }

    /**
     * Add a new listener using a WeakReference.
     *
     * @param newListener the listener to be added to this set
     * @see #addSoftQuitListener
     * @see #addDirectQuitListener
     * @see #removeQuitListener
     */
    public void addWeakQuitListener(
            QuitListener newListener )
    {
        theQuitListeners.addWeak( newListener );
    }

    /**
     * Add a new listener using a SoftReference.
     *
     * @param newListener the listener to be added to this set
     * @see #addWeakQuitListener
     * @see #addDirectQuitListener
     * @see #removeQuitListener
     */
    public void addSoftQuitListener(
            QuitListener newListener )
    {
        theQuitListeners.addSoft( newListener );
    }
    
    /**
     * Add a new listener directly, i.e. without using References.
     *
     * @param newListener the new listener
     * @see #addWeakQuitListener
     * @see #addSoftQuitListener
     * @see #removeQuitListener
     */
    public void addDirectQuitListener(
            QuitListener newListener )
    {
        theQuitListeners.addDirect( newListener );
    }

    /**
     * Remove a listener.
     *
     * @param oldListener the to-be-removed listener
     * @see #addWeakQuitListener
     * @see #addSoftQuitListener
     * @see #addDirectQuitListener
     */
    public void removeQuitListener(
            QuitListener oldListener )
    {
        theQuitListeners.remove( oldListener );
    }

    /**
     * When this is set to true, we have initiated quit already.
     */
    private boolean haveInitiatedQuit = false;

    /**
     * The types of events that can be sent.
     */
    protected static enum EventType {
        PREPARE_FOR_QUIT {
            public void fireEventToListener(
                    QuitListener listener )
            {
                listener.prepareForQuit();
            }
        },
        DIE {
            public void fireEventToListener(
                    QuitListener listener )
            {
                try {
                    listener.die();
                } catch( IsDeadException ex ) {
                    // ignore
                }
            }
        };

        /**
         * Fire the event.
         *
         * @param listener the listener to send the event to
         */
        public abstract void fireEventToListener(
                QuitListener listener );
    }

    /**
      * The current set of quit listeners.
      */
    protected FlexibleListenerSet<QuitListener,Void,EventType> theQuitListeners
            = new FlexibleListenerSet<QuitListener,Void,EventType>( true ) {
                    /**
                     * Fire the event to one contained object.
                     *
                     * @param listener the receiver of this event
                     * @param event the sent event
                     * @param parameter dispatch parameter
                     */
                    protected void fireEventToListener(
                            QuitListener listener,
                            Void         event,
                            EventType    parameter )
                    {
                        parameter.fireEventToListener( listener );
                    }

    };
}
