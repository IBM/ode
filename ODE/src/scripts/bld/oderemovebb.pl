#!/usr/local/bin/perl
$nfsbase = "/build/";
$dfsbase = "/ode/build/";
open( RELNUMFILE, "< release.num" );
$RELEASE_NUM=readline( *RELNUMFILE );
close( RELNUMFILE );

#remove end of line character from the strings read from file
chop $RELEASE_NUM;

# Get the environment variable and set to a local variable
$SIMULATE = $ENV{'SIMULATE'};

$nfssbrc = $nfsbase . $RELEASE_NUM . "/.sbrc";
$dfssbrc = $dfsbase . $RELEASE_NUM . "/.sbrc";

@sbes=`mkbb -rc $dfssbrc -list`;
$num_keep = 2;
$num_lines = $#sbes + 1;

$sb_idx = 0;
$new_sb_idx = 0;
while ($sb_idx < $num_lines) 
{
  # get sandbox name
  ($crap, $sbname) = split( /:/, $sbes[ $sb_idx ] );
  $sbname =~ s/ //g;
  chop( $sbname );
  $sb_idx++;

  # get sandbox base
  ($crap, $sbdir)  = split( /:/, $sbes[ $sb_idx ] );
  if ($crap eq "  Variable Base")
  {
    $sb_idx++;
    ($crap, $sbdir)  = split( /:/, $sbes[ $sb_idx ] );
  }
  $sbdir =~ s/ //g;
  chop( $sbdir );
  $sb_idx++;

  $sb_abspath{ $sbname } = $sbdir . '/' . $sbname ;
  $sb_list[ $new_sb_idx ] = $sbname;
  $new_sb_idx++;
} 

$num_sbes = @sb_list;
$num_to_delete = $num_sbes - $num_keep;
if ($num_to_delete <= 0)
{
  print "Currently `$num_sbes' backing builds and need to keep `$num_keep'\n";
  print "No backing builds need to be deleted\n";
  exit;
}

print "List of current backing builds (in order of oldest to newest)\n";
$sb_idx = 0;
foreach $sb (sort comp_sb @sb_list)
{
  $sb_ordered[$sb_idx] = $sb;
  print "Sandbox => $sb \n";
  $sb_idx++;
}

$sb_idx = 0;
while ($num_to_delete > 0)
{
  $cmd = "mkbb -rc $dfssbrc -undo $sb_ordered[$sb_idx] -auto";
  print $cmd . "\n";
  if ($SIMULATE ne "yes")
  {
    $rc = system( $cmd );
  }
  if ($rc != 0)
  {
    $have_error = 1;
    print "Sandbox deletion failed on DFS for $sb_ordered[$sb_idx]\n";
  }
  $cmd = "mkbb -rc $nfssbrc -undo $sb_ordered[$sb_idx] -auto";
  print $cmd . "\n";
  if ($SIMULATE ne "yes")
  {
    $rc = system( $cmd );
  }
  if ($rc != 0)
  {
    $have_error = 1;
    print "Sandbox deletion failed on NFS for $sb_ordered[$sb_idx]\n";
  }

  $sb_idx++;
  $num_to_delete--;  
}

if ($have_error)
{
  exit 1;
}

# This subroutine will compare sandbox names like:
#   23, 23a, 23f, 24, 25
# and return in the order above ("oldest" sandbox first)
sub comp_sb
{
  $_ = $a;
  $sba_num = $a;
  $sba_sub = "";
  if (/[a-z,A-Z]*$/)
  {
    $sba_num = $a;
    $sba_num =~ s/[a-z,A-Z]*$//;
    $sba_sub =~ s/^$sba_num//;
  }
  $_ = $b;
  $sbb_num = $b;
  $sbb_sub = "";
  if (/[a-z,A-Z]*$/)
  {
    $sbb_num =~ s/[a-z,A-Z]*$//;
    $sbb_sub =~ s/^$sbb_num//;
  }
  if ($sba_num eq $sbb_num)
  {
    return ($sba_sub cmp $sbb_sub);
  }
  return ($sba_num <=> $sbb_num);
}
