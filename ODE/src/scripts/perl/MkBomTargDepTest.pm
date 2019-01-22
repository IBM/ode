###########################################
#
# MkBomTargDepTest module
# - module to test mk bill of materials
#   target dependencies flag -B
#
###########################################

package MkBomTargDepTest;

use File::Path;
use OdeEnv;
use OdeFile;
use OdeTest;
use OdePath;
use OdeUtil;
use OdeInterface;
use strict;
use Cwd;

my $bom_u = "bom.u";
my $numComments = 6;   # number of comment lines in a BOM file
my @testLines;

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
  my $tfileo;
  my $tfile1c;
  my $tfile2c;
  my $to;
  my $t1c;
  my $t2c;
  my $mtime1;
  my $mtime2;
  my $mtime3;
  my $makefile_dir;
  my $makefile;
  my $dir = cwd();

  OdeInterface::logPrint( "in MkBomTargDepTest\n");
 
  ## Testing target dependencies

  ## locating the makefiles directory
  $makefile_dir = join( $OdeEnv::dirsep, (".", "makefiles", "dependencies") );
  if (! -d $makefile_dir)
  {
    $makefile_dir = join( $OdeEnv::dirsep, ($OdeEnv::bb_base, "src", "scripts",
                          "perl", "makefiles", "dependencies") );
    if (! -d $makefile_dir)
    {
      OdeInterface::printError( "mk : target dependencies" );
      OdeInterface::logPrint( 
          "Could not locate makefiles directory - $makefile_dir\n" );
      return( 1 );
    }
  }
                            
  $makefile = $makefile_dir . $OdeEnv::dirsep . "*"; 
  ## copying all the makefiles into the temp directory
  OdeEnv::runSystemCommand( "$OdeEnv::copy $makefile $OdeEnv::tempdir" );
  ## cd'ing to the temp directory having the makefiles
  OdePath::chgdir( $OdeEnv::tempdir );

  ## running mk -f colon1.mk
  ## testing the use of ":" in the makefile 

  ## target t.o, sources t1.c t2.c doesn't exist
  $tfileo = $OdeEnv::tempdir . $OdeEnv::dirsep . "t.o";
  $tfile1c = $OdeEnv::tempdir . $OdeEnv::dirsep . "t1.c";
  $tfile2c = $OdeEnv::tempdir . $OdeEnv::dirsep . "t2.c";
  $to = OdePath::unixize( $tfileo );
  $t1c = OdePath::unixize( $tfile1c );
  $t2c = OdePath::unixize( $tfile2c );
  OdeInterface::logPrint( 
                    "ODETEST: $tfileo, $tfile1c, $tfile2c don't exist\n" );
  @testLines = (
                1,
                $t1c . " :",
                1,
                $t2c . " :",
                3,
                $to . " : \\",
                $t1c . " \\",
                $t2c
                );
  $status = runMkTest( "colon1.mk", $tfileo, "changed", $tfile1c, "changed",
                       $tfile2c, "changed", "target dependencies \":\"", 1 ,
                       $numComments + 8 );
  if (!$error) { $error = $status; }

  ## source $tfile1c is modified recently
  OdeInterface::logPrint( "ODETEST: a source $tfile1c is recently modified\n" );
  OdeFile::touchFile( $tfileo, 200000 );
  OdeFile::touchFile( $tfile1c, 100000 );
  OdeFile::touchFile( $tfile2c, 200000 );
  @testLines = (
                2,
                $to . " : \\",
                $t1c
                );
  $status = runMkTest( "colon1.mk", $tfileo, "changed", $tfile1c,
            "unchanged", $tfile2c, "unchanged", "target dependencies \":\"", 2,
            $numComments + 3 );
  if (!$error) { $error = $status; }

  ## using -a when source $tfile1c is modified recently
  OdeInterface::logPrint( "ODETEST: using -a when source $tfile1c is recently modified\n" );
  OdeFile::touchFile( $tfileo, 200000 );
  OdeFile::touchFile( $tfile1c, 100000 );
  OdeFile::touchFile( $tfile2c, 200000 );
  @testLines = (
                1,
                $t1c . " :",
                1,
                $t2c . " :",
                3,
                $to . " : \\",
                $t1c . " \\",
                $t2c
                );
  $status = runMkTest( "colon1.mk", $tfileo, "changed", $tfile1c,
          "changed", $tfile2c, "changed", "target dependencies \":\"", 3,
          $numComments + 8, "-a" );
  if (!$error) { $error = $status; }

  ## source $tfile2c is modified recently
  OdeInterface::logPrint( "ODETEST: a source $tfile2c is recently modified\n" );
  OdeFile::touchFile( $tfileo, 200000 );
  OdeFile::touchFile( $tfile1c, 200000 );
  OdeFile::touchFile( $tfile2c, 100000 );
  @testLines = (
                2,
                $to . " : \\",
                $t2c
                );
  $status = runMkTest( "colon1.mk", $tfileo, "changed", $tfile1c,
           "unchanged", $tfile2c, "unchanged", "target dependencies \":\"", 4,
           $numComments + 3 );
  if (!$error) { $error = $status; }

  ## both the sources $tfile1c and $tfile2c modified recently
  OdeInterface::logPrint( 
      "ODETEST: sources $tfile1c, $tfile2c are recently modified\n" );
  OdeFile::touchFile( $tfileo, 200000 );
  OdeFile::touchFile( $tfile1c, 100000 );
  OdeFile::touchFile( $tfile2c, 100000 );
  @testLines = (
                3,
                $to . " : \\",
                $t1c . " \\",
                $t2c
                );
  $status = runMkTest( "colon1.mk", $tfileo, "changed", $tfile1c,
           "unchanged", $tfile2c, "unchanged", "target dependencies \":\"", 5,
           $numComments + 4 );
  if (!$error) { $error = $status; }

  ## both the sources $tfile1c and $tfile2c are up to date
  OdeInterface::logPrint( 
        "ODETEST: sources $tfile1c, $tfile2c are up to date\n" );
  OdeFile::touchFile( $tfileo, 200000 );
  OdeFile::touchFile( $tfile1c, 200000 );
  OdeFile::touchFile( $tfile2c, 200000 );
  @testLines = ();
  $status = runMkTest( "colon1.mk", $tfileo, "unchanged", $tfile1c,
            "unchanged", $tfile2c, "unchanged", "target dependencies \":\"", 6,
            $numComments );
  if (!$error) { $error = $status; }

  ## using -a when both the sources $tfile1c and $tfile2c are up to date
  OdeInterface::logPrint( "ODETEST: using -a when $tfile1c and $tfile2c are up to date\n" );
  @testLines = (
                1,
                $t1c . " :",
                1,
                $t2c . " :",
                3,
                $to . " : \\",
                $t1c . " \\",
                $t2c
                );
  $status = runMkTest( "colon1.mk", $tfileo, "changed", $tfile1c,
          "changed", $tfile2c, "changed", "target dependencies \":\"", 7,
          $numComments + 8, "-a" );
  if (!$error) { $error = $status; }

  ## target $tfileo is recently modified than its sources
  OdeInterface::logPrint( "ODETEST: target $tfileo is recently modified\n" );
  OdeFile::touchFile( $tfileo, 100000 );
  OdeFile::touchFile( $tfile1c, 200000 );
  OdeFile::touchFile( $tfile2c, 200000 );
  @testLines = ();
  $status = runMkTest( "colon1.mk", $tfileo, "unchanged", $tfile1c,
            "unchanged", $tfile2c, "unchanged", "target dependencies \":\"", 8,
            $numComments );
  if (!$error) { $error = $status; }

  ## using -a when target $tfileo is modified more recently than its sources
  OdeInterface::logPrint( "ODETEST: using -a when target $tfileo modified more recently than sources\n" );
  @testLines = (
                1,
                $t1c . " :",
                1,
                $t2c . " :",
                3,
                $to . " : \\",
                $t1c . " \\",
                $t2c
                );
  $status = runMkTest( "colon1.mk", $tfileo, "changed", $tfile1c,
          "changed", $tfile2c, "changed", "target dependencies \":\"", 9,
          $numComments + 8, "-a" );
  if (!$error) { $error = $status; }
  @delete_list = ($tfileo, $tfile1c, $tfile2c, $bom_u);
  OdeFile::deltree( \@delete_list );

  if ( $OdeEnv::WIN32 )
  {
    ## running mk -f colon1blank.mk
    ## testing the use of ":" in the makefile with embedded blanks in filenames
  
    ## target t.o, sources t1.c t2.c should not exist
    $tfileo = $OdeEnv::tempdir . $OdeEnv::dirsep . "t t.o";
    $tfile1c = $OdeEnv::tempdir . $OdeEnv::dirsep . "t 1.c";
    $tfile2c = $OdeEnv::tempdir . $OdeEnv::dirsep . "t 2.c";
    $to = OdePath::unixize( $tfileo );
    $t1c = OdePath::unixize( $tfile1c );
    $t2c = OdePath::unixize( $tfile2c );
    OdeInterface::logPrint( "ODETEST: -B test with embedded blanks in filenames\n" );
    ## removing temporary files
    @delete_list = ($tfileo, $tfile1c, $tfile2c);
    OdeFile::deltree( \@delete_list );
    if ((-f $tfileo) || (-f $tfile1c) || (-f $tfile2c))
    {
      OdeInterface::logPrint( "$tfileo or $tfile1c or $tfile2c not deleted\n" );
    }
    # note that filenames with blanks are not quoted in the bom.u file.
    @testLines = (
                  3,
                  $to . " : \\",
                  $t1c . " \\",
                  $t2c,
                  1,
                  $t1c . " :",
                  1,
                  $t2c . " :"
                  );
    $status = runMkTest( "colon1blank.mk", $tfileo, "changed", $tfile1c,
                         "changed", $tfile2c,
                         "changed", "dependencies - blanks in filenames \":\"",
                         1, $numComments + 8 );
    if (!$error) { $error = $status; }
  
    @delete_list = ($tfileo, $tfile1c, $tfile2c, $bom_u);
    OdeFile::deltree( \@delete_list );
  
  }


  ## cd'ing back to the current directory
  OdePath::chgdir( $dir );

  @delete_list = ($OdeEnv::tempdir . $OdeEnv::dirsep . "colon1.mk",
                  $OdeEnv::tempdir . $OdeEnv::dirsep . "colon1blank.mk",
                  $OdeTest::tmpfile);
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
#   makefile: the name of the makefile
#   tfileo, tfile1c, tfile2c: test files used in the makefile
#   tfileo_stamp, tfile1c_stamp, tfile2c_stamp: strings which
#      indicate if the respective test files are made. Can
#      be either changed or unchanged
#   tststr: the string to be output in the result
#   testno: test number
#   numLines: number of lines that should be in bom.u file
#   arg_list: the list of arguments to be appended to the command 
############################################################
sub runMkTest( $$$$$$$$$$@ )
{
  my( $makefile, $tfileo, $tfileo_stamp, $tfile1c, $tfile1c_stamp, 
      $tfile2c, $tfile2c_stamp, $tststr, $testno, $numLines,
      @arg_list ) = @_;
  my $status;
  my $mk_error = 0;
  my $command;
  my $error_txt;
  my $numFileLines;
  # Getting the last modification times of the test files
  my $mtime1 = (stat $tfileo)[9];
  my $mtime2 = (stat $tfile1c)[9];
  my $mtime3 = (stat $tfile2c)[9];
  $makefile = $OdeEnv::tempdir . $OdeEnv::dirsep . $makefile;
  if (! -f $makefile)
	{
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint( "Could not locate makefile - $makefile\n" );
    return( 1 );
	}

  # delete any bom.u file
  my @delete_list = ($bom_u);
  # remove any existing temporary files
  OdeFile::deltree( \@delete_list );

  $command = "mk -f $makefile -B $bom_u @arg_list > $OdeTest::tmpfile $OdeEnv::stderr_redir";
  $status = OdeEnv::runOdeCommand( $command );

  # test for existance of bom.u file.  There should always be one,
  # even if it only has comments.  The test of the file contents
  # will happen after return from this routine.
  if ( ! -f $bom_u )
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint(
                    "Could not locate bill-of-materials file: $bom_u\n" );
    return( 1 );
  }
  $numFileLines = OdeFile::numLinesInFile( $bom_u );
  if ( $numFileLines != $numLines )
  {
    OdeInterface::printError( "mk : $tststr" );
    OdeInterface::logPrint(
                    "$bom_u had $numFileLines lines; expected $numLines\n" );
    return( 1 );
  }

  # Copy output from output files to build logfile
  OdeFile::concatFiles( $OdeTest::tmpfile, $OdeEnv::bldcurr );
  OdeFile::concatFiles( $bom_u, $OdeEnv::bldcurr );
  # Check command exit code
  if ($status != 0)
  {
    $mk_error = 1;
    $error_txt = "mk returned a non-zero exit code";
  }

  if (!$mk_error && 
        (($tfileo_stamp eq "changed") && ((stat $tfileo)[9] == $mtime1)))
  {
    $mk_error = 1;
    $error_txt = "target $tfileo is not made";
  }
  if (!$mk_error && 
         (($tfileo_stamp eq "unchanged") && ((stat $tfileo)[9] != $mtime1)))
  {
    $mk_error = 1;
    $error_txt = "target $tfileo is made";
  }
  if (!$mk_error && 
        (($tfile1c_stamp eq "changed") && ((stat $tfile1c)[9] == $mtime2)))
  {
    $mk_error = 1;
    $error_txt = "source $tfile1c is not made";
  }
  if (!$mk_error && 
         (($tfile1c_stamp eq "unchanged") && ((stat $tfile1c)[9] != $mtime2)))
  {
    $mk_error = 1;
    $error_txt = "source $tfile1c is made";
  }
  if (!$mk_error && 
        (($tfile2c_stamp eq "changed") && ((stat $tfile2c)[9] == $mtime3)))
  {
    $mk_error = 1;
    $error_txt = "source $tfile2c is not made";
  }
  if (!$mk_error && 
         (($tfile2c_stamp eq "unchanged") && ((stat $tfile2c)[9] != $mtime3)))
  {
    $mk_error = 1;
    $error_txt = "source $tfile2c is made";
  }

  # check for valid lines in bom.u file
  if (@testLines > 0)
  {
    my $i = 0;
    my $j;
    my $jcount;
    my $flix;
    my $lineNumber;
    my $nextLine;
    my $dataLines = @testLines;
    TESTLINE:
    while ($i < $dataLines)
    {
      if ( @testLines[$i] =~ /^\d+$/ ) # is the line all digits
      {
        $jcount = @testLines[$i];
        if (++$i >= $dataLines)
        {
          $mk_error = 1;
          $error_txt = "Not enough lines in test data!";
          last TESTLINE;
        }
        $lineNumber = findInTrimmedFile( @testLines[$i], $bom_u );
        if (!$lineNumber)
        {
          $mk_error = 1;
          $error_txt = "Did not find line '" . @testLines[$i] .
                       "' in file $bom_u";
          last TESTLINE;
        }
        for ($j = 1; $j < $jcount; ++$j)
        {
          $lineNumber++;
          if (++$i >= $dataLines)
          {
            $mk_error = 1;
            $error_txt = "Not enough lines in test data!";
            last TESTLINE;
          }
          if ( @testLines[$i] =~ /^\d+$/ ) #is the data line all digits
          {
            $mk_error = 1;
            $error_txt = "Invalid test data format; testLines[$i] should not be integer.";
                         
            last TESTLINE;
          }
          $nextLine = findInTrimmedFile( @testLines[$i], $bom_u, $lineNumber );
          if ($nextLine != $lineNumber)
          {
            $mk_error = 1;
            $error_txt = "Did not find '" . @testLines[$i] .
                         "' as line $lineNumber in file $bom_u";
            last TESTLINE;
          }
        }
      }
      else
      {
        $mk_error = 1;
        $error_txt = "Invalid test data format; an integer was expected at testLines[$i].";
        last TESTLINE;
      }
      ++$i;
    }
  }

  if ($mk_error)
  {
    # Test Case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'}, "mk",
                                $testno, $tststr );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'}, "mk",
                                $testno, $tststr );
  }
  return $mk_error;
}


