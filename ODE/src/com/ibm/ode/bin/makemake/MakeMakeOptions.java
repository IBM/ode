package com.ibm.ode.bin.makemake;

import java.awt.Color;
import java.io.File;
import java.util.Vector;

public class MakeMakeOptions
{
  public static Color OK_COND = Color.green;
  public static Color PROGRESS_COND = Color.yellow;
  public static Color ERROR_COND = Color.red;
  public static final int QUIET_VERBOSITY = 0;
  public static final int NORMAL_VERBOSITY = 1;
  public static final int VERBOSE_VERBOSITY = 2;
  public static final int DEBUG_VERBOSITY = 3;
  public static final int NOVICE_GUI = 0;
  public static final int INTERMEDIATE_GUI = 1;
  public static final int ADVANCED_GUI = 2;
  public static final String[] GUI_LEVELS = { "Novice", "Intermediate",
      "Advanced" };
  // optvar indices where each gui level ends
  public static final int[] GUI_LEVEL_OPTVAR_LIMITS = { 13, 23, 37 };
  public static final String DEFAULT_MAKEFILE_NAME = "makefile.ode";
  public static final String DEFAULT_MAKEFILE_BACKUP_SUFF = ".bak";
  public static final String DEFAULT_IDIR = "/bin/";
  public static final String DEFAULT_EXPDIR = "/lib/";
  public static final String DEFAULT_EXPINCDIR = "/include/";
  public static final String DEFAULT_EXPLIBTOP = "../";
  public static final String DEFAULT_EXPINCTOP = "../";
  public static final int DEFAULT_ASSIGNMENT_TYPE_INDEX = 1; // "+="
  public static final String[] ASSIGNMENT_TYPES = {
      "=", "+=", "?=", ":=", "%=", "!=" };
  public static final String[] optvar_names = {
      "PROGRAMS", "LIBRARIES", "SHARED_LIBRARIES",
      "OBJECTS", "OFILES", "JAVA_CLASSES",
      "INCLUDES", "EXPLIB_TARGETS",
      "EXPINCDIR", "EXPDIR", /* use "=" instead of "+=" */
      "EXPINCTOP", "EXPLIBTOP", /* use "=" instead of "+=" */
      "ILIST",
      "IDIR", /* use "=" instead of "+=" */
      // intermediate level starts here
      "EXPINC_SUBDIRS", "OBJECTS_SUBDIRS", "EXPLIB_SUBDIRS", "SUBDIRS",
      "CFLAGS", "LDFLAGS", "SHLDFLAGS",
      "LIBS", "EXTRA_LIBS", "EXTRA_OFILES",
      // advanced level starts here
      "CCFAMILY", "CCTYPE", "CCVERSION", /* use "=" instead of "+=" */
      "LIB_SUFF", /* use "=" instead of "+=" */
      "INCFLAGS", "LIBFLAGS",
      "JAVA_PACKAGE_NAME", /* use "=" instead of "+=" */
      "JAR_LIBRARIES", "JAR_OBJECTS", "OTHER_JAR_OBJECTS",
      "JAR_RUNDIR", /* use "=" instead of "+=" */
      "JFLAGS", "JARFLAGS", "UNKNOWN_FILES" };
  public static final String[] optvar_assignment = {
      "+=", "+=", "+=",
      "+=", "+=", "+=",
      "+=", "+=",
      "=", "=", /* EXPINCDIR, EXPDIR */
      "=", "=", /* EXPINCTOP, EXPLIBTOP */
      "+=",
      "=", /* IDIR */
      // intermediate level starts here
      "+=", "+=", "+=", "+=",
      "+=", "+=", "+=",
      "+=", "+=", "+=",
      // advanced level starts here
      "=", "=", "=", /* CCFAMILY, CCTYPE, CCVERSION */
      "=", /* LIB_SUFF */
      "+=", "+=",
      "=", /* JAVA_PACKAGE_NAME */
      "+=", "+=", "+=",
      "=", /* JAR_RUNDIR */
      "+=", "+=", "+=" };
  public static final String[] optvar_comments = {
      "the executable programs to create",
      "the archive libraries to create",
      "the shared libraries to create",
      "the object files (as targets)",
      "the object files (as dependents)",
      "the Java class files to generate",
      "the headers to export",
      "the libraries to export",
      "the header export directory",
      "the library export directory",
      "the path from EXPINCDIR to the CONTEXT-specific dir",
      "the path from EXPDIR to the CONTEXT-specific dir",
      "the targets to be installed",
      "the install directory",
      "the subdirs to traverse for the EXPINC pass",
      "the subdirs to traverse for the OBJECTS pass", 
      "the subdirs to traverse for the EXPLIB pass", 
      "the subdirs to traverse for the STANDARD pass",
      "the compiler flags",
      "the linker flags for executable creation",
      "the linker flags for shared library creation",
      "the libraries to depend on",
      "additional libraries to give to the link command",
      "additional objects to give to the link command",
      "the compiler family (e.g., native)",
      "the compiler type (e.g., cpp)",
      "the compiler version (as allowed by the rules)",
      "the default library suffix",
      "the include flags for the compiler (e.g., -I/tmp)",
      "the library flags for the linker (e.g., -L/tmp)",
      "the Java package name (e.g., COM.ibm.proj)",
      "the Java .jar libraries to create",
      "the dependents of the .jar libraries",
      "additional non-dependent components of the .jar libraries",
      "the directory in which the jar command is run",
      "the flags for the javac command",
      "the flags for the jar command",
      "the unrecognized files in this directory (not used)" };
  // indices into the optvar_names array
  public static final int PROGRAMS_INDEX = 0;
  public static final int LIBRARIES_INDEX = 1;
  public static final int SHARED_LIBRARIES_INDEX = 2;
  public static final int OBJECTS_INDEX = 3;
  public static final int OFILES_INDEX = 4;
  public static final int JAVA_CLASSES_INDEX = 5;
  public static final int INCLUDES_INDEX = 6;
  public static final int EXPLIB_TARGETS_INDEX = 7;
  public static final int EXPINCDIR_INDEX = 8;
  public static final int EXPDIR_INDEX = 9;
  public static final int EXPINCTOP_INDEX = 10;
  public static final int EXPLIBTOP_INDEX = 11;
  public static final int ILIST_INDEX = 12;
  public static final int IDIR_INDEX = 13;
  public static final int EXPINC_SUBDIRS_INDEX = 14;
  public static final int OBJECTS_SUBDIRS_INDEX = 15;
  public static final int EXPLIB_SUBDIRS_INDEX = 16;
  public static final int SUBDIRS_INDEX = 17;
  public static final int CFLAGS_INDEX = 18;
  public static final int LDFLAGS_INDEX = 19;
  public static final int SHLDFLAGS_INDEX = 20;
  public static final int LIBS_INDEX = 21;
  public static final int EXTRA_LIBS_INDEX = 22;
  public static final int EXTRA_OFILES_INDEX = 23;
  public static final int LIB_SUFF_INDEX = 27;
  public static final int UNKNOWN_FILES_INDEX = 37;

