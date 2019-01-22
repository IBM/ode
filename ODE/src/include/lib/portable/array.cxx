/**
 * Array
 *
 * This file should be included by one .cpp file per
 * program/library, in which explicit instantiation(s)
 * should take place for all types of this template
 * used in that program/library.
 *
**/
#define _ODE_LIB_PORTABLE_ARRAY_CPP_
#include "lib/portable/array.hpp"


template< class Type >
Array< Type >::Array( unsigned long size,
    boolean (*equalCompareFunc)(
        const Type &elem1, const Type &elem2 ) ) :
    elementsEqual( equalCompareFunc ),
    elements( 0 ), num_elements( 0 ), num_allocated( 0 )
{
  if (size > 0)
    extendElements( size );
}

template< class Type >
Array< Type >::Array( const Array< Type > &array ) :
    elementsEqual( array.elementsEqual ),
    elements( 0 ), num_elements( 0 ), num_allocated( 0 )
{
  append( array );
}

template< class Type >
Array< Type >::~Array()
{
  destroy();
}

template< class Type >
void Array< Type >::destroy()
{
  delete[] elements;
  elements = 0;
  num_elements = num_allocated = 0;
}

template< class Type >
void Array< Type >::setNumElements( unsigned int new_num_elements )
{
  if (new_num_elements > num_allocated)
    extendElements( (new_num_elements - num_allocated) + ARRAY_ALLOC_SIZE );
  num_elements = new_num_elements;
}


template< class Type >
Array< Type > &Array< Type >::append( const Array< Type > &array )
{
  if ((array.num_elements + num_elements) > num_allocated)
    extendElements( array.num_elements );
  // note that lastIndex() shouldn't be called in the "while" portion
  // of the for syntax, so that an array can be appended to itself.
  for (int i = ARRAY_FIRST_INDEX, j = array.lastIndex(); i <= j; ++i)
    this->append( array[i] );
  return (*this);
}

template< class Type >
Array< Type > &Array< Type >::append( const Type &element )
{
  if (num_elements >= num_allocated)
    extendElements();
  elements[num_elements] = element;
  ++num_elements;
  return (*this);
}

template< class Type >
Array< Type > &Array< Type >::insertAtPosition( const Type &element, int idx )
{
  if ( num_elements >= num_allocated )
    extendElements();

  // Bump all items after insert position one position so we have
  // room for the new guy.
  int i = num_elements;
  while ( i > idx )
  {
    elements[i] = elements [i-1];
    --i;
  }

  elements[idx] = element;
  ++num_elements;
  return (*this);
}

template< class Type >
void Array< Type >::extendElements( unsigned long extend_size )
{
  num_allocated += extend_size;
  Type *tmp = new Type[num_allocated];
  for (int i = 0; i < num_elements; ++i)
    tmp[i] = elements[i];
  delete[] elements;
  elements = tmp;
}

/**
 * WARNING: expensive function...try not to use if possible.
**/
template< class Type >
Array< Type > &Array< Type >::prepend( const Array< Type > &array )
{
  if (this == &array)
    return (*this);
  if ((array.num_elements + num_elements) > num_allocated)
    extendElements( array.num_elements );
  for (int i = array.lastIndex(); i >= ARRAY_FIRST_INDEX; --i)
    this->prepend( array[i] );
  return (*this);
}

/**
 * WARNING: expensive function...try not to use if possible.
**/
template< class Type >
Array< Type > &Array< Type >::prepend( const Type &element )
{
  if (num_elements >= num_allocated)
    extendElements();
  for (int i = num_elements; i > 0; --i)
    elements[i] = elements[i - 1];
  elements[0] = element;
  ++num_elements;
  return (*this);
}

/**
 * Equality is defined as all elements, in the same order, being
 * equal (according to Type::operator==).
**/
template< class Type >
boolean Array< Type >::operator==( const Array< Type > &array ) const
{
  if (this == &array)
    return (true);
  if (this->size() != array.size())
    return (false);
  // the if statement is outside the while loop for performance reasons.
  // this logic should be cleaned up (the elementsEqual check need only
  // happen once per construction).
  if (this->elementsEqual == 0)
  {
    for (int i = ARRAY_FIRST_INDEX; i <= lastIndex(); ++i)
      if (!(this->elementAtPosition( i ) == array.elementAtPosition( i )))
        return (false);
  }
  else
  {
    for (int i = ARRAY_FIRST_INDEX; i <= lastIndex(); ++i)
      if (!this->elementsEqual( this->elementAtPosition( i ),
          array.elementAtPosition( i ) ))
        return (false);
  }
  return (true);
}

template< class Type >
Array< Type > &Array< Type >::operator=( const Array< Type > &array )
{
  if (this == &array)
    return (*this);
  this->clear();
  return (append( array ));
}

/**
 * Add enough copies of element to extend the size of
 * the array to newsize.  If the array already contains
 * at least newsize elements, this function does nothing.
**/
template< class Type >
Array< Type > &Array< Type >::extendTo( unsigned int newsize,
    const Type &element )
{
  if (newsize > num_allocated)
    extendElements( newsize - num_allocated );
  for (int i = num_elements; i < newsize; ++i)
    this->append( element );
  return (*this);
}

/**
 * Add enough empty elements to extend the size of
 * the array to newsize.  If the array already contains
 * at least newsize elements, this function does nothing.
 * We don't add ARRAY_ALLOC_SIZE since we'll assume the
 * user wants EXACTLY newsize elements.
**/
template< class Type >
Array< Type > &Array< Type >::extendTo( unsigned int newsize )
{
  if (newsize > num_allocated)
    extendElements( newsize - num_allocated );
  if (newsize > num_elements)
    num_elements = newsize;
  return (*this);
}

template< class Type >
void Array< Type >::removeAtPosition( unsigned long index )
{
  index -= ARRAY_FIRST_INDEX; // normalize for native array
  --num_elements;
  for (; index < num_elements; ++index)
    elements[index] = elements[index + 1];
}
