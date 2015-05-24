/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.infogrid.jee.viewlet.probe.shadow;

import org.infogrid.viewlet.InvalidViewletActionException;

/**
 * Thrown if a sweep action was initiated, but the MeshBase did not have a Sweeper.
 */
public class NoSweeperException
        extends
            InvalidViewletActionException
{
    /**
     * Constructor.
     *
     * @param v the Viewlet that threw the Exception
     */
    public NoSweeperException(
            ShadowAwareAllMeshBasesViewlet v )
    {
        super( v );
    }
}
