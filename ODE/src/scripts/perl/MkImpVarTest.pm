###############################################
#
# MkImpVarTest module
# - module to test mk implied variables
#   Also tests debug flags a,d,g1,g2,j,m,v,V,A
#
###############################################

package MkImpVarTest;

use File::Path;
use OdeEnv;
use OdeFile;
use OdeTest;
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
  my $mfile;
  my $mfiledir;
  my $test4;
  my $test;
  my $status;
  my $error = 0;
  my $dir = cwd();
  my @arg_list;
  my @str_list;
  my @delete_list;
  my $testfile = join( $OdeEnv::dirsep, ($OdeEnv::tempdir, "testdir1",
                                         "testdir2", "testfile.mk") );

  OdeInterface::logPrint( "in MkImpVarTest\n");
 
  ## Testing implied variables 
  ## running mk -f var_impvars.mk
  if (defined( $OdeEnv::UNIX ))
  {
    @arg_list=("-DUNIX");
    @str_list = ("dollar is \$", "make is mk", "curdir is $dir", 
                 "makeflags is -DUNIX=1", "pound is \#"); 
  }
  else
  {
    @str_list = ("dollar is \$", "make is mk", "curdir is $dir", 
                 "makeflags is", "pound is \"\#\""); 
  }
  $status = runMkTest( "var_impvars.mk", 
              "implied variables \$,MAKE,CURDIR,MAKEFLAGS,ODERELEASE,POUND",
              6, 1, \@str_list, \@arg_list );
  if (!$error) { $error = $status; }

  ##running mk -f var_allsrc.mk
  @str_list = ("using allsrc, test1 test2 test3",
               "using symbol, test1 test2 test3");
  $status = runMkTest( "var_allsrc.mk", "implied variables .ALLSRC", 2, 2,  
                       \@str_list, \@arg_list );
  if (!$error) { $error = $status; }

  ##running mk -f var_impsrc.mk
  @str_list = ("using impsrc, test.c", "using symbol, test.c",
               "using symbol without braces, test.c",
               "using impsrc, test.o", "using symbol, test.o");
  $status = runMkTest( "var_impsrc.mk", "implied variables .IMPSRC", 5, 3,  
                       \@str_list, \@arg_list );
  if (!$error) { $error = $status; }

  ##running mk -f var_oodate.mk
  $mfile = join( $OdeEnv::dirsep, ( ".", "makefiles", "variables", "var_oodate.mk" ) );
  $mfiledir = join( $OdeEnv::dirsep, ( $OdeEnv::tempdir,  "makefiles",  "variables" ) );
  mkpath( $mfiledir );
  $status = OdeEnv::runSystemCommand( "$OdeEnv::copy $mfile $mfiledir" );
  if ($status)
  {
    OdeFile::deltree( $OdeEnv::tempdir );
    die("copy failed!\n");
  }

  OdePath::chgdir( $OdeEnv::tempdir );
  @delete_list = ( "test4", "test4a", "test4b", ($OdeEnv::tempdir . "makefiles") );
  OdeFile::touchFile( "test4", 5000 );
  OdeFile::deltree( "test4a" );
  OdeFile::touchFile( "test4b" );
  $test4 = "test4 is " . $OdeEnv::tempdir . $OdeEnv::dirsep . "test4a " .
            $OdeEnv::tempdir . $OdeEnv::dirsep . "test4b";
  $test4 = OdePath::unixize( $test4 );
  $test  = "test is test1 test3 " . $OdeEnv::tempdir . $OdeEnv::dirsep . "test4";
  $test  = OdePath::unixize( $test );
  @str_list = ("test1 is test1a", "test2 is test2a", "test3a is test3b",
               "test3 is", $test4, $test );
  $status = runMkTest( "var_oodate.mk", "implied variables .OODATE", 6, 4,
                       \@str_list, \@arg_list );
  if (!$error) { $error = $status; }
  OdeFile::deltree( \@delete_list );
  OdePath::chgdir( $dir );

  ##running mk -f var_prefix.mk
  @str_list = ("src", "second test", "test.c.h", "first test", "test.c" );
  $status = runMkTest( "var_prefix.mk", "implied variables .PREFIX" , 5, 5,  
                       \@str_list, \@arg_list );
  if (!$error) { $error = $status; }

  ##running mk -f var_archive_member.mk
  @str_list=("test1 archive is tlib.b", "test1 archive using symbol is tlib.b",
             "test1 member is foo.b", "test1 member using symbol is foo.b",
             "test2 archive is tlib", "test2 member is foo",
             "test3 archive is tlib.a.b", "test3 member is foo.a.b");
  $status = runMkTest( "var_archive_member.mk", 
                       "implied variables .ARCHIVE .MEMBER", 8, 6,  
                       \@str_list, \@arg_list );
  if (!$error) { $error = $status; }
  
  ##running mk -f var_target.mk
  @str_list = ("target for test1 is test1", "target for test2 is test2",
               "target for test3 is test2.c", "target is targ1");
  $status = runMkTest( "var_target.mk", "implied variables .TARGET", 4, 7,  
                       \@str_list, \@arg_list );
  if (!$error) { $error = $status; }

  ##running mk -f var_target_error1.mk
  $status = runMkErrorTest( "var_target_error1.mk", 
                            "implied variables .TARGET", 8 );  
  if (!$error) { $error = $status; }

  ##running mk -f var_targets.mk
  @str_list = ("targets for test1 are targ1 targ2",
               "targets for test2 are targ1 targ2",
               "targets for test3 are targ1 targ2",
               "targets are targ1 targ2");
  @arg_list = ("targ1", "targ2");
  $status = runMkTest( "var_targets.mk", "implied variables .TARGETS", 5, 9,  
                       \@str_list, \@arg_list );
  if (!$error) { $error = $status; }

  ## creating testfile.mk for the next test
  mkpath( $OdeEnv::tempdir . $OdeEnv::dirsep . "testdir1" . 
          $OdeEnv::dirsep . "testdir2" );
  open( INPUT, "> $testfile") ||
     OdeInterface::logPrint( "Could not open file $testfile\n" );
  print( INPUT "testvar=100\n" );
  close( INPUT );
  if (!(-f $testfile))
  {
    OdeInterface::printError( "mk : implied variables" );
    OdeInterface::logPrint( "Could not locate $testfile\n" );
    return( 1 );
  }

  ##running mk -f var_vpath.mk
  $testfile = join( $OdeEnv::dirsep, 
                    ($OdeEnv::tempdir, "testdir1", "testdir2") );
  @str_list = ("ODEMKPASS");
  @arg_list = ("FILEPATH=$testfile"); 
  $status = runMkTest( "var_vpath.mk", "implied variables .VPATH", 1, 10,  
                       \@str_list, \@arg_list );
  if (!$error) { $error = $status; }


  ##running mk -f var_dollar.mk
  @str_list=("ubaclass.java", "c","ubaclass", "a");
  @arg_list = ("");
  $status = runMkTest( "var_dollar.mk", 
                       "variables with dollar signs", 4, 11,  
                       \@str_list, \@arg_list );
  if (!$error) { $error = $status; }

  ##running mk -f var_dollar.mk -DODEMAKE_DOLLARS
  @str_list=("\$x\$", "\$bub\$ba\$.java","\$a\$bc", "\$x\$", "\$bub\$ba\$.class",
             "a\$b");
  @arg_list = ("-DODEMAKE_DOLLARS");
  $status = runMkTest( "var_dollar.mk", 
                       "variables with dollar signs", 6, 12,  
                       \@str_list, \@arg_list );
  if (!$error) { $error = $status; }

  
  ## testing debug flag "a"
  ##running mk -f var_archive_member.mk -da
  @str_list=("finding member tlib.b(foo.b)", "finding member tlib(foo)",
             "finding member tlib.a.b(foo.a.b)");
  $status = runMkDebug_a( "var_archive_member.mk", 
                       "debug flag \"a\"", 6, 8, 13, @str_list );  
  if (!$error) { $error = $status; }
  ##running mk -f var_target.mk -da
  $status = runMkDebug_a( "var_target.mk", "debug flag \"a\"", 0, 4, 14 );
  if (!$error) { $error = $status; }
  
  ## testing debug flag "v"
  ##running mk -f var_assign.mk -dv
  if (defined( $OdeEnv::UNIX ))
  {
    @str_list=("MAKEFLAGS= -dv -DUNIX=\"1\"", 
               "Setting global variable .TARGETS= all");
    $status = runMkDebug_vV( "var_assign.mk", "v", "debug flag \"v\"", 58, 70,  
                             15, @str_list );
  }
  else
  {
    @str_list=("MAKEFLAGS= -dv", "Setting global variable .TARGETS= all");
    $status = runMkDebug_vV( "var_assign.mk", "v", "debug flag \"v\"", 57, 69,  
                             15, @str_list );
  }
  if (!$error) { $error = $status; }

  ##running mk -f var_impsrc.mk -dv
  if (defined( $OdeEnv::UNIX ))
  {
    $status = runMkDebug_vV( "var_impsrc.mk", "v", "debug flag \"v\"", 4, 9, 16,
                             @str_list );
  }
  else
  {
    $status = runMkDebug_vV( "var_impsrc.mk", "v", "debug flag \"v\"", 3, 8, 16,
                             @str_list );
  }
  if (!$error) { $error = $status; }

  ## testing debug flag "V"
  ##running mk -f var_target.mk -dV
  if (defined( $OdeEnv::UNIX ))
  {
    @str_list=("MAKEFLAGS= -dvV -DUNIX=\"1\"", 
               "Setting global variable .TARGETS= all",
               "Parsing \${VAR5}", "Parsing \$VAR12=var12");
    $status = runMkDebug_vV( "var_assign.mk", "V", "debug flag \"V\"", 66, 78,  
                            17, @str_list );
  }
  else
  {
    @str_list=("MAKEFLAGS= -dvV", "Setting global variable .TARGETS= all",
               "Parsing \${VAR5}", "Parsing \$VAR12=var12");
    $status = runMkDebug_vV( "var_assign.mk", "V", "debug flag \"V\"", 65, 77,  
                            17, @str_list );
  }
  if (!$error) { $error = $status; }
  ##running mk -f var_impsrc.mk -dV
  if (defined( $OdeEnv::UNIX ))
  {
    @str_list=("MAKEFLAGS= -dvV -DUNIX=\"1\"", 
               "Setting global variable .TARGETS= all",
              "Parsing \${.IMPSRC}", "Parsing \${<}", "Parsing \$<");
    $status = runMkDebug_vV( "var_impsrc.mk", "V", "debug flag \"V\"", 14, 19, 
                            18, @str_list );
  }
  else
  {
    @str_list=("MAKEFLAGS= -dvV", "Setting global variable .TARGETS= all",
              "Parsing \${.IMPSRC}", "Parsing \${<}", "Parsing \$<");
    $status = runMkDebug_vV( "var_impsrc.mk", "V", "debug flag \"V\"", 13, 18, 
                            18, @str_list );
  }
  if (!$error) { $error = $status; }
  
  ## testing debug flag "d"
  ##running mk -f var_assign.mk -dd
  $status = runMkDebug_d( "var_assign.mk", "debug flag \"d\"", 106, 19 );
  if (!$error) { $error = $status; }
  ##running mk -f var_impsrc.mk -dd
  $status = runMkDebug_d( "var_impsrc.mk", "debug flag \"d\"", 34, 20 );
  if (!$error) { $error = $status; }

  ## testing debug flag "m"
  ##running mk -f var_target.mk -dm
  @str_list=("Making `test1' since it doesn't exist", 
             "Making `test2.c' since it doesn't exist", 
             "Making `test2' since source was modified", 
             "Making `targ1' since source was modified");
  $status = runMkDebug_m( "var_target.mk", "debug flag \"m\"", 8, 4, 21, 
                          @str_list );
  if (!$error) { $error = $status; }
  ##running mk -f var_assign.mk -dm
  @str_list=("Making `test1' since it doesn't exist", 
             "Making `test2' since it doesn't exist", 
             "Making `test3' since it doesn't exist", 
             "Making `test4' since it doesn't exist", 
             "Making `test5' since it doesn't exist", 
             "Making `test6' since it doesn't exist", 
             "Making `test7' since it doesn't exist", 
             "Making `test8' since it doesn't exist", 
             "Making `test9' since it doesn't exist"); 
  $status = runMkDebug_m( "var_assign.mk", "debug flag \"m\"", 25, 12, 22,
                          @str_list );
  if (!$error) { $error = $status; }

  ## testing debug flag "j"
  ##running mk -f var_target.mk -dj
  $status = runMkDebug_j( "var_target.mk", "debug flag \"j\"", 12, 23 );
  if (!$error) { $error = $status; }
  ##running mk -f var_assign.mk -dj
  $status = runMkDebug_j( "var_assign.mk", "debug flag \"j\"", 36, 24 );
  if (!$error) { $error = $status; }

  ## testing debug flag "g1"
  ##running mk -f var_impsrc.mk -dg1
  @str_list=("Target node `all'", "Target node `test.c'"); 
  $status = runMkDebug_g1( "var_impsrc.mk", "debug flag \"g1\"", 18, 2, 25, 
                           @str_list );
  if (!$error) { $error = $status; }
  ##running mk -f var_assign.mk -dg1
  @str_list=("Target node `all'", "Target node `test1'", 
             "Target node `test2'", "Target node `test3'",
             "Target node `test4'", "Target node `test5'",
             "Target node `test6'", "Target node `test7'",
             "Target node `test8'", "Target node `test9'");
  $status = runMkDebug_g1( "var_assign.mk", "debug flag \"g1\"", 103, 13, 26, 
                           @str_list );
  if (!$error) { $error = $status; }

  ## testing debug flag "g2"
  ##running mk -f var_impsrc.mk -dg2
  @str_list=("Target node `all'", "Target node `test.a'", 
             "Target node `test.c'", "Target node `test.o'");
  $status = runMkDebug_g2( "var_impsrc.mk", "debug flag \"g2\"", 35, 4, 27, 
                           @str_list );
  if (!$error) { $error = $status; }
  ##running mk -f var_assign.mk -dg2
  @str_list=("Target node `all'", "Target node `test1'", 
             "Target node `test2'", "Target node `test3'",
             "Target node `test4'", "Target node `test5'",
             "Target node `test6'", "Target node `test7'",
             "Target node `test8'", "Target node `test9'");
  $status = runMkDebug_g2( "var_assign.mk", "debug flag \"g2\"", 103, 13, 28, 
                           @str_list );
  if (!$error) { $error = $status; }


  ## testing debug flags "p" and "s"
  ##running mk -f var_imprules.mk -dps
  @arg_list=("-dps");
  @str_list = ("Suff: Adding .b->.a", "Patterns: Adding %.x : %.y",  
               "Suff: Trying .b->.a for foo.a", "Suff: Using  foo.b->foo.a",
               "Patterns: Using  %.x : %.y", "foo.b", "foo.a", "foo.y", "foo.x"); 
  $status = runMkTest( "var_imprules.mk", "debug options -dp and -ds", 
                       9, 29, \@str_list, \@arg_list );
  if (!$error) { $error = $status; }


  ## testing debug flag "A"
  ##running mk -f var_vpath.mk -dA
  $testfile = join( $OdeEnv::dirsep, 
                    ($OdeEnv::tempdir, "testdir1", "testdir2") );
  $status = runMkDebug_A( "var_vpath.mk", "debug flag \"A\"", 81, 30,
                       "FILEPATH=$testfile" );
  if (!$error) { $error = $status; }

  $testfile = $OdeEnv::tempdir . $OdeEnv::dirsep . "testdir1";
  @delete_list = ($OdeTest::tmpfile, $testfile);
  OdeFile::deltree( \@delete_list );
  return $error;
}

