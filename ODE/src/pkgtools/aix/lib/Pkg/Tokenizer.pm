#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++  
#  
# (C) COPYRIGHT International Business Machines Corp. 1996, 1997
# All Rights Reserved 
#  
# US Government Users Restricted Rights - Use, duplication or 
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp. 
#
# ORIGINS: 27
#
#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
#
# NAME        : Pkg::Tokenizer Class
# DESCRIPTION :
#    The 'Pkg::Tokenizer' Class implements the processing of the 
#    product definition and config files into language tokens.
#    For the most part the tokenizer is not context sensitive and
#    is therefore unable to check language syntax.  The tokenizer
#    must be used by a parser to parse the tokens.  For the 
#    pkgtools toolset, this parser is the Pkg::Spec class.
#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package Pkg::Tokenizer;

#
# Standard Package Preamble
#
require 5.002;
require Exporter;
require Pkg::Log;
@ISA    = qw(Exporter Pkg::Log);

use Carp;
use English;
use FileHandle;
use strict;
#**********************************************************************
# The Pkg::Tokenizer class implements the mechanism to track and 
# extract tokens from a given file.  Tokens are defined as a 
# sequence of characters delimitted by one of the following 
# characters:
#   [ ] , ; { } " ? + = (space)
#"
#**********************************************************************

#*********************************************************************
# Object Declaration section (ie. the header file part)
#*********************************************************************
#
# Possible Return Codes
#   These are accessed as "$Pkg::Tokenizer::Err_Bad_Name"...
#        $Continue is really an internal code and should never 
#        get out of get_token.  
#   All error codes begin at 1000.  Values under 1000 are not
#   error conditions.
#
use vars(qw($OK $Continue $EOF $Err_Unknown    $Err_Runaway_Quote 
	    $Err_Invalid_Char  $Err_cpp_Failed $Err_No_cpp_Cmd));
$OK            = 0;
$Continue      = 900;
$EOF           = 999;

$Err_Unknown           = 1000;
$Err_Runaway_Quote     = 1001;
$Err_Invalid_Char      = 1003;
$Err_cpp_Failed        = 1004;
$Err_No_cpp_Cmd        = 1005;



#=======================================================================
# NAME    : new
# PURPOSE : Creates and initializes the tokenizer.
# INPUTS  :
#   file - name of the file to be read
# OUTPUTS :
#   none.
# RETURNS : (really sets in status variable)
#   0 = success
#  !0 = error.
#=======================================================================
sub new
{
    my($class, $file, $cpp_flags, $no_cpp) = @_;
    my $rc = $OK;

    my $self = {};
    bless $self, $class;

    $self->{Last_RC}    = $OK;
    $self->{Line_nmbr}  = 1;
    $self->{File_name}  = $file;
    $self->{Quote_line} = 0;
    $self->{In_Quote}   = 0;
    $self->{Token}      = "";
    $self->{Line}       = [];
    $self->{CPP_Flags}  = $cpp_flags;
    $self->{No_CPP}     = $no_cpp;
    $self->{FH}         = new FileHandle;

    if ($no_cpp)
    {
	if (!$self->{FH}->open("<$file"))
	{
	    $rc = $Err_Unknown;
	    carp("Unable to open file '$file'; $!\n");	
	}
    }
    else
    {
	my $cpp_cmd = `ksh whence cpp`;
	chop($cpp_cmd);
	$self->Debug("getting cpp from '$cpp_cmd'.");
	if ($cpp_cmd ne "")
	{
	    if (!$self->{FH}->open("cpp $cpp_flags $file 2>/dev/null|"))
	    {
		$rc = $Err_Unknown;
		carp("Unable to open file '$file'; $!\n");
	    }
	}
	else
	{
	    $rc =$Err_No_cpp_Cmd;
	}
    }
    $self->{Last_RC} = $rc;
    return($self);
}

#=======================================================================
# NAME    : term
# PURPOSE : Terminates and shuts down the tokenizer.
# INPUTS  :
#   none,
# OUTPUTS :
#   none.
# RETURNS : 
#   nothing.
#=======================================================================
sub term
{
    my $self = shift;

    my $rc = $OK;
    $self->{FH}->close();

    if (! $self->{No_CPP})
    {
	if ($?)
	{
	    $self->{Last_RC} = $!;
	    $rc = $Err_cpp_Failed;
	}
	else
	{
	    $rc = $self->{Last_RC} = $OK;
	}
    }
    else
    {
	$rc = $self->{Last_RC} = $OK;
    }

    $self->Debug("RC = '$rc'.");
    return($rc);
}

