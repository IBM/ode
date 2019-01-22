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
# NAME        : Pkg::Packager Class
# DESCRIPTION :
#    The 'Pkg::Packager' Class is really an abstract class.  It is
#    the interface to create an installable package, either an
#    install image or an update image.  It cannot really stand 
#    on its own because it relies on info to be provided by other
#    classes.
#    While in 'perl' the restrictions are much less limiting, the
#    idea is that all packaging specific information is kept in
#    the object.  The fields that are logically part of this class
#    are:
#        - image_name  : this field is typically specified in the
#                        Product or the Update object definition.
#        - version     : this field is typically specified in the
#                        Product or the Update object definition,
#                        or in the config file.
#        - release     : this field is typically specified in the
#                        Product or the Update object definition,
#                        or in the config file.
#        - maint_level : this field is typically specified in the
#                        Product or the Update object definition,
#                        or in the config file.
#        - fix_level   : this field is typically specified in the
#                        Product or the Update object definition,
#                        or in the config file.
#        - platform    : this field is typically specified in the
#                        config file since it can only be RS/6000.
#        - comp_ids    : this field is typically specified in the
#                        config file since there is usually only
#                        one compids file.
#        - _prod_name  : this field is supplied by the class that
#                        has a package to create (either Update or
#                        Product).  For Products, it is just the
#                        name of the Product object.  For Updates,
#                        it is the name of the Product to which
#                        the fileset being updated belongs.
#        - _content    : this field is supplied by the class that
#                        is creating the package.  It is derived 
#                        from the 'content' field of the spec.
#        - _media_type : this field is supplied by the class that
#                        is creating the package.  It is derived 
#                        from the type of package to create,
#                        the type of update, and the "force_update"
#                        flag.
#    The Pkg::Packager class contains some "abstract" methods.  These
#    methods MUST be implemented in the class the inherits from 
#    Pkg::Packager.
#        - get_work_dir : The 'get_work_dir' method 
#        - get_output_dir :
#        - get          :
#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package Pkg::Packager;

#
# Standard Package Preamble
#
require 5.002;
require Exporter;
require Pkg::Object;                        # required for several methods

@ISA    = qw(Exporter Pkg::Object);

use English;
use strict;
#*********************************************************************
# The Product module implements the packaging product object.
#
use vars(qw(@err_table));

#
# $err_table contains the error message formats for the 
# various error messages.  Note that this table is just
# an array, and so the strings need to be in the correct
# "slot" in order to be found for the correct error.
#
@err_table = ("No Error",
  # $Err_Could_Not_Open_File = 1 ($file_name)
	      "Could not open, or create, file (%s).",
  # $Err_Could_Not_Find_File = 2 ($file_name)
	      "Could not find file '%s'.",
  # $Err_Content_Conflict = 3 (no parameters)
	      "Content Conflict between filesets USR/ROOT content " .
	      "and SHARE content in same product.",
  # $Err_Could_Not_Create_TOC = 4 (no parameters)
	      "Failed to create package TOC (lpp_name file).",
  # $Err_Package_Cmd_Failed = 5 (no parameters)
	      "Creation of image failed (adepackage command).",
  # $Err_No_Compids_Entry = 6 ($product_name, $compid_file)
	      "Could not find an entry '%s' in compids file '%s'.",
);
use vars(qw($Err_Could_Not_Open_File  $Err_Could_Not_Find_File
	    $Err_Content_Conflict     $Err_Could_Not_Create_TOC 
	    $Err_Package_Cmd_Failed   $Err_No_Compids_Entry));

$Err_Could_Not_Open_File  =  1;
$Err_Could_Not_Find_File  =  2;
$Err_Content_Conflict     =  3;
$Err_Could_Not_Create_TOC =  4;
$Err_Package_Cmd_Failed   =  5;
$Err_No_Compids_Entry     =  6;

#*********************************************************************
# Object Implementation Section (ie. the code part) 
#*********************************************************************
#-----------------------------------------------------------------
# "Public" Methods
#-----------------------------------------------------------------

#=================================================================
# NAME     : record liblpp file
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
sub record_liblpp_file
{
    my($self, $content, $filename) = @_;
    $self->Debug("$self->{Name}, content = $content, file = $filename");
    my $rc = 0;

    if ($filename =~ /\.copyright$/o)
    {
	push @{$self->{"_copyright_$content"}}, $filename;
    }
    else
    {
	push @{$self->{"_liblpp_$content"}}, $filename;	
    }
    return($rc);
}


