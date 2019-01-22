package com.ibm.ode.bin.sandboxfiles;

import java.io.*;
import java.util.*;

import com.ibm.ode.lib.io.Path;
import com.ibm.ode.lib.io.CommandLine;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.io.UsagePrintable;
import com.ibm.ode.lib.string.*;
import com.ibm.ode.lib.util.PlatformShellSystemCall;
import com.ibm.ode.bin.gui.*;
import javax.swing.JOptionPane;

/**
 * This GUI tool displays a list of files and directories,
 * typically within a sandbox, although that is not necessary.
 * The user is provided with means to act on the files. 
 * Initial version derives its file and directory data from
 * running sbls. If started in a workon, the information
 * includes files from the backing chain.
**/
public class SandboxFiles
{
  public CommandLine command_line = null;
  public SFState list_state = null;
  public String defaultEditor = "";
  public String[] chain = null;
  public String[] envp = null; // see comments for the getChain method.
  public SFFrame frame = null;

  private boolean debugState = false;
  private String[] args;
  private String backedvar = "BACKED_SANDBOXDIR=";
  private String sandboxbasevar = "SANDBOXBASE=";


  /**
   * Program entry point.
   *
   * @param args The arguments passed to the SandboxFiles command.
  **/
  public static void main( String[] args )
  {
    new SandboxFiles( args ).run();
  }


  /**
   * Check the arguments a bit more after getting the sandbox list
  **/
  public SandboxFiles( String[] args )
  {
    this.args = args;
  }


  public void run()
  {
    processCmdline( args );
    chain = getChain();
    list_state = new SFState( this );
    if (command_line.isState( "-info" ))
      Interface.printAlways( "Would display a list of " +
                             list_state.size() + " files ", false );
    else
    {
      frame = new SFFrame( this, list_state );
      frame.initSFFrame();
      frame.pack();
      frame.setVisible( true );
      Interface.printDebug( "Window was set visible" );
    }
  }


