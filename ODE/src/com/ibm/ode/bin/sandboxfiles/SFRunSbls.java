package com.ibm.ode.bin.sandboxfiles;

import java.io.*;
import com.ibm.ode.bin.gui.*;
import com.ibm.ode.lib.string.*;
import com.ibm.ode.lib.util.*;
import com.ibm.ode.lib.io.*;

public class SFRunSbls
{
  /**
   * Run "sbls -alpR" or "sbls -alp" and return its output in a StringBuffer.
   * The environment variable BACKED_SANDBOXDIR is always set by
   * this method when running sbls, based upon the value of chain.
   * If chain is null or is zero length then BACKED_SANDBOXDIR is not set,
   * so that sbls will not follow a backing chain.
  **/
  public static StringBuffer runSbls( String[] chain, boolean recurse,
                                      String filespec,
                                      SandboxFiles main_class )
  {
    // join the elements of chain using the system path separator
    return runSbls( StringTools.join( chain,
                                      System.getProperty( "path.separator" ) ),
                    recurse, filespec, main_class );
  }

  /**
   * Run "sbls -alpR" or "sbls -alp" and return its output in a StringBuffer.
   * The environment variable BACKED_SANDBOXDIR is always set by
   * this method when running sbls, based upon the value of chain.
   * If chain is null or is zero length then BACKED_SANDBOXDIR is not set,
   * so that sbls will not follow a backing chain.
  **/
  public static StringBuffer runSbls( String chain, boolean recurse,
                                      String filespec,
                                      SandboxFiles main_class )
  {
    // if chain is not null and not equal to "" then it will be set as
    // the value of BACKED_SANDBOXDIR.
    String cmd = (recurse ? "sbls -alpR " : "sbls -alp " ) + filespec;
    // choose a rather arbitrary starting buffer size, assuming that we will
    // avoid many reallocations if the output of sbls is large.
    StringBuffer out = new StringBuffer( 50000 );
    if (chain != null)
      main_class.envp[0] = "BACKED_SANDBOXDIR=" + chain.trim();
    else
      main_class.envp[0] = "BACKED_SANDBOXDIR=";
    try
    {
      Interface.printDebug( "Running " + cmd );
      PlatformShellSystemCall.exec( cmd, main_class.envp, out, null, true );
    }
    catch (IOException e)
    {
      //Interface.printError( cmd + ": " + e.getMessage() );
      GuiTextMsg.showErrorMsg( main_class.frame,
                                "sbls: " + e.getMessage(),
                                "ERROR: IOException" );
    }
    catch (InterruptedException e)
    {
      //Interface.printError( cmd + ": " + e.getMessage() );
      GuiTextMsg.showErrorMsg( main_class.frame,
                                "sbls: " + e.getMessage(),
                                "ERROR: InterruptedException" );
    }
    return out;
  }

}
