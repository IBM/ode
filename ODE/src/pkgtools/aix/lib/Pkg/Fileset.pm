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
# NAME        : Pkg::Fileset Class
# DESCRIPTION :
#    The 'Pkg::Fileset' Class implements the Fileset object for the
#    pkgtools toolset.  This class stores the definition of a
#    fileset found in a product definition file and then provides
#    the methods necessary to include the fileset in an installation
#    package acceptable to AIX Version 4.x's installp command.
#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package Pkg::Fileset;

#
# Standard Package Preamble
#
require 5.002;
require Exporter;
require Pkg::Object;

# inherit from Exporter and Pkg::Object
@ISA    = qw(Exporter Pkg::Object);  

use English;
use strict;

#*********************************************************************
# "Static" variables for the Fileset object
#
use vars(qw(%rqd_fields @irrelevant_fields $log @err_table));

# %rqd_fields identifies the minimum set of fields that must
# be defined in order to create a fileset part of a package.
# The value part is either 'local' which means that the field
# must be defined within the fileset itself, of 'parent'
# which means the field can be defined in a "parent".
#
%rqd_fields = (
	       content     => "parent",
	       description => "local",
	       inslist     => "local",
	       version     => "parent",
	       release     => "parent",
	       maint_level => "parent",
	       fix_level   => "parent",
	      );
#
# @irrelevant_fields is a list of fields that are irrelevant
# in Fileset object definitions.  These fields should appear
# somewhere else (Product or Config file right now).
#
@irrelevant_fields = (qw(adepackage_flags filesets         image_name
			 pd_file          problems_fixed   product_name
			 ship_path        work_dir         ar_flags));
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
	   "The field '%s' is required to be in the Fileset,\n\t".
	   "definition but it was not found for fileset '%s'.\n\t".
	   "It must be specified in the Fileset defintion itself.",
  # $Err_Missing_Rqd_Field = 5 ($field_name, $fileset_name)
	   "The field '%s' is required for Filesets, but\n\t".
	   "was not found for fileset '%s'.  It may be\n\t".
           "specified in the Fileset, Product, or config file.",
  # $Err_Irrelevant_Field = 6 ($field_name, $line, $file)
	   "The field '%s' (found on line %d of file\n\t'%s')\n\t".
           "is irrelevant in a Fileset definition.  It is being ignored.",
  # $Err_Dup_Field_Spec = 7 ($field_name, $line, $file)
	   "Multiple definitions of field '%s' have been found.  Using\n\t".
	   "the last instance, found on line %d of file\n\t'%s')",
  # $Err_No_Copyright_Info = 8 ($fileset_name)
	   "No Copyright info was found for fileset '%s'\n",
  # $Err_No_Objclass_DB = 9
	   "Fileset contains ODM files, but no ODM class definition was found",
  # $Err_Merge_Failed = 10 ($fileset_lp, $product_lp)
	      "Could not merge '%s' into '%s'.",
  # $Err_Invalid_VRMF_Format = 11 ($fileset, $version, $rlse, $maint, $fix)
	      "Invalid VRMF format for '%s'.  Version and release must be " .
	      "1 or 2 decimal digits, maint_level and fix_level must be " .
	      "1 to 4 decimal digits.  Input values were: version = '%s', " .
	      "release = '%s', maint_level = '%s', fix_level = '%s'.",
);
use vars(qw($Err_Could_Not_Open_File  $Err_Could_Not_Create_Dir
	    $Err_Could_Not_Find_File  $Err_Missing_Local_Field
	    $Err_Missing_Rqd_Field    $Err_Irrelevant_Field
	    $Err_Dup_Field_Spec       $Err_No_Copyright_Info
	    $Err_No_Objclass_DB       $Err_Merge_Failed
	    $Err_Invalid_VRMF_Format));



