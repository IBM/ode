#########################################
#
# Mkdeptest module
# - module to run mkdeptest for different
#   test levels
#
#########################################

package Mkdeptest;

use File::Path;
use File::Copy;
use File::Basename;
use OdeEnv;
use OdeFile;
use OdeTest;
use OdeUtil;
use OdePath;
use OdeInterface;
use Cwd;
use strict;

# Defining these vars to be global to all 
#  subroutines in this file
my $file1_c;
my $file2_c;
my $file3_c;
my $stdio_h;
my $file6_c;
my $file7_c;
my $file1_c_path;
my $file2_c_path;
my $file3_c_path;
my $stdio_h_path;
my $file6_c_path;
my $file6_c_quoted;
my $file7_c_path;
my $file8_c;
my $ipath1;
my $ipath1_result;
my $usr_dir;
my $objectdir_abs_path;
my $makeobjdir_path;
my $makeobjdir_abs_path;

my $depend_mk;
my $depend_dir;
my $depend_subdir;
my $depend_dir_sb;
my $depend_dir_bb;
my $depend_dir_blank;
my $root_dir;
my $sb_dir;
my $src_dir;
my $file1_u;
my $file2_u;
my $file3_u;
my $file4_u;
my $file4_sb_u;
my $file6_u;
my $file1_o;
my $file1_o_abs;
my $file2_o;
my $file3_o;
my $file4_o;
my $file5_o;
my $file6_o;
my $file6_o_quoted;
my $file7_o;
my $file8_other;
my $file8_o;
my $file8_o_abs;
my $depsep = ":  ";    # delimiting string between target and source files
my $maketop = "\${MAKETOP}";
my $fakebottom = "\${FAKEBOTTOM}";

#########################################
#
# sub run
# - run test sequence based on testlevel
#
#########################################
sub run ()
{
  my $status;
  if ( $OdeUtil::arghash{'testlevel'} eq "normal" )
  {
    $status = normal();
    return( $status );
  }
  if ( $OdeUtil::arghash{'testlevel'} eq "regression" )
  {
    $status = regression();
    return( $status );
  }
  elsif ( $OdeUtil::arghash{'testlevel'} eq "fvt" )
  {
    $status = fvt();
    return( $status );
  }
  else
  {
    OdeInterface::logPrint( 
      "ERROR: undefined testlevel $OdeUtil::arghash{'testlevel'}\n" );
    return( 1 );
  }

}

#########################################
#
# sub normal
# - execute normal (minimal) set of tests
#
#########################################
sub normal ()
{
  my $command;
  my $status;
  my $error = 0;
  my $orig_dir = cwd();
  my ($dep1, $dep2, $dep3, $dep4);
  my $numlines = 10;  # 10 lines in depend.mk for full dependencies

  OdeInterface::logPrint( "in Mkdeptest - test level is normal\n");
  OdeEnv::runOdeCommand( "mkdep -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

  # Create temporary source files
  if (createFiles() || (!-f $file1_u) || (!-f $file2_u)) 
  {
    OdeInterface::printError("mkdep : Error creating test files");
    return( 1 );
  }

  my @delete_list = ($depend_mk);
  # remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  #   cd to depend_dir
  if (! OdePath::chgdir( $depend_dir ) )
  {
    OdeInterface::printError( "mkdep : Could not cd to $depend_dir: $!" );
    return( 1 );
  }

  # Test Case #1 - normal mkdep usage
  $dep1 = OdePath::unixize( $file1_o . $depsep . $file1_c );
  $dep2 = OdePath::unixize( $file1_o . $depsep . $file2_c );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $file3_c );
  $dep4 = OdePath::unixize( $file3_o . $depsep . $stdio_h );
  $command = "mkdep . >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ( $status  ||
     ( ! -f $depend_mk ) ||
     ( !OdeFile::findInFile( $dep1, $depend_mk, 1 )) ||
     ( !OdeFile::findInFile( $dep2, $depend_mk, 1 )) ||
     ( !OdeFile::findInFile( $dep3, $depend_mk, 1 )) ||
     ( !OdeFile::findInFile( $dep4, $depend_mk, 1 )) ||
     ( OdeFile::numLinesInFile( $depend_mk ) != $numlines )
     )
  {
    # Test #1 failed
    OdeInterface::printResult( "fail", "normal", "mkdep", 1, "mkdep ." );
    $error = 1;
  }
  else
  {
    # Test #1 passed
    OdeInterface::printResult( "pass", "normal", "mkdep", 1, "mkdep ." );
  }
  
  OdeFile::deltree( \@delete_list );    

  # Test Case #2 - normal mkdep -top -rm -elxdep usage
  #   must first set BACKED_SANDBOXDIR environment variable
  $ENV{'BACKED_SANDBOXDIR'} = $sb_dir;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $maketop . $file1_c_path );
  $dep2 = OdePath::unixize( 
                    $file1_o . $depsep . $maketop . "../" . $file2_c_path );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $maketop . $file3_c_path );

  $command = "mkdep -elxdep -rm -top . >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ( $status  ||
     ( ! -f $depend_mk ) ||
     ( -f $file1_u ) ||
     ( !OdeFile::findInFile( $dep1, $depend_mk, 1 )) ||
     ( !OdeFile::findInFile( $dep2, $depend_mk, 1 )) ||
     ( !OdeFile::findInFile( $dep3, $depend_mk, 1 )) ||
     ( OdeFile::numLinesInFile( $depend_mk ) != ($numlines - 1)) 
     )
  {
    # Test #2 failed
    OdeInterface::printResult( "fail", "normal", "mkdep", 2, 
                               "mkdep -elxdep -rm -top ." );
    $error = 1;
  }
  else
  {
    # Test #2 passed
    OdeInterface::printResult( "pass", "normal", "mkdep", 2, 
                               "mkdep -elxdep -rm -top ." );
  }
  
  OdeFile::deltree( \@delete_list );    

  # Unset BACKED_SANDBOXDIR
  $ENV{'BACKED_SANDBOXDIR'} = "";

  OdePath::chgdir( $orig_dir );

  return( $error );
}

