package com.ibm.ode.bin.makemake;

import java.io.*;
import java.awt.TextArea;
import java.awt.Checkbox;
import java.util.Vector;

import com.ibm.ode.lib.io.Path;
import com.ibm.ode.lib.io.Interface;
import com.ibm.ode.lib.io.Version;
import com.ibm.ode.lib.string.StringTools;
import com.ibm.ode.bin.makemake.MakeMakeOptions;

public class MakeMakeFileMaker implements Runnable
{
  private static final String INCLUDE_RULES_TEXT = ".include <${RULES_MK}>";
  private static final String CLASS_SUFF = "${CLASS_SUFF}";
  private static final String OBJ_SUFF = "${OBJ_SUFF}";
  private static File curdir;
  private boolean done, verified, got_data, entire_subtree, entire_subtree_skip;
  private String entire_subtree_start;
  private MakeMakeOptions options;
  private MakeMakeOutputable error_printer;
  private File start_dir;


  public MakeMakeFileMaker( MakeMakeOptions options, File dir )
  {
    this( options, dir, null );
  }


  public MakeMakeFileMaker( MakeMakeOptions options, File dir,
      MakeMakeOutputable error_printer )
  {
    this.options = options;
    this.start_dir = dir;
    this.error_printer = error_printer;
    this.entire_subtree = false;
  }


  public synchronized void run()
  {
    options.success = createMakefiles( start_dir );
    done = true;
    notify();
  }


  public static boolean createMakefiles( MakeMakeOptions options, File dir )
  {
    return (createMakefiles( options, dir, null ));
  }


  /**
   * Recursive function which processes each directory's
   * contents.  Files are handled by another function.
   *
   * @param options The current MakeMake options settings.
   * @param dir The directory to process.  Assumed to be
   * non-null.
   * @param error_printer When this object isn't null, it
   * will be used for printing error messages instead of
   * using stdout.
   * @return True if makefile was successfully created, or false
   * if not.
  **/
  public static boolean createMakefiles( MakeMakeOptions options,
      File dir, MakeMakeOutputable error_printer )
  {
    return (new MakeMakeFileMaker( options, dir,
        error_printer ).createMakefiles( dir ));
  }


  public static boolean updateMakefilePath( MakeMakeOptions options )
  {
    try
    {
      options.makefile_path = new File( curdir.getCanonicalPath() +
          File.separator + options.makefile_name );
      return (true);
    }
    catch (IOException e)
    {
      return (false);
    }
  }


  private synchronized boolean createMakefiles( File dir )
  {
    if (!dir.isDirectory())
      return (false);

    curdir = dir; // for updateMakefilePath's sake

    if (!updateMakefilePath( options ))
      return (false);

    options.optvar_vals = new String[options.optvar_names.length];
    for (int i = 0; i < options.optvar_vals.length; ++i)
      options.optvar_vals[i] = "";
    options.targvar_names = new Vector();
    options.targvar_vals = new Vector();
    for (int i = 0; i < options.customvar_names.size(); ++i)
      options.customvar_mfinclude.setElementAt( new Boolean( false ), i );
    options.available_subdirs = null;

    processFiles( dir, options );

    String[] dirs = Path.getDirContents( dir, false, Path.CONTENTS_DIRS );

    if (dirs != null)
    {
      StringBuffer dir_names = new StringBuffer();
      for (int i = 0; i < dirs.length; ++i)
        dir_names.append( dirs[i] ).append( " " );
      options.available_subdirs = dir_names.toString();
      if (options.process_subdirs)
      {
        options.optvar_vals[MakeMakeOptions.EXPINC_SUBDIRS_INDEX] =
            dir_names.toString();
        options.optvar_vals[MakeMakeOptions.OBJECTS_SUBDIRS_INDEX] =
            dir_names.toString();
        options.optvar_vals[MakeMakeOptions.EXPLIB_SUBDIRS_INDEX] =
            dir_names.toString();
        options.optvar_vals[MakeMakeOptions.SUBDIRS_INDEX] =
            dir_names.toString();
      }
    }

    if (options.autoskip && options.makefile_path.exists())
      options.optvar_vals = null; // skip this makefile
    else if (entire_subtree && entire_subtree_skip)
      options.optvar_vals = null; // skip this makefile
    else if (options.use_gui && options.verify && !entire_subtree)
    {
      got_data = true;
      verified = false;
      notify();
      try
      {
        while (!verified)
          wait(); 
        if (entire_subtree)
        {
          entire_subtree_start = dir.toString();
          if (options.optvar_vals == null)
            entire_subtree_skip = true;
          else
            entire_subtree_skip = false;
        }
      }
      catch (InterruptedException e)
      {
        return (false);
      }
    }

    if (options.optvar_vals != null) // don't skip this makefile
    {
      if (options.info)
      {
        StringBuffer msg = new StringBuffer();
        if (options.use_gui)
        {
          msg.append( "WOULD HAVE CREATED " ).append(
              options.makefile_name ).append( " IN " ).append(
              dir.toString() );
          if (options.process_subdirs)
            msg.append( " AND ALL SUBDIRECTORIES" );
        }
        else
        {
          msg.append( "MakeMake: Would have created " ).append(
              options.makefile_name ).append( " in " ).append(
              dir.toString() );
          if (options.process_subdirs)
            msg.append( " and all subdirectories" );
        }
        if (error_printer != null)
          error_printer.print( msg.toString() );
        else
          Interface.printAlways( msg.toString() );
        return (false);
      }

      if (options.backup && options.makefile_path.exists())
      {
        File backup_name = new File( options.makefile_path.toString() +
            MakeMakeOptions.DEFAULT_MAKEFILE_BACKUP_SUFF );
        backup_name.delete();
        options.makefile_path.renameTo( backup_name );
      }

      PrintWriter pw = Path.openFileWriter( options.makefile_path.toString(),
          false, true );

      if (pw == null)
      {
        String msg;
        if (options.use_gui)
          msg = "CAN'T CREATE " + options.makefile_path.toString();
        else
          msg = "MakeMake: Can't create " +
              options.makefile_path.toString() + " (skipping).";
        if (error_printer != null)
          error_printer.print( msg );
        else
          Interface.printWarning( msg );
        return (false);
      }
 
      MakeMakePrintWriter outfile = new MakeMakePrintWriter( pw );

      writeInfo( outfile, options );
      outfile.close();
    }

    // RECURSION
    if (dirs != null && options.process_subdirs)
    {
      for (int i = 0; i < dirs.length; ++i)
        if (!createMakefiles( new File( dir + File.separator + dirs[i] ) ))
          return (false);
    }

    if (dir.toString().equals( entire_subtree_start )) // done with subtree
      entire_subtree = false;

    return (true);
  }