$Err_Could_Not_Open_File  =  1;
$Err_Could_Not_Create_Dir =  2;
$Err_Could_Not_Find_File  =  3;
$Err_Missing_Local_Field  =  4;
$Err_Missing_Rqd_Field    =  5;
$Err_Irrelevant_Field     =  6;
$Err_Dup_Field_Spec       =  7;
$Err_No_Copyright_Info    =  8;
$Err_No_Objclass_DB       =  9;
$Err_Merge_Failed         = 10;
$Err_Invalid_VRMF_Format  = 11;

#---------------------------------------------------------------------
# The following variables a class variables, but are intended to
# be used only within this Class.  They define the extensions for
# their respective set of files.  The extensions are kept in arrays
# to reduce the likely hood of errors by repeatedly typing the
# names.
#---------------------------------------------------------------------
use vars(qw(@_inv_files @_ODM_scripts));

@_inv_files   = qw(inventory size al tcb acf);
@_ODM_scripts = qw(odmadd odmdel unodmadd);

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

     my $self = Pkg::Object::new($class, $config, $file, $line, $parser);
    bless $self, $class;
    $self->Debug();
    Pkg::Fileset::initialize($self);
    return($self);
}


#=================================================================
# NAME     : prepare for install
# PURPOSE  : perform the necessary operations to prepare this
#            fileset object to be included in an install image.
# INPUTS   : 
# OUTPUTS  : 
#   - Several files are created to be used by the product when
#     creating the install image.
# NOTES    :
#   - It is expected that the product object has already done
#     the work to setup the working directory.
#   - It is the product objects responsibility to create the
#     actual install image.
#   - 2 files are created that are name specific:
#       1) INSLIST - this file is the merged inslists from
#                    all filesets.  Each fileset appends its
#                    inslist info to this file.
#       2) LPFILE  - this file is the merged <fileset>.lp
#                    files from all filesets.  Each fileset 
#                    appends its .lp file to this file.
# RETURNS  :
#   0 = success
#  !0 = Error.
#=================================================================
sub prep_for_install
{
    my($self, $output_dir) = @_;
    $self->Debug("$self->{Name}");
    my $rc = 0;

    # Set package type
    $self->{'_pkg_type'} = "Install";

    if (($rc = $self->check_integrity()) < 8)
    {
	$self->Log("Preparing Fileset '$self->{Name}' for install...");
	# Create the .lp file first;
	# NOTE - we cannot do anything without the .lp file.
	$rc =  $self->create_lp_file();
	
	if ($rc < 8)
	{
	    $rc |= $self->generate_inventory_info();
	    $rc |= $self->generate_copyright_info();
	    $rc |= $self->generate_requisites();
	    $rc |= $self->generate_ODM_scripts();
	    $rc |= $self->generate_supersedes();
	    $rc |= $self->handle_control_files();
	    
	    # Add the inslist to the cumulative inslist for the product.
	    #
	    my $il = $self->find_file($self->get('inslist'));
	    if ($il)
	    {
		if ((system("cat $il >>INSLIST")) != 0)
		{
		    $rc |= 16;
		    $self->error('Error', $Err_Merge_Failed, 
				 $il, "INSLIST"); 
		}
	    }
	    # Put the .lp file into the cumulative .lp for the product.
	    #
	    my $lp = $self->find_file($self->get('_lp_file'));
	    if ($lp)
	    {
		if (system("cat $lp >>LPFILE") != 0)
		{
		    $rc |= 16;
		    $self->error('Error', $Err_Merge_Failed, 
				 $lp, "LPFILE");
		}
	    }
	    else
	    {
		$rc |= 16;
		$self->error('Error', $Err_Could_Not_Find_File, 
			     $lp, "fileset = '$self->{Name}'");
	    }
	}
    }
    return($rc);
} # END mk_install


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
	}
    }
    return($rc);
}


