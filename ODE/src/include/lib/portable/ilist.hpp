#ifndef  _ODE_LIB_PORTABLE_ILIST_HPP_
#define  _ODE_LIB_PORTABLE_ILIST_HPP_

#include <base/odebase.hpp>

// WARNING:
// if ARRAY_FIRST_INDEX (in odebase.hpp) changes, you MUST modify
// lastIndex() as well!!!


/**
 * Basic node.  Element information is stored in the
 * derived class ODEDTLink (see nilist.hpp).
 *
**/
struct ODEDLink
{
  inline virtual ~ODEDLink() {}
  ODEDLink *next_;  
  ODEDLink *prev_;  
};


/**
 * Base class for a list.  Contains the non-template functions
 * and data needed to traverse, add to, and delete from a list.
 *
**/
class ODEDListBase
{
  public:

    //Constructor and Destructor
    inline ODEDListBase();
    inline virtual ~ODEDListBase();

    //clearing the list.
    void removeAll();
 
    //Removing from the List
    inline void removeAtPosition( unsigned long i );

    //Miscellanious
    inline unsigned long firstIndex() const;
    inline unsigned long lastIndex() const;
    inline unsigned long length() const;
    inline unsigned long size() const;
    inline boolean       isEmpty() const;


  protected:

    //Data members.
    ODEDLink *head_;          
    ODEDLink *tail_;          

    //Number of items
    unsigned long nitems_;        

    //Adding to the List
    inline void appendElement(ODEDLink* a)  ;
    inline void prependElement(ODEDLink* a);
    inline void insertAtPosition(unsigned long, ODEDLink*);

    //Searching Elements from the list
    ODEDLink *findPositionAt(unsigned long i) const;     


  private:

    inline void init();
    boolean removeElement( ODEDLink *a );
    boolean insertBeforeElement( ODEDLink *existing_element, ODEDLink* a );
};


inline ODEDListBase::ODEDListBase()
{
  init(); 
}

inline ODEDListBase::~ODEDListBase()
{
  removeAll();
}

inline void ODEDListBase::init()
{
  nitems_ = 0;
  head_ = tail_ = 0;
}

inline unsigned long ODEDListBase::firstIndex() const
{
  return ARRAY_FIRST_INDEX;
}

/**
 * Correct formula is:
 *   ARRAY_FIRST_INDEX + nitems_ - 1
 * However, since ARRAY_FIRST_INDEX will likely never change
 * (the number used is arbitrary), we don't need to perform
 * calculations.
**/
inline unsigned long ODEDListBase::lastIndex() const
{
  return nitems_;
}

inline unsigned long ODEDListBase::length() const
{
  return nitems_; 
}

inline unsigned long ODEDListBase::size() const
{
  return nitems_; 
}

inline void ODEDListBase::prependElement(ODEDLink* a)
{
  insertAtPosition( firstIndex(), a );
}

inline void ODEDListBase::appendElement(ODEDLink* a)
{
  insertAtPosition( lastIndex() + 1, a );
}

inline boolean ODEDListBase::isEmpty() const
{
  return nitems_==0;
} 

inline void ODEDListBase::insertAtPosition( unsigned long i, ODEDLink *a )
{
  insertBeforeElement( findPositionAt( i ), a );
}

inline void ODEDListBase::removeAtPosition( unsigned long i )
{
  removeElement( findPositionAt( i ) );
}

#endif // _ODE_LIB_PORTABLE_ILIST_HPP_
