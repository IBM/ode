#########################################
#
# Buildtest module
# - module to run Buildtest for different
#   test levels
#
#########################################

package Buildtest;
use strict;
use OdeUtil;
use OdeEnv;
use OdeTest;
use OdePath;
use OdeInterface;
use Cwd;
use File::Copy;
use File::Path;

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
         "ERROR: undefined testlevel $OdeUtil::arghash{'testlevel'}\n");
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
  my $file;
  my $filestr;
  my $filestr1;
  my $dir = cwd();
  my $bbdir;
  $bbdir = $OdeEnv::tempdir . $OdeEnv::dirsep . "bbexample";

  OdeInterface::logPrint( "in Buildtest - test level is normal\n");

  if (-d $bbdir)
  {
    OdeFile::deltree( $bbdir );
  }
  $status = makebbexample();
  if ($status)
  {
    OdeFile::deltree( $bbdir );
    return $status;
  }

  OdeEnv::runOdeCommand( "build -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");
  $filestr = $bbdir . $OdeEnv::dirsep . ".sbrc";
  $filestr1 = join( $OdeEnv::dirsep, 
                    ($bbdir, "src", "bin", "server", "server.c") );
  if ( !(-f $filestr) || !(-f $filestr1) )
  {
    #Test case failed
    OdeInterface::printError( 
              "build : rc file or server.c file does not exist" );
    $error = 1;
    return $error;
  }
  OdePath::chgdir( $bbdir . $OdeEnv::dirsep . "src" );
  $filestr = "..$OdeEnv::dirsep" . ".sbrc";
  $command = "build -rc $filestr clobber_all >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  $file = "server" . $OdeEnv::obj_suff;
  $filestr1 = join( $OdeEnv::dirsep, ($bbdir, "obj",
                                   $OdeEnv::machine, "bin", "server", $file) );
  #check command exit code 
  if (($status == 0) && !(-f $filestr1))
  {
    #Test 1 Passed
    OdeInterface::printResult( "pass", "normal", "build", 1, 
                               "build -rc clobber_all" );
  }
  else
  {
    #Test 1 Failed
    OdeInterface::printResult( "fail", "normal", "build", 1, 
                               "build -rc clobber_all" );
    $error = 1;
  }

  $filestr = join( $OdeEnv::dirsep, ($bbdir, "obj",
                                     $OdeEnv::machine, "bin", "server") );
  OdeFile::deltree( $filestr );
  $filestr = "..$OdeEnv::dirsep" . ".sbrc";
  $command = "build -rc $filestr >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if (($status == 0) && (-f $filestr1))
  {
    #Test2 passed
    OdeInterface::printResult( "pass", "normal", "build", 2, "build -rc" );
  }
  else
  {
    #Test2 failed
    OdeInterface::printResult( "fail", "normal", "build", 2, "build -rc" );
    $error = 1;
  }

  if (!$OdeEnv::MVS)
  {
    $command = "build -rc $filestr install_all >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $status = OdeEnv::runOdeCommand( $command );
    if ($OdeEnv::machine eq "x86_nt_4")
    {
      $filestr1 = join( $OdeEnv::dirsep, ($bbdir, "inst.images",
                                   $OdeEnv::machine, "mdata", "ODEHELLO", "usr",
                                   "lpp", "odehello", "bin", "server.exe") );
    }
    elsif ($OdeEnv::machine eq "x86_os2_4")
    {
      $filestr1 = join( $OdeEnv::dirsep, ($bbdir, "tools",
                                          $OdeEnv::machine, "usr", "lpp",
                                          "odehello", "bin", "server.exe") );
    }
    else
    {
      if ((defined ($OdeEnv::LINUX)) ||
          (defined ($OdeEnv::ANY_BSD))) 
      {
        $filestr1 = join( $OdeEnv::dirsep, ($bbdir, "inst.images",
                                          $OdeEnv::machine, "shipdata", "usr",
                                          "lpp", "odehello", "bin", "server") );
       }
      elsif (defined( $OdeEnv::AIX ))
      {
    $filestr1 = join( $OdeEnv::dirsep, ($bbdir, "inst.images",
                                          $OdeEnv::machine, "shipdata", "usr",
                                          "odehello", "bin", "server") );
      }
      else
      {
             $filestr1 = join( $OdeEnv::dirsep, ($bbdir, "inst.images",
                                          $OdeEnv::machine, "shipdata", "opt",
                                          "odehello", "bin", "server") );
      }
    
    }
    if (($status == 0) && (-f $filestr1))
    {
      $filestr1 .= " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
      $status = OdeEnv::runOdeCommand( $filestr1 );
      if ($status == 0)
      {
        #Test3 passed
        OdeInterface::printResult( "pass", "normal", "build", 3,
                                   "build -rc install_all" );
        OdeInterface::logPrint( "installed example executed correctly\n" );
      }
      else
      {
        #Test3 failed
        OdeInterface::printResult( "fail", "normal", "build", 3,
                                   "build -rc install_all" );
        OdeInterface::logPrint( 
                        "installed example does not execute correctly\n" );
        $error = 1;
      }
    }
    else
    {
      #Test3 failed
      OdeInterface::printResult( "fail", "normal", "build", 3,
                                 "build -rc install_all" );
      OdeInterface::logPrint( "example is not installed\n" );
      $error = 1;
    }
  }
  elsif ($OdeEnv::MVS)
  {
    $command = "build -rc $filestr install_all >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $status = OdeEnv::runOdeCommand( $command );
    $filestr = join( $OdeEnv::dirsep, ($bbdir, "inst.images",
                                       $OdeEnv::machine, "shipdata", "usr",
                                       "lpp", "odehello", "bin", "server.p") );
    $filestr1 = join( $OdeEnv::dirsep, ($bbdir, "inst.images",
                                        $OdeEnv::machine, "shipdata", "usr",
                                      "lpp", "odehello", "bin", "server.lst") );
    if (($status == 0) && (-f $filestr) && (-f $filestr1))
    {
      #Test3 passed
      OdeInterface::printResult( "pass", "normal", "build", 3,
                                 "build -rc install_all" );
    }
    else
    {
      #Test3 failed
      OdeInterface::printResult( "fail", "normal", "build", 3,
                                 "build -rc install_all" );
      OdeInterface::logPrint( "example is not installed\n" );
      $error = 1;
    }
  }

  $filestr = "..$OdeEnv::dirsep" . ".sbrc";
  $command = "build -rc $filestr clobber_all >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  $filestr1 = join( $OdeEnv::dirsep, 
                    ($bbdir, "obj", $OdeEnv::machine, 
                     "bin", "example.o") );
  if (($status == 0) && !(-f $filestr1))
  {
    #Test4 passed
    OdeInterface::printResult( "pass", "normal", "build", 4,
                                     "build -rc clobber_all" );
  }
  else
  {
    #Test4 failed 
    OdeInterface::printResult( "fail", "normal", "build", 4,
                                     "build -rc clobber_all" );
  }
  OdePath::chgdir( $dir );
  OdeFile::deltree( $bbdir );
  return $error;
}

    
#########################################
#
# sub regression
# - execute regression set of tests
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
  my $filestr;
  my $filestr1;
  my $filestr2;
  my $num_files = 0;
  my $dir = cwd();
  my $bbdir;
  $bbdir = $OdeEnv::tempdir . $OdeEnv::dirsep . "bbexample";
  my @delete_list = ( $OdeTest::test_sb_rc, $OdeTest::test_sb, 
                      $OdeTest::tmpfile, $bbdir );

  OdeInterface::logPrint( "in buildtest - test level is regression\n");

  #Remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  $status = makebbexample();
  if ($status)
  {
    OdeFile::deltree( \@delete_list );
    return $status;
  }

  OdeEnv::runOdeCommand( "build -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

  $command = "mksb -dir $OdeTest::test_sbdir -rc $OdeTest::test_sb_rc "
             . "-back $bbdir $OdeTest::test_sb_name "
             . "-m $OdeEnv::machine -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $status = OdeEnv::runOdeCommand( $command );
  if (($status != 0) || (OdeTest::validateSandbox( $OdeTest::test_sb,
                                        $OdeTest::test_sb_rc, \$error_txt ))) 
  {
    #Test case failed
    OdeInterface::printError( "build: mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  $filestr = join( $OdeEnv::dirsep, 
                   ($bbdir, "obj", $OdeEnv::machine) );
  if (-d $filestr)
  {
    OdeFile::deltree( $filestr );
  }
  $filestr = join( $OdeEnv::dirsep, 
                   ($bbdir, "inst.images", $OdeEnv::machine) );
  if (-d $filestr)
  {
    OdeFile::deltree( $filestr );
  }
  $filestr = join( $OdeEnv::dirsep, 
                   ($bbdir, "tools", $OdeEnv::machine) );
  if (-d $filestr)
  {
    OdeFile::deltree( $filestr );
  }
  $filestr = join( $OdeEnv::dirsep, 
                   ($bbdir, "export", $OdeEnv::machine) );
  if (-d $filestr)
  {
    OdeFile::deltree( $filestr );
  }

  ## Regression Test Case 1 - run build -rc export_all
  ## Should build the current directory
  $filestr = join( $OdeEnv::dirsep, 
                   ($OdeTest::test_sb, "src", "inc") );
  mkpath( $filestr );
  OdePath::chgdir( $filestr );
  #UNIX assumes that server.h exists in the src/inc of sandbox when export_all
  #is used and it makes a symbolic link to it. Since it does'nt exist, the
  #file is to be copied into src/inc
  if (defined( $OdeEnv::UNIX ))
  {
    $filestr1 = join( $OdeEnv::dirsep, ($bbdir, "src", 
                                        "inc", "server.h") ); 
    copy( $filestr1, $filestr . $OdeEnv::dirsep . "server.h" );
    if (!(-f $filestr . $OdeEnv::dirsep . "server.h"))
    {
      OdeInterface::logPrint( 
              "server.h not present in the src\inc of the sandbox\n" );
    }
  }
  $command = "build -rc $OdeTest::test_sb_rc export_all "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "build -rc export_all";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "export", 
                                     $OdeEnv::machine, "usr", "include", 
                                     "server.h") );
  if (!$reg_error && !(-f $filestr) && !(-l $filestr))
  {
    $reg_error = 1;
    $error_txt = "$filestr does not exist";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "build", 1, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "build", 1, $tmpstr );
  }

  #get back to the earlier current directory
  OdePath::chgdir( $dir );
  $dir = cwd();

  ## Regression Test Case 2 - run build -rc -here 
  ## Should build the target specified by -here 
  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "lib") );
  mkpath( $filestr );
  $filestr = $OdeEnv::dirsep . "lib";
  $command = "build -rc $OdeTest::test_sb_rc -here $filestr "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "build -rc -here";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "obj", $OdeEnv::machine,
                                     "lib") );
  if (!$reg_error && 
          !(-f ($filestr . $OdeEnv::dirsep . "printmsg" . $OdeEnv::obj_suff)))
  {
    $reg_error = 1;
    $error_txt = "$filestr does not contain printmsg$OdeEnv::obj_suff"
  }
  if (!$reg_error && !(-f ($filestr . $OdeEnv::dirsep . "depend.mk")))
  {
    $reg_error = 1;
    $error_txt = "$filestr does not contain depend.mk"
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "build", 2, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "build", 2, $tmpstr );
  }

  ## Regression Test Case 3 - run build -rc; build install_all
  ## Should build the target directory and put the executables in the
  ## inst.images directory
  $filestr = join( $OdeEnv::dirsep, 
                   ($OdeTest::test_sb, "src", "bin", "server") );
  mkpath( $filestr );
  $filestr1 = $OdeEnv::dirsep . "bin" . $OdeEnv::dirsep . "server";
  $command = "build -rc $OdeTest::test_sb_rc -here $filestr1 " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "build -rc -here";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "obj", 
                                     $OdeEnv::machine, "bin", "server") );
  if (!(defined( $OdeEnv::UNIX ))) 
  {
    $filestr .= $OdeEnv::dirsep . "server.exe";
  }
  else
  {
    $filestr .= $OdeEnv::dirsep . "server";
  }
  if (!$reg_error && !(-f $filestr))
  {
    $reg_error = 1;
    $error_txt = "build did not make $filestr";
  }
  if (!$reg_error)
  {
    $filestr1 .= $OdeEnv::dirsep . "install_all";
    $command = "build -rc $OdeTest::test_sb_rc $filestr1 " .
               ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $tmpstr = "build -rc install_all";
    $status = OdeEnv::runOdeCommand( $command );
    #check command exit code
    if ($status != 0) 
    {
      $reg_error = 1;
      $error_txt = "build returned a non-zero exit code";
    }
    if ($OdeEnv::machine eq "x86_nt_4")
    {
      $filestr1 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "inst.images",
                                   $OdeEnv::machine, "mdata", "ODEHELLO", "usr",
                                   "lpp", "odehello", "bin", "server.exe") );
    }
    elsif ($OdeEnv::machine eq "x86_os2_4")
    {
      $filestr1 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "tools",
                                          $OdeEnv::machine, "usr", "lpp",
                                          "odehello", "bin", "server.exe") );
    }
    elsif ($OdeEnv::MVS)
    {
      $filestr1 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "inst.images",
                                        $OdeEnv::machine, "shipdata", "usr",
                                       "lpp", "odehello", "bin", "server.p") );
    }
    else
    {
      if (defined($OdeEnv::ANY_BSD))
      {
        $filestr1 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "inst.images",
                                            $OdeEnv::machine, "shipdata", "usr",
                                            "lpp", "odehello", "bin",
                                            "server") );
      }
      elsif (defined( $OdeEnv::AIX ) ||
             defined ($OdeEnv::LINUX)) 
      {
        $filestr1 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "inst.images",
                                            $OdeEnv::machine, "shipdata", "usr",
                                            "odehello", "bin", "server") );        
      }
      else
      {
        $filestr1 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "inst.images",
                                            $OdeEnv::machine, "shipdata", "opt",
                                            "odehello", "bin", "server") );

      }
    }
    if (!$reg_error && !(-f $filestr1))
    {
      $reg_error = 1;
      $error_txt = "file not installed in inst.images";
    }
    $command = "$filestr >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $status = OdeEnv::runSystemCommand( $command );
    #check command exit code
    if ($status != 0) 
    {
      $reg_error = 1;
      $error_txt = "executable made by build returned a non-zero exit code";
    }
  }
    
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "build", 3, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "build", 3, $tmpstr );
  }
  $filestr1 = join( $OdeEnv::dirsep, 
                    ($OdeTest::test_sb, "obj", $OdeEnv::machine, "bin",
                                                                 "server") );
  #deleting obj/bin/server
  OdeFile::deltree( $filestr1 );
  if (-f $filestr1)
  {
    OdeInterface::logPrint( "$filestr1 not deleted\n" );
  }

  ## Regression Test Case 4 - run build depend_all
  ## Should create depend.mk in bin\server of obj directory
  $filestr = $OdeEnv::dirsep . "bin" . $OdeEnv::dirsep . "server" .
             $OdeEnv::dirsep . "depend_all";
  $command = "build -rc $OdeTest::test_sb_rc $filestr >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "build -rc depend_all";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "obj", $OdeEnv::machine,
                                     "bin", "server") );
  if (!$reg_error && (!(-f $filestr . $OdeEnv::dirsep . "depend.mk") ||
                       (OdeFile::numDirEntries( $filestr ) != 1)))
  {
    $reg_error = 1;
    $error_txt = "depend.mk not created or extra files created";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "build", 4, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "build", 4, $tmpstr );
  }

  #deleting obj/bin/server
  OdeFile::deltree( $filestr1 );
  if (-f $filestr1)
  {
    OdeInterface::logPrint( "$filestr1 not deleted\n" );
  }

  ## Regression Test Case 5 - run build comp_all
  ## Should build the target
  $filestr = $OdeEnv::dirsep . "bin" . $OdeEnv::dirsep . "server" .
             $OdeEnv::dirsep . "comp_all";
  $command = "build -rc $OdeTest::test_sb_rc $filestr >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "build -rc comp_all";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "obj", $OdeEnv::machine,
                                     "bin", "server") );
  if (!$reg_error && (!(-f $filestr . $OdeEnv::dirsep . "depend.mk") ||
                       (OdeFile::numDirEntries( $filestr ) <= 1)))
  {
    $reg_error = 1;
    $error_txt = "depend.mk not created or less number of files created";
  }
  if (!$reg_error)
  {
    $filestr = $OdeTest::test_sb . $OdeEnv::dirsep . "export";
    if (-d $filestr)
    {
      OdeFile::deltree( $filestr );
    }
    else
    {
      OdeInterface::logPrint( "$filestr does not exist" );
    }
    $filestr1 = $OdeEnv::dirsep . "inc" . $OdeEnv::dirsep . "comp_all";
    $command = "build -rc $OdeTest::test_sb_rc $filestr1 "
               . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $tmpstr = "build -rc comp_all";
    $status = OdeEnv::runOdeCommand( $command );
    #check command exit code
    if ($status != 0) 
    {
      $reg_error = 1;
      $error_txt = "build returned a non-zero exit code";
    }
    if (!$reg_error && 
          ((-d $filestr) || (-f $filestr . $OdeEnv::dirsep . "server.h")))
    {
      $reg_error = 1;
      $error_txt = "$filestr created";
    }
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "build", 5, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "build", 5, $tmpstr );
  }

  ## Regression Test Case 6 - run build crap_all clobber_all -ignore
  ## Should skip the invalid target and remove the files in obj directory
  $filestr = $OdeEnv::dirsep . "bin" . $OdeEnv::dirsep . "crap_all";
  $filestr1 = $OdeEnv::dirsep . "bin" . $OdeEnv::dirsep . "clobber_all";
  $command = "build -rc $OdeTest::test_sb_rc $filestr $filestr1 -ignore"
             . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "build -rc crap_all clobber_all -ignore";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if ($status != 0) 
  {
    $reg_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $filestr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "obj", 
                                     $OdeEnv::machine, "bin", "server") );
  if ($OdeEnv::MVS)
  {
    if (!$reg_error && (OdeFile::numDirEntries( $filestr ) != 6))
    {
      $reg_error = 1;
      $error_txt = "incorrect number of files in $filestr deleted";
    }
  }
  else
  {
    if (!$reg_error && (OdeFile::numDirEntries( $filestr ) != 0))
    {
      $reg_error = 1;
      $error_txt = "all the files in $filestr not deleted";
    }
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "build", 6, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $reg_error = 0;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "build", 6, $tmpstr );
  }

  ## Regression Test Case 7 - run build -rc -badflag
  ## Should produce an error
  $command = "build -rc $OdeTest::test_sb_rc -badflag > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $tmpstr = "build -rc -badflag";
  $status = OdeEnv::runOdeCommand( $command );

  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );

  #check command exit code
  if ($status == 0) 
  {
    $reg_error = 1;
    $error_txt = "build returned a zero exit code on error";
  }
  if (!$reg_error && !(OdeFile::findInFile( "ERROR", $OdeTest::tmpfile )))
  {
    $reg_error = 1;
    $error_txt = "command did not produce an error";
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "build", 7, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "build", 7, $tmpstr );
  }
  OdeFile::deltree( \@delete_list );
  return $error;
}

#########################################
#
# sub fvt
# - execute FVT set of tests
#
#########################################
sub fvt ()
{
  print("fvt not implemented yet!\n");
}
             
#########################################
#
# sub makebbexample
# - create a bbexample for the tests
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
                           "build Test : mkbb does not work" );
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
  return $status;
}

1;
