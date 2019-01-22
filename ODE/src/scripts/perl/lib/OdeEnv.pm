#########################################
#
# OdeEnv module
# - environment and/or platform-specific
#   subroutines
# 
#########################################

package OdeEnv;

use OdeUtil;
use OdeInterface;
use OdePath;
use File::Path;
use File::Copy;
use Cwd;

#########################################
#
# sub runSystemCommand
# - execute a system command with necessary
#   shell prepends, and return exit code from
#   system command.
#
# - arguments
#   command: system command to run
#
# Note: since BEGINLIBPATH on OS/2 can't
#  be set in normal ways, the "set BEGINLIBPATH"
#  command is prepended to every OS/2 command
#  that has not specified usepath
#
#########################################
sub runSystemCommand ( $ )
{
  my ($command) = @_;
  my $status;

  OdeInterface::logPrint("Executing: $command\n");
 
  if (defined($UNIX))
  {
    if (defined($OS400))
    {
      system( "rm /QSYS.LIB/" . $OS400_OUTPUTDIR . ".LIB/*" );
      $status = system( "export OUTPUTDIR=" . $OS400_OUTPUTDIR .
                        " && " . $command );
    }
    else
    {
      $status = system( $command );
    }
  }
# Must prepend shell and set BEGLINLIBPATH for OS/2 commands  
  elsif ( defined($OS2) && !$OdeUtil::arghash{'usepath'} )
  {
    $command = "set $libvar=$pathdir && " . $command;
    $status = system( $system_shell, $system_shell_runflag, $command );
  }
# Must prepend shell for NT and OS/2 commands  
  else
  {
    $status = system( $system_shell, $system_shell_runflag, $command );
  } 

  return $status;
}

#########################################
#
# sub runOdeCommand
# - execute an ODE command with necessary
#   shell prepends, and return exit code from
#   system command.
# - must add necessary user defined odeflags
#   and debug flags, then call runSystemCommand
#
# - arguments
#   command: ode command to run
#   useflags (optional): boolean to use odeflags
#     default is true
#
#########################################
sub runOdeCommand ( $;$ )
{
  my ($command, $useflags) = @_;
  my $newcommand;
  my $odetool;
  my $options;
  my $index;
  my $status;

  # Get Ode tool name - index of first space
  ($odetool, $options) = split( / /, $command, 2 );

  # Add any odeflags to command if useflags is not false
  if ( ($OdeUtil::arghash{'odeflags'}) && ($useflags ne "false") )
  {
    $newcommand = "$odetool $OdeUtil::arghash{'odeflags'}";
  }   
  else
  {
    $newcommand = $odetool;
  }
  # Add any debug flags to command
  if ($OdeUtil::arghash{'debug'} eq "on")
  {
    if ($odetool eq "mk")
    {
      $newcommand = "$newcommand -dA";
    }
    else
    {
      $newcommand = "$newcommand -debug";
    }
  }  
  # Add options back on  
  $newcommand = "$newcommand $options";

  # Now call runSystemCommand
  $status = runSystemCommand( $newcommand );

  # On Windows/OS2, we need a little sleep after workon and build
  # to allow the slow OS to close the files associated with those
  # processes.
  if (!defined($UNIX) && ($odetool eq "workon" || $odetool eq "build"))
  {
    sleep( 3 );
  }

  return $status;
}

