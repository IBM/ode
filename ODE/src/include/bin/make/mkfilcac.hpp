/**
 *  MakefileCache
 *
**/
#ifndef _ODE_BIN_MAKE_MKFILCAC_HPP_
#define _ODE_BIN_MAKE_MKFILCAC_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/portable/hashtabl.hpp"

class Makefile;

class MakefileCache
{
  public:
    // constructors and destructor
    MakefileCache() {};
    ~MakefileCache();

    // access functions
    inline static Makefile *get( const SmartCaseString &mf );
    inline static void put( const SmartCaseString &mf, Makefile *data );

  private:
    static const int hashsize; // Initial hash table size
    static Hashtable< SmartCaseString, Makefile *> Makefiles;
};

inline Makefile *MakefileCache::get( const SmartCaseString &mf )
{
  Makefile **mfpptr = (Makefile **)Makefiles.get(mf);
  if (mfpptr ==0 || *mfpptr ==0) return 0;

  return *mfpptr;
}

inline void MakefileCache::put( const SmartCaseString &mf, Makefile *data)
{
  Makefiles.put(mf, data);
}

#endif //_ODE_BIN_MAKE_MKFILCAC_HPP_


