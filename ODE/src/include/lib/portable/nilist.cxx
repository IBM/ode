/**
 * ODETDList
 *
 * This file should be included by one .cpp file per
 * program/library, in which explicit instantiation(s)
 * should take place for all types of this template
 * used in that program/library.
**/
#define _ODE_LIB_PORTABLE_NILIST_CPP_
#include "lib/portable/nilist.hpp"

template< class T > 
ODETDList<T>::ODETDList( const ODETDList<T> &list ) :
    elementsEqual( list.elementsEqual )
{
  ListEnumeration< T > elems( &list );
  while (elems.hasMoreElements())
    this->appendElement( new ODEDTLink<T>( *elems.nextElement()) );
}

template< class T >
ODETDList< T > &ODETDList< T >::operator=( const ODETDList< T > &list )
{
  if (this == &list)
    return (*this);
  this->removeAll();
  ListEnumeration< T > elems( &list );
  while (elems.hasMoreElements())
    this->addAsLast( *elems.nextElement() );
  return (*this);
}

template< class T >
boolean ODETDList< T >::operator==( const ODETDList< T > &cmp ) const
{
  if (this == &cmp)
    return (true);
  if (this->size() != cmp.size())
    return (false);
  ODEDLink *ptr1 = head_, *ptr2 = cmp.head_;

  // the ifs are outside the while-loop for performance reasons
  // only.  this needs to be cleaned up more in the future.
  if (this->elementsEqual == 0)
  {
    while (ptr1 != 0) // lists are same size, so only need to check one
    {
      if (!(((ODEDTLink< T >*)ptr1)->info_ == ((ODEDTLink< T >*)ptr2)->info_))
        return (false);
      ptr1 = ptr1->next_;
      ptr2 = ptr2->next_;
    }
  }
  else
  {
    while (ptr1 != 0)
    {
      if (!this->elementsEqual( ((ODEDTLink< T >*)ptr1)->info_,
          ((ODEDTLink< T >*)ptr2)->info_ ))
        return (false);
      ptr1 = ptr1->next_;
      ptr2 = ptr2->next_;
    }
  }
  return (true);
}
