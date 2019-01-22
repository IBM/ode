###########################################
#
# MkTargDepTest module
# - module to test mk target dependencies
#   also tests "-a" commandline flag
#
###########################################

package MkTargDepTest;

use File::Path;
use OdeEnv;
use OdeFile;
use OdeTest;
use OdePath;
use OdeUtil;
use OdeInterface;
use strict;
use Cwd;

#########################################
#
# sub run
#
#########################################
sub run ()
{
  my $status;
  my $error = 0;
  my $mk_error = 0;
  my $command;
  my $error_txt;
  my @delete_list;
  my $tfile1;
  my $tfile2;
  my $tfile3;
  my $tfile4;
  my $mtime1;
  my $mtime2;
  my $mtime3;
  my $makefile_dir;
  my $makefile;
  my $dir = cwd();

  OdeInterface::logPrint( "in MkTargDepTest\n");
 
  ## Testing target dependencies

  ## locating the makefiles directory
  $makefile_dir = join( $OdeEnv::dirsep, (".", "makefiles", "dependencies") );
  if (! -d $makefile_dir)
  {
    $makefile_dir = join( $OdeEnv::dirsep, ($OdeEnv::bb_base, "src", "scripts",
                          "perl", "makefiles", "dependencies") );
    if (! -d $makefile_dir)
    {
      OdeInterface::printError( "mk : target dependencies" );
      OdeInterface::logPrint( 
          "Could not locate makefiles directory - $makefile_dir\n" );
      return( 1 );
    }
  }
                            
  $makefile = $makefile_dir . $OdeEnv::dirsep . "*"; 
  ## copying all the makefiles into the temp directory
  OdeEnv::runSystemCommand( "$OdeEnv::copy $makefile $OdeEnv::tempdir" );
  ## cd'ing to the temp directory having the makefiles
  OdePath::chgdir( $OdeEnv::tempdir );

  ## running mk -f colon1.mk
  ## testing the use of ":" in the makefile 

  ## target t.o, sources t1.c t2.c doesn't exist
  $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "t.o";
  $tfile2 = $OdeEnv::tempdir . $OdeEnv::dirsep . "t1.c";
  $tfile3 = $OdeEnv::tempdir . $OdeEnv::dirsep . "t2.c";
  OdeInterface::logPrint( 
                    "ODETEST: $tfile1, $tfile2, $tfile3 doesn't exist\n" );
  $status = runMkTest( "colon1.mk", $tfile1, "changed", $tfile2, "changed",
                       $tfile3, "changed", "target dependencies \":\"", 1 );
  if (!$error) { $error = $status; }

  ## source $tfile2 is modified recently
  OdeInterface::logPrint( "ODETEST: a source $tfile2 is recently modified\n" );
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 100000 );
  OdeFile::touchFile( $tfile3, 200000 );
  $status = runMkTest( "colon1.mk", $tfile1, "changed", $tfile2,
            "unchanged", $tfile3, "unchanged", "target dependencies \":\"", 2 );
  if (!$error) { $error = $status; }

  ## using -a when source $tfile2 is modified recently
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 100000 );
  OdeFile::touchFile( $tfile3, 200000 );
  $status = runMkTest( "colon1.mk", $tfile1, "changed", $tfile2,
          "changed", $tfile3, "changed", "target dependencies \":\"", 3, "-a" );
  if (!$error) { $error = $status; }

  ## source $tfile3 is modified recently
  OdeInterface::logPrint( "ODETEST: a source $tfile3 is recently modified\n" );
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 200000 );
  OdeFile::touchFile( $tfile3, 100000 );
  $status = runMkTest( "colon1.mk", $tfile1, "changed", $tfile2,
           "unchanged", $tfile3, "unchanged", "target dependencies \":\"", 4 );
  if (!$error) { $error = $status; }

  ## both the sources $tfile2 and $tfile3 modified recently
  OdeInterface::logPrint( 
      "ODETEST: sources $tfile2, $tfile3 are recently modified\n" );
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 100000 );
  OdeFile::touchFile( $tfile3, 100000 );
  $status = runMkTest( "colon1.mk", $tfile1, "changed", $tfile2,
           "unchanged", $tfile3, "unchanged", "target dependencies \":\"", 5 );
  if (!$error) { $error = $status; }

  ## both the sources $tfile2 and $tfile3 are up to date
  OdeInterface::logPrint( 
        "ODETEST: sources $tfile2, $tfile3 are up to date\n" );
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 200000 );
  OdeFile::touchFile( $tfile3, 200000 );
  $status = runMkTest( "colon1.mk", $tfile1, "unchanged", $tfile2,
            "unchanged", $tfile3, "unchanged", "target dependencies \":\"", 6 );
  if (!$error) { $error = $status; }

  ## using -a when both the sources $tfile2 and $tfile3 are up to date
  $status = runMkTest( "colon1.mk", $tfile1, "changed", $tfile2,
          "changed", $tfile3, "changed", "target dependencies \":\"", 7, "-a" );
  if (!$error) { $error = $status; }

  ## target $tfile1 is recently modified than its sources
  OdeInterface::logPrint( "ODETEST: target $tfile1 is recently modified\n" );
  OdeFile::touchFile( $tfile1, 100000 );
  OdeFile::touchFile( $tfile2, 200000 );
  OdeFile::touchFile( $tfile3, 200000 );
  $status = runMkTest( "colon1.mk", $tfile1, "unchanged", $tfile2,
            "unchanged", $tfile3, "unchanged", "target dependencies \":\"", 8 );
  if (!$error) { $error = $status; }

  ## using -a when target $tfile1 is modified recently than its sources
  $status = runMkTest( "colon1.mk", $tfile1, "changed", $tfile2,
          "changed", $tfile3, "changed", "target dependencies \":\"", 9, "-a" );
  if (!$error) { $error = $status; }

  if ( $OdeEnv::WIN32 )
  {
    ## running mk -f colon1blank.mk
    ## testing the use of ":" in the makefile with embedded blanks in filenames
  
    ## target t.o, sources t1.c t2.c doesn't exist
    $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "t t.o";
    $tfile2 = $OdeEnv::tempdir . $OdeEnv::dirsep . "t 1.c";
    $tfile3 = $OdeEnv::tempdir . $OdeEnv::dirsep . "t 2.c";
    OdeInterface::logPrint( 
                      "ODETEST: $tfile1, $tfile2, $tfile3 doesn't exist\n" );
    $status = runMkTest( "colon1blank.mk", $tfile1, "changed", $tfile2, "changed",
                         $tfile3, "changed", "dependencies - blanks in filenames \":\"", 1 );
    if (!$error) { $error = $status; }
  
    ## source $tfile2 is modified recently
    OdeInterface::logPrint( "ODETEST: a source $tfile2 is recently modified\n" );
    OdeFile::touchFile( $tfile1, 200000 );
    OdeFile::touchFile( $tfile2, 100000 );
    OdeFile::touchFile( $tfile3, 200000 );
    $status = runMkTest( "colon1blank.mk", $tfile1, "changed", $tfile2,
              "unchanged", $tfile3, "unchanged", "dependencies - blanks in filenames \":\"", 2 );
    if (!$error) { $error = $status; }
  
    ## using -a when source $tfile2 is modified recently
    OdeFile::touchFile( $tfile1, 200000 );
    OdeFile::touchFile( $tfile2, 100000 );
    OdeFile::touchFile( $tfile3, 200000 );
    $status = runMkTest( "colon1blank.mk", $tfile1, "changed", $tfile2,
            "changed", $tfile3, "changed", "dependencies - blanks in filenames \":\"", 3, "-a" );
    if (!$error) { $error = $status; }
  
    ## source $tfile3 is modified recently
    OdeInterface::logPrint( "ODETEST: a source $tfile3 is recently modified\n" );
    OdeFile::touchFile( $tfile1, 200000 );
    OdeFile::touchFile( $tfile2, 200000 );
    OdeFile::touchFile( $tfile3, 100000 );
    $status = runMkTest( "colon1blank.mk", $tfile1, "changed", $tfile2,
             "unchanged", $tfile3, "unchanged", "dependencies - blanks in filenames \":\"", 4 );
    if (!$error) { $error = $status; }
  
    ## both the sources $tfile2 and $tfile3 modified recently
    OdeInterface::logPrint( 
        "ODETEST: sources $tfile2, $tfile3 are recently modified\n" );
    OdeFile::touchFile( $tfile1, 200000 );
    OdeFile::touchFile( $tfile2, 100000 );
    OdeFile::touchFile( $tfile3, 100000 );
    $status = runMkTest( "colon1blank.mk", $tfile1, "changed", $tfile2,
             "unchanged", $tfile3, "unchanged", "dependencies - blanks in filenames \":\"", 5 );
    if (!$error) { $error = $status; }
  
    ## both the sources $tfile2 and $tfile3 are up to date
    OdeInterface::logPrint( 
          "ODETEST: sources $tfile2, $tfile3 are up to date\n" );
    OdeFile::touchFile( $tfile1, 200000 );
    OdeFile::touchFile( $tfile2, 200000 );
    OdeFile::touchFile( $tfile3, 200000 );
    $status = runMkTest( "colon1blank.mk", $tfile1, "unchanged", $tfile2,
              "unchanged", $tfile3, "unchanged", "dependencies - blanks in filenames \":\"", 6 );
    if (!$error) { $error = $status; }
  
    ## using -a when both the sources $tfile2 and $tfile3 are up to date
    $status = runMkTest( "colon1blank.mk", $tfile1, "changed", $tfile2,
            "changed", $tfile3, "changed", "dependencies - blanks in filenames \":\"", 7, "-a" );
    if (!$error) { $error = $status; }
  
    ## target $tfile1 is recently modified than its sources
    OdeInterface::logPrint( "ODETEST: target $tfile1 is recently modified\n" );
    OdeFile::touchFile( $tfile1, 100000 );
    OdeFile::touchFile( $tfile2, 200000 );
    OdeFile::touchFile( $tfile3, 200000 );
    $status = runMkTest( "colon1blank.mk", $tfile1, "unchanged", $tfile2,
              "unchanged", $tfile3, "unchanged", "dependencies - blanks in filenames \":\"", 8 );
    if (!$error) { $error = $status; }
  
    ## using -a when target $tfile1 is modified recently than its sources
    $status = runMkTest( "colon1blank.mk", $tfile1, "changed", $tfile2,
            "changed", $tfile3, "changed", "dependencies - blanks in filenames \":\"", 9, "-a" );
    if (!$error) { $error = $status; }
  
  }

  ## running mk -f colon2.mk
  ## testing ":" with a target repeating in the makefile
  $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "t.o";
  $tfile2 = $OdeEnv::tempdir . $OdeEnv::dirsep . "t1.c";
  $tfile3 = $OdeEnv::tempdir . $OdeEnv::dirsep . "t2.c";
  $tfile4 = $OdeEnv::tempdir . $OdeEnv::dirsep . "colon2.mk";
  if (! -f $tfile4)
  {
    OdeInterface::logPrint( "Could not locate makefile - $tfile4\n" );
  }
  $command = "mk -f $tfile4 > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  if (!$mk_error && (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != 5))
  {
    $mk_error = 1;
    $error_txt = "Incorrect number of targets executed";
  }
  if (!$mk_error && 
       (OdeFile::findInFile( "making second all", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "an unexpected target executed";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                               10, "target dependencies \":\"" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                               10, "target dependencies \":\"" );
  }

  ## removing temporary files t.o, t1.c t2.c
  @delete_list = ($tfile1, $tfile2, $tfile3);
  OdeFile::deltree( \@delete_list );
  if ((-f $tfile1) || (-f $tfile2) || (-f $tfile3))
  {
    OdeInterface::logPrint( "$tfile1 or $tfile2 or $tfile3 not deleted\n" );
  }

  ## running mk -f bang1.mk
  ## testing the use of "!" in the makefile

  ## target $tfile1, sources $tfile2, $tfile3 doesn't exist
  OdeInterface::logPrint( 
                     "ODETEST: $tfile1, $tfile2, $tfile3 doesn't exist\n" );
  $status = runMkTest( "bang1.mk", $tfile1, "changed", $tfile2, "changed",
                       $tfile3, "changed", "target dependencies \"!\"", 11 );
  if (!$error) { $error = $status; }

  ## both the sources $tfile2 and $tfile3 are uptodate
  OdeInterface::logPrint( 
                      "ODETEST: sources $tfile2, $tfile3 are up to date\n" );
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 200000 );
  OdeFile::touchFile( $tfile3, 200000 );
  $status = runMkTest( "bang1.mk", $tfile1, "changed", $tfile2, "changed",
                       $tfile3, "changed", "target dependencies \"!\"", 12 );
  if (!$error) { $error = $status; }

  ## using -a when both the sources $tfile2 and $tfile3 are uptodate
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 200000 );
  OdeFile::touchFile( $tfile3, 200000 );
  $status = runMkTest( "bang1.mk", $tfile1, "changed", $tfile2, "changed",
                    $tfile3, "changed", "target dependencies \"!\"", 13, "-a" );
  if (!$error) { $error = $status; }

  ## source $tfile2 is recently modified
  OdeInterface::logPrint( "ODETEST: source $tfile2 is recently modified\n" );
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 100000 );
  OdeFile::touchFile( $tfile3, 200000 );
  $status = runMkTest( "bang1.mk", $tfile1, "changed", $tfile2, "changed",
                       $tfile3, "changed", "target dependencies \"!\"", 14 );
  if (!$error) { $error = $status; }

  ## using -a when source $tfile2 is recently modified
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 100000 );
  OdeFile::touchFile( $tfile3, 200000 );
  $status = runMkTest( "bang1.mk", $tfile1, "changed", $tfile2, "changed",
                    $tfile3, "changed", "target dependencies \"!\"", 15, "-a" );
  if (!$error) { $error = $status; }

  ## source $tfile3 is recently modified
  OdeInterface::logPrint( "ODETEST: source $tfile3 is recently modified\n" );
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 200000 );
  OdeFile::touchFile( $tfile3, 100000 );
  $status = runMkTest( "bang1.mk", $tfile1, "changed", $tfile2, "changed",
                    $tfile3, "changed", "target dependencies \"!\"", 16 );
  if (!$error) { $error = $status; }

  ## both the sources $tfile2 and $tfile3 are recently modified
  OdeInterface::logPrint( 
           "ODETEST: sources $tfile2, $tfile3 are recently modified\n" );
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 100000 );
  OdeFile::touchFile( $tfile3, 100000 );
  $status = runMkTest( "bang1.mk", $tfile1, "changed", $tfile2, "changed",
                    $tfile3, "changed", "target dependencies \"!\"", 17 );
  if (!$error) { $error = $status; }

  ## target $tfile1 is recently modified than its sources
  OdeInterface::logPrint( "ODETEST: target $tfile1 is recently modified\n" );
  OdeFile::touchFile( $tfile1, 100000 );
  OdeFile::touchFile( $tfile2, 200000 );
  OdeFile::touchFile( $tfile3, 200000 );
  $status = runMkTest( "bang1.mk", $tfile1, "changed", $tfile2, "changed",
                    $tfile3, "changed", "target dependencies \"!\"", 18 );
  if (!$error) { $error = $status; }

  ## using -a when target $tfile1 is modified recently than its sources
  OdeFile::touchFile( $tfile1, 100000 );
  OdeFile::touchFile( $tfile2, 200000 );
  OdeFile::touchFile( $tfile3, 200000 );
  $status = runMkTest( "bang1.mk", $tfile1, "changed", $tfile2, "changed",
                    $tfile3, "changed", "target dependencies \"!\"", 19, "-a" );
  if (!$error) { $error = $status; }

  ## running mk -f bang2.mk
  ## testing "!" with a target repeating in the makefile
  $tfile4 = $OdeEnv::tempdir . $OdeEnv::dirsep . "bang2.mk";
  if (! -f $tfile4)
  {
    OdeInterface::logPrint( "Could not locate makefile - $tfile4\n" );
  }
  $command = "mk -f $tfile4 > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  if (!$mk_error && (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != 5))
  {
    $mk_error = 1;
    $error_txt = "Incorrect number of targets executed";
  }
  if (!$mk_error && 
       (OdeFile::findInFile( "making second all", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "an unexpected target executed";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                               20, "target dependencies \"!\"" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                               20, "target dependencies \"!\"" );
  }

  OdeFile::deltree( \@delete_list );
  if ((-f $tfile1) || (-f $tfile2) || (-f $tfile3))
  {
    OdeInterface::logPrint( "$tfile1 or $tfile2 or $tfile3 not deleted\n" );
  }

  ## running mk -f doublecolon1.mk
  ## testing the use of "::" in the makefile

  ## target t.o, sources t1.c t2.c doesn't exist
  OdeInterface::logPrint( 
                     "ODETEST: $tfile1, $tfile2, $tfile3 doesn't exist\n" );
  $status = runMkTest( 
              "doublecolon1.mk", $tfile1, "changed", $tfile2, "changed",
                       $tfile3, "changed", "target dependencies \"::\"", 21 );
  if (!$error) { $error = $status; }

  ## source $tfile2 is recently modified
  OdeInterface::logPrint( "ODETEST: source $tfile2 is recently modified\n" );
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 100000 );
  OdeFile::touchFile( $tfile3, 200000 );
  $status = runMkTest( 
              "doublecolon1.mk", $tfile1, "changed", $tfile2, "changed",
                         $tfile3, "changed", "target dependencies \"::\"", 22 );
  if (!$error) { $error = $status; }

  ## source $tfile3 is recently modified
  OdeInterface::logPrint( "ODETEST: source $tfile3 is recently modified\n" );
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 200000 );
  OdeFile::touchFile( $tfile3, 100000 );
  $status = runMkTest( 
              "doublecolon1.mk", $tfile1, "changed", $tfile2, "changed",
                        $tfile3, "changed", "target dependencies \"::\"", 23 );
  if (!$error) { $error = $status; }

  ## both the sources $tfile2 and $tfile3 are recently modified
  OdeInterface::logPrint( 
           "ODETEST: sources $tfile2, $tfile3 are recently modified\n" );
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 100000 );
  OdeFile::touchFile( $tfile3, 100000 );
  $status = runMkTest( 
              "doublecolon1.mk", $tfile1, "changed", $tfile2, "changed",
                        $tfile3, "changed", "target dependencies \"::\"", 24 );
  if (!$error) { $error = $status; }

  ## both the sources $tfile2 and $tfile3 are uptodate
  OdeInterface::logPrint( 
                      "ODETEST: sources $tfile2, $tfile3 are up to date\n" );
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 200000 );
  OdeFile::touchFile( $tfile3, 200000 );
  $status = runMkTest( 
              "doublecolon1.mk", $tfile1, "changed", $tfile2, "changed",
                        $tfile3, "changed", "target dependencies \"::\"", 25 );
  if (!$error) { $error = $status; }

  ## target $tfile1 is recently modified
  OdeInterface::logPrint( "ODETEST: target $tfile1 is recently modified\n" );
  OdeFile::touchFile( $tfile1, 100000 );
  OdeFile::touchFile( $tfile2, 200000 );
  OdeFile::touchFile( $tfile3, 200000 );
  $status = runMkTest( 
              "doublecolon1.mk", $tfile1, "changed", $tfile2, "changed",
                        $tfile3, "changed", "target dependencies \"::\"", 26 );
  if (!$error) { $error = $status; }

  OdeFile::deltree( \@delete_list );
  if ((-f $tfile1) || (-f $tfile2) || (-f $tfile3))
  {
    OdeInterface::logPrint( "$tfile1 or $tfile2 or $tfile3 not deleted\n" );
  }

  ## running mk -f doublecolon2.mk
  ## target t.o, sources t1.c t2.c doesn't exist
  OdeInterface::logPrint( 
                     "ODETEST: $tfile1, $tfile2, $tfile3 doesn't exist\n" );
  $status = runMkTest( 
              "doublecolon2.mk", $tfile1, "changed", $tfile2, "changed",
                         $tfile3, "changed", "target dependencies \"::\"", 27 );
  if (!$error) { $error = $status; }

  ## both the sources $tfile2 and $tfile3 are uptodate
  OdeInterface::logPrint( 
                    "ODETEST: sources $tfile2, $tfile3 are up to date\n" );
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 200000 );
  OdeFile::touchFile( $tfile3, 200000 );
  $status = runMkTest( 
              "doublecolon2.mk", $tfile1, "unchanged", $tfile2, "unchanged",
                      $tfile3, "unchanged", "target dependencies \"::\"", 28 );
  if (!$error) { $error = $status; }
  
  ## using -a when both the sources $tfile2 and $tfile3 are uptodate
  $status = runMkTest( 
              "doublecolon2.mk", $tfile1, "changed", $tfile2, "changed",
                   $tfile3, "changed", "target dependencies \"::\"", 29, "-a" );
  if (!$error) { $error = $status; }

  OdeFile::deltree( \@delete_list );
  if ((-f $tfile1) || (-f $tfile2) || (-f $tfile3))
  {
    OdeInterface::logPrint( "$tfile1 or $tfile2 or $tfile3 not deleted\n" );
  }

  ## running mk -f doublecolon3.mk
  ## testing "::" with a target repeating in the makefile
  ## target t.o, sources t1.c t2.c doesn't exist
  OdeInterface::logPrint( 
             "ODETEST: $tfile1, $tfile2, $tfile3 doesn't exist\n" );
  $status = runMkTest( 
              "doublecolon3.mk", $tfile1, "changed", $tfile2, "changed",
                        $tfile3, "changed", "target dependencies \"::\"", 30 );
  if (!$error) { $error = $status; }

  ## source $tfile2 is recently modified
  OdeInterface::logPrint( "ODETEST: source $tfile2 is recently modified\n" );
  OdeFile::touchFile( $tfile1, 200000 );
  OdeFile::touchFile( $tfile2, 100000 );
  OdeFile::touchFile( $tfile3, 200000 );
  $status = runMkTest( 
              "doublecolon3.mk", $tfile1, "changed", $tfile2, "changed",
                      $tfile3, "unchanged", "target dependencies \"::\"", 31 );
  if (!$error) { $error = $status; }
  
  OdeFile::deltree( \@delete_list );
  if ((-f $tfile1) || (-f $tfile2) || (-f $tfile3))
  {
    OdeInterface::logPrint( "$tfile1 or $tfile2 or $tfile3 not deleted\n" );
  }
  ## repeating a target with : and ! operators
  $status = runMkErrorTest( "colon_bang_error.mk", $tfile1, $tfile2, 
                                  "target dependencies \":\", \"!\"", 32 );
  if (!$error) { $error = $status; }

  OdeFile::deltree( \@delete_list );
  if ((-f $tfile1) || (-f $tfile2) || (-f $tfile3))
  {
    OdeInterface::logPrint( "$tfile1 or $tfile2 or $tfile3 not deleted\n" );
  }
  ## repeating a target with : and :: operators
  $status = runMkErrorTest( "colon_dcolon_error.mk", $tfile1, $tfile2, 
                                 "target dependencies \":\", \"::\"", 33 );

  OdeFile::deltree( \@delete_list );
  if ((-f $tfile1) || (-f $tfile2) || (-f $tfile3))
  {
    OdeInterface::logPrint( "$tfile1 or $tfile2 or $tfile3 not deleted\n" );
  }
  ## repeating a target with ! and :: operators
  $status = runMkErrorTest( "bang_dcolon_error.mk", $tfile1, $tfile2, 
                                  "target dependencies \"!\", \"::\"", 34 );

  ## cd'ing back to the current directory
  OdePath::chgdir( $dir );

  @delete_list = ($OdeEnv::tempdir . $OdeEnv::dirsep . "colon1.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "colon2.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "bang1.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "bang2.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "doublecolon1.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "doublecolon2.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "doublecolon3.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "colon_bang_error.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "colon_dcolon_error.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "bang_dcolon_error.mk",
                  $tfile1, $tfile2, $tfile3, $OdeTest::tmpfile);
  OdeFile::deltree( \@delete_list );
  return $error;
}

