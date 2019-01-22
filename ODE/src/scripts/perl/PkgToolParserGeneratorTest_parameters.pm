#################################################
#
# PkgToolParserGeneratorTest_parameters module
# - module to test parameters for
#   pkgtool parsergeneratorinitiator
#
#################################################

package PkgToolParserGeneratorTest_parameters;

use File::Path;
use OdeEnv;
use OdeFile;
use OdeTest;
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
  my $error = 0;
  my $dir = cwd();
  my $cmf_file;
  my $tfile1;
  my $tfile2;
  my $testfile;
  my $command;
  my $status;
  my $tdir;
  my @check_list;
  my @pcdstr_list;
  my @pkgstr_list;
  my @delete_list;
  my $pkgFamily;

  OdeInterface::logPrint( "in PkgToolParserGeneratorTest_parameters\n" );

  if (defined( $OdeEnv::AIX ))
  {
    $cmf_file = "cmf_pp_aix";
    $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "pcd.pd";
    $tfile2 = $OdeEnv::tempdir . $OdeEnv::dirsep . "tfilepkg.il";
    $pkgFamily = "mkinstall" ;
  }
  elsif (defined( $OdeEnv::WIN32 ))
  {
    $cmf_file = "cmf_pp_aix";
    $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "pcd.pd";
    $tfile2 = $OdeEnv::tempdir . $OdeEnv::dirsep . "tfilepkg.il";
    $pkgFamily = "ispe" ;
  }
  elsif (defined( $OdeEnv::SOLARIS ) || defined( $OdeEnv::SCO ) ||
         defined( $OdeEnv::IRIX ))
  {
    $cmf_file = "cmf_pp_solaris";
    $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "prototype";
    $tfile2 = $OdeEnv::tempdir . $OdeEnv::dirsep . "pkginfo";
    $pkgFamily = "pkgmk" ;
  }
  elsif (defined( $OdeEnv::HP ))
  {
    $cmf_file = "cmf_pp_hp";
    $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "pcd.psf";
    $pkgFamily = "swpackage" ;
  }
  elsif (defined( $OdeEnv::MVS ))
  {
    $cmf_file = "cmf_pp_mvs";
    $tfile1 = $OdeEnv::tempdir . $OdeEnv::dirsep . "pcd.mvs";
    $pkgFamily = "mvs" ;
  }

  $testfile = $OdeEnv::tempdir . $OdeEnv::dirsep . $cmf_file;
  if (! -f $testfile)
  {
    OdeInterface::printError( 
                 "PkgToolParserGeneratorTest_parameters : parameters test" );
    OdeInterface::logPrint( "Could not locate cmf file - $testfile\n" );
    return( 1 );
  }

  $tdir = $OdeEnv::tempdir . $OdeEnv::dirsep . "test";
  if (-d $tdir)
  {
    OdeFile::deltree( $tdir );
  }
  mkpath( $tdir );
  if (! -d $tdir )
  {
    OdeInterface::logPrint( "$tdir not created\n" );
  }
  @delete_list = ( $tfile1, $tfile2 );
  OdeFile::deltree( \@delete_list );

  ## cd'ing to the temp directory
  OdePath::chgdir( $OdeEnv::tempdir );

  ## Test 1: running parsergeneratorinitiator on a cmf file
  $command = "java  -DCONTEXT=$OdeEnv::machine -DTOSTAGE=$OdeEnv::tempdir " .
             "-DPKG_CONTROL_DIR=$OdeEnv::tempdir -DPKG_CMF_FILE=$cmf_file " .
             "-DPKG_CLASS=IPP -DPKG_TYPE=USER -DPKGFAMILY=$pkgFamily " .
             "com.ibm.ode.pkg.parserGenerator.ParserGeneratorInitiator " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  if (defined( $OdeEnv::AIX ))
  {
    @pcdstr_list = ( $tfile1, "product tdirpkg", "fileset tfilepkg", 
                      "filesets=[tfilepkg]", "ship_path=\"$OdeEnv::tempdir\"", 
                      "inslist=tfilepkg.il" );
    @pkgstr_list = ( $tfile2, "F test test 755 ./tfile" );
    @check_list = ( \@pcdstr_list, \@pkgstr_list );
  }
  elsif (defined( $OdeEnv::SOLARIS ) || defined( $OdeEnv::SCO ) ||
         defined( $OdeEnv::IRIX ))
  {
    @pcdstr_list = ( $tfile1, "i pkginfo ", 
                "1 F tfilepkg ./tfile=$OdeEnv::tempdir/./tfile 755 test test" );
    @pkgstr_list = ( $tfile2, "PKG=tdirpkg", "NAME=tdirpkg", "DESC=test",
                     "CATEGORY=application", "CLASSES=  tfilepkg  " );
    @check_list = ( \@pcdstr_list, \@pkgstr_list );
  }
  elsif (defined( $OdeEnv::HP ))
  {
    @pcdstr_list = ( $tfile1, "depot", "tag tdirpkg", "category application",
                     "fileset", "is_reboot false", 
                     "directory $OdeEnv::tempdir/./=./", "file tfile tfile" );
    @check_list = ( \@pcdstr_list );
  }
  elsif (defined( $OdeEnv::MVS ))
  {
    @pcdstr_list = ( $tfile1, "<type> ipp", "<applid> tester", 
                     "<copyright> ./crap.txt", "<cpydate> 1234", 
                     "<fesn> 1234567", "<function> tdirpkg",
                     "<description> test", "<delete> abcd123",
     "./tfile NONE<type>ipp <distlib>.<distname>tfile<parttype>mod<permissions>755<userId>test<groupId>test" );
    @check_list = ( \@pcdstr_list );
  }
  $status = runTest( $command, 1, \@check_list );
  if (!$error) { $error = $status; }

  OdeFile::deltree( \@delete_list );

  ## Test 2: running the same command again but with IPP and USER set 
  ## in mixed case
  ## should work as case 1
  $command = "java -DCONTEXT=$OdeEnv::machine -DTOSTAGE=$OdeEnv::tempdir " .
             "-DPKG_CONTROL_DIR=$OdeEnv::tempdir -DPKG_CMF_FILE=$cmf_file " .
             "-DPKG_CLASS=iPp -DPKG_TYPE=uSEr -DPKGFAMILY=$pkgFamily " .
             "com.ibm.ode.pkg.parserGenerator.ParserGeneratorInitiator " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = runTest( $command, 2, \@check_list );
  if (!$error) { $error = $status; }

  OdeFile::deltree( \@delete_list );

  ## Test 3: running parsergeneratorinitiator on a cmf file with altered TOSTAGE
  ## the content in the generated files should have slightly changed on all
  ## platforms except MVS
  $command = "java -DCONTEXT=$OdeEnv::machine -DTOSTAGE=$tdir " .
             "-DPKG_CONTROL_DIR=$OdeEnv::tempdir -DPKG_CMF_FILE=$cmf_file " .
             "-DPKG_CLASS=IPP -DPKG_TYPE=USER -DPKGFAMILY=$pkgFamily " .
             "com.ibm.ode.pkg.parserGenerator.ParserGeneratorInitiator " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  if (defined( $OdeEnv::AIX ))
  {
    @pcdstr_list = ( $tfile1, "product tdirpkg", "fileset tfilepkg", 
                      "filesets=[tfilepkg]", "ship_path=\"$tdir\"", 
                      "inslist=tfilepkg.il" );
    @pkgstr_list = ( $tfile2, "F test test 755 ./tfile" );
    @check_list = ( \@pcdstr_list, \@pkgstr_list );
  }
  elsif (defined( $OdeEnv::SOLARIS ) || defined( $OdeEnv::SCO ) ||
         defined( $OdeEnv::IRIX ))
  {
    @pcdstr_list = ( $tfile1, "i pkginfo", 
                     "1 F tfilepkg ./tfile=$tdir/./tfile 755 test test" );
    @pkgstr_list = ( $tfile2, "PKG=tdirpkg", "NAME=tdirpkg", "DESC=test",
                     "CATEGORY=application", "CLASSES=tfilepkg" );
    @check_list = ( \@pcdstr_list, \@pkgstr_list );
  }
  elsif (defined( $OdeEnv::HP ))
  {
    @pcdstr_list = ( $tfile1, "depot", "tag tdirpkg", "category application",
		     "fileset", "is_reboot false", 
		     "directory $tdir/./=./", "file tfile tfile" );
    @check_list = ( \@pcdstr_list );
  }
  elsif (defined( $OdeEnv::MVS ))
  {
    @pcdstr_list = ( $tfile1, "<type> ipp", "<applid> tester", 
                     "<copyright> ./crap.txt", "<cpydate> 1234", 
                     "<fesn> 1234567", "<function> tdirpkg",
                     "<description> test", "<delete> abcd123",
     "./tfile NONE<type>ipp <distlib>.<distname>tfile<parttype>mod<permissions>755<userId>test<groupId>test" );
    @check_list = ( \@pcdstr_list );
  }
  $status = runTest( $command, 3, \@check_list );
  if (!$error) { $error = $status; }

  OdeFile::deltree( \@delete_list );

  ## Test 4: running parsergeneratorinitiator on a cmf file with 
  ## PKG_CONTROL_DIR set to a different directory
  ## files are generated in the directory specified by PKG_CONTROL_DIR
  $command = "java -DCONTEXT=$OdeEnv::machine -DTOSTAGE=$OdeEnv::tempdir " .
             "-DPKG_CONTROL_DIR=$tdir -DPKG_CMF_FILE=$cmf_file " .
             "-DPKG_CLASS=IPP -DPKG_TYPE=USER -DPKGFAMILY=$pkgFamily " .
             "com.ibm.ode.pkg.parserGenerator.ParserGeneratorInitiator " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  if (defined( $OdeEnv::AIX ))
  {
    $tfile1 = $tdir . $OdeEnv::dirsep . "pcd.pd";
    $tfile2 = $tdir . $OdeEnv::dirsep . "tfilepkg.il";
    @pcdstr_list = ( $tfile1, "product tdirpkg", "fileset tfilepkg", 
                      "filesets=[tfilepkg]", "ship_path=\"$OdeEnv::tempdir\"", 
                      "inslist=tfilepkg.il" );
    @pkgstr_list = ( $tfile2, "F test test 755 ./tfile" );
    @check_list = ( \@pcdstr_list, \@pkgstr_list );
  }
  elsif (defined( $OdeEnv::SOLARIS ) || defined( $OdeEnv::SCO ) ||
         defined( $OdeEnv::IRIX ))
  {
    $tfile1 = $tdir . $OdeEnv::dirsep . "prototype";
    $tfile2 = $tdir . $OdeEnv::dirsep . "pkginfo";
    @pcdstr_list = ( $tfile1, "i pkginfo", 
                "1 F tfilepkg ./tfile=$OdeEnv::tempdir/./tfile 755 test test" );
    @pkgstr_list = ( $tfile2, "PKG=tdirpkg", "NAME=tdirpkg", "DESC=test",
                     "CATEGORY=application", "CLASSES=tfilepkg" );
    @check_list = ( \@pcdstr_list, \@pkgstr_list );
  }
  elsif (defined( $OdeEnv::HP ))
  {
    $tfile1 = $tdir . $OdeEnv::dirsep . "pcd.psf";
    @pcdstr_list = ( $tfile1, "depot", "tag tdirpkg", "category application",
                     "fileset", "is_reboot false", 
                     "directory $OdeEnv::tempdir/./=./", "file tfile tfile" );
    @check_list = ( \@pcdstr_list );
  }
  elsif (defined( $OdeEnv::MVS ))
  {
    $tfile1 = $tdir . $OdeEnv::dirsep . "pcd.mvs";
    @pcdstr_list = ( $tfile1, "<type> ipp", "<applid> tester", 
                     "<copyright> ./crap.txt", "<cpydate> 1234", 
                     "<fesn> 1234567", "<function> tdirpkg",
                     "<description> test", "<delete> abcd123",
     "./tfile NONE<type>ipp <distlib>.<distname>tfile<parttype>mod<permissions>755<userId>test<groupId>test" );
    @check_list = ( \@pcdstr_list );
  }
  $status = runTest( $command, 4, \@check_list );
  if (!$error) { $error = $status; }

  OdeFile::deltree( \@delete_list );

  $testfile = $OdeEnv::tempdir . $OdeEnv::dirsep . "testfile";
  OdeFile::touchFile( $testfile );

  ## Test 5: running parsergeneratorinitiator on a cmf file with 
  ## TOSTAGE set to a file
  ## should not generate an error, should generate files in PKG_CONTROL_DIR
  $command = "java -DCONTEXT=$OdeEnv::machine -DTOSTAGE=$testfile " .
             "-DPKG_CONTROL_DIR=$tdir -DPKG_CMF_FILE=$cmf_file " .
             "-DPKG_CLASS=IPP -DPKG_TYPE=USER -DPKGFAMILY=$pkgFamily " .
             "com.ibm.ode.pkg.parserGenerator.ParserGeneratorInitiator " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  if (defined( $OdeEnv::AIX ))
  {
    @pcdstr_list = ( $tfile1, "product tdirpkg", "fileset tfilepkg", 
                      "filesets=[tfilepkg]", "ship_path=\"$testfile\"", 
                      "inslist=tfilepkg.il" );
    @pkgstr_list = ( $tfile2, "F test test 755 ./tfile" );
    @check_list = ( \@pcdstr_list, \@pkgstr_list );
  }
  elsif (defined( $OdeEnv::SOLARIS ) || defined( $OdeEnv::SCO ) ||
         defined( $OdeEnv::IRIX ))
  {
    @pcdstr_list = ( $tfile1, "i pkginfo", 
                "1 F tfilepkg ./tfile=$testfile/./tfile 755 test test" );
    @pkgstr_list = ( $tfile2, "PKG=tdirpkg", "NAME=tdirpkg", "DESC=test",
                     "CATEGORY=application", "CLASSES=tfilepkg" );
    @check_list = ( \@pcdstr_list, \@pkgstr_list );
  }
  elsif (defined( $OdeEnv::HP ))
  {
    @pcdstr_list = ( $tfile1, "depot", "tag tdirpkg", "category application",
                     "fileset", "is_reboot false", 
                     "directory $testfile/./=./", "file tfile tfile" );
    @check_list = ( \@pcdstr_list );
  }
  elsif (defined( $OdeEnv::MVS ))
  {
    @pcdstr_list = ( $tfile1, "<type> ipp", "<applid> tester", 
                     "<copyright> ./crap.txt", "<cpydate> 1234", 
                     "<fesn> 1234567", "<function> tdirpkg",
                     "<description> test", "<delete> abcd123",
     "./tfile NONE<type>ipp <distlib>.<distname>tfile<parttype>mod<permissions>755<userId>test<groupId>test" );
    @check_list = ( \@pcdstr_list );
  }
  $status = runTest( $command, 5, \@check_list );
  if (!$error) { $error = $status; }

  OdeFile::deltree( \@delete_list );

  ## Test 6: running parsergeneratorinitiator on a cmf file with 
  ## PKG_CONTROL_DIR set to a file
  ## should generate an error on UNIX machines
  $command = "java -DCONTEXT=$OdeEnv::machine -DTOSTAGE=$OdeEnv::tempdir " .
             "-DPKG_CONTROL_DIR=$tfile1 -DPKG_CMF_FILE=$cmf_file " .
             "-DPKG_CLASS=IPP -DPKG_TYPE=USER -DPKGFAMILY=$pkgFamily " .
             "com.ibm.ode.pkg.parserGenerator.ParserGeneratorInitiator " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  if (!defined( $OdeEnv::WIN32 ))
  {
    $status = runErrorTest( $command, 6 );
  }
  else
  {
    @check_list = ();
    $status = runTest( $command, 6, \@check_list );
  }
  if (!$error) { $error = $status; }
  
  @delete_list = ( $tfile1, $tfile2, $tdir, $testfile );
  OdeFile::deltree( \@delete_list );

  ## Test 7: running parsergeneratorinitiator on a cmf file with 
  ## PKG_TYPE set to OFFICIAL
  ## should complain about missing PKG_API_URL
  $command = "java -DCONTEXT=$OdeEnv::machine -DTOSTAGE=$OdeEnv::tempdir " .
             "-DPKG_CONTROL_DIR=$OdeEnv::tempdir -DPKG_CMF_FILE=$cmf_file " .
             "-DPKG_CLASS=IPP -DPKG_TYPE=OFFICIAL -DPKGFAMILY=$pkgFamily " .
             "com.ibm.ode.pkg.parserGenerator.ParserGeneratorInitiator " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = runErrorTest( $command, 7 );
  if (!$error) { $error = $status; }

  OdeFile::deltree( \@delete_list );

  ## Test 8: running parsergeneratorinitiator on a cmf file with 
  ## invalid PKG_API_URL 
  ## should generate an error
  $command = "java -DCONTEXT=$OdeEnv::machine -DTOSTAGE=$OdeEnv::tempdir " .
             "-DPKG_CONTROL_DIR=$OdeEnv::tempdir -DPKG_CMF_FILE=$cmf_file " .
             "-DPKG_CLASS=IPP -DPKG_TYPE=OFFICIAL -DPKG_API_URL=crap " .
             "-DPKGFAMILY=$pkgFamily " .
             "com.ibm.ode.pkg.parserGenerator.ParserGeneratorInitiator " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = runErrorTest( $command, 8 );
  if (!$error) { $error = $status; }

  OdeFile::deltree( \@delete_list );

  ## Test 9: running parsergeneratorinitiator on a cmf file with 
  ## PKG_CLASS=SP, PKG_TYPE=OFFICIAL
  ## should complain about missing PKG_FIX_STRATEGY
  $command = "java -DCONTEXT=$OdeEnv::machine -DTOSTAGE=$OdeEnv::tempdir " .
             "-DPKG_CONTROL_DIR=$OdeEnv::tempdir -DPKG_CMF_FILE=$cmf_file " .
             "-DPKG_CLASS=SP -DPKG_TYPE=OFFICIAL -DPKGFAMILY=$pkgFamily " .
             "com.ibm.ode.pkg.parserGenerator.ParserGeneratorInitiator " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = runErrorTest( $command, 9 );
  if (!$error) { $error = $status; }

  OdeFile::deltree( \@delete_list );

  ## Test 10: running parsergeneratorinitiator on a cmf file with 
  ## PKG_CLASS=SP, PKG_TYPE=OFFICIAL, PKG_FIX_STRATEGY=CUMULATIVE and 
  ## invalid PKG_API_URL
  ## should complain about invalid PKG_API_URL
  $command = "java -DCONTEXT=$OdeEnv::machine -DTOSTAGE=$OdeEnv::tempdir " .
             "-DPKG_CONTROL_DIR=$OdeEnv::tempdir -DPKG_CMF_FILE=$cmf_file " .
             "-DPKG_CLASS=SP -DPKG_TYPE=OFFICIAL -DPKGFAMILY=$pkgFamily " .
             "-DPKG_FIX_STRATEGY=cumulative -DPKG_API_URL=crap " .
             "com.ibm.ode.pkg.parserGenerator.ParserGeneratorInitiator " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = runErrorTest( $command, 10 );
  if (!$error) { $error = $status; }

  OdeFile::deltree( \@delete_list );

  ## Test 11: running parsergeneratorinitiator on a cmf file with 
  ## PKG_CLASS=SP, PKG_TYPE=OFFICIAL, PKG_FIX_STRATEGY=refresh
  ## should complain about missing PKG_API_URL
  $command = "java -DCONTEXT=$OdeEnv::machine -DTOSTAGE=$OdeEnv::tempdir " .
             "-DPKG_CONTROL_DIR=$OdeEnv::tempdir -DPKG_CMF_FILE=$cmf_file " .
             "-DPKG_CLASS=SP -DPKG_TYPE=OFFICIAL " .
             "-DPKG_FIX_STRATEGY=refresh -DPKGFAMILY=$pkgFamily " .
             "com.ibm.ode.pkg.parserGenerator.ParserGeneratorInitiator " .
             ">> $OdeEnv::bldcurr $OdeEnv::stderr_redir";
  $status = runErrorTest( $command, 11 );
  if (!$error) { $error = $status; }

  OdeFile::deltree( \@delete_list );
  OdePath::chgdir( $dir );
  return( $error );

}

