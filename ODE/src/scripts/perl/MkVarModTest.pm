#########################################
#
# MkVarModtest module
# - module to test mk variable modifiers
#
#########################################

package MkVarModTest;

use File::Path;
use File::Copy;
use File::Basename;
use OdeEnv;
use OdeFile;
use OdeTest;
use OdeUtil;
use OdeInterface;
use strict;
use Cwd;

# Defining these vars to be global to all 
#  subroutines in this file
my $avar_outfile="varmod_a.out";
my $avar1="HappyHappyJoyJoy";
my $avar2="Crap";
my $avar3="WeDontNeedNoStinkingBadges";
my $Adesc=":A variable modifier";
my $adesc=":a variable modifier";


#########################################
#
# sub run
#
#########################################
sub run ()
{

  my @delete_list;
  my $dir = cwd();
  my $tfile1;
  my $tfile2;
  my $cmd_file = $OdeEnv::tempdir . $OdeEnv::dirsep . "ode_input.txt";
  ## locating the makefiles directory
  my $makefile_dir = join( $OdeEnv::dirsep, (".", "makefiles", "varmod") );
  if (! -d $makefile_dir )
  {
    $makefile_dir = join( $OdeEnv::dirsep, ($OdeEnv::bb_base, "src", "scripts",
                          "perl", "makefiles", "cmdflags") );
    if (! -d $makefile_dir)
    {
      OdeInterface::printError( "mk: variable modifiers" );
      OdeInterface::logPrint(
          "Could not locate the makefiles directory - $makefile_dir\n" );
      return( 1 );
    }
  }

  $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep;
  @delete_list = ($tfile1 . "varmod_atsign.mk", $tfile1 . "varmod_bang.mk",
                  $tfile1 . "varmod_eq.mk", $tfile1 . "varmod_lower_b.mk",
                  $tfile1 . "varmod_lower_l.mk", $tfile1 . "varmod_lower_p.mk",
                  $tfile1 . "varmod_lower_d.mk",
                  $tfile1 . "varmod_lower_m.mk", $tfile1 . "varmod_lower_n.mk",
                  $tfile1 . "varmod_lower_s.mk", $tfile1 . "varmod_lower_u.mk",
                  $tfile1 . "varmod_upper_b.mk", $tfile1 . "varmod_upper_d.mk",
                  $tfile1 . "varmod_upper_e.mk", $tfile1 . "varmod_upper_f.mk",
                  $tfile1 . "varmod_upper_h.mk", $tfile1 . "varmod_upper_l.mk",
                  $tfile1 . "varmod_upper_m.mk", $tfile1 . "varmod_upper_n.mk",
                  $tfile1 . "varmod_upper_p.mk", $tfile1 . "varmod_upper_r.mk",
                  $tfile1 . "varmod_upper_s.mk", $tfile1 . "varmod_upper_t.mk",
                  $tfile1 . "varmod_upper_u.mk", $tfile1 . "varmod_upper_x.mk",
                  $tfile1 . "varmod_upper_c.mk", $tfile1 . "varmod_upper_g.mk",
                  $tfile1 . "varmod_bang.mk",
                  $tfile1 . "varmod_upper_a.mk", $tfile1 . "varmod_lower_a.mk",
                  $tfile1 . "varmod_lower_rm.mk", $tfile1 . "varmod_lower_rr.mk",
                  $tfile1 . "tdir1", $tfile1 . "tsb", $tfile1 . "crap1",
                  $tfile1 . "crap3", $OdeTest::test_sb, $OdeTest::test_sb_rc,
                  $cmd_file, $OdeTest::tmpfile);
  # remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  my $makefile = $makefile_dir . $OdeEnv::dirsep . "*";
  ## copying all the makefiles into the temp directory
  OdeEnv::runSystemCommand( "$OdeEnv::copy $makefile $OdeEnv::tempdir" );
  ## cd'ing to the temp directory
  OdePath::chgdir( $OdeEnv::tempdir );

  my @makefiles = ( "varmod_upper_b.mk",
                    "varmod_lower_b.mk",
                    "varmod_upper_d.mk",
                    "varmod_upper_u.mk",
                    "varmod_upper_e.mk",
                    "varmod_upper_r.mk",
                    "varmod_upper_f.mk",
                    "varmod_upper_h.mk",
                    "varmod_upper_t.mk",
                    "varmod_upper_l.mk",
                    "varmod_lower_l.mk",
                    "varmod_lower_u.mk",
                    "varmod_upper_m.mk",
                    "varmod_lower_m.mk",
                    "varmod_upper_n.mk",
                    "varmod_lower_n.mk",
                    "varmod_upper_s.mk",
                    "varmod_lower_s.mk", 
                    "varmod_lower_x_upper_m.mk",
                    "varmod_lower_x_lower_m.mk",
                    "varmod_lower_x_upper_n.mk",
                    "varmod_lower_x_lower_n.mk",
                    "varmod_lower_x_upper_s.mk",
                    "varmod_lower_x_lower_s.mk",
                    "varmod_upper_p.mk",
                    "varmod_lower_p.mk",
                    "varmod_lower_d.mk",
                    "varmod_upper_x.mk",
                    "varmod_eq.mk",
                    "varmod_atsign.mk",
                    "varmod_upper_c.mk",
                    "varmod_upper_c.mk",
                    "varmod_upper_c.mk",
                    "varmod_bang.mk",
                    "varmod_upper_a.mk",
                    "varmod_lower_a.mk",
                    "varmod_lower_rm.mk",
                    "varmod_lower_rr.mk",
                    "varmod_upper_q.mk" );
  my @test_strs = ( ":B variable modifier", 
                    ":b variable modifier",
                    ":D variable modifier", 
                    ":U variable modifier", 
                    ":E variable modifier",
                    ":R variable modifier",
                    ":F variable modifier",
                    ":H variable modifier",
                    ":T variable modifier",
                    ":L variable modifier",
                    ":l variable modifier", 
                    ":u variable modifier",
                    ":M variable modifier", 
                    ":m variable modifier", 
                    ":N variable modifier",
                    ":n variable modifier", 
                    ":S variable modifier",
                    ":s variable modifier",
                    ":xM variable modifier",
                    ":xm variable modifier",
                    ":xN variable modifier",
                    ":xn variable modifier",
                    ":xS variable modifier",
                    ":xs variable modifier",
                    ":P variable modifier",
                    ":p variable modifier",
                    ":d variable modifier",
                    ":X<D|F|B> variable modifier",
                    "= (substitution) variable modifier",
                    "@ variable modifier",
                    ":C variable modifier",
                    ":C variable modifier",
                    ":C variable modifier",
                    ":! variable modifier",
                    "$Adesc",
                    "$adesc",
                    ":rm variable modifier",
                    ":rr variable modifier",
                    ":Q variable modifier" );

  # Variables to test the :d operator
  my $dvar1 = $ENV{'HOME'};
  if ( substr( $dvar1, length( $dvar1 ) - 1, 1 ) ne $OdeEnv::dirsep) 
  {
    $dvar1 .= $OdeEnv::dirsep; # add trailing dirsep, if none
  } 
  my $dvarhome = $dvar1;
  $dvar1 .= $OdeEnv::machine;  # Add machine directory
  my $dvar2 = $OdeEnv::machine;
  $dvar2 = substr( $dvar2, 0, length( $dvar2 ) - 1 ) . "*";
  # Variables to test the :X and ! operators
  my $xvar = 
       "\"$OdeEnv::logdir" . " $OdeEnv::logdir" . "notadir $OdeEnv::bldcurr";
  $xvar .= $OdeEnv::pathsep . "$OdeEnv::logdir" . "notafile\"";
  my $isunix;
  # lists containing variables to test :C operator
  my @cvar1;
  my @cvar2;
  my @cvar3;
  # lists containing variables to test :G operator
  my @gvar1;
  my @gvar2;
  my $gvar2_1;
  my $gvar2_2;
  my $gvar2_3;
  my $tdir = basename( $OdeEnv::tempdir );
  my $root;
  if (defined( $OdeEnv::UNIX ))
  {
    $root = "/";
  }
  else
  {
    ## extracting the drive letter and appending : and /
    $root = substr( $OdeTest::test_sb, 0, 1 );
    $root .= ":/";
  }
  $gvar2_1 = $OdeEnv::pathsep . $root . "dir1";
  $gvar2_2 = "-I" . $root . "dir1";
  $gvar2_3 = $OdeEnv::pathsep . $OdeTest::test_sb;
  $gvar2_3 = join( $OdeEnv::dirsep, 
                   ($gvar2_3, "obj", $OdeEnv::machine, "dir1") );
  $gvar2_3 .= $OdeEnv::pathsep . $OdeTest::test_sb;
  $gvar2_3 = join( $OdeEnv::dirsep, ($gvar2_3, "src", "dir1") );
  $gvar2_3 .= $OdeEnv::pathsep . $OdeTest::test_bbdir;
  $gvar2_3 = join( $OdeEnv::dirsep, 
                   ($gvar2_3, "obj", $OdeEnv::machine, "dir1") );
  $gvar2_3 .= $OdeEnv::pathsep . $OdeTest::test_bbdir;
  $gvar2_3 = join( $OdeEnv::dirsep, ($gvar2_3, "src", "dir1") );
  $gvar2_3 = OdePath::unixize( $gvar2_3 );

  if ( defined( $OdeEnv::UNIX ) ) 
  { 
    $isunix = "-DUNIX";
    ## assumed that the user cannot create a directory in /dev/null
    @cvar1 = ("./tdir1/tdir2/tdir3/:/dev/null/tdir:\${tdir}",
              "$OdeEnv::tempdir/crap1/crap2",
              "../$tdir/crap3/../crap3/crap4/");
    @cvar2 = ("/dev/null/tdir", "", "");
    @cvar3 = ("./tdir1/tdir2/tdir3/ \${tdir}",
              "$OdeEnv::tempdir/crap1/crap2",
              "../$tdir/crap3/../crap3/crap4/");
    @gvar1 = ("VAR1=\"-I/dir1 -V\"", "VAR1=\"-I/dir1\"", "VAR1=\"-Idir1 -V\"",
              "VAR1=\"-I/dir1 -V\" error");
    @gvar2 = ("VAR2=\"$gvar2_1\"", "VAR2=\"$gvar2_2\"", "VAR2=\"$gvar2_3\"", 
              "");
  }
  else
  {
    @cvar1 = ("./tdir1/tdir2/tdir3/;4:/usr/bin/tdir;\${tdir}",
              "$OdeEnv::tempdir/crap1/crap2",
              "../$tdir/crap3/../crap3/crap4/");
    @cvar2 = ("4:/usr/bin/tdir", "", "");
    @cvar3 = ("./tdir1/tdir2/tdir3/ \${tdir}",
              "$OdeEnv::tempdir/crap1/crap2",
              "../$tdir/crap3/../crap3/crap4/");
    @gvar1 = ("VAR1=\"-I/dir1 -V\"", "VAR1=\"-I/dir1\"", "VAR1=\"-Idir1 -V\"",
              "VAR1=\"-I/dir1 -V\" error");
    @gvar2 = ("VAR2=\"$gvar2_1\"", "VAR2=\"$gvar2_2\"", "VAR2=\"$gvar2_3\"", 
              "");
  }
  
  my @mk_args = (   "", 
                    "",
                    "", 
                    "", 
                    "",
                    "",
                    "FINDPATH=$OdeEnv::logdir RES1=$OdeEnv::bldcurr VAR1=$OdeEnv::machine.testlog",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "", 
                    "", 
                    "", 
                    "", 
                    "", 
                    "", 
                    "", 
                    "", 
                    "", 
                    "",
                    "CASE_INSENSITIVE_FILE_NAMES=$OdeEnv::CASE_INSENSITIVE_FILE_NAMES",
                    "CASE_INSENSITIVE_FILE_NAMES=$OdeEnv::CASE_INSENSITIVE_FILE_NAMES",
                    "FINDPATH=$OdeEnv::logdir RES1=$OdeEnv::bldcurr VAR1=$OdeEnv::machine.testlog",
                    "FINDPATH=$OdeEnv::logdir RES1=$OdeEnv::bldcurr VAR1=$OdeEnv::machine.testlog",
                    "FINDPATH=$dvarhome RES1=$dvar1 VAR1=$OdeEnv::machine VAR2=$dvar2",
                    "VAR1=$xvar RES_XD=$OdeEnv::logdir RES_XF=$OdeEnv::bldcurr RES_XB=\"$OdeEnv::logdir $OdeEnv::bldcurr\"",
                    "",
                    "",
                    "VAR1=\"@cvar1[0]\" VAR2=\"@cvar2[0]\" VAR3=\"@cvar3[0]\" tdir=$OdeEnv::tempdir",
                    "VAR1=\"@cvar1[1]\" VAR2=\"@cvar2[1]\" VAR3=\"@cvar3[1]\" tdir=$OdeEnv::tempdir", 
                    "VAR1=\"@cvar1[2]\" VAR2=\"@cvar2[2]\" VAR3=\"@cvar3[2]\" tdir=$OdeEnv::tempdir",
                    "$isunix",
                    "FILE=\"$avar_outfile\" VAR1=\"$avar1\" VAR2=\"$avar2\"",
                    "FILE=\"$avar_outfile\" VAR1=\"$avar1\" VAR2=\"$avar3\"",
                    "VAR1=\"$OdeEnv::pathsep\"",
                    "VAR1=\"$OdeEnv::pathsep\"",
                    "" );
  my @num_tests = ( 4, 4, 4, 4, 10, 11, 3, 3, 3, 3, 4, 4, 7, 7, 7, 7, 11, 12,
                    7, 7, 7, 7, 20, 23,
                    3, 3, 3, 3, 2, 3, 4, 4, 4, 4, 2, 2, 10, 7, 5 );
  my $error_txt;
  my $idx;
  my $command;
  my $status;
  my $error = 0;
  my $testno = 1;

  OdeInterface::logPrint( "in MkVarModTest\n");
  
  OdeEnv::runOdeCommand( "mk -version >> $OdeEnv::bldcurr $OdeEnv::stderr_redir");


  $command = "mksb -dir $OdeTest::test_sbdir -rc $OdeTest::test_sb_rc "
           . "-back $OdeTest::test_bbdir $OdeTest::test_sb_name "
           . "-m $OdeEnv::machine -auto >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if (($status != 0) || (OdeTest::validateSandbox( $OdeTest::test_sb,
                                        $OdeTest::test_sb_rc, \$error_txt )))
  {
    #Test case failed
    OdeInterface::printError( ":G variable modifier : mksb does not work" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
    $error = 1;
    OdeFile::deltree( \@delete_list );
    return $error;
  }

  for ( $idx = 0; $idx < scalar @makefiles; $idx++)
  {
    $status = runMkTest( 
         $makefiles[$idx], $test_strs[$idx], $num_tests[$idx], $testno,
                                                               $mk_args[$idx] );
    $testno++;                                                        
    if (!$error) { $error = $status }
  }

  my @makefiles = ( "varmod_bang.mk",
                    "varmod_upper_a.mk",
                    "varmod_lower_a.mk",
                    "varmod_upper_q.mk" );
  my @test_strs = ( "! (shell command) variable modifier", 
                    "$Adesc",
                    "$adesc",
                    ":Q variable modifier" );
  my @mk_args = (   "test5", 
                    "FILE=\"$avar_outfile\" VAR1=\"$avar1\" VAR2=\"$avar2\" Errtest1",
                    "FILE=\"$avar_outfile\" VAR1=\"$avar1\" VAR2=\"$avar3\" Errtest1",
                    "test6" );
  for ( $idx = 0; $idx < scalar @makefiles; $idx++)
  {
    $status = runMkErrorTest( 
         $makefiles[$idx], $test_strs[$idx], $testno, $mk_args[$idx] );
    $testno++;                                                        
    if (!$error) { $error = $status }
  }
  
  $tfile1 = join( $OdeEnv::dirsep, ($OdeEnv::tempdir, "varmod_upper_g.mk") );
  $tfile2 = join( $OdeEnv::dirsep, ($OdeTest::test_sb, "src") ); 
  ## copying the makefile into the sandbox
  copy( $tfile1, $tfile2 );
  $tfile2 .= $OdeEnv::dirsep . "varmod_upper_g.mk";
  if (! -f $tfile2)
  {
    OdeInterface::printError( "mk: :G variable modifier" );
    OdeInterface::logPrint( "Could not copy $tfile1 into $tfile2\n" );
    return( 1 );
  }
  for ( $idx = 0; $idx < scalar @gvar1; $idx++)
  {
    $status = runvarmod_GTest( $testno, @gvar1[$idx], @gvar2[$idx] );
    $testno++;                                                        
    if (!$error) { $error = $status }
  }

  OdeFile::deltree( \@delete_list );
  OdePath::chgdir( $dir );
  return( $error );

}