#=================================================================
# NAME     : link product
# PURPOSE  : makes the link from this fileset to the product
#            to which it belongs.
# INPUTS   :
#     $prod - ptr product object
# OUTPUTS  : none
# NOTES    :
#   * This method is intended only for the Pkg::Product class or
#     the Pkg::Parser class.  It is not really intended for others.
# RETURNS  : 
#   nothing.
#=================================================================
sub link_product
{
    my($self, $prod) = @_;
    $self->Debug("fs = $self->{'Name'}, product = $prod->{Name}");

    $self->{'Product'} = $prod;
    $self->{'_parent'} = $prod;
}


#=================================================================
# NAME     : check integrity
# PURPOSE  : The check_integrity method checks the integrity 
#            of the fileset definition as a whole.  Checks are
#            done for the minimum set of field.  The check for
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
    my $self = shift;
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
    $rc = $self->valid_vrmf();
    return($rc)
}


#==========================================================================
# Name     : generate inventory info
# Purpose  : This function generates the inventory info required
#            by installp and by other stages of the process of
#            creating an install image.
# Inputs   :
#   - pkg_type  -- type of installation package to create, install or update.
# Outputs  :
#   - output is really created by 'adeinv'.
#
# NOTES    :
#   - temp files are created.  These should be cleaned up,
#     but right now they are not.
# Returns  :
#    0 = SUCCESS
#   !0 = failure; error messages will be generated here.
#==========================================================================
sub generate_inventory_info
{
    my $self = shift;
    $self->Debug();
    
    my $rc = 0;
    my $il = "";

    my $work_dir = $self->get_work_dir;
    my $lp = $self->{'_lp_file'};
    my $uid_flag = $self->get('uid_table');
    $uid_flag = "-t $uid_flag " if ($uid_flag);
    my $inv_flags = " " . $uid_flag . $self->get('adeinv_flags') . " ";

    if ($self->{'_pkg_type'} eq "Update")
    {
	# For update packages, we must generate a subset inslist
	# that contains only those files listed in the 'shipfiles' 
	# field.  Must also add flags for adeinv.
	#
	# Should make a 'gen_subset_inslist' method in Fileset 
	#   that will error out if it is called.
	#   'gen_subset_inslist' should be in the Update class.
	$il = $self->gen_subset_inslist();
	$inv_flags .= " -U $self->{'_short_VRMF'}";
    }
    else
    {
	$il = $self->find_file($self->{'inslist'});	
    }
    # The 1st call is the same for USR and SHARE
    # content, except the SHARE content requires a -D flag.
    #
    my $content = 'U';
    if ($self->{'_content'} eq 'H')
    {
	$inv_flags = " -D $inv_flags";     # Fileset has SHARE content
	$content = $self->{'_content'};
    }
    # Finally run adeinv if an inslist was found (or created).
    #
    if ($il ne "")
    {
	# First remove all files that could be generated.
	my($file, $ext, $base);
	$base = "$work_dir/$self->{Name}.";
	foreach $ext (@_inv_files)
	{
	    $file = "$base" . "$ext";
	    unlink($file);
	}
	unlink("$work_dir/lpp.acf");

	my $cmd = "adeinv -i $il -u $lp -l " . $self->get_product_name() .
	      " -s " . $self->get('ship_path') . " -d " . $work_dir;
	$rc = $self->execute_cmd("$cmd $inv_flags");

	# Save all generated files, for later inclusion in liblpp.a
	foreach $ext (@_inv_files)
	{
	    $file = "$base" . "$ext";
	    $self->liblpp_file($content, $file) if (-f $file);
	} 
	# lpp.acf is special case.  It does not follow name conventions.
	$file = "$work_dir/lpp.acf";
	$self->liblpp_file($content, $file) if (-f $file);

	# Now run adeinv again for the ROOT content (if there is any).
	if ($self->{'_content'} eq 'B')
	{
	    # First remove all files that could be generated.
	    $base = "$work_dir/root/$self->{Name}.";
	    foreach $ext (@_inv_files)
	    {
		$file = "$base" . "$ext";
		unlink($file);
	    } 
	    unlink("$work_dir/root/lpp.acf");

	    $cmd .= "/root";
	    $inv_flags =~ s/ -D //o;  # remove share flag (if present)
	    $content = 'B';
	    $rc += $self->execute_cmd("$cmd -r $inv_flags");

	    # Save all generated files, for later inclusion in liblpp.a
	    foreach $ext (@_inv_files)
	    {
		$file = "$base" . "$ext";
		$self->liblpp_file($content, $file) if (-f $file);
	    }
	    # lpp.acf is special case.  It does not follow name conventions.
	    $file = "$work_dir/root/lpp.acf";
	    $self->liblpp_file($content, $file) if (-f $file);
	}
    }
    else
    {
	$rc = 8;
	if ($self->{'_pkg_type'} eq "Install")
	{
	    $self->error('Error', $Err_Could_Not_Find_File, 
			 $self->{'inslist'});
	    # NOTE:
	    #    error messages for "Update" pkgs are generated
	    #    when creating the subset inslist in the Update class.
	}
    }
    return($rc);
} # END generate_inventory_info


