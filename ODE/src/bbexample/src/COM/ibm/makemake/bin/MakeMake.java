package COM.ibm.makemake.bin;

import java.io.*;

import COM.ibm.makemake.lib.Path;
import COM.ibm.makemake.lib.CommandLine;
import COM.ibm.makemake.lib.Interface;
import COM.ibm.makemake.lib.UsagePrintable;

/**
 * This tool is used to generate skeleton makefiles
 * for a sandbox's source tree.  It looks for source
 * files (.c, .cpp, .java, etc.) in the specified
 * directory (and all subdirectories if -sub is given).
 * It puts these files in typical variables (OBJECTS,
 * JAVA_CLASSES, etc.) in the makefile it generates.
 *
 * Options include -clobber, which overwrites makefiles
 * if they already exist (otherwise it appends), -sub,
 * which processes all subdirectories of the specified
 * starting directory, and -mfname, which allows the
 * user to specify an alternate name for the makefiles
 * created (the default is "makefile").
**/
public class MakeMake implements UsagePrintable
{
  private static final String INCLUDE_RULES_TEXT = ".include <${RULES_MK}>";
  private static final String MAKEFILE_DEFAULT_NAME = "makefile";
  private static final String CLASS_SUFF = "${CLASS_SUFF}";
  private static final String OBJ_SUFF = "${OBJ_SUFF}";

  private static CommandLine command_line;
  private static String makefile_name;
  private static File start_dir;
  private static boolean process_subdirs, clobber;


  /**
   * Program entry point.
   *
   * @param args The arguments passed to the MakeMake command.
  **/
  public static void main( String[] args )
  {
    processCmdline( args );
    createMakefiles( start_dir );
  }


  /**
   * Parse and evaluate the command line arguments.
   *
   * @param args The arguments passed to the MakeMake command.
  **/
  private static void processCmdline( String[] args )
  {
    String[] states = { "-sub", "-clobber",
        "-info", "-quiet", "-verbose", "-debug" };
    String[] qual_vars = { "-mfname" };

    command_line = new CommandLine( states, qual_vars, true, args,
        true, new MakeMake() );
    String[] unqual_vars = command_line.getUnqualifiedVariables();
    if (unqual_vars == null)
      Interface.quitWithErrMsg( "MakeMake: must supply a path.", 1 );
    start_dir = new File( unqual_vars[0] );
    if (!start_dir.isDirectory())
      Interface.quitWithErrMsg( "MakeMake: path must be an existing " +
          "directory.", 1 );
    if (command_line.isState( "-info" ))
    {
      Interface.printAlways( "Would generate makefiles in all subdirectories" +
          " of " + start_dir );
      Interface.quit( 0 );
    }
    makefile_name = command_line.getQualifiedVariable( "-mfname", 1 );
    if (makefile_name == null)
      makefile_name = MAKEFILE_DEFAULT_NAME;
    process_subdirs = command_line.isState( "-sub" );
    clobber = command_line.isState( "-clobber" );
  }


  /**
   * Recursive function which processes each directory's
   * contents.  Files are handled by another function.
   *
   * @param dir The directory to process.  Assumed to be
   * non-null.
  **/
  private static void createMakefiles( File dir )
  {
    String makefile_path = dir.getPath() + File.separator + makefile_name;
    PrintWriter outfile = Path.openFileWriter( makefile_path,
        !clobber, true );

    if (process_subdirs)
    {
      String[] dirs = Path.getDirContents( dir, false, Path.CONTENTS_DIRS );

      if (dirs != null)
      {
        StringBuffer dir_names = new StringBuffer();
        for (int i = 0; i < dirs.length; ++i)
        {
          dir_names.append( dirs[i] ).append( " " );
          createMakefiles( new File( dir + File.separator + dirs[i] ) );
        }
        if (outfile != null)
        {
          outfile.println( "# subdirectories to traverse for OBJECTS pass" );
          outfile.println( "OBJECTS_SUBDIRS = " + dir_names );
          outfile.println();
          outfile.println( "# subdirectories to traverse for STANDARD pass" );
          outfile.println( "SUBDIRS = " + dir_names );
          outfile.println();
        }
      }
    }

    if (outfile == null)
    {
      Interface.printWarning( "MakeMake: Couldn't open " + makefile_path +
          " for writing (skipping)." );
    }
    else
    {
      String[] files = Path.getDirContents( dir, false, Path.CONTENTS_FILES );
      outfile.println( "# programs to build, of the form " +
          "progname${PROG_SUFF}" );
      outfile.println( "PROGRAMS = " );
      outfile.println();
      outfile.println( "# libraries to build, of the form " +
          "libname${LIB_SUFF}" );
      outfile.println( "LIBRARIES = " );
      outfile.println();
      if (files != null)
        processFiles( outfile, files );
      outfile.println( INCLUDE_RULES_TEXT );
      outfile.close();
    }
  }


  /**
   * Check each file to see if it's a known source file.  If so,
   * place it in an appropriate variable with the proper target
   * extension.
   *
   * @param outfile The makefile handle where the output is written.
   * Assumed to be non-null.
   * @param files The array of files to process.  Assumed to be
   * non-null.
  **/
  private static void processFiles( PrintWriter outfile, String[] files )
  {
    StringBuffer java_files = new StringBuffer(), c_files = new StringBuffer();

    for (int i = 0; i < files.length; ++i)
    {
      if (files[i].endsWith( ".java" ))
        java_files.append( files[i].substring(
            0, files[i].length() - 5 ) ).append( CLASS_SUFF ).append( " " );
      else if (files[i].endsWith( ".c" ) || files[i].endsWith( ".C" ))
        c_files.append( files[i].substring(
            0, files[i].length() - 2 ) ).append( OBJ_SUFF ).append( " " );
      else if (files[i].endsWith( ".cc" ))
        c_files.append( files[i].substring(
            0, files[i].length() - 3 ) ).append( OBJ_SUFF ).append( " " );
      else if (files[i].endsWith( ".cpp" ))
        c_files.append( files[i].substring(
            0, files[i].length() - 4 ) ).append( OBJ_SUFF ).append( " " );
    }
    outfile.println( "# object files to build, of the form " +
        "objname${OBJ_SUFF}" );
    outfile.println( "OBJECTS = " + c_files );
    outfile.println();
    outfile.println( "# Java class files to build, of the form " +
        "javaname${CLASS_SUFF}" );
    outfile.println( "JAVA_CLASSES = " + java_files );
    outfile.println();
  }


  /**
   * The usage message function that satisfies the UsagePrintable
   * interface.  Typically called by CommandLine.
  **/
  public void printUsage()
  {
    Interface.printAlways( "Usage: MakeMake [options] <path>" );
    Interface.printAlways( "       path: The root directory of the source " +
        "tree." );
    Interface.printAlways( "       options:" );
    Interface.printAlways( "           -mfname <makefile name>" );
    Interface.printAlways( "           -sub" );
    Interface.printAlways( "           -clobber" );
    Interface.printAlways( "           -info -usage -version -rev" );
  }
}
