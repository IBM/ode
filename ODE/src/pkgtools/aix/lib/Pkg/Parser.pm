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
# NAME        : Pkg::Parser Class
# DESCRIPTION :
#    The 'Pkg::Parser' Class implements the reading and loading of
#    product definition and config files.  It is responsible for
#    parsing these files and verifying language syntax.
#--------------------------------------------------------------------------
# Change History:
#   $Log: Parser.pm,v $
#   Revision 1.0.1.2  1997/08/28 15:09:36  bender
#    Added ability to add addition .pd files to the list of pd files to load.
#    This rqmt needed by Update definitions that identify their full .pd file.
#    Also added support for loading and processing Update objects.
#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package Pkg::Parser;

#
# Standard Package Preamble
#
require 5.002;
require Exporter;
require Pkg::Log;
@ISA    = qw(Exporter Pkg::Log);

use Pkg::Tokenizer;
use English;
use FileHandle;
use Getopt::Long;
STDOUT->autoflush;

use Pkg::Product;
use Pkg::Fileset;
use Pkg::Update;

use strict;
#*********************************************************************
# The Pkg::Parser module implements the logic to parse a given
# package specification file.  It is implemented as an object.
# The primary method (load_file) loads the file and constructs
# an in memory structure (hash) representing the specification
# found in the file.
#
# This module uses the Pkg::Tokenizer module to extract the tokens
# from the file.
#
# Usage is:
#  $pkg = new Pkg::Parser($cfg_obj, $cpp_flags, $no_cpp)
#  $rc = $pkg->load_file(file_name);
#  exit $rc;
#
# NOTE!!!
#   -- IF the $cfg_obj input parameter is an empty string,
#      then this Pkg::Parser object is for parsing the config 
#      file.  The config file is not allowed to have any packaging
#      object definitions AND it only contains variable assignment
#      statements.
#   -- IF the $cfg_obj is a reference, then the Pkg::Parser 
#      is for parsing .pd files.
#*********************************************************************

#*********************************************************************
# Object Declaration section (ie. the header file part)
#*********************************************************************
use vars(qw(@Pkg_Object_Types));
#
# Possible Error Codes
#
use vars(qw(@err_table $OK 
	    $Err_Bad_Name      $Err_Expect_Semi    $Err_Runaway_Array
	    $Err_Bad_Value     $Err_Multiple_Names $Err_Runaway_Quote
	    $Err_Missing_Equal $Err_Unknown_Error  $Err_Unknown_Object_Type
	    $Err_No_PD_Files   $Err_LS_Failed      $Err_Pkg_Obj_Not_Allowed
	    $Err_Read_Failed   $Err_cpp_Failed     $Err_Could_Not_Link_Fileset 
	    $Err_No_cpp_Cmd));

