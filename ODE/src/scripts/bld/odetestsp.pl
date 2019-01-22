# Run "mksb -rev" on a service provider to determine
# early if there might be problems.
$cmd = "mksb -rev";
if ($^O eq MSWin32)
{
  $status = system( "cmd", "/c", $cmd );
  $status = $status / 256;
  if ($status != 1)
  {
    print "Service provider failed for `$^O' \n";
    exit(1);
  }
  print "Service provider succeded for `$^O' \n";
  exit(0);
}
elsif ($^O eq os2)
{
  $status = system( "cmd", "/c", $cmd );
  $status = $status / 256;
  if ($status != 1)
  {
    print "Service provider failed for `$^O' \n";
    exit(1);
  }
  print "Service provider succeded for `$^O' \n";
  exit(0);
} # $context eq "x86_os2_4"

$status = system( $cmd );
$status = $status / 256;
if ($status != 1)
{
  print "Service provider failed for `$^O', rc=$status \n";
  exit(1);
}
print "Service provider succeded for `$^O' \n";
exit(0);