#=================================================================
# NAME     : record content
# PURPOSE  : to record and verify the content of a fileset
#            with respect to the product.
#            The verification is simply that a product with
#            USR/ROOT content does not have any SHARE content 
#            and vice versa.
# INPUTS   :
#    $content - the install CONTENT affected by this file.
#               This variable is the single character translation
#               done by the Fileset methods (H = SHARE, U = USR,
#               B = ROOT);
# OUTPUTS  : none
# NOTES    :
#   - Instance variable is modified as a result of this call.
# RETURNS  :
#   0 = success
#  !0 = Error.
#=================================================================
sub record_content
{
    my($self, $content) = @_;
    $self->Debug("$self->{Name}, content = $content");
    my $rc = 0;

    if ($self->{"_content"})
    {
	if ($self->{"_content"} =~ /[BU]/o)
	{
	    # Already have filesets recorded with USR/ROOT content.
	    if ($content eq 'H')
	    {
		$rc = 8;
		$self->error('Error', $Err_Content_Conflict);
	    }
	    else
	    {
		$self->{"_content"} = $content if ($content eq 'B');
	    }
	}
	else
	{
	    # Already have filesets recorded with SHARE content.
	    if ($content ne 'H')
	    {
		$rc = 8;
		$self->error('Error', $Err_Content_Conflict);
	    }
	}
    }
    else
    {
	$self->{"_content"} = $content;
    }
    return($rc);
}


#=================================================================
# NAME     : record boot flag
# PURPOSE  : to record the boot requirements of the filesets
#            belonging to this product.  The recording of the
#            information is based on the "strength" of the flag.
#            'N' (no boot required) is lowest. 'b' (bosboot 
#            rqd, but no reboot) is second.  'B' (bosboot AND
#            reboot rquired) is highest.
# INPUTS   :
#    $boot_flag - The boot_flag from the fileset.
# OUTPUTS  : none
# NOTES    :
#   - Instance variable is modified as a result of this call.
# RETURNS  :
#   0 = success
#  !0 = Error.
#=================================================================
sub record_boot_flag
{
    my($self, $boot_flag) = @_;
    $self->Debug("$self->{Name}, flag = $boot_flag");
    my $rc = 0;

    if ($self->{"_boot_flag"})
    {
	if (($self->{'_boot_flag'} ne 'B') && ($boot_flag =~ /Bb/o))
	{
	    # Saved value not the highest, and the new value
	    # is 1 of top 2, so save the new value regardless
	    # of whether the saved value is 'b' or 'N'
	    $self->{'_boot_flag'} = $boot_flag;
	}
    }
    else
    {
	# No value yet, so just save this one.
	$self->{"_boot_flag"} = $boot_flag;
    }
    return($rc);
}


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

    $self->register_errors(\@err_table);
}


#==========================================================================
# Name     : create package
# Purpose  : 'create_package' is the method that performs the final
#            steps to creating a package and then actually creates
#            the image.  This method works for install and update
#            images.
# Inputs   :
# Outputs  :
#   - an installable image is created (either an update or install image).
# Notes    :
#   - if any of the steps fail, none of the subsequent steps can
#     be performed.
# Returns  :
#    0 = SUCCESS
#   !0 = failure; error messages will be generated here.
#==========================================================================
sub create_package
{
    my $self = shift;
    my($output_dir) = @_;

    my $rc = 0;

    $rc  = $self->generate_productid_file;
    $rc  = $rc < 8 ? $self->generate_liblpp_a : $rc;
    $rc  = $rc < 8 ? $self->generate_TOC() : $rc;
    $rc  = $rc < 8 ? $self->package_image($output_dir) : $rc;

    $rc  = $rc < 8 ? 0 : $rc;

    return($rc);
}