#==========================================================================
# Name     : generate copyright info
# Purpose  : This function generates the copyright file.
#            This generation may just be creating a file
#            containing the copyright statement from the product
#            specificaton, or the file may be generated based on
#            the keys specified and the information in the copyright.map
#            file. 
# Inputs   :
# Outputs  :
# Returns  :
#    0 = SUCCESS
#   !0 = failure; error messages will be generated here.
#==========================================================================
sub generate_copyright_info
{
    my $self = shift;
    $self->Debug();

    my $rc = 0;
    
    my $work_dir = $self->get_work_dir;
    my $fn = "$work_dir/$self->{Name}.copyright";
    unlink($fn);
    #-----------------------------------------------------
    # By default, generate the statement from the
    # keys.  If the "keys" field is not present,
    # then use the 'copyright' field.
    #-----------------------------------------------------
    my $copyright = $self->get('copyright');
    if ($copyright eq "")
    {
	my $cr_keys = $self->get('copyright_keys');
	if (length($cr_keys))
	{
	    my $key_file = "$work_dir/$self->{Name}.cr";
	    if (open(KF, ">$key_file"))
	    {
		if (ref($cr_keys) =~ /ARRAY/io)
		{
		    my $key;
		    foreach $key (@{$cr_keys})
		    {
			$key =~ s/^\s*\"\s*(.*)\s*\"\s*$/$1/o; # strip "s
			print KF "$key\n";
		    }
		}
		else
		{
		    $cr_keys =~ s/^\s*\"\s*(.*)\s*\"\s*$/$1/o; # strip "s
		    print KF "$cr_keys\n";
		}
		close(KF);
		#
		# Create the "real" copyright file now
		# based on the key file we just created.
		my $cmd = "adecopyright -f " . 
		      $self->find_file($self->get('copyright_map')) .
		      " -t " . $self->find_file($self->get('compids_table')) .
		      " -l " . $self->get_product_name() .
		      " "    . $self->get('adecopyright_flags') .
		      " $key_file >$fn";
		if ($self->execute_cmd("$cmd") != 0)
		{
		    $rc = 4;
		}
	    }
	    else
		{
		    $self->error('Warning', $Err_Could_Not_Open_File,
				 $key_file, $!);
		    $rc = 4;
		}
	}
	else
	{
	    $self->error('Warning', $Err_No_Copyright_Info, $self->{Name});
	    $rc = 4;
	}
    }
    else 
    {
	if (open(CR, ">$fn"))
	{
	    $copyright =~ s/^\s*\"\s*(.*)\s*\"\s*$/$1/so;  # strip "s
	    print CR $copyright, "\n";
	    close(CR);
	}
	else
	{
	    $self->error('Warning', $Err_Could_Not_Open_File, $fn, $!);
	    $rc = 4;
	}
    }
    # If all went well, add the copyright file to the list of 
    # liblpp.a files.
    if ($rc == 0)
    {
	my $content = $self->get('_content') eq 'H' ? 'H' : 'U';
	$self->liblpp_file($content, $fn);
    }
    return($rc);
} # END generate_copyright_info


