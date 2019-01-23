/**
 * MakefileCache
 **/
using namespace std;
#define _ODE_BIN_MAKE_MKFILCAC_CPP_

#include <base/binbase.hpp>
#include "bin/make/mkfilcac.hpp"
#include "bin/make/makefile.hpp"

const int MakefileCache::hashsize = 10;
Hashtable< SmartCaseString, Makefile* > MakefileCache::Makefiles( hashsize );

MakefileCache::~MakefileCache()
{
  HashElementEnumeration< SmartCaseString, Makefile* >
      enumer( &Makefiles );
  Makefile * const *mf;
  while (enumer.hasMoreElements())
  {
    mf = enumer.nextElement();
    if (mf != 0)
      delete *mf;
  }
}
