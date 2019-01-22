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
# NAME        : Pkg::Product Class
# DESCRIPTION :
#    The 'Pkg::Product' Class implements the Product object for the
#    pkgtools toolset.  This class stores the definition of a
#    product found in a product definition file and then provides
#    the methods necessary to package the product and its assocated
#    filesets into an installation package acceptable to AIX Version 
#    4.x's installp command.
#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package Pkg::Product;

#
# Standard Package Preamble
#
require 5.002;
require Exporter;
require Pkg::Packager;

@ISA    = qw(Exporter Pkg::Packager);

use English;
use strict;
#*********************************************************************
# The Product module implements the packaging product object.
#
use vars(qw(%rqd_fields @irrelevant_fields @err_table));

# %rqd_fields identifies the minimum set of fields that must
# be defined in order to create a fileset part of a package.
# The value part is either 'local' which means that the field
# must be defined within the fileset itself, of 'parent'
# which means the field can be defined in a "parent".
#
%rqd_fields = (
	       description => "local",
	       filesets    => "local",
	       image_name  => "local",
	       ship_path   => "parent",
	       language    => "parent",
	       platform    => "parent",
	       version     => "parent",
	       release     => "parent",
	       maint_level => "parent",
	       fix_level   => "parent",
	      );
#
# @irrelevant_fields is a list of fields that are irrelevant
# in Fileset object definitions.  These fields should appear
# somewhere else (Product or Config file right now).
@irrelevant_fields = (qw(odm_add_files     root_add_files    inslist
			 pd_file           requisites        v_reqs
			 shipfiles));
#
# $err_table contains the error message formats for the 
# various error messages.  Note that this table is just
# an array, and so the strings need to be in the correct
# "slot" in order to be found for the correct error.
#
@err_table = ("No Error",
  # $Err_Could_Not_Open_File = 1 ($file_name)
	      "Could not open, or create, file (%s).",
  # $Err_Could_Not_Create_Dir = 2 ($dir_name)
	      "Could not create directory '%s'.",
  # $Err_Could_Not_Find_File = 3 ($file_name)
	      "Could not find file '%s'.",
  # $Err_Missing_Local_Field = 4 ($field_name, $product)
	   "The field '%s' is required to be in the Product\n\t".
	   "definition, but it was not found for product '%s'.\n\t".
	   "It must be specified in the Product defintion itself.",
  # $Err_Missing_Rqd_Field = 5 ($field_name, $product_name)
	   "The field '%s' is required for Products, but was\n\t".
	   "was not found for product '%s'.  It may be specified\n\t".
           "in the Product or config file.",
  # $Err_Irrelevant_Field = 6 ($field_name, $line, $file)
	   "The field '%s' (found on line %d of file\n\t'%s')\n\t".
           "is irrelevant in a Product definition.  It is being ignored.",
  # $Err_Dup_Field_Spec = 7 ($field_name, $line, $file)
	   "Multiple definitions of field '%s' have been found.  Using\n\t".
	   "the last instance, found on line %d of file\n\t'%s'.",
  # $Err_Could_Not_CD = 8 ($dir)
	      "Could cd to directory '%s'.",
  # $Err_Duplicate_Filesets = 9 ($fs_name)
	"Found multiple occurances of fileset '%s'.",
);
use vars(qw($Err_Could_Not_Open_File  $Err_Could_Not_Create_Dir
	    $Err_Could_Not_Find_File  $Err_Missing_Local_Field
	    $Err_Missing_Rqd_Field    $Err_Irrelevant_Field
	    $Err_Dup_Field_Spec	      $Err_Could_Not_CD
	    $Err_Duplicate_Filesets));

$Err_Could_Not_Open_File  =  1;
$Err_Could_Not_Create_Dir =  2;
$Err_Could_Not_Find_File  =  3;
$Err_Missing_Local_Field  =  4;
$Err_Missing_Rqd_Field    =  5;
$Err_Irrelevant_Field     =  6;
$Err_Dup_Field_Spec       =  7;
$Err_Could_Not_CD         =  8;
$Err_Duplicate_Filesets   =  9;