#==========================================================================
# Name     : generate requisites
# Purpose  : This function generates the requisite file to be
#            used in generating the TOC (lpp_name) file.
# Inputs   :
#   $gen_reqs - an optional parameter.  It is a reference to
#            an array of requisite specifications.  In most 
#            cases, this parameter will not be present, but
#            it may be if an Update image is being handled.
# Outputs  :
#   - file called <fileset.name>.prereq may be generated if 
#     requisites are identified in the Fileset spec.
# NOTES    :
# Returns  :
#    0 = SUCCESS
#   !0 = failure; error messages will be generated here.
#==========================================================================
sub generate_requisites
{
    my $self = shift;
    my($gen_reqs) = @_;
    $self->Debug();
    my $rc = 0;

    my $req_file = $self->get_work_dir() . "/" . $self->{Name} . ".prereq";
    unlink($req_file);

    my ($req, $reqs, $user_prereq);


    #See if user-defined prereq file exists
    if ($user_prereq = $self->get('user_prereq'))
    {
      if ( -f $user_prereq )
      {
        system("cp $user_prereq $req_file" );
      }
      else
      {
	      $self->error($Err_Could_Not_Open_File, $user_prereq, $!);
	      $rc = 8;
      }
    }
    else 
    {
      # No user_prereq file, read v_reqs tag
      if ($reqs = $self->get('v_reqs'))
      {
        $self->Debug("requisites found for $self->{Name}, " . scalar(@{$reqs}));
	      if (open(REQ, ">$req_file"))
	      {
	        # Should really do some validation of requisite line here.
	        # But for now, just dump the record into the file.
	        #
	        foreach $req (@{$reqs})
	        {
            $req =~ s/^\s*\"(.*)\"\s*$/$1/s;   # strip double quotes (")
            print REQ "$req\n";
          }
	        # Add any requisites passed in directly to this method.
	        # Typically, these req's will come from the Update class.
	        #
          foreach $req (@{$gen_reqs})
          {
            $req =~ s/^\s*\"(.*)\"\s*$/$1/s;   # strip double quotes (")
            print REQ "$req\n";
          }
          close(REQ);
          unlink($req_file) if (-z $req_file);   # remove file if it is empty
        }
        else
        {
          $self->error($Err_Could_Not_Open_File, $req_file, $!);
          $rc = 8;
        }
      }
      else
      {
	      $self->Debug("NO requisites for $self->{Name}");	
      }
    }
    return($rc);
} # END generate_requisites


#==========================================================================
# Name     : generate ODM scripts
# Purpose  : This function generates the ODM scripts necessary
#            to properly install the product.  
# Inputs   :
# Outputs  :
#   - output is really created by 'mkodmupdt'.
# NOTES    :
#   - several odm scripts will be generated.  There will be a set
#     of scripts for each .add file identified in the pkg specification. 
# Returns  :
#    0 = SUCCESS
#   !0 = failure; error messages will be generated here.
#==========================================================================
sub generate_ODM_scripts
{
    my $self = shift;
    $self->Debug();
    my $rc = 0;

    my $work_dir = $self->get_work_dir();
    my $add_file;
    #
    # Always check for 'non-ROOT' content files first.
    #
    my $content = $self->{'_content'} eq "H" ? 'H' : 'U';
    my $add_files = $self->get('odm_add_files');
    if (defined($add_files))
    {    
	$rc = $self->create_odm_scripts($add_files, $content, $work_dir);
    }
    #
    # Only do the ROOT content if the fileset content indicates
    # ROOT content is present.
    #
    $content = $self->{'_content'};
    $add_files =  $self->get('root_odm_files');
    if (($rc == 0) && ($content eq 'B') && defined($add_files))
    {
	$rc = $self->create_odm_scripts($add_files, $content, 
					"$work_dir/root");
    }
    return($rc);
}


