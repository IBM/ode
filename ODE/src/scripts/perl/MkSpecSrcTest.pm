###########################################
#
# MkSpecSrcTest module
# - module to test mk special sources
#   Also tests debug flag "t"
#
###########################################

package MkSpecSrcTest;

use File::Path;
use File::Copy;
use File::Basename;
use OdeEnv;
use OdeFile;
use OdeTest;
use OdeUtil;
use OdePath;
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
  my $dir = cwd();
  my @arg_list;
  my @str_list;
  my @delete_list;
  my $makefile;
  my $makefile_name;
  my $tdir1;
  my $tdir2;
  my $tdir3;
  my $tfile;
  my $tlink1;
  my $tlink2;
  my $tlink3;

  OdeInterface::logPrint( "in MkSpecSrcTest\n");
 
  ## Testing  special sources

  ## testing .INVISIBLE
  ## running mk -f var_impvars.mk
  @str_list = (
    "In src11, allsrc=",
    "In src11, oodate=",
    "In src1, allsrc=src12",
    "In src1, oodate=src12",
    "In src3, allsrc=src3a src3b",
    "In src3, oodate=src3a src3b",
    "In all, allsrc=src2",
    "In all, oodate=src2"
    );
  $status = runMkTest( "invisible.mk", "special source .INVISIBLE" , 
                       8, 1, \@str_list);
  if (!$error) { $error = $status; }

  ## testing .MAKE and .PMAKE
  ## running mk -f make1.mk
  @str_list = ("executing test1", "executing test2", "executing test3");
  $status = runMkTest( "make1.mk", "special sources .MAKE/.PMAKE" , 3, 2, \@str_list);
  if (!$error) { $error = $status; }
 
  ## running mk -f make1.mk -t
  @arg_list=("-t");
  @str_list = ("executing test1", "mk: Touching `test2'", 
               "mk: Unable to touch `test2'", "executing test3", "mk: Touching `all'", 
               "mk: Unable to touch `all'");
  $status = runMkTest( "make1.mk", "special sources .MAKE/.PMAKE" , 6, 3, \@str_list, 
                       \@arg_list );
  if (!$error) { $error = $status; }

  ## running mk -f make1.mk -n
  @arg_list=("-n");
  @str_list = ("executing test1", "echo executing test2", "executing test3" );
  $status = runMkTest( "make1.mk", "special sources .MAKE/.PMAKE", 3, 4, \@str_list, 
                       \@arg_list );
  if (!$error) { $error = $status; }

  ## running mk -f make2.mk
  @str_list = ("executing test1", "executing make");
  $status = runMkTest( "make2.mk", "special source .MAKE", 2, 5, \@str_list );
  if (!$error) { $error = $status; }

  ## running mk -f make2.mk .PMAKE
  @arg_list=(".PMAKE");
  @str_list = ("executing test1", "executing pmake");
  $status = runMkTest( "make2.mk", "special source .PMAKE", 2, 6, \@str_list, \@arg_list );
  if (!$error) { $error = $status; }
 
  ## running mk -f make3.mk -t
  @arg_list=("-t");
  @str_list = ("executing test2", "executing test3", "executing test4",
               "executing test1", "oodate is test2 test3 test4");
  $status = runMkTest( "make3.mk", "special source .MAKE", 5, 7, \@str_list, 
                       \@arg_list );
  if (!$error) { $error = $status; }
 
  ## running mk -f make4.mk -j5
  @arg_list=("-j5");
  @str_list = ("executing test2", "executing test2", "executing test2",
               "executing test3", "executing test4",
               "executing test1", "oodate is test2 test3 test4");
  $status = runPMkTest( "make4.mk", "special source .PMAKE", 7, 8, \@str_list, 
                       \@arg_list );
  if (!$error) { $error = $status; }
  
  
  ## testing .NOTMAIN
  ## running mk -f notmain.mk
  @str_list = ("executing targ4", "executing targ3");
  $status = runMkTest( "notmain.mk", "special source .NOTMAIN", 2, 9, 
                       \@str_list );
  if (!$error) { $error = $status; }

  ## running mk -f notmain_error1.mk
  $status = runMkErrorTest( "notmain_error1.mk", "special source .NOTMAIN", 10 );
  if (!$error) { $error = $status; }
  
  ## testing .SPECTARG
  ## running mk -f spectargmk -t
  @arg_list=("-t");
  @str_list = ("mk: Not touching `test1'", "mk: Touching `test21'", 
               "> WARNING: mk: Unable to touch `test21'",
               "mk: Not touching `test2'", "mk: Touching `test3'",
               "> WARNING: mk: Unable to touch `test3'");
  $status = runMkTest( 
                "spectarg.mk", "special source .SPECTARG", 6, 11, 
                \@str_list, \@arg_list );
  if (!$error) { $error = $status; }

  ## testing .PRECMDS .POSTCMDS and .REPLCMDS
  ## running mk -f cmds.mk
  @str_list = ("cmd6", "cmd7", "cmd3", "cmd4", "cmd1", "cmd2",
               "cmd5", "cmd6", "cmd7", "end_foo1", "cmda", "cmdb",
               "end_foo2", "cmdx", "cmdy");

  $status = runMkTest( 
                "cmds.mk", "special sources .PRE/POST/REPL/CMDS", 15, 12, 
                \@str_list );
  if (!$error) { $error = $status; }


  ## testing .PASSES
  ## running mk -f passes.mk
  $makefile_name = "spsources" . $OdeEnv::dirsep . "passes.mk";
  $makefile = dirname( OdeTest::getMakefile( $makefile_name ) );
  $tdir1 = join( $OdeEnv::dirsep, 
                 ($OdeEnv::tempdir, "makefiles", "spsources") );
  mkpath( $tdir1 );
  $makefile .= $OdeEnv::dirsep . "*";
  ## copying all the required makefiles to the temp directory
  ## as the test can be done only by being present in the makefile directory
  OdeEnv::runSystemCommand( "$OdeEnv::copy $makefile $tdir1" );

  if (!(-f ($tdir1 . $OdeEnv::dirsep . "passes.mk")) ||
      !(-f (join( $OdeEnv::dirsep, ($tdir1, "testdir1", "makefile") ))) ||
      !(-f (join( $OdeEnv::dirsep, ($tdir1, "testdir1", "mf.mk") ))) ||
      !(-f (join( $OdeEnv::dirsep, 
                 ($tdir1, "testdir1", "testdir11", "makefile") ))) ||
      !(-f (join( $OdeEnv::dirsep, ($tdir1, "testdir2", "makefile") ))))
  {
    OdeInterface::logPrint( 
                  "All the makefiles not copied to $OdeEnv::tempdir\n" );
  }
    
  ## cd'ing into the temp directory
  OdePath::chgdir( $tdir1 ); 
  $tdir1 =~ s/\\/\//g;
  $tdir2 = $tdir1 . "/" . "testdir1";
  $tdir3 = $tdir1 . "/" . "testdir2";
  @str_list = ("Pass: pass1, Action: action1", 
               "testdir1",
               "in $tdir2 looking for tgt1",
               "testdir1",
               "in $tdir1 looking for tgt1",
               "Pass: pass2, Action: action1",
               "testdir1",
               "in $tdir2 looking for tgt2",
               "testdir1",
               "testdir2",
               "in $tdir3 looking for tgt2",
               "testdir2",
               "in $tdir1 looking for tgt2",
               "Pass: pass3, Action: action1",
               "testdir11",
               "testdir11",
               "in $tdir1 looking for tgt2");
  $status = runMkTest( "passes.mk", "special source .PASSES", 17, 13, 
                       \@str_list );
  if (!$error) { $error = $status; }

  ## testing .DIRS
  $tdir1 = join( $OdeEnv::dirsep,
               ($OdeEnv::tempdir, "makefiles", "spsources", "all") );
  $tfile = join( $OdeEnv::dirsep,
               ($OdeEnv::tempdir, "makefiles", "spsources", "t1.c") );
  mkpath( $tdir1 );
  OdeFile::touchFile( $tfile, 1000 );
  @str_list = ("`all' is up to date");
  $status = runMkTest( "dirs.mk", "special source .DIRS", 1, 14, \@str_list );
  if (!$error) { $error = $status; }

  OdePath::chgdir( $dir );

  ## testing .NORMTARG
  ## running mk -f normtarg.mk
  @arg_list=("");
  @str_list = ("transform.b", "transform.a", "transform.b", ".b.a");
  $status = runMkTest( 
                "normtarg.mk", "special source .NORMTARG", 4, 15, 
                \@str_list, \@arg_list );
  if (!$error) { $error = $status; }

  ## testing debug flag "t"
  ## running mk -f notmain.mk -dt
  $status = runMkDebug_t( "notmain.mk", "debug flag \"t\"", 23, 2, 16 );
  if (!$error) { $error = $status; }
  ## running mk -f make2.mk -dt
  $status = runMkDebug_t( "make2.mk", "debug flag \"t\"", 9, 2, 17 );
  if (!$error) { $error = $status; }

  ## testing .LINKS
  ## running mk -f links.mk
  $tfile  = $OdeEnv::tempdir . $OdeEnv::dirsep . "tst1f";
  $tlink1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "tst1l";
  $tlink2 = $OdeEnv::tempdir . $OdeEnv::dirsep . "tst2l";
  $tlink3 = $OdeEnv::tempdir . $OdeEnv::dirsep . "tst3l";
  if (defined( $OdeEnv::UNIX ))
  {
    OdeEnv::runSystemCommand( "touch $tfile" );
    OdeEnv::runSystemCommand( "ln -s -f $tfile $tlink1" );
    if ((!(-f $tfile)) || (!(-l $tlink1)))
    {
      OdeInterface::logPrint( "$tfile or $tlink1 not created\n" );
    }
    @arg_list=("FILEPATH=$OdeEnv::tempdir");
    @str_list = ("ln -s -f $tfile $tlink2", "ln -s -f $tlink2 $tlink3");
    $status = runMkTest( "links.mk", "special source .LINKS", 2, 
                          18, \@str_list, \@arg_list );
    if (!$error) { $error = $status; }
    if (!(-l $tlink2) || !(-l $tlink3))
    {
      OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'},
                                    "mk", 18, "special source .LINKS" );
      OdeInterface::logPrint( 
             "Test Failed: $tlink2 or $tlink3 in test 13 not created\n" );
      if (!$error) { $error = 1; }
    }
    else
    {
      OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'},
                                    "mk", 18, "special source .LINKS" );
    }
  }
  else
  {
    OdeEnv::runSystemCommand( "echo \"\" > $tfile" );
    OdeEnv::runSystemCommand( "echo \"\" > $tlink1" );
    $status = runMkErrorTest( "links.mk", "special source .LINKS", 18 );
    if (!$error) { $error = $status; }
  }

                          
  ## testing .FORCEBLD
  ## running mk -f forcebld.mk
  @arg_list=("init -DTESTDIR=$OdeEnv::tempdir/");
  @str_list = ("initialization");
  # This creates the files for the following test.
  $status = runMkTest( 
                "forcebld.mk", "special source .FORCEBLD", 1, 19, 
                \@str_list, \@arg_list );
  if (!$error) { $error = $status; }

  @arg_list=("-DTESTDIR=$OdeEnv::tempdir/");
  @str_list = ("targ1", "targ2", "making targa and targb", "making targ.abc",
               "making targ.o", "cleanup",
               ".rrm $OdeEnv::tempdir/targ1 $OdeEnv::tempdir/targ2 $OdeEnv::tempdir/targa $OdeEnv::tempdir/targb", 
               ".rrm $OdeEnv::tempdir/targ.xyz $OdeEnv::tempdir/targ.abc $OdeEnv::tempdir/targ.o");
  $status = runMkTest( 
                "forcebld.mk", "special source .FORCEBLD", 8, 20, 
                \@str_list, \@arg_list );
  if (!$error) { $error = $status; }


  ## testing .REPLSRCS
  ## running mk -f replsrcs.mk
  @arg_list=("");
  @str_list = ("srcA", "srcB", "srcC", "targ1-cmd1", "srcE", "srcF",
               "targ2-cmd1", "srcH", "targ3+4-cmd1", "targ3+4-cmd2", "targ.a",
               "srcJ", "targ.b-cmd2", "targ.d", "srcM", "targ.c-cmd2");
  $status = runMkTest( 
                "replsrcs.mk", "special source .REPLSRCS", 16, 21, 
                \@str_list, \@arg_list );
  if (!$error) { $error = $status; }


  ## testing .LINKTARGS
  ## running mk -f linktargs.mk
  @arg_list=("");
  @str_list = ("targ1", "targ2", "targc", "making targa and targb", "targz",
               "making targy", "making targx");
  $status = runMkTest( 
                "linktargs.mk", "special source .LINKTARGS", 7, 22, 
                \@str_list, \@arg_list );
  if (!$error) { $error = $status; }



  @delete_list = ($OdeEnv::tempdir . $OdeEnv::dirsep . "makefiles", 
                  $tfile, $tlink1, $tlink2, $tlink3, $OdeTest::tmpfile);
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
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   num_tests: number of tests performed in the makefile
#   testno: test number
#   str_list: reference to a list of all the strings
#             to be looked for in the tempfile
#   args: reference to a list of any other commandline args
############################################################
sub runMkTest( $$$$$;$ )
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
  if (!defined( $args ))
  {
  	@$args[0] = "";
  }
  ## passes.mk is in the temp directory for test11
  if (($testno == 13) || ($testno == 14))
  {
    $makefile = $filename;
  }
  else
  {
    $makefile_name = "spsources" . $OdeEnv::dirsep . $filename;
    $makefile = OdeTest::getMakefile( $makefile_name );
    if (!$makefile)
    {
      OdeInterface::printError( "mk : $tststr" );
      OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
      return( 1 );
    }
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
      if (OdeFile::findInFile( @$str_list[$index], $OdeTest::tmpfile,
          0, $index+1) != ($index + 1))
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
# sub runPMkTest   (Parallel Mk test- multi threaded)
# - to make and run the command with all the
#   arguments passed and check the results.
#   Very similar to runMkTest, except that we check that
#   the order of the output is NOT the same as str_list
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   num_tests: number of tests performed in the makefile
#   testno: test number
#   str_list: reference to a list of all the strings
#             to be looked for in the tempfile
#             This is the order the results would be if 
#             it were single threaded.  So we ensure that
#             the order is NOT the same.
#   args: reference to a list of any other commandline args
############################################################
sub runPMkTest( $$$$$;$ )
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
  if (!defined( $args ))
  {
  	@$args[0] = "";
  }

    $makefile_name = "spsources" . $OdeEnv::dirsep . $filename;
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
      if (OdeFile::findInFile( @$str_list[$index], $OdeTest::tmpfile,
          0, $index+1) != ($index + 1))
      {
        $mk_error = 1;
        $error_txt = "Error occured while testing $tststr";
        last;
      }
    }
  }

  $mk_error = !$mk_error;

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
  my ($filename, $tststr, $testno, @args) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $status;
  my $error_txt;
  $makefile_name = "spsources" . $OdeEnv::dirsep . $filename;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::printError( "mk :$tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
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

####################################################################
#
# sub runMkDebug_t
# - to make and run the command with all the
#   arguments passed and the debug flag "t"
#   and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   debug_lines: number of debug lines. These are prepended by Targ:
#   num_tests: number of tests performed in the makefile
#   testno: test number
#   args: a list of any other commandline args
####################################################################
sub runMkDebug_t( $@ )
{
  my ($filename, $tststr, $debug_lines, $num_tests, $testno, @args) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $targ_count = 0;
  my $status;
  my $error_txt;
  $makefile_name = "spsources" . $OdeEnv::dirsep . $filename;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
  }
  $command = "mk -f $makefile @args -dt > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  # Check the number of debug lines
  $targ_count = OdeFile::numPatternsInFile( "Targ:", $OdeTest::tmpfile );
  if (!$mk_error && ($targ_count != $debug_lines))
  {
    $mk_error = 1;
    $error_txt = "Output has incorrect number of debug lines";
  }
  $targ_count = $debug_lines + $num_tests;
  if (!$mk_error &&
          (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != $targ_count))
  {
    $mk_error = 1;
    $error_txt = "Output has incorrect number of lines";
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

1;
