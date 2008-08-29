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

package org.infogrid.util;

import org.infogrid.util.logging.Log;

import java.util.ArrayList;
import java.util.Iterator;

import java.lang.ref.WeakReference;

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
      * Constructor.
      */
    public QuitManager()
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

        firePrepareForQuit();
        fireDie();

        synchronized( this ) {
            this.notifyAll();
        }
    }

    /**
      * Add a listener.
      *
      * @param newListener the new listener
      * @see #removeQuitListener
      */
    public void addQuitListener(
            QuitListener newListener )
    {
        synchronized( theQuitListeners ) {
            theQuitListeners.add( new WeakReference<QuitListener>( newListener ));
        }
    }

    /**
      * Remove a listener.
      *
      * @param oldListener the to-be-removed listener
      * @see #addQuitListener
      */
    public void removeQuitListener(
            QuitListener oldListener )
    {
        synchronized( theQuitListeners ) {
            theQuitListeners.remove( oldListener );
        }
    }

    /**
      * Fire the prepareForQuit message.
      */
    protected void firePrepareForQuit()
    {
        // we have to clone this because the listeners unsubscribe themselves
        Iterator<WeakReference<QuitListener>> theIter;
        
        synchronized( this ) {
            if( theQuitListeners == null || theQuitListeners.isEmpty() ) {
                return;
            }
            theIter = ( new ArrayList<WeakReference<QuitListener>>( theQuitListeners )).iterator();
        }
        while( theIter.hasNext() ) {
            WeakReference<QuitListener> currentRef = theIter.next();
            QuitListener                current    = currentRef.get();

            if( current == null ) {
                continue;
            }

            try {
                current.prepareForQuit();
            } catch( IsDeadException ex ) {
                log.info( "QuitListener is dead already: " + current, ex );
                // a chain reaction may cause us to invoke the same thing once too often1
            } catch( Throwable all ) {
                log.error( "Exception thrown by QuitListener", all );
            }
        }
    }

    /**
      * Fire the die message.
      */
    protected void fireDie()
    {
        // we have to clone this because the listeners unsubscribe themselves
        Iterator<WeakReference<QuitListener>> theIter;
        
        synchronized( this ) {
            if( theQuitListeners == null || theQuitListeners.isEmpty() ) {
                return;
            }
            theIter = ( new ArrayList<WeakReference<QuitListener>>( theQuitListeners )).iterator();
        }
        while( theIter.hasNext() ) {
            WeakReference<QuitListener> currentRef = theIter.next();
            QuitListener                current    = currentRef.get();

            if( current == null ) {
                continue;
            }

            try {
                current.die();
            } catch( IsDeadException ex ) {
                log.info( "QuitListener is dead already: " + current, ex );
                // a chain reaction may cause us to invoke the same thing once too often1
            } catch( Throwable all ) {
                log.error( "Exception thrown by QuitListener", all );
            }
        }
    }

    /**
     * When this is set to true, we have initiated quit already.
     */
    private boolean haveInitiatedQuit = false;

    /**
      * The current set of quit listeners.
      */
    protected ArrayList<WeakReference<QuitListener>> theQuitListeners = new ArrayList<WeakReference<QuitListener>>();
}