@err_table = ("No Error Detected.",
  # $Err_Runaway_Quote = 1 ($start_line, $in_file)
    "Runaway quote, possibly starting at line %d of file\n\t'%s'.",
  # $Err_Runaway_Array = 2 ($start_line, $in_file)
    "Runaway array list, possibly starting at line %d of file\n\t'%s'.",
  # $Err_Unknown_Object_Type = 3 ($type_name, $line, $file)
    "Unknown object type (%s) detected at line %d of file '%s'.",
  # $Err_Bad_Name = 4 ($name, $line, $file)
    "Invalid name/attribute (%s) found on line %d of file\n\t'%s'.",
  # $Err_Missing_Equal = 5 ($line, $file)
    "Could not find equal sign (=) on line %d of file\n\t'%s'.",
  # $Err_Bad_Value = 6 ($value, $line, $file)
    "Scalar value (%s) contains invalid characters,\n\t(".
     "on line %d of file '%s').",
  # $Err_Expect_Semi = 7 ($token, $line, $file)
    "Expected semi-colon, but found '%s' on line %d of file\n\t'%s'.",
  # $Err_Multiple_Names = 8 ($line, $file)
    "Detected multiple 'Name' attributes; last on line %d of file\n\t'%s'.",
  # $Err_No_PD_Files = 9 (no parameters)
    "No product definition files were specified and none were found to load.",
  # $Err_LS_Failed = 10 ($errno)
    "Unable to open directory to search for .pd files; error => '%s'.",
  # $Err_Pkg_Obj_Not_Allowed = 11 ($line, $file)
    "Packaging objects are not allowed in config files (line %d in\n\t'%s').",
  # $Err_Read_Failed = 12 ($file)
    "Could not read the file '%s'.",
  # $Err_Could_Not_Link_Fileset = 13 ($fileset)
    "Could not link fileset '%s' to any products",
  # $Err_cpp_Failed = 14 ($file, $cpp_errno)
    "'cpp' failed on file '%s'; %s.",
  # $Err_No_cpp_Cmd = 15 (no parameters)
    "Could not find the 'cpp' command in your PATH.",
  # $Err_Unknown_Error = 15
    "Unknown error encountered on line %d of file\n\t'%s'."
);
$OK                = 0;
$Err_Runaway_Quote = 1;       # No closing quote found before EOF
$Err_Runaway_Array = 2;       # No closing array char (]) found before EOF
$Err_Unknown_Object_Type = 3; # Object type name is not in list of valid names
$Err_Bad_Name      = 4;       # Invalid characters in attribute name
$Err_Missing_Equal = 5;       # No assignment operator found
$Err_Bad_Value     = 6;       # Invalid character in a non-string value 
$Err_Expect_Semi   = 7;       # Missing a semi-colon on previous line
$Err_Multiple_Names= 8;       # Multiple obj 'name' attributes found
$Err_No_PD_Files   = 9;       # Could not find any .pd files to load
$Err_LS_Failed     = 10;      # Could not find any .pd files to load
$Err_Pkg_Obj_Not_Allowed = 11;# pkg objects are not allowed in config files
$Err_Read_Failed   = 12;      # Given file is not readable
$Err_Could_Not_Link_Fileset = 13;      # Could not associate fileset w/ product
$Err_cpp_Failed    = 14;      # cpp close failed -- which means cpp failed.
$Err_No_cpp_Cmd    = 15;      # Could not find 'cpp' command in PATH.
$Err_Unknown_Error = 16;      # Unknown Error 
#
#  Array of valid package object types
#
@Pkg_Object_Types = (qw(Product Fileset Shipfile Update));

#---------------------------------------------------------------------
# "Private Static" Variables.
#---------------------------------------------------------------------
#======================================================================
# Each state in the set of possible parser states is handled by 
# a separate function.  This helps isolate processing and makes
# it easy to get to the correct code by using function pointers.
# The following sets up the function declarations and initializes
# the state variable.
#
# NOTE: That the functions are really private methods.  This
#       means that in the code in this modul, the '$self' 
#       pointer must be explicitly passed.
#======================================================================
use vars(qw($St_Idle $St_Start $St_Attribute $St_Equal $St_Value
	    $St_Array_Value $St_End_Stmt $St_Skip_Array $St_Skip_Stmt
	    @ST_METHODS));

@ST_METHODS = (\&Idle,        \&Start,    \&Attribute,  \&Equal,    \&Value, 
	       \&Array_Value, \&End_Stmt, \&Skip_Array, \&Skip_Stmt);

$St_Idle        = 0;
$St_Start       = 1;
$St_Attribute   = 2;
$St_Equal       = 3;
$St_Value       = 4;
$St_Array_Value = 5;
$St_End_Stmt    = 6;
$St_Skip_Array  = 7;
$St_Skip_Stmt   = 8;
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
    my $class = shift;
    my($cfg, $cpp_flags, $no_cpp, $read_cfg)   = @_;

    my $self  = {};
    bless $self, $class;

    $self->Debug($class);

    $self->initialize($cfg, $cpp_flags, $no_cpp, $read_cfg);
    return($self);
}