#########################################
#
# sub regression
# - execute regresssion set of tests
#
#########################################
sub regression ()
{
  my $command;
  my $status;
  my $error = 0;
  my $reg_error = 0;
  my $error_txt;
  my $tmpstr;
  my $orig_dir = cwd();
  my ($dep1, $dep2, $dep3, $dep4, $dep5, $dep6);
  my $numlines = 10;  # 10 lines in depend.mk for full dependencies
  # 16 lines in depend.mk if also using .u file in subdir
  my $numlines_with_subdir = 16;
  my $numlines_filename_blanks = 8;
  my $numlines_abs = 8;
  my $depend_new_mk;


  OdeInterface::logPrint( "in Mkdeptest - test level is regression\n");
  OdeEnv::runOdeCommand( "mkdep -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

  # Create temporary source files
  if (createFiles() || (!-f $file1_u) || (!-f $file2_u)) 
  {
    OdeInterface::printError("mkdep : Error creating test files");
    return( 1 );
  }

  #   cd to depend_dir
  if (! OdePath::chgdir( $depend_dir ) )
  {
    OdeInterface::printError( "mkdep : Could not cd to $depend_dir: $!" );
    return( 1 );
  }
  my @delete_list = ($depend_mk);

  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  #  must first set BACKED_SANDBOXDIR environment variable to establish
  #  the value of ${MAKETOP} for cases with -top and -elxdep
  $ENV{'BACKED_SANDBOXDIR'} = $sb_dir;

  ## Regression Test Case 1 - generate a depend.mk file
  # normal mkdep usage with no flags
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $file1_c );
  $dep2 = OdePath::unixize( $file1_o . $depsep . $file2_c );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $file3_c );
  $dep4 = OdePath::unixize( $file3_o . $depsep . $stdio_h );
  $tmpstr = "mkdep .";
  $command = "mkdep . >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists
  if (!$reg_error && (!-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk not found.";
  }
  # Check if "file1.u" exists
  if (!$reg_error && (!-f $file1_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file1_u removed without -rm flag";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (!OdeFile::findInFile( $dep2, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file3.c
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" not found in $depend_mk";
  }
  # Check for dependency line -> file3.o: stdio.h
  if (!$reg_error && (!OdeFile::findInFile( $dep4, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep4\" not found in $depend_mk";
  }
  # Should only be $numlines lines in depend.mk file
  if ( !$reg_error && (OdeFile::numLinesInFile( $depend_mk ) !=  $numlines ))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 1, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 1, $tmpstr );
  }
 
  # Remove existing files for next test case
  OdeFile::deltree( \@delete_list );

  ## Regression Test Case 2 - generate a depend.mk file with 
  # -elxdep, -rm, and -top flags
  #  Dependency with "stdio.h" should NOT be in the depend.mk file
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $maketop . $file1_c_path );
  $dep2 = OdePath::unixize( 
                    $file1_o . $depsep . $maketop . "../" . $file2_c_path );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $maketop . $file3_c_path );
  $tmpstr = "mkdep -top -elxdep .";
  $command = "mkdep -top -rm -elxdep . >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists
  if (!$reg_error && (!-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk not found.";
  }
  # Check if "file1.u" exists
  if (!$reg_error && (-f $file1_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file1_u not removed";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (!OdeFile::findInFile( $dep2, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" not found in $depend_mk";
  }
  # Check for dependency line -> file2.o: file3.c
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" not found in $depend_mk";
  }
  # Check for any reference to "stdio.h" in depend.mk - should not exist
  if (!$reg_error && (OdeFile::findInFile( "stdio.h", $depend_mk)))
  {
    $reg_error = 1;
    $error_txt = "Extra dependency relating to stdio.h found in $depend_mk";
  }
  # Should only be $numlines - 1 lines in depend.mk file
  if ( !$reg_error && 
       (OdeFile::numLinesInFile( $depend_mk ) != ( $numlines - 1 )))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 2, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 2, $tmpstr );
  }

  # Remove existing files for next test case
  OdeFile::deltree( \@delete_list );

  # Create temporary source files for next test case
  if (createFiles() || (!-f $file1_u) || (!-f $file2_u)) 
  {
    OdeInterface::logPrint("Error creating test files\n");
    return( 1 );
  }  
  
  ## Regression Test Case 3 - generate a depend.mk file with -file flag
  #  Output should be in file "depend.new.mk"
  $dep1 = OdePath::unixize( $file1_o . $depsep . $maketop . $file1_c_path );
  $dep2 = OdePath::unixize( 
                    $file1_o . $depsep . $maketop . "../" . $file2_c_path );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $maketop . $file3_c_path );
  $depend_new_mk = $depend_dir . $OdeEnv::dirsep . "depend.new.mk";
  @delete_list = ($depend_new_mk);
  $reg_error = 0;
  $tmpstr = "mkdep -top -elxdep -file .";
  $command = 
      "mkdep -top -elxdep -file $depend_new_mk . >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.new.mk" file exists
  if (!$reg_error && (!-f $depend_new_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_new_mk not found";
    system( "ls -al $depend_dir");
  }
  # Check if "depend.mk" file exists - should NOT
  if (!$reg_error && (-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk found";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $depend_new_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" not found in $depend_new_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (!OdeFile::findInFile( $dep2, $depend_new_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" not found in $depend_new_mk";
  }
  # Check for dependency line -> file2.o: file3.c
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $depend_new_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" not found in $depend_new_mk";
  }
  # Check for any reference to "stdio.h" in depend.new.mk - should not exist
  if (!$reg_error && (OdeFile::findInFile( "stdio.h", $depend_new_mk)))
  {
    $reg_error = 1;
    $error_txt = "Extra dependency relating to stdio.h found in $depend_new_mk";
  }
  # Should only be $numlines - 1 lines in depend.new.mk file
  if ( !$reg_error && 
       (OdeFile::numLinesInFile( $depend_new_mk ) != ( $numlines - 1 )))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_new_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 3, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 3, $tmpstr );
  }

  # Remove existing files for next test case
  OdeFile::deltree( \@delete_list );

  ## Regression Test Case 4 - use multiple directories
  # specify multiple directories for which to generate a depend.mk file
  # and verify all dependenices were properly created
  @delete_list = ($depend_mk);
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $file1_c );
  $dep2 = OdePath::unixize( $file1_o . $depsep . $file2_c );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $file3_c );
  $dep4 = OdePath::unixize( $file3_o . $depsep . $stdio_h );
  $dep5 = OdePath::unixize( $file4_o . $depsep . $file1_c );
  $dep6 = OdePath::unixize( $file5_o . $depsep . $stdio_h );

  $tmpstr = "mkdep . subdir";
  $command = "mkdep . subdir >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists
  if (!$reg_error && (!-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk not found.";
  }
  # Check if "file1.u" exists
  if (!$reg_error && (!-f $file1_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file1_u removed without -rm flag";
  }
  # Check if "file2.u" exists
  if (!$reg_error && (!-f $file2_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file2_u removed without -rm flag";
  }  
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (!OdeFile::findInFile( $dep2, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" not found in $depend_mk";
  }
  # Check for dependency line -> file2.o: file3.c
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" not found in $depend_mk";
  }
  # Check for dependency line -> file3.o: stdio.h
  if (!$reg_error && (!OdeFile::findInFile( $dep4, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep4\" not found in $depend_mk";
  }
  # Check for dependency line -> file4.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep5, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep5\" not found in $depend_mk";
  }
  # Check for dependency line -> file5.o: stdio.h
  if (!$reg_error && (!OdeFile::findInFile( $dep6, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep6\" not found in $depend_mk";
  }
  # Should only be $numlines_with_subdir lines in depend.new.mk file
  if ( !$reg_error && 
       (OdeFile::numLinesInFile( $depend_mk ) != ( $numlines_with_subdir )))
  { 
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_new_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 4, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 4, $tmpstr );
  }

  # Remove existing files for next test case
  OdeFile::deltree( \@delete_list );

  ## Regression Test Case 5 - test sandbox merge functionality
  # from a pseudo-sandbox (defined by specifying BACKED_SANDBOXDIR)
  # modify one of the .u files used to generate the backing build's
  # depend.mk and regenerate the depend.mk in the sandbox dir.  The
  # new depend.mk should contain both the dependencies from the sandbox
  # .u file, as well as the second, unmodified, .u file from the 
  # backing build.
  $reg_error = 0;
  $dep1 = "file1.o" . $depsep . "file1.c";
  $dep2 = "file1.o" . $depsep . "file1.h";
  $dep3 = "file2.o" . $depsep . "file2.c";
  $dep4 = "file2.o" . $depsep . "file2.h";

  # specify BACKED_SANDBOXDIR
  $ENV{'BACKED_SANDBOXDIR'} = 
                   $depend_dir_sb . $OdeEnv::pathsep . $depend_dir_bb;
  # cd to backing build directory
  OdePath::chgdir( $depend_dir_bb );
  # create backing build version of depend.mk
  $tmpstr = "mkdep (test sandbox merge functionality)";
  $command = "mkdep -rm . >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists and all deps were generated, but not dep4
  if ( !$reg_error && 
       ( (!-f "depend.mk") ||
       (!OdeFile::findInFile( $dep1, "depend.mk", 1)) ||
       (!OdeFile::findInFile( $dep2, "depend.mk", 1)) ||
       (!OdeFile::findInFile( $dep3, "depend.mk", 1)) ||
       (OdeFile::findInFile( $dep4, "depend.mk", 1)) )
     )
  {
    $reg_error = 1;
    $error_txt = "Error creating backing build depend.mk";
  }
  # now cd to sandbox dir
  if ( !$reg_error ) { OdePath::chgdir( $depend_dir_sb ) };
  # create sandbox version of depend.mk - have a different file2.u in this dir
  $command = "mkdep . >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  if ( !$reg_error ) { $status = OdeEnv::runOdeCommand( $command ); }
  if ( !$reg_error && $status )
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists and all deps were generated, including dep4
  if ( !$reg_error && 
       ( (!-f "depend.mk") ||
       (!OdeFile::findInFile( $dep1, "depend.mk", 1)) ||
       (!OdeFile::findInFile( $dep2, "depend.mk", 1)) ||
       (!OdeFile::findInFile( $dep3, "depend.mk", 1)) ||
       (!OdeFile::findInFile( $dep4, "depend.mk", 1)) ) )
  {
    $reg_error = 1;
    $error_txt = "Error creating sandbox depend.mk";
  }  
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 5, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 5, $tmpstr );
  }
  # Cd back to depend_dir
  OdePath::chgdir( $depend_dir );

  ## Regression Test Case 6 - test for non-zero exit code
  # force mkdep to fail and check if a non-zero exit code was supplied
  $reg_error = 0;
  $command = "mkdep -badflag . >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mkdep <invalid flag>";
  $status = OdeEnv::runOdeCommand( $command );
  # Check command exit code
  if ($status == 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a zero exit code after an error";
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 6, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 6, $tmpstr );
  }

  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  #  set BACKED_SANDBOXDIR environment variable to establish
  #  the value of ${MAKETOP} for cases with -top and -elxdep
  $ENV{'BACKED_SANDBOXDIR'} = $sb_dir;

  ## Regression Test Case 7 - generate a depend.mk file
  # mkdep -Kpath -elxdep
  # Eliminates all extra dependencies except for those with path prefix.
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $file1_c );
  $dep2 = OdePath::unixize( $file1_o . $depsep . $file2_c );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $file3_c );
  $dep4 = OdePath::unixize( $file3_o . $depsep . $stdio_h );
  $tmpstr = "mkdep . -exldep -K";
  $command = "mkdep . -elxdep -K$src_dir >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists
  if (!$reg_error && (!-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk not found.";
  }
  # Check if "file1.u" exists
  if (!$reg_error && (!-f $file1_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file1_u removed without -rm flag";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (OdeFile::findInFile( $dep2, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file3.c
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" not found in $depend_mk";
  }
  # Check for dependency line -> file3.o: stdio.h
  if (!$reg_error && (OdeFile::findInFile( $dep4, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep4\" found in $depend_mk";
  }
  # Should only be $numlines lines in depend.mk file
  if ( !$reg_error && (OdeFile::numLinesInFile( $depend_mk ) !=  $numlines - 2 ))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 7, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 7, $tmpstr );
  }

  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );
 

  ## Regression Test Case 8 - generate a depend.mk file
  # mkdep -Ipath
  # Keeps all dependencies and shortens those with path prefix.
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $file1_c_path );
  $dep2 = OdePath::unixize( $file1_o . $depsep . $file2_c );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $file3_c_path );
  $dep4 = OdePath::unixize( $file3_o . $depsep . $stdio_h );
  $tmpstr = "mkdep . -I";
  $command = "mkdep . -I$src_dir >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists
  if (!$reg_error && (!-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk not found.";
  }
  # Check if "file1.u" exists
  if (!$reg_error && (!-f $file1_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file1_u removed without -rm flag";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (!OdeFile::findInFile( $dep2, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file3.c
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" not found in $depend_mk";
  }
  # Check for dependency line -> file3.o: stdio.h
  if (!$reg_error && (!OdeFile::findInFile( $dep4, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep4\" not found in $depend_mk";
  }
  # Should only be $numlines lines in depend.mk file
  if ( !$reg_error && (OdeFile::numLinesInFile( $depend_mk ) !=  $numlines ))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 8, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 8, $tmpstr );
  }

  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );
 

  ## Regression Test Case 9 - generate a depend.mk file
  # mkdep -Ipath -elxdep
  # Removes all dependencies except for those with path prefix that are
  # shortened by path.
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $file1_c_path );
  $dep2 = OdePath::unixize( $file1_o . $depsep . $file2_c );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $file3_c_path );
  $dep4 = OdePath::unixize( $file3_o . $depsep . $stdio_h );
  $tmpstr = "mkdep . -I -elxdep";
  $command = "mkdep . -I$src_dir -elxdep >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists
  if (!$reg_error && (!-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk not found.";
  }
  # Check if "file1.u" exists
  if (!$reg_error && (!-f $file1_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file1_u removed without -rm flag";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (OdeFile::findInFile( $dep2, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file3.c
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" not found in $depend_mk";
  }
  # Check for dependency line -> file3.o: stdio.h
  if (!$reg_error && (OdeFile::findInFile( $dep4, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep4\" found in $depend_mk";
  }
  # Should only be $numlines lines in depend.mk file
  if ( !$reg_error && (OdeFile::numLinesInFile( $depend_mk ) !=  $numlines - 2 ))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 9, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 9, $tmpstr );
  }

  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );
 

  ## Regression Test Case 10 - generate a depend.mk file
  # mkdep -Kpath1 -Ipath2 -elxdep
  # These options will keep dependencies prefixed by path1 and shorten those 
  # with a path2 prefix.
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $ipath1_result );
  $dep2 = OdePath::unixize( $file1_o . $depsep . $file2_c );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $file3_c );
  $dep4 = OdePath::unixize( $file3_o . $depsep . $stdio_h );
  $tmpstr = "mkdep . -K -I -elxdep";
  $command = "mkdep . -K$src_dir -I$ipath1 -elxdep >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists
  if (!$reg_error && (!-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk not found.";
  }
  # Check if "file1.u" exists
  if (!$reg_error && (!-f $file1_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file1_u removed without -rm flag";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (OdeFile::findInFile( $dep2, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file3.c
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" not found in $depend_mk";
  }
  # Check for dependency line -> file3.o: stdio.h
  if (!$reg_error && (OdeFile::findInFile( $dep4, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep4\" found in $depend_mk";
  }
  # Should only be $numlines lines in depend.mk file
  if ( !$reg_error && (OdeFile::numLinesInFile( $depend_mk ) !=  $numlines - 2 ))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 10, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 10, $tmpstr );
  }

  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );

 
  ## Regression Test Case 11 - generate a depend.mk file
  # test mkdep with -E flag
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $file1_c );
  $dep2 = OdePath::unixize( $file1_o . $depsep . $file2_c );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $file3_c );
  $dep4 = OdePath::unixize( $file3_o . $depsep . $stdio_h );
  $tmpstr = "mkdep . -E -E";
  $command = "mkdep . -E*file3* -Efile1*:*file2*,'$usr_dir'/* >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists
  if (!$reg_error && (!-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk not found.";
  }
  # Check if "file1.u" exists
  if (!$reg_error && (!-f $file1_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file1_u removed without -rm flag";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (OdeFile::findInFile( $dep2, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file3.c
  if (!$reg_error && (OdeFile::findInFile( $dep3, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" found in $depend_mk";
  }
  # Check for dependency line -> file3.o: stdio.h
  if (!$reg_error && (OdeFile::findInFile( $dep4, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep4\" found in $depend_mk";
  }
  # Should only be $numlines lines in depend.mk file
  if ( !$reg_error && (OdeFile::numLinesInFile( $depend_mk ) !=  $numlines - 3 ))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 11, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 11, $tmpstr );
  }
 
  # Remove existing files for next test case
  OdeFile::deltree( \@delete_list );


  ## Regression Test Case 12 - generate a depend.mk file
  # eliminate a primariy dependency using -elpdep
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $file1_c );
  $dep2 = OdePath::unixize( $file1_o . $depsep . $file2_c );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $file3_c );
  $dep4 = OdePath::unixize( $file3_o . $depsep . $stdio_h );
  $tmpstr = "mkdep .";
  $command = "mkdep . -elpdep.c,.h >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists
  if (!$reg_error && (!-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk not found.";
  }
  # Check if "file1.u" exists
  if (!$reg_error && (!-f $file1_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file1_u removed without -rm flag";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (OdeFile::findInFile( $dep1, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (!OdeFile::findInFile( $dep2, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file3.c
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" not found in $depend_mk";
  }
  # Check for dependency line -> file3.o: stdio.h
  if (!$reg_error && (!OdeFile::findInFile( $dep4, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep4\" not found in $depend_mk";
  }
  # Should only be $numlines lines in depend.mk file
  if ( !$reg_error && (OdeFile::numLinesInFile( $depend_mk ) !=  $numlines - 1 ))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 12, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 12, $tmpstr );
  }
 
  # Remove existing files for next test case
  OdeFile::deltree( \@delete_list );


  ## Regression Test Case 13 - generate a depend.mk file
  # Test mkdep -suff
  # Use a *.other file as input instead of *.u. Do it in the same directory as
  # a *.u, to see that *.u is ignored. Have at least one absolute target in the
  # *.other file.  Check if the absolute part of the target is removed.
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $file1_c );
  $dep2 = OdePath::unixize( $file1_o . $depsep . $file2_c );
  $dep3 = OdePath::unixize( $file8_o . $depsep . $file8_c );
  $dep4 = OdePath::unixize( $file8_o . $depsep . $stdio_h );
  $tmpstr = "mkdep . -suff";
  $command = "mkdep . -suff.other >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists
  if (!$reg_error && (!-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk not found.";
  }
  # Check if "file8.other" exists
  if (!$reg_error && (!-f $file8_other))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file8_other removed without -rm flag";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (!OdeFile::findInFile( $dep2, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" not found in $depend_mk";
  }
  # Check for dependency line -> file8.o: file8.c
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" not found in $depend_mk";
  }
  # Check for dependency line -> file8.o: stdio.h
  if (!$reg_error && (!OdeFile::findInFile( $dep4, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep4\" not found in $depend_mk";
  }
  # Should only be $numlines lines in depend.mk file
  if ( !$reg_error && (OdeFile::numLinesInFile( $depend_mk ) !=
                        $numlines_abs ))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 13, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 13, $tmpstr );
  }
 
  # Remove existing files for next test case
  OdeFile::deltree( \@delete_list );


  ## Regression Test Case 14 - generate a depend.mk file
  # Test mkdep -suff
  # Use a *.other file as input instead of *.u. Do it in the same directory as
  # a *.u, to see that *.u is ignored. Have at least one absolute target in the
  # *.other file.  Check if the absolute part of the target is NOT removed.
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o_abs . $depsep . $file1_c );
  $dep2 = OdePath::unixize( $file1_o_abs . $depsep . $file2_c );
  $dep3 = OdePath::unixize( $file8_o_abs . $depsep . $file8_c );
  $dep4 = OdePath::unixize( $file8_o_abs . $depsep . $stdio_h );
  $tmpstr = "mkdep . -suff -abs";
  $command = "mkdep . -suff.other -abs >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists
  if (!$reg_error && (!-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk not found.";
  }
  # Check if "file8.other" exists
  if (!$reg_error && (!-f $file8_other))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file8_other removed without -rm flag";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (!OdeFile::findInFile( $dep2, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" not found in $depend_mk";
  }
  # Check for dependency line -> file8.o: file8.c
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" not found in $depend_mk";
  }
  # Check for dependency line -> file8.o: stdio.h
  if (!$reg_error && (!OdeFile::findInFile( $dep4, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep4\" not found in $depend_mk";
  }
  # Should only be $numlines lines in depend.mk file
  if ( !$reg_error && (OdeFile::numLinesInFile( $depend_mk ) != 
                        $numlines_abs ))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 14, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 14, $tmpstr );
  }
 
  # Remove existing files for next test case
  OdeFile::deltree( \@delete_list );


  ## Regression Test Case 15 - generate a depend.mk file
  # Test mkdep -suff -abs -top -subst
  # Use a *.other file as input instead of *.u. Do it in the same directory as
  # a *.u, to see that *.u is ignored. Have at least two absolute targets, one
  # which -top will change to ${MAKETOP} and one which it will not.
  # The -subst will transform MAKETOP to FAKEBOTTOM on the dependency side,
  # not the source side.
  $reg_error = 0;
  $dep1 = OdePath::unixize( $maketop . $file1_o . $depsep .
                                              $fakebottom . $file1_c_path );
  $dep2 = OdePath::unixize( $maketop . $file1_o . $depsep .
                                      $fakebottom . "../" . $file2_c_path );
  $dep3 = OdePath::unixize( $file8_o_abs . $depsep . $file8_c );
  $dep4 = OdePath::unixize( $file8_o_abs . $depsep . $stdio_h );
  $tmpstr = "mkdep . -suff -abs -top -subst";
  $command = "mkdep . -suff.other -abs -top -substMAKETOP=FAKEBOTTOM >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists
  if (!$reg_error && (!-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk not found.";
  }
  # Check if "file8.other" exists
  if (!$reg_error && (!-f $file8_other))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file8_other removed without -rm flag";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (!OdeFile::findInFile( $dep2, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" not found in $depend_mk";
  }
  # Check for dependency line -> file8.o: file8.c
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" not found in $depend_mk";
  }
  # Check for dependency line -> file8.o: stdio.h
  if (!$reg_error && (!OdeFile::findInFile( $dep4, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep4\" not found in $depend_mk";
  }
  # Should only be $numlines lines in depend.mk file
  if ( !$reg_error && (OdeFile::numLinesInFile( $depend_mk ) != 
                        $numlines_abs ))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 15, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 15, $tmpstr );
  }
 
  # Remove existing files for next test case
  OdeFile::deltree( \@delete_list );


  ## Regression Test Case 16 - generate a depend.mk file with 
  # -elxdep, and -top flags and OBJECTDIR set to an absolute path.
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $maketop . $file1_c_path );
  $dep2 = OdePath::unixize( 
                    $file1_o . $depsep . $maketop . "file2.c" );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $maketop . $file3_c_path );
  $ENV{'OBJECTDIR'} = $objectdir_abs_path;
  $tmpstr = "mkdep -top -elxdep . with absolute OBJECTDIR";
  $command = "mkdep -top -elxdep . >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists
  if (!$reg_error && (!-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk not found.";
  }
  # Check if "file1.u" exists
  if (!$reg_error && !(-f $file1_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file1_u was removed";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (!OdeFile::findInFile( $dep2, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" not found in $depend_mk";
  }
  # Check for dependency line -> file2.o: file3.c
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" not found in $depend_mk";
  }
  # Check for any reference to "stdio.h" in depend.mk - should not exist
  if (!$reg_error && (OdeFile::findInFile( "stdio.h", $depend_mk)))
  {
    $reg_error = 1;
    $error_txt = "Extra dependency relating to stdio.h found in $depend_mk";
  }
  # Should only be $numlines - 1 lines in depend.mk file
  if ( !$reg_error && 
       (OdeFile::numLinesInFile( $depend_mk ) != ( $numlines - 1 )))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 16, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 16, $tmpstr );
  }

  # Remove existing files for next test case
  OdeFile::deltree( \@delete_list );

  # Create temporary source files for next test case
  if (createFiles() || (!-f $file1_u) || (!-f $file2_u)) 
  {
    OdeInterface::logPrint("Error creating test files\n");
    return( 1 );
  }  


  ## Regression Test Case 17 - generate a depend.mk file with 
  # -elxdep, and -top flags and absolute value in MAKEOBJDIR.
  #  MAKEOBJDIR should override OBJECTDIR.
  #  Dependency with "stdio.h" should be in the depend.mk file
  #  with ${MAKETOP} even though it is outside the sandbox.
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $maketop . $file1_c_path );
  $dep2 = OdePath::unixize( 
                    $file1_o . $depsep . $maketop .  "../" . $file2_c_path );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $maketop . $file3_c_path );
  $dep4 = OdePath::unixize( $file3_o . $depsep . $maketop . "stdio.h" );
  $ENV{'MAKEOBJDIR'} = $makeobjdir_abs_path;
  $tmpstr = "mkdep -top -elxdep . with absolute MAKEOBJDIR";
  $command = "mkdep -top -elxdep . >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists
  if (!$reg_error && (!-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk not found.";
  }
  # Check if "file1.u" exists
  if (!$reg_error && !(-f $file1_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file1_u was removed";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (!OdeFile::findInFile( $dep2, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" not found in $depend_mk";
  }
  # Check for dependency line -> file2.o: file3.c
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" not found in $depend_mk";
  }
  # Check for dependency line -> file3.o: stdio.h
  if (!$reg_error && (!OdeFile::findInFile( $dep4, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep4\" not found in $depend_mk";
  }
  # Should only be $numlines lines in depend.mk file
  if ( !$reg_error && 
       (OdeFile::numLinesInFile( $depend_mk ) != ( $numlines )))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 17, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 17, $tmpstr );
  }

  # Remove existing files for next test case
  OdeFile::deltree( \@delete_list );

  # Create temporary source files for next test case
  if (createFiles() || (!-f $file1_u) || (!-f $file2_u)) 
  {
    OdeInterface::logPrint("Error creating test files\n");
    return( 1 );
  }  
  

  ## Regression Test Case 18 - generate a depend.mk file with 
  # -elxdep, and -top flags and relative value in MAKEOBJDIR.
  #  MAKEOBJDIR should override OBJECTDIR.
  #  Dependency with "stdio.h" should not be in the depend.mk file.
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $maketop . $file1_c_path );
  $dep2 = OdePath::unixize( 
                    $file1_o . $depsep . $maketop . "file2.c" );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $maketop . $file3_c_path );
  $ENV{'MAKEOBJDIR'} = $makeobjdir_path;
  $ENV{'SANDBOXBASE'} = $sb_dir;
  $ENV{'SOURCEBASE'} = $src_dir;
  $tmpstr = "mkdep -top -elxdep . with relative MAKEOBJDIR";
  $command = "mkdep -top -elxdep . >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "mkdep returned a non-zero exit code";
  }
  # Check if "depend.mk" file exists
  if (!$reg_error && (!-f $depend_mk))
  {
    $reg_error = 1;
    $error_txt = "$depend_mk not found.";
  }
  # Check if "file1.u" exists
  if (!$reg_error && !(-f $file1_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file1_u was removed";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep1\" not found in $depend_mk";
  }
  # Check for dependency line -> file1.o: file2.c
  if (!$reg_error && (!OdeFile::findInFile( $dep2, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep2\" not found in $depend_mk";
  }
  # Check for dependency line -> file2.o: file3.c
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $depend_mk, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$dep3\" not found in $depend_mk";
  }
  # Check for any reference to "stdio.h" in depend.mk - should not exist
  if (!$reg_error && (OdeFile::findInFile( "stdio.h", $depend_mk)))
  {
    $reg_error = 1;
    $error_txt = "Extra dependency relating to stdio.h found in $depend_mk";
  }
  # Should only be $numlines - 1 lines in depend.mk file
  if ( !$reg_error && 
       (OdeFile::numLinesInFile( $depend_mk ) != ( $numlines - 1 )))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $depend_mk";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkdep", 18, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkdep", 18, $tmpstr );
  }

  # Remove existing files for next test case
  OdeFile::deltree( \@delete_list );

  # Create temporary source files for next test case
  if (createFiles() || (!-f $file1_u) || (!-f $file2_u)) 
  {
    OdeInterface::logPrint("Error creating test files\n");
    return( 1 );
  }  
  

  ## Regression Test Case 19 - generate a depend.mk file
  # Test for NT support of embedded blanks in filenames and
  # mkdep usage with -qb flag.
  $reg_error = 0;
  if ( $OdeEnv::WIN32 )
  {
    OdePath::chgdir( $depend_dir_blank );
    $dep1 = OdePath::unixize( $file6_o_quoted . $depsep . $file6_c_quoted );
    $dep2 = OdePath::unixize( $file6_o_quoted . $depsep . $file7_c );
    $dep3 = OdePath::unixize( $file7_o . $depsep . $file6_c_quoted );
    $dep4 = OdePath::unixize( $file7_o . $depsep . $file7_c );
    $tmpstr = "mkdep -qb .";
    $command = "mkdep -qb . >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $status = OdeEnv::runOdeCommand( $command ); # Check command exit code
    if ($status != 0)
    {
      $reg_error = 1;
      $error_txt = "mkdep returned a non-zero exit code";
    }
    # Check if "depend.mk" file exists
    if (!$reg_error && (!-f "depend.mk" ))
    {
      $reg_error = 1;
      $error_txt = "depend.mk not found.";
    }
    # Check if "file6.u" exists
    if (!$reg_error && (!-f $file6_u))
    {
      $reg_error = 1;
      $error_txt = "Dependency file $file6_u removed without -rm flag";
    }
    # Check for dependency line -> file6.o: file6.c
    if (!$reg_error && (!OdeFile::findInFile( $dep1, "depend.mk", 1)))
    {
      $reg_error = 1;
      $error_txt = "Dependency \"$dep1\" not found in depend.mk";
    }
    # Check for dependency line -> file1.o: file2.c
    if (!$reg_error && (!OdeFile::findInFile( $dep2, "depend.mk", 1)))
    {
      $reg_error = 1;
      $error_txt = "Dependency \"$dep2\" not found in depend.mk";
    }
    # Check for dependency line -> file1.o: file3.c
    if (!$reg_error && (!OdeFile::findInFile( $dep3, "depend.mk", 1)))
    {
      $reg_error = 1;
      $error_txt = "Dependency \"$dep3\" not found in depend.mk";
    }
    # Check for dependency line -> file3.o: stdio.h
    if (!$reg_error && (!OdeFile::findInFile( $dep4, "depend.mk", 1)))
    {
      $reg_error = 1;
      $error_txt = "Dependency \"$dep4\" not found in depend.mk";
    }
    # Should only be $numlines_filename_blanks lines in depend.mk file
    if ( !$reg_error && (OdeFile::numLinesInFile( "depend.mk" ) != 
                         $numlines_filename_blanks ))
    {
      $reg_error = 1;
      $error_txt = "Wrong number of lines in depend.mk";
    }
    if ($reg_error)
    {
      # Test case failed
      $error = 1;
      OdeInterface::printResult( "fail", "regression", "mkdep", 19, $tmpstr );
      OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    }
    else
    {
      # Test case passed
      OdeInterface::printResult( "pass", "regression", "mkdep", 19, $tmpstr );
    }
   
  }

  # Remove existing files for next test case
  OdeFile::deltree( \@delete_list );

  # Unset BACKED_SANDBOXDIR
  delete $ENV{'BACKED_SANDBOXDIR'};
  delete $ENV{'OBJECTDIR'};
  delete $ENV{'MAKEOBJDIR'};

  OdePath::chgdir( $orig_dir );

  return( $error );
}

