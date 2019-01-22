#########################################
#
# Gendeptest module
# - module to run gendeptest for different
#   test levels
#
#########################################

package Gendeptest;

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
# subroutines in this file
my $file1_c;
my $file2_c;
my $fileb2_c;
my $file1_h;
my $file2_h;
my $file3_h;
my $file4_h;
my $file5_h;
my $fileb5_h;

my $depend_dir;
my $depend_inc_dir;
my $depend_inbc_dir;
my $file1_u;
my $file2_u;
my $fileb2_u;
my $file1_o;
my $file2_o;
my $fileb2_o;
my $numComments = 3;   # number of comment lines in a dependency file
my $depsep = ": ";    # delimiting string between target and source files

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
  my ($dep1, $dep2, $dep3);

  OdeInterface::logPrint( "in Gendeptest - test level is normal\n");
  OdeEnv::runOdeCommand( "gendep -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

  # Create temporary source files
  if (createFiles()) 
  {
    OdeInterface::logPrint("gendeptest: Error creating test files\n");
    return( 1 );
  }

  #   cd to depend_dir
  if (! OdePath::chgdir( $depend_dir ) )
  {
    OdeInterface::logPrint( "gendeptest: Could not cd to $depend_dir: $!\n" );
    return( 1 );
  }

  my @delete_list = ($file1_u);
  # remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  # Test Case #1 - normal gendep usage
  $dep1 = OdePath::unixize( $file1_o . $depsep . $file1_c );
  $dep2 = OdePath::unixize( $file1_o . $depsep . $file2_h );
  $command = "gendep $file1_c >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ( $status ||
       (!-f $file1_u) ||
       (!OdeFile::findInFile( $dep1, $file1_u, 1)) ||
       (!OdeFile::findInFile( $dep2, $file1_u, 1))
     )
  {
    OdeInterface::printResult( "fail", "normal", "gendep", 1, "gendep" );
    $error = 1;
  }
  else
  {
    # Test #1 passed
    OdeInterface::printResult( "pass", "normal", "gendep", 1, "gendep" );
  }

  # Test Case #2 - define a command line variable
  $dep1 = OdePath::unixize( "file1.o" . $depsep . $file1_c );
  $dep2 = OdePath::unixize( "file1.o" . $depsep . $file1_h );
  $dep3 = OdePath::unixize( "file1.o" . $depsep . $file4_h );
  $command = "gendep -s.o -DVAR2 $file1_c >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ( $status ||
       (!-f $file1_u) ||
       (!OdeFile::findInFile( $dep1, $file1_u, 1)) ||
       (!OdeFile::findInFile( $dep2, $file1_u, 1)) ||
       (!OdeFile::findInFile( $dep3, $file1_u, 1))
     )
  {
    OdeInterface::printResult( "fail", "normal", "gendep", 2, "gendep -s -D" );
    $error = 1;
  }
  else
  {
    # Test #2 passed
    OdeInterface::printResult( "pass", "normal", "gendep", 2, "gendep -s -D" );
  }

  OdeFile::deltree( \@delete_list );    

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
  my $cmd_file = $OdeEnv::tempdir . $OdeEnv::dirsep . "ode_input.txt";
  my $orig_dir = cwd();
  my $numdeps;
  my ($dep1, $dep2, $dep3);
  my @delete_list;

  OdeInterface::logPrint( "in Gendeptest - test level is regression\n");
  OdeEnv::runOdeCommand( "gendep -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

  # Create temporary source files
  if (createFiles()) 
  {
    OdeInterface::printError("gendep : Error creating test files");
    return( 1 );
  }  
  #   cd to depend_dir
  if (! OdePath::chgdir( $depend_dir ) )
  {
    OdeInterface::print( "gendep : Could not cd to $depend_dir: $!" );
    return( 1 );
  }

  if ( $OdeEnv::WIN32 )
  {
    @delete_list = ($file1_u, $file2_u, $cmd_file, $fileb2_u);
  }
  else
  {
    @delete_list = ($file1_u, $file2_u, $cmd_file);
  }
  # Remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  ## Regression Test Case 1 - generate a .u file
  # this tests parsing of #include and #define by gendep
  # and some simple conditional logic
  $dep1 = OdePath::unixize( $file1_o . $depsep . $file1_c );
  $dep2 = OdePath::unixize( $file1_o . $depsep . $file2_h );
  $command = "gendep $file1_c >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "gendep file1.c";
  $numdeps = 2; # Test should generate two dependencies in dep file
  $status = OdeEnv::runOdeCommand( $command );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "gendep returned a non-zero exit code";
  }
  # Check if ".u" file exists
  if (!$reg_error && (!-f $file1_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file1_u not found.";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $file1_u, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$file1_o: $file1_c\" not found in $file1_u";
  }
  # Check for dependency line -> file1.o: file2.h
  if (!$reg_error && (!OdeFile::findInFile( $dep2, $file1_u, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$file1_o: $file2_h\" not found in $file1_u";
  }
  # Should only be numdeps dependencies in .u file
  if ( !$reg_error && 
       (OdeFile::numLinesInFile( $file1_u ) != ($numdeps + $numComments)))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $file1_u";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "gendep", 1, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "gendep", 1, $tmpstr );
  }
  # Remove existing ".u" files for next test case
  OdeFile::deltree( \@delete_list );

  ## Regression Test Case 2 - generate a .u file with -D flag
  # this tests the use of the -D flag, as well as a recursive
  # include statement - file1.h includes file4.h; as well as 
  # some simple conditional logic
  $dep1 = OdePath::unixize( $file1_o . $depsep . $file1_c );
  $dep2 = OdePath::unixize( $file1_o . $depsep . $file1_h );
  $dep3 = OdePath::unixize( $file1_o . $depsep . $file4_h );
  $reg_error = 0;
  $command = "gendep -DVAR2 $file1_c >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "gendep -DVAR file1.c";
  $numdeps = 3; # Test should generate two dependencies in dep file
  $status = OdeEnv::runOdeCommand( $command );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "gendep returned a non-zero exit code";
  }
  # Check if ".u" file exists
  if (!$reg_error && (!-f $file1_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file1_u not found.";
  }
  # Check for dependency line -> file1.o: file1.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $file1_u, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$file1_o: $file1_c\" not found in $file1_u";
  }
  # Check for dependency line -> file1.o: file1.h
  if (!$reg_error && (!OdeFile::findInFile( $dep2, $file1_u, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$file1_o: $file1_h\" not found in $file1_u";
  }
  # Check for dependency line -> file1.o: file4.h
  if (!$reg_error && (!OdeFile::findInFile( $dep3, $file1_u, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$file1_o: $file4_h\" not found in $file1_u";
  }
  # Should only be numdeps dependencies in .u file
  if ( !$reg_error && 
       (OdeFile::numLinesInFile( $file1_u ) != ($numdeps + $numComments)))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $file1_u";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "gendep", 2, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "gendep", 2, $tmpstr );
  }
  # Remove existing ".u" files for next test case
  OdeFile::deltree( \@delete_list );

  ## Regression Test Case 3 - generate a .u file with -I flag
  # This tests the -I flag by generating a depend file for "file2.c"
  # which includes "file5.h", which is located in a different
  # directory than "file2.c"  
  $dep1 = OdePath::unixize( $file2_o . $depsep . $file2_c );
  $dep2 = OdePath::unixize( $file2_o . $depsep . $file5_h );
  $reg_error = 0;
  $command = "gendep -I$depend_inc_dir $file2_c >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "gendep -I file2.c";
  $numdeps = 2; # Test should generate two dependencies in dep file
  $status = OdeEnv::runOdeCommand( $command );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "gendep returned a non-zero exit code";
  }
  # Check if ".u" file exists
  if (!$reg_error && (!-f $file2_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file2_u not found.";
  }
  # Check for dependency line -> file2.o: file2.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $file2_u, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$file2_o: $file2_c\" not found in $file2_u";
  }
  # Check for dependency line -> file2.o: file5.h
  if (!$reg_error && (!OdeFile::findInFile( $dep2, $file2_u, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$file2_o: $file5_h\" not found in $file2_u";
  }
  # Should only be numdeps dependencies in .u file
  if ( !$reg_error && 
       (OdeFile::numLinesInFile( $file2_u ) != ($numdeps + $numComments)))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $file2_u";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "gendep", 3, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "gendep", 3, $tmpstr );
  }

  # Remove existing ".u" files for next test case
  OdeFile::deltree( \@delete_list );

  ## Regression Test Case 4 - generate a .u file with -I flag
  # This tests the -E flag by including, then excluding, the directory
  #  that contains "file5.h"
  $dep1 = OdePath::unixize( $file2_o . $depsep . $file2_c );
  $dep2 = OdePath::unixize( $file2_o . $depsep . $file5_h );
  $reg_error = 0;
  $command = "gendep -I$depend_inc_dir -E$depend_inc_dir $file2_c " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "gendep -I -E file2.c";
  $numdeps = 1; # Test should generate one dependencies in dep file
  $status = OdeEnv::runOdeCommand( $command );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "gendep returned a non-zero exit code";
  }
  # Check if ".u" file exists
  if (!$reg_error && (!-f $file2_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file2_u not found.";
  }
  # Check for dependency line -> file2.o: file2.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $file2_u, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$file2_o: $file1_c\" not found in $file1_u";
  }
  # Check for dependency line (should not exist!) -> file2.o: file5.h
  if (!$reg_error && (OdeFile::findInFile( $dep2, $file2_u, 1)))
  {
    $reg_error = 1;
    $error_txt = "Excluded dependency \"$file2_o: $file5_h\" found in $file2_u";
  }
  # Should only be numdeps dependencies in .u file
  if ( !$reg_error && 
       (OdeFile::numLinesInFile( $file2_u ) != ($numdeps + $numComments)))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $file2_u";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "gendep", 4, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "gendep", 4, $tmpstr );
  }

  # Remove existing ".u" files for next test case
  OdeFile::deltree( \@delete_list );

  ## Regression Test Case 5 - generate a .u file with -I flag
  # This tests the -E flag by including, then excluding, the directory
  #  that contains "file5.h".
  # This test is the same as the previous except that the -I and -E
  # parameters are in a response file.
  $dep1 = OdePath::unixize( $file2_o . $depsep . $file2_c );
  $dep2 = OdePath::unixize( $file2_o . $depsep . $file5_h );
  $reg_error = 0;
  open( INPUT, "> $cmd_file" ) ||
     OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "-I$depend_inc_dir\n" );
  print( INPUT "-E $depend_inc_dir\n" );
  close( INPUT );
  $command = "gendep -respfile $cmd_file $file2_c " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "gendep -respfile containing -I -E file2.c";
  $numdeps = 1; # Test should generate one dependencies in dep file
  $status = OdeEnv::runOdeCommand( $command );
  # Check command exit code
  if ($status != 0)
  {
    $reg_error = 1;
    $error_txt = "gendep returned a non-zero exit code";
  }
  # Check if ".u" file exists
  if (!$reg_error && (!-f $file2_u))
  {
    $reg_error = 1;
    $error_txt = "Dependency file $file2_u not found.";
  }
  # Check for dependency line -> file2.o: file2.c
  if (!$reg_error && (!OdeFile::findInFile( $dep1, $file2_u, 1)))
  {
    $reg_error = 1;
    $error_txt = "Dependency \"$file2_o: $file1_c\" not found in $file1_u";
  }
  # Check for dependency line (should not exist!) -> file2.o: file5.h
  if (!$reg_error && (OdeFile::findInFile( $dep2, $file2_u, 1)))
  {
    $reg_error = 1;
    $error_txt = "Excluded dependency \"$file2_o: $file5_h\" found in $file2_u";
  }
  # Should only be numdeps dependencies in .u file
  if ( !$reg_error && 
       (OdeFile::numLinesInFile( $file2_u ) != ($numdeps + $numComments)))
  {
    $reg_error = 1;
    $error_txt = "Wrong number of lines in $file2_u";
  }
  if ($reg_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "gendep", 5, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "gendep", 5, $tmpstr );
  }

  # Remove existing ".u" files for next test case
  OdeFile::deltree( \@delete_list );

  ## Regression Test Case 6 - test for non-zero exit code
  # force gendep to fail and check if a non-zero exit code was supplied
  $reg_error = 0;
  $command = "gendep -badflag $file2_c >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "gendep <invalid flag>";
  $status = OdeEnv::runOdeCommand( $command );
  # Check command exit code
  if ($status == 0)
  {
    $reg_error = 1;
    $error_txt = "gendep returned a zero exit code after an error";
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "gendep", 6, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "gendep", 6, $tmpstr );
  }

  # Remove existing ".u" files for next test case
  OdeFile::deltree( \@delete_list );

  if ( $OdeEnv::WIN32 )
  {
    ## Regression Test Case 7 - generate a .u file with -I flag
    # using filenames with embedded blanks (NT only).
    # This tests the -I flag by generating a depend file for "file 2.c"
    # which includes "file 5.h", which is located in a different
    # directory than "file 2.c"  
    $dep1 = '"' . OdePath::unixize( $fileb2_o ) . '"' . $depsep .
            '"' . OdePath::unixize( $fileb2_c ) . '"';
    $dep2 = '"' . OdePath::unixize( $fileb2_o ) . '"' . $depsep . 
            '"' . OdePath::unixize( $fileb5_h ) . '"';
    $reg_error = 0;
    $command = "gendep -I\"$depend_inc_dir\" \"$fileb2_c\" >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $tmpstr = 'gendep -I "file 2.c"';
    $numdeps = 2; # Test should generate two dependencies in dep file
    $status = OdeEnv::runOdeCommand( $command );
    # Check command exit code
    if ($status != 0)
    {
      $reg_error = 1;
      $error_txt = "gendep returned a non-zero exit code";
    }
    # Check if ".u" file exists
    if (!$reg_error && (!-f $fileb2_u))
    {
      $reg_error = 1;
      $error_txt = "Dependency file $fileb2_u not found.";
    }
    # Check for dependency line -> "file 2.o": "file 2.c"
    if (!$reg_error && (!OdeFile::findInFile( $dep1, $fileb2_u, 1)))
    {
      $reg_error = 1;
      $error_txt = "Dependency '$dep1' not found in $fileb2_u";
    }
    # Check for dependency line -> "file 2.o": "file 5.h"
    if (!$reg_error && (!OdeFile::findInFile( $dep2, $fileb2_u, 1)))
    {
      $reg_error = 1;
      $error_txt = "Dependency '$dep2' not found in $fileb2_u";
    }
    # Should only be numdeps dependencies in .u file
    if ( !$reg_error && 
         (OdeFile::numLinesInFile( $fileb2_u ) != ($numdeps + $numComments)))
    {
      $reg_error = 1;
      $error_txt = "Wrong number of lines in $fileb2_u";
    }
    if ($reg_error)
    {
      # Test case failed
      $error = 1;
      OdeInterface::printResult( "fail", "regression", "gendep", 7, $tmpstr );
      OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    }
    else
    {
      # Test case passed
      OdeInterface::printResult( "pass", "regression", "gendep", 7, $tmpstr );
    }
  
    # Remove existing ".u" files for next test case
    OdeFile::deltree( \@delete_list );
  }
  
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

  # test the following:
  # -s flag
  # -abs flag
  # -nodup flag
  # -table flag
  # more advanced conditional logic tests
  # -condfalse flag
  # -q flag
  # macro handling
  # Ode options
  # rules usage?
  # depend_all target?
  # usage in a workon?
}