#=================================================================
# NAME     : load files
# PURPOSE  : load_files is the public method that initiates
#            the process of loading files.
# INPUTS   :
#    $file_list - reference to a list of file names to be loaded
# OUTPUTS  :
#    object is modified to include the specification information
#           found in the file.
# NOTES    :
#   - 0 or more files can be in the $file_list array.
#   - If no files are specified (or the list is undefined),
#     then a search is performed for the .pd files and all 
#     matches are loaded.
#   - If only 1 file is specified, it can be in a list,
#     or it can be passed as is.  This method is smart enough
#     to handle both cases.
#   - actuall loading of the file is done by load_curr_file method.
# RETURNS  : 
#    0 = Successful load
#   !0 = A failure occured (error messages are generated 
#        detailing the error(s) found).
#=================================================================
sub load_files
{
    no strict;
    my $self = shift;
    my ( $file_list ) = @_;
    my @array;
    $self->Debug( "scalar( \$file_list ) = " . scalar( $file_list ) );
    my $rc = 0;

    if ( (ref($file_list)) eq 'ARRAY') {
        @{$file_list} = $self->find_pd_files() unless @{$file_list};
        $self->{'_flist'} = $file_list;
    } elsif (scalar $file_list) {
        $self->{'_flist'} = [$file_list];
    }

    if (scalar(@{$self->{'_flist'}}) > 0)
    {
	my $file;
	foreach $file (@{$self->{'_flist'}})
	{
	    $rc += $self->load_curr_file($file);
	}
    }
    else
    {
	$self->error("Error", $Err_No_PD_Files);
	$rc = 8;
    }
    return($rc);
} 

#=================================================================
# NAME     : add product definition file
# PURPOSE  : 'add_pd_file' adds the specified pd file to the
#            list of pd files to load.
# INPUTS   :
#    $file - pathname of the pd file to be added to the load list.
# OUTPUTS  :
# NOTES    :
#   - This method is to be used by the packaging objects when
#     they have discovered a pd file that needs to be loaded.
#     Update objects require this mechanism if the 'pd_file'
#     field is specified to identify the pd file containing
#     the full fileset definition for the fileset being updated.
#   - Care must be taken to keep from loading the same files
#     multiple times.
# RETURNS  : 
#    nothing.
#=================================================================
sub add_pd_file
{
    my $self = shift;
    my($file) = @_;
    $self->Debug("file = $file");

    #---------------------------------------------------
    # NEED TO ADD LOGIC TO PREVENT LOADING SAME FILE
    #      MULTIPLE TIMES!!!
    #---------------------------------------------------
    if (scalar(grep(/$file/, @{$self->{'_flist'}})) == 0)
    {
	$self->Debug("Adding file ($file) to load list");
	push(@{$self->{'_flist'}}, $file);
    }
}