#==========================================================================
# Name     : generate supersedes
# Purpose  : This function generates any supersede information
#            exlicitly entered into the Filseset definition.
# Inputs   :
# Outputs  :
#   - a <fileset>.supersede file is created.
# Returns  :
#    0 = SUCCESS
#   !0 = failure; error messages will be generated here.
#==========================================================================
sub generate_supersedes
{
    my $self = shift;
    $self->Debug();
    my $rc = 0;

    if (defined($self->{'supersedes'}))
    {
	my $work_dir = $self->get_work_dir();
	my $file = "$work_dir/$self->{Name}.supersede";
	if (open(SUPER, ">$file"))
	{
	    my $supersede;
	    foreach $supersede (@{$self->{'supersedes'}})
	    {
		$supersede =~ s/^\s*\"\s*(.*)\s*\"\s*$/$1/so; # strip "s
		print SUPER "$supersede\n";
	    }
	    close(SUPER);
	}
	else
	{
	    $rc = 8;
	    $self->error('Error', $Err_Could_Not_Open_File, $file, $!);
	}
    }
    return($rc);
} # END generate_supersedes


#==========================================================================
# Name     : handle control files
# Purpose  : This function handles recording the control files
#            into the appropriate list of liblpp.a files.
# Inputs   : none
# Outputs  : 
#   - information recorded into the Product object.
# Returns  :
#    0 = SUCCESS
#   !0 = failure; error messages will be generated here.
#==========================================================================
sub handle_control_files
{
    my $self = shift;
    $self->Debug();
    my $rc = 0;

    #
    # Always check for 'non-ROOT' content files first.
    #
    my $content = $self->{'_content'} eq "H" ? 'H' : 'U';
    if (defined($self->{'control_files'}))
    {    
	$rc = $self->record_control_files($self->{'control_files'},
					  $content);
    }
    #
    # Only do the ROOT content if the fileset content indicates
    # ROOT content is present.
    #
    $content = $self->{'_content'};
    if (($rc == 0) && ($content eq 'B') && 
	defined($self->{'root_control_files'}))
    {
	$rc = $self->record_control_files($self->{'root_control_files'},
					  $content);
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
# PURPOSE  : initializes the newly created Pkg::Fileset object
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

    $self->register_errors(\@err_table);
    return;
}

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
	   $self->{'_parent'}->get($field));
}


