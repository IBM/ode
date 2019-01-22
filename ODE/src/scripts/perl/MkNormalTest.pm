###########################################
#
# MkNormalTest module
# - module to test the normal function of mk
#   all the special passes are tested as
#   part of rules
#
###########################################

package MkNormalTest;

use File::Path;
use File::Basename;
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
  my $cmd_file = $OdeEnv::tempdir . $OdeEnv::dirsep . "ode_input.txt";
  my $tmpstr;
  my $srcfiles;
  my $rulesfiles;
  my $bbsrcdir;
  my $rulesdir;
  my $libfile;
  my @delete_list;
  my $tfile1;
  my $tfile2;
  my $tfile3;
  my $tfile4;
  my $tfile5;
  my $mtime1;
  my $mtime2;
  my $mtime3;
  my $mtime4;
  my $mtime5;
  my $filecount1;
  my $filecount2;
  my $bb_dir = $OdeEnv::tempdir . $OdeEnv::dirsep . "bbexample";
  @delete_list = ($OdeTest::test_sb, $OdeTest::test_sb_rc, $cmd_file, 
                  $OdeTest::tmpfile, $bb_dir);

  OdeInterface::logPrint( "in MkNormalTest\n");

  #Remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  $status = makebbexample();
  if ($status)
  {
    OdeFile::deltree( \@delete_list );
    return $status;
  }

  ## creating the sandbox
  $command = "mksb -auto -back $bb_dir -dir $OdeTest::test_sbdir" .
             " -rc $OdeTest::test_sb_rc -m $OdeEnv::machine " .
             "$OdeTest::test_sb_name >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $error_txt = "mksb returned a non-zero exit code";
  }
  if (!$status)
  {
    $mk_error = OdeTest::validateSandbox(
                    $OdeTest::test_sb, $OdeTest::test_sb_rc, \$error_txt );
  }
  if ($status || $mk_error)
  {
    OdeInterface::printError( "mk Normal Test : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    return $error;
  }
  $tmpstr = join( $OdeEnv::dirsep, 
                  ($OdeTest::test_sb, "obj", $OdeEnv::machine) ); 
  ## make sure that the obj/machine directory in the sandbox is empty
  if (OdeFile::numDirEntries( $tmpstr ) > 0)
  {
    OdeInterface::logPrint( "obj directory in the sandbox is not empty\n" );
  }

  if (defined( $OdeEnv::MVS ))
  {
    $filecount1 = 2;
    $filecount2 = 13;
  }
  else
  {
    $filecount1 = 1;
    $filecount2 = 7;
  }
  ## Test 1: running mk in src/bin/server in the sandbox and making sure 
  ## nothing is built
  $tmpstr = join( $OdeEnv::dirsep, 
                  ($OdeTest::test_sb, "src", "bin", "server") ); 
  mkpath( $tmpstr );
  open( INPUT, "> $cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "cd $tmpstr\n" );
  print( INPUT "mk > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc < $cmd_file " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if (defined( $OdeEnv::UNIX ))
  {
    open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
      warn ("Could not open file $OdeEnv::bldcurr\n");
    print( BLDLOG 
         "Executing: cd $tmpstr; mk > $OdeTest::tmpfile $OdeEnv::stderr_redir; exit;\n" );
    close( BLDLOG );
  }
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  if ($status)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  $tmpstr = join( $OdeEnv::dirsep, 
               ($OdeTest::test_sb, "obj", $OdeEnv::machine, "bin", "server") ); 
  if (!$mk_error && (! -d $tmpstr))
  {
    $mk_error = 1;
    $error_txt = "$tmpstr not created\n";
  }
  if (!$mk_error && (OdeFile::numDirEntries( $tmpstr) > 0))
  {
    $mk_error = 1;
    $error_txt = "src/bin/server is built when the files are up to date";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, 
                               "mk", 1, "Normal Test" );
    OdeInterface::logPrint( "test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, 
                               "mk", 1, "Normal Test" );
  }
  $tmpstr = join( $OdeEnv::dirsep, 
               ($OdeTest::test_sb, "obj", $OdeEnv::machine, "bin") ); 
  OdeFile::deltree( $tmpstr );
  if (-d $tmpstr)
  {
    OdeInterface::logPrint( "$tmpstr not deleted\n" );
  }

  ## Test 2: running mk -a in src/lib in the sandbox 
  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "lib") );
  mkpath( $tmpstr );
  open( INPUT, "> $cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "cd $tmpstr\n" );
  print( INPUT "mk -a > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc < $cmd_file " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if (defined( $OdeEnv::UNIX ))
  {
    open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
      warn ("Could not open file $OdeEnv::bldcurr\n");
    print( BLDLOG 
         "Executing: cd $tmpstr; mk -a > $OdeTest::tmpfile $OdeEnv::stderr_redir; exit;\n" );
    close( BLDLOG );
  }
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  if ($status)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  if (defined( $OdeEnv::UNIX ))
  {
    $libfile = "libexa.a";
  }
  else
  {
    $libfile = "exa.lib";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "obj", 
                                          $OdeEnv::machine, "lib") );
  if (!$mk_error && ((! -f $tmpstr . $OdeEnv::dirsep . $libfile) ||
            (! -f $tmpstr . $OdeEnv::dirsep . "printmsg" . $OdeEnv::obj_suff) ||
            (! -f $tmpstr . $OdeEnv::dirsep . "printnl" . $OdeEnv::obj_suff) ||
            (! -f $tmpstr . $OdeEnv::dirsep . "depend.mk")))
  {
    $mk_error = 1;
    $error_txt = "a file(s) missing in $tmpstr\n"; 
  }
  $tmpstr = dirname( $tmpstr );
  if (!$mk_error && ((-d $tmpstr . $OdeEnv::dirsep . "bin") ||
                     (-d $tmpstr . $OdeEnv::dirsep . "inc") ||
                     (-d $tmpstr . $OdeEnv::dirsep . "COM") ||
                     (-d $tmpstr . $OdeEnv::dirsep . "doc")))
  {
    $mk_error = 1;
    $error_txt = "directories other than lib made in $tmpstr";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "export", 
                                  $OdeEnv::machine, "usr", "lib", $libfile) );
  if (!$mk_error && (! -f $tmpstr))
  {
    $mk_error = 1;
    $error_txt = "$tmpstr not made when library is built\n";
  }
  $tmpstr = dirname( $tmpstr );
  if (!$mk_error && (OdeFile::numDirEntries( $tmpstr ) > 1))
  {
    $mk_error = 1;
    $error_txt = "files other than $libfile present in $tmpstr";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, 
                               "mk", 2, "Normal Test" );
    OdeInterface::logPrint( "test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, 
                               "mk", 2, "Normal Test" );
  }

  ## Test 3: running mk in src/bin/server in the sandbox after building
  ## the library
  $tmpstr = join( $OdeEnv::dirsep, 
                  ($OdeTest::test_sb, "src", "bin", "server") ); 
  open( INPUT, "> $cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "cd $tmpstr\n" );
  print( INPUT "mk > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc < $cmd_file " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if (defined( $OdeEnv::UNIX ))
  {
    open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
      warn ("Could not open file $OdeEnv::bldcurr\n");
    print( BLDLOG 
         "Executing: cd $tmpstr; mk > $OdeTest::tmpfile $OdeEnv::stderr_redir; exit;\n" );
    close( BLDLOG );
  }
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  if ($status)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "obj",
                   $OdeEnv::machine, "bin", "server") ); 
  $libfile = "server" . $OdeEnv::prog_suff;
  if (!$mk_error && 
       (! -f $tmpstr . $OdeEnv::dirsep . $libfile))
  {
    $mk_error = 1;
    $error_txt = "$libfile not made";
  }
  if (!$mk_error && (OdeFile::numDirEntries( $tmpstr ) > $filecount1))
  {
    $mk_error = 1;
    $error_txt = "files other than $libfile made in $tmpstr";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, 
                               "mk", 3, "Normal Test" );
    OdeInterface::logPrint( "test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, 
                               "mk", 3, "Normal Test" );
  }

  ## Test 4: running mk -a in src/bin/server in the sandbox 
  $tmpstr .= $OdeEnv::dirsep . $libfile;
  if (-f $tmpstr)
  {
    $mtime1 = (stat $tmpstr)[9];
  }
  else
  {
    OdeInterface::logPrint( "$libfile does not exist\n" );
  }
  ## sleep for 2 secs to make sure that server.exe gets a new filestamp
  sleep 2;
  $tmpstr = join( $OdeEnv::dirsep, 
                  ($OdeTest::test_sb, "src", "bin", "server") ); 
  open( INPUT, "> $cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "cd $tmpstr\n" );
  print( INPUT "mk -a > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc < $cmd_file " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if (defined( $OdeEnv::UNIX ))
  {
    open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
      warn ("Could not open file $OdeEnv::bldcurr\n");
    print( BLDLOG 
         "Executing: cd $tmpstr; mk -a > $OdeTest::tmpfile $OdeEnv::stderr_redir; exit;\n" );
    close( BLDLOG );
  }
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  if ($status)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "obj",
                   $OdeEnv::machine, "bin", "server") ); 
  $libfile = "server" . $OdeEnv::prog_suff;
  if (!$mk_error && 
       (! -f $tmpstr . $OdeEnv::dirsep . $libfile))
  {
    $mk_error = 1;
    $error_txt = "$libfile not made";
  }
  if (!$mk_error &&
       ((stat $tmpstr . $OdeEnv::dirsep . $libfile)[9] == $mtime1))
  {
    $mk_error = 1;
    $error_txt = "$libfile not remade";
  }
  ## Assuming src/bin/server in bbexample doesn't change in future
  if (!$mk_error && (OdeFile::numDirEntries( $tmpstr ) != $filecount2))
  {
    $mk_error = 1;
    $error_txt = "all the files not made in $tmpstr";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, 
                               "mk", 4, "Normal Test" );
    OdeInterface::logPrint( "test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, 
                               "mk", 4, "Normal Test" );
  }

  ## Test 5: touch retserver.c in backing build and run mk in src/bin/server  
  ## in the sandbox
  ## should remake only retserver.obj, retserver.exe and depend.mk
  $tfile1 = $tmpstr . $OdeEnv::dirsep . "retserver" . $OdeEnv::prog_suff;
  $tfile2 = $tmpstr . $OdeEnv::dirsep . "retserver" . $OdeEnv::obj_suff;
  $tfile3 = $tmpstr . $OdeEnv::dirsep . "depend.mk";
  $tfile4 = $tmpstr . $OdeEnv::dirsep . "server" . $OdeEnv::prog_suff;
  $tfile5 = $tmpstr . $OdeEnv::dirsep . "buildserver" . $OdeEnv::obj_suff;
  if (-f $tfile1)
  {
    $mtime1 = (stat $tfile1)[9];
  }
  else
  {
    OdeInterface::logPrint( "$tfile1 does not exist\n" );
  }
  if (-f $tfile2)
  {
    $mtime2 = (stat $tfile2)[9];
  }
  else
  {
    OdeInterface::logPrint( "$tfile2 does not exist\n" );
  }
  if (-f $tfile3)
  {
    $mtime3 = (stat $tfile3)[9];
  }
  else
  {
    OdeInterface::logPrint( "$tfile3 does not exist\n" );
  }
  if (-f $tfile4)
  {
    $mtime4 = (stat $tfile4)[9];
  }
  else
  {
    OdeInterface::logPrint( "$tfile4 does not exist\n" );
  }
  if (-f $tfile5)
  {
    $mtime5 = (stat $tfile5)[9];
  }
  else
  {
    OdeInterface::logPrint( "$tfile5 does not exist\n" );
  }
  $tmpstr = join( $OdeEnv::dirsep, ($OdeEnv::tempdir, "bbexample", "src",
                                    "bin", "server", "retserver.c") );
  ## sleep for 3 secs to make sure that retserver.c and retserver.obj
  ## differ by atleast 3 secs
  sleep 3;
  ## touching retserver.c in the backing build
  OdeFile::touchFile( $tmpstr );
  $tmpstr = join( $OdeEnv::dirsep, 
                  ($OdeTest::test_sb, "src", "bin", "server") ); 
  open( INPUT, "> $cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "cd $tmpstr\n" );
  print( INPUT "mk > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc < $cmd_file " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if (defined( $OdeEnv::UNIX ))
  {
    open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
      warn ("Could not open file $OdeEnv::bldcurr\n");
    print( BLDLOG 
         "Executing: cd $tmpstr; mk > $OdeTest::tmpfile $OdeEnv::stderr_redir; exit;\n" );
    close( BLDLOG );
  }
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  if ($status)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "obj",
                   $OdeEnv::machine, "bin", "server") ); 
  if (!$mk_error && (OdeFile::numDirEntries( $tmpstr ) != $filecount2))
  {
    $mk_error = 1;
    $error_txt = "all the files not made in $tmpstr";
  }
  if (!$mk_error && (((stat $tfile1)[9] == $mtime1) ||
                     ((stat $tfile2)[9] == $mtime2) ||
                     ((stat $tfile3)[9] == $mtime3)))
  {
    $mk_error = 1;
    $error_txt = "$tfile1 or $tfile2 or $tfile3 not remade when retserver.c" .
                 " is touched in the backing build";
  }
  if (!$mk_error && (((stat $tfile4)[9] != $mtime4) ||
                     ((stat $tfile5)[9] != $mtime5)))
  {
    $mk_error = 1;
    $error_txt = "$tfile4 or $tfile5 are made when they are up to date";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, 
                               "mk", 5, "Normal Test" );
    OdeInterface::logPrint( "test Failed: $error_txt\n" );
    $error = 1;
    $mk_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, 
                               "mk", 5, "Normal Test" );
  }
  OdeFile::deltree( \@delete_list );
  return $error;
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
  my $mk_error = 0;
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
      $mk_error = OdeTest::validateSandbox( $bb_dir, $bb_rc, \$error_txt );
    }
    if ($status || $mk_error)
    {
      OdeInterface::printError( "mk Normal Test : mkbb does not work" );
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
  if (defined( $OdeEnv::UNIX ))
  {
    open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
      warn ("Could not open file $OdeEnv::bldcurr\n");
    print( BLDLOG "Executing: mk > $OdeTest::tmpfile $OdeEnv::stderr_redir; exit;\n" );
    close( BLDLOG );
  }
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  if ($status)
  {
    OdeInterface::printError( "mk Normal Test" );
    OdeInterface::logPrint( 
             "Test Failed: workon or mk returned a non-zero exit code\n" );
    $error = 1;
    return $error;
  }

}
1;
