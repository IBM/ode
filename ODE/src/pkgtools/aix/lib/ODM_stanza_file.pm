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
# NAME        : ODM_stanza_file
# DESCRIPTION :
#  The "ODM_stanza_file" class implements an interface to IBM ODM stanza
#  files.  These files are often called "ODM add files" or just simply
#  "add files".
#
#  This class provides a mechanism to read a stanza file, remove stanzas 
#  from the set read, add stanzas to the set read, search the set of
#  stanzas, and to write the set of stanzas to a file (which will be 
#  suitable for the 'odmadd' command.
#
#  This class uses the following instance variables:
#    - file_name : The name of the file (if any) from which the stanzas
#                  were read.  If multiple files were used to populate
#                  a particular object, this variable will only contain
#                  the name of the last file read.
#    - file_cnt  : A count of the number of files read to populate a
#                  particular object.  Starts at zero.
#    - stanza_cnt: A count of the number of stanzas read.
#    - stanzas   : The primary variable.  The 'stanzas' variable is 
#                  an array of references 'odm_stanza' objects.
#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package ODM_stanza_file;

require 5.002;
require Exporter;
@ISA = (qw(Exporter));

use English;
use Carp;
use strict;

use ODM_stanza;
#==========================================================================
# NAME    : new
# PURPOSE : 'new' is the constructor for to 'ODM_stanza_file' class.
# INPUTS  :
#    - $fname : optional parameter identifying the file to read
#               during contstruction
# OUTPUTS : 
#     none.
# RETURNS :
#    SUCCESS = reference to ODM_stanza_file object.
#    FAILURE = undef
#==========================================================================
sub new
{
    my($class, $fname) = @_;

    my $self = {};
    bless $self, $class;

    if (defined($fname))
    {
	if ($self->read_file($fname) != 0)
	{
	    $self = undef;
	}
    }
    return($self);
} # END new()


#==========================================================================
# NAME    : read_file
# PURPOSE : 'read_file' reads the given stanza file creating ODM_stanza
#           objects and storing them in the 'stanzas' hash table.
# INPUTS  :
#   - $fname : pathname of the stanza file to read.
# OUTPUTS :
#   none.
# NOTES   :
# RETURNS :
#    SUCCESS = 0
#    FAILURE = non-zero
#==========================================================================
sub read_file
{
    my $self = shift;
    my($fname) = @_;
    my $rc = 0;

    if (open(ADD, "<$fname"))
    {
	$self->{'file_name'} = $fname;
	$self->{'file_cnt'}++;
	
	my($line, $stanza, $odm_class, $attr, $value);
	my $state = 1;
	while ($line = <ADD>)
	{
	    chomp $line;
	    if ($state == 1)
	    {
		# Looking for an "attribute/value" pair
		if ($line =~ /^\s+(\w+)\s+=\s+/o)
		{
		    # Got a pair.
		    ($attr, $value) = split(/\s+=\s+/, $line, 2);
		    if (substr($value, 0, 1) eq "\"")
		    {
			if (substr($value, -1, 1) ne "\"")
			{
			    # Have a continued string.
			    $state = 2;
			}
			else
			{
			    $stanza->store_pair($attr, $value);
			}
		    }
		    else
		    {
			$stanza->store_pair($attr, $value);
		    }
		}
		elsif ($line =~ /^\s*(\w+)\s*:\s*$/o)
		{
		    # found start of a stanza!
		    $odm_class = $1;
		    $state = 1;
		    $stanza = ODM_stanza->new($odm_class);
		    if (defined($stanza))
		    {
			$self->add_stanza($stanza)
		    }		    
		}
	    }
	    elsif ($state == 2)
	    {
		# Got a value continuation situation.
		$value .= "\n$line";
		if (substr($line, -1, 1) eq "\"")
		{
		    $state = 1;
		    $stanza->store_pair($attr, $value);
		}
	    }
	    else
	    {
		# Got an invalid internal state error!
		croak("Invalid state encountered ($state) " .
		      "while loading '$fname'\n");
		$rc = 100;
	    }
	}
	close(ADD);
	if ($state == 2)
	{
	    # End of file encountered while still have value
	    # continuation active.
	    carp("The value for field '$attr' in class '$odm_class' " .
		 "in '$fname' did not complete before EOF!\n");
	    $rc = 101;
	}
    }
    else
    {
	carp("Unable to open file '$fname'; $!");
	$rc = 1;
    }
    return($rc);
} # END read_file()