#########################################
#
# sub initPlatform
# - initialize many variables used througout
#   the ODE perl scripts.  Many of these
#   are platform-specific.
#
#########################################
sub initPlatform ()
{
  my $OSTYPE = $^O;

  $tempdir = "/tmp";
  $stderr_redir = "2>&1";
  $system_shell = $ENV{'SHELL'};
  $system_shell_runflag = $ENV{'SHELL_RUNFLAG'};

  if ($OSTYPE eq "aix")
  {
    if (substr( `uname -m`, 0, 4 ) eq "ia64")
    {
      $AIX_IA64 = 1;
      $machine = "ia64_aix_5";
      $libvar = LD_LIBRARY_PATH;
    }
    else
    {
      $AIX_PPC = 1;
      $machine = "rios_aix_4";
      $libvar = LIBPATH;
    }
    $UNIX = 1;
    $AIX = 1;
  }
  elsif ($OSTYPE eq "hpux")
  {
    $machine = "hp9000_ux_10";
    $libvar = SHLIB_PATH;
    $UNIX = 1;
    $HP = 1;
  }
  elsif ($OSTYPE eq "solaris")
  {
    if (substr( `uname -m`, 0, 3 ) eq "i86")
    {
      $machine = "x86_solaris_2";
      $SOLARIS_X86 = 1;
    }
    else
    {
      $machine = "sparc_solaris_2";
      $SOLARIS_SPARC = 1;
    }
    $libvar = LD_LIBRARY_PATH;
    $UNIX = 1;
    $SOLARIS = 1;
  }
  elsif ($OSTYPE eq "MSWin32")
  {
    if (!defined($system_shell_runflag))
    {
      $system_shell_runflag = "/C";
    }
    if ($ENV{'OS'})
    {
      $machine = "x86_nt_4";
      $WINNT = 1;
      if (!defined($system_shell))
      {
        $system_shell = "CMD.EXE";
      }
    }
    else
    {
      $machine = "x86_95_4";
      $WIN95 = 1;
      if (!defined($system_shell))
      {
        $system_shell = "COMMAND.COM";
      }
      $stderr_redir = "";
    }
    $WIN32 = 1;
    $CASE_INSENSITIVE_FILE_NAMES = 1;
    $tempdir = "c:\\temp";
  }
  elsif ($OSTYPE eq "os2")
  {
    $machine = "x86_os2_4";
    $libvar = BEGINLIBPATH;
    $OS2 = 1;
    $CASE_INSENSITIVE_FILE_NAMES = 1;
    $tempdir = "c:\\temp";
    if (!defined($system_shell))
    {
      $system_shell = "CMD.EXE";
    }
    if (!defined($system_shell_runflag))
    {
      $system_shell_runflag = "/C";
    }
  }
  elsif ($OSTYPE eq "os390")
  {
    $machine = "mvs390_oe_2";
    $libvar = LIBPATH;
    $UNIX = 1;
    $MVS = 1;
  }
  elsif (substr( $OSTYPE, 0, 5 ) eq "linux")
  {
    if (substr( `uname -m`, 0, 3 ) eq "ppc")
    {
      $LINUX_PPC = 1;
      $machine = "ppc_linux_2";
    }
    elsif (substr( `uname -m`, 0, 5 ) eq "sparc")
    {
      $LINUX_SPARC = 1;
      $machine = "sparc_linux_2";
    }
    elsif (substr( `uname -m`, 0, 4 ) eq "ia64")
    {
      $LINUX_IA64 = 1;
      $machine = "ia64_linux_2";
    }
    elsif (substr( `uname -m`, 0, 5 ) eq "alpha")
    {
      $LINUX_ALPHA = 1;
      $machine = "alpha_linux_2";
    }
    elsif (substr( `uname -m`, 0, 5 ) eq "s390x")
    {
      $LINUX_ZSERIES = 1;
      $machine = "zseries_linux_2";
    }
    elsif (substr( `uname -m`, 0, 4 ) eq "s390")
    {
      $LINUX_S390 = 1;
      $machine = "s390_linux_2";
    }
    else
    {
      $LINUX_X86 = 1;
      $machine = "x86_linux_2";
    }
    $libvar = LD_LIBRARY_PATH;
    $UNIX = 1;
    $LINUX = 1;
  }
  elsif ($OSTYPE eq "netbsd")
  {
    $machine = "x86_netbsd_1";
    $libvar = LD_LIBRARY_PATH;
    $UNIX = 1;
    $NETBSD = 1;
    $ANY_BSD = 1;
  }
  elsif ($OSTYPE eq "openbsd")
  {
    $machine = "x86_openbsd_2";
    $libvar = LD_LIBRARY_PATH;
    $UNIX = 1;
    $OPENBSD = 1;
    $ANY_BSD = 1;
  }
  elsif ($OSTYPE eq "freebsd")
  {
    $machine = "x86_freebsd_3";
    $libvar = LD_LIBRARY_PATH;
    $UNIX = 1;
    $FREEBSD = 1;
    $ANY_BSD = 1;
  }
  elsif ($OSTYPE eq "dynixptx")
  {
    $machine = "x86_ptx_4";
    $libvar = LD_LIBRARY_PATH;
    $UNIX = 1;
    $DYNIXPTX = 1;
  }
  elsif ($OSTYPE eq "beos")
  {
    $machine = "x86_beos_4";
    $libvar = LIBRARY_PATH;
    $UNIX = 1;
    $BEOS = 1;
    $tempdir = "/boot/var/tmp";
  }
  elsif ($OSTYPE eq "svr4" || $OSTYPE eq "sco3.2v5.0")
  {
    $machine = "x86_sco_7";
    $libvar = LD_LIBRARY_PATH;
    $UNIX = 1;
    $SCO = 1;
  }
  elsif ($OSTYPE eq "irix")
  {
    $machine = "mips_irix_6";
    $libvar = LD_LIBRARY_PATH;
    $UNIX = 1;
    $IRIX = 1;
  }
  elsif ($OSTYPE eq "dec_osf")
  {
    $machine = "alpha_tru64_5";
    $libvar = LD_LIBRARY_PATH;
    $UNIX = 1;
    $TRU64 = 1;
    $tempdir = "/cluster/members/member0/tmp";
  }
  elsif ($OSTYPE eq "os400")
  {
    $machine = "as400_os400_4";
    $libvar = LD_LIBRARY_PATH;
    $UNIX = 1;
    $OS400 = 1;
    $OS400_OUTPUTDIR = "ODETEST";
    $system_shell = "/usr/bin/qsh";
  }
  else
  {
    OdeInterface::logPrint( "no dice - your OS is not supported\n" );
    exit(1);
  }

  if (!defined($system_shell))
  {
    $system_shell = "/bin/sh";
  }
  if (!defined($system_shell_runflag))
  {
    $system_shell_runflag = "-c";
  }

  ## dfsbase is a path to ode binaries in ODE build environment. Will be used
  ## if odeexe is not defined as a command line or an environment variable
  ## bb_base is a path to location of backing build in ODE build environment
  ## odeexe is a path to ode binaries in users environment. Should be set
  ## in the command line or in the environment
  ## bbjar is a path to the location of bbexample and rules zip files. Defaults
  ## to zip file locations in ODE build environment

  ## checking if bbdir is specified in the command line or in the environment
  if ( $OdeUtil::arghash{'bbdir'})
  {
    $bbex_dir = $OdeUtil::arghash{'bbdir'};
  }
  elsif ($ENV{'BBDIR'})
  {
    $bbex_dir = $ENV{'BBDIR'};
  }
  if ($bbex_dir)
  {
    if ( substr( $bbex_dir, length( $bbex_dir ) - 1, 1 ) eq $dirsep) 
    {
      chop( $bbex_dir );  # Remove trailing dirsep if present
    } 
  }

  ## set dfs and backing build locations and other commands ####
  if (defined( $UNIX ))
  {
    $dirsep = "/";
    $pathsep = ":";
    $copy = "cp -r";
    $pwd = "pwd";
    $list_env = "env";
    $obj_suff = ".o";
    $prog_suff = "";
    if (!defined($ENV{'HOME'}))
    {
      die("ERROR: HOME not defined. Exiting ...\n");
    }

    if (defined( $MVS ))
    {
      my $_hostname = `uname -n`;
      chop( $_hostname );
      if ($_hostname eq "MVSC")
      {
        # set env vars for install pass on MVSC
        $ENV{'_C89_PLIB_PREFIX'} = "CEE.V1R5M0";
        $ENV{'_C89_PVERSION'} = 0x11080000;
        $ENV{'_C89_CVERSION'} = 0x21030000;
      }
    }

    # use ode version supplied by command line input
    $dfsbase="/ode/build/" . $OdeUtil::arghash{'release'} .
             $dirsep . $OdeUtil::arghash{'build'};
    $bb_base=$dfsbase;
    # bbuild_base is used by createBbExample to locate bbexample.zip
    $bbuild_base="/ode/build/" . $OdeUtil::arghash{'release'} . $dirsep .
                 $OdeUtil::arghash{'build'} . $dirsep . "inst.images" . 
                 $dirsep . "ship";
  }
  else
  {
    $dirsep = "\\";
    $pathsep = ";";
    $pwd = "cd";
    $list_env = "set";
    $obj_suff = ".obj";
    $prog_suff = ".exe";

    if (defined($ENV{'TEMP'}))
    {
      $tempdir = $ENV{'TEMP'};
    }

    if (defined( $WIN32 ))
    {
      $copy = "xcopy /E /Q /I";
    }
    else  # OS/2
    {
      $copy = "xcopy /E /S";
    }
    # use ode version supplied by command line input
    $dfsbase="O:\\build\\" . $OdeUtil::arghash{'release'} 
              . $dirsep . $OdeUtil::arghash{'build'};
    $bb_base=$dfsbase;
    # bbuild_base is used by createBbExample to locate bbexample.zip
    $bbuild_base="O:\\build\\" . $OdeUtil::arghash{'release'}
                 . $dirsep . $OdeUtil::arghash{'build'} . $dirsep
                 . "inst.images" . $dirsep . "ship";
    # assign HOME to c: if not defined
    if (!defined($ENV{'HOME'}))
    {
      $ENV{'HOME'} = "C:";
    }
  }

  $homedir = $ENV{'HOME'};
  if ( substr( $homedir, length( $homedir ) - 1, 1 ) ne $dirsep) 
  {
    $homedir .= $dirsep;  # Add trailing dirsep if necessary
  } 
  $homedir .= $machine;  # Add machine directory
  if (! -d $homedir)
  {
    print("creating path $homedir..\n");
    mkpath( $homedir );
  }
  $homedir .= $dirsep;  # Add trailing dirsep

  # Log files
  ## checking if logdir is specified in the command line or in the environment
  if ( $OdeUtil::arghash{'logdir'})
  {
    $logdir = $OdeUtil::arghash{'logdir'};
  }
  elsif ($ENV{'LOGDIR'})
  {
    $logdir = $ENV{'LOGDIR'};
  }
  else
  {
    $logdir = $homedir . "logs";
  }
  # Create test log directory
  if (! -d $logdir)
  {
    print("creating path $logdir..\n");
    mkpath( $logdir );
  }
  if (! -W $logdir)
  {
    die("ERROR: $logdir not writable. Exiting ...\n");
  }
  # Since OS/2 can't make a path with trailing slash, we add it here 
  $logdir = $logdir . $dirsep;
  # Path to and name of current log file
  $bldcurr = $logdir . "$machine.testlog";
  # Path to and name of previous log file
  $bldprev = $logdir . "$machine.testlog.prev";
  
  # Set up tempdir
  if ( $OdeUtil::arghash{'tempdir'} ) 
  { 
    $tempdir = $OdeUtil::arghash{'tempdir'};
  }
  elsif ($ENV{'TEMPDIR'})
  {
    $tempdir = $ENV{'TEMPDIR'};
  }
  if ( substr( $tempdir, length( $tempdir ) - 1, 1 ) ne $dirsep) 
  {
    $tempdir .= $dirsep;  # Add trailing dirsep if necessary
  } 
  $tempdir .= "odetest_$$";  # Add ode test dir with process id
  if (! -d $tempdir)
  {
    print("creating path $tempdir..\n");
    mkpath( $tempdir );
  }
  else
  {
    print("Cannot execute test scripts!\n");
    die("Temporary directory $tempdir already exists. Exiting ...\n");
  }

  # build string will be the letter "b" followed by
  # one or more digits or alpha with any number of periods
  # changed 20030919 - Bonnie Lee to support 20030915.0300 build names.

  my $build_string = 'b[a-zA-Z0-9\.]+';

  my $file_prefix;
  my $opref = $OdeUtil::arghash{'release'} < 3.0 ? "odei" : "ode" ;

  if ($OdeUtil::arghash{'release'} eq "latest")
  {
    $file_prefix = "%release_name%_" . $build_string;
  }
  else
  {
    $file_prefix = $opref . $OdeUtil::arghash{'release'} . "_" . $build_string;
  }

  ## pick any file which looks like odei2.5_b123_bbexample.zip
  ## and odei2.5_b123_rules.zip
  if (defined( $MVS ))
  {
    $bbzipexpr = $file_prefix . "_bbexample_ebcdic.zip";
    $ruleszipexpr = $file_prefix . "_rules_ebcdic.zip";
  }
  else
  {
    $bbzipexpr = $file_prefix . "_bbexample.zip";
    $ruleszipexpr = $file_prefix . "_rules.zip";
  }
  $toolszipexpr = $file_prefix . "_tools.jar";
  ## overwrite bbuild_base if bbjar is defined in the environment or in the
  ## command line
  if ( $OdeUtil::arghash{'bbjar'} )
  {
    $bbuild_base = $OdeUtil::arghash{'bbjar'};
  }
  elsif ($ENV{'BBJAR'})
  {
    $bbuild_base = $ENV{'BBJAR'};
  }

  ## check if oderules is defined in the environment or in the command line
  $rules_dir = "";
  if ( $OdeUtil::arghash{'oderules'} )
  {
    $rules_dir = $OdeUtil::arghash{'oderules'};
  }
  elsif ($ENV{'ODERULES'})
  {
    $rules_dir = $ENV{'ODERULES'};
  }

  # Set up log files
  if (-f "$bldprev")
  {
    unlink( $bldprev );
  }
  if (-f "$bldcurr")
  {
    copy($bldcurr, $bldprev);
  }
}