#*********************************************************************
# Object Implementation Section (ie. the code part) 
#*********************************************************************
#-----------------------------------------------------------------
# "Public" Methods
#-----------------------------------------------------------------
#=================================================================
# NAME     : constructor (new)
# PURPOSE  : create the specification object and initialize it.
# INPUTS   : none
# OUTPUTS  : none
# RETURNS  : pointer to object
#=================================================================
sub new
{
    my($class, $config, $file, $line, $parser) = @_;

    my $self = Pkg::Object->new($config, $file, $line, $parser);
    bless $self, $class;
    $self->Debug("class = $class");
    $self->initialize();
    return($self);
}


#=================================================================
# NAME     : mk_install
# PURPOSE  : perform the necessary operations to generate an
#            installable package.
# INPUTS   : 
#   $output_dir - pathname of directory in which the install 
#            image is to be placed.
# OUTPUTS  : an installable package
# NOTES    :
#   - Only install images are generated at this level.  Updates
#     are created at the fileset level.
# RETURNS  :
#   0 = success
#  !0 = Error.
#=================================================================
sub mk_install
{
    my($self, $output_dir) = @_;
    $self->Debug("$self->{Name}");
    my $rc = 0;

    if (($rc = $self->check_integrity()) < 8)
    {
	my ($fs_name, $working_dir);

	$self->{'_pkg_type'} = "Install";
	$self->{'_product_name'} = $self->{Name};
	chomp($self->{'_start_dir'} = `pwd`);

	$self->Log("Creating install package for '$self->{Name}'");
	if (($working_dir = $self->get_work_dir()))
	{
	    if (chdir($working_dir))
	    {
		unlink("INSLIST")  if (-f "INSLIST");
		unlink("LPFILE")   if (-f "LPFILE");
		$self->{'_inslist'}    = "INSLIST";
		$self->{'_lp_file'}    = "LPFILE";
		$self->{'_media_type'} = "I";

		# Have each fileset prepare for an install image...
		foreach $fs_name (@{$self->{'filesets'}})
		{
		    my $fs = $self->{Fileset_objs}{$fs_name};
		    $rc   |= $fs->prep_for_install();
		}
		# Do not put the pieces together if there was an error detected
		# above.
		#
		if ($rc < 8)
		{
		    $self->Log("Generating product level files");
		    $rc = $self->create_package($output_dir);
		    $self->Log("Package complete for '$self->{Name}'; " .
			       "rc = $rc");
		}
		chdir($self->{'_start_dir'});
	    }
	    else
	    {
		$self->error('Severe', $Err_Could_Not_CD, $working_dir, "$!");
		$rc = 16;
	    }
	}
	else
	{
	    $self->error('Severe', $Err_Could_Not_Create_Dir, 
			 "working dir", "$!");
	    $rc = 16;
	}
    }
    return($rc);
}


#=================================================================
# NAME     : liblpp file
# PURPOSE  : to record the files that should be included in 
#            a liblpp.a.  This method is called by the Fileset
#            objects when they have either created a file for
#            liblpp.a or when they have a field that identifies
#            such a file.
# INPUTS   :
#    $content - the install CONTENT affected by this file.
#               This variable is the single character translation
#               done by the Fileset methods (H = SHARE, U = USR,
#               B = ROOT);
#    $file    - pathname to the file (should really be full path
#               just to be safe).
# OUTPUTS  : none
# NOTES    :
#   - Instance variable is modified as a result of this call.
# RETURNS  :
#   0 = success
#  !0 = Error.
#=================================================================
sub liblpp_file
{
    my($self, $content, $filename) = @_;
    $self->Debug("$self->{Name}, content = $content, file = $filename");
    my $rc = 0;

    $self->record_liblpp_file($content, $filename);
    return($rc);
}


#=================================================================
# NAME     : check integrity
# PURPOSE  : The check_integrity method checks the integrity 
#            of the product object definition as a whole.  
#            Checks ared done for the minimum set of field.  
#            The check for irrelevant fields is done in the 
#            "store_valid_field" method.
# INPUTS   : none.
# OUTPUTS  : none
# NOTES    :
#   - Error messages are generated if necessary.
# RETURNS  :
#   0 = success
#  !0 = Error.
#=================================================================
sub check_integrity
{
    my($self) = @_;
    my $rc = 0;

    $self->Debug();

    my $rqd_field;
    foreach $rqd_field (keys %rqd_fields)
    {
	if ($rqd_fields{$rqd_field} eq "local")
	{
	    if (! defined($self->{$rqd_field}))
	    {
		$self->error("Error", $Err_Missing_Local_Field, 
			     $rqd_field, $self->{'Name'});
		$rc = 8;
	    }
	}
	else
	{
	    if ($self->get($rqd_field) eq "")
	    {
		$self->error("Error", $Err_Missing_Rqd_Field, 
			     $rqd_field, $self->{'Name'});
		$rc = 8;
	    }
	}
    }
    return($rc)
}


