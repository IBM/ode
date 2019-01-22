/**
 * Vector
 *
 * This file should be included by one .cpp file per
 * program/library, in which explicit instantiation(s)
 * should take place for all types of this template
 * used in that program/library.
**/
#define _ODE_LIB_PORTABLE_VECTOR_CPP_
#include "lib/portable/vector.hpp"


template< class Type >
unsigned long Vector< Type >::copyInto( Type array[] ) const
{
  for (unsigned int i = ARRAY_FIRST_INDEX; i <= this->lastIndex(); ++i)
    array[i] = this->elementAtPosition( i );
  return (this->size());
}


template< class Type >
unsigned int Vector< Type >::indexOf( const Type &element,
    unsigned int index ) const
{
  index = (index < ARRAY_FIRST_INDEX) ? ARRAY_FIRST_INDEX : index;

  // the ifs are outside the for-loops since elementsEqual only
  // needs to be tested once.  this is a quick-n-dirty performance
  // tactic, and should be cleaned up properly at some point.
  VectorEnumeration< Type > vector_enum( this );
  int i = ARRAY_FIRST_INDEX;
  if (this->elementsEqual == 0)
  {
    while (vector_enum.hasMoreElements())
    {
      if (*vector_enum.nextElement() == element)
        return (i);
      i++;
    }  
  }
  else
  {
    while (vector_enum.hasMoreElements())
    {
      if (this->elementsEqual( *vector_enum.nextElement(), element ))
        return (i);
      i++;
    }
  }
  return (ELEMENT_NOTFOUND);
}


template< class Type >
unsigned int Vector< Type >::lastIndexOf( const Type &element,
    unsigned int index ) const
{
  index = (index > this->lastIndex()) ? this->lastIndex() : index;

  // the ifs are outside the for-loops since elementsEqual only
  // needs to be tested once.  this is a quick-n-dirty performance
  // tactic, and should be cleaned up properly at some point.
  if (this->elementsEqual == 0)
  {
    for (unsigned int i = index; i >= ARRAY_FIRST_INDEX; --i)
      if (this->elementAtPosition( i ) == element)
        return (i);
  }
  else
  {
    for (unsigned int i = index; i >= ARRAY_FIRST_INDEX; --i)
      if (this->elementsEqual( this->elementAtPosition( i ), element ))
        return (i);
  }
  return (ELEMENT_NOTFOUND);
}


template< class Type >
boolean Vector< Type >::removeElement( const Type &element )
{
  unsigned int index;
  
  if ((index = indexOf( element )) != ELEMENT_NOTFOUND)
  {
    this->removeAtPosition( index );
    return (true);
  }
  return (false);
}
