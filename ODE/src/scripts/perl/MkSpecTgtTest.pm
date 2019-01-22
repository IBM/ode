###########################################
#
# MkSpecTgtTest module
# - module to test mk special targets
#
###########################################

package MkSpecTgtTest;

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
  my @arg_list;
  my @str_list;
  my @delete_list;
  my $tfile1;
  my $tfile2;
  my $tfile3;
  my $tfile4;
  my $tfile5;
  my $tfile6;
  my $makefile_name;
  my $makefile;
  my $dir = cwd();

  @delete_list = ($OdeEnv::tempdir . $OdeEnv::dirsep . "path.mk",
                   $OdeEnv::tempdir . $OdeEnv::dirsep . "dir1",
                   $OdeEnv::tempdir . $OdeEnv::dirsep . "testfile1.txt",
                   $OdeTest::tmpfile);

  OdeInterface::logPrint( "in MkSpecTgtTest\n");
 
  ## Testing  special targets

  ## running mk -f sptargets1.mk
  @str_list = (".BEGIN should be executed first",
               "test1 should be executed after .BEGIN",
               ".END should be executed penultimately",
               ".EXIT should be executed at the end");
  $status = runMkTest( "sptargets1.mk", "special targets .BEGIN .END .EXIT", 
                       1, \@str_list);
  if (!$error) { $error = $status; }

  ## running mk -f sptargets1.mk test1
  ## output should be the same as above
  @arg_list = ("test1");
  $status = runMkTest( "sptargets1.mk", "special targets .BEGIN .END .EXIT", 
                        2, \@str_list, \@arg_list);
  if (!$error) { $error = $status; }

  ## running mk -f sptargets1.mk .BEGIN
  @arg_list = (".BEGIN");
  @str_list = (".BEGIN should be executed first",
               ".END should be executed penultimately",
               ".EXIT should be executed at the end");
  $status = runMkTest( "sptargets1.mk", "special targets .BEGIN .END .EXIT", 
                        3, \@str_list, \@arg_list);
  if (!$error) { $error = $status; }

  ## running mk -f sptargets1.mk .END
  ## output should be the same as above
  @arg_list = (".END");
  $status = runMkTest( "sptargets1.mk", "special targets .BEGIN .END .EXIT", 
                        4, \@str_list, \@arg_list);
  if (!$error) { $error = $status; }

  ## running mk -f sptargets1.mk .EXIT
  @arg_list = (".EXIT");
  @str_list = (".BEGIN should be executed first",
               ".EXIT should be executed at the end",
               ".END should be executed penultimately");
  $status = runMkTest( "sptargets1.mk", "special targets .BEGIN .END .EXIT", 
                        5, \@str_list, \@arg_list);
  if (!$error) { $error = $status; }

  ## running mk -f sptargets2.mk
  @str_list = (".EXIT should be executed first",
               ".BEGIN should be executed after .EXIT",
               "test3 should be executed after .BEGIN",
               "test2 should be executed after test3",
               ".MAIN should be executed after test2",
               ".END should be the last target");
  $status = runMkTest( "sptargets2.mk", "special targets .MAIN .ORDER", 6, 
                        \@str_list);
  if (!$error) { $error = $status; }
  
  ## testing .ORDER
  ## running mk -f order1.mk
  @str_list = ("test2", "test4", "test7", "test6", "test5", "test3", "test1");
  $status = runMkTest( "order1.mk", "specail target .ORDER", 7, \@str_list);
  if (!$error) { $error = $status; }

  ## running mk -f order2.mk
  @str_list = ("BEGIN", "END","EXIT");
  $status = runMkTest( "order2.mk", "special target .ORDER", 8, \@str_list);
  if (!$error) { $error = $status; }

  ## running mk -f order3.mk -j3
  @arg_list = ("-j3");
  @str_list = ("starting test1", "ending test1", "starting test2",
               "ending test2", "starting test3", "ending test3");
  $status = runMkTest( "order3.mk", "special target .ORDER", 9, 
                       \@str_list, \@arg_list);
  if (!$error) { $error = $status; }

  ## testing .SUFFIXES
  ## running mk -f suffixes.mk
  @str_list = ("Making test.c", "Converting test.c to test.o",
               "Converting test.o to test.a", "Making testb.tar",
               "Converting testb.tar to testb.tar.Z", "Making f.1",
               "Converting f.1 to f.2", "Making g.1", "Converting g.1 to g.2");
  $status = runMkTest( "suffixes.mk", "special target .SUFFIXES", 10, 
                       \@str_list);
  if (!$error) { $error = $status; }

  ## running mk -f suffixes_error1.mk
  $status = runMkErrorTest( "suffixes_error1.mk", "special target .SUFFIXES", 
                            11);
  if (!$error) { $error = $status; }

  ## running mk -f suffixes_error2.mk
  $status = runMkErrorTest( "suffixes_error2.mk", "special target .SUFFIXES", 
                            12);
  if (!$error) { $error = $status; }



  ## testing .LINKTARGS
  ## running mk -f linktargs.mk
  @str_list = ("making a1 a2 a3", "b1", "b2", "c3", "c4", "c5", "c1", "c3 c4 c5", "pre-d cmd", "d3");
  $status = runMkTest( "linktargs.mk", "special target .LINKTARGS", 13, 
                       \@str_list);
  if (!$error) { $error = $status; }

  ## running mk -f linktargs.mk c2 c1 d4
  @arg_list = ("c2 c1 d4");
  @str_list = ("c3", "c4", "c5", "c2", "c3 c4 c5", "pre-d cmd", "d4");
  $status = runMkTest( "linktargs.mk", "special target .LINKTARGS", 14, 
                       \@str_list, \@arg_list);
  if (!$error) { $error = $status; }



  ## Testing .NOTPARALLEL
  ## running mk -f notparallel.mk -j3
  @arg_list = ("-j3");
  @str_list = ("starting test1", "ending test1", "starting test2",
               "ending test2", "starting test3", "ending test3");
  $status = runMkTest( "notparallel.mk", "special target .NOTPARALLEL", 15, 
                       \@str_list, \@arg_list);
  if (!$error) { $error = $status; }

  ## Testing .PATH
  ## running mk -f path.mk FILEPATH=path1 CFILEPATH=path2
  $makefile_name = "sptargets" . $OdeEnv::dirsep . "path.mk";
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
  {
    OdeInterface::logPrint( "$makefile_name not found\n" );
  }
  else
  {
    OdeEnv::runSystemCommand( "$OdeEnv::copy $makefile $OdeEnv::tempdir" );
  }
  $tfile1 = join( $OdeEnv::dirsep, 
                  ($OdeEnv::tempdir, "dir1", "dir2") );
  mkpath( $tfile1 );
  $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "testfile1.txt";
  $tfile2 = join( $OdeEnv::dirsep, 
                  ($OdeEnv::tempdir, "dir1", "testfile1.txt") );
  $tfile3 = join( $OdeEnv::dirsep, 
                  ($OdeEnv::tempdir, "dir1", "testfile2.txt") );
  $tfile4 = join( $OdeEnv::dirsep, 
                  ($OdeEnv::tempdir, "dir1", "test1.c") );
  $tfile5 = join( $OdeEnv::dirsep, 
                  ($OdeEnv::tempdir, "dir1", "dir2","test1.c") );
  $tfile6 = join( $OdeEnv::dirsep, 
                  ($OdeEnv::tempdir, "dir1", "dir2","test2.c") );
  open( INPUT, "> $tfile1" ) ||
    OdeInterface::logPrint( "Could not open file $tfile1\n" );
  print( INPUT "testvar1=100" );
  close( INPUT );
  open( INPUT, "> $tfile2" ) ||
    OdeInterface::logPrint( "Could not open file $tfile2\n" );
  print( INPUT "testvar1=200" );
  close( INPUT );
  open( INPUT, "> $tfile3" ) ||
    OdeInterface::logPrint( "Could not open file $tfile3\n" );
  print( INPUT "testvar2=200" );
  close( INPUT );
  open( INPUT, "> $tfile4" ) ||
    OdeInterface::logPrint( "Could not open file $tfile4\n" );
  print( INPUT "cvar1=2000" );
  close( INPUT );
  open( INPUT, "> $tfile5" ) ||
    OdeInterface::logPrint( "Could not open file $tfile5\n" );
  print( INPUT "cvar1=3000" );
  close( INPUT );
  open( INPUT, "> $tfile6" ) ||
    OdeInterface::logPrint( "Could not open file $tfile6\n" );
  print( INPUT "cvar2=3000" );
  close( INPUT );

  ## cd'ing to the temp directory having the makefile path.mk
  OdePath::chgdir( $OdeEnv::tempdir );
  $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "dir1";
  $tfile2 = $tfile1 . $OdeEnv::dirsep . "dir2";

  @str_list = ("testvar1 is 100", "testvar2 is 200", "cvar1 is 2000");
  @arg_list = ("FILEPATH=$tfile1", "CFILEPATH=$tfile2");
  $status = runMkTest( "path.mk", "special target .PATH", 16, 
                       \@str_list, \@arg_list );
  if (!$error) { $error = $status; }


  ## cd'ing back to the current directory
  OdePath::chgdir( $dir );

  ## running mk -f path_error1.mk FILEPATH=path
  @arg_list = ("FILEPATH=$OdeEnv::tempdir");
  $status = runMkErrorTest( "path_error1.mk", "special target .PATH", 17, 
                             @arg_list );
  if (!$error) { $error = $status; }

  ## running mk -f sptargets3.mk
  $makefile_name = "sptargets" . $OdeEnv::dirsep . "sptargets3.mk";
  $makefile = OdeTest::getMakefile( $makefile_name );
  if (!$makefile)
	{
    OdeInterface::printError( "mk : special targets" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile_name\n" );
    OdeFile::deltree( \@delete_list );
    return( 1 );
	}
  $command = "mk -f $makefile > $OdeTest::tmpfile $OdeEnv::stderr_redir";
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
  if (!$mk_error && 
       (!(OdeFile::findInFile( "ERROR", $OdeTest::tmpfile ))) ||
       (!(OdeFile::findInFile( 
                  ".BEGIN should be executed first", $OdeTest::tmpfile ))) ||
       (!(OdeFile::findInFile( "crap", $OdeTest::tmpfile ))) ||
       (!(OdeFile::findInFile( ".ERROR executed", $OdeTest::tmpfile ))) ||
       (!(OdeFile::findInFile( ".EXIT executed at end", $OdeTest::tmpfile )))) 
  {
    $mk_error = 1;
    $error_txt = "mk did not produce an error or all the targets " .
                 "are not executed";
  }
  if ($mk_error)
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, 
                                  "mk", 18, "special target .ERROR" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $status = 1;
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, 
                                 "mk", 18, "special targets .ERROR" );
    $status = 0;
  }
  if (!$error) { $error = $status; }

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
#   testno: test number
#   str_list: reference to a list of all the strings
#             to be looked for in the tempfile
#   args: reference to a list of any other commandline args
############################################################
sub runMkTest( $$$$;$ )
{
  my ($filename, $tststr, $testno, $str_list, $args) = @_;
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
  ## path.mk is in the temp directory for test14
  if ($testno == 16)
  {
    $makefile = $filename;
  }
  else
  {
    $makefile_name = "sptargets" . $OdeEnv::dirsep . $filename;
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
      if (!(OdeFile::findInFile( 
                       @$str_list[$index], $OdeTest::tmpfile, 0, $index+1 )))
      {
        $mk_error = 1;
        $error_txt = "Error occured while testing $tststr";
        last;
      }
    }
  }
  # Check if the correct number of targets are executed
  if (!$mk_error && 
     (OdeFile::numLinesInFile( $OdeTest::tmpfile ) != $str_num))
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
  $makefile_name = "sptargets" . $OdeEnv::dirsep . $filename;
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
1;
