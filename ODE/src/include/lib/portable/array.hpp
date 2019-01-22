/**
 * Array
 *
 * Note that the type of object stored must have
 * operator== defined.
**/
#ifndef _ODE_LIB_PORTABLE_ARRAY_HPP_
#define _ODE_LIB_PORTABLE_ARRAY_HPP_


#include <base/odebase.hpp>
#include "lib/portable/collectn.hpp"

// each time the array is reallocated, how many
// EXTRA elements do we allocate?
#define ARRAY_ALLOC_SIZE 8
#define ARRAY_DEFAULT_SIZE 20


template< class Type >
class Array
{
  public:

    // constructors

    // This constructor does actually create an array of size.
    Array( unsigned long size = ARRAY_DEFAULT_SIZE,
        boolean (*equalCompareFunc)(
        const Type &elem1, const Type &elem2 ) = 0 );
    Array( const Array< Type > &array );
    ~Array();

    void destroy();
    void removeAtPosition( unsigned long index );

    inline void clear(); // this one exists only for speed
    inline void clear( boolean deallocate );
    inline unsigned long size() const;   // number of elements in array
    inline unsigned long length() const; // same as size()
    inline boolean isEmpty() const;
    inline unsigned long firstIndex() const;
    inline unsigned long lastIndex() const;

    inline Type &operator[]( unsigned long index );
    inline const Type &operator[]( unsigned long index ) const;
    inline Type &elementAtPosition( unsigned long index ) const;

    // concatenate an array or a single element
    inline Array< Type > &operator+=( const Array< Type > &array );
    inline Array< Type > &operator+=( const Type &element );

    // equality comparison
    boolean operator==( const Array< Type > &array ) const;

    // assignment
    Array< Type > &operator=( const Array< Type > &array );

    Array< Type > &prepend( const Array< Type > &array );
    Array< Type > &prepend( const Type &element );
    Array< Type > &append( const Array< Type > &array );
    Array< Type > &append( const Type &element );
    Array< Type > &insertAtPosition( const Type &element, int idx );
    inline Array< Type > &extendTo( unsigned int newsize,
        const Type &element );
    Array< Type > &extendTo( unsigned int newsize );
    inline Array< Type > *extendToAsPtr( unsigned int newsize );
    inline Array< Type > *extendToAsPtr( unsigned int newsize,
        const Type &element );
    inline Array< Type > &add( const Type &element );

    void setNumElements( unsigned int new_num_elements );

  protected:

    boolean (*elementsEqual)( const Type &elem1, const Type &elem2 );


  private:

    // incrementally increase elements
    void extendElements( unsigned long extend_size = ARRAY_ALLOC_SIZE );
    Type *elements;
    unsigned long num_elements;
    unsigned long num_allocated;
};

template< class Type >
inline unsigned long Array< Type >::size() const
{
  return (num_elements);
}

template< class Type >
inline unsigned long Array< Type >::length() const
{
  return (num_elements);
}

template< class Type >
inline boolean Array< Type >::isEmpty() const
{
  return (num_elements == 0);
}

template< class Type >
inline unsigned long Array< Type >::firstIndex() const
{
  return (ARRAY_FIRST_INDEX);
}

template< class Type >
inline unsigned long Array< Type >::lastIndex() const
{
  return (num_elements);
}

template< class Type >
inline Type &Array< Type >::operator[]( unsigned long index )
{
  return (elementAtPosition( index ));
}

template< class Type >
inline const Type &Array< Type >::operator[]( unsigned long index ) const
{
  return (elementAtPosition( index ));
}

template< class Type >
inline Type &Array< Type >::elementAtPosition( unsigned long index ) const
{
  return (elements[index - ARRAY_FIRST_INDEX]);
}

template< class Type >
inline Array< Type > &Array< Type >::operator+=( const Array< Type > &array )
{
  return (append( array ));
}

template< class Type >
inline Array< Type > &Array< Type >::operator+=( const Type &element )
{
  return (append( element ));
}

template< class Type >
inline Array< Type > &Array< Type >::add( const Type &element )
{
  return (append( element ));
}

template< class Type >
inline Array< Type > *Array< Type >::extendToAsPtr( unsigned int newsize )
{
  return (&extendTo( newsize ));
}

template< class Type >
inline Array< Type > *Array< Type >::extendToAsPtr( unsigned int newsize,
    const Type &element )
{
  return (&extendTo( newsize, element ));
}

template< class Type >
inline void Array< Type >::clear()
{
  num_elements = 0;
}

template< class Type >
inline void Array< Type >::clear( boolean deallocate )
{
  if (deallocate)
    destroy();
  else
    clear();
}

#endif /* _ODE_LIB_PORTABLE_ARRAY_HPP_ */
