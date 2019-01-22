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
# NAME        : ODM_stanza
# DESCRIPTION :
#  The "ODM_stanza" class implements an indivudual ODM stanza typically
#  found in an ODM stanza file.  These stanzafiles are often called 
#  "ODM add files" or just simply "add files".
#
#  This class simpy holds a stanza.  The ODM_stanza class is typically
#  used in conjunction with the ODM_stanza_file class to manage an
#  ODM stanza file.  The "ODM_stanza_file" class holds instances of
#  ODM_stanza objects.
#
#  This class uses the following instance variables:
#    - class   : name of the class to which the stanza belongs.
#    - <attr_name> : each attribute/field for the stanza is 
#                kept in a instance variable by attribute name.
#++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
package ODM_stanza;

require 5.002;
require Exporter;
@ISA = (qw(Exporter));

use English;
use Carp;
use strict;
#==========================================================================
# NAME    : new
# PURPOSE : 'new' is the constructor for to 'ODM_stanza' class.
# INPUTS  :
#    - $odm_class : optional parameter identifying the ODM class to
#           which this stanza belongs.
# OUTPUTS : 
#     none.
# RETURNS :
#    SUCCESS = reference to ODM_stanza object.
#    FAILURE = undef
#==========================================================================
sub new
{
    my($class, $odm_class) = @_;

    my $self = {};
    bless $self, $class;

    $self->{'class'} = $odm_class;

    return($self);
} # END new()


#==========================================================================
# NAME    : store attribute/value pair
# PURPOSE : 'store_pair' stores the given attribute/value pair
#           in the ODM_stanza object.
# INPUTS  :
#   - $attr  - the name of the attribute.
#   - $value - the value to to associated with the attribute.
# OUTPUTS :
#   none.
# NOTES   :
# RETURNS :
#    SUCCESS = 0
#    FAILURE = non-zero
#==========================================================================
sub store_pair
{
    my $self = shift;
    my($attr, $value) = @_;
    my $rc = 0;

    $attr =~ s/^\s*(.*?)\s*$/$1/;
    $self->{$attr} = $value;

    return($rc);
} # END store_pair()


#==========================================================================
# NAME    : get class
# PURPOSE : 'get_class' gets the odm_class name for the stanza object.
# INPUTS  :
#   none.
# OUTPUTS :
#   none.
# RETURNS :
#   SUCCESS = odm_class to which the object belongs.
#   FAILURE = empty string
#==========================================================================
sub get_class
{
    my $self = shift;

    return($self->{'class'});
} # END get_class()



#==========================================================================
# NAME    : get value
# PURPOSE : 'get_value' gets the value for the specified attribute.
# INPUTS  :
#    - $attr : name of the attribute whose value is wanted.
# OUTPUTS :
#    none.
# NOTES   :
#    - none.
# RETURNS :
#    SUCCESS = value string
#    FAILURE = undef.
#==========================================================================
sub get_value
{
    my $self = shift;
    my($attr) = @_;

    return($self->{$attr});
} # END get_value()


#==========================================================================
# NAME    : print
# PURPOSE : 'print' prints the stanza in a format acceptable to odmadd.
# INPUTS  :
#   $fh   : filehandle where output is to be sent.
# OUTPUTS :
#   - output sent to given file handle.
# NOTES   :
#   - none.
# RETURNS :
#   - results of print
#==========================================================================
sub print
{
    my $self = shift;
    my($fh) = @_;

    my $rc = print($fh "\n$self->{'class'}:\n");

    my $attr;
    foreach $attr (sort(keys %{$self}))
    {
	last if $rc == 0;
	next if ($attr eq "class");
	$rc = print($fh "\t$attr = $self->{$attr}\n");
    }
    return(!$rc);
} # END print
1;