  /**
   * Returns the backing chain as an array of Strings, with first String
   * being for the sandbox itself.
   * This method uses flags like -sb and -rc if they exist.
   * The environment variable BACKED_SANDBOXDIR is used otherwise.
   * If no backing chain can be found, null is returned.
   * As a side effect, the public static String[] envp variable is set
   * to the current environment, with envp[0] used for BACKED_SANDBOXDIR.
   * If no BACKED_SANDBOXDIR exists in the environment, envp[0] is set
   * to "BACKED_SANDBOXDIR=". It is expected that SFRunSbls and perhaps
   * other parts of the program will modify envp[0] as needed to run
   * system or shell commands. Note that the envp variable is used only
   * when something in the environment must be set or changed while
   * running a command. Otherwise only null should be passed as the envp
   * object.
  **/
  private String[] getChain()
  {
    // 1. If there is -rc or -sb, run it with currentsb to get the
    // backing chain, and to verify that the sandbox is valid. If it is not
    // valid, then rerun with error messages shown? In any case, for now, end
    // the program.
    // 2. If there is no -rc or -sb then look for BACKED_SANDBOXDIR when
    // getting the environment.
    // shell -c env and parse the output to get the
    // chain, if any. On Windows and OS/2 do shell /C set BACKED_SANDBOXDIR
    // and parse the output.
    boolean windowsLike = 
        PlatformConstants.isWindowsMachine(
                           PlatformConstants.CURRENT_MACHINE ) ||
        PlatformConstants.X86_OS2_4_MACHINE.equals(
                           PlatformConstants.CURRENT_MACHINE );
    String rcfile = command_line.getRcfile();
    String sandbox = command_line.getSandbox();
    String cmd;
    String[] chain = null;
    if (rcfile != null || sandbox != null)
    {
      chain = runCurrentsbForChain( rcfile, sandbox );
      if (chain != null && chain.length == 0)
        chain = null;
      if (chain == null && (rcfile != null || sandbox != null))
      { // We should have found a chain if the explicit or default sandbox
        // was specified.
        JOptionPane.showMessageDialog( frame, 
                                 "The " +
                                 (sandbox == null ? "default" : sandbox ) +
                                 " sandbox" +
                                 (rcfile == null ? "" : " in rcfile " + rcfile) +
                                 "\nis not valid or has ERROR status!",
                                 "Invalid sandbox",
                                 JOptionPane.WARNING_MESSAGE );
        return (null);
      }
    }
    // run set or env to get the environment.
    cmd = ( windowsLike ? "set" : "env");
    Interface.printDebug( "about to run cmd='" + cmd + "'" );
    StringBuffer out = new StringBuffer();
    int retcode = 0;
    try
    {
      retcode = PlatformShellSystemCall.exec( cmd, null, out, null, false );
      if (retcode == 0)
      {
        // sniff at lines in StringBuffer
        LineNumberReader lineReader =
           new LineNumberReader( new StringReader( out.toString() ) );
        String line = "";
        String chainLine = "";
        Vector venvp = new Vector();
        venvp.addElement( backedvar ); // have it be first
        venvp.addElement( sandboxbasevar ); // have it be second
        while ((line = lineReader.readLine()) != null)
        {
          if (!line.equals( "" ))
          {
            if (line.startsWith( backedvar ))
            {
              venvp.setElementAt( line, 0 );
              chainLine = line;
            }
            else if (line.startsWith( sandboxbasevar ))
            {
              venvp.setElementAt( line, 1 );
            }
            else
              venvp.addElement( line );
          }
        } // end while
        // convert Vector to String[] and store in envp.
        envp = new String[venvp.size()];
        venvp.copyInto( envp );
        Interface.printDebug( "backing chain from environment '" +
                              chainLine + "'" );
        if ((chain == null || chain.length == 0) && !chainLine.equals( "" ))
        {
          chain = StringTools.split( chainLine.substring( backedvar.length()),
                                    System.getProperty( "path.separator" ) );
        }
        if (chain != null && chain.length > 0)
        {
          envp[0] = backedvar + StringTools.join( chain,
                                      System.getProperty( "path.separator" ) );
          envp[1] = sandboxbasevar + chain[0];
        }
      }
      else
        Interface.printDebug( "PlatformShellSystemCall.exec retcode=" +
                              retcode );
    }
    catch (IOException e)
    {
      Interface.printError( cmd + ": IOException: " + e.getMessage() );
      GuiTextMsg.showErrorMsg( frame,
                               cmd + ": " + e.getMessage(),
                               "ERROR: IOException" );
    }
    catch (InterruptedException e)
    {
      Interface.printError( cmd + ": InterruptedException: " +
                            e.getMessage() );
      GuiTextMsg.showErrorMsg( frame,
                               cmd + ": " + e.getMessage(),
                               "ERROR: InterruptedException" );
    }

    if (chain != null && chain.length == 0)
      chain = null;
    if (chain != null)
      for (int i = 0; i < chain.length; ++i)
        chain[i] = Path.canonicalize( new File( chain[i] ) );
    return chain;
  }