#################################################
#
# sub runTest
# - to run the command and check the result
#
# - arguments
#   command: the command to be run
#   testno: test number
#   check_list: a list of lists containing files 
#               and strings in those respective files 
#################################################
sub runTest( $$$ )
{
  my ($command, $testno, $check_list) = @_;
  my $error_txt;
  my $status;
  my $pkgtools_error = 0;
  my $check_count;
  my $check_index = 0;
  my $list_count;
  my $list_index = 0;
  my $file;
  my $ip_str;
  my $found_str;
  my $tmpstr;
  
  $status = OdeEnv::runOdeCommand( $command );
  # Check command exit code
  if ($status)
  {
    $pkgtools_error = 1;
    $error_txt = "parsergeneratorinitiator returned a non-zero exit code";
  }
  $check_count = scalar( @$check_list );
  while (!defined( $OdeEnv::WIN32 ) && 
         !$pkgtools_error && ($check_index < $check_count))
  {
    $list_count = scalar( @{$check_list->[$check_index]} );
    $file = $check_list->[$check_index][0];
    if (! -f $file)
    {
      $pkgtools_error = 1;
      $error_txt = "$file not created";
    }
    ## if there are some strings to look for in the above file,
    ## read each line, chop of the spaces in it and compare with
    ## the given string formatted to not contain spaces
    if (!$pkgtools_error && ($list_count > 1))
    {
      $list_index = 1; 
      while (!$pkgtools_error && ($list_index < $list_count))
      {
        $found_str = 0;
        open( FILE, "< $file" ) ||
            OdeInterface::logPrint( "Could not open file $file\n" );
        $ip_str = $check_list->[$check_index][$list_index];
        $ip_str =~ s/\s+//g;
        $ip_str =~ s/\;//;
        while (!eof( FILE ))
        {
          $tmpstr = readline( *FILE );
          $tmpstr =~ s/\s+//g;
          $tmpstr =~ s/\;//;
          if ($tmpstr eq $ip_str)
          {
            $found_str = 1;
            last;
          }
        }
        if (!$found_str)
        {
          $pkgtools_error = 1;
          $error_txt = 
          $error_txt = "could not find string " .
                       "$check_list->[$check_index][$list_index] in $file";
        } 
        close( FILE );
        $list_index++;
      }
    } 
    $check_index++;
  }
  if ($pkgtools_error) 
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'},
                  "pkgtools", $testno, "parsergeneratorinitiator parameters" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'},
                  "pkgtools", $testno, "parsergeneratorinitiator parameters" );
  }
  return $pkgtools_error;
}

#################################################
#
# sub runErrorTest
# - to run the command and check if an error
#   occured
#
# - arguments
#   command: the command to be run
#   testno: test number
#################################################
sub runErrorTest( $$ )
{
  my ($command, $testno) = @_;
  my $status;
  my $error_txt;
  my $pkgtools_error = 0;

  $status = OdeEnv::runOdeCommand( $command );
  # Check command exit code
  if (!$status)
  {
    $pkgtools_error = 1;
    $error_txt = "parsergeneratorinitiator returned a zero exit code";
  }
  if ($pkgtools_error) 
  {
    # Test case failed
    OdeInterface::printResult( "fail", $OdeUtil::arghash{'testlevel'},
                  "pkgtools", $testno, "parsergeneratorinitiator parameters" );
    OdeInterface::logPrint( "Test Failed: $error_txt\n" );
  }
  else
  {
    OdeInterface::printResult( "pass", $OdeUtil::arghash{'testlevel'},
                  "pkgtools", $testno, "parsergeneratorinitiator parameters" );
  }
  return $pkgtools_error;
}

1;
