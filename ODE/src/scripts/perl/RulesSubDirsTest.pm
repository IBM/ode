###########################################
#
# RulesSubDirsTest module
# - module to test specfication of
#   SUBDIRS in a makefile
#
###########################################

package RulesSubDirsTest;

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
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $bb_dir;
  my @delete_list = ($OdeTest::test_sb, $OdeTest::test_sb_rc, 
                     $OdeTest::tmpfile);

  OdeInterface::logPrint( "in RulesSubDirsTest\n");

  if (-d $OdeTest::test_sb)
  {
    OdeFile::deltree( $OdeTest::test_sb );
  }
  $bb_dir = $OdeEnv::tempdir . $OdeEnv::dirsep . "bbexample";
  ## creating the sandbox
  $command = "mksb -auto -back $bb_dir -dir $OdeTest::test_sbdir"
             . " -rc $OdeTest::test_sb_rc -m $OdeEnv::machine -auto "
             . $OdeTest::test_sb_name . " >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $error_txt = "mksb returned a non-zero exit code";
  }
  if (!$status)
  {
    $rules_error = OdeTest::validateSandbox( $OdeTest::test_sb, 
                                         $OdeTest::test_sb_rc, \$error_txt );
  }
  if ($status || $rules_error) 
  {
    # Test case failed
    OdeInterface::printError( 
          "rules SubDirs Test : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    OdeFile::deltree( \@delete_list );
    return 1;
  }

  $rules_error = test_subdirs();

  OdeFile::deltree( \@delete_list );
  return $rules_error;
}