  /**
   * this will return only when createMakefiles is wait()ing.
  **/
  public synchronized boolean waitForFirstDir()
  {
    try
    {
      while (!got_data && !done)
        wait(); 
    }
    catch (InterruptedException e)
    {
      return (false);
    }
    return (!done);
  }


  public synchronized boolean moreDirs( boolean entire_subtree )
  {
    if (done)
      return (false);
    else
    {
      verified = true;
      got_data = false;
      this.entire_subtree = entire_subtree;
      notify();
      try
      {
        while (!got_data && !done)
          wait(); 
        if (done)
          return (false);
      }
      catch (InterruptedException e)
      {
        return (false);
      }
      return (true);
    }
  }


  public static void writeInfo( MakeMakeOutputable outfile,
      MakeMakeOptions options )
  {
    // write out the intro header comment
    outfile.println( "#" );
    outfile.print( "# ODE makefile (" );
    outfile.print( options.makefile_path.toString() );
    outfile.println( ")" );
    outfile.println( "# Generated by MakeMake (" +
        Version.getOdeVersionNumber() + " " +
        Version.getOdeLevelName() + ")" );
    outfile.println( "#" );
    outfile.println( "" );

    // write out the main variables
    for (int i = 0; i < options.optvar_names.length; ++i)
    {
      if (options.optvar_vals[i].length() > 0)
      {
        outfile.print( "# " );
        outfile.println( options.optvar_comments[i] );
        outfile.print( options.optvar_names[i] );
        outfile.print( " " );
        outfile.print( options.optvar_assignment[i] );
        outfile.print( " " );
        writeWithContinuation( outfile, options.optvar_vals[i],
            options.optvar_names[i].length() + 4 + 1 );
        outfile.println( "" );
      }
    }

    // write out the custom variables that don't contain
    // a variable expansion in their name
    writeCustomInfo( outfile, options, true );

    // write out the .include line to get the ODE rules
    outfile.println( "# include the ODE rules" );
    outfile.println( INCLUDE_RULES_TEXT );
    outfile.println( "" );

    // write out the target-specific variables
    String name, val;
    for (int i = 0; i < options.targvar_names.size(); ++i)
    {
      name = (String)options.targvar_names.elementAt( i );
      val = (String)options.targvar_vals.elementAt( i );
      if (val.length() > 0)
      {
        outfile.print( name );
        outfile.print( " = " );
        writeWithContinuation( outfile, val, name.length() + 4 + 1 );
        outfile.println( "" );
      }
    }

    // write out the custom variables that contain
    // a variable expansion in their name
    writeCustomInfo( outfile, options, false );
  }