#########################################
#
# sub setPaths
# - will set paths to ode binaries based
#   on value of build, unless no_toolbase
#   is set, in which case any system calls
#   to ode commands will use what paths are
#   in the user's environment.
#
#########################################
sub setPaths ()
{ 
  my $tfile = $tempdir . $dirsep . "tfile";
  my $tvar;
  my $setcommand;
  if ( !$OdeUtil::arghash{'usepath'} )
  {
    ## use ode binaries from odeexe if defined in the command line
    ## or in the environment
    if ($OdeUtil::arghash{'odeexe'})
    {
      $pathdir = $OdeUtil::arghash{'odeexe'};
    }
    elsif ($ENV{'ODEEXE'})
    {
      $pathdir = $ENV{'ODEEXE'};
    }
    else
    {
      $pathdir = join( $dirsep, ($dfsbase, "inst.images", $machine, "bin") );
    }
    # verify path to ode binaries
    if (! -d $pathdir)
    {
      OdeInterface::logPrint( 
        "ERROR: specified build directory $pathdir not valid\n");
      return( 1 );
    }
    ## set environment variable CLASSPATH to odeclass if defined in the
    ## command line or in the environment
    if ($OdeUtil::arghash{'odeclass'})
    {
      $classdir = $OdeUtil::arghash{'odeclass'};
    }
    elsif ($ENV{'ODECLASS'})
    {
      $classdir = $ENV{'ODECLASS'};
    }
    else
    {
      $classdir = join( $dirsep, ($dfsbase, "inst.images", "ship") );
    }
    $tvar = OdeFile::findFileInDir( $toolszipexpr, $classdir );
    ## if zip file for tools exist in classdir, add it to classdir
    if ($tvar != 1)
    {
      $classdir .= $dirsep . $tvar;
    }
    # set environment variables
    if (defined( $libvar ))
    {
    # libvar may not be retrieved using ENV, hence the round-about method
      if (!defined( $UNIX ))
      {
        $setcommand = "set $libvar > $tfile";
        $status = system( $system_shell, $system_shell_runflag, $setcommand );
      }
      else
      {
        $setcommand = "echo \$$libvar > $tfile";
        $status = system( $setcommand );
      }
      open( LIBVAR, "< $tfile" ) ||
       warn( "Could not open $tfile\n" );
      $tvar = readline( *LIBVAR );
      close( LIBVAR );
      # chopping variable name, "=", leading and trailing spaces from tvar
      $tvar =~ s/$libvar//g;
      $tvar =~ s/=//g;
      $tvar =~ s/^\s+//;
      $tvar =~ s/\s+$//;
      $pathdir .= $pathsep . $tvar;
      $ENV{$libvar} = $pathdir;
      OdeFile::deltree( $tfile ); 
    }
    $ENV{'PATH'} = $pathdir . $pathsep . $ENV{'PATH'};
    $ENV{'CLASSPATH'} = $classdir . $pathsep . $ENV{'CLASSPATH'};
  }
  if ($bbex_dir)
  {
    if (defined( $UNIX ))
    {
      if (substr( $bbex_dir, 0, 1 ) ne $dirsep) 
      {
        OdeInterface::logPrint( "$bbex_dir should be an absolute path\n" );
        return(1);
      }
    }
    else
    {
      if (!(substr( $bbex_dir, 0, 1 ) =~ /[a-zA-Z]/) ||
                 (substr( $bbex_dir, 1, 1 ) ne ":"))
      {
        my $tstr = "$bbex_dir should be an absolute path starting with a ";
        $tstr .= "drive letter followed by a :";
        OdeInterface::logPrint( "$tstr\n" );
        return(1);
      }
    }
  }     
  if ($bbex_dir && (! -d $bbex_dir))
  {
    OdeInterface::logPrint( 
          "ERROR: specified bbexample directory does not exist\n" );
    return(1);
  }
  return( 0 );
}