#--------------------------------------------------------------------------
#--------------------------------------------------------------------------
#   P R I V A T E    methods follow
#--------------------------------------------------------------------------
#--------------------------------------------------------------------------
#==========================================================================
# Name     : generate product id
# Purpose  : This function generates the product id information
#            required by IBM distribution services.  I am not sure
# Inputs   :
# Outputs  :
#   - product id file.
# NOTES    :
#   - Note that this step is not required by installp.  It is
#     required for IBM developed products being distributed 
#     through Boulder.
# Returns  :
#    0 = SUCCESS
#   !0 = failure; error messages will be generated here.
#==========================================================================
sub generate_productid_file
{
    my $self = shift;
    $self->Debug();

    my $rc = 0;

    my $id_file = $self->get_work_dir() . "/productid";
    unlink($id_file);

    my $compids = $self->find_file($self->get('compids_table'));
    if (defined($compids))
    {
	if (open(COMPS, "<$compids"))
	{
	    if (open(PRODID, ">$id_file"))
	    {
		my $prod = $self->get('_product_name');
		my($line, $raw_id, $junk);
		while ($line = <COMPS>)
		{
		    if ($line =~ /^$prod:/)
		    {
			($junk, $raw_id, $junk) = split(/:/, $line);
			last;
		    }
		}
		close(COMPS);
		
		if ($raw_id ne "")
		{
		    # Turn Raw ID into correct format.
                    print(PRODID "$prod ");
		    print(PRODID substr($raw_id, 0, 4), "-", 
			  substr($raw_id, 4, 5), "\n");
		    $self->record_liblpp_file($self->{'_content'} eq "H" 
					          ? 'H' : 'U',
					      $id_file);
                }
		else
		{
		    $self->error("Warning", $Err_No_Compids_Entry, 
				 $prod, $compids);
		    $rc = 4;
		}
		close(PRODID);
	    }
	    else
	    {
		$self->error("Warning", $Err_Could_Not_Open_File, 
			     $id_file, $!);
		$rc = 4;
	    }
	}
	else
	{
	    $self->error("Warning", $Err_Could_Not_Open_File, $compids, $!);
	    $rc = 4;
	}
    }
    return($rc);
} # END generate_productid_file


#==========================================================================
# Name     : generate liblpp.a
# Purpose  : This function generates the liblpp.a archive.
#            This archive contains all of the control information
#            and scripts needed by installp to install the product.
# Inputs   :
# Outputs  :
#   - liblpp.a is created.
# Returns  :
#    0 = SUCCESS
#   !0 = failure; error messages will be generated here.
#==========================================================================
sub generate_liblpp_a
{
    my $self = shift;
    $self->Debug();
    my $rc = 0;

    my $liblpp;
    if ($self->{'_content'} eq 'H')
    {
	$liblpp = $self->get_work_dir . "/data/liblpp.a";
	$rc = $self->build_liblpp($liblpp, $self->{'_liblpp_H'}, 
          $self->{'_copyright_H'});
    }
    else
    {
	# Do the USR content liblpp.a first.
	$liblpp = $self->get_work_dir . "/liblpp.a";
	$rc = $self->build_liblpp($liblpp, $self->{'_liblpp_U'}, 
          $self->{'_copyright_U'});
	#
	# if all is well, and there is some ROOT content, do
	# the ROOT liblpp.a
	if (($rc == 0) && ($self->{'_content'} eq 'B'))
	{
	    if (defined($self->{'_liblpp_B'}))
	    {
		$liblpp = $self->get_work_dir . "/root/liblpp.a";
		$rc = $self->build_liblpp($liblpp, $self->{'_liblpp_B'});
	    }
	    else
	    {
		# if there is nothing to put into the ROOT liblpp.a,
		# then there really is no ROOT content.  Try to
		# reset the content to USR.
		$self->record_content('U');
	    }
	}
    }
    return($rc);
} # END generate_liblpp_a


#==========================================================================
# Name     : generate table of contents
# Purpose  : This function generates the table of contents for
#            the product.  'adelppname' is used at this time.
# Inputs   :
# Outputs  :
#   - output is really created by 'adelppname'.
# Returns  :
#    0 = SUCCESS
#   !0 = failure; error messages will be generated here.
#==========================================================================
sub generate_TOC
{
    my $self = shift;
    $self->Debug();
    my $rc = 0;
    my $format = "adelppname -l %s -u %s -v %d -r %d -m %d -F %d " .
	         "-p %1.1s -f 4 -t %s -c %s -o %s ";

    # Update packages require extra parameters on the command line.
    if ($self->{'_pkg_type'} eq "Update")
    {
	#------------------------------------------------------------------
	# NOTE:
	#   If the key file cannot be found, leave the flag
	#   off.  It is assumed that when this file was to be 
	#   created that error messages would have been generated.
	#   Furthermore, if the lack of such a file is a fatal
	#   error, we would not have gotten to this point.
	#------------------------------------------------------------------
	my $keyfile = $self->find_file($self->get('_key_info'));
	if ($keyfile ne "")
	{
	    $format .= " -k $keyfile ";
	}
    }
    my($outfile, $liblpp);
    if ($self->{'_content'} eq 'H')
    {
	# SHARED content product.  Must put the ouput in the 'data'
	# dir and get liblpp.a from there.
	$outfile = "data/lpp_name";
	$liblpp  = "data/liblpp.a";
    }
    else
    {
	# USR/ROOT content product.  Output goes in the current
	# dir and liblpp.a comes from there also.
	$outfile = "lpp_name";
	$liblpp  = "liblpp.a";
	
    }
    my $cmd = sprintf($format, 
		      $self->{'_product_name'},    $self->{'_lp_file'}, 
		      $self->get('version'),       $self->get('release'),
		      $self->get('maint_level'),   $self->get('fix_level'),
		      uc($self->get('platform')),
		      $self->get('_media_type'),   $self->get('compids_table'),
		      $outfile);

    if ($rc = $self->execute_cmd("$cmd"))
    {
	$self->error('Severe', $Err_Could_Not_Create_TOC);
	$rc = 16;
    }    
    return($rc);
} # END generate_TOC


