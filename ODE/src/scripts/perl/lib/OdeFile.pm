#########################################
#
# OdeFile module
# - subroutines relating to File operations
# 
#########################################

package OdeFile;

use OdeEnv;
use OdeUtil;
use OdeInterface;
use File::Path;
use File::Copy;
use Carp;
use DirHandle ();

#########################################
#
# sub findInFile
# - find a string in a file, returns 
#   the line number if the
#   string was found, zero if string was
#   not found
#
# NOTE: this function is case insensitve
# as all input is treated as lowercase
# prior to checking; i.e.
# the string "aa" will be treated as
# found in a file containing
# "BAA BAA BLACK SHEEP"
#
# NOTE: newlines characters "\n" are ignored
# in the input files and need not be appended
# to any search strings
#
# - arguments
#   str: string to find
#   filename: file to search
#   matchline: when true, match str to an
#    entire line of the file; when false,
#    match str to any substring of a line 
#    in the file.  Default is false;
#   lineno: if specified, the function
#    matches the string at that particular
#    line in the file. Default is 0, which
#    means that the string can be found
#    anywhere in the file.
#
#  Example:
#   if matchline is false, the string "BAA"
#   will be found in "BAA BAA BLACK SHEEP",
#   but if matchline is true, it will not
#   be found since it does not match the 
#   entire line.
#########################################
sub findInFile ( $$;$$ )
{
  my($str, $filename, $matchline, $lineno) = @_;
  my $found_str = 0;
  my $line;
  my $status;
  $matchline ||= 0;
  $lineno ||= 0;
  
  $str = lc( $str );

  open( FILEHND, "< $filename" ) ||
    OdeInterface::logPrint( "Could not open file $filename\n" );

  while (!eof( FILEHND ) )
  {
    $line = lc( readline( *FILEHND ) );
    $found_str++;
    ## skip the lines until the specified line no. is reached
    if ($lineno && ($found_str < $lineno))
    {
      next;
    }
    chop( $line );
    if ($matchline and ($line eq $str))
    {
      close( FILEHND );
      return $found_str;
    }
    elsif ( !$matchline and index( $line, $str ) != -1  )
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


#########################################
#
# sub numDirEntries
# - count the number of entries in a
#   directory.  If an entry is a directory
#   numDirEntries will be called recursively
#   to get the total number of entries.
#
# - arguments
#   dir: directory to open and read to
#   obtain the number of entries
#########################################
sub numDirEntries ( $ )
{
  my($dir) = @_;
  my $num_entries = 0;
  my @filelist;
  my $tmpvar;

  opendir( DIR, $dir ) || warn ( "can't open $dir" );
  @filelist = readdir( DIR );
  while ( @filelist )
  {
    $tmpvar = shift( @filelist );
    # Ignore . and .. entries
    if (($tmpvar ne ".") && ($tmpvar ne ".."))
    {
      $num_entries += 1;
      $tmpvar = $dir . $OdeEnv::dirsep . $tmpvar;
      # If a directory, recursively call this function
      if ( -d $tmpvar )
      {
        $num_entries += numDirEntries( $tmpvar );
      }
    }
  }
  closedir( DIR );
  return( $num_entries );
}

#########################################
#
# sub numLinesInFile
# - count the number of lines in a file
# - return the number of lines
#
# - arguments
#   filename: file whose lines is to be 
#   counted
#########################################
sub numLinesInFile( $ )
{
  my($tmpfile) = @_;
  my $lines;
  open( FILE, $tmpfile ) ||
    OdeInterface::logPrint( "Could not open file $tmpfile\n");
  while (sysread FILE, $buffer, 4096)
  {
    $lines += ($buffer =~ tr/\n//);
  }
  close FILE;
  return $lines;
}

#########################################
#
# sub numPatternsInFile
# - counts the number of lines in a file
#   that contain a specific pattern.
#   The pattern  does not have to exist 
#   on the line by itself
# - returns number of patterns matched
#
# - arguments
#   filename: file whose lines is to be 
#   counted
#   str: pattern to match
#
#########################################
sub numPatternsInFile( $$ )
{
  my($str, $tmpfile) = @_;
  my $line;
  my $matches = 0;

 open( FILE, "< $tmpfile" ) ||
    OdeInterface::logPrint( "Could not open file $tmpfile\n" );

  while( !eof( FILE ) )
  {
    $line = readline( *FILE );
    if (index( $line, $str) >=0) 
		{
      $matches++;
		}
  }
  close(FILE);
  return( $matches );
}

#########################################
#
# sub removeLinesFromFile
# - remove lines from a file that contain
#   any of the specified strings
# - return number of lines removed
#
# - arguments
#   filename: file to remove lines from
#   tokenlist: list of strings that 
#   will be used to remove lines from
#   filename that contain any string
#   in tokenlist
#########################################
sub removeLinesFromFile ( $@ )
{
  my($filename, @tokenlist) = @_;
  my $old = $filename;
  my $new = "$filename.tmp.$$";
  my $line;
  my $numlines = 0;

  open( OLD, "< $old" ) ||
    OdeInterface::logPrint( "Could not open file $old\n" );
  open( NEW, "> $new" ) ||
    OdeInterface::logPrint( "Could not open file $new\n" );

  my $num = scalar( @tokenlist );
  while( !eof( OLD ) )
  {
    my $tk_index = 0;

    $line = readline( *OLD );
    while ($tk_index < $num )
    {
      # If token found in file, skip to next line
      if (index( $line, $tokenlist[$tk_index] ) != -1)
      {
        $numlines++;
        last;
      }
      # If last token and still not found, print to new file
      elsif ($tk_index == ($num - 1))
      {
        print( NEW $line );
      }
      $tk_index = $tk_index + 1;
    }
  }

  close(OLD);
  close(NEW);
  ## rename doesn't work consistently on OS2 and move doesn't exist
  ## in the perl library for AIX
  if (defined( $OdeEnv::UNIX ))
  {
    rename( $new, $old);
  }
  else
  {
    move( $new, $old);
  }

  return( $numlines );
}

#########################################
#
# sub touchFile
# - touches all the files in a list
#   returns 0 if success and 1 otherwise
#
# - arguments
#   files : file or a reference to a list  
#           of files to be touched 
#   newtime : optional argument
#   if specified, updates the timestamp on 
#   the file with the difference between
#   the current time and the newtime.
#########################################
sub touchFile( $;$ )
{
  my( $files, $newtime ) = @_; 
  $files = [$files] unless ref $files;
  if (!defined( $newtime ))
  {
    $newtime = time;
  }
  else
  {
    $newtime = time - $newtime;
  }
  foreach $file (@{$files})
  {
    if ((utime $newtime, $newtime, $file) <= 0)
    {
      if ((open( TMP, "> $file" )))
      {
        close( TMP );
        utime $newtime, $newtime, $file;
        OdeInterface::logPrint( 
                   "$file not existent to touch, hence creating...\n" );
      }
      else
      {
        warn "Couldn't touch $file\n";
        return 1;
      }
    }
  }

  return 0;
}


#########################################
#
# sub concatFiles
# - appends a file to another file,
#   returns 0 if success and 1 otherwise
#
# - arguments
#   file1: file to append
#   file2: file to append to
#
#########################################
sub concatFiles( $$ )
{
  my($file1, $file2) = @_;
  my $line;
  my $errorcode;

  $errorcode = open( FILE1, "< $file1" ) ||
    print( "Could not open file $file1\n" );
  $errorcode = open( FILE2, ">> $file2" ) ||
    print( "Could not open file $file2\n" );
  while( !eof( FILE1 ) )
  {
    $line = readline( *FILE1 );
    (print FILE2 $line);
  }
  close(FILE1);
  close(FILE2);
  return !$errorcode;
}


############################################
#
# sub findFileInDir
# - finds a file matching a regular expression
#   in a directory
#   Returns the name of the file if found
#   else returns 1
#
# - arguments
#   file: expression matching the file name
#   dir: directory to be searched in
############################################
sub findFileInDir( $$ )
{
  my( $file, $dir ) = @_;
  my @filelist;
  my $tmpvar;

  opendir( DIR, $dir ) ||
    warn( "can't open $dir" );
  @filelist = readdir( DIR );
  while (@filelist)
  {
    $tmpvar = shift( @filelist );
    if ($tmpvar =~ /$file/)
    {
      closedir( DIR );
      return $tmpvar;
    }
  }
  closedir( DIR );
  return 1;
}

#########################################
#
# sub getDirSize
# - tabulate the size of a directory, 
#   including all files in any subdirectories.
#   Returns the size, in bytes.
#
# - arguments
#   dir: directory to open and access to
#   obtain the size
#########################################
sub getDirSize ( $ )
{
  my($dir) = @_;
  my $num_bytes = 0;
  my @filelist;
  my $tmpvar;
  my @st;

  opendir( DIR, $dir ) || warn ( "can't open $dir" );
  @filelist = readdir( DIR );
  while ( @filelist )
  {
    $tmpvar = shift( @filelist );
    # Ignore . and .. entries
    if (($tmpvar ne ".") && ($tmpvar ne ".."))
    {
      $tmpvar = $dir . $OdeEnv::dirsep . $tmpvar;
      # If a directory, recursively call this function
      if ( -d $tmpvar )
      {
        $num_bytes += getDirSize( $tmpvar );
      }
      # If a file, get its size
      else
      {
        @st = stat( $tmpvar );
        $num_bytes += $st[7];
      }
    }
  }
  closedir( DIR );
  return( $num_bytes );
}

#########################################
#
# sub deltree
# - removes all the files and directories
#   passed as arguments
#
# -arguments
#  roots: list of directories/files to be deleted
#  verbose: display if unlink or rmdir is used
#  safe: only try to delete files with write acess
#
#  NOTES:  This function is virtually a copy
#  of the Perl standard module library function
#  File::Path::rmtree().  The reason this function
#  is used is the following:
#  - The perl-supplied function for platforms, 
#    if the third argument is true will not,
#    delete a symbolic link if the file it
#    points to is missing, regardless of permissions.
#  - If the third argument is false, the perl-supplied 
#    function on Unix systems will go into an infinite 
#    loop if it cannot delete a file (i.e. no write access).  
#
#########################################

sub deltree {
    my($roots, $verbose, $safe) = @_;
    my(@files);
    my($count) = 0;
    $roots = [$roots] unless ref $roots;
    $verbose ||= 0;
    $safe ||= 0;
    my($root);
    foreach $root (@{$roots}) {
	$root =~ s#/$##;
	(undef, undef, my $rp) = lstat $root or next;
	$rp &= 07777;	# don't forget setuid, setgid, sticky bits
	if ( -d _ ) {
	    # notabene: 0777 is for making readable in the first place,
	    # it's also intended to change it to writable in case we have
	    # to recurse in which case we are better than rm -rf for 
	    # subtrees with strange permissions
	    chmod(0777, ($Is_VMS ? VMS::Filespec::fileify($root) : $root))
	      or carp "Can't make directory $root read+writeable: $!"
		unless $safe;

	    my $d = DirHandle->new($root)
	      or carp "Can't read $root: $!";
	    @files = $d->read;
	    $d->close;

	    # Deleting large numbers of files from VMS Files-11 filesystems
	    # is faster if done in reverse ASCIIbetical order 
	    @files = reverse @files if $Is_VMS;
	    ($root = VMS::Filespec::unixify($root)) =~ s#\.dir$## if $Is_VMS;
	    @files = map("$root/$_", grep $_!~/^\.{1,2}$/,@files);
	    $count += deltree(\@files,$verbose,$safe);
	    if ($safe &&
		($Is_VMS ? !&VMS::Filespec::candelete($root) : !-w $root)) {
		print "skipped $root\n" if $verbose;
		next;
	    }
	    chmod 0777, $root
	      or carp "Can't make directory $root writeable: $!"
		if $force_writeable;
	    print "rmdir $root\n" if $verbose;
	    if (rmdir $root) {
		++$count;
	    }
	    else {
		carp "Can't remove directory $root: $!";
		chmod($rp, ($Is_VMS ? VMS::Filespec::fileify($root) : $root))
		    or carp("and can't restore permissions to "
		            . sprintf("0%o",$rp) . "\n");
	    }
	}
	else { 
	    if ($safe &&
		($Is_VMS ? !&VMS::Filespec::candelete($root) : !-w $root)) {
		print "skipped $root\n" if $verbose;
		next;
	    }
	    chmod 0666, $root
	      or carp "Can't make file $root writeable: $!"
		if $force_writeable;
	    print "unlink $root\n" if $verbose;
	    # delete all versions under VMS
	    for (;;) {
		unless (unlink $root) {
		    carp "Can't unlink file $root: $!";
		    if ($force_writeable) {
			chmod $rp, $root
			    or carp("and can't restore permissions to "
			            . sprintf("0%o",$rp) . "\n");
		    }
		    last;
		}
		++$count;
		last unless $Is_VMS && lstat $root;
	    }
	}
    }
    $count;
}


1;