  public boolean use_gui, process_subdirs, backup, use_ofiles, verify, autoskip;
  public boolean info; // ODE options
  public boolean success; // multi-use boolean for success (like errno in C)
  public int verbosity, gui_level;
  public File start_dir, makefile_path;
  public String makefile_name, available_subdirs;
  public String[] optvar_vals;
  public Vector obj_suffs, hdr_suffs, java_suffs;
  public Vector targvar_names, targvar_vals;
  public Vector customvar_names, customvar_vals, customvar_comments,
      customvar_assignment, customvar_mfinclude;

  public MakeMakeOptions()
  {
    use_gui = false;
    process_subdirs = false;
    backup = true;
    use_ofiles = false;
    autoskip = false;
    verify = true;
    info = false;
    verbosity = NORMAL_VERBOSITY;
    gui_level = NOVICE_GUI;
    start_dir = null;
    makefile_name = null;
    available_subdirs = null;
    makefile_path = null;
    optvar_vals = new String[optvar_names.length];
    obj_suffs = new Vector( 10 ); // allow for some growth
    hdr_suffs = new Vector( 5 ); // allow for some growth
    java_suffs = new Vector( 2 ); // allow for some growth
    customvar_names = new Vector();
    customvar_vals = new Vector();
    customvar_comments = new Vector();
    customvar_assignment = new Vector();
    customvar_mfinclude = new Vector();
  }
}
