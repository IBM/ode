package com.ibm.ode.bin.sandboxlist;

import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;

import com.ibm.ode.lib.io.Path;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.util.*;
import com.ibm.ode.bin.gui.*;
import com.ibm.ode.lib.string.StringTools;

public class SLListParser
{
  StringBuffer out;
  StringBuffer err;
  int rc;
  String cmd;
  String rcFileFlag;

  SLListParser( String rcFileFlag )
  {
    this.rcFileFlag = rcFileFlag;
    if (rcFileFlag.equals( "" ))
      cmd = "mksb -list";
    else
      cmd = "mksb -list " + rcFileFlag;
  }

  void parse( Vector v )
  {
    Interface.printDebug( "Parsing output of: " + cmd );
    v.removeAllElements();

    // Run the cmd, capturing the output in two StringBuffer varibles out
    // and err. Also record the return code in rc.
    // Have a loop parsing out, and for each sandbox note whether it is
    // is default, and save its name, optional variable base, and base.
    // Then call a routine that runs currentsb to get info about it being
    // a backing build and whether it is valid.
    int retcode;
    StringBuffer out = new StringBuffer();
    StringBuffer err = new StringBuffer();
    try
    {
      retcode = PlatformShellSystemCall.exec( cmd, null, out, err, true );
      // now we need to scan the out buffer, looking for ends of line,
      // and parse each line.
      // Create the line number reader
      LineNumberReader lineReader =
         new LineNumberReader( new StringReader( err.toString() + 
                                                 out.toString() ) );
      String line;
      // keep compiler happy
      SLInfo sli = new SLInfo( "fake" ); 
      boolean inSandbox = false;
      String name;
      String varBase;
      String base;
      
      // Loop until null
      while ( ( line = lineReader.readLine() ) != null )
      {
        String tok1 = "";
        String tok2 = "";
        String tok3 = "";
        StringTokenizer st = new StringTokenizer( line );
        if (st.hasMoreTokens())
          tok1 = st.nextToken();
        if (st.hasMoreTokens())
          tok2 = st.nextToken();
        if (st.hasMoreTokens())
          tok3 = st.nextToken();
        if (inSandbox)
        {
          // see if line has form " Variable Base: varbase".
          // If so, add varbase to sli and leave inSandbox true.
          // Otherwise, if line has the form " Base: base" then
          // add the information to sli, and add sli to Vector v, then
          // set inSandbox false.
          if (tok1.equals( "Variable" ) && tok2.equals( "Base:" ) &&
              !tok3.equals( "" ))
          {
            sli.setVariableBase( tok3 );
          }
          else if (tok1.equals( "Base:" ) && !tok2.equals( "" ))
          {
            sli.setBase( tok2 );
            inSandbox = false;
            getMoreSandboxInfo( rcFileFlag, sli, false );
            v.addElement( sli );
          }
          else
          {
            throw (new GuiCommandOutputException( 
                  "Expecting 'Variable base:' or 'Base:' line but got: " +
                  line ));
          }
        }
        else
        {
          // If line has form "Default Sandbox: name" or " Sandbox: name"
          // then create a new sli and set the appropriate information
          // and set inSandbox true.
          if (tok1.equals( "Default" ) && tok2.equals( "Sandbox:" ) &&
              !tok3.equals( "" ))
          {
            sli = new SLInfo( tok3 );
            sli.setIsDefault( true );
            inSandbox = true;
          }
          else if (tok1.equals( "Sandbox:" ) && !tok2.equals( "" ))
          {
            sli = new SLInfo( tok2 );
            inSandbox = true;
          }
          // Ignore any other line, and do not output as an error message.
          // For example the real error message that happens if there is
          // no .sandboxrc file, should be ignored. The user should see
          // a table with no entries.
        }
      }
      // If inSandbox is true, we missed some output somehow, and an error
      // should be indicated. Print info from the current sli, of course.
      if (inSandbox)
        throw (new GuiCommandOutputException( "Missing 'Base:' line!" ));
    }
    catch (IOException e)
    {
      //Interface.printError( cmd + ": " + e.getMessage() );
      GuiTextMsg.showErrorMsg( SandboxList.frame,
                                cmd + ": " + e.getMessage(),
                                "ERROR: IOException" );
    }
    catch (InterruptedException e)
    {
      //Interface.printError( cmd + ": " + e.getMessage() );
      GuiTextMsg.showErrorMsg( SandboxList.frame,
                                cmd + ": " + e.getMessage(),
                                "ERROR: InterruptedException" );
    }
    catch (GuiCommandOutputException e)
    {
      //Interface.printError( cmd + ": output parsing error: " +
      //                      e.getMessage() );
      GuiTextMsg.showErrorMsg( SandboxList.frame,
                                cmd + ": " + e.getMessage(),
                                "ERROR: Output parsing error" );
    }
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
   * @param SLInfo sli: Object holding some information about the sandbox.
   * Additional information will be set by this routine.
   * @param boolean showMsgs: if true, a modal text dialog is popped up that
   * shows the results of running currentsb -list [-rc rcfile], including
   * the return code.
   * If -debug was specified on the command line, the same information will
   * be output to stdout.
  **/
  void getMoreSandboxInfo( String rcFileFlag, SLInfo sli, boolean showMsgs )
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
    int chainCount = 0;
    String cmd = "currentsb -chain " + sli.getSandboxName();
    if (!rcFileFlag.equals( "" ))
      cmd = cmd + " " + rcFileFlag;
    try
    {
      Interface.printDebug( "Parsing output of: " + cmd );
      retcode = PlatformShellSystemCall.exec( cmd, null, out, err
                                              , true // interleave
                                              );
      //@@@ Interface.printDebug( "@@@ Error output of " + cmd +
      //@@@                       " (return code = " + retcode + ")" +
      //@@@                       ":\n" + err.toString() +
      //@@@                       "@@@ End of error output" );
      //@@@ Interface.printDebug( "@@@ Output of " + cmd + ":\n" +
      //@@@                       out.toString() +
      //@@@                       "@@@ End of output" );
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
        //Interface.printDebug( "@@@ currentsb -chain output line: " + line );
        StringTokenizer st = new StringTokenizer( line );
        if (st.hasMoreTokens())
          tok1 = st.nextToken();
        if (st.hasMoreTokens())
          tok2 = st.nextToken();
        //Interface.printDebug( "@@@ tok1=" + tok1 + " tok2=" + tok2 );
        if (tok1.equals( "" ))
          continue; // skip blank line, though it it probably a bug
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
          // Assume it is part of the backing chain
          ++chainCount;
        }
      }
    }
    catch (IOException e)
    {
      //Interface.printError( cmd + ": " + e.getMessage() );
      GuiTextMsg.showErrorMsg( SandboxList.frame,
                                cmd + ": " + e.getMessage(),
                                "ERROR: IOException" );
    }
    catch (InterruptedException e)
    {
      //Interface.printError( cmd + ": " + e.getMessage() );
      GuiTextMsg.showErrorMsg( SandboxList.frame,
                                cmd + ": " + e.getMessage(),
                                "ERROR: InterruptedException" );
    }
    catch (GuiCommandOutputException e)
    {
      //Interface.printError( cmd + ": output parsing error: " +
      //                      e.getMessage() );
      GuiTextMsg.showErrorMsg( SandboxList.frame,
                                cmd + ": " + e.getMessage(),
                                "ERROR: Output parsing error" );
    }
  if (chainCount == 1)
    sli.setIsBackingBuild( true );
  if (retcode != 0 || foundError)
    sli.setStatus( SLInfo.STATUS_ERROR );
  else if (foundWarning)
    sli.setStatus( SLInfo.STATUS_WARNING );
  else
    sli.setStatus( SLInfo.STATUS_OK );
  }

}
