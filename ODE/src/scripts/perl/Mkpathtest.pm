#########################################
#
# Mkpathtest module
# - module to run mkpathtest for different
#   test levels
#
#########################################

package Mkpathtest;
use strict;
use OdeUtil;
use OdeEnv;
use OdeTest;
use OdePath;
use OdeInterface;
use Cwd;

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
    print("ERROR: undefined testlevel $OdeUtil::arghash{'testlevel'}\n");
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
  my $testdir = join( $OdeEnv::dirsep, 
                   ($OdeTest::test_sbdir, $OdeTest::test_sb_name, "testfile") );
  my @delete_list = ($OdeTest::test_sb);
  OdeFile::deltree( \@delete_list );

  OdeInterface::logPrint( "in Mkpathtest - test level is normal\n");
  OdeEnv::runOdeCommand( "mkpath -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");
  
  $command = "mkpath $testdir >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $status = OdeEnv::runOdeCommand( $command );

  if (($status == 0) && (-d $OdeTest::test_sb))
  {
    #Test 1 Passed
    OdeInterface::printResult( "pass", "normal", "mkpath", 1, "mkpath" );
  }
  else
  {
    #Test 1 Failed
    OdeInterface::printResult( "fail", "normal", "mkpath", 1, "mkpath" );
    $error = 1;
  }
  OdeFile::deltree( \@delete_list );
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
  my $dir = cwd();
  my $testpath1 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "testfile1") );
  my $testpath2 = $testpath1 . $OdeEnv::dirsep;
  my $testpath3;
  my $error = 0;
  my $reg_error = 0;
  my $error_txt;
  my $tmpstr;
  my @delete_list = ($OdeTest::test_sb, $OdeTest::test_sb1);

  OdeInterface::logPrint( "in Mkpathtest - test level is regression\n");
  OdeEnv::runOdeCommand( "mkpath -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

  #Remove any existing temporary files
  OdeFile::deltree( \@delete_list );
  
  ## Regression Test Case 1 - Make a path 
  $command = "mkpath $testpath1 >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $tmpstr = "mkpath ./test_sb/testfile1";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if (!($status == 0)) 
  {
    $reg_error = 1;
    $error_txt = "mkpath returned a non-zero exit code";
  }

  if (!$reg_error && !(-d $OdeTest::test_sb))
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::test_sb not created";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mkpath", 1, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mkpath", 1, $tmpstr );
    OdeFile::deltree( \@delete_list );
  }
    
  ## Regression Test Case 2 - Make another path with a trailing slash
  $command = "mkpath $testpath2 >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $tmpstr = "mkpath /tmp/test_sb/testfile1/";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if (!($status == 0)) 
  {
    $reg_error = 1;
    $error_txt = "mkpath returned a non-zero exit code";
  }

  if (!$reg_error && !(-d $testpath1 ))
  {
    $reg_error = 1;
    $error_txt = "$testpath2 not created";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mkpath", 2, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mkpath", 2, $tmpstr );
    OdeFile::deltree( $testpath1 );
  }

  ## Regression Test Case 3 - Make a partial existing path 
  if ((-d $testpath1) || !(-d $OdeTest::test_sb)) 
  {
    OdeInterface::logPrint( "$testpath1 not deleted from Test # 2\n" );
  }
  $command = "mkpath $testpath2 >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $tmpstr = "mkpath /tmp/test_sb/testfile1/";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if (!($status == 0)) 
  {
    $reg_error = 1;
    $error_txt = "mkpath returned a non-zero exit code";
  }

  if (!$reg_error &&  !(-d $testpath1))
  {
    $reg_error = 1;
    $error_txt = "$testpath2 not created";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mkpath", 3, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mkpath", 3, $tmpstr );
  }

  ## Regression Test Case 4 - Make a relative path
  OdePath::chgdir( $testpath1 )
    || OdeInterface::logPrint( "Can't cd to $testpath1\n");
  $testpath3 = join( $OdeEnv::dirsep, 
                     ("..", "..", $OdeTest::test_sb1_name, "testfile1") );

  $command = "mkpath $testpath3 >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $tmpstr = "mkpath ../../test_sb1/testfile1";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if (!($status == 0)) 
  {
    $reg_error = 1;
    $error_txt = "mkpath returned a non-zero exit code";
  }

  if (!$reg_error &&  
      !(-d join( $OdeEnv::dirsep, ("..", "..", $OdeTest::test_sb1_name) )))
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::test_sb1 not created";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mkpath", 4, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;

    OdePath::chgdir( $dir );
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mkpath", 4, $tmpstr );
    OdePath::chgdir( $dir );
    OdeFile::deltree( \@delete_list );
  }

  ## Regression Test Case 5 - Make a path with incorrect slashes
  $testpath3 = $testpath2 . "\\testfile2//\\\\testfile3\\//"; 
  $command = "mkpath $testpath3 >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
  $tmpstr = "mkpath /tmp/test_sb1/testfile1\\testfile2//\\\\testfile3\\//";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if (!($status == 0)) 
  {
    $reg_error = 1;
    $error_txt = "mkpath returned a non-zero exit code";
  }

  my $testfile2 = join( $OdeEnv::dirsep, ($testpath2, "testfile2") );

  if (!$reg_error && 
      !(-d join( $OdeEnv::dirsep, ($testpath2, "testfile2", "testfile3") )))
  {
    $reg_error = 1;
    $error_txt = "$testpath3 not created";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mkpath", 5, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mkpath", 5, $tmpstr );
    OdeFile::deltree( \@delete_list );
  }

  ## Regression Test Case 6 - On UNIX make a path where there is no permission
  ## to write, On Non-UNIX, make a path on an invalid drive
  ## This test not to be run by a user with permissions to write into /usr/bin
  if (defined( $OdeEnv::UNIX ))
  {
    my $path = "/usr/bin";
    if ((-d $path) && !(-w $path))
    {
      my $testpath = $path . "/testdir/";
      $command = "mkpath $testpath >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
      $tmpstr = "mkpath /usr/bin/testdir/";
      $status = OdeEnv::runOdeCommand( $command );
      if ($status == 0)
      {
        #Test case failed
        $error_txt = "mkpath returned a zero exit code when trying to create a";
        $error_txt = $error_txt . " path with no write permissions";
        OdeInterface::printResult( "fail", "regression", "mkpath", 6, $tmpstr );
        OdeInterface::logPrint( "Test Failed: $error_txt\n" );
        $error = 1;
        OdeFile::deltree( \@delete_list );
        return $error;
      }
      else
      {
        #Test case passed
        OdeInterface::printResult( "pass", "regression", "mkpath", 6, $tmpstr );
        OdeFile::deltree( \@delete_list );
      }
    }
  }
  else
  {
    my $path = "4\:\\dir";
    my $testpath = $path . $OdeEnv::dirsep . "dir1" . $OdeEnv::dirsep;
    $command = "mkpath $testpath >> $OdeEnv::bldcurr $OdeEnv::stderr_redir"; 
    $tmpstr = "mkpath 4\:\\dir\\dir1\\";
    $status = OdeEnv::runOdeCommand( $command );
    if ($status == 0)
    {
      #Test case failed
      $error_txt = "mkpath returned a zero exit code when trying to create a";
      $error_txt = $error_txt . " directory on invalid drive";
      OdeInterface::printResult( "fail", "regression", "mkpath", 6, $tmpstr );
      OdeInterface::logPrint( "Test Failed: $error_txt\n" );
      $error = 1;
      OdeFile::deltree( \@delete_list );
      return $error;
    }
    else
    {
      #Test case passed
      OdeInterface::printResult( "pass", "regression", "mkpath", 6, $tmpstr );
      OdeFile::deltree( \@delete_list );
    }
  }
        
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
  print("fvt not implemented yet!\n");
}
             

1;
