#################################################
#
# OdeInterface module
# - provide the print functions for the scripts
#
#################################################

package OdeInterface;

use OdeEnv;
use OdeUtil;
use IO::Tee;

#################################################
#
# sub printResult
# - print the result of the test with the
#   arguments
#
# - arguments:
#   result - the result of the test. 
#            can be "pass" or "fail"
#   type - type of the test performed
#          can be normal, regression, fvt
#   tool - the tool being tested
#   testno - test number
#   tdesc - optional argument
#           a brief description of the test
#
################################################
sub printResult( $$$$;$ )
{
  my ($result, $type, $tool, $testno, $tdesc) = @_;

  if (lc($result) eq "pass")
  {
    $result = "ODETESTPASS";
  }
  elsif (lc($result) eq "fail")
  {
    $result = "ODETESTFAIL";
  }

  if (lc($type) eq "normal")
  {
    $type = "NRL";
  }
  elsif (lc($type) eq "regression")
  {
    $type = "REG";
  }
  elsif (lc($type) eq "fvt")
  {
    $type = "FVT";
  }

  if ($tdesc ne "")
  {
    logPrint( "$result : $type : $tool : $type Test # $testno : $tdesc\n" );
  }
  else
  {
    logPrint( "$result : $type : $tool : $type Test # $testno\n" );
  }
}

#########################################
#
# sub logPrint
# - write specified string to STDOUT and
#   also to the bldcurr file.
#
# NOTE: This function may be modified in
# the future to do the following;
# - allow filename as argument since currently
#   we are assuming all output goes to bldcurr
#
# - arguments
#   str: string to print
#########################################
sub logPrint( $ )
{
  my($str) = @_;
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  my $tee = IO::Tee->new(\*STDOUT, \*BLDLOG);
  print( $tee $str);
  close( BLDLOG );
}

#################################################
#
# sub printError
# - to print an error message to STDOUT and also
#   to the bldcurr file
#
# - argument:
#   str - a string to be appended after 
#         ODETESTFAIL
#
#################################################
sub printError( $ )
{
  my($str) = @_;
  my $error_msg = "ODETESTFAIL : $str : actual tests not executed " .
                  "due to an internal error\n";
  open( BLDLOG, ">> $OdeEnv::bldcurr" ) ||
    warn ("Could not open file $OdeEnv::bldcurr\n");
  my $tee = IO::Tee->new(\*STDOUT, \*BLDLOG);
  print( $tee $error_msg);
  close( BLDLOG );
}

1;