#########################################
#
# sub runMkTest
# - execute and validate a mk test
#
# - returns 1 is makefile not found, otherwise
#   returns test error condition
# - arguments
#   makefile - makefile to use
#   test_desc - test description
#   num_tests - number of tests in makefile
#   testno - test number
#   mk_args - arguments to the mk command
#
#########################################
sub runMkTest( $$$ )
{
  my($makefile, $test_desc, $num_tests, $testno, @mk_args) = @_;
  my $command;
  my $status;
  my $error = 0;
  my $mk_error = 0;
  my $error_txt;

  $mk_error = 0;
  unshift( @mk_args, "-f $makefile" );
  $command = "mk @mk_args > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  # Copy output from output file to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }
  # Check if any tests failed
  if (!$mk_error && ( OdeFile::findInFile( "ODEMKERROR", $OdeTest::tmpfile ) 
      || (OdeFile::numPatternsInFile( 
          "ODEMKPASS", $OdeTest::tmpfile ) != $num_tests)))
  {
    $mk_error = 1;
    $error_txt = "mk " . $test_desc . " error";
  }
  # Check :A output
  if ($test_desc eq $Adesc)
  {
    if (OdeFile::findInFile( "$avar1", "$avar_outfile" ) ||
        !OdeFile::findInFile( "$avar2", "$avar_outfile" ))
    {
      $mk_error = 1;
      $error_txt = "mk " . $test_desc . " output file error";
    }
  }
  # Check :a output
  if ($test_desc eq $adesc)
  {
    if (!OdeFile::findInFile( "$avar1", "$avar_outfile" ) ||
        !OdeFile::findInFile( "$avar2", "$avar_outfile" ) ||
        !OdeFile::findInFile( "$avar3", "$avar_outfile" ))
    {                                        
      $mk_error = 1;
      $error_txt = "mk " . $test_desc . " output file error";
    }
  }
  if ($mk_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                                      $testno, $test_desc );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                                      $testno, $test_desc );
  }
  
  return( $error );

}

