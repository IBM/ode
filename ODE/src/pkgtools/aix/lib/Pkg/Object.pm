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
# NAME        : Pkg::Object base Class
# DESCRIPTION :
#    The 'Pkg::Object' base Class provides the base methods and
#    variables common to all pkgtools object classes.  This
#    class should not be instantiated on its own, but rather made
#    an inherited class of one of the pkgtools toolset object
#    class definitions.
#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package Pkg::Object;

#
# Standard Package Preamble
#
require 5.002;
require Exporter;
require Pkg::Log;
@ISA    = qw(Exporter Pkg::Log);

#use Pkg::Parser;
use English;
use strict;

#*********************************************************************
# "Static" variables for the Fileset object
#
use vars(qw(@valid_fields %field_types @err_table));

#
# @valid_fields identifies the valid field names.  These fields are
# identified here since they can be specified in almost any object.
#
@valid_fields = (qw(adecopyright_flags adeinv_flags       adepackage_flags
		    base_level         boot_rqmt          comments
		    compids_table      content            control_files
		    copyright          copyright_keys     copyright_map 
		    description        filesets           fix_info_file  
		    fix_level          image_name         input_path
		    inslist            install_space      language
		    maint_level        odm_add_files      odm_class_def
		    odm_history_dir    pd_file            platform
		    problems_fixed     product_name       release
		    requisites         root_add_files     root_control_files 
		    shipfiles          ship_path          supersedes
		    uid_table          update_space       version
		    v_reqs             work_dir           ar_flags
        user_prereq ));
%field_types = (
		 adecopyright_flags => "string",
		 adeinv_flags       => "string",
		 adepackage_flags   => "string",
		 ar_flags           => "string",
		 base_level         => "scalar",
		 boot_rqmt          => "scalar",
		 comments           => "string",
		 compids_table      => "scalar",
		 content            => "scalar",
		 control_files      => "array of scalar",
		 copyright          => "string",
		 copyright_keys     => "array of string",
		 copyright_map      => "scalar",
		 description        => "string",
		 filesets           => "array of scalar",
		 fix_info_file      => "scalar",
		 fix_level          => "scalar",
		 image_name         => "scalar",
		 input_path         => "array of scalar",
		 inslist            => "scalar",
		 install_space      => "array of string",
		 language           => "scalar",
		 maint_level        => "scalar",
		 odm_add_files      => "array of scalar",
		 odm_class_def      => "scalar",
		 odm_history_dir    => "scalar",
		 pd_file            => "scalar",
		 platform           => "scalar",
		 problems_fixed     => "array of string",
		 product_name       => "scalar",
		 release            => "scalar",
		 requisites         => "array of string",
		 root_add_files     => "array of scalar",
		 root_control_files => "array of scalar",
		 shipfiles          => "array of scalar",
		 ship_path          => "scalar",
		 supersedes         => "array of string",
		 uid_table          => "scalar",
		 update_space       => "array of string",
		 user_prereq        => "scalar",
		 version            => "scalar",
		 v_reqs             => "array of string",
		 work_dir           => "scalar",
		);
#
# @err_table contains the error message formats for the 
# various error messages.  Note that this table is just
# an array, and so the strings need to be in the correct
# "slot" in order to be found for the correct error.
#
@err_table = ("No Error",
  # $Err_Bad_Case_Field_Name = 1 ($attr_name, $line, $file, $possible_match)
	      "Found field name of '%s' on line '%d' of '%s'\n" .
	      "\tthat does not match any known fields.  It appears that \n" .
	      "\tthe case is wrong, maybe it should be '%s'; Ignoring it.\n",
  # $Err_Unknown_Field = 2 ($attr_name, $line, $file)
	      "Unknown field name '%s' on line %s of file '%s'",
  # $Err_Dup_In_Field_List = 3 ($attr_name)
	      "Multiple instances of the field name '%s' in the valid " .
	      "fields array!!\n\tThis is an internal error and should be " .
	      "reported!",
  # $Err_Invalid_Type = 4 ($attr_name, $line, $file, $type, $desired_type)
	      "Field '%s' on line '%d' in file '%s'\n\t" .
	      "is type '%s'.  It should have a value type of '%s'.",
  # $Err_Unknown_Type = 5 ($attr_name, $line, $file, $type)
	      "Field '%s' on line '%d' in file '%s'\n\t" .
	      "has an unknown value type (%s).",
  # $Err_Requisites_Field = 6 ($line, $file)
	    "The 'requisites' field was found on line '%d' in file '%s'.\n\t".
	    "This field is NOT supported.  Use the 'v_reqs' field.\n\t".
	    "For now, I will change the field name internally.",
);
use vars(qw($Err_Bad_Case_Field_Name $Err_Unknown_Field 
	    $Err_Dup_In_Field_List   $Err_Invalid_Type 
	    $Err_Unknown_Type        $Err_Requisites_Field
	    $Err_Supersedes_Field));

