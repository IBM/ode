#########################################
#
# OdePath module
# - Path and file system releated utilities
# 
#########################################

package OdePath;

use OdeEnv;
use OdeUtil;
use OdeInterface;
use Cwd;

#########################################
#
# sub unixize
# - convert backslahses to forward slashes
#   in string for all non-Unix platforms
# - return
#   unixized string
# - arguments
#   str: string unixize
#########################################
sub unixize ( $ )
{
  my($str) = @_;
  if (! defined( $OdeEnv::Unix ))
  {
    $str =~ s/\\/\//g;
  }
  return( $str );
}

#########################################
#
# sub chgdir
# - change directory
#  differs from perl builtin function by
#  handling the case on OS/2 where a cd
#  is being done across drive letters.  In
#  this case, (i.e. cd'ing from c:\temp\dir
#  to o:\test), OS/2 will properly execute
#  the cd, but will still register the 
#  c:\temp\dir directory as "in use", since
#  it is the cwd of the c: drive.  Therefore
#  commands such as "deltree c:\temp"  will
#  fail, even if the "actual" cwd is o:\test.
#
#  This is resolved, for OS/2 only, by checking
#  if drives are being spanned by the chdir,
#  and if so, cd to the root directory of the
#  beginning drive before cd'ing to the new
#  drive.  The new cwd of the beginning drive
#  would be "c:\" in our example, leaving
#  lower directories available for deletion.
#
#  - returns 1 if successful, 0 if failure
#
#  - arguments
#  dirname - directory to cd to
# 
#########################################
sub chgdir ( $ )
{
  my($new_dir) = @_;
  my $curr_dir;
  my $root_dir;
  my $curr_drive;
  my $new_drive;

  if (defined( $OdeEnv::OS2 ))
  {
    if (isAbsolutePath( $new_dir ) )
    {
      $curr_dir = cwd();
      $new_drive = substr( $new_dir, 0, 1 );
      $curr_drive = substr( $curr_dir, 0, 1 );

      # cd'ing to a different drive
      if ($new_drive ne $curr_drive)
      {
        $root_dir = $curr_drive . ":\\";
	      OdeInterface::logPrint( "cd'ing to $root_dir\n" );
        chdir( $root_dir );
      }
    }
  }
  OdeInterface::logPrint( "cd'ing to $new_dir\n" );
  return( chdir( $new_dir ) );

}


#########################################
#
# sub isAbsolutePath
# - return 1 (true) if given path is absolute,
#   return empty string (false) if not.
#########################################
sub isAbsolutePath
{
  local $_ = shift if (@_);
  if (defined( NT ) or defined( OS2 ))
  {
    return m#^[a-z]:[\\/]#i;
  }
  else
  {
    return m#^/#;
  }
}


1;
