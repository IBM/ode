package com.ibm.ode.bin.makemake;

import java.io.*;
import java.util.Vector;

import com.ibm.ode.lib.io.Path;
import com.ibm.ode.lib.string.StringTools;
import com.ibm.ode.bin.makemake.MakeMakeOptions;

public class MakeMakeConfigFile
{
  public static final String CONFIG_FILE_NAME = "makemake.cfg";
  public static final String CONFIG_FILE_DIR =
      System.getProperty( "user.home" );
  public static final String CONFIG_FILE_PATH = CONFIG_FILE_DIR +
      File.separator + CONFIG_FILE_NAME;


  public static boolean read( MakeMakeOptions options )
  {
    try
    {
      BufferedReader handle = Path.openFileReader( CONFIG_FILE_PATH );
      if (handle == null)
        return (false);
      String line;
      while ((line = handle.readLine()) != null)
      {
        line = line.trim();
        if (line.equals( "" ) || line.startsWith( "#" ))
          continue;
        String[] var_and_val = StringTools.split( line, "=", 2 );
        if (var_and_val[0].equals( "process_subdirs" ))
          options.process_subdirs = Boolean.valueOf(
              var_and_val[1] ).booleanValue();
        else if (var_and_val[0].equals( "backup" ))
          options.backup = Boolean.valueOf( var_and_val[1] ).booleanValue();
        else if (var_and_val[0].equals( "use_ofiles" ))
          options.use_ofiles = Boolean.valueOf(
              var_and_val[1] ).booleanValue();
        else if (var_and_val[0].equals( "verify" ))
          options.verify = Boolean.valueOf( var_and_val[1] ).booleanValue();
        else if (var_and_val[0].equals( "autoskip" ))
          options.autoskip = Boolean.valueOf( var_and_val[1] ).booleanValue();
        else if (var_and_val[0].equals( "info" ))
          options.info = Boolean.valueOf( var_and_val[1] ).booleanValue();
        else if (var_and_val[0].equals( "verbosity" ))
          options.verbosity = Integer.valueOf( var_and_val[1] ).intValue();
        else if (var_and_val[0].equals( "gui_level" ))
          options.gui_level = Integer.valueOf( var_and_val[1] ).intValue();
        else if (var_and_val[0].equals( "makefile_name" ))
          options.makefile_name = var_and_val[1];
        else if (var_and_val[0].equals( "obj_suffs" ))
          options.obj_suffs.addElement( var_and_val[1] );
        else if (var_and_val[0].equals( "hdr_suffs" ))
          options.hdr_suffs.addElement( var_and_val[1] );
        else if (var_and_val[0].equals( "java_suffs" ))
          options.java_suffs.addElement( var_and_val[1] );
        else if (var_and_val[0].equals( "customvar_names" ))
          options.customvar_names.addElement( var_and_val[1] );
        else if (var_and_val[0].equals( "customvar_vals" ))
          options.customvar_vals.addElement( var_and_val[1] );
        else if (var_and_val[0].equals( "customvar_comments" ))
          options.customvar_comments.addElement( var_and_val[1] );
        else if (var_and_val[0].equals( "customvar_assignment" ))
          options.customvar_assignment.addElement( Integer.valueOf(
              var_and_val[1] ) );
      }
      handle.close();
      return (true);
    }
    catch (IOException e)
    {
      return (false);
    }
  }


  public static boolean write( MakeMakeOptions options )
  {
    PrintWriter handle = Path.openFileWriter(
        CONFIG_FILE_PATH, false, false );
    if (handle == null)
      return (false);

    handle.println( "# ODE MakeMake configuration file" );
    handle.println( "# Users may edit this file at their own risk" );
    handle.println();
    handle.println( "process_subdirs=" + options.process_subdirs );
    handle.println( "backup=" + options.backup );
    handle.println( "use_ofiles=" + options.use_ofiles );
    handle.println( "verify=" + options.verify );
    handle.println( "autoskip=" + options.autoskip );
    handle.println( "info=" + options.info );
    handle.println( "verbosity=" + options.verbosity );
    handle.println( "gui_level=" + options.gui_level );
    handle.println( "makefile_name=" + options.makefile_name );

    writeVector( handle, options.obj_suffs, "obj_suffs" );
    writeVector( handle, options.hdr_suffs, "hdr_suffs" );
    writeVector( handle, options.java_suffs, "java_suffs" );
    writeVector( handle, options.customvar_names, "customvar_names" );
    writeVector( handle, options.customvar_vals, "customvar_vals" );
    writeVector( handle, options.customvar_comments, "customvar_comments" );
    writeVector( handle, options.customvar_assignment,
        "customvar_assignment" );
    handle.close();
    return (true);
  }


  private static void writeVector( PrintWriter handle, Vector vector,
      String variable )
  {
    if (vector != null)
      for (int i = 0; i < vector.size(); ++i)
      {
        handle.print( variable + "=" );
        handle.println( vector.elementAt( i ) );
      }
  }
}
