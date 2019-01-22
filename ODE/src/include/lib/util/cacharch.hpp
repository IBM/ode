/**
 * Archive utilities
**/
#ifndef _ODE_LIB_UTIL_CACHARCH_HPP_
#define _ODE_LIB_UTIL_CACHARCH_HPP_

#include <base/odebase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/string/smartstr.hpp"
#include "lib/util/cachamem.hpp"
#include "lib/portable/hashtabl.hpp"
#include "lib/io/path.hpp"

class Archive : public SmartCaseString
{
  public:

    Archive( const String &name ) : SmartCaseString( Path::unixize( name ) ),
        valid( false )
    {
      readMembers();
    };
    Archive( const Archive &archive ) :
        SmartCaseString( archive ), members( archive.members ),
        valid( archive.valid )
    {
    };
    ~Archive()
    {
      members.clear();
    }

    static void parse( const String &input, StringArray *result = 0 );
        // throw ParseException
    static String extractArchName( const String &name );
    static String extractMembName( const String &name );
    CachedArchMember *getMemb( const String &membname ) const;
    inline boolean isValid() const;
    void readMembers();


  private:

    Hashtable< String, CachedArchMember * >  members;
    boolean valid;
};

inline boolean Archive::isValid() const
{
  return (valid);
}

#endif //_ODE_LIB_UTIL_CACHARCH_HPP_
