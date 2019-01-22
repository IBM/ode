package com.ibm.ode.bin.makemake;

import java.io.PrintWriter;

import com.ibm.ode.bin.makemake.MakeMakeOutputable;

public class MakeMakePrintWriter extends PrintWriter
    implements MakeMakeOutputable
{
  public MakeMakePrintWriter( PrintWriter pw )
  {
    super( pw, true );
  }
}
