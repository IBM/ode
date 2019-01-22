#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++  
#  
# (C) COPYRIGHT International Business Machines Corp. 1997
# All Rights Reserved 
#  
# US Government Users Restricted Rights - Use, duplication or 
# disclosure restricted by GSA ADP Schedule Contract with IBM Corp. 
#
# ORIGINS: 27
#
#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
#
# NAME        : Pkg::Update Class
# DESCRIPTION :
#    The 'Pkg::Update' Class implements the Update object for the
#    pkgtools toolset.  This class stores the definition of a
#    fileset update found in an update definition file and then
#    provides the methods necessary to create the update package 
#    acceptable to AIX Version 4.x's installp command.
#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package Pkg::Update;

#
# Standard Package Preamble
#
require 5.002;
require Exporter;
require Pkg::Fileset;
require Pkg::Packager;

@ISA    = qw(Exporter Pkg::Fileset Pkg::Packager);  

use English;
use strict;
use ODM_stanza_file;

#*********************************************************************
# "Static" variables for the Update object
#
use vars(qw(%rqd_fields @irrelevant_fields @local_fields
	    $log @err_table));

# %rqd_fields identifies the minimum set of fields that must
# be defined in order to create an update package.
# The value part is either 'local' which means that the field
# must be defined within the update itself, or 'parent'
# which means the field can be defined in a "parent".
#
%rqd_fields = (
	       content         => "parent",
	       description     => "parent",
	       boot_rqmt       => "parent",
	       inslist         => "parent",
	       shipfiles       => "local",
	       version         => "parent",
	       release         => "parent",
	       maint_level     => "parent",
	       fix_level       => "local",
#	       fix_info_file   => "parent",
	      );
#
# @irrelevant_fields is a list of fields that are irrelevant
# in Update object definitions.  These fields should appear
# somewhere else (Fileset, Product or Config file right now).
#
@irrelevant_fields = (qw(filesets));
#
# @local_fields is a list of the fields that must be found locally
# to the Update object.  In other words, when one of these fields
# is requested, it must be found in the current Update object.
# The "parent" objects cannot be searched.
#
@local_fields = (qw(control_files        fix_level
		    odm_add_files        pd_file
		    problems_fixed       requisites
		    root_add_files       root_control_files   
		    shipfiles 	         v_reqs));
#
# $log is a pointer to the log object.  This object provides
# the logging, error reporting, and debug message facilities.
# It should be initialized before the 1st Fileset object is
# created.  However, if it is not, it will be when the 1st 
# Fileset object is created.
#
$log = undef;

#
# @err_table contains the error message formats for the 
# various error messages.  Note that this table is just
# an array, and so the strings need to be in the correct
# "slot" in order to be found for the correct error.
#
@err_table = ("No Error",
  # $Err_Could_Not_Open_File = 1 ($file_name, $errno_msg)
	   "Could not create file (%s); $!",
  # $Err_Could_Not_Create_Dir = 2 ($dir_name, $errno_msg)
	   "Could not create directory '%s'; $!\n",
  # $Err_Could_Not_Find_File = 3 ($file_name)
	   "Could not find file '%s'",
  # $Err_Missing_Local_Field = 4 ($field_name, $fileset)
	   "The field '%s' is required to be in the Update,\n\t".
	   "definition but it was not found for update '%s'.\n\t".
	   "It must be specified in the Update defintion itself.",
  # $Err_Missing_Rqd_Field = 4 ($field_name, $fileset_name)
	   "The field '%s' is required for Updates, but\n\t".
	   "was not found for update '%s'.  It may be\n\t".
           "specified in the Update, Fileset, Product, or config file.",
  # $Err_Irrelevant_Field = 6 ($field_name, $line, $file)
	   "The field '%s' (found on line %d of file\n\t'%s')\n\t".
           "is irrelevant in an Update definition.  It is being ignored.",
  # $Err_Dup_Field_Spec = 7 ($field_name, $line, $file)
	   "Multiple definitions of field '%s' have been found.  Using\n\t".
	   "the last instance, found on line %d of file\n\t'%s')",
  # $Err_Could_Not_CD = 8 ($dir)
	      "Could cd to directory '%s'.",
  # $Err_No_Copyright_Info = 9 ($fileset_name)
	   "No Copyright info was found for update to Fileset '%s'\n",
  # $Err_No_Objclass_DB = 10
	   "Update contains ODM files, but no ODM class definition was found",
  # $Err_No_ODM_History = 11
	   "Update contains ODM files, but no ODM history directory was found",
  # $Err_No_Fix_Info = 12
	   "Update identifes fixed problems, but no fix info file was found",
  # $Err_No_Problem_List = 13
	   "Update does not identify any fixed problems",
  # $Err_Gen_Inslist_Failed = 14
	   "Generation of the update inslist failed",
  # $Err_No_ODM_Hist_File = 15 ($add_file, $history_dir, $fileset_name)
	   "Could not find history file for '%s' in the history directory " .
	   "'%s', or a subdirectory '%s'.",
  # $Err_Nothing_To_Update = 16 ($fileset)
	   "There is nothing to update for '%s'.  You must specify at least " .
	   "one of 'shipfiles', 'control_files', 'odm_add_files', " .
	   "'root_control_files', or 'root_odm_files'." ,
);
use vars(qw($Err_Could_Not_Open_File  $Err_Could_Not_Create_Dir
	    $Err_Could_Not_Find_File  $Err_Missing_Local_Field  
	    $Err_Missing_Rqd_Field    $Err_Irrelevant_Field
	    $Err_Dup_Field_Spec	      $Err_Could_Not_CD
	    $Err_No_Copyright_Info    $Err_No_Objclass_DB
	    $Err_No_ODM_History	      $Err_No_Fix_Info
	    $Err_No_Problem_List      $Err_Gen_Inslist_Failed
	    $Err_No_ODM_Hist_File     $Err_Nothing_To_Update));