#=================================================================
# NAME     : store valid field
# PURPOSE  : validates the key/value pair and stores in the 
#            object if all is well
# INPUTS   : 
#    attr  - the field name (aka the key).
#    value - the value for the field.
#    line  - The line number containing this field
#    file  - The file in which the statement resides.
# OUTPUTS  : none
# NOTES    :
#   - ensures the attribute is a valid, known name
#   - values are not currently verified, but they need to be.
# RETURNS  :
#   0 = success
#  !0 = Error.
#=================================================================
sub store_valid_field
{
    my($self, $attr, $value, $line, $file) = @_;
    my $rc = 0;

    $self->Debug("Attr name = '$attr', line = $line, file = '$file'");

    if (($rc = $self->validate_field(\$attr, $value, $line, $file)) == 0)
    {
	if (scalar(grep(/^$attr$/, @irrelevant_fields)) > 0)
	{
	    $self->error("Warning", $Err_Irrelevant_Field,
			 $attr, $line, $file);
	    $rc = 2;
	}
	else
	{
	    if (defined($self->{$attr}))
	    {
		$self->error("Warning", $Err_Dup_Field_Spec,
			     $attr, $line, $file);
		$rc = 5;
	    }
#	    $self->Debug("Storing field '$attr'");
	    $self->{$attr} = $value;
	}
    }
    return($rc);
}

#=================================================================
# NAME     : add fileset
# PURPOSE  : verifies that the given fileset object belongs
#            to this product and adds it to the Fileset_objs
#            set if it does.
# INPUTS   : 
#    fs  - ptr to the fileset object to be added.
# OUTPUTS  : none
# NOTES    : none
# RETURNS  :
#   0 = success
#   1 = Fileset does not belong to this product.
#  >1 = some other error occured
#=================================================================
sub add_fileset
{
    my($self, $fs) = @_;
    my $rc = 0;

    $rc = scalar(grep(/^$fs->{Name}$/, @{$self->{'filesets'}}));
    if ($rc == 1)
    {
	# Fileset belongs here, so save it.
	$self->{Fileset_objs}{$fs->{'Name'}} = $fs;
	$fs->link_product($self);
	$rc = 0;
	$self->Debug("Fileset '$fs->{'Name'} to product '$self->{'Name'}");
    }
    elsif ($rc == 0)
    {
	$rc = 1;
    }
    else
    {
	# Found multiple hits in the list for this fileset...
	$self->error("Error", $Err_Duplicate_Filesets, $fs->{Name});
    }
    return($rc);
}
#-----------------------------------------------------------------
#-----------------------------------------------------------------
# "Private" Data and Methods
#-----------------------------------------------------------------
#-----------------------------------------------------------------
#=================================================================
# NAME     : initialize
# PURPOSE  : initializes the newly created Pkg::Product object
# INPUTS   : none
# OUTPUTS  : none
# RETURNS  : nothing
#=================================================================
sub initialize
{
    my($self) = @_;
    $self->Debug();

    $self->Pkg::Packager::initialize();

    $self->register_errors(\@err_table);
    $self->{Fileset_objs} = {};              # ptr to hash for Fileset objects
}


#==========================================================================
# Name     : build input path
# Purpose  : The bld_input_path method constructs the input path based
#            upon object attributes (name, vrmf) and the user supplied 
#            directory list (input_path).
# Inputs   : none
# Outputs  :
#     - private instance variable (_input_path) is modified.
# NOTES    :
#   The search path should be the following:
#     1) The current directory
#     2) the directory from which the command was executed ('_start')
#     3) the directory containing the file in which this object
#        was defined ('_src_dir')
#     4) For each non-absolute pathname in the 'input_path', make it
#        relative to the to pwd, start dir, and source dir. 
# Returns  :
#    SUCCESS = full pathname to the file
#    FAILURE = null string
#==========================================================================
sub bld_input_path
{
    my $self = shift;

    my $path = "";
    my $input_path;
    my $pwd;

    chomp($pwd = `pwd`);
    my $dir;
    foreach $dir ($pwd, $self->get('_start_dir'), $self->get('_src_dir'))
    {
	$input_path .= ":" . $dir . "/$self->{Name}" . ":" . $dir;	    
    }
    my $in_path = $self->get('input_path');
    if (defined($in_path))
    {
	foreach $path (@{$self->get('input_path')})
	{
	    if (substr($path, 0, 1) ne "/")
	    {
		foreach $dir ($pwd,
			      $self->get('_start_dir'), 
			      $self->get('_src_dir'))
		{
		    $input_path .= ":" . $dir . "/" . $path;
		}
	    }
	    else
	    {
		$input_path .= ":" . $path;
	    }
	}
    }
    $self->{'_input_path'} = $input_path
} # END bld_input_path