###############################################
#
# sub setPlBase
# - will parse the command line, initialize
#   the platform-specific and global variables,
#   and set up the required paths required
#   for .pl files to run.
#   Called by all the .pl files except testbld.pl
#   to do the initial setup
###############################################
sub setPlBase()
{
  my $status;
  my $nowdate;
  my $nowtime;

  #### Parse input command line ####
  $status = OdeUtil::parseCommandLine( );
  if ($status) { die "Exiting ...\n" };

  ## Initialize all platform-specific variables ####
  initPlatform();

  ## Set up global test variables ###
  OdeTest::setTestVars();

  ## Set paths to ode binaries ####
  $status = setPaths();
  if ($status) 
  { 
    OdeFile::deltree( $tempdir );
    die "Exiting ...\n" 
  }

  ## Write current time and date to log files
  $nowdate = OdeUtil::getCurrentDate();
  $nowtime = OdeUtil::getCurrentTime();
  open( BLDLOG, "> $OdeEnv::bldcurr" );
  print( BLDLOG "Test started at: $nowtime on $nowdate\n\n" );
  close( BLDLOG );

  ## create bbexample in bbdir if bbdir is specified else create in 
  ## home/<machine>. If bbexample already exists, it is not replaced
  if ($bbex_dir)
  {
    if (! -d $bbex_dir . $dirsep . "bbexample" . $dirsep . "src" .
             $dirsep . "bin")
    {
      createBbExample( $bbex_dir );
    }
  }
  elsif (! -d $homedir . $dirsep . "bbexample" . $dirsep . "src" . 
              $dirsep . "bin")
  {
    createBbExample( $homedir );
  }
}

