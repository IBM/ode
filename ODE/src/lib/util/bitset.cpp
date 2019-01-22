/**
 * BitSet
**/

#define _ODE_LIB_UTIL_BITSET_CPP_
#include "lib/util/bitset.hpp"

const char BitSet::SET_BIT = '1';
const char BitSet::CLEAR_BIT = '0';
const int BitSet::INCREMENT_SIZE = 64;


/**
 * Assignment just means resizing this object and
 * then duplicating the bit string.
**/
BitSet &BitSet::operator=( const BitSet &set )
{
  if (this != &set)
  {
    resize( set.nbits, false );
    strcpy( this->bits, set.bits );
  }
  return (*this);
}

/**
 * Returns a newly allocated string of bits of the
 * specified size (which must have already been
 * normalized with the normalize() function).
**/
char *BitSet::newBits( unsigned int normalized_bits ) const
{
  char *rc = new char[normalized_bits + 1];
  memset( rc, CLEAR_BIT, normalized_bits );
  rc[normalized_bits] = '\0'; // makes operator char* easy to implement
  return (rc);
}

/**
 * Change the size of the bits to the normalized
 * value of the nbits parameter.
 *
 * If save_bits is true, the bits from the original
 * will be preserved after resizing (up to the new
 * size).  If save_bits is false, the new bits set
 * will have all bits cleared.
**/
void BitSet::resize( unsigned int nbits, boolean save_bits )
{
  nbits = normalize( nbits );
  if (this->nbits != nbits)
  {
    char *newbits = newBits( nbits );
    if (save_bits)
    {
      int copysize = (this->nbits > nbits) ? nbits : this->nbits;
      strncpy( newbits, bits, copysize );
    }

    delete[] bits;
    bits = newbits;
    this->nbits = nbits;
  }
}

/**
 * Equality means each bit being set the same in
 * each set.  If one set contains more bits, that
 * set must have all its "extra" bits set to zero
 * for equality to be true.
**/
boolean BitSet::equals( const BitSet &set ) const
{
  const char *clear_cmp = 0;
  int from_clear = 0, to_clear = -1; // to_clear should be < from_clear
  int cmp_length;
  
  if (this == &set)
    return (true);
  if (nbits < set.nbits)
  {
    from_clear = lastIndex();
    to_clear = set.lastIndex();
    clear_cmp = set.bits;
    cmp_length = nbits;
  }
  else if (nbits > set.nbits)
  {
    from_clear = set.lastIndex();
    to_clear = lastIndex();
    clear_cmp = bits;
    cmp_length = set.nbits;
  }
  else
    cmp_length = nbits;
  
  for (int i = from_clear; i <= to_clear; ++i)
    if (clear_cmp[i] != CLEAR_BIT)
      return (false);
  return (strncmp( bits, set.bits, cmp_length ) == 0);
}

/**
 * Performs a bitwise AND with the bits in the
 * 'set' parameter (widening this set if
 * necessary).
**/
void BitSet::And( const BitSet &set )
{
  int i;

  if (nbits < set.nbits)
    resize( set.nbits );
  for (i = set.firstIndex(); i <= set.lastIndex(); ++i)
    if (set.bits[i] == CLEAR_BIT)
      bits[i] = CLEAR_BIT;
  for (int j = i; j <= lastIndex(); ++j)
    bits[j] = CLEAR_BIT;
}

/**
 * Performs a bitwise OR with the bits in the
 * 'set' parameter (widening this set if
 * necessary).
**/
void BitSet::Or( const BitSet &set )
{
  if (this == &set)
    return;
  if (nbits < set.nbits)
    resize( set.nbits );
  for (int i = set.firstIndex(); i <= set.lastIndex(); ++i)
    if (set.bits[i] == SET_BIT)
      bits[i] = SET_BIT;
}

/**
 * Reverse the settings of each bit.
**/
void BitSet::Not( unsigned int bit )
{
  if (bit < firstIndex())
    return;
  if (bit > lastIndex())
    resize( bit - firstIndex() + 1 );
  if (bits[bit] == SET_BIT)
    bits[bit] = CLEAR_BIT;
  else
    bits[bit] = SET_BIT;
}

/**
 * Performs a bitwise XOR with the bits in the
 * 'set' parameter (widening this set if
 * necessary).
**/
void BitSet::Xor( const BitSet &set )
{
  if (this == &set)
    clearAll();
  else
  {   
    if (nbits < set.nbits)
      resize( set.nbits );
    for (int i = set.firstIndex(); i <= set.lastIndex(); ++i)
      if (set.bits[i] == SET_BIT)
        Not( i );
  }
}
