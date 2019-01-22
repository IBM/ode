##################################################
#
# RulesNewActionsTest module
# - module to test creation of new passes
#   targets and actions
# New Action added: NEWACTION
# Passes defined for NEWACTION: 
#       NEWPPPASS NEWPROGPASS EXPLIB
#       NEWPPPASS: makes .pp files
#       NEWPROGPASS: makes executables 
#       EXPLIB: removes programs and libraries
# New Target added: 
#       LIBRARIES is added to _BUILD_STANDARD_TARGETS_
# Note: EXPLIB pass works as predefined in ODE rules
#       when used with BUILD action. Works as described 
#       above when only used with NEWACTION
#
##################################################

package RulesNewActionsTest;

use File::Path;
use File::Copy;
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
  my $tfile;
  my $tmpstr;
  my @delete_list = ($OdeTest::test_sb, $OdeTest::test_sb_rc, 
                     $OdeTest::tmpfile);

  OdeInterface::logPrint( "in RulesNewActionsTest\n");

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
          "rules NewActions Test : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    OdeFile::deltree( \@delete_list );
    return 1;
  }

  ## copying editing Makeconf
  $tmpstr = join( $OdeEnv::dirsep, ($bb_dir, "src", "Makeconf") );
  $tfile = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src") );
  OdeEnv::runSystemCommand( "$OdeEnv::copy $tmpstr $tfile" );
  $tfile .= $OdeEnv::dirsep . "Makeconf";
  if (! -f $tfile)
  {
    # Test case failed
    OdeInterface::printError( 
          "rules NewActions Test : $tfile not created" );
    OdeInterface::logPrint( "Test Failed\n" );
    return 1;
  }
  OdeInterface::logPrint( 
         "Adding the name of new rules file and other stuff to Makeconf\n" );
  open( INPUT, ">> $tfile") ||
    warn( "Could not open $tfile\n" );
  print( INPUT "RULES_MK=myrules.mk\n" );
  print( INPUT "EXTRA_ACTIONS=NEWACTION\n" );
  print( INPUT "_NEWPPPASS_SUBDIRS_=\${_STANDARD_SUBDIRS_}\n" );
  print( INPUT "_NEWPROGPASS_SUBDIRS_=\${_STANDARD_SUBDIRS_}\n" );
  print( INPUT "_NEWACTION_EXPLIB_SUBDIRS_=\${_STANDARD_SUBDIRS_}\n" );
  print( INPUT ".if defined(MAKEFILE_PASS)\n" );
  print( INPUT "_NEWACTION_PASSES_=\${MAKEFILE_PASS}\n" );
  print( INPUT ".else\n" );
  print( INPUT "_NEWACTION_PASSES_=NEWPPPASS NEWPROGPASS EXPLIB\n" );
  print( INPUT ".endif\n" );
  print( INPUT "_NEWACTION_ACTION_=newaction\n" );
  print( INPUT "_newaction_action_=NEWACTION\n" );
  close( INPUT );

  ## copying and editing makefile
  $tmpstr = join( $OdeEnv::dirsep, ($bb_dir, "src", "makefile") );
  $tfile = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src") );
  OdeEnv::runSystemCommand( "$OdeEnv::copy $tmpstr $tfile" );
  $tfile .= $OdeEnv::dirsep . "makefile";
  if (! -f $tfile)
  {
    # Test case failed
    OdeInterface::printError( 
          "rules NewActions Test : $tfile not created" );
    OdeInterface::logPrint( "Test Failed\n" );
    return 1;
  }
  ## removing .include<rules.mk> so as to add subdirs for new passes before
  ## including rules
  OdeFile::removeLinesFromFile( $tfile, ".include" );
  OdeInterface::logPrint( "Adding subdirs for new passes to the makefile\n" );
  open( INPUT, ">> $tfile") ||
    warn( "Could not open $tfile\n" );
  print( INPUT "NEWPPPASS_SUBDIRS=lib bin/server\n" );
  print( INPUT "NEWPROGPASS_SUBDIRS=lib bin/server bin/client\n" );
  print( INPUT "NEWACTION_EXPLIB_SUBDIRS=lib bin/server bin/client\n" );
  print( INPUT ".include<\${RULES_MK}>\n" );
  close( INPUT );

  ## creating myrules.mk
  $tfile = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src", "rules_mk") );
  mkpath( $tfile );
  $tfile .= $OdeEnv::dirsep . "myrules.mk";
  OdeInterface::logPrint( "Creating new rules file myrules.mk\n" );
  open( INPUT, ">> $tfile") ||
    warn( "Could not open $tfile\n" );
  print( INPUT ".include <rules.mk>\n" );
  print( INPUT "_NEWPPPASS_SUBDIRS_=\${NEWPPPASS_SUBDIRS:S|/|;|g}\n" );
  print( INPUT "_NEWPROGPASS_SUBDIRS_=\${NEWPROGPASS_SUBDIRS:S|/|;|g}\n" );
  print( INPUT
    "_NEWACTION_EXPLIB_SUBDIRS_=\${NEWACTION_EXPLIB_SUBDIRS:S|/|;|g}\n" );
  print( INPUT "newaction_all:\$\${_all_targets_};\@\n" );
  print( INPUT 
   "_NEWACTION_NEWPPPASS_TARGETS_=\${_ALL_OFILES_:S|\${OBJ_SUFF}\$|.pp|g}\n" );
  print( INPUT 
     "_NEWACTION_NEWPROGPASS_TARGETS_=\${PROGRAMS}\n" );
  $tmpstr = "_NEWACTION_EXPLIB_TARGETS_=\${PROGRAMS:S/\^/clean_/g} " .
    "\${PROGRAMS:S/\$/.X/g:S/\^/clean_/g} " .
    "\${OFILES:U\${PROGRAMS:S/\${PROG_SUFF}\$/\${OBJ_SUFF}/g}:S/\^/clean_/g} " .
    "\${LIBRARIES:S/\^/clean_/g}";
  print( INPUT "$tmpstr\n" );
  print( INPUT ".if !defined(SPECIAL_PASSES)\n" );
  print( INPUT ".ORDER: \${NEWACTION:L:\@.ACTION.\@{_PASS_ACTIONS}\@}\n" );
  print( INPUT ".endif\n" );
  print( INPUT "_BUILD_STANDARD_TARGETS_ += \${LIBRARIES}\n" );
  print( INPUT "\${_NEWACTION_EXPLIB_TARGETS_:Mclean*}: .SPECTARG\n" );
  print( INPUT ".if !defined(UNIX)\n" );
  print( INPUT 
         "\t\@\${ECHO} \${RM} \${_RMFLAGS_} \${_CLEANFILES_:S;/;\\\\;g}\n" );
  print( INPUT 
         "\t\@\${ECHO} \${_CLEANFILES_:S;/;\\\\;g:\@WORD\@\\\n" );
  print( INPUT "\t\${WORD:!if exist \${WORD} \${RM} \\\n" );
  print( INPUT "\t\${_RMFLAGS_} \${WORD}!}\@} >\${NULL_DEVICE}\n" );
  print( INPUT ".else\n" );
  print( INPUT "\t-\${RM} \${_RMFLAGS_} \${_CLEANFILES_}\n" );
  print( INPUT ".endif\n" );
  close( INPUT );
  if (! -f $tfile)
  {
    # Test case failed
    OdeInterface::printError( 
          "rules NewActions Test : $tfile not created" );
    OdeInterface::logPrint( "Test Failed\n" );
    return 1;
  }

  $rules_error = test_newactions();

  OdeFile::deltree( \@delete_list );
  return $rules_error;
}

