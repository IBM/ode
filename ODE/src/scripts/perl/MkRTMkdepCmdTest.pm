#########################################
#
# MkRTMkdepCmdTest module
# - module to test mk's mkdep realtime command logic
#
#########################################

package MkRTMkdepCmdTest;

use File::Path;
use OdeEnv;
use OdeFile;
use OdeTest;
use OdeUtil;
use OdeInterface;
use File::Copy;
use File::Basename;
use OdePath;
use Cwd;
use strict;

# Defining these vars to be global to all subroutines in this file
my $file1_c;
my $file2_c;
my $file3_c;
my $stdio_h;
my $file1_c_path;
my $file2_c_path;
my $file3_c_path;
my $stdio_h_path;

my $depend_mk;
my $depend_dir;
my $root_dir;
my $sb_dir;
my $src_dir;
my $file1_u;
my $file1_o;
my $file2_o;
my $file3_o;
my $depsep = ":  ";    # delimiting string between target and source files
my $maketop = "\${MAKETOP}";

#########################################
#
# sub run
#
#########################################
sub run ()
{
  my $status;
  my $error = 0;

  my $command;
  my $status;
  my $reg_error = 0;
  my $error_txt;
  my @arg_list;
  my @str_list;
  my $makefile_name;
  my $tdir1;
  my $tmpstr;
  my $orig_dir = cwd();
  my ($dep1, $dep2, $dep3, $dep4);
  my $numlines = 10;  # 10 lines in depend.mk for full dependencies

  OdeInterface::logPrint( "in MkRTMkdepCmdTest\n");
  
  # Create temporary source files
  if (createFiles() || (!-f $file1_u))
  {
    OdeInterface::printError("mk : .rmkdep : Error creating test files");
    return( 1 );
  }
  my $makefile_path;
  $makefile_path = join( $OdeEnv::dirsep, (cwd(), "makefiles",
                                           "commands", "rtcmd_mkdep.mk") );

  #   cd to depend_dir
  if (! OdePath::chgdir( $depend_dir ) )
  {
    OdeInterface::printError( "mk : .rmkdep : Could not cd to $depend_dir: $!" );
    return( 1 );
  }
  my @delete_list = ($depend_mk);

  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  #  must first set BACKED_SANDBOXDIR environment variable to establish
  #  the value of ${MAKETOP} for cases with -top and -elxdep
  $ENV{'BACKED_SANDBOXDIR'} = $sb_dir;
  

  # test1: mk .rmkdep with no flags
  # Generate a depend.mk file, normal mkdep usage with no flags
  $status = runMkTest( $makefile_path,
                       ".rmkdep with no flags", 1, 1, "test1" );
  if (!$error) { $error = $status; }
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $file1_c );
  $dep2 = OdePath::unixize( $file1_o . $depsep . $file2_c );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $file3_c );
  $dep4 = OdePath::unixize( $file3_o . $depsep . $stdio_h );
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
    OdeInterface::printResult( "fail", "regression", "mk .rmkdep", 1, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mk .rmkdep", 1, $tmpstr );
  }

  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );


  # test2: mk .rmkdep with some mkdep flags
  # Generate a depend.mk file with -elxdep and -top flags
  # Dependency with "stdio.h" should NOT be in the depend.mk file
  $status = runMkTest( $makefile_path,
                       ".rmkdep with some flags", 1, 2, "test2" );
  if (!$error) { $error = $status; }
  $reg_error = 0;
  $dep1 = OdePath::unixize( $file1_o . $depsep . $maketop . $file1_c_path );
  $dep2 = OdePath::unixize( 
                    $file1_o . $depsep . $maketop . "../" . $file2_c_path );
  $dep3 = OdePath::unixize( $file2_o . $depsep . $maketop . $file3_c_path );
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
    OdeInterface::printResult( "fail", "regression", "mk .rmkdep -top -elxdep",
                               2, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mk .rmkdep -top -elxdep",
                               2, $tmpstr );
  }

  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );


  # test3: .rmkdep with a bad flag
  $status = runMkErrorTest( $makefile_path,
                            ".rmkdep with bad flag", 3, "test3" );
  if (!$error) { $error = $status; }

  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  # .rmkdep with ODE flag (should be an error)
  $status = runMkErrorTest( $makefile_path,
                            ".rmkdep with ODE flag", 4, "test4" );
  if (!$error) { $error = $status; }

  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );
  OdeFile::deltree( $OdeTest::tmpfile );

  # Unset BACKED_SANDBOXDIR
  delete $ENV{'BACKED_SANDBOXDIR'};

  OdePath::chgdir( $orig_dir );

  $tdir1 = join( $OdeEnv::dirsep, 
                 ($OdeEnv::tempdir, "makefiles", "commands") );
  mkpath( $tdir1 );
  $makefile_name = "makefiles" . $OdeEnv::dirsep . "commands" . $OdeEnv::dirsep . "rtcmd_rcp.mk";
  OdeEnv::runSystemCommand( "$OdeEnv::copy $makefile_name $tdir1" );
  $makefile_name = "makefiles" . $OdeEnv::dirsep . "commands" . $OdeEnv::dirsep . "rtcmd_rmv.mk";
  OdeEnv::runSystemCommand( "$OdeEnv::copy $makefile_name $tdir1" );
  ## cd'ing into the temp directory
  OdePath::chgdir( $OdeEnv::tempdir );
  
  # test5: mk -f rtcmd_rcp.mk
  # Test Runtime Cmd's   .rcp and .rrm
  @str_list=("initialization", ".rcp file1 file2", ".rcp file1 file2 subdir1",
             ".rcp subdir1/file3 ./", ".rcp file1 file2 file3 subdir2",
             "file1 file1 file1 file2 file2 file2 file3 file3 file3",
             "cleanup", ".rrm file1 file2 file3",
             ".rrm subdir1/file1 subdir1/file2 subdir1/file3",
             ".rrm subdir2/file1 subdir2/file2 subdir2/file3",
             ".rrm subdir1 subdir2", "file?");
  @arg_list = ("");
  $status = runMkTest2( "rtcmd_rcp.mk", "Runtime Cmd: .rcp", 12, 5,  
                       \@str_list, \@arg_list );
  if (!$error) { $error = $status; }

  
  # test6: mk -f rtcmd_rmv.mk
  # Test Runtime Cmd's   .rmv and .rrm
  @str_list=("initialization", ".rmv file1 file2", ".rmv file2 file3 subdir1",
             ".rmv subdir1/file5 ./", 
             ".rmv subdir1/file2 subdir1/file3 subdir1/file4 subdir2",
             "file5", "file*", "file2 file3 file4", "cleanup",".rrm file5",
             ".rrm subdir2/file4 subdir2/file2 subdir2/file3",
             ".rrm subdir1 subdir2", "file?", "file*", "*");
  @arg_list = ("");
  $status = runMkTest2( "rtcmd_rmv.mk", "Runtime Cmd: .rcp", 15, 6,  
                       \@str_list, \@arg_list );
  if (!$error) { $error = $status; }

  OdePath::chgdir( $orig_dir );

  return $error; 
  
}