#=======================================================================
# NAME    : get_token
# PURPOSE : extract the next token from the current file.
# INPUTS  :
#   none
# OUTPUTS :
#   Token - module level variable holding the current token.
# RETURNS : 
#  Tkn::OK       = success
#  anything else = failure (see list of Err codes.
#=======================================================================
sub get_token
{
    my $self = shift;
    my $rc = $Continue;

    $self->Debug();

    $self->{Token} = "";
    my $c = $self->gc;
    if (defined($c))
    {
	while (defined($c) && $rc == $Continue)
	{
	    if ($c =~ /[\#\s\"\[\]\;\,\\\?\+\{\}\=]/o)               #"
	    {
		$rc = $self->special_char($c);
	    }
	    else
	    {
		$self->{Token} .= $c;
	    }
	    $c = $self->gc if ($rc == $Continue);
	} # END while loop
	$rc = ($Continue && $self->{Token} ne "") ? $OK : $EOF; 
    }
    else
    {
	$rc = ($self->{In_Quote}) ? $Err_Runaway_Quote : $EOF;
	$self->{In_Quote} = 0;
    }
    $self->{Last_RC} = $rc;
    $self->Debug("rc = $rc, token = $self->{Token}");
    return($rc);
}

#*********************************************************************
# Private data and functions
#*********************************************************************
sub gc
{
    my $self = shift;

    while ((!scalar(@{$self->{Line}})) && (! $self->{FH}->eof()))
    {
	my $line = $self->{FH}->getline();
	$self->Debug("read line '$line'");
	@{$self->{Line}} = split(//, $line);
	$self->{Line_nmbr}++;
    }
    return(shift(@{$self->{Line}}));
}

sub un_gc
{
    my $self = shift;
    my($c) = @_;

    if (defined($c))
    {
	unshift(@{$self->{Line}}, $c);
    }
}


#=======================================================================
# NAME    : handle_comment
# PURPOSE : To handle comment lines.  These lines can also contain
#           information from 'cpp'.  cpp info lines contain line
#           number and file name information.  This information
#           must be extracted and saved in the object for use in
#           error messages and such.
# INPUTS  :
#   none
# OUTPUTS :
#   Object may be modified with new line number and file name
#          information.
# RETURNS : nothing;
#=======================================================================
sub handle_comment
{
    my $self = shift;

    my $ls_sep = $LIST_SEPARATOR;
    $LIST_SEPARATOR = "";          # makes the Line array look like a string

    $self->Debug("@{$self->{Line}}");

    my($line, $file);
    if (($line, $file) = ("@{$self->{Line}}" =~ m/^line (\d+) \"(.*)\"$/o))
    {
	$self->Debug("Found new Line/File info ($file, $line)");
	$self->{Line_nmbr} = $line - 1;
	$self->{File_name} = $file;
    }
    $LIST_SEPARATOR = $ls_sep;
    # in all cases, Empty the line array so the 
    #    next line will be read.
    splice(@{$self->{Line}}, 0, @{$self->{Line}});
}


sub special_char
{
    my $self = shift;
    my ($c) = @_;
    my $rc = $Continue;

    if ($self->{In_Quote} && $c ne "\"")
    {
	# All special characters are put into the string if
	# A string is active.  (simplifies each case in 
	# C_switch below).
	$self->{Token} .= $c;
	if($c =~ /\\/o)
	{
	    # If the char is a backslash, I need to swallow
	    # the next character immediately;
	    # (it does not get any special treatment)
	    $c = $self->gc;
	    if ($c)
	    {
		$self->{Token} .= $c;
	    }
	    else
	    {
		$rc = $OK;
	    }
	    last C_switch;
	}
    }
    else
    {
	C_switch: 
	{
	  ($c =~ /\#/o) && do  # Comment character found.
	  {
	      if ($self->{Token} ne "")
	      {
		  # Signal end of current token and save 
		  # the comment for next time
		  $self->un_gc($c);
	      }
	      else
	      {
		  $self->handle_comment();
	      }
	  };
	  ($c =~ /[\,\[\]\{\}\=\;]/o) && do
	  {
	      if ($self->{Token} ne "")
	      {
		  # Signal end of current token and save 
		  # the semi for next time
		  $rc = $OK;
		  $self->un_gc($c);
	      }
	      else
	      {
		  $self->{Token} = $c;
		  $rc = $OK;
	      }
	      last C_switch;
	  };
	  ($c =~ /[\?\+]/o) && do
	  {
	      if ($self->{Token} ne "")
	      {
		  # Signal end of current token and save 
		  # the semi for next time
		  $self->un_gc($c);
	      }
	      else
	      {
		  # Must check to see if there is an equal
		  # sign following it.  If there is,
		  # include it in the current token.
		  # if not, this token stands as is.
		  $self->{Token} = $c;
		  $c = $self->gc;
		  if (defined($c))
		  {
		      if ($c eq "=")
		      {
			  $self->{Token} .= $c;
		      }
		      else
		      {
			  $self->un_gc($c);
		      }
		  }
	      }
	      $rc = $OK;
	      last C_switch;
	  };
	  ($c =~ /\s/o) && do
	  {
	      # White spece is the end of a token if there is a
	      # a token active ($token ne "").  Otherwise it
	      # is ignored.
	      if ($self->{Token} ne "")
	      {
		  $rc = $OK;
	      }
	      last C_switch;
	  };
	  ($c =~ /\"/o) && do                         #"
	  {
	      # The quote will either start or end a string.
	      # If starting, save the line number for possible
	      # error messages.
	      if ($self->{In_Quote})
	      {
		  $rc = $OK;
		  $self->{In_Quote} = 0;
	      }
	      else
	      {
		  $self->{In_Quote} = 1;
		  $self->{Quote_line} = $self->{Line_nmbr};
	      }
	      $self->{Token} .= $c;
	      last C_switch;
	  };
       };
    }
    return($rc);
} # End special_char


