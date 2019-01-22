/**
 * Hash table related classes:
 *
 * Elementable (abstract)
 * Keyable (abstract)
 * Hashable (abstract)
 *
**/
#ifndef _ODE_LIB_PORTABLE_HASHABLE_HPP_
#define _ODE_LIB_PORTABLE_HASHABLE_HPP_

#include <base/odebase.hpp>

unsigned long ODEStringHashFunction( const char *s, unsigned long h );

#ifdef REFERENCE_TYPE_OVERLOADS
inline unsigned long ODEHashFunction( const char &e, unsigned long h )
#else
inline unsigned long ODEHashFunction( char e, unsigned long h )
#endif
{
  return (e % h);
}

#ifdef REFERENCE_TYPE_OVERLOADS
inline unsigned long ODEHashFunction( const int &e, unsigned long h )
#else
inline unsigned long ODEHashFunction( int e, unsigned long h )
#endif
{
  return (e % h);
}

#ifdef REFERENCE_TYPE_OVERLOADS
inline unsigned long ODEHashFunction( char *&s, unsigned long h )
#else
inline unsigned long ODEHashFunction( char *s, unsigned long h )
#endif
{
  return (ODEStringHashFunction( (const char*)s, h ));
}

#ifdef REFERENCE_TYPE_OVERLOADS
inline unsigned long ODEHashFunction( const char *&s, unsigned long h )
#else
inline unsigned long ODEHashFunction( const char *s, unsigned long h )
#endif
{
  return (ODEStringHashFunction( (const char*)s, h ));
}

#ifdef REFERENCE_TYPE_OVERLOADS
inline unsigned long ODEHashFunction( char *const &s, unsigned long h )
{
  return (ODEStringHashFunction( (const char*)s, h ));
}

inline unsigned long ODEHashFunction( const char *const &s, unsigned long h )
{
  return (ODEStringHashFunction( (const char*)s, h ));
}
#endif

/**
 * Derive from this abstract class to guarantee that a
 * class can be stored as an element in Hashtable.  Note that
 * the default and copy constructors must also be defined.
**/
template< class Element >
class Elementable
{
  public:
  
  // derived class must implement default & copy constructors
//  Element();
//  Element( const Element &element );

    // ensure proper destruction of derived classes
    virtual ~Elementable() {};

//    virtual Element &operator=( const Element &element ) = 0;
    virtual boolean operator==( const Element &element ) const = 0;
};


/**
 * Derive from this abstract class to guarantee that a
 * class can be used as a key in Hashtable.  Note that
 * the default and copy constructors must also be defined.
**/
template< class Key >
class Keyable
{
  public:
  
  // derived class must implement default & copy constructors
//  Key();
//  Key( const Key &element );

    // ensure proper destruction of derived classes
    virtual ~Keyable() {};

    virtual unsigned long ODEHashFunction( unsigned long hash_max ) const
    {
      return (::ODEHashFunction( this->toCharPtr(), hash_max ));
    };

    virtual char *toCharPtr() const
    {
      return ("");
    };

    virtual boolean operator==( const Key &key ) const = 0;
};


/**
 * Derive from this abstract class to guarantee that a
 * class can be used as both a key and an element in Hashtable.
 * Note that the default and copy constructors must also be defined.
 *
 * We only need/want to derive from Keyable, since it has everything
 * needed and we don't want to use multiple inheritance if we don't
 * have to (complicates operator= "if (this==&rhs)" equality checks).
**/
template< class Element >
class Hashable : public Keyable< Element >
//, public Elementable< Element >
{
};

/**
 * The hash function template.  The default implementation
 * is to call object.ODEHashFunction().  The user can either
 * implement ODEHashFunction() as a member function of the Key
 * class, create an explicit global hash function with the
 * specific class type as the first parameter, or implement
 * the char* operator and allow the default ODEHashFunction
 * in Keyable be used.
**/
template< class Type >
inline unsigned long ODEHashFunction( const Type &object,
    unsigned long hash_max )
{
  return (object.ODEHashFunction( hash_max ));
}

#endif /* _ODE_LIB_PORTABLE_HASHABLE_HPP_ */
