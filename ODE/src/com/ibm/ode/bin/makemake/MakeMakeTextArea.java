package com.ibm.ode.bin.makemake;

import java.awt.TextArea;

import com.ibm.ode.bin.makemake.MakeMakeOutputable;

public class MakeMakeTextArea extends TextArea implements MakeMakeOutputable
{
  public MakeMakeTextArea( String text, int rows, int columns, int scrolling )
  {
    super( text, rows, columns, scrolling );
  }


  public void print( String str )
  {
    append( str );
  }


  public void println( String str )
  {
    append( str );
    append( System.getProperty( "line.separator" ) );
  }
}
