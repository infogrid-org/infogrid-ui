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

package org.infogrid.util.context;

/**
  * All objects that make use of having <code>Context</code> support this interface.
  * All classes that implement <code>ObjectInContext</code> also take (at least)
  * one parameter for their constructor, i.e. the <code>Context</code> in which they run.
  */
public interface ObjectInContext
{
     /**
      * Obtain the context in which this <code>ObjectInContext</code> runs.
      *
      * @return the Context in which this <code>ObjectInContext</code> runs.
      */
    public abstract Context getContext();
}