  private static void writeCustomInfo( MakeMakeOutputable outfile,
      MakeMakeOptions options, boolean pre_include_vars )
  {
    String var, val, comment;
    boolean write_info;

    if (options.customvar_names == null)
      return;

    for (int i = 0; i < options.customvar_names.size(); ++i)
    {
      var = (String)options.customvar_names.elementAt( i );
      val = (String)options.customvar_vals.elementAt( i );
      comment = (String)options.customvar_comments.elementAt( i );
      write_info = ((Boolean)options.customvar_mfinclude.elementAt(
          i )).booleanValue();
      if (var.indexOf( "$" ) >= 0) // does name need expansion?
      {
        if (pre_include_vars)
          write_info = false;
      }
      else
      {
        if (!pre_include_vars)
          write_info = false;
      }
      if (write_info)
      {
        if (comment.length() > 0)
        {
          outfile.print( "# " );
          outfile.println( comment );
        }
        outfile.print( var );
        outfile.print( " " +
            MakeMakeOptions.ASSIGNMENT_TYPES[
                ((Integer)options.customvar_assignment.elementAt(
                    i )).intValue()] +
            " " );
        writeWithContinuation( outfile, val, var.length() + 4 + 1 );
        outfile.println( "" );
      }
    }
  }


  private static void writeWithContinuation( MakeMakeOutputable outfile,
      String string, int start_column )
  {
    final int MAX_COLUMN = 75;
    String[] words = StringTools.split( string, ' ' );
    int column = start_column;

    for (int i = 0; i < words.length; ++i)
    {
      if (words[i].length() + column > MAX_COLUMN)
      {
        outfile.println( "\\" );
        outfile.print( "     " );
        column = 3;
      }
      outfile.print( words[i] );
      outfile.print( " " );
      column += words[i].length() + 1;
    }
    outfile.println( "" );
  }


  /**
   * Check each file to see if it's a known source file.  If so,
   * place it in an appropriate variable with the proper target
   * extension.
   *
   * @param dir The directory to process.
   * @param options The options that will control processing.
  **/
  private static void processFiles( File dir, MakeMakeOptions options )
  {
    int i, j;
    StringBuffer java_files = new StringBuffer(),
        c_files = new StringBuffer(),
        h_files = new StringBuffer(),
        unknown_files = new StringBuffer();
    String[] files = Path.getDirContents( dir, false, Path.CONTENTS_FILES );

    if (files == null)
      return;

    for (i = 0; i < files.length; ++i)
    {
      for (j = 0; j < options.java_suffs.size(); ++j)
        if (files[i].endsWith( (String)options.java_suffs.elementAt( j ) ))
        {
          java_files.append( files[i].substring( 0,
              files[i].length() - ((String)options.java_suffs.elementAt(
              j )).length() ) ).append( CLASS_SUFF ).append( " " );
          break;
        }
      if (j < options.java_suffs.size()) // found a match already
        continue;
      for (j = 0; j < options.obj_suffs.size(); ++j)
        if (files[i].endsWith( (String)options.obj_suffs.elementAt( j ) ))
        {
          c_files.append( files[i].substring(
              0, files[i].length() - ((String)options.obj_suffs.elementAt(
              j )).length() ) ).append( OBJ_SUFF ).append( " " );
          break;
        }
      if (j < options.obj_suffs.size()) // found a match already
        continue;
      for (j = 0; j < options.hdr_suffs.size(); ++j)
        if (files[i].endsWith( (String)options.hdr_suffs.elementAt( j ) ))
        {
          h_files.append( files[i] ).append( " " );
          break;
        }
      if (j < options.hdr_suffs.size()) // found a match already
        continue;
      unknown_files.append( files[i] ).append( " " );
    }

    if (java_files.length() > 0)
      options.optvar_vals[MakeMakeOptions.JAVA_CLASSES_INDEX] =
          java_files + " ";

    if (c_files.length() > 0)
    {
      if (options.use_ofiles)
        options.optvar_vals[MakeMakeOptions.OFILES_INDEX] = c_files + " ";
      else
        options.optvar_vals[MakeMakeOptions.OBJECTS_INDEX] = c_files + " ";
    }

    if (h_files.length() > 0)
    {
      options.optvar_vals[MakeMakeOptions.INCLUDES_INDEX] = h_files + " ";
      options.optvar_vals[MakeMakeOptions.EXPINCDIR_INDEX] =
          MakeMakeOptions.DEFAULT_EXPINCDIR;
      options.optvar_vals[MakeMakeOptions.EXPINCTOP_INDEX] =
          MakeMakeOptions.DEFAULT_EXPINCTOP;
    }

    if (unknown_files.length() > 0)
    {
      options.optvar_vals[MakeMakeOptions.UNKNOWN_FILES_INDEX] =
          unknown_files + " ";
    }
  }
}
