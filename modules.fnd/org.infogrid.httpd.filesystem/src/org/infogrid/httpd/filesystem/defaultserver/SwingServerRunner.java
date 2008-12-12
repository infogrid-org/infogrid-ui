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

package org.infogrid.httpd.filesystem.defaultserver;

import org.infogrid.module.Module;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashMap;

/**
 * A simple executable that can run the HttpFilesystemServer, with a primitive Swing GUI
 * to start and stop the server.
 */
public class SwingServerRunner
{
    /**
     * Main program.
     *
     * @param args command-line arguments
     */
    public static void main(
            String [] args )
    {
        try {
            main0( args );
        } catch( Exception ex ) {
            ex.printStackTrace();
        }
    }

    /**
     * Real main program.
     *
     * @param args command-line arguments
     * @throws Exception any Exception
     */
    protected static void main0(
            String [] args )
        throws
            Exception
    {
        JFrame f = createFrame();
        f.setVisible( true );

        HashMap<String,Object> parameters = new HashMap<String,Object>();
        parameters.put( "org.httpd.filesystem.defaultserver.DocumentRoot", new File( System.getProperty( "user.home" )));

        HttpFilesystemServer.configure(
                parameters,
                null,
                null );
    }

    /**
     * Create the GUI.
     *
     * @return the JFrame containing the GUI
     */
    protected static JFrame createFrame()
    {
        JFrame f = new JFrame( "ServerRunner" );
        f.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );

        JPanel        p     = new JPanel( new BorderLayout() );
        final JButton act   = new JButton( "activate!" );
        final JButton deact = new JButton( "deactivate!" );

        deact.setEnabled( false );
        p.add( act,   BorderLayout.WEST );
        p.add( deact, BorderLayout.EAST );

        act.addActionListener( new ActionListener() {
            public void actionPerformed(
                    ActionEvent e )
            {
                act.setEnabled( false );
                deact.setEnabled( true );

                try {
                    HttpFilesystemServer.activate(
                            new Module[] {},
                            new Object[] {},
                            new Object[] {},
                            null );
                } catch( Exception ex ) {
                    ex.printStackTrace();
                }
            }
        } );
        deact.addActionListener( new ActionListener() {
            public void actionPerformed(
                    ActionEvent e )
            {
                act.setEnabled( true );
                deact.setEnabled( false );

                HttpFilesystemServer.deactivate(
                        new Module[] {},
                        null );
            }
        } );

        f.getContentPane().add( p );
        f.pack();

        return f;
    }
}