#########################################
#
# sub runMkErrorTest
# - execute and validate a mk test
#
# - returns 1 is makefile not found, otherwise
#   returns test error condition
# - arguments
#   makefile - makefile to use
#   test_desc - test description
#   testno - test number
#   mk_args - arguments to the mk command
#
#########################################
sub runMkErrorTest( $@ )
{
  my($makefile, $test_desc, $testno, @mk_args) = @_;
  my $command;
  my $status;
  my $error = 0;
  my $mk_error = 0;
  my $error_txt;

  $mk_error = 0;
  $command = "mk -f $makefile @mk_args > $OdeTest::tmpfile $OdeEnv::stderr_redir";
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
  # Check if no targets are executed
  if (!$mk_error && (OdeFile::findInFile( "ODEMKPASS", $OdeTest::tmpfile) ||
                     OdeFile::findInFile( "ODEMKERROR", $OdeTest::tmpfile)))
  {
    $mk_error = 1;
    $error_txt = "mk executed a target";
  }
  if ($mk_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                                      $testno, $test_desc );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                                      $testno, $test_desc );
  }
  return( $error );
}

#########################################
#
# sub runvarmod_GTest
# - execute and validate a test for :G 
#   variable modifier
#
# - returns 1 is makefile not found, otherwise
#   returns test error condition
# - arguments
#   testno - test number
#   mk_args - arguments to the mk command
#
#########################################
sub runvarmod_GTest( $@ )
{
  my( $testno, @mk_args) = @_;
  my $command;
  my $status;
  my $error = 0;
  my $mk_error = 0;
  my $error_txt;
  my $cmd_file = $OdeEnv::tempdir . $OdeEnv::dirsep . "ode_input.txt";

  $mk_error = 0;
  open( INPUT, ">$cmd_file" ) ||
    OdeInterface::logPrint( "Could not open file $cmd_file\n" );
  print( INPUT "mk -f varmod_upper_g.mk @mk_args > $OdeTest::tmpfile $OdeEnv::stderr_redir\n" );
  print( INPUT "exit\n" );
  close( INPUT );
  $command = "workon -rc $OdeTest::test_sb_rc -sb $OdeTest::test_sb_name "
            . "< $cmd_file >> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );
  if (defined( $OdeEnv::UNIX ))
  {
    open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
      warn ("Could not open file $OdeEnv::bldcurr\n");
    print( BLDLOG "Executing: mk -f varmod_upper_g.mk @mk_args; exit;\n" );
    close( BLDLOG );
  }
  # Copy contents of tempfile into build log file
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  #check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "workon or mk returned a non-zero exit code";
  }
  # Check if any tests failed
  if (!$mk_error && ( OdeFile::findInFile( "ODEMKERROR", $OdeTest::tmpfile ) 
      || (OdeFile::numPatternsInFile( 
          "ODEMKPASS", $OdeTest::tmpfile ) != 1)))
  {
    $mk_error = 1;
    $error_txt = "mk :G variable modifier error";
  }
  if ($mk_error)
  {
    # Test case failed
    $error = 1;
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                $testno, ":G variable modifier" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                $testno, ":G variable modifier" );
  }
  return( $error );
}

1;