#########################################
#
# sub fvt
# - execute FVT set of tests
#
#########################################
sub fvt ()
{
  OdeInterface::logPrint("fvt not implemented yet!\n");

}

#########################################
#
# sub createFiles
# - create the temporary source and 
#   dependency files used to test mkdep.
#
#########################################
sub createFiles ()
{  
  my $orig_dir;
  my $status;
  my $command;
  my $error = 0;

  # Create temporary files
  $depend_dir = $OdeEnv::tempdir . $OdeEnv::dirsep . "depend_dir";
  $depend_subdir = $depend_dir . $OdeEnv::dirsep . "subdir";
  $depend_dir_sb = $depend_dir . $OdeEnv::dirsep . "sb";
  $depend_dir_bb = $depend_dir . $OdeEnv::dirsep . "bb";
  $depend_dir_blank = $depend_dir . $OdeEnv::dirsep . "blank";
  mkpath( $depend_subdir );
  mkpath( $depend_dir_sb );
  mkpath( $depend_dir_bb );
  mkpath( $depend_dir_blank );

  $depend_mk = $depend_dir . $OdeEnv::dirsep . "depend.mk";
  $file1_u = $depend_dir . $OdeEnv::dirsep . "file1.u";
  $file2_u = $depend_subdir . $OdeEnv::dirsep . "file2.u";
  $file3_u = $depend_dir_bb . $OdeEnv::dirsep . "file3.u";
  $file4_u = $depend_dir_bb . $OdeEnv::dirsep . "file4.u";
  $file4_sb_u = $depend_dir_sb . $OdeEnv::dirsep . "file4.u";
  $file6_u = $depend_dir_blank . $OdeEnv::dirsep . "file6.u";
  $file1_o = "file1" . $OdeEnv::obj_suff;
  $file2_o = "file2" . $OdeEnv::obj_suff;
  $file3_o = "file3" . $OdeEnv::obj_suff;
  $file4_o = "file4" . $OdeEnv::obj_suff;
  $file5_o = "file5" . $OdeEnv::obj_suff;
  $file6_o = "file 6" . $OdeEnv::obj_suff;
  $file6_o_quoted = "\"file 6" . $OdeEnv::obj_suff . "\"";
  $file7_o = "\"file 7" . $OdeEnv::obj_suff . "\"";
  $file8_other = $depend_dir . $OdeEnv::dirsep . "file8.other";
  $file8_o = "file8" . $OdeEnv::obj_suff;

  $root_dir = join( $OdeEnv::dirsep, ( $depend_dir, "root" ));
  $sb_dir = join( $OdeEnv::dirsep, ( $root_dir, "sb" ));
  $src_dir = join( $OdeEnv::dirsep, ( $sb_dir, "src" ));
  $file1_c_path = join( $OdeEnv::dirsep, ( "bin", "filedir", "file1.c" ));
  $file1_c = join( $OdeEnv::dirsep, ( $src_dir, $file1_c_path ));
  $file1_o_abs = join( $OdeEnv::dirsep, ( $src_dir, $file1_o ));
  $file2_c_path = join( $OdeEnv::dirsep, ( "lib", "file2.c" ));
  $file2_c = join( $OdeEnv::dirsep, ( $sb_dir, $file2_c_path ));
  $file3_c_path = join( $OdeEnv::dirsep, ( "latest", "file3.c" ));
  $file3_c = join( $OdeEnv::dirsep, ( $src_dir, $file3_c_path ));
  $stdio_h_path = join( $OdeEnv::dirsep, ( "usr", "include", "stdio.h" ));
  $stdio_h = join( $OdeEnv::dirsep, ( $root_dir, $stdio_h_path ));
  $file6_c_path = join( $OdeEnv::dirsep, ( "bin", "filedir", "file 6.c" ));
  $file6_c = join( $OdeEnv::dirsep, ( $src_dir, $file6_c_path ));
  $file6_c_quoted = "\"" . $file6_c . "\"";
  $file7_c_path = join( $OdeEnv::dirsep, ( "bin", "filedir", "file 7.c" ));
  $file7_c = "\"" . join( $OdeEnv::dirsep, ( $src_dir, $file7_c_path )) . "\"";
  $file8_c = join( $OdeEnv::dirsep, ( $root_dir, "extra", "file8.c" ));
  $file8_o_abs = join( $OdeEnv::dirsep, ( $root_dir, "extra", $file8_o ));
  $ipath1 = join( $OdeEnv::dirsep, ( $src_dir, "bin" ));
  $ipath1_result = join( $OdeEnv::dirsep, ( "filedir", "file1.c" ));
  $usr_dir = join( $OdeEnv::dirsep, ( $depend_dir, "root", "usr" ));
  $makeobjdir_path = join( $OdeEnv::dirsep, ( "..", "lib" ));
  $makeobjdir_abs_path = join( $OdeEnv::dirsep, ( $root_dir, "usr",
                                                  "include" ));
  $objectdir_abs_path = join( $OdeEnv::dirsep, ( $sb_dir, "lib" ));
  
  # create dependency file - file1.u 
  if (open( FILE1, "> $file1_u" )) {
    print( FILE1 "# file1.u - temporary dependency file for mkdep\n");
    if (defined( $OdeEnv::WIN32 ))
    {
      print( FILE1 "$file1_o: $file1_c\n");
      print( FILE1 "$file1_o: $file2_c\n");
    }
    else
    {
      print( FILE1 "$file1_o: $file1_c \\\n");
      print( FILE1 "          $file2_c\n");
    }
    print( FILE1 "$file2_o: $file3_c\n");
    print( FILE1 "$file3_o: $stdio_h\n");
    close( FILE1 );
  }
  else 
  {
    OdeInterface::logPrint( "Could not open file $file1_u\n" );
    return( 1 );
  }

  # create dependency file - file2.u 
  if (open( FILE2, "> $file2_u" )) {
    print( FILE2 "# file2.u - temporary dependency file for mkdep\n");
    print( FILE2 "$file4_o: $file1_c\n");
    print( FILE2 "$file5_o: $stdio_h\n");
    close( FILE2 );
  }
  else 
  {
    OdeInterface::logPrint( "Could not open file $file1_u\n" );
    return( 1 );
  }

  # create dependency file - file3.u 
  if (open( FILE3, "> $file3_u" )) {
    print( FILE3 "# file3.u - temporary dependency file for mkdep\n");
    print( FILE3 "file1.o: file1.c\n");
    print( FILE3 "file1.o: file1.h\n");
    close( FILE3 );
  }
  else 
  {
    OdeInterface::logPrint( "Could not open file $file3_u\n" );
    return( 1 );
  }

  # create dependency file - file4.u in backing build dir
  if (open( FILE4, "> $file4_u" )) {
    print( FILE4 "# file4.u - temporary dependency file for mkdep\n");
    print( FILE4 "file2.o: file2.c\n");
    close( FILE4 );
  }
  else 
  {
    OdeInterface::logPrint( "Could not open file $file3_u\n" );
    return( 1 );
  }

  # create dependency file - file4.u in sandbox dir 
  if (open( FILE5, "> $file4_sb_u" )) {
    print( FILE5 "# file4.u - temporary dependency file for mkdep\n");
    print( FILE5 "file2.o: file2.c\n");
    print( FILE5 "file2.o: file2.h\n");
    close( FILE5 );
  }
  else 
  {
    OdeInterface::logPrint( "Could not open file $file3_u\n" );
    return( 1 );
  }

  # create dependency file - file6.u 
  if (defined( $OdeEnv::WIN32 ))
  {
    if (open( FILE6, "> $file6_u" )) {
      print( FILE6 "# file6.u - temporary dependency file for mkdep\n");
      print( FILE6 "$file6_o: $file6_c\n");
      print( FILE6 "$file6_o: $file7_c\n");
      print( FILE6 "$file7_o: $file6_c\n");
      print( FILE6 "$file7_o: $file7_c\n");
      close( FILE6 );
    }
    else 
    {
      OdeInterface::logPrint( "Could not open file $file6_u\n" );
      return( 1 );
    }
  }

  # create dependency file - file8.other 
  if (open( FILE8, "> $file8_other" )) {
    print( FILE8 "# file8.other - temporary dependency file for mkdep\n");
    if (defined( $OdeEnv::WIN32 ))
    {
      print( FILE8 "$file1_o_abs: $file1_c\n");
      print( FILE8 "$file1_o_abs: $file2_c\n");
    }
    else
    {
      print( FILE8 "$file1_o_abs: $file1_c \\\n");
      print( FILE8 "          $file2_c\n");
    }
    print( FILE8 "$file8_o_abs: $file8_c\n");
    print( FILE8 "$file8_o_abs: $stdio_h\n");
    close( FILE8 );
  }
  else 
  {
    OdeInterface::logPrint( "Could not open file $file8_other\n" );
    return( 1 );
  }

  return( 0 );

}

1;
