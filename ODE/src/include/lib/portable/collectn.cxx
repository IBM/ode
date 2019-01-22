/**
 * Functions available to all collections.
 *
 * This file should be included by one .cpp file per
 * program/library, in which explicit instantiation(s)
 * should take place for all types of this template
 * used in that program/library.
**/
#define _ODE_LIB_PORTABLE_COLLECTN_CPP_
#include "lib/portable/collectn.hpp"

template< class Type >
boolean elementsEqual( const Type &elem1, const Type &elem2 )
{
  if (elem1 == 0 && elem2 == 0)
    return (true);
  if (elem1 != 0 && elem2 != 0)
    return (*elem1 == *elem2);
  return (false);
}
