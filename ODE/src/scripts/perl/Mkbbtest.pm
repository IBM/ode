#########################################
#
# Mkbbtest module
# - module to run mkbbtest for different
#   test levels
#
#########################################

package Mkbbtest;

use File::Path;
use File::Copy;
use File::Compare;
use OdeUtil;
use OdeEnv;
use OdeFile;
use OdeTest;
use OdeInterface;
use strict;

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
  my @delete_list = (join( $OdeEnv::dirsep, 
                      ($OdeTest::test_sbdir, $OdeTest::test_sb_name) ),
                     $OdeTest::test_sb_rc); 

  print( "in Mkbbtest - test level is normal\n");
  OdeEnv::runOdeCommand( "mkbb -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");
  
  #Remove any existing temporary files
  OdeFile::deltree( \@delete_list);

  $command = "mkbb -dir $OdeTest::test_sbdir" 
             . " -rc $OdeTest::test_sb_rc -m $OdeEnv::machine "
             . $OdeTest::test_sb_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  # Create sandbox - test #1
  $status = OdeEnv::runOdeCommand( $command );
  if ( ($status == 0) && (-d $OdeTest::test_sb) && (-f $OdeTest::test_sb_conf))
  {
    # Test #1 passed
    OdeInterface::printResult( "pass", "normal", "mkbb", 1, "mkbb -dir -rc -m");
    # Remove sandbox - test #2
    $command = "mkbb -rc $OdeTest::test_sb_rc -auto -undo "
               . $OdeTest::test_sb_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
    $status = OdeEnv::runOdeCommand( $command );
    if ( ($status == 0) && (! -d $OdeTest::test_sb) )
    {      
      # Test #2 passed
      OdeInterface::printResult( "pass", "normal", "mkbb", 2, 
                                 "mkbb -rc -undo -auto" );
    } 
    else
    {      
      # Test #2 failed
      OdeInterface::printResult( "fail", "normal", "mkbb", 2, 
                                 "mkbb -rc -undo -auto" );
      $error = 1;
    } 
  }
  else
  {
    # Test #1 failed
    OdeInterface::printResult( "fail", "normal", "mkbb", 1, "mkbb -dir -rc -m");
    $error = 1;
  } 

  OdeFile::deltree( \@delete_list );

  return( $error );
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
  my $bbuild1 = "test_bbuild_1";
  my $bbuild2 = "test_bbuild_2";
  my $machine1;
  my $machine2;
  my $build_list = join( $OdeEnv::dirsep, ($OdeTest::test_sbdir, "build_list"));
  my $error = 0;
  my $reg_error = 0;
  my $error_txt;
  my $tmpstr;
  my @delete_list =  (join( $OdeEnv::dirsep, ($OdeTest::test_sbdir, $bbuild1) ),
                      join( $OdeEnv::dirsep, ($OdeTest::test_sbdir, $bbuild2) ),
                      $OdeTest::tmpfile, $OdeTest::test_sb_rc, $build_list);

  OdeInterface::logPrint( "in Mkbbtest - test level is regression\n");
  OdeEnv::runOdeCommand( "mkbb -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");

  #Remove any existing temporary files
  OdeFile::deltree( \@delete_list);
  
  $command = "mkbb -dir $OdeTest::test_sbdir -blist $build_list -auto" 
             . " -rc $OdeTest::test_sb_rc -m $OdeEnv::machine "
             . "$bbuild1 >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mkbb -dir -blist -auto -rc -m ";
  $machine1 = join( $OdeEnv::dirsep, 
                     ($OdeTest::test_sbdir, $bbuild1, "obj", 
                     $OdeEnv::machine) );

  ## Regression Test Case 1 - Create a backing build
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if (!($status == 0)) 
  {
    $reg_error = 1;
    $error_txt = "mkbb returned a non-zero exit code";
  }

  if (!$reg_error)
  {
    $reg_error = OdeTest::validateSandbox( join( $OdeEnv::dirsep, 
                     ($OdeTest::test_sbdir, $bbuild1) ), 
                      $OdeTest::test_sb_rc, \$error_txt );
  }

  if (!$reg_error && !(-f $build_list))
  {
    $reg_error = 1;
    $error_txt = "Build list $build_list not created for $bbuild1";
  }

  if (!$reg_error && !(OdeFile::findInFile( $bbuild1, $build_list )))
  {
    $reg_error = 1;
    $error_txt = "$bbuild1 not found in $build_list";
  }

  if (!$reg_error && !(OdeFile::findInFile( "default $bbuild1", 
                                            $OdeTest::test_sb_rc )))
  {
    $reg_error = 1;
    $error_txt = "$bbuild1 not found in $OdeTest::test_sb_rc";
  }

  if ( !$reg_error && !(-d $machine1))
  {
    $reg_error = 1;
    $error_txt = "directory $machine1 does not exist";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mkbb", 1, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list);
    return $error;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mkbb", 1, $tmpstr );
  }

  ## Regression Test Case 2 - Create another backing build with
  ## the same buildlist and .sbrc as the first one
  $command = "mkbb -dir $OdeTest::test_sbdir -blist $build_list -auto" 
             . " -def -rc $OdeTest::test_sb_rc -m $OdeEnv::machine "
             . "$bbuild2 >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mkbb -dir -blist -auto -def -rc -m";
  $machine1 = join( $OdeEnv::dirsep, 
                     ($OdeTest::test_sbdir, $bbuild2, "obj", 
                     $OdeEnv::machine) );

  $status = OdeEnv::runOdeCommand( $command );

  #check command exit code
  if (!($status == 0))
  {      
    $reg_error = 1;
    $error_txt = "mkbb returned a non-zero exit code" ;
  } 

  if (!$reg_error)
  {
    $reg_error = OdeTest::validateSandbox( join( $OdeEnv::dirsep, 
                     ($OdeTest::test_sbdir, $bbuild1) ), 
                      $OdeTest::test_sb_rc, \$error_txt );
  }


  if (!$reg_error && !(OdeFile::findInFile( $bbuild1, $OdeTest::test_sb_rc )))
  {
    $reg_error = 1;
    $error_txt = "$bbuild1 not found in $OdeTest::test_sb_rc";
  }

  if (!$reg_error && !(OdeFile::findInFile( $bbuild2, $OdeTest::test_sb_rc )))
  {
    $reg_error = 1;
    $error_txt = "$bbuild2 not found in $OdeTest::test_sb_rc";
  }

  if (!$reg_error && !(OdeFile::findInFile( "default $bbuild2", 
                                            $OdeTest::test_sb_rc )))
  {
    $reg_error = 1;
    $error_txt = "$bbuild2 not set to default in $OdeTest::test_sb_rc";
  }

  if (!$reg_error && !(OdeFile::findInFile( $bbuild1, $build_list )))
  {
    $reg_error = 1;
    $error_txt = "$bbuild1 not found in $build_list";
  }

  if (!$reg_error) 
  {
    if (OdeFile::findInFile( $bbuild2, $build_list ) == 0)
    {
      $reg_error = 1;
      $error_txt = "$bbuild2 not found in $build_list";
    }
  }

  if (!$reg_error) 
  {
    if (OdeFile::findInFile( $bbuild2, $build_list ) != 1)
    {
      $reg_error = 1;
      $error_txt = "$bbuild2 not the first entry in $build_list";
    }
  }

  if ( !$reg_error && !(-d $machine1))
  {
    $reg_error = 1;
    $error_txt = "directory $machine1 does not exist";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mkbb", 2, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list);
    return $error;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mkbb", 2, $tmpstr );
  }

  ## Regression Test Case 3 - List all the backing builds
  $command = "mkbb -list -rc $OdeTest::test_sb_rc "
             . "> $OdeTest::tmpfile";
  $tmpstr = "mkbb -list -rc";
  $status = OdeEnv::runOdeCommand( $command );

  #concatenating tmpfile to the logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );

  #check command exit code
  if (!($status == 0))
  {      
    $reg_error = 1;
    $error_txt = "mkbb -list -rc returned a non-zero exit code" ;
  } 

  if (!$reg_error && !(OdeFile::findInFile( $bbuild1, $OdeTest::tmpfile )))
  {
    $reg_error = 1;
    $error_txt = "$bbuild1 not listed" ;
  }
    
  if (!$reg_error && !(OdeFile::findInFile( $bbuild2, $OdeTest::tmpfile )))
  {
    $reg_error = 1;
    $error_txt = "$bbuild2 not listed" ;
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mkbb", 3, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list);
    return $error;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mkbb", 3, $tmpstr );
  }


  ## Regression Test Case 4 - Make first backing build be default and
  ## add another machine for it.
  $command = "mkbb -auto" 
             . " -def -rc $OdeTest::test_sb_rc -m $OdeEnv::machine" . "_other "
             . "$bbuild1 >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mkbb -auto -def -rc -m for existing backing build";
  $machine1 = join( $OdeEnv::dirsep, 
                     ($OdeTest::test_sbdir, $bbuild1, "obj", 
                     $OdeEnv::machine) );
  $machine2 = join( $OdeEnv::dirsep, 
                     ($OdeTest::test_sbdir, $bbuild1, "obj", 
                     $OdeEnv::machine . "_other") );

  $status = OdeEnv::runOdeCommand( $command );

  #check command exit code
  if (!($status == 0))
  {      
    $reg_error = 1;
    $error_txt = "mkbb returned a non-zero exit code" ;
  } 

  if (!$reg_error)
  {
    $reg_error = OdeTest::validateSandbox( join( $OdeEnv::dirsep, 
                     ($OdeTest::test_sbdir, $bbuild1) ), 
                      $OdeTest::test_sb_rc, \$error_txt );
  }

  if (!$reg_error && !(OdeFile::findInFile( $bbuild1, $OdeTest::test_sb_rc )))
  {
    $reg_error = 1;
    $error_txt = "$bbuild1 not found in $OdeTest::test_sb_rc";
  }

  if (!$reg_error && !(OdeFile::findInFile( $bbuild2, $OdeTest::test_sb_rc )))
  {
    $reg_error = 1;
    $error_txt = "$bbuild2 not found in $OdeTest::test_sb_rc";
  }

  if (!$reg_error && !(OdeFile::findInFile( "default $bbuild1", 
                                            $OdeTest::test_sb_rc )))
  {
    $reg_error = 1;
    $error_txt = "$bbuild1 not set to default in $OdeTest::test_sb_rc";
  }

  if (!$reg_error && !(OdeFile::findInFile( $bbuild1, $build_list )))
  {
    $reg_error = 1;
    $error_txt = "$bbuild1 not found in $build_list";
  }

  if (!$reg_error) 
  {
    if (OdeFile::findInFile( $bbuild2, $build_list ) == 0)
    {
      $reg_error = 1;
      $error_txt = "$bbuild2 not found in $build_list";
    }
  }

  if (!$reg_error) 
  {
    if (OdeFile::findInFile( $bbuild2, $build_list ) != 1)
    {
      $reg_error = 1;
      $error_txt = "$bbuild2 not the first entry in $build_list";
    }
  }

  if ( !$reg_error && !(-d $machine1))
  {
    $reg_error = 1;
    $error_txt = "directory $machine1 does not exist";
  }

  if ( !$reg_error && !(-d $machine2))
  {
    $reg_error = 1;
    $error_txt = "directory $machine2 does not exist";
  }

  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mkbb", 4, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list);
    return $error;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mkbb", 4, $tmpstr );
  }



  ## Regression Test Case 5 - Remove the first backing build previously created
  $command = "mkbb -undo -auto -rc $OdeTest::test_sb_rc "
             . "$bbuild1 >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mkbb -undo -rc -auto";

  $status = OdeEnv::runOdeCommand( $command );

  #check command exit code
  if (!($status == 0))
  {      
    $reg_error = 1;
    $error_txt = "mkbb -undo returned a non-zero exit code" ;
  } 

  if (!$reg_error && (-d join( $OdeEnv::dirsep, 
                                  ($OdeTest::test_sbdir, $bbuild1) )))
  {
    $reg_error = 1;
    $error_txt = "$bbuild1 not deleted" ;
  }

  if (!$reg_error && OdeFile::findInFile( $bbuild1, $build_list ))
  {
    $reg_error = 1;
    $error_txt = "$bbuild1 not deleted from $build_list" ;
  }

  if (!$reg_error && OdeFile::findInFile( $bbuild1, $OdeTest::test_sb_rc ))
  {
    $reg_error = 1;
    $error_txt = "$bbuild1 not deleted from $OdeTest::test_sb_rc" ;
  }
    
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mkbb", 5, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list);
    return $error;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mkbb", 5, $tmpstr );
  }

  ## Regression Test Case 6 - Remove the second backing build previously created
  $command = "mkbb -undo -auto -rc $OdeTest::test_sb_rc "
             . "$bbuild2 >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  #check command exit code
  if (!($status == 0))
  {      
    $reg_error = 1;
    $error_txt = "mkbb -undo returned a non-zero exit code" ;
  } 

  if (!$reg_error && (-d join( $OdeEnv::dirsep, 
                                  ($OdeTest::test_sbdir, $bbuild2) )))
  {
    $reg_error = 1;
    $error_txt = "$bbuild2 not deleted" ;
  }

  if (!$reg_error && OdeFile::findInFile( $bbuild2, $build_list ))
  {
    $reg_error = 1;
    $error_txt = "$bbuild2 not deleted from $build_list" ;
  }

  if (!$reg_error && (-f $OdeTest::test_sb_rc ))
  {
    $reg_error = 1;
    $error_txt = "$OdeTest::test_sb_rc not deleted after deleting $bbuild2" ;
  }
  if ($reg_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "regression", "mkbb", 6, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list);
    return $error;
  }
  else
  {
    #Test case passed
    OdeInterface::printResult( "pass", "regression", "mkbb", 6, $tmpstr );
  }

  ## Regression Test Case 7 - test for non-zero exit code
  ## force mkbb to fail and check if a non-zero exit code was supplied
  $reg_error = 0;
  $command = "mkbb -badflag . >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $tmpstr = "mkbb <invalid flag>";
  $status = OdeEnv::runOdeCommand( $command );
  # Check command exit code
  if ($status == 0)
  {
    $reg_error = 1;
    $error_txt = "mkbb returned a zero exit code after an error";
    $error = 1;
    OdeInterface::printResult( "fail", "regression", "mkbb", 7, $tmpstr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    # Test case passed
    OdeInterface::printResult( "pass", "regression", "mkbb", 7, $tmpstr );
  }

  OdeFile::deltree( \@delete_list);
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
  my $command;
  my $status1;
  my $status2;
  my $status3;
  my $error = 0;
  my $fvt_error = 0;
  my $error_txt;
  my $bbuild1 = "test_bbuild_1";
  my $bbuild2 = "test_bbuild_2";
  my $bbuild3 = "test_bbuild_3";
  my $bbuild1_list = join( $OdeEnv::dirsep, ($OdeTest::test_sbdir, $bbuild1,  
                                          "bbuild1_list") );
  my $bbuild2_list = join( $OdeEnv::dirsep, ($OdeTest::test_sbdir, $bbuild2,
                                          "bbuild2_list") );
  my $bbuild3_list = join( $OdeEnv::dirsep, ($OdeTest::test_sbdir, "bbdir",
                                           $bbuild3, "bbuild3_list") );
  my $test_sbdir1 = $OdeTest::test_sbdir;  
  my $test_sbdir2 = $OdeTest::test_sbdir; 
  my $test_sbdir3 = $OdeTest::test_sbdir . $OdeEnv::dirsep . "bbdir"; 
  my @delete_list = (join( $OdeEnv::dirsep, ($OdeTest::test_sbdir, $bbuild1) ),
                    join( $OdeEnv::dirsep, ($OdeTest::test_sbdir, $bbuild2) ), 
                    $test_sbdir3);
  my $bbuild1_rc = join( $OdeEnv::dirsep, 
                        ($OdeTest::test_sbdir, $bbuild1, $OdeTest::test_rc) );
  my $bbuild2_rc = join( $OdeEnv::dirsep, 
                        ($OdeTest::test_sbdir, $bbuild2, $OdeTest::test_rc) );
  my $bbuild3_rc = join( $OdeEnv::dirsep, 
                        ($OdeTest::test_sbdir, "bbdir", $bbuild3, 
                         $OdeTest::test_rc) );

  OdeFile::deltree( \@delete_list);
  mkpath( $test_sbdir3 );
  OdeInterface::logPrint( "in Mkbbtest - test level is FVT\n");
  OdeEnv::runOdeCommand( "mkbb -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");  

  ## FVT Test Case - OD50 - make three builds
  $command = "mkbb -auto -dir $test_sbdir1 -blist $bbuild1_list"
             . " -rc $bbuild1_rc -m $OdeEnv::machine $bbuild1"
             . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";

  $status1 = OdeEnv::runOdeCommand( $command );

  $command = "mkbb -auto -dir $test_sbdir2 -blist $bbuild1_list"
             . " -rc $bbuild2_rc -m $OdeEnv::machine $bbuild2"
             . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";

  $status2 = OdeEnv::runOdeCommand( $command );

  $command = "mkbb -auto -dir $test_sbdir3 -blist $bbuild3_list -sc -nobld"
             . " -rc $bbuild3_rc -m $OdeEnv::machine $bbuild3 "
             . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";

  $status3 = OdeEnv::runOdeCommand( $command );

  if (!($status1 == 0 && $status2 == 0 && $status3 == 0))
  {
    $fvt_error = 1;
    $error_txt = "mkbb returned a non zero exit code";
  }
  if (!$fvt_error)
  {
    $fvt_error = OdeTest::validateSandbox( join( $OdeEnv::dirsep, 
                     ($test_sbdir1, $bbuild1) ), $bbuild1_rc, \$error_txt );
  }
  if (!$fvt_error)
  {
    $fvt_error = OdeTest::validateSandbox( join( $OdeEnv::dirsep, 
                     ($test_sbdir2, $bbuild2) ), $bbuild2_rc, \$error_txt );
  }
  if (!$fvt_error)
  {
    $fvt_error = OdeTest::validateSandbox( join( $OdeEnv::dirsep, 
                     ($test_sbdir3, $bbuild3) ), $bbuild3_rc, \$error_txt );
  }

  if (!$fvt_error && !(-f $bbuild1_list))
  {
    $fvt_error = 1;
    $error_txt = "Build list not created in $bbuild1";
  }

  if (!$fvt_error && !(-f $bbuild3_list))
  {
    $fvt_error = 1;
    $error_txt = "Build list not created in $bbuild3";
  }

  if (!$fvt_error && !(OdeFile::findInFile( $bbuild1, $bbuild1_list )))
  {
    $fvt_error = 1;
    $error_txt = "$bbuild1 not found in $bbuild1_list";
  }

  if (!$fvt_error && !(OdeFile::findInFile( $bbuild2, $bbuild1_list )))
  {
    $fvt_error = 1;
    $error_txt = "$bbuild2 not found in $bbuild1_list";
  }

  if (!$fvt_error && !(OdeFile::findInFile( $bbuild3, $bbuild3_list )))
  {
    $fvt_error = 1;
    $error_txt = "$bbuild3 not found in $bbuild3_list";
  }

  if (!$fvt_error)  
  {
    my $tmpvar1 = join( $OdeEnv::dirsep, ($OdeTest::test_sbdir, $bbuild1, 
                      "rc_files", "sb.conf") ); 
    my $tmpvar2 = join( $OdeEnv::dirsep, ($OdeTest::test_sbdir, $bbuild2, 
                      "rc_files", "sb.conf") ); 
    if (!(compare( $tmpvar1, $tmpvar2 ) == 0)) 
    {
      $fvt_error = 1;
      $error_txt = "$tmpvar1 and $tmpvar2 does not match"; 
    }
  }

  if (!$fvt_error)  
  {
    my $tmpvar1 = join( $OdeEnv::dirsep, ($OdeTest::test_sbdir, $bbuild1, 
                      "rc_files", "sb.conf") ); 
    my $tmpvar3 = join( $OdeEnv::dirsep, ($test_sbdir3, $bbuild3, 
                      "rc_files", "sb.conf") ); 
    if (!(compare( $tmpvar1, $tmpvar3 ) != 0))
    {
      $fvt_error = 1;
      $error_txt = "$tmpvar1 and $tmpvar3 does not match"; 
    }
  }

  if ($fvt_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "fvt", "mkbb", "OD50" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $fvt_error = 0; #Reset for the next Test case
  }
  else
  {
    #Test Case Passed
    OdeInterface::printResult( "pass", "fvt", "mkbb", "OD50" );
  }

  ## FVT Test Case - OD51 - Test the upgrade features
  my $bbuild1_sbconf = join( $OdeEnv::dirsep, ($OdeTest::test_sbdir, $bbuild1, 
                               "rc_files", "sb.conf") ); 
  OdeFile::removeLinesFromFile( $bbuild1_sbconf, "machine_list", 
                                $OdeEnv::machine );
  $command = "mkbb -auto -upgrade $bbuild1 -rc $bbuild1_rc"
             . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status1 = OdeEnv::runOdeCommand( $command );
  if (!($status1 == 0))
  {
    $fvt_error = 1;
    $error_txt = "mkbb -upgrade returned non zero exit code";
  }

  if (!$fvt_error && !(OdeFile::findInFile( "machine_list", $bbuild1_sbconf))) 
  {
    $fvt_error = 1;
    $error_txt = "machine_list could not be created in the $bbuild1_sbconf"; 
    $error_txt = $error_txt . " after being removed";
  }

  if (!$fvt_error && !(OdeFile::findInFile( $OdeEnv::machine, $bbuild1_sbconf)))
  {
    $fvt_error = 1;
    $error_txt = "machine_name could not be created in the $bbuild1_sbconf";
    $error_txt = $error_txt . " after being removed";
  }

  if (!$fvt_error) 
  {
    if (!(unlink( $bbuild1_sbconf )) == 1) 
    {
      $fvt_error = 1;
      $error_txt = "sb.conf could not be deleted from the backing build";
    }
  }
  
  $command = "mkbb -auto -upgrade $bbuild1 -rc $bbuild1_rc" 
             . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status1 = OdeEnv::runOdeCommand( $command );
  if (!$status1 == 0)
  {
    $fvt_error = 1;
    $error_txt = "mkbb -upgrade returned a non zero exit code";
  }

  if (!$fvt_error && !(-f $bbuild1_sbconf))  
  {
    $fvt_error = 1;
    $error_txt = "sb.conf does not exist in the backing build ";
  }
  if (!$fvt_error && !(OdeFile::findInFile( "ode_sc true", $bbuild1_sbconf ))) 
  {
    $fvt_error = 1;
    $error_txt = "ode_sc true could not be found in $bbuild1_sbconf";
  }

  if (!$fvt_error && 
            !(OdeFile::findInFile( "ode_build_env true", $bbuild1_sbconf ))) 
  {
    $fvt_error = 1;
    $error_txt = "ode_build_env true could not be found in $bbuild1_sbconf";
  }

  if ($fvt_error)
  {
    #Test case failed
    OdeInterface::printResult( "fail", "fvt", "mkbb", "OD51" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $fvt_error = 0; #Reset for the next Test case
  }
  else
  {
    #Test Case Passed
    OdeInterface::printResult( "pass", "fvt", "mkbb", "OD51" );
  }
  OdeFile::deltree( \@delete_list);
  return $error;      
  # FVT Test Cases OD53 and OD54 are to be implemented
}
             
1;