#==========================================================================
# Name     : create <fileset>.lp file
# Purpose  : This function creates the ".lp" file for the given
#            fileset.
# Inputs   :
# Outputs  :
#   - $fs->{Name}.lp file is created.
# NOTES    :
#    - error messages are generated and so do not need to be
#      by the caller.
#    - It is assumed that checks for required fields has already
#      been performed -- no definition validity is checked here.
#    - There are 3 main parts to this function.  It might be a
#      good idea to split the 1st two parts out into separate
#      methods/functions.  The 3 parts are...
#        1) The first maps the 'content' field to installp
#           codes and creates the content directories.
#        2) The 2nd maps the 'boot_rqmt' field to installp
#           codes.
#        3) Finally the <fileset>.lp file is created.
# Returns  :
#    filename on success
#    void string on failure
#==========================================================================
sub create_lp_file
{
    my $self = shift;
    $self->Debug();
    my $lp = undef;

    my $work_dir = $self->get_work_dir;
    $lp = "$work_dir/$self->{Name}.lp";
    unlink("$lp");

    if (open(LP, ">$lp"))
    {
	my $rc = 0;
	#-----------------------------------------------------------
	# Map the content field into the appropriate character
	# for installp.  And while we are at it, create the
	# necessary directories.
	#-----------------------------------------------------------
	my $content = $self->get('content');
	my $dir;
	if (($content =~ /data/io) ||
	    ($content =~ /share/io))
	{
	    $content = "H";
	    $dir = "$work_dir/data";
	    $rc = !mkdir($dir, 0755) if (! -d "data");
	} 
	elsif (($content =~ /root/io) ||
	       ($content =~ /both/io))
	{
	    $content = "B";
	    $dir = "$work_dir/root";
	    $rc = !mkdir($dir, 0755) if (! -d "$work_dir/root");
	} 
	else
	{
	    $content = "U";
	}
	$self->{"_content"} = $content;
	if ($rc)
	{
	    # If the directory could not be created, report
	    # it and prevent continuation after this function.
	    #
	    $self->error('Error', $Err_Could_Not_Create_Dir, $dir, $!);
	    $rc = 8;
	}
	$rc |= $self->record_content($content);

	#--------------------------------------------------------------
	# Map the boot requirement field into the appropriate
	# character for installp.
	#--------------------------------------------------------------
	my $boot_flag = $self->get('boot_rqmt');
	if (defined($boot_flag))
	{
	    if (($boot_flag eq 'B') || ($boot_flag =~ /^reboot/io))
	    {
		$boot_flag = 'B';
	    }
	    elsif (($boot_flag eq 'b') || ($boot_flag =~ /^bosboot/io))
	    {
		$boot_flag = 'b';
	    }
	    else
	    {
		$boot_flag = 'N';
	    }
	}
	else
	{
	    # default to No bosboot/reboot required.
	    $boot_flag = 'N';
	}
	$self->record_boot_flag($boot_flag);

	#------------------------------------------------------------
	# Finally, create the "closing text" and create the file...
	# Note: The closing must end in a newline, but it may
	#       include the optional "comments" field.
	#------------------------------------------------------------
	my $comments = $self->get('comments');
	$comments =~ s/^\s*\"(.*)\"\s*$/$1/s;         # strip double quotes (")
	my $closing = "\n" . $comments;
	if (substr($closing, length($closing)-1, 1) ne "\n")
	{
	    $closing .= "\n";
	}

	my $description = $self->get('description');
	$description =~ s/^\s*\"(.*)\"\s*$/$1/;       # strip double quotes (")
	print LP "$self->{Name} $boot_flag $content ", 
	$self->get('language'), " ", $description, $closing;
	close(LP);
	$self->{'_lp_file'} = $lp;
    }
    else
    {
	$self->error('Error', $Err_Could_Not_Open_File, $lp, $!);
	undef $lp;
    }
    return($lp);
}


#=================================================================
# NAME     : get work dir
# PURPOSE  : In the Fileset class, the work_dir should not
#            be defined.  It should come from the Product,
#            Config, or maybe even Update objects.
# INPUTS   : none
# OUTPUTS  : none
# NOTES    :
#   * This method is intended to be a private method.
#   * This method expects that the directory has already
#     been created.
# RETURNS  : 
#   pathname = SUCCESS,  working dir now exists 
#              (may have been created)
#   null str = FAILURE, working dir does not exist.
#=================================================================
sub get_work_dir
{
    my($self) = @_;

    $self->Debug("fs = $self->{'Name'}");
    return($self->{Product}->get_work_dir());
}