######################################
#
# sub createBbExample
# - will create the bbexample in 
#   mybbdir
#
# - arguments
#   mybbdir: directory where bbexample
#    is to be created
######################################
sub createBbExample( $ )
{
  my $bb_rc;
  my ($mybbdir) = @_;
  # chop trailing dirsep of mybbdir if present
  if (substr( $mybbdir, length( $mybbdir ) - 1, 1 ) eq $dirsep) 
  {
    chop( $mybbdir );
  }
  my $bbexdir = $mybbdir;
  my $tmpvar;
  my $mybbsrcdir;
  my $bbzip;
  my $ruleszip;
  my $bbzipdir;
  my $ruleszipdir;
  my $rules_dir_files;
  my $ret_val;
  my $dir = cwd();

  if (! -W $bbexdir)
  {
    OdeInterface::logPrint( "ERROR: $bbexdir not writable\n");
    OdeFile::deltree( $tempdir );
    die "Exiting ...\n";
  }
  # verify path to bbexample.zip
  if (! -d $bbuild_base)
  {
    OdeInterface::logPrint(
      "ERROR: specified zip file directory $bbuild_base not valid\n");
    OdeFile::deltree( $tempdir );
    die "Exiting ...\n";
  }
  ## Create backing build ####
  $mybbdir .= $dirsep . "bbexample";
  $bb_rc = $mybbdir . $dirsep . ".sbrc";
  $retval = runOdeCommand( 
       "mkbb -rc $bb_rc -dir $bbexdir -m $machine bbexample" );
  if ($retval) 
  { 
    OdeFile::deltree( $tempdir );
    die("mkbb failed! Exiting ...\n"); 
  }
  OdeInterface::logPrint( "created backing build ...\n" );

  $bbzip = OdeFile::findFileInDir( $bbzipexpr, $bbuild_base );
  if ($bbzip == 1)
  {
    OdeFile::deltree( $tempdir );
    OdeFile::deltree( $bbexdir . $dirsep . "bbexample" );
    die( "zipped bbexample \'$bbzipexpr\' not found to copy. Exiting ...\n" );
  }
  $ruleszip = OdeFile::findFileInDir( $ruleszipexpr, $bbuild_base );
  if ($ruleszip == 1)
  {
    OdeFile::deltree( $tempdir );
    OdeFile::deltree( $bbexdir . $dirsep . "bbexample" );
    die( "zipped rules not found to copy. Exiting ...\n" );
  }

  ## delete bbexample zip file if already exists
  $tmpvar = $bbexdir . $dirsep . $bbzip;
  if (-f $tmpvar)
  {
    if (defined( $UNIX ))
    {
      $retval = runSystemCommand( "chmod -R +w $tmpvar" );
      if ($retval) 
      { 
        OdeFile::deltree( $tempdir );
        OdeFile::deltree( $bbexdir . $dirsep . "bbexample" );
        die("chmod failed! Exiting ...\n"); 
      }
    }
    else
    {
      $retval = runSystemCommand( "attrib -R $tmpvar" );
      if ($retval) 
      { 
        OdeFile::deltree( $tempdir );
        OdeFile::deltree( $bbexdir . $dirsep . "bbexample" );
        die("attrib failed! Exiting ...\n"); 
      }
    }
    OdeFile::deltree( $tmpvar );
  }

  ## Copy zip files for bbexample and rules ####
  $bbzipdir = $bbuild_base . $dirsep . $bbzip;
  runSystemCommand( "$copy $bbzipdir $bbexdir" );
  if (! -f $tmpvar)
  {
    OdeFile::deltree( $tempdir );
    OdeFile::deltree( $bbexdir . $dirsep . "bbexample" );
    die("copying of zip file failed! Exiting ...\n"); 
  }
  $mybbsrcdir = $mybbdir . $dirsep . "src";
  $ruleszipdir = $bbuild_base . $dirsep . $ruleszip;
  runSystemCommand( "$copy $ruleszipdir $mybbsrcdir" );
  if (! -f $mybbsrcdir . $dirsep . $ruleszip)
  {
    OdeFile::deltree( $tempdir );
    OdeFile::deltree( $bbexdir . $dirsep . "bbexample" );
    OdeFile::deltree( $bbexdir . $dirsep . "bbzip" );
    die("copying of zip file failed! Exiting ...\n"); 
  }

  ##unjar zip files
  OdePath::chgdir( $mybbsrcdir );
  if (defined( $OS400 ))
  {
    $dearc = "pax -rf";
  }
  else
  {
    $dearc = "jar -xf";
  }

  $retval = runSystemCommand( "$dearc $ruleszip" );
  if ($retval) 
  { 
    OdeFile::deltree( $mybbdir );
    OdeFile::deltree( $tempdir );
    die("unjaring of $ruleszip failed! Exiting ...\n"); 
  }
  OdePath::chgdir( $bbexdir );
  $retval = runSystemCommand( "$dearc $bbzip" );
  if ($retval) 
  { 
    OdeFile::deltree( $mybbdir );
    OdeFile::deltree( $tempdir );
    die("unjaring of $bbzip failed! Exiting ...\n"); 
  }
  OdeInterface::logPrint( "unjared files into bbexample ...\n" );

  ## Check if user has updated rules files ##
  if ( $rules_dir ne "" )
  {
    ## Exit if given directory is invalid ####
    if (! -d $rules_dir)
    {
      OdeInterface::logPrint(
        "ERROR: specified directory $rules_dir not valid\n");
      OdeFile::deltree( $tempdir );
      die "Exiting ...\n";
    }

    OdePath::chgdir( $mybbsrcdir . $dirsep . "rules_mk" );
    $rules_dir_files = $rules_dir . $dirsep . "*.mk";

    ## Copy updated rules files for bbexample and rules ####
    $bbzipdir = $bbuild_base . $dirsep . $bbzip;
    runSystemCommand( "$copy $rules_dir_files ." );
  }

  OdePath::chgdir( $dir );

  if (defined( $UNIX ))
  {
    $retval = runSystemCommand( "chmod -R +w $mybbsrcdir" );
    if ($retval) 
    { 
      OdeFile::deltree( $tempdir );
      die("chmod failed! Exiting ...\n"); 
    }
    $retval = runSystemCommand( "chmod -R +w $mybbdir" );
    if ($retval) 
    { 
      OdeFile::deltree( $tempdir );
      die("chmod failed! Exiting ...\n"); 
    }
  }

  OdeFile::deltree( $bbexdir . $dirsep . $bbzip );
  OdeFile::deltree( $mybbsrcdir . $dirsep . $ruleszip );
}

1;