#=================================================================
# NAME     : get
# PURPOSE  : get the requested field from somewhere in the
#            pecking order.
# INPUTS   : 
#   - self  - the object ptr
#   - field - name of field to be obtained.
# OUTPUTS  : none
# NOTES    :
#   - might should add some logging to indicate from which
#     object the value was obtained.  That would complicate
#     the code though.
# RETURNS  : 
#   - value if found
#   - undef if not found
#=================================================================
sub get
{
    my($self, $field) = @_;
    $self->Debug("$self->{Name} '$field'...");
    return(defined($self->{$field}) ? $self->{$field} :
	   $self->{Config}->get($field));
}


#=================================================================
# NAME     : get work dir
# PURPOSE  : get pathname of the workdir and ensure that it
#            exists
# INPUTS   : none
# OUTPUTS  :
#    - may create the working directory.
# NOTES    :
#   * This method is intended to be a private method.
# RETURNS  : 
#   pathname = SUCCESS,  working dir now exists 
#              (may have been created)
#   null str = FAILURE, working dir does not exists and could
#              not be created.
#=================================================================
sub get_work_dir
{
    my $self = shift;

    $self->Debug("prod = $self->{'Name'}");

    # Some initial setup stuff (maybe this should be 
    # a separate function)
    #
    my $work_dir = $self->get('_work_dir');
    if (!defined($work_dir))
    {
	$work_dir = $self->get('work_dir');
	if (!defined($work_dir))
	{
	    $work_dir = "WORK_$self->{Name}";
	}
	if (substr($work_dir, 0, 1) ne "/")
	{
	    $work_dir = "$self->{'_start_dir'}/$work_dir";
	}
	if (! -d $work_dir)
	{
	    if (! mkdir($work_dir, 0755))
	    {
		$self->error('Error', $Err_Could_Not_Create_Dir, 
			     $work_dir, $!);
		$work_dir = undef;
	    }
	}
	$self->{'_work_dir'} = $work_dir;
    }
    return($work_dir);
}


#==========================================================================
# Name     : create <product>.lp file
# Purpose  : The product .lp file is created after successfully
#            doing all fileset work.  It creates the <product>.lp 
#            file based on information discovered while doing the
#            Fileset work AND from information contained in the 
#            Product definition.
# Inputs   :
# Outputs  :
#   - $self->{Name}.lp file is created.
# NOTES    :
#    - error messages are generated and so do not need to be
#      by the caller.
#    - It is assumed that checks for required fields has already
#      been performed -- no definition validity is checked here.
# Returns  :
#    filename on success
#    void string on failure
#==========================================================================
sub create_lp_file
{
    my($self) = @_;
    $self->Debug();
    my $lp = undef;

    my $work_dir = $self->get_work_dir();
    $lp = "$work_dir/$self->{Name}.lp";
    if (open(LP, ">$lp"))
    {
	my $rc = 0;

	# Use the recorded content and boot requirements 
	# from the Filesets.
	#
	my $content = $self->get('_content');
	my $boot_flag = $self->get('_boot_flag');


	# The closing must end in a newline, but it may
	# include the optional "comments" field.
	#
	my $closing = "\n" . $self->get('comments');
	if (substr($closing, length($closing)-1, 1) ne "\n")
	{
	    $closing .= "\n";
	}

	print LP "$self->{Name} $boot_flag $content ", 
	$self->get('language'), " ", $self->get('description'), 
	$closing;
	close(LP);
	$self->{'_lp_file'} = $lp;
    }
    else
    {
	$self->error('Error', $Err_Could_Not_Open_File, $lp, "$!");
	undef $lp;
    }
    return($lp);
}
1;
