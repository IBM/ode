#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++  
#
# Licensed Materials - Property of IBM
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
# NAME        : Pkg::Config Class
# DESCRIPTION :
#    The 'Pkg::Config' Class implements the default configuration
#    information access method for the pkgtools toolset.  
#    This class stores the default packaging configuration data
#    found in a configuration data file and then provides
#    the methods necessary to access that information.
#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package Pkg::Config;

#
# Standard Package Preamble
#
require 5.002;
require Exporter;
require Pkg::Log;
@ISA    = qw(Exporter Pkg::Log);

#*************************************************************************
# The Pkg::Config package finds and reads the configuration
# data file.  It obtains the processing capability from the
# Pkg::Parser class.  The Parser is able to determine that a
# config file is being loaded rather than a defintion file.
#*************************************************************************

#-----------------------------------------------------------------
# Possible Error Codes and their messages....
#
use vars(qw(@err_table $OK 
	    $Err_No_Config_File $Err_No_Default_File));

@err_table = ("No Error Detected.",
  # $Err_No_Config_File = 1 ($cfg_file)
    "Could not find the given config file (%s).\n",
  # $Err_No_Default_file = 2 (no parameters)
    "Could not find the default config file.\n"
);
$OK                  = 0;
$Err_No_Config_File  = 1;
$Err_No_Default_File = 2;
#
#-----------------------------------------------------------------
#-----------------------------------------------------------------
# "Public" Methods
#-----------------------------------------------------------------
#=================================================================
# NAME     : constructor (new)
# PURPOSE  : create the configuration object and initialize it.
# INPUTS   : none
# OUTPUTS  : none
# RETURNS  : pointer to object
#=================================================================
sub new
{
    my $class = shift;
    my ($cpp_flags, $no_cpp, $file) = @_;

    my $self  = Pkg::Log->new();
    bless $self, $class;
    my $rc = $self->initialize($cpp_flags, $no_cpp, $file);
    if ($rc)
    {
	$self = undef;
    }
    return($self);
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
#   - ensures the attribute is a valid, known name -- not yet though
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

    # REALLY need to add validation similar to the Fileset/Product
    # classes.
    $self->{$attr} = $value;
    
    return($rc);
}


sub Dump
{
    my $self = shift;

    my $save_ofs = $OFS;
    $OFS = " ";
    my $field;
    print STDOUT "\nPkg::Config::Dump:\n";
    
    foreach $field (sort keys %{$self})
    {
	print STDOUT "    $field\t= ";
	if (ref($self->{$field}) =~ /ARRAY/o)
	{
	    print STDOUT "[@{$self->{$field}}]\n";
	}
	else
	{
	    print STDOUT "$self->{$field}\n";
	}
    }
    print STDOUT "\n";
    $OFS = $save_ofs;
}
#-----------------------------------------------------------------
#-----------------------------------------------------------------
# "Private" Data and Methods
#-----------------------------------------------------------------
#-----------------------------------------------------------------
#=================================================================
# NAME     : initialize
# PURPOSE  : initializes the newly created Pkg::Config object
# INPUTS   : none
# OUTPUTS  : none
# RETURNS  : nothing
#=================================================================
sub initialize
{
    my ($self, $cpp_flags, $no_cpp, $file) = @_;
    my $rc = 0;

    # Set initial state, and save the inputs 1st.
    # 
    $self->register_errors(\@err_table);
    $self->{Name} = "Pkg::Config";
    $self->{'cpp_flags'} = $cpp_flags;
    $self->{'no_cpp'}    = $no_cpp;

    # Finally, load the config file....
    #
    if ($file ne "")
    {
	my $parser = Pkg::Parser->new($self, $cpp_flags, $no_cpp, 1);
	$rc = $parser->load_files($file);
    }
    else
    {
	# Find the default config file (pkgtools.conf).  It should be
	# in the INC path some where
	#
	my $dir;
	my $dflt_file;
	my $found_it = 0;
	foreach $dir (@INC)
	{
	    $dflt_file = "$dir/pkgtools.conf";
	    $self->Debug("Checking for '$dflt_file'.");
	    if (-f $dflt_file && -r $dflt_file)
	    {
		$found_it = 1;
		last;
	    }
	}
	if ($found_it)
	{
	    my $parser = Pkg::Parser->new($self, $cpp_flags, $no_cpp, 1);
	    $rc = $parser->load_files($dflt_file);
	}
	else
	{
	    $rc = $Err_No_Default_File;
	    $self->error('Severe', $rc);
	}
    }
    return($rc);
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
    $self->Debug("'$field' -- " . defined($self->{$field}) . "...");
    return(defined($self->{$field}) ? $self->{$field} : undef);
}
1;
