#########################################
#
# Rulestest module
# - module to run rulestest for different
#   test levels
#
#########################################

package Rulestest;

use File::Path;
use File::Copy;
use File::Basename;
use OdeEnv;
use OdeFile;
use OdeTest;
use OdeUtil;
use OdeInterface;
use strict;

use RulesTargetsTest;
use RulesPassesTest;
use RulesSubDirsTest;
use RulesNewActionsTest;
use RulesPkgTest;

#########################################
#
# sub run
# - run test sequence based on testlevel
#
#########################################
sub run()
{
  my $status;

  if ( $OdeUtil::arghash{'testlevel'} eq "normal" )
  {
    $status = normal();
    return( $status );
  }
  elsif ( $OdeUtil::arghash{'testlevel'} eq "regression" )
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
# - TODO: clean up all bogus file,dir names
# - logic???
#########################################
sub normal()
{
  my $command;
  my $status;
  my $error = 0;
  my @delete_list = ($OdeTest::test_sb, $OdeTest::test_sb_rc);
  my $cmd_file = $OdeEnv::tempdir . $OdeEnv::dirsep . "ode_input.txt";
  my $logger_c = join( $OdeEnv::dirsep,
       ($OdeTest::test_sb_src, "bin", "logger", "logger.c"));
  my $logger_o = join( $OdeEnv::dirsep,
       ($OdeTest::test_sb_obj, "bin", "logger", "logger" . $OdeEnv::obj_suff));
  my $logger_exe = join( $OdeEnv::dirsep,
       ($OdeTest::test_sb_obj, "bin", "logger", "logger" . $OdeEnv::prog_suff));
  my $logger_exe_inst;
  if (defined( $OdeEnv::MVS ))
  {
    $logger_exe_inst = join( $OdeEnv::dirsep, ($OdeTest::test_sb_inst,
      "shipdata", "usr", "lpp", "odehello", "bin", "logger.p"));
  }
  elsif (defined( $OdeEnv::AIX ))
  {
    $logger_exe_inst = join( $OdeEnv::dirsep, ($OdeTest::test_sb_inst,
      "shipdata", "usr", "odehello", "bin", "logger") );
  }
  elsif (defined( $OdeEnv::HP ) || defined( $OdeEnv::SOLARIS ) ||
         defined( $OdeEnv::SCO ) || defined( $OdeEnv::IRIX ))
  {
    $logger_exe_inst = join( $OdeEnv::dirsep, ($OdeTest::test_sb_inst,
      "shipdata", "opt", "odehello", "bin", "logger") );
  }
  elsif (defined( $OdeEnv::OS2 ))
  {
    $logger_exe_inst = join( $OdeEnv::dirsep, ($OdeTest::test_sb_tools,
      "usr", "lpp", "odehello", "bin", "logger" . $OdeEnv::prog_suff));
  }
  elsif (defined( $OdeEnv::WIN32 ))
  {
    $logger_exe_inst = join( $OdeEnv::dirsep, ($OdeTest::test_sb_inst,
      "mdata", "OdeHello", "usr", "lpp", "odehello", "bin",
      "logger" . $OdeEnv::prog_suff));
  }
  else # For all other OSes
  {
    $logger_exe_inst = join( $OdeEnv::dirsep, ($OdeTest::test_sb_inst,
      "shipdata", "usr", "lpp", "odehello", "bin",
      "logger" . $OdeEnv::prog_suff));
  }

  OdeInterface::logPrint( "in Rulestest - test level is normal\n");

  OdeEnv::runOdeCommand( "mk -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

  # remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  $command = "mksb -auto -back $OdeTest::test_bbdir -dir $OdeTest::test_sbdir"
           . " -rc $OdeTest::test_sb_rc -m $OdeEnv::machine "
           . $OdeTest::test_sb_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  # Create sandbox
  $status = OdeEnv::runOdeCommand( $command );
  if ( ($status == 0) && (-d $OdeTest::test_sb) && (-f $OdeTest::test_sb_conf))
  {
    # call mk in workon logger.exe out of date - Test #1
    open( INPUT, "> $cmd_file" ) ||
      OdeInterface::logPrint( "Could not open file $cmd_file\n" );
    print( INPUT "mklinks -copy -auto bin/logger/logger.c\n" );
    print( INPUT "cd bin" . $OdeEnv::dirsep . "logger\n" );
    print( INPUT "mk -a\n");
    close( INPUT );
    $command = "workon -rc $OdeTest::test_sb_rc < $cmd_file " .
               " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $status = OdeEnv::runOdeCommand( $command );
    if (($status == 0) && (-f $logger_o) &&
        (-f $logger_c) && (-f $logger_exe))
    {
      OdeInterface::printResult( "pass", "normal", "rules", 1, "mk logger.exe");
    }
    else
    {
      OdeInterface::printResult( "fail", "normal", "mk", 1, "mk logger.exe");
      $error = 1;
    }
    # call mk in workon logger install_all - Test #2
    open( INPUT, "> $cmd_file" ) ||
      OdeInterface::logPrint( "Could not open file $cmd_file\n" );
    print( INPUT "cd bin" . $OdeEnv::dirsep . "logger\n" );
    print( INPUT "mk -a install_all\n");
    close( INPUT );
    $command = "workon -rc $OdeTest::test_sb_rc < $cmd_file " .
               ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $status = OdeEnv::runOdeCommand( $command );
    if (($status == 0) && (-f $logger_exe_inst))
    {
      OdeInterface::printResult( "pass", "normal", "rules", 2,
                                 "mk install_all (logger)");
    }
    else
    {
      OdeInterface::printResult( "fail", "normal", "rules", 2,
                                 "mk install_all (logger)");
      $error = 1;
    }
    # call mk in workon logger clobber_all - Test #3
    open( INPUT, "> $cmd_file" ) ||
      OdeInterface::logPrint( "Could not open file $cmd_file\n" );
    print( INPUT "cd bin" . $OdeEnv::dirsep . "logger\n" );
    print( INPUT "mk -a clobber_all\n");
    close( INPUT );
    $command = "workon -rc $OdeTest::test_sb_rc < $cmd_file " .
               ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $status = OdeEnv::runOdeCommand( $command );
    if (($status == 0) && (!-f $logger_o) &&
        (-f $logger_c) && (!-f $logger_exe))
    {
      OdeInterface::printResult( "pass", "normal", "rules", 3,
                                 "mk clobber_all (logger)");
    }
    else
    {
      OdeInterface::printResult( "fail", "normal", "rules", 3,
                                 "mk clobber_all (logger)");
      $error = 1;
    }
  }
  else
  {
    # mksb doesn't work
      OdeInterface::printError( "mk : mksb does not work");
      $error = 1;
  }
    
  OdeFile::deltree( \@delete_list );

  return( $error );
}


#########################################
#
# sub fvt
# - execute FVT set of tests
#
#########################################
sub fvt()
{
  my $status;
  OdeInterface::logPrint( 
    "INFO: Rulestest.pm fvt() not implemented, calling regression\n" );
  $status = regression();
  return $status;
}

#########################################
#
# sub regression
# - execute regression set of tests
#
#########################################
sub regression()
{
  my $status;
  my $passes_error = 0;
  my $subdirs_error = 0;
  my $newactions_error = 0; 
  my $targets_error = 0;
  my $pkg_error = 0;
  my $rules_error = 0; 
  my $bbdir = $OdeEnv::tempdir . $OdeEnv::dirsep . "bbexample";
  my $cmd_file = $OdeEnv::tempdir . $OdeEnv::dirsep . "ode_input.txt";
  my @delete_list = ($bbdir, $cmd_file, $OdeTest::tmpfile);

  OdeFile::deltree( \@delete_list );

  # create and make a bbexample
  $status = makebbexample();
  if ($status)
  {
    OdeFile::deltree( \@delete_list );
    return $status;
  }

  # execute predefined passes test
  $status = RulesPassesTest::run();
  if ($status) 
  {
    $passes_error = $status;
    $rules_error = 1;
  }  
  # execute subdirs test
  $status = RulesSubDirsTest::run();
  if ($status) 
  {
    $subdirs_error = $status;
    $rules_error = 1;
  }  
  # execute new actions/passes test
  $status = RulesNewActionsTest::run();
  if ($status) 
  {
    $newactions_error = $status;
    $rules_error = 1;
  }  
  # if testlevel is fvt execute predefined targets test
  if ( $OdeUtil::arghash{'testlevel'} eq "fvt" )
  {
    $status = RulesTargetsTest::run();
    if ($status) 
    {
      $targets_error = $status;
      $rules_error = 1;
    }  
  }
  # execute packaging rules test
  $status = RulesPkgTest::run();
  if ($status)
  {
    $pkg_error = $status;
    $rules_error = 1;
  }

  # check all error codes and report accordingly
  if ($rules_error)
  {
    if ($passes_error) 
    { 
      OdeInterface::logPrint( "  Rules - predefined passes test failed\n");
    }
    if ($subdirs_error) 
    { 
      OdeInterface::logPrint( "  Rules - SUBDIRS test failed\n");
    }
    if ($newactions_error) 
    { 
      OdeInterface::logPrint( "  Rules - New actions/passes test failed\n");
    }
    if ($targets_error) 
    { 
      OdeInterface::logPrint( "  Rules - predefined targets test failed\n");
    }
    if ($pkg_error)
    {
      OdeInterface::logPrint( "  Rules - Packaging rules test failed\n");
    }
  }
  else
  {
    OdeInterface::logPrint( "All rules tests passed!\n");
  }
  OdeFile::deltree( \@delete_list );
  return( $rules_error );

}

#########################################
#
# sub makebbexample
# - create and make a bbexample for the 
#   tests
#
#########################################
sub makebbexample()
{
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $tmpstr;
  my $cmd_file = $OdeEnv::tempdir . $OdeEnv::dirsep . "ode_input.txt";
  my $bb_dir;
  my $bb_rc;
  my $srcfiles;
  my $rulesfiles;
  my $bbsrcdir;
  my $rulesdir;

  OdeInterface::logPrint( "in RulesTest\n");

  $bb_dir = $OdeEnv::tempdir . $OdeEnv::dirsep . "bbexample";
  $bb_rc = join( $OdeEnv::dirsep, ($OdeEnv::tempdir, "bbexample", ".sbrc") );
  if (!$OdeEnv::bbex_dir)
  {
    OdeEnv::createBbExample( $OdeEnv::tempdir );
  }
  else
  {
    $command = "mkbb -rc $bb_rc -dir $OdeEnv::tempdir -m $OdeEnv::machine " .
               "-auto bbexample";
    $status = OdeEnv::runOdeCommand( $command );
    if ($status)
    {
      $error_txt = "mkbb returned a non-zero exit code";
    }
    if (!$status)
    {
      $rules_error = OdeTest::validateSandbox( $bb_dir, $bb_rc, \$error_txt );
    }
    if ($status || $rules_error)
    {
      OdeInterface::printError(
                    "rules Predefined targets Test : mkbb does not work" );
      OdeInterface::logPrint( "Test Failed: $error_txt\n" );
      $error = 1;
      return $error;
    }
    $srcfiles = join( $OdeEnv::dirsep,
                      ($OdeEnv::bbex_dir, "bbexample", "src") );
    if (defined ($OdeEnv::UNIX))
    {
      $bb_dir = $OdeEnv::tempdir . $OdeEnv::dirsep . "bbexample";
      $status = OdeEnv::runSystemCommand(
                        "$OdeEnv::copy $srcfiles $bb_dir" );
      if ($status) 
      { 
        OdeFile::deltree( $OdeEnv::tempdir );
        die("copy failed!\n"); 
      }
      $status = OdeEnv::runSystemCommand( "chmod -R +w $bb_dir" );
      if ($status) 
      { 
        OdeFile::deltree( $OdeEnv::tempdir );
        die("chmod failed!\n"); 
      }
    }
    else
    {
      $bb_dir = join( $OdeEnv::dirsep, ($OdeEnv::tempdir, "bbexample", "src") );
      $status = OdeEnv::runSystemCommand(
                      "$OdeEnv::copy $srcfiles $bb_dir" );
      if ($status) 
      { 
        OdeFile::deltree( $OdeEnv::tempdir );
        die("copy failed!\n"); 
      }
    }
  }
  open( INPUT, "> $cmd_file" ) ||
  OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "mk > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $bb_rc < $cmd_file " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  print( BLDLOG "Executing: mk > $OdeTest::tmpfile $OdeEnv::stderr_redir; exit;\n" );
  close( BLDLOG );
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  if ($status)
  {
    OdeInterface::printError(
                  "rules Predefined targets Test" );
    OdeInterface::logPrint( 
                "Test Failed: workon or mk returned a non-zero exit code\n" );
    $error = 1;
    return $error;
  }
    
}

1;
