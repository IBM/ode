/**
 * Hash functions
 *
**/
#define _ODE_LIB_PORTABLE_HASHABLE_CPP_
#include "lib/portable/hashable.hpp"

/**
 * Simple/fast (5n + 3) additive hash function.
**/
unsigned long ODEStringHashFunction( const char *s, unsigned long h )
{
  unsigned long val = 0;

  while (*s != '\0')
    val += *(s++);

  return (val % h);
}
