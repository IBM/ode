/**
 * Hashtable
 *
 * This file should be included by one .cpp file per
 * program/library, in which explicit instantiation(s)
 * should take place for all types of this template
 * used in that program/library.
**/
#define _ODE_LIB_PORTABLE_HASHTABL_CPP_
#include "lib/portable/hashtabl.hpp"


/**
 * Default constructor
**/
template< class Key, class Element >
Hashtable< Key, Element >::Hashtable( unsigned long max_size ) :
    hash_max( max_size )
{
  table = new TableEntryType[hash_max];
  for (int i = 0; i < hash_max; ++i)
    table[i] = 0;
}

/**
 * Copy constructor
**/
template< class Key, class Element >
Hashtable< Key, Element >::Hashtable( const Hashtable< Key, Element > &copy ) :
    table( 0 ), hash_max( 0 )
{
  operator=( copy ); // does the real work
}

template< class Key, class Element >
Hashtable< Key, Element >::~Hashtable()
{
  destroy();
}

template< class Key, class Element >
void Hashtable< Key, Element >::destroy()
{
  clear();
  delete[] table;
}

template< class Key, class Element >
void Hashtable< Key, Element >::clear()
{
  ListEnumeration< HashStored< Key, Element >* > elems;
  const HashStored< Key, Element > *elem;

  for (int i = 0; i < hash_max; ++i)
  {
    if (table[i] != 0)
    {
      elems.setObject( table[i] );
      while (elems.hasMoreElements())
      {
        elem = *(elems.nextElement());
        delete (HashStored< Key, Element > *)elem;
      }
      delete table[i]; // calls list destructor
      table[i] = 0;
    }
  }
}

template< class Key, class Element >
Hashtable< Key, Element > &Hashtable< Key, Element >::operator=(
    const Hashtable< Key, Element > &copy )
{
  ListEnumeration< HashStored< Key, Element >* > elems;
  const HashStored< Key, Element > *elem;
  destroy();
  hash_max = copy.hash_max;
  table = new TableEntryType[hash_max];
  for (int i = 0; i < hash_max; ++i)
  {
    if (copy.table[i] == 0)
      table[i] = 0;
    else
    {
      table[i] = new ODETDList< HashStored< Key, Element >* >;
      elems.setObject( copy.table[i] );
      while (elems.hasMoreElements())
      {
        elem = *(elems.nextElement());
        table[i]->addAsLast( new HashStored< Key, Element >(
            elem->key, elem->element ) );
      }
    }
  }
  return (*this);
}

template< class Key, class Element >
boolean Hashtable< Key, Element >::put( const Key &key,
    const Element &element )
{
  unsigned long hash_val = ODEHashFunction( key, hash_max );

  if (table[hash_val] == 0)
    table[hash_val] = new ODETDList< HashStored< Key, Element >* >;
  else
  {
    ListEnumeration< HashStored< Key, Element >* > elems( table[hash_val] );
    const HashStored< Key, Element > *elem;
    while (elems.hasMoreElements())
    {
      elem = *(elems.nextElement());
      if (elem->key == key)
      {
        ((HashStored< Key, Element > *)elem)->element = element;
        return (false); // replacement causes false return value
      }
    }
  }

  // key not already here, so add it
  table[hash_val]->addAsLast( new HashStored< Key, Element >( key, element ) );
  return (true);
}

/**
 * Returns null if element wasn't found.
**/
template< class Key, class Element >
const Element *Hashtable< Key, Element >::get( const Key &key ) const
{
  unsigned long hash_val = ODEHashFunction( key, hash_max );
  if (table[hash_val] != 0)
  {
    ListEnumeration< HashStored< Key, Element >* > elems( table[hash_val] );
    const HashStored< Key, Element > *elem;
    while (elems.hasMoreElements())
    {
      elem = *(elems.nextElement());
      if (elem->key == key)
        return (&(elem->element));
    }
  }
  return (0);
}

/**
 * See if an element exists, regardless of the key value.
 *
 * VERY expensive!
**/
template< class Key, class Element >
boolean Hashtable< Key, Element >::contains( const Element &element ) const
{
  const HashStored< Key, Element > *elem;
  ListEnumeration< HashStored< Key, Element >* > elems;
  for (int i = 0; i < hash_max; ++i)
  {
    if (table[i] == 0)
      continue;
    elems.setObject( table[i] );
    while (elems.hasMoreElements())
    {
      elem = *(elems.nextElement());
      if (elem->element == element)
        return (true);
    }
  }
  return (false);
}
    
template< class Key, class Element >
unsigned long Hashtable< Key, Element >::size() const
{
  unsigned long count = 0;

  for (int i = 0; i < hash_max; ++i)
    if (table[i] != 0)
      count += table[i]->size();
  return (count);
}

template< class Key, class Element >
boolean Hashtable< Key, Element >::remove( const Key &key )
{
  unsigned long hash_val = ODEHashFunction( key, hash_max );
  if (table[hash_val] == 0)
    return (false);
  ListEnumeration< HashStored< Key, Element >* > elems( table[hash_val] );
  const HashStored< Key, Element > *elem;
  int i = ARRAY_FIRST_INDEX;
  while (elems.hasMoreElements())
  {
    elem = *(elems.nextElement());
    if (elem->key == key)
    {
      delete (HashStored< Key, Element > *)elem;
      table[hash_val]->removeAtPosition( i );
      return (true);
    }
    ++i;
  }
  return (false);
}

template< class Key, class Element >
void HashEnumBase< Key, Element >::setObject(
    const Hashtable< Key, Element > *table )
{
  this->table = table;
  for (x = 0; x < table->hash_max; ++x)
  {
    if (table->table[x] != 0 && table->table[x]->size() > 0)
    {
      elems.setObject( table->table[x] );
      return;
    }
  }
}

template< class Key, class Element >
boolean HashEnumBase< Key, Element >::hasMoreElements()
{
  while (x < table->hash_max && !elems.hasMoreElements())
  {
    if (++x < table->hash_max)
    {
      if (table->table[x] != 0 && table->table[x]->size() > 0)
        elems.setObject( table->table[x] );
    }
  }
  return (x < table->hash_max);
}
