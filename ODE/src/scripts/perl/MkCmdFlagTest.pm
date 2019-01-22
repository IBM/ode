###########################################
#
# MkCmdFlagTest module
# - module to test mk command line flags
#
###########################################

package MkCmdFlagTest;

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
  my @touch_list;
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

  OdeInterface::logPrint( "in MkCmdFlagTest\n");
 
  ## Testing command line flags

  ## locating the makefiles directory
  $makefile_dir = join( $OdeEnv::dirsep, (".", "makefiles", "cmdflags") );
  if (! -d $makefile_dir )
  {
    $makefile_dir = join( $OdeEnv::dirsep, ($OdeEnv::bb_base, "src", "scripts",
                          "perl", "makefiles", "cmdflags") );
    if (! -d $makefile_dir)
    {
      OdeInterface::printError( "mk: commandline flags" );
      OdeInterface::logPrint(
          "Could not locate the makefiles directory - $makefile_dir\n" );
      return( 1 );
    }
  }
  $makefile = $makefile_dir . $OdeEnv::dirsep . "*"; 
  ## copying all the makefiles into the temp directory
  OdeEnv::runSystemCommand( "$OdeEnv::copy $makefile $OdeEnv::tempdir" );
  ## cd'ing to the temp directory 
  OdePath::chgdir( $OdeEnv::tempdir );

  ## testing -w -s -i flags
  ## running mk -f flag_wsi.mk
  $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "flag_wsi.mk";
  if (! -f $tfile1)
  {
    OdeInterface::logPrint( "Could not locate makefile - $tfile1\n" );
  }
  $command = "mk -f $tfile1 -w -s -i > $OdeTest::tmpfile $OdeEnv::stderr_redir";
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
       (!OdeFile::findInFile( "making t1", $OdeTest::tmpfile ) ||
        !OdeFile::findInFile( "making t2", $OdeTest::tmpfile ) ||
        !OdeFile::findInFile( "making t5", $OdeTest::tmpfile ) ||
        !OdeFile::findInFile( "making all", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "a target not executed";
  }
  if (!$mk_error && 
      (OdeFile::numPatternsInFile( "ERROR", $OdeTest::tmpfile ) != 3))
  {
    $mk_error = 1;
    $error_txt = "mk did not produce all the errors";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                1, "commandline flags -w -s -i" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                1, "commandline flags -w -s -i" );
  }

  $tfile2 = $OdeEnv::tempdir . $OdeEnv::dirsep . "t.o";
  $tfile3 = $OdeEnv::tempdir . $OdeEnv::dirsep . "t1.c";
  $tfile4 = $OdeEnv::tempdir . $OdeEnv::dirsep . "t2.c";
  @delete_list = ($tfile2, $tfile3, $tfile4);
  OdeFile::deltree( \@delete_list );
  if ((-f $tfile2) || (-f $tfile3) || (-f $tfile4))
  {
    OdeInterface::logPrint( "$tfile2 or $tfile3 or $tfile4 not deleted\n" );
  }

  ## testing -t flag
  ## running mk -f flag_nt.mk -t
  ## the files $tfile3, $tfile3, $tfile4 doesn't exist
  OdeInterface::logPrint( 
                "ODETEST: $tfile2, $tfile3, $tfile4 doesn't exist\n" );
  $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "flag_nt.mk";
  if (! -f $tfile1)
  {
    OdeInterface::logPrint( "Could not locate makefile - $tfile1\n" );
  }
  $command = "mk -f $tfile1 -t > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  if (!$mk_error && ((-f $tfile2) || (-f $tfile3) || (-f $tfile4)))
  {
    $mk_error = 1;
    $error_txt = "the target or a source is created when it does not exist";
  }
  if (!$mk_error && ((OdeFile::findInFile( "making t.o", $OdeTest::tmpfile )) 
                  || (OdeFile::findInFile( "making t1.c", $OdeTest::tmpfile ))
                  || (OdeFile::findInFile( "making t2.c", $OdeTest::tmpfile ))))
  {
    $mk_error = 1;
    $error_txt = "a target or a source command is executed";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                2, "commandline flag -t" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                2, "commandline flag -t" );
  }
            
  OdeFile::deltree( \@delete_list );
  if ((-f $tfile2) || (-f $tfile3) || (-f $tfile4))
  {
    OdeInterface::logPrint( "$tfile2 or $tfile3 or $tfile4 not deleted\n" );
  }

  ## testing -n flag
  ## running mk -f flag_nt.mk -n
  ## the files $tfile3, $tfile3, $tfile4 doesn't exist
  $command = "mk -f $tfile1 -n > $OdeTest::tmpfile $OdeEnv::stderr_redir";
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
         ((!OdeFile::findInFile( "echo making t.o", $OdeTest::tmpfile )) 
          || (!OdeFile::findInFile( "echo making t1.c", $OdeTest::tmpfile ))
          || (!OdeFile::findInFile( "echo making t2.c", $OdeTest::tmpfile ))))
  {
    $mk_error = 1;
    $error_txt = "a target or a source command is not displayed or " .
                 "is executed";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                3, "commandline flag -n" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                3, "commandline flag -n" );
  }

  ## running mk -f flag_nt.mk -t
  ## target $tfile2 is up to date
  @touch_list = ($tfile2, $tfile3);
  OdeFile::touchFile( \@touch_list, 10000 );
  OdeFile::touchFile( $tfile4, 20000 );
  if ((! -f $tfile2) || (! -f $tfile3) || (! -f $tfile4))
  {
    OdeInterface::logPrint( "$tfile2 or $tfile3 or $tfile4 not created\n" );
  }
  OdeInterface::logPrint( "ODETEST: target $tfile2 is up to date\n" );
  $mtime1 = (stat $tfile2)[9];
  $mtime2 = (stat $tfile3)[9];
  $mtime3 = (stat $tfile4)[9];
  $command = "mk -f $tfile1 -t > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  if (!$mk_error && (((stat $tfile2)[9] != $mtime1) || 
                     ((stat $tfile3)[9] != $mtime2) ||
                     ((stat $tfile4)[9] != $mtime3)))
  {
    $mk_error = 1;
    $error_txt = "the target or a source is touched when the target " .
                 "is up to date";
  }
  if (!$mk_error && ((OdeFile::findInFile( "making t.o", $OdeTest::tmpfile )) 
                  || (OdeFile::findInFile( "making t1.c", $OdeTest::tmpfile ))
                  || (OdeFile::findInFile( "making t2.c", $OdeTest::tmpfile ))))
  {
    $mk_error = 1;
    $error_txt = "a target or a source command is executed when the " .
                 "target is up to date";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                4, "commandline flag -t" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                4, "commandline flag -t" );
  }

  ## running mk -f flag_nt.mk -n
  ## target $tfile2 is up to date
  @touch_list = ($tfile2, $tfile3);
  OdeFile::touchFile( \@touch_list, 10000 );
  OdeFile::touchFile( $tfile4, 20000 );
  if ((! -f $tfile2) || (! -f $tfile3) || (! -f $tfile4))
  {
    OdeInterface::logPrint( "$tfile2 or $tfile3 or $tfile4 does not exist\n" );
  }
  $command = "mk -f $tfile1 -n > $OdeTest::tmpfile $OdeEnv::stderr_redir";
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
         ((OdeFile::findInFile( "making t.o", $OdeTest::tmpfile )) 
          || (OdeFile::findInFile( "making t1.c", $OdeTest::tmpfile ))
          || (OdeFile::findInFile( "making t2.c", $OdeTest::tmpfile ))))
  {
    $mk_error = 1;
    $error_txt = "a target or a source command is displayed or executed " .
                 "when the target is up to date"; 
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                5, "commandline flag -n" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                5, "commandline flag -n" );
  }

  ## running mk -f flag_nt.mk -t
  ## source $tfile3 is recently modified
  @touch_list = ($tfile2, $tfile4);
  OdeFile::touchFile( \@touch_list, 20000 );
  OdeFile::touchFile( $tfile3, 10000 );
  if ((! -f $tfile2) || (! -f $tfile3) || (! -f $tfile4))
  {
    OdeInterface::logPrint( "$tfile2 or $tfile3 or $tfile4 does not exist\n" );
  }
  OdeInterface::logPrint( "ODETEST: source $tfile3 is recently modified\n" );
  $mtime1 = (stat $tfile2)[9];
  $mtime2 = (stat $tfile3)[9];
  $mtime3 = (stat $tfile4)[9];
  $command = "mk -f $tfile1 -t > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  if (!$mk_error && (((stat $tfile2)[9] == $mtime1) || 
                     ((stat $tfile3)[9] != $mtime2) ||
                     ((stat $tfile4)[9] != $mtime3)))
  {
    $mk_error = 1;
    $error_txt = "the target is not touched or a source is touched " . 
                 "when the target is out of date";
  }
  if (!$mk_error && ((OdeFile::findInFile( "making t.o", $OdeTest::tmpfile )) 
                  || (OdeFile::findInFile( "making t1.c", $OdeTest::tmpfile ))
                  || (OdeFile::findInFile( "making t2.c", $OdeTest::tmpfile ))))
  {
    $mk_error = 1;
    $error_txt = "a target or a source command is executed when the " .
                 "target is out of date";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                6, "commandline flag -t" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                6, "commandline flag -t" );
  }
  
  ## running mk -f flag_nt.mk -n
  ## source $tfile3 is recently modified
  @touch_list = ($tfile2, $tfile4);
  OdeFile::touchFile( \@touch_list, 20000 );
  OdeFile::touchFile( $tfile3, 10000 );
  if ((! -f $tfile2) || (! -f $tfile3) || (! -f $tfile4))
  {
    OdeInterface::logPrint( "$tfile2 or $tfile3 or $tfile4 does not exist\n" );
  }
  $command = "mk -f $tfile1 -n > $OdeTest::tmpfile $OdeEnv::stderr_redir";
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
         ((!OdeFile::findInFile( "echo making t.o", $OdeTest::tmpfile )) 
          || (OdeFile::findInFile( "echo making t1.c", $OdeTest::tmpfile ))
          || (OdeFile::findInFile( "echo making t2.c", $OdeTest::tmpfile ))))
  {
    $mk_error = 1;
    $error_txt = "a target command is not displayed or is executed or a  " .
                 "source command is displayed when the target is out of date"; 
  }
  if (!$mk_error && 
          ((OdeFile::findInFile( "making t1.c", $OdeTest::tmpfile ))
          || (OdeFile::findInFile( "making t2.c", $OdeTest::tmpfile ))))
  {
    $mk_error = 1;
    $error_txt = "a source command is executed when the target is out of date";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                7, "commandline flag -n" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                7, "commandline flag -n" );
  }

  ## running mk -f flag_nt.mk -t -n
  ## source $tfile3 is recently modified
  $mtime1 = (stat $tfile2)[9];
  $mtime2 = (stat $tfile3)[9];
  $mtime3 = (stat $tfile4)[9];
  $command = "mk -f $tfile1 -t -n > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  if (!$mk_error && (((stat $tfile2)[9] == $mtime1) || 
                     ((stat $tfile3)[9] != $mtime2) ||
                     ((stat $tfile4)[9] != $mtime3)))
  {
    $mk_error = 1;
    $error_txt = "the target is not touched or a source is touched " . 
                 "when the target is out of date";
  }
  if (!$mk_error && 
         ((OdeFile::findInFile( "making t.o", $OdeTest::tmpfile )) 
          || (OdeFile::findInFile( "making t1.c", $OdeTest::tmpfile ))
          || (OdeFile::findInFile( "making t2.c", $OdeTest::tmpfile ))))
  {
    $mk_error = 1;
    $error_txt = "a target or a source command is displayed or " .
                 "executed when the target is out of date"; 
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                8, "commandline flag -t -n" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                8, "commandline flag -t -n" );
  }

  ## trying to touch a special target 
  ## source $tfile3 is recently modified
  ## running mk -f flag_t.mk -t t.o
  $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "flag_t.mk";
  if (! -f $tfile1)
  {
    OdeInterface::logPrint( "Could not locate makefile - $tfile1\n" );
  }
  $mtime1 = (stat $tfile2)[9];
  $mtime2 = (stat $tfile3)[9];
  $command = "mk -f $tfile1 -t t.o > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  if (!$mk_error && (((stat $tfile2)[9] != $mtime1) || 
                     ((stat $tfile3)[9] != $mtime2)))
  {
    $mk_error = 1;
    $error_txt = "the target or a source is touched when the target is " . 
                 "is a special target and out of date";
  }
  if (!$mk_error && ((OdeFile::findInFile( "making t.o", $OdeTest::tmpfile )) 
                 || (OdeFile::findInFile( "making t1.c", $OdeTest::tmpfile ))))
  {
    $mk_error = 1;
    $error_txt = "the target or a source command is executed when the " . 
                 "target is a special target and out of date";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                9, "commandline flag -t" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                9, "commandline flag -t" );
  }

  ## source $tfile3 is recently modified
  ## running mk -f flag_t.mk -n t.o
  $command = "mk -f $tfile1 -n t.o > $OdeTest::tmpfile $OdeEnv::stderr_redir";
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
         ((!OdeFile::findInFile( "echo making t.o", $OdeTest::tmpfile )) 
          || (OdeFile::findInFile( "echo making t1.c", $OdeTest::tmpfile ))))
  {
    $mk_error = 1;
    $error_txt = "a target command is not displayed or is executed or a  " .
                 "source command is displayed when the target is a special " .
                 " target and out of date"; 
  }
  if (!$mk_error && 
          (OdeFile::findInFile( "making t1.c", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "a source command is executed when the target is " .
                 "a special target and out of date";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                10, "commandline flag -n" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                10, "commandline flag -n" );
  }

  ## testing -b flag
  ## running mk -f flag_b.mk -b
  ## creating "dir1" directory which is a source in flag_b.mk
  mkpath( $OdeEnv::tempdir . $OdeEnv::dirsep . "dir1" );
  ## creating "all" directory which is a target in flag_b.mk
  mkpath( $OdeEnv::tempdir . $OdeEnv::dirsep . "all" );
  ## making source dir1 older than target all
  OdeFile::touchFile( $OdeEnv::tempdir . $OdeEnv::dirsep . "dir1", 1000 );
  ## the target directory is up to date
  $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "all";
  OdeInterface::logPrint( "ODETEST: target directory $tfile1 is up to date\n" );
  $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "flag_b.mk";
  if (! -f $tfile1)
  {
    OdeInterface::logPrint( "Could not locate makefile - $tfile1\n" );
  }
  $command = "mk -f $tfile1 -b > $OdeTest::tmpfile $OdeEnv::stderr_redir";
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
          ((OdeFile::findInFile( "making all", $OdeTest::tmpfile )) ||
           (OdeFile::findInFile( "making t1.c", $OdeTest::tmpfile ))))
  {
    $mk_error = 1;
    $error_txt = "a source or a target command is executed when the target " .
                 "is uptodate";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                11, "commandline flag -b" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                11, "commandline flag -b" );
  }

  ## testing -k flag
  ## running mk -f flag_k.mk -k all t3
  $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "flag_k.mk";
  if (! -f $tfile1)
  {
    OdeInterface::logPrint( "Could not locate makefile - $tfile1\n" );
  }
  $command = "mk -f $tfile1 -k all t3 > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status == 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a zero exit code";
  }
  if (!$mk_error && 
          (!OdeFile::findInFile( "ERROR", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "a source command did not produce an error";
  }
  if (!$mk_error && 
          (OdeFile::findInFile( "making all", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "a target built after an error occured";
  }
  if (!$mk_error && 
          ((!OdeFile::findInFile( "making t2", $OdeTest::tmpfile )) ||
           (!OdeFile::findInFile( "making t3", $OdeTest::tmpfile ))))
  {
    $mk_error = 1;
    $error_txt = "a command not executed after an error occured";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                12, "commandline flag -k" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                12, "commandline flag -k" );
  }

  ## testing -usage flag
  ## running mk -f flag_wsi.mk -usage
  $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "flag_wsi.mk";
  $command = "mk -f $tfile1 -usage > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # the use of -usage should prevent the makefile from being parsed
  # should not report an error even if the makefile has one
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  if (!$mk_error && 
          (OdeFile::findInFile( "ERROR", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "an error occured while executing the command";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                13, "commandline flag -usage" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                13, "commandline flag -usage" );
  }

  ## testing -version flag
  ## running mk -f flag_wsi.mk -version
  $command = "mk -f $tfile1 -version > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # the use of -version should prevent the makefile from being parsed
  # should not report an error even if the makefile has one
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  if (!$mk_error && 
          (OdeFile::findInFile( "ERROR", $OdeTest::tmpfile )))
  {
    $mk_error = 1;
    $error_txt = "an error occured while executing the command";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                14, "commandline flag -version" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                14, "commandline flag -version" );
  }

  ## cd'ing back to the current directory
  OdePath::chgdir( $dir );

  @delete_list = ($OdeEnv::tempdir . $OdeEnv::dirsep . "flag_wsi.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "flag_k.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "flag_nt.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "flag_t.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "flag_b.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "flag_jl.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "all",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "dir1",
                  $tfile2, $tfile3, $tfile4, $OdeTest::tmpfile);
  OdeFile::deltree( \@delete_list );
  return $error;
}

1;
