/**
 * Stack
 *
 * This file should be included by one .cpp file per
 * program/library, in which explicit instantiation(s)
 * should take place for all types of this template
 * used in that program/library.
**/
#define _ODE_LIB_PORTABLE_STACK_CPP_
#include "lib/portable/stack.hpp"


template< class Type >
Type *Stack< Type >::pop()
{
  Type *rc = new Type( *peek() );
  this->removeAtPosition( this->lastIndex() );
  return (rc);
}

template< class Type >
const Type &Stack< Type >::push( const Type &element )
{
  this->addAsLast( element );
  return (element);
}