#========================================================================
# Name     : Report Error  
# Purpose  : To generate an appropriate error message for the given 
#            error id.
# NOTE:
#   This function is kept in place because is adds some of the
#   additional info required by the error message.  It also xlates
#   some of the codes to the codes for this package.
#========================================================================
sub report_error
{
    my $self = shift;
    my($err_id, $info) = @_;

    if ($err_id)
    {
      Sw_Err: {
	($err_id == $Pkg::Tokenizer::Err_Runaway_Quote) && do
	{   
	    $self->error("Error", $Err_Runaway_Quote, 
			 $info->{Line_nmbr}, $info->{File_name});
	    last Sw_Err;
	};
	#
	# These errors just have line number and file name parameters.
	#
	(($err_id == $Err_Runaway_Array)       ||
	 ($err_id == $Err_Multiple_Names)      ||
	 ($err_id == $Err_Pkg_Obj_Not_Allowed) ||
	 ($err_id == $Err_Missing_Equal))      && do
	{
	    $self->error("Error", $err_id, 
			 $info->{Line_nmbr}, $info->{File_name});
	    last Sw_Err;
	};
	#
	# These Errors have a require toke, line, and file
	# parameters
	#
	(($err_id == $Err_Unknown_Object_Type) ||
	 ($err_id == $Err_Expect_Semi)         ||
	 ($err_id == $Err_Bad_Value)           ||
	 ($err_id == $Err_Bad_Name))           && do
	{
	    $self->error("Error", $err_id, $info->{Token}, 
			 $info->{Line_nmbr}, $info->{File_name});
	    last Sw_Err;
	};
	($err_id < $Err_Unknown_Error) && do
	{
	    $self->error("Error", $err_id, $info);
	    last Sw_Err;
	};
	#
	# An unexpected error code has been received. 
	# Handle it as best we can.
	#
	$err_id = $Err_Unknown_Error;
	if (defined($info))
	{
	    if (ref $info =~ /Tokenizer/o)
	    {
		$self->error("Error", $err_id, $info->{Line_nmbr}, 
			     $info->{File_name}, $info->{Token});
	    }
	    else
	    {
		$self->error("Error", $err_id, 0, 0, $info);
	    }
	}
	else
	{
	    $self->error("Error", $err_id, 0, 0);
	}
	last Sw_Err;
      };  # End the switch.
    }
}
#-----------------------------------------------------------------
#-----------------------------------------------------------------
# "Private" Data and Methods
#-----------------------------------------------------------------
#-----------------------------------------------------------------
#=================================================================
# NAME     : initialize
# PURPOSE  : initializes the newly created Pkg::Parser object
# INPUTS   : none
# OUTPUTS  : none
# RETURNS  : nothing
#=================================================================
sub initialize
{
    my $self = shift;
    my($cfg, $cpp_flags, $no_cpp, $read_cfg) = @_;

    $self->register_errors(\@err_table);

    $self->{State_Methods} = \@ST_METHODS;  # ptr to array of state methods
    $self->{Init_State}    = $St_Idle;

    # 1st save inputs
    $self->{Config}      = $cfg;            # ptr to hash for Config info
    $self->{'cpp_flags'} = $cpp_flags;
    $self->{'no_cpp'}    = $no_cpp;
    $self->{Read_Cfg}    = $read_cfg;

    # Now init internal variables
    $self->{Product}  = {};    # ptr to hash for Product objects
    $self->{Fileset}  = {};    # ptr to hash for Fileset objects
    $self->{Curr_attr}= "";    # Name of the currently "active" attribute.
    $self->{Curr_file}= "";    # Name of file currently being read.
    $self->{Err_Code} = $OK;   # Highest Error detected in the file.
    $self->{Last_RC}  = $OK;   # Return Code of last operation

    #
    # If we are processing the config file, the current object is
    # is the config file.
    if ($read_cfg)
    {
	$self->{Curr_obj} = $cfg;
    }
    else
    {
	$self->{Curr_obj} = undef; # ptr to hash for current pkg object
    }

    #-------------------------------------------------------------
    # Main state variable, and "overriding state" variables
    #-------------------------------------------------------------
    $self->{State} = $St_Idle; # Main state variable, initially Idle

    $self->{Array_line} = 0;   # Line array starts on (for error reporting)
    $self->{In_Array}   = 0;   # Flag indicating currently in an array def.
    $self->{Name_Needed}= 0;   # Flag indicating object name is known or not.
    $self->{Skip_Object}= 0;   # Flag indicating object needs to be skipped
}


