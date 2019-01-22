#########################################
#
# OdeUtil module
# - miscellaneous utility subroutines
# 
#########################################

package OdeUtil;

use OdeEnv;
use IO::Tee;
use Cwd;

#########################################
#
# sub getCurrentTime
# - return current system time in
#   hour:minute:second format
# - written due to certain perl versions
#   lacking full Time library modules
#
#########################################
sub getCurrentTime ()
{
  my @now     = localtime( time() );
  my $now_hour  = sprintf( "%2.2d", $now[2] );
  my $now_min   = sprintf( "%2.2d", $now[1] );
  my $now_sec   = sprintf( "%2.2d", $now[0] );
  my $rightnow  = "$now_hour:$now_min:$now_sec";

  return $rightnow;
}

#########################################
#
# sub getCurrentDate
# - return current system date in
#   month/day/year format
# - written due to certain perl versions
#   lacking full Time library modules
#
#########################################
sub getCurrentDate ()
{
  my @now     = localtime( time() );
  my $now_year  = sprintf( "%2.2d", $now[5] );
  my $now_month = sprintf( "%2.2d", $now[4]+1 );
  my $now_day   = sprintf( "%2.2d", $now[3] );
  my $today     = "$now_month/$now_day/$now_year";

  return $today;
}

#########################################
#
# sub parseCommandLine
# - parse command line
#   and establish global argument hash
#
#########################################
sub parseCommandLine (  )
{
  my $curr_argv;
  my $test_arg;
  my $test_key;
  my $test_val;
  my $curr_arg;
  my $curr_val;
  my $num_t;
  my $num_k;

  # initialize argument hash to default values
  %arghash = (
    release => "latest",
    build => "latest",
    usepath => 0,
    testlevel => "regression",
    numtests => "1",
    numkeep => "0",
    odeflags => "",
    debug => "off",
    tempdir => "",
    bbdir => "",
    logdir => "",
    odeexe => "",
    odeclass => "",
    oderules => "",
    bbjar => ""
  );

  foreach $curr_argv (@ARGV)
  {
    # split input on '=' sign  
    ($curr_arg, $curr_val) = split( /=/, $curr_argv, 2 );
    # making command line arguments case-insensitive
    $curr_arg = lc( $curr_arg );
    # curr_val only defined if '=' in input parameter
    if (defined( $curr_val ))
    {
      # loop through valid arguments
      while ( ($test_key, $test_val) = each %arghash )
      {
        if ($curr_arg eq $test_key)
        {
          # Assign new value to hash
          # Treat all non-ode command flags as lowercase
          if ($curr_arg ne "odeflags")
          {
            $curr_val = lc( $curr_val );
	  }
          $arghash{ $curr_arg } = $curr_val;
        }
      }
    }
  }

  # Validate input
  # - testlevel must be normal, regression, or fvt
  # - usepath will be used if defined with any non-zero value
  # - build/release can be anything
  $test_val = $arghash{ "testlevel" };
  if ($test_val)
  {
    if ( ($test_val ne "normal") && 
         ($test_val ne "fvt") && 
         ($test_val ne "regression") )
    {
      print("ERROR: testlevel must be \"normal\", \"regression\", or \"fvt\"\n");
      return( 1 );
    }
  }
  else
  {
    $test_val = "regression";
  }

  # Validate numtests/numkeep input
  # - numtests = total number of tests to run
  # - numkeep = total number of tests to consider when
  #   computing averages (best numkeep tests are kept)
  $num_t = $arghash{ "numtests" };
  $num_k = $arghash{ "numkeep" };
  if ($num_t <= 0)
  {
    print("ERROR: number of tests must be nonzero\n");
    return( 1 );
  }
  if ($num_k <= 0)
  {
    $arghash{ "numkeep" } = $arghash{ "numtests" };
  }
  if ($num_k > $num_t)
  {
    print("ERROR: number of tests to keep cannot be more");
    print( " than number of tests\n" );
    return( 1 );
  }
  if ($ENV{'WORKON'} == 1)
  {
    print("ERROR: scripts should not be run in a workon session\n");
    return( 1 );
  }

  
  # Other input parameters are not restricted at this time
  # - use at your own risk!

  return( 0 );

}

1;