#==========================================================================
# Name     : package image
# Purpose  : This function puts all of the pieces together to 
#            create the installable image.
# Inputs   :
#   $output_dir - the directory in which the image file is to be 
#             placed.
# Outputs  :
#   - output is really created by 'adepackage'.
#
# Returns  :
#    0 = SUCCESS
#   !0 = failure; error messages will be generated here.
#==========================================================================
sub package_image
{
    my($self, $output_dir) = @_;
    $self->Debug();
    my $rc = 0;
    my $format = "adepackage -l %s -f %s -i %s -s %s ";

    # Update packages require extra parameters on the command line.
    if ($self->{'_pkg_type'} eq "Update")
    {
	$format .= " -U " . $self->get('_long_VRMF') .
	           " -o " . $self->get('Name');
    }
    $format .= "-D" if ($self->{'_content'} eq 'H');
    $format .= " %s ";

    # construct the output file.  If an output directory is
    # specified, the file will be placed in there.  Note
    # that if the output directory is not absolute, it is
    # made to be relative to the start dir.
    #
    my $filename;
    if (substr($self->{'image_name'}, 0, 1) eq "/")
    {
	$filename = $self->{'image_name'};
    } 
    elsif ($output_dir ne "")
    {
	$output_dir =~ s?/*$??o;        # remove any trailing slashes
	if (substr($self->{'image_name'}, 0, 1) eq "/")
	{
	    $filename = 
		  "$self->{'_start_dir'}/$output_dir/$self->{'image_name'}";
	}
	else
	{
	    $filename = "$output_dir/$self->{'image_name'}";
	}
    }
    else
    {
	$filename = "$self->{'_start_dir'}/$self->{'image_name'}";
    }
    # Finally, construct and run the command...
    #
    my $cmd = sprintf($format,   $self->get('_product_name') , $filename, 
		      $self->{'_inslist'}, $self->get('ship_path'),  
		      $self->get('adepackage_flags'));
    if ($rc = $self->execute_cmd("$cmd"))
    {
	$self->error('Severe', $Err_Package_Cmd_Failed);
	$rc = 16;
    } 
    return($rc);
} # END package_image


#=================================================================
# NAME     : build liblpp.a
# PURPOSE  : loads the given set of files into the appropriate
#            liblpp.a file.
# INPUTS   : 
#    $liblpp - Pathname of the liblpp.a to be created.
#    $flist1  - ref to list of files that must be first in archive
#    $flist2  - ref to list of other files to be included in the archive
# OUTPUTS  :
#    - liblpp.a archive is created.
# NOTES    :
#   - what if the list of files gets too long for the command line??
# RETURNS  : 
#    0 = success
#   !0 = failure
#=================================================================
sub build_liblpp
{
    my $self = shift;
    my($liblpp, $rest, $cr_files) = @_;
    $self->Debug("cr_files = $cr_files; rest = $rest");

    my $rc = 0;
    my $flags = $self->get('ar_flags');
    $flags .= "clq";
    my $cmd = "ar $flags $liblpp ";

    if (defined($cr_files))
    {
	chmod(0750, @{$cr_files});
	$cmd .= "@{$cr_files}";
    } 

    chmod(0750, @{$rest});
    $cmd .= " @{$rest}";

    unlink($liblpp) if (-f $liblpp);
    return(($self->execute_cmd($cmd) == 0) ? 0 : 16);
}
1;
