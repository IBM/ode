package com.ibm.ode.bin.makemake;

import java.io.*;
import java.util.Vector;

import com.ibm.ode.lib.io.Path;
import com.ibm.ode.lib.string.StringTools;
import com.ibm.ode.lib.io.CommandLine;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.io.UsagePrintable;
import com.ibm.ode.bin.makemake.MakeMakeOptions;
import com.ibm.ode.bin.makemake.MakeMakeFrame;
import com.ibm.ode.bin.makemake.MakeMakeFileMaker;
import com.ibm.ode.bin.makemake.MakeMakeConfigFile;

/**
 * This tool is used to generate skeleton makefiles
 * for a sandbox's source tree.  It looks for source
 * files (.c, .cpp, .java, etc.) in the specified
 * directory (and all subdirectories if -sub is given).
 * It puts these files in typical variables (OBJECTS,
 * JAVA_CLASSES, etc.) in the makefile it generates.
 *
**/
public class MakeMake implements UsagePrintable
{
  private static CommandLine command_line;
  private static MakeMakeOptions options;


  /**
   * Program entry point.
   *
   * @param args The arguments passed to the MakeMake command.
  **/
  public static void main( String[] args )
  {
    processCmdline( args ); // first, evaluate the command line args
    runMakeMake();          // next, run the program
  }


  /**
   * Either create the GUI window (if the user specified so), or
   * just create basic makefiles.
   *
  **/
  private static void runMakeMake()
  {
    if (options.info)
    {
      Interface.printAlways( "Would generate a makefile in " +
          options.start_dir, false );
      if (options.process_subdirs)
        Interface.printAlways( " and all subdirectories thereof", false );
      Interface.quit( 0 );
    }
    else if (options.use_gui)
      new MakeMakeFrame( options );
    else
    {
      Interface.print( "Using batch mode." );
      MakeMakeFileMaker.createMakefiles( options, options.start_dir );
    }
  }


  /**
   * Parse and evaluate the command line arguments.
   *
   * @param args The arguments passed to the MakeMake command.
  **/
  private static void processCmdline( String[] args )
  {
    String[] states = { "-sub", "-nobackup", "-ofiles", "-noverify",
        "-autoskip", "-info", "-quiet", "-verbose", "-debug" };
    String[] qual_vars = { "-mfname", "-guilevel",
        "-osuffs", "-jsuffs", "-isuffs" };
    String suff_tmp;

    command_line = new CommandLine( states, qual_vars, true, args,
        false, new MakeMake() );
    options = new MakeMakeOptions();

    String[] unqual_vars = command_line.getUnqualifiedVariables();
    options.use_gui = (unqual_vars == null);
    if (MakeMakeConfigFile.read( options ))
    {
      Interface.printVerbose( "Successfully read config file (" +
          MakeMakeConfigFile.CONFIG_FILE_PATH + ")." );
      for (int i = 0; i < options.customvar_names.size(); ++i)
        options.customvar_mfinclude.addElement( new Boolean( false ) );
    }
    else if (options.use_gui)
    {
      Interface.printWarning( "Couldn't read config file (" +
          MakeMakeConfigFile.CONFIG_FILE_PATH + ")...ignoring." );
    }

    initSuffs();
    if (unqual_vars == null)
      options.start_dir = new File( Path.getcwd() );
    else
      options.start_dir = new File( unqual_vars[0] );
    if (!options.start_dir.isDirectory())
      Interface.quitWithErrMsg( "MakeMake: path \"" +
          options.start_dir + "\" must be an existing " +
          "directory.", 1 );
    String mfname = command_line.getQualifiedVariable( "-mfname", 1 );
    if (mfname != null)
      options.makefile_name = mfname;
    else if (options.makefile_name == null)
      options.makefile_name = MakeMakeOptions.DEFAULT_MAKEFILE_NAME;

    suff_tmp = command_line.getQualifiedVariable( "-osuffs", 1 );
    if (suff_tmp != null)
      addSuffs( options.obj_suffs, StringTools.split( suff_tmp, ',' ) );
    suff_tmp = command_line.getQualifiedVariable( "-jsuffs", 1 );
    if (suff_tmp != null)
      addSuffs( options.java_suffs, StringTools.split( suff_tmp, ',' ) );
    suff_tmp = command_line.getQualifiedVariable( "-isuffs", 1 );
    if (suff_tmp != null)
      addSuffs( options.hdr_suffs, StringTools.split( suff_tmp, ',' ) );

    String guilevel = command_line.getQualifiedVariable( "-guilevel", 1 );
    if (guilevel != null)
    {
      if (guilevel.equals( "0" ) || guilevel.equals( "1" ) ||
          guilevel.equals( "2" ))
        options.gui_level = Integer.parseInt( guilevel );
      else
        Interface.quitWithErrMsg( "MakeMake: invalid -guilevel argument", 1 );
    }
    if (command_line.isState( "-sub" ))
      options.process_subdirs = true;
    if (command_line.isState( "-nobackup" ))
      options.backup = false;
    if (command_line.isState( "-info" ))
      options.info = true;
    if (command_line.isState( "-ofiles" ))
      options.use_ofiles = true;
    if (command_line.isState( "-autoskip" ))
      options.autoskip = true;
    if (!options.use_gui || command_line.isState( "-noverify" ))
      options.verify = false;
  }


  private static void initSuffs()
  {
    if (options.obj_suffs.size() < 1)
    {
      options.obj_suffs.addElement( ".c" );
      options.obj_suffs.addElement( ".C" );
      options.obj_suffs.addElement( ".cc" );
      options.obj_suffs.addElement( ".cpp" );
    }
    if (options.hdr_suffs.size() < 1)
    {
      options.hdr_suffs.addElement( ".h" );
      options.hdr_suffs.addElement( ".hpp" );
    }
    if (options.java_suffs.size() < 1)
    {
      options.java_suffs.addElement( ".java" );
    }
  }


  private static void addSuffs( Vector suff_list, String[] new_suffs )
  {
    if (new_suffs == null)
      return;
    for (int i = 0; i < new_suffs.length; ++i)
      suff_list.addElement( new_suffs[i] );
  }


  /**
   * The usage message function that satisfies the UsagePrintable
   * interface.  Typically called by CommandLine.
  **/
  public void printUsage()
  {
    Interface.printAlways( "Usage: MakeMake [options] [path]" );
    Interface.printAlways( "       path: The root directory of the source " +
        "tree (for batch mode)." );
    Interface.printAlways( "       options:" );
    Interface.printAlways( "           -guilevel <0|1|2>" );
    Interface.printAlways( "           -mfname <makefile name>" );
    Interface.printAlways( "           -osuffs <suffix>[,suffix...]" );
    Interface.printAlways( "           -jsuffs <suffix>[,suffix...]" );
    Interface.printAlways( "           -isuffs <suffix>[,suffix...]" );
    Interface.printAlways( "           -noverify -sub -nobackup -ofiles " +
        "-autoskip" );
    Interface.printAlways( "           -info -usage -version -rev" );
  }
}
