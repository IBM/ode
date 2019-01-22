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
# NAME        : Pkg::Log Class
# DESCRIPTION :
#    The 'Pkg::Log' Class implements all logging related activities.
#    These activities include error reporting, status logging, and
#    debug message generation.  This class can be instatiated on
#    its own to provide "standard" logging capabilites to commands.
#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package Pkg::Log;

#
# Standard Package Preamble
#
require 5.002;
require Exporter;
@ISA    = qw(Exporter);

use English;
use Carp;

use strict;

#*********************************************************************
# "Static" variables for the Fileset object
#
use vars(qw($dbg_pattern $Cmd));
#
# $dbg_pattern is a static variable that contains the regular
# expression used to determine which debug messages should be
# generated and which should not.  The $dbg_pattern is used
# on the subroutine name.  If the pattern is found, the message
# is generated; if not, it is not.
$dbg_pattern = "";

$Cmd="Pkg::Log";

#*********************************************************************
# Object Implementation Section (ie. the code part) 
#*********************************************************************
#-----------------------------------------------------------------
# "Public" Methods
#-----------------------------------------------------------------
#=================================================================
# NAME     : constructor (new)
# PURPOSE  : create the Log object and initialize it.
# INPUTS   : 
#   $class  - name of the class to be created
# OUTPUTS  : none
# RETURNS  : pointer to object
#=================================================================
sub new
{
    my($class, $cmd) = @_;

    my $self = {};
    bless $self, $class;
    $self->initialize($cmd);
    return($self);
}

sub initialize
{
    my ($self, $cmd) = @_;

    if ($cmd)
    {
	$Cmd = $cmd;	
    }
    return;
}


#=================================================================
# NAME     : register errors
# PURPOSE  : registers the error message formats for the "real"
#            object.
# INPUTS   : 
#   $err_table - reference to the error table to use.
# OUTPUTS  : none.
# RETURNS  : nothing
#=================================================================
sub register_errors
{
    my ($self, $err_table) = @_;
    my($pkg, $fn, $line, $sub, $hasargs, $wantargs) = caller(0);
    my $name = "_" . $pkg . "::_Errors";

    $self->{$name} = $err_table;
}


#=================================================================
# NAME     : Set Debug
# PURPOSE  : modifies the debug pattern filter.
# INPUTS   : 
#   $pattern - new pattern.
# OUTPUTS  : none.
# RETURNS  : nothing
#=================================================================
sub set_debug
{
    my ($self, $pattern) = @_;
    $dbg_pattern = $pattern;
}

#=================================================================
# NAME     : Log
# PURPOSE  : The 'Log' method is used to report progress 
#            information.  It timestamps each message
#            and puts the result on STDOUT.
# INPUTS   : 
#   $msg - the message that is to be logged.
# OUTPUTS  : STDOUT
# RETURNS  : nothing
#=================================================================
sub Log
{
    my ($self, $msg) = @_;
    my($sec, $min, $hr, $junk) = localtime(time);

    printf(STDOUT "%02d:%02d:%02d --\t%s\n", 
	           $hr, $min,$sec,    $msg);
}


#=================================================================
# NAME     : Debug
# PURPOSE  : The 'Debug' method is used to report detailed 
#            progress information suitable for aiding in problem
#            diagnosis.  It prepends the package name and the
#            subroutine name of the caller to the message
#            being posted.  Messages from Debug are printed
#            STDERR.
# INPUTS   : 
#   $msg - the message that is to be logged.
# OUTPUTS  : STDOUT
# RETURNS  : nothing
#=================================================================
sub Debug
{
    my ($self, $msg) = @_;
    if ($dbg_pattern)
    {
	my $junk;
	my($pkg, $fn, $line, $sub, $hasargs, $wantargs) = caller(0);
	($pkg, $fn, $junk, $sub, $hasargs, $wantargs) = caller(1);
	if (($dbg_pattern ne "") && ($sub =~ /$dbg_pattern/))
	{
	    print STDERR "@@ $sub($line)=> $msg\n";
	}
    }
    return;
}


#=================================================================
# NAME     : error
# PURPOSE  : generate the appropriate error message
# INPUTS   : 
#   $severity - severity id (ie ERROR, WARNING, etc)
#   $err_id   - error id number (to index into the format array)
#   @parms    - parameters to the error message.
# OUTPUTS  : 
#    STDERR - gets message
# RETURNS  : nothing
#=================================================================
sub error
{
    my ($self, $severity, $err_id, @parms) = @_;

    #
    # Start 2 calls back to see if the caller is an override 
    # of this method (ie method name is error).  If it is,
    # use that stack frame info; if it is not, move up
    # a frame to get the correct package info.
    #
    my($pkg, $fn, $line, $sub, $hasargs, $wantargs) = caller(1);
    if ($sub !~ /::error$/o)
    {
	($pkg, $fn, $line, $sub, $hasargs, $wantargs) = caller(0);
    }
    my $err_table = "_" . $pkg . "::_Errors";

    my $format = "$Cmd(%s) - ";
    my $my_parms = 1;            # nmbr of my added parms to the format str.

    if ($err_id !~ /^\d+$/o)
    {
	# No error number.  Assume whole string passed in $err_id.
	$format .= $err_id;
    }
    elsif (defined($self->{$err_table}->[$err_id]))
    {
	# Add error msg format to the preamble started above.
	$format .= $self->{$err_table}[$err_id];	
    }
    else
    {
	# UNKNOWN error number.  Indicate it in Format str.
	$format .= "unknown Error Code '$err_id' at " . $sub . ";";
    }

    my $msg = sprintf($format, uc($severity), @parms);
    #
    # Determine if there are more @parms than there are parm fields
    # in the format string and append any extras to the message.
    #
    my @c = ($format =~ /(%[-\d.]*[sdxXofg])/go);
    my $cnt = scalar(@c) - $my_parms;
    if (scalar(@parms) - $cnt > 0)
    {
	splice(@parms, 0, $cnt);
	my $ofs = $OFS;
	$OFS = " ";
	$msg .= sprintf(" -- (@parms)");
	$OFS = $ofs;
    }
    print STDERR "\n$msg\n\n";
    return;
}

#-----------------------------------------------------------------
#-----------------------------------------------------------------
# "Private" Data and Methods
#-----------------------------------------------------------------
#-----------------------------------------------------------------
1;


