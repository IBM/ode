package com.ibm.ode.bin.makemake;

import java.awt.*;
import java.awt.event.*;

import com.ibm.ode.bin.makemake.MakeMakeOptions;

public class MakeMakeTextDialog extends Dialog
{
  private MakeMakeTextField text_field;
  private String result;
  private class MakeMakeTextField extends TextField
  {
    private String illegal_chars;
    private int maxlen;
    public MakeMakeTextField( String text, String illegal_chars, int maxlen )
    {
      super( text );
      this.illegal_chars = illegal_chars;
      this.maxlen = maxlen;
      selectAll();
      setFont( new Font( "Monospaced", Font.PLAIN, 12 ) );
      setBackground( Color.white );
      setForeground( Color.black );
      enableEvents( AWTEvent.KEY_EVENT_MASK );
    }

    /**
     * Validation is not currently performed, but if it ever is,
     * this is where it will go.
    **/
    public boolean isInputValid()
    {
      return (true);
    }
  
    protected void processKeyEvent( KeyEvent e )
    {
      if (e.getID() == KeyEvent.KEY_PRESSED)
      {
        int key = e.getKeyCode();
        char keyc = e.getKeyChar();
        if (key == KeyEvent.VK_ENTER && isInputValid())
        {
          result = getText();
          dispose();
        }
        else if (maxlen > 0 && getText().length() >= maxlen &&
            !isEditChar( key ))
          e.consume();
        else if (illegal_chars.indexOf( keyc ) >= 0)
          e.consume();
      }
    }

    private boolean isEditChar( int ch )
    {
      if (ch == KeyEvent.VK_BACK_SPACE || ch == KeyEvent.VK_DELETE ||
          ch == KeyEvent.VK_HOME || ch == KeyEvent.VK_END ||
          ch == KeyEvent.VK_UP || ch == KeyEvent.VK_DOWN ||
          ch == KeyEvent.VK_LEFT || ch == KeyEvent.VK_RIGHT)
        return (true);
      return (false);
    }
  }
  

  public MakeMakeTextDialog( Frame frame, String title,
      String label, String field_text, String illegal_chars, int maxlen )
  {
    super( frame, title, true );
    result = null;
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
    text_field = new MakeMakeTextField( field_text, illegal_chars, maxlen );
    Point winloc = frame.getLocationOnScreen();
    setLocation( winloc.x + 20, winloc.y + 20 );
    add( new Label( label, Label.CENTER ), bag_constraints );
    ++bag_constraints.gridy;
    add( text_field, bag_constraints );
    ++bag_constraints.gridy;

    Button ok_button = new Button( "  OK  " );
    ok_button.setBackground( SystemColor.control );
    ok_button.setForeground( SystemColor.controlText );
    ok_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { if (text_field.isInputValid()) result = text_field.getText();
        dispose(); } } );
    bag_constraints.gridwidth = 1;
    bag_constraints.anchor = GridBagConstraints.SOUTHWEST;
    add( ok_button, bag_constraints );

    Button cancel_button = new Button( "Cancel" );
    cancel_button.setBackground( SystemColor.control );
    cancel_button.setForeground( SystemColor.controlText );
    cancel_button.addActionListener( new ActionListener()
        { public void actionPerformed( ActionEvent e )
        { dispose(); } } );
    bag_constraints.gridx = 1;
    bag_constraints.anchor = GridBagConstraints.SOUTHEAST;
    add( cancel_button, bag_constraints );

    addWindowListener( new WindowAdapter()
        { public void windowClosing( WindowEvent e )
        { dispose(); }
        public void windowOpened( WindowEvent e )
        { text_field.requestFocus(); } } );
    pack();
  }


  public String run()
  {
    show();
    return (result);
  }
}