###########################################################
#
# sub runMkTest
# - to make and run the command with all the
#   arguments passed and check the results
#
# - arguments
#   makefile: absolute path to the makefile
#   tststr: the string to be output in the result
#   num_tests: number of tests performed in the makefile
#   testno: test number
#   args: a list of any other commandline args
############################################################
sub runMkTest( $@ )
{
  my ($makefile, $tststr, $num_tests, $testno, @args) = @_;
  my $command;
  my $mk_error = 0;
  my $status;
  my $error_txt;
  if (!$makefile)
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile\n" );
    return( 1 );
  }
  $command = "mk -f $makefile @args > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  # Check if test passed
  # will catch both ERROR and ODEMKERROR messages in tmpfile
  if (!$mk_error && (OdeFile::findInFile( "ERROR", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "Error occured while testing $tststr";
  }
  # Check if only the specified targets are executed
  if (!$mk_error && 
  (OdeFile::numPatternsInFile( "ODEMKPASS", $OdeTest::tmpfile ) != $num_tests))
  {
    $mk_error = 1;
    $error_txt = "Incorrect number of targets executed while testing $tststr";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                                         $testno, $tststr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                                         $testno, $tststr );
  }
  return $mk_error;
}




##############################################################
#
# sub runMkTest2
# - to make and run the command with all the
#   arguments passed and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   num_tests: number of tests performed in the makefile
#   testno: test number
#   str_list: reference to a list of all the strings
#             to be looked for in the tempfile
#   args: a reference to a list of any other commandline args
#############################################################
sub runMkTest2( $$$$$$ )
{
  my ($filename, $tststr, $num_tests, $testno, $str_list, $args) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $status;
  my $error_txt;
  my $str_num = scalar( @$str_list );
  my $index;
  $makefile_name = "commands" . $OdeEnv::dirsep . $filename;

  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
  }
  $command = "mk -f $makefile @$args > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  # Check if the output of mk is correct
  if (!$mk_error)
  {
    for( $index=0; $index<$str_num; $index++)
    {
      if (!(OdeFile::findInFile( @$str_list[$index], $OdeTest::tmpfile )))
      {
        $mk_error = 1;
        $error_txt = "Error occured while testing $tststr";
        last;
      }
    }
  }
  # Check if the correct number of targets are executed
  if (!$mk_error && 
     (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != $num_tests))
  {
    $mk_error = 1;
    $error_txt = "Incorrect number of targets executed while testing $tststr";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, 
                               "mk", $testno, $tststr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, 
                               "mk", $testno, $tststr );
  }
  return $mk_error;
}