##########################################
#
# sub test_subdirs
# - to test SUBDIRS specified in makefile
#
##########################################
sub test_subdirs()
{
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $tfile;
  my $filecount1;
  my $filecount2;
  my $filecount3;
  my $filecount4;
  my $filecount5;
  my $filecount6;
  my $filecount7;
  my $filecount8;
  my $obj_dir = join( $OdeEnv::dirsep,
                         ($OdeTest::test_sb, "obj", $OdeEnv::machine) );
  my $dir = cwd();
  my $tmpstr;

  $tmpstr = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "bin") );
  mkpath( $tmpstr );
  if (! -d $tmpstr)
  {
    OdeInterface::logPrint( "$tmpstr not created\n" );
  }
  ## creating a makefile in bin directory
  $tfile = $tmpstr . $OdeEnv::dirsep . "mf";
  open( INPUT, "> $tfile") ||
    warn( "Could not open $tfile\n" );
  print( INPUT "SUBDIRS=client\n" );
  print( INPUT "OBJECTS_SUBDIRS=logger\n" );
  print( INPUT "OBJECTS_SUBDIRS+=../bin/server\n" );
  print( INPUT ".include<\${RULES_MK}>\n" );
  close( INPUT );
  if (! -f $tfile)
  {
    OdeInterface::printError( "rules SubDirs Test" );
    OdeInterface::logPrint( "$tfile not created\n" );
  }
  OdePath::chgdir( $tmpstr );
  if (defined( $OdeEnv::MVS ))
  {
    $filecount1 = 15;
    $filecount2 = 3;
    $filecount3 = 15;
    $filecount4 = 5;
    $filecount5 = 5;
    $filecount6 = 13;
    $filecount7 = 13;
    $filecount8 = 6;
  }
  else
  {
    $filecount1 = 8;
    $filecount2 = 2;
    $filecount3 = 8;
    $filecount4 = 3;
    $filecount5 = 3;
    $filecount6 = 7;
    $filecount7 = 7;
    $filecount8 = 4;
  }

  ## running build -fmf MAKEFILE_PASS=STANDARD
  ## should try to build the client directory only as specified by
  ## SUBDIRS in makefile "mf", nothing should be built as it is upto date
  $command = "build -rc $OdeTest::test_sb_rc -f$tfile MAKEFILE_PASS=STANDARD "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "bin";
  if (!$rules_error && (! -d $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
  }
  if (!$rules_error && ((-d $obj_dir . $OdeEnv::dirsep . "lib") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "inc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "doc")))
  {
    $rules_error = 1;
    $error_txt = "a lib or inc or doc directory created in $obj_dir";
  }
  if (!$rules_error && ((-d $tmpstr . $OdeEnv::dirsep . "server") ||
                        (-d $tmpstr . $OdeEnv::dirsep . "logger")))
  {
    $rules_error = 1;
    $error_txt = "server or logger directory created in $tmpstr";
  }
  $tmpstr .= $OdeEnv::dirsep . "client";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or built when the files are upto date";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "SUBDIRS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                1, "SUBDIRS" );
  }

  ## running build -fmf MAKEFILE_PASS=STANDARD -a
  ## should build the client directory only as specified by SUBDIRS
  $command = "build -rc $OdeTest::test_sb_rc -f$tfile MAKEFILE_PASS=STANDARD "
             . "-a >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  if (!$rules_error && ((-d $obj_dir . $OdeEnv::dirsep . "lib") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "inc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "doc")))
  {
    $rules_error = 1;
    $error_txt = "a lib or inc or doc directory created in $obj_dir";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "bin";
  if (!$rules_error && ((-d $tmpstr . $OdeEnv::dirsep . "server") ||
                        (-d $tmpstr . $OdeEnv::dirsep . "logger")))
  {
    $rules_error = 1;
    $error_txt = "server or logger directory created in $tmpstr";
  }
  $tmpstr .= $OdeEnv::dirsep . "client";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount1)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                                2, "SUBDIRS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                2, "SUBDIRS" );
  }

  OdeFile::deltree( $obj_dir );
  if (-d $obj_dir)
  {
    OdeInterface::logPrint( "$obj_dir not deleted\n" );
  }
  $tfile = join( $OdeEnv::dirsep, ($OdeEnv::tempdir, "bbexample", 
                                    "src", "bin", "server", "makefile") );
  $tmpstr = join( $OdeEnv::dirsep, 
                  ($OdeTest::test_sb, "src", "bin", "server") );
  mkpath( $tmpstr );
  if (! -d $tmpstr)
  {
    OdeInterface::logPrint( "$tmpstr not created\n" );
  }
  ## copying makefile into sandbox
  else
  {
    OdeEnv::runSystemCommand( "$OdeEnv::copy $tfile $tmpstr" );
  }
  ## adding OBJECTS=server.obj to makefile in server directory
  $tmpstr .= $OdeEnv::dirsep . "makefile";
  open( INPUT, ">> $tmpstr" ) ||
     warn( "Could not open $tmpstr\n" );
  print( INPUT "\nOBJECTS=server\${OBJ_SUFF}" );
  close( INPUT );

  ## running build -fmf MAKEFILE_PASS=OBJECTS -a
  ## should try to generate obj files in server and logger directories
  ## the obj files should be generated in server directory only as 
  ## OBJECTS is specified in only its makefile
  $tfile = join( $OdeEnv::dirsep, 
                  ($OdeTest::test_sb, "src", "bin", "mf") );
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=OBJECTS "
             . "-a -f$tfile >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  if (!$rules_error && ((-d $obj_dir . $OdeEnv::dirsep . "lib") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "inc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "doc")))
  {
    $rules_error = 1;
    $error_txt = "a lib or inc or doc directory created in $obj_dir";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "bin";
  if (!$rules_error && ((! -d $tmpstr . $OdeEnv::dirsep . "server") ||
                        (! -d $tmpstr . $OdeEnv::dirsep . "logger")))
  {
    $rules_error = 1;
    $error_txt = "server or logger directory not created in $tmpstr";
  }
  if (!$rules_error && (-d $tmpstr . $OdeEnv::dirsep . "client"))
  {
    $rules_error = 1;
    $error_txt = "client directory created in $tmpstr";
  }
  if (!$rules_error && 
      ((OdeFile::numDirEntries
            ( $tmpstr . $OdeEnv::dirsep . "server" ) != $filecount2) ||
       (OdeFile::numDirEntries( $tmpstr . $OdeEnv::dirsep . "logger" ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "server or logger has incorrect number of files";
  }
  $tmpstr .= $OdeEnv::dirsep . "server";
  if (!$rules_error &&
       ((! -f $tmpstr . $OdeEnv::dirsep . "depend.mk") ||
        (! -f $tmpstr . $OdeEnv::dirsep . "server" . $OdeEnv::obj_suff))) 
  {
    $rules_error = 1;
    $error_txt = "depend.mk or server$OdeEnv::obj_suff not created in $tmpstr";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                                3, "OBJECTS_SUBDIRS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                3, "OBJECTS_SUBDIRS" );
  }

  OdeFile::deltree( $obj_dir );
  if (-d $obj_dir)
  {
    OdeInterface::logPrint( "$obj_dir not deleted\n" );
  }

  $tmpstr = $OdeTest::test_sb . $OdeEnv::dirsep . "src";
  ## creating a makefile in src directory
  $tfile = $tmpstr . $OdeEnv::dirsep . "mf";
  open( INPUT, "> $tfile") ||
    warn( "Could not open $tfile\n" );
  print( INPUT "EXPORT_EXPLIB_SUBDIRS=lib\n" );
  print( INPUT "BUILD_STANDARD_SUBDIRS=bin/client bin/logger\n" );
  print( INPUT ".include<\${RULES_MK}>" );
  close( INPUT );
  if (! -f $tfile)
  {
    OdeInterface::printError( "rules SubDirs Test" );
    OdeInterface::logPrint( "$tfile not created\n" );
  }
  OdePath::chgdir( $tmpstr );

  ## running build -fmf export_all
  $command = "build -rc $OdeTest::test_sb_rc export_all "
             . "-a -f$tfile >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  } 
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "lib";
  if (!$rules_error && ((! -d $tmpstr) ||
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount8)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  if (!$rules_error && ((-d $obj_dir . $OdeEnv::dirsep . "inc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "doc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "cmf") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "bin") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "COM") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "copyright")))
  {
    $rules_error = 1;
    $error_txt = "a directory other than lib created in $obj_dir";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                                4, "EXPORT_EXPLIB_SUBDIRS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                4, "EXPORT_EXPLIB_SUBDIRS" );
  }

  OdeFile::deltree( $obj_dir );
  if (-d $obj_dir)
  {
    OdeInterface::logPrint( "$obj_dir not deleted\n" );
  }

  ## running build -fmf build_all
  $command = "build -rc $OdeTest::test_sb_rc build_all "
             . "-a -f$tfile >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  } 
  if (!$rules_error && ((-d $obj_dir . $OdeEnv::dirsep . "lib") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "inc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "doc")))
  {
    $rules_error = 1;
    $error_txt = "a lib or inc or doc directory created in $obj_dir";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "bin";
  if (!$rules_error && ((! -d $tmpstr . $OdeEnv::dirsep . "client") ||
                        (! -d $tmpstr . $OdeEnv::dirsep . "logger")))
  {
    $rules_error = 1;
    $error_txt = "client or logger directory not created in $tmpstr";
  }
  if (!$rules_error && (-d $tmpstr . $OdeEnv::dirsep . "server"))
  {
    $rules_error = 1;
    $error_txt = "server directory created in $tmpstr";
  }
  if (!$rules_error && 
      ((OdeFile::numDirEntries( 
            $tmpstr . $OdeEnv::dirsep . "client") != $filecount3) ||
       (OdeFile::numDirEntries( 
            $tmpstr . $OdeEnv::dirsep . "logger") != $filecount4)))
  {
    $rules_error = 1;
    $error_txt = "incorrect number of files in client or logger of $tmpstr";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                                5, "BUILD_STANDARD_SUBDIRS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                5, "BUILD_STANDARD_SUBDIRS" );
  }

  OdeFile::deltree( $obj_dir );
  if (-d $obj_dir)
  {
    OdeInterface::logPrint( "$obj_dir not deleted\n" );
  }

  ## running build -fmf depend_all -a
  ## should not build anything as BUILD_STANDARD_SUBDIRS is specified
  ## in makefile instead of DEPEND_STANDARD_SUBDIRS
  $command = "build -rc $OdeTest::test_sb_rc depend_all "
             . "-a -f$tfile >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  } 
  if (!$rules_error && ((! -d $obj_dir) || 
                        (OdeFile::numDirEntries( $obj_dir ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$obj_dir not created or is not empty";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                                6, "BUILD_STANDARD_SUBDIRS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                6, "BUILD_STANDARD_SUBDIRS" );
  }

  OdeFile::deltree( $obj_dir );
  if (-d $obj_dir)
  {
    OdeInterface::logPrint( "$obj_dir not deleted\n" );
  }
  $tmpstr = $OdeTest::test_sb . $OdeEnv::dirsep . "src";
  ## creating a makefile in src directory
  ## to show the precedence of STANDARD_SUBDIRS over SUBDIRS
  $tfile = $tmpstr . $OdeEnv::dirsep . "mf";
  open( INPUT, "> $tfile") ||
    warn( "Could not open $tfile\n" );
  print( INPUT "SUBDIRS=bin/client\n" );
  print( INPUT "STANDARD_SUBDIRS=bin/logger\n" );
  print( INPUT ".include<\${RULES_MK}>" );
  close( INPUT );
  if (! -f $tfile)
  {
    OdeInterface::printError( "rules SubDirs Test" );
    OdeInterface::logPrint( "$tfile not created\n" );
  }

  ## running build -fmf build_all -a
  ## should build bin/logger directory as specified by STANDARD_SUBDIRS 
  ## instead of bin/client as specified by SUBDIRS
  $command = "build -rc $OdeTest::test_sb_rc build_all "
             . "-a -f$tfile >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  } 
  if (!$rules_error && ((-d $obj_dir . $OdeEnv::dirsep . "lib") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "inc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "doc")))
  {
    $rules_error = 1;
    $error_txt = "a lib or inc or doc directory created in $obj_dir";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "bin";
  if (!$rules_error && ((-d $tmpstr . $OdeEnv::dirsep . "client") ||
                        (-d $tmpstr . $OdeEnv::dirsep . "server")))
  {
    $rules_error = 1;
    $error_txt = "client or server directory created in $tmpstr";
  }
  $tmpstr .= $OdeEnv::dirsep . "logger";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount5)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                                7, "SUBDIRS precedence" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                7, "SUBDIRS precedence" );
  }

  OdeFile::deltree( $obj_dir );
  if (-d $obj_dir)
  {
    OdeInterface::logPrint( "$obj_dir not deleted\n" );
  }
  $tmpstr = $OdeTest::test_sb . $OdeEnv::dirsep . "src";
  ## creating a makefile in src directory
  ## to show the precedence of BUILD_STANDARD_SUBDIRS over STANDARD_SUBDIRS
  ## and SUBDIRS
  $tfile = $tmpstr . $OdeEnv::dirsep . "mf";
  open( INPUT, "> $tfile") ||
    warn( "Could not open $tfile\n" );
  print( INPUT "SUBDIRS=bin/client\n" );
  print( INPUT "STANDARD_SUBDIRS=bin/logger\n" );
  print( INPUT "BUILD_STANDARD_SUBDIRS=bin/server\n" );
  print( INPUT ".include<\${RULES_MK}>" );
  close( INPUT );
  if (! -f $tfile)
  {
    OdeInterface::printError( "rules SubDirs Test" );
    OdeInterface::logPrint( "$tfile not created\n" );
  }

  ## running build -fmf build_all -a
  ## should build bin/server directory as specified by BUILD_STANDARD_SUBDIRS 
  $command = "build -rc $OdeTest::test_sb_rc build_all "
             . "-a -f$tfile >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  } 
  if (!$rules_error && ((-d $obj_dir . $OdeEnv::dirsep . "lib") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "inc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "doc")))
  {
    $rules_error = 1;
    $error_txt = "a lib or inc or doc directory created in $obj_dir";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "bin";
  if (!$rules_error && ((-d $tmpstr . $OdeEnv::dirsep . "client") ||
                        (-d $tmpstr . $OdeEnv::dirsep . "logger")))
  {
    $rules_error = 1;
    $error_txt = "client or logger directory created in $tmpstr";
  }
  $tmpstr .= $OdeEnv::dirsep . "server";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount6)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                                8, "SUBDIRS precedence" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                8, "SUBDIRS precedence" );
  }

  OdeFile::deltree( $obj_dir );
  if (-d $obj_dir)
  {
    OdeInterface::logPrint( "$obj_dir not deleted\n" );
  }

  ## running build -fmf MAKEFILE_PASS=STANDARD -a
  ## should build bin/server directory as specified by BUILD_STANDARD_SUBDIRS 
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=STANDARD "
             . "-a -f$tfile >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  } 
  if (!$rules_error && ((-d $obj_dir . $OdeEnv::dirsep . "lib") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "inc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "doc")))
  {
    $rules_error = 1;
    $error_txt = "a lib or inc or doc directory created in $obj_dir";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "bin";
  if (!$rules_error && ((-d $tmpstr . $OdeEnv::dirsep . "client") ||
                        (-d $tmpstr . $OdeEnv::dirsep . "logger")))
  {
    $rules_error = 1;
    $error_txt = "client or logger directory created in $tmpstr";
  }
  $tmpstr .= $OdeEnv::dirsep . "server";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount7)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                                9, "SUBDIRS precedence" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                9, "SUBDIRS precedence" );
  }

  OdeFile::deltree( $obj_dir );
  if (-d $obj_dir)
  {
    OdeInterface::logPrint( "$obj_dir not deleted\n" );
  }
  $tmpstr = $OdeTest::test_sb . $OdeEnv::dirsep . "src";
  ## creating a makefile in src directory with an invalid subdir
  $tfile = $tmpstr . $OdeEnv::dirsep . "mf";
  open( INPUT, "> $tfile") ||
    warn( "Could not open $tfile\n" );
  print( INPUT "SUBDIRS=bin/client crap\n" );
  print( INPUT ".include<\${RULES_MK}>" );
  close( INPUT );
  if (! -f $tfile)
  {
    OdeInterface::printError( "rules SubDirs Test" );
    OdeInterface::logPrint( "$tfile not created\n" );
  }

  ## running build -fmf MAKEFILE_PASS=STANDARD -a
  ## should build bin/client directory and give an error
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=STANDARD "
             . "-a -f$tfile >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if (!$status)
  {
    $rules_error = 1;
    $error_txt = "build returned a zero exit code";
  }
  if (!$rules_error && ((! -d $obj_dir . $OdeEnv::dirsep . "bin") ||
                        (! -d $obj_dir . $OdeEnv::dirsep . "crap")))
  {
    $rules_error = 1;
    $error_txt = "bin or crap directory not created in $obj_dir";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "client") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount1)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "crap";
  if (!$rules_error && (OdeFile::numDirEntries( $tmpstr ) != 0))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr is not empty";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                                10, "SUBDIRS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                10, "SUBDIRS" );
  }

  OdePath::chgdir( $dir );
  return $error;
}

  
1;