#==========================================================================
# NAME    : write_file
# PURPOSE : 'write_file' writes the ODM stanzas in the 'stanzas' hash
#           table to the given file.
# INPUTS  :
#   - $fname : '$fname' is the pathname of the file in which to write
#              the stanzas.  If no file is given, the stanzas are
#              written to the file identified by the 'file_name' 
#              instance variable.
# OUTPUTS :
#   - file is written on disk.
# NOTES   :
#   - if no file can be identified by either method described above,
#     the operation is aborted.
# RETURNS :
#   SUCCESS = 0
#   FAILURE = non-zero
#==========================================================================
sub write_file
{
    my $self = shift;
    my($fname) = @_;
    my $rc = 0;

    $fname = $self->{'file_name'} if (!defined($fname));
    if (defined($fname))
    {
	if (open(ADD, ">$fname"))
	{
	    my $stanza;
	    foreach $stanza (@{$self->{'stanzas'}})
	    {
		$stanza->print(\*ADD);
	    }
	    close(ADD);
	}
	else
	{
	    carp("Could not open '$fname'; $!\n");
	    $rc = 2;
	}
    }
    else
    {
	carp("Cannot write stanza file, no file_name found!\n");
	$rc = 1;
    }
    return($rc);
} # END write_file



#==========================================================================
# NAME    : add stanza
# PURPOSE : 'add_stanza' adds the given set of stanzas to the
#           'stanzas' array.
# INPUTS  :
#    - $ref : could be reference to a single stanza, or a 
#           reference to an array of stanzas.
# OUTPUTS :
#    - 'stanzas' : instance variable is modified.
# NOTES   :
#    - Perl does not support overloading of functions per se.  However,
#      parameters can have different types by default.  This method
#      can handle 2 different parameter types.
#        1) a reference to a single stanza
#        2) a reference to an array of references to stanzas.
# RETURNS :
#    SUCCESS = 0
#    FAILURE = non-zero
#==========================================================================
sub add_stanza
{
    my $self = shift;
    my($ref) = @_;
    my $rc = 0;

    if (ref($ref) =~ /^ARRAY/o)
    {
	# Have an array.
	my $stanza;
	foreach $stanza (@{$ref})
	{
	    $self->add_stanza($stanza);
	}
    }
    elsif (ref($ref) =~ /^ODM_stanza/o)
    {
	push(@{$self->{'stanzas'}}, $ref);
	$self->{'stanza_cnt'}++;
    }
    else
    {
	carp("Unknow parameter to 'add_stanza' - " . ref($ref) . "\n");
	$rc = 1;
    }
    return($rc);
} # END rm_stanza


#==========================================================================
# NAME    : remove stanza
# PURPOSE :
# INPUTS  :
# OUTPUTS :
# NOTES   :
# RETURNS :
#==========================================================================
sub rm_stanza
{
    my $self = shift;
    my $rc = 1;

    carp("The 'rm_stanza' method is not yet functional...\n");
    return($rc);
} # END rm_stanza


#==========================================================================
# NAME    : find stanza
# PURPOSE : 'find_stanza' provides a mechanism to search a particular
#           ODM class for stanzas matching a pattern.
# INPUTS  :
#   $class : the ODM class name to search.
#   $crit  : reference to the search criteria.
#            $crit is a reference to a hash of "attribute/value" pairs.
#            However, the value is a Perl regular expression to use
#            on the associated attribute (or field).  Each pair must
#            match in order for the stanza to pass the criteria.  There
#            is currently no mechanism for an OR type operation.
# OUTPUTS :
#   none
# NOTES   :
#   - If '$crit' contains multiple "attribute/value" pairs, ALL criteria
#     must pass in order for the stanza to match.  The operation is an
#     AND operation.
#   - There is currently no mechanism for an "OR" operation on the
#     criteria values.
# RETURNS :
#   reference to an array of references to ODM_stanza objects.
#==========================================================================
sub find_stanza
{
    my $self = shift;
    my($class, $crit) = @_;
    my $result = [];
    
    if (ref($crit) =~ /^HASH/o)
    {
	my $stanza;
	foreach $stanza (@{$self->{'stanzas'}})
	{
	    if ($stanza->get_class() =~ /$class/)
	    {
		my($attr, $pat);
		my $matched = 1;
		foreach $attr (keys %{$crit})
		{
		    $pat = $crit->{$attr};
		    if ($stanza->get_value($attr) !~ /$pat/)
		    {
			$matched = 0;
			last;
		    }
		}
		if ($matched)
		{
		    push(@{$result}, $stanza);
		}
	    }
	}
    }
    else
    {
	carp("Search criteria is not in a HASH table, " . ref($crit) . "\n");
	$result = undef;
    }
    return($result);
} # END find_stanza

#==========================================================================
# NAME    : get stanza count
# PURPOSE : 'get_stanza_cnt' returns the value in the 'stanza_cnt' field
#           to the caller.
# INPUTS  :
#     none.
# OUTPUTS : 
#     none.
# RETURNS :
#    value in 'stanza_cnt' field.
#==========================================================================
sub get_stanza_cnt
{
    my $self = shift;

    return($self->{'stanza_cnt'});
} # END get_stanza_cnt()
1;
