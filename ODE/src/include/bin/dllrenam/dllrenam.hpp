#ifndef _ODE_BIN_DLLRENAM_DLLRENAM_HPP_
#define _ODE_BIN_DLLRENAM_DLLRENAM_HPP_

#include <base/odebase.hpp>
#include <base/binbase.hpp>
#include "lib/string/string.hpp"
#include "lib/string/strarray.hpp"
#include "lib/io/cmdline.hpp"
#include "lib/io/path.hpp"
#include "lib/portable/array.hpp"


class DLLRename : public Tool
{
  public:

    static String program_name;

    DLLRename( const char **argv, const char **envp ) :
        Tool( envp ), args( argv ), envs( envp ), cmdline( 0 ) {}

    inline ~DLLRename()
    {
      delete cmdline;
    }

    static int main( const char **argv, const char **envp );
    boolean run();
    void printUsage() const;


  private:

    const char **args;
    const char **envs;
    CommandLine *cmdline;

    void checkCommandLine();
    boolean verifyInFile( const String &file );
    boolean verifyOutDir( const String &dir );
    boolean changeNames( const String &oldname, const String &newname,
        const String &olddir = Path::getcwd(),
        const String &newdir = Path::getcwd() );
    boolean changeName( const String &oldname, const String &newname,
        const String &oldpath, const String &newpath );
    int nextChar( ifstream *ifptr, Array< int > &buf );
};

#endif // _ODE_BIN_DLLRENAM_DLLRENAM_HPP_