###########################################################
#
# sub runMkErrorTest
# - to make and run the command with all the
#   arguments passed and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   testno: test number
#   args: a list of any other commandline args
############################################################
sub runMkErrorTest( $@ )
{
  my ($makefile, $tststr, $testno, @args) = @_;
  my $command;
  my $mk_error = 0;
  my $status;
  my $error_txt;
  if (!$makefile)
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile\n" );
    return( 1 );
  }
  $command = "mk -f $makefile @args > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status == 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a zero exit code";
  }
  # Check if mk produced an error
  if (!$mk_error && !(OdeFile::findInFile( "ERROR", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "mk did not produce an error";
  }
  # Check if no targets are executed
  if (!$mk_error && 
  ((OdeFile::numPatternsInFile( "PASS", $OdeTest::tmpfile ) != 0) ||
   (OdeFile::numPatternsInFile( "FAIL", $OdeTest::tmpfile ) != 0)))
  {
    $mk_error = 1;
    $error_txt = "Some targets executed after an error occured";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                                         $testno, $tststr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                                         $testno, $tststr );
  }
  return $mk_error;
}

#########################################
#
# sub createFiles
# - create the temporary source and 
#   dependency files used to test .rmkdep
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
  mkpath( $depend_dir );

  $depend_mk = $depend_dir . $OdeEnv::dirsep . "depend.mk";
  $file1_u = $depend_dir . $OdeEnv::dirsep . "file1.u";
  $file1_o = "file1" . $OdeEnv::obj_suff;
  $file2_o = "file2" . $OdeEnv::obj_suff;
  $file3_o = "file3" . $OdeEnv::obj_suff;

  $root_dir = join( $OdeEnv::dirsep, ( $depend_dir, "root" ));
  $sb_dir = join( $OdeEnv::dirsep, ( $root_dir, "sb" ));
  $src_dir = join( $OdeEnv::dirsep, ( $sb_dir, "src" ));
  $file1_c_path = join( $OdeEnv::dirsep, ( "bin", "filedir", "file1.c" ));
  $file1_c = join( $OdeEnv::dirsep, ( $src_dir, $file1_c_path ));
  $file2_c_path = join( $OdeEnv::dirsep, ( "lib", "file2.c" ));
  $file2_c = join( $OdeEnv::dirsep, ( $sb_dir, $file2_c_path ));
  $file3_c_path = join( $OdeEnv::dirsep, ( "latest", "file3.c" ));
  $file3_c = join( $OdeEnv::dirsep, ( $src_dir, $file3_c_path ));
  $stdio_h_path = join( $OdeEnv::dirsep, ( "usr", "include", "stdio.h" ));
  $stdio_h = join( $OdeEnv::dirsep, ( $root_dir, $stdio_h_path ));
  
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

  return( 0 );

}
  
1;