#=================================================================
# NAME     : load current file
# PURPOSE  : performs the operation of loading the specification
#            in the given file.
# INPUTS   :
#    curr_file - name of the file to be loaded
# OUTPUTS  :
#    object is modified to include the specification information
#           found in the file.
# NOTES    :
#   - This method is meant to be a "private" method.
# RETURNS  : 
#    0 = Successful load
#   !0 = A failure occured (error messages are generated 
#        detailing the error(s) found).
#=================================================================
sub load_curr_file
{
    my $self = shift;
    my($curr_file) = @_;
    $self->Debug($curr_file);

    my $rc = 0;

    if (substr($curr_file, 0, 1) ne "/")
    {
	$curr_file = "$ENV{PWD}/$curr_file";
    }
    if (-r $curr_file && -s $curr_file)
    {
	$self->{Curr_file} = $curr_file;

	my $tkn = new Pkg::Tokenizer($curr_file, 
				    $self->{'cpp_flags'},
				    $self->{'no_cpp'});

	if ((defined($tkn)) && 
	    (($rc = $tkn->{Last_RC}) == $Pkg::Tokenizer::OK))
	{
	    $self->Debug("Tokenizer = $tkn; %{$tkn}");
	    $self->status($OK);
	    $self->{State} = ($self->{Read_Cfg}) ? $St_Attribute : $St_Idle;
	    my $id;
	    while (($id = $tkn->get_token()) != $Pkg::Tokenizer::EOF)
	    {
		if ($id == $Pkg::Tokenizer::OK)
		{
		    &{$self->{State_Methods}[$self->{State}]}($self, $tkn);
		}
		else
		{
		    $self->status($id, $tkn);
		}
	    }
	    if (($rc = $tkn->term()) == $Pkg::Tokenizer::Err_cpp_Failed)
	    {
		$self->{Err_Code} = $Err_cpp_Failed;
		$self->error('Severe', $self->{Err_Code}, $curr_file, $tkn->{Last_RC});
	    } 
	}
	elsif ($tkn->{Last_RC} == $Pkg::Tokenizer::Err_No_cpp_Cmd)
	{
		$self->{Err_Code} = $Err_No_cpp_Cmd;
		$self->error('Severe', $self->{Err_Code});
	}
	else
	{
	    $self->status($Err_Unknown_Error, $rc);
	}
    }
    else
    {
	$self->status($Err_Read_Failed, $curr_file);
    }
    return($self->{Err_Code});
}


#========================================================================
# Name     : find product definition files
# Purpose  : This function searches the current directory for files
#            ending in ".pd".  All matches are returned via a reference
#            to an array.
# RETURNS  :
#    reference to an array.  Array will be empty if no files are found.
#========================================================================
sub find_pd_files
{
    my $self = shift;
    my $results = ();            # Initialize list to be empty.
    my $ext = ".pd";
    my $file;
    no strict;

    opendir (DIR, ".") || $self->error("Eror", $Err_LS_Failed, "$!") ; 
    my @list = grep (/$ext$/, readdir (DIR));
    closedir (DIR);
    foreach $file (@list) {
       print STDOUT "Find_Pd_Files is loading : $file\n";
       push @{$results}, $file;     # Add file names as they are found.
    }
    return @{$results};            # Return a reference to the array.
}

#========================================================================
# NAME     : link fileset to product
# PURPOSE  : links the given fileset to its corresponding product.
# INPUTS   :
#   $fs    - pointer to the fileset object to be linked.
# NOTES    :
#   * This method is really a "private" method.
# RETURNS  :
#    0 = success
#   !0 = failure.
#========================================================================
sub link_fileset_to_product
{
    my $self = shift;
    my($fs) = @_;
    my $rc = 0;

    $self->Debug("fs = $fs->{Name}");

    my $prod;
    foreach $prod (values %{$self->{Product}})
    {
	last if (($rc = $prod->add_fileset($fs)) == 0);
    }
    if ($rc == 1)
    {
	$self->error("Error", $Err_Could_Not_Link_Fileset, $fs->{Name});
    }
    return $rc;
}

#========================================================================
# NAME     : link update to fileset
# PURPOSE  : links the given update object to its fileset's .pd definition
# INPUTS   :
#   $upd    - pointer to the update object to be linked.
# NOTES    :
#   * This method is really a "private" method.
# RETURNS  :
#    0 = success
#   !0 = failure.
#========================================================================
sub link_update_to_fileset
{
    my $self = shift;
    my($upd) = @_;
    my $rc = 0;

    $self->Debug("upd = $upd->{Name}");

    my $fs = $self->{Fileset}{$upd->{Name}};
    if (defined $fs)
    {
	$upd->link_fileset($fs);
    }
    return $rc;
}

