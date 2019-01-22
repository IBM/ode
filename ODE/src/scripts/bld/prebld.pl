#!/bin/perl
# prebld:
#   - Maps Release, Level and Build Date information
#   - Removes .gone files
#   - This script is intended to run after extract and
#     before the source is built
#

&setBaseVals(); # set variables needed
&processFiles(); # open
exit; # done


#
#  SUBROUTINES
#


sub processFiles()
{
	# open VerRel.lst and read it
	open(FILE,$filelist) || die "Could not open $filelist";
	@lines = <FILE>; # get all lines into an array
	close(FILE);

	$numlines = @lines;
	@allfiles = ();
	for ($i = 0; $i < $numlines; ++$i)
	{
		push(@allfiles,<$buildbasedir/$lines[$i]>);
	}

	$numfiles = @allfiles;
	for ($i = 0; $i < $numfiles; ++$i)
	{
		$filename = $allfiles[$i];
		$newfile = $filename.tmp;
		print "Processing $filename\n";
		open(INFILE,$filename) || die "Couldn't open $filename for reading";
		open(OUTFILE,">$newfile") || die "Couldn't open $newfile for writing";
		while (<INFILE>)
		{
			s/$levelkey/$levelname/g;
			s/$releasenumkey/$releasenum/g;
			s/$releasekey/$releasename/g;
			s/$releasekeylower/$releasenamelower/g;
			s/$datekey/$now/g;
			print OUTFILE;
		}
		close(INFILE);
		close(OUTFILE);
		unlink($filename) || die "Couldn't remove $filename";
		rename($filename.tmp,$filename) ||
			die "Couldn't rename $filename.tmp to $filename";
	}
}


sub setBaseVals()
{
	$arglen = @ARGV;
	&showUsage() if ($arglen != 0 && $arglen != 3);

	if ($arglen == 3)
	{
		$basedir = $ARGV[0];
	}
	elsif (open(BASEDIR_FILE,"base.dir"))
	{
		$basedir = <BASEDIR_FILE>;
		chop($basedir);
		close(BASEDIR_FILE);
	}
	else
	{
		die "No basedir given on cmdline and couldn't open base.dir file";
	}

	if ($arglen == 3)
	{
		$releasenum = $ARGV[1];
	}
	elsif (open(RELEASE_FILE,"release.num"))
	{
		$releasenum = <RELEASE_FILE>;
		chop($releasenum);
		close(RELEASE_FILE);
	}
	else
	{
		die "No release given on cmdline and couldn't open release.num file";
	}

	if ($arglen == 3)
	{
		$levelname = $ARGV[2];
	}
	elsif (open(BUILD_FILE,"build.name"))
	{
		$levelname = <BUILD_FILE>;
		chop($levelname);
		close(BUILD_FILE);
	}
	else
	{
		die "No build given on cmdline and couldn't open build.name file";
	}

	$releasename = "ODE$releasenum";
	$releasenamelower = "ode$releasenum";
   $buildbasedir = "$basedir/src";

	# Main processing for versions and release
	$filelist = "$buildbasedir/scripts/bld/VerRel.lst";

	$levelkey = "%LEVEL_NAME%";
	$releasenumkey = "%RELEASE_NUM%";
	$releasekey = "%RELEASE_NAME%";
	$releasekeylower = "%release_name%";
	$datekey = "%BUILD_DATE%";

	&setTimeVals();

	$now = "${day}-${monthname}-${year}";
}


sub setTimeVals()
{
	@loctime = localtime( time() );
	$hour = $loctime[2];
	$hour = "0$hour" if (length($hour) < 2);
	$min = $loctime[1];
	$min = "0$min" if (length($min) < 2);
	$month = $loctime[4] + 1;
	$month = "0$month" if (length($month) < 2);
	$day = $loctime[3];
	$day = "0$day" if (length($day) < 2);
	$year = $loctime[5] + 1900;
	@monthnames = ("Jan","Feb","Mar","Apr","May","Jun",
		"Jul","Aug","Sep","Oct","Nov","Dec");
	$monthname = $monthnames[$month - 1];
}


sub showUsage()
{
	print "Usage: $0 [basedir release build]\n";
	print "  ex., $0\n";
	print "  ex., $0 d:/builds/mybb 5.0 20041004.0550\n";
	die;
}
