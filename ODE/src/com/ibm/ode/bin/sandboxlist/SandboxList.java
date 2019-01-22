package com.ibm.ode.bin.sandboxlist;

import java.io.*;
import java.util.Vector;

import com.ibm.ode.lib.io.Path;
import com.ibm.ode.lib.string.StringTools;
import com.ibm.ode.lib.string.PlatformConstants;
import com.ibm.ode.lib.io.CommandLine;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.io.UsagePrintable;

/**
 * This GUI tool displays a list of sandboxes and provides
 * the user with means to operate on sandboxes. 
 * The initial version derives all sandboxes from a single
 * .sandboxrc file which is either supplied by the user or
 * is found from the execution environment. If no .sandboxrc
 * file is found, it will be created when the user creates
 * a backing build or a backed sandbox using this GUI.
**/
public class SandboxList
{
  static CommandLine command_line = null;
  static SLState list_state = null;
  static SLFrame frame = null;
  static boolean debugState = false;
  static String defaultEditor = "";


  /**
   * Program entry point.
   *
   * @param args The arguments passed to the SandboxList command.
  **/
  public static void main( String[] args )
  {
    processCmdline( args );
    new SandboxList( command_line.getRcfile() );
    Interface.printDebug( "SandboxList main() is ending" );
  }


  /**
   * Check the arguments a bit more after getting the sandbox list
  **/
  public SandboxList( String rcfile )
  {
    list_state = new SLState( command_line.getRcfile() );
    if (command_line.isState( "-info" ))
    {
      Interface.printAlways( "Would display a list of " +
                             list_state.size() +
                             " sandboxes ", false );
      if (list_state.getRcFile().equals( "" ))
        Interface.printAlways( "with no RC file specified.",
                               false );
      else
        Interface.printAlways( "from RC file " + list_state.getRcFile(),
                               false );
      Interface.quit( 0 );
    }
    frame = new SLFrame();
    frame.initSLFrame( list_state );
    frame.pack();
    frame.setVisible( true );
    Interface.printDebug( "Window was set visible" );
  }


  /**
   * Parse and evaluate the command line arguments.
   *
   * @param args The arguments passed to the SandboxList command.
  **/
  private static void processCmdline( String[] args )
  {
    String[] states = { "" }; // no non-standard flags defined
    String[] qual_vars = { "-rc", "-ed" };

    command_line = new CommandLine( states, qual_vars, false, args,
                                    false, new SandboxListUsage() );
    debugState = command_line.isState( "-debug" );
    String vars[] = command_line.getQualifiedVariable( "-ed" );
    if (vars != null)
      defaultEditor = vars[vars.length - 1];
    else if (PlatformConstants.isWindowsMachine(
                           PlatformConstants.CURRENT_MACHINE ))
      defaultEditor = "edit";
    else if (PlatformConstants.X86_OS2_4_MACHINE.equals(
                           PlatformConstants.CURRENT_MACHINE ))
      defaultEditor = "tedit";
    else
      defaultEditor = "vi";
  }


  /**
   * add code here to do cleanup on exit, if we need to
  **/
  public static void exit( int rc )
  {
    System.exit( rc );
  }

}


class SandboxListUsage implements UsagePrintable
{
  /**
   * The usage message function that satisfies the UsagePrintable
   * interface.  Typically called by CommandLine.
  **/
  public void printUsage()
  {
    Interface.printAlways( "Usage: gui [-rc file] [-ed editor] [ODE-options]" );
    Interface.printAlways( "  ODE-options: -info -usage -version -rev" );
    Interface.printAlways( "               -quiet -normal -verbose -debug" );
  }
}