##############################################################
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
#   args: a reference to a list of any other commandline args
#############################################################
sub runMkTest( $$$$$$ )
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
  $makefile_name = "variables" . $OdeEnv::dirsep . $filename;
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
  my ($filename, $tststr, $testno, @args) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $status;
  my $error_txt;
  $makefile_name = "variables" . $OdeEnv::dirsep . $filename;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
	{
    OdeInterface::printError( "mk : $tststr" );
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

########################################################################
#
# sub runMkDebug_a
# - to make and run the command with all the
#   arguments passed and the debug flag "a"
#   and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   debug_lines: number of debug lines. These are prepended by Arch:
#   num_tests: number of tests performed in the makefile
#   testno: test number
#   str_list: a list of all the strings to be looked for in the tempfile
########################################################################
sub runMkDebug_a( $@ )
{
  my ($filename, $tststr, $debug_lines, $num_tests, $testno, @str_list) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $arch_count = 0;
  my $status;
  my $error_txt;
  my $str_num = scalar( @str_list );
  my $index;
  $makefile_name = "variables" . $OdeEnv::dirsep . $filename;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
  }
  $command = "mk -f $makefile -da > $OdeTest::tmpfile $OdeEnv::stderr_redir";
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
  $arch_count = OdeFile::numPatternsInFile( "Arch:", $OdeTest::tmpfile );
  if (!$mk_error && ($arch_count != $debug_lines))
  {
    $mk_error = 1;
    $error_txt = "Output has incorrect number of debug lines";
  }
  $arch_count = $debug_lines + $num_tests;
  if (!$mk_error && 
      (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != $arch_count))
  {
    $mk_error = 1;
    $error_txt = "Output has incorrect number of lines";
  }
  ## check if all the required lines are in the output
  if (!$mk_error)
  {
    for( $index=0; $index<$str_num; $index++)
    {
      if (!(OdeFile::findInFile( @str_list[$index], $OdeTest::tmpfile )))
      {
        $mk_error = 1;
        $error_txt = "An error occured in a debug statement";
        last;
      }
    }
  }
  # Check if the correct number of targets are executed
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

########################################################################
#
# sub runMkDebug_vV
# - to make and run the command with all the
#   arguments passed and the debug flags "v", "V"
#   and check the results
#
# - arguments
#   filename: the name of the makefile
#   flag: the debug flag to be tested. Can be "v" or "V"
#   tststr: the string to be output in the result
#   debug_lines: number of debug lines. These are prepended by Var:
#   tot_lines: total number of lines in the output
#   testno: test number
#   str_list: a list of all the strings to be looked for in the tempfile
########################################################################
sub runMkDebug_vV( $@ )
{
  my ($filename, $flag, $tststr, $debug_lines, $tot_lines, $testno, 
      @str_list) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $var_count = 0;
  my $status;
  my $error_txt;
  $makefile_name = "variables" . $OdeEnv::dirsep . $filename;
  my $str_num = scalar( @str_list );
  my $index;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
  }
  if (defined( $OdeEnv::UNIX ))
  {
    $command = "mk -f $makefile -d$flag -DUNIX > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  }
  else
  {
    $command = "mk -f $makefile -d$flag > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  }
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
  $var_count = OdeFile::numPatternsInFile( "Var:", $OdeTest::tmpfile );
  if (!$mk_error && ($var_count == 0))
  {
    $mk_error = 1;
    $error_txt = "Output has no debug lines";
  }
  if (!$mk_error && 
      (OdeFile::numLinesInFile( $OdeTest::tmpfile ) == 0))
  {
    $mk_error = 1;
    $error_txt = "Output has no lines";
  }
  ## check if all the required lines are in the output
  if (!$mk_error)
  {
    for( $index=0; $index<$str_num; $index++)
    {
      if (!(OdeFile::findInFile( @str_list[$index], $OdeTest::tmpfile )))
      {
        $mk_error = 1;
        $error_txt = "An error occured in a debug statement";
        last;
      }
    }
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
# sub runMkDebug_d
# - to make and run the command with all the
#   arguments passed and the debug flag "d"
#   and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   debug_lines: number of debug lines. These are prepended by Dir:
#   testno: test number
#   args: a list of any other commandline args
####################################################################
sub runMkDebug_d( $@ )
{
  my ($filename, $tststr, $debug_lines, $testno, @args) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $dir_count = 0;
  my $status;
  my $error_txt;
  $makefile_name = "variables" . $OdeEnv::dirsep . $filename;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
  }
  if (defined( $OdeEnv::UNIX ))
  {
    $command = "mk -f $makefile @args -dd -DUNIX > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  }
  else
  {
    $command = "mk -f $makefile @args -dd > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  }
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
  $dir_count = OdeFile::numPatternsInFile( "Dir:", $OdeTest::tmpfile );
  if (!$mk_error && ($dir_count == 0))
  {
    $mk_error = 1;
    $error_txt = "Output has no debug lines";
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

########################################################################
#
# sub runMkDebug_m
# - to make and run the command with all the
#   arguments passed and the debug flag "m"
#   and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   debug_lines: number of debug lines. These are prepended by Mod:
#   num_tests: number of tests performed in the makefile
#   testno: test number
#   str_list: a list of all the strings to be looked for in the tempfile
########################################################################
sub runMkDebug_m( $@ )
{
  my ($filename, $tststr, $debug_lines, $num_tests, $testno, @str_list) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $mod_count = 0;
  my $status;
  my $error_txt;
  my $str_num = scalar( @str_list );
  my $index;
  $makefile_name = "variables" . $OdeEnv::dirsep . $filename;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
  }
  if (defined( $OdeEnv::UNIX ))
  {
    $command = "mk -f $makefile -dm -DUNIX > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  }
  else
  {
    $command = "mk -f $makefile -dm > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  }
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
  $mod_count = OdeFile::numPatternsInFile( "Mod:", $OdeTest::tmpfile );
  if (!$mk_error && ($mod_count != $debug_lines))
  {
    $mk_error = 1;
    $error_txt = "Output has incorrect number of debug lines";
  }
  $mod_count = $debug_lines + $num_tests;
  if (!$mk_error && 
      (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != $mod_count))
  {
    $mk_error = 1;
    $error_txt = "Output has incorrect number of lines";
  }
  ## check if all the required lines are in the output
  if (!$mk_error)
  {
    for( $index=0; $index<$str_num; $index++)
    {
      if (!(OdeFile::findInFile( @str_list[$index], $OdeTest::tmpfile )))
      {
        $mk_error = 1;
        $error_txt = "An error occured in a debug statement";
        last;
      }
    }
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
# sub runMkDebug_j
# - to make and run the command with all the
#   arguments passed and the debug flag "j"
#   and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   debug_lines: number of debug lines. These are prepended by Job:
#   testno: test number
#   args: a list of any other commandline args
####################################################################
sub runMkDebug_j( $@ )
{
  my ($filename, $tststr, $debug_lines, $testno, @args) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $job_count = 0;
  my $status;
  my $error_txt;
  my $line;
  $makefile_name = "variables" . $OdeEnv::dirsep . $filename;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
  }
  if (defined( $OdeEnv::UNIX ))
  {
    $command = "mk -f $makefile @args -dj -DUNIX > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  }
  else
  {
    $command = "mk -f $makefile @args -dj > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  }
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
  $job_count = OdeFile::numPatternsInFile( "Job:", $OdeTest::tmpfile );
  if (!$mk_error && ($job_count != $debug_lines))
  {
    $mk_error = 1;
    $error_txt = "Output has incorrect number of debug lines";
  }
  if (!$mk_error && 
       (!OdeFile::findInFile( "Max total jobs = 1", $OdeTest::tmpfile) ||
        !OdeFile::findInFile( "Max local jobs = 1", $OdeTest::tmpfile))) 
  {
    $mk_error = 1;
    $error_txt = "Error in the number of total and local jobs";
  }
  open( FILE, "< $OdeTest::tmpfile" ) ||
     OdeInterface::logPrint( "Could not open file $OdeTest::tmpfile\n" );
  while( !eof( FILE ) )
  {
    $line = readline( *FILE );
    if (index( $line, "nJobs = 0" ) >= 0)
    {
      if (index( $line, "nLocalJobs = 0" ) < 0)
      {
        $mk_error = 1;
        $error_txt = "nLocalJobs not equal to nJobs";
        last;
      }
    }
    elsif (index( $line, "nJobs = 1" ) >= 0)
    {
      if (index( $line, "nLocalJobs = 1" ) < 0)
      {
        $mk_error = 1;
        $error_txt = "nLocalJobs not equal to nJobs";
        last;
      }
    }
  }
  close( FILE );
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

########################################################################
#
# sub runMkDebug_dg1
# - to make and run the command with all the
#   arguments passed and the debug flag "g1"
#   and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   lines: the total number of lines in the file
#   unmade_count: number of UNMADE statements in the file
#   testno: test number
#   str_list: a list of all the strings to be looked for in the tempfile
########################################################################
sub runMkDebug_g1( $@ )
{
  my ($filename, $tststr, $lines, $unmade_count, $testno, @str_list) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $status;
  my $error_txt;
  my $str_num = scalar( @str_list );
  my $index;
  $makefile_name = "variables" . $OdeEnv::dirsep . $filename;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
  }
  if (defined( $OdeEnv::UNIX ))
  {
    $command = "mk -f $makefile -dg1 -DUNIX > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  }
  else
  {
    $command = "mk -f $makefile -dg1 > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  }
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  if (!$mk_error && (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != $lines))
  {
    $mk_error = 1;
    $error_txt = "output has incorrect number of lines";
  }
  if (!$mk_error && 
      (OdeFile::findInFile( "Graph: *** before the build", 
       $OdeTest::tmpfile ) != 1))
  {
    $mk_error = 1;
    $error_txt = "error occured in the output heading";
  }
  if (!$mk_error && 
      (OdeFile::numPatternsInFile( "State: UNMADE", $OdeTest::tmpfile ) != 
                                                              $unmade_count))
  {
    $mk_error = 1;
    $error_txt = "error occured in the statements State: UNMADE";
  }
  ## check if all the required lines are in the output
  if (!$mk_error)
  {
    for( $index=0; $index<$str_num; $index++)
    {
      if (!(OdeFile::findInFile( @str_list[$index], $OdeTest::tmpfile )))
      {
        $mk_error = 1;
        $error_txt = "An error occured in a debug statement";
        last;
      }
    }
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

########################################################################
#
# sub runMkDebug_dg2
# - to make and run the command with all the
#   arguments passed and the debug flag "g2"
#   and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   lines: the total number of lines in the file
#   made_count: number of MADE statements in the file
#   testno: test number
#   str_list: a list of all the strings to be looked for in the tempfile
########################################################################
sub runMkDebug_g2( $@ )
{
  my ($filename, $tststr, $lines, $made_count, $testno, @str_list) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $status;
  my $error_txt;
  my $str_num = scalar( @str_list );
  my $index;
  $makefile_name = "variables" . $OdeEnv::dirsep . $filename;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
  }
  if (defined( $OdeEnv::UNIX ))
  {
    $command = "mk -f $makefile -dg2 -DUNIX > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  }
  else
  {
    $command = "mk -f $makefile -dg2 > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  }
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  if (!$mk_error && (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != $lines))
  {
    $mk_error = 1;
    $error_txt = "output has incorrect number of lines";
  }
  if (!$mk_error && 
      (OdeFile::findInFile( "Graph: *** after the build", 
       $OdeTest::tmpfile ) == 1))
  {
    $mk_error = 1;
    $error_txt = "error occured in the output heading";
  }
  if (!$mk_error && 
      (OdeFile::numPatternsInFile( "State: MADE", $OdeTest::tmpfile ) != 
                                                              $made_count))
  {
    $mk_error = 1;
    $error_txt = "error occured in the statements State: MADE";
  }
  ## check if all the required lines are in the output
  if (!$mk_error)
  {
    for( $index=0; $index<$str_num; $index++)
    {
      if (!(OdeFile::findInFile( @str_list[$index], $OdeTest::tmpfile )))
      {
        $mk_error = 1;
        $error_txt = "An error occured in a debug statement";
        last;
      }
    }
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

########################################################################
#
# sub runMkDebug_A
# - to make and run the command with all the
#   arguments passed and the debug flag "A"
#   and check the results
#
# - arguments
#   filename: the name of the makefile
#   tststr: the string to be output in the result
#   lines: the total number of lines in the file
#   testno: test number
#   args: a list of any other commandline args
########################################################################
sub runMkDebug_A( $@ )
{
  my ($filename, $tststr, $lines, $testno, @args) = @_;
  my $makefile_name;
  my $makefile;
  my $command;
  my $mk_error = 0;
  my $status;
  my $error_txt;
  $makefile_name = "variables" . $OdeEnv::dirsep . $filename;
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    return( 1 );
  }
  $command = "mk -f $makefile -dA @args > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  if (!$mk_error && (OdeFile::numLinesInFile( $OdeTest::tmpfile ) == 0))
  {
    $mk_error = 1;
    $error_txt = "output has no lines";
  }
  if (!$mk_error &&
      (!OdeFile::findInFile( "All debugging options on", $OdeTest::tmpfile ) ||
       !OdeFile::findInFile( "MAKEFLAGS= -dacdjimpstvV", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "error in heading or MAKEFLAGS";
  }
  if (!$mk_error && 
      (!OdeFile::findInFile( "Var:", $OdeTest::tmpfile )  || 
       !OdeFile::findInFile( "Cond:", $OdeTest::tmpfile ) || 
       !OdeFile::findInFile( "Inc:", $OdeTest::tmpfile )  || 
       !OdeFile::findInFile( "Dir:", $OdeTest::tmpfile )  || 
       !OdeFile::findInFile( "Targ:", $OdeTest::tmpfile ) || 
       !OdeFile::findInFile( "Job:", $OdeTest::tmpfile )  || 
       !OdeFile::findInFile( "Mod:", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "a debug flag not executed";
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