#=================================================================
# NAME     : valid VRMF
# PURPOSE  : 'valid_vrmf' validates the VRMF format.
# INPUTS   : none.
# OUTPUTS  : none
# NOTES    :
#   - A valid VRMF has the following restrictions.
#        version     = 1 or 2 decimal digits
#        release     = 1 or 2 decimal digits
#        maint_level = 1 to 4 decimal digits
#        fix_level   = 1 to 4 decimal digits
# RETURNS  :
#   0 = success
#   8 = Error.
#=================================================================
sub valid_vrmf
{
    my $self = shift;
    my $rc = 0;

    if ($self->get('version')     !~  m/^\d{1,2}$/ ||
	$self->get('release')     !~  m/^\d{1,2}$/ ||
	$self->get('maint_level') !~  m/^\d{1,4}$/ ||
	$self->get('fix_level')   !~  m/^\d{1,4}$/)
    {
	$self->error('Error', $Err_Invalid_VRMF_Format, $self->{'Name'},
		     $self->get('version'), $self->get('release'),
		     $self->get('maint_level'), $self->get('fix_level'));
	$rc = 8;
    }
    return($rc);
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

    if ($objclass_db ne "")
    {
	my $add_file;
	foreach $add_file (@{$add_files})
	{
	    $add_file =~ s/^\s*\"\s*(.*)\s*\"\s*$/$1/o;      # strip quotes (")
	    my $add_path = $self->find_file($add_file);
	    if ($add_path ne "")
	    {
		my $cmd = "mkodmupdt -i -c $add_path -o $self->{Name} " .
		      "-t $objclass_db -d $out_dir";
		if ($self->execute_cmd($cmd) == 0)
		{
		    # Add any output files to the liblpp list.
		    #
		    $add_file = substr($add_file, 0, 
				       rindex($add_file, "."));
		    my $dirname = "$out_dir/$self->{Name}.$add_file.";
		    my($file, $ext);
		    foreach $ext (@_ODM_scripts)
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
		$self->error('Error', $Err_Could_Not_Find_File,
			     $add_file);
		$rc = 8;
	    }
	}
    }
    else
    {
	$self->error('Error', $Err_No_Objclass_DB);
	$rc = 8;
    }
    return($rc);
} # END create_odm_scripts


#==========================================================================
# Name     : record control files
# Purpose  : This function records the given set of control files
#            into liblpp.a for the product.  Each file "must be
#            found" so that the absolute path is added.
# Inputs   :
#    $cntl_files- reference to an array of control file names.
#                NOTE that these names may have double quotes 
#                     around them.
#    $content  - The package content to which these add files belong.
#                This info is needed when recording the filenames
#                in the list of files for liblpp.
# Outputs  :
#   - information is recorded in the Product object.
# Returns  :
#    0 = SUCCESS
#   !0 = failure; error messages will be generated here.
#==========================================================================
sub record_control_files
{
    my($self, $cntl_files, $content) = @_;
    my $rc = 0;
    $self->Debug("content = $content");

    my $file;
    foreach $file (@{$cntl_files})
    {
	$file =~ s/^\s*\"\s*(.*)\s*\"\s*$/$1/o;       # strip double quotes (")
	my $path = $self->find_file($file);
	if ($path)
	{
	    $self->liblpp_file($content, $path);
	}
	else
	{
	    $self->error('Error', $Err_Could_Not_Find_File, $file);
	    $rc = 8;
	}
    }
    return($rc);
} # END record_control_files


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
#   - This method just passes the file info on to the 
#     Product object.  This method exists to provide a common
#     interface for other methods.  This method is overridden
#     by the Update class to store the info in the object.
# RETURNS  :
#   0 = success
#  !0 = Error.
#=================================================================
sub liblpp_file
{
    my($self, $content, $filename) = @_;
    $self->Debug("$self->{Name}, content = $content, file = $filename");

    my $rc = $self->{Product}->record_liblpp_file($content, $filename);
    return($rc);
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
#   - This method just passes the boot flag info on to the 
#     Product object.  This method exists to provide a common
#     interface for other methods.  This method is overridden
#     by the Update class to store the info in the object.
# RETURNS  :
#   0 = success
#  !0 = Error.
#=================================================================
sub record_boot_flag
{
    my($self, $boot_flag) = @_;
    $self->Debug("$self->{Name}, flag = $boot_flag");

    my $rc =  $self->{Product}->record_boot_flag($boot_flag);
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
#   - This method just passes the content info on to the 
#     Product object.  This method exists to provide a common
#     interface for other methods.  This method is overridden
#     by the Update class to store the info in the object.
# RETURNS  :
#   0 = success
#  !0 = Error.
#=================================================================
sub record_content
{
    my $self = shift;
    my($content) = @_;
    $self->Debug("$self->{Name}, content = $content");

    my $rc =  $self->{Product}->record_content($content);
    return($rc);
}

sub get_product_name
{
    my $self = shift;
    return($self->{Product}{Name});
}
1;
