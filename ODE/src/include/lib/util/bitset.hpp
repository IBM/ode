/**
 * BitSet
**/

#ifndef _ODE_LIB_UTIL_BITSET_HPP_
#define _ODE_LIB_UTIL_BITSET_HPP_

#include <string.h>

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/portable/hashable.hpp"

class BitSet
{
  public:

    BitSet() : nbits( normalize( 0 ) ),
        bits( newBits( nbits ) ) {};
    BitSet( int nbits ) : nbits( normalize( nbits ) )
    {
      bits = newBits( this->nbits );
    };
    BitSet( const BitSet &copy ) : nbits( copy.nbits ),
        bits( newBits( nbits ) )
    {
      strcpy( bits, copy.bits );
    };
    ~BitSet()
    {
      delete[] bits;
      bits = 0;
    };

    inline unsigned long ODEHashFunction( unsigned long hash_max ) const;
    BitSet &operator=( const BitSet &set );
    inline boolean operator==( const BitSet &set ) const;
    inline char *toCharPtr() const;

    inline int firstIndex() const;
    inline int lastIndex() const;
    inline String toString() const;
    boolean equals( const BitSet &set ) const;
    inline BitSet *clone() const; // user must deallocate returned pointer
    inline unsigned int size() const;
    void resize( unsigned int nbits, boolean save_bits = true );

    void And( const BitSet &set );
    inline void clear( unsigned int bit );
    inline void clearAll();
    inline boolean get( unsigned int bit ) const;
    void Or( const BitSet &set );
    inline void set( unsigned int bit );
    inline void setAll();
    void Not( unsigned int bit );
    void Xor( const BitSet &set );

  private:

    static const char ODEDLLPORT SET_BIT;
    static const char ODEDLLPORT CLEAR_BIT;
    static const int ODEDLLPORT INCREMENT_SIZE;
    int nbits;
    char *bits;

    inline int normalize( int nbits ) const;
    char *newBits( unsigned int normalized_bits ) const;
};

/**
 * Convert x to the next highest multiple of the
 * INCREMENT_SIZE (64).  This ensures our bit sizes
 * are always 64, 128, 192, 256, etc.
**/
inline int BitSet::normalize( int nbits ) const
{
  return (nbits + (INCREMENT_SIZE -
      ((nbits - 1) % INCREMENT_SIZE)) - 1);
}

/**
 * The index of the first (leftmost) bit in the set.
 * This is backwards from a typical bit representation
 * (in which bit zero is the rightmost), but it simplifies
 * operations on the set.
**/
inline int BitSet::firstIndex() const
{
  return (0);
}

/**
 * The index of the last (rightmost) bit in the set.
 * This is backwards from a typical bit representation
 * (in which bit N is the leftmost), but it simplifies
 * operations on the set.
**/
inline int BitSet::lastIndex() const
{
  return (nbits - 1);
}

/**
 * Returns the size (in bits) of the set.  This will
 * always be an even multiple of INCREMENT_SIZE (64).
**/
inline unsigned int BitSet::size() const
{
  return (nbits);
}

/**
 * Returns the bit set as a String object.  This
 * is a string of zeros and ones.
**/
inline String BitSet::toString() const
{
  return (bits);
}

/**
 * Create a new bit set.  Just calls the copy
 * constructor.  User must deallocate the returned
 * pointer when finished using it.
**/
inline BitSet *BitSet::clone() const
{
  return (new BitSet( *this ));
}

/**
 * See the equals() function.
**/
inline boolean BitSet::operator==( const BitSet &set ) const
{
  return (equals( set ));
}

/**
 * Returns the bit set as a character array.  This
 * returns a pointer to the internal bit representation
 * (which *is* null terminated for convenience), so do
 * not modify the contents unless you wish to alter the
 * set which it came from.
**/
inline char *BitSet::toCharPtr() const
{
  return (bits);
}

inline unsigned long BitSet::ODEHashFunction( unsigned long hash_max ) const
{
  return (::ODEHashFunction( bits, hash_max ));
}

/**
 * Returns the setting of the specified bit.
**/
inline boolean BitSet::get( unsigned int bit ) const
{
  return (bits[bit] == SET_BIT);
}

/**
 * Turn all the bits off.
**/
inline void BitSet::clearAll()
{
  memset( bits, CLEAR_BIT, nbits );
}

/**
 * Turn the specified bit on.
**/
inline void BitSet::clear( unsigned int bit )
{
  bits[bit] = CLEAR_BIT;
}

/**
 * Turn the specified bit on.
**/
inline void BitSet::set( unsigned int bit )
{
  bits[bit] = SET_BIT;
}

/**
 * Turn all the bits on.
**/
inline void BitSet::setAll()
{
  memset( bits, SET_BIT, nbits );
}

#endif /* _ODE_LIB_UTIL_BITSET_HPP_ */