$Err_Bad_Case_Field_Name  = 1;
$Err_Unknown_Field        = 2;
$Err_Dup_In_Field_List    = 3;
$Err_Invalid_Type         = 4;
$Err_Unknown_Type         = 5;
$Err_Requisites_Field     = 6;
$Err_Supersedes_Field     = 7;
#
#*********************************************************************
# Object Implementation Section (ie. the code part) 
#*********************************************************************
#-----------------------------------------------------------------
# "Public" Methods
#-----------------------------------------------------------------
#=================================================================
# NAME     : constructor (new)
# PURPOSE  : create the specification object and initialize it.
# INPUTS   : 
#   $class  - name of the class to be created
#   $config - ref to config object.
#   $file   - name of the file containing this definition
#   $line   - line nmbr inf $file where definition started.
# OUTPUTS  : none
# RETURNS  : pointer to object
#=================================================================
sub new
{
    my($class, $config, $file, $line, $parser) = @_;

    my $self = {};
    bless $self, $class;
    $self->Debug();
    Pkg::Object::initialize($self, $config, $file, $line, $parser);
    return($self);
}


#=================================================================
# NAME     : validate field
# PURPOSE  : The validate_field method validates the given 
#            field.  The validation includes known/valid 
#            field name, correct value type, and in some 
#            cases, validation of the contents of the value.
# INPUTS   : 
#   $field - reference the name of the field to validate
#   $value - reference to the value assigned.
#   $line  - the line number in $file on which the statment was found
#   $file  - the actual file being processed.
# OUTPUTS  : none.
# NOTES    : none.
# RETURNS  :
#     0 = success
#    !0 = failure
#         1 = unknown field.
#         8 = invalid value type.
#         9 = invalid value contents.
#=================================================================
sub validate_field
{
    my ($self, $field, $value, $line, $file) = @_;
    my $rc = 0;

    $self->Debug();
    #
    # First lets handle some special cases for field names
    #    that need to be mapped to other names for now.
    #
    my $attr = $$field;
    if ($attr eq "requisites")
    {
	$self->error('Warning', $Err_Requisites_Field, 
		     $line, $file);
	$attr = "v_reqs";
	$$field = $attr;                # Make sure the input field is updated.
    }

    my @matches = grep(/^$attr$/i, @valid_fields);
    if (scalar(@matches) == 1)
    {
	if ($attr eq $matches[0])
	{
	    my $type = ref($value);
	    if (! $type)
	    {
		# value is not a reference.  It is either
		# a scalar or a string.
		$type = ($value =~ /^".*"$/o) ? "string" : "scalar";
	    }
	    elsif ($type =~ /array/io)
	    {
		$type = "array";
	    }
	    else
	    {
		$self->error('Error', $Err_Unknown_Type, 
			     $attr, $line, $file, $type);
		$rc = 9;
	    }
	    #
	    # If type has been determined, verify it is correct
	    # for the given field
	    if ($rc == 0)
	    {
		if ($field_types{$attr} !~ /^$type/i)
		{
		    if ($type eq "string"  && $field_types{$attr} eq "scalar")
		    {
			$value =~ s/^\s*\"(.*?)\s*\"\s*$/$1/so;   # strip "s
		    }
		    elsif ($type eq "scalar" && 
			   $field_types{$attr} eq "string")
		    {
			$value = "\"$value\"";                 # add "s
		    }
		    else
		    {
			$self->error('Error', $Err_Invalid_Type, 
				     $attr, $line, $file, $type, 
				     $field_types{$attr});
			$rc = 8;
		    }
		}
	    }
	}
	else
	{
	    $self->error('Warning', $Err_Bad_Case_Field_Name, 
			 $attr, $line, $file, $matches[0]);
	    $rc = 1;
	}
    }
    elsif (scalar(@matches) == 0)
    {
	$self->error('Warning', $Err_Unknown_Field, $attr, $line, $file);
	$rc = 1;
    }
    else
    {
	# Have got multiple matches; this should never happen
	$self->error('Error', $Err_Dup_In_Field_List, $attr);
	$rc = 32;
    }
    return($rc);
}