###########################################################
#
# sub runMkTest
# - to make and run the command with all the
#   arguments passed and check the results
#
# - arguments
#   makefile: the name of the makefile
#   tfile1, tfile2, tfile3: test files used in the makefile
#   tfile1_stamp, tfile2_stamp, tfile3_stamp: strings which
#      indicate if the respective test files are made. Can
#      be either changed or unchanged
#   tststr: the string to be output in the result
#   testno: test number
#   arg_list: the list of arguments to be appended to the command 
############################################################
sub runMkTest( $$$$$$$$$@ )
{
  my( $makefile, $tfile1, $tfile1_stamp, $tfile2, $tfile2_stamp, 
      $tfile3, $tfile3_stamp, $tststr, $testno, @arg_list ) = @_;
  my $status;
  my $mk_error = 0;
  my $command;
  my $error_txt;
  # Getting the last modification times of the test files
  my $mtime1 = (stat $tfile1)[9];
  my $mtime2 = (stat $tfile2)[9];
  my $mtime3 = (stat $tfile3)[9];
  $makefile = $OdeEnv::tempdir . $OdeEnv::dirsep . $makefile;
  if (! -f $makefile)
	{
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile\n" );
    return( 1 );
	}

  $command = "mk -f $makefile @arg_list > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }

  if (!$mk_error && 
        (($tfile1_stamp eq "changed") && ((stat $tfile1)[9] == $mtime1)))
  {
    $mk_error = 1;
    $error_txt = "target $tfile1 is not made";
  }
  if (!$mk_error && 
         (($tfile1_stamp eq "unchanged") && ((stat $tfile1)[9] != $mtime1)))
  {
    $mk_error = 1;
    $error_txt = "target $tfile1 is made";
  }
  if (!$mk_error && 
        (($tfile2_stamp eq "changed") && ((stat $tfile2)[9] == $mtime2)))
  {
    $mk_error = 1;
    $error_txt = "source $tfile2 is not made";
  }
  if (!$mk_error && 
         (($tfile2_stamp eq "unchanged") && ((stat $tfile2)[9] != $mtime2)))
  {
    $mk_error = 1;
    $error_txt = "source $tfile2 is made";
  }
  if (!$mk_error && 
        (($tfile3_stamp eq "changed") && ((stat $tfile3)[9] == $mtime3)))
  {
    $mk_error = 1;
    $error_txt = "source $tfile3 is not made";
  }
  if (!$mk_error && 
         (($tfile3_stamp eq "unchanged") && ((stat $tfile3)[9] != $mtime3)))
  {
    $mk_error = 1;
    $error_txt = "source $tfile3 is made";
  }
  if ($mk_error)
  {
    # Test Case failed
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

###########################################################
#
# sub runMkErrorTest
# - to make and run the command with all the
#   arguments passed and check the results
#
# - arguments
#   makefile: the name of the makefile
#   tststr: the string to be output in the result
#   tfile1, tfile2: test files used in the makefile
#   testno: test number
#   args: a list of any other commandline args
############################################################
sub runMkErrorTest( $$$@ )
{
  my ($makefile, $tfile1, $tfile2, $tststr, $testno, @args) = @_;
  my $command;
  my $mk_error = 0;
  my $status;
  my $error_txt;
  $makefile = $OdeEnv::tempdir . $OdeEnv::dirsep . $makefile;
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
  if (!$mk_error && (-f $tfile1))
  {
    $mk_error = 1;
    $error_txt = "mk created $tfile1";
  }
  if (!$mk_error && (-f $tfile2))
  {
    $mk_error = 1;
    $error_txt = "mk created $tfile2";
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
1;