##########################################
#
# sub test_newactions
# - to test the creation of new actions 
#   and passes
#
##########################################
sub test_newactions()
{
  my $status;
  my $error = 0;
  my $rules_error = 0;
  my $command;
  my $error_txt;
  my $tfile;
  my $file_count;
  my $bb_dir = $OdeEnv::tempdir . $OdeEnv::dirsep . "bbexample";
  my $obj_dir = join( $OdeEnv::dirsep,
                         ($OdeTest::test_sb, "obj", $OdeEnv::machine) );
  my $export_dir = join( $OdeEnv::dirsep,
                         ($OdeTest::test_sb, "export", $OdeEnv::machine) );
  my $dir = cwd();
  my $tmpstr = $OdeTest::test_sb . $OdeEnv::dirsep . "src";
  my $filecount1;
  my $filecount2;
  my $filecount3;
  my $filecount4;
  my $filecount5;
  my $filecount6;
  OdePath::chgdir( $tmpstr );

  if (defined( $OdeEnv::MVS ))
  {
    $filecount1 = 16;
    $filecount2 = 15;
    $filecount3 = 10;
    $filecount4 = 8;
    $filecount5 = 5;
    $filecount6 = 8;
  }
  else
  {
    $filecount1 = 10;
    $filecount2 = 8;
    $filecount3 = 4;
    $filecount4 = 1;
    $filecount5 = 3;
    $filecount6 = 6;
  }

  ## running build MAKEFILE_PASS=NEWPPPASS newaction_all
  ## should make .pp files in lib and bin/server
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=NEWPPPASS "
             . "newaction_all >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "lib";
  if (-f $tmpstr . $OdeEnv::dirsep . "depend.mk")
  {
    $file_count = 3;
  }
  else
  {
    $file_count = 2;
  }
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $file_count)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  if (!$rules_error && ((! -f $tmpstr . $OdeEnv::dirsep . "printmsg.pp") ||
                        (! -f $tmpstr . $OdeEnv::dirsep . "printnl.pp")))
  {
    $rules_error = 1;
    $error_txt = "printmsg.pp or printnl.pp missing in $tmpstr";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "server") );
  if (-f $tmpstr . $OdeEnv::dirsep . "depend.mk")
  {
    $file_count = 4;
  }
  else
  {
    $file_count = 3;
  }
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $file_count)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  if (!$rules_error && ((! -f $tmpstr . $OdeEnv::dirsep . "server.pp") ||
                        (! -f $tmpstr . $OdeEnv::dirsep . "retserver.pp") ||
                        (! -f $tmpstr . $OdeEnv::dirsep . "buildserver.pp")))
  {
    $rules_error = 1;
    $error_txt = "server.pp, retserver.pp or buildserver.pp missing in $tmpstr";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "bin";
  if (!$rules_error && ((-d $tmpstr . $OdeEnv::dirsep . "client") ||
                        (-d $tmpstr . $OdeEnv::dirsep . "logger")))
  {
    $rules_error = 1;
    $error_txt = "client or logger directory created in $tmpstr";
  }
  if (!$rules_error && ((-d $obj_dir . $OdeEnv::dirsep . "inc") ||
                        (-d $obj_dir . $OdeEnv::dirsep . "doc")))
  {
    $rules_error = 1;
    $error_txt = "a inc or doc directory created in $obj_dir";
  }
  if (!$rules_error && (OdeFile::numDirEntries( $export_dir ) != 0))
  {
    $rules_error = 1;
    $error_txt = "$export_dir not empty";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               1, "NEWACTIONS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                1, "NEWACTIONS" );
  }

  ## running build MAKEFILE_PASS=NEWPROGPASS newaction_all -a
  ## should make executables in bin/server and bin/client
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=NEWPROGPASS "
             . "newaction_all -a >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "lib";
  if (-f $tmpstr . $OdeEnv::dirsep . "depend.mk")
  {
    $file_count = 3;
  }
  else
  {
    $file_count = 2;
  }
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $file_count)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  if (!$rules_error && ((! -f $tmpstr . $OdeEnv::dirsep . "printmsg.pp") ||
                        (! -f $tmpstr . $OdeEnv::dirsep . "printnl.pp")))
  {
    $rules_error = 1;
    $error_txt = "printmsg.pp or printnl.pp missing in $tmpstr";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "server") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount1)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  if (!$rules_error && 
          ((! -f $tmpstr . $OdeEnv::dirsep . "server.pp") ||
           (! -f $tmpstr . $OdeEnv::dirsep . "server$OdeEnv::obj_suff") ||
           (! -f $tmpstr . $OdeEnv::dirsep . "server$OdeEnv::prog_suff")))
  {
    $rules_error = 1;
    $error_txt = "server.pp or server$OdeEnv::obj_suff or " .
                 "server$OdeEnv::prog_suff missing in $tmpstr";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "client") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount2)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "logger") );
  if (!$rules_error && (-d $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr created";
  }
  if (!$rules_error && (OdeFile::numDirEntries( $export_dir ) != 0))
  {
    $rules_error = 1;
    $error_txt = "$export_dir not empty";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               2, "NEWACTIONS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                2, "NEWACTIONS" );
  }

  ## running build MAKEFILE_PASS=EXPLIB newaction_all
  ## must delete executables and libraries in lib, bin/server, bin/client
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=EXPLIB "
             . "newaction_all >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "lib";
  if (-f $tmpstr . $OdeEnv::dirsep . "depend.mk")
  {
    $file_count = 3;
  }
  else
  {
    $file_count = 2;
  }
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $file_count)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "server") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount3)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "client") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount4)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "logger") );
  if (!$rules_error && (-d $tmpstr))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr created";
  }
  if (!$rules_error && (OdeFile::numDirEntries( $export_dir ) != 0))
  {
    $rules_error = 1;
    $error_txt = "$export_dir not empty";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               3, "NEWACTIONS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                3, "NEWACTIONS" );
  }

  ## running build MAKEFILE_PASS=STANDARD -a
  ## must also build library as this target is appended in myrules.mk
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=STANDARD "
             . "-a >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "lib";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount6)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "server") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount1)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "client") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount2)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "logger") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount5)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "inc") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != 0)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or is not empty";
  }
  if (!$rules_error && (OdeFile::numDirEntries( $export_dir ) != 0))
  {
    $rules_error = 1;
    $error_txt = "$export_dir not empty";
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               4, "NEWACTIONS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                4, "NEWACTIONS" );
  }

  ## running build MAKEFILE_PASS=EXPLIB 
  ## since newaction_all is not defined, EXPLIB must work as predefined
  ## in ODE rules
  $command = "build -rc $OdeTest::test_sb_rc MAKEFILE_PASS=EXPLIB "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  ## none of the executables and libraries should be deleted
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "lib";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount6)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "server") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount1)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "client") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount2)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($export_dir, "usr", "lib") );
  if (!$rules_error && (! -d $tmpstr) )
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
  }
  ## exa.lib or libexa.a should be exported
  if (!$rules_error )
  {
    if (defined( $OdeEnv::UNIX ))
    {
      $tmpstr .= $OdeEnv::dirsep . "libexa.a";
      if (! -l $tmpstr)
      {
        $rules_error = 1;
        $error_txt = "$tmpstr not created";
      }
    }
    else
    {
      $tmpstr .= $OdeEnv::dirsep . "exa.lib";
      if (! -f $tmpstr)
      {
        $rules_error = 1;
        $error_txt = "$tmpstr not created";
      }
    }
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               5, "NEWACTIONS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                5, "NEWACTIONS" );
  }

  ## running build newaction_all
  ## must execute all the passes defined for this action
  $command = "build -rc $OdeTest::test_sb_rc newaction_all "
             . ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if ($status)
  {
    $rules_error = 1;
    $error_txt = "build returned a non-zero exit code";
  }
  $tmpstr = $obj_dir . $OdeEnv::dirsep . "lib";
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount5)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "server") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount3)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "client") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount4)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($obj_dir, "bin", "logger") );
  if (!$rules_error && ((! -d $tmpstr) || 
                        (OdeFile::numDirEntries( $tmpstr ) != $filecount5)))
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created or has incorrect number of files";
  }
  $tmpstr = join( $OdeEnv::dirsep, ($export_dir, "usr", "lib") );
  if (!$rules_error && (! -d $tmpstr) )
  {
    $rules_error = 1;
    $error_txt = "$tmpstr not created";
  }
  ## exa.lib or libexa.a should be exported
  if (!$rules_error )
  {
    if (defined( $OdeEnv::UNIX ))
    {
      $tmpstr .= $OdeEnv::dirsep . "libexa.a";
      if (! -l $tmpstr)
      {
        $rules_error = 1;
        $error_txt = "$tmpstr not created";
      }
    }
    else
    {
      $tmpstr .= $OdeEnv::dirsep . "exa.lib";
      if (! -f $tmpstr)
      {
        $rules_error = 1;
        $error_txt = "$tmpstr not created";
      }
    }
  }
  if ($rules_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "rules",
                               6, "NEWACTIONS" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    $rules_error = 0;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "rules",
                                6, "NEWACTIONS" );
  }

  OdePath::chgdir( $dir );
  return $error;
}

  
1;