#=================================================================
# NAME     : execute command
# PURPOSE  : This method provides a common mechanism with which
#            to execute the base packaging commands.  It logs 
#            the start and the rc.  It also translates the
#            rc back to a usable value.
# INPUTS   : 
#   $cmd   - command to be executed
# OUTPUTS  : none.
# NOTES    :
#   - This method only works for the execution of commands
#     where any results on STDOUT do not need to be given 
#     directly back to the caller.  STDOUT output can be 
#     redirected to a file and the caller can then process
#     the file.
# RETURNS  :
#     The translated result of the command.
#=================================================================
sub execute_cmd
{
    my ($self, $cmd) = @_;
    $self->Log("Executing command '$cmd'");
    my $rc = system("$cmd");
    $rc = $rc / 256;
    $self->Log("Result of command is '$rc'");
    return($rc);
}



#=================================================================
# NAME     : error
# PURPOSE  : generate the appropriate error message
#            Note that this method overrides the error method
#            in Pkg::Log.  This method adds some information 
#            about object type, name, and starting line number.
#            The "standard" error method in Pkg::Log is called 
#            to actually generate the message.
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

    my($pkg, $fn, $line, $sub, $hasargs, $wantargs) = caller(0);

    my $format = "for %s:%s [line %d of %s]";

    my $msg = sprintf($format, ref($self), $self->{Name},
		      $self->{'_line_nmbr'}, $self->{'_in_file'});
    $self->Pkg::Log::error($severity, $err_id, @parms, $msg);;
    return;
}


#==========================================================================
# Name     : find file
# Purpose  : find_file searches for the requested file in
#            the predefined order.
# Inputs   :
#   $file  - name of the file to find.
# Outputs  :
# Notes    :
#    - This method requires that each class that inherits the
#      Object class define the function 'bld_input_path' to
#      construct the directory search order.
#    - On the 1st call to find_file for a particular object,
#      the input search path is constructed via a call to
#      'bld_input_path'.
# Returns  :
#    SUCCESS = full pathname to the file
#    FAILURE = null string
#==========================================================================
sub find_file
{
    my $self = shift;
    my($file) = @_;
    my $path = "";

    # If this is the 1st time for find_file, the input_path
    # search path must be constructed.
    #
    if (!defined($self->{'_input_path'}))
    {
	$self->bld_input_path();
    }

    if ($file ne "" )
    {
	if (substr($file, 0, 1) ne "/")       # Is given file an absolute path?
	{
	    # There is a file and it is not an absolute path,
	    # so search for it.
	    #
	    my $input_path = $self->{'_input_path'};
	    my $dir;
	    $self->Debug("input_path = '$input_path'; file = '$file'");
	    foreach $dir (split(/:/, $input_path))
	    {
		next if ($dir eq "");
		my $tmp = "$dir/$file";
		if (-f $tmp)
		{
		    $path = "$dir/$file";
		    last;
		}
	    }
	}
	else
	{
	    # Absolute path names are returned as is.
	    $path = $file;
	}
    }
    # Lets "normalize" the pathname before returning it by... 
    #   1) removing any multiple continguous slashes
    #   2) removing any "." directories
    #   3) compressing out any ".." directories.
    #
    $path =~ s|//+|/|go;
    $path =~ s|/\./|/|go;
    while (($path =~ s|/[^/]*/\.\./|/|go)) {};

    $self->Debug("returning path = '$path'");
    return($path);
} # END find_file


#-----------------------------------------------------------------
#-----------------------------------------------------------------
# "Private" Data and Methods
#-----------------------------------------------------------------
#-----------------------------------------------------------------
#=================================================================
# NAME     : initialize
# PURPOSE  : initializes the newly created Pkg::Object object
# INPUTS   :
#   $config - ref to config data object
#   $file   - name of the file containing this definition
#   $line   - line number in $file where definition started.
# OUTPUTS  : none
# RETURNS  : nothing
#=================================================================
sub initialize
{
    my($self, $config, $file, $line, $parser) = @_;

    $self->{Config}       = $config;    # ptr to hash for general config info
    $self->{'_in_file'}   = $file;
    $self->{'_line_nmbr'} = $line;
    $self->{'_parser'}    = $parser;

    $self->{'_src_dir'}   = substr($file, 0, rindex($file, "/"));
    $self->{Name}         = "Pending";  # Name is not yet known. 
    $self->{'_Errors'}    = undef;

    $self->register_errors(\@err_table);
    return;
}
1;