  /**
   * This method checks the specified sandbox using the currentsb command
   * to determine if the sandbox is "valid" and whether it is a backing build.
   * Validity is determined by checking for ">> ERROR:" messages
   * from "currentsb -chain sandbox".
   * Note that some warning messages do not indicate a problem:
   * > WARNING: workon: Unable to find Buildconf () for sandbox snark
   * > WARNING: workon: Unable to find Buildconf.exp () for sandbox snark
   * since the sandbox or its backing build may be new and not have its
   * files created yet. The user would not want to be prevented from doing a
   * workon for the sandbox. The indicated warning would not prevent a workon.
   * The intent of indicating validity is to give some sort of warning to the
   * user that there may be problems.
   * @param rcfile is null or the -rc flag value
   * @param sandbox is null or the -sb flag value
  **/
  private String[] runCurrentsbForChain( String rcfile, String sandbox )
  {
    // Run the cmd, capturing the output in two StringBuffer varibles out
    // and err. Also record the return code in rc.
    // Have a loop parsing out, and for each sandbox note whether it is
    // is default, and save its name, optional variable base, and base.
    // Then call a routine that runs currentsb to get info about it being
    // a backing build and whether it is valid.
    int retcode = 0;
    StringBuffer out = new StringBuffer();
    StringBuffer err = new StringBuffer();
    boolean foundError = false;
    boolean foundWarning = false;
    String pathsep = System.getProperty( "path.separator" );
    int chainCount = 0;
    String retstring = "";
    String cmd = "currentsb -chain" +
                 (rcfile == null ? "" : " -rc " + rcfile) +
                 (sandbox == null ? "" : " -sb " + sandbox);
    try
    {
      Interface.printDebug( "Parsing output of: " + cmd );
      retcode = PlatformShellSystemCall.exec( cmd, null, out, err
                                              , true // interleave
                                              );
      // now we need to scan the out buffer, looking for ends of line,
      // and parse each line.
      // Create the line number reader
      LineNumberReader lineReader =
         new LineNumberReader( new StringReader( err.toString() + 
                                                 out.toString() ) );
      String line;
      
      // Loop until null
      while ( ( line = lineReader.readLine() ) != null )
      {
        String tok1 = "";
        String tok2 = "";
        StringTokenizer st = new StringTokenizer( line );
        if (st.hasMoreTokens())
          tok1 = st.nextToken();
        if (st.hasMoreTokens())
          tok2 = st.nextToken();
        if (tok1.equals( "" ))
          continue; // skip blank line, though it is probably a bug
        if (tok1.charAt( 0 ) == '>' )
        { // should be warning or error message
          if (tok2.equals( "WARNING:" ))
            foundWarning = true;
          else if (tok2.equals( "ERROR:" ))
            foundError = true;
          else
          {
            throw (new GuiCommandOutputException( 
                  "Expecting 'WARNING:' or 'ERROR:' line but got: " +
                  line ));
          }
        }
        else
        {
          // if we gave currentsb a -sb argument, it will return the sandbox
          // name as well as the full paths for the backing chain; so skip it
          // if it is not an absolute path.
          if (sandbox != null && !new File(tok1).isAbsolute())
            continue;
          // Assume it is part of the backing chain
          if (chainCount > 0)
            retstring = retstring + pathsep + tok1;
          else
            retstring = tok1;
          ++chainCount;
        }
      }
    }
    catch (IOException e)
    {
      //Interface.printError( cmd + ": " + e.getMessage() );
      GuiTextMsg.showErrorMsg( null,
                               cmd + ": " + e.getMessage(),
                               "ERROR: IOException" );
    }
    catch (InterruptedException e)
    {
      //Interface.printError( cmd + ": " + e.getMessage() );
      GuiTextMsg.showErrorMsg( null,
                               cmd + ": " + e.getMessage(),
                               "ERROR: InterruptedException" );
    }
    catch (GuiCommandOutputException e)
    {
      //Interface.printError( cmd + ": output parsing error: " +
      //                      e.getMessage() );
      GuiTextMsg.showErrorMsg( null,
                               cmd + ": " + e.getMessage(),
                               "ERROR: Output parsing error" );
    }
    // split on the path separator
    Interface.printDebug( "backing chain from currentsb '" +
                          retstring + "'" );
    return StringTools.split( retstring, pathsep );
  }


  /**
   * Parse and evaluate the command line arguments.
   *
   * @param args The arguments passed to the SandboxFiles command.
  **/
  private void processCmdline( String[] args )
  {
    String[] states = { "-sandboxonly", "-norecurse" };
    String[] qual_vars = { "-ed", "-rc", "-sb" };

    command_line = new CommandLine( states, qual_vars, true, args,
                                    false, new SandboxFilesUsage() );
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
}


class SandboxFilesUsage implements UsagePrintable
{
  /**
   * The usage message function that satisfies the UsagePrintable
   * interface.  Typically called by CommandLine.
  **/
  public void printUsage()
  {
    Interface.printAlways( 
 "Usage: SandboxFiles [-ed editor] [-sandboxonly] [-norecurse] [-sb sandbox]" );
    Interface.printAlways( 
 "                    [-rc rcfile] [filespec...] [ODE-options...]" );
    Interface.printAlways(
 "       ODE-options: -info -usage -version -rev" );
    Interface.printAlways(
 "                    -quiet -normal -verbose -debug" );
  }
}
