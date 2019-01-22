/**
 * Hashtable
 *
**/
#ifndef _ODE_LIB_PORTABLE_HASHTABL_HPP_
#define _ODE_LIB_PORTABLE_HASHTABL_HPP_

/**
 *
 * HOW TO USE THE HashKeyEnumeration AND HashElementEnumeration CLASSES:
 *
 * The following examples use a HashKeyEnumeration object, but the
 * use of a HashElementEnumeration is identical.
 *
 * Create a HashKeyEnumeration object in one of two ways:
 *
 * // CASE #1
 * Hashtable< MyKeyClass, MyElementClass > my_hash_table;
 * HashKeyEnumeration< MyKeyClass, MyElementClass >
 *     my_enum( &my_hash_table );
 *
 * // CASE #2
 * HashKeyEnumeration< MyKeyClass, MyElementClass > my_enum;
 * Hashtable< MyKeyClass, MyElementClass > my_hash_table;
 * my_enum.setObject( &my_hash_table );
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

#include <base/odebase.hpp>
#include "lib/portable/hashable.hpp"
#include "lib/portable/nilist.hpp"

// Default initial capacity for Hashtable.
// A reasonably sized prime number.
#define DEFAULT_HASH_SIZE 31

template< class Key, class Element >
struct HashStored
{
  inline HashStored( const Key &k, const Element &e ) :
      key( k ), element( e ) {}
  inline boolean operator==( const HashStored< Key, Element > &cmp ) const;
  Key key;
  Element element;
};

template< class Key, class Element >
inline boolean HashStored< Key, Element >::operator==(
    const HashStored< Key, Element > &cmp ) const
{
  return (key == cmp.key && element == cmp.element);
}

// declaration so Hashtable can make HashEnumBase a friend
template< class Key, class Element > class HashEnumBase;

/**
 * The Hashtable class.
 *
 * Note that max_size (in the constructor) is the maximum
 * table size, but the actual number of elements stored is
 * infinitely growable.  Choose the max size carefully, as
 * too small a number will result in inefficient retrieval
 * (as more and more elements hash to the same array index),
 * but too large a number will waste memory.
 *
 * There are two ways for the user to specify a different
 * hash function (for int and char*, there are explicit
 * functions provided; for class objects, the default
 * is to call its ODEHashFunction, which the default
 * action is to use the char* operator to call that
 * version of the provided hash function).
 *   1. (preferred) override the ODEHashFunction() member
 *      function in class Keyable.
 *   2. Implement a global ODEHashFunction function which
 *      overrides the template version.
 *
 * RESTRICTIONS: the Key and Element types must either
 * derive from Keyable and Elementable, respectively,
 * or ensure that the functions declared therein are
 * defined.
 *
**/
template< class Key, class Element >
class Hashtable
{
  friend class HashEnumBase< Key, Element >;

  public:

    Hashtable( unsigned long max_size = DEFAULT_HASH_SIZE );
    Hashtable( const Hashtable< Key, Element > &copy );
    ~Hashtable();

    Hashtable< Key, Element > &operator=(
        const Hashtable< Key, Element > &copy );

    boolean put( const Key &key, const Element &element );
    const Element *get( const Key &key ) const;
    boolean contains( const Element &element ) const;
    inline boolean containsKey( const Key &key ) const;
    unsigned long size() const;
    boolean remove( const Key &key );
    inline boolean isEmpty() const;
    void clear();


  private:

    // Forbid equality operator.  Do not implement (too expensive).
    inline boolean operator==( const Hashtable< Key, Element > &element ) const;

    void destroy();

    typedef ODETDList< HashStored< Key, Element >* >* TableEntryType;
    TableEntryType *table;
    unsigned long hash_max;
};


template< class Key, class Element >
inline boolean Hashtable< Key, Element >::containsKey( const Key &key ) const
{
  return (get( key ) != 0);
}

template< class Key, class Element >
inline boolean Hashtable< Key, Element >::isEmpty() const
{
  return (size() < 1);
}

template< class Key, class Element >
inline boolean Hashtable< Key, Element >::operator==( const Hashtable< Key, Element > &element ) const
{
  return (table == element.table);
}

/**
 * INTERNAL USE
 *
 * This is what HashKeyEnumeration and HashElementEnumeration
 * derive from.  It doesn't have much use by itself to other
 * classes.
**/
template< class Key, class Element >
class HashEnumBase
{
  public:

    inline HashEnumBase() :
        table( 0 ), x( 0 ) {}
    inline HashEnumBase( const Hashtable< Key, Element > *table );

    void setObject( const Hashtable< Key, Element > *table );
    boolean hasMoreElements();


  protected:

    const Hashtable< Key, Element > *table;
    unsigned long x; // x-index into the table (row #)
    // NOTE: the y-index (column #) is represented by the
    // range "table[i].firstIndex() to table[i].lastIndex()"
    ListEnumeration< HashStored< Key, Element >* > elems;
};

template< class Key, class Element >
inline HashEnumBase< Key, Element >::HashEnumBase(
    const Hashtable< Key, Element > *table ) :
    table( table ), x( 0 )
{
  setObject( table );
}

template< class Key, class Element >
class HashKeyEnumeration : public HashEnumBase< Key, Element >
{
  public:

    inline HashKeyEnumeration() {}
    inline HashKeyEnumeration( const Hashtable< Key, Element > *table ) :
        HashEnumBase< Key, Element >( table ) {}

    inline const Key *nextElement()
    {
      return &((*(this->elems.nextElement()))->key);
    }
};

template< class Key, class Element >
class HashElementEnumeration : public HashEnumBase< Key, Element >
{
  public:

    inline HashElementEnumeration() {}
    inline HashElementEnumeration( const Hashtable< Key, Element > *table ) :
        HashEnumBase< Key, Element >( table ) {}

    inline const Element *nextElement()
    {
      return &((*(this->elems.nextElement()))->element);
    }
};

#endif /* _ODE_LIB_PORTABLE_HASHTABL_HPP_ */