#########################################
#
# sub findInTrimmedFile
# - find a string in a file, returns 
#   the line number if the
#   string was found, zero if string was
#   not found.  This function is a little bit
#   different from OdeFile::findInFile because
#   it trims the leading and trailing blanks
#   from lines it reads from the file before
#   doing the comparison.  It assumes that
#   the string being searched for has also
#   been trimmed similarly.
#
# NOTE: this function is case insensitve
# as all input is treated as lowercase
# prior to checking.
#
# - arguments
#   str: string to find
#   filename: file to search
#   lineno: if specified, the function
#    matches the string at that particular
#    line in the file. Default is 0, which
#    means that the string can be found
#    anywhere in the file.
#########################################
sub findInTrimmedFile ( $$@ )
{
  my($str, $filename, $lineno) = @_;
  my $found_str = 0;
  my $line;
  my $status;
  $lineno ||= 0;
  
  $str = lc( $str );

  open( FILEHND, "< $filename" ) ||
    OdeInterface::logPrint( "Could not open file $filename\n" );

  while (!eof( FILEHND ) )
  {
    $line = lc( readline( *FILEHND ) );
    $found_str++;
    if ( $line =~ /\S/ ) # if there is some non-whitespace
    {
      $line =~ s/^\s*(.+?)\s*$/$1/; # remove leading and trailing whitespace
    }
    else
    {
      next;
    }
    ## skip the lines until the specified line no. is reached
    if ($lineno && ($found_str < $lineno))
    {
      next;
    }
    if ($line eq $str)
    {
      close( FILEHND );
      return $found_str;
    }
    ## quit if the line no. is crossed
    if ($found_str == $lineno)
    {
      close( FILEHND );
      return 0;
    }
  }
    
  close( FILEHND );
  return 0;
}

1;