#########################################
#
# sub createFiles
# - create the temporary source files
#   used to test gendep.
#
#########################################
sub createFiles ()
{  

  # Create temporary source files
  $depend_dir = $OdeEnv::tempdir . $OdeEnv::dirsep . "depend_dir";
  $depend_inc_dir = $depend_dir . $OdeEnv::dirsep . "inc";
  mkpath( $depend_inc_dir );
  $file1_c = $depend_dir . $OdeEnv::dirsep . "file1.c";
  $file2_c = $depend_dir . $OdeEnv::dirsep . "file2.c";
  $file1_h = $depend_dir . $OdeEnv::dirsep . "file1.h";
  $file2_h = $depend_dir . $OdeEnv::dirsep . "file2.h";
  $file3_h = $depend_dir . $OdeEnv::dirsep . "file3.h";
  $file4_h = $depend_dir . $OdeEnv::dirsep . "file4.h";
  $file5_h = $depend_inc_dir . $OdeEnv::dirsep . "file5.h";
  $file1_u = $depend_dir . $OdeEnv::dirsep . "file1.u";
  $file2_u = $depend_dir . $OdeEnv::dirsep . "file2.u";
  $file1_o = "file1" . $OdeEnv::obj_suff;
  $file2_o = "file2" . $OdeEnv::obj_suff;

  # file1.c - some simple preprocessor logic
  if (open( FILE1, "> $file1_c" )) {
    print( FILE1 "/* file1.c - temporary test file for gendep */\n");
    print( FILE1 "#define VAR1 1\n");
    print( FILE1 "#ifdef VAR2\n");
    print( FILE1 "#include \"file1.h\"\n");
    print( FILE1 "#elif defined( VAR1 )\n");
    print( FILE1 "#include \"file2.h\"\n");
    print( FILE1 "#else\n");
    print( FILE1 "#include \"file3.h\"\n");
    print( FILE1 "#endif\n");
    close( FILE1 );
  }
  else 
  {
    OdeInterface::logPrint( "Could not open file $file1_c\n" );
    return( 1 );
  }

  # file2.c - include files in other directory
  if (open( FILE2, "> $file2_c" )) {
    print( FILE2 "/* file2.c - temporary test file for gendep */\n");
    print( FILE2 "#include \"file5.h\"\n");
    close( FILE2 );
  }

  # file1.h - recursively include file4.h  
  if (open( INC1, "> $file1_h" ))
  {
  print( INC1 "/* file1.h - temporary test file for gendep */\n");
  print( INC1 "#include \"file4.h\"\n");
  close( INC1 );
  }
  else
  {
    OdeInterface::logPrint( "Could not open file $file1_h\n" );
    return( 1 );
  }


  # file2.h - basically empty
  if (open( INC2, "> $file2_h" ))
  {
  print( INC2 "/* file2.h - temporary test file for gendep */\n");
  close( INC2 );
  }
  else
  {
    OdeInterface::logPrint( "Could not open file $file2_h\n" );
    return( 1 );
  }
  # file3.h - basically empty
  if (open( INC3, "> $file3_h" ))
  {
  print( INC3 "/* file3.h - temporary test file for gendep */\n");
  close( INC3 );
  }
  else
  {
    OdeInterface::logPrint( "Could not open file $file3_h\n" );
    return( 1 );
  }
  # file4.h - basically empty
  if (open( INC4, "> $file4_h" ))
  {
  print( INC4 "/* file4.h - temporary test file for gendep */\n");
  close( INC4 );
  }
  else
  {
    OdeInterface::logPrint( "Could not open file $file4_h\n" );
    return( 1 );
  }
  # file4.5 - basically empty
  if (open( INC5, "> $file5_h" ))
  {
  print( INC5 "/* file5.h - temporary test file for gendep */\n");
  close( INC5 );
  }
  else
  {
    OdeInterface::logPrint( "Could not open file $file5_h\n" );
    return( 1 );
  }

  # - create the temporary source files (NT only)
  #   used to test gendep, with blanks in filenames.
  if ( $OdeEnv::WIN32 )
  {
    $depend_inbc_dir = $depend_dir . $OdeEnv::dirsep . "in c";
    mkpath( $depend_inbc_dir );
    $fileb2_c = $depend_dir . $OdeEnv::dirsep . "file 2.c";
    $fileb5_h = $depend_inc_dir . $OdeEnv::dirsep . "file 5.h";
    $fileb2_u = $depend_dir . $OdeEnv::dirsep . "file 2.u";
    $fileb2_o = "file 2" . $OdeEnv::obj_suff;
  
    # fileb2.c - include files in other directory
    if (open( FILE2, "> $fileb2_c" )) {
      print( FILE2 "/* \"file 2.c\" - temporary test file for gendep */\n");
      print( FILE2 "#include \"file 5.h\"\n");
      close( FILE2 );
    }
  
    # fileb5.h - basically empty
    if (open( INC5, "> $fileb5_h" ))
    {
    print( INC5 "/* \"file 5.h\" - temporary test file for gendep */\n");
    close( INC5 );
    }
    else
    {
      OdeInterface::logPrint( "Could not open file $fileb5_h\n" );
      return( 1 );
    }
  }

  return( 0 );

}


1;
