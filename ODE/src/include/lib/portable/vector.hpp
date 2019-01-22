/**
 * Vector
 *
**/
#ifndef _ODE_LIB_PORTABLE_VECTOR_HPP_
#define _ODE_LIB_PORTABLE_VECTOR_HPP_

/**
 *
 * HOW TO USE THE VectorEnumeration CLASS:
 *
 * Create a VectorEnumeration object in one of two ways:
 *
 * // CASE #1
 * Vector< MyClass > my_vector;
 * VectorEnumeration< MyClass > my_enum( &my_vector );
 *
 * // CASE #2
 * VectorEnumeration< MyClass > my_enum;
 * Vector< MyClass > my_vector;
 * my_enum.setObject( &my_vector );
 *
 * In either case, you should NEVER PASS A NULL POINTER to either the
 * constructor or setObject().  Furthermore, when using the default
 * constructor (case #2), you should NOT attempt to call either
 * hasMoreElements() or nextElement() before first calling setObject()
 * with a valid object pointer.
 *
 * The first case is for normal usage, where you already have the object
 * that you wish to enumerate.
 *
 * The second case is for situations where you do not yet have the
 * object you wish to enumerate (e.g., when you are enumerating inside
 * a loop for an array of objects, such as occurs in Hashtable::contains).
 *
 * The setObject function allows you to reset the enumeration to the
 * beginning of another (or the same) object without constructing a new
 * enumeration object.  Do not pass a null pointer.
 *
 * Then you just use hasMoreElements() and nextElement()
 * as with the standard [Java] Enumeration class.  It is unwise to
 * call nextElement() without calling hasMoreElements() first.
 *
 * while (my_enum.hasMoreElements())
 * {
 *   ptr = my_enum.nextElement();
 *   ...
 * }
 *
**/

#include <limits.h>
#include "lib/portable/nilist.hpp"
#include "lib/portable/collectn.hpp"

/**
 * A specialized array to mimic Java's Vector class.
 * See also the ODETDList class for other member functions.
**/
template< class Type >
class Vector : public ODETDList< Type >
{
  public:

    // constructors
    Vector( unsigned long size = 20,
        boolean (*equalCompareFunc)(
            const Type &elem1, const Type &elem2 ) = 0 ) :
        ODETDList< Type >( size, equalCompareFunc ) {};
    Vector( const Vector< Type > &vector ) : ODETDList< Type >( vector ) {};

    unsigned long copyInto( Type array[] ) const;
    inline boolean contains( const Type &element ) const;
    unsigned int indexOf( const Type &element,
        unsigned int index = ARRAY_FIRST_INDEX ) const;
    unsigned int lastIndexOf( const Type &element,
        unsigned int index = UINT_MAX ) const;
    inline const Type *elementAt( unsigned long index ) const;
    inline const Type *firstElement() const;
    inline const Type *lastElement() const;
    inline void insertElementAt( const Type &element, unsigned long index );
    inline void setElementAt( const Type &element, unsigned long index );
    inline void removeElementAt( unsigned long index );
    inline void addElement( const Type &element );
    inline void addWithoutDup( const Type &element );
    boolean removeElement( const Type &element );
    inline void removeAllElements();
};

template< class Type >
class VectorEnumeration : public ListEnumeration< Type >
{
  public:
    VectorEnumeration() :
        ListEnumeration< Type >() {};
    VectorEnumeration( const Vector< Type > *listptr ) :
        ListEnumeration< Type >( listptr ) {};
};

template< class Type >
inline const Type *Vector< Type >::elementAt( unsigned long index ) const
{
  return (&(this->elementAtPosition( index )));
}

template< class Type >
inline const Type *Vector< Type >::firstElement() const
{
  return (&(ODETDList< Type >::firstElement()));
}

template< class Type >
inline const Type *Vector< Type >::lastElement() const
{
  return (&(ODETDList< Type >::lastElement()));
}

template< class Type >
inline void Vector< Type >::insertElementAt( const Type &element,
    unsigned long index )
{
  addAtPosition( index, element );
}

template< class Type >
inline void Vector< Type >::addElement( const Type &element )
{
  addAsLast( element );
}

template< class Type >
inline void Vector< Type >::removeAllElements()
{
  this->removeAll();
}

template< class Type >
inline boolean Vector< Type >::contains( const Type &element ) const
{
  return (indexOf( element ) != ELEMENT_NOTFOUND);
}

template< class Type >
inline void Vector< Type >::setElementAt( const Type &element,
    unsigned long index )
{
  this->elementAtPosition( index ) = element;
}

template< class Type >
inline void Vector< Type >::removeElementAt( unsigned long index )
{
  this->removeAtPosition( index );
}

template< class Type >
inline void Vector< Type >::addWithoutDup( const Type &element )
{
  // No Duplicates, only add if element is not already in the vector.
  if ( !contains( element) )
    addAsLast( element );
}

#endif /* _ODE_LIB_PORTABLE_VECTOR_HPP_ */
