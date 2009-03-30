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

/**
  * Makes it easier to implement QuitListeners.
  */
public abstract class AbstractQuitListener
        implements
            QuitListener
{
    /**
      * Announces that a quit is imminent. Note that because of
      * potential communication between various Objects that receive this
      * notification in arbitrary order, the Object still needs to be
      * functional after it has received this message and react to other
      * Objects' requests (if any).
      */
    public void prepareForQuit()
    {
        // do nothing on this level
    }

    /**
      * This is the last message the Object will receive before the
      * shutdown of the VM.
      */
    public void die()
    {
        // do nothing on this level
    }
}

