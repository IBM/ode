#ifndef _ODE_LIB_PORTABLE_NILIST_HPP_
#define _ODE_LIB_PORTABLE_NILIST_HPP_

#include "lib/portable/ilist.hpp"

/**
 *
 * HOW TO USE THE ListEnumeration CLASS:
 *
 * Create a ListEnumeration object in one of two ways:
 *
 * // CASE #1
 * ODETDList< MyClass > my_list;
 * ListEnumeration< MyClass > my_enum( &my_list );
 *
 * // CASE #2
 * ListEnumeration< MyClass > my_enum;
 * ODETDList< MyClass > my_list;
 * my_enum.setObject( &my_list );
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


template< class T >
class ODEDTLink : public ODEDLink
{
  public:

    inline ODEDTLink( const T &element ) : info_( element ) {};
    T info_;
};

template< class T > class ListEnumeration;

/* *********
*
*  ODETDList<T> This is a templatized doubly-linked list.
*
* **********/
template< class T >
class ODETDList : public ODEDListBase
{
  friend class ListEnumeration< T >;

  public:

    //constructors and destructor.
    inline ODETDList() {}
    inline ODETDList( unsigned int size,
        boolean (*equalCompareFunc)( const T &elem1, const T &elem2 ) = 0 ) :
        elementsEqual( equalCompareFunc ) {}
    ODETDList( const ODETDList<T>& list );

    //Insertion
    inline void addAsLast( const T &elem );
    inline void addAsFirst( const T &elem );
    inline void addAtPosition( unsigned long i, const T &elem );

    //Searching Elements from the list
    inline T& elementAtPosition(unsigned long i) const;
    inline T& firstElement() const;
    inline T& lastElement() const;

    //Indexing Elements
    inline T& operator[]( unsigned long i );
    inline const T& operator[]( unsigned long index ) const;

    ODETDList< T > &operator=( const ODETDList< T > &list );
    boolean operator==( const ODETDList< T > &cmp ) const;


  protected:

    boolean (*elementsEqual)( const T &elem1, const T &elem2 );
};

template< class T > 
inline T& ODETDList< T >::elementAtPosition(unsigned long i) const
{
  return ((ODEDTLink<T>*)ODEDListBase::findPositionAt(i))->info_;
}

template< class T > 
inline T& ODETDList< T >::firstElement() const
{
  return elementAtPosition( ARRAY_FIRST_INDEX );
}

template< class T > 
inline T& ODETDList< T >::lastElement() const
{
  return elementAtPosition( this->lastIndex() );
}

template< class T > 
inline void ODETDList< T >::addAsLast(const T& val)
{
  ODEDListBase::appendElement( new ODEDTLink<T>(val) );
}

template< class T > 
inline void ODETDList< T >::addAsFirst(const T& val)
{
  ODEDListBase::prependElement( new ODEDTLink<T>(val) );
}

template< class T > 
inline void ODETDList< T >::addAtPosition(unsigned long i, const T& val)
{
  ODEDListBase::insertAtPosition( i, new ODEDTLink<T>(val) );
}

template< class T > 
inline T& ODETDList< T >::operator[](unsigned long i)  
{
  return elementAtPosition(i);
}

template< class T > 
inline const T& ODETDList< T >::operator[](unsigned long i) const
{
  return elementAtPosition(i);
}


template< class T >
class ListEnumeration
{
  public:
      
    inline ListEnumeration() :
        listptr( 0 ), nodeptr( 0 ) {}
    inline ListEnumeration( const ODETDList< T > *listptr ) :
        listptr( listptr ), nodeptr( listptr->head_ ) {}
            
    inline void setObject( const ODETDList< T > *listptr );
    inline const T *nextElement();
    inline boolean hasMoreElements();


  private:
      
    const ODETDList< T > *listptr;
    const ODEDLink *nodeptr;
};

template< class T >
inline void ListEnumeration< T >::setObject( const ODETDList< T > *listptr )
{
  this->listptr = listptr;
  nodeptr = listptr->head_;
}

template< class T >
inline const T *ListEnumeration< T >::nextElement()
{
  const T *rc = &(((ODEDTLink< T >*)nodeptr)->info_);
  nodeptr = nodeptr->next_;
  return (rc);
}

template< class T >
inline boolean ListEnumeration< T >::hasMoreElements()
{
  return (nodeptr != 0);
}

#endif // _ODE_LIB_PORTABLE_NILIST_HPP_
