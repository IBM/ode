/**
 * Stack
 *
**/
#ifndef _ODE_LIB_PORTABLE_STACK_HPP_
#define _ODE_LIB_PORTABLE_STACK_HPP_

/**
 *
 * HOW TO USE THE StackEnumeration CLASS:
 *
 * Create a StackEnumeration object in one of two ways:
 *
 * // CASE #1
 * Stack< MyClass > my_stack;
 * StackEnumeration< MyClass > my_enum( &my_stack );
 *
 * // CASE #2
 * StackEnumeration< MyClass > my_enum;
 * Stack< MyClass > my_stack;
 * my_enum.setObject( &my_stack );
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

#include "lib/portable/nilist.hpp"
#include "lib/portable/collectn.hpp"


/**
 * A specialized array to mimic Java's Stack class.
 * See also the ODETDList class for other member functions.
 * Note that the top of the stack is the end of the array,
 * so popping occurs from end.
**/
template< class Type >
class Stack : public ODETDList< Type >
{
  public:

    // constructors
    Stack( unsigned long size = 50,
        boolean (*equalCompareFunc)(
            const Type &elem1, const Type &elem2 ) = 0 ) :
        ODETDList< Type >( size, equalCompareFunc ) {};
    Stack( const Stack< Type > &stack ) : ODETDList< Type >( stack ) {};

    inline boolean empty() const;
    inline const Type *peek() const;
    Type *pop(); // user must deallocate returned pointer
    const Type &push( const Type &element );
};

template< class Type >
inline boolean Stack< Type >::empty() const
{
  return (this->isEmpty());
}

template< class Type >
inline const Type *Stack< Type >::peek() const
{
  return (&(this->elementAtPosition( this->lastIndex() )));
}

template< class Type >
class StackEnumeration : public ListEnumeration< Type >
{
  public:
      
    StackEnumeration( const Stack< Type > *listptr ) :
        ListEnumeration< Type >( listptr ) {};
};            

#endif /* _ODE_LIB_PORTABLE_STACK_HPP_ */
