package com.ibm.ode.bin.makemake;

import java.awt.AWTEvent;

public class MakeMakeEvent extends AWTEvent
{
  public static final int RUN_EVENT =
      AWTEvent.RESERVED_ID_MAX + 1;
  public static final int VERIFY_DONE_EVENT =
      AWTEvent.RESERVED_ID_MAX + 2;
  public static final int SAVE_EVENT =
      AWTEvent.RESERVED_ID_MAX + 3;
  public static final int MFNAME_CHANGE_EVENT =
      AWTEvent.RESERVED_ID_MAX + 4;
  public static final int GUILEVEL_CHANGE_EVENT =
      AWTEvent.RESERVED_ID_MAX + 5;

  public MakeMakeEvent( Object source, int id )
  {
    super( source, id );
  }
}