$Err_Could_Not_Open_File  =  1;
$Err_Could_Not_Create_Dir =  2;
$Err_Could_Not_Find_File  =  3;
$Err_Missing_Local_Field  =  4;
$Err_Missing_Rqd_Field    =  5;
$Err_Irrelevant_Field     =  6;
$Err_Dup_Field_Spec       =  7;
$Err_Could_Not_CD         =  8;
$Err_No_Copyright_Info    =  9;
$Err_No_Objclass_DB       = 10;
$Err_No_ODM_History       = 11;
$Err_No_Fix_Info          = 12;
$Err_No_Problem_List      = 13;
$Err_Gen_Inslist_Failed   = 14;
$Err_No_ODM_Hist_File     = 15;
$Err_Nothing_To_Update    = 16;

#*********************************************************************
# Object Implementation Section (ie. the code part) 
#*********************************************************************
#-----------------------------------------------------------------
# "Public" Methods
#-----------------------------------------------------------------
#=================================================================
# NAME     : constructor (new)
# PURPOSE  : create the update object and initialize it.
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

    my $self = Pkg::Fileset::new($class, $config, $file, $line, $parser);
    bless $self, $class;
    $self->Debug();
    $self->initialize();
    return($self);
}


#=================================================================
# NAME     : mk_update
# PURPOSE  : perform the necessary operations to generate an
#            update package
# INPUTS   : 
#   $output_dir - pathname of directory in which the install 
#            image is to be placed.
# OUTPUTS  : an installable package
# NOTES    :
# RETURNS  :
#   0 = success
#  !0 = Error.
#=================================================================
sub mk_update
{
    my($self, $output_dir) = @_;
    $self->Debug("$self->{Name}");
    my $rc = 0;

    # Set package type and create VRMF strings.
    $self->{'_short_VRMF'} = sprintf("%d.%d.%d.%d",
				   $self->get('version'),
				   $self->get('release'),
				   $self->get('maint_level'),
				   $self->get('fix_level'));
    $self->{'_long_VRMF'}  = sprintf("%d.%d.%d.%d",
				   $self->get('version'),
				   $self->get('release'),
				   $self->get('maint_level'),
				   $self->get('fix_level'));
    if (($rc = $self->check_integrity()) < 8)
    {
	$self->Log("Creating update package for '$self->{Name}'");

	chomp($self->{'_start_dir'} = `pwd`);
	my ($working_dir);

	if ($working_dir = $self->get_work_dir())
	{
	    if (chdir($working_dir))
	    {
		# Create the .lp file;
		# NOTE - we cannot do anything without the .lp file.
		$rc =  $self->create_lp_file();

		if ($rc < 8)
		{
		    $rc |= $self->generate_inventory_info();
		    $rc |= $self->generate_copyright_info();
		    $rc |= $self->generate_requisites($self->special_reqs());
		    $rc |= $self->generate_ODM_scripts();
		    $rc |= $self->generate_supersedes();
		    $rc |= $self->handle_control_files();
		    $rc |= $self->generate_problem_info();
		}
		# Do not put the pieces together if there was an error 
		# detected above.
		#
		if ($rc < 8)
		{
		    if (! defined($self->{'image_name'}))
		    {
			$self->{'image_name'} = $self->{Name} . "_" .
			                        $self->{'_short_VRMF'};
		    }
		    $rc = $self->create_package($output_dir);
		    $self->Log("Update complete for '$self->{Name}'; " .
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
# NAME     : store valid field
# PURPOSE  : validates the key/value pair and stores in the 
#            object if all is well
# INPUTS   : 
#    attr  - the field name (aka the key).
#    value - the value for the field.
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
	    $self->{$attr} = $value;
	    # Updates have the option of specifying the pd file
	    # containing the fileset definition for the fileset
	    # that is being updated.
	    # So, if this attribute is the 'pd_file' attribute,
	    # find the file and give the full pathname to the 
	    # parser.
	    if ($attr eq 'pd_file')
	    {
		my $pd_file = $self->find_file($value);
		if ($pd_file ne "")
		{
		    $self->{'_parser'}->add_pd_file($pd_file);
		}
		else
		{
		    $self->error("Warning", $Err_Could_Not_Find_File, 
				 $pd_file);
		    $rc = 5;
		}
	    }
	}
    }
    return($rc);
}


#=================================================================
# NAME     : check integrity
# PURPOSE  : The check_integrity method checks the integrity 
#            of the update definition as a whole.  Checks are
#            done for the minimum set of fields.  The check for
#            irrelevant fields is done in the "store_valid_field"
#            method.
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
    # If all required fields are present, we must do some
    # more complicated verfications...
    #     1) At least one of the following fields must be
    #        be present
    #           - shipfiles
    #           - control_files
    #           - odm_add_files
    #           - root_control_files
    #           - root_odm_files
    #     2) VRMF must be in valid format.
    if ($self->get('shipfiles')          eq "" &&
        $self->get('control_files')      eq "" &&
        $self->get('odm_add_files')      eq "" &&
        $self->get('root_control_files') eq "" &&
        $self->get('root_odm_files')     eq "")
    {
	$self->error("Error", $Err_Nothing_To_Update, 
		     $self->{'Name'}, $self->{'_short_VRMF'});
	$rc = 8;
    }
    $rc = $self->valid_vrmf();
    return($rc)
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
# NAME     : link fileset
# PURPOSE  : makes the link from this update to the fileset
#            to which it belongs.
# INPUTS   :
#     $fs - ptr fileset object
# OUTPUTS  : none
# NOTES    :
#   - This method will only be called if a "pd_file" field is
#     present in the update definition object.  If not present,
#     then fields defined in the Fileset definition cannot be
#     used (or found).
# RETURNS  : 
#   nothing.
#=================================================================
sub link_fileset
{
    my($self, $fs) = @_;
    $self->Debug("fs = $fs->{Name}");

    $self->{'Fileset'} = $fs;
    $self->{'_parent'} = $fs;
}


#-----------------------------------------------------------------
#-----------------------------------------------------------------
# "Private" Data and Methods
#-----------------------------------------------------------------
#-----------------------------------------------------------------
#=================================================================
# NAME     : initialize
# PURPOSE  : initializes the newly created Pkg::Update object
# INPUTS   : none.
# OUTPUTS  : none
# RETURNS  : nothing
#=================================================================
sub initialize
{
    my $self = shift;
    $self->Debug();

    $self->{'Product'}  = undef;   # ptr to product to which this fs belongs
    $self->{'_parent'}  = undef;

    $self->{'_pkg_type'}   = "Update";
    $self->{'_media_type'} = "S";

    $self->register_errors(\@err_table);
    $self->Pkg::Packager::initialize();

    return;
}


#=================================================================
# NAME     : get
# PURPOSE  : get the requested field from somewhere in the
#            pecking order.
# INPUTS   : 
#   - field - name of field to be obtained.
# OUTPUTS  : none
# NOTES    :
#   - If the "_parent" link is not defined, all fields must
#     be pulled locally.  If it is defined, any field not
#     found locally will be requested from the "parent".
# RETURNS  : 
#   - value if found
#   - undef if not found
#=================================================================
sub get
{
    my($self, $field) = @_;
    $self->Debug("$self->{Name} '$field'...");
    return(defined($self->{$field}) ? $self->{$field} :
	   ((scalar(grep /^($field)$/, @local_fields) == 0) &&
	    defined($self->{'_parent'}))
	           ? $self->{'_parent'}->get($field)    
	           : undef);
}


#=================================================================
# NAME     : get work dir
# PURPOSE  : get pathname of the workdir and ensure that it
#            exists
# INPUTS   : none
# OUTPUTS  :
#    - may create the working directory.
# NOTES    :
#   - This method is equivalent to the same method in the Product
#     class.  However, in this class, the user can specify a
#     "work_dir" field to better locate the work info.  This field
#     is provided because it is invisioned that several updates
#     for different filesets could be defined and built from a
#     single update definition file.  It might be desired that
#     the working directories be kept separate, in this case, to
#     facilitate easier debug of any issues that may arise.
# RETURNS  : 
#   pathname = SUCCESS,  working dir now exists 
#              (may have been created)
#   null str = FAILURE, working dir does not exists and could
#              not be created.
#=================================================================
sub get_work_dir
{
    my($self) = @_;

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
	    $work_dir = "WORK_$self->{Name}_$self->{'_short_VRMF'}";
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
# Name     : special requisites
# Purpose  : 'special_reqs' handles the case of generating "special"
#            requisites that a unique to updates.
# Inputs   :
# Outputs  :
# NOTES    :
#   - Updates have to handle the case of the "base_level" of a fileset
#     being different than the VRMF default base_level.  The VRMF default
#     is a VRMF value that ends in 0.0 - such as 4.2.0.0.
#     However, it is possible (and has been done by AIX) to define 
#     a different base level value.  The base_level identifies the oldest
#     VRMF level that a maintanence level Update can be applied to.
#   - The base level is only relevant when generating maintanence level
#     updates.  A Maintanence level update is identified by a VRMF 
#     that has a value greater than 0 in the 'maint_level' field and
#     a value of 0 in the 'fix_level' field.
# Returns  :
#   SUCCESS = pathname of the subset inslist file.
#   FAILURE = null pathname.
#==========================================================================
sub special_reqs
{
    my $self = shift;
    $self->Debug();
    my $reqs = [];

    if (($self->get('maint_level') > 0) && ($self->get('fix_level') == 0))
    {
	# Have a maintanence level update, base_level is relevant!
	my $base_level = $self->get('base_level');
	if ($base_level =~ /^\d+\.\d+\.0+\.0+$/o)
	{
	    # Base_level is not the default.  Special requisites required.
	    push(@{$reqs}, "*prereq $self->{Name} $base_level");
	}
	else
	{
	    # No modification to the default base_level has been made.
	    # Therefore, no special requisites are required.
	    $reqs = undef;
	}
    }
    else
    {
	# This update is NOT a maintanence level.  Base_level is irrelevant.
	# Therefore, no special requisites are required.
	$reqs = undef;
    }
    return($reqs);
}


#==========================================================================
# Name     : generate problem information
# Purpose  : generate_problem_info creates the fixdata file that is
#            in liblpp.a and the problem fix info section of the TOC.
# Inputs   :
# Outputs  :
#   - fixdata file: The ODM stanza file containing the problem fix
#            stanzas for this update.
#   - keyword_info: the keyword section of the TOC.  
# NOTES    :
#   - the fixdata file is in ODM add file stanza format.  It identifies
#     the problem (or keyword), abstract, symptom text, and a list of
#     updates needed to fix the problem.
#   - The keyword_info file goes into the TOC (lpp_name).  It contains
#     a single line for each keyword/problem.  The first item on the
#     the line is the keyword (problem id) followed by the number of
#     filesets needed to fix the problem.  Finally the keyword/problem
#     abstract is last.
# Returns  :
#   SUCCESS = 0
#   FAILURE = non-zero
#==========================================================================
sub generate_problem_info
{
    my $self = shift;
    $self->Debug();
    my $rc = 0;

    my $work_dir = $self->get_work_dir();
    my $key_file = "$work_dir/key_info";
    my $fixdata  = "$work_dir/" . $self->{Name} . ".fixdata";
    unlink($key_file);
    unlink($fixdata);

    my $full_fix_info = $self->find_file($self->get('fix_info_file'));
    my $problem_list  = $self->get('problems_fixed');
    if (defined($problem_list) && defined($full_fix_info))
    {
	# got something to look at; lets see if we can open everything up...
	$self->Debug("Loading fix info '$full_fix_info'");
	my $start = (times)[0];
	my $stanza_file = ODM_stanza_file->new($full_fix_info);
	if (defined($stanza_file))
	{
	    $self->Debug($stanza_file->get_stanza_cnt() . " stanzas in " .
			 ((times)[0] - $start) . " seconds.");
	    if (open(KEY, ">$key_file"))
	    {
		if (open(FIX, ">$fixdata"))
		{
		    $start = (times)[0];
		    # Now that all files are open and the stanza file
		    # is loaded, loop thru the problem fix list and
		    # act on each problem/keyword specified.
		    my($problem, $crit);
		    foreach $problem (@{$problem_list})
		    {
			$crit .= "$problem|";
		    }
		    $crit = substr($crit, 0, length($crit) - 1);
		    my $stanza_set = $stanza_file->find_stanza('fix',
						{'name' => "^($crit)\$"});
		    my $matches = 0;
		    my($stanza, $abs);
		    foreach $stanza (@{$stanza_set})
		    {
			# Got a stanza match.
			# spit out the TOC info line
			# and then print the whole stanza to the
			# local fixdata file.
			$abs = $stanza->get_value('abstract');
			$abs =~ s/^\"(.*)\"$/$1/s;  # strip quotes (")
			print(KEY $stanza->get_value('name'), " ",
			      $self->fs_cnt($stanza->get_value('filesets')),
			      " ", $abs, "\n");
						
			$stanza->print(\*FIX);
			$matches++;
		    }
		    close(FIX);
		    close(KEY);

		    $self->Debug("matched $matches out of " . 
				 scalar(@{$problem_list}) . " in " .
				 ((times)[0] - $start) . " seconds.");
		    if ($matches > 0)
		    {
			# had some matches, so record the files.
			$self->liblpp_file($self->get('_content') eq 'H' 
					       ? 'H' : 'U', 
					   $fixdata);
			$self->{'_key_info'} = $key_file;
		    }
		    else
		    {
			# should probably have some kind of message here.
			unlink($fixdata);
			unlink($key_file);
		    }
		}
		else
		{
		    $self->error('Error', $Err_Could_Not_Open_File, 
				 $fixdata, $!);
		    $rc = 4;
		}
	    }
	    else
	    {
		$self->error('Error', $Err_Could_Not_Open_File, 
			     $key_file, $!);
		$rc = 4;
	    }
	}
	else
	{
	    $rc = 4;
	}
    }
    else
    {
	if (!defined($problem_list))
	{
	    $self->error('Warning', $Err_No_Problem_List);
	    $rc = 4;
	}
	if (!defined($full_fix_info))
	{
	    $self->error('Warning', $Err_No_Fix_Info);
	    $rc = 4;
	}
    }
    return($rc);
}

#==========================================================================
# Name     : generate subset inslist
# Purpose  : gen_subset_inslist creates a subset inslist from the
#            full inslist.  The subset contains the entries needed
#            for the files listed in the 'shipfiles' field.
# Inputs   :
# Outputs  :
#   - File created called 'INSLIST' that contains the subset.
# NOTES    :
#   - The file name of the created file can be anything (it is given
#     to the Packager class via the '_inslist' field).  The name used
#     is consistent with the Product/Fileset classes.
#   - The 'ptfins' command is used to generate the subset file.  This
#     command requires the list of desired ship files to be in a file.
#   - It also requires an env var to be set 'TOP'.  The var must be
#     be set, but it does not matter what it is set to.
#   - Debug output from 'ptfins' can be enabled by setting the
#     'DEBUG_PTFINS' env var.
# Returns  :
#   SUCCESS = pathname of the subset inslist file.
#   FAILURE = null pathname.
#==========================================================================
sub gen_subset_inslist
{
    my $self = shift;
    $self->Debug();

    my $work_dir = $self->get_work_dir();
    my $sil = "$work_dir/$self->{Name}.il";

    unlink($sil);
    my $shipfiles = $self->get('shipfiles');
    if ($shipfiles)
    {
	my $filelist = "$work_dir/filelist";
	if (open(SF, ">$filelist"))
	{
	    my $file;
	    foreach $file (@{$shipfiles})
	    {
		print(SF "$file\n");
	    }
	    close(SF);

	    my $cmd = "ptfins -f $filelist -o $self->{Name} -i " . 
		  $self->find_file($self->get('inslist'));
	    if ($ENV{'TOP'} eq "")
	    {
		$cmd = "TOP=" . $work_dir . " $cmd";
	    }
	    if ($self->execute_cmd($cmd) != 0)
	    {
		$self->error('Error', $Err_Gen_Inslist_Failed);
		$sil = undef;
	    }
	}
	else
	{
	    $self->error('Error', $Err_Could_Not_Open_File, "$filelist", $!);
	    $sil = undef;
	}
	
    }
    else
    {
	if (system("touch $sil") != 0)
	{
	    $self->error('Error', $Err_Could_Not_Open_File, "$sil", $!);
	    $sil = undef;
	}
    }
    $self->{'_inslist'} = $sil;    
    return($sil);
}

#==========================================================================
# Name     : create odm scripts
# Purpose  : This function actually creates the odm scripts for
#            each of the add files listed in the $add_files array.
# Inputs   :
#    $add_files- reference to an array of addfile names.
#                NOTE that these names may have double quotes 
#                     around them.
#    $content  - The package content to which these add files belong.
#                This info is needed when adding the scripts to the
#                list of files for liblpp.
#    $out_dir  - The directory into which the scripts are to be placed.
# Outputs  :
#   - output is really created by 'mkodmupdt'.
# NOTES    :
#   - several odm scripts will be generated.  There will be a set
#     of scripts for each .add file identified in the pkg specification. 
# Returns  :
#    0 = SUCCESS
#   !0 = failure; error messages will be generated here.
#==========================================================================
sub create_odm_scripts
{
    my($self, $add_files, $content, $out_dir) = @_;
    my $rc = 0;
    $self->Debug("content = $content; ");

    my $objclass_db = $self->find_file($self->get('odm_class_def'));
    my $odm_hist    = $self->get('odm_history_dir');

    if ($objclass_db ne "" && $odm_hist ne "")
    {
	my $add_file;
	foreach $add_file (@{$add_files})
	{
	    $add_file =~ s/^\s*\"\s*(.*?)\s*\"\s*$/$1/o;     # strip quotes (")
	    my $add_path = $self->find_file($add_file);
	    if ($add_path ne "")
	    {
		# Find the history file for this specific add file.
		# The history file will either be directly in the
		# history dir, or in a fileset subdirectory of
		# the history dir.
		my $hist_file = $self->find_file("$odm_hist/$add_file");
		if ($hist_file eq "")
		{
		    $hist_file = $self->find_file(
					"$odm_hist/$self->{Name}/$add_file");
		}
		if ($hist_file ne "")
		{
		    my $cmd = "mkodmupdt -u -c $add_path -o $self->{Name} " .
			  "-t $objclass_db -p $hist_file -d $out_dir";
		    if ($self->execute_cmd($cmd) == 0)
		    {
			# Add any output files to the liblpp list.
			#
			$add_file = substr($add_file, 0, 
					   rindex($add_file, "."));
			my $dirname = "$out_dir/$self->{Name}.$add_file.";
			my($file, $ext);
			foreach $ext (@Pkg::Fileset::_ODM_scripts)
			{
			    $file = $dirname . "$ext";
			    $self->liblpp_file($content, $file)  if (-s $file);
			}
		    }
		    else
		    {
			# No message required.  'mkodmupdt' spits out
			# its own error messages.
			$rc = 8;
		    }
		}
		else
		{
		    $self->error('Error', $Err_No_ODM_Hist_File, $add_file,
				 $odm_hist, $self->{Name});
		    $rc = 8;
		}
	    }
	    else
	    {
		$self->error('Error', $Err_Could_Not_Find_File,
			     $add_file);
		$rc = 8;
	    }
	}
    }
    else
    {
	# The Objclass DB and/or the ODM history directory were
	#     not specified.
	$self->error('Error', $Err_No_Objclass_DB) if ($objclass_db eq "");
	$self->error('Error', $Err_No_ODM_History) if ($odm_hist    eq "");
	$rc = 8;
    }
    return($rc);
} # END create_odm_scripts


#=================================================================
# NAME     : liblpp file
# PURPOSE  : to record the files that should be included in 
#            a liblpp.a.  This method is called to record all
#            liblpp based files (whether created or supplied).
# INPUTS   :
#    $content - the install CONTENT affected by this file.
#               This variable is the single character translation
#               done by the Fileset/Update methods (H = SHARE,
#               U = USR, B = ROOT);
#    $file    - pathname to the file (should really be full path
#               just to be safe).
# OUTPUTS  : none
# NOTES    :
#   - Instance variable is modified as a result of this call.
#   - This method overrides the method in the Fileset class.
#     For Update objects, the liblpp file info must be kept
#     locally since the package will be created from within
#     this object.  The Fileset class just passes the info on
#     to the Product object.
# RETURNS  :
#   0 = success
#  !0 = Error.
#=================================================================
sub liblpp_file
{
    my($self, $content, $filename) = @_;
    $self->Debug("$self->{Name}, content = $content, file = $filename");

    return($self->record_liblpp_file($content, $filename));
}


#=================================================================
# NAME     : record boot flag
# PURPOSE  : to record the boot requirements flag of the update
# INPUTS   :
#    $boot_flag - The boot_flag from the fileset.
# OUTPUTS  : none
# NOTES    :
#   - Instance variable is modified as a result of this call.
# NOTES    :
#   - Instance variable is modified as a result of this call.
#   - This method overrides the method in the Fileset class.
#     For Update objects, the content info must be kept locally
#     since the package will be created from within this
#     object.  The Fileset class just passes the info on to the
#     Product object.
# RETURNS  :
#   0 = success
#  !0 = Error.
#=================================================================
sub record_boot_flag
{
    my($self, $boot_flag) = @_;
    $self->Debug("$self->{Name}, flag = $boot_flag");

    $self->{"_boot_flag"} = $boot_flag;
    return(0);
}


#=================================================================
# NAME     : record content
# PURPOSE  : to record the content of the package.
# INPUTS   :
#    $content - the install CONTENT affected by this file.
#               This variable is the single character translation
#               done by the Fileset/Update methods (H = SHARE,
#               U = USR, B = ROOT);
# OUTPUTS  : none
# NOTES    :
#   - Instance variable is modified as a result of this call.
#   - This method overrides the method in the Fileset class.
#     For Update objects, the content info must be kept locally
#     since the package will be created from within this
#     object.  The Fileset class just passes the info on to the
#     Product object.
# RETURNS  :
#   0 = success
#  !0 = Error.
#=================================================================
sub record_content
{
    my($self, $content) = @_;
    $self->Debug("$self->{Name}, content = $content");

    $self->{'_content'} = $content;
    return(0);
}


sub get_product_name
{
    my $self = shift;
    $self->Debug("_product_name = '$self->{'_product_name'}'");

    if (!defined($self->{'_product_name'}))
    {
	$self->{'_product_name'} = (defined($self->{'product_name'}))
				     ? $self->{'product_name'}
				     : $self->{'_parent'}{Product}{Name};
    }
    return($self->{'_product_name'});
}



#=================================================================
# NAME     : fileset count
# PURPOSE  : fs_cnt counts the number of filesets listed
#            in the string passed in.
# INPUTS   :
#    $fs_list : string of fileset/VRMFs.  There is one per line.
# OUTPUTS  : none
# NOTES    :
#   - The fileset list is a multi-line string (possibly containing
#     continuation characters -- '\n\' -- which must be stripped).
#   - Each line contains an update identification (fileset and VRMF).
#     The fileset and VRMF are separated by a colon (':').
# RETURNS  :
#    number of filesets needed to fix.
#=================================================================
sub fs_cnt
{
    my $self = shift;
    my($fs_list) = @_;

    my $cnt = 0;
    my($upd, $fs, $vrmf, $rem);

    $fs_list =~ s/\\n\\//sg;                        # remove continuation chars
    $fs_list =~ s/^\"(.*)\"$/$1/s;                  # strip double quotes (")
    while ($fs_list ne "")
    {
	($upd, $rem) = split(/\n/, $fs_list, 2);
	$fs_list = $rem;
	($fs, $vrmf) = split(/:/, $upd);

	# count the fileset if the VRMF part looks valid
	$cnt++ if ($vrmf =~ m/^\s*\d{1,2}\.\d{1,2}\.\d{1,4}\.\d{1,4}\s*$/);
    }
    return($cnt);
}
1;
