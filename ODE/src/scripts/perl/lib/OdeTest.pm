#########################################
#
# OdeTest module
# - testing subroutines
# 
#########################################

package OdeTest;

use OdeEnv;
use OdeFile;
use OdeUtil;
use File::Basename;

#########################################
#
# sub validateSandbox
# - validate sandbox by checking that all
#   of the required files and directories
#   have been created.  returns 0 if sandbox
#   is valid, 1 if sandbox is not valid
#
# - arguments: 
#   sb - full pathname of sandbox to validate
#   rc - full pathname of rc file of sandbox
#   error_txt - error text to display
#
#########################################
sub validateSandbox( $$$ )
{
  my ($sb, $rc, $error_txt) = @_;
  my $sb_exp_dir = $sb . $OdeEnv::dirsep . "export";
  my $sb_obj_dir = $sb . $OdeEnv::dirsep . "obj";
  my $sb_src_dir = $sb . $OdeEnv::dirsep . "src";
  my $sb_inst_dir = $sb . $OdeEnv::dirsep . "inst.images";
  my $sb_tools_dir = $sb . $OdeEnv::dirsep . "tools";
  my $sb_rcf_dir = $sb . $OdeEnv::dirsep . "rc_files";
  my $sb_conf = $sb . $OdeEnv::dirsep . "rc_files" . 
                $OdeEnv::dirsep . "sb.conf";
  my $sb_name = basename( $sb );
  my $sb_error = 0;

  if ( !(-d $sb))
  {
    $sb_error = 1;
    $$error_txt = "directory $sb does not exist";
  }
  if ( !$sb_error && !(-f $sb_conf))
  {
    $sb_error = 1;
    $$error_txt = "configuration file $sb_conf does not exist";
  }
  if ( !$sb_error && !(-f $rc))
  {
    $sb_error = 1;
    $$error_txt = "rc file $rc does not exist";
  }
  if ( !$sb_error && !(OdeFile::findInFile( $sb_name, $rc )))
  {
    $sb_error = 1;
    $$error_txt = "rc file $rc does not contain reference to $sb_name";
  }
  if ( !$sb_error && !(-d $sb_exp_dir))
  {
    $sb_error = 1;
    $$error_txt = "directory $sb_exp_dir does not exist";
  }
  if ( !$sb_error && !(-d $sb_obj_dir))
  {
    $sb_error = 1;
    $$error_txt = "directory $sb_obj_dir does not exist";
  }
  if ( !$sb_error && !(-d $sb_src_dir))
  {
    $sb_error = 1;
    $$error_txt = "directory $sb_src_dir does not exist";
  }
  if ( !$sb_error && !(-d $sb_inst_dir))
  {
    $sb_error = 1;
    $$error_txt = "directory $sb_inst_dir does not exist";
  }
  if ( !$sb_error && !(-d $sb_tools_dir))
  {
    $sb_error = 1;
    $$error_txt = "directory $sb_tools_dir does not exist";
  }
  if ( !$sb_error && !(-d $sb_rcf_dir))
  {
    $sb_error = 1;
    $$error_txt = "directory $sb_rcf_dir does not exist";
  }
   
  return( $sb_error );
}


#########################################
#
# sub setTestVars
# - define common variables used across multiple
#   test scripts
#
#########################################
sub setTestVars ()
{

  # Define test variables used for all ode tests
  if (defined( $OdeEnv::bbex_dir))
  {
    $test_bbdir = $OdeEnv::bbex_dir . $OdeEnv::dirsep . "bbexample";
  }
  else
  {
    $test_bbdir = $OdeEnv::homedir . "bbexample"; # BBDIR
  }
  $test_sbdir = $OdeEnv::tempdir;  # SBDIR
  $test_sb_name = "odetestsb0";  # ODESB
  $test_sb1_name = "odetestsb1";  # ODESB
  $test_rc = "odetestrc";  # RC
  # added these
  $test_sb = $test_sbdir . $OdeEnv::dirsep . $test_sb_name;
  $test_sb1 = $test_sbdir . $OdeEnv::dirsep . $test_sb1_name;
  $test_sb_conf = $test_sb . $OdeEnv::dirsep . 
                  "rc_files" . $OdeEnv::dirsep . "sb.conf";
  $test_sb1_conf = $test_sb1 . $OdeEnv::dirsep . "rc_files" . 
                   $OdeEnv::dirsep . "sb.conf";
  $test_sb_rc = $test_sbdir . $OdeEnv::dirsep . $test_rc;
  $tmpfile = $test_sbdir . $OdeEnv::dirsep . "tempfile.txt";
  # sandbox directories
  $test_sb_obj = $test_sb . $OdeEnv::dirsep . "obj" . 
                 $OdeEnv::dirsep . $OdeEnv::machine;
  $test_sb_export = $test_sb . $OdeEnv::dirsep . "export" . 
                 $OdeEnv::dirsep . $OdeEnv::machine;
  $test_sb_inst = $test_sb . $OdeEnv::dirsep . "inst.images" . 
                 $OdeEnv::dirsep . $OdeEnv::machine;
  $test_sb_tools = $test_sb . $OdeEnv::dirsep . "tools" . 
                 $OdeEnv::dirsep . $OdeEnv::machine;
  $test_sb_src = $test_sb . $OdeEnv::dirsep . "src";

  return( 0 );
}

#########################################
#
# sub getMakefile
# - returns a path to a specified makefile
#  first looks in the current directory
#  for the makefile, then will look
#  in the base (src/scripts/perl/makefiles)
#  directory.
#
# arguments: filename - makefile to find
#
# returns makefile, or empty string if
# makefile is not found
#########################################

sub getMakefile( $ )
{
  my ($filename) = @_;
  my $makefile;
  my ($basedir, $scripts_dir);

  # Look in curdir first for makefile
  $makefile = join( $OdeEnv::dirsep, ("." , "makefiles" , $filename));
  if (-f $makefile) { return( $makefile ) }

  # Look on DFS
  $basedir = join( $OdeEnv::dirsep, 
                ($OdeEnv::bb_base, "src", "scripts", "perl", "makefiles") );
  $makefile = $basedir . $OdeEnv::dirsep . $filename;
  if (-f $makefile) { return( $makefile ) }

  # Look in local scripts dir
  $scripts_dir = join( $OdeEnv::dirsep, 
                ($OdeEnv::bbex_dir, "scripts", "perl")  );
  $makefile = join($OdeEnv::dirsep, 
                ($scripts_dir, "makefiles" , $filename));
  if (-f $makefile) { return( $makefile ) }

  # Searches failed; return null string
  return( "" );
}

1;