#========================================================================
# NAME     : assign name
# PURPOSE  : attach the given name to the object and to link
#            object to other objects appropriately.
# INPUTS   :
#   $name  - Name to be assigned to the "curr_obj".
# NOTES    :
#   * This method is really a "private" method.
# RETURNS  :
#    0 = success
#   !0 = failure.
#========================================================================
sub assign_name
{
    my $self  = shift;
    my($name) = @_;
    my $rc = 0;
    $self->Debug("Name = '$name'");

    my $type = ref($self->{Curr_obj});
    $type =~ s/^Pkg:://o;             # Need to strip off the Pkg:: prefix.
    $self->Debug("putting curr_obj into set '$type'");
    
    $self->{Curr_obj}{Name} = $name;

    if ($type eq 'Update')
    {
	push(@{$self->{$type}{$name}}, $self->{Curr_obj});   # save ptr to obj.
	$self->link_update_to_fileset($self->{Curr_obj});
    }
    else
    {
	$self->{$type}{$name}   = $self->{Curr_obj};         # save ptr to obj.
	if ($type eq 'Fileset')
	{
	    $self->link_fileset_to_product($self->{Curr_obj});

	    # Need to link this fileset to any update definitions already
	    #    found for this fileset. Typically there will only be 1,
	    #    but I am not ready to make that a limitation yet.
	    my $upd;
	    foreach $upd (@{$self->{Update}{$name}})
	    {
		$self->link_update_to_fileset($upd);	    
	    }
	}
    }
    return($rc);
} 

#========================================================================
# Name     : valid name
# Purpose  : To verify input variable is a valid object or attribute
#            name
# RETURNS  :
#    1 = variable is a valid name.
#    0 = variable is not a valid name
#========================================================================
sub valid_name
{
    my($name) = @_;

#    $self->Debug("$name");

    my $rc = ($name =~ /^\w[\w\d\-\.]*$/o);
    return $rc;
}

