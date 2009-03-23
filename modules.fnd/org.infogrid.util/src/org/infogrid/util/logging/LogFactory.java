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

package org.infogrid.util.logging;

/**
 * Interface for objects that know how to create Log instances. This is the interface
 * between InfoGrid logging and underlying loggers such as <code>Basic</code> and
 * <code>log4j</code>.
 */
public abstract class LogFactory
{
    /**
     * Create a new Log object.
     *
     * @param channelName the name of the channel
     * @return the created Log object
     */
    public Log create(
            String channelName )
    {
        return create( channelName, DEFAULT_DUMPER_FACTORY );
    }

    /**
     * Create a new Log object.
     *
     * @param channelClass the class of the channel
     * @return the created Log object
     */
    public Log create(
            Class channelClass )
    {
        return create( channelClass.getName(), DEFAULT_DUMPER_FACTORY );
    }

    /**
     * Obtain a Logger and specify a dumper factory instead of the default.
     *
     * @param clazz the Class for which we are looking for a Log object
     * @param dumperFactory the factory for dumpers
     * @return the Log object
     */
    public Log create(
            Class                  clazz,
            BufferingDumperFactory dumperFactory )
    {
        return create( clazz.getName(), dumperFactory );
    }

    /**
     * Obtain a Logger and specify a dumper factory instead of the default.
     *
     * @param name name of the Log object that we are looking for
     * @param dumperFactory the factory for dumpers
     * @return the Log object
     */
    public abstract Log create(
            String                 name,
            BufferingDumperFactory dumperFactory );

    /**
     * <p>Show a message to the user interface. This requires an implementation
     *    of the LogFactory that is appropriate for the user interface used by the
     *    application, e.g. AWT dialogs or HTML pages.</p>
     * <p>As an application developer, you should never have to invoke this; instead,
     *    use {@link Log#showMessage Log.showMessage}. However, if you implement a new
     *    LogFactory, obviously you have to develop an implementation of this method.</p>
     *
     * @param logger the logger from which this invocation is delegated
     * @param parentComponent the component to which the error message is reported, e.g.
     *        an AWT component, or an HTML page
     * @param message the error message
     * @param title the title of the dialog
     */
    public abstract void showMessage(
            Log    logger,
            Object parentComponent,
            String message,
            String title );

    /**
     * The default DumperFactory.
     */
    protected static BufferingDumperFactory DEFAULT_DUMPER_FACTORY = ToStringDumperFactory.create();
}
