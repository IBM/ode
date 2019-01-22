package com.ibm.ode.bin.makemake;

import java.awt.*;
import java.awt.event.*;

public class MakeMakeYesNoDialog extends Dialog
{
  private boolean result;
  

  public MakeMakeYesNoDialog( Frame frame, String title, String question )
  {
    super( frame, title, true );
    result = false;
    setLayout( new GridBagLayout() );
    GridBagConstraints bag_constraints = new GridBagConstraints();
    bag_constraints.gridwidth = GridBagConstraints.REMAINDER;
    bag_constraints.gridheight = 1;
    bag_constraints.gridx = 0;
    bag_constraints.gridy = 0;
    bag_constraints.insets = new Insets( 6, 10, 6, 10 );
    bag_constraints.weightx = 1.0;
    bag_constraints.fill = GridBagConstraints.HORIZONTAL;
    setBackground( SystemColor.window );
    setForeground( SystemColor.windowText );
    setFont( new Font( "Dialog", Font.BOLD, 12 ) );
    Point winloc = frame.getLocationOnScreen();
    setLocation( winloc.x + 20, winloc.y + 20 );
    add( new Label( question, Label.CENTER ), bag_constraints );
    ++bag_constraints.gridy;

    Button yes_button = new Button( "YES" );
    yes_button.setBackground( SystemColor.control );
    yes_button.setForeground( SystemColor.controlText );
    yes_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { result = true; dispose(); } } );
    bag_constraints.gridwidth = 1;
    bag_constraints.anchor = GridBagConstraints.SOUTHWEST;
    add( yes_button, bag_constraints );

    Button no_button = new Button( " NO " );
    no_button.setBackground( SystemColor.control );
    no_button.setForeground( SystemColor.controlText );
    no_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { result = false; dispose(); } } );
    bag_constraints.gridx = 1;
    bag_constraints.anchor = GridBagConstraints.SOUTHEAST;
    add( no_button, bag_constraints );

    addWindowListener( new WindowAdapter()
        { public void windowClosing( WindowEvent e )
        { result = false; dispose(); } } );
    pack();
  }


  public boolean run()
  {
    show();
    return (result);
  }
}