#========================================================================
# Name     : valid value
# Purpose  : To verify input value is valid.  Currently this function
#            is always TRUE, but it is here for easing future 
#            enhancements.
# RETURNS  :
#    1 = variable is a valid name.
#    0 = variable is not a valid name
#========================================================================
sub valid_value
{
    my($value) = @_;
    my $rc = 1;

    if ($value !~ /^".*"$/so)
    {
	$rc = ($value !~ /[\\=\#\,\(\)]/o);
    }
    return $rc;
}

#========================================================================
#========================================================================
# The following functions are the "state" functions.  One for each
# valid state of the parser.  
#
#  - Each takes the current token as input.
#  - Each returns nothing.
#  - Each reports any errors found.
#  - Each moves the State variable to the next appropriate state.
#========================================================================
sub Idle
{
    my $self = shift;
    my($token) = @_;
    my $rc = $OK;
    $self->Debug($token->{Token});

    my $class_name = ucfirst(lc($token->{Token})); 
    if (grep($class_name , @Pkg_Object_Types))
    {
	$class_name = "Pkg::$class_name";
	$self->{State} = $St_Start;
	
	$self->{Curr_obj} = $class_name->new($self->{Config}, 
					     $token->{File_name}, 
					     $token->{Line_nmbr},
					     $self);
	$self->Debug("created $self->{Curr_obj}, ref = '" . 
		     ref($self->{Curr_obj}) . "'");
	$self->{Name_Needed} = 1;
    }
    else
    {
	$self->{Skip_object} = 1;
	$rc = $Err_Unknown_Object_Type;
    }
    $self->status($rc, $token);
} # End Idle

sub Start
{
    my $self = shift;
    my($token) = @_;
    my $rc = $OK;
    $self->Debug($token->{Token});

    if ($token->{Token} eq "{")
    {
	# Open object char; move to Attribute state.
	#
	$self->{State} = $St_Attribute;
    }
    elsif (valid_name($token->{Token}))
    {
	# Got name for object; save it, flag it, but stay here (in same state)
	#
	$self->{Name_Needed} = 0;
	$self->assign_name($token->{Token});
    }
    else
    {
	# Invalid Input; mark obj for skip and report error.
	#
	$self->{Skip_object} = 1;
	$rc = $Err_Bad_Name;
    }
    $self->status($rc, $token);
} # End Start

sub Attribute
{
    my $self = shift;
    my($token) = @_;
    my $rc = $OK;
    $self->Debug($token->{Token});

    if ($token->{Token} eq "}")
    {
	if ($self->{Read_Cfg})
	{
	    $rc = $Err_Pkg_Obj_Not_Allowed;
	}
	else
	{
	    $self->{State} = $St_Idle;
	}
    }
    elsif (valid_name($token->{Token}))
    {
	if ($token->{Token} =~ /^name$/io)
	{
	    if ($self->{Name_Needed})
	    {
		# Got name for object; save it and flag it
		#
		$self->{Name_Needed} = 0;
		$self->assign_name($token->{Token});
	    }
	    else
	    {
		$rc = $Err_Multiple_Names;
		$self->{State} = $St_Skip_Stmt;
	    }
	}
	else
	{
	    $self->{Curr_attr} = $token->{Token};
	    $self->{State}     = $St_Equal;
	}
    }
    else
    {
	$self->{State} = $St_Skip_Stmt;
	$rc = $Err_Bad_Name;
    }
    $self->status($rc, $token);
}

sub Equal
{
    my $self = shift;
    my($token) = @_;
    my $rc = $OK;
    $self->Debug($token->{Token});

    if ($token->{Token} eq "=")
    {
	$self->{State} = $St_Value;
    }
    elsif ($token->{Token} eq "+=")
    {
	# need to figure out how to implement this capability
    }
    elsif ($token->{Token} eq "?=")
    {
	# need to figure out how to implment this capability
    }
    else
    {
	$rc = $Err_Missing_Equal;
	$self->{State} = $St_Skip_Stmt;
    }
    $self->status($rc, $token);
}

sub Value
{
    my $self = shift;
    my($token) = @_;
    my $rc = $OK;
    $self->Debug($token->{Token});

    if ($token->{Token} eq '[')             # is the token a '[' character?
    {
	my $a_ref  = [];
	if ($self->{Curr_obj}->store_valid_field($self->{Curr_attr}, $a_ref,
					     $token->{Line_nmbr}, 
					     $token->{File_name}) > 7)
	{
	    $rc = $Err_Bad_Value;
	}
	$self->{State}      = $St_Array_Value;
	$self->{Array_line} = $token->{Line_nmbr};
    }
    elsif (valid_value($token->{Token}))
    {
	if ($self->{Curr_obj}->store_valid_field($self->{Curr_attr}, 
					     $token->{Token},
					     $token->{Line_nmbr}, 
					     $token->{File_name}) > 7)
	{
	    $rc = $Err_Bad_Value;
	} 
	$self->{State}     = $St_End_Stmt;
    }
    else
    {
	$rc = $Err_Bad_Value;
	$self->{State} = $St_End_Stmt;
    }
    $self->status($rc, $token);
}

sub Array_Value
{
    my $self = shift;
    my($token) = @_;
    my $rc = $OK;
    $self->Debug($token->{Token});

    if ($token->{Token} eq "]")
    {
	$self->{State}      = $St_End_Stmt;
	$self->{Array_line} = 0;
    }
    elsif (valid_value($token->{Token}))
    {
	push(@{$self->{Curr_obj}{$self->{Curr_attr}}}, $token->{Token});
    }
    else
    {
	$rc = $Err_Bad_Value;
	$self->{State} = $St_Skip_Array;
    }
    $self->status($rc, $token);
}

sub End_Stmt
{
    my $self = shift;
    my($token) = @_;
    my $rc = $OK;

    $self->Debug($token->{Token});

    if ($token->{Token} eq ";")
    {
	$self->{State} = $St_Attribute;
    }
    else
    {
	$rc = $Err_Expect_Semi;
	$self->{State} = $St_Skip_Stmt;
    }
    $self->status($rc, $token);    
}

sub Skip_Array
{
    my $self = shift;
    my($token) = @_;

    $self->Debug($token->{Token});

    if ($token->{Token} eq "]")
    {
	$self->{Array_line} = 0;
	$self->{State}      = $St_End_Stmt;
    }
}

sub Skip_Stmt
{
    my $self = shift;
    my($token) = @_;

    $self->Debug($token->{Token});

    if ($token->{Token} eq "[")
    {
	$self->{State} = $St_Skip_Array;
    }
    elsif ($self->{Read_Cfg} && $token->{Token} eq '{')
    {
	$self->status($Err_Pkg_Obj_Not_Allowed);
	$self->{Skip_object} = 1;
    }
    elsif ($token->{Token} eq ";")
    {
	$self->{State} = $St_Attribute;
    }
}

sub status
{
    my $self = shift;
    my($err, $info) = @_;
    
    $self->{Last_RC} = $err;
    if ($err != $OK)
    {
	$self->{Err_Code} |= 8;
	$self->report_error($err, $info);
    }
}


#========================================================================
#========================================================================
# The following set of "public" methods are primariy debug methods
# that dump (or print) detailed information about the Parser and
# what it has done thus far.
# In addition to printing the Parser, each object that the Parser
# has processed is printed (dumped).
#========================================================================
sub Dump
{
    my $self = shift;

    print STDOUT "\nPkg::Parser::Dump:",
    "\n   Curr_file = ", $self->{Curr_file},
    "\n   Err_Code  = ", (defined($self->{Err_Code})) ? $self->{Err_Code} : "",
    "\n   Last_RC   = ", (defined($self->{Last_RC}))  ? $self->{Last_RC}  : "",
    "\n   State     = ", (defined($self->{State}))    ? $self->{State}    : "",
    "\n   In_Array  = ", (defined($self->{In_Array})) ? $self->{In_Array} : "",
    "\n   Array_line= ", (defined($self->{Array_line}))?$self->{Array_line}:"",
    "\n   Name_Needed=", (defined($self->{Name_Needed}))?$self->{Name_Needed}:"",
    "\n   Skip_Object=", (defined($self->{Skip_Object}))?$self->{Skip_Object}:"",
    "\n   Curr_attr = ", (defined($self->{Curr_attr}))? $self->{Curr_attr}: "",
    "\n   Curr_obj  =\n"; 
    (defined($self->{Curr_obj})) && print_object($self->{Curr_obj}, "      ");
    print STDOUT "   Products  = ", scalar(keys(%{$self->{Product}})), "\n";
    $self->print_products("      ");
    print STDOUT "   Filesets  = ", scalar(keys(%{$self->{Fileset}})), "\n";
    $self->print_filesets("      "); 
    print STDOUT "\n";
}

sub print_products
{
    my $self = shift;
    my $spc = (defined($_[0])) ? $_[0] : "";

    my $key;
    foreach $key (keys %{$self->{Product}})
    {
	print_object($self->{Product}{$key}, $spc);
    }
}

sub print_filesets
{
    my $self = shift;
    my $spc = (defined($_[0])) ? $_[0] : "";

    my $key;
    foreach $key (keys %{$self->{Fileset}})
    {
	print_object($self->{Fileset}{$key}, $spc);
    }
}

sub print_object
{
    my $obj = shift;
    my $spc = (defined($_[0])) ? $_[0] : "";

    my $save_OFS = $OFS;
    $OFS = " ";
    print $spc, ref($obj), $obj->{Name}, "\n", "$spc\{\n";
    my $attr;
    foreach $attr (keys %{$obj})
    {
	if ($attr ne "Name" && $attr ne "_Type")
	{
	    print $spc, "   $attr = ";
	    if (ref($obj->{$attr}) =~ /ARRAY/o)
	    {
		print "[@{$obj->{$attr}}];\n";
	    }
	    else
	    {
		print "$obj->{$attr};\n";
	    }
	}
    }
    $OFS = $save_OFS;
    print $spc, " }\n";
}
1;

