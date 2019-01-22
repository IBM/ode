package com.ibm.ode.bin.sandboxfiles;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.io.Version;
import com.ibm.ode.lib.util.*;
import com.ibm.ode.bin.gui.*;


/**
 * Should show copyright, version, etc. Preferably the version and so
 * on should be something the build scripts automatically replace when
 * an ODE build is done.
**/
class SFAboutCmd implements SFAction
{

  public void doAction( SFFrame frame )
  {
    JDialog dia = new JDialog( frame, "About", true );
    // Add a bunch of things to the contentPane: cp.add( child );
    Container cp = dia.getContentPane();
    cp.setLayout( new BoxLayout( cp, BoxLayout.Y_AXIS ) );

    JLabel toolName = new JLabel( "ODE GUI Sandbox Files Tool",
                                  SwingConstants.CENTER );
    toolName.setBorder( BorderFactory.createEmptyBorder( 10, 10, 10, 10 ) );
    cp.add( toolName );

    JLabel version = new JLabel( "Version: " + Version.getOdeVersionNumber() +
                                 " (Build " + Version.getOdeLevelName() + ")",
                                 SwingConstants.CENTER );
    version.setBorder( BorderFactory.createEmptyBorder( 0, 10, 10, 10 ) );
    cp.add( version );

    JLabel copyright = new JLabel( "\u00a9 2002 IBM Corporation",
                                   SwingConstants.CENTER );
    copyright.setBorder( BorderFactory.createEmptyBorder( 0, 10, 10, 10 ) );
    cp.add( copyright );

    dia.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
    Point loc = frame.getLocation();
    Dimension frdim = frame.getSize();
    // show the create dialog
    dia.pack();
    Dimension diadim = dia.getSize();
    dia.setLocation( loc.x + (frdim.width - diadim.width)/2,
                     loc.y + (frdim.height - diadim.height)/2 );
    dia.setVisible( true );
  }
}
